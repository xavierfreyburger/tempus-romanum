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
public class ChooseDayDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // String[] values = getResources().getStringArray(R.array.days_of_month_values);
        final int dayOfMonth = Integer.valueOf(((MainActivity)getActivity()).getDayEditText().getText().toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_day_of_month))
                .setSingleChoiceItems(R.array.days_of_month_values, dayOfMonth - 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Sélectionner le jour correspondant
                        ((MainActivity)getActivity()).getDayEditText().setText(String.valueOf(which + 1));
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