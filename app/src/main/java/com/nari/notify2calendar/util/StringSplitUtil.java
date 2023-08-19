package com.nari.notify2calendar.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nari.notify2calendar.dbHandler.DBHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringSplitUtil {


    String TAG = "StringSplitUtil" ;

    Context context ;

    SharedPreferences prefs;

    boolean eventTy = false ;
    String eventTime = "" ;
    String alarmTime = "" ;

    public StringSplitUtil(Context context) {
        this.context = context ;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        eventTy = prefs.getBoolean("event_ty", false) ;
        eventTime = prefs.getString("event_time", "11:00") ;
        alarmTime = prefs.getString("alarm_time", "30") ;

    }

    /**
     * 문자열 줄바꿈 으로 나누기
     * @param strBody
     */
    public int doStringSpilt(String phoneNo, String strBody) {

        int iPos = 0 ;

        String lines[] = strBody.split("\\r?\\n");
        for(int i=0; i < lines.length ; i++) {

            Log.i(TAG, i + "=" + lines[i]);
            iPos = doSplitString(lines[i], ".", strBody, phoneNo) ;
            if (iPos < 1) {
                iPos = doSplitString(lines[i], "-", strBody, phoneNo);
            }
            if (iPos < 1) {
                iPos = doSplitString(lines[i], "/", strBody, phoneNo);
            }
            if (iPos < 1) {
                iPos = doSplitString(lines[i], "일", strBody, phoneNo);
            }
            if (iPos < 1) {
                iPos = doSplitString(lines[i], "월", strBody, phoneNo);
            }
        }
        return iPos ;
    }

    /**
     * 문자열에 날자를 구분하는 값을 찾아서 기록할 준비를 한다.
     * @param line      : 구분할 문자열
     * @param strSpilt  : 점검할 구분자
     * @param strBody   : 전체 문장
     * @return          : 찾은 위치
     */
    private int doSplitString(String line, String strSpilt, String strBody, String phoneNo) {
        int iPos = 0 ;
        int iLen = 0 ;
        long iEventID = 0 ;

        Log.i(TAG, "line=" + line + ", eventTy=" + eventTy);
        do {
            iPos = line.indexOf(strSpilt, iLen) ;
            if (iPos != -1) {
                String sDate = getDate(line, strSpilt) ;
                String chkStr = chkString(line) ;
                if (!"".equals(sDate) && !"".equals(chkStr)) {
                    Log.i(TAG, "doSplitString(" + strSpilt + "," + phoneNo + "," + sDate + "," + chkStr + ")" + strBody + "\n\n\neventTy=" + eventTy) ;
                    DBHandler dbHandler = DBHandler.open(context);
                    if (!dbHandler.chkString(phoneNo, strBody)) {
                        if (eventTy) {
                            GetGoogleCalendarID getGoogleCalendarID = new GetGoogleCalendarID(context);
                            iEventID = getGoogleCalendarID.insertEvent(context, phoneNo, strBody, sDate);
                        }
                        dbHandler.insertRecvList(phoneNo, strBody, chkStr, sDate, iEventID) ;
                    }
                    dbHandler.close();
                }
                iLen += iPos ;
                iLen++; // 한글자 다음부터
            } else {
                iLen = line.length() ; // 한개도 없으면 그냥 종료
            }
        } while(iLen < line.length()) ;

        /* 2019.08.23 구분자와 날자는 찾았지만, 일정에 등록하지 않으면 다른 구분자로 찾아 보도록 개선 */
        if (iEventID == 0) {
            iPos = -1 ;
        }
        return iPos ;
    }

    /**
     * 문자열에 점검할 글자가 들어 있는 지 확인 하기
     * @param line
     * @return
     */
    private String chkString(String line) {
        String bResult = "" ;

        DBHandler dbHandler = DBHandler.open(context);
        Cursor rs = dbHandler.selectAll() ;
        while (rs.moveToNext()) {
            Log.i(TAG, "chkString=" +rs.getString(1));
            if (line.indexOf(rs.getString(1)) != -1) {
                bResult = rs.getString(1) ;
                break ;
            }
        }
        dbHandler.close();
        return bResult ;
    }


    /**
     * 문자열에서 날자만 골라 내기
     * @param line : 원본 문자열
     * @param strSpilt : 날자글 구분하는 값
     */
    public String getDate(String line, String strSpilt) {
        String strDate = "" ;
        try {
            String tYear = getToday().substring(0, 4); // 지금시점의 연도 구함

            Log.i(TAG, line + "][" + strSpilt);

            int iPos = 0;
            do {
                iPos = line.indexOf(strSpilt, iPos + 1);
                if (iPos != -1) {
                    strDate = getLine2Date(line, iPos, strSpilt);
                }
                Log.e(TAG, iPos + "=" + strDate);
            } while (iPos + 1 < line.length() && iPos != -1 && !validateDate(strDate));

            Log.i(TAG, "getDate(" + strDate + ") returned");
        } catch (Exception e) {

        }
        return strDate ;
    }

    /**
     * line 에 날자가 되는 숫자가 있나 ?
     * @param line
     * @param iPos
     * @param strSplit : 월, 일 등이 오는 경우를 찾기 위해서...
     * @return
     */
    public String getLine2Date(String line, int iPos, String strSplit) {
        String strDate = "" ;
        try {
            String tYear = getToday().substring(0, 4); // 지금시점의 연도 구함
            Log.i(TAG, "getLine2Date(" + line + "," + iPos + ") start");
            if ("일".equals(strSplit)) {
                String iDay = getNumeric(line, iPos) ;
                strDate = getToday().substring(0, 6) + iDay;
            } else if ("월".equals(strSplit)) {
                int iDayPos = line.indexOf("일", iPos); // 월 뒤에 일이 오는 경우만
                if (iDayPos > 0) {
                    String iMonth = getNumeric(line, iPos) ;
                    String iDay = getNumeric(line, iDayPos) ;
                    Log.e(TAG, "월 일 [" + iMonth + "/" + iDay + "]") ;
                    strDate = tYear + iMonth + iDay ;
                } else {
                    strDate = "" ;
                }
            } else if (tYear.equals(line.substring(iPos - 4, iPos))) {
                // 앞에 4개가 연도 라면 yyyy-mm-dd 라고 본다
                Log.e(TAG, "연도일치=" + line.substring(iPos - 4, iPos) + "/" + line.substring(iPos + 1, iPos + 3) + "/" + line.substring(iPos + 4, iPos + 7));
                strDate = line.substring(iPos - 4, iPos) + line.substring(iPos + 1, iPos + 3) + line.substring(iPos + 4, iPos + 7);
            } else if (isNumeric(line.substring(iPos - 2, iPos - 1)) && isNumeric(line.substring(iPos - 2, iPos)) && isNumeric(line.substring(iPos + 1, iPos + 3))) {
                // 구분자가 있다면 위치에서 앞2글자/뒤2글자 가 숫자 일때
                Log.e(TAG, "00/00=" + line.substring(iPos - 2, iPos) + "/" + line.substring(iPos + 1, iPos + 3));
                strDate = tYear + line.substring(iPos - 2, iPos) + line.substring(iPos + 1, iPos + 3);
                strDate = strDate.replaceAll(" ", "0"); // _7/21 처럼 숫자앞에 공백이 오는 경우
            } else if (isNumeric(line.substring(iPos - 1, iPos)) && isNumeric(line.substring(iPos + 1, iPos + 3))) {
                // 구분자가 있다면 위치에서 앞1글자/뒤2글자 가 숫자 일때
                Log.e(TAG, "0/00=" + line.substring(iPos - 1, iPos) + "/" + line.substring(iPos + 1, iPos + 3));
                strDate = tYear + "0" + line.substring(iPos - 1, iPos) + line.substring(iPos + 1, iPos + 3);
            } else {
                strDate = "";
            }
            // 날자 검증이 안되면 버림
            if (!validateDate(strDate)) {
                strDate = "";
            }
            strDate = strDate.substring(0, 8);
        } catch (Exception e) {
            strDate = "" ;
        }

        Log.i(TAG, "getLine2Date(" + line + "," + iPos + ") end return Value=[" + strDate + "]") ;

        return strDate ;
    }

    /**
     * 문자열에서 특정위치 뽑아오기
     * @param line
     * @param iPos
     * @return
     */
    private String getNumeric(String line, int iPos) {
        return line.substring(iPos-2, iPos) ;
    }

    /**
     * 오늘 날자 리턴
     * @return
     */
    private String getToday() {
        String strToday = "" ;

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        strToday = sdf.format(date);

        return strToday ;
    }

    /**
     * 출처: https://khanorder.tistory.com/entry/JAVA-isNumeric [호짱의 개발 블로그]
     * @param input
     * @return
     */
    public boolean isNumeric(String input) {
        try {
            Double.parseDouble(input);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }


    /**
     * 날자인지 확인
     * @param checkDate
     * @return
     */
    public  boolean  validateDate(String checkDate){

        Log.i(TAG, "validateDate(" + checkDate + ") start... ") ;
        Log.i(TAG, "validateDate(" + checkDate.substring(0, 4) + ") start... ") ;
        // 2019.07.19 1901 ~ 2135 년까지만 등록 가능
        if (Integer.parseInt(checkDate.substring(0, 4)) >= 1901 && Integer.parseInt(checkDate.substring(0, 4)) <= 2135 ) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                dateFormat.setLenient(false);
                dateFormat.parse(checkDate);
                return true;

            } catch (Exception e) {
                return false;
            }
        } else {
            return false ;
        }

    }

}
