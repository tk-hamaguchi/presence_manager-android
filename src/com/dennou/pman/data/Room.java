package com.dennou.pman.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Room {
	private static final String TABLE = "rooms";
	private static final String ID = "id";
	private static final String NAME = "name";
	
	private int id;
	private String name;
	
	public Room() {
	}
	
	public Room(Cursor c) {
		id = c.getInt(c.getColumnIndex(ID));
		name = c.getString(c.getColumnIndex(NAME));
	}
	
	public static List<Room> list(SQLiteDatabase db){
		ArrayList<Room> list = new ArrayList<Room>();
		Cursor c = db.query(TABLE, new String[]{ID,NAME}, null, null, null, null, null);
		while(c.moveToNext()){
			Room room = new Room(c);
			list.add(room);
		}
		return list;
	}
	
	public static Room find(SQLiteDatabase db, int id){
		Cursor c = db.query(TABLE, new String[]{ID,NAME}, null, null, null, null, null);
		if(c.moveToNext()){
			return new Room(c);
		}
		return null;
	}
	
	public void insert(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(ID, id);
		values.put(NAME, name);
		db.insert(TABLE, null, values);
	}
	
	public void update(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		db.update(TABLE, values, "id=?", new String[]{String.valueOf(id)});
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
