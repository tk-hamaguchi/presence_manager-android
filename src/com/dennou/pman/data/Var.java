package com.dennou.pman.data;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Var {
	//タグ用
	public static final String SV_HOST = "pmanager.ddo.jp:3000";
	public static final String SV_SCHEME = "http";
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_BEARER = "Bearer %s";
	public static final String UA = "PmClient";
	public static final String CHARSET = "utf-8";
	
	//ログイン用
	public static final String AUTH_URI = String.format("%s://%s/users/auth/twitter",SV_SCHEME, SV_HOST);
	public static final String AUTH_SCHEME = "com.dennou.pman";
	public static final String AUTH_PATH = "/auth";
	public static final String AUTH_ERROR_PATH = "/error";
	public static final String AUTH_PARAM_TOKEN = "token";
	public static final String AUTH_PARAM_USER = "user";
	
	//venues
	public static final String VENUE_URI = String.format("%s://%s/api/venues", SV_SCHEME, SV_HOST);
	//Seat
	public static final String SEAT_URI = String.format("%s://%s/api/seats?venue=%s", SV_SCHEME, SV_HOST, "%s");
	
	//Attend
    public static final String ATTEND_URI = String.format(
    		"%s://%s/seminars/attend?code=%s&sign=%s", Var.SV_SCHEME, Var.SV_HOST, "%s", "%s");
	public static final String ATTEND_API_URI = String.format(
			"%s://%s/api/seminars/attend", SV_SCHEME, SV_HOST);
	public static final String ATTEND_PARAM_NFC_TAG = "code";
	public static final String ATTEND_PARAM_SIGN = "sign";
	public static final String ATTEND_PARAM_SECRET = "secret";
	
	
	
	
	//Seminar
	public static final String SEMINAR_URI = String.format(
    		"%s://%s/api/seminars/detail?code=%s&sign=%s", Var.SV_SCHEME, Var.SV_HOST, "%s", "%s");
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:MM:ss.SSS'Z'";
	public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.JAPAN);
}
