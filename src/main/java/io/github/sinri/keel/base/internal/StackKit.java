package io.github.sinri.keel.base.internal;

import io.github.sinri.keel.base.KeelInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class StackKit {

    @NotNull
    public static String renderThrowableChain(@Nullable Throwable throwable) {
        return renderThrowableChain(throwable, KeelInstance.IgnorableCallStackPackage);
    }

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

    @NotNull
    private static String buildStackChainText(@Nullable StackTraceElement[] stackTrace, @NotNull Set<String> ignorableStackPackageSet) {
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

}
