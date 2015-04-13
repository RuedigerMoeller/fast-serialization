/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nustaq.serialization.util;

import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 29.11.12
 * Time: 20:38
 * To change this template use File | Settings | File Templates.
 */
public class FSTUtil {

    static int[] EmptyIntArray = new int[10000];
    static Object[] EmptyObjArray = new Object[10000];
    static ObjectStreamField[] NO_FIELDS = new ObjectStreamField[0];
    public static Unsafe unFlaggedUnsafe = FSTUtil.getUnsafe(); // even if unsafe is disabled, use it for memoffset computation

    static {
        if (unFlaggedUnsafe != null) {
            refoff = unFlaggedUnsafe.arrayBaseOffset(Object[].class);
            bufoff = unFlaggedUnsafe.arrayBaseOffset(byte[].class);
            intoff = unFlaggedUnsafe.arrayBaseOffset(int[].class);
            longoff = unFlaggedUnsafe.arrayBaseOffset(long[].class);
            longscal = unFlaggedUnsafe.arrayIndexScale(long[].class);
            intscal = unFlaggedUnsafe.arrayIndexScale(int[].class);
            chscal = unFlaggedUnsafe.arrayIndexScale(char[].class);
            refscal = unFlaggedUnsafe.arrayIndexScale(Object[].class);
            choff = unFlaggedUnsafe.arrayBaseOffset(char[].class);
            doubleoff = unFlaggedUnsafe.arrayBaseOffset(double[].class);
            doublescal = unFlaggedUnsafe.arrayIndexScale(double[].class);
            floatoff = unFlaggedUnsafe.arrayBaseOffset(float[].class);
            floatscal = unFlaggedUnsafe.arrayIndexScale(float[].class);
        } else {
            refscal = 0;
            refoff = 0;
            longoff = 0;
            longscal = 0;
            bufoff = 0;
            intoff = 0;
            intscal = 0;
            choff = 0;
            chscal = 0;
            doublescal = 0;
            doubleoff = 0;
            floatscal = 0;
            floatoff = 0;
        }
    }

    public final static long refoff;
    public final static long refscal;
    public final static long bufoff;
    public final static long choff;
    public final static long intoff;
    public final static long longoff;
    public final static long doubleoff;
    public final static long floatoff;
    public final static long intscal;
    public final static long longscal;
    public final static long chscal;
    public final static long floatscal;
    public final static long doublescal;

    public static void clear(int[] arr) {
        int count = 0;
        final int length = EmptyIntArray.length;
        while (arr.length - count > length) {
            System.arraycopy(EmptyIntArray, 0, arr, count, length);
            count += length;
        }
        System.arraycopy(EmptyIntArray, 0, arr, count, arr.length - count);
    }

    public static void clear(Object[] arr) {
        final int arrlen = arr.length;
        clear(arr, arrlen);
    }

    public static void clear(Object[] arr, int arrlen) {
        int count = 0;
        final int length = EmptyObjArray.length;
        while (arrlen - count > length) {
            System.arraycopy(EmptyObjArray, 0, arr, count, length);
            count += length;
        }
        System.arraycopy(EmptyObjArray, 0, arr, count, arrlen - count);
    }

