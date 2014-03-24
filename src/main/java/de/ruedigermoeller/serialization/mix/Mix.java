package de.ruedigermoeller.serialization.mix;

import com.cedarsoftware.util.DeepEquals;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Date: 19.03.14
 * Time: 22:38
 * To change this template use File | Settings | File Templates.
 */
public class Mix {

    // numbers: low 4bit == 1, high 4 bit denote length of int. if > int 64 length of floating point id
    public final static byte INT_8      = 0b00000001;
    public final static byte INT_16     = 0b00010001;
    public final static byte INT_32     = 0b00100001;
    public final static byte INT_64     = 0b00110001;

    public final static byte ARRAY_MASK = (byte)0b10000000; // next item expected to be length
    public final static byte UNSIGN_MASK =  0b01000000; // next item expected to be unsigned

    public final static byte CHAR       = INT_16|UNSIGN_MASK;
    
    //    public final static byte FLOAT      = 0b01000001;
    public final static byte DOUBLE     = 0b00000010;

    public final static byte TUPEL    = 0b00000011; // id elems .. OR top 4 bits contains len if < 16
    public final static byte ATOM     = 0b00000100; // nr of atom .. OR top 4 bits contains atom id if < 16

    // default atoms full ids (hi 4 = id, low 4 = atom
    public final static byte NULL      = 0b00010100;
    public final static byte TUPEL_END = 0b00100100;
    public final static byte STR_8     = 0b00110100;
    public final static byte STR_16    = 0b01000100;
    public final static byte MAP       = 0b01010100;
    public final static byte DATE      = 0b01100100;

    // global Atom instances 
    private static final Atom ATOM_TUPEL_END = new Atom("tuple_end", TUPEL_END>>>4);
    private static final Atom ATOM_NULL = new Atom("nil", NULL>>>4);
    private static final Atom ATOM_STR_8 = new Atom("string8", STR_8>>>4);
    private static final Atom ATOM_STR_16 = new Atom("string", STR_16>>>4);
    private static final Atom ATOM_MAP = new Atom("map", MAP>>>4); // key, val, key, val, ....
    private static final Atom ATOM_DATE = new Atom("date", DATE>>>4); // long

    /**
     *
     * @param type
     * @return 0 - 1 byte, 1 = 2 byte, 2 = 4 byte, 3 = 8 byte
     */
    public static byte extractNumBytes(byte type) {
        return (byte) ((type&0b110000)>>>4);
    }

    public static class Out {

        byte bytez[] = new byte[1000];
        int pos = 0;
        
        private void writeOut(byte b) {
            bytez[pos++] = b;
        }

        /**
         * writes tag+len. First next object must be tupel tag, then len elements.
         * If len is unknown, -1 can be provided. The end of the tupel must be marked with
         * a TUPEL_END atom
         * @param len
         */
        public void writeTupelHeader( long len ) {
            if (len < 16 && len > 0 )
                writeOut((byte) (TUPEL|len<<4));
            else {
                writeOut(TUPEL);
                writeIntPacked(len);
            }
        }

        public void writeDouble( double d ) {
            writeOut(DOUBLE);
            final long data = Double.doubleToLongBits(d);
            writeRawInt((byte) 3, data);
        }

        public void writeInt( byte type, long data ) {
            writeOut(type);
            writeRawInt(extractNumBytes(type), data);
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
                writeInt(INT_8, data);
            else if ( data <= Short.MAX_VALUE && data >= Short.MIN_VALUE )
                writeInt(INT_16, data);
            else if ( data <= Integer.MAX_VALUE && data >= Integer.MIN_VALUE )
                writeInt(INT_32, data);
            else if ( data <= Long.MAX_VALUE && data >= Long.MIN_VALUE )
                writeInt(INT_64, data);
        }

