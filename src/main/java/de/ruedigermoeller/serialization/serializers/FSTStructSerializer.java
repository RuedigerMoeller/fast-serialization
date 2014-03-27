package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.heapoff.bytez.onheap.HeapBytez;
import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;
import de.ruedigermoeller.serialization.FSTBasicObjectSerializer;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

import java.io.IOException;

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
 * Date: 09.11.13
 * Time: 22:06
 * To change this template use File | Settings | File Templates.
 */
public class FSTStructSerializer extends FSTBasicObjectSerializer {

    public static boolean COMPRESS = true;

    /**
     * write the contents of a given object
     */
    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        FSTStruct str = (FSTStruct) toWrite;
        if ( ! str.isOffHeap() ) {
            str = str.toOffHeap();
        }
        int byteSize = str.getByteSize();
        out.writeInt(byteSize);
        if ( COMPRESS ) {
            long base = str.___offset;
            int intsiz = byteSize/4;
            for ( int i=0; i<intsiz;i++ ) {
                int value = str.getInt();
                value = (value << 1) ^ (value >> 31);
                str.___offset+=4;
                while ((value & 0xFFFFFF80) != 0L) {
                    out.writeByte((value & 0x7F) | 0x80);
                    value >>>= 7;
                }
                out.writeByte(value & 0x7F);
            }
            int remainder = byteSize&3;
            for ( int i = 0; i < remainder; i++) {
                byte aByte = str.getByte();
                out.writeByte(aByte);
                str.___offset++;
            }
            str.___offset = base;
        } else {
            byte b[] = new byte[byteSize]; // fixme: cache threadlocal
            str.getBase().getArr(str.getOffset(), b, 0, byteSize);
            out.write( b, 0, byteSize);
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int len = in.readFInt();
        byte bytes[] = new byte[len];

        if ( COMPRESS ) {
            int intsiz = len/4;
            int count = 0;
            for ( int n=0; n<intsiz;n++ ) {
                int value = 0;
                int i = 0;
                int b;
                while (((b = in.readByte()) & 0x80) != 0) {
                    value |= (b & 0x7F) << i;
                    i += 7;
                }
                value = value | (b << i);
                int temp = (((value << 31) >> 31) ^ value) >> 1;
                value = temp ^ (value & (1 << 31));

                bytes[count++] = (byte) ((value >>> 0) & 0xFF);
                bytes[count++] = (byte) ((value >>>  8) & 0xFF);
                bytes[count++] = (byte) ((value >>> 16) & 0xFF);
                bytes[count++] = (byte) ((value >>> 24) & 0xFF);
            }
            int remainder = len&3;
            for ( int i = 0; i < remainder; i++) {
                bytes[count++] = in.readByte();
            }
        } else {
            in.read(bytes);
        }
        HeapBytez hb = new HeapBytez(bytes);
        return FSTStructFactory.getInstance().createStructWrapper(hb, 0);
    }
}
