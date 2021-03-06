package com.dev.xavier.tempusromanum;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.preference.PreferenceManager;

import java.util.Locale;

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
class LocaleHelper {

    static Context updateLanguage(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean forcedLatin = pref.getBoolean(context.getString(R.string.saved_force_latin), Boolean.parseBoolean(context.getString(R.string.default_force_latin)));

        if (forcedLatin) {
            final String latinLanguage = context.getString(R.string.latin_locale_code);
            // Forcer le latin comme langue de l'application
            if (!getCurrentLocale(context).equals(latinLanguage))
                context = LocaleHelper.updateResources(context, latinLanguage);
        } else {
            // Rétablir la langue défini dans les options système
            final String systemLanguage = getSystemLocale().getLanguage();
            if (!getCurrentLocale(context).equals(systemLanguage))
                context = LocaleHelper.updateResources(context, systemLanguage);
        }
        return context;
    }

    static String getCurrentLocale(Context context) {
        //return getSystemLocale().getDefault().getLanguage();
        return context.getResources().getConfiguration().getLocales().get(0).getLanguage();
    }


    private static Locale getSystemLocale() {
        return Locale.getDefault();
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        context = context.createConfigurationContext(config);

        return context;
    }
}