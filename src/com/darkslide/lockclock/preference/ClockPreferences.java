/*
 * Copyright (C) 2012 Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.darkslide.lockclock.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.format.DateFormat;

import com.darkslide.lockclock.ClockWidgetProvider;
import com.darkslide.lockclock.R;
import com.darkslide.lockclock.misc.Constants;

public class ClockPreferences extends PreferenceFragment implements
    OnSharedPreferenceChangeListener {

    private Context mContext;
    private ListPreference mClockFontColor;
    private ListPreference mAlarmFontColor;
    private CheckBoxPreference mAmPmToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        addPreferencesFromResource(R.xml.preferences_clock);

        mContext = getActivity();
        mClockFontColor = (ListPreference) findPreference(Constants.CLOCK_FONT_COLOR);
        mAlarmFontColor = (ListPreference) findPreference(Constants.CLOCK_ALARM_FONT_COLOR);
        mAmPmToggle = (CheckBoxPreference) findPreference(Constants.CLOCK_AM_PM_INDICATOR);

        updateFontColorsSummary();
        updateAmPmToggle();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
        Intent updateIntent = new Intent(mContext, ClockWidgetProvider.class);
        mContext.sendBroadcast(updateIntent);
    }

    private void updateFontColorsSummary() {
        if (mClockFontColor != null) {
            mClockFontColor.setSummary(mClockFontColor.getEntry());
        }
        if (mAlarmFontColor != null) {
            mAlarmFontColor.setSummary(mAlarmFontColor.getEntry());
        }
    }

    private void updateAmPmToggle() {
        if (DateFormat.is24HourFormat(mContext)) {
            mAmPmToggle.setEnabled(false);
        } else {
            mAmPmToggle.setEnabled(true);
        }
    }
}
