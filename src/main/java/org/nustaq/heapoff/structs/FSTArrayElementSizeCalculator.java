package org.nustaq.heapoff.structs;

import org.nustaq.heapoff.structs.unsafeimpl.FSTStructFactory;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 24.07.13
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public interface FSTArrayElementSizeCalculator {
    public int getElementSize(Field arrayRef, FSTStructFactory fac);
    public Class<? extends FSTStruct> getElementType(Field arrayRef, FSTStructFactory fac);
}
