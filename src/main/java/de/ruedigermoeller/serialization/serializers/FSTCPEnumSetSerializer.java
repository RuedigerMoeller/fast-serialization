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
import java.lang.reflect.Field;
import java.util.EnumSet;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 11.11.12
 * Time: 04:09
 *
 * EnumSet Serializer for Cross Platform serialization. Writes full Strings instead of ordinals
 *
 */
public class FSTCPEnumSetSerializer extends FSTBasicObjectSerializer {

    Field elemType;
    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        EnumSet enset = (EnumSet) toWrite;
        int count = 0;
        out.writeInt(enset.size());
        if ( enset.isEmpty() ) { //WTF only way to determine enumtype ..
            EnumSet compl = EnumSet.complementOf(enset);
            out.writeClassTag(compl.iterator().next().getClass());
        } else {
            for (Object element : enset) {
                if ( count == 0 ) {
                    out.writeStringUTF(element.getClass().getName());
                }
                out.writeStringUTF(element.toString());
                count++;
            }
        }
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
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int len = in.readInt();
        Class elemCl = in.getClassForName( in.readStringUTF() );
        EnumSet enSet = EnumSet.noneOf(elemCl);
        in.registerObject(enSet,streamPositioin,serializationInfo, referencee); // IMPORTANT, else tracking double objects will fail
        for (int i = 0; i < len; i++) {
            String val = in.readStringUTF();
            enSet.add(Enum.valueOf(elemCl,val));
        }
        return enSet;
    }
}
