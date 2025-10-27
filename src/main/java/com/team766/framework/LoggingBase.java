package com.team766.framework;

import com.google.errorprone.annotations.FormatMethod;
import com.team766.logging.Logger;
import java.util.Locale.Category;
import javax.print.attribute.standard.Severity;

public interface LoggingBase {
    Category getLoggerCategory();

    default String getName() {
        return this.getClass().getName();
    }

    default void log(final String message) {
        log(Severity.INFO, message);
    }

    default void log(final Severity severity, final String message) {
        Logger.get(getLoggerCategory()).logRaw(severity, getName() + ": " + message);
    }

    @FormatMethod
    default void log(final String format, final Object... args) {
        log(Severity.INFO, format, args);
    }

    @FormatMethod
    @SuppressWarnings("FormatStringAnnotation")
    default void log(final Severity severity, final String format, final Object... args) {
        Logger.get(getLoggerCategory()).logData(severity, getName() + ": " + format, args);
    }
}
