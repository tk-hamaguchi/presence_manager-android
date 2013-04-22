package com.dennou.pman.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RoomDB extends SQLiteOpenHelper {
	private static final int DB_VERSION = 4;
	public static final String ADMIN_DB = "room.db";
	public static final String USER_DB = "room.db";
	private SQLiteDatabase db;
	private static Integer lock = 0;
	
	//1 room tag
	
	public RoomDB(Context context, String db) {
		super(context, db, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		onUpgrade(db, 0, 0);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try{
			db.beginTransaction();
			if(oldVersion<1 || true){
				try{
					db.execSQL("drop table rooms");
				}catch(Exception ex){
					
				}
				try{
					db.execSQL("drop table tags");
				}catch(Exception ex){
					
				}
				db.execSQL(String.format("create table rooms (%s)",
						android.provider.BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
								"id INTEGER,"+
								"name TEXT "
								));
				
				db.execSQL(String.format("create table tags (%s)",
						android.provider.BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
								"id INTEGER,"+
								"room_id INTEGER,"+
								"name TEXT,"+
								"secret TEXT"
								));
				
				for(int i=0;i<10; i++){
					Room room = new Room();
					room.setId(i);
					room.setName(String.format("%d号室", i));
					room.insert(db);
				}
				
				for(int i=0;i<10; i++){
					for(int j=0; j<i+1; j++){
						Seat tag = new Seat();
						tag.setId(1000 + i*10+j);
						tag.setRoomId(i);
						tag.setName(String.format("%s号室  %d", i, j));
						tag.setSecret("1d102a8c");
						tag.insert(db);
					}
				}
				Seat test = new Seat();
				test.setId(1);
				test.setRoomId(0);
				test.setName(String.format("%s号室  %d",0, 1));
				test.setSecret("1d102a8c");
				test.insert(db);
			}
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}
	}
	
	public void setTransaction(){
		db.beginTransaction();
	}
	
	public void setWritableDb(){
		synchronized(lock){
			getLock();
		}
		db = this.getWritableDatabase();
	}
	
	public void setReadableDb(){
		synchronized(lock){
			getLock();
		}
		db = this.getReadableDatabase();
	}
	
	public void setCommit(){
		if(db == null)
			return;
		
		if(db.inTransaction()){
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		db.close();
		db = null;
		releaseLock();
	}
	
	public void closeWithoutCommit(){
		if(db == null)
			return;
		
		if(db.inTransaction())
			db.endTransaction();
		db.close();
		db = null;
		releaseLock();
	}

	public SQLiteDatabase getDb(){
		return db;
	}
	
	private synchronized void getLock(){
		while(lock>0){
			try {
				wait(10);
			} catch (InterruptedException e) {
				Log.d("BookDBHelper", "lock released!");
			}
		}
		lock++;
		Log.d("BookDBHelper", "getLock");
	}
	
	private synchronized void releaseLock(){
		notify();
		lock--;
		Log.d("BookDBHelper", "notify");
	}
}