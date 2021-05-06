package com.dev.xavier.tempusromanum;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;

public class NotificationPublisher extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;
    private static final int romeFoundationDay = 21;
    private static final int romeFoundationMonth = 4;

    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Récupération du parmétrage des notifications
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean romeFoundationAlert = pref.getBoolean("alert_rome_founding", false);
        final boolean nonesAlert = pref.getBoolean("alert_nones", false);
        final boolean idesAlert = pref.getBoolean("alert_ides", false);

        // Gérer le mécanisme de répétition automatique
        if (!(romeFoundationAlert || nonesAlert || idesAlert)) {
            // désactiver la répétition
            cancelRepeating();
            return;
        } else if (alarmMgr == null || alarmIntent == null) {
            // Mettre en place la répétition automatique
            setupRepeating(context);
            return;
        }

        // Générer le texte de la notification
        String title = null;
        Calendar cal = Calendar.getInstance();
        final int dayNumber = cal.get(Calendar.DAY_OF_MONTH);
        final int monthNumber = cal.get(Calendar.MONTH) + 1;

        if (nonesAlert && dayNumber == Calendarium.nonaeMensium(monthNumber)) {

            // Message concernant les nones
                title = context.getString(R.string.notification_nones_title) + getMonthLabel(context, monthNumber);

        } else if (idesAlert && dayNumber == Calendarium.idusMensium(monthNumber)) {

            // Message concernant les ides
            title = context.getString(R.string.notification_ides_title) + getMonthLabel(context, monthNumber);

        } else if (dayNumber == romeFoundationDay && monthNumber == romeFoundationMonth) {

            // Message concernant l'anniversaire de la fondation de Rome
            title = context.getString(R.string.notification_rome_founding_title);

        } else {
            title = "TEST";
            // TODO return;
        }

        // Envoier la notification
        sendNotification(context, title, null);
    }

    private void sendNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // PendingIntent permettant d'ouvrir l'application lors d'un clic sur la notification
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(context, MainActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel))
                .setSmallIcon(R.drawable.ic_stat_tempusromanum)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void setupRepeating(Context context) {

        Intent intent = new Intent(context, NotificationPublisher.class);
        alarmIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);


        // Mise en place d'une alarme automatique à 9H
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        // Répétition toutes les 15 mins pour faire des tests
        // alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
    }

    private void cancelRepeating() {
        if (alarmMgr != null && alarmIntent != null) {
            alarmMgr.cancel(alarmIntent);
            alarmIntent = null;
            alarmMgr = null;
        }
    }

    private String getMonthLabel(Context context, int monthNumber) {
        try {
            int resourceId = context.getResources().getIdentifier("month_" + monthNumber, "string", context.getPackageName());
            return context.getString(resourceId);
        } catch (Exception e) {
            return "";
        }
    }
}
