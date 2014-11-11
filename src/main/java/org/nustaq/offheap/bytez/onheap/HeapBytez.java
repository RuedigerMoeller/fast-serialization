package org.nustaq.offheap.bytez.onheap;

import org.nustaq.offheap.bytez.BasicBytez;
import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.malloc.MallocBytez;
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
 * Date: 16.11.13
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */

/**
 * byte array abstraction based on heap byte arrays
 */
public class HeapBytez implements Bytez {
    static Unsafe unsafe = FSTUtil.getUnsafe();
    static long byteoff = FSTUtil.bufoff;
    static long caoff = FSTUtil.choff;
    static long saoff = FSTUtil.choff;
    static long iaoff = FSTUtil.intoff;
    static long laoff = FSTUtil.longoff;
    static long daoff = FSTUtil.doubleoff;
    static long faoff = FSTUtil.floatoff;

    byte[] base;
    long off;
    long len;

    private void checkIndex(long index, int len ) {
//        if ( index >= base.length || index < off-byteoff ) {
//            throw new RuntimeException("aua");
//        }
//        index += len-1;
//        if ( index >= base.length || index < off-byteoff ) {
//            throw new RuntimeException("aua 1");
//        }
    }

    public HeapBytez(byte[] base) {
        this(base,0);
    }

    public HeapBytez(byte[] base, long offIndex) {
        this(base,offIndex, base.length-offIndex);
    }

    public HeapBytez(byte[] base, long offIndex, long len) {
        setBase(base, offIndex, len);
    }

    public void setBase(byte[] base, long offIndex, long len) {
        this.base = base;
        this.off = byteoff+offIndex;
        this.len = len;
    }

    public HeapBytez slice(long off, int len) {
        if (off+len >= base.length)
            throw new RuntimeException("invalid slice "+off+":"+len+" mylen:"+base.length);
        return new HeapBytez(base,off,len);
    }

    @Override
    public byte get(long byteIndex) {
        checkIndex(byteIndex,1);
        return unsafe.getByte(base,off+byteIndex);
    }

    @Override
    public boolean getBool(long byteIndex) {
        checkIndex(byteIndex,1);
        return unsafe.getByte(base,off+byteIndex) != 0;
    }

    @Override
    public char getChar(long byteIndex) {
        checkIndex(byteIndex,2);
        return unsafe.getChar(base, off + byteIndex);
    }

    @Override
    public short getShort(long byteIndex) {
        checkIndex(byteIndex,2);
        return unsafe.getShort(base, off + byteIndex);
    }

    @Override
    public int getInt(long byteIndex) {
        checkIndex(byteIndex,4);
        int res = unsafe.getInt(base, off + byteIndex);
        return res;
    }

    @Override
    public long getLong(long byteIndex) {
        checkIndex(byteIndex,8);
        return unsafe.getLong(base, off + byteIndex);
    }

    @Override
    public float getFloat(long byteIndex) {
        checkIndex(byteIndex,4);
        return unsafe.getFloat(base, off + byteIndex);
    }

    @Override
    public double getDouble(long byteIndex) {
        checkIndex(byteIndex,8);
        return unsafe.getDouble(base, off + byteIndex);
    }

    @Override
    public void put(long byteIndex, byte value) {
        checkIndex(byteIndex,1);
        unsafe.putByte(base,off+byteIndex,value);
    }

    @Override
    public void putBool(long byteIndex, boolean val) {
        checkIndex(byteIndex,1);
        put(byteIndex,(byte) (val ? 1 : 0) );
    }

    @Override
    public void putChar(long byteIndex, char c) {
        checkIndex(byteIndex,2);
        unsafe.putChar(base, off + byteIndex, c);
    }

    @Override
    public void putShort(long byteIndex, short s) {
        checkIndex(byteIndex,2);
        unsafe.putShort(base, off + byteIndex, s);
    }

    @Override
    public void putInt(long byteIndex, int i) {
        checkIndex(byteIndex,4);
        unsafe.putInt(base, off + byteIndex, i);
    }

    @Override
    public void putLong(long byteIndex, long l) {
        checkIndex(byteIndex,8);
        unsafe.putLong(base, off + byteIndex, l);
    }

    @Override
    public void putFloat(long byteIndex, float f) {
        checkIndex(byteIndex,4);
        unsafe.putFloat(base, off + byteIndex, f);
    }

