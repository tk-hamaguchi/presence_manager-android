package com.dennou.pman;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.dennou.pman.data.NfcTag;
import com.dennou.pman.data.Seat;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.Venue;
import com.dennou.pman.data.VenueDB;
import com.dennou.pman.logic.LoadSeatTask;
import com.dennou.pman.logic.LoadVenueTask;
import com.esp.common.handler.AlertHandler;

public class TagFormatActivity extends BaseActivity{
	private static final String TAG = "TagFormatActivity";

	//Roomデータ
	private ArrayAdapter<Venue> aaVenue;
	private ArrayAdapter<Seat> aaSeat;
	private TempData tempData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_format);
		
		tempData = TempData.getInstance(this);
		
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
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if(TempData.getInstance(this).getAuthToken()==null){
			showPleaseLogin();
		}else{
			setInitData();
		}
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
			Venue venue = (Venue)av.getSelectedItem();
			if(venue != null)
				setSeat(venue);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
		
	};
	
	private void setSeat(Venue venue){
		aaSeat.clear();
		VenueDB db = new VenueDB(this, VenueDB.ADMIN_DB);
		List<Seat> list = null;
		try{
			db.setReadableDb();
			list = Seat.list(db.getDb(), venue.getId());
		}finally{
			db.closeWithoutCommit();
		}
		if(list.size() > 0){
			for(Seat seat:list){
				aaSeat.add(seat);
			}
		}else{
			Log.d(TAG, "seat=0, go load seat");
			loadSeat(venue);
		}
	}
	
	private void setInitData(){
		VenueDB db = new VenueDB(this, VenueDB.ADMIN_DB);
		try{
			db.setWritableDb();
			aaVenue.clear();
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
	
	private NfcTag loadNfcTag(int seatId){
		VenueDB db = new VenueDB(this, VenueDB.ADMIN_DB);
		try{
			db.setReadableDb();
			NfcTag nfcTag =  NfcTag.findByIssuer(db.getDb(), NfcTag.ISSUER_TYPE_SEAT, seatId);
			return nfcTag;
		}finally{
			db.closeWithoutCommit();
		}
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
			Seat seat = aaSeat.getItem(position);
			NfcTag nfcTag = loadNfcTag(seat.getId());
			tempData.setNfcTag(nfcTag);
			
			Intent it = new Intent(TagFormatActivity.this, NfcActivity.class);
			it.putExtra(Intent.EXTRA_TITLE, seat.getName());
			startActivity(it);
		}
	};
		
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
