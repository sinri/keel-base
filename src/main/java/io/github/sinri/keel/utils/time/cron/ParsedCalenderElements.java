package io.github.sinri.keel.utils.time.cron;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

/**
 * Represents parsed calendar elements, including minute, hour, day, month, weekday, and second.
 * This class is used to encapsulate the components of a date and time for use in matching against cron expressions.
 *
 * @since 4.0.0
 */
public class ParsedCalenderElements {
    public final int minute;
    public final int hour;
    public final int day;
    public final int month;
    public final int weekday;

    // debug use
    public final int second;

    /**
     * Constructs a new instance of ParsedCalenderElements with the specified minute, hour, day, month, weekday, and
     * second.
     *
     * @param minute  the minute component (0-59)
     * @param hour    the hour component (0-23)
     * @param day     the day of the month (1-31)
     * @param month   the month (1-12)
     * @param weekday the day of the week (0-6, where 0 is Sunday)
     * @param second  the second component (0-59), used for debugging
     */
    public ParsedCalenderElements(int minute, int hour, int day, int month, int weekday, int second) {
        this.minute = minute;
        this.hour = hour;
        this.day = day;
        this.month = month;
        this.weekday = weekday;
        this.second = second;
    }

    /**
     * Constructs a new instance of ParsedCalenderElements with the specified minute, hour, day, month, and weekday.
     * The second component is set to 0 by default.
     *
     * @param minute  the minute component (0-59)
     * @param hour    the hour component (0-23)
     * @param day     the day of the month (1-31)
     * @param month   the month (1-12)
     * @param weekday the day of the week (0-6, where 0 is Sunday)
     */
    public ParsedCalenderElements(int minute, int hour, int day, int month, int weekday) {
        this(minute, hour, day, month, weekday, 0);
    }

    /**
     * Constructs a new instance of ParsedCalenderElements from the provided Calendar object.
     * This constructor extracts and sets the minute, hour, day, month, weekday, and second components
     * from the given Calendar object. The month is adjusted to be 1-based (January = 1), and the weekday
     * is adjusted to be 0-based (Sunday = 0).
     *
     * @param currentCalendar the Calendar object from which to extract the date and time components
     */
    public ParsedCalenderElements(@NotNull Calendar currentCalendar) {
        minute = currentCalendar.get(Calendar.MINUTE);
        hour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        day = currentCalendar.get(Calendar.DAY_OF_MONTH);
        month = 1 + currentCalendar.get(Calendar.MONTH);// make JAN 1, ...
        weekday = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1; // make sunday 0, ...
        second = currentCalendar.get(Calendar.SECOND);
    }

    /**
     * Returns a string representation of the parsed calendar elements.
     * The format is "(second) minute hour day month weekday".
     *
     * @return a string in the format "(second) minute hour day month weekday"
     */
    @Override
    public String toString() {
        return "(" + second + ") " + minute + " " + hour + " " + day + " " + month + " " + weekday;
    }
}
