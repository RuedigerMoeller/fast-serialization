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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import sun.misc.Unsafe;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 29.11.12
 * Time: 20:38
 * To change this template use File | Settings | File Templates.
 */
public class FSTUtil {

    private static final Object[] EmptyObjArray = new Object[10000];
    public static final Unsafe unFlaggedUnsafe = UnsafeUtil.UNSAFE;//FSTUtil.getUnsafe(); // even if unsafe is disabled, use it for memoffset computation
    //public static UnsafeAndroid unFlaggedUnsafeAndroid = new UnsafeAndroid();

    static void clear(int[] arr) {
        Arrays.fill(arr, 0);
    }

    static void clear(Object[] arr) {
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

    public static <T extends Throwable> void rethrow(Throwable exception) throws T {
        throw (T) exception;
    }

    // obsolete
    private static String getPackage(Class clazz) {
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

    public static Class getRealEnumClass(Class enumClass) {
        if (enumClass.isAnonymousClass()) {
            return enumClass.getSuperclass();
        }
        return enumClass;
    }
}