        public void writeArray( Object primitiveArray, int start, int len ) {
            byte type = ARRAY_MASK;
            Class<?> componentType = primitiveArray.getClass().getComponentType();
            if ( componentType == boolean.class ) type |= INT_8;
            else if ( componentType == byte.class ) type |= INT_8;
            else if ( componentType == short.class ) type |= INT_16;
            else if ( componentType == char.class ) type |= INT_16 | UNSIGN_MASK;
            else if ( componentType == int.class ) type |= INT_32;
            else if ( componentType == long.class ) type |= INT_64;
            else if ( componentType == double.class ) type |= DOUBLE;
            else throw new RuntimeException("unsupported type "+componentType.getName());
            writeOut(type);
            writeIntPacked(len);
            int numBytes = extractNumBytes(type);
            for ( int i = start; i < start+len; i++ ) {
                if ( componentType == boolean.class )
                    writeRawInt((byte) numBytes, Array.getBoolean(primitiveArray,i) ? 1 : 0 );
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
            writeTupelHeader(1);
            writeAtom(ATOM_DATE);
            writeInt(INT_64,d.getTime());
        }

        public void writeString( String s ) {
            writeTupelHeader(1);
            boolean isAsc = true;
            for (int i=0; i < s.length(); i++) {
                if (s.charAt(i) >= 127 ) {
                    isAsc = false;
                    break;
                }
            }
            if (isAsc) {
                writeAtom(ATOM_STR_8);
                final byte[] bytes = s.getBytes();
                writeArray(bytes, 0, bytes.length );
            } else {
                writeAtom(ATOM_STR_16);
                final char[] chars = s.toCharArray();
                writeArray(chars, 0, chars.length );
            }
        }

        public void writeAtom(Atom atom) {
            if (atom.getId() < 16 )
                writeOut((byte) (ATOM|atom.getId()<<4));
            else {
                writeOut(ATOM);
                writeIntPacked(atom.getId());
            }
        }
    }

    public static class In {

        protected byte bytez[];
        protected int pos;

        public In(byte[] bytez, int pos) {
            this.bytez = bytez;
            this.pos = pos;
        }

        public byte readIn() {
            return bytez[pos++];
        }
        
