package com.dev.xavier.tempusromanum;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * Copyright 2019 Xavier Freyburger
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
public class LocaleHelper {

    public static void updateLanguage(Context context) {
        SharedPreferences pref = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        final boolean forcedLatin = pref.getBoolean(context.getString(R.string.saved_force_latin), Boolean.valueOf(context.getString(R.string.default_force_latin)));
        final String currentLanguage = Locale.getDefault().getLanguage();

        if(forcedLatin) {
            final String latinLanguage = context.getString(R.string.latin_locale_code);
            if(!currentLanguage.equals(latinLanguage)) {
                // Forcer le latin comme langue de l'application
                LocaleHelper.updateResources(context,latinLanguage);
            }
        } else {
            final String systemLanguage = LocaleHelper.getSystemLocale().getLanguage();
            if(!currentLanguage.equals(systemLanguage)) {
                // Remettre la langue sélectionnée dans les paramètres système
                LocaleHelper.updateResources(context, systemLanguage);
            }
        }
    }

    private static Locale getSystemLocale() {
        return Resources.getSystem().getConfiguration().locale;
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}