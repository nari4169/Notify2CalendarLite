package com.nari.notify2calendar.dbHandler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	private static final String DB_NAME = "SMS2CALENDAR" ;
	private static final int DB_Ver = 1 ; //
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_Ver);
	}
	@Override 
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table wordList (" +
				"_id integer primary key autoincrement, " +
				"strValue text," +  // 점검할 문자열
				"use_yn text" +     // 사용여부
			")";
		db.execSQL(sql) ;

		sql = "create table receiveList (" +
				"_id integer primary key autoincrement, " +
				"inPhoneNumber text," +  // 전화번호
				"strBody text," +        // 내용
				"chkValue text," +       // 점검한 문자
				"regDate text," +        // 카렌다에 등록할 날자
				"eventID text" +         // 카렌다에 등록한 EventID
				")";
		db.execSQL(sql) ;
	}

	/**
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 */
	@Override 
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/* 삭제하면 안됨.
		db.execSQL("drop table if exists UploadImageList") ;
		onCreate(db) ;*/
		if ( newVersion == 2) {

		}

	}
	
}
