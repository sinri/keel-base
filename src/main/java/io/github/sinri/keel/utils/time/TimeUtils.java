package io.github.sinri.keel.utils.time;

import io.github.sinri.keel.utils.time.cron.KeelCronExpression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    public static final String MYSQL_DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String MYSQL_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String MYSQL_DATE_PATTERN = "yyyy-MM-dd";
    public static final String MYSQL_TIME_PATTERN = "HH:mm:ss";
    public static final String GMT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * @return current timestamp expressed in MySQL Date Time Format
     * @since 3.0.10
     */
    public static String getCurrentDatetime() {
        return getCurrentDateExpression(MYSQL_DATETIME_PATTERN);
    }

    /**
     * @since 3.0.10
     */
    public static String getCurrentDate() {
        return getCurrentDateExpression(MYSQL_DATE_PATTERN);
    }

    /**
     * @param format "yyyyMMdd" or "yyyy-MM-dd HH:mm:ss", etc. if null, return null
     * @return the date string or null
     * @since 2.6
     */
    public static String getCurrentDateExpression(String format) {
        Date currentTime = new Date();
        return getDateExpression(currentTime, format);
    }

    /**
     * @param format for example: yyyy-MM-ddTHH:mm:ss
     * @since 2.6
     */
    public static String getDateExpression(Date date, String format) {
        if (format == null || format.isEmpty()) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    /**
     * @since 3.0.10
     */
    public static String getDateExpression(long timestamp, String format) {
        Date date = new Date(timestamp);
        return getDateExpression(date, format);
    }

    /**
     * From MySQL DataTime String to Standard Expression
     *
     * @param localDateTimeExpression yyyy-MM-ddTHH:mm:ss
     * @return yyyy-MM-dd HH:mm:ss
     * @since 2.7
     */
    public static String getMySQLFormatLocalDateTimeExpression(String localDateTimeExpression) {
        return LocalDateTime.parse(localDateTimeExpression)
                            .format(DateTimeFormatter.ofPattern(MYSQL_DATETIME_PATTERN));
    }

    /**
     * @return Date Time in RFC 1123: Mon, 31 Oct 2022 01:18:43 GMT
     * @since 2.9.1
     */
    public static String getGMTDateTimeExpression(ZoneId zoneId) {
        DateTimeFormatter gmt = DateTimeFormatter.ofPattern(
                                                         GMT_PATTERN,
                                                         Locale.ENGLISH
                                                 )
                                                 .withZone(ZoneId.of("GMT"));
        return gmt.format(LocalDateTime.now(zoneId));
    }

    /**
     * @since 3.0.1
     */
    public static String getGMTDateTimeExpression() {
        return getGMTDateTimeExpression(ZoneId.systemDefault());
    }

    /**
     * As of 4.1.0, reimplemented.
     *
     * @since 3.0.1
     */
    protected static String makeStandardWidthField(int x, int w) {
        if (x < 0) throw new IllegalArgumentException();
        String numeric = String.valueOf(x);
        if (w < numeric.length()) throw new IllegalArgumentException();
        String prefix = "0".repeat(w - numeric.length());
        return prefix + numeric;
    }

    /**
     * @since 3.0.1
     */
    public static String toMySQLDatetime(LocalDateTime datetime) {
        return makeStandardWidthField(datetime.getYear(), 4)
                + "-" + makeStandardWidthField(datetime.getMonthValue(), 2)
                + "-" + makeStandardWidthField(datetime.getDayOfMonth(), 2)
                + " "
                + makeStandardWidthField(datetime.getHour(), 2)
                + ":" + makeStandardWidthField(datetime.getMinute(), 2)
                + ":" + makeStandardWidthField(datetime.getSecond(), 2);
    }

    /**
     * @param formatPattern MYSQL_DATETIME_PATTERN,MYSQL_DATE_PATTERN,MYSQL_TIME_PATTERN
     * @since 3.0.1
     */
    public static String toMySQLDatetime(LocalDateTime localDateTime, String formatPattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatPattern);
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * As of 4.1.0, the date string is parsed with strict mode.
     *
     * @param dateStr   A string expressing date time.
     * @param formatStr The format pattern. Consider to use provided pattern constants.
     * @return Date instance.
     * @since 3.0.11
     */
    public static @Nullable Date parseExpressionToDateInstance(@Nonnull String dateStr, @Nonnull String formatStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatStr);
            format.setLenient(false); // 启用严格模式，校验日期是否真实存在
            return format.parse(dateStr);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Invalid date format or non-existent date: " + dateStr + " with pattern: " + formatStr, e);
        }
    }

    /**
     * @param instant 时间戳（UTC 时间）
     * @param zoneId  时区，如 {@code ZoneOffset.UTC}
     * @param pattern 日期时间表述字符串的格式
     * @return 指定时间戳在指定时区对应的日期时间表述字符串
     * @since 4.0.6
     */
    public static String getInstantExpression(Instant instant, ZoneId zoneId, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).withZone(zoneId).format(instant);
    }

    /**
     * @since 4.0.0
     */
    public static boolean isNowMatchCronExpression(@Nonnull String cronExpression) {
        return new KeelCronExpression(cronExpression).match(Calendar.getInstance());
    }

    /**
     * @since 4.0.0
     */
    public static boolean isNowMatchCronExpression(@Nonnull KeelCronExpression cronExpression) {
        return cronExpression.match(Calendar.getInstance());
    }
}
