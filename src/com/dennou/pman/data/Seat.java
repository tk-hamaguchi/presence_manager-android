package com.dennou.pman.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Seat{
	private static final String TABLE = "seats";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String VENUE_ID = "venue_id";
	
	private int id;
	private String name;
	private int venueId;
	
	public Seat() {
	}
	
	public Seat(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Seat(Cursor c) {
		id = c.getInt(c.getColumnIndex(ID));
		venueId = c.getInt(c.getColumnIndex(VENUE_ID));
		name = c.getString(c.getColumnIndex(NAME));
	}
	
	public static List<Seat> list(SQLiteDatabase db, int venue_id){
		ArrayList<Seat> list = new ArrayList<Seat>();
		Cursor c = db.query(TABLE, new String[]{ID, VENUE_ID, NAME},
				"venue_id=?",
				new String[]{String.valueOf(venue_id)}, null, null, null);
		while(c.moveToNext()){
			Seat tag = new Seat(c);
			list.add(tag);
		}
		return list;
	}
	
	public static Seat find(SQLiteDatabase db, int id){
		Cursor c = db.query(TABLE, new String[]{ID, VENUE_ID, NAME}, "id=?",
				new String[]{String.valueOf(id)}, null, null, null);
		if(c.moveToNext()){
			return new Seat(c);
		}
		return null;
	}
	
	public static void delete(SQLiteDatabase db, int venueId){
		db.delete(TABLE, "venue_id=?", new String[]{String.valueOf(venueId)});
	}
	
	public static void delete(SQLiteDatabase db){
		db.delete(TABLE, null, null);
	}
	
	public void insert(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(ID, id);
		values.put(VENUE_ID, venueId);
		values.put(NAME, name);
		db.insert(TABLE, null, values);
	}
	
	public void update(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(VENUE_ID, venueId);
		values.put(NAME, name);
		db.update(TABLE, values, "id=?", new String[]{String.valueOf(id)});
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVenueId() {
		return venueId;
	}

	public void setVenueId(int venueId) {
		this.venueId = venueId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format("%s", name);
	}
}
