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
import java.lang.reflect.Field;
import java.util.EnumSet;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 11.11.12
 * Time: 04:09
 * To change this template use File | Settings | File Templates.
 */
public class FSTEnumSetSerializer extends FSTBasicObjectSerializer {

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
                    out.writeClassTag(element.getClass());
                }
                out.writeObjectInternal(element, null, Enum.class);
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
        Class elemCl = in.readClass().getClazz();
        EnumSet enSet = EnumSet.noneOf(elemCl);
        in.registerObject(enSet,streamPositioin,serializationInfo, referencee); // IMPORTANT, else tracking double objects will fail
        for (int i = 0; i < len; i++)
            enSet.add(in.readObjectInternal(Enum.class));
        return enSet;
    }
}
