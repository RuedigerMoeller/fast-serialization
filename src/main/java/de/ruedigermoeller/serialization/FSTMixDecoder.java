package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.mix.Mix;
import de.ruedigermoeller.serialization.mix.MixIn;
import de.ruedigermoeller.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

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
 * Date: 02.04.2014
 * Time: 19:13
 * To change this template use File | Settings | File Templates.
 */
public class FSTMixDecoder implements FSTDecoder {
    
    FSTConfiguration conf;
    MixIn input;
    private InputStream inputStream;

    public FSTMixDecoder(FSTConfiguration conf) {
        this.conf = conf;
        input = new MixIn(null,0);
    }

    @Override
    public String readStringUTF() throws IOException {
        Object read = input.readValue();
        if ( read instanceof String )
            return (String) read;
        // in case preceding atom has been consumed b[] => str 8 char[] => str 16;
        if ( read instanceof byte[] ) {
            return new String((byte[])read,0,0,((byte[]) read).length);
        } else if ( read instanceof char[] ) {
            return new String((char[])read,0,((char[]) read).length);
        } else if ( read == Mix.ATOM_TUPEL_END )
            return null;
        throw new RuntimeException("Expected String, byte[], char[] or tupel end");
    }

    @Override
    public String readStringAsc() throws IOException {
        return (String) input.readValue();
    }

    @Override
    /**
     * if array is null => create own array. if len == -1 => use len read
     */
    public Object readFPrimitiveArray(Object array, Class componentType, int len) {
        Object arr = input.readValue();
        int length = Array.getLength(arr);
        if ( len != -1 && len != length)
            throw new RuntimeException("unexpected arrays size");
        Class<?> componentTypeRead = arr.getClass().getComponentType();
        if (componentTypeRead != componentType) {
            throw new RuntimeException("expected native array of "+componentType+" found "+componentTypeRead);
        } else {
            return arr;
        }
    }

    @Override
    public void readFIntArr(int len, int[] arr) throws IOException {
        int res[] = (int[]) input.readValue();
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
        return input.readDouble();
    }

    @Override
    public float readFFloat() throws IOException {
        return (float) input.readDouble();
    }

    @Override
    public byte readFByte() throws IOException {
        return (byte) input.readInt();
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
                FSTUtil.rethrow(e);
            }            
        }
    }

    @Override
    public void ensureReadAhead(int bytes) {
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

    int lastObjectLen;
    int lastObjectTagType;
    public byte readObjectHeaderTag() throws IOException {
        byte tag = input.peekIn();
        final int type = tag & 0xf;
        lastObjectTagType = type;
        if ( type == Mix.OBJECT || type == Mix.TUPEL ) {
            input.readIn();
            lastObjectLen = tag>>4;
            if (lastObjectLen == 0 )
                lastObjectLen = (int) input.readInt();
            return FSTObjectOutput.OBJECT;
        }
        switch ( type ) {
            case Mix.STR_16:
            case Mix.STR_8:
                return FSTObjectOutput.STRING;
            case Mix.INT_8:
            case Mix.CHAR:
            case Mix.INT_16:
            case Mix.INT_32:
            case Mix.INT_64:
            case Mix.DOUBLE:
                lastReadDirectObject = input.readValue();
                return FSTObjectOutput.DIRECT_OBJECT;
        }
        return -77;
    }
    
    public Object getDirectObject() // in case class already resolves to read object (e.g. mix input)
    {
        Object tmp = lastReadDirectObject;
        lastReadDirectObject = null;
        return tmp;
    }

    Object lastReadDirectObject; // in case readClass already reads full mix value
    @Override
    public FSTClazzInfo readClass() throws IOException, ClassNotFoundException {
        Object read = input.readValue();
        if ( read == Mix.ATOM_STR_8 || read == Mix.ATOM_STR_16 )
            return conf.getCLInfoRegistry().getCLInfo(classForName(String.class.getName()));
        String name = (String) read;
        String clzName = conf.getClassForCPName(name);
        return conf.getCLInfoRegistry().getCLInfo(classForName(clzName));
    }

    @Override
    public Class classForName(String name) throws ClassNotFoundException {
        return Class.forName(name);
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

}
