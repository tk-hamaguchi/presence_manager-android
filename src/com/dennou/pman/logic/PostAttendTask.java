package com.dennou.pman.logic;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
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
			String uri = Var.ATTEND_API_URI;
			
			JSONObject json = new JSONObject();
			json.put("code", code);
			json.put("sign", sign);
			json.put("secret", secret);
			
			StringEntity entity = new StringEntity(json.toString());
			entity.setContentType("application/json");
			entity.setChunked(false);
			
			HttpPost post = new HttpPost(uri);
			String bearer = String.format(Var.HEADER_BEARER, TempData.getInstance(context).getAuthToken());
			post.setHeader(Var.HEADER_AUTHORIZATION, bearer);
			post.setEntity(entity);
			Log.d(TAG, json.toString());
			
			HttpResponse response = client.execute( post );
			statusCode = response.getStatusLine().getStatusCode();
			
			if ( statusCode != HttpStatus.SC_OK )
				return Boolean.FALSE;
			
			VenueDB db = new VenueDB(context, VenueDB.ADMIN_DB); 
			try{
				db.setWritableDb();
			}finally{
				db.closeWithoutCommit();
			}
			return Boolean.TRUE;
		}  catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	public int getStatusCode(){
		return statusCode;
	}
}
