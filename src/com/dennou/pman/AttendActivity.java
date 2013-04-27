package com.dennou.pman;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dennou.pman.data.Seat;
import com.dennou.pman.data.Seminar;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Venue;
import com.dennou.pman.logic.LoadSeminarTask;
import com.dennou.pman.logic.PostAttendTask;
import com.dennou.pman.nfc.PmTag;
import com.esp.common.handler.AlertHandler;

public class AttendActivity extends BaseActivity{
	private static final String TAG = "AttendActivity";

	private AlertHandler alert;
	private AlertDialog dialog;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN);
	
	//NFC関係
	private LoadSeminarTask task;
	private PmTag pmTag;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attend);
		
		//ALertDialog
		alert = new AlertHandler(this);
		Button btAttend = (Button)findViewById(R.id.bt_attend);
		btAttend.setOnClickListener(btAttendClick);
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
	
	@Override
	protected void onStop() {
		super.onStop();
		if(dialog != null && dialog.isShowing())
			dialog.dismiss();
	}
	
	private void handleNdef(Intent intent){
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            for (int i = 0; i < rawMsgs.length; i++) {
                NdefMessage msg = (NdefMessage) rawMsgs[i];
                Log.d(TAG, msg.toString());
                pmTag = PmTag.get(msg);
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
					showError(getStatusCode());
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
	
	private void showError(int statusCode){
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.app_name);
		if(statusCode==404){
			ab.setMessage(R.string.at_msg_no_seminar);
		}else{
			ab.setMessage(R.string.at_msg_comm_error);
		}
		ab.setCancelable(false);
		ab.setPositiveButton(R.string.c_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		dialog = ab.show();
	}
	
	private void invokeBrowser(String uri){
		Intent it = new Intent(Intent.ACTION_VIEW);
		it.addCategory(Intent.CATEGORY_DEFAULT);
		it.setData(Uri.parse(uri));
		startActivity(it);
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
	
	private OnClickListener btAttendClick = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(pmTag != null){
				attend(pmTag);
			}else{
				alert.obtainMessage(AlertHandler.ID_SHOW_TOAST, R.string.at_msg_please_touch, 0).sendToTarget();
			}
		}
	};
	
	private void attend(PmTag pmTag){
		PostAttendTask task = new PostAttendTask(this, pmTag.getCode(), pmTag.getSign(), pmTag.getSecret()){
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					alert.obtainMessage(AlertHandler.ID_DISMISS).sendToTarget();
					alert.obtainMessage(AlertHandler.ID_SHOW_TOAST, R.string.at_msg_attend_complete, 0).sendToTarget();
					finish();
				}else{
					if(getStatusCode()==404){
						alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.at_msg_no_seminar, 0).sendToTarget();
					}else{
						alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.at_msg_comm_error, 0).sendToTarget();
					}
				}
			}			
		};
		alert.obtainMessage(AlertHandler.ID_SHOW_MSG, R.string.at_msg_exec_attend, 0).sendToTarget();
		task.execute(new String[]{});
	}
}
