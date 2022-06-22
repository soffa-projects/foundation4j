package dev.soffa.foundation.commons;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import dev.soffa.foundation.error.TechnicalException;
import lombok.SneakyThrows;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateUtil {

    public static final String DEFAULT_FORMAT = "yyyyMMddHHmmss";
    public static Locale defaultLocale = Locale.getDefault();

    public static Date now() {
        return Date.from(Instant.now());
    }

    public static long nano() {
        return Date.from(Instant.now()).getTime() * 1000;
    }

    public static long nano(Date date) {
        return date.getTime() * 1000;
    }

    public static void setDefaultLocale(Locale locale) {
        defaultLocale = locale;
    }

    public static String format() {
        return format(DEFAULT_FORMAT);
    }

    public static String format(String format) {
        return format(format, new Date());
    }

    @Deprecated
    public static String format(String format, Date date) {
        return format(date, format);
    }

    public static String[] format(Date from, Date to, String format) {
        return new String[]{format(from, format), format(to, format)};
    }

    public static String format(Date date, String format) {
        return new SimpleDateFormat(format, defaultLocale).format(date);
    }

    public static String format(Date date) {
        return new SimpleDateFormat(DEFAULT_FORMAT, defaultLocale).format(date);
    }

    @Deprecated
    public static Date addSeconds(Date date, int value) {
        return new DateTime(date).plusSeconds(value).toDate();
    }

    public static Date plusSeconds(Date date, int value) {
        return new DateTime(date).plusSeconds(value).toDate();
    }

    public static Date plusMinutes(Date date, int value) {
        return new DateTime(date).plusMinutes(value).toDate();
    }

    public static Date plusHours(Date date, int value) {
        return new DateTime(date).plusHours(value).toDate();
    }

    public static Date plusDays(Date date, int value) {
        return new DateTime(date).plusDays(value).toDate();
    }

    public static Date plusYears(Date date, int value) {
        return new DateTime(date).plusYears(value).toDate();
    }

    public static Date minusMonth(Date date, int value) {
        return new DateTime(date).minusMonths(value).toDate();
    }

    public static Date endOfTheMonth(Date date) {
        return new DateTime(date).dayOfMonth().withMaximumValue().toDate();
    }

    public static boolean isBeforeNow(Date date) {
        return new DateTime(date).isBeforeNow();
    }

    public static boolean isAfterNow(Date date) {
        return new DateTime(date).isAfterNow();
    }

    public static boolean isBefore(Date date1, Date date2) {
        return new DateTime(date1).isBefore(new DateTime(date2));
    }

    public static boolean isBeforeNowMinusSeconds(Date date1, int secondsBefore) {
        return new DateTime(date1).isBefore(DateTime.now().minusSeconds(secondsBefore));
    }

    public static boolean isAfter(Date date1, Date date2) {
        return new DateTime(date1).isAfter(new DateTime(date2));
    }

    @SneakyThrows
    public static Date parse(String input) {
        if (input == null) {
            return null;
        }
        try {
            Parser parser = new Parser();
            List<?> groups = parser.parse(input);
            DateGroup first = (DateGroup) groups.get(0);
            return first.getDates().get(0);
        } catch (Exception e) {
            throw new TechnicalException("INVALID_DATE: " + e.getMessage(), e);
        }
    }


}
