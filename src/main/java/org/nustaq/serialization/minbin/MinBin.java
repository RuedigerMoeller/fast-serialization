package org.nustaq.serialization.minbin;

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

    public static final Object END_MARKER = "_E_";
    public static MinBin DefaultInstance = new MinBin();
    
    public final static byte INT_8  = 0b0001; // 1 (17 = array)
    public final static byte INT_16 = 0b0010; // 2 (18 ..)
    public final static byte INT_32 = 0b0011; // 3 (19 ..)
    public final static byte INT_64 = 0b0100; // 4 (20 ..)
    public final static byte TAG    = 0b0101; // 5, top 5 bits contains tag id
    public final static byte END    = 0b0110; // 6, end marker
    public final static byte RESERV = 0b0111; // escape for future extension

    public final static byte UNSIGN_MASK = 0b01000; // int only
    public final static byte ARRAY_MASK = 0b10000;// int only, next item expected to be length
    public final static byte CHAR   = UNSIGN_MASK|INT_16;

    /** return wether type is primitive or primitive array */
    public static boolean isPrimitive(byte type) { return (type & 0b111) < TAG; }
    /** return wether type is a tag */
    public static boolean isTag(byte type) { return (type & 0b111) == TAG; }
    /** extract tag id/nr from byte  */
    public static byte getTagId(byte type) { return (byte) (type >>> 3); }
    /** get tag code as written to stream from tag id  */
    public static byte getTagCode(byte tagId) { return (byte) ((tagId << 3)|TAG); }
    /** is primitive type signed ? */
    public static boolean isSigned(byte type) { return (type & 0b111) < TAG && (type & UNSIGN_MASK) == 0; }
    /** is primitive and array array */
    public static boolean isArray(byte type) {  return (type & 0b111) < TAG && (type & ARRAY_MASK) != 0; }
    public static byte extractNumBytes(byte type) { return (byte) (1 << ((type & 0b111)-1)); }

    // predefined tag id's
    public static final byte NULL = 7;
    public static final byte STRING = 0;
    public static final byte OBJECT = 5;
    public static final byte SEQUENCE = 6;
    public static final byte DOUBLE = 2;
    public static final byte DOUBLE_ARR = 3;
    public static final byte FLOAT = 1;
    public static final byte FLOAT_ARR = 4;
    public static final byte BOOL = 8;
    public static final byte HANDLE = 9;

    HashMap<Class,TagSerializer> clz2Ser = new HashMap<>();
    TagSerializer tag2Ser[] = new TagSerializer[32];
    int tagCount = 0;

    private TagSerializer nullTagSer = new MBTags.NullTagSer();

    public MinBin() {
        registerTag(new MBTags.StringTagSer());       // 0
        registerTag(new MBTags.FloatTagSer());        // 1
        registerTag(new MBTags.DoubleTagSer());       // 2
        registerTag(new MBTags.DoubleArrTagSer());    // 3
        registerTag(new MBTags.FloatArrTagSer());     // 4
        registerTag(new MBTags.MBObjectTagSer());     // 5
        registerTag(new MBTags.MBSequenceTagSer());   // 6
        registerTag(nullTagSer);                      // 7
        registerTag(new MBTags.BigBoolTagSer());      // 8
        registerTag(new MBTags.RefTagSer());          // 9
    }

    public void registerTag(TagSerializer ts) {
        registerTag(ts.getClassEncoded(),ts);
    }
    public void registerTag(Class clazz,TagSerializer ts) {
        ts.setTagId(tagCount++);
        if ( clazz != null )
            clz2Ser.put(clazz,ts);
        tag2Ser[ts.getTagId()]=ts;
    }

    public TagSerializer getSerializerForId(int tagId) {
        return tag2Ser[tagId];
    }

    public TagSerializer getSerializerFor(Object toWrite) {
        if ( toWrite == null ) {
            return nullTagSer;
        }
        return clz2Ser.get(toWrite.getClass());
    }

    /**
     * extract base type includin unsignend flag excluding array flag 
     * @param type
     * @return
     */
    public static byte getBaseType(byte type) {
        return (byte) ((type&0b111)|(type&UNSIGN_MASK));
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
        public abstract void writeTag(Object data, MBOut out);
        /**
         * tag is already read, reconstruct the object
         * @param in
         * @return
         */
        public abstract Object readTag(MBIn in);
        /**
         * @return the class this tag serializer is responsible for
         */
        public abstract Class getClassEncoded();
    }

}