    @Override
    public void putDouble(long byteIndex, double d) {
        checkIndex(byteIndex,8);
        unsafe.putDouble(base, off + byteIndex, d);
    }

    @Override
    public long length() {
        return len;
    }

    @Override
    public void getArr(long byteIndex, byte[] target, int elemoff, int numElems) {
        unsafe.copyMemory(base,off+byteIndex,target,off+elemoff,numElems);
    }

    @Override
    public void getCharArr(long byteIndex, char[] target, int elemoff, int numElems) {
        unsafe.copyMemory(base,off+byteIndex,target,caoff+elemoff*2+off-byteoff,numElems*2);
    }

    @Override
    public void getShortArr(long byteIndex, short[] target, int elemoff, int numElems) {
        unsafe.copyMemory(base,off+byteIndex,target,saoff+elemoff*2+off-byteoff,numElems*2);
    }

    @Override
    public void getIntArr(long byteIndex, int[] target, int elemoff, int numElems) {
        unsafe.copyMemory(base,off+byteIndex,target,iaoff+elemoff*4+off-byteoff,numElems*4);
    }

    @Override
    public void getLongArr(long byteIndex, long[] target, int elemoff, int numElems) {
        unsafe.copyMemory(base,off+byteIndex,target,laoff+elemoff*8+off-byteoff,numElems*8);
    }

    @Override
    public void getFloatArr(long byteIndex, float[] target, int elemoff, int numElems) {
        unsafe.copyMemory(base,off+byteIndex,target,faoff+elemoff*4+off-byteoff,numElems*4);
    }

    @Override
    public void getDoubleArr(long byteIndex, double[] target, int elemoff, int numElems) {
        unsafe.copyMemory(base,off+byteIndex,target,daoff+elemoff*4+off-byteoff,numElems*8);
    }

