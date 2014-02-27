package de.ruedigermoeller.heapoff.bytez;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 16.11.13
 * Time: 12:15
 * To change this template use File | Settings | File Templates.
 */

/**
 * abstraction of byte arrays similar to ByteBuffer without the need to create temp objects in order to get long,int,.. views
 * additionally supports volatile read/write (for byte[] based backing buffers only !)
 */
public interface Bytez {

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

    public void set(long byteIndex, byte[] source, int elemoff, int numElems);
    public void setChar(long byteIndex, char[] source, int elemoff, int numElems);
    public void setShort(long byteIndex, short[] source, int elemoff, int numElems);
    public void setInt(long byteIndex, int[] source, int elemoff, int numElems);
    public void setLong(long byteIndex, long[] source, int elemoff, int numElems);
    public void setFloat(long byteIndex, float[] source, int elemoff, int numElems);
    public void setDouble(long byteIndex, double[] source, int elemoff, int numElems);
    public void setBoolean(int byteIndex, boolean[] o, int i, int siz);

    public void copyTo(Bytez other, long otherByteIndex, long myByteIndex, long lenBytes);
    public Bytez newInstance(long size);

    public boolean compareAndSwapInt( long offset, int expect, int newVal);
    public boolean compareAndSwapLong( long offset, long expect, long newVal);

    public byte[] toBytes(int startIndex, int len);
    /**
     * @return return underlying as byte array, not supported by MallocBytez. Use getArr to extract data instead.
     */
    public byte[] asByteArray();

    /**
     * @return the start index inside the byte array returned by asByteArray, not supported by MallocBytez
     */
    public int getBAOffsetIndex();
    /**
     * @return the length inside the byte array returned by asByteArray, not supported by MallocBytez
     */
    public int getBALength();


}
