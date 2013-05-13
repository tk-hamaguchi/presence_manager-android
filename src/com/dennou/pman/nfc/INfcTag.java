package com.dennou.pman.nfc;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.Tag;

public interface INfcTag {
	public NdefMessage getNdefMessage(Context context, Tag tag);
}
