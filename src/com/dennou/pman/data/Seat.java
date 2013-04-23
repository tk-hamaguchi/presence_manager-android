package com.dennou.pman.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Seat {
	private static final String TABLE = "tags";
	private static final String ID = "id";
	private static final String ROOM_ID = "room_id";
	private static final String NAME = "name";
	private static final String SECRET = "secret";
	
	private int id;
	private int roomId;
	private String name;
	private String secret;
	
	public Seat() {
	}
	
	public Seat(int id, int roomId, String name, String secret){
		this.id = id;
		this.roomId = roomId;
		this.name = name;
		this.secret = secret;
	}
	
	public Seat(Cursor c) {
		id = c.getInt(c.getColumnIndex(ID));
		roomId = c.getInt(c.getColumnIndex(ROOM_ID));
		name = c.getString(c.getColumnIndex(NAME));
		secret = c.getString(c.getColumnIndex(SECRET));
	}
	
	public static List<Seat> list(SQLiteDatabase db, int room_id){
		ArrayList<Seat> list = new ArrayList<Seat>();
		Cursor c = db.query(TABLE, new String[]{ID, ROOM_ID, NAME, SECRET}, "room_id=?",
				new String[]{String.valueOf(room_id)}, null, null, null);
		while(c.moveToNext()){
			Seat tag = new Seat(c);
			list.add(tag);
		}
		return list;
	}
	
	public static Seat find(SQLiteDatabase db, int id){
		Cursor c = db.query(TABLE, new String[]{ID, ROOM_ID, NAME, SECRET}, "tag_id=?",
				new String[]{String.valueOf(id)}, null, null, null);
		if(c.moveToNext()){
			return new Seat(c);
		}
		return null;
	}
	
	public void insert(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(ID, id);
		values.put(ROOM_ID, roomId);
		values.put(NAME, name);
		values.put(SECRET, secret);
		db.insert(TABLE, null, values);
	}
	
	public void update(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(ROOM_ID, roomId);
		values.put(NAME, name);
		values.put(SECRET, secret);
		db.update(TABLE, values, "id=?", new String[]{String.valueOf(id)});
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
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
}
