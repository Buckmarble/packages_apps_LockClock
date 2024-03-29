/*
 * Copyright (C) 2013 David van Tonder
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

package com.darkslide.lockclock.weather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.darkslide.lockclock.misc.Constants;
import com.darkslide.lockclock.misc.Preferences;
import com.darkslide.lockclock.misc.WidgetUtils;
import com.darkslide.lockclock.R;

public class ForecastActivity extends Activity implements OnClickListener {
    private static final String TAG = "ForecastActivity";

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Stop the animation
            ImageView view = (ImageView) findViewById(R.id.weather_refresh);
            view.setAnimation(null);

            if (!intent.getBooleanExtra(WeatherUpdateService.EXTRA_UPDATE_CANCELLED, false)) {
                updateForecastPanel();
            }
        }
    };

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // If we are in keyguard, override the default transparent theme
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = km.isKeyguardLocked();
        if (locked) {
            if (WidgetUtils.isTranslucencyAvailable()) {
                setTheme(android.R.style.Theme_Holo_NoActionBar_TranslucentDecor);
            } else {
                setTheme(android.R.style.Theme_Holo_NoActionBar);
            }
        }
        super.onCreate(savedInstanceState);

        // Get the window ready
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (locked) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            final Drawable wallpaperDrawable = wallpaperManager.getFastDrawable();
            window.setBackgroundDrawable(wallpaperDrawable);
        } else if (WidgetUtils.isTranslucencyAvailable()) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        registerReceiver(mUpdateReceiver, new IntentFilter(WeatherUpdateService.ACTION_UPDATE_FINISHED));
        updateForecastPanel();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mUpdateReceiver);
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
    }

    private void updateForecastPanel() {
        // Get the forecasts data
        WeatherInfo weather = Preferences.getCachedWeatherInfo(this);
        if (weather == null) {
            Log.e(TAG, "Error retrieving forecast data, exiting");
            finish();
            return;
        }

        View fullLayout = ForecastBuilder.buildFullPanel(this, R.layout.forecast_activity, weather);
        setContentView(fullLayout);
        fullLayout.requestFitSystemWindows();

        // Register an onClickListener on Weather refresh
        findViewById(R.id.weather_refresh).setOnClickListener(this);

        // Register an onClickListener on the fake done button
        findViewById(R.id.button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.button) {
            // Setup anim with desired properties and start the animation
            ImageView view = (ImageView) findViewById(R.id.weather_refresh);
            RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setInterpolator(new LinearInterpolator());
            anim.setRepeatCount(Animation.INFINITE);
            anim.setDuration(700);
            view.startAnimation(anim); 

            Intent i = new Intent(this, WeatherUpdateService.class);
            i.setAction(WeatherUpdateService.ACTION_FORCE_UPDATE);
            startService(i);
        } else {
            finish();
        }
    }
}
