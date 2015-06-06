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

import org.nustaq.offheap.bytez.BasicBytez;
import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.serialization.*;
import org.nustaq.serialization.util.FSTUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ruedi on 09.11.2014.
 *
 * uses unsafe to read values directly from memory instead an inputstream. no value compression is applied.
 *
 */
public class FSTBytezDecoder  implements FSTDecoder {

    BasicBytez input;
    HeapBytez ascStringCache;
    FSTConfiguration conf;
    public FSTClazzNameRegistry clnames;
    long pos;
    InputStream inputStream;
    long readUntil = 0;

    public FSTBytezDecoder(FSTConfiguration conf, BasicBytez input) {
        this(conf);
        if ( FSTConfiguration.isAndroid )
            throw new RuntimeException("not supported on android");
        this.input = input;
    }

    public FSTBytezDecoder(FSTConfiguration conf) {
        this.conf = conf;
        clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
        if (clnames == null) {
            clnames = new FSTClazzNameRegistry(conf.getClassRegistry(), conf);
        } else {
            clnames.clear();
        }
    }

    byte tmp[];
    public int ensureReadAhead(int bytes) {
        if ( inputStream != null ) {
            if ( pos+bytes > readUntil ) {
                return readNextInputChunk(bytes);
            }
        } else if ( pos+bytes > input.length() ) {
            return -1;
        }
        return 0;
    }

