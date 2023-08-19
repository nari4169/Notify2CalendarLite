package com.nari.notify2calendar.option;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import androidx.annotation.Nullable;

import com.nari.notify2calendar.R;

public class SettingPreferenceFragment extends PreferenceFragment {

    SharedPreferences prefs;

    ListPreference eventTimePreference;
    ListPreference alarmTimePreference;
    SwitchPreference eventTyPreference;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting_preference);
        eventTimePreference = (ListPreference)findPreference("event_time");
        alarmTimePreference = (ListPreference)findPreference("alarm_time");
        eventTyPreference = (SwitchPreference)findPreference("event_ty");

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if(!prefs.getString("event_time", "").equals("")){
            eventTimePreference.setSummary(prefs.getString("event_time", "11:00"));
        }

        if(!prefs.getString("alarm_time", "").equals("")){
            alarmTimePreference.setSummary(prefs.getString("alarm_time", getString(R.string.before15min)));
        }

        if(prefs.getBoolean("event_ty", false)) {
            eventTyPreference.setSummary(getString(R.string.labelTrue));
        } else {
            eventTyPreference.setSummary(getString(R.string.labelFalse));
        }

        prefs.registerOnSharedPreferenceChangeListener(prefListener);

    }// onCreate

    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("event_time")){
                eventTimePreference.setSummary(prefs.getString("event_time", "11:00"));
            }

            if(key.equals("alarm_time")){
                alarmTimePreference.setSummary(prefs.getString("alarm_time", getString(R.string.before15min)));
            }

            if(prefs.getBoolean("event_ty", false)) {
                eventTyPreference.setSummary(getString(R.string.labelTrue));
            } else {
                eventTyPreference.setSummary(getString(R.string.labelFalse));
            }
        }
    };

}
