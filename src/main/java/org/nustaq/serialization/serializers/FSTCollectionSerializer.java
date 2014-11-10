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

package org.nustaq.serialization.serializers;

import org.nustaq.serialization.util.FSTUtil;
import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

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
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        try {
            Object res = null;
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
                res = objectClass.newInstance();
            }
            in.registerObject(res, streamPositioin,serializationInfo, referencee);
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
            throw FSTUtil.rethrow(th);
        }
    }
}
