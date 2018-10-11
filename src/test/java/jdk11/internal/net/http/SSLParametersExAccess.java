package jdk11.internal.net.http;

import java.lang.reflect.Method;
import javax.net.ssl.SSLParameters;

/**
 * Initialize the {@code applicationProtocols} parameter in
 * {@link SSLParameters} when it is known (i.e., on Java 9 or above) and do
 * nothing when it is not known (on Java 8).
 */
public final class SSLParametersExAccess {

    public static SSLParameters init(SSLParameters sslParams, String[] applicationProtocols) {
        if (!IS_JAVA8) {
            setApplicationProtocols(sslParams, applicationProtocols);
        }
        return sslParams;
    }

    public static void setEnableRetransmissions(SSLParameters sslParams, boolean enableRetransmissions) {
        if (!IS_JAVA8) {
            setEnableRetrans(sslParams, enableRetransmissions);
        }
    }

    public static boolean getEnableRetransmissions(SSLParameters sslParams) {
        return true;
    }

    private SSLParametersExAccess() {
        throw new AssertionError();
    }

    private static void setApplicationProtocols(SSLParameters params, String[] protocols) {
        try {
            APP_PROTOCOLS_SET.invoke(params, new Object[] { protocols });
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static void setEnableRetrans(SSLParameters params, boolean enableRetransmissions) {
        try {
            ENABLE_RETRANS_SET.invoke(params, enableRetransmissions);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static final boolean IS_JAVA8 = isJava8();
    private static final Method APP_PROTOCOLS_SET;
    private static final Method ENABLE_RETRANS_SET;
    static {
        try {
            if (!isJava8()) {
                // assume it's Java 9 or higher
                APP_PROTOCOLS_SET = SSLParameters.class.getDeclaredMethod("setApplicationProtocols", String[].class);
                APP_PROTOCOLS_SET.setAccessible(true);
                ENABLE_RETRANS_SET = SSLParameters.class.getDeclaredMethod("setEnableRetransmissions", boolean.class);
                ENABLE_RETRANS_SET.setAccessible(true);
            } else {
                // Java 8
                APP_PROTOCOLS_SET = null;
                ENABLE_RETRANS_SET = null;
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static boolean isJava8() {
        try {
            return "52.0".equals(System.getProperty("java.class.version"));
        } catch (Exception ignore) {
        }
        return false;
    }
}
