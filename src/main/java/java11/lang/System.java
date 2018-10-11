package java11.lang;

import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;

public class System {

    /**
     * Returns an instance of {@link Logger Logger} for the caller's
     * use.
     *
     * @implSpec
     * Instances returned by this method route messages to loggers
     * obtained by calling { @link LoggerFinder#getLogger(java.lang.String,
     * java11.lang.Module) LoggerFinder.getLogger(name, module)}, where
     * {@code module} is the caller's module.
     * In cases where {@code System.getLogger} is called from a context where
     * there is no caller frame on the stack (e.g when called directly
     * from a JNI attached thread), {@code IllegalCallerException} is thrown.
     * To obtain a logger in such a context, use an auxiliary class that will
     * implicitly be identified as the caller, or use the system { @link
     * LoggerFinder#getLoggerFinder() LoggerFinder} to obtain a logger instead.
     * Note that doing the latter may eagerly initialize the underlying
     * logging system.
     *
     * @apiNote
     * This method may defer calling the { @link
     * LoggerFinder#getLogger(java.lang.String, java11.lang.Module)
     * LoggerFinder.getLogger} method to create an actual logger supplied by
     * the logging backend, for instance, to allow loggers to be obtained during
     * the system initialization time.
     *
     * @param name the name of the logger.
     * @return an instance of {@link Logger} that can be used by the calling
     *         class.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws IllegalCallerException if there is no Java caller frame on the
     *         stack.
     *
     * @since 9
     */
    // @CallerSensitive 忽略权限检查漏洞
    public static Logger getLogger(String name) {
        Objects.requireNonNull(name);
//        final Class<?> caller = Reflection.getCallerClass();
//        if (caller == null) {
//            throw new IllegalCallerException("no caller frame");
//        }

        return SimpleConsoleLogger.makeSimpleLogger("simpleConsoleLogger");
    }


    /**
     * {@code System.Logger} instances log messages that will be
     * routed to the underlying logging framework the { @link System.LoggerFinder
     * LoggerFinder} uses.
     *
     * {@code System.Logger} instances are typically obtained from
     * the {@link java.lang.System System} class, by calling
     * {@link java11.lang.System#getLogger(java.lang.String) System.getLogger(loggerName)}
     * or { @link java11.lang.System#getLogger(java.lang.String, java.util.ResourceBundle)
     * System.getLogger(loggerName, bundle)}.
     *
     * @see java11.lang.System#getLogger(java.lang.String)
     * @ see java11.lang.System#getLogger(java.lang.String, java.util.ResourceBundle)
     * @ see java11.lang.System.LoggerFinder
     *
     * @since 9
     */
    public interface Logger {

        /**
         * System {@linkplain Logger loggers} levels.
         *
         * A level has a {@linkplain #getName() name} and {@linkplain
         * #getSeverity() severity}.
         * Level values are {@link #ALL}, {@link #TRACE}, {@link #DEBUG},
         * {@link #INFO}, {@link #WARNING}, {@link #ERROR}, {@link #OFF},
         * by order of increasing severity.
         * <br>
         * {@link #ALL} and {@link #OFF}
         * are simple markers with severities mapped respectively to
         * {@link java.lang.Integer#MIN_VALUE Integer.MIN_VALUE} and
         * {@link java.lang.Integer#MAX_VALUE Integer.MAX_VALUE}.
         * <p>
         * <b>Severity values and Mapping to {@code java.util.logging.Level}.</b>
         * <p>
         * {@linkplain System.Logger.Level System logger levels} are mapped to
         * {@linkplain java.util.logging.Level  java.util.logging levels}
         * of corresponding severity.
         * <br>The mapping is as follows:
         * <br><br>
         * <table class="striped">
         * <caption>System.Logger Severity Level Mapping</caption>
         * <thead>
         * <tr><th scope="col">System.Logger Levels</th>
         *     <th scope="col">java.util.logging Levels</th>
         * </thead>
         * <tbody>
         * <tr><th scope="row">{@link Logger.Level#ALL ALL}</th>
         *     <td>{@link java.util.logging.Level#ALL ALL}</td>
         * <tr><th scope="row">{@link Logger.Level#TRACE TRACE}</th>
         *     <td>{@link java.util.logging.Level#FINER FINER}</td>
         * <tr><th scope="row">{@link Logger.Level#DEBUG DEBUG}</th>
         *     <td>{@link java.util.logging.Level#FINE FINE}</td>
         * <tr><th scope="row">{@link Logger.Level#INFO INFO}</th>
         *     <td>{@link java.util.logging.Level#INFO INFO}</td>
         * <tr><th scope="row">{@link Logger.Level#WARNING WARNING}</th>
         *     <td>{@link java.util.logging.Level#WARNING WARNING}</td>
         * <tr><th scope="row">{@link Logger.Level#ERROR ERROR}</th>
         *     <td>{@link java.util.logging.Level#SEVERE SEVERE}</td>
         * <tr><th scope="row">{@link Logger.Level#OFF OFF}</th>
         *     <td>{@link java.util.logging.Level#OFF OFF}</td>
         * </tbody>
         * </table>
         *
         * @since 9
         *
         * @ see java11.lang.System.LoggerFinder
         * @see java11.lang.System.Logger
         */
        enum Level {