    @Override
    public void getBooleanArr(long byteIndex, boolean[] target, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++) {
            target[elemoff+i] = getBool(byteIndex+i);
        }
    }

    @Override
    public void set(long byteIndex, byte[] source, int elemoff, int numElems) {
        checkIndex(byteIndex,numElems);
        unsafe.copyMemory(source,off+elemoff,base,off+byteIndex,numElems);
    }

    @Override
    public void setChar(long byteIndex, char[] source, int elemoff, int numElems) {
        checkIndex(byteIndex,numElems*2);
        unsafe.copyMemory(source,caoff+off-byteoff+elemoff*2,base,off+byteIndex,numElems*2);
    }

    @Override
    public void setShort(long byteIndex, short[] source, int elemoff, int numElems) {
        checkIndex(byteIndex,numElems*2);
        unsafe.copyMemory(source,caoff+off-byteoff+elemoff*2,base,off+byteIndex,numElems*2);
    }

    @Override
    public void setInt(long byteIndex, int[] source, int elemoff, int numElems) {
        checkIndex(byteIndex,numElems*4);
        unsafe.copyMemory(source,iaoff+off-byteoff+elemoff*4,base,off+byteIndex,numElems*4);
    }

    @Override
    public void setLong(long byteIndex, long[] source, int elemoff, int numElems) {
        checkIndex(byteIndex,numElems*8);
        unsafe.copyMemory(source,laoff+off-byteoff+elemoff*8,base,off+byteIndex,numElems*8);
    }

    @Override
    public void setFloat(long byteIndex, float[] source, int elemoff, int numElems) {
        checkIndex(byteIndex,numElems*4);
        unsafe.copyMemory(source,faoff+off-byteoff+elemoff*4,base,off+byteIndex,numElems*4);
    }

    @Override
    public void setDouble(long byteIndex, double[] source, int elemoff, int numElems) {
        checkIndex(byteIndex,numElems*8);
        unsafe.copyMemory(source,daoff+off-byteoff+elemoff*8,base,off+byteIndex,numElems*8);
    }

    @Override
    public void setBoolean(long byteIndex, boolean[] o, int elemoff, int numElems) {
        checkIndex(byteIndex,numElems);
        for ( int i = 0; i < numElems; i++) {
            put(byteIndex+i, (byte) (o[i+elemoff] ? 1 : 0));
        }
    }

    @Override
    public void copyTo(BasicBytez other, long otherByteIndex, long myByteIndex, long lenBytes) {
        if ( lenBytes == 0 )
            return;
        checkIndex(myByteIndex, (int) lenBytes);
        if ( other instanceof HeapBytez) {
            HeapBytez hp = (HeapBytez) other;
            unsafe.copyMemory(base,off+myByteIndex,hp.base,hp.off+otherByteIndex,lenBytes);
        } else if (other instanceof MallocBytez ) {
            MallocBytez mb = (MallocBytez) other;
            unsafe.copyMemory(base,off+myByteIndex,null,otherByteIndex+mb.getBaseAdress(),lenBytes);
        } else {
            for ( long i = 0; i < lenBytes; i++ ) {
                other.put(otherByteIndex+i,get(myByteIndex+i));
            }
        }
    }

    @Override
    public Bytez newInstance(long size) {
        return new HeapBytez(new byte[(int) size]);
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expect, int newVal) {
        return unsafe.compareAndSwapInt(base,off+offset,expect,newVal);
    }

    @Override
    public boolean compareAndSwapLong(long offset, long expect, long newVal) {
        return unsafe.compareAndSwapLong(base, off + offset, expect, newVal);
    }

    @Override
    public byte[] toBytes(long startIndex, int len) {
        byte res[] = new byte[len];
        System.arraycopy(base, (int) (off-FSTUtil.bufoff)+(int)startIndex,res,0,len);
        return res;
    }

    @Override
    public byte[] asByteArray() {
        return base;
    }

    /**
     * @return the start index inside the byte array returned by asByteArray, not supported by MallocBytez
     */
    @Override
    public int getBAOffsetIndex() {
        return (int) (off- FSTUtil.bufoff);
    }

    /**
     * @return the length inside the byte array returned by asByteArray, not supported by MallocBytez
     */
    @Override
    public int getBALength() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int hashCode() {
        return base.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof HeapBytez)
            return base == ((HeapBytez) obj).base;
        return false;
    }

    @Override
    public boolean getBoolVolatile(long byteIndex) {
        return getVolatile(byteIndex) != 0;
    }

    @Override
    public byte getVolatile(long byteIndex) {
        return unsafe.getByteVolatile(base, off + byteIndex);
    }

    @Override
    public char getCharVolatile(long byteIndex) {
        return unsafe.getCharVolatile(base, off + byteIndex);
    }

    @Override
    public short getShortVolatile(long byteIndex) {
        return unsafe.getShortVolatile(base, off + byteIndex);
    }

    @Override
    public int getIntVolatile(long byteIndex) {
        return unsafe.getIntVolatile(base, off + byteIndex);
    }

    @Override
    public long getLongVolatile(long byteIndex) {
        return unsafe.getLongVolatile(base, off + byteIndex);
    }

    @Override
    public float getFloatVolatile(long byteIndex) {
        return unsafe.getFloatVolatile(base, off + byteIndex);
    }

    @Override
    public double getDoubleVolatile(long byteIndex) {
        return unsafe.getDoubleVolatile(base, off + byteIndex);
    }

    @Override
    public void putBoolVolatile(long byteIndex, boolean value) {
        putVolatile(byteIndex, (byte) (value?1:0));
    }

    @Override
    public void putVolatile(long byteIndex, byte value) {
        unsafe.putByteVolatile(base, off + byteIndex, value);
    }

    @Override
    public void putCharVolatile(long byteIndex, char c) {
        unsafe.putCharVolatile(base, off + byteIndex, c);
    }

    @Override
    public void putShortVolatile(long byteIndex, short s) {
        unsafe.putShortVolatile(base, off + byteIndex, s);
    }

    @Override
    public void putIntVolatile(long byteIndex, int i) {
        unsafe.putIntVolatile(base, off + byteIndex, i);
    }

    @Override
    public void putLongVolatile(long byteIndex, long l) {
        unsafe.putLongVolatile(base, off + byteIndex, l);
    }

    @Override
    public void putFloatVolatile(long byteIndex, float f) {
        unsafe.putFloatVolatile(base, off + byteIndex, f);
    }

    @Override
    public void putDoubleVolatile(long byteIndex, double d) {
        unsafe.putDoubleVolatile(base, off + byteIndex, d);
    }


    public byte[] getBase() {
        return base;
    }

    /**
     * @return offset to byte array INCLUDING native bytearray header
     */
    public long getOff() {
        return off;
    }

    /**
     * @return offset to byte array EXCLUDING native bytearray header
     */
    public long getOffsetIndex() {
        return off-byteoff;
    }

}
