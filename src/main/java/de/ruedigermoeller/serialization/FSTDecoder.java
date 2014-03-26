package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.util.FSTInputStream;
import de.ruedigermoeller.serialization.util.FSTUtil;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;

public class FSTDecoder {

    FSTInputStream input;
    byte ascStringCache[];
    FSTConfiguration conf;

    public FSTDecoder(FSTConfiguration conf) {
        this.conf = conf;
    }

    final void ensureReadAhead(int bytes) throws IOException {
        // currently complete object is read ahead
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
        int len = readCInt();
        char[] charBuf = getCharBuf(len * 3);
        ensureReadAhead(len * 3);
        byte buf[] = input.buf;
        int count = input.pos;
        int chcount = 0;
        for (int i = 0; i < len; i++) {
            char head = (char) ((buf[count++] + 256) & 0xff);
            if (head < 255) {
                charBuf[chcount++] = head;
            } else {
                int ch1 = ((buf[count++] + 256) & 0xff);
                int ch2 = ((buf[count++] + 256) & 0xff);
                charBuf[chcount++] = (char) ((ch1 << 8) + (ch2 << 0));
            }
        }
        input.pos = count;
        return new String(charBuf, 0, chcount);
    }

    /**
     * len < 127 !!!!!
     *
     * @return
     * @throws java.io.IOException
     */
    public String readStringAsc() throws IOException {
        int len = readFByte();
        if (ascStringCache == null || ascStringCache.length < len)
            ascStringCache = new byte[len];
        ensureReadAhead(len);
        System.arraycopy(input.buf, input.pos, ascStringCache, 0, len);
        input.pos += len;
        return new String(ascStringCache, 0, 0, len);
    }

