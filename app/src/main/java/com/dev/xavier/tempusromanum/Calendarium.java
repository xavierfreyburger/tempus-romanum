package com.dev.xavier.tempusromanum;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Copyright 2019 Damien Appel
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
public class Calendarium {
    public static String tempus(Date p_tempus)
    {
        Calendar calendarium = Calendar.getInstance();
        calendarium.setTime(p_tempus);

        int dies = calendarium.get(Calendar.DAY_OF_MONTH);
        int mensis = calendarium.get(Calendar.MONTH)+1;
        int idus = idusMensium(mensis);
        int nonae = idus-8;
        int dieiMensis=calendarium.getActualMaximum(Calendar.DAY_OF_MONTH);

        StringBuilder summa=new StringBuilder();

        if (dies==1)
            summa	.append("kalendis ")
                    .append(mensesRomaniAblativus(mensis));
        else if (dies >1 && dies < nonae -1)
            summa	.append("ante diem ")
                    .append(ordinalis(nonae-dies+1))
                    .append(" nonas ")
                    .append( mensesRomaniAccusativus(mensis));
        else if (dies == nonae -1)
            summa 	.append("pridie nonas ")
                    .append(mensesRomaniAccusativus(mensis));
        else if (dies == nonae)
            summa 	.append("nonis ")
                    .append(mensesRomaniAblativus(mensis));
        else if (dies > nonae && dies < idus - 1)
            summa	.append("ante diem ")
                    .append(ordinalis(idus-dies+1))
                    .append(" idus ")
                    .append(mensesRomaniAccusativus(mensis));
        else if (dies == idus - 1)
            summa	.append("pridie idus ")
                    .append(mensesRomaniAccusativus(mensis));
        else if (dies == idus)
            summa 	.append("idibus ")
                    .append(mensesRomaniAblativus(mensis));
        else if (dies > idus && dies < dieiMensis)
            summa	.append("ante diem ")
                    .append(ordinalis(dieiMensis-dies+2))
                    .append(" kalendas ")
                    .append(mensesRomaniAccusativus(mensis+1));
        else if (dies == dieiMensis)
            summa 	.append("pridie kalendas ")
                    .append(mensesRomaniAccusativus(mensis+1));

        String annusRomanus = romanusNumerus(calendarium.get(Calendar.YEAR));

        switch (calendarium.get(Calendar.ERA)) {
            case GregorianCalendar.AD:
                summa.append(" ").append(annusRomanus).append(" A.D.");
                break;
            case GregorianCalendar.BC:
                summa.append(" ").append(annusRomanus).append(" B.C.");
                break;
        }

        return summa.toString();
    }

    private static int idusMensium(int p_mensis)
    {
        if (p_mensis == 1 || p_mensis == 2 || p_mensis==4 || p_mensis==6 || p_mensis==8 || p_mensis==9 || p_mensis==11 || p_mensis==12)
            return 13;
        else
            return 15;
    }

    private static String mensesRomaniAblativus (int p_mensis)
    {
        switch (p_mensis)
        {
            case 1 : return "ianuariis";
            case 2 : return "februariis";
            case 3 : return "martiis";
            case 4 : return "aprilibus";
            case 5 : return "maiis";
            case 6 : return "iuniis";
            case 7 : return "iuliis";
            case 8 : return "augustis";
            case 9 : return "septembribus";
            case 10 : return "octobribus";
            case 11 : return "novembribus";
            case 12 : return "decembribus";
            case 13 : return "ianuariis";
        }

        throw new IllegalArgumentException (String.valueOf(p_mensis));
    }

    private static String mensesRomaniAccusativus (int p_mensis)
    {
        switch (p_mensis)
        {
            case 1 : return "ianuarias";
            case 2 : return "februarias";
            case 3 : return "martias";
            case 4 : return "apriles";
            case 5 : return "maias";
            case 6 : return "iunias";
            case 7 : return "iulias";
            case 8 : return "augustas";
            case 9 : return "septembres";
            case 10 : return "octobres";
            case 11 : return "novembres";
            case 12 : return "decembres";
            case 13 : return "ianuarias";
        }

        throw new IllegalArgumentException (String.valueOf(p_mensis));
    }

    private static String  ordinalis (int p_arg)
    {
        switch (p_arg)
        {
            case 3 :	return "tertium" ;
            case 4 :	return "quartum" ;
            case 5 :	return "quintum" ;
            case 6 :	return "sextum";
            case 7 :	return "septimum" ;
            case 8 :	return "octavum" ;
            case 9 :	return "nonum";
            case 10 :	return "decimum" ;
            case 11 :	return "undecimum" ;
            case 12 :	return "duodecimum";
            case 13 :	return "tertium decimum";
            case 14 :	return "quartum decimum";
            case 15 :	return "quintum decimum";
            case 16 :	return "sextum decimum" ;
            case 17 :	return "septimum decimum" ;
            case 18 :	return "duodevincesimum";
            case 19 :	return "undevincesimum";
        }

        return "error";
    }

    private static String romanusNumerus(int p_numerus)
    {
        StringBuilder sb = new StringBuilder();
        int times = 0;
        String[] romaniNumeri = new String[] { "I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M" };
        int[] intergri = new int[] { 1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000 };
        for (int i = intergri.length - 1; i >= 0; i--)
        {
            times = p_numerus / intergri[i];
            p_numerus %= intergri[i];
            while (times > 0)
            {
                sb.append(romaniNumeri[i]);
                times--;
            }
        }
        return sb.toString();
    }
}
