package com.dennou.pman.data;

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
	
}
