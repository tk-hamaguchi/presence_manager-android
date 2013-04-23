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
import com.dennou.pman.data.Seat;

public class LoadSeatTask extends AsyncTask<String, Void, Boolean> {
	private final static String TAG = "LoadSeatTask";
	private Context context;
	private List<Seat> seatList;
	
	public LoadSeatTask(Context context) {
		this.context = context;
	}
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			seatList = new ArrayList<Seat>();
			
			HttpGet get = new HttpGet(Var.VENUE_URI);
			String bearer = String.format(Var.HEADER_BEARER, TempData.getInstance(context).getAuthToken());
			get.setHeader(Var.HEADER_AUTHORIZATION, bearer);
			
			HttpResponse response = client.execute( get );
			int status = response.getStatusLine().getStatusCode();
			
			if ( status == HttpStatus.SC_OK ){
		        String body = EntityUtils.toString(response.getEntity(), Var.CHARSET);
		        JSONObject json = new JSONObject(body);
				
		        //Venue
		        JSONObject venueObj = json.getJSONObject("venue");
				Venue venue = new Venue(venueObj.getInt("id"), venueObj.getString("name"));
				//Seat
				JSONArray seatArray = json.getJSONArray("seat");
				for(int i=0;i<seatArray.length(); i++){
					JSONObject obj = seatArray.getJSONObject(i);
					int id = obj.getInt("id");
					String name = obj.getString("name");
					String sequense = obj.getString("sequense");
					Seat seat = new Seat(id, venue.getId(), name, sequense);
					Log.d(TAG, venue.getId() + seat.getId() + seat.getName());
					seatList.add(seat);
				}
			}
			
			RoomDB db = new RoomDB(context, RoomDB.ADMIN_DB); 
			try{
				db.setWritableDb();
				Venue.delete(db.getDb());
			}finally{
				db.closeWithoutCommit();
			}
			return Boolean.valueOf(true);
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return Boolean.valueOf(false);
	}
	
	public List<Seat> getSeatList() {
		return seatList;
	}
}
