package com.dennou.pman.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VenueDB extends SQLiteOpenHelper {
	private static final int DB_VERSION = 11;
	public static final String ADMIN_DB = "admin.db";
	public static final String USER_DB = "user.db";
	private SQLiteDatabase db;
	private static Integer lock = 0;
	
	//1 room tag
	
	public VenueDB(Context context, String db) {
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
					db.execSQL("drop table venues");
				}catch(Exception ex){
					
				}
				try{
					db.execSQL("drop table seats");
				}catch(Exception ex){
					
				}
				try{
					db.execSQL("drop table seminars");
				}catch(Exception ex){
					
				}
				db.execSQL(String.format("create table venues (%s)",
						android.provider.BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
								"id INTEGER,"+
								"name TEXT "
								));
				
				db.execSQL(String.format("create table seats (%s)",
						android.provider.BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
								"id INTEGER,"+
								"venue_id INTEGER,"+
								"name TEXT,"+
								"sign TEXT,"+
								"secret TEXT"
								));
				
				db.execSQL(String.format("create table seminars (%s)",
						android.provider.BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
								"id INTEGER,"+
								"name TEXT,"+
								"started_at INTEGER,"+
								"ended_at INTEGER,"+
								"description TEXT,"+
								"url TEXT,"+
								"venue_name TEXT,"+
								"seat_name TEXT,"+
								"nfc_tag_id INTEGER,"+
								"nfc_tag_secret TEXT,"+
								"nfc_tag_sign TEXT"
								));
				
				/* ダミーデータ生成
				for(int i=0;i<10; i++){
					Venue venue = new Venue();
					venue.setId(i);
					venue.setName(String.format("%d号室", i));
					venue.insert(db);
				}
				
				for(int i=0;i<10; i++){
					for(int j=0; j<i+1; j++){
						Seat tag = new Seat();
						tag.setId(1000 + i*10+j);
						tag.setVenueId(i);
						tag.setName(String.format("%s号室  %d", i, j));
						tag.setSecret("1d102a8c");
						tag.setSign("12345678901234567891234567890123456789");
						tag.insert(db);
					}
				}
				Seat test = new Seat();
				test.setId(1);
				test.setVenueId(0);
				test.setName(String.format("%s号室  %d",0, 1));
				test.setSecret("1d102a8c");
				test.setSign("12345678901234567891234567890123456789");
				test.insert(db);
				*/
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
		Log.d("DBHelper", "getLock");
	}
	
	private synchronized void releaseLock(){
		notify();
		lock--;
		Log.d("DBHelper", "notify");
	}
}