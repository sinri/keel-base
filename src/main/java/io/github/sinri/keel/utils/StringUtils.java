package io.github.sinri.keel.utils;

import io.github.sinri.keel.utils.encryption.base32.Base32;
import io.vertx.core.buffer.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Map<String, String> HttpEntityEscapeDictionary = Map.of(
            "&", "&amp;",
            "@", "&commat;",
            "<", "&lt;",
            ">", "&gt;"
    );
    private static final String NyaCodeDict = "-ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz.";
    private static final char[] NyaCodeDictChars = NyaCodeDict.toCharArray();


    private StringUtils() {
    }

    /**
     * 给定一个数组x，用separator作为分隔符，将x中的所有元素的字符串化值拼接起来。
     *
     * @param x         an array
     * @param separator separator
     * @param <T>       the class of item in array
     * @return the joined string
     * @since 1.11
     * @since 3.0.8 toString → String.valueOf
     */
    @NotNull
    @Deprecated
    public static <T> String joinStringArray(@Nullable T[] x, @NotNull String separator) {
        if (x == null) return "";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x.length; i++) {
            if (i > 0) result.append(separator);
            result.append(x[i]);
        }
        return result.toString();
    }

    /**
     * 给定一个列表x，用separator作为分隔符，将x中的所有元素的字符串化值拼接起来。
     *
     * @param x         a list
     * @param separator separator
     * @return the joined string
     * @since 2.0 List → Collection
     * @since 3.0.7 Collection → Iterable
     * @since 3.0.8 toString → String.valueOf
     */
    @NotNull
    @Deprecated
    public static String joinStringArray(@Nullable Iterable<?> x, @NotNull String separator) {
        if (x == null) return "";

        StringBuilder result = new StringBuilder();

        final int[] i = {0};
        x.forEach(item -> {
            if (i[0] > 0) result.append(separator);
            result.append(item);
            i[0] += 1;
        });

        return result.toString();
    }

    /**
     * 获取一个Buffer的十六进制表达，每个字节以两个字符表示（字母大写）。 字节间空格分隔；每行容纳一定量的字节数。
     *
     * @param buffer  an instance of Buffer defined in Vertx
     * @param rowSize how many bytes in one row
     * @return the matrix of hex as string
     * @since 1.11
     */
    @NotNull
    public static String bufferToHexMatrix(@NotNull Buffer buffer, int rowSize) {
        StringBuilder matrix = new StringBuilder();
        String s = BinaryUtils.encodeHexWithUpperDigits(buffer);
        for (int i = 0; i < s.length(); i += 2) {
            matrix.append(s, i, i + 2).append(" ");
            if ((i / 2) % rowSize == rowSize - 1) {
                matrix.append("\n");
            }
        }
        return matrix.toString();
    }

    /**
     * Make `apple_pie` to `ApplePie` or `applePie`.
     *
     * @since 3.0.12
     */
    @Nullable
    public static String fromUnderScoreCaseToCamelCase(@Nullable String underScoreCase, boolean firstCharLower) {
        if (underScoreCase == null) {
            return null;
        }
        if (underScoreCase.isEmpty()) {
            return "";
        }
        String[] parts = underScoreCase.toLowerCase().split("[\\s_]");
        List<String> camel = new ArrayList<>();

        boolean isFirst = true;
        for (var part : parts) {
            if (part != null && !part.isBlank()) {
                if (isFirst && firstCharLower) {
                    camel.add(part);
                    isFirst = false;
                } else {
                    camel.add(part.substring(0, 1).toUpperCase() + part.substring(1));
                }
            }
        }

        return String.join("", camel);
    }

    /**
     * Make `apple_pie` to `ApplePie`.
     *
     * @since 2.7
     */
    public static String fromUnderScoreCaseToCamelCase(@Nullable String underScoreCase) {
        return fromUnderScoreCaseToCamelCase(underScoreCase, false);
    }

    /**
     * @since 2.7
     */
    @Nullable
    public static String fromCamelCaseToUserScoreCase(@Nullable String camelCase) {
        if (camelCase == null) {
            return null;
        }
        if (camelCase.isBlank()) {
            return "";
        }
        if (camelCase.length() == 1) {
            return camelCase.toLowerCase();
        }
        List<String> parts = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            String current = camelCase.substring(i, i + 1);
            if (current.matches("[\\s_]")) continue;
            if (part.length() == 0) {
                part.append(current.toLowerCase());
            } else {
                if (current.matches("[A-Z]")) {
                    parts.add(part.toString());
                    part = new StringBuilder();
                }
                part.append(current.toLowerCase());
            }
        }
        if (part.length() > 0) {
            parts.add(part.toString());
        }
        return String.join("_", parts);
    }

    /**
     * @since 2.9
     */
    @NotNull
    public static String buildStackChainText(@Nullable StackTraceElement[] stackTrace,
                                             @NotNull Set<String> ignorableStackPackageSet) {
        StringBuilder sb = new StringBuilder();
        if (stackTrace != null) {
            String ignoringClassPackage = null;
            int ignoringCount = 0;
            for (StackTraceElement stackTranceItem : stackTrace) {
                String className = stackTranceItem.getClassName();
                String matchedClassPackage = null;
                for (var cp : ignorableStackPackageSet) {
                    if (className.startsWith(cp)) {
                        matchedClassPackage = cp;
                        break;
                    }
                }
                if (matchedClassPackage == null) {
                    if (ignoringCount > 0) {
                        sb.append("\t\t")
                          .append("[").append(ignoringCount).append("] ")
                          .append(ignoringClassPackage)
                          .append(System.lineSeparator());

                        ignoringClassPackage = null;
                        ignoringCount = 0;
                    }

                    sb.append("\t\t")
                      .append(stackTranceItem.getClassName())
                      .append(".")
                      .append(stackTranceItem.getMethodName())
                      .append(" (")
                      .append(stackTranceItem.getFileName())
                      .append(":")
                      .append(stackTranceItem.getLineNumber())
                      .append(")")
                      .append(System.lineSeparator());
                } else {
                    if (ignoringCount > 0) {
                        if (Objects.equals(ignoringClassPackage, matchedClassPackage)) {
                            ignoringCount += 1;
                        } else {
                            sb.append("\t\t")
                              .append("[").append(ignoringCount).append("] ")
                              .append(ignoringClassPackage)
                              .append(System.lineSeparator());

                            ignoringClassPackage = matchedClassPackage;
                            ignoringCount = 1;
                        }
                    } else {
                        ignoringClassPackage = matchedClassPackage;
                        ignoringCount = 1;
                    }
                }
            }
            if (ignoringCount > 0) {
                sb.append("\t\t")
                  .append("[").append(ignoringCount).append("] ")
                  .append(ignoringClassPackage)
                  .append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    /**
     * @since 2.9
     */
    @NotNull
    public static String buildStackChainText(@Nullable StackTraceElement[] stackTrace) {
        return buildStackChainText(stackTrace, Set.of());
    }

    /**
     * @since 2.9
     */
    @NotNull
    public static String renderThrowableChain(@Nullable Throwable throwable, @NotNull Set<String> ignorableStackPackageSet) {
        if (throwable == null) return "";
        Throwable cause = throwable.getCause();
        StringBuilder sb = new StringBuilder();
        sb
                .append("\t")
                .append(throwable.getClass().getName())
                .append(": ")
                .append(throwable.getMessage())
                .append(System.lineSeparator())
                .append(buildStackChainText(throwable.getStackTrace(), ignorableStackPackageSet));

        while (cause != null) {
            sb
                    .append("\t↑ ")
                    .append(cause.getClass().getName())
                    .append(": ")
                    .append(cause.getMessage())
                    .append(System.lineSeparator())
                    .append(buildStackChainText(cause.getStackTrace(), ignorableStackPackageSet))
            ;

            cause = cause.getCause();
        }

        return sb.toString();
    }

    /**
     * @since 2.9
     */
    @NotNull
    public static String renderThrowableChain(@Nullable Throwable throwable) {
        return renderThrowableChain(throwable, StackUtils.IgnorableCallStackPackage);
    }

    /**
     * @since 2.9.4
     */
    @NotNull
    public static byte[] encodeWithBase64ToBytes(@NotNull String s) {
        return BinaryUtils.encodeWithBase64(s.getBytes());
    }

    /**
     * @since 2.9.4
     */
    @NotNull
    public static String encodeWithBase64(@NotNull String s) {
        return new String(encodeWithBase64ToBytes(s));
    }

    /**
     * @since 2.9.4
     */
    @NotNull
    public static byte[] decodeWithBase64ToBytes(@NotNull String s) {
        return Base64.getDecoder().decode(s);
    }

    /**
     * @since 2.9.4
     */
    @NotNull
    public static String encodeWithBase32(@NotNull String s) {
        return Base32.encode(s.getBytes());
    }

    /**
     * @since 2.9.4
     */
    @NotNull
    public static byte[] decodeWithBase32ToBytes(@NotNull String s) {
        return Base32.decode(s);
    }

    /**
     * @since 2.9.4
     */
    @NotNull
    public static String decodeWithBase32(@NotNull String s) {
        return new String(decodeWithBase32ToBytes(s));
    }

    /**
     * @param flags compile flags, such as `Pattern.DOTALL`.
     * @param group such as 0 for the entire, n for the Nth component.
     * @since 3.0.8
     */
    @NotNull
    public static List<String> regexFindAll(@NotNull String regex, int flags, @NotNull String text, int group) {
        List<String> blankParamGroups = new ArrayList<>();
        Pattern patternForSpacedArgument = Pattern.compile(regex, flags);
        Matcher patternForSpacedArgumentMatcher = patternForSpacedArgument.matcher(text);
        while (patternForSpacedArgumentMatcher.find()) {
            blankParamGroups.add(patternForSpacedArgumentMatcher.group(group));
        }
        return blankParamGroups;
    }

    /**
     * @see <a href="https://www.freeformatter.com/html-entities.html">HTTP Entities</a>
     * @since 3.0.11
     */
    public static String escapeForHttpEntity(String raw) {
        AtomicReference<String> x = new AtomicReference<>(raw);
        HttpEntityEscapeDictionary.forEach((k, v) -> x.set(x.get().replace(k, v)));
        return x.get();
    }

    /**
     * @since 3.2.14
     * @since 3.2.15 PR from yhzdys
     */
    public static String encodeToNyaCode(@NotNull String raw) {
        String encoded = URLEncoder.encode(raw, StandardCharsets.UTF_8);
        int i = 0;
        char[] chars = encoded.toCharArray(), buffer = new char[chars.length << 1];
        for (char c : chars) {
            buffer[i++] = NyaCodeDictChars[(c & 0B11000000) >> 6];
            buffer[i++] = NyaCodeDictChars[c & 0B00111111];
        }
        return new String(buffer);
    }

    /**
     * @since 3.2.14
     * @since 3.2.15 PR from yhzdys
     */
    public static String decodeFromNyaCode(@NotNull String code) {
        int idx = 0;
        char[] chars = code.toCharArray(), buffer = new char[chars.length >> 1];
        for (int i = 0; i < chars.length; ) {
            buffer[idx++] = (char) (NyaCodeDict.indexOf(chars[i++]) << 6 | NyaCodeDict.indexOf(chars[i++]));
        }
        return URLDecoder.decode(new String(buffer), StandardCharsets.UTF_8);
    }

    /**
     * Truncates a string to the specified length and adds an ellipsis if truncated.
     *
     * @param str       the string to truncate
     * @param maxLength the maximum length of the string
     * @return the truncated string
     * @since 4.0.12
     */
    @NotNull
    public static String truncateWithEllipsis(@Nullable String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     *
     * @param str the string to check
     * @return true if the string is null, empty, or contains only whitespace
     * @since 4.0.12
     */
    public static boolean isNullOrBlank(@Nullable String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Reverses a string.
     *
     * @param str the string to reverse
     * @return the reversed string
     * @since 4.0.12
     */
    @NotNull
    public static String reverse(@Nullable String str) {
        if (str == null) return "";
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * Counts the occurrences of a substring within a string.
     *
     * @param str    the string to search in
     * @param subStr the substring to search for
     * @return the number of occurrences
     * @since 4.0.12
     */
    public static int countOccurrences(@Nullable String str, @Nullable String subStr) {
        if (str == null || subStr == null || subStr.isEmpty()) return 0;
        return (str.length() - str.replace(subStr, "").length()) / subStr.length();
    }

    /**
     * Removes all whitespace from a string.
     *
     * @param str the string to remove whitespace from
     * @return the string with all whitespace removed
     * @since 4.0.12
     */
    @NotNull
    public static String removeWhitespace(@Nullable String str) {
        if (str == null) return "";
        return str.replaceAll("\\s+", "");
    }

    /**
     * Checks if a string contains only digits.
     *
     * @param str the string to check
     * @return true if the string contains only digits
     * @since 4.0.12
     */
    public static boolean isNumericAsIntegralNumber(@Nullable String str) {
        if (str == null || str.isEmpty()) return false;
        return str.chars().allMatch(Character::isDigit);
    }

    /**
     * Checks if a string represents a valid number (including negative numbers and decimals).
     * Examples of valid numbers:
     * - "123" (positive integer)
     * - "-123" (negative integer)
     * - "123.456" (positive decimal)
     * - "-123.456" (negative decimal)
     * - "0.123" (decimal less than 1)
     * - "-0.123" (negative decimal less than 1)
     *
     * @param str the string to check
     * @return true if the string represents a valid number
     * @since 4.0.12
     */
    public static boolean isNumericAsRealNumber(@Nullable String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Capitalizes the first letter of each word in a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string
     * @since 4.0.12
     */
    @NotNull
    public static String capitalizeWords(@Nullable String str) {
        if (str == null || str.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : str.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                c = Character.toUpperCase(c);
                capitalizeNext = false;
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Removes all non-alphanumeric characters from a string.
     *
     * @param str the string to clean
     * @return the cleaned string
     * @since 4.0.12
     */
    @NotNull
    public static String removeNonAlphanumeric(@Nullable String str) {
        if (str == null) return "";
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Checks if a string is a valid email address.
     *
     * @param email the email address to validate
     * @return true if the email address is valid
     * @since 4.0.12
     */
    public static boolean isValidEmail(@Nullable String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }


    /**
     * Generates a random string of specified length.
     *
     * @param length the length of the random string
     * @return the random string
     * @since 4.0.12
     */
    @NotNull
    public static String generateRandomString(int length) {
        return generateRandomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
    }

    /**
     * Generates a random string of specified length using custom character set.
     *
     * @param length  the length of the random string
     * @param charSet the character set to use for generating the random string
     * @return the random string
     * @since 4.0.12
     */
    @NotNull
    public static String generateRandomString(int length, @NotNull String charSet) {
        if (charSet.isEmpty()) {
            throw new IllegalArgumentException("Character set cannot be null or empty");
        }
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charSet.length());
            sb.append(charSet.charAt(index));
        }
        return sb.toString();
    }
}
