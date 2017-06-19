package org.nustaq.logging;

public final class FSTLogger {

    private static Log binding;

    /**
     * Initialize FST logging with a {@link Log} binding.
     * @param log
     */
    public static void setBinding(Log log) {
        binding = log;
    }

    public static FSTLogger getLogger(Class<?> clazz) {
        return new FSTLogger(clazz.getName());
    }

    private final String loggerName;
    private FSTLogger(String loggerName) {
        this.loggerName = loggerName;
    }

    public void log(Level level, String message, /* nullable */ Throwable ex) {
        if (binding != null) {
            binding.log(loggerName, level, message, ex);
        }
    }

    public enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    public interface Log {

        void log(String name, Level level, String message, /* nullable */ Throwable ex);
    }
}
