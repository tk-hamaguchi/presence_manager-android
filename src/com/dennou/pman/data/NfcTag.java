package com.dennou.pman.data;

import com.dennou.pman.nfc.INfcTag;
import com.dennou.pman.nfc.PmTag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.Tag;

public class NfcTag implements INfcTag{
	public static final int ISSUER_TYPE_SEAT = 1;
	public static final int ISSUER_TYPE_SEMINAR = 2;
	
	
	private static final String TABLE = "nfc_tags";
	private static final String ID = "id";
	private static final String SIGN = "sign";
	private static final String SECRET = "secret";
	private static final String ISSUER_ID = "issuer_id";
	private static final String ISSUER_TYPE = "issuer_type";
	
	private int id;
	private String sign;
	private String secret;
	private int issuerId;
	private int issuerType;
	
	public NfcTag() {
	}

	public NfcTag(int id, String sign, String secret) {
		this.id = id;
		this.sign = sign;
		this.secret = secret;
	}
	
	public NfcTag(Cursor c) {
		id = c.getInt(c.getColumnIndex(ID));
		sign = c.getString(c.getColumnIndex(SIGN));
		secret = c.getString(c.getColumnIndex(SECRET));
	}
	
	public static NfcTag find(SQLiteDatabase db, int id){
		Cursor c = db.query(TABLE, new String[]{ID, SIGN, SECRET, ISSUER_TYPE, ISSUER_ID}, "id=?",
				new String[]{String.valueOf(id)}, null, null, null);
		if(c.moveToNext()){
			return new NfcTag(c);
		}
		return null;
	}
	
	public static NfcTag findByIssuer(SQLiteDatabase db, int issuer_type, int issuerId){
		Cursor c = db.query(TABLE, new String[]{ID, SIGN, SECRET, ISSUER_TYPE, ISSUER_ID},
				"issuer_type=? and issuer_id=?",
				new String[]{String.valueOf(issuer_type), String.valueOf(issuerId)}, null, null, null);
		if(c.moveToNext()){
			return new NfcTag(c);
		}
		return null;
	}
	
	public void insert(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(ID, id);
		values.put(SIGN, sign);
		values.put(SECRET, secret);
		values.put(ISSUER_TYPE, issuerType);
		values.put(ISSUER_ID, issuerId);
		db.insert(TABLE, null, values);
	}
	
	public void update(SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(SIGN, sign);
		values.put(SECRET, secret);
		values.put(ISSUER_TYPE, issuerType);
		values.put(ISSUER_ID, issuerId);
		db.update(TABLE, values, "id=?", new String[]{String.valueOf(id)});
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}

	public int getIssuerId() {
		return issuerId;
	}

	public void setIssuerId(int issuerId) {
		this.issuerId = issuerId;
	}

	public int getIssuerType() {
		return issuerType;
	}

	public void setIssuerType(int issuerType) {
		this.issuerType = issuerType;
	}

	@Override
	public NdefMessage getNdefMessage(Context context, Tag tag) {
		String host = TempData.getInstance(context).getHost();
		return PmTag.getNdefMessage(host, this, tag.getId());
	}
}
