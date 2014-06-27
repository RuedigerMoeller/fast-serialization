package org.nustaq.heapoff.bytez.malloc;

import org.nustaq.heapoff.bytez.Bytez;
import org.nustaq.heapoff.bytez.onheap.HeapBytez;
import org.nustaq.serialization.util.FSTUtil;
import sun.misc.Unsafe;

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
 * Date: 17.11.13
 * Time: 00:01
 * To change this template use File | Settings | File Templates.
 */
public class MallocBytez implements Bytez {

    static Unsafe unsafe = FSTUtil.getUnsafe();
    static long byteoff = FSTUtil.bufoff;
    static long caoff = FSTUtil.choff;
    static long saoff = FSTUtil.choff;
    static long iaoff = FSTUtil.intoff;
    static long laoff = FSTUtil.longoff;
    static long daoff = FSTUtil.doubleoff;
    static long faoff = FSTUtil.floatoff;


    long baseAdress;
    long length;
    public MallocBytez(long adr, long len) {
        setBase(adr, len);
    }

    public void setBase(long adr, long len) {
        baseAdress = adr;
        length = len;
    }

    @Override
    public byte get(long byteIndex) {
        return unsafe.getByte(baseAdress +byteIndex);
    }

    @Override
    public boolean getBool(long byteIndex) {
        return unsafe.getByte(baseAdress +byteIndex) != 0;
    }

    @Override
    public char getChar(long byteIndex) {
        return unsafe.getChar(baseAdress + byteIndex);
    }

    @Override
    public short getShort(long byteIndex) {
        return unsafe.getShort(baseAdress + byteIndex);
    }

    @Override
    public int getInt(long byteIndex) {
        int res = unsafe.getInt(baseAdress + byteIndex);
        return res;
    }

    @Override
    public long getLong(long byteIndex) {
        return unsafe.getLong(baseAdress + byteIndex);
    }

    @Override
    public float getFloat(long byteIndex) {
        return unsafe.getFloat(baseAdress + byteIndex);
    }

    @Override
    public double getDouble(long byteIndex) {
        return unsafe.getDouble(baseAdress + byteIndex);
    }

    @Override
    public void put(long byteIndex, byte value) {
        unsafe.putByte(baseAdress + byteIndex, value);
    }

    @Override
    public void putBool(long byteIndex, boolean val) {
        put(byteIndex, (byte) (val ? 1 : 0));
    }

    @Override
    public void putChar(long byteIndex, char c) {
        unsafe.putChar(baseAdress + byteIndex, c);
    }

    @Override
    public void putShort(long byteIndex, short s) {
        unsafe.putShort(baseAdress + byteIndex, s);
    }

    @Override
    public void putInt(long byteIndex, int i) {
        unsafe.putInt(baseAdress + byteIndex, i);
    }

    @Override
    public void putLong(long byteIndex, long l) {
        unsafe.putLong(baseAdress + byteIndex, l);
    }

    @Override
    public void putFloat(long byteIndex, float f) {
        unsafe.putFloat(baseAdress + byteIndex, f);
    }

    @Override
    public void putDouble(long byteIndex, double d) {
        unsafe.putDouble( baseAdress + byteIndex, d);
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public void getArr(long byteIndex, byte[] target, int elemoff, int numElems) {
        unsafe.copyMemory(null,baseAdress+byteIndex,target, byteoff+elemoff,numElems);
    }

    @Override
    public void getCharArr(long byteIndex, char[] target, int elemoff, int numElems) {
        unsafe.copyMemory(null,baseAdress +byteIndex,target,caoff+elemoff*2,numElems*2);
    }

    @Override
    public void getShortArr(long byteIndex, short[] target, int elemoff, int numElems) {
        unsafe.copyMemory(null,baseAdress +byteIndex,target,saoff+elemoff*2,numElems*2);
    }

    @Override
    public void getIntArr(long byteIndex, int[] target, int elemoff, int numElems) {
        unsafe.copyMemory(null,baseAdress +byteIndex,target,iaoff+elemoff*4,numElems*4);
    }

    @Override
    public void getLongArr(long byteIndex, long[] target, int elemoff, int numElems) {
        unsafe.copyMemory(null, baseAdress +byteIndex,target,laoff+elemoff*8,numElems*8);
    }

    @Override
    public void getFloatArr(long byteIndex, float[] target, int elemoff, int numElems) {
        unsafe.copyMemory(null, baseAdress +byteIndex,target,faoff+elemoff*4,numElems*4);
    }

    @Override
    public void getDoubleArr(long byteIndex, double[] target, int elemoff, int numElems) {
        unsafe.copyMemory(null, baseAdress +byteIndex,target,daoff+elemoff*8,numElems*8);
    }

    @Override
    public void set(long byteIndex, byte[] source, int elemoff, int numElems) {
        unsafe.copyMemory(source, byteoff+elemoff,null, baseAdress +byteIndex,numElems);
    }

    @Override
    public void setChar(long byteIndex, char[] source, int elemoff, int numElems) {
        unsafe.copyMemory(source,caoff+elemoff*2, null, baseAdress +byteIndex,numElems*2);
    }

    @Override
    public void setShort(long byteIndex, short[] source, int elemoff, int numElems) {
        unsafe.copyMemory(source,caoff+elemoff*2, null, baseAdress +byteIndex,numElems*2);
    }

    @Override
    public void setInt(long byteIndex, int[] source, int elemoff, int numElems) {
        unsafe.copyMemory(source,iaoff+elemoff*4,null, baseAdress +byteIndex,numElems*4);
    }

    @Override
    public void setLong(long byteIndex, long[] source, int elemoff, int numElems) {
        unsafe.copyMemory(source,laoff+ elemoff*8, null, baseAdress +byteIndex,numElems*8);
    }

    @Override
    public void setFloat(long byteIndex, float[] source, int elemoff, int numElems) {
        unsafe.copyMemory(source,faoff+elemoff*4, null, baseAdress +byteIndex,numElems*4);
    }

    @Override
    public void setDouble(long byteIndex, double[] source, int elemoff, int numElems) {
        unsafe.copyMemory(source,daoff+elemoff*8,null, baseAdress +byteIndex,numElems*8);
    }

    @Override
    public void setBoolean(int byteIndex, boolean[] o, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++) {
            put(byteIndex+i, (byte) (o[i+elemoff] ? 1 : 0));
        }
    }

