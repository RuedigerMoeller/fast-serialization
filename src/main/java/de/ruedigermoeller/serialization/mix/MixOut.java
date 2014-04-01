package de.ruedigermoeller.serialization.mix;

import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Date;

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
public class MixOut {

    byte bytez[] = new byte[500];
    int pos = 0;

    private void writeOut(byte b) {
        if ( pos == bytez.length - 1 ) {
            byte tmp[] = new byte[Math.min(bytez.length + 50 * 1000 * 1000, bytez.length * 2)];
            System.arraycopy(bytez,0,tmp,0,pos);
            bytez = tmp;
        }
        bytez[pos++] = b;
    }

    /**
     * writes tag+len. First object after must be tupel tag, then len elements.
     * If len is unknown, -1 can be provided. The end of the tupel must be marked with
     * a TUPEL_END atom
     * @param len
     */
    public void writeTupelHeader( long len, boolean isStrMap ) {
        if (len < 16 && len > 0 )
            writeOut((byte) ( (isStrMap? Mix.OBJECT : Mix.TUPEL)|len<<4));
        else {
            writeOut((isStrMap? Mix.OBJECT : Mix.TUPEL));
            writeIntPacked(len);
        }
    }

    public void writeDouble( double d ) {
        writeOut(Mix.DOUBLE);
        final long data = Double.doubleToLongBits(d);
        writeRawInt((byte) 3, data);
    }

    public void writeInt( byte type, long data ) {
        writeOut(type);
        writeRawInt(Mix.extractNumBytes(type), data);
    }

    /**
     * 
     * @param numBytes - 0 = 1 byte, 1 = 2 byte, 2 = 4 byte, 3 = 8 byte
     * @param data
     */
    protected void writeRawInt(byte numBytes, long data) {
        numBytes = (byte) (1<<numBytes);
        for ( int i = 0; i < numBytes; i++ ) {
            writeOut((byte) (data&0xff));
            data = data >>> 8;
        }
    }

    public void writeIntPacked(long data) {
        if ( data <= Byte.MAX_VALUE && data >= Byte.MIN_VALUE )
            writeInt(Mix.INT_8, data);
        else if ( data <= Short.MAX_VALUE && data >= Short.MIN_VALUE )
            writeInt(Mix.INT_16, data);
        else if ( data <= Integer.MAX_VALUE && data >= Integer.MIN_VALUE )
            writeInt(Mix.INT_32, data);
        else if ( data <= Long.MAX_VALUE && data >= Long.MIN_VALUE )
            writeInt(Mix.INT_64, data);
    }

    public void writeArray( Object primitiveArray, int start, int len ) {
        byte type = Mix.ARRAY_MASK;
        Class<?> componentType = primitiveArray.getClass().getComponentType();
        if ( componentType == boolean.class ) type |= Mix.INT_8;
        else if ( componentType == byte.class ) type |= Mix.INT_8;
        else if ( componentType == short.class ) type |= Mix.INT_16;
        else if ( componentType == char.class ) type |= Mix.INT_16 | Mix.UNSIGN_MASK;
        else if ( componentType == int.class ) type |= Mix.INT_32;
        else if ( componentType == long.class ) type |= Mix.INT_64;
        else if ( componentType == double.class ) type |= Mix.DOUBLE;
        else throw new RuntimeException("unsupported type "+componentType.getName());
        writeOut(type);
        writeIntPacked(len);
        int numBytes = Mix.extractNumBytes(type);
        for ( int i = start; i < start+len; i++ ) {
            if ( componentType == boolean.class )
                writeRawInt((byte) numBytes, Array.getBoolean(primitiveArray, i) ? 1 : 0 );
            else if ( componentType == double.class )
                writeRawInt((byte) 3, Double.doubleToLongBits(Array.getDouble(primitiveArray,i)));
            else
                writeRawInt((byte) numBytes, Array.getLong(primitiveArray, i));
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // standard atoms
    //

    public void writeDate(Date d) {
        writeTupelHeader(1,false);
        writeAtom(Mix.ATOM_DATE);
        writeInt(Mix.INT_64,d.getTime());
    }

    public void writeString( String s ) {
        writeTupelHeader(1,false);
        boolean isAsc = true;
        for (int i=0; i < s.length(); i++) {
            if (s.charAt(i) >= 127 ) {
                isAsc = false;
                break;
            }
        }
        if (isAsc) {
            writeAtom(Mix.ATOM_STR_8);
            final byte[] bytes = s.getBytes();
            writeArray(bytes, 0, bytes.length );
        } else {
            writeAtom(Mix.ATOM_STR_16);
            final char[] chars = s.toCharArray();
            writeArray(chars, 0, chars.length );
        }
    }

    public void writeAtom(Mix.Atom atom) {
        if (atom.getId() < 16 )
            writeOut((byte) (Mix.ATOM|atom.getId()<<4));
        else {
            writeOut(Mix.ATOM);
            writeIntPacked(atom.getId());
        }
    }

    public int getWritten() {
        return pos;
    }

    public byte[] getBytez() {
        return bytez;
    }

    /**
     * completely reset state
     */
    public void reset() {
        pos = 0;        
    }

    /**
     * completely reset and use given bytearray as buffer
     * @param bytez
     */
    public void reset(byte[] bytez) {
        pos = 0;
        this.bytez = bytez;
    }

    /**
     * only reset position (after flush)
     */
    public void resetPosition() {
        pos = 0;
    }
}
