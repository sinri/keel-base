package io.github.sinri.keel.utils.time.cron;


import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents a cron expression and provides methods to match it against a given date and time.
 * The cron expression is parsed into sets of valid values for each field (minute, hour, day, month, weekday).
 * This class supports the standard cron syntax, including ranges, lists, and increments.
 *
 * @since 2.9.3 moved from io.github.sinri.keel.servant.sundial to here.
 */
public class KeelCronExpression {
    final Set<Integer> minuteOptions = new HashSet<>();
    final Set<Integer> hourOptions = new HashSet<>();
    final Set<Integer> dayOptions = new HashSet<>();
    final Set<Integer> monthOptions = new HashSet<>();
    final Set<Integer> weekdayOptions = new HashSet<>();
    private final @NotNull String rawCronExpression;

    /**
     * Constructs a new KeelCronExpression from the given raw cron expression.
     *
     * @param rawCronExpression the raw cron expression to parse and validate
     *                          The expression should consist of 5 space-separated fields:
     *                          - Minute (0-59)
     *                          - Hour (0-23)
     *                          - Day of the month (1-31)
     *                          - Month (1-12)
     *                          - Day of the week (0-6, where 0 is Sunday)
     *                          Each field can be a specific value, a range, a list of values, or an asterisk (*).
     *                          Examples: "0 0 1 1 0" (every first day of January at midnight), "0 0 * * *" (midnight
     *                          every day)
     * @throws RuntimeException if the provided cron expression is invalid (e.g., incorrect number of fields or invalid
     *                          values)
     */
    public KeelCronExpression(@NotNull String rawCronExpression) {
        this.rawCronExpression = rawCronExpression;

        String[] parts = rawCronExpression.trim().split("\\s+");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid Cron Expression");
        }

        String minuteExpression = parts[0]; // 0-59
        String hourExpression = parts[1]; // 0-23
        String dayExpression = parts[2]; // 1-31
        String monthExpression = parts[3]; // 1-12
        String weekdayExpression = parts[4];// 0-6

        parseField(minuteExpression, minuteOptions, 0, 59);
        parseField(hourExpression, hourOptions, 0, 23);
        parseField(dayExpression, dayOptions, 1, 31);
        parseField(monthExpression, monthOptions, 1, 12);
        parseField(weekdayExpression, weekdayOptions, 0, 6);
    }

    /**
     * Parses the given Calendar object and returns a ParsedCalenderElements instance.
     *
     * @param currentCalendar the Calendar object to parse, must not be null
     * @return a ParsedCalenderElements instance containing the parsed date and time components
     * @since 3.2.4
     */
    public static ParsedCalenderElements parseCalenderToElements(@NotNull Calendar currentCalendar) {
        return new ParsedCalenderElements(currentCalendar);
    }

    /**
     * Determines if the given Calendar object matches the cron expression.
     *
     * @param currentCalendar the Calendar object to match against the cron expression, must not be null
     * @return true if the Calendar object matches the cron expression, false otherwise
     */
    public boolean match(@NotNull Calendar currentCalendar) {
        ParsedCalenderElements parsedCalenderElements = new ParsedCalenderElements(currentCalendar);
        return match(parsedCalenderElements);
    }

    /**
     * Determines if the given parsed calendar elements match the cron expression.
     *
     * @param parsedCalenderElements the ParsedCalenderElements instance to match against the cron expression, must not
     *                               be null
     * @return true if the ParsedCalenderElements match the cron expression, false otherwise
     */
    public boolean match(@NotNull ParsedCalenderElements parsedCalenderElements) {
        return minuteOptions.contains(parsedCalenderElements.minute)
                && hourOptions.contains(parsedCalenderElements.hour)
                && dayOptions.contains(parsedCalenderElements.day)
                && monthOptions.contains(parsedCalenderElements.month)
                && weekdayOptions.contains(parsedCalenderElements.weekday);
    }

    /**
     * Parses the given raw component of a cron expression and populates the option set with valid values.
     *
     * @param rawComponent the raw component of the cron expression to parse
     * @param optionSet    the set to populate with the parsed values
     * @param min          the minimum allowed value for the component
     * @param max          the maximum allowed value for the component
     * @throws IllegalArgumentException if the raw component is invalid or contains out-of-range values
     */
    private void parseField(@NotNull String rawComponent, @NotNull Set<Integer> optionSet, int min, int max) {
        if (rawComponent.equals("*")) {
            for (int i = min; i <= max; i++) {
                optionSet.add(i);
            }
            return;
        }

        ArrayList<String> parts = new ArrayList<>();
        if (rawComponent.contains(",")) {
            String[] t1 = rawComponent.split(",");
            parts.addAll(Arrays.asList(t1));
        } else {
            parts.add(rawComponent);
        }

        for (String part : parts) {
            part = part.trim();

            Matcher matcher0 = Pattern.compile("^\\d+$").matcher(part);
            if (matcher0.matches()) {
                optionSet.add(Integer.parseInt(part));
                continue;
            }

            Matcher matcher1 = Pattern.compile("^(\\d+)-(\\d+)$").matcher(part);
            if (matcher1.matches()) {
                int start = Integer.parseInt(matcher1.group(1));
                int end = Integer.parseInt(matcher1.group(2));
                if (start < min || end > max || start > end) {
                    throw new IllegalArgumentException();
                }
                for (int i = start; i <= end; i++) {
                    optionSet.add(i);
                }
                continue;
            }

            Matcher matcher2 = Pattern.compile("^\\*[*/](\\d+)$").matcher(part);
            if (matcher2.matches()) {
                int mask = Integer.parseInt(matcher2.group(1));
                for (int i = 0; i <= max; i += mask) {
                    if (i >= min) {
                        optionSet.add(i);
                    }
                }
                continue;
            }

            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the raw cron expression that was used to initialize this KeelCronExpression instance.
     *
     * @return the raw cron expression as a non-null String
     */
    @NotNull
    public String getRawCronExpression() {
        return rawCronExpression;
    }

    /**
     * Returns a string representation of this KeelCronExpression, which is the raw cron expression used to initialize
     * the instance.
     *
     * @return the raw cron expression as a non-null String
     */
    @Override
    public String toString() {
        return getRawCronExpression();
    }
}
