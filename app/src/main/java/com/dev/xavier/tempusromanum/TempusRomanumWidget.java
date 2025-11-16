package com.dev.xavier.tempusromanum;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;


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
public class TempusRomanumWidget extends AppWidgetProvider {

    private static Date currentDate;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Chargement des préférences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        // 1.1 font size
        final int fontSize = Integer.parseInt(pref.getString(context.getString(R.string.saved_font_size), context.getString(R.string.default_font_size)));

        // 1.2 Bold font
        final boolean fontBold = pref.getBoolean(context.getString(R.string.saved_font_bold),Boolean.parseBoolean(context.getString(R.string.default_font_bold)));

        // 1.3 font color
        final String colorName = pref.getString(context.getString(R.string.saved_font_color), context.getString(R.string.default_font_color));
        @SuppressLint("DiscouragedApi") final int colorResId = context.getResources().getIdentifier(colorName, "color", context.getPackageName());
        final int fontColor = ContextCompat.getColor(context, colorResId);

        // 1.4 Background color
        final String backgroundColorName = pref.getString(context.getString(R.string.saved_background_color), context.getString(R.string.default_background_color));
        @SuppressLint("DiscouragedApi") final int backgroundColorResId = context.getResources().getIdentifier(backgroundColorName, "color", context.getPackageName());
        final int backgroundColor = ContextCompat.getColor(context, backgroundColorResId);

        // 1.5 Background transparency ratio (value is actually stored as opacity [0-100] we need to transforme it to transparency)
        final int backgroundTransparency = 100 - pref.getInt(context.getString(R.string.saved_background_transparency), Integer.parseInt(context.getString(R.string.default_background_transparency)));
        final float backgroundTransparencyRatio = backgroundTransparency / (float) 100;

        // 2.1 Sentence mode
        final boolean sentenceMode = pref.getBoolean(context.getString(R.string.saved_date_sentence_mode), Boolean.parseBoolean(context.getString(R.string.default_date_sentence_mode)));

        // 2.2 Display week day
        final boolean displayWeekDay = pref.getBoolean(context.getString(R.string.saved_date_week_day_display), Boolean.parseBoolean(context.getString(R.string.default_date_week_day_display)));

        // 2.3 Display year
        final boolean yearDisplay = pref.getBoolean(context.getString(R.string.saved_date_year_display), Boolean.parseBoolean(context.getString(R.string.default_date_year_display)));

        // 2.4 Year reference
        final Calendarium.InitiumCalendarii yearRef;
        if (yearDisplay) {
            final String yearRefStr = pref.getString(context.getString(R.string.saved_date_year_reference), context.getString(R.string.default_date_year_reference));
            yearRef = Calendarium.InitiumCalendarii.valueOf(yearRefStr);
        } else {
            yearRef = Calendarium.InitiumCalendarii.SINE;
        }

        // 2.5 Shorten era
        final boolean shortenEra = pref.getBoolean(context.getString(R.string.saved_date_shorten_era_display), Boolean.parseBoolean(context.getString(R.string.default_date_shorten_era_display)));

        // Faut-il calculer la mise à jour ?
        if (currentDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            Calendar newCal = Calendar.getInstance();
            if (cal.get(Calendar.DAY_OF_MONTH) != newCal.get(Calendar.DAY_OF_MONTH) || cal.get(Calendar.MONTH) != newCal.get(Calendar.MONTH) || cal.get(Calendar.YEAR) != newCal.get(Calendar.YEAR)) {
                currentDate = newCal.getTime();
            }
        } else {
            currentDate = new Date();
        }

        // Calcul de la date en latin
        CharSequence widgetText = Calendarium.tempus(currentDate, sentenceMode, displayWeekDay, yearRef, shortenEra);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tempus_romanum_widget);



        SpannableString span = new SpannableString(widgetText);
        // Set font normal or bold
        span.setSpan(new StyleSpan(fontBold ? Typeface.BOLD : Typeface.NORMAL), 0, widgetText.length(), 0);

        // Set font size
        span.setSpan(new AbsoluteSizeSpan(fontSize, true), 0, widgetText.length(), 0);

        views.setTextViewText(R.id.appwidget_text, span);

        // Set font color
        views.setTextColor(R.id.appwidget_text, fontColor);
        // Set background color with transparency
        views.setInt(R.id.appwidget_text, "setBackgroundColor", getColorWithAlpha(backgroundColor, backgroundTransparencyRatio));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Permettre l'ouverture de l'application sur clic du widget
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.setComponent(new ComponentName(context.getPackageName(), MainActivity.class.getName()));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context.getApplicationContext(), "There was a problem loading the application: ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of the
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static int getColorWithAlpha(int color, float ratio) {
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(alpha, r, g, b);
    }
}

