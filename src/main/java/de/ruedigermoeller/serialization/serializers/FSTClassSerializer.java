package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        boolean isPrimitive = in.readBoolean();
        String name = in.readStringUTF();
        Class cls = isPrimitive ? primitiveMap.get(name) : in.getClassForName(name);
        in.registerObject(cls, streamPositioin, serializationInfo, referencee);
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
