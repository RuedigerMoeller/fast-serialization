package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.util.FSTOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public class FSTEnccoder {

    private final FSTConfiguration conf;
    private FSTOutputStream buffout;
    private byte[] ascStringCache;

    public FSTEnccoder(FSTConfiguration conf) {
        this.conf = conf;
    }

    public void writeFBooleanArr(boolean[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++)
            writeFByte(arr[i] ? 1 : 0);
    }

    public void writeFLongArr(long[] array) throws IOException {
        long[] arr = (long[]) array;
        for (int i = 0; i < arr.length; i++)
            writeFLong(arr[i]);
    }

    public void writeFFloatArr(float[] array) throws IOException {
        float[] arr = (float[]) array;
        for (int i = 0; i < array.length; i++)
            writeFFloat(arr[i]);
    }

    public void writeFDoubleArr(double[] array) throws IOException {
        double[] arr = array;
        for (int i = 0; i < arr.length; i++)
            writeFDouble(arr[i]);
    }

    public void writeFShortArr(short[] array) throws IOException {
        short[] arr = array;
        for (int i = 0; i < array.length; i++)
            writeFShort(arr[i]);
    }

    public void writeFCharArr(char[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++)
            writeFChar(arr[i]);
    }

    public void writeFByteArr(byte[] array) throws IOException {
        writeFByteArr(array, 0, array.length);
    }

    /**
     * does not write length, just plain bytes
     *
     * @param array
     * @param length
     * @throws java.io.IOException
     */
    public void writeFByteArr(byte[] array, int start, int length) throws IOException {
        buffout.ensureFree(length);
        System.arraycopy(array, start, buffout.buf, buffout.pos, length);
        buffout.pos += length;
    }

    public void writeStringUTF(String str) throws IOException {
        final int strlen = str.length();

        writeFInt(strlen);
        buffout.ensureFree(strlen * 3);
        final byte[] bytearr = buffout.buf;
        int count = buffout.pos;

        for (int i = 0; i < strlen; i++) {
            final char c = str.charAt(i);
            bytearr[count++] = (byte) c;
            if (c >= 255) {
                bytearr[count - 1] = (byte) 255;
                bytearr[count++] = (byte) ((c >>> 8) & 0xFF);
                bytearr[count++] = (byte) ((c >>> 0) & 0xFF);
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

    public void writeFShort(short c) throws IOException {
        if (c < 255 && c >= 0) {
            writeFByte(c);
        } else {
            writeFByte(255);
            writeFShort(c);
        }
    }

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
            buf[count++] = (byte) ((c >>> 8) & 0xFF);
            buf[count++] = (byte) ((c >>> 0) & 0xFF);
            buffout.pos += 3;
        }
    }

    public final void writeFByte(int v) throws IOException {
        buffout.ensureFree(1);
        buffout.buf[buffout.pos++] = (byte) v;
    }

    public void writeFIntThin(int v[]) throws IOException {
        final int length = v.length;
        for (int i = 0; i < length; i++) {
            final int anInt = v[i];
            if (anInt != 0) {
                writeFInt(i);
                writeFInt(anInt);
            }
        }
        writeFInt(length); // stop marker
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
                buf[count++] = -128;
                buf[count++] = (byte) ((anInt >>> 8) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            } else {
                buf[count++] = -127;
                buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 8) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            }
        }
        buffout.pos = count;
    }

    public void writeFInt(int anInt) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if (anInt > -127 && anInt <= 127) {
            if (buffout.buf.length <= buffout.pos + 1) {
                buffout.ensureFree(1);
            }
            buffout.buf[buffout.pos++] = (byte) anInt;
        } else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
            if (buffout.buf.length <= buffout.pos + 2) {
                buffout.ensureFree(3);
            }
            final byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = -128;
            buf[count++] = (byte) ((anInt >>> 8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buffout.pos += 3;
        } else {
            buffout.ensureFree(5);
            final byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = -127;
            buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buffout.pos = count;
        }
    }

    /**
     * Writes a 4 byte float.
     */
    public void writeFFloat(float value) throws IOException {
        writeFInt(Float.floatToIntBits(value));
    }

    public void writeFDouble(double value) throws IOException {
        writeFLong(Double.doubleToLongBits(value));
    }

    public void writeFLong(long anInt) throws IOException {
// -128 = short byte, -127 == 4 byte
        if (anInt > -126 && anInt <= 127) {
            writeFByte((int) anInt);
        } else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
            writeFByte(-128);
            writeFShort((short)anInt);
        } else if (anInt >= Integer.MIN_VALUE && anInt <= Integer.MAX_VALUE) {
            writeFByte(-127);
            writeFInt((int) anInt);
        } else {
            writeFByte(-126);
            writeFLong(anInt);
        }
    }

    public int getWritten() {
        return buffout.pos;
    }

    public void setWritten(int written) {
        this.buffout.pos = written;
    }

    /**
     * close and flush to underlying stream if present. The stream is also closed
     * @throws IOException
     */
    public void close() throws IOException {
        buffout.close();
    }

    public void reset() {
        buffout.reset();
    }

    public void skip(int i) {
        buffout.pos+=i;
    }

    /**
     * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
     * @param position
     * @param v
     */
    public void writeInt32At(int position, int v) {
        buffout.buf[position] = (byte) ((v >>> 24) & 0xFF);
        buffout.buf[position+1] = (byte) ((v >>> 16) & 0xFF);
        buffout.buf[position+2] = (byte) ((v >>>  8) & 0xFF);
        buffout.buf[position+3] = (byte) ((v >>> 0) & 0xFF);
    }

    /**
     * if output stream is null, just encode into a byte array 
     * @param outstream
     */
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
    public void flush() throws IOException {
        buffout.flush();
    }

    public void ensureFree(int bytes) throws IOException {
        buffout.ensureFree(bytes);
    }

    public byte[] getBuffer() {
        return buffout.buf;
    }

    public void reset(byte[] out) {
        buffout.reset(out);
    }
}