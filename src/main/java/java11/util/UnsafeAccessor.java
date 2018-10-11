package java11.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author chuxiaofeng
 */
@SuppressWarnings("WeakerAccess")
public class UnsafeAccessor {

    public static final Unsafe UNSAFE;

    static {
        UNSAFE = getUnsafe();
    }

    static Unsafe getUnsafe() {
        // Field field = Unsafe.class.getDeclaredField("theUnsafe");
        // field.setAccessible(true);
        // UNSAFE = (Unsafe) field.get(null);
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException ignored) { }

        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
                Class<Unsafe> k = Unsafe.class;
                for (Field f : k.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object x = f.get(null);
                    if (k.isInstance(x)) {
                        return k.cast(x);
                    }
                }
                throw new NoSuchFieldError("the Unsafe");
            });
        } catch (PrivilegedActionException e) {
            throw new Error("Could not initialize intrinsics", e.getCause());
        }
    }
}