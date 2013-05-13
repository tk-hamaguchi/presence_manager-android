package com.dennou.pman;

import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dennou.pman.data.NfcTag;
import com.dennou.pman.data.Seminar;
import com.dennou.pman.data.TempData;
import com.dennou.pman.data.VenueDB;
import com.dennou.pman.logic.AndroidUtility;
import com.dennou.pman.logic.LoadMySeminarTask;
import com.dennou.pman.logic.SeminarAdapter;
import com.esp.common.handler.AlertHandler;

public class MySeminarActivity extends BaseActivity {
	private SeminarAdapter adapter;
	private TempData tempData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_seminar);
		
		tempData = TempData.getInstance(this);
		
		setSeminarAdapter();
		ListView lvSeminar = (ListView)findViewById(R.id.lv_seminar);
		lvSeminar.setAdapter(adapter);
		lvSeminar.setOnItemClickListener(seminarItemClick);
		
		Button btReload = (Button)findViewById(R.id.bt_reload);
		btReload.setOnClickListener(reloadClick);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		loadDb();
	}
	
	private void loadDb(){
		VenueDB db = new VenueDB(this, VenueDB.ADMIN_DB);
		try{
			db.setReadableDb();
			List<Seminar> list = Seminar.list(db.getDb());
			adapter.clear();
			Date now = new Date();
			for(Seminar seminar:list){
				if(seminar.getEndedAt().getTime()>now.getTime())
					adapter.add(seminar);
			}
		}finally{
			db.closeWithoutCommit();
		}
	}
	
	private NfcTag loadNfcTag(int seminarId){
		VenueDB db = new VenueDB(this, VenueDB.ADMIN_DB);
		try{
			db.setReadableDb();
			NfcTag nfcTag =  NfcTag.findByIssuer(db.getDb(), NfcTag.ISSUER_TYPE_SEMINAR, seminarId);
			return nfcTag;
		}finally{
			db.closeWithoutCommit();
		}
	}
	
	private void loadMySeminr(){
		alert.obtainMessage(AlertHandler.ID_SHOW_MSG, R.string.msg_comm_get, 0).sendToTarget();
		LoadMySeminarTask task = new LoadMySeminarTask(this){
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					alert.obtainMessage(AlertHandler.ID_DISMISS).sendToTarget();
					if(seminarList.size()>0){
						alert.obtainMessage(AlertHandler.ID_SHOW_TOAST, R.string.msg_comm_complete, 0).sendToTarget();
					}else{
						alert.obtainMessage(AlertHandler.ID_SHOW_TOAST, R.string.ms_msg_no_seminar, 0).sendToTarget();
					}
					loadDb();
				}else{
					alert.obtainMessage(AlertHandler.ID_SHOW_DLG, R.string.msg_comm_error, 0).sendToTarget();
				}
			}			
		};
		task.execute(new String[]{});
	}
	
	private void setSeminarAdapter(){
		adapter = new SeminarAdapter(this){
			@Override
			public View getView(int position, View v, ViewGroup parent) {
				View ret = super.getView(position, v, parent);
				
				//タイトルにURLをリンク
				TextView tvSeminar = (TextView)ret.findViewById(R.id.tv_seminar);
				tvSeminar.setTag(getItem(position));
				tvSeminar.setOnClickListener(seminarClick);
				
				//座席タグじゃないので、非表示にする
				ViewGroup llSeatRow = (ViewGroup)ret.findViewById(R.id.ll_seat_row);
				llSeatRow.setVisibility(View.GONE);
				return ret;
			}
		};
	}
	
	private OnClickListener seminarClick = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			Seminar seminar = (Seminar)v.getTag();
			AndroidUtility.openUri(MySeminarActivity.this, seminar.getUrl());
		}
	};
	
	private OnClickListener reloadClick = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			loadMySeminr();
		}
	};
	
	private OnItemClickListener seminarItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
			Seminar seminar = (Seminar)adapter.getItemAtPosition(position);
			NfcTag nfcTag = loadNfcTag(seminar.getId());
			tempData.setNfcTag(nfcTag);
			
			Intent it = new Intent(MySeminarActivity.this, NfcActivity.class);
			it.putExtra(Intent.EXTRA_TITLE, seminar.getName());
			startActivity(it);
		}
	};
}