            // for convenience, we're reusing java.util.logging.Level int values
            // the mapping logic in sun.util.logging.PlatformLogger depends
            // on this.
            /**
             * A marker to indicate that all levels are enabled.
             * This level {@linkplain #getSeverity() severity} is
             * {@link Integer#MIN_VALUE}.
             */
            ALL(Integer.MIN_VALUE),  // typically mapped to/from j.u.l.Level.ALL
            /**
             * {@code TRACE} level: usually used to log diagnostic information.
             * This level {@linkplain #getSeverity() severity} is
             * {@code 400}.
             */
            TRACE(400),   // typically mapped to/from j.u.l.Level.FINER
            /**
             * {@code DEBUG} level: usually used to log debug information traces.
             * This level {@linkplain #getSeverity() severity} is
             * {@code 500}.
             */
            DEBUG(500),   // typically mapped to/from j.u.l.Level.FINEST/FINE/CONFIG
            /**
             * {@code INFO} level: usually used to log information messages.
             * This level {@linkplain #getSeverity() severity} is
             * {@code 800}.
             */
            INFO(800),    // typically mapped to/from j.u.l.Level.INFO
            /**
             * {@code WARNING} level: usually used to log warning messages.
             * This level {@linkplain #getSeverity() severity} is
             * {@code 900}.
             */
            WARNING(900), // typically mapped to/from j.u.l.Level.WARNING
            /**
             * {@code ERROR} level: usually used to log error messages.
             * This level {@linkplain #getSeverity() severity} is
             * {@code 1000}.
             */
            ERROR(1000),  // typically mapped to/from j.u.l.Level.SEVERE
            /**
             * A marker to indicate that all levels are disabled.
             * This level {@linkplain #getSeverity() severity} is
             * {@link Integer#MAX_VALUE}.
             */
            OFF(Integer.MAX_VALUE);  // typically mapped to/from j.u.l.Level.OFF

            private final int severity;

            Level(int severity) {
                this.severity = severity;
            }

            /**
             * Returns the name of this level.
             * @return this level {@linkplain #name()}.
             */
            public final String getName() {
                return name();
            }

            /**
             * Returns the severity of this level.
             * A higher severity means a more severe condition.
             * @return this level severity.
             */
            public final int getSeverity() {
                return severity;
            }
        }

        /**
         * Returns the name of this logger.
         *
         * @return the logger name.
         */
        String getName();

        /**
         * Checks if a message of the given level would be logged by
         * this logger.
         *
         * @param level the log message level.
         * @return {@code true} if the given log message level is currently
         *         being logged.
         *
         * @throws NullPointerException if {@code level} is {@code null}.
         */
        boolean isLoggable(Level level);

        /**
         * Logs a message.
         *
         * @implSpec The default implementation for this method calls
         * {@code this.log(level, (ResourceBundle)null, msg, (Object[])null);}
         *
         * @param level the log message level.
         * @param msg the string message (or a key in the message catalog, if
         * this logger is a { @link
         * LoggerFinder#getLocalizedLogger(java.lang.String,
         * java.util.ResourceBundle, java11.lang.Module) localized logger});
         * can be {@code null}.
         *
         * @throws NullPointerException if {@code level} is {@code null}.
         */
        default void log(Level level, String msg) {
            log(level, null, msg, (Object[]) null);
        }

