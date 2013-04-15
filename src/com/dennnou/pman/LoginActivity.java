package com.dennnou.pman;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml.Encoding;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
	private static final String TAG = "LoginActivity";

	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter[] filters;
	private String[][] techs;
	private Tag nfcTag;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		//NFC
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		filters = new IntentFilter[]{tech};
		// Setup a tech list for all NfcF tags
		techs = new String[][] { new String[] { MifareClassic.class.getName() } };
		
		//View
		Button btWrite = (Button)findViewById(R.id.bt_write);
		btWrite.setOnClickListener(onWriteClick);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
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
	
	private void writeUriToTag(String uri){
		byte[] uriData = uri.getBytes(Charset.forName(Encoding.US_ASCII.name()));
		ByteBuffer bb = ByteBuffer.allocate(uriData.length + 1);
		bb.put((byte)0);
		bb.put(uriData);
		
		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, new byte[]{'U'}, new byte[0], bb.array());
		NdefMessage ndef = new NdefMessage(new NdefRecord[]{record});
		
		NdefFormatable nf = NdefFormatable.get(nfcTag);
		try {
			nf.connect();
			nf.format(ndef);
			nf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private View.OnClickListener onWriteClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			EditText etUri = (EditText)findViewById(R.id.et_uri);
			String uri = etUri.getText().toString();
			writeUriToTag(uri);
		}
	};
}
