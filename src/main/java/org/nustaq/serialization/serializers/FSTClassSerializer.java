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
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 02.06.13
 * Time: 16:51
 * To change this template use File | Settings | File Templates.
 */
public class FSTClassSerializer extends FSTBasicObjectSerializer {

    // addition of m.weindel: handling of primitive classes, thx
    private final Map<String, Class> primitiveMap;

    public FSTClassSerializer() {
        super();
        primitiveMap = new HashMap<String,Class>();
        Class[] primitives = {byte.class, short.class, int.class, long.class, float.class, double.class, char.class, boolean.class, void.class};
        for (Class cls: primitives) {
            primitiveMap.put(cls.getName(), cls);
        }
    }

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Class cls = (Class) toWrite;
        out.writeBoolean(cls.isPrimitive());
        out.writeStringUTF(cls.getName());
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        boolean isPrimitive = in.readBoolean();
        String name = in.readStringUTF();
        Class cls = isPrimitive ? primitiveMap.get(name) : in.getClassForName(name);
        in.registerObject(cls, streamPosition, serializationInfo, referencee);
        return cls;
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
}
