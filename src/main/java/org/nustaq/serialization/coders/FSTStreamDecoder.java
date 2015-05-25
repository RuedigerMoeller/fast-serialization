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
import org.nustaq.serialization.FSTClazzNameRegistry;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTDecoder;
import org.nustaq.serialization.util.FSTInputStream;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Default Coder used for serialization. Decodes a binary stream written with FSTStreamEncoder
 */
public class FSTStreamDecoder implements FSTDecoder {

    private FSTInputStream input;
    byte ascStringCache[];
    FSTConfiguration conf;
    public FSTClazzNameRegistry clnames;

    public FSTStreamDecoder(FSTConfiguration conf) {
        this.conf = conf;
        clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
        if (clnames == null) {
            clnames = new FSTClazzNameRegistry(conf.getClassRegistry(), conf);
        } else {
            clnames.clear();
        }
    }

    public int ensureReadAhead(int bytes) {
        input.ensureReadAhead(bytes);
        return 0; // checking for eof too expensive ..
    }

    char chBufS[];
    char[] getCharBuf(int siz) {
        char chars[] = chBufS;
        if (chars == null || chars.length < siz) {
            chars = new char[Math.max(siz,15)];
            chBufS = chars;
        }
        return chars;
    }

    public String readStringUTF() throws IOException {
        int len = readFInt();
        char[] charBuf = getCharBuf(len*3);
        input.ensureReadAhead(len * 3);
        byte buf[] = input.buf;
        int count = input.pos;
        int chcount = 0;
        for (int i = 0; i < len; i++) {
            char head = (char) ((buf[count++] + 256) &0xff);
            if (head < 255) {
                charBuf[chcount++] = head;
            } else {
                int ch1 = ((buf[count++] + 256) &0xff);
                int ch2 = ((buf[count++] + 256) &0xff);
                charBuf[chcount++] = (char) ((ch1 << 0) + (ch2 << 8));
            }
        }
        input.pos = count;
        return new String(charBuf, 0, chcount);
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
        if (ascStringCache == null || ascStringCache.length < len)
            ascStringCache = new byte[len];
        input.ensureReadAhead(len);
        System.arraycopy(input.buf, input.pos, ascStringCache, 0, len);
        input.pos += len;
        return new String(ascStringCache, 0, 0, len);
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
        try {
            if (componentType == byte.class) {
                byte[] arr = (byte[]) array;
                ensureReadAhead(arr.length); // fixme: move this stuff to the stream !
                System.arraycopy(input.buf,input.pos,arr,0,len);
                input.pos += len;
                return arr;
            } else if (componentType == int.class) {
                final int[] arr = (int[]) array;
                readFIntArr(len, arr);
                return arr;
            } else if (componentType == long.class) {
                long[] arr = (long[]) array;
                readFLongArr(len, arr);
                return arr;
            } else if (componentType == char.class) {
                char[] arr = (char[]) array;
                for (int j = 0; j < len; j++) {
                    arr[j] = readFChar();
                }
                return arr;
            } else if (componentType == double.class) {
                double[] arr = (double[]) array;
                ensureReadAhead(arr.length * 8);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFDouble();
                }
                return arr;
            } else if (componentType == short.class) {
                short[] arr = (short[]) array;
                ensureReadAhead(arr.length * 2);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFShort();
                }
                return arr;
            } else if (componentType == float.class) {
                float[] arr = (float[]) array;
                ensureReadAhead(arr.length * 4);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFFloat();
                }
                return arr;
            } else if (componentType == boolean.class) {
                boolean[] arr = (boolean[]) array;
                ensureReadAhead(arr.length);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFByte() == 0 ? false : true;
                }
                return arr;
            } else {
                throw new RuntimeException("unexpected primitive type " + componentType.getName());
            }
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    // compressed version
    public void _readFIntArr(int len, int[] arr) throws IOException {
        ensureReadAhead(5 * len);
        final byte buf[] = input.buf;
        int count = input.pos;
        for (int j = 0; j < len; j++) {
            final byte head = buf[count++];
            // -128 = short byte, -127 == 4 byte
            if (head > -127 && head <= 127) {
                arr[j] = head;
                continue;
            }
            if (head == -128) {
                final int ch1 = (buf[count++]+256)&0xff;
                final int ch2 = (buf[count++]+256)&0xff;
                arr[j] = (short)((ch1 << 8) + (ch2 << 0));
                continue;
            } else {
                int ch1 = (buf[count++]+256)&0xff;
                int ch2 = (buf[count++]+256)&0xff;
                int ch3 = (buf[count++]+256)&0xff;
                int ch4 = (buf[count++]+256)&0xff;
                arr[j] = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
            }
        }
        input.pos = count;
    }

    @Override // uncompressed version
    public void readFIntArr(int len, int[] arr) throws IOException {
        int bytelen = arr.length * 4;
        ensureReadAhead(bytelen);
        int count = input.pos;
        final byte buf[] = input.buf;
        for (int j = 0; j < len; j++) {
            int ch1 = (buf[count++] + 256) & 0xff;
            int ch2 = (buf[count++] + 256) & 0xff;
            int ch3 = (buf[count++] + 256) & 0xff;
            int ch4 = (buf[count++] + 256) & 0xff;
            arr[j] = (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
        }
        input.pos+=bytelen;
    }

    public void readFLongArr(int len, long[] arr) throws IOException {
        int bytelen = arr.length * 8;
        ensureReadAhead(bytelen);
        int count = input.pos;
        final byte buf[] = input.buf;
        for (int j = 0; j < len; j++) {
            long ch8 = (buf[count++] + 256) & 0xff;
            long ch7 = (buf[count++] + 256) & 0xff;
            long ch6 = (buf[count++] + 256) & 0xff;
            long ch5 = (buf[count++] + 256) & 0xff;
            long ch4 = (buf[count++] + 256) & 0xff;
            long ch3 = (buf[count++] + 256) & 0xff;
            long ch2 = (buf[count++] + 256) & 0xff;
            long ch1 = (buf[count++] + 256) & 0xff;
            arr[j] = ((ch1 << 56) + (ch2 << 48) + (ch3 << 40) + (ch4 << 32) + (ch5 << 24) + (ch6 << 16) + (ch7 << 8) + (ch8 << 0));
        }
        input.pos+=bytelen;
    }

    @Override
    public int readFInt() throws IOException {
        ensureReadAhead(5);
        final byte head = readFByte();
        // -128 = short byte, -127 == 4 byte
        if (head > -127 && head <= 127) {
            return head;
        }
        if (head == -128) {
            int count = input.pos;
            final byte buf[] = input.buf;
            int ch1 = (buf[count++] + 256) & 0xff;
            int ch2 = (buf[count++] + 256) & 0xff;
            input.pos = count;
            return (short) ((ch2 << 8) + (ch1 << 0));
        } else {
            int count = input.pos;
            final byte buf[] = input.buf;
            int ch1 = (buf[count++] + 256) & 0xff;
            int ch2 = (buf[count++] + 256) & 0xff;
            int ch3 = (buf[count++] + 256) & 0xff;
            int ch4 = (buf[count++] + 256) & 0xff;
            input.pos = count;
            int res = (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
            return res;
        }
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
        input.ensureReadAhead(1);
        return input.buf[input.pos++];
    }

    @Override
    public final int readIntByte() throws IOException {
        input.ensureReadAhead(1);
        if ( input.isFullyRead() )
            return -1;
        return input.buf[input.pos++] & 0xff;
    }

    @Override
    public long readFLong() throws IOException {
        input.ensureReadAhead(9);
        byte head = readFByte();
        // -128 = short byte, -127 == 4 byte
        if (head > -126 && head <= 127) {
            return head;
        }
        if (head == -128) {
            int count = input.pos;
            final byte buf[] = input.buf;
            int ch1 = (buf[count++] + 256) & 0xff;
            int ch2 = (buf[count++] + 256) & 0xff;
            input.pos = count;
            return (short) ((ch2 << 8) + (ch1 << 0));
        } else if (head == -127) {
            int count = input.pos;
            final byte buf[] = input.buf;
            int ch1 = (buf[count++] + 256) & 0xff;
            int ch2 = (buf[count++] + 256) & 0xff;
            int ch3 = (buf[count++] + 256) & 0xff;
            int ch4 = (buf[count++] + 256) & 0xff;
            input.pos = count;
            int res = (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
            return res;
        } else {
            ensureReadAhead(8);
            int count = input.pos;
            final byte buf[] = input.buf;
            long ch8 = (buf[count++] + 256) & 0xff;
            long ch7 = (buf[count++] + 256) & 0xff;
            long ch6 = (buf[count++] + 256) & 0xff;
            long ch5 = (buf[count++] + 256) & 0xff;
            long ch4 = (buf[count++] + 256) & 0xff;
            long ch3 = (buf[count++] + 256) & 0xff;
            long ch2 = (buf[count++] + 256) & 0xff;
            long ch1 = (buf[count++] + 256) & 0xff;
            input.pos = count;
            return ((ch1 << 56) + (ch2 << 48) + (ch3 << 40) + (ch4 << 32) + (ch5 << 24) + (ch6 << 16) + (ch7 << 8) + (ch8 << 0));
        }
    }

    @Override
    public char readFChar() throws IOException {
        input.ensureReadAhead(3);
        char head = (char) ((readFByte() + 256) & 0xff);
        // -128 = short byte, -127 == 4 byte
        if (head >= 0 && head < 255) {
            return head;
        }
        int ch1 = readFByte() & 0xff;
        int ch2 = readFByte() & 0xff;
        return (char)((ch1 << 0) + (ch2 << 8));
    }


    @Override
    public short readFShort() throws IOException {
        input.ensureReadAhead(3);
        int head = readFByte() & 0xff;
        if (head >= 0 && head < 255) {
            return (short) head;
        }
        int ch1 = readFByte() & 0xff;
        int ch2 = readFByte() & 0xff;
        return (short)((ch1 << 0) + (ch2 << 8));
    }

    private char readPlainChar() throws IOException {
        input.ensureReadAhead(2);
        int count = input.pos;
        final byte buf[] = input.buf;
        int ch2 = (buf[count++] + 256) & 0xff;
        int ch1 = (buf[count++] + 256) & 0xff;
        input.pos = count;
        return (char) ((ch1 << 8) + (ch2 << 0));
    }

    private short readPlainShort() throws IOException {
        input.ensureReadAhead(2);
        int count = input.pos;
        final byte buf[] = input.buf;
        int ch1 = (buf[count++] + 256) & 0xff;
        int ch2 = (buf[count++] + 256) & 0xff;
        input.pos = count;
        return (short) ((ch2 << 8) + (ch1 << 0));
    }

    @Override
    public int readPlainInt() throws IOException {
        input.ensureReadAhead(4);
        int count = input.pos;
        final byte buf[] = input.buf;
        int ch1 = (buf[count++] + 256) & 0xff;
        int ch2 = (buf[count++] + 256) & 0xff;
        int ch3 = (buf[count++] + 256) & 0xff;
        int ch4 = (buf[count++] + 256) & 0xff;
        input.pos = count;
        int res = (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
        return res;
    }

    private long readPlainLong() throws IOException {
        input.ensureReadAhead(8);
        int count = input.pos;
        final byte buf[] = input.buf;
        long ch8 = (buf[count++] + 256) & 0xff;
        long ch7 = (buf[count++] + 256) & 0xff;
        long ch6 = (buf[count++] + 256) & 0xff;
        long ch5 = (buf[count++] + 256) & 0xff;
        long ch4 = (buf[count++] + 256) & 0xff;
        long ch3 = (buf[count++] + 256) & 0xff;
        long ch2 = (buf[count++] + 256) & 0xff;
        long ch1 = (buf[count++] + 256) & 0xff;
        input.pos = count;
        return ((ch1 << 56) + (ch2 << 48) + (ch3 << 40) + (ch4 << 32) + (ch5 << 24) + (ch6 << 16) + (ch7 << 8) + (ch8 << 0));
    }

    @Override
    public byte[] getBuffer() {
        return input.buf;
    }

    @Override
    public int getInputPos() {
        return input.pos;
    }

    @Override
    public void moveTo(int position) {
        input.pos = position;
    }

    @Override
    public void reset() {
        input.reset();
        clnames.clear();
    }

    @Override
    public void setInputStream(InputStream in) {
        if ( input == null )
            input = new FSTInputStream(in);
        else
            input.initFromStream(in);
        clnames.clear();
    }

    @Override
    public void resetToCopyOf(byte[] bytes, int off, int len) {
        input.reset();
        input.ensureCapacity(len);
        input.count = len;
        System.arraycopy(bytes, off, input.buf, 0, len);
        clnames.clear();
        input.byteBacked = true;
    }

    @Override
    public void resetWith(byte[] bytes, int len) {
        clnames.clear();
        input.reset();
        input.count = len;
        input.buf = bytes;
        input.pos = 0;
        input.byteBacked = true;
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
        input.pos+=n;
    }

    @Override
    public void readPlainBytes(byte[] b, int off, int len) {
        input.ensureReadAhead(len);
        System.arraycopy(input.buf,input.pos,b,off,len);
        input.pos += len;
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
    public int getObjectHeaderLen()
    {
        return -1;
    }

    @Override
    public void consumeEndMarker() {}

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
        input.pos -= bytes;
    }

    @Override
    public void readArrayEnd(FSTClazzInfo clzSerInfo) {
    }

    @Override
    public void readObjectEnd() {
    }

    @Override
    public Object coerceArrayElement(Class arrType, Object value) {
        return value;
    }


}