        /**
         * Logs a lazily supplied message.
         *
         * If the logger is currently enabled for the given log message level
         * then a message is logged that is the result produced by the
         * given supplier function.  Otherwise, the supplier is not operated on.
         *
         * @implSpec When logging is enabled for the given level, the default
         * implementation for this method calls
         * {@code this.log(level, (ResourceBundle)null, msgSupplier.get(), (Object[])null);}
         *
         * @param level the log message level.
         * @param msgSupplier a supplier function that produces a message.
         *
         * @throws NullPointerException if {@code level} is {@code null},
         *         or {@code msgSupplier} is {@code null}.
         */
        default void log(Level level, Supplier<String> msgSupplier) {
            Objects.requireNonNull(msgSupplier);
            if (isLoggable(Objects.requireNonNull(level))) {
                log(level, null, msgSupplier.get(), (Object[]) null);
            }
        }

        /**
         * Logs a message produced from the given object.
         *
         * If the logger is currently enabled for the given log message level then
         * a message is logged that, by default, is the result produced from
         * calling  toString on the given object.
         * Otherwise, the object is not operated on.
         *
         * @implSpec When logging is enabled for the given level, the default
         * implementation for this method calls
         * {@code this.log(level, (ResourceBundle)null, obj.toString(), (Object[])null);}
         *
         * @param level the log message level.
         * @param obj the object to log.
         *
         * @throws NullPointerException if {@code level} is {@code null}, or
         *         {@code obj} is {@code null}.
         */
        default void log(Level level, Object obj) {
            Objects.requireNonNull(obj);
            if (isLoggable(Objects.requireNonNull(level))) {
                this.log(level, null, obj.toString(), (Object[]) null);
            }
        }

        /**
         * Logs a message associated with a given throwable.
         *
         * @implSpec The default implementation for this method calls
         * {@code this.log(level, (ResourceBundle)null, msg, thrown);}
         *
         * @param level the log message level.
         * @param msg the string message (or a key in the message catalog, if
         * this logger is a { @link
         * LoggerFinder#getLocalizedLogger(java.lang.String,
         * java.util.ResourceBundle, java11.lang.Module) localized logger});
         * can be {@code null}.
         * @param thrown a {@code Throwable} associated with the log message;
         *        can be {@code null}.
         *
         * @throws NullPointerException if {@code level} is {@code null}.
         */
        default void log(Level level, String msg, Throwable thrown) {
            this.log(level, null, msg, thrown);
        }

        /**
         * Logs a lazily supplied message associated with a given throwable.
         *
         * If the logger is currently enabled for the given log message level
         * then a message is logged that is the result produced by the
         * given supplier function.  Otherwise, the supplier is not operated on.
         *
         * @implSpec When logging is enabled for the given level, the default
         * implementation for this method calls
         * {@code this.log(level, (ResourceBundle)null, msgSupplier.get(), thrown);}
         *
         * @param level one of the log message level identifiers.
         * @param msgSupplier a supplier function that produces a message.
         * @param thrown a {@code Throwable} associated with log message;
         *               can be {@code null}.
         *
         * @throws NullPointerException if {@code level} is {@code null}, or
         *                               {@code msgSupplier} is {@code null}.
         */
        default void log(Level level, Supplier<String> msgSupplier,
                         Throwable thrown) {
            Objects.requireNonNull(msgSupplier);
            if (isLoggable(Objects.requireNonNull(level))) {
                this.log(level, null, msgSupplier.get(), thrown);
            }
        }

        /**
         * Logs a message with an optional list of parameters.
         *
         * @implSpec The default implementation for this method calls
         * {@code this.log(level, (ResourceBundle)null, format, params);}
         *
         * @param level one of the log message level identifiers.
         * @param format the string message format in {@link
         * java.text.MessageFormat} format, (or a key in the message
         * catalog, if this logger is a { @link
         * LoggerFinder#getLocalizedLogger(java.lang.String,
         * java.util.ResourceBundle, java11.lang.Module) localized logger});
         * can be {@code null}.
         * @param params an optional list of parameters to the message (may be
         * none).
         *
         * @throws NullPointerException if {@code level} is {@code null}.
         */
        default void log(Level level, String format, Object... params) {
            this.log(level, null, format, params);
        }

        /**
         * Logs a localized message associated with a given throwable.
         *
         * If the given resource bundle is non-{@code null},  the {@code msg}
         * string is localized using the given resource bundle.
         * Otherwise the {@code msg} string is not localized.
         *
         * @param level the log message level.
         * @param bundle a resource bundle to localize {@code msg}; can be
         * {@code null}.
         * @param msg the string message (or a key in the message catalog,
         *            if {@code bundle} is not {@code null}); can be {@code null}.
         * @param thrown a {@code Throwable} associated with the log message;
         *        can be {@code null}.
         *
         * @throws NullPointerException if {@code level} is {@code null}.
         */
        void log(Level level, ResourceBundle bundle, String msg,
                 Throwable thrown);

