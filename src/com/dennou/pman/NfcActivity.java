package com.dennou.pman;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dennou.pman.data.TempData;
import com.esp.common.handler.AlertHandler;

public class NfcActivity extends BaseActivity{
	private static final String TAG = "NfcActivity";
	public static final String TARGET_DATA = "target_data";
	
	//NFC関係
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter[] filters;
	private String[][] techs;
	private NdefMessage targetData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);
		
		//NFC
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if(nfcAdapter != null){
			pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
					getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
			filters = new IntentFilter[]{tech};
			
			// Setup a tech list for MifareaClassic tags
			techs = new String[][] { 
					new String[]{Ndef.class.getName()},
					new String[]{NdefFormatable.class.getName()} };
		}
		
		Button btCancel = (Button)findViewById(R.id.bt_cancel);
		btCancel.setOnClickListener(btCancelClick);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(nfcAdapter != null)
			nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techs);
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
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
			Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if(Ndef.get(tag)!=null || NdefFormatable.get(tag)!=null){
				writeTag(tag);
			}else if(tag != null){
				alert.obtainMessage(AlertHandler.ID_SHOW_TOAST,
						R.string.tf_msg_unsupport_tag, 0).sendToTarget();
				Log.d(TAG, tag.toString());
			}
		}else{
			Log.d(TAG, intent.getAction());
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if(TempData.getInstance(this).getAuthToken()==null){
			showPleaseLogin();
			return;
		}
		
		Intent it = getIntent();
		targetData = it.getParcelableExtra(TARGET_DATA);
	}
	
	private void showPleaseLogin(){
		AlertDialog.Builder ab= new AlertDialog.Builder(this);
		ab.setPositiveButton(R.string.c_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		ab.setTitle(R.string.app_name);
		ab.setMessage(R.string.tf_msg_please_login);
		ab.setCancelable(false);
		ab.show();
	}
	
	private void writeTag(Tag tag){
		//Tag初期化
		AsyncTask<Tag, Void, Boolean> atask = new AsyncTask<Tag, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Tag... params) {
				try {
					AlertHandler.wait(100);
					if(NdefFormatable.get(params[0]) != null){
						NdefFormatable nf = NdefFormatable.get(params[0]);
						nf.connect();
						nf.format(targetData);
						nf.close();
					}else if(Ndef.get(params[0]) != null){
						Ndef ndef = Ndef.get(params[0]);
						ndef.connect();
						ndef.writeNdefMessage(targetData);
						ndef.close();
					}
					return true;
				}catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					targetData= null;
					Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_msg_complete, 0).sendToTarget();
				}else{
					Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_msg_failed, 0).sendToTarget();
				}
			}
		};
		if(targetData!=null){
			Message.obtain(alert, AlertHandler.ID_SHOW_MSG, R.string.tf_writing, 0).sendToTarget();
			atask.execute(new Tag[]{tag});
		}
	}
	
	private OnClickListener btCancelClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	}; 
}
