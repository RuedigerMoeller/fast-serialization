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
package org.nustaq.serialization.coders;

import org.nustaq.serialization.*;
import org.nustaq.serialization.minbin.MBOut;
import org.nustaq.serialization.minbin.MinBin;

import java.io.*;
import java.lang.reflect.Array;

/**
 * Date: 30.03.2014
 * Time: 18:47
 *
 * serializes into self describing binary format MinBin
 */
public class FSTMinBinEncoder implements FSTEncoder {

    MBOut out = new MBOut();
    OutputStream outputStream;
    FSTConfiguration conf;
    private int offset = 0;

    public FSTMinBinEncoder(FSTConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public void writeRawBytes(byte[] bufferedName, int off, int length) throws IOException {
        out.writeArray(bufferedName, off, length);
    }

    /**
     * does not write class tag and length
     *
     * @param array
     * @param start
     * @param length @throws java.io.IOException
     */
    @Override
    public void writePrimitiveArray(Object array, int start, int length) throws IOException {
        out.writeArray(array, start, length);
    }

    @Override
    public void writeStringUTF(String str) throws IOException {
        out.writeTag(str);
    }

    @Override
    public void writeFShort(short c) throws IOException {
        out.writeInt(MinBin.INT_16,c);
    }

    @Override
    public void writeFChar(char c) throws IOException {
        out.writeInt(MinBin.CHAR, c);
    }

    @Override
    public void writeFByte(int v) throws IOException {
        out.writeInt(MinBin.INT_8, v);
    }

    @Override
    public void writeFInt(int anInt) throws IOException {
        out.writeInt(MinBin.INT_32,anInt);
    }

    @Override
    public void writeFLong(long anInt) throws IOException {
        out.writeInt(MinBin.INT_64, anInt);
    }

    @Override
    public void writeFFloat(float value) throws IOException {
        out.writeTag(value);
    }

    @Override
    public void writeFDouble(double value) throws IOException {
        out.writeTag(value);
    }

    @Override
    public int getWritten() {
        return out.getWritten()+offset;
    }

    @Override
    public void skip(int i) {
        throw new RuntimeException("not supported");
    }

    /**
     * close and flush to underlying stream if present. The stream is also closed
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        if ( outputStream != null ) {
            
            outputStream.close();
        }
    }

    @Override
    public void reset(byte[] bytez) {
        offset = 0;
        if (bytez!=null)
            out.reset(bytez);
        else
            out.reset();
    }

    /**
     * resets stream (positions are lost)
     *
     * @throws java.io.IOException
     */
    @Override
    public void flush() throws IOException {
        if ( outputStream != null ) {
            outputStream.write(out.getBytez(),0,out.getWritten());
            offset = out.getWritten();
            out.resetPosition();
        }
    }

    /**
     * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
     *
     * @param position
     * @param v
     */
    @Override
    public void writeInt32At(int position, int v) {
        throw new RuntimeException("not supported");
    }

    /**
     * if output stream is null, just encode into a byte array
     *
     * @param outstream
     */
    @Override
    public void setOutstream(OutputStream outstream) {
        this.outputStream = outstream;
    }

    @Override
    public void ensureFree(int bytes) throws IOException {

    }

    @Override
    public byte[] getBuffer() {
        return out.getBytez();
    }

    @Override
    public void registerClass(Class possible) {

    }

    @Override
    public void writeClass(Class cl) {
       // already written in write tag
    }

    @Override
    public void writeClass(FSTClazzInfo clInf) {
        // already written in write tag
    }

    @Override
    public void writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo) {
        byte[] bufferedName = (byte[]) subInfo.getBufferedName();
        if ( bufferedName != null ) {
            out.writeRaw(bufferedName,0,bufferedName.length);
        } else {
            // fully cache metadata
            int pos = out.getWritten();

            out.writeTag(subInfo.getName());

            int len = out.getWritten() - pos;
            bufferedName = new byte[len];
            System.arraycopy(out.getBytez(),pos,bufferedName,0,len);
            subInfo.setBufferedName(bufferedName);
        }
    }
    
