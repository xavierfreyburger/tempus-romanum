package com.dev.xavier.tempusromanum;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Callable;

/**
 * Copyright 2022 Xavier Freyburger
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
class NotificationPermissionHelper {

    private static final int REQUEST_CODE = 12345;

    public static int getRequestCode() {
        return REQUEST_CODE;
    }

    /**
     * Initialisation du canal de communication des notifications
     * @param context context
     */
    public static void initNotificationChannel(Context context) {
        final String name = context.getString(R.string.notification_channel);
        NotificationChannel channel = new NotificationChannel(name, name, NotificationManager.IMPORTANCE_DEFAULT); // L'importance ne pourra plus être modifiée par la suite
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * @param context context
     * @return Vrai si les notifications sont activées dans les paramètres système
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean areNotificationsEnabled(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            return context.getSystemService(NotificationManager.class).areNotificationsEnabled();
        }
    }

    /**
     * Vérifie l'autorisation au notification, si nécessaire essaye de les obtenir ou désactive l'option de notification
     * @param context context
     * @param notify s'il faut notifier l'utilisateur dans une Snackbar
     * @param buildInPreferencesDisabler Si renseignée, méthode à appeler pour désactiver les notifications
     */
    public static void checkPermission(Context context, boolean notify, Callable<Boolean> buildInPreferencesDisabler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Vérifier et demander si nécessaire la permission à l'utilisateur d'envoyer des notifications
            grantNotifications(context);
        } else if (!context.getSystemService(NotificationManager.class).areNotificationsEnabled()) {
            // Les notifications sont désactivées dans les paramètres système
            disableNotificationsPreferences(context, notify, buildInPreferencesDisabler);
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref.getBoolean("alert_rome_founding", false) || pref.getBoolean("alert_nones", false) || pref.getBoolean("alert_ides", false)) {
            // Vérification que le chanel Tempus Romanum est actif
            NotificationChannel notificationChannel = context.getSystemService(NotificationManager.class).getNotificationChannel(context.getString(R.string.notification_channel));
            if (notificationChannel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                // Suggest user to enable notification chanel in system settings
                showMessageOKCancel(context, context.getString(R.string.request_permission_notifications_channel),
                        (dialog, which) -> openSystemChannelSetting(context));
            }
        }
    }

    /**
     * Ouvre le menu de paramétrage système des notifications de l'application
     * @param context context
     */
    private static void openSystemNotificationSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        context.startActivity(intent);
    }

    /**
     * Ouvre le menu de paramétrage système du canal de notification de l'application
     * @param context context
     */
    private static void openSystemChannelSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getString(R.string.notification_channel));
        context.startActivity(intent);
    }

    /**
     * Si nécessaire demande la permission des notifications à l'utilisateur
     * @param context context
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static void grantNotifications(Context context) {
        // Si au moins une options activées, demander l'autorisation
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean romeFoundationAlert = pref.getBoolean("alert_rome_founding", false);
        final boolean nonesAlert = pref.getBoolean("alert_nones", false);
        final boolean idesAlert = pref.getBoolean("alert_ides", false);
        if ((romeFoundationAlert || nonesAlert || idesAlert) && context instanceof AppCompatActivity ) {
            // Vérifier s'il faut demander l'autorisation l'utilisateur
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // L'autorisation est déjà accordée
                return;
            }
            if ( ((Activity)context).shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // L'autorisation a été révoquée, proposer une explication pour le convaincre l'utilisateur d'accepter l'autorisation à nouveau
                showMessageOKCancel(context, context.getString(R.string.request_permission_notifications),
                        (dialog, which) -> requestPermission((AppCompatActivity)context));
            } else {
                // Demander l'autorisation
                requestPermission((AppCompatActivity)context);
            }
        }
    }

    /**
     * Lance une demande de permission dont la réponse sera évaluée par l'activité en paramètre
     * @param activity Activité qui recevera la réponse
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static void requestPermission(AppCompatActivity activity) {
        activity.requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
    }

    /**
     * Affiche une popup de dialogue avec l'utilisateur, avec possibilité d'attacher une action au bouton OK
     * @param context context
     * @param message message
     * @param okListener méthode appelée si clic sur bouton OK
     */
    private static void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), okListener)
                .setNegativeButton(context.getString(R.string.cancel), null)
                .create()
                .show();
    }

    /**
     * Désactive les notifications dans les préférences de l'application
     * @param context context
     * @param notify s'il faut notifier l'utilisateur dans une Snackbar
     * @param buildInPreferencesDisabler Si renseignée, méthode à appeler pour désactiver les notifications
     */
    private static void disableNotificationsPreferences(Context context, boolean notify, Callable<Boolean> buildInPreferencesDisabler) {
        boolean change = false;

        // Détecter s'il faut désactiver quelque chose ou non
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref.getBoolean("alert_rome_founding", false) || pref.getBoolean("alert_nones", false) || pref.getBoolean("alert_ides", false)) {
            change = true;
            if (buildInPreferencesDisabler != null) {
                // Effectuer les changements
                try {
                    change = buildInPreferencesDisabler.call();
                } catch (Exception ignored) {
                }
            }
        }

        if(change && notify) {
            // Avertir que le système empèche l'activation des notifications dans une Snackbar au bas de l'écran et proposer l'accès rapide aux paramètres
            Snackbar.make(((Activity)context).findViewById(android.R.id.content), context.getString(R.string.notification_premission_error), Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.open), v -> openSystemNotificationSetting(context))
                    .show();
        }
    }

    /**
     * Traite le retour d'une demande de permission de notification
     * @param grantResults int[] tableau de reponses au demandes de notifications
     * @param context context
     * @param notify s'il faut notifier l'utilisateur dans une Snackbar
     * @param buildInPreferencesDisabler Si renseignée, méthode à appeler pour désactiver les notifications
     */
    public static void handlePermissionResult(int[] grantResults, Context context, boolean notify, Callable<Boolean> buildInPreferencesDisabler) {
        if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            // Désactiver les options de notification dans les préférences
            disableNotificationsPreferences(context, notify, buildInPreferencesDisabler);
        }
    }
}
