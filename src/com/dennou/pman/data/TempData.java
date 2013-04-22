package com.dennou.pman.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TempData {
	private static final String TAG_AUTH_TOKEN = "temp.auth_token";
	private static final String TAG_ACCOUNT = "temp.account";
	
	private static TempData instance;
	
	private String authToken;
	private String account;
	
	private TempData(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		authToken = pref.getString(TAG_AUTH_TOKEN, null);
		account = pref.getString(TAG_ACCOUNT, null);
	}
	
	public static TempData getInstance(Context context){
		if(instance == null){
			instance = new TempData(context);
		}
		return instance;
	}
	
	public void save(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(TAG_AUTH_TOKEN, authToken);
		editor.putString(TAG_ACCOUNT, account);
		editor.commit();
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}
}
