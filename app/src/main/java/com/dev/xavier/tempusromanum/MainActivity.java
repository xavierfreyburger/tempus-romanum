package com.dev.xavier.tempusromanum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Copyright 2021 Xavier Freyburger
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
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, TextWatcher {

    private TextView outputDate;
    private EditText dayEditText;
    private EditText monthEditText;
    private EditText yearEditText;
    private RadioGroup eraRadioGroup;
    private boolean customDate = false;
    private boolean lockTextWatcher = false;
    private boolean romanNumber = false;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Initialisation du channel de notifications
        final String name = getString(R.string.notification_channel);
        NotificationChannel channel = new NotificationChannel(name, name, NotificationManager.IMPORTANCE_DEFAULT); // L'importance ne pourra plus être modifiée par la suite
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // Gérer le cas ou les notifications sont désactivée pour les versions d'Android avant 13
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.areNotificationsEnabled()) {
                disableNotificationsPreferences();
            }
        }

        // Initialisation des contrôles
        outputDate = findViewById(R.id.outputDate);
        dayEditText = findViewById(R.id.dayEditText);
        monthEditText = findViewById(R.id.monthEditText);
        yearEditText = findViewById(R.id.yearEditText);
        eraRadioGroup = findViewById(R.id.eraRadioGroup);
        FloatingActionButton fab = findViewById(R.id.fab);
        FloatingActionButton fab2 = findViewById(R.id.fab2);

        // Sélection par défaut de l'êre moderne
        if (eraRadioGroup.getCheckedRadioButtonId() == -1) {
            eraRadioGroup.check(R.id.adRadioButton);
        }

        // Mise en place de l'écoute sur modification de la date par l'utilisateur
        dayEditText.addTextChangedListener(this);
        monthEditText.addTextChangedListener(this);
        yearEditText.addTextChangedListener(this);
        eraRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!lockTextWatcher) {
                customDate = true;
                updateDate();
            }
        });

        // Mise en place du dialogue de choix du jour
        dayEditText.setOnClickListener(v -> {
            DialogFragment newFragment = new ChooseDayDialogFragment();
            newFragment.show(getSupportFragmentManager(), "dayOfMonth");
        });

        // Mise en place du dialogue de choix du mois
        monthEditText.setOnClickListener(v -> {
            DialogFragment newFragment = new ChooseMonthDialogFragment();
            newFragment.show(getSupportFragmentManager(), "month");
        });

        // Bouton copier dans le presse-papier
        fab.setOnClickListener(view -> {
            // On ne copie que la phase de la date, pas les indications optionnelles concernant les nones et les ides (elles sont séparées par un saut de ligne)
            String txt = (String) outputDate.getText();
            if (txt == null) {
                txt = "";
            } else if (txt.contains("\n")) {
                txt = txt.substring(0, txt.indexOf("\n"));
            }

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("simple text", txt);
            clipboard.setPrimaryClip(clip);
            Snackbar.make(view, getString(R.string.text_copied_to_clipboard), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

        // Bouton reset date
        fab2.setOnClickListener(view -> {
            // Forcer la date du jour
            customDate = false;
            updateDate(true);
            Snackbar.make(view, getString(R.string.date_reset), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

        // Mise en place du listener des paramètres
        setupSharedPreferences();

        // Listener retour du dialogue système d'autorisation des notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher = registerForActivityResult(new RequestPermission(), isGranted -> {
                if (!isGranted) {
                    // Désactiver les options de notification dans les préférences
                    disableNotificationsPreferences();
                }
            });
        }
        
        // Tester que l'autorisation aux notifications est donnée sinon désactiver les notifications
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            grantNotifications();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            // Les notifications sont désactivées dans les paramètres système
            disableNotificationsPreferences();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Open settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        switch (key) {
            case "sentence_mode":
            case "week_day_display":
            case "year_display":
            case "year_reference":
            case "shorten_era":
            case "font_size":
            case "font_color":
            case "background_color":
            case "background_transparency":
                // Mettre à jour le widget
                updateWidget();
                break;
            case "alert_rome_founding":
            case "alert_nones":
            case "alert_ides":
                // Mettre à jour les notifications
                updateNotifications();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!lockTextWatcher) {
            customDate = true;
            updateDate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
        Editor ed = mPrefs.edit();
        ed.putBoolean("customDate", customDate);
        if (customDate) {
            ed.putString("day", dayEditText.getText().toString());
            ed.putString("month", monthEditText.getText().toString());
            ed.putString("year", yearEditText.getText().toString());
            ed.putInt("era", eraRadioGroup.getCheckedRadioButtonId());
        }
        ed.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (romanNumber) {
            // Paramétrer le clavier pour écrire en chiffres Romains
            yearEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);

            // Paramétrer le filtre des touches
            yearEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps(), NumberHelper.romanNumeraFilter});

        } else {

            // Remettre les paramètre de saisie du clavier du système
            yearEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }


        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
        customDate = mPrefs.getBoolean("customDate", false);

        // Si une date à été entrée par l'utilisateur ==> restoration
        if (customDate) {

            lockTextWatcher = true;

            prefNumberValueRetriever(mPrefs, "day", dayEditText);
            prefNumberValueRetriever(mPrefs, "month", monthEditText);
            prefNumberValueRetriever(mPrefs, "year", yearEditText);

            int era = mPrefs.getInt("era", 0);
            if (era != 0) {
                eraRadioGroup.check(era);
            }

            lockTextWatcher = false;
        }
        updateDate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        // Met à jour la locale
        super.attachBaseContext(LocaleHelper.updateLanguage(base));
        // Mettre à jour l'info de l'affichage en mode romain ou decimal
        romanNumber = LocaleHelper.getCurrentLocale(base).equals(getString(R.string.latin_locale_code));
    }

    private void prefNumberValueRetriever(SharedPreferences pref, String key, EditText editText) {
        String value = pref.getString(key, null);

        if (value != null && value.length() > 0) {
            if (romanNumber) {
                if (!NumberHelper.isRoman(value.charAt(0))) {
                    value = Calendarium.romanusNumerus(Integer.parseInt(value));
                }
            } else {
                if (!NumberHelper.isDecimal(value.charAt(0))) {
                    value = String.valueOf(NumberHelper.decimal(value));
                }
            }
            editText.setText(value);
        }
    }

    private void updateDate() {
        updateDate(false);
    }

    @SuppressLint("NonConstantResourceId")
    private void updateDate(boolean forceNewDate) {
        final Date date;

        Integer d = null;
        Integer m = null;
        Integer y = null;

        boolean updateDay = false;
        boolean updateMonth = false;
        boolean updateYear = false;

        yearEditText.setError(null);

        if (!forceNewDate) {
            d = dayEditText.getText() == null || dayEditText.getText().length() == 0 ? null : romanNumber ? NumberHelper.decimal(dayEditText.getText().toString()) : Integer.valueOf(dayEditText.getText().toString());
            m = monthEditText.getText() == null || monthEditText.getText().length() == 0 ? null : romanNumber ? NumberHelper.decimal(monthEditText.getText().toString()) : Integer.valueOf(monthEditText.getText().toString());
            try {
                y = yearEditText.getText() == null || yearEditText.getText().length() == 0 ? null : romanNumber ? NumberHelper.decimal(yearEditText.getText().toString()) : Integer.valueOf(yearEditText.getText().toString());
            } catch (NumberFormatException e) {
                displayYearError();
            }


            // Contrôle de la validité du jour saisi max 31
            if (d != null) {
                int newd = d;
                if (d <= 0) {
                    d = null;
                } else {
                    newd = d % 32;
                    if (newd == 0) {
                        newd = 1;
                    }
                }
                if (d != null) {
                    updateDay = newd != d;
                    d = newd;
                }
            }

            // Contôle du mois saisi max 12
            if (m != null) {
                int newm = m;
                if (m <= 0) {
                    m = null;
                } else {
                    newm = m % 13;
                    if (newm == 0) {
                        newm = 1;
                    }
                }
                if (m != null) {
                    updateMonth = newm != m;
                    m = newm;
                }
            }

            // Contrôle de l'année saisie max 3999
            if (y != null) {
                int newy = y;
                if (y <= 0) {
                    y = null;
                } else {
                    newy = y % 4000;
                    if (newy == 0) {
                        newy = 1;
                    }
                }
                if (y != null) {
                    updateYear = newy != y;
                    y = newy;
                }
            }
        }

        if (forceNewDate || (d == null && m == null && y == null)) {
            // Toutes les zones sont vides, on initialise avec la date du jour
            date = new Date();
        } else if (d == null || m == null || y == null) {
            // toutes les zones ne sont pas saisie, on laisse tel-quel
            return;
        } else {
            // Calcul de la date
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, d);
            c.set(Calendar.MONTH, m - 1);
            c.set(Calendar.YEAR, y);
            switch (eraRadioGroup.getCheckedRadioButtonId()) {
                case R.id.adRadioButton:
                    c.set(Calendar.ERA, GregorianCalendar.AD);
                    break;
                case R.id.bcRadioButton:
                    c.set(Calendar.ERA, GregorianCalendar.BC);
                    break;
            }
            date = c.getTime();
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Options concernant la date
        // 2.1 Sentence mode
        final boolean sentenceMode = pref.getBoolean(getString(R.string.saved_date_sentence_mode), Boolean.parseBoolean(getString(R.string.default_date_sentence_mode)));

        // 2.2 Display week day
        final boolean displayWeekDay = pref.getBoolean(getString(R.string.saved_date_week_day_display), Boolean.parseBoolean(getString(R.string.default_date_week_day_display)));

        // 2.3 Display year
        final boolean yearDisplay = pref.getBoolean(getString(R.string.saved_date_year_display), Boolean.parseBoolean(getString(R.string.default_date_year_display)));

        // 2.4 Year reference
        final Calendarium.InitiumCalendarii yearRef;
        if (yearDisplay) {
            final String yearRefStr = pref.getString(getString(R.string.saved_date_year_reference), getString(R.string.default_date_year_reference));
            yearRef = Calendarium.InitiumCalendarii.valueOf(yearRefStr);
        } else {
            yearRef = Calendarium.InitiumCalendarii.SINE;
        }

        // 2.5 Shorten era
        final boolean shortenEra = pref.getBoolean(getString(R.string.saved_date_shorten_era_display), Boolean.parseBoolean(getString(R.string.default_date_shorten_era_display)));

        // 2.6 Display current Nones
        final boolean displayNones = pref.getBoolean(getString(R.string.saved_current_nones), Boolean.parseBoolean(getString(R.string.default_current_nones)));

        // 2.7 Display current Ides
        final boolean displayIdes = pref.getBoolean(getString(R.string.saved_current_ides), Boolean.parseBoolean(getString(R.string.default_current_ides)));

        // Génération de la date en latin
        StringBuilder sb = new StringBuilder(Calendarium.tempus(date, sentenceMode, displayWeekDay, yearRef, shortenEra));

        // Ajout des compléments Nones/Ides si nécessaire
        if (displayNones || displayIdes) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            final int currentMonth = cal.get(Calendar.MONTH) + 1;
            sb.append("\n");
            if (displayNones) {
                // Afficher le jour des nones
                final int nones = Calendarium.nonaeMensium(currentMonth);
                try {
                    @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier("num_" + nones, "string", getPackageName());
                    String number = getString(resourceId);
                    sb.append("\n").append(getString(R.string.nones_label)).append(number);
                } catch (Exception ignored) {
                }
            }
            if (displayIdes) {
                // Afficher le jour des ides
                final int ides = Calendarium.idusMensium(currentMonth);
                try {
                    @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier("num_" + ides, "string", getPackageName());
                    String number = getString(resourceId);
                    sb.append("\n").append(getString(R.string.ides_label)).append(number);
                } catch (Exception ignored) {
                }
            }
        }

        // Mise à jour du champ texte
        outputDate.setText(sb);

        // Si la date n'est pas la même que celle renseignée par l'utilisateur, mise à jour des champs de saisies
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        lockTextWatcher = true;
        if (d == null || updateDay || d != calendar.get(Calendar.DAY_OF_MONTH)) {
            dayEditText.setText(romanNumber ? Calendarium.romanusNumerus(calendar.get(Calendar.DAY_OF_MONTH)) : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        }
        if (m == null || updateMonth || m != (calendar.get(Calendar.MONTH) + 1)) {
            monthEditText.setText(romanNumber ? Calendarium.romanusNumerus(calendar.get(Calendar.MONTH) + 1) : String.valueOf(calendar.get(Calendar.MONTH) + 1));
        }
        if (y == null || updateYear || y != calendar.get(Calendar.YEAR)) {
            yearEditText.setText(romanNumber ? Calendarium.romanusNumerus(calendar.get(Calendar.YEAR)) : String.valueOf(calendar.get(Calendar.YEAR)));
        }

        if (forceNewDate) {
            switch (calendar.get(Calendar.ERA)) {
                case GregorianCalendar.AD:
                    eraRadioGroup.check(R.id.adRadioButton);
                    break;
                case GregorianCalendar.BC:
                    eraRadioGroup.check(R.id.bcRadioButton);
                    break;

            }
        }
        lockTextWatcher = false;
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void updateWidget() {
        Intent intent = new Intent(this, TempusRomanumWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), TempusRomanumWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    public void displayYearError() {
        yearEditText.setError(getString(R.string.number_error));
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void grantNotifications() {
        // Si au moins une options activées, demander l'autorisation
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean romeFoundationAlert = pref.getBoolean("alert_rome_founding", false);
        final boolean nonesAlert = pref.getBoolean("alert_nones", false);
        final boolean idesAlert = pref.getBoolean("alert_ides", false);
        if ((romeFoundationAlert || nonesAlert || idesAlert) && requestPermissionLauncher != null ) {
            // Vérifier s'il faut demander l'autorisation l'utilisateur
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // L'autorisation est déjà accordée
                return;
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // L'autorisation a été révoquée, proposer une explication pour le convaincre l'utilisateur d'accepter l'autorisation à nouveau
                showMessageOKCancel(getString(R.string.request_permission_notifications),
                        (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS));
            } else {
                // Demander l'autorisation
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void updateNotifications() {
        // Appeller NotificationPublisher
        Intent intent = new Intent(this, NotificationPublisher.class);
        intent.putExtra(getString(R.string.notification_switch), false);
        sendBroadcast(intent);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), okListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }

    private void disableNotificationsPreferences() {
        boolean change = false;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = pref.edit();

        if(pref.getBoolean("alert_rome_founding", false)) {
            prefEditor.putBoolean("alert_rome_founding", false);
            change = true;
        }
        if(pref.getBoolean("alert_nones", false)) {
            prefEditor.putBoolean("alert_nones", false);
            change = true;
        }
        if(pref.getBoolean("alert_ides", false)) {
            prefEditor.putBoolean("alert_ides", false);
            change = true;
        }

        if(change) {
            // Sauvegarder les changements
            prefEditor.apply();
        }
    }

    /*
     * Getters
     */

    public EditText getDayEditText() {
        return dayEditText;
    }

    public EditText getMonthEditText() {
        return monthEditText;
    }

    public boolean isRomanNumber() {
        return romanNumber;
    }
}