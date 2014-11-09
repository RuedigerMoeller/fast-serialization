package org.nustaq.offheap.bytez;

import java.nio.ByteBuffer;

/**
 * Created by ruedi on 08.11.2014.
 *
 * basic memory abstraction
 */
public interface BasicBytez extends ByteSource {
    public byte get(long byteIndex);
    public boolean getBool(long byteIndex);
    public char getChar(long byteIndex);
    public short getShort(long byteIndex);
    public int getInt(long byteIndex);
    public long getLong(long byteIndex);
    public float getFloat(long byteIndex);
    public double getDouble(long byteIndex);

    public void put(long byteIndex, byte value);
    public void putBool(long byteIndex, boolean val);
    public void putChar(long byteIndex, char c);
    public void putShort(long byteIndex, short s);
    public void putInt(long byteIndex, int i);
    public void putLong(long byteIndex, long l);
    public void putFloat(long byteIndex, float f);
    public void putDouble(long byteIndex, double d);

    public long length();

    public void getArr(long byteIndex, byte[] target, int elemoff, int numElems);
    public void getCharArr(long byteIndex, char[] target, int elemoff, int numElems);
    public void getShortArr(long byteIndex, short[] target, int elemoff, int numElems);
    public void getIntArr(long byteIndex, int[] target, int elemoff, int numElems);
    public void getLongArr(long byteIndex, long[] target, int elemoff, int numElems);
    public void getFloatArr(long byteIndex, float[] target, int elemoff, int numElems);
    public void getDoubleArr(long byteIndex, double[] target, int elemoff, int numElems);
    public void getBooleanArr(long byteIndex, boolean[] target, int elemoff, int numElems);

    public void set(long byteIndex, byte[] source, int elemoff, int numElems);
    public void setChar(long byteIndex, char[] source, int elemoff, int numElems);
    public void setShort(long byteIndex, short[] source, int elemoff, int numElems);
    public void setInt(long byteIndex, int[] source, int elemoff, int numElems);
    public void setLong(long byteIndex, long[] source, int elemoff, int numElems);
    public void setFloat(long byteIndex, float[] source, int elemoff, int numElems);
    public void setDouble(long byteIndex, double[] source, int elemoff, int numElems);
    public void setBoolean(long byteIndex, boolean[] o, int i, int siz);

    public void copyTo(Bytez other, long otherByteIndex, long myByteIndex, long lenBytes);
    public Bytez newInstance(long size);

}
