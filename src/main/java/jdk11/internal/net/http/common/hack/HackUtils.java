package jdk11.internal.net.http.common.hack;

/**
 * @author chuxiaofeng
 */
public class HackUtils {

    private static final double JAVA_CLASS_VERSION;
    private static final boolean IS_JAVA8;
    private static final boolean AT_LEAST_JAVA9;
    private static final boolean TESTS_PRETEND_JAVA8;

    public static final boolean ASSERTIONSENABLED;

    static {
        boolean enabled = false;
        assert enabled = true;
        ASSERTIONSENABLED = enabled;
        try {
            JAVA_CLASS_VERSION = Double.valueOf(System.getProperty("java.class.version"));
            TESTS_PRETEND_JAVA8 = Boolean.parseBoolean(System.getProperty("test.java.version.pretend8", "false"));
        } catch (Exception e) {
            throw new Error(e);
        }
        IS_JAVA8 = (52.0 == JAVA_CLASS_VERSION);
        AT_LEAST_JAVA9 = (53.0 <= JAVA_CLASS_VERSION);
    }

    private static boolean pretendToBeJava8ForTests() {
        return AT_LEAST_JAVA9 && TESTS_PRETEND_JAVA8;
    }

    public static boolean isJava8() {
        return IS_JAVA8 || pretendToBeJava8ForTests();
    }
}