    public static String toString(Throwable th) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        return th.getClass().getSimpleName() + ":" + th.getMessage() + "\n" + sw.toString();
    }

    public static RuntimeException rethrow(Throwable ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        }
        return new RuntimeException(ex);
    }

    public static String getPackage(Class clazz) {
        String s = clazz.getName();
        int i = s.lastIndexOf('[');
        if (i >= 0) {
            s = s.substring(i + 2);
        }
        i = s.lastIndexOf('.');
        if (i >= 0) {
            return s.substring(0, i);
        }
        return "";
    }

    public static boolean isPackEq(Class clazz1, Class clazz2) {
        return getPackage(clazz1).equals(getPackage(clazz2));
    }

    public static Method findPrivateMethod(Class clazz, String methName,
                                              Class[] clazzArgs,
                                              Class retClazz) {
        try {
            Method m = clazz.getDeclaredMethod(methName, clazzArgs);
            int modif = m.getModifiers();
            if ((m.getReturnType() == retClazz) && ((modif & Modifier.PRIVATE) != 0) && ((modif & Modifier.STATIC) == 0)) {
                m.setAccessible(true);
                return m;
            }
            return null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static Method findDerivedMethod(Class clazz, String metnam,
                                              Class[] argClzz,
                                              Class retClz) {
        Method m = null;
        Class defCl = clazz;
        while (defCl != null) {
            try {
                m = defCl.getDeclaredMethod(metnam, argClzz);
                break;
            } catch (NoSuchMethodException ex) {
                defCl = defCl.getSuperclass();
            }
        }
        if (m == null) {
            return null;
        }
        if (m.getReturnType() != retClz) {
            return null;
        }
        int mods = m.getModifiers();
        if ((mods & (Modifier.STATIC | Modifier.ABSTRACT)) != 0) {
            return null;
        } else if ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
            m.setAccessible(true);
            return m;
        } else if ((mods & Modifier.PRIVATE) != 0) {
            m.setAccessible(true);
            if (clazz == defCl) {
                return m;
            }
            return null;
        } else {
            m.setAccessible(true);
            if (isPackEq(clazz, defCl)) {
                return m;
            }
            return null;
        }
    }

    public static void printEx(Throwable e) {
        while (e.getCause() != null && e.getCause() != e) {
            e = e.getCause();
        }
        e.printStackTrace();
    }

    public static boolean isPrimitiveArray(Class c) {
        Class componentType = c.getComponentType();
        if (componentType == null) {
            return c.isPrimitive();
        }
        return isPrimitiveArray(c.getComponentType());
    }

    public static Unsafe getUnsafe() {
        try {
            if (unFlaggedUnsafe != null)
                return unFlaggedUnsafe;
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) { /* ... */ }
        return null;
    }

    public static int writeSignedVarInt(int value, byte out[], int index) {
        return writeUnsignedVarInt((value << 1) ^ (value >> 31), out, index);
    }

    public static int writeUnsignedVarInt(int value, byte[] out, int index) {
        while ((value & 0xFFFFFF80) != 0L) {
            out[index++] = (byte) ((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out[index++] = (byte) (value & 0x7F);
        return index;
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }
        return fields;
    }

    public static byte[] readAll(InputStream is)
        throws Exception {
        int pos = 0;
        byte[] buffer = new byte[1024];
        while (true) {
            int toRead;
            if (pos >= buffer.length) {
                toRead = buffer.length * 2;
                if (buffer.length < pos + toRead) {
                    buffer = Arrays.copyOf(buffer, pos + toRead);
                }
            } else {
                toRead = buffer.length - pos;
            }
            int byt = is.read(buffer, pos, toRead);
            if (byt < 0) {
                if (pos != buffer.length) {
                    buffer = Arrays.copyOf(buffer, pos);
                }
                break;
            }
            pos += byt;
        }
        return buffer;
    }

    public static void main( String arg[] ) {

        int array[] = new int[30_000];
        for ( int i = 0; i < 20; i++ ) {
            long tim = System.currentTimeMillis();
            testOrdinaryFill(array);
            System.out.println("tim ordinary:"+(System.currentTimeMillis()-tim));
        }

        for ( int i = 0; i < 20; i++ ) {
            long tim = System.currentTimeMillis();
            testCopyFill(array);
            System.out.println("tim:"+(System.currentTimeMillis()-tim));
        }

        Object oarray[] = new Object[30_000];
        for ( int i = 0; i < 20; i++ ) {
            long tim = System.currentTimeMillis();
            testOrdinaryFillO(oarray);
            System.out.println("tim Object ordinary:"+(System.currentTimeMillis()-tim));
        }

        for ( int i = 0; i < 20; i++ ) {
            long tim = System.currentTimeMillis();
            testCopyFillO(oarray);
            System.out.println("tim Object:"+(System.currentTimeMillis()-tim));
        }
    }

    private static void testCopyFill(int[] array) {
        for ( int i = 0; i < 100000; i++ ) {
            clear(array);
        }
    }

    private static void testOrdinaryFill(int[] array) {
        for ( int i = 0; i < 100000; i++ ) {
            Arrays.fill(array,0);
        }
    }

    private static void testCopyFillO(Object[] array) {
        for ( int i = 0; i < 100000; i++ ) {
            clear(array);
        }
    }

    private static void testOrdinaryFillO(Object[] array) {
        for ( int i = 0; i < 100000; i++ ) {
            Arrays.fill(array,0);
        }
    }

}
