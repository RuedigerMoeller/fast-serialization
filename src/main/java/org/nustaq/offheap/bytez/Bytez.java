package org.nustaq.offheap.bytez;

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
public interface Bytez extends BasicBytez {

    public Bytez slice(long off, int len);

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

    public boolean compareAndSwapInt( long offset, int expect, int newVal);
    public boolean compareAndSwapLong( long offset, long expect, long newVal);

    public byte[] toBytes(long startIndex, int len);
    /**
     * @return return the underlying byte array, not supported by MallocBytez !. Use getArr to extract data by copy instead.
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
