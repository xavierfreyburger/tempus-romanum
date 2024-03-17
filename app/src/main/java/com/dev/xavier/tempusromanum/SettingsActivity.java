package com.dev.xavier.tempusromanum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;

import java.util.Objects;

/**
 * Copyright 2019 Xavier Freyburger
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">License link</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SettingsActivity extends AppCompatActivity {

    private final static String SCROLL_TO_KEY = "scrollTo";
    private SettingsFragment settingsFragment;
    private boolean processingPermissionsResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_activity_settings);
        setContentView(R.layout.settings_activity);

        settingsFragment = new SettingsFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Si demandé, scroller jusqu'à l'option demandée
        Bundle b = getIntent().getExtras();
        if(b != null) {
            String key = b.getString(SCROLL_TO_KEY);
            if (key != null) {
                settingsFragment.scrollToPreference(key);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!processingPermissionsResult) {
            // Tester la permission au notifications
            NotificationPermissionHelper.checkPermission(this, true, this::disableNotificationInSettingsFragment);
        } else {
            // Désactivation du lock lié au résultat système de test des notifications
            processingPermissionsResult = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Retour sur MainActivity lors de l'appui sur le bouton retour du menu
            // Nécessaire en cas de mise en arrière plan alors que le menu des options est affiché
            final Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (upIntent != null) {
                if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == NotificationPermissionHelper.getRequestCode()) {
            // onRequestPermissionsResult est appelé avant onResume, on peut donc mettre en place un lock
            processingPermissionsResult = true;
            NotificationPermissionHelper.handlePermissionResult(grantResults, this, true, this::disableNotificationInSettingsFragment);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        // Met à jour la locale
        super.attachBaseContext(LocaleHelper.updateLanguage(base));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    /**
     * Recharger complètement la vue
     * @param scrollTopreferenceKey Key de l'option jusqu'à laquelle il faudra scroller à l'initialisation de la vue
     */
    public void reloadActivity(String scrollTopreferenceKey) {
        Intent intent = Intent.makeRestartActivityTask(getIntent().getComponent());
        if(scrollTopreferenceKey != null) {
            // Préciser la clé de l'option jusqu'à laquelle il faudra scroller à l'affichage de la vue
            intent.putExtra(SCROLL_TO_KEY, scrollTopreferenceKey);
        }
        // Recréer la vue
        startActivity(intent);
    }

    /**
     * Désactive les notifications directement au niveau des boutons
     * @return boolean Vrai si l'état d'un bouton a été modifié, faux sinon
     */
    private boolean disableNotificationInSettingsFragment() {
        return settingsFragment.disableNotification();
    }

    public void checkNotificationPermissions() {
        NotificationPermissionHelper.checkPermission(this, true, this::disableNotificationInSettingsFragment);
    }
}