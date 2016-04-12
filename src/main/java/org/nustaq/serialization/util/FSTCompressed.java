package org.nustaq.serialization.util;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 20.06.13
 * Time: 19:50
 * To change this template use File | Settings | File Templates.
 */

/**
 * allows to temporary compress a serializable Object to reduce instance count on the heap.
 * When the compressed object is accessed it is deserialized and a WeakReference holds the
 * deserialized objects to avoid multiple deserialization in a loop.
 *
 * Currently only implementation is FST2ByteCompressed, real offheap (direct buffer) should be easy to do.
 * @param <T>
 */
public abstract class FSTCompressed<T> {

    WeakReference<T> cached = null;

    public T get() {
        T res = null;
        if ( cached != null )
            res = cached.get();
        if (res != null) {
            return res;
        }
        byte[] array = getArray();
        if ( array == null ) {
            return null;
        }
        FSTObjectInput objectIn = getConf().getObjectInput(array,array.length);
        try {
            res = (T) objectIn.readObject();
            cached = new WeakReference<T>(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void set(T object) {
        if ( object == null ) {
            throw new RuntimeException("Object must not be null");
        }
        FSTObjectOutput objectOutput = getConf().getObjectOutput();
        try {
            objectOutput.writeObject(object);
            cached = new WeakReference<T>(object);
            storeArray(objectOutput.getBuffer(),objectOutput.getWritten());
        } catch (Exception e) {
            // CGLogger.warning(this, e); //TODO
        } finally {
            try {
                objectOutput.flush();
            } catch (IOException e) {
                //CGLogger.warning(this, e); //TODO
            }
        }
    }

    protected abstract void storeArray(byte[] buffer, int written);
    protected abstract FSTConfiguration getConf();
    public abstract byte[] getArray();
    public abstract int getLen();
    public abstract int getOffset();
}
