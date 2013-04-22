package com.dennou.pman;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.dennnou.pman.R;
import com.dennou.pman.data.Room;
import com.dennou.pman.data.RoomDB;
import com.dennou.pman.data.Seat;
import com.dennou.pman.nfc.PmTag;
import com.esp.common.handler.AlertHandler;
import com.esp.common.handler.IAlertActivity;

@SuppressWarnings("deprecation")
public class TagFormatActivity extends Activity implements IAlertActivity{
	private static final String TAG = "TagFormatActivity";

	private AlertHandler alert;
	
	//NFC関係
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter[] filters;
	private String[][] techs;
	private Tag nfcTag;
	
	//Roomデータ
	private ArrayAdapter<Room> aaRoom;
	private ArrayAdapter<Seat> aaSeat;
	
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
					new String[]{MifareClassic.class.getName()},
					new String[]{Ndef.class.getName()},
					new String[]{NdefFormatable.class.getName()} };
		}
		
		//View
		//Button btWrite = (Button)findViewById(R.id.bt_write);
		//btWrite.setOnClickListener(onWriteClick);
		
		//List
		aaRoom = new ArrayAdapter<Room>(this, android.R.layout.simple_list_item_1);
		Spinner spRoom = (Spinner)findViewById(R.id.sp_room);
		spRoom.setAdapter(aaRoom);
		spRoom.setOnItemSelectedListener(spItemSelected);
		
		aaSeat = new ArrayAdapter<Seat>(this, android.R.layout.simple_list_item_1);
		ListView lvSeat = (ListView)findViewById(R.id.lv_seat);
		lvSeat.setAdapter(aaSeat);
		lvSeat.setOnItemClickListener(lvItemClick);
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
	
	@Override
	protected void onStart() {
		super.onStart();
		
		RoomDB db = new RoomDB(this, RoomDB.ADMIN_DB);
		try{
			db.setWritableDb();
			List<Room>roomList = Room.list(db.getDb());
			for(Room room:roomList){
				aaRoom.add(room);
			}
			
			if(roomList.size()>0){
				for(Seat seat :Seat.list(db.getDb(), roomList.get(0).getId())){
					aaSeat.add(seat);
				}
			}
		}finally{
			db.closeWithoutCommit();
		}
	}

	private AdapterView.OnItemSelectedListener spItemSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> av, View v, int arg2, long id) {
			Room room = (Room)av.getSelectedItem();
			if(room != null)
				setSeat(room.getId());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
		
	};
	
	private void setSeat(int roomId){
		aaSeat.clear();
		RoomDB db = new RoomDB(this, RoomDB.ADMIN_DB);
		try{
			db.setReadableDb();
			for(Seat seat:Seat.list(db.getDb(), roomId)){
				aaSeat.add(seat);
			}
		}finally{
			db.closeWithoutCommit();
		}
	}
	
	private OnItemClickListener lvItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int position, long id) {
			final Seat seat = aaSeat.getItem(position);
			AlertDialog.Builder ab = new AlertDialog.Builder(TagFormatActivity.this);
			ab.setMessage(R.string.tf_msg_write);
			ab.setPositiveButton(R.string.c_ok, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					try{
						writeTag(seat);
					}catch(Exception e){
						e.printStackTrace();
						Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_msg_write, 0).sendToTarget();
					}
				}
			});
			ab.setNegativeButton(R.string.c_cancel, null);
			ab.show();
		}
	};
	
	
	private void writeTag(Seat seat) throws Exception{
		//Tag初期化
		final Seat ts = seat;
		AsyncTask<Tag, Void, Boolean> atask = new AsyncTask<Tag, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Tag... params) {
				AlertHandler.wait(100);
				PmTag pmTag = new PmTag(params[0]);
				return (Boolean)pmTag.writeSeatTag(ts);
			}
		};
		try {
			Message.obtain(alert, AlertHandler.ID_SHOW_MSG, R.string.tf_writing, 0).sendToTarget();
			atask.execute(new Tag[]{nfcTag});
			if(atask.get()){
				Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_msg_complete, 0).sendToTarget();
			}else{
				Message.obtain(alert, AlertHandler.ID_SHOW_DLG, R.string.tf_msg_failed, 0).sendToTarget();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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
