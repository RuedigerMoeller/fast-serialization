package de.ruedigermoeller.serialization.mix;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

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
 * Date: 06.04.2014
 * Time: 23:22
 * To change this template use File | Settings | File Templates.
 */
public class MinBin {
    
    public static MinBin DefaultInstance = new MinBin();    
    
    public final static byte INT_8  = 0b0001;
    public final static byte INT_16 = 0b0010;
    public final static byte INT_32 = 0b0011;
    public final static byte INT_64 = 0b0100;
    public final static byte TAG    = 0b0101; // top 5 bits contains tag id 

    public final static byte UNSIGN_MASK = 0b01000; // int only
    public final static byte ARRAY_MASK = 0b10000;// int only, next item expected to be length

    /** return wether type is primitive or primitive array */
    static boolean isPrimitive(byte type) { return (type & 0b111) < TAG; }
    /** return wether type is a tag */
    static boolean isTag(byte type) { return (type & 0b111) == TAG; }
    /** extract tag id from byte  */
    static byte getTagId(byte type) { return (byte) (type >>> 3); }
    /** get tag code from tag id  */
    static byte getTagCode(byte tagId) { return (byte) ((tagId << 3)|TAG); }
    /** is primitive type signed ? */
    static boolean isSigned(byte type) { return (type & 0b111) < TAG && (type & UNSIGN_MASK) == 0; }
    /** is primitieve type an array */
    static boolean isArray(byte type) {  return (type & 0b111) < TAG && (type & ARRAY_MASK) == 0; }

    HashMap<Class,TagSerializer> clz2Ser = new HashMap<>();
    int tagCount = 0;
    private TagSerializer nullTagSer = new TagSerializer() {
        @Override
        public void writeTag(Object data, Out out) {
        }
        @Override
        public Object readTag(In in) {
            return null;
        }
        @Override
        public Class getClassEncoded() {
            return Object.class;
        }
    };

    public MinBin() {
        
        registerTag(nullTagSer);
        registerTag(new TagSerializer() {
            @Override
            public void writeTag(Object data, Out out) {
                String s = (String) data;
                boolean isAsc = true;
                for (int i=0; i < s.length(); i++) {
                    if (s.charAt(i) >= 127 ) {
                        isAsc = false;
                        break;
                    }
                }
                if (isAsc) {
                    byte[] strBytes = s.getBytes();
                    out.writeArray(strBytes, 0, strBytes.length);
                } else {
                    final char[] chars = s.toCharArray();
                    out.writeArray(chars, 0, chars.length);
                }
            }
            @Override
            public Object readTag(In in) {
                return null;
            }
            @Override
            public Class getClassEncoded() {
                return String.class;
            }
        });
        registerTag(new TagSerializer() {
            @Override
            public void writeTag(Object data, Out out) {
                byte[] bytes = Double.toString((Double) data).getBytes();
                out.writeArray(bytes, 0, bytes.length);
            }
            @Override
            public Object readTag(In in) {
                return null;
            }
            @Override
            public Class getClassEncoded() {
                return Double.class;
            }
        });
    }

    public void registerTag(TagSerializer ts) {
        registerTag(ts.getClassEncoded(),ts);
    }
    public void registerTag(Class clazz,TagSerializer ts) {
        ts.setTagId(tagCount++);
        clz2Ser.put(clazz,ts);
    }

    private TagSerializer getSerializerFor(Object toWrite) {
        if ( toWrite == null ) {
            return nullTagSer;
        }
        return clz2Ser.get(toWrite.getClass());
    }


    public static abstract class TagSerializer {
        int tagId;
        public int getTagId() {
            return tagId;
        }
        public void setTagId(int tagId) {
            this.tagId = tagId;
        }
        /**
         * tag is already written. break down the given object into more tags or primitives
         * @param data
         * @param out
         */
        public abstract void writeTag(Object data, Out out);
        /**
         * tag is already read, reconstruct the object
         * @param in
         * @return
         */
        public abstract Object readTag(In in);
        /**
         * @return the class this tag serializer is responsible for
         */
        public abstract Class getClassEncoded();
    }
    
    public static class Out {
        byte bytez[] = new byte[500];
        int pos = 0;
        
        MinBin mb;

        public Out() {
            this(MinBin.DefaultInstance);
        }

        public Out(MinBin mb) {
            this.mb = mb;
        }

        /**
         * write single byte, grow byte array if needed
         * @param b
         */
        private void writeOut(byte b) {
            if (pos == bytez.length - 1) {
                byte tmp[] = new byte[Math.min(bytez.length + 50 * 1000 * 1000, bytez.length * 2)];
                System.arraycopy(bytez, 0, tmp, 0, pos);
                bytez = tmp;
            }
            bytez[pos++] = b;
        }
        /**
         * write an int type with header
         * @param type
         * @param data
         */
        public void writeInt(byte type, long data) {
            if (!isPrimitive(type) || isArray(type))
                throw new RuntimeException("illegal type code");
            writeOut(type);
            writeRawInt(type, data);
        }
        /**
         * encode int without header tag
         * @param data
         */
        protected void writeRawInt(byte type, long data) {
            int numBytes = (byte) (1 << (type & 0b111));
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
            if (data <= Byte.MAX_VALUE && data >= Byte.MIN_VALUE)            writeInt(INT_8,  data);
            else if (data <= Short.MAX_VALUE && data >= Short.MIN_VALUE)     writeInt(INT_16, data);
            else if (data <= Integer.MAX_VALUE && data >= Integer.MIN_VALUE) writeInt(INT_32, data);
            else if (data <= Long.MAX_VALUE && data >= Long.MIN_VALUE)       writeInt(INT_64, data);
        }

        /**
         * write primitive array + header. no floating point or object array allowed. Just int based types
         * @param primitiveArray
         * @param start
         * @param len
         */
        public void writeArray(Object primitiveArray, int start, int len) {
            byte type = ARRAY_MASK;
            Class<?> componentType = primitiveArray.getClass().getComponentType();
            if (componentType == boolean.class)    type |= INT_8;
            else if (componentType == byte.class)  type |= INT_8;
            else if (componentType == short.class) type |= INT_16;
            else if (componentType == char.class)  type |= INT_16 | UNSIGN_MASK;
            else if (componentType == int.class)   type |= INT_32;
            else if (componentType == long.class)  type |= INT_64;
            else throw new RuntimeException("unsupported type " + componentType.getName());
            writeOut(type);
            writeIntPacked(len);
            for (int i = start; i < start + len; i++) {
                if (componentType == boolean.class)
                    writeRawInt(type, Array.getBoolean(primitiveArray, i) ? 1 : 0);
                else
                    writeRawInt(type, Array.getLong(primitiveArray, i));
            }
        }

        public void writeTagHeader(byte tagId) {
            writeOut(getTagCode(tagId));
        }
        
        public void writeTag( Object obj ) {
            TagSerializer tagSerializer = mb.getSerializerFor(obj);
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
        
    }

    public static class In {

    }

}