        public long readInt() {
            byte type = readIn();
            if ( (type & 0xf) >= DOUBLE || ((type&ARRAY_MASK)!=0)) {
                pos--;
                throw new RuntimeException("no integer based id avaiable");
            }
            byte numBytes = extractNumBytes(type);
            long l = readRawInt(numBytes);
            if ( (type & UNSIGN_MASK) == 0 ) {
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
            if ( (type & 0xf) != DOUBLE ) {
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
                case INT_8:
                    if ( (typeTag&ARRAY_MASK) != 0 )
                        return readArray(typeTag);
                    byte len = extractNumBytes(typeTag);
                    switch ( len ) {
                        case 0: return (byte)readRawInt(len);
                        case 1: if ((typeTag&UNSIGN_MASK) == 0) 
                                    return new Short((short) readRawInt(len));
                                else
                                    return new Character((char) readRawInt(len));
                        case 2: return (int)readRawInt(len);
                        case 3: return new Long(readRawInt(len));
                    }
                case DOUBLE:
                    if ( (typeTag&ARRAY_MASK) != 0 )
                        return readArray(typeTag);
                    return Double.longBitsToDouble(readRawInt((byte) 3));
                case TUPEL:
                    return readTupel(typeTag);
                case ATOM:
                    if ( typeTag == TUPEL_END)
                        return ATOM_TUPEL_END;
                    if ( typeTag == STR_16)
                        return ATOM_STR_16;
                    if ( typeTag == STR_8)
                        return ATOM_STR_8;
                    if ( typeTag == MAP)
                        return ATOM_MAP;
                    if ( typeTag == DATE)
                        return ATOM_DATE;
                    if ( (typeTag>>>4) == 0 )
                        return new Atom((int) readInt());
                    else
                        return new Atom(typeTag>>>4);
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
            byte typelen = extractNumBytes(type);
            byte baseType = (byte) (type&0xf);
            int len = (int) readInt(); 
            Object result = null;
            if ( baseType == INT_8 ) {
                switch (typelen) {
                    case 0:
                        result = new byte[len];
                        break;
                    case 1:
                        if ((type & UNSIGN_MASK) != 0)
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
            } else if (baseType == DOUBLE) {
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
            if ( (tag>>>4) > 0)
                len = (tag>>>4)&0xf;
            else
                len = (int) readInt();
            Object type = readValue(); // id
            Object res = readCustomTupel( len, type );
            if ( res == null ) {
                res = readBuiltInTupel(len, type);
            }
            if ( res == null ) {
                return readDefaultTupel(len, type);
            }
            return res;
        }

        protected Object readBuiltInTupel(int len, Object type) {
            if (type==ATOM_STR_8) {
                final byte[] bytes = (byte[]) readValue();
                return new String(bytes, 0, 0, bytes.length );
            }
            if (type==ATOM_STR_16) {
                final char[] chars = (char[]) readValue();
                return new String(chars, 0, chars.length );
            }
            return null;
        }

        protected Object readDefaultTupel(int len, Object type) {
            if ( len == -1 ) { // unknown length
                ArrayList cont = new ArrayList();
                Object read;
                while( (read = readValue()) != ATOM_TUPEL_END ) {
                    cont.add(read);
                }
                return new Tupel(type,cont.toArray());
            } else {
                Object res[] = new Object[len];
                for ( int i = 0; i < len; i++ ) {
                    res[i] = readValue();
                }
                return new Tupel(type,res);
            }
        }

        protected Object readCustomTupel(int len, Object type) {
            return null;
        }

    }

    public static class Tupel {
        Object content[];
        Object id;

        public Tupel(Object id, Object[] content) {
            this.content = content;
            this.id = id;
        }

        public Object[] getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "Tupel{ id= "+id+", "+
                    "content=" + Arrays.deepToString(content) +
                    '}';
        }
    }
    
    public static class Atom {
        int id = 0;
        String name;
        public Atom(int value) {
            this.id = value;
        }
        public Atom(String name, int value) {
            this.id = value;
            this.name = name;
        }
        
        @Override
        public int hashCode() { return id; }
        @Override
        public boolean equals( Object o ) { return o instanceof Atom && ((Atom) o).id == id; }
        public int getId() { return id; }
        @Override
        public String toString() {
            return "#" + name + "("+ id +')';
        }
    }


    public static void main( String a[] ) {
        Mix.Out out = new Out();
        boolean bool[] = { true, false, false, false, true };
        byte bytes[] = { 0,1,-1,Byte.MAX_VALUE, Byte.MIN_VALUE };
        char chars[] = { 45345, 24234, 354, 0, 65535 };
        short shorts[] = { 5345, -24234, 354, 0, 5535 };
        int ints[] = { -345345, 234234234, -234234654, 0, -1 };
        double doubles[] = { 345.345, 123123.459867, 0.0 };
        long longs[] = {123123123123l,-4356456456456l,12313,3,-1, Long.MAX_VALUE, Long.MIN_VALUE };

        out.writeInt(INT_8, 99);
        out.writeInt(INT_8, -126);
        out.writeInt(CHAR, 34533);
        out.writeInt(CHAR, 14533);
        out.writeInt(INT_16, Short.MAX_VALUE);
        out.writeInt(INT_16, Short.MIN_VALUE);
        out.writeInt(INT_32, 1234567);
        out.writeInt(INT_32, -1234567);
        out.writeInt(INT_32, Integer.MAX_VALUE);
        out.writeInt(INT_32, Integer.MIN_VALUE);
        out.writeInt(INT_64, Long.MAX_VALUE);
        out.writeInt(INT_64, Long.MIN_VALUE);
        out.writeDouble(1.234);
        out.writeDouble(-1.234);
        out.writeArray(bool, 0, bool.length);
        out.writeArray(bytes, 0, bytes.length);
        out.writeArray(chars,0,chars.length);
        out.writeArray(shorts,0,shorts.length);
        out.writeArray(longs, 0, longs.length);
        out.writeArray(ints, 0, ints.length);
        out.writeArray(doubles,0,doubles.length);
        
        out.writeString("Hallo");
        out.writeString("Hallöää");

        out.writeTupelHeader(1);
        out.writeAtom(ATOM_STR_8); // type of tupel (optional/hint)
        out.writeArray("hallO".getBytes(), 0, 5);

        out.writeTupelHeader(8);
        out.writeAtom(ATOM_MAP);
        out.writeString("key"); out.writeString("value");
        out.writeString("key1"); out.writeInt(INT_32, 23423);
        out.writeString("wide"); out.writeString("üölPÖÄ");
        out.writeString("date"); out.writeDate(new Date());

        out.writeAtom(ATOM_TUPEL_END);



        System.out.println("POK"+out.pos);

        In in = new In(out.bytez, 0);
        Object read = null;
        do {
            read = in.readValue();
            if ( read instanceof Character )
                System.out.println(read.getClass().getSimpleName()+" "+(int)((Character) read).charValue());
            else
                System.out.println(read.getClass().getSimpleName()+" "+read);

            if ( read instanceof byte[] && ((byte[]) read).length != 5 ) { // one wrong because of bool[]
                System.out.println("BYTES:"+DeepEquals.deepEquals(read,bytes));
            }
            if ( read instanceof char[] ) {
                System.out.println("CHARS:"+DeepEquals.deepEquals(read,chars));
            }
            if ( read instanceof short[] ) {
                System.out.println("SHORTS:"+DeepEquals.deepEquals(read,shorts));
            }
            if ( read instanceof int[] ) {
                System.out.println("INTS:"+DeepEquals.deepEquals(read,ints));
            }
            if ( read instanceof double[] ) {
                System.out.println("DBLS:"+DeepEquals.deepEquals(read,doubles));
            }
            if ( read instanceof long[] ) {
                System.out.println("LONG:"+DeepEquals.deepEquals(read,longs));
            }
        } while( read != ATOM_TUPEL_END);
    }

}
