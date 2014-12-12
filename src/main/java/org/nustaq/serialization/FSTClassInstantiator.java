package org.nustaq.serialization;

import java.lang.reflect.Constructor;

/**
 * Created by ruedi on 12.12.14.
 */
public interface FSTClassInstantiator {

    public Object newInstance(Class clazz, Constructor cons, boolean doesRequireInit, boolean unsafeAsLastResort );
    public Constructor findConstructorForExternalize(Class clazz);
    public Constructor findConstructorForSerializable(Class clazz);

}
