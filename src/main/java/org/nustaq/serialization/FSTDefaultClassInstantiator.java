package org.nustaq.serialization;

import org.nustaq.serialization.util.FSTUtil;
import sun.reflect.ReflectionFactory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Created by ruedi on 12.12.14.
 *
 * Valid for common x86 JDK's (not android)
 *
 */
public class FSTDefaultClassInstantiator implements FSTClassInstantiator {

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
            ignored.printStackTrace();
            return null;
        }
    }

    public Constructor findConstructorForExternalize(Class clazz) {
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

    public Constructor findConstructorForSerializable(Class clazz) {
        if (!Serializable.class.isAssignableFrom(clazz)) {
            // in case forceSerializable flag is present, just look for no-arg constructor
            return findConstructorForExternalize(clazz);
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
            return c;
        } catch (NoClassDefFoundError cle) {
            return null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }


}
