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
package org.nustaq.serialization;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ruedi on 27.03.14.
 */
public interface FSTEncoder {

    void setConf(FSTConfiguration conf);

    void writeRawBytes(byte[] bufferedName, int off, int length) throws IOException;
    /**
     * does not write class tag and length
     *
     * @param array
     * @throws IOException
     */
    void writePrimitiveArray(Object array, int start, int length) throws IOException;
    
    void writeStringUTF(String str) throws IOException;

    void writeFShort(short c) throws IOException;
    void writeFChar(char c) throws IOException;
    void writeFByte(int v) throws IOException;
    void writeFInt(int anInt) throws IOException;
    void writeFLong(long anInt) throws IOException;
    void writeFFloat(float value) throws IOException;
    void writeFDouble(double value) throws IOException;

    int getWritten();
    void skip(int i);

    /**
     * close and flush to underlying stream if present. The stream is also closed
     * @throws java.io.IOException
     */
    void close() throws IOException;
    void reset(byte[] out); // resets outbuff only

    /**
     * resets stream (positions are lost)
     * @throws java.io.IOException
     */
    void flush() throws IOException;

    /**
     * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
     * @param position
     * @param v
     */
    void writeInt32At(int position, int v);

    /**
     * if output stream is null, just encode into a byte array
     * @param outstream
     */
    void setOutstream(OutputStream outstream);

    void ensureFree(int bytes) throws IOException;

    byte[] getBuffer();

    void registerClass(Class possible);

    void writeClass(Class cl);
    void writeClass(FSTClazzInfo clInf);

    // write a meta byte item. return true if encoder wrote full object (e.g. literal, primitive)
    boolean writeTag(byte tag, Object info, long somValue, Object toWrite, FSTObjectOutput oout) throws IOException;

    // return true, if this already wrote everything
    boolean writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo, Object value);

    void externalEnd(FSTClazzInfo clz); // demarkls the end of an externalizable or classes with serializer registered

    boolean isWritingAttributes();

    boolean isPrimitiveArray(Object array, Class<?> componentType);

    boolean isTagMultiDimSubArrays();

    void writeVersionTag(int version) throws IOException;

    boolean isByteArrayBased();

    void writeArrayEnd();

    void writeFieldsEnd(FSTClazzInfo serializationInfo);

    FSTConfiguration getConf();
}
