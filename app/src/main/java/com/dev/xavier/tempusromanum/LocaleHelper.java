package com.dev.xavier.tempusromanum;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

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
class LocaleHelper {

    static Context updateLanguage(Context context) {
        SharedPreferences pref = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        final boolean forcedLatin = pref.getBoolean(context.getString(R.string.saved_force_latin), Boolean.valueOf(context.getString(R.string.default_force_latin)));
        final String currentLanguage = Locale.getDefault().getLanguage();

        if(forcedLatin) {
            final String latinLanguage = context.getString(R.string.latin_locale_code);
            if(!currentLanguage.equals(latinLanguage)) {
                // Forcer le latin comme langue de l'application
                context = LocaleHelper.updateResources(context,latinLanguage);
            }
        } else {
            final String systemLanguage = LocaleHelper.getSystemLocale().getLanguage();
            if(!currentLanguage.equals(systemLanguage)) {
                // Remettre la langue sélectionnée dans les paramètres système
                context = LocaleHelper.updateResources(context, systemLanguage);
            }
        }
        return context;
    }

    static String getCurrentLocale() {
        return getSystemLocale().getDefault().getLanguage();
    }

    private static Locale getSystemLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getSystemLocaleNew();
        } else {
            return getSystemLocaleOld();
        }
    }

    private static Locale getSystemLocaleNew() {
        return Locale.getDefault();
    }

    @SuppressWarnings("deprecation")
    private static Locale getSystemLocaleOld() {
        return Resources.getSystem().getConfiguration().locale;
    }

    private static Context updateResources(Context context, String language) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context = updateResourcesNew(context, language);
        } else {
            updateResourcesOld(context, language);
        }
        return context;
    }

    private static Context updateResourcesNew(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        context = context.createConfigurationContext(config);
        return context;
    }

    @SuppressWarnings("deprecation")
    private static void updateResourcesOld(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}