package com.esp.common.handler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class AlertHandler extends Handler{
	public static final int ID_SHOW_MSG = 1;
	public static final int ID_SHOW_DLG = 2;
	public static final int ID_DISMISS = 3;
	public static final int ID_SHOW_TOAST = 4;
	public static final String KEY_MESSAGE = "msg";

	private IAlertActivity activity;
	private int curId;
	
	public AlertHandler(IAlertActivity activity){
		this.activity = activity;
		curId = 0;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what){
			case ID_SHOW_MSG:
				if(msg.obj != null)
					showMessage((String)msg.obj);
				else
					showMessage(msg.arg1);
				break;
				
			case ID_SHOW_DLG:
				if(msg.obj != null)
					showDialog((String)msg.obj);
				else
					showDialog(msg.arg1);
				break;
			case ID_SHOW_TOAST:
				if(msg.obj != null)
					showToast((String)msg.obj, Toast.LENGTH_SHORT);
				else
					showToast(msg.arg1, msg.arg2);
				break;
				
				
			case ID_DISMISS:
				dissmiss();
		}
	}
	
	private void showMessage(int msgId){
		if(curId != 0 && curId != ID_SHOW_MSG)
			activity.showMessage(curId, null);
		activity.showMessage(ID_SHOW_MSG, ((Context)activity).getString(msgId) );
		curId = ID_SHOW_MSG;
	}
	
	private void showMessage(String msg){
		if(curId != 0 && curId != ID_SHOW_MSG)
			activity.showMessage(curId, null);
		activity.showMessage(ID_SHOW_MSG, msg);
		curId = ID_SHOW_MSG;
	}	
	
	private void showDialog(int msgId){
		if(curId != 0 && curId != ID_SHOW_DLG)
			activity.showMessage(curId, null);
		activity.showMessage(ID_SHOW_DLG, ((Context)activity).getString(msgId) );
		curId = ID_SHOW_DLG;
	}
	
	private void showDialog(String msg){
		if(curId != 0 && curId != ID_SHOW_DLG)
			activity.showMessage(curId, null);
		activity.showMessage(ID_SHOW_DLG, msg);
		curId = ID_SHOW_DLG;
	}
	
	private void dissmiss(){
		if(curId != 0)
			activity.showMessage(curId, null);
		curId = 0;
	}
	
	private void showToast(int msgId, int duration){
		Toast.makeText(((Context)activity), msgId, duration).show();
	}
	
	private void showToast(String msg, int duration){
		Toast.makeText(((Context)activity), msg, duration).show();
	}
	
	public static Dialog getDialog(int id, Context context, Bundle args){
		String msg = args.containsKey(AlertHandler.KEY_MESSAGE) ?
							args.getString(AlertHandler.KEY_MESSAGE): "";

		Dialog dlg = null;
		switch(id){
		case AlertHandler.ID_SHOW_DLG:
			AlertDialog.Builder ab = new AlertDialog.Builder(context);
			ab.setPositiveButton("OK", null);
			ab.setMessage(msg);
			dlg = ab.create();
			break;
			
		case AlertHandler.ID_SHOW_MSG:
			dlg = new ProgressDialog(context);
			dlg.setCancelable(false);
			((ProgressDialog)dlg).setMessage(msg);
			break;
		}
		return dlg;
	}
	
	public static void prepareDialog(int id, Dialog dialog, Bundle args){
		String msg = args.getString(AlertHandler.KEY_MESSAGE);
		switch(id){
			case AlertHandler.ID_SHOW_DLG:
				((AlertDialog)dialog).setMessage(msg);
				break;
			case AlertHandler.ID_SHOW_MSG:
				((ProgressDialog)dialog).setMessage(msg);
				break;
		}
	}
	
	public static void wait(int msec){
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
