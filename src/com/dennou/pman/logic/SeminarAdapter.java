package com.dennou.pman.logic;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dennou.pman.R;
import com.dennou.pman.data.Seminar;

public class SeminarAdapter extends ArrayAdapter<Seminar> {
	private Activity activity;
	
	public SeminarAdapter(Activity context) {
		super(context, R.layout.item_seminar);
		this.activity = context;
	}
	
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		if(v == null)
		{
			LayoutInflater ll = (LayoutInflater)activity.getLayoutInflater();
			v = ll.inflate(R.layout.item_seminar, null);
		}
		showSeminar((ViewGroup)v, getItem(position));
		return v;
	}

	public static void showSeminar(ViewGroup vg, Seminar seminar){
		TextView tvSeminar = (TextView)vg.findViewById(R.id.tv_seminar);
		tvSeminar.setText(seminar.getName());
		
		TextView tvDate = (TextView)vg.findViewById(R.id.tv_date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN);
		tvDate.setText(sdf.format(seminar.getStartedAt()));
		
		TextView tvVenue = (TextView)vg.findViewById(R.id.tv_venue);
		tvVenue.setText(seminar.getVenueName());
		
		TextView tvSeat = (TextView)vg.findViewById(R.id.tv_seat);
		tvSeat.setText(seminar.getSeatName());
		
		TextView tvDescription = (TextView)vg.findViewById(R.id.tv_description);
		tvDescription.setText(seminar.getDescription());
	}
}
