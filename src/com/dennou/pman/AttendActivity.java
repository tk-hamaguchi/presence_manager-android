package com.dennou.pman;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dennnou.pman.R;
import com.dennou.pman.data.Seat;
import com.dennou.pman.data.Seminar;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.data.Venue;
import com.dennou.pman.logic.LoadSeminarTask;
import com.dennou.pman.nfc.PmTag;
import com.esp.common.handler.AlertHandler;

public class AttendActivity extends BaseActivity{
	private static final String TAG = "AttendActivity";

	private AlertHandler alert;
	private AlertDialog dialog;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN);
	
	//NFC関係
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter[] filters;
	private LoadSeminarTask task;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attend);
		
		//ALertDialog
		alert = new AlertHandler(this);
		
		//NFC
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if(nfcAdapter != null){
			Intent pi = new Intent(this, getClass());
			pi.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			pi.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			pi.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			
			pendingIntent = PendingIntent.getActivity(this, 0, pi, 0);
			IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
			ndef.addDataScheme(Var.SV_SCHEME);
			ndef.addDataAuthority(Var.SV_HOST, null);
			filters = new IntentFilter[]{ndef};
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(nfcAdapter != null)
			nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(nfcAdapter != null)
			nfcAdapter.disableForegroundDispatch(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(dialog != null && dialog.isShowing())
			dialog.dismiss();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(!checkLogin())
			return;
		
		Log.d(TAG, "onNewIntent:" + intent.getAction());
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
			if(task==null || task.getStatus()==Status.FINISHED)
				handleNdef(intent);
		}
	}
	
	private void handleNdef(Intent intent){
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            for (int i = 0; i < rawMsgs.length; i++) {
                NdefMessage msg = (NdefMessage) rawMsgs[i];
                Log.d(TAG, msg.toString());
                PmTag pmTag = PmTag.get(msg);
                if(pmTag!=null){
                	handlePmTag(pmTag);
                }
            }
        }
	}
	
	private void handlePmTag(PmTag pmTag){
		LoadSeminarTask task = new LoadSeminarTask(this, pmTag.getCode(), pmTag.getSign()){
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					alert.obtainMessage(AlertHandler.ID_DISMISS).sendToTarget();
					showSeminar(getSeminar(), getVenue(), getSeat());
				}else{
					if(statusCode==404){
						alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.at_msg_no_seminar, 0).sendToTarget();
					}else{
						alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.at_msg_comm_error, 0).sendToTarget();
					}
				}
			}
		};
		alert.obtainMessage(AlertHandler.ID_SHOW_MSG, R.string.msg_comm_get, 0).sendToTarget();
		task.execute(new String[]{});
	}
	
	private void showSeminar(Seminar seminar, Venue venue, Seat seat){
		TextView tvSeminar = (TextView)findViewById(R.id.tv_seminar);
		tvSeminar.setText(seminar.getName());
		TextView tvVenue = (TextView)findViewById(R.id.tv_venue);
		tvVenue.setText(venue.getName());
		TextView tvSeat = (TextView)findViewById(R.id.tv_seat);
		tvSeat.setText(seat.getName());
		TextView tvDate = (TextView)findViewById(R.id.tv_date);
		tvDate.setText(sdf.format(seminar.getStartedAt()));
		TextView tvDescription = (TextView)findViewById(R.id.tv_description);
		tvDescription.setText(seminar.getDescription());
		TextView tvUrl = (TextView)findViewById(R.id.tv_url);
		tvUrl.setText(seminar.getUrl());
		tvUrl.setTag(seminar.getUrl());
		tvUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				invokeBrowser((String)v.getTag());
			}
		});
	}
	
	private void invokeBrowser(String uri){
		Intent it = new Intent(Intent.ACTION_VIEW);
		it.addCategory(Intent.CATEGORY_DEFAULT);
		it.setData(Uri.parse(uri));
		startActivity(it);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(!checkLogin())
			return;
		
		Intent intent = getIntent();
		Log.d(TAG, "onStart:" + intent.getAction());
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
			if(task==null || task.getStatus()==Status.FINISHED)
				handleNdef(intent);
		}
	}
	
	private boolean checkLogin(){
		if(TempData.getInstance(this).getAuthToken()==null){
			alert.obtainMessage(AlertHandler.ID_SHOW_TOAST, R.string.at_msg_please_login, 0).sendToTarget();
			Intent it = new Intent(this, LoginActivity.class);
			startActivity(it);
			finish();
			return false;
		}else{
			return true;
		}
	}
}
