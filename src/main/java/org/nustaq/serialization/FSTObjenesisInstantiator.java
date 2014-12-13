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

import org.nustaq.serialization.util.FSTUtil;
import org.objenesis.Objenesis;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Created by ruedi on 12.12.14.
 *
 * Used for Android as does not require sun.* or Unsafe to instantiate classes.
 * Might be slower for some cases (probably executes field init)
 *
 */
public class FSTObjenesisInstantiator implements FSTClassInstantiator {

    ObjectInstantiator objInstantiator;

    public FSTObjenesisInstantiator( Objenesis objenesis, Class clazz ) {
        objInstantiator = objenesis.getInstantiatorOf( clazz );
    }

    static Object[] empty = new Object[0];
    @Override
    public Object newInstance(Class clazz, Constructor cons, boolean doesRequireInit, boolean unsafeAsLastResort) {
        if ( cons != null )
            try {
                return cons.newInstance(empty);
            } catch (Exception e) {
                throw FSTUtil.rethrow(e);
            }
        return objInstantiator.newInstance();
    }

    @Override
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

    @Override
    public Constructor findConstructorForSerializable(Class clazz) {
        return findConstructorForExternalize(clazz);
    }
}
