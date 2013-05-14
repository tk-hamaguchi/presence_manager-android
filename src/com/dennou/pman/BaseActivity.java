package com.dennou.pman;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import com.esp.common.handler.AlertHandler;
import com.esp.common.handler.IAlertActivity;

@SuppressWarnings("deprecation")
public class BaseActivity extends Activity implements IAlertActivity{
	
	protected AlertHandler alert;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		alert = new AlertHandler(this);
	}

	@Override
	public void showMessage(int id, String message) {
		try {
			if(message != null){
				Bundle bundle = new Bundle();
				bundle.putString(AlertHandler.KEY_MESSAGE, message);
				showDialog(id, bundle);
			}else{
				dismissDialog(id);
			}
		} catch (Exception e) {
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		super.onCreateDialog(id,args);
		return AlertHandler.getDialog(id, this, args);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		AlertHandler.prepareDialog(id, dialog, args);
        super.onPrepareDialog(id, dialog);
	}
}
