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

import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTDecoder;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.minbin.MBIn;
import org.nustaq.serialization.minbin.MBObject;
import org.nustaq.serialization.minbin.MinBin;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * Date: 02.04.2014
 * Time: 19:13
 *
 * Deserializes from self describing binary MinBin format
 */
public class FSTMinBinDecoder implements FSTDecoder {
    
    FSTConfiguration conf;
    MBIn input;
    private InputStream inputStream;

    public FSTMinBinDecoder(FSTConfiguration conf) {
        this.conf = conf;
        input = new MBIn(null,0);
    }

    @Override
    public String readStringUTF() throws IOException {
        Object read = input.readObject();
        if (read instanceof String)
            return (String) read;
        // in case preceding atom has been consumed b[] => str 8 char[] => str 16;
        if (read instanceof byte[]) {
            return new String((byte[]) read, 0, 0, ((byte[]) read).length);
        } else if (read instanceof char[]) {
            return new String((char[]) read, 0, ((char[]) read).length);
        } else if (MinBin.END_MARKER == read) {
            return null;
        } else if ( read == null )
            return null;
        throw new RuntimeException("Expected String, byte[], char[] or tupel end");
    }

    @Override
    public String readStringAsc() throws IOException {
        return (String) input.readObject();
    }

    @Override
    /**
     * if array is null => create own array. if len == -1 => use len read
     */
    public Object readFPrimitiveArray(Object array, Class componentType, int len) {
        if ( componentType == double.class ) {
            double[] da = (double[]) array;
            for (int i = 0; i < da.length; i++) {
                da[i] = (double) input.readTag(input.readIn());
            }
            return da;
        }
        if ( componentType == float.class ) {
            float[] da = (float[]) array;
            for (int i = 0; i < da.length; i++) {
                da[i] = (float) input.readTag(input.readIn());
            }
            return da;
        }
        Object arr = array; // input.readObject();
        int length = Array.getLength(arr);
        if ( len != -1 && len != length)
            throw new RuntimeException("unexpected arrays size");
        byte type = 0;
        if (componentType == boolean.class)    type |= MinBin.INT_8;
        else if (componentType == byte.class)  type |= MinBin.INT_8;
        else if (componentType == short.class) type |= MinBin.INT_16;
        else if (componentType == char.class)  type |= MinBin.INT_16 | MinBin.UNSIGN_MASK;
        else if (componentType == int.class)   type |= MinBin.INT_32;
        else if (componentType == long.class)  type |= MinBin.INT_64;
        else throw new RuntimeException("unsupported type " + componentType.getName());
        input.readArrayRaw(type,len,array);
        return arr;
    }

    @Override
    public void readFIntArr(int len, int[] arr) throws IOException {
        int res[] = (int[]) input.readObject();
        for (int i = 0; i < len; i++) {
              arr[i] = res[i];
        }
    }

    @Override
    public int readFInt() throws IOException {
        return (int) input.readInt();
    }

    @Override
    public double readFDouble() throws IOException {
        return (double) input.readObject();
    }

    @Override
    public float readFFloat() throws IOException {
        return (float) input.readObject();
    }

    @Override
    public byte readFByte() throws IOException {
        return (byte) input.readInt();
    }

    @Override
    public int readIntByte() throws IOException {
        return (int) input.readInt();
    }

    @Override
    public long readFLong() throws IOException {
        return input.readInt();
    }

    @Override
    public char readFChar() throws IOException {
        return (char) input.readInt();
    }

    @Override
    public short readFShort() throws IOException {
        return (short) input.readInt();
    }

    @Override
    public int readPlainInt() throws IOException {
        throw new RuntimeException("not supported");
    }

    @Override
    public byte[] getBuffer() {
        return input.getBuffer();
    }

    @Override
    public int getInputPos() {
        return input.getPos();
    }

