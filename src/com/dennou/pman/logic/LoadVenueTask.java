package com.dennou.pman.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dennou.pman.data.VenueDB;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.data.Venue;

public class LoadVenueTask extends AsyncTask<String, Void, Boolean> {
	private final static String TAG = "LoadVenueTask";
	private Context context;
	private List<Venue> venueList;
	private int statusCode;
	
	public LoadVenueTask(Context context) {
		this.context = context;
	}
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			venueList = new ArrayList<Venue>();
			
			String uriFormat = Var.getUri(Var.VENUE_URI, context);
			HttpGet get = new HttpGet(uriFormat);
			String bearer = String.format(Var.HEADER_BEARER, TempData.getInstance(context).getAuthToken());
			get.setHeader(Var.HEADER_AUTHORIZATION, bearer);
			
			Log.d(TAG, "uri="+uriFormat);
			HttpResponse response = client.execute( get );
			statusCode = response.getStatusLine().getStatusCode();
			Log.d(TAG, "status="+statusCode);
			
			if ( statusCode != HttpStatus.SC_OK )
				return Boolean.FALSE;
			
	        String body = EntityUtils.toString(response.getEntity(), Var.CHARSET);
	        JSONObject json = new JSONObject(body);
			JSONArray venueArray = json.getJSONArray("venue");
			Log.d(TAG, "venue count="+venueArray.length());
			for(int i=0;i<venueArray.length(); i++){
				JSONObject obj = venueArray.getJSONObject(i);
				int id = obj.getInt("id");
				String name = obj.getString("name");
				Venue venue = new Venue(id, name);
				Log.d(TAG, venue.getId() + venue.getName());
				venueList.add(venue);
			}
			
			VenueDB db = new VenueDB(context, VenueDB.ADMIN_DB); 
			try{
				db.setWritableDb();
				Venue.delete(db.getDb());
				for(Venue v:venueList){
					v.insert(db.getDb());
				}
			}finally{
				db.closeWithoutCommit();
			}
			return Boolean.TRUE;
		}  catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	public List<Venue> getVenueList() {
		return venueList;
	}
	public int getStatusCode(){
		return statusCode;
	}
}
