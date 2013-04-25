package com.dennou.pman.logic;

public class Utility {
	public static boolean sequenceEqual(byte[] a, byte[]b){
		if(a!=null&&b==null || a==null&&a!=null)
			return false;
		if(a==null && b==null)
			return false;
		
		if(a.length != b.length)
			return false;
		
		for(int i=0; i<a.length; i++){
			if(a[i] != b[i])
				return false;
		}
		return false;
	}
}
