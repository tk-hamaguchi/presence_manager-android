package com.dennou.pman.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Seat {
	private static final String TABLE = "seats";
	private static final String ID = "id";
	private static final String VENUE_ID = "venue_id";
	private static final String NAME = "name";
	private static final String SECRET = "secret";
	private static final String SIGN = "sign";
	
	private int id;
	private int venueId;
	private String name;
	private String secret;
	private String sign;
	
	public Seat() {
	}
	
	public Seat(int id, String name, String secret, String sign){
		this.id = id;
		this.name = name;
		this.secret = secret;
		this.sign = sign;
	}
	
	public Seat(Cursor c) {
		id = c.getInt(c.getColumnIndex(ID));
		venueId = c.getInt(c.getColumnIndex(VENUE_ID));
		name = c.getString(c.getColumnIndex(NAME));
		secret = c.getString(c.getColumnIndex(SECRET));
		sign = c.getString(c.getColumnIndex(SIGN));
	}
	
	public static List<Seat> list(SQLiteDatabase db, int room_id){
		ArrayList<Seat> list = new ArrayList<Seat>();
		Cursor c = db.query(TABLE, new String[]{ID, VENUE_ID, NAME, SECRET, SIGN}, "venue_id=?",
				new String[]{String.valueOf(room_id)}, null, null, null);
		while(c.moveToNext()){
			Seat tag = new Seat(c);
			list.add(tag);
		}
		return list;
	}
	
	public static Seat find(SQLiteDatabase db, int id){
		Cursor c = db.query(TABLE, new String[]{ID, VENUE_ID, NAME, SECRET, SIGN}, "id=?",
				new String[]{String.valueOf(id)}, null, null, null);
		if(c.moveToNext()){
			return new Seat(c);
		}
		return null;
	}
	
	public static void delete(SQLiteDatabase db, int venueId){
		db.delete(TABLE, "venue_id=?", new String[]{String.valueOf(venueId)});
	}
	
	public void insert(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(ID, id);
		values.put(VENUE_ID, venueId);
		values.put(NAME, name);
		values.put(SECRET, secret);
		values.put(SIGN, sign);
		db.insert(TABLE, null, values);
	}
	
	public void update(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(VENUE_ID, venueId);
		values.put(NAME, name);
		values.put(SECRET, secret);
		values.put(SIGN, sign);
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

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	@Override
	public String toString() {
		return String.format("%s", name);
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
}
