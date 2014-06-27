package com.lanian.btbeacon;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class BlueBeaconDBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "bluebeacon.db";
	private static final int DB_VERSION = 1;
	
	public static abstract class MessageEntry implements BaseColumns {
		public static final String TABLE_NAME = "messages";
		public static final String COLUMN_NAME_ADDRESS = "address";
		public static final String COLUMN_NAME_DIRECTION = "direction";
		public static final String COLUMN_NAME_MESSAGE = "message";
		public static final String COLUMN_NAME_TIME = "time";
		
		public static final int COLUMN_VALUE_DIRECTION_RECEIVE = 0;
		public static final int COLUMN_VALUE_DIRECTION_SEND = 1;
	}
	
	public static abstract class BeaconEntry implements BaseColumns {
		public static final String TABLE_NAME = "beacons";
		public static final String COLUMN_NAME_ADDRESS = "address";
		public static final String COLUMN_NAME_ALIAS = "alias";
		public static final String COLUMN_NAME_BANNED = "banned";		
	}
	
	private static final String SQL_CREATE_MESSAGE_ENTRIES = 
			"CREATE TABLE "+MessageEntry.TABLE_NAME+" ("+
					MessageEntry._ID+" INTEGER PRIMARY KEY,"+
					MessageEntry.COLUMN_NAME_ADDRESS+" TEXT,"+
					MessageEntry.COLUMN_NAME_DIRECTION+" INTEGER,"+
					MessageEntry.COLUMN_NAME_MESSAGE+" TEXT,"+
					MessageEntry.COLUMN_NAME_TIME+" TEXT"+
			")";
	
	private static final String SQL_CREATE_BEACON_ENTRIES = 
			"CREATE TABLE "+BeaconEntry.TABLE_NAME+" ("+
					BeaconEntry._ID+" INTEGER PRIMARY KEY,"+
					BeaconEntry.COLUMN_NAME_ADDRESS+" TEXT UNIQUE,"+
					BeaconEntry.COLUMN_NAME_ALIAS+" TEXT,"+
					BeaconEntry.COLUMN_NAME_BANNED+" INTEGER"+
					
			")";
	
	public BlueBeaconDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_MESSAGE_ENTRIES);
		db.execSQL(SQL_CREATE_BEACON_ENTRIES);
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public Cursor queryConversation(SQLiteDatabase db) {
		String sql = "SELECT DISTINCT "+
				MessageEntry.TABLE_NAME+"."+MessageEntry.COLUMN_NAME_ADDRESS+","+BeaconEntry.TABLE_NAME+"."+BeaconEntry.COLUMN_NAME_ALIAS+
				" FROM "+MessageEntry.TABLE_NAME+
				" LEFT JOIN "+BeaconEntry.TABLE_NAME+
				" ON "+MessageEntry.TABLE_NAME+"."+MessageEntry.COLUMN_NAME_ADDRESS+"="+BeaconEntry.TABLE_NAME+"."+BeaconEntry.COLUMN_NAME_ADDRESS+
				" WHERE "+BeaconEntry.TABLE_NAME+"."+BeaconEntry.COLUMN_NAME_BANNED+" IS NULL OR "+BeaconEntry.TABLE_NAME+"."+BeaconEntry.COLUMN_NAME_BANNED+"=0";
		return db.rawQuery(sql, null);
	}
}
