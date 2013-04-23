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

import com.dennou.pman.data.RoomDB;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.data.Venue;

public class LoadVenueTask extends AsyncTask<String, Void, Boolean> {
	private final static String TAG = "LoadVenueTask";
	private Context context;
	private List<Venue> venueList;
	
	public LoadVenueTask(Context context) {
		this.context = context;
	}
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			venueList = new ArrayList<Venue>();
			
			HttpGet get = new HttpGet(Var.VENUE_URI);
			String bearer = String.format(Var.HEADER_BEARER, TempData.getInstance(context).getAuthToken());
			get.setHeader(Var.HEADER_AUTHORIZATION, bearer);
			
			HttpResponse response = client.execute( get );
			int status = response.getStatusLine().getStatusCode();
			
			if ( status == HttpStatus.SC_OK ){
		        String body = EntityUtils.toString(response.getEntity(), Var.CHARSET);
		        JSONObject json = new JSONObject(body);
				JSONArray venueArray = json.getJSONArray("venue");
				for(int i=0;i<venueArray.length(); i++){
					JSONObject obj = venueArray.getJSONObject(i);
					int id = obj.getInt("id");
					String name = obj.getString("name");
					Venue venue = new Venue(id, name);
					Log.d(TAG, venue.getId() + venue.getName());
					venueList.add(venue);
				}
			}
			
			RoomDB db = new RoomDB(context, RoomDB.ADMIN_DB); 
			try{
				db.setWritableDb();
				Venue.delete(db.getDb());
				for(Venue v:venueList){
					v.insert(db.getDb());
				}
			}finally{
				db.closeWithoutCommit();
			}
			return Boolean.valueOf(true);
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return Boolean.valueOf(false);
	}
	
	public List<Venue> getVenueList() {
		return venueList;
	}
}
