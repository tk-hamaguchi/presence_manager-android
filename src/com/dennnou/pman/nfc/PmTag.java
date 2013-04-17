package com.dennnou.pman.nfc;

import java.nio.ByteBuffer;

import android.nfc.tech.MifareClassic;
import android.util.Log;

public class PmTag {
	private String TAG = "PmTag";
	private static final byte[] madKeyA = new byte[]{(byte)0xA0, (byte)0xA1, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5};
	private static final byte[] madKeyB = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};

	private static final byte[] nfcKeyA = new byte[]{(byte)0xD3, (byte)0xF7, (byte)0xD3, (byte)0xF7, (byte)0xD3, (byte)0xF7};
	private static final byte[] nfcKeyB = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
	
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
    
    private MifareClassic mf;
    
    public PmTag(MifareClassic mf) {
    	this.mf = mf;
	}
    
    public boolean initializeTag(){
		ByteBuffer bb = ByteBuffer.allocate(MifareClassic.BLOCK_SIZE);
		int srcIndex = 0;
		
		try {
			mf.connect();
			//MAD 0=MAD, 1,2,3,4=NFC, 5~F=MY
			auth(0);
			
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
				auth(s);
				mf.writeBlock(mf.sectorToBlock(s)+3, bb.array());
			}
			
			//MY
			bb.rewind();
			bb.put(PmTag.myKeyA);
			bb.put(PmTag.secNfcacb);
			bb.put((byte)0x43);//GPB Ver1.0, Read allow, write deny
			bb.put(PmTag.myKeyB);
			for(int s: mySector){
				auth(s);
				mf.writeBlock(mf.sectorToBlock(s)+3, bb.array());
			}
			
			mf.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    }
    
    private void auth(int sector) throws Exception{
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
}
