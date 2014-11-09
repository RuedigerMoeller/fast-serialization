package org.nustaq.serialization.coders;

import org.nustaq.offheap.bytez.BasicBytez;
import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTClazzNameRegistry;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTDecoder;
import org.nustaq.serialization.util.FSTInputStream;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ruedi on 09.11.2014.
 *
 * no value compression is applied
 *
 */
public class FSTBytezDecoder  implements FSTDecoder {

    private BasicBytez input;
    HeapBytez ascStringCache;
    FSTConfiguration conf;
    public FSTClazzNameRegistry clnames;
    long pos;

    public FSTBytezDecoder(FSTConfiguration conf) {
        this.conf = conf;
        clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
        if (clnames == null) {
            clnames = new FSTClazzNameRegistry(conf.getClassRegistry(), conf);
        } else {
            clnames.clear();
        }
    }

    public void ensureReadAhead(int bytes) {

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
        char[] charBuf = getCharBuf(len * 3);
        ensureReadAhead(len * 3);
        long count = pos;
        int chcount = 0;
        for (int i = 0; i < len; i++) {
            char head = (char) ((input.get(count++) + 256) & 0xff);
            if (head < 255) {
                charBuf[chcount++] = head;
            } else {
                int ch1 = ((input.get(count++) + 256) & 0xff);
                int ch2 = ((input.get(count++) + 256) & 0xff);
                charBuf[chcount++] = (char) ((ch1 << 0) + (ch2 << 8));
            }
        }
        pos = count;
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
        int len = readFByte();
        if (ascStringCache == null || ascStringCache.length() < len)
            ascStringCache = new HeapBytez(new byte[len]);
        ensureReadAhead(len);
//        System.arraycopy(input.buf, input.pos, ascStringCache, 0, len);
        input.copyTo(ascStringCache, 0, pos, len);
        pos += len;
        return new String(ascStringCache.getBase(), 0, 0, len);
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
        if (componentType == byte.class) {
            byte arr[] = (byte[]) array;
            input.getArr(pos, arr, 0, len);
            pos += len;
            return arr;
        } else if (componentType == char.class) {
            char[] arr = (char[]) array;
            input.getCharArr(pos, arr, 0, len);
            pos += len * 2;
            return arr;
        } else if (componentType == short.class) {
            short[] arr = (short[]) array;
            input.getShortArr(pos, arr, 0, len);
            pos += len * 2;
            return arr;
        } else if (componentType == int.class) {
            int[] arr = (int[]) array;
            input.getIntArr(pos, arr, 0, len);
            pos += len * 4;
            return arr;
        } else if (componentType == float.class) {
            float[] arr = (float[]) array;
            input.getFloatArr(pos, arr, 0, len);
            pos += len * 4;
            return arr;
        } else if (componentType == double.class) {
            double[] arr = (double[]) array;
            input.getDoubleArr(pos, arr, 0, len);
            pos += len * 8;
            return arr;
        } else if (componentType == long.class) {
            long[] arr = (long[]) array;
            input.getLongArr(pos, arr, 0, len);
            pos += len * 8;
            return arr;
        } else if (componentType == boolean.class) {
            boolean[] arr = (boolean[]) array;
            input.getBooleanArr(pos, arr, 0, len);
            pos += len * 8;
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
        throw new RuntimeException("not implemented");
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
    }

    @Override
    public void setInputStream(InputStream in) {
        throw new RuntimeException("not implementable");
    }

    @Override
    public void resetToCopyOf(byte[] bytes, int off, int len) {
        throw new RuntimeException("not implementable");
    }

    @Override
    public void resetWith(byte[] bytes, int len) {
        throw new RuntimeException("not implementable");
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
        input.set(pos, b, off, len);
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

}
