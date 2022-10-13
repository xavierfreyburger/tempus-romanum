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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class NotificationPermissionHelper {

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
     * @param requestPermissionLauncher demande de permission
     * @param notify s'il faut notifier l'utilisateur dans une Snackbar
     * @param buildInPreferencesDisabler Si renseigné, méthode à appeler pour désactiver les notifications
     */
    public static void checkPermission(Context context, ActivityResultLauncher<String> requestPermissionLauncher, boolean notify, Callable<Boolean> buildInPreferencesDisabler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Vérifier et demander si nécessaire la permission à l'utilisateur d'envoyer des notifications
            grantNotifications(context, requestPermissionLauncher);
        } else if (!context.getSystemService(NotificationManager.class).areNotificationsEnabled()) {
            // Les notifications sont désactivées dans les paramètres système
            disableNotificationsPreferences(context, notify, buildInPreferencesDisabler);
        }

        // Vérification que le chanel Tempus Romanum est actif
        NotificationChannel notificationChannel = context.getSystemService(NotificationManager.class).getNotificationChannel(context.getString(R.string.notification_channel));
        if( notificationChannel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
            // Suggest user to enable notification chanel in system settings
            showMessageOKCancel(context, context.getString(R.string.request_permission_notifications_channel) ,
                    (dialog, which) -> openSystemChannelSetting(context));
        }
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
     * @param requestPermissionLauncher demande de permission
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static void grantNotifications(Context context, ActivityResultLauncher<String> requestPermissionLauncher) {
        // Si au moins une options activées, demander l'autorisation
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean romeFoundationAlert = pref.getBoolean("alert_rome_founding", false);
        final boolean nonesAlert = pref.getBoolean("alert_nones", false);
        final boolean idesAlert = pref.getBoolean("alert_ides", false);
        if ((romeFoundationAlert || nonesAlert || idesAlert) && requestPermissionLauncher != null ) {
            // Vérifier s'il faut demander l'autorisation l'utilisateur
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // L'autorisation est déjà accordée
                return;
            }
            if ( ((Activity)context).shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // L'autorisation a été révoquée, proposer une explication pour le convaincre l'utilisateur d'accepter l'autorisation à nouveau
                showMessageOKCancel(context, context.getString(R.string.request_permission_notifications),
                        (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS));
            } else {
                // Demander l'autorisation
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
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
     * @param buildInPreferencesDisabler Si renseigné, méthode à appeler pour désactiver les notifications
     */
    private static void disableNotificationsPreferences(Context context, boolean notify, Callable<Boolean> buildInPreferencesDisabler) {
        boolean change = false;

        if(buildInPreferencesDisabler != null) {
            // Désactiver les préférences de notifications avec la méthode fournie en paramètres
            try {
                change = buildInPreferencesDisabler.call();
            } catch (Exception ignored) {}
        } else {
            // Désactiver les préférence de notifications avec la méthode standard
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor prefEditor = pref.edit();

            if (pref.getBoolean("alert_rome_founding", false)) {
                prefEditor.putBoolean("alert_rome_founding", false);
                change = true;
            }
            if (pref.getBoolean("alert_nones", false)) {
                prefEditor.putBoolean("alert_nones", false);
                change = true;
            }
            if (pref.getBoolean("alert_ides", false)) {
                prefEditor.putBoolean("alert_ides", false);
                change = true;
            }
            if(change) {
                // Sauvegarder les changements
                prefEditor.apply();
            }
        }

        if(change && notify) {
            // Avertir que le système empèche l'activation des notifications dans une Snackbar au bas de l'écran
            Snackbar.make(((Activity)context).findViewById(android.R.id.content), context.getString(R.string.notification_premission_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Attache une action sur le retour d'une demande de permission aux notifications envoyée à l'utilisateur
     * Si négatif désactive les notifications de l'application
     * @param context context
     * @param notify s'il faut notifier l'utilisateur dans une Snackbar
     * @param buildInPreferencesDisabler Si renseigné, méthode à appeler pour désactiver les notifications
     * @return ActivityResultLauncher<String> demande de permission
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static ActivityResultLauncher<String> registerPermissionLauncher(Context context, boolean notify, Callable<Boolean> buildInPreferencesDisabler) {
        return ((AppCompatActivity)context).registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                // Désactiver les options de notification dans les préférences
                disableNotificationsPreferences(context, notify, buildInPreferencesDisabler);
            }
        });
    }

    /**
     * Détache l'action sur le retour d'une demande de permission aux notifications envoyée à l'utilisateur
     * @param requestPermissionLauncher demande de permission
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static void unregisterPermissionLauncher(ActivityResultLauncher<String> requestPermissionLauncher)
    {
        if ( requestPermissionLauncher != null ) {
            try {
                requestPermissionLauncher.unregister();
            } catch (Exception ignored) {}
        }
    }
}
