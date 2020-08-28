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
public class ChooseMonthDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final String[] values = getResources().getStringArray(R.array.months);
        final String monthtxt = ((MainActivity)getActivity()).getMonthEditText().getText().toString();

        // Récupération de la valeur du mois actuellement sélectionné
        int month = 1;

        if(((MainActivity)getActivity()).isRomanNumber()) {
            // En romain
            for(int i = 1 ; i <= values.length ; i++) {
                if(values[i - 1].equals(monthtxt)) {
                    month = i;
                    break;
                }
            }
        } else {
            month = Integer.parseInt(monthtxt);
        }

        // Création du dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_month))
                .setSingleChoiceItems(R.array.months, month - 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Sélectionner le mois correspondant
                        ((MainActivity)getActivity()).getMonthEditText().setText(values[which]);
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Fermer le dialogue sans rien changer
                    }
                });
        return builder.create();
    }
}