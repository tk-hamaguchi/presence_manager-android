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
	private static final String OPENED_AT = "opened_at";
	private static final String CLOSED_AT = "closed_at";
	private static final String DESCRIPTION = "description";
	private static final String URL = "url";
	private static final String VENUE_NAME = "venue_name";
	private static final String SEAT_NAME = "seat_name";
	private static final String NFC_TAG_ID = "nfc_tag_id";
	private static final String NFC_TAG_SECRET = "nfc_tag_secret";
	private static final String NFC_TAG_SIGN = "nfc_tag_sign";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN);
	
	private int id;
	private String name;
	private Date startedAt;
	private Date endedAt;
	private Date openedAt;
	private Date closedAt;
	private String description;
	private String url;
	private String venueName;
	private String seatName;
	//NFC用
	private int nfcTagId;
	private String nfcTagSecret;
	private String nfcTagSign;
	
	public Seminar(){
	}
	
	public Seminar(Cursor c){
		id = c.getInt(c.getColumnIndex(ID));
		name = c.getString(c.getColumnIndex(NAME));
		startedAt = new Date(c.getLong(c.getColumnIndex(STARTED_AT)));
		endedAt = new Date(c.getLong(c.getColumnIndex(ENDED_AT)));
		openedAt = new Date(c.getLong(c.getColumnIndex(OPENED_AT)));
		closedAt = new Date(c.getLong(c.getColumnIndex(CLOSED_AT)));
		description = c.getString(c.getColumnIndex(DESCRIPTION));
		url = c.getString(c.getColumnIndex(URL));
		venueName = c.getString(c.getColumnIndex(VENUE_NAME));
		seatName = c.getString(c.getColumnIndex(SEAT_NAME));
		
		nfcTagId = c.getInt(c.getColumnIndex(NFC_TAG_ID));
		nfcTagSecret = c.getString(c.getColumnIndex(NFC_TAG_SECRET));
		nfcTagSign = c.getString(c.getColumnIndex(NFC_TAG_SIGN));
	}
	
	public static Seminar find(SQLiteDatabase db, int id){
		Cursor c = db.query(TABLE, new String[]{ID, NAME, STARTED_AT,ENDED_AT,OPENED_AT,CLOSED_AT,DESCRIPTION,URL,VENUE_NAME,SEAT_NAME,
												NFC_TAG_ID, NFC_TAG_SECRET,NFC_TAG_SIGN},
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
		Cursor c = db.query(TABLE, new String[]{ID, NAME, STARTED_AT,ENDED_AT,OPENED_AT,CLOSED_AT,DESCRIPTION,URL,VENUE_NAME,SEAT_NAME,
											NFC_TAG_ID, NFC_TAG_SECRET, NFC_TAG_SIGN},
				null,
				null,
				null, null, STARTED_AT + " DESC");
		while(c.moveToNext()){
			list.add(new Seminar(c));
		}
		return list;
	}
	
	public static void deleteAll(SQLiteDatabase db){
		db.delete(TABLE, null, null);
	}
	
	public void insert(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(ID, id);
		values.put(NAME, name);
		values.put(STARTED_AT, startedAt.getTime());
		values.put(ENDED_AT, endedAt.getTime());
		values.put(OPENED_AT, openedAt.getTime());
		values.put(CLOSED_AT, closedAt.getTime());
		values.put(DESCRIPTION, description);
		values.put(URL, url);
		values.put(VENUE_NAME, venueName);
		values.put(SEAT_NAME, seatName);
		values.put(NFC_TAG_ID, nfcTagId);
		values.put(NFC_TAG_SECRET, nfcTagSecret);
		values.put(NFC_TAG_SIGN, nfcTagSign);
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

	public int getNfcTagId() {
		return nfcTagId;
	}

	public void setNfcTagId(int nfcTagId) {
		this.nfcTagId = nfcTagId;
	}

	public String getNfcTagSecret() {
		return nfcTagSecret;
	}

	public void setNfcTagSecret(String nfcTagSecret) {
		this.nfcTagSecret = nfcTagSecret;
	}

	public String getNfcTagSign() {
		return nfcTagSign;
	}

	public void setNfcTagSign(String nfcTagSign) {
		this.nfcTagSign = nfcTagSign;
	}

	public Date getOpenedAt() {
		return openedAt;
	}

	public void setOpenedAt(Date openedAt) {
		this.openedAt = openedAt;
	}

	public Date getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(Date closedAt) {
		this.closedAt = closedAt;
	}
}
