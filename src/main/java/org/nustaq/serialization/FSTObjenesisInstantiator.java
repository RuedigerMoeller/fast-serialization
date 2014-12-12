package org.nustaq.serialization;

import org.objenesis.Objenesis;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Constructor;

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

    @Override
    public Object newInstance(Class clazz, Constructor cons, boolean doesRequireInit, boolean unsafeAsLastResort) {
        return objInstantiator.newInstance();
    }

    @Override
    public Constructor findConstructorForExternalize(Class clazz) {
        return null;
    }

    @Override
    public Constructor findConstructorForSerializable(Class clazz) {
        return null;
    }
}
