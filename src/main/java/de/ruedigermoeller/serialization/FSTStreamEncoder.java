package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.util.FSTOutputStream;
import de.ruedigermoeller.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

public class FSTStreamEncoder implements FSTEncoder {

    private final FSTConfiguration conf;
    
    private FSTClazzNameRegistry clnames;
    private FSTOutputStream buffout;
    private byte[] ascStringCache;

    public FSTStreamEncoder(FSTConfiguration conf) {
        this.conf = conf;
        clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
        if ( clnames == null ) {
            clnames = new FSTClazzNameRegistry(conf.getClassRegistry(), conf);
        } else {
            clnames.clear();
        }
    }

    public void writeFBooleanArr(boolean[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++)
            writeFByte(arr[i] ? 1 : 0);
    }

    public void writeFLongArr(long[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++)
            writeFLong(arr[i]);
    }

    public void writeFFloatArr(float[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++)
            writeFFloat(arr[i]);
    }

    public void writeFDoubleArr(double[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++)
            writeFDouble(arr[i]);
    }

    public void writeFShortArr(short[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++)
            writeFShort(arr[i]);
    }

    public void writeFCharArr(char[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++)
            writeFChar(arr[i]);
    }

    @Override
    public void writeFByteArr(byte[] array) throws IOException {
        writeFByteArr(array, 0, array.length);
    }

    /**
     * write len + array
     * @param array
     * @throws IOException
     */
    public void writePrimitiveArray(Object array) throws IOException {
        final int len = Array.getLength(array);
        writeFInt(len);
        Class<?> componentType = array.getClass().getComponentType();
        if ( componentType == byte.class ) {
            writeFByteArr((byte[]) array, 0, len);
        } else
        if ( componentType == char.class ) {
            writeFCharArr((char[]) array);
        } else
        if ( componentType == short.class ) {
            writeFShortArr((short[]) array);
        } else
        if ( componentType == int.class ) {
            writeFIntArr((int[]) array);
        } else
        if ( componentType == double.class ) {
            writeFDoubleArr((double[]) array);
        } else
        if ( componentType == float.class ) {
            writeFFloatArr((float[]) array);
        } else
        if ( componentType == long.class ) {
            writeFLongArr((long[]) array);
        } else
        if ( componentType == boolean.class ) {
            writeFBooleanArr((boolean[]) array);
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
    @Override
    public void writeFByteArr(byte[] array, int start, int length) throws IOException {
        buffout.ensureFree(length);
        System.arraycopy(array, start, buffout.buf, buffout.pos, length);
        buffout.pos += length;
    }

    @Override
    public void writeStringUTF(String str) throws IOException {
        final int strlen = str.length();

        writeFInt(strlen);
        buffout.ensureFree(strlen * 3);
        final byte[] bytearr = buffout.buf;
        int count = buffout.pos;

        for (int i = 0; i < strlen; i++) {
            final char c = str.charAt(i);
            // inlined
            bytearr[count++] = (byte) c;
            if (c >= 255) {
                bytearr[count - 1] = (byte) 255;
                bytearr[count++] = (byte) (c >>> 0);
                bytearr[count++] = (byte) (c >>> 8);
            }
        }
        buffout.pos = count;
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
        buffout.ensureFree(len);
        if (ascStringCache == null || ascStringCache.length < len)
            ascStringCache = new byte[len];
        name.getBytes(0, len, ascStringCache, 0);
        writeFByteArr(ascStringCache, 0, len);
    }

    @Override
    public void writeFShort(short c) throws IOException {
        if (c < 255 && c >= 0) {
            writeFByte(c);
        } else {
            writeFByte(255);
            writePlainShort(c);
        }
    }

    @Override
    public void writeFChar(char c) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if (c < 255 && c >= 0) {
            buffout.ensureFree(1);
            buffout.buf[buffout.pos++] = (byte) c;
        } else {
            buffout.ensureFree(3);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) 255;
            buf[count++] = (byte) (c >>> 0);
            buf[count++] = (byte) (c >>> 8);
            buffout.pos += 3;
        }
    }

    @Override
    public final void writeFByte(int v) throws IOException {
        buffout.ensureFree(1);
        buffout.buf[buffout.pos++] = (byte) v;
    }

