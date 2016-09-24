package org.nustaq.serialization.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.misc.Unsafe;

/**
 * Created by fabianterhorst on 24.09.16.
 */

public final class UnsafeUtil {

    public static final Unsafe UNSAFE;

    public static final long OBJECT_ARRAY_BASE;
    public static final long OBJECT_ARRAY_SHIFT;

    public static final long IDENTIFIER_DATA_OFFSET;

    static {
        Unsafe unsafe;

        try {
            unsafe = findUnsafe();
        } catch (RuntimeException e) {
            unsafe = null;
        }
        if (unsafe == null) {
            throw new RuntimeException("Incompatible JVM - sun.misc.Unsafe support is missing");
        }

        try {
            Field identifierData = Identifier.class.getDeclaredField("data");
            identifierData.setAccessible(true);
            IDENTIFIER_DATA_OFFSET = unsafe.objectFieldOffset(identifierData);

            OBJECT_ARRAY_BASE = unsafe.arrayBaseOffset(Object[].class);
            int indexScale = unsafe.arrayIndexScale(Object[].class);
            OBJECT_ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(indexScale);

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException();
        }

        UNSAFE = unsafe;
    }

    private static Unsafe findUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException se) {
            return AccessController.doPrivileged(new PrivilegedAction<Unsafe>() {
                @Override
                public Unsafe run() {
                    try {
                        Class<Unsafe> type = Unsafe.class;
                        try {
                            Field field = type.getDeclaredField("theUnsafe");
                            field.setAccessible(true);
                            return type.cast(field.get(type));

                        } catch (Exception e) {
                            for (Field field : type.getDeclaredFields()) {
                                if (type.isAssignableFrom(field.getType())) {
                                    field.setAccessible(true);
                                    return type.cast(field.get(type));
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Unsafe unavailable", e);
                    }
                    throw new RuntimeException("Unsafe unavailable");
                }
            });
        }
    }

    private UnsafeUtil() {
    }

}
