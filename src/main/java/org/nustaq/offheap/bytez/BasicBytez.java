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

    /**
     * copy to a byte array
     * @param byteIndex - offset index in this buffer to start copying
     * @param target - array to copy to
     * @param elemoff - offset in target array
     * @param numElems - length to copy to
     */
    public void getArr(long byteIndex, byte[] target, int elemoff, int numElems);

    /**
     * see getArr
     */
    public void getCharArr(long byteIndex, char[] target, int elemoff, int numElems);
    /**
     * see getArr
     */
    public void getShortArr(long byteIndex, short[] target, int elemoff, int numElems);
    /**
     * see getArr
     */
    public void getIntArr(long byteIndex, int[] target, int elemoff, int numElems);
    /**
     * see getArr
     */
    public void getLongArr(long byteIndex, long[] target, int elemoff, int numElems);
    /**
     * see getArr
     */
    public void getFloatArr(long byteIndex, float[] target, int elemoff, int numElems);
    /**
     * see getArr
     */
    public void getDoubleArr(long byteIndex, double[] target, int elemoff, int numElems);
    /**
     * see getArr
     */
    public void getBooleanArr(long byteIndex, boolean[] target, int elemoff, int numElems);

    public void set(long byteIndex, byte[] source, int elemoff, int numElems);
    public void setChar(long byteIndex, char[] source, int elemoff, int numElems);
    public void setShort(long byteIndex, short[] source, int elemoff, int numElems);
    public void setInt(long byteIndex, int[] source, int elemoff, int numElems);
    public void setLong(long byteIndex, long[] source, int elemoff, int numElems);
    public void setFloat(long byteIndex, float[] source, int elemoff, int numElems);
    public void setDouble(long byteIndex, double[] source, int elemoff, int numElems);
    public void setBoolean(long byteIndex, boolean[] o, int i, int siz);

    public void copyTo(BasicBytez other, long otherByteIndex, long myByteIndex, long lenBytes);
    public Bytez newInstance(long size);

}
