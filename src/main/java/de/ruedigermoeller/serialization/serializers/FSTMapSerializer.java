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

package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.*;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 17:47
 * To change this template use File | Settings | File Templates.
 */
public class FSTMapSerializer extends FSTBasicObjectSerializer {
    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Map col = (Map)toWrite;
        out.writeInt(col.size());
        Class[] possibleClasses = referencedBy.getPossibleClasses();
        if ( (possibleClasses == null || possibleClasses.length == 0) ) {
            Class possibleKeys[] = {null};
            Class possibleVals[] = {null};
            for (Object o : col.entrySet()) {
                Map.Entry next = (Map.Entry) o;
                Object key = next.getKey();
                Object value = next.getValue();
                out.writeObjectInternal(key, possibleKeys);
                out.writeObjectInternal(value, possibleVals);
                if ( key != null ) {
                    possibleKeys[0] = key.getClass();
                }
                if ( value != null ) {
                    possibleVals[0] = value.getClass();
                }
            }
        } else {
            for (Iterator iterator = col.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry next = (Map.Entry) iterator.next();
                out.writeObjectInternal(next.getKey(), possibleClasses);
                out.writeObjectInternal(next.getValue(), possibleClasses);
            }
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object res = null;
        int len = in.readInt();
        if ( objectClass == HashMap.class ) {
            res = new HashMap(len);
        } else
        if ( objectClass == Hashtable.class ) {
            res = new Hashtable(len);
        } else
        {
            res = objectClass.newInstance();
        }
        in.registerObject(res, streamPositioin,serializationInfo, referencee);
        Map col = (Map)res;
        Class[] possibleClasses = referencee.getPossibleClasses();
        if ( (possibleClasses == null || possibleClasses.length == 0) ) {
            Class possibleKeys[] = {null};
            Class possibleVals[] = {null};
            for ( int i = 0; i < len; i++ ) {
                Object key = in.readObjectInternal(possibleKeys);
                Object val = in.readObjectInternal(possibleVals);
                if ( key != null ) {
                    possibleKeys[0] = key.getClass();
                }
                if ( val != null ) {
                    possibleVals[0] = val.getClass();
                }
                col.put(key,val);
            }
        } else {
            for ( int i = 0; i < len; i++ ) {
                Object key = in.readObjectInternal(possibleClasses);
                Object val = in.readObjectInternal(possibleClasses);
                col.put(key,val);
            }
        }
        return res;
    }
}
