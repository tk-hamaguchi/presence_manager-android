package com.dennou.pman;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.dennnou.pman.R;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Var;
import com.dennou.pman.logic.LoadSeminarTask;
import com.dennou.pman.nfc.PmTag;
import com.esp.common.handler.AlertHandler;

public class AttendActivity extends BaseActivity{
	private static final String TAG = "AttendActivity";

	private AlertHandler alert;
	private AlertDialog dialog;
	
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
			pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
					getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
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
		LoadSeminarTask task = new LoadSeminarTask(this, pmTag.getCode(), pmTag.getSecret()){
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					alert.obtainMessage(AlertHandler.ID_DISMISS).sendToTarget();
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
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if(TempData.getInstance(this).getAuthToken()==null){
			finish();
			return;
		}
	}
}
