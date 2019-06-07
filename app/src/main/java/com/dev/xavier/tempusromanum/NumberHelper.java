package com.dev.xavier.tempusromanum;

import android.util.Log;

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
public class NumberHelper {

    private static final String[] romains = new String[] { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
    private static final int[] decimaux = new int[] { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };

    public static int decimal(String nombreRomain) throws NumberFormatException
    {
        StringBuilder sb = new StringBuilder(nombreRomain);
        int decimal = 0;

        for(int i = 0; i < romains.length; i++)
        {
            if(sb.length() < romains[i].length())
                continue;

            while(sb.length() > 0 && sb.substring(0, romains[i].length()).equals(romains[i]))
            {
                decimal += decimaux[i];
                sb.delete(0, romains[i].length());
            }
        }

        if(sb.length() > 0) {
            Log.d("roman_number_error", "Le nombre romain entré est faux");
            throw new NumberFormatException();
        }

        return decimal;
    }

    public static boolean isDecimal(char c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
            default:
                return false;
        }
    }

    public static boolean isRoman(char c) {
        switch (c) {
            case 'I':
            case 'V':
            case 'X':
            case 'L':
            case 'C':
            case 'D':
            case 'M':
                return true;
            default:
                return false;
        }
    }
}
