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

import com.dennou.pman.data.NfcTag;
import com.dennou.pman.data.Seat;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.data.VenueDB;

public class LoadSeatTask extends AsyncTask<String, Void, Boolean> {
	private final static String TAG = "LoadSeatTask";
	
	private Context context;
	private List<Seat> seatList;
	private List<NfcTag> nfcTagList;
	
	private int venueId;
	private int statusCode;
	
	public LoadSeatTask(Context context, int venueId) {
		this.context = context;
		this.venueId = venueId;
	}
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			seatList = new ArrayList<Seat>();
			nfcTagList = new ArrayList<NfcTag>();
			
			String uriFormat = Var.getUri(Var.SEAT_URI, context);
			String uri = String.format(uriFormat, venueId);
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
			int venueId= venueObj.getInt("id");
			//Seat
			JSONArray nfcTagArray = json.getJSONArray("seat");
			Log.d(TAG, "seat count=" + nfcTagArray.length());
			for(int i=0;i<nfcTagArray.length(); i++){
				JSONObject seatObj = nfcTagArray.getJSONObject(i);
				int id = seatObj.getInt("id");
				String name = seatObj.getString("name");
				Seat seat = new Seat(id, name);
				seat.setVenueId(venueId);
				Log.d(TAG, venueId + seat.getId() + seat.getName());
				seatList.add(seat);
				
				JSONObject nfcTagObj = seatObj.getJSONObject("nfc_tag");
				NfcTag nfcTag = new NfcTag(nfcTagObj.getInt("id"), 
						nfcTagObj.getString("sign"), nfcTagObj.getString("secret"));
				nfcTag.setIssuerType(NfcTag.ISSUER_TYPE_SEAT);
				nfcTag.setIssuerId(seat.getId());
				nfcTagList.add(nfcTag);
			}
			
			VenueDB db = new VenueDB(context, VenueDB.ADMIN_DB); 
			try{
				db.setWritableDb();
				Seat.delete(db.getDb(), venueId);
				for(Seat seat:seatList){
					seat.insert(db.getDb());
				}
				for(NfcTag nfcTag:nfcTagList){
					if(NfcTag.find(db.getDb(), nfcTag.getId())!=null){
						nfcTag.update(db.getDb());
					}else{
						nfcTag.insert(db.getDb());
					}
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
	
	public List<Seat> getSeatList() {
		return seatList;
	}
	public int getStatusCode(){
		return statusCode;
	}
}
