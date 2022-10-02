package com.dev.xavier.tempusromanum;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
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
//AppCompatActivity
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(null);
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
            registerPermissionLauncher();
            grantNotifications();
        }
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
                // Si nécessaire demander l'autorisation aux notifications
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    grantNotifications();
                } else {
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    if (!notificationManager.areNotificationsEnabled()) {
                        // Les notifications sont désactivées dans les paramètres système
                        disableNotificationsPreferences();
                    }
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

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void grantNotifications() {
        // Si au moins une options activées, demander l'autorisation
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean romeFoundationAlert = pref.getBoolean("alert_rome_founding", false);
        final boolean nonesAlert = pref.getBoolean("alert_nones", false);
        final boolean idesAlert = pref.getBoolean("alert_ides", false);
        if ((romeFoundationAlert || nonesAlert || idesAlert) && requestPermissionLauncher != null) {
            // Vérifier s'il faut demander l'autorisation l'utilisateur
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // L'autorisation est déjà accordée
                return;
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // L'autorisation a été révoquée, proposer une explication pour le convaincre l'utilisateur d'accepter l'autorisation à nouveau
                showMessageOKCancel(getString(R.string.request_permission_notifications),
                        (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS));
            } else {
                // Demander l'autorisation
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void disableNotificationsPreferences() {

        boolean change = false;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = pref.edit();

        if(pref.getBoolean("alert_rome_founding", false)) {
            prefEditor.putBoolean("alert_rome_founding", false);
            change = true;
        }
        if(pref.getBoolean("alert_nones", false)) {
            prefEditor.putBoolean("alert_nones", false);
            change = true;
        }
        if(pref.getBoolean("alert_ides", false)) {
            prefEditor.putBoolean("alert_ides", false);
            change = true;
        }

        if(change) {
            // Sauvegarder les changements
            prefEditor.apply();
            // Avertir que le système empèche l'activation des notifications
            Toast.makeText(this, getString(R.string.notification_premission_error), Toast.LENGTH_LONG).show();
            // Recharger la page pour prendre en compte les nouvelles valeurs
            reloadActivity();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SettingsActivity.this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), okListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }

    private void reloadActivity() {
        // Supprimer les dépendances si nécessaire
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            unregisterPermissionLauncher();
        }

        // Recreate
        startActivity(Intent.makeRestartActivityTask(getIntent().getComponent()));
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void registerPermissionLauncher() {
        // Listener retour du dialogue système d'autorisation des notifications
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                // Désactiver les options de notification dans les préférences
                disableNotificationsPreferences();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void unregisterPermissionLauncher()
    {
        if ( requestPermissionLauncher != null ) {
            try {
                requestPermissionLauncher.unregister();
            } catch (Exception ignored) {
            }
            requestPermissionLauncher = null;
        }
    }
}