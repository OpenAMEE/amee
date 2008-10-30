package com.jellymold.utils.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ZodiacDate {

    private static final int YEAR = 1900;

    protected static String[] zodiac = {
            "Capricorn", "Aquarius", "Pisces",
            "Aries", "Taurus", "Gemini",
            "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius"
    };

    protected static GregorianCalendar[] signStartDates = {
            new GregorianCalendar(YEAR, Calendar.JANUARY, 21),
            new GregorianCalendar(YEAR, Calendar.FEBRUARY, 20),
            new GregorianCalendar(YEAR, Calendar.MARCH, 21),
            new GregorianCalendar(YEAR, Calendar.APRIL, 21),
            new GregorianCalendar(YEAR, Calendar.MAY, 22),
            new GregorianCalendar(YEAR, Calendar.JUNE, 22),
            new GregorianCalendar(YEAR, Calendar.JULY, 24),
            new GregorianCalendar(YEAR, Calendar.AUGUST, 24),
            new GregorianCalendar(YEAR, Calendar.SEPTEMBER, 24),
            new GregorianCalendar(YEAR, Calendar.OCTOBER, 24),
            new GregorianCalendar(YEAR, Calendar.NOVEMBER, 23),
            new GregorianCalendar(YEAR, Calendar.DECEMBER, 22)
    };


    public static String getZodiacSign(Calendar cal) {
        GregorianCalendar comp = new GregorianCalendar(YEAR, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        String ret = zodiac[0];
        int i = 0;
        for (GregorianCalendar start : signStartDates) {
            if (comp.before(start)) {
                ret = zodiac[i];
                break;
            }
            i++;
        }
        return ret;
    }

    public static String getZodiacSign(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return getZodiacSign(cal);
    }

    public static String getZodiacSign(String string) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String ret = "";
        try {
            Date date = (Date) formatter.parse(string);
            ret = getZodiacSign(date);
        } catch (ParseException pe) {
            //swallow
        }
        return ret;
    }

    //test
    public static void main(String... args) {
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            //Aquarius (me)
            Date date = (Date) formatter.parse("26/01/75");
            System.out.print("Aquarius -> ");
            System.out.print(getZodiacSign(date) + "\r\n");
            //Taurus (David Beckham, apparently)
            date = (Date) formatter.parse("02/05/1975");
            System.out.print("Taurus -> ");
            System.out.print(getZodiacSign(date) + "\r\n");
            //Mystic Meg 
            System.out.print("Leo -> ");
            System.out.print(getZodiacSign("27/07/1942") + "\r\n");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
