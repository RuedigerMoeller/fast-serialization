/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nustaq.offheap.bytez.malloc;

import jdk.incubator.foreign.*;
import org.nustaq.offheap.bytez.BasicBytez;
import org.nustaq.offheap.bytez.Bytez;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * Date: 17.11.13
 * Time: 00:01
 *
 * implementation of Bytez interface using unsafe on raw allocated memory
 *
 */
public class MemoryBytez implements Bytez {

    MemorySegment memseg;

    protected MemoryBytez() {}

    public MemoryBytez(long len) {
        memseg = MemorySegment.allocateNative(len, ResourceScope.newImplicitScope());
    }

    public MemoryBytez(MemorySegment mem) {
        memseg = mem;
    }

    public MemoryBytez slice(long off, int len) {
        return new MemoryBytez(memseg.asSlice(off,len));
    }

    @Override
    public byte get(long byteIndex) {
        return MemoryAccess.getByteAtOffset(memseg,byteIndex);
    }

    @Override
    public boolean getBool(long byteIndex) {
        return get(byteIndex) != 0;
    }

    @Override
    public char getChar(long byteIndex) {
        return MemoryAccess.getCharAtOffset(memseg,byteIndex);
    }

    @Override
    public short getShort(long byteIndex) {
        return MemoryAccess.getShortAtOffset(memseg,byteIndex);
    }

    @Override
    public int getInt(long byteIndex) {
        return MemoryAccess.getIntAtOffset(memseg,byteIndex);
    }

    @Override
    public long getLong(long byteIndex) {
        return MemoryAccess.getLongAtOffset(memseg,byteIndex);
    }

    @Override
    public float getFloat(long byteIndex) {
        return MemoryAccess.getFloatAtOffset(memseg,byteIndex);
    }

    @Override
    public double getDouble(long byteIndex) {
        return MemoryAccess.getDoubleAtOffset(memseg,byteIndex);
    }

    @Override
    public void put(long byteIndex, byte value) {
        MemoryAccess.setByteAtOffset(memseg, byteIndex, value);
    }

    @Override
    public void putBool(long byteIndex, boolean val) {
        MemoryAccess.setByteAtOffset(memseg, byteIndex, (byte) (val?1:0));
    }

    @Override
    public void putChar(long byteIndex, char c) {
        MemoryAccess.setCharAtOffset(memseg, byteIndex, c);
    }

    @Override
    public void putShort(long byteIndex, short s) {
        MemoryAccess.setShortAtOffset(memseg, byteIndex, s);
    }

    @Override
    public void putInt(long byteIndex, int i) {
        MemoryAccess.setIntAtOffset(memseg, byteIndex, i);
    }

    @Override
    public void putLong(long byteIndex, long l) {
        MemoryAccess.setLongAtOffset(memseg, byteIndex, l);
    }

    @Override
    public void putFloat(long byteIndex, float f) {
        MemoryAccess.setFloatAtOffset(memseg, byteIndex, f);
    }

    @Override
    public void putDouble(long byteIndex, double d) {
        MemoryAccess.setDoubleAtOffset(memseg, byteIndex, d);
    }

    @Override
    public long length() {
        return memseg.byteSize();
    }

    @Override
    public void getArr(long byteIndex, byte[] target, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            target[elemoff+i] = get(byteIndex+i);
    }

    @Override
    public void getCharArr(long byteIndex, char[] target, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            target[elemoff+i] = getChar(byteIndex*2+i);
    }

    @Override
    public void getShortArr(long byteIndex, short[] target, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            target[elemoff+i] = getShort(byteIndex*2+i);
    }

    @Override
    public void getIntArr(long byteIndex, int[] target, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            target[elemoff+i] = getInt(byteIndex*4+i);
    }

    @Override
    public void getLongArr(long byteIndex, long[] target, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            target[elemoff+i] = getLong(byteIndex*8+i);
    }

