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

/**
 * Created by ruedi on 07.03.14.
 */
import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
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
public class FSTArrayListSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        ArrayList col = (ArrayList)toWrite;
        int size = col.size();
        out.writeInt(size);
        Class lastClz = null;
        FSTClazzInfo lastInfo = null;
        for (int i = 0; i < size; i++) {
            Object o = col.get(i);
            if ( o != null ) {
                lastInfo = out.writeObjectInternal(o, o.getClass() == lastClz ? lastInfo : null, null);
                lastClz = o.getClass();
            } else
                out.writeObjectInternal(o, null, null);
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        try {
            int len = in.readInt();
            ArrayList res = new ArrayList(len);
            in.registerObject(res, streamPosition,serializationInfo, referencee);
            for ( int i = 0; i < len; i++ ) {
                final Object o = in.readObjectInternal(null);
                res.add(o);
            }
            return res;
        } catch (Throwable th) {
            FSTUtil.rethrow(th);
        }
        return null;
    }

}
