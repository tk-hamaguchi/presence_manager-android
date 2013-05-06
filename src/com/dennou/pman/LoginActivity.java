package com.dennou.pman;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.dennou.pman.data.Seat;
import com.dennou.pman.data.Seminar;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.data.Venue;
import com.dennou.pman.data.VenueDB;
import com.dennou.pman.logic.AndroidUtility;
import com.dennou.pman.logic.SeminarAdapter;
import com.esp.common.handler.AlertHandler;

public class LoginActivity extends BaseActivity{
	private static final String TAG = "LoginActivity";
	
	private static final int INDEX_LOGIN = 0;
	private static final int INDEX_HOME = 1;
	
	private WebView webView;
	private AlertHandler alert;
	private Dialog dialog;
	private TempData tempData;
	
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
		
		Button btLog = (Button)findViewById(R.id.bt_log);
		btLog.setOnClickListener(btLogClick);
		Button btSeminar = (Button)findViewById(R.id.bt_seminar);
		btSeminar.setOnClickListener(btSeminarClick);
		
		tempData.setHost("pm2013-03.herokuapp.com");
		tempData.save(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		setView();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		if(dialog != null && dialog.isShowing())
			dialog.dismiss();
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
				Intent itTagFormat = new Intent(this, TagFormatActivity.class);
				startActivity(itTagFormat);
				return true;
			case R.id.menu_logout:
				showLogout();
				break;
			case R.id.menu_config:
				Intent itPref = new Intent(this, PrefActivity.class);
				startActivity(itPref);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//ログイン状態へ
	private void setView(){
		ViewFlipper vf = (ViewFlipper)findViewById(R.id.vf_home);
		if(tempData.getAuthToken() == null){
			String uriFormat = Var.getUri(Var.AUTH_URI, this);
			webView.loadUrl(uriFormat);
			vf.setDisplayedChild(INDEX_LOGIN);
		}else{
			TextView tvAccount = (TextView)findViewById(R.id.tv_account);
			tvAccount.setText(tempData.getAccount());
			showLog();
			
			vf.setDisplayedChild(INDEX_HOME);
		}
	}
	
	private void showLogout(){
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.app_name);
		ab.setMessage(R.string.lo_msg_ask_logout);
		ab.setPositiveButton(R.string.c_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				logout();
			}
		});
		ab.setNegativeButton(R.string.c_cancel, null);
		dialog = ab.show();
	}
	
	private void logout(){
		VenueDB db = new VenueDB(this, VenueDB.ADMIN_DB);
		try{
			db.setWritableDb();
			Venue.delete(db.getDb());
			Seat.delete(db.getDb());
		}finally{
			db.closeWithoutCommit();
		}
		tempData.setAccount(null);
		tempData.setAuthToken(null);
		tempData.save(this);
		setView();
	}
	
	private void showLog(){
		List<Seminar> list;
		VenueDB db = new VenueDB(this, VenueDB.USER_DB);
		try{
			Calendar cal = Calendar.getInstance();
			db.setReadableDb();
			list = Seminar.list(db.getDb());
			Seminar s = new Seminar();
			s.setName("Android講習会");
			s.setStartedAt(new Date(0));
			cal.add(Calendar.DAY_OF_MONTH, 1);
			s.setEndedAt(cal.getTime());
			s.setVenueName("大講義室--3");
			s.setSeatName("A-29");
			s.setId(10000);
			s.setUrl("http://m.yahoo.co.jp/");
			list.add(s);
			
			s = new Seminar();
			s.setName("Android講習会-2");
			s.setStartedAt(new Date(0));
			s.setEndedAt(cal.getTime());
			s.setVenueName("大講義室--3");
			s.setSeatName("A-30");
			s.setId(10000);
			s.setUrl("http://m.yahoo.co.jp/");
			list.add(s);
			
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			Date now = Calendar.getInstance().getTime();
			
			ViewGroup v = (ViewGroup)findViewById(R.id.inc_seminar);
			if(list.size() != 0 && now.getTime() < list.get(0).getEndedAt().getTime()){
				Seminar seminar = list.get(0);
				SeminarAdapter.showSeminar(v, seminar);
				TextView tvSeminar = (TextView)v.findViewById(R.id.tv_seminar);
				tvSeminar.setTag(seminar);
				tvSeminar.setOnClickListener(seminarClick);
				
				v.setVisibility(View.VISIBLE);
			}else{
				v.setVisibility(View.GONE);
			}
		}finally{
			db.closeWithoutCommit();
		}
	}
	
	private OnClickListener seminarClick = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			Seminar seminar = (Seminar)v.getTag();
			if(seminar.getUrl() == null || !seminar.getUrl().startsWith("http"))
				return;
			AndroidUtility.openUri(LoginActivity.this, seminar.getUrl());
		}
	};
	
	private OnClickListener btLogClick =new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent it = new Intent(LoginActivity.this, LogActivity.class);
			startActivity(it);
		}
	};
	
	private OnClickListener btSeminarClick =new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String uri = Var.getUri(Var.CREATE_SEMINAR_URI, LoginActivity.this);
			AndroidUtility.openUri(LoginActivity.this, uri);
		}
	};
	
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
					String uriFormat = Var.getUri(Var.AUTH_URI, LoginActivity.this);
					webView.loadUrl(uriFormat);
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
