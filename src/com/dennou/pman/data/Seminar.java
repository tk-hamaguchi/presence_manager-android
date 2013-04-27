package com.dennou.pman.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Seminar {
	private static final String TABLE = "seminars";
	private static final String ID  = "id";
	private static final String NAME = "name";
	private static final String STARTED_AT = "started_at";
	private static final String ENDED_AT = "ended_at";
	private static final String DESCRIPTION = "description";
	private static final String URL = "url";
	private static final String VENUE_NAME = "venue_name";
	private static final String SEAT_NAME = "seat_name";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN);
	
	private int id;
	private String name;
	private Date startedAt;
	private Date endedAt;
	private String description;
	private String url;
	private String venueName;
	private String seatName;
	
	public Seminar(){
	}
	
	public Seminar(Cursor c){
		id = c.getInt(c.getColumnIndex(ID));
		name = c.getString(c.getColumnIndex(NAME));
		startedAt = new Date(c.getLong(c.getColumnIndex(STARTED_AT)));
		endedAt = new Date(c.getLong(c.getColumnIndex(ENDED_AT)));
		description = c.getString(c.getColumnIndex(DESCRIPTION));
		url = c.getString(c.getColumnIndex(URL));
		venueName = c.getString(c.getColumnIndex(VENUE_NAME));
		seatName = c.getString(c.getColumnIndex(SEAT_NAME));
	}
	
	public static Seminar find(SQLiteDatabase db, int id){
		Cursor c = db.query(TABLE, new String[]{ID, NAME, STARTED_AT,ENDED_AT,DESCRIPTION,URL,VENUE_NAME,SEAT_NAME},
				"id=?",
				new String[]{String.valueOf(id)},
				null, null, null);
		while(c.moveToNext()){
			return new Seminar(c);
		}
		return null;
	}
	
	public static List<Seminar>list(SQLiteDatabase db){
		List<Seminar>list = new ArrayList<Seminar>();
		Cursor c = db.query(TABLE, new String[]{ID, NAME, STARTED_AT,ENDED_AT,DESCRIPTION,URL,VENUE_NAME,SEAT_NAME},
				null,
				null,
				null, null, null);
		while(c.moveToNext()){
			list.add(new Seminar(c));
		}
		return list;
	}
	
	public void insert(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(ID, id);
		values.put(NAME, name);
		values.put(STARTED_AT, startedAt.getTime());
		values.put(ENDED_AT, endedAt.getTime());
		values.put(DESCRIPTION, description);
		values.put(URL, url);
		values.put(VENUE_NAME, venueName);
		values.put(SEAT_NAME, seatName);
		db.insert(TABLE, null, values);
	}
	
	@Override
	public String toString(){
		return String.format("%s/%s", name, sdf.format(startedAt));
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getEndedAt() {
		return endedAt;
	}
	public void setEndedAt(Date endedAt) {
		this.endedAt = endedAt;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getStartedAt() {
		return startedAt;
	}
	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getVenueName() {
		return venueName;
	}

	public void setVenueName(String venueName) {
		this.venueName = venueName;
	}

	public String getSeatName() {
		return seatName;
	}

	public void setSeatName(String seatName) {
		this.seatName = seatName;
	}
}
