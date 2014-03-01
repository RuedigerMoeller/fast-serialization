/*
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 */
package de.ruedigermoeller.serialization.util;

import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.io.IOException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.*;

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
    public static Unsafe unsafe;
    public static Unsafe unFlaggedUnsafe = FSTUtil.getUnsafe(); // even if unsafe is disabled, use it for memoffset computation

    static {
        if ( System.getProperty("fst.unsafe","false").equals("true") ) {
            FSTUtil.unsafe = unFlaggedUnsafe;
        }
        if ( unFlaggedUnsafe != null ) {
            bufoff = unFlaggedUnsafe.arrayBaseOffset(byte[].class);
            intoff = unFlaggedUnsafe.arrayBaseOffset(int[].class);
            longoff = unFlaggedUnsafe.arrayBaseOffset(long[].class);
            longscal = unFlaggedUnsafe.arrayIndexScale(long[].class);
            intscal = unFlaggedUnsafe.arrayIndexScale(int[].class);
            chscal = unFlaggedUnsafe.arrayIndexScale(char[].class);
            choff = unFlaggedUnsafe.arrayBaseOffset(char[].class);
            doubleoff = unFlaggedUnsafe.arrayBaseOffset(double[].class);
            doublescal = unFlaggedUnsafe.arrayIndexScale(double[].class);
            floatoff = unFlaggedUnsafe.arrayBaseOffset(float[].class);
            floatscal = unFlaggedUnsafe.arrayIndexScale(float[].class);
        } else {
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
        while( arr.length - count > length) {
            System.arraycopy(EmptyIntArray,0,arr,count, length);
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
        while( arrlen - count > length) {
            System.arraycopy(EmptyObjArray,0,arr,count, length);
            count += length;
        }
        System.arraycopy(EmptyObjArray,0,arr,count, arrlen -count);
    }

    public static RuntimeException rethrow( Throwable ex ) {
        if ( ex instanceof RuntimeException ) {
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

    public static Constructor findConstructorForExternalize(Class clazz) {
        try {
            Constructor c = clazz.getDeclaredConstructor((Class[]) null);
            c.setAccessible(true);
            if ((c.getModifiers() & Modifier.PUBLIC) != 0) {
                return c;
            } else {
                return null;
            }
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static Constructor findConstructorForSerializable(Class clazz) {
        Class curCl = clazz;
        while (Serializable.class.isAssignableFrom(curCl)) {
            if ((curCl = curCl.getSuperclass()) == null) {
                return null;
            }
        }
        try {
            Constructor c = curCl.getDeclaredConstructor((Class[]) null);
            int mods = c.getModifiers();
            if ((mods & Modifier.PRIVATE) != 0 ||
                    ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 &&
                            !isPackEq(clazz, curCl)))
            {
                return null;
            }
            c = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, c);
            c.setAccessible(true);
            return c;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    static boolean isPackEq(Class clazz1, Class clazz2) {
        return getPackage(clazz1).equals(getPackage(clazz2));
    }

    public static Method findPrivateMethod(Class clazz, String methName,
                                           Class[] clazzArgs,
                                           Class retClazz)
    {
        try {
            Method m = clazz.getDeclaredMethod(methName, clazzArgs);
            int modif = m.getModifiers();
            if ((m.getReturnType() == retClazz) && ((modif & Modifier.PRIVATE) != 0) && ((modif & Modifier.STATIC) == 0))
            {
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
                                           Class retClz)
    {
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
            if ( isPackEq(clazz, defCl) ) {
                return m;
            }
            return null;
        }
    }

    public static void printEx(Throwable e) {
        while( e.getCause() != null && e.getCause() != e ) {
            e = e.getCause();
        }
        e.printStackTrace();
    }

    public static boolean isPrimitiveArray(Class c) {
        Class componentType = c.getComponentType();
        if ( componentType == null ) {
            return c.isPrimitive();
        }
        return isPrimitiveArray(c.getComponentType());
    }

    public static Unsafe getUnsafe() {
        try {
            if ( unFlaggedUnsafe !=null )
                return unFlaggedUnsafe;
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe)f.get(null);
        } catch (Exception e) { /* ... */ }
        return null;
    }

    public static int writeSignedVarInt(int value, byte out[], int index) {
        return writeUnsignedVarInt((value << 1) ^ (value >> 31), out, index);
    }

    public static int writeUnsignedVarInt(int value, byte[] out, int index) {
        while ((value & 0xFFFFFF80) != 0L) {
            out[index++]= (byte) ((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out[index++]= (byte) (value & 0x7F);
        return index;
    }
}
