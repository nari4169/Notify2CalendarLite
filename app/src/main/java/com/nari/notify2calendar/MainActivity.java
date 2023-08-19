package com.nari.notify2calendar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nari.notify2calendar.dbHandler.DBHandler;
import com.nari.notify2calendar.option.SettingActivity;
import com.nari.notify2calendar.util.BackPressCloseHandler;
import com.nari.notify2calendar.util.FileUtil;
import com.nari.notify2calendar.util.GetGoogleCalendarID;
import com.nari.notify2calendar.util.ViewRecvList;
import com.nari.notify2calendar.util.ViewRecvListAdapter;
import com.nari.notify2calendar.util.utilToast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity" ;
    ArrayList<String> chkPermission = new ArrayList<String>() ;
    final static int PERMISSION_REQUEST_CODE = 1000 ;

    ListView listRecvList ;
    ViewRecvListAdapter adapter ;
    ArrayList<ViewRecvList> listViewItemList ;

    SharedPreferences prefs;

    boolean eventTy = false ;
    String eventTime = "" ;
    String alarmTime = "" ;

    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backPressCloseHandler = new BackPressCloseHandler(this);

        checkFunction("onCreate");

        if (!permissionGrantred()) {
            Intent intent = new Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        Intent intent=new Intent(this.getIntent());
        String sender=intent.getStringExtra("sender");
        String contents=intent.getStringExtra("contents");
        String receivedDate=intent.getStringExtra("receivedDate");

        Log.i(TAG, "sender=" + sender);
        Log.i(TAG, "contents=" + contents);
        Log.i(TAG, "receivedDate=" + receivedDate);

        listRecvList = findViewById(R.id.viewRecvList1) ;
        listViewItemList = new ArrayList<ViewRecvList>() ;

        listRecvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.i(TAG, "position=" + position + ", id=" + id) ;
                for(int i = 0; i < listViewItemList.size() ; i++) {
                    Log.i(TAG, "(" + i + ")=[" + listViewItemList.get(i).getId() + "]" ) ;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getString(R.string.lableDelete))
                        .setMessage(getString(R.string.dialogDelete))
                        .setPositiveButton(getString(R.string.lableDelete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DBHandler dbHandler = DBHandler.open(MainActivity.this) ;
                                long lCnt = dbHandler.deleteRecvList(Integer.parseInt(listViewItemList.get((int) position).getId()) ) ;
                                dbHandler.close();
                                if (lCnt > 0) {

                                    // 등록을 사용 할 때
                                    if (eventTy) {
                                        GetGoogleCalendarID getGoogleCalendarID = new GetGoogleCalendarID(getApplicationContext());
                                        getGoogleCalendarID.deleteEventAll(Long.parseLong(listViewItemList.get((int) position).getEventID()));
                                    }
                                    // 삭제를 했으니 새로 그리자
                                    doListViewRefresh() ;
                                }

                            }
                        })
                        .setNegativeButton(getString(R.string.lableCancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed() ;
            }
        });
    }

    /**
     * 화면이 다시 돌아오면 새로 보여주기 위해서
     */
    @Override
    protected void onResume() {
        super.onResume();

        doListViewRefresh();
    }

    /**
     * 뒤로 가기 버튼
     */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        backPressCloseHandler.onBackPressed();

    }

    /**
     * 리스트뷰 채워주기
     */
    private boolean doListViewRefresh() {

        boolean bResult = false ;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        eventTy = prefs.getBoolean("event_ty", false) ;
        eventTime = prefs.getString("event_time", "11:00") ;
        alarmTime = prefs.getString("alarm_time", "30") ;

        Log.i(TAG, "Option(" + eventTime + "," + alarmTime + "," + eventTy + ")") ;

        listViewItemList.clear();
        DBHandler dbHandler = DBHandler.open(this) ;
        Cursor rs = dbHandler.selectRecvListAll() ;
        while (rs.moveToNext()) {
            ViewRecvList viewRecvList = new ViewRecvList() ;
            viewRecvList.setId(rs.getString(rs.getColumnIndex("_id")));
            viewRecvList.setStrBody(rs.getString(rs.getColumnIndex("strBody")));
            viewRecvList.setChkValue(rs.getString(rs.getColumnIndex("chkValue")));
            viewRecvList.setRegDate(rs.getString(rs.getColumnIndex("regDate")));
            viewRecvList.setEventId(rs.getString(rs.getColumnIndex("eventID")));
            viewRecvList.setInPhoneNumber(rs.getString(rs.getColumnIndex("inPhoneNumber")));

            listViewItemList.add(viewRecvList) ;

        }
        rs.close();
        adapter = new ViewRecvListAdapter(listViewItemList) ;
        listRecvList.setAdapter(adapter);

        Log.i(TAG, "count=" + adapter.getCount());
        return bResult ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_strValue_mng1:

                Intent intent = new Intent(getApplicationContext(), com.nari.notify2calendar.strValueManager.class);
                startActivity(intent);

                return true ;
/* 2019.3 이후 google 에서 sms 의 읽기 쓰기를 할 수 없도록 하고 있음.
            case R.id.action_ReceivedSMS:

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 실행하는 시간을 측정할 수 없기 때문에
                        getReceivedSMSMMS() ;
                    }
                }).start();

                return true ;*/
            case R.id.action_backup1:
                checkFunction("WRITE_EXTERNAL_STORAGE");
                if (isExternalStorageAvail()) {
                    new MainActivity.ExportDatabaseTask().execute();
                    SystemClock.sleep(500);
                } else {
                    Log.i(TAG, "외장디스크 권한 ?");
                    utilToast.makeToast(MainActivity.this,
                            getString(R.string.msgNotBackup), Toast.LENGTH_SHORT)
                            .show();
                }
                return true ;
            case R.id.action_option:

                Intent intentOp = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intentOp);

                return true ;
            case R.id.action_help:

                Intent openURL = new Intent(Intent.ACTION_VIEW);
                openURL.setData(Uri.parse("https://6k2emgblog.wordpress.com/2019/07/02/sms-mms-%ec%9d%84-calendar-%ec%9d%bc%ec%a0%95%ec%9c%bc%eb%a1%9c-%eb%b3%b4%eb%82%b4%eb%8a%94-%eb%b0%a9%eb%b2%95/"));
                startActivity(openURL);

                /* 달력에 있는 거 삭제는 일단 보류
                GetGoogleCalendarID getGoogleCalendarID = new GetGoogleCalendarID(this) ;
                long google_index = getGoogleCalendarID.getGoogleCalendarID() ;
                Log.i(TAG, "google_index=" + google_index) ;
                DBHandler dbHandler = DBHandler.open(this);
                Cursor rs = dbHandler.selectRecvListAll() ;
                while (rs.moveToNext()) {
                    long iCnt = getGoogleCalendarID.deleteEventAll(rs.getLong(4));
                    Log.i(TAG, "" + iCnt) ;
                }
                dbHandler.close();
                */

                return true ;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 외장 MicroSD Card 사용 가능 한가?
     * @return
     */
    private boolean isExternalStorageAvail() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 백업 하기
     */
    private class ExportDatabaseTask extends AsyncTask<Void, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        // can use UI thread here
        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.msgBackup));
            dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Boolean doInBackground(final Void... args) {

            File dbFile = new File(Environment.getDataDirectory() + "/data/com.nari.notify2calendar/databases/SMS2CALENDAR");
            File exportDir = new File(Environment.getExternalStorageDirectory(), "SMS2CALENDAR");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, dbFile.getName() + ".sqlite"); // 2019.05.29 확장자를 지정하다.

            try {
                file.createNewFile();
                FileUtil.copyFile(dbFile, file);
                return true;
            } catch (IOException e) {
                Log.i(TAG, "ERROR=" + e.toString()) ;
                return false;
            }

        }

        // can use UI thread here
        @Override
        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (success) {
                utilToast.makeToast(MainActivity.this, getString(R.string.msgBackupCompiled), Toast.LENGTH_SHORT).show();
            } else {
                utilToast.makeToast(MainActivity.this, getString(R.string.msgNotBackup), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 권한 검증
     * @return
     */
    public boolean checkFunction(String pOption){
        boolean bResult = false ;
        chkPermission.clear();
        int permissioninfo = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) ;
        if (permissioninfo != PackageManager.PERMISSION_GRANTED) {
            chkPermission.add(Manifest.permission.WRITE_CALENDAR);
        }
        permissioninfo = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) ;
        if (permissioninfo != PackageManager.PERMISSION_GRANTED) {
            chkPermission.add(Manifest.permission.READ_CALENDAR);
        }

        if ("WRITE_EXTERNAL_STORAGE".equals(pOption)){
            permissioninfo = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ;
            if (permissioninfo != PackageManager.PERMISSION_GRANTED) {
                chkPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (chkPermission.size() > 0) {
            String strArray[] = new String[chkPermission.size()] ;
            strArray = chkPermission.toArray(strArray) ;
            ActivityCompat.requestPermissions(this, strArray, PERMISSION_REQUEST_CODE);
        }

        return bResult ;
    }

    /**
     * 노티에 대한 권한 ?
     * @return
     */
    private boolean permissionGrantred() {
        Set<String> sets = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (sets != null && sets.contains(getPackageName())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 권한을 얻었다면
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i(TAG, "requestCode=" + requestCode) ;
        switch(requestCode) {
            case PERMISSION_REQUEST_CODE:
                checkFunction("");
                break ;
            default:
                /// finish();
                break ;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


}
