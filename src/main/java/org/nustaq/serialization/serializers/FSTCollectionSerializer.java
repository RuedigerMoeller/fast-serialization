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
package org.nustaq.serialization.serializers;

import org.nustaq.serialization.*;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class FSTCollectionSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Collection col = (Collection)toWrite;
        int size = col.size();
        out.writeInt(size);
        Class lastClz = null;
        FSTClazzInfo lastInfo = null;
        if ( col.getClass() == ArrayList.class ) {
            List l = (List) col;
            for (int i = 0; i < size; i++) {
                Object o = l.get(i);
                if ( o != null ) {
                    lastInfo = out.writeObjectInternal(o, o.getClass() == lastClz ? lastInfo : null, null);
                    lastClz = o.getClass();
                } else
                    out.writeObjectInternal(o, null, null);
            }
        } else
        {
            for (Object o : col) {
                if ( o != null ) {
                    lastInfo = out.writeObjectInternal(o, o.getClass() == lastClz ? lastInfo : null, null);
                    lastClz = o.getClass();
                } else
                    out.writeObjectInternal(o, null, null);
            }
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        try {
            Object res;
            int len = in.readInt();
            if ( objectClass == ArrayList.class ) {
                res = new ArrayList(len);
            } else
            if ( objectClass == HashSet.class ) {
                res = new HashSet(len);
            } else
            if ( objectClass == Vector.class ) {
                res = new Vector(len);
            } else
            if ( objectClass == LinkedList.class ) {
                res = new LinkedList();
            } else {
                if ( AbstractList.class.isAssignableFrom(objectClass) && objectClass.getName().startsWith( "java.util.Arrays" ) ) {
                    // some collections produced by JDK are not properly instantiable (e.g. Arrays.ArrayList), fall back to arraylist then
                    res = new ArrayList<>();
                } else {
                    res = objectClass.newInstance();
                }
            }
            in.registerObject(res, streamPosition,serializationInfo, referencee);
            Collection col = (Collection)res;
            if ( col instanceof ArrayList ) {
                ((ArrayList)col).ensureCapacity(len);
            }
            for ( int i = 0; i < len; i++ ) {
                final Object o = in.readObjectInternal(null);
                col.add(o);
            }
            return res;
        } catch (Throwable th) {
            FSTUtil.<RuntimeException>rethrow(th);
        }
        return null;
    }
}
