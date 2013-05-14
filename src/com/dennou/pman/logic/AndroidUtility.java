package com.dennou.pman.logic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AndroidUtility {
	public static boolean openUri(Context context, String uri){
		try {
			Intent it = new Intent(Intent.ACTION_VIEW);
			it.addCategory(Intent.CATEGORY_DEFAULT);
			it.setData(Uri.parse(uri));
			context.startActivity(it);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