        /**
         * Logs a message with resource bundle and an optional list of
         * parameters.
         *
         * If the given resource bundle is non-{@code null},  the {@code format}
         * string is localized using the given resource bundle.
         * Otherwise the {@code format} string is not localized.
         *
         * @param level the log message level.
         * @param bundle a resource bundle to localize {@code format}; can be
         * {@code null}.
         * @param format the string message format in {@link
         * java.text.MessageFormat} format, (or a key in the message
         * catalog if {@code bundle} is not {@code null}); can be {@code null}.
         * @param params an optional list of parameters to the message (may be
         * none).
         *
         * @throws NullPointerException if {@code level} is {@code null}.
         */
        void log(Level level, ResourceBundle bundle, String format,
                 Object... params);
    }

    /**
     * @author chuxiaofeng
     */
    @SuppressWarnings("WeakerAccess")
    public static class SimpleConsoleLogger implements Logger {

        static final Level DEFAULT_LEVEL = getDefaultLevel();
        static Level getDefaultLevel() {
            String levelName = java.lang.System.getProperty("jdk.system.logger.level", "INFO");
            try {
                return Level.valueOf(levelName);
            } catch (IllegalArgumentException iae) {
                return Level.INFO;
            }
        }

        final String name;
        SimpleConsoleLogger(String name) {
            this.name = name;
        }

        String getSimpleFormatString() {
            return SimpleConsoleLogger.Formatting.SIMPLE_CONSOLE_LOGGER_FORMAT;
        }

        @Override
        public final String getName() {
            return name;
        }

        private Enum<?> logLevel(Level level) {
            return level;
        }

        // ---------------------------------------------------
        //                 From Logger
        // ---------------------------------------------------

        @Override
        public final boolean isLoggable(Level level) {
            return level != Level.OFF && level.ordinal() >= DEFAULT_LEVEL.ordinal();
        }

        @Override
        public final void log(Level level, ResourceBundle bundle, String key, Throwable thrown) {
            if (isLoggable(level)) {
                if (bundle != null) {
                    key = getString(bundle, key);
                }
                publish(getCallerInfo(), logLevel(level), key, thrown);
            }
        }

        @Override
        public final void log(Level level, ResourceBundle bundle, String format, Object... params) {
            if (isLoggable(level)) {
                if (bundle != null) {
                    format = getString(bundle, format);
                }
                publish(getCallerInfo(), logLevel(level), format, params);
            }
        }

        /**
         * Default platform logging support - output messages to System.err -
         * equivalent to ConsoleHandler with SimpleFormatter.
         */
        static PrintStream outputStream() {
            return java.lang.System.err;
        }

        // Returns the caller's class and method's name; best effort
        // if cannot infer, return the logger's name.
        private String getCallerInfo() {
// 这里可以将 statck walker 替换为 new Throwable().getStackTrace(),
// 但是前者是惰性的，不用栈回溯，后者开销比较大，综上，callerInfo 功能废弃
//        StackTraceElement[] bt = new Throwable().getStackTrace();
//        if (frame.isPresent()) {
//            return frame.get().getClassName() + " " + frame.get().getMethodName();
//        } else {
            return name;
//        }
        }