    @Override
    public void copyTo(Bytez other, long otherByteIndex, long myByteIndex, long lenBytes) {
        if ( other instanceof HeapBytez) {
            HeapBytez hp = (HeapBytez) other;
            unsafe.copyMemory(null,baseAdress+myByteIndex, hp.getBase(), hp.getOff()+otherByteIndex,lenBytes);
        } else {
            for ( long i = 0; i < lenBytes; i++ ) {
                other.put(otherByteIndex+i,get(myByteIndex+i));
            }
        }
    }

    @Override
    public Bytez newInstance(long size) {
        return new MallocBytez(unsafe.allocateMemory(size),size);
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expect, int newVal) {
        return unsafe.compareAndSwapInt(null, baseAdress + offset, expect, newVal);
    }

    @Override
    public boolean compareAndSwapLong(long offset, long expect, long newVal) {
        return unsafe.compareAndSwapLong(null, baseAdress + offset, expect, newVal);
    }

    @Override
    public byte[] toBytes(long startIndex, int len) {
        byte res[] = new byte[len];
        unsafe.copyMemory(null,baseAdress+startIndex, res, FSTUtil.bufoff, len );
        return res;
    }

    @Override
    public byte[] asByteArray() {
        throw new RuntimeException("malloc bytez do not support this");
    }

    /**
     * @return the start index inside the byte array returned by asByteArray, not supported by MallocBytez
     */
    @Override
    public int getBAOffsetIndex() {
        throw new RuntimeException("malloc bytez do not support this");
    }

    /**
     * @return the length inside the byte array returned by asByteArray, not supported by MallocBytez
     */
    @Override
    public int getBALength() {
        throw new RuntimeException("malloc bytez do not support this");
    }

    @Override
    public int hashCode() {
        return (int) (baseAdress ^ (baseAdress >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof MallocBytez)
            return baseAdress == ((MallocBytez) obj).baseAdress && length == ((MallocBytez) obj).length;
        return false;
    }

    @Override
    public boolean getBoolVolatile(long byteIndex) {
        return getVolatile(byteIndex) != 0;
    }

    @Override
    public byte getVolatile(long byteIndex) {
        return unsafe.getByteVolatile(null, baseAdress + byteIndex); // FIXME: what to do here ?
    }

    @Override
    public char getCharVolatile(long byteIndex) {
        return unsafe.getCharVolatile(null, baseAdress + byteIndex);
    }

    @Override
    public short getShortVolatile(long byteIndex) {
        return unsafe.getShortVolatile(null, baseAdress + byteIndex);
    }

    @Override
    public int getIntVolatile(long byteIndex) {
        return unsafe.getIntVolatile(null, baseAdress + byteIndex);
    }

    @Override
    public long getLongVolatile(long byteIndex) {
        return unsafe.getLongVolatile(null, baseAdress + byteIndex);
    }

    @Override
    public float getFloatVolatile(long byteIndex) {
        return unsafe.getFloatVolatile(null, baseAdress + byteIndex);
    }

    @Override
    public double getDoubleVolatile(long byteIndex) {
        return unsafe.getDoubleVolatile(null, baseAdress + byteIndex);
    }

    @Override
    public void putBoolVolatile(long byteIndex, boolean value) {
        putVolatile(byteIndex, (byte) (value?1:0));
    }

    @Override
    public void putVolatile(long byteIndex, byte value) {
        unsafe.putByteVolatile(null, baseAdress + byteIndex, value);
    }

    @Override
    public void putCharVolatile(long byteIndex, char c) {
        unsafe.putCharVolatile(null, baseAdress + byteIndex, c);
    }

    @Override
    public void putShortVolatile(long byteIndex, short s) {
        unsafe.putShortVolatile(null, baseAdress + byteIndex, s);
    }

    @Override
    public void putIntVolatile(long byteIndex, int i) {
        unsafe.putIntVolatile(null, baseAdress + byteIndex, i);
    }

    @Override
    public void putLongVolatile(long byteIndex, long l) {
        unsafe.putLongVolatile(null, baseAdress + byteIndex, l);
    }

    @Override
    public void putFloatVolatile(long byteIndex, float f) {
        unsafe.putFloatVolatile(null, baseAdress + byteIndex, f);
    }

    @Override
    public void putDoubleVolatile(long byteIndex, double d) {
        unsafe.putDoubleVolatile(null, baseAdress + byteIndex, d);
    }

    void free() {
        unsafe.freeMemory(baseAdress);
        MallocBytezAllocator.alloced.addAndGet(-length);
    }

    public long getBaseAdress() {
        return baseAdress;
    }

    public long getLength() {
        return length;
    }
}
