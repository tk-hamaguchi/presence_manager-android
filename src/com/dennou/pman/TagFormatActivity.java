package com.dennou.pman;

import java.util.List;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.dennnou.pman.R;
import com.dennou.pman.data.Seat;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Venue;
import com.dennou.pman.data.VenueDB;
import com.dennou.pman.logic.LoadSeatTask;
import com.dennou.pman.logic.LoadVenueTask;
import com.dennou.pman.nfc.PmTag;
import com.esp.common.handler.AlertHandler;

public class TagFormatActivity extends BaseActivity{
	private static final String TAG = "TagFormatActivity";

	private AlertHandler alert;
	private AlertDialog dialog;
	
	//NFC関係
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter[] filters;
	private String[][] techs;
	
	//Roomデータ
	private ArrayAdapter<Venue> aaVenue;
	private ArrayAdapter<Seat> aaSeat;
	private Seat targetSeat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_format);
		
		//ALertDialog
		alert = new AlertHandler(this);
		
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
		
		//List
		aaVenue = new ArrayAdapter<Venue>(this, android.R.layout.simple_list_item_1);
		Spinner spRoom = (Spinner)findViewById(R.id.sp_room);
		spRoom.setAdapter(aaVenue);
		spRoom.setOnItemSelectedListener(spItemSelected);
		
		aaSeat = new ArrayAdapter<Seat>(this, android.R.layout.simple_list_item_1);
		ListView lvSeat = (ListView)findViewById(R.id.lv_seat);
		lvSeat.setAdapter(aaSeat);
		lvSeat.setOnItemClickListener(lvItemClick);
		
		Button btRefresh = (Button)findViewById(R.id.bt_room_refresh);
		btRefresh.setOnClickListener(btRefreshClick);
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
		if(dialog != null && dialog.isShowing())
			dialog.dismiss();
		targetSeat = null;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
			Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if(Ndef.get(tag)!=null || NdefFormatable.get(tag)!=null){
				if(targetSeat != null)
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
			finish();
			return;
		}
		
		setInitData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tag_format, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_reload){
			loadVenue();
			return true;
		}else{
			return false;
		}
	}
	
	private AdapterView.OnItemSelectedListener spItemSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> av, View v, int arg2, long id) {
			Venue room = (Venue)av.getSelectedItem();
			if(room != null)
				setSeat(room.getId());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
		
	};
	
	private void setSeat(int roomId){
		aaSeat.clear();
		VenueDB db = new VenueDB(this, VenueDB.ADMIN_DB);
		try{
			db.setReadableDb();
			for(Seat seat:Seat.list(db.getDb(), roomId)){
				aaSeat.add(seat);
			}
		}finally{
			db.closeWithoutCommit();
		}
	}
	
	private void setInitData(){
		VenueDB db = new VenueDB(this, VenueDB.ADMIN_DB);
		try{
			db.setWritableDb();
			List<Venue>roomList = Venue.list(db.getDb());
			for(Venue room:roomList){
				aaVenue.add(room);
			}
			db.closeWithoutCommit();
		}finally{
			db.closeWithoutCommit();
		}
		
		if(aaVenue.getCount()==0){
			loadVenue();
		}
	}
	
	private void loadVenue(){
		LoadVenueTask task = new LoadVenueTask(this){
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					alert.obtainMessage(AlertHandler.ID_DISMISS).sendToTarget();
					aaVenue.clear();
					for(Venue v:getVenueList()){
						aaVenue.add(v);
					}
				}else{
					if(getStatusCode() != 403){
						alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.tf_msg_com_error, 0).sendToTarget();
					}else{
						showPleaseLogin();
					}
				}
			}
			
		};
		alert.obtainMessage(AlertHandler.ID_SHOW_MSG, R.string.tf_msg_comm, 0).sendToTarget();
		task.execute(new String[]{});
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
	
	private OnItemClickListener lvItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int position, long id) {
			targetSeat = aaSeat.getItem(position);
			AlertDialog.Builder ab = new AlertDialog.Builder(TagFormatActivity.this);
			ab.setMessage(R.string.tf_msg_write);
			ab.setIcon(android.R.drawable.ic_popup_disk_full);
			ab.setNegativeButton(R.string.c_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					targetSeat = null;
					dialog.dismiss();
				}
			});
			dialog = ab.show();
		}
	};
	
	
	private void writeTag(Tag tag){
		//Tag初期化
		AsyncTask<Tag, Void, Boolean> atask = new AsyncTask<Tag, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Tag... params) {
				AlertHandler.wait(100);
				PmTag pmTag = new PmTag(params[0]);
				return (Boolean)pmTag.writeSeatTag(targetSeat);
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_msg_complete, 0).sendToTarget();
				}else{
					Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_msg_failed, 0).sendToTarget();
				}
				dialog.dismiss();
				targetSeat = null;
			}
		};
		Message.obtain(alert, AlertHandler.ID_SHOW_MSG, R.string.tf_writing, 0).sendToTarget();
		atask.execute(new Tag[]{tag});
	}
	
	private View.OnClickListener btRefreshClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Spinner spVenue = (Spinner)findViewById(R.id.sp_room);
			Venue venue = (Venue)spVenue.getSelectedItem();
			if(venue != null){
				loadSeat(venue);
			}
		}
	};
	
	private void loadSeat(Venue venue){
		LoadSeatTask task = new LoadSeatTask(this, venue.getId()){
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					alert.obtainMessage(AlertHandler.ID_DISMISS).sendToTarget();
					aaSeat.clear();
					for(Seat s:getSeatList()){
						aaSeat.add(s);
					}
				}else{
					if(getStatusCode() != 403){
						alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.tf_msg_com_error, 0).sendToTarget();
					}else{
						showPleaseLogin();
					}
				}
			}
			
		};
		alert.obtainMessage(AlertHandler.ID_SHOW_MSG, R.string.tf_msg_comm, 0).sendToTarget();
		task.execute(new String[]{});
	}
}
