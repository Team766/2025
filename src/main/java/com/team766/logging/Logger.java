package com.team766.logging;

import com.google.errorprone.annotations.FormatMethod;
import com.team766.config.ConfigFileReader;
import com.team766.library.CircularBuffer;
import com.team766.library.ValueProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;

public final class Logger {

    private static class LogUncaughtException implements Thread.UncaughtExceptionHandler {
        @SuppressWarnings("CatchAndPrintStackTrace")
        public void uncaughtException(final Thread t, final Throwable e) {
            e.printStackTrace();

            LoggerExceptionUtils.logException(e);

            System.exit(1);
        }
    }

    private static final int MAX_NUM_RECENT_ENTRIES = 100;
    private static final String LOG_FILE_PATH_KEY = "logFilePath";
    private static final String ADVANTAGE_KIT_KEY = "LogMessages";

    private static EnumMap<Category, Logger> m_loggers =
            new EnumMap<Category, Logger>(Category.class);
    private CircularBuffer<LogEntry> m_recentEntries =
            new CircularBuffer<LogEntry>(MAX_NUM_RECENT_ENTRIES);

    static {
        for (Category category : Category.values()) {
            m_loggers.put(category, new Logger(category));
        }

        Thread.setDefaultUncaughtExceptionHandler(new LogUncaughtException());
    }

    public static ValueProvider<String> getLogDirFromConfig() {
        return ConfigFileReader.getInstance().getString(LOG_FILE_PATH_KEY);
    }

    public static Logger get(final Category category) {
        return m_loggers.get(category);
    }

    private final Category m_category;

    private Logger(final Category category) {
        m_category = category;
    }

    public Collection<LogEntry> recentEntries() {
        return Collections.unmodifiableCollection(m_recentEntries);
    }

    @FormatMethod
    public void logData(final Severity severity, final String format, final Object... args) {
        logRaw(severity, String.format(format, args));
    }

    public void logRaw(final Severity severity, final String message) {
        var entry = new LogEntry(new Date(), severity, m_category, message);
        m_recentEntries.add(entry);
        org.littletonrobotics.junction.Logger.recordOutput(ADVANTAGE_KIT_KEY, message);
    }
}
