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
package org.nustaq.serialization;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class FSTSerializerRegistry {

    private FSTSerializerRegistryDelegate delegate;

    public static FSTObjectSerializer NULL = new NULLSerializer();

    public void setDelegate(FSTSerializerRegistryDelegate delegate) {
        this.delegate = delegate;
    }

    public FSTSerializerRegistryDelegate getDelegate() {
        return delegate;
    }

    static class NULLSerializer implements FSTObjectSerializer {

        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) {
        }

        @Override
        public void readObject(FSTObjectInput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) {
        }

        @Override
        public boolean willHandleClass(Class cl) {
            return true;
        }

        /**
         * @return true if FST can skip a search for same instances in the serialized ObjectGraph. This speeds up reading and writing and makes
         *         sense for short immutable such as Integer, Short, Character, Date, .. . For those classes it is more expensive (CPU, size) to do a lookup than to just
         *         write the Object twice in case.
         */
        @Override
        public boolean alwaysCopy() {
            return false;
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput fstObjectInput, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) {
            return null;
        }
    };

    final static class SerEntry {
        boolean forSubClasses = false;
        FSTObjectSerializer ser;

        SerEntry(boolean forSubClasses, FSTObjectSerializer ser) {
            this.forSubClasses = forSubClasses;
            this.ser = ser;
        }
    }

    HashMap<Class,SerEntry> map = new HashMap<Class, SerEntry>(97);

    public final FSTObjectSerializer getSerializer(Class cl) {
        if ( cl.isPrimitive()) {
            return null;
        }
        if ( delegate != null ) {
            FSTObjectSerializer ser = delegate.getSerializer(cl);
            if ( ser != null ) {
                return ser;
            }
        }
        return getSerializer(cl,cl);
    }

    final FSTObjectSerializer getSerializer(Class cl, Class lookupStart) {
        if ( cl == null ) {
            return null;
        }
        final SerEntry serEntry = map.get(cl);
        if ( serEntry != null ) {
            if ( cl == lookupStart && serEntry.ser.willHandleClass(cl)) {
                return serEntry.ser;
            }
            if ( serEntry.forSubClasses && serEntry.ser.willHandleClass(cl) ) {
                putSerializer(lookupStart,serEntry.ser, false);
                return serEntry.ser;
            }
        }
        if ( cl != Object.class && cl != null ) {
            return getSerializer(cl.getSuperclass(),lookupStart);
        }
        return null;
    }

    public void putSerializer( Class cl, FSTObjectSerializer ser, boolean includeSubclasses) {
        map.put(cl,new SerEntry(includeSubclasses,ser));
    }


}