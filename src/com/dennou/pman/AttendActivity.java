package com.dennou.pman;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dennou.pman.data.Seat;
import com.dennou.pman.data.Seminar;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Venue;
import com.dennou.pman.logic.AndroidUtility;
import com.dennou.pman.logic.LoadSeminarDetailTask;
import com.dennou.pman.logic.PostAttendTask;
import com.dennou.pman.logic.SeminarAdapter;
import com.dennou.pman.nfc.PmTag;
import com.esp.common.handler.AlertHandler;

public class AttendActivity extends BaseActivity{
	private static final String TAG = "AttendActivity";

	private AlertDialog dialog;
	//NFC関係
	private LoadSeminarDetailTask task;
	private PmTag pmTag;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attend);
		
		//ALertDialog
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
				parseNdef(intent);
		
			if(pmTag != null){
	        	handlePmTag(pmTag);
	        	return;
			}else{
				alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.at_msg_invalid_tag, 0).sendToTarget();
			}
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(dialog != null && dialog.isShowing())
			dialog.dismiss();
	}
	
	private void parseNdef(Intent intent){
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            for (int i = 0; i < rawMsgs.length; i++) {
                NdefMessage msg = (NdefMessage) rawMsgs[i];
                Log.d(TAG, msg.toString());
                pmTag = PmTag.get(msg);
            }
        }
        
		Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(tag != null && pmTag!=null){
        	pmTag.setUid(tag.getId());
        }else{
        	pmTag = null;
        }
	}
	
	private void handlePmTag(PmTag pmTag){
		LoadSeminarDetailTask task = new LoadSeminarDetailTask(this, pmTag.getCode(), pmTag.getSign()){
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
		ViewGroup vg = (ViewGroup)findViewById(R.id.inc_seminar);
		seminar.setVenueName(venue.getName());
		seminar.setSeatName(seat.getName()!=null? seat.getName(): "--");
		SeminarAdapter.showSeminar(vg, seminar);
		TextView tvSeminar = (TextView)vg.findViewById(R.id.tv_seminar);
		tvSeminar.setTag(seminar);
		tvSeminar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Seminar seminar = (Seminar)v.getTag();
				AndroidUtility.openUri(AttendActivity.this, seminar.getUrl());
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
		PostAttendTask task = new PostAttendTask(this, pmTag.getCode(), pmTag.getSign(), pmTag.getUid(), pmTag.getSecret()){
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