        private String toString(Throwable thrown) {
            String throwable = "";
            if (thrown != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                thrown.printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return throwable;
        }

        private synchronized String format(Enum<?> level,
                                           String msg, Throwable thrown, String callerInfo) {

            ZonedDateTime zdt = ZonedDateTime.now();
            String throwable = toString(thrown);

            return String.format(getSimpleFormatString(),
                    zdt,
                    callerInfo,
                    name,
                    level.name(),
                    msg,
                    throwable);
        }

        // publish accepts both PlatformLogger Levels and LoggerFinder Levels.
        private void publish(String callerInfo, Enum<?> level, String msg) {
            outputStream().print(format(level, msg, null, callerInfo));
        }
        // publish accepts both PlatformLogger Levels and LoggerFinder Levels.
        private void publish(String callerInfo, Enum<?> level, String msg, Throwable thrown) {
            outputStream().print(format(level, msg, thrown, callerInfo));
        }
        // publish accepts both PlatformLogger Levels and LoggerFinder Levels.
        private void publish(String callerInfo, Enum<?> level, String msg, Object... params) {
            msg = params == null || params.length == 0 ? msg
                    : SimpleConsoleLogger.Formatting.formatMessage(msg, params);
            outputStream().print(format(level, msg, null, callerInfo));
        }

        public static SimpleConsoleLogger makeSimpleLogger(String name) {
            return new SimpleConsoleLogger(name);
        }

        static String getString(ResourceBundle bundle, String key) {
            if (bundle == null || key == null) return key;
            try {
                return bundle.getString(key);
            } catch (MissingResourceException x) {
                // Emulate what java.util.logging Formatters do
                // We don't want unchecked exception to propagate up to
                // the caller's code.
                return key;
            }
        }

        static final class Formatting {
            // The default simple log format string.
            // Used both by SimpleConsoleLogger when java.logging is not present,
            // and by SurrogateLogger and java.util.logging.SimpleFormatter when
            // java.logging is present.
            static final String DEFAULT_FORMAT =
                    "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";

            // The system property key that allows to change the default log format
            // when java.logging is not present. This is used to control the formatting
            // of the SimpleConsoleLogger.
            static final String DEFAULT_FORMAT_PROP_KEY =
                    "jdk.system.logger.format";

            // The system property key that allows to change the default log format
            // when java.logging is present. This is used to control the formatting
            // of the SurrogateLogger (used before java.util.logging.LogManager is
            // initialized) and the java.util.logging.SimpleFormatter (used after
            // java.util.logging.LogManager is  initialized).
            static final String JUL_FORMAT_PROP_KEY =
                    "java.util.logging.SimpleFormatter.format";

            // The simple console logger format string
            static final String SIMPLE_CONSOLE_LOGGER_FORMAT =
                    getSimpleFormat(DEFAULT_FORMAT_PROP_KEY, null);

            static String getSimpleFormat(String key, Function<String, String> defaultPropertyGetter) {
                // Double check that 'key' is one of the expected property names:
                // - DEFAULT_FORMAT_PROP_KEY is used to control the
                //   SimpleConsoleLogger format when java.logging is
                //   not present.
                // - JUL_FORMAT_PROP_KEY is used when this method is called
                //   from the SurrogateLogger subclass. It is used to control the
                //   SurrogateLogger format and java.util.logging.SimpleFormatter
                //   format when java.logging is present.
                // This method should not be called with any other key.
                if (!DEFAULT_FORMAT_PROP_KEY.equals(key)
                        && !JUL_FORMAT_PROP_KEY.equals(key)) {
                    throw new IllegalArgumentException("Invalid property name: " + key);
                }

                // Do not use any lambda in this method. Using a lambda here causes
                //    jdk/test/java/lang/invoke/lambda/LogGeneratedClassesTest.java
                // to fail - because that test has a testcase which somehow references
                // PlatformLogger and counts the number of generated lambda classes.
                // 修改成 System.getProperty
                // String format = GetPropertyAction.privilegedGetProperty(key);
                String format =  java.lang.System.getProperty(key);

                if (format == null && defaultPropertyGetter != null) {
                    format = defaultPropertyGetter.apply(key);
                }
                if (format != null) {
                    try {
                        // validate the user-defined format string
                        String.format(format, ZonedDateTime.now(), "", "", "", "", "");
                    } catch (IllegalArgumentException e) {
                        // illegal syntax; fall back to the default format
                        format = DEFAULT_FORMAT;
                    }
                } else {
                    format = DEFAULT_FORMAT;
                }
                return format;
            }


            // Copied from java.util.logging.Formatter.formatMessage
            static String formatMessage(String format, Object... parameters) {
                // Do the formatting.
                try {
                    if (parameters == null || parameters.length == 0) {
                        // No parameters.  Just return format string.
                        return format;
                    }
                    // Is it a java.text style format?
                    // Ideally we could match with
                    // Pattern.compile("\\{\\d").matcher(format).find())
                    // However the cost is 14% higher, so we cheaply check for
                    //
                    boolean isJavaTestFormat = false;
                    final int len = format.length();
                    for (int i=0; i<len-2; i++) {
                        final char c = format.charAt(i);
                        if (c == '{') {
                            final int d = format.charAt(i+1);
                            if (d >= '0' && d <= '9') {
                                isJavaTestFormat = true;
                                break;
                            }
                        }
                    }
                    if (isJavaTestFormat) {
                        return java.text.MessageFormat.format(format, parameters);
                    }
                    return format;
                } catch (Exception ex) {
                    // Formatting failed: use format string.
                    return format;
                }
            }
        }
    }

}
