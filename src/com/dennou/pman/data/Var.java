package com.dennou.pman.data;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;

public class Var {
	//タグ用
	public static final String SV_HOST = "pmanager.ddo.jp:3000";
	public static final String SV_SCHEME = "http";
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_BEARER = "Bearer %s";
	public static final String UA = "PmClient";
	public static final String CHARSET = "utf-8";
	
	//ログイン用
	public static final String AUTH_URI = "%s://%s/users/auth/twitter";
	public static final String AUTH_SCHEME = "com.dennou.pman";
	public static final String AUTH_PATH = "/auth";
	public static final String AUTH_ERROR_PATH = "/error";
	public static final String AUTH_PARAM_TOKEN = "token";
	public static final String AUTH_PARAM_USER = "user";
	
	//venues
	public static final String VENUE_URI = "%s://%s/api/venues";
	//Seat
	public static final String SEAT_URI = "%s://%s/api/seats?venue=%%s";
	
	//Attend
    public static final String ATTEND_URI = "%s://%s/seminars/attend?code=%%s&sign=%%s";
	public static final String ATTEND_API_URI = "%s://%s/api/seminars/attend";
	public static final String ATTEND_PARAM_NFC_TAG = "code";
	public static final String ATTEND_PARAM_SIGN = "sign";
	public static final String ATTEND_PARAM_SECRET = "secret";
	
	//Seminar detail
	public static final String SEMINAR_URI = "%s://%s/api/seminars/detail?code=%%s&sign=%%s";
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:MM:ss'Z'";
	public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.JAPAN);

	//My seminar
	public static final String MY_SEMINAR_URI = "%s://%s/api/seminars";
	
	//イベント作成
	public static final String CREATE_SEMINAR_URI = "%s://%s/seminars";
	
	public static String getUri(String uri, Context context){
		TempData tempData = TempData.getInstance(context);
		String host = tempData.getHost();
		return String.format(uri, SV_SCHEME, host);
	}
}
