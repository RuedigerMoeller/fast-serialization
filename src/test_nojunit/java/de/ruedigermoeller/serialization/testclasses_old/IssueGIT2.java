package de.ruedigermoeller.serialization.testclasses_old;

import de.ruedigermoeller.serialization.*;

import java.io.IOException;
import java.nio.ByteBuffer;

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
 * Date: 26.03.2014
 * Time: 21:51
 * To change this template use File | Settings | File Templates.
 */
public class IssueGIT2 {

    public static void main(String[] args) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);

        FSTConfiguration fst = FSTConfiguration.createDefaultConfiguration();
        fst.registerSerializer(ByteBuffer.class, new ByteBufferSerializer(), true);

        FSTObjectOutput out = fst.getObjectOutput();
        out.writeObject(buffer);
        System.out.println(out.getWritten());
    }

    public static class ByteBufferSerializer extends FSTBasicObjectSerializer {

        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo,
                                FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            ByteBuffer buffer = (ByteBuffer) toWrite;
            byte[] array = new byte[buffer.limit()];
            buffer.position(0);
            buffer.get(array);
            out.writeFInt(array.length);
            out.write(array);
        }

        @Override
        public Object instantiate(@SuppressWarnings("rawtypes") Class objectClass, FSTObjectInput in,
                                  FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException,
                ClassNotFoundException, InstantiationException, IllegalAccessException {
            byte[] array = new byte[in.readFInt()];
            in.read(array);
            return ByteBuffer.wrap(array);
        }

    }

}