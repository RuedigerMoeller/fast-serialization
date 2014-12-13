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
package org.nustaq.serialization.minbin;

/**
* Date: 12.04.2014
* Time: 22:10
*
* Input reader for minbin byte[]
*/
public class MBIn {

    MinBin mb;
    protected byte bytez[];
    protected int pos;
    int count; // length of data

    public MBIn(byte[] bytez, int pos) {
        this(MinBin.DefaultInstance,bytez,pos);
    }

    public MBIn(MinBin mb, byte[] bytez, int pos) {
        this.bytez = bytez;
        this.pos = pos;
        this.mb = mb;
        if ( bytez != null )
            this.count = bytez.length;
    }

    public byte readIn() {
        return bytez[pos++];
    }

    public byte peekIn() {
        return bytez[pos];
    }

    private long readRawInt(byte type) {
        long res = 0;
        int numBytes = MinBin.extractNumBytes(type);
        int shift = 0;
        for ( int i = 0; i < numBytes; i++ ) {
            long b = (readIn()+256) & 0xff;
            res += b<<shift;
            shift+=8;
        }
        return res;
    }

    public long readInt() {
        byte type = readIn();
        if ( !MinBin.isPrimitive(type) || MinBin.isArray(type)) {
            pos--;
            throw new RuntimeException("no integer based id avaiable:"+type);
        }
        int numBytes = MinBin.extractNumBytes(type);
        long l = readRawInt(type);
        if ( MinBin.isSigned(type) ) {
            switch (numBytes) {
                case 1: return (long) (byte) l;
                case 2: return (long) (short) l;
                case 4: return (long) (int) l;
                case 8: return l;
                default: throw new RuntimeException("Wat?");
            }
        }
        return l;
    }

    public Object readArray() {
        byte type = readIn();
        if ( ! MinBin.isArray(type) || ! MinBin.isPrimitive(type) )
            throw new RuntimeException("not a primitive array "+type);
        int len = (int) readInt();
        byte baseType = MinBin.getBaseType(type);
        Object result;
        switch (baseType) {
            case MinBin.INT_8:  result = new byte[len];  break;
            case MinBin.INT_16: result = new short[len]; break;
            case MinBin.CHAR:   result = new char[len];  break;
            case MinBin.INT_32: result = new int[len]; break;
            case MinBin.INT_64: result = new long[len]; break;
            default:
                throw new RuntimeException("unknown array type");
        }
        return readArrayRaw(type, len, result);
                
    }

    /**
     * read into preallocated array, allows to write to different type (e.g. boolean[] from byte[])
     * @param type type tag.
     * @param len
     * @param resultingArray
     * @return
     */
    public Object readArrayRaw(byte type, int len, Object resultingArray) {
        Class componentType = resultingArray.getClass().getComponentType();
        if ( componentType == byte.class ) {
            byte[] barr = (byte[]) resultingArray;
            for ( int i = 0; i < len; i++ ) {
                barr[i] = (byte) readRawInt(type);
            }
        } else  if ( componentType == short.class ) {
            short[] sArr = (short[]) resultingArray;
            for ( int i = 0; i < len; i++ ) {
                sArr[i] = (short) readRawInt(type);
            }
        } else  if ( componentType == char.class ) {
            char[] cArr = (char[]) resultingArray;
            for (int i = 0; i < len; i++) {
                cArr[i] = (char) readRawInt(type);
            }
        } else  if ( componentType == int.class ) {
            int[] iArr = (int[]) resultingArray;
            for (int i = 0; i < len; i++) {
                iArr[i] = (int) readRawInt(type);
            }
        } else  if ( componentType == long.class ) {
            long[] lArr = (long[]) resultingArray;
            for (int i = 0; i < len; i++) {
                lArr[i] = readRawInt(type);
            }
        } else if ( componentType == boolean.class ) {
            boolean[] boolArr = (boolean[]) resultingArray;
            for (int i = 0; i < len; i++) {
                boolArr[i] = readRawInt(MinBin.INT_8) != 0;
            }
        } else
            throw new RuntimeException("unsupported array type "+resultingArray.getClass().getName());
        return resultingArray;
    }
    
    public Object readTag( byte tag ) {
        int tagId = MinBin.getTagId(tag);
        MinBin.TagSerializer ts = mb.getSerializerForId(tagId);
        return ts.readTag(this);
    }
    
    public Object readObject() {
        byte type = peekIn();
        if (type==MinBin.END) {
            readIn();
            return MinBin.END_MARKER;
        }
        if ( MinBin.isPrimitive(type) ) {
            if ( MinBin.isArray(type) ) {
                return readArray();
            }
            switch (type) {
                case MinBin.INT_8: return (byte)readInt();
                case MinBin.INT_16: return (short)readInt();
                case MinBin.CHAR: return (char)readInt();
                case MinBin.INT_32: return (int)readInt();
                case MinBin.INT_64: return (long)readInt();
                default: throw new RuntimeException("unexpected primitive type:"+type);
            }
            
        } else {
            if ( MinBin.getTagId(type) == MinBin.HANDLE ) {
                return new MBRef((Integer) readTag(readIn()));
            }
            return readTag(readIn());
        }
    }

    public byte[] getBuffer() {
        return bytez;
    }

    public int getPos() {
        return pos;
    }

    public void setBuffer(byte[] buf, int count) {
        this.bytez = buf;
        pos = 0;
        this.count = count;
    }

    public void reset() {
        setBuffer(bytez,count);
    }
}