    @Override
    public void moveTo(int position) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void setInputStream(InputStream in) {
        this.inputStream = in;
        if ( in != null ) {
            try {
                int count = 0;
                int chunk_size = 1000;
                byte buf[] = input.getBuffer(); 
                if (buf==null) {
                    buf = new byte[chunk_size];
                }
                int read = in.read(buf);
                count+=read;
                while( read != -1 ) {
                    try {
                        if ( buf.length < count+chunk_size ) {
                            byte tmp[] = new byte[buf.length*2];
                            System.arraycopy(buf,0,tmp,0,count);
                            buf = tmp;
                        }
                        read = in.read(buf,count,chunk_size);
                        if ( read > 0 )
                            count += read;
                    } catch ( IndexOutOfBoundsException iex ) {
                        read = -1; // many stream impls break contract
                    }
                }
                in.close();
                input.setBuffer(buf, count);
            } catch (IOException e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }            
        }
    }

    @Override
    public int ensureReadAhead(int bytes) {
        return 0;
    }

    @Override
    public void reset() {
        input.reset();
    }

    @Override
    public void resetToCopyOf(byte[] bytes, int off, int len) {
        if (off != 0 )
            throw new RuntimeException("not supported");
        input.setBuffer(bytes,len);
    }

    @Override
    public void resetWith(byte[] bytes, int len) {
        input.setBuffer(bytes,len);
    }

    public int getObjectHeaderLen() // len field of last header read (if avaiable)
    {
        if ( lastObjectLen < 0 )
            return (int) input.readInt();
        return lastObjectLen;
    }

    int lastObjectLen;
    Class lastDirectClass;
    public byte readObjectHeaderTag() throws IOException {
        lastObjectLen = -1;
        byte tag = input.peekIn();
        lastDirectClass = null;
        if ( MinBin.isTag(tag) ) {
            if ( MinBin.getTagId(tag) == MinBin.HANDLE ) {
                input.readIn(); // consume
                return FSTObjectOutput.HANDLE;
            }
            if ( MinBin.getTagId(tag) == MinBin.STRING )
                return FSTObjectOutput.STRING;
            if ( MinBin.getTagId(tag) == MinBin.BOOL ) {
                Boolean b = (Boolean) input.readObject();
                return b ? FSTObjectOutput.BIG_BOOLEAN_TRUE : FSTObjectOutput.BIG_BOOLEAN_FALSE;
            }
            if (    MinBin.getTagId(tag) == MinBin.DOUBLE ||
                    MinBin.getTagId(tag) == MinBin.DOUBLE_ARR ||
                    MinBin.getTagId(tag) == MinBin.FLOAT_ARR ||
                    MinBin.getTagId(tag) == MinBin.FLOAT
            )
            {
                lastReadDirectObject = input.readObject();
                return FSTObjectOutput.DIRECT_OBJECT;
            }
            input.readIn();
            if (MinBin.getTagId(tag) == MinBin.SEQUENCE) {
                try {
                    String cln = (String) input.readObject();
//                  client should use explicit sequence constructor
//                    if ( cln == null ) {
//                        lastDirectClass = MBSequence.class;
//                        // fast terminate as assume js object array so no int for len there ..
//                        return FSTObjectOutput.OBJECT;
//                    } else
                    {
                        lastDirectClass = conf.getClassRegistry().classForName(conf.getClassForCPName(cln));
                    }
                } catch (ClassNotFoundException e) {
                    FSTUtil.<RuntimeException>rethrow(e);
                }
                if ( lastDirectClass.isEnum() ) {
                    input.readInt(); // consume length of 1
                    String enumString = (String) input.readObject();
                    lastReadDirectObject = Enum.valueOf(lastDirectClass,enumString);
                    lastDirectClass = null;
                    return FSTObjectOutput.DIRECT_OBJECT;
                } else
                if ( lastDirectClass.isArray() )
                    return FSTObjectOutput.ARRAY;
                else {
                    input.readInt(); // consume -1 for unknown sequence length
                    return FSTObjectOutput.OBJECT; // with serializer
                }
            }
            if (MinBin.getTagId(tag)==MinBin.NULL)
                return FSTObjectOutput.NULL;
            return FSTObjectOutput.OBJECT;
        }
        lastReadDirectObject = input.readObject();
        return FSTObjectOutput.DIRECT_OBJECT;
    }
    
