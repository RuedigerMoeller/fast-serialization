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

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 15:04
 *
 * contains a map from class => serializer.
 * One can register Serializers for exact classes or a class and all its subclasses (can have unexpected consequences in case a subclass holds additional state).
 *
 */
class FSTSerializerRegistry {

    private FSTSerializerRegistryDelegate delegate;

    static final FSTObjectSerializer NULL = new NULLSerializer();

    void setDelegate(FSTSerializerRegistryDelegate delegate) {
        this.delegate = delegate;
    }

    FSTSerializerRegistryDelegate getDelegate() {
        return delegate;
    }

    private static class NULLSerializer implements FSTObjectSerializer {

        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) {
        }

        @Override
        public void readObject(FSTObjectInput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws Exception {
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
        public Object instantiate(Class objectClass, FSTObjectInput fstObjectInput, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
            return null;
        }
    }

    private final static class SerEntry {
        boolean forSubClasses = false;
        final FSTObjectSerializer ser;

        SerEntry(boolean forSubClasses, FSTObjectSerializer ser) {
            this.forSubClasses = forSubClasses;
            this.ser = ser;
        }
    }

    private final HashMap<Class,SerEntry> map = new HashMap<>(97);

    final FSTObjectSerializer getSerializer(Class cl) {
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

    private FSTObjectSerializer getSerializer(Class cl, Class lookupStart) {
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

    void putSerializer(Class cl, FSTObjectSerializer ser, boolean includeSubclasses) {
        map.put(cl,new SerEntry(includeSubclasses,ser));
    }
}
