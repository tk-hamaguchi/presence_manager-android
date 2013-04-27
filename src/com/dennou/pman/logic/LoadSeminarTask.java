package com.dennou.pman.logic;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
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

public class LoadSeminarTask extends AsyncTask<String, Void, Boolean> {
	private final static String TAG = "LoadSeminarTask";
	private Context context;
	
	private int code;
	private String sign;
	
	private Seminar seminar;
	private Seat seat;
	private Venue venue;
	protected int statusCode;
	
	public LoadSeminarTask(Context context, int code, String sign) {
		this.context = context;
		this.code = code;
		this.sign = sign;
	}
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			String uri = String.format(Var.SEMINAR_URI, code, sign);
			HttpGet get = new HttpGet(uri);
			String bearer = String.format(Var.HEADER_BEARER, TempData.getInstance(context).getAuthToken());
			get.setHeader(Var.HEADER_AUTHORIZATION, bearer);
			
			Log.d(TAG, "uri=" + uri);
			HttpResponse response = client.execute( get );
			statusCode = response.getStatusLine().getStatusCode();
			Log.d(TAG, "status="+statusCode);
			
			if ( statusCode != HttpStatus.SC_OK )
				return Boolean.FALSE;
			
	        String body = EntityUtils.toString(response.getEntity(), Var.CHARSET);
	        JSONObject json = new JSONObject(body);
			
	        //Venue
	        JSONObject venueObj = json.getJSONObject("venue");
			venue = new Venue();
			venue.setName(venueObj.getString("name"));
			//Seat
			JSONObject seatObj = json.getJSONObject("seat");
			seat = new Seat();
			seat.setName(seatObj.getString("name"));
			
			//Seminar
			JSONObject seminarObj = json.getJSONObject("seminar");
			seminar = new Seminar();
			seminar.setName(seminarObj.getString("name"));
			seminar.setDescription(seminarObj.getString("description"));
			seminar.setStartedAt(Var.sdf.parse(seminarObj.getString("started_at")));
			seminar.setEndedAt(Var.sdf.parse(seminarObj.getString("ended_at")));
			seminar.setUrl(seminarObj.getString("url"));
			return Boolean.TRUE;
		}catch(Exception ex){
			return Boolean.FALSE;
		}
	}

	public Venue getVenue() {
		return venue;
	}
	public Seminar getSeminar() {
		return seminar;
	}
	public Seat getSeat() {
		return seat;
	}
	public int getStatusCode(){
		return statusCode;
	}
}
