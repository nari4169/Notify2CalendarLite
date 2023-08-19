package com.nari.notify2calendar.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

public class GetGoogleCalendarID {

    String TAG = "GetGoogleCalendarID" ;
    Context context ;

    /**
     * https://developer.android.com/guide/topics/providers/calendar-provider?authuser=1&hl=ko 참조
     */
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    public GetGoogleCalendarID (Context context) {
        this.context = context ;
    }

    /**
     * 카렌더 ID 받아 오기 gmail 계정으로 등록한 것 중에서 젤 처음꺼.
     * @return
     */
    public long getGoogleCalendarID() {
        long google_index = 0 ;

        Log.i(TAG, "getGoogleCalendarID Start") ;
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "(" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?)";
        String[] selectionArgs = new String[] {"com.google"};
        // Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        // Use the cursor to step through the returned records
        if (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            google_index = calID ;

            Log.i(TAG, calID + ") displayname=" + displayName + ", accountName=" + accountName + ", ownerName=" + ownerName) ;

        }
        cur.close();

        return google_index ;
    }

    /**
     * 이벤트 등록하기
     * @param phoneNo
     * @param strBody
     * @param strDate
     * @return
     */
    public long insertEvent(Context context, String phoneNo, String strBody, String strDate) {
        long lResult = 0 ;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String eventTime = prefs.getString("event_time", "11:00").replaceAll("[^0-9]", "") ;
        String alarmTime = prefs.getString("alarm_time", "30").replaceAll("[^0-9]", "") ;

        String timezone = TimeZone.getDefault().getID(); // Asia/Korea
        long calID = getGoogleCalendarID() ;
        long startMillis = 0;
        long endMillis = 0;
        int iHour = Integer.parseInt(eventTime.substring(0, 2)) ;
        int iMin =  Integer.parseInt(eventTime.substring(2, 4)) ;

        Log.i(TAG, "timeZone=" + timezone + "," + iHour + ":" + iMin) ;

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Integer.parseInt(strDate.substring(0, 4)),
                Integer.parseInt(strDate.substring(4, 6))-1, // 월 산출은 -1 을 해야 제대로 됨
                Integer.parseInt(strDate.substring(6, 8)), iHour, iMin);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(Integer.parseInt(strDate.substring(0, 4)),
                Integer.parseInt(strDate.substring(4, 6))-1, // 월 산출은 -1 을 해야 제대로 됨
                Integer.parseInt(strDate.substring(6, 8)), iHour + 1, iMin);
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, strBody.substring(0, 10));
        values.put(CalendarContract.Events.ORGANIZER, phoneNo);
        values.put(CalendarContract.Events.DESCRIPTION, strBody);
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone); // 기준지역 timeZone 이 되도록
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        lResult = Long.parseLong(uri.getLastPathSegment());

        Log.i(TAG, "Result=" + lResult ) ;

        long eventID = lResult;
        values = new ContentValues();
        values.put(CalendarContract.Reminders.MINUTES, Integer.parseInt(alarmTime)); // 30분 전에 알림 ?
        values.put(CalendarContract.Reminders.EVENT_ID, eventID);
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);

        values = new ContentValues();
        values.put(CalendarContract.Attendees.ATTENDEE_NAME, phoneNo); // 참석자 정보 추가
        values.put(CalendarContract.Attendees.ATTENDEE_EMAIL, phoneNo);
        values.put(CalendarContract.Attendees.ATTENDEE_RELATIONSHIP, CalendarContract.Attendees.RELATIONSHIP_ATTENDEE);
        values.put(CalendarContract.Attendees.ATTENDEE_TYPE, CalendarContract.Attendees.TYPE_OPTIONAL);
        values.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_INVITED);
        values.put(CalendarContract.Attendees.EVENT_ID, eventID);
        uri = cr.insert(CalendarContract.Attendees.CONTENT_URI, values);

        return eventID ; // 이벤트 ID가 돌아가야 나중에 삭제 하니 변경 하면 안됨.
    }

    /**
     * 이벤트 삭제 처리
     * @param eventID
     * @return
     */
    public long deleteEventAll(long eventID) {
        long rows = 0 ;
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
        rows = context.getContentResolver().delete(deleteUri, null, null);
        Log.i(TAG, "Rows deleted: " + rows);
        return rows ;
    }

}
