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
public class Calendarium
{
    public enum InitiumCalendarii
    {
        SINE, // les années ne sont pas affichées
        ANNO_DOMINI, // les années sont comptées depuis JC
        AB_URBE_CONDITA, // les années sont comptées depuis la création de Rome
    }

    /**
     * @param p_tempus date a convertir en latin
     * @param p_est si true, la traduction commence par "Est ..."
     * @param p_nomenDiei si true, ajoute le jour de la semaine
     * @param p_initium Point de référence pour les années.
     * @param p_eraBrevis si true, l’erre sera abrégée (A.D. ou  A.U.C.), sinon elle sera en toute lettre.
     * @return
     */
    public static String tempus(Date p_tempus, boolean p_est, boolean p_nomenDiei, InitiumCalendarii p_initium, boolean p_eraBrevis)
    {
        Calendar calendarium = new GregorianCalendar();
        calendarium.setTime(p_tempus);

        int dies = calendarium.get(Calendar.DAY_OF_MONTH);
        int mensis = calendarium.get(Calendar.MONTH)+1;
        int idus = idusMensium(mensis);
        int nonae = idus-8;
        int dieiMensis=calendarium.getActualMaximum(Calendar.DAY_OF_MONTH);
        int diesHebdomadis=calendarium.get(Calendar.DAY_OF_WEEK);

        StringBuilder summa=new StringBuilder();

        if (p_est)
            summa.append("Est ");

        if (p_nomenDiei)
            summa.append(nomenDiei(diesHebdomadis));

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

        if (p_initium == InitiumCalendarii.AB_URBE_CONDITA)
        {
            int annus=calendarium.get(Calendar.YEAR);

            // si est ante diem undecimum kalendas maias, annum uno minuit quia Roma ante diem undecimum kalendas maias aedificata est.
            if (calendarium.get(Calendar.MONTH) < Calendar.APRIL || (calendarium.get(Calendar.MONTH) == Calendar.APRIL) && (calendarium.get(Calendar.DAY_OF_MONTH) < 21))
                annus--;

            // Quia Roma in DCCLIII aedificatus est.
            if (calendarium.get(GregorianCalendar.ERA) == GregorianCalendar.AD)
                annus+=753;
            else
                annus=754-annus;

            boolean anteUrbemConditam=false;
            if (annus<=0)
            {
                annus=-annus+1;
                anteUrbemConditam=true;
            }

            String annusRomanus = romanusNumerus(annus);

            if (anteUrbemConditam)
                summa.append(" ").append(annusRomanus).append(p_eraBrevis ? " Ant.U.C." : " ante Urbem conditam.");
            else
                summa.append(" ").append(annusRomanus).append(p_eraBrevis ? " A.U.C." : " ab Urbe condita.");
        }
        else if (p_initium == InitiumCalendarii.ANNO_DOMINI)
        {
            String annusRomanus = romanusNumerus(calendarium.get(Calendar.YEAR));
            if (calendarium.get(GregorianCalendar.ERA) == GregorianCalendar.AD)
                summa.append(" ").append(annusRomanus).append(p_eraBrevis ? " A.D." : " anno domini.");
            else
                summa.append(" ").append(annusRomanus).append(p_eraBrevis ? " A.C.N." : " ante christum natum.");
        }
        else
            summa.append(".");

        return summa.toString();
    }

    private static String nomenDiei (int p_diesHebdomadis)
    {
        switch (p_diesHebdomadis)
        {
            case Calendar.SUNDAY: return "dies solis ";
            case Calendar.MONDAY : return "dies lunae ";
            case Calendar.TUESDAY : return "dies Martis ";
            case Calendar.WEDNESDAY : return "dies Mercurii ";
            case Calendar.THURSDAY : return "dies Jovis ";
            case Calendar.FRIDAY : return "dies Veneris ";
            case Calendar.SATURDAY : return "dies Saturni ";
        }

        throw new IllegalArgumentException (String.valueOf(p_diesHebdomadis));
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

        throw new IllegalArgumentException (String.valueOf(p_arg));
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