    protected int readNextInputChunk(int bytes) {
        try {
            int toRead = Math.max(Integer.MAX_VALUE, bytes);
            if ( inputStream instanceof ByteArrayInputStream ) {
                toRead = Math.min(((ByteArrayInputStream) inputStream).available(),toRead);
            }
            if ( tmp == null || tmp.length < toRead ) {
                tmp = new byte[toRead];
            }
            int read = inputStream.read(tmp, 0, toRead);
            if ( read > 0 ) {
                if ( input.length() < pos+read ) {
                    BasicBytez bytez = input.newInstance(2*(pos + read));
                    input.copyTo(bytez,0,0,pos);
                    input = (HeapBytez) bytez;
                }
                input.set(pos,tmp,0,read);
                readUntil = pos+read;
                return read;
            } else if ( read == -1 )
                return -1;
            // fixme: should loop in case read == 0
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return 0;
    }

    char chBufS[];
    char[] getCharBuf(int siz) {
        char chars[] = chBufS;
        if (chars == null || chars.length < siz) {
            chars = new char[Math.max(siz, 15)];
            chBufS = chars;
        }
        return chars;
    }

    public String readStringUTF() throws IOException {
        int len = readFInt();
        char[] charBuf = getCharBuf(len * 2);
        ensureReadAhead(len*2);
        input.getCharArr(pos,charBuf,0,len);
        pos += len*2;
        return new String(charBuf, 0, len);
    }

    public byte readObjectHeaderTag() throws IOException {
        return readFByte();
    }

    /**
     * len < 127 !!!!!
     *
     * @return
     * @throws java.io.IOException
     */
    @Override
    public String readStringAsc() throws IOException {
        int len = readFInt();
        if (ascStringCache == null || ascStringCache.length() < len)
            ascStringCache = new HeapBytez(new byte[len]);
        ensureReadAhead(len);
//        System.arraycopy(input.buf, input.pos, ascStringCache, 0, len);
        input.copyTo(ascStringCache, 0, pos, len);
        pos += len;
        return new String(ascStringCache.getBase(), 0, 0, len);
    }

    public BasicBytez getInput() {
        return input;
    }

    public void setInput(BasicBytez input) {
        this.input = input;
    }

    /**
     * assumes class header+len already read
     *
     * @param componentType
     * @param len
     * @return
     */
    @Override
    public Object readFPrimitiveArray(Object array, Class componentType, int len) {
        // FIXME: if else chaining could be avoided
        if (componentType == byte.class) {
            ensureReadAhead(len);
            byte arr[] = (byte[]) array;
            input.getArr(pos, arr, 0, len);
            pos += len;
            return arr;
        } else if (componentType == char.class) {
            ensureReadAhead(len*2);
            char[] arr = (char[]) array;
            input.getCharArr(pos, arr, 0, len);
            pos += len * 2;
            return arr;
        } else if (componentType == short.class) {
            ensureReadAhead(len*2);
            short[] arr = (short[]) array;
            input.getShortArr(pos, arr, 0, len);
            pos += len * 2;
            return arr;
        } else if (componentType == int.class) {
            ensureReadAhead(len*4);
            int[] arr = (int[]) array;
            input.getIntArr(pos, arr, 0, len);
            pos += len * 4;
            return arr;
        } else if (componentType == float.class) {
            ensureReadAhead(len*4);
            float[] arr = (float[]) array;
            input.getFloatArr(pos, arr, 0, len);
            pos += len * 4;
            return arr;
        } else if (componentType == double.class) {
            ensureReadAhead(len*8);
            double[] arr = (double[]) array;
            input.getDoubleArr(pos, arr, 0, len);
            pos += len * 8;
            return arr;
        } else if (componentType == long.class) {
            ensureReadAhead(len*8);
            long[] arr = (long[]) array;
            input.getLongArr(pos, arr, 0, len);
            pos += len * 8;
            return arr;
        } else if (componentType == boolean.class) {
            ensureReadAhead(len);
            boolean[] arr = (boolean[]) array;
            input.getBooleanArr(pos, arr, 0, len);
            pos += len;
            return arr;
        } else {
            throw new RuntimeException("unexpected primitive type " + componentType.getName());
        }
    }

    @Override
    public void readFIntArr(int len, int[] arr) throws IOException {
        input.getIntArr(pos, arr, 0, len);
        pos += len * 4;
    }

    @Override
    public int readFInt() throws IOException {
        return readPlainInt();
    }

    @Override
    public double readFDouble() throws IOException {
        return Double.longBitsToDouble(readPlainLong());
    }

    /**
     * Reads a 4 byte float.
     */
    @Override
    public float readFFloat() throws IOException {
        return Float.intBitsToFloat(readPlainInt());
    }

    @Override
    public final byte readFByte() throws IOException {
        ensureReadAhead(1);
        return input.get(pos++);
    }

    @Override
    public int readIntByte() throws IOException {
        final int res = ensureReadAhead(1);
        if ( res == -1 )
            return -1;
        return input.get(pos++) & 0xff;
    }

    @Override
    public long readFLong() throws IOException {
        return readPlainLong();
    }

    @Override
    public char readFChar() throws IOException {
        return readPlainChar();
    }

    @Override
    public short readFShort() throws IOException {
        return readPlainShort();
    }

    private char readPlainChar() throws IOException {
        ensureReadAhead(2);
        char res = input.getChar(pos);
        pos += 2;
        return res;
    }

    private short readPlainShort() throws IOException {
        ensureReadAhead(2);
        short res = input.getShort(pos);
        pos += 2;
        return res;
    }

    @Override
    public int readPlainInt() throws IOException {
        ensureReadAhead(4);
        int res = input.getInt(pos);
        pos += 4;
        return res;
    }

    private long readPlainLong() throws IOException {
        ensureReadAhead(8);
        long res = input.getLong(pos);
        pos += 8;
        return res;
    }

    @Override
    public byte[] getBuffer() {
        if ( input instanceof HeapBytez && ((HeapBytez) input).getOffsetIndex() == 0 ) {
            return ((HeapBytez) input).asByteArray();
        }
        byte res[] = new byte[(int) pos];
        input.getArr(0,res,0, (int) pos);
        return res;
    }

    @Override
    public int getInputPos() {
        return (int) pos;
    }

    @Override
    public void moveTo(int position) {
        pos = position;
    }

    @Override
    public void reset() {
        pos = 0;
        clnames.clear();
        inputStream = null;
    }

    @Override
    public void setInputStream(InputStream in) {
        if ( in == FSTObjectInput.emptyStream ) {
            inputStream = null;
            readUntil = 0;
            return;
        }
        this.inputStream = in;
        clnames.clear();
        pos = 0;
        if ( input == null )
            input = new HeapBytez(new byte[4096]);
        readUntil = 0;
    }

    @Override
    public void resetToCopyOf(byte[] bytes, int off, int len) {
        inputStream = null;
        if ( input == null ) {
            byte[] base = new byte[len];
            input = new HeapBytez(base,0,len);
        }
        if ( input.length() < len )
        {
            input = (HeapBytez) input.newInstance(len);
        }
        input.set(0, bytes, off, len);
        pos = 0;
        clnames.clear();
    }

    @Override
    public void resetWith(byte[] bytes, int len) {
        inputStream = null;
        if ( input == null ) {
            input = new HeapBytez(bytes,0,len);
            return;
        }
        // suboptimal method for non heap backing
        if ( input.getClass() == HeapBytez.class ) {
            ((HeapBytez)input).setBase(bytes,0,len);
        } else {
            BasicBytez newBytez = input.newInstance(len);
            newBytez.set(0,bytes,0,len);
        }
        pos = 0;
        clnames.clear();
    }

    @Override
    public FSTClazzInfo readClass() throws IOException, ClassNotFoundException {
        return clnames.decodeClass(this);
    }

    @Override
    public Class classForName(String name) throws ClassNotFoundException {
        return clnames.classForName(name);
    }

    @Override
    public void registerClass(Class possible) {
        clnames.registerClass(possible);
    }

    @Override
    public void close() {
        conf.returnObject(clnames);
    }

    @Override
    public void skip(int n) {
        pos += n;
    }

    @Override
    public void readPlainBytes(byte[] b, int off, int len) {
        ensureReadAhead(len);
//        System.arraycopy(input.buf,input.pos,b,off,len);
        input.getArr(pos, b, off, len);
        pos += len;
    }

    @Override
    public boolean isMapBased() {
        return false;
    }

    @Override
    public Object getDirectObject() // in case class already resolves to read object (e.g. mix input)
    {
        return null;
    }

    @Override
    public int getObjectHeaderLen() {
        return -1;
    }

    @Override
    public void consumeEndMarker() {
    }

    @Override
    public Class readArrayHeader() throws Exception {
        return readClass().getClazz();
    }

    @Override
    public void readExternalEnd() {
        // do nothing for direct encoding
    }

    @Override
    public boolean isEndMarker(String s) {
        return false;
    }

    @Override
    public int readVersionTag() throws IOException {
        return readFByte();
    }

    @Override
    public void pushBack(int bytes) {
        pos -= bytes;
    }

    @Override
    public void readArrayEnd(FSTClazzInfo clzSerInfo) {}

    @Override
    public void readObjectEnd() {}

    @Override
    public Object coerceElement(Class arrType, Object value) {
        return value;
    }

    @Override
    public int available() {
        return (int) (input.length()-pos);
    }

    @Override
    public boolean inArray() {
        return false;
    }

    @Override
    public void startFieldReading(Object newObj) {

    }

}
