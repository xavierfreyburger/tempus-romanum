package com.dev.xavier.tempusromanum;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    /**
     * Désactive les notifications directement au niveau des boutons
     * @return boolean Vrai si l'état d'un bouton a été modifié, faux sinon
     */
    public boolean disableNotification() {
        boolean change = false;

        SwitchPreferenceCompat switchFoundingRome = findPreference("alert_rome_founding");
        SwitchPreferenceCompat switchNones = findPreference("alert_nones");
        SwitchPreferenceCompat switchIdes = findPreference("alert_ides");

        if(switchFoundingRome != null && switchFoundingRome.isChecked()) {
            switchFoundingRome.setChecked(false);
            change = true;
        }
        if(switchNones != null && switchNones.isChecked()) {
            switchNones.setChecked(false);
            change = true;
        }
        if(switchIdes != null && switchIdes.isChecked()) {
            switchIdes.setChecked(false);
            change = true;
        }
        return change;
    }
}
