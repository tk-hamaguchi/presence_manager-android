package com.dennou.pman;

import java.util.Calendar;
import java.util.Date;
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
			Calendar cal = Calendar.getInstance();
			db.setReadableDb();
			List<Seminar> list = Seminar.list(db.getDb());
			Seminar s = new Seminar();
			s.setName("Android講習会");
			s.setStartedAt(new Date(0));
			cal.add(Calendar.DAY_OF_MONTH, 1);
			s.setEndedAt(cal.getTime());
			s.setVenueName("大講義室--3");
			s.setSeatName("A-29");
			s.setId(10000);
			s.setUrl("http://m.yahoo.co.jp/");
			list.add(s);
			
			s = new Seminar();
			s.setName("Android講習会-2");
			s.setStartedAt(new Date(0));
			s.setEndedAt(cal.getTime());
			s.setVenueName("大講義室--3");
			s.setSeatName("A-30");
			s.setId(10000);
			s.setUrl("http://m.yahoo.co.jp/");
			list.add(s);
			
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
