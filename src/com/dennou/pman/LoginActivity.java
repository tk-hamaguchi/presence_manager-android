package com.dennou.pman;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.dennnou.pman.R;
import com.dennou.pman.data.Venue;
import com.dennou.pman.data.VenueDB;
import com.dennou.pman.data.Seat;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.nfc.PmTag;
import com.esp.common.handler.AlertHandler;

public class LoginActivity extends BaseActivity{
	private static final String TAG = "LoginActivity";
	
	private static final int INDEX_LOGIN = 0;
	private static final int INDEX_HOME = 1;
	
	private WebView webView;
	private AlertHandler alert;
	private TempData tempData;
	private PmTag pmTag;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		alert = new AlertHandler(this);
		tempData = TempData.getInstance(this);
	
		webView = (WebView) findViewById(R.id.wv_login);
		webView.setWebViewClient(webViewClient);
		webView.getSettings().setJavaScriptEnabled(true);
		String ua = webView.getSettings().getUserAgentString();
		webView.getSettings().setUserAgentString( ua + "/" + Var.UA);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent it = getIntent();
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(it.getAction())){
			handleNdef(it);
			if(pmTag != null && pmTag.getSecret()!=null){
				setView();
			}else{
				alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.lo_msg_please_touch, 0).sendToTarget();
			}
		}else{
			setView();
		}
	}
	
	private void handleNdef(Intent it){
		Tag tag = (Tag)it.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		try {
			pmTag = PmTag.get(Ndef.get(tag));
			if(pmTag != null){
				handlePmTag(pmTag);
			}else{
				
			}
			setView();
		}  catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handlePmTag(PmTag pmTag){
		VenueDB db = new VenueDB(this, VenueDB.USER_DB);
		try{
			db.setReadableDb();
			Seat seat = Seat.find(db.getDb(), pmTag.getSeatId());
			if(seat != null){
				Venue room = Venue.find(db.getDb(), seat.getVenueId());
				setSeatInfo(room, seat);
			}else{
				//情報取得
				AsyncTask<String, Void, Boolean> at = new AsyncTask<String, Void, Boolean>(){
					@Override
					protected Boolean doInBackground(String... params) {
						
						return null;
					}
					
					@Override
					protected void onPostExecute(Boolean result) {
						super.onPostExecute(result);
						if(result){
							
						}else{
							alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.lo_msg_invalid_tag, 0).sendToTarget();
						}
					}
				};
				at.execute(new String[]{});
				at.get();
			}
			db.closeWithoutCommit();
		}catch(Exception e){
			
		}
	}
	
	private void setSeatInfo(Venue room, Seat seat){
		TextView tvRoom = (TextView)findViewById(R.id.tv_name);
		tvRoom.setText(room.getName());
		TextView tvSeat = (TextView)findViewById(R.id.tv_name); 
		tvSeat.setText(seat.getName());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.menu_tag_init:
				Intent it = new Intent(this, TagFormatActivity.class);
				startActivity(it);
				return true;
			case R.id.menu_logout:
				logout();
		}
		return super.onOptionsItemSelected(item);
	}
	
	//ログイン状態へ
	private void setView(){
		ViewFlipper vf = (ViewFlipper)findViewById(R.id.vf_home);
		if(tempData.getAuthToken() == null){
			webView.loadUrl(Var.AUTH_URI);
			vf.setDisplayedChild(INDEX_LOGIN);
		}else{
			TextView tvAccount = (TextView)findViewById(R.id.tv_account);
			tvAccount.setText(tempData.getAccount());
			
			vf.setDisplayedChild(INDEX_HOME);
		}
	}
	
	private void logout(){
		tempData.setAccount(null);
		tempData.setAuthToken(null);
		tempData.save(this);
		setView();
	}
	
	private WebViewClient webViewClient = new WebViewClient() {
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, url);
			Uri uri = Uri.parse(url);
			if (uri.getScheme().equals(Var.AUTH_SCHEME)){
				if(Var.AUTH_PATH.equals(uri.getPath())){
					tempData.setAuthToken( uri.getQueryParameter(Var.AUTH_PARAM_TOKEN) );
					tempData.setAccount( uri.getQueryParameter(Var.AUTH_PARAM_USER) );
					tempData.save(LoginActivity.this);
					Log.d(TAG, tempData.getAccount());
					alert.obtainMessage(AlertHandler.ID_SHOW_TOAST, R.string.lo_msg_login_complete, 0).sendToTarget();
					setView();
				}else{
					alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.lo_msg_login_failed, 0).sendToTarget();
					webView.loadUrl(Var.AUTH_URI);
				}
				return true;
			}else{
				return super.shouldOverrideUrlLoading(view, url);
			}
		}
		
		public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
			alert.obtainMessage(AlertHandler.ID_SHOW_MSG, R.string.app_name, 0);
		};
		
		public void onPageFinished(WebView view, String url) {
			alert.obtainMessage(AlertHandler.ID_DISMISS);
		};
	};
}
