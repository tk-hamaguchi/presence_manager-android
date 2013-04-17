package com.dennnou.pman;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dennnou.pman.nfc.PmTag;
import com.esp.common.handler.AlertHandler;
import com.esp.common.handler.IAlertActivity;

@SuppressWarnings("deprecation")
public class TagFormatActivity extends Activity implements IAlertActivity{
	private static final String TAG = "TagFormatActivity";

	private AlertHandler alert;
	
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter[] filters;
	private String[][] techs;
	private Tag nfcTag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_format);
		
		//ALertDialog
		alert = new AlertHandler(this);
		
		//NFC
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		filters = new IntentFilter[]{tech};
		
		// Setup a tech list for MifareaClassic tags
		techs = new String[][] { new String[] { MifareClassic.class.getName() } };
		
		//View
		Button btWrite = (Button)findViewById(R.id.bt_write);
		btWrite.setOnClickListener(onWriteClick);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techs);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		nfcAdapter.disableForegroundDispatch(this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
			nfcTag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Log.d(TAG, nfcTag.toString());
		}else{
			Log.d(TAG, intent.getAction());
		}
	}

	private void formatTag(){
		MifareClassic mf = MifareClassic.get(nfcTag);
		if(mf == null){
			Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_please_tag, 0).sendToTarget();
			return;
		}
		
		AsyncTask<MifareClassic, Void, Boolean> atask = new AsyncTask<MifareClassic, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(MifareClassic... params) {
				AlertHandler.wait(100);
				PmTag pmTag = new PmTag(params[0]);
				return (Boolean)pmTag.initializeTag();
			}
		};
		try {
			Message.obtain(alert, AlertHandler.ID_SHOW_MSG, R.string.tf_writing, 0).sendToTarget();
			atask.execute(new MifareClassic[]{mf});
			if(atask.get()){
				Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_complete, 0).sendToTarget();
			}else{
				Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_faled, 0).sendToTarget();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private View.OnClickListener onWriteClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			formatTag();
		}
	};
	
	@Override
	public void showMessage(int id, String message) {
		try {
			if(message != null){
				Bundle bundle = new Bundle();
				bundle.putString(AlertHandler.KEY_MESSAGE, message);
				showDialog(id, bundle);
			}else{
				dismissDialog(id);
			}
		} catch (Exception e) {
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		super.onCreateDialog(id,args);
		return AlertHandler.getDialog(id, this, args);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		AlertHandler.prepareDialog(id, dialog, args);
        super.onPrepareDialog(id, dialog);
	}
}
