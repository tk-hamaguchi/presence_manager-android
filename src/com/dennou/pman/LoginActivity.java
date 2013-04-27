package com.dennou.pman;

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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.dennou.pman.data.Seat;
import com.dennou.pman.data.Seminar;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.data.Venue;
import com.dennou.pman.data.VenueDB;
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
				Intent itAttend = new Intent(this, AttendActivity.class);
				startActivity(itAttend);
				break;
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
			db.setReadableDb();
			list = Seminar.list(db.getDb());
		}finally{
			db.closeWithoutCommit();
		}
		ArrayAdapter<Seminar> aa = new ArrayAdapter<Seminar>(this, android.R.layout.simple_list_item_1);
		for(Seminar seminar:list){
			aa.add(seminar);
		}
		ListView lvLog = (ListView)findViewById(R.id.lb_log);
		lvLog.setAdapter(aa);
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
