package com.dev.xavier.tempusromanum;

import android.text.InputFilter;
import android.text.Spanned;

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
class NumberHelper {
    private static final String[][] romain = {{"MMM", "MM", "M"}, {"CM", "DCCCC", "DCCC", "DCC", "DC", "D", "CD", "CCCC", "CCC", "CC", "C"},
            {"XC", "LXXXX", "LXXX", "LXX", "LX", "L", "XL", "XXXX", "XXX", "XX", "X"}, {"IX", "VIIII", "VIII", "VII", "VI", "V", "IIII", "IV", "III", "II", "I"}};
    private static final int[][] decimal = {{3000, 2000, 1000}, {900, 900, 800, 700, 600, 500, 400, 400, 300, 200, 100},
            {90, 90, 80, 70, 60, 50, 40, 40, 30, 20, 10}, {9, 9, 8, 7, 6, 5, 4, 4, 3, 2, 1}};

    static InputFilter romanNumeraFilter = new InputFilter()
    {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
        {
            for (int i = start; i < end; i++)
            {
                final char ch = source.charAt(i);
                // only Roman numeral are allowed
                if(ch == '\n') {
                    return null;
                } else if( !isRoman(ch))
                {
                    if(source instanceof Spanned) {
                        StringBuilder sb = new StringBuilder(source);
                        for (int j = sb.length() - 1; j >= 0; j--) {
                            if(isRoman(sb.charAt(j))) {
                                continue;
                            }
                            sb.deleteCharAt(j);
                        }
                        return sb.toString();
                    }
                    else {
                        return "";
                    }
                }
            }

            return null;
        }
    };


    static Integer decimal(String nombreRomain) throws NumberFormatException
    {
        if(nombreRomain == null || nombreRomain.length() == 0) {
            return null;
        }
        int value = 0;
        StringBuilder sb = new StringBuilder(nombreRomain);

        for(int i = 0 ; i < romain.length ; i++) {
            if(sb.length() == 0) {
                return value;
            }
            for(int j = 0 ; j < romain[i].length ; j++) {

                final String rom = romain[i][j];

                if(sb.length() < rom.length()) {
                    continue;
                }

                if(sb.substring(0, rom.length()).equals(rom)) {
                    value += decimal[i][j];
                    sb.delete(0, rom.length());
                    break;
                }
            }
        }

        if(sb.length() > 0) {
            throw new NumberFormatException();
        }

        return value;
    }

    static boolean isDecimal(char c) {
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

    static boolean isRoman(char c) {
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
