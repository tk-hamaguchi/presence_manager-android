package com.dennou.pman.logic;

import java.util.ArrayList;

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

import com.dennou.pman.data.Seminar;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.data.VenueDB;

public class LoadMySeminarTask extends AsyncTask<String, Void, Boolean> {
	private final static String TAG = "LoadSeminarTask";
	private Context context;
	
    protected ArrayList<Seminar> list;
	protected int statusCode;
	
	public LoadMySeminarTask(Context context) {
		this.context = context;
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			String format = Var.getUri(Var.MY_SEMINAR_URI, context);
			String uri = String.format(format);
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
			
			//Seminar
	        list = new ArrayList<Seminar>();
			JSONArray seminarArray = json.getJSONArray("seminar");
			for(int i=0; i<seminarArray.length(); i++){
				JSONObject seminarObj = seminarArray.getJSONObject(i).getJSONObject("seminar");
				Seminar seminar = new Seminar();
				seminar.setId(seminarObj.getInt("id"));
				seminar.setName(seminarObj.getString("name"));
				seminar.setDescription(seminarObj.getString("description"));
				seminar.setStartedAt(Var.sdf.parse(seminarObj.getString("opened_at")));
				seminar.setEndedAt(Var.sdf.parse(seminarObj.getString("closed_at")));
				seminar.setUrl(seminarObj.getString("url"));

				JSONObject venueObj = seminarArray.getJSONObject(i).getJSONObject("venue");
				seminar.setVenueName(venueObj.getString("name"));
				
				JSONObject nfcTagObj = seminarArray.getJSONObject(i).getJSONObject("nfc_tag");
				seminar.setNfcTagId(nfcTagObj.getInt("code"));
				seminar.setNfcTagSecret(nfcTagObj.getString("secret"));
				seminar.setNfcTagSign(nfcTagObj.getString("sign"));
				
				list.add(seminar);
			}
			saveDb();
			return Boolean.TRUE;
		}catch(Exception ex){
			return Boolean.FALSE;
		}
	}
	
	private void saveDb(){
		VenueDB db = new VenueDB(context, VenueDB.ADMIN_DB);
		try{
			db.setWritableDb();
			Seminar.deleteAll(db.getDb());
			for(Seminar seminar:list){
				seminar.insert(db.getDb());
			}
		}finally{
			db.closeWithoutCommit();
		}
	}

	public int getStatusCode(){
		return statusCode;
	}
}
