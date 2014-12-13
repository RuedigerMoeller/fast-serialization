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
 * Time: 17:47
 * To change this template use File | Settings | File Templates.
 */
public class FSTMapSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Map col = (Map)toWrite;
        out.writeInt(col.size());
        FSTClazzInfo lastKClzI = null;
        FSTClazzInfo lastVClzI = null;
        Class lastKClz = null;
        Class lastVClz = null;
        for (Iterator iterator = col.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry next = (Map.Entry) iterator.next();
            Object key = next.getKey();
            Object value = next.getValue();
            if ( key != null && value != null ) {
                lastKClzI = out.writeObjectInternal(key, key.getClass() == lastKClz ? lastKClzI : null, null);
                lastVClzI = out.writeObjectInternal(value, value.getClass() == lastVClz ? lastVClzI : null, null);
                lastKClz = key.getClass();
                lastVClz = value.getClass();
            } else
            {
                out.writeObjectInternal(key, null, null);
                out.writeObjectInternal(value, null, null);
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
        for ( int i = 0; i < len; i++ ) {
            Object key = in.readObjectInternal(null);
            Object val = in.readObjectInternal(null);
            col.put(key,val);
        }
        return res;
    }
}
