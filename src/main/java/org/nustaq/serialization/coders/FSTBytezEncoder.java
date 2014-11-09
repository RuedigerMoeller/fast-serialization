package org.nustaq.serialization.coders;

import org.nustaq.offheap.bytez.BasicBytez;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTClazzNameRegistry;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTEncoder;
import org.nustaq.serialization.util.FSTOutputStream;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ruedi on 08.11.2014.
 *
 * Enables serialization to arbitrary byte oriented memory
 *
 * no compression is applied, so writeFxx == writePlainXX
 */
public class FSTBytezEncoder implements FSTEncoder {

    private final FSTConfiguration conf;

    private FSTClazzNameRegistry clnames;
    private BasicBytez buffout;
    private long pos;
    private byte[] ascStringCache;

    public FSTBytezEncoder(FSTConfiguration conf) {
        this.conf = conf;
        clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
        if ( clnames == null ) {
            clnames = new FSTClazzNameRegistry(conf.getClassRegistry(), conf);
        } else {
            clnames.clear();
        }
    }

    void writeFBooleanArr(boolean[] arr, int off, int len) throws IOException {
        ensureFree(len);
        buffout.setBoolean(pos,arr,off,len);
        pos += len;
    }

    public void writeFFloatArr(float[] arr, int off, int len) throws IOException {
        ensureFree(4*len);
        buffout.setFloat(pos, arr, off, len);
        pos += 4*len;
    }

    public void writeFDoubleArr(double[] arr, int off, int len) throws IOException {
        ensureFree(8*len);
        buffout.setDouble(pos,arr,off,len);
        pos += 8 * len;
    }

    public void writeFShortArr(short[] arr, int off, int len) throws IOException {
        ensureFree(2*len);
        buffout.setShort(pos, arr, off, len);
        pos += 2 * len;
    }

    public void writeFCharArr(char[] arr, int off, int len) throws IOException {
        ensureFree(2*len);
        buffout.setChar(pos,arr,off,len);
        pos += 2 * len;
    }

    void writeFIntArr(int[] arr, int off, int len) throws IOException {
        ensureFree(4*len);
        buffout.setInt(pos, arr, off, len);
        pos += 4 * len;
    }

    void writeFLongArr(long[] arr, int off, int len) throws IOException {
        ensureFree(8*len);
        buffout.setLong(pos, arr, off, len);
        pos += 8 * len;
    }

    /**
     * write prim array no len no tag
     *
     *
     * @param array
     * @throws IOException
     */
    public void writePrimitiveArray(Object array, int off, int len) throws IOException {
        Class<?> componentType = array.getClass().getComponentType();
        if ( componentType == byte.class ) {
            writeRawBytes((byte[]) array, off, len);
        } else
        if ( componentType == char.class ) {
            writeFCharArr((char[]) array, off, len);
        } else
        if ( componentType == short.class ) {
            writeFShortArr((short[]) array, off, len);
        } else
        if ( componentType == int.class ) {
            writeFIntArr((int[]) array, off, len);
        } else
        if ( componentType == double.class ) {
            writeFDoubleArr((double[]) array, off, len);
        } else
        if ( componentType == float.class ) {
            writeFFloatArr((float[]) array, off, len);
        } else
        if ( componentType == long.class ) {
            writeFLongArr((long[]) array, off, len);
        } else
        if ( componentType == boolean.class ) {
            writeFBooleanArr((boolean[]) array, off, len);
        } else {
            throw new RuntimeException("expected primitive array");
        }
    }


    /**
     * does not write length, just plain bytes
     *
     * @param array
     * @param length
     * @throws java.io.IOException
     */
    public void writeRawBytes(byte[] array, int start, int length) throws IOException {
        buffout.set(pos,array,start,length);
        pos += length;
    }

    @Override
    public void writeStringUTF(String str) throws IOException {
        final int strlen = str.length();

        writeFInt(strlen);
        long count = pos;
        for (int i=0; i<strlen; i++) {
            final char c = str.charAt(i);
            buffout.put(count++,(byte)c);
            if ( c >= 255) {
                buffout.put(count-1, (byte)255);
                buffout.put(count++, (byte) ((c >>> 0) & 0xFF));
                buffout.put(count++, (byte) ((c >>> 8) & 0xFF));
            }
        }
        pos = count;
    }

