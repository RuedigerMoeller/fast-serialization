package org.nustaq.offheap.bytez.niobuffers;

import org.nustaq.offheap.bytez.BasicBytez;

import java.nio.ByteBuffer;

/**
 * Created by moelrue on 5/5/15.
 */
public class ByteBufferBasicBytez implements BasicBytez {

    ByteBuffer buffer;

    public ByteBufferBasicBytez(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public byte get(long byteIndex) {
        return buffer.get((int) byteIndex);
    }

    @Override
    public boolean getBool(long byteIndex) {
        return buffer.get((int) byteIndex) != 0;
    }

    @Override
    public char getChar(long byteIndex) {
        return buffer.getChar((int) byteIndex);
    }

    @Override
    public short getShort(long byteIndex) {
        return buffer.getShort((int) byteIndex);
    }

    @Override
    public int getInt(long byteIndex) {
        return buffer.getInt((int) byteIndex);
    }

    @Override
    public long getLong(long byteIndex) {
        return buffer.getLong((int) byteIndex);
    }

    @Override
    public float getFloat(long byteIndex) {
        return buffer.getFloat((int) byteIndex);
    }

    @Override
    public double getDouble(long byteIndex) {
        return buffer.getDouble((int) byteIndex);
    }

    @Override
    public void put(long byteIndex, byte value) {
        buffer.put((int) byteIndex,value);
    }

    @Override
    public void putBool(long byteIndex, boolean val) {
        buffer.put((int) byteIndex, (byte) (val?1:0));
    }

    @Override
    public void putChar(long byteIndex, char c) {
        buffer.putChar((int) byteIndex, c);
    }

    @Override
    public void putShort(long byteIndex, short s) {
        buffer.putShort((int) byteIndex, s);
    }

    @Override
    public void putInt(long byteIndex, int i) {
        buffer.putInt((int) byteIndex, i);
    }

    @Override
    public void putLong(long byteIndex, long l) {
        buffer.putLong((int) byteIndex, l);
    }

    @Override
    public void putFloat(long byteIndex, float f) {
        buffer.putFloat((int) byteIndex, f);
    }

    @Override
    public void putDouble(long byteIndex, double d) {
        buffer.putDouble((int) byteIndex, d);
    }

    @Override
    public long length() {
        return buffer.limit();
    }

    @Override
    public void getArr(long byteIndex, byte[] target, int elemoff, int numElems) {
        int position = buffer.position();
        buffer.position((int) byteIndex);
        buffer.get(target,elemoff,numElems);
        buffer.position(position);
    }

    @Override
    public void getCharArr(long byteIndex, char[] target, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            target[i+elemoff] = buffer.getChar((int) (byteIndex+i*2));
        }
    }

    @Override
    public void getShortArr(long byteIndex, short[] target, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            target[i+elemoff] = buffer.getShort((int) (byteIndex+i * 2));
        }
    }

    @Override
    public void getIntArr(long byteIndex, int[] target, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            target[i+elemoff] = buffer.getInt((int) (byteIndex+i * 4));
        }
    }

    @Override
    public void getLongArr(long byteIndex, long[] target, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            target[i+elemoff] = buffer.getLong((int) (byteIndex+i * 8));
        }
    }

    @Override
    public void getFloatArr(long byteIndex, float[] target, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            target[i+elemoff] = buffer.getFloat((int) (byteIndex+i * 4));
        }
    }

    @Override
    public void getDoubleArr(long byteIndex, double[] target, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            target[i+elemoff] = buffer.getDouble((int) (byteIndex+i * 8));
        }
    }

    @Override
    public void getBooleanArr(long byteIndex, boolean[] target, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            target[i+elemoff] = buffer.getInt((int) (byteIndex+i)) != 0;
        }
    }

    @Override
    public void set(long byteIndex, byte[] source, int elemoff, int numElems) {
        int position = buffer.position();
        buffer.position((int) byteIndex);
        buffer.put(source, elemoff, numElems);
        buffer.position(position);
    }

    @Override
    public void setChar(long byteIndex, char[] source, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            buffer.putChar((int) (byteIndex+2*i),source[i+elemoff]);
        }
    }

    @Override
    public void setShort(long byteIndex, short[] source, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            buffer.putShort((int) (byteIndex + 2 * i), source[i + elemoff]);
        }
    }

    @Override
    public void setInt(long byteIndex, int[] source, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            buffer.putInt((int) (byteIndex+4*i),source[i+elemoff]);
        }
    }

    @Override
    public void setLong(long byteIndex, long[] source, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            buffer.putLong((int) (byteIndex+8*i),source[i+elemoff]);
        }
    }

    @Override
    public void setFloat(long byteIndex, float[] source, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            buffer.putFloat((int) (byteIndex+4*i),source[i+elemoff]);
        }
    }

    @Override
    public void setDouble(long byteIndex, double[] source, int elemoff, int numElems) {
        for ( int i=0; i <numElems; i++ ) {
            buffer.putDouble((int) (byteIndex+8*i),source[i+elemoff]);
        }
    }

    @Override
    public void setBoolean(long byteIndex, boolean[] source, int elemoff, int siz) {
        for ( int i=0; i <siz; i++ ) {
            buffer.put((int) (byteIndex + i), (byte) (source[i + elemoff] ? 1 : 0));
        }
    }

    @Override
    public void copyTo(BasicBytez other, long otherByteIndex, long myByteIndex, long lenBytes) {

    }

    @Override
    public BasicBytez newInstance(long size) {
        return new ByteBufferBasicBytez(ByteBuffer.allocate((int) size));
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
}
