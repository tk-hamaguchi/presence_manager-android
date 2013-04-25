package com.dennou.pman.nfc;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.util.Log;
import android.util.Xml.Encoding;

import com.dennou.pman.data.Seat;
import com.dennou.pman.data.Var;

public class PmTag {
	private String TAG = "PmTag";
	public static final String MIME = "application/com.dennou.pm";
	
	private static final byte[] madKeyA = new byte[]{(byte)0xA0, (byte)0xA1, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5};
	private static final byte[] madKeyB = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};

	public static final byte[] nfcKeyA = new byte[]{(byte)0xD3, (byte)0xF7, (byte)0xD3, (byte)0xF7, (byte)0xD3, (byte)0xF7};
	public static final byte[] nfcKeyB = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
	
	private static byte[] myKeyA = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
	private static byte[] myKeyB = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
	
    private static byte[] mad = new byte[] { 
            (byte)0x69 ,(byte)0x00 ,(byte)0x03 ,(byte)0xE1 ,(byte)0x03 ,(byte)0xE1 ,(byte)0x03 ,(byte)0xE1 ,(byte)0x03 ,(byte)0xE1 ,(byte)0x02 ,(byte)0x00 ,(byte)0x02 ,(byte)0x00 ,(byte)0x02 ,(byte)0x00
           ,(byte)0x02 ,(byte)0x00 ,(byte)0x02 ,(byte)0x00 ,(byte)0x02 ,(byte)0x00 ,(byte)0x02 ,(byte)0x00 ,(byte)0x02 ,(byte)0x00 ,(byte)0x02 ,(byte)0x00 ,(byte)0x02 ,(byte)0x00 ,(byte)0x02 ,(byte)0x00
       };
    private static final byte[] secMadacb = new byte[]{(byte)0x78, (byte)0x77, (byte)0x88};
    private static final byte[] secNfcacb = new byte[]{(byte)0x78, (byte)0x77, (byte)0x88};
    
    private static final byte[] nfcSector = new byte[]{1,2,3,4};
    private static final byte[] mySector = new byte[]{5,6,7,8,9,10,11,12,13,14,15};
    
    
    private Tag tag;
    
    private int code;
    private String secret;
    private String sign;
    
    public PmTag() {
	}
    public PmTag(Tag tag){
    	this.tag = tag;
    }
        
    public static PmTag get(NdefMessage ndef){
		try {
			PmTag pmTag = new PmTag();
			for(NdefRecord record:ndef.getRecords()){
				if(record.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
					record.getType()!=null && record.getType()[0]=='U'){
					Uri uri = Uri.parse(new String(record.getPayload(), Encoding.US_ASCII.toString()));
					if(Var.SV_HOST.equals(uri.getHost())){
						String code = uri.getQueryParameter(Var.ATTEND_PARAM_NFC_TAG);
						pmTag.code = Integer.getInteger(code);
						pmTag.sign = uri.getQueryParameter(Var.ATTEND_PARAM_SIGN);
					}
				}else if(record.getTnf() == NdefRecord.TNF_MIME_MEDIA && record.getType()!=null){
					String type = new String(record.getType(),Encoding.US_ASCII.toString());
					if(PmTag.MIME.equals(type)){
						byte dataType = record.getPayload()[0];
						if(dataType==0)
							pmTag.secret = new String(record.getPayload(), 1, record.getPayload().length-1);
					}
				}
			}
			if(pmTag.sign !=null && pmTag.secret!=null)
				return pmTag;
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public boolean initializeTag(){
		ByteBuffer bb = ByteBuffer.allocate(MifareClassic.BLOCK_SIZE);
		int srcIndex = 0;
		
		try {
			MifareClassic mf = MifareClassic.get(tag);
			mf.connect();
			//MAD 0=MAD, 1,2,3,4=NFC, 5~F=MY
			auth(mf, 0);
			
			bb.rewind();
			bb.put(PmTag.mad, srcIndex, MifareClassic.BLOCK_SIZE);
			srcIndex += MifareClassic.BLOCK_SIZE;
			mf.writeBlock(1, bb.array());
			
			bb.rewind();
			bb.put(PmTag.mad, srcIndex, MifareClassic.BLOCK_SIZE);
			mf.writeBlock(2, bb.array());
			
			bb.rewind();
			bb.put(PmTag.madKeyA);
			bb.put(PmTag.secMadacb);
			bb.put((byte)0xC1);//GPB MAD, multi app, MAD_VER=1
			bb.put(PmTag.madKeyB);
			mf.writeBlock(3, bb.array());
			
			//NFC
			bb.rewind();
			bb.put(PmTag.nfcKeyA);
			bb.put(PmTag.secNfcacb);
			bb.put((byte)0x43);//GPB Ver1.0, Read allow, write deny
			bb.put(PmTag.nfcKeyB);
			for(int s : nfcSector){
				auth(mf, s);
				mf.writeBlock(mf.sectorToBlock(s)+3, bb.array());
			}
			
			//MY
			bb.rewind();
			bb.put(PmTag.myKeyA);
			bb.put(PmTag.secNfcacb);
			bb.put((byte)0x43);//GPB Ver1.0, Read allow, write deny
			bb.put(PmTag.myKeyB);
			for(int s: mySector){
				auth(mf, s);
				mf.writeBlock(mf.sectorToBlock(s)+3, bb.array());
			}
			
			mf.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    }
    
    private void auth(MifareClassic mf, int sector) throws Exception{
		if(mf.authenticateSectorWithKeyB(sector, MifareClassic.KEY_DEFAULT)){
			Log.d(TAG, "auth with defkey"+ sector);
			return;
		}
		
		byte[] key = getKey(sector);
		if(mf.authenticateSectorWithKeyB(sector, key)){
			Log.d(TAG, "auth with my key"+ sector);
			return;
		}
		throw new Exception("key is not match!");
    }
    
    private byte[] getKey(int sector){
		if(sector == 0){
			return madKeyB;
		}
		for(int s : nfcSector){
			if(s == sector)
				return nfcKeyB;
		}
		for(int s : mySector){
			if(s == sector)
				return myKeyB;
		}
		return MifareClassic.KEY_DEFAULT;
    }
    
    @SuppressLint("DefaultLocale")
	public boolean writeSeatTag(Seat seat){
		try {
			String uri = String.format(Locale.US, Var.ATTEND_URI, seat.getId(), seat.getSign());
			byte[] uriData = uri.getBytes(Charset.forName(Encoding.US_ASCII.name()));
			ByteBuffer bb = ByteBuffer.allocate(uriData.length + 1);
			bb.put((byte)0);
			bb.put(uriData);
			
			NdefRecord primary = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, new byte[]{'U'}, new byte[0], bb.array());
			ByteBuffer bbSecret = ByteBuffer.allocate(seat.getSecret().getBytes(Encoding.US_ASCII.toString()).length + 1);
			bbSecret.put((byte)0);
			bbSecret.put(seat.getSecret().getBytes(Encoding.US_ASCII.toString()));
			
			NdefRecord secret = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
					MIME.getBytes(Encoding.US_ASCII.toString()), new byte[0], bbSecret.array());
			
			NdefMessage ndef = new NdefMessage(new NdefRecord[]{primary, secret});
			
			Ndef nf = Ndef.get(tag);
			if(nf != null){
				nf.connect();
				nf.writeNdefMessage(ndef);
				nf.close();
				return true;
			}
			
			NdefFormatable mf = NdefFormatable.get(tag);
			if(mf != null){
				mf.connect();
				mf.format(ndef);
				mf.close();
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    }

    public void readSecret() throws Exception
    {
    	MifareClassic mf = MifareClassic.get(tag);
    	auth(mf, mySector[0]);
    	byte[] block = mf.readBlock(mf.sectorToBlock(mySector[0]));
    	secret = new String(block, Encoding.US_ASCII.toString());
    }
    
	public int getCode() {
		return code;
	}

	public void setSode(int code) {
		this.code = code;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
}
