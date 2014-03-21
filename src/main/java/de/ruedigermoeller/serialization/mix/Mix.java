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
 * Date: 19.03.14
 * Time: 22:38
 * To change this template use File | Settings | File Templates.
 */
public class Mix {

    public final static byte INT_8  = 0b00000001;
    public final static byte INT_16 = 0b00010001;
    public final static byte INT_32 = 0b00100001;
    public final static byte INT_64 = 0b00110001;
    public final static byte DOUBLE = 0b0010;
    public final static byte ARRAY  = 0b0011; // len type elems..
    public final static byte TUPEL  = 0b0100; // len id elems..

    public static class Out {

        public void write(byte b) {
        }

        public void writeArrayHeader( byte type, int len ) {
            write(ARRAY);
            if ( len < Short.MAX_VALUE )
                writeInt(INT_16,len);
            else
                writeInt(INT_32,len);
            if ( (type & 0xF) > 2 ) {
                throw new RuntimeException("An array may only contain primitive values");
            }
            write(type);
        }

        /**
         * writes tag+len. First next object must be tupel tag, then len elements
         * @param len
         */
        public void writeTupelHeader( int len ) {
            write(TUPEL);
            if ( len < Short.MAX_VALUE )
                writeInt(INT_16,len);
            else
                writeInt(INT_32,len);
            write(TUPEL);
        }

        public void writeInt( byte type,   long data ) {
            write(type);
            if ( type == DOUBLE) {
                type = INT_64;
            }
            writeRawInt(type, data);
        }

        public void writeRawInt(byte type, long data) {
            for ( int i = 0; i < (type&0xf); i++ ) {
                write( (byte) (data&0xFF) );
                data >>>= 8;
            }
        }

        public void writeRawDouble(byte type, double data) {
            writeRawInt(INT_64, Double.doubleToLongBits(data));
        }

        public void writeDouble( double d ) {
            write(DOUBLE);
            writeRawDouble(DOUBLE, Double.doubleToLongBits(d));
        }

    }

    public static class In {
        
        public byte read() {
            return 0;
        }
        
        public Object readObject() {
            return readObject(read());
        }

        public double readDouble() {
            return Double.longBitsToDouble(readInt());
        }

        public long readInt() {
            byte type = read();
            return readInt(type);
        }

        private long readInt(byte type) {
            long res = 0;
            for ( int i = 0; i < (type&0xf); i++ ) {
                int b = (read()+256)&0xff;
                res += b<<(i*2);
            }
            return res;
        }

        protected Object readObject(byte typeTag) {
            switch (typeTag&0xf) {
                case INT_8:
                    return new IntValue(typeTag,readInt(typeTag));
                case DOUBLE:
                    return new DoubleValue(Double.longBitsToDouble(readInt(typeTag)));
                case TUPEL:
                    return readTupel();
                case ARRAY:
                    return readArray();
                default:
                    throw new RuntimeException("expected tupel or array");
            }
//            return null;
        }

        private Object readArray() {
            IntValue len = (IntValue) readObject();
            byte type = read();
            ArrayValue val = new ArrayValue(type, (int) len.longValue());
            return val;
        }

        protected Object readTupel() {
            return null;            
        }

    }

    public static class Value {
        protected byte type;
    }

    public static class ArrayValue extends Value {
        Object array;
        public ArrayValue(byte type, int len) {
            this.type = type;
            switch (type>>>4) {
                case 0: array = new byte[len]; break;
                case 1: array = new short[len]; break;
                case 2: array = new int[len]; break;
                case 3: array = new long[len]; break;
                default: throw new RuntimeException("unknown array type");
            }
        }

        public void setDouble( int index, double val ) {
            Array.setLong(array, index, Double.doubleToLongBits(val));
        }

        public void setInt( int index, long val ) {
            switch (type>>>4) {
                case 0: Array.setByte(array, index, (byte) val); break;
                case 1: Array.setShort(array, index, (short) val); break;
                case 2: Array.setInt(array, index, (int) val); break;
                case 3: Array.setLong(array, index, val); break;
                default: throw new RuntimeException("unknown array type");
            }
        }

    }

    public static class DoubleValue extends Value {
        double val;
        public DoubleValue(double val) {
            this.val = val;
            type = DOUBLE;
        }
    }

    public static class IntValue extends Value {
        long val;
        public IntValue(byte type, long val) {
            this.val = val;
            this.type = type;
        }
        long longValue() { return val; }
    }

    public static class Tupel {
        ArrayList content;
    }
    
}
