package com.dennou.pman;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.dennou.pman.data.Seminar;
import com.dennou.pman.data.VenueDB;
import com.dennou.pman.logic.AndroidUtility;
import com.dennou.pman.logic.SeminarAdapter;

public class LogActivity extends Activity {
	private SeminarAdapter sa;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		
		setSeminarAdapter();
		ListView lvLog = (ListView)findViewById(R.id.lv_log);
		lvLog.setAdapter(sa);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		loadLog();
	}
	
	private void loadLog(){
		VenueDB db = new VenueDB(this, VenueDB.USER_DB);
		try{
			db.setReadableDb();
			List<Seminar> list = Seminar.list(db.getDb());
			for(Seminar seminar:list){
				sa.add(seminar);
			}
		}finally{
			db.closeWithoutCommit();
		}
	}
	
	private void setSeminarAdapter(){
		sa = new SeminarAdapter(this){
			@Override
			public View getView(int position, View v, ViewGroup parent) {
				View ret = super.getView(position, v, parent);
				TextView tvSeminar = (TextView)ret.findViewById(R.id.tv_seminar);
				tvSeminar.setTag(getItem(position));
				tvSeminar.setOnClickListener(seminarClick);
				return ret;
			}
		};
	}
	
	private OnClickListener seminarClick = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			Seminar seminar = (Seminar)v.getTag();
			if(seminar.getUrl() == null || !seminar.getUrl().startsWith("http"))
				return;
			AndroidUtility.openUri(LogActivity.this, seminar.getUrl());
		}
	};
}
