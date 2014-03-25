package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.util.FSTOutputStream;

import java.io.IOException;

public class FSTCodec {
    
    private FSTOutputStream buffout;
    private byte[] ascStringCache;
    private int written;

    public FSTCodec() {
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

    public void writeCCharArr(char[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++)
            writeCChar(arr[i]);
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
        written += length;
        buffout.pos += length;
    }

    public void writeStringUTF(String str) throws IOException {
        final int strlen = str.length();

        writeCInt(strlen);
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
        written += count - buffout.pos;
        buffout.pos = count;
    }

    public void writeCShort(short c) throws IOException {
        if (c < 255 && c >= 0) {
            writeFByte(c);
        } else {
            writeFByte(255);
            writeFShort(c);
        }
    }

    public void writeCChar(char c) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if (c < 255 && c >= 0) {
            buffout.ensureFree(1);
            buffout.buf[buffout.pos++] = (byte) c;
            written++;
        } else {
            buffout.ensureFree(3);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) 255;
            buf[count++] = (byte) ((c >>> 8) & 0xFF);
            buf[count++] = (byte) ((c >>> 0) & 0xFF);
            buffout.pos += 3;
            written += 3;
        }
    }

    public void writeFChar(int v) throws IOException {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buf[count++] = (byte) ((v >>> 8) & 0xFF);
        buffout.pos += 2;
        written += 2;
    }

    public void writeFShort(int v) throws IOException {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>> 8) & 0xFF);
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buffout.pos += 2;
        written += 2;
    }

    public final void writeFByte(int v) throws IOException {
        buffout.ensureFree(1);
        buffout.buf[buffout.pos++] = (byte) v;
        written++;
    }

    public void writeFInt(int v) throws IOException {
        buffout.ensureFree(4);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buf[count++] = (byte) ((v >>> 8) & 0xFF);
        buf[count++] = (byte) ((v >>> 16) & 0xFF);
        buf[count++] = (byte) ((v >>> 24) & 0xFF);
        buffout.pos += 4;
        written += 4;
    }

    public void writeFLong(long v) throws IOException {
        buffout.ensureFree(8);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buf[count++] = (byte) ((v >>> 8) & 0xFF);
        buf[count++] = (byte) ((v >>> 16) & 0xFF);
        buf[count++] = (byte) ((v >>> 24) & 0xFF);
        buf[count++] = (byte) (v >>> 32);
        buf[count++] = (byte) (v >>> 40);
        buf[count++] = (byte) (v >>> 48);
        buf[count++] = (byte) (v >>> 56);
        buffout.pos += 8;
        written += 8;
    }

    public void writeFIntThin(int v[]) throws IOException {
        final int length = v.length;
        for (int i = 0; i < length; i++) {
            final int anInt = v[i];
            if (anInt != 0) {
                writeCInt(i);
                writeCInt(anInt);
            }
        }
        writeCInt(length); // stop marker
    }

    public void writeFIntArr(int v[]) throws IOException {
        final int free = 4 * v.length;
        buffout.ensureFree(free);
        final byte[] buf = buffout.buf;
        int count = buffout.pos;
        for (int i = 0; i < v.length; i++) {
            final int anInt = v[i];
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
        }
        written += count - buffout.pos;
        buffout.pos = count;
    }

    public void writeCIntArr(int v[]) throws IOException {
        final int free = 5 * v.length;
        buffout.ensureFree(free);
        final byte[] buf = buffout.buf;
        int count = buffout.pos;
        for (int i = 0; i < v.length; i++) {
            final int anInt = v[i];
            if (anInt > -127 && anInt <= 127) {
                buffout.buf[count++] = (byte) anInt;
                written++;
            } else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
                buf[count++] = -128;
                buf[count++] = (byte) ((anInt >>> 8) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
                written+=3;
            } else {
                buf[count++] = -127;
                buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 8) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
                written+=5;
            }
        }
        buffout.pos = count;
    }

    public void writeCInt(int anInt) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if (anInt > -127 && anInt <= 127) {
            if (buffout.buf.length <= buffout.pos + 1) {
                buffout.ensureFree(1);
            }
            buffout.buf[buffout.pos++] = (byte) anInt;
            written++;
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
            written += 3;
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
            written += 5;
        }
    }

    /**
     * Writes a 4 byte float.
     */
    public void writeCFloat(float value) throws IOException {
        writeFInt(Float.floatToIntBits(value));
    }

    /**
     * Writes a 4 byte float.
     */
    public void writeFFloat(float value) throws IOException {
        writeFInt(Float.floatToIntBits(value));
    }

    public void writeCDouble(double value) throws IOException {
        writeFLong(Double.doubleToLongBits(value));
    }

    public void writeFDouble(double value) throws IOException {
        writeFLong(Double.doubleToLongBits(value));
    }

    public void writeCLong(long anInt) throws IOException {
// -128 = short byte, -127 == 4 byte
        if (anInt > -126 && anInt <= 127) {
            writeFByte((int) anInt);
        } else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
            writeFByte(-128);
            writeFShort((int) anInt);
        } else if (anInt >= Integer.MIN_VALUE && anInt <= Integer.MAX_VALUE) {
            writeFByte(-127);
            writeFInt((int) anInt);
        } else {
            writeFByte(-126);
            writeFLong(anInt);
        }
    }

    public int getWritten() {
        return written;
    }

    public void setWritten(int written) {
        this.written = written;
    }

    /**
     * length < 127 !!!!!
     *
     * @param name
     * @throws java.io.IOException
     */
    void writeStringAsc(String name) throws IOException {
        int len = name.length();
        writeFByte((byte) len);
        buffout.ensureFree(len);
        if (ascStringCache == null || ascStringCache.length < len)
            ascStringCache = new byte[len];
        name.getBytes(0, len, ascStringCache, 0);
        writeFByteArr(ascStringCache, 0, len);
    }

    public void close() throws IOException {
        buffout.close();
    }

    public void reset() {
        buffout.reset();
        written = 0;
    }

    public void skip(int i) {
        buffout.pos+=i;
        written+=i;
    }

    public void setBuffout( FSTOutputStream fout ) {
        buffout = fout;
    }
    /**
     * used to write uncompressed int at a (eventually recent) position
     * @param conditional
     * @param v
     */
    public void writeInt32At(int conditional, int v) {
        buffout.buf[conditional] = (byte) ((v >>> 24) & 0xFF);
        buffout.buf[conditional+1] = (byte) ((v >>> 16) & 0xFF);
        buffout.buf[conditional+2] = (byte) ((v >>>  8) & 0xFF);
        buffout.buf[conditional+3] = (byte) ((v >>> 0) & 0xFF);
    }

    public FSTOutputStream getBuffout() {
        return buffout;
    }

    public void flush() throws IOException {
        buffout.flush();
    }

    public void ensureFree(int bytes) throws IOException {
        buffout.ensureFree(bytes);
    }
}