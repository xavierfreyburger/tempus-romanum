package com.dev.xavier.tempusromanum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

/**
 * Copyright 2019 Xavier Freyburger
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SettingsActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener {

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_activity_settings);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupSharedPreferences();

        // Si nécessaire demander l'autorisation aux notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher = NotificationPermissionHelper.registerPermissionLauncher(this, true, true);
        }
        NotificationPermissionHelper.checkPermission(this, requestPermissionLauncher,true, true);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        // Met à jour la locale
        super.attachBaseContext(LocaleHelper.updateLanguage(base));
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "alert_rome_founding":
            case "alert_nones":
            case "alert_ides": {
                // Test la permission aux notifications
                if(sharedPreferences.getBoolean(key, false)) {
                    NotificationPermissionHelper.checkPermission(this, requestPermissionLauncher,true, true);
                }
                break;
            }
            case "force_latin":
                // Recharger la vue des paramètres pour que le changement de langue soit pris en compte
                reloadActivity();
                break;
            default:
                break;
        }
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void reloadActivity() {
        // Recreate
        startActivity(Intent.makeRestartActivityTask(getIntent().getComponent()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationPermissionHelper.unregisterPermissionLauncher(requestPermissionLauncher);
        }
    }
}