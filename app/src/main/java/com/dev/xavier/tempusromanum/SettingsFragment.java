package com.dev.xavier.tempusromanum;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

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
                if(!NotificationPermissionHelper.areNotificationsEnabled(this.getContext())) {
                    userInitiatedActionKey = preference.getKey();
                }
                ((SettingsActivity) requireActivity()).checkNotificationPermissions();
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