    /**
     * length < 127 !!!!!
     *
     * @param name
     * @throws java.io.IOException
     */
    void writeStringAsc(String name) throws IOException {
        int len = name.length();
        if ( len >= 127 ) {
            throw new RuntimeException("Ascii String too long");
        }
        writeFByte((byte) len);
        if (ascStringCache == null || ascStringCache.length < len)
            ascStringCache = new byte[len];
        name.getBytes(0, len, ascStringCache, 0);
        writeRawBytes(ascStringCache, 0, len);
    }

    @Override
    public void writeFShort(short c) throws IOException {
        writePlainShort(c);
    }

    public void writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo) {

    }

    public boolean writeTag(byte tag, Object info, long somValue, Object toWrite) throws IOException {
        writeFByte(tag);
        return false;
    }

    @Override
    public void writeFChar(char c) throws IOException {
        writePlainChar(c);
    }

    @Override
    public final void writeFByte(int v) throws IOException {
        ensureFree(1);
        buffout.put(pos++, (byte) v);
    }

    @Override
    public void writeFInt(int anInt) throws IOException {
        writePlainInt(anInt);
    }

    @Override
    public void writeFLong(long anInt) throws IOException {
        writePlainLong(anInt);
    }

    /**
     * Writes a 4 byte float.
     */
    @Override
    public void writeFFloat(float value) throws IOException {
        writePlainInt(Float.floatToIntBits(value));
    }

    @Override
    public void writeFDouble(double value) throws IOException {
        writePlainLong(Double.doubleToLongBits(value));
    }

    @Override
    public int getWritten() {
        return (int) pos;
    }

    /**
     * close and flush to underlying stream if present. The stream is also closed
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        conf.returnObject(clnames);
    }

    @Override
    public void reset(byte[] out) {
        throw new RuntimeException("not implemented");
//        buffout.reset(out); // fixme: classname clearing ?
    }

    @Override
    public void reset() {
        pos = 0;
        clnames.clear();
    }

    @Override
    public void skip(int i) {
        pos+=i;
    }

    /**
     * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
     * @param position
     * @param v
     */
    @Override
    public void writeInt32At(int position, int v) {
        try {
            ensureFree( position+4);
        } catch (IOException e) {
            FSTUtil.rethrow(e);
        }
        buffout.putInt(position,v);
    }

    /**
     * if output stream is null, just encode into a byte array
     * @param outstream
     */
    @Override
    public void setOutstream(OutputStream outstream) {
        throw new RuntimeException("not implemented");
    }

    /**
     * writes current buffer to underlying output and resets buffer.
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
//        buffout.flush();
    }

    @Override
    public void ensureFree(int bytes) throws IOException {
//        buffout.ensureFree(bytes);
    }

    @Override
    public byte[] getBuffer() {
        throw new RuntimeException("not implementable");
    }

    public void registerClass(Class possible) {
        clnames.registerClass(possible);
    }

    @Override
    public final void writeClass(Class cl) {
        try {
            clnames.encodeClass(this,cl);
        } catch ( IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }

    @Override
    public final void writeClass(FSTClazzInfo clInf) {
        try {
            clnames.encodeClass(this, clInf);
        } catch ( IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }


    private void writePlainLong(long v) throws IOException {
        ensureFree(8);
        buffout.putLong(pos,v);
        pos += 8;
    }

    private void writePlainShort(int v) throws IOException {
        ensureFree(2);
        buffout.putShort(pos, (short) v);
        pos += 2;
    }

    private void writePlainChar(int v) throws IOException {
        ensureFree(2);
        buffout.putChar(pos, (char) v);
        pos += 2;
    }

    private void writePlainInt(int v) throws IOException {
        ensureFree(4);
        buffout.putInt(pos, (short) v);
        pos += 4;
    }

    public void externalEnd(FSTClazzInfo clz) {
    }

    @Override
    public boolean isWritingAttributes() {
        return false;
    }

    public boolean isPrimitiveArray(Object array, Class<?> componentType) {
        return componentType.isPrimitive();
    }

    public boolean isTagMultiDimSubArrays() {
        return false;
    }

    @Override
    public void writeVersionTag(int version) throws IOException {
        writeFByte(version);
    }

}

