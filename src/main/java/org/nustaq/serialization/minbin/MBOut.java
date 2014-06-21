package org.nustaq.serialization.minbin;

import java.lang.reflect.Array;

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
* Date: 12.04.2014
* Time: 22:11
* To change this template use File | Settings | File Templates.
*/
public class MBOut {
    byte bytez[] = new byte[500];
    int pos = 0;
    
    MinBin mb;

    public MBOut() {
        this(MinBin.DefaultInstance);
    }

    public MBOut(MinBin mb) {
        this.mb = mb;
    }

    /**
     * write single byte, grow byte array if needed
     * @param b
     */
    void writeOut(byte b) {
        if (pos == bytez.length - 1) {
            resize();
        }
        bytez[pos++] = b;
    }

    private void resize() {
        byte tmp[] = new byte[Math.min(bytez.length + 50 * 1000 * 1000, bytez.length * 2)];
        System.arraycopy(bytez, 0, tmp, 0, pos);
        bytez = tmp;
    }

    /**
     * write an int type with header
     * @param type
     * @param data
     */
    public void writeInt(byte type, long data) {
        if (!MinBin.isPrimitive(type) || MinBin.isArray(type))
            throw new RuntimeException("illegal type code");
        writeOut(type);
        writeRawInt(type, data);
    }
    /**
     * encode int without header tag
     * @param data
     */
    protected void writeRawInt(byte type, long data) {
        int numBytes = MinBin.extractNumBytes(type);
        for (int i = 0; i < numBytes; i++) {
            writeOut((byte) (data & 0xff));
            data = data >>> 8;
        }
    }
    /**
     * encode int using only as much bytes as needed to represent it
     * @param data
     */
    public void writeIntPacked(long data) {
        if (data <= Byte.MAX_VALUE && data >= Byte.MIN_VALUE)            writeInt(MinBin.INT_8,  data);
        else if (data <= Short.MAX_VALUE && data >= Short.MIN_VALUE)     writeInt(MinBin.INT_16, data);
        else if (data <= Integer.MAX_VALUE && data >= Integer.MIN_VALUE) writeInt(MinBin.INT_32, data);
        else if (data <= Long.MAX_VALUE && data >= Long.MIN_VALUE)       writeInt(MinBin.INT_64, data);
    }

    /**
     * write primitive array + header. no floating point or object array allowed. Just int based types
     * @param primitiveArray
     * @param start
     * @param len
     */
    public void writeArray(Object primitiveArray, int start, int len) {
        byte type = MinBin.ARRAY_MASK;
        Class<?> componentType = primitiveArray.getClass().getComponentType();
        if (componentType == boolean.class)    type |= MinBin.INT_8;
        else if (componentType == byte.class)  type |= MinBin.INT_8;
        else if (componentType == short.class) type |= MinBin.INT_16;
        else if (componentType == char.class)  type |= MinBin.INT_16 | MinBin.UNSIGN_MASK;
        else if (componentType == int.class)   type |= MinBin.INT_32;
        else if (componentType == long.class)  type |= MinBin.INT_64;
        else throw new RuntimeException("unsupported type " + componentType.getName());
        writeOut(type);
        writeIntPacked(len);
        switch (type) {
            case MinBin.INT_8|MinBin.ARRAY_MASK: {
                if ( componentType == boolean.class ) {
                    boolean[] arr = (boolean[]) primitiveArray;
                    for (int i = start; i < start + len; i++) {
                        writeRawInt(type, arr[i] ? 1 : 0);
                    }
                } else {
                    byte[] arr = (byte[]) primitiveArray;
                    for (int i = start; i < start + len; i++) {
                        writeRawInt(type, arr[i]);
                    }
                }
            }
            break;
            case MinBin.CHAR|MinBin.ARRAY_MASK: {
                char[] charArr = (char[]) primitiveArray;
                for (int i = start; i < start + len; i++) {
                    writeRawInt(type, charArr[i]);
                }
            }
            break;
            case MinBin.INT_32|MinBin.ARRAY_MASK: {
                int[] arr = (int[]) primitiveArray;
                for (int i = start; i < start + len; i++) {
                    writeRawInt(type, arr[i]);
                }
            }
            break;
            case MinBin.INT_64|MinBin.ARRAY_MASK: {
                long[] arr = (long[]) primitiveArray;
                for (int i = start; i < start + len; i++) {
                    writeRawInt(type, arr[i]);
                }
            }
            break;
            default: {
                for (int i = start; i < start + len; i++) {
                    if (componentType == boolean.class)
                        writeRawInt(type, Array.getBoolean(primitiveArray, i) ? 1 : 0);
                    else
                        writeRawInt(type, Array.getLong(primitiveArray, i));
                }
            }
        }
    }

    public void writeTagHeader(byte tagId) {
        writeOut(MinBin.getTagCode(tagId));
    }
    
    public void writeTag( Object obj ) {
        if (obj==MinBin.END_MARKER) {
            writeOut(MinBin.END);
            return;
        }
        MinBin.TagSerializer tagSerializer = mb.getSerializerFor(obj);
        if ( tagSerializer == null ) {
            throw new RuntimeException("no tag serializer found for "+obj.getClass().getName());
        }
        writeTagHeader((byte) tagSerializer.getTagId());
        tagSerializer.writeTag(obj,this);
    }

    public int getWritten() {  return pos; }
    public byte[] getBytez() { return bytez; }
    /**
     * completely reset state
     */
    public void reset() { pos = 0; }
    /**
     * completely reset and use given bytearray as buffer
     * @param bytez
     */
    public void reset(byte[] bytez) {
        pos = 0;
        this.bytez = bytez;
    }
    /**
     * only reset position
     */
    public void resetPosition() {
        pos = 0;
    }

    public void writeObject(Object o) {
        if ( o == null ) {
            writeTag(o);
        } else if ( o.getClass().isPrimitive() && o.getClass() != float.class && o.getClass() != double.class ) {
            writeIntPacked(((Number) o).longValue());
        } else if ( o.getClass().isArray() && o.getClass().getComponentType() != float.class && o.getClass().getComponentType() != double.class ) {
            writeArray(o,0,Array.getLength(o));
        } else if (o.getClass() == Byte.class){
            writeInt(MinBin.INT_8, ((Number)o).longValue());
        } else if (o.getClass() == Short.class){
            writeInt(MinBin.INT_16, ((Number)o).longValue());
        } else if (o.getClass() == Character.class){
            writeInt(MinBin.CHAR, ((Character)o).charValue());
        } else if (o.getClass() == Integer.class){
            writeInt(MinBin.INT_32, ((Number)o).longValue());
        } else if (o.getClass() == Long.class){
            writeInt(MinBin.INT_64, ((Number)o).longValue());
        } else {
            writeTag(o);
        }
    }

    /**
     * allow write through to underlying byte for performance reasons
     * @param bufferedName
     * @param i
     * @param length
     */
    public void writeRaw(byte[] bufferedName, int i, int length) {
        if (pos+length >= bytez.length - 1) {
            resize();
        }
        System.arraycopy(bufferedName,i,bytez,pos,length);
        pos += length;
    }
}
