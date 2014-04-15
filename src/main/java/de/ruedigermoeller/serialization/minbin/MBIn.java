package de.ruedigermoeller.serialization.minbin;

import de.ruedigermoeller.serialization.mix.Mix;

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
* Time: 22:10
* To change this template use File | Settings | File Templates.
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
     * @param result
     * @return
     */
    public Object readArrayRaw(byte type, int len, Object result) {
        Class componentType = result.getClass().getComponentType();
        for ( int i = 0; i < len; i++ ) {
            if ( componentType == boolean.class )
                Array.setBoolean(result, i, readRawInt(type) == 0 ? false : true);
            else  if ( componentType == byte.class )
                Array.setByte(result, i, (byte) readRawInt(type));
            else  if ( componentType == short.class )
                Array.setShort(result, i, (short) readRawInt(type));
            else  if ( componentType == char.class )
                Array.setChar(result, i, (char) readRawInt(type));
            else  if ( componentType == int.class )
                Array.setInt(result, i, (int) readRawInt(type));
            else  if ( componentType == long.class )
                Array.setLong(result, i, readRawInt(type));
            else throw new RuntimeException("unsupported array type "+result.getClass().getName());
        }
        return result;
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
            return readTag(readIn());
        }
    }
}
