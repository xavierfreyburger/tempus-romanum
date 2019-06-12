package com.dev.xavier.tempusromanum;

import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, TextWatcher {

    private TextView outputDate;
    private EditText dayEditText;
    private EditText monthEditText;
    private EditText yearEditText;
    private RadioGroup eraRadioGroup;
    private boolean customDate = false;
    private boolean lockTextWatcher = false;
    private boolean romanNumber = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        outputDate = findViewById(R.id.outputDate);
        dayEditText = findViewById(R.id.dayEditText);
        monthEditText = findViewById(R.id.monthEditText);
        yearEditText = findViewById(R.id.yearEditText);
        eraRadioGroup = findViewById(R.id.eraRadioGroup);
        FloatingActionButton fab = findViewById(R.id.fab);
        FloatingActionButton fab2 = findViewById(R.id.fab2);

        // Sélection par défaut de êre moderne
        if(eraRadioGroup.getCheckedRadioButtonId() == -1)
        {
            eraRadioGroup.check(R.id.adRadioButton);
        }

        // Mise en place de l'écoute sur modification de la date par l'utilisateur
        dayEditText.addTextChangedListener(this);
        monthEditText.addTextChangedListener(this);
        yearEditText.addTextChangedListener(this);
        eraRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(!lockTextWatcher) {
                    customDate = true;
                    updateDate();
                }
            }
        });

        // Mise en place du dialogue de choix du jour
        dayEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new ChooseDayDialogFragment();
                newFragment.show(getSupportFragmentManager(), "dayOfMonth");
            }
        });

        // Mise en place du dialogue de choix du mois
        monthEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new ChooseMonthDialogFragment();
                newFragment.show(getSupportFragmentManager(), "month");
            }
        });

        // Bouton copier dans le presse-papier
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text",outputDate.getText());
                clipboard.setPrimaryClip(clip);
                Snackbar.make(view, getString(R.string.text_copied_to_clipboard), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Bouton reset date
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Forcer la date du jour
                customDate = false;
                updateDate(true);
                Snackbar.make(view, getString(R.string.date_reset), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Mise en place du listener des paramètres
        setupSharedPreferences();
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

        switch (key)
        {
            case "sentence_mode":
            case "week_day_display":
            case "year_display":
            case "year_reference":
            case "shorten_era":
            case "font_size":
            case "font_color":
                updateWidget();
                break;
            case "force_latin":
                // Recharger la vue des paramètres pour qu'elle soit affichée en latin
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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
        if(!lockTextWatcher) {
            customDate = true;
            updateDate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putBoolean("customDate", customDate);
        if(customDate) {
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

        // Activer/Désactiver l'affichage du clavier lors de l'edition des années
        if(romanNumber) {

            // Mettre l'affichage en mode serif
            dayEditText.setTypeface(Typeface.SERIF);
            monthEditText.setTypeface(Typeface.SERIF);
            yearEditText.setTypeface(Typeface.SERIF);

            // Paramétrer le clavier Romain
            yearEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |  InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);

            // Paramétrer le filtre des touches
            yearEditText.setFilters(new InputFilter[] {new InputFilter.AllCaps(), NumberHelper.romanNumeraFilter});

        } else {

            // Mettre l'affichage en sans serif
            dayEditText.setTypeface(Typeface.SANS_SERIF);
            monthEditText.setTypeface(Typeface.SANS_SERIF);
            yearEditText.setTypeface(Typeface.SANS_SERIF);

            // Remettre les paramètre de saisie du clavier du système
            yearEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }


        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
        customDate = mPrefs.getBoolean("customDate", false);

        // Si une date à été entrée par l'utilisateur ==> restoration
        if(customDate) {

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
        romanNumber = LocaleHelper.getCurrentLocale().equals(getString(R.string.latin_locale_code));
    }

    private void prefNumberValueRetriever(SharedPreferences pref, String key,  EditText editText) {
        String value = pref.getString(key, null);

        if (value != null && value.length() > 0) {
            if(romanNumber)
            {
                if(!NumberHelper.isRoman(value.charAt(0))) {
                    value = Calendarium.romanusNumerus(Integer.valueOf(value));
                }
            } else {
                if(!NumberHelper.isDecimal(value.charAt(0))) {
                    value = String.valueOf(NumberHelper.decimal(value));
                }
            }
            editText.setText(value);
        }
    }

    private void updateDate()
    {
        updateDate(false);
    }

    private void updateDate(boolean forceNewDate)
    {
        final Date date;

        Integer d = null;
        Integer m = null;
        Integer y = null;

        boolean updateDay = false;
        boolean updateMonth = false;
        boolean updateYear = false;

        if(!forceNewDate) {
            d = dayEditText.getText() == null || dayEditText.getText().length() == 0 ? null : romanNumber ? NumberHelper.decimal(dayEditText.getText().toString()) : Integer.valueOf(dayEditText.getText().toString());
            m = monthEditText.getText() == null || monthEditText.getText().length() == 0 ? null : romanNumber ? NumberHelper.decimal(monthEditText.getText().toString()) : Integer.valueOf(monthEditText.getText().toString());
            try {
                y = yearEditText.getText() == null || yearEditText.getText().length() == 0 ? null : romanNumber ? NumberHelper.decimal(yearEditText.getText().toString()) : Integer.valueOf(yearEditText.getText().toString());
            } catch (NumberFormatException e)
            {
                displayYearError();
                y = null;
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
                if(d != null)
                {
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
                if(m != null) {
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
                if(y != null) {
                    updateYear = newy != y;
                    y = newy;
                }
            }
        }

        if(forceNewDate || (d == null && m == null && y == null))
        {
            // Toutes les zones sont vides, on initialise avec la date du jour
            date = new Date();
        }
        else if(d == null || m == null || y == null)
        {
            // toutes les zones ne sont pas saisie, on laisse tel-quel
            return;
        }
        else
        {
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
        SharedPreferences pref = android.preference.PreferenceManager.getDefaultSharedPreferences(this);

        // Options concernant la date
        // 2.1 Sentence mode
        final boolean sentenceMode = pref.getBoolean(getString(R.string.saved_date_sentence_mode), Boolean.valueOf(getString(R.string.default_date_sentence_mode)));

        // 2.2 Display week day
        final boolean displayWeekDay = pref.getBoolean(getString(R.string.saved_date_week_day_display), Boolean.valueOf(getString(R.string.default_date_week_day_display)));

        // 2.3 Display year
        final boolean yearDisplay = pref.getBoolean(getString(R.string.saved_date_year_display), Boolean.valueOf(getString(R.string.default_date_year_display)));

        // 2.4 Year reference
        final Calendarium.InitiumCalendarii yearRef;
        if(yearDisplay) {
            final String yearRefStr = pref.getString(getString(R.string.saved_date_year_reference), getString(R.string.default_date_year_reference));
            yearRef = Calendarium.InitiumCalendarii.valueOf(yearRefStr);
        }
        else {
            yearRef = Calendarium.InitiumCalendarii.SINE;
        }

        // 2.5 Shorten era
        final boolean shortenEra = pref.getBoolean(getString(R.string.saved_date_shorten_era_display), Boolean.valueOf(getString(R.string.default_date_shorten_era_display)));

        // Mise à jour du champ texte
        outputDate.setText(Calendarium.tempus(date, sentenceMode, displayWeekDay, yearRef, shortenEra));

        // Si la date n'est pas la même que celle renseignée par l'utilisateur, mise à jour des champs de saisies
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        lockTextWatcher = true;
        if(d == null || updateDay || d != calendar.get(Calendar.DAY_OF_MONTH)) {
            dayEditText.setText(romanNumber ? Calendarium.romanusNumerus(calendar.get(Calendar.DAY_OF_MONTH)) : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        }
        if(m == null || updateMonth || m != (calendar.get(Calendar.MONTH)+1)) {
            monthEditText.setText(romanNumber ? Calendarium.romanusNumerus(calendar.get(Calendar.MONTH) + 1) : String.valueOf(calendar.get(Calendar.MONTH) + 1));
        }
        if(y == null || updateYear || y != calendar.get(Calendar.YEAR)) {
            yearEditText.setText(romanNumber ? Calendarium.romanusNumerus(calendar.get(Calendar.YEAR)) : String.valueOf(calendar.get(Calendar.YEAR)));
        }

        if(forceNewDate) {
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