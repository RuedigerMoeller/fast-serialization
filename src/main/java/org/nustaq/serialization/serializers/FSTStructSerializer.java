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

import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.offheap.structs.unsafeimpl.FSTStructFactory;
import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;

/**
 * Date: 09.11.13
 * Time: 22:06
 *
 * serializes struct classes
 *
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
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws Exception {
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