    /**
     * utility for fast-cast
     *
     * @param componentType
     * @param len
     * @return
     */
    public Object readFPrimitiveArray(Class componentType, int len) {
        try {
            Object array = Array.newInstance(componentType, len);
            if (componentType == byte.class) {
                byte[] arr = (byte[]) array;
                ensureReadAhead(arr.length); // fixme: move this stuff to the stream !
                System.arraycopy(input.buf,input.pos,arr,0,len);
                return arr;
            } else if (componentType == char.class) {
                char[] arr = (char[]) array;
                for (int j = 0; j < len; j++) {
                    arr[j] = readCChar();
                }
                return arr;
            } else if (componentType == short.class) {
                short[] arr = (short[]) array;
                ensureReadAhead(arr.length * 2);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFShort();
                }
                return arr;
            } else if (componentType == int.class) {
                final int[] arr = (int[]) array;
                readFIntArr(len, arr);
                return arr;
            } else if (componentType == float.class) {
                float[] arr = (float[]) array;
                ensureReadAhead(arr.length * 4);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFFloat();
                }
                return arr;
            } else if (componentType == double.class) {
                double[] arr = (double[]) array;
                ensureReadAhead(arr.length * 8);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFDouble();
                }
                return arr;
            } else if (componentType == long.class) {
                long[] arr = (long[]) array;
                ensureReadAhead(arr.length * 8);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFLong();
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
                throw new RuntimeException("unexpected primitive type " + componentType);
            }
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public final int readFInt() throws IOException {
        ensureReadAhead(4);
        int count = input.pos;
        final byte buf[] = input.buf;
        int ch4 = (buf[count++] + 256) & 0xff;
        int ch3 = (buf[count++] + 256) & 0xff;
        int ch2 = (buf[count++] + 256) & 0xff;
        int ch1 = (buf[count++] + 256) & 0xff;
        input.pos = count;
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public void readFIntArr(int len, int[] arr) throws IOException {
        ensureReadAhead(4 * len);
        final byte buf[] = input.buf;
        int count = input.pos;
        for (int j = 0; j < len; j++) {
            int ch4 = (buf[count++] + 256) & 0xff;
            int ch3 = (buf[count++] + 256) & 0xff;
            int ch2 = (buf[count++] + 256) & 0xff;
            int ch1 = (buf[count++] + 256) & 0xff;
            arr[j] = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }
        input.pos = count;
    }

    void readCIntArr(int len, int[] arr) throws IOException {
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
                final int ch1 = (buf[count++] + 256) & 0xff;
                final int ch2 = (buf[count++] + 256) & 0xff;
                arr[j] = (short) ((ch1 << 8) + (ch2 << 0));
                continue;
            } else {
                int ch1 = (buf[count++] + 256) & 0xff;
                int ch2 = (buf[count++] + 256) & 0xff;
                int ch3 = (buf[count++] + 256) & 0xff;
                int ch4 = (buf[count++] + 256) & 0xff;
                arr[j] = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
            }
        }
        input.pos = count;
    }

    public final int readCInt() throws IOException {
        ensureReadAhead(5);
        final byte buf[] = input.buf;
        int count = input.pos;
        final byte head = buf[count++];
        // -128 = short byte, -127 == 4 byte
        if (head > -127 && head <= 127) {
            input.pos = count;
            return head;
        }
        if (head == -128) {
            final int ch1 = (buf[count++] + 256) & 0xff;
            final int ch2 = (buf[count++] + 256) & 0xff;
            input.pos = count;
            return (short) ((ch1 << 8) + (ch2 << 0));
        } else {
            int ch1 = (buf[count++] + 256) & 0xff;
            int ch2 = (buf[count++] + 256) & 0xff;
            int ch3 = (buf[count++] + 256) & 0xff;
            int ch4 = (buf[count++] + 256) & 0xff;
            input.pos = count;
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }
    }

    public double readFDouble() throws IOException {
        return Double.longBitsToDouble(readFLong());
    }

    public final byte readFByte() throws IOException {
        ensureReadAhead(1);
        return input.buf[input.pos++];
    }

    public long readFLong() throws IOException {
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

    public long readCLong() throws IOException {
        ensureReadAhead(9);
        byte head = readFByte();
        // -128 = short byte, -127 == 4 byte
        if (head > -126 && head <= 127) {
            return head;
        }
        if (head == -128) {
            int ch1 = readFByte();
            int ch2 = readFByte();
            return (short)((ch1 << 8) + (ch2 << 0));
        } else if (head == -127) {
            ensureReadAhead(4);
            int count = input.pos;
            final byte buf[] = input.buf;
            int ch4 = (buf[count++] + 256) & 0xff;
            int ch3 = (buf[count++] + 256) & 0xff;
            int ch2 = (buf[count++] + 256) & 0xff;
            int ch1 = (buf[count++] + 256) & 0xff;
            input.pos = count;
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
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

    public char readFChar() throws IOException {
        ensureReadAhead(2);
        int count = input.pos;
        final byte buf[] = input.buf;
        int ch2 = (buf[count++] + 256) & 0xff;
        int ch1 = (buf[count++] + 256) & 0xff;
        input.pos = count;
        return (char) ((ch1 << 8) + (ch2 << 0));
    }

    public char readCChar() throws IOException {
        ensureReadAhead(3);
        char head = (char) ((readFByte() + 256) & 0xff);
        // -128 = short byte, -127 == 4 byte
        if (head >= 0 && head < 255) {
            return head;
        }
        int ch1 = readFByte();
        int ch2 = readFByte();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * Reads a 4 byte float.
     */
    public float readCFloat() throws IOException {
        return Float.intBitsToFloat(readFInt());
    }

    public float readFFloat() throws IOException {
        return Float.intBitsToFloat(readFInt());
    }

    /**
     * Reads an 8 bytes double.
     */
    public double readCDouble() throws IOException {
        ensureReadAhead(8);
        return Double.longBitsToDouble(readFLong());
    }

    public short readFShort() throws IOException {
        ensureReadAhead(2);
        int count = input.pos;
        final byte buf[] = input.buf;
        int ch1 = (buf[count++] + 256) & 0xff;
        int ch2 = (buf[count++] + 256) & 0xff;
        input.pos = count;
        return (short) ((ch1 << 8) + (ch2 << 0));
    }

    public short readCShort() throws IOException {
        ensureReadAhead(3);
        int head = ((int) readFByte() + 256) & 0xff;
        if (head >= 0 && head < 255) {
            return (short) head;
        }
        int ch1 = readFByte();
        int ch2 = readFByte();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    public FSTInputStream getInput() {
        return input;
    }

    public void push(byte[] buf, int pos, int length) {
        input.push(buf,pos,length);
    }

    public void pop() {
        input.pop();
    }
}