    @Override
    public boolean writeTag(byte tag, Object infoOrObject, long somValue, Object toWrite, FSTObjectOutput oout) throws IOException {
        switch (tag) {
            case FSTObjectOutput.HANDLE:
                out.writeTagHeader(MinBin.HANDLE);
                out.writeInt(MinBin.INT_32,somValue);
                return true;
            case FSTObjectOutput.NULL:
                out.writeTag(null);
                return true;
            case FSTObjectOutput.TYPED:
            case FSTObjectOutput.OBJECT:
                FSTClazzInfo clzInfo = (FSTClazzInfo) infoOrObject;
                if (clzInfo.useCompatibleMode() && clzInfo.getSer() == null ) {
                    throw new RuntimeException("Unsupported backward compatibility mode for class '"+clzInfo.getClazz().getName()+"'. Pls register a Custom Serializer to fix");
                }
                if (clzInfo.getClazz() == String.class )
                    break;
                if (clzInfo.getClazz() == Double.class )
                    break;
                if (clzInfo.getClazz() == Float.class )
                    break;
                if (clzInfo.getClazz() == Byte.class )
                    break;
                if (clzInfo.getClazz() == Short.class )
                    break;
                if (clzInfo.getClazz() == Integer.class )
                    break;
                if (clzInfo.getClazz() == Long.class )
                    break;
                if (clzInfo.getClazz() == Character.class )
                    break;
                if (clzInfo.getClazz() == Boolean.class )
                    break;
//                if ( clzInfo.getClazz() == Byte.class || clzInfo.getClazz() == Short.class || clzInfo.getClazz() == Character.class ) {
//                    out.writeObject(toWrite);
//                    return true;
//                }
                if ( clzInfo.getSer()!=null || clzInfo.isExternalizable() ) {
                    out.writeTagHeader(MinBin.SEQUENCE);
                    writeSymbolicClazz(clzInfo.getClazz());
                    out.writeIntPacked(-1); // END Marker required
                } else
                {
                    out.writeTagHeader(MinBin.OBJECT);
                    writeSymbolicClazz(clzInfo.getClazz());
                    out.writeIntPacked(clzInfo.getFieldInfo().length);
                }
                break;
            case FSTObjectOutput.ONE_OF:
                throw new RuntimeException("not implemented");
            case FSTObjectOutput.STRING:
                break; // ignore, header created by calling writeUTF
            case FSTObjectOutput.BIG_BOOLEAN_FALSE:
                out.writeTag(Boolean.FALSE);
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_BOOLEAN_TRUE:
                out.writeTag(Boolean.TRUE);
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_LONG:
                break; // ignore, header implicitely created by writing long.
            case FSTObjectOutput.BIG_INT:
                break;// ignore, header implicitely created by writing int.
            case FSTObjectOutput.ARRAY:
                Class<?> clz = infoOrObject.getClass();
                Class<?> componentType = clz.getComponentType();
                if ( clz.isArray() && componentType.isPrimitive() )
                {
                    if ( componentType == double.class ) {
                        out.writeTagHeader(MinBin.SEQUENCE);
                        writeSymbolicClazz(clz);
                        int length = Array.getLength(infoOrObject);
                        out.writeIntPacked(length);
                        for ( int i = 0; i < length; i++ ) {
                            out.writeTag(Array.getDouble(infoOrObject,i));
                        }
                    } else if ( componentType == float.class ) {
                        out.writeTagHeader(MinBin.SEQUENCE);
                        writeSymbolicClazz(clz);
                        int length = Array.getLength(infoOrObject);
                        out.writeIntPacked(length);
                        for ( int i = 0; i < length; i++ ) {
                            out.writeTag(Array.getFloat(infoOrObject, i));
                        }
                    } else {
                        out.writeArray(infoOrObject, 0, Array.getLength(infoOrObject));
                    }
                    return true;
                } else {
                    out.writeTagHeader(MinBin.SEQUENCE);
                    writeSymbolicClazz(clz);
                }
                break;
            case FSTObjectOutput.ENUM:
                out.writeTagHeader(MinBin.SEQUENCE);
                boolean isEnumClass = toWrite.getClass().isEnum();
                Class c = toWrite.getClass();
                if (!isEnumClass) {
                    // weird stuff ..
                    while (c != null && !c.isEnum()) {
                        c = toWrite.getClass().getSuperclass();
                    }
                    if (c == null) {
                        throw new RuntimeException("Can't handle this enum: " + toWrite.getClass());
                    }
                    writeSymbolicClazz(c);
                } else {
                    writeSymbolicClazz(c);
                }
                out.writeIntPacked(1);
                out.writeObject(toWrite.toString());
                return true;
            default:
                throw new RuntimeException("unexpected tag "+tag);
        }
        return false;
    }

    private void writeSymbolicClazz(Class<?> clz) {
        byte b[] = conf.getCrossPlatformBinaryCache(clz.getName());
        if ( b == null ) {
            int pos = out.getWritten();

            out.writeTag(classToString(clz));

            int len = out.getWritten() - pos;
            b = new byte[len];
            System.arraycopy(out.getBytez(), pos, b, 0, len);
            conf.registerCrossPlatformClassBinaryCache(clz.getName(), b);
        } else {
            out.writeRaw(b,0,b.length);
        }
        return;
    }

    protected String classToString(Class clz) {
        return conf.getCPNameForClass(clz);
    }

    @Override
    public void externalEnd(FSTClazzInfo clz) {
        if ( clz == null ||
             clz.isExternalizable() ||
             ( clz.getSer() instanceof FSTCrossPlatformSerialzer && ((FSTCrossPlatformSerialzer) clz.getSer()).writeTupleEnd())
           ) {
            out.writeTag(MinBin.END_MARKER);
        }
    }

    @Override
    public boolean isWritingAttributes() {
        return true;
    }

    public boolean isPrimitiveArray(Object array, Class<?> componentType) {
        return componentType.isPrimitive() && array instanceof double[] == false && array instanceof float[] == false;
    }

    public boolean isTagMultiDimSubArrays() {
        return true;
    }

    @Override
    public void writeVersionTag(int version) throws IOException {
        // versioning not supported for minbin
    }

    @Override
    public boolean isByteArrayBased() {
        return true;
    }

    @Override
    public void writeArrayEnd() {

    }

    @Override
    public void writeFieldsEnd(FSTClazzInfo serializationInfo) {

    }

}
