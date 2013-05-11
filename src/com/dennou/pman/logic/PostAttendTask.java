package com.dennou.pman.logic;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dennou.pman.data.Seat;
import com.dennou.pman.data.Seminar;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.data.Venue;
import com.dennou.pman.data.VenueDB;

public class PostAttendTask extends AsyncTask<String, Void, Boolean> {
	private final static String TAG = "LoadSeatTask";
	private Context context;
	private int statusCode;
	private int code;
	private String sign;
	private String secret;
	
	public PostAttendTask(Context context, int code, String sign, String secret) {
		this.context = context;
		this.code = code;
		this.sign = sign;
		this.secret = secret;
	}
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			String uri = Var.getUri(Var.ATTEND_API_URI, context);
			
			JSONObject json = new JSONObject();
			json.put("code", code);
			json.put("sign", sign);
			json.put("secret", secret);
			
			StringEntity entity = new StringEntity(json.toString());
			entity.setContentType("application/json");
			entity.setChunked(false);
			
			Log.d(TAG, "url=" + uri);
			HttpPost post = new HttpPost(uri);
			String bearer = String.format(Var.HEADER_BEARER, TempData.getInstance(context).getAuthToken());
			post.setHeader(Var.HEADER_AUTHORIZATION, bearer);
			post.setEntity(entity);
			Log.d(TAG, String.format("code=%s sign=%s secret=%s", code, sign, secret));
			
			HttpResponse response = client.execute( post );
			statusCode = response.getStatusLine().getStatusCode();
			Log.d(TAG, "status="+statusCode);
			
			if ( statusCode != HttpStatus.SC_OK )
				return Boolean.FALSE;
			
	        //Venue
	        String body = EntityUtils.toString(response.getEntity(), Var.CHARSET);
	        json = new JSONObject(body);
	        
	        JSONObject venueObj = json.getJSONObject("venue");
			Venue venue = new Venue();
			venue.setName(venueObj.getString("name"));
			
			//Seat
			Seat seat = new Seat();
			if(json.has("seat")){
				JSONObject seatObj = json.getJSONObject("seat");
				seat.setName(seatObj.getString("name"));
			}
			
			//Seminar
			JSONObject seminarObj = json.getJSONObject("seminar");
			Seminar seminar = new Seminar();
			seminar.setId(seminarObj.getInt("id"));
			seminar.setName(seminarObj.getString("name"));
			seminar.setDescription(seminarObj.getString("description"));
			seminar.setStartedAt(Var.sdf.parse(seminarObj.getString("opened_at")));
			seminar.setEndedAt(Var.sdf.parse(seminarObj.getString("closed_at")));
			seminar.setUrl(seminarObj.getString("url"));
			seminar.setVenueName(venue.getName());
			seminar.setSeatName(seat.getName());
			
			VenueDB db = new VenueDB(context, VenueDB.USER_DB); 
			try{
				db.setWritableDb();
				if(Seminar.find(db.getDb(), seminar.getId()) == null)
					seminar.insert(db.getDb());
			}finally{
				db.closeWithoutCommit();
			}
			return Boolean.TRUE;
		}catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	public int getStatusCode(){
		return statusCode;
	}
}