    public void writeFIntArr(int v[]) throws IOException {
        final int free = 5 * v.length;
        buffout.ensureFree(free);
        final byte[] buf = buffout.buf;
        int count = buffout.pos;
        for (int i = 0; i < v.length; i++) {
            final int anInt = v[i];
            if (anInt > -127 && anInt <= 127) {
                buffout.buf[count++] = (byte) anInt;
            } else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
                writeFByte(-128);
                writePlainShort(anInt);
            } else {
                writeFByte(-127);
                writePlainInt(anInt);
            }
        }
        buffout.pos = count;
    }

    @Override
    public void writeFInt(int anInt) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if (anInt > -127 && anInt <= 127) {
            if (buffout.buf.length <= buffout.pos + 1) {
                buffout.ensureFree(1);
            }
            buffout.buf[buffout.pos++] = (byte) anInt;
        } else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
            writeFByte(-128);
            writePlainShort(anInt);
        } else {
            writeFByte(-127);
            writePlainInt(anInt);
        }
    }

    @Override
    public void writeFLong(long anInt) throws IOException {
// -128 = short byte, -127 == 4 byte
        if (anInt > -126 && anInt <= 127) {
            writeFByte((int) anInt);
        } else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
            writeFByte(-128);
            writePlainShort((short)anInt);
        } else if (anInt >= Integer.MIN_VALUE && anInt <= Integer.MAX_VALUE) {
            writeFByte(-127);
            writePlainInt((int) anInt);
        } else {
            writeFByte(-126);
            writePlainLong(anInt);
        }
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
        return buffout.pos;
    }

    @Override
    public void setWritten(int written) {
        this.buffout.pos = written;
    }

    /**
     * close and flush to underlying stream if present. The stream is also closed
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        buffout.close();
        conf.returnObject(clnames);
    }

    @Override
    public void reset(byte[] out) {
        buffout.reset(out); // fixme: classname clearing ?
    }

    @Override
    public void reset() {
        buffout.reset();
        clnames.clear();
    }

    @Override
    public void skip(int i) {
        buffout.pos+=i;
    }

    /**
     * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
     * @param position
     * @param v
     */
    @Override
    public void writeInt32At(int position, int v) {
        buffout.buf[position] = (byte)  (v >>> 0);
        buffout.buf[position+1] = (byte) (v >>> 8);
        buffout.buf[position+2] = (byte) (v >>> 16);
        buffout.buf[position+3] = (byte) (v >>> 24);
    }

    /**
     * if output stream is null, just encode into a byte array 
     * @param outstream
     */
    @Override
    public void setOutstream(OutputStream outstream) {
        if ( buffout == null ) 
        {
            // try reuse
            buffout = (FSTOutputStream) conf.getCachedObject(FSTOutputStream.class);
            if ( buffout == null ) // if fail, alloc
                buffout = new FSTOutputStream(1000, outstream);
            else
                buffout.reset(); // reset resued fstoutput
        }
        if ( outstream == null )
            buffout.setOutstream(buffout);
        else
            buffout.setOutstream(outstream);
    }
    
    /**
     * resets stream (positions are lost)
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        buffout.flush();
    }

    @Override
    public void ensureFree(int bytes) throws IOException {
        buffout.ensureFree(bytes);
    }

    @Override
    public byte[] getBuffer() {
        return buffout.buf;
    }

    public void registerClass(Class possible) {
        clnames.registerClass(possible);
    }

    @Override
    public final void writeClass(FSTObjectOutput out, Class cl) {
        try {
            clnames.encodeClass(out,cl);
        } catch ( IOException e) {
            FSTUtil.rethrow(e);
        }
    }

    @Override
    public final void writeClass(FSTObjectOutput out,FSTClazzInfo clInf) {
        try {
            clnames.encodeClass(out, clInf);
        } catch ( IOException e) {
            FSTUtil.rethrow(e);
        }
    }
    

    private void writePlainLong(long v) throws IOException {
        buffout.ensureFree(8);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) (v >>> 0);
        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) (v >>> 16);
        buf[count++] = (byte) (v >>> 24);
        buf[count++] = (byte) (v >>> 32);
        buf[count++] = (byte) (v >>> 40);
        buf[count++] = (byte) (v >>> 48);
        buf[count++] = (byte) (v >>> 56);
        buffout.pos += 8;
    }

    private void writePlainShort(int v) throws IOException {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) (v >>> 0);
        buf[count++] = (byte) (v >>> 8);
        buffout.pos += 2;
    }

    private void writePlainChar(int v) throws IOException {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) (v >>> 0);
        buf[count++] = (byte) (v >>> 8);
        buffout.pos += 2;
    }

    private void writePlainInt(int v) throws IOException {
        buffout.ensureFree(4);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) (v >>> 0);
        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) (v >>> 16);
        buf[count++] = (byte) (v >>> 24);
        buffout.pos += 4;
    }

}

