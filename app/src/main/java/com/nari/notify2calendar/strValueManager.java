package com.nari.notify2calendar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nari.notify2calendar.dbHandler.DBHandler;
import com.nari.notify2calendar.util.ViewStrValue;
import com.nari.notify2calendar.util.ViewStrValueAdapter;

import java.util.ArrayList;

public class strValueManager extends AppCompatActivity {

    ListView listStrValue ;
    ViewStrValueAdapter adapter ;
    String TAG = "strValueManager" ;
    ArrayList<ViewStrValue> listViewItemList ;
    int strCnt = 0 ;
    static private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_str_value_manager);
        listStrValue =  findViewById(R.id.viewStrValue);
        listViewItemList = new ArrayList<ViewStrValue>();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.e(TAG, "onAdLoaded");
            }
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.e(TAG, "onAdClosed");
            }

            @Override
            public void onAdOpened() {
                Log.e(TAG, "onAdOpened");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.e(TAG, "onAdClicked");
            }
            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.e(TAG, "onAdImpression");
            }
        });

        listStrValue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), StrValueAdd.class);
                intent.putExtra("mode", "update");
                intent.putExtra("Id", listViewItemList.get(position).getId());
                intent.putExtra("StrValue", listViewItemList.get(position).getStrValue());
                startActivity(intent);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), StrValueAdd.class);
                intent.putExtra("mode", "insert");
                startActivity(intent);

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
     * ListView 에 다가 입력한 값을 보여 줍니다.
     */
    private void doListViewRefresh() {

        listViewItemList.clear(); // 이미 배열에 등록된 자료가 있으니 다시 오면 계속 추가 되서.

        DBHandler dbHandler = DBHandler.open(this) ;
        Cursor rs = dbHandler.selectAll() ;
        while (rs.moveToNext()) {

            ViewStrValue viewStrValue = new ViewStrValue() ;
            viewStrValue.setId(rs.getString(0));
            viewStrValue.setStrValue(rs.getString(1));
            listViewItemList.add(viewStrValue) ;

            Log.i(TAG, "getId      =" + viewStrValue.getId()) ;
            Log.i(TAG, "getStrValue=" + viewStrValue.getStrValue()) ;

        }
        rs.close();

        // 리스트뷰 참조 및 Adapter달기

        adapter = new ViewStrValueAdapter(listViewItemList) ;
        listStrValue.setAdapter(adapter);

        strCnt = adapter.getCount() ;
        Log.i(TAG, "Count=" + strCnt) ;

    }

}