    public Object getDirectObject() // in case class already resolves to read object (e.g. mix input)
    {
        Object tmp = lastReadDirectObject;
        lastReadDirectObject = null;
        return tmp;
    }

    Object lastReadDirectObject; // in case readClass already reads full minbin value
    @Override
    public FSTClazzInfo readClass() throws IOException, ClassNotFoundException {
        if (lastDirectClass != null ) {
            FSTClazzInfo clInfo = conf.getCLInfoRegistry().getCLInfo(lastDirectClass);
            lastDirectClass = null;
            return clInfo;
        }
        Object read = input.readObject();
        String name = (String) read;
        String clzName = conf.getClassForCPName(name);
        return conf.getCLInfoRegistry().getCLInfo(classForName(clzName));
    }

    HashMap<String,Class> clzCache = new HashMap<>();
    @Override
    public Class classForName(String name) throws ClassNotFoundException {
        if ("Object".equals(name))
            return MBObject.class;
        Class aClass = clzCache.get(name);
        if (aClass!=null)
            return aClass;
        aClass = Class.forName(name);
        clzCache.put(name,aClass);
        return aClass;
    }

    @Override
    public void registerClass(Class possible) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void close() {
        //TODO
        throw new RuntimeException("not implemented");
    }

    @Override
    public void skip(int n) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void readPlainBytes(byte[] b, int off, int len) {
        for (int i = 0; i < len; i++) {
            b[i+off] = input.readIn();
        }
    }

    @Override
    public boolean isMapBased() {
        return true;
    }

    public void consumeEndMarker() {
        byte type = input.peekIn();
        if (type==MinBin.END) {
            input.readIn();
        }
    }

    @Override
    public Class readArrayHeader() throws Exception {
        byte tag = input.peekIn(); // need to be able to consume MinBin Sequence tag silently
        if ( MinBin.getTagId(tag) == MinBin.NULL ) {
            input.readIn();
            lastDirectClass = null;
            return null;
        }
        if ( lastDirectClass != null )
            return readClass().getClazz();
        if ( MinBin.getTagId(tag) == MinBin.SEQUENCE ) {
            input.readIn(); // consume (multidim array)
        } else if ( MinBin.isPrimitive(tag) ) {
            input.readIn(); // consume tag
            switch (MinBin.getBaseType(tag)) {
                case MinBin.INT_8:
                    return byte[].class;
                case MinBin.INT_16:
                    if (MinBin.isSigned(tag) )
                        return short[].class;
                    return char[].class;
                case MinBin.INT_32:
                    return int[].class;
                case MinBin.INT_64:
                    return long[].class;
            }
        }
        return readClass().getClazz();
    }

    @Override
    public void readExternalEnd() {
        if ( input.peekIn() == MinBin.END ) {
            input.readIn();
        }
    }

    @Override
    public boolean isEndMarker(String s) {
        return MinBin.END_MARKER == s;
    }

    @Override
    public int readVersionTag() throws IOException {
        return 0; // versioning not supported for minbin
    }

    @Override
    public void pushBack(int bytes) {
        input.setPos(input.getPos()-bytes);
    }

    @Override
    public void readArrayEnd(FSTClazzInfo clzSerInfo) {

    }

    @Override
    public void readObjectEnd() {

    }

    @Override
    public Object coerceElement(Class arrType, Object value) {
        if ( value instanceof Number ) {
            Number n = (Number) value;
            if ( arrType == Byte.class ) {
                return new Byte(n.byteValue());
            } else if ( arrType == Short.class ) {
                return new Short(n.shortValue());
            } else if ( arrType == Integer.class ) {
                return new Integer(n.intValue());
            } else if ( arrType == Long.class ) {
                return new Long(n.longValue());
            } else if ( arrType == Double.class ) {
                return new Double(n.doubleValue());
            } else if ( arrType == Float.class ) {
                return new Float(n.floatValue());
            } else if ( arrType == Character.class ) {
                return new Character((char) n.intValue());
            }
        }
        return value;
    }

    @Override
    public int available() {
        return input.getCount() - input.getPos();
    }

    @Override
    public boolean inArray() {
        return false;
    }

}
