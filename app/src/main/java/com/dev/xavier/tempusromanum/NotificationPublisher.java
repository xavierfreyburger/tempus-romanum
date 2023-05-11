package com.dev.xavier.tempusromanum;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Copyright 2021 Xavier Freyburger
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
public class NotificationPublisher extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;
    private static final int romeFoundationDay = 21;
    private static final int romeFoundationMonth = 4;
    private static final int notificationTargetHour = 8;

    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            // 1. Réception de l'évenement de boot -> si nécessaire mettre en place la répétition automatique
            if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                // Réception de l'évènement de boot
                setupRepeating(context);
                return;
            }

            // 2. Analyse des Extras, si notity == false -> mettre en place la répétition automatique
            // Récupération du paramétrage d'affichage de la notification
            final boolean notify = intent.getExtras().getBoolean(context.getString(R.string.notification_switch), true);
            if (!notify) {
                setupRepeating(context);
                return;
            }
        }

        // 3. Transfert au gestionnaire de notification
        sendNotification(context);
    }

    /**
     * Envoi d'une notification au système
     *
     * @param context context
     */
    private void sendNotification(Context context) {
        // Si les notifications sont désactivées dans les paramètres système, ne rien faire
        if( !NotificationPermissionHelper.areNotificationsEnabled(context)) {
            return;
        }
        // Récupération du parmétrage des notifications
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean romeFoundationAlert = pref.getBoolean("alert_rome_founding", false);
        final boolean nonesAlert = pref.getBoolean("alert_nones", false);
        final boolean idesAlert = pref.getBoolean("alert_ides", false);

        // Générer le texte de la notification
        String title;
        Calendar cal = Calendar.getInstance();
        final int dayNumber = cal.get(Calendar.DAY_OF_MONTH);
        final int monthNumber = cal.get(Calendar.MONTH) + 1;

        if (nonesAlert && dayNumber == Calendarium.nonaeMensium(monthNumber)) {

            // Message concernant les nones
            title = LocaleHelper.getLocaleStringResource(R.string.notification_nones_title, context) + getMonthLabel(context, monthNumber);

        } else if (idesAlert && dayNumber == Calendarium.idusMensium(monthNumber)) {

            // Message concernant les ides
            title = LocaleHelper.getLocaleStringResource(R.string.notification_ides_title, context) + getMonthLabel(context, monthNumber);

        } else if (romeFoundationAlert && monthNumber == romeFoundationMonth && dayNumber == romeFoundationDay) {

            // Message concernant l'anniversaire de la fondation de Rome
            title = LocaleHelper.getLocaleStringResource(R.string.notification_rome_founding_title, context);

        } else {
            // Ce jour ne nécessite pas de notification
            // -- Pour tests -> commenter "return"
            //title = "It's testing day !!";
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // PendingIntent permettant d'ouvrir l'application lors d'un clic sur la notification
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel))
                .setSmallIcon(R.drawable.ic_stat_tempusromanum)
                .setContentTitle(title)
                .setContentText(null)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Mise en œuvre de la répétition automatique de l'alarme
     *
     * @param context context
     */
    private void setupRepeating(Context context) {
        // Récupération du parmétrage des notifications
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean romeFoundationAlert = pref.getBoolean("alert_rome_founding", false);
        final boolean nonesAlert = pref.getBoolean("alert_nones", false);
        final boolean idesAlert = pref.getBoolean("alert_ides", false);

        // Gérer le mécanisme de répétition automatique
        if (!(romeFoundationAlert || nonesAlert || idesAlert)) {
            // désactiver la répétition
            cancelRepeating();
        } else if (alarmMgr == null || alarmIntent == null) {
            // Mettre en place la répétition automatique
            activateRepeating(context);
        }
    }

    /**
     * Activation de la répétition automatique de l'alarme
     *
     * @param context context
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    private void activateRepeating(Context context) {
        Intent intent = new Intent(context, NotificationPublisher.class);
        // The mutable PendingIntent object allows the system to add the EXTRA_ALARM_COUNT intent extra
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            alarmIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);

        // Mise en place d'une alarme automatique pour le lendemain à 8H
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, notificationTargetHour);

        // Mise en place de la répétition de l'alarme
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        // Pour tests, alarme dans 10s
        //alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10 * 1000, alarmIntent);
    }

    /**
     * Désactivation de la répétition automatique de l'alarme
     */
    private void cancelRepeating() {
        if (alarmMgr != null && alarmIntent != null) {
            alarmMgr.cancel(alarmIntent);
        }
        alarmIntent = null;
        alarmMgr = null;
    }

    /**
     * Récupération du libélé du mois dans les fichiers strings avec prise ne compte de la locale de l'application
     *
     * @param context     context
     * @param monthNumber numéro du mois : 1-12
     * @return Le libellé du mois
     */
    private String getMonthLabel(Context context, int monthNumber) {
        try {
            @SuppressLint("DiscouragedApi") int resourceId = context.getResources().getIdentifier("month_" + monthNumber, "string", context.getPackageName());
            return LocaleHelper.getLocaleStringResource(resourceId, context);
        } catch (Exception e) {
            return "";
        }
    }
}
