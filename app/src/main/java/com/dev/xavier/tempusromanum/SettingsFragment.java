package com.dev.xavier.tempusromanum;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

/**
 * Copyright 2022 Xavier Freyburger
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
public class SettingsFragment extends PreferenceFragmentCompat {

    private String userInitiatedActionKey = null;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        switch (preference.getKey()) {
            case "alert_rome_founding":
            case "alert_nones":
            case "alert_ides": {
                // Gestion des notifications
                if(!NotificationPermissionHelper.areNotificationsEnabled(this.getContext())) {
                    userInitiatedActionKey = preference.getKey();
                }
                ((SettingsActivity) requireActivity()).checkNotificationPermissions();
                break;
            }
            case "force_latin": {
                // Gestion du changement de langue
                ((SettingsActivity) requireActivity()).reloadActivity(preference.getKey());
                break;
            }
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    /**
     * Désactive les notifications directement au niveau des boutons
     * @return boolean Vrai si l'état d'un bouton a été modifié, faux sinon
     */
    public boolean disableNotification() {
        if(userInitiatedActionKey != null) {

            boolean change = false;

            SwitchPreferenceCompat switchPref = findPreference(userInitiatedActionKey);

            if (switchPref != null && switchPref.isChecked()) {
                switchPref.setChecked(false);
                change = true;
            }
            userInitiatedActionKey = null;
            return change;
        }
        return true;
    }
}
