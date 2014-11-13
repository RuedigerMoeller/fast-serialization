package org.nustaq.serialization.coders;

import org.nustaq.offheap.bytez.BasicBytez;
import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTClazzNameRegistry;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTEncoder;
import org.nustaq.serialization.simpleapi.FSTBufferTooSmallException;
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
    OutputStream outStream;
    boolean autoResize = true;

    public FSTBytezEncoder(FSTConfiguration conf, BasicBytez base) {
        this.conf = conf;
        buffout = base;
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
        ensureFree((int) (pos+length));
        buffout.set(pos,array,start,length);
        pos += length;
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

    @Override
    public void writeStringUTF(String str) throws IOException {
        final int strlen = str.length();
        writeFInt(strlen);
        ensureFree(strlen*2);
        char c[] = getCharBuf(strlen);
        str.getChars(0,strlen,c,0);
        buffout.setChar(pos,c,0,strlen);
        pos += strlen*2;
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
        this.outStream = outstream;
    }

    /**
     * writes current buffer to underlying output and resets buffer.
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        if ( outStream != null )
            outStream.write(getBuffer(), 0, (int) pos);
        pos = 0;
    }

    @Override
    public void ensureFree(int bytes) throws IOException {
        if ( buffout.length() <= pos+bytes) {
            if ( autoResize ) {
                BasicBytez newbytez = buffout.newInstance(Math.max(pos + bytes, buffout.length() * 2));
                buffout.copyTo(newbytez, 0, 0, pos);
                // debug
                //            for ( int i = 0; i < pos; i++) {
                //                if ( buffout.get(i) != newbytez.get(i) ) {
                //                    throw new RuntimeException("error");
                //                }
                //            }
                buffout = newbytez;
            } else {
                throw FSTBufferTooSmallException.Instance;
            }
        }
    }

    // default is true
    public boolean isAutoResize() {
        return autoResize;
    }

    public void setAutoResize(boolean autoResize) {
        this.autoResize = autoResize;
    }

    @Override
    public byte[] getBuffer() {
        if (isPlainBAAccessible()) {
            return ((HeapBytez) buffout).asByteArray();
        }
        byte res[] = new byte[(int) pos];
        buffout.getArr(0,res,0, (int) pos);
        return res;
    }

    protected boolean isPlainBAAccessible() {
        return buffout.getClass() == HeapBytez.class && ((HeapBytez) buffout).getOffsetIndex() == 0;
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
        buffout.putInt(pos, v);
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

    @Override
    public boolean isByteArrayBased() {
        return false || isPlainBAAccessible();
    }

}