    @Override
    public void getFloatArr(long byteIndex, float[] target, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            target[elemoff+i] = getFloat(byteIndex*4+i);
    }

    @Override
    public void getDoubleArr(long byteIndex, double[] target, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            target[elemoff+i] = getDouble(byteIndex*8+i);
    }

    @Override
    public void getBooleanArr(long byteIndex, boolean[] target, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            target[elemoff+i] = getBool(byteIndex+i);
    }

    @Override
    public void set(long byteIndex, byte[] source, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            put(byteIndex+i, source[i+elemoff]);
    }

    @Override
    public void setChar(long byteIndex, char[] source, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            putChar(byteIndex*2+i, source[i+elemoff]);
    }

    @Override
    public void setShort(long byteIndex, short[] source, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            putShort(byteIndex*2+i, source[i+elemoff]);
    }

    @Override
    public void setInt(long byteIndex, int[] source, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            putInt(byteIndex*4+i, source[i+elemoff]);
    }

    @Override
    public void setLong(long byteIndex, long[] source, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            putLong(byteIndex*8+i, source[i+elemoff]);
    }

    @Override
    public void setFloat(long byteIndex, float[] source, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            putFloat(byteIndex*4+i, source[i+elemoff]);
    }

    @Override
    public void setDouble(long byteIndex, double[] source, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            putDouble(byteIndex*8+i, source[i+elemoff]);
    }

    @Override
    public void setBoolean(long byteIndex, boolean[] o, int elemoff, int numElems) {
        for ( int i = 0; i < numElems; i++)
            put(byteIndex+i, (byte) (o[i+elemoff] ? 1 : 0));
    }

    @Override
    public void copyTo(BasicBytez other, long otherByteIndex, long myByteIndex, long lenBytes) {
        for ( long i = 0; i < lenBytes; i++ ) {
            other.put(otherByteIndex+i,get(myByteIndex+i));
        }
    }

    @Override
    public BasicBytez newInstance(long size) {
        return new MemoryBytez((long)size);
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expect, int newVal) {
        // compareAndExchange is gone ?? provide dummy impl unsync'ed
        int intAtOffset = MemoryAccess.getIntAtOffset(memseg, offset);
        if ( expect == intAtOffset ) {
            MemoryAccess.setIntAtOffset(memseg, offset, newVal);
            return true;
        }
        return false;
    }

    @Override
    public boolean compareAndSwapLong(long offset, long expect, long newVal) {
        // compareAndExchange is gone ?? provide dummy impl unsync'ed
        long longAtOffset = MemoryAccess.getLongAtOffset(memseg, offset);
        if ( expect == longAtOffset ) {
            MemoryAccess.setLongAtOffset(memseg, offset, newVal);
            return true;
        }
        return false;
    }

    @Override
    public byte[] toBytes(long startIndex, int len) {
        return memseg.asSlice(startIndex,len).toByteArray();
    }

    @Override
    public byte[] asByteArray() {
        return memseg.toByteArray();
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
        return memseg.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof MemoryBytez)
            return memseg.equals(((MemoryBytez) obj).memseg);
        return false;
    }

    void free() {
        memseg = null; //.close();
    }

    public long getLength() {
        return memseg.byteSize();
    }

    public static void main(String[] args) throws Exception {
        long siz = 1000 * 1_000_000L;
        Bytez m = new MallocBytezAllocator().alloc(siz);
        Bytez m1 = new MemoryBytez(siz);
        while (true) {
            for (int i = 0; i < 10; i++)
                testPerf(m, "unsafe");
            for (int i = 0; i < 10; i++)
                testPerf(m, "safe");
        }
    }

    private static long testPerf(Bytez m, String unsafe) {
        long ti = System.currentTimeMillis();
        long sum = 0;
        long max = m.length() / 4;
        for (long i = 0; i < max; i++ ) {
            sum+=m.getInt(i);
        }
        System.out.println(unsafe+" tim "+(System.currentTimeMillis()-ti));
        return sum;
    }

}
