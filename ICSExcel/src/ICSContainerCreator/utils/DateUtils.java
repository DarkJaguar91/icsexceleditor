package ICSContainerCreator.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    protected static Date getFirstDayOfBussinessYear() {
        Calendar calendar = Calendar.getInstance();

        int month = calendar.get(Calendar.MONTH);
        int week = calendar.get(Calendar.WEEK_OF_MONTH);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (month < 7 || (week == 1 && day < 4)) {
            calendar.add(Calendar.YEAR, -1);
        }

        calendar.set(Calendar.MONTH, 7);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return calendar.getTime();
    }

    protected static int daysBetween(Date A, Date B) {
        final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

        return (int) ((A.getTime() - B.getTime()) / DAY_IN_MILLIS);

    }

    public static int getWeek(Date date) {
        return (int) Math.floor(daysBetween(date, getFirstDayOfBussinessYear()) / 7f);
    }

    public static Date fromString(String date) throws ParseException {
        DateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
        return format.parse(date);
    }

    public static String getDateFromWeek(int week) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getFirstDayOfBussinessYear());

        calendar.add(Calendar.WEEK_OF_YEAR, week);

        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        return format.format(calendar.getTime());
    }
}
