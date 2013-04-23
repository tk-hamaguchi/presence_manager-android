package com.dennou.pman.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Venue {
	private static final String TABLE = "rooms";
	private static final String ID = "id";
	private static final String NAME = "name";
	
	private int id;
	private String name;
	
	public Venue() {
	}
	
	public Venue(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Venue(Cursor c) {
		id = c.getInt(c.getColumnIndex(ID));
		name = c.getString(c.getColumnIndex(NAME));
	}
	
	public static List<Venue> list(SQLiteDatabase db){
		ArrayList<Venue> list = new ArrayList<Venue>();
		Cursor c = db.query(TABLE, new String[]{ID,NAME}, null, null, null, null, null);
		while(c.moveToNext()){
			Venue room = new Venue(c);
			list.add(room);
		}
		return list;
	}
	
	public static Venue find(SQLiteDatabase db, int id){
		Cursor c = db.query(TABLE, new String[]{ID,NAME}, null, null, null, null, null);
		if(c.moveToNext()){
			return new Venue(c);
		}
		return null;
	}
	
	public static void delete(SQLiteDatabase db){
		db.delete(TABLE, null, null);
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
