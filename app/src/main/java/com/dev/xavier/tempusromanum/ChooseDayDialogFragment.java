package com.dev.xavier.tempusromanum;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
public class ChooseDayDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final String[] values = getResources().getStringArray(R.array.days_of_month);
        final String daytxt = ((MainActivity) getActivity()).getDayEditText().getText().toString();

        // Récupération de la valeur du jour actuellement sélectionné
        int dayOfMonth = 1;
        if (((MainActivity) getActivity()).isRomanNumber()) {
            // En romain
            for (int i = 1; i <= values.length; i++) {
                if (values[i - 1].equals(daytxt)) {
                    dayOfMonth = i;
                    break;
                }
            }
        } else {
            // En décimal
            dayOfMonth = Integer.parseInt(daytxt);
        }

        // Création du dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_day_of_month))
                .setSingleChoiceItems(R.array.days_of_month, dayOfMonth - 1, (dialog, which) -> {
                    // Sélectionner le jour correspondant
                    ((MainActivity) getActivity()).getDayEditText().setText(values[which]);
                    dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    // Fermer le dialogue sans rien changer
                });

        return builder.create();
    }
}