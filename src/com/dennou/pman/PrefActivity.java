package com.dennou.pman;

import com.dennou.pman.data.TempData;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.app.Activity;
import android.view.Menu;

public class PrefActivity extends PreferenceActivity {

	private TempData tempData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.conf);
		tempData = TempData.getInstance(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		EditTextPreference prefHost = (EditTextPreference)findPreference(TempData.TAG_HOST);
		tempData.setHost(prefHost.getText());
		tempData.save(this);
	}
}
