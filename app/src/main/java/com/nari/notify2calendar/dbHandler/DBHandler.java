package com.nari.notify2calendar.dbHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHandler {

	private DBHelper helper;
	private SQLiteDatabase db;

	String TAG = "DBHandler" ;
	
	public DBHandler(Context ctx) {
		helper = new DBHelper(ctx);
		db = helper.getWritableDatabase(); 
	}
	public static DBHandler open(Context ctx) throws SQLException{
		DBHandler handler = new DBHandler(ctx);
		return handler;
	}
	
	public void close(){
		helper.close();
	}

	/**
	 * 점검문자열 기록
	 * @param strValue
	 * @return 등록 건수
	 */
	public long insertStrValue(String strValue) {
		long result = 0 ;
		ContentValues values = new ContentValues();
		values.put("strValue", strValue) ;
		values.put("use_yn", "Y") ; // 사용여부는 일단 Yes
		result = db.insert("wordList", null, values) ;
		Log.i(TAG, "insert cnt=" + result) ;
		return result ;
	}

	/**
	 * 점검 문자열 사용여부 갱신
	 * @param _id
	 * @param strValue
	 * @param use_yn
	 * @return update 건수
	 */
	public long updateStrValue(int _id, String strValue, String use_yn) {
		long result = 0 ;
		ContentValues values = new ContentValues();
		values.put("strValue", strValue) ;
		values.put("use_yn", use_yn) ;
		result = db.update("wordList", values, "_id = " + _id , null );
		Log.i(TAG, "udpate cnt=" + result) ;
		return result ;
	}

	/**
	 * Lite 버전은 등록하는 갯수를 제한 합니다.
	 * @return
	 */
	public long getWordCnt(){
		long lCnt = 0 ;
		String sql = "select count(*) as lCnt from wordList where use_yn != 'N' " ;
		Cursor cursor = db.rawQuery(sql, null) ;
		cursor.moveToFirst();
		if (cursor.moveToNext()) {
			lCnt = cursor.getLong(0) ;
		}
		Log.i(TAG, "wordlist Count=" + lCnt) ;
		return lCnt;
	}

	/**
	 * 점검문자열 조회
	 * @return cursor
	 */
	public Cursor selectAll(){
		String sql = "select _id, strValue, use_yn from wordList where use_yn != 'N' order by _id desc" ;
		Cursor cursor = db.rawQuery(sql, null) ;
		return cursor;
	}

	/**
	 * 카렌다에 등록하는 내용 기록해 두기
	 * @param phoneNo
	 * @param strBody
	 * @param chkValue
	 * @param regDate
	 * @return 등록한 건수
	 */
	public long insertRecvList(String phoneNo, String strBody, String chkValue, String regDate, long eventID) {
		long result = 0 ;
		ContentValues values = new ContentValues();
		values.put("inPhoneNumber", phoneNo) ;
		values.put("strBody", strBody) ;
		values.put("chkValue", chkValue) ;
		values.put("regDate", regDate) ;
		values.put("eventID", String.valueOf(eventID)) ;
		//if (!chkString(phoneNo, strBody)) {
		result = db.insert("receiveList", null, values);
		//}
		Log.i(TAG, "insert cnt=" + result) ;
		return result ;
	}

	/**
	 * 수신 데이터 삭제하기
	 * @param _id
	 * @return
	 */
	public long deleteRecvList(int _id) {
		long result = 0 ;

		result = db.delete("receiveList", "_id =" + _id, null);

		Log.i(TAG, "deleteRecvList(" + _id + ")=" + result) ;

		return result ;
	}

	/**
	 * 해당 문자열이 이미 기록된 것인지 체크
	 * @param phoneNo
	 * @param strBody
	 * @return
	 */
	public boolean chkString(String phoneNo, String strBody) {
		boolean bResult = false ;

		String sql = "select count(*) from receiveList where inPhoneNumber = '" + phoneNo + "' and strBody = '" + strBody + "'" ;
		Cursor cursor = db.rawQuery(sql, null) ;
		if (cursor.moveToNext()) {
			try { // 혹시나 에러가 날까봐서
				int iCnt = cursor.getInt(0);
				Log.i(TAG, strBody + " 갯수=" + iCnt) ;
				if (iCnt > 0) {
					bResult = true;
				}
			} catch (Exception e) {
				Log.i(TAG, strBody + "chkString Error !!!") ;
			}
		}

		return bResult ;
	}

	/**
	 * 수신 문자열 전체 조회
	 *
	 * @return
	 */
	public Cursor selectRecvListAll(){
		String sql = "select _id, strBody, inPhoneNumber, chkValue, regDate, eventID from receiveList order by _id desc" ;
		Cursor cursor = db.rawQuery(sql, null) ;
		return cursor;
	}

}
