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
package org.nustaq.serialization;

import org.nustaq.logging.FSTLogger;
import org.nustaq.serialization.util.FSTUtil;
import sun.reflect.ReflectionFactory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ruedi on 12.12.14.
 *
 * Valid for common x86 JDK's (not android)
 *
 */
public class FSTDefaultClassInstantiator implements FSTClassInstantiator {

    private static final FSTLogger logger = FSTLogger.getLogger(FSTDefaultClassInstantiator.class);

    /**
     * reduce number of generated classes. Can be cleared riskless in case.
     */
    public static ConcurrentHashMap<Class,Constructor> constructorMap = new ConcurrentHashMap<>();

    @Override
    public Object newInstance(Class clazz, Constructor cons, boolean doesRequireInit, boolean unsafeAsLastResort) {
        try {
            if (!doesRequireInit && FSTUtil.unFlaggedUnsafe != null) { // no performance improvement here, keep for nasty constructables ..
                return FSTUtil.unFlaggedUnsafe.allocateInstance(clazz);
            }
            if ( cons == null ) // no suitable constructor found
            {
                if ( unsafeAsLastResort ) {
                    // best effort. use Unsafe to instantiate.
                    // Warning: if class contains transient fields which have default values assigned ('transient int x = 3'),
                    // those will not be assigned after deserialization as unsafe instantiation does not execute any default
                    // construction code.
                    // Define a public no-arg constructor to avoid this behaviour (rarely an issue, but there are cases).
                    if ( FSTUtil.unFlaggedUnsafe != null ) {
                        return FSTUtil.unFlaggedUnsafe.allocateInstance(clazz);
                    }
                    throw new RuntimeException("no suitable constructor found and no Unsafe instance avaiable. Can't instantiate "+ clazz.getName());
                }
            }
            return cons.newInstance();
        } catch (Throwable ignored) {
            logger.log(FSTLogger.Level.INFO, "Failed to construct new instance", ignored);
            return null;
        }
    }

    public Constructor findConstructorForExternalize(Class clazz) {
        try {
            Constructor c = clazz.getDeclaredConstructor((Class[]) null);
            if ( c == null )
                return null;
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

    public Constructor findConstructorForSerializable(final Class clazz) {
        if (!Serializable.class.isAssignableFrom(clazz)) {
            // in case forceSerializable flag is present, just look for no-arg constructor
            return findConstructorForExternalize(clazz);
        }
        if ( FSTClazzInfo.BufferConstructorMeta) {
            Constructor constructor = constructorMap.get(clazz);
            if (constructor != null) {
                return constructor;
            }
        }
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
                         !FSTUtil.isPackEq(clazz, curCl))) {
                return null;
            }
            c = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, c);
            c.setAccessible(true);

            if ( FSTClazzInfo.BufferConstructorMeta)
                constructorMap.put(clazz,c);
            return c;
        } catch (NoClassDefFoundError cle) {
            return null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }


}
