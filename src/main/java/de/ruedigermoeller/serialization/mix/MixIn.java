package de.ruedigermoeller.serialization.mix;

import java.lang.reflect.Array;
import java.util.ArrayList;

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
* Date: 01.04.2014
* Time: 19:30
* To change this template use File | Settings | File Templates.
*/
public class MixIn {

    protected byte bytez[];
    protected int pos;

    public MixIn(byte[] bytez, int pos) {
        this.bytez = bytez;
        this.pos = pos;
    }

    public byte readIn() {
        return bytez[pos++];
    }
    
    public long readInt() {
        byte type = readIn();
        if ( (type & 0xf) >= Mix.DOUBLE || ((type& Mix.ARRAY_MASK)!=0)) {
            pos--;
            throw new RuntimeException("no integer based id avaiable");
        }
        byte numBytes = Mix.extractNumBytes(type);
        long l = readRawInt(numBytes);
        if ( (type & Mix.UNSIGN_MASK) == 0 ) {
            switch (numBytes) {
                case 0:
                    return (long) (byte) l;
                case 1:
                    return (long) (short) l;
                case 2:
                    return (long) (int) l;
                case 3:
                    return l;
            }
        }
        return l;
    }

    public double readDouble() {
        byte type = readIn();
        if ( (type & 0xf) != Mix.DOUBLE ) {
            pos--;
            throw new RuntimeException("no double id avaiable");
        }
        return Double.longBitsToDouble(readRawInt((byte) 3));
    }

    public Object readValue() {
        return readValue(readIn());
    }

    protected Object readValue(byte typeTag) {
        int rawType = typeTag & 0xf;
        switch (rawType) {
            case Mix.INT_8:
                if ( (typeTag& Mix.ARRAY_MASK) != 0 )
                    return readArray(typeTag);
                byte len = Mix.extractNumBytes(typeTag);
                switch ( len ) {
                    case 0: return (byte)readRawInt(len);
                    case 1: if ((typeTag& Mix.UNSIGN_MASK) == 0) 
                                return new Short((short) readRawInt(len));
                            else
                                return new Character((char) readRawInt(len));
                    case 2: return (int)readRawInt(len);
                    case 3: return new Long(readRawInt(len));
                }
            case Mix.DOUBLE:
                if ( (typeTag& Mix.ARRAY_MASK) != 0 )
                    return readArray(typeTag);
                return Double.longBitsToDouble(readRawInt((byte) 3));
            case Mix.TUPEL:
            case Mix.OBJECT:
                return readTupel(typeTag);
            case Mix.ATOM:
                if ( typeTag == Mix.TUPEL_END)
                    return Mix.ATOM_TUPEL_END;
                if ( typeTag == Mix.STR_16)
                    return Mix.ATOM_STR_16;
                if ( typeTag == Mix.STR_8)
                    return Mix.ATOM_STR_8;
                if ( typeTag == Mix.MAP)
                    return Mix.ATOM_MAP;
                if ( typeTag == Mix.ARR)
                    return Mix.ATOM_ARR;
                if ( typeTag == Mix.DATE)
                    return Mix.ATOM_DATE;
                if ( (typeTag>>>4) == 0 )
                    return new Mix.Atom((int) readInt());
                else
                    return new Mix.Atom(typeTag>>>4);
            default:
                throw new RuntimeException("unknown type "+typeTag);
        }
    }

    private long readRawInt(byte numBytes) {
        long res = 0;
        numBytes = (byte) (1<<numBytes);
        int shift = 0;
        for ( int i = 0; i < numBytes; i++ ) {
            long b = (readIn()+256) & 0xff;
            res += b<<shift;
            shift+=8;
        }
        return res;
    }

    private Object readArray(byte type) {
        byte typelen = Mix.extractNumBytes(type);
        byte baseType = (byte) (type&0xf);
        int len = (int) readInt(); 
        Object result = null;
        if ( baseType == Mix.INT_8 ) {
            switch (typelen) {
                case 0:
                    result = new byte[len];
                    break;
                case 1:
                    if ((type & Mix.UNSIGN_MASK) != 0)
                        result = new char[len];
                    else
                        result = new short[len];
                    break;
                case 2:
                    result = new int[len];
                    break;
                case 3:
                    result = new long[len];
                    break;
                default:
                    throw new RuntimeException("unknown array type");
            }
        } else if (baseType == Mix.DOUBLE) {
            result = new double[len];        
        } else
            throw new RuntimeException("unknown array structure");
        return readArrayRaw(typelen, len, result);
    }

    /**
     * read into preallocated array, allows to write to different type (e.g. boolean[] from byte[])
     * @param typelen 0..2 number of bytes-1 per int.
     * @param len
     * @param result
     * @return
     */
    public Object readArrayRaw(byte typelen, int len, Object result) {
        Class componentType = result.getClass().getComponentType();
        for ( int i = 0; i < len; i++ ) {
            if ( componentType == boolean.class )
                Array.setBoolean(result, i, readRawInt(typelen) == 0 ? false : true);
            else if ( componentType == double.class )
                Array.setDouble(result, i, Double.longBitsToDouble(readRawInt((byte) 3)) );
            else  if ( componentType == byte.class )
                Array.setByte(result, i, (byte) readRawInt(typelen));
            else  if ( componentType == short.class )
                Array.setShort(result, i, (short) readRawInt(typelen));
            else  if ( componentType == char.class )
                Array.setChar(result, i, (char) readRawInt(typelen));
            else  if ( componentType == int.class )
                Array.setInt(result, i, (int) readRawInt(typelen));
            else  if ( componentType == long.class )
                Array.setLong(result, i, readRawInt(typelen));
        }
        return result;
    }

    protected Object readTupel(byte tag) {
        int len;
        if ( (tag>>>4) > 0) {
            len = (tag>>>4)&0xf;
        }
        else {
            len = (int) readInt();
        }
        Object type = readValue(); // id
        Object res = readCustomTupel( len, type );
        if ( res == null ) {
            res = readBuiltInTupel(len, type);
        }
        if ( res == null ) {
            return readDefaultTupel(len, type, (byte) (tag&0xf));
        }
        return res;
    }

    protected Object readBuiltInTupel(int len, Object type) {
        if (type== Mix.ATOM_STR_8) {
            final byte[] bytes = (byte[]) readValue();
            String res = new String(bytes, 0, 0, bytes.length);
            return res;
        }
        if (type== Mix.ATOM_STR_16) {
            final char[] chars = (char[]) readValue();
            return new String(chars, 0, chars.length );
        }
        return null;
    }

    protected Object readDefaultTupel(int len, Object type, byte tag) {
        if ( len == -1 ) { // unknown length
            ArrayList cont = new ArrayList();
            Object read;
            while( (read = readValue()) != Mix.ATOM_TUPEL_END ) {
                cont.add(read);
            }
            return new Mix.Tupel(type,cont.toArray(), tag == Mix.OBJECT );
        } else {
            Object res[] = new Object[len];
            for ( int i = 0; i < len; i++ ) {
                res[i] = readValue();
            }
            return new Mix.Tupel(type,res, tag == Mix.OBJECT);
        }
    }

    protected Object readCustomTupel(int len, Object type) {
        return null;
    }

}
