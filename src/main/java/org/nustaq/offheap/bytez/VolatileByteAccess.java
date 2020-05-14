package org.nustaq.offheap.bytez;

public interface VolatileByteAccess {

    public boolean getBoolVolatile(long byteIndex);
    public byte getVolatile(long byteIndex);
    public char getCharVolatile(long byteIndex);
    public short getShortVolatile(long byteIndex);
    public int getIntVolatile(long byteIndex);
    public long getLongVolatile(long byteIndex);
    public float getFloatVolatile(long byteIndex);
    public double getDoubleVolatile(long byteIndex);

    public void putBoolVolatile(long byteIndex, boolean value);
    public void putVolatile(long byteIndex, byte value);
    public void putCharVolatile(long byteIndex, char c);
    public void putShortVolatile(long byteIndex, short s);
    public void putIntVolatile(long byteIndex, int i);
    public void putLongVolatile(long byteIndex, long l);
    public void putFloatVolatile(long byteIndex, float f);
    public void putDoubleVolatile(long byteIndex, double d);

}
