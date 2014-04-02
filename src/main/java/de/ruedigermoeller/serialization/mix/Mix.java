package de.ruedigermoeller.serialization.mix;

import com.cedarsoftware.util.DeepEquals;
import de.ruedigermoeller.serialization.LeanMap;

import java.io.PrintStream;
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

    public final static byte TUPEL    = 0b00000011; // id elems .. OR top 4 bits contain len < 16. == Object Array
    public final static byte OBJECT   = 0b00000100; // id elems .. OR top 4 bits contain len < 16  == key<String> value<Object>
    public final static byte ATOM     = 0b00000101; // nr of atom .. OR top 4 bits contains atom id if < 16

    // default atoms full ids (hi 4 = id, low 4 = atom
    public final static byte NULL      = 0b00010000|ATOM;
    public final static byte TUPEL_END = 0b00100000|ATOM;
    public final static byte STR_8     = 0b00110000|ATOM;
    public final static byte STR_16    = 0b01000000|ATOM;
    public final static byte MAP       = 0b01010000|ATOM; // generic map key, value
    public final static byte DATE      = 0b01100000|ATOM; 
    public final static byte ARR       = 0b01110000|ATOM; // generic object array or collection

    // global Atom instances 
    public static final Atom ATOM_TUPEL_END = new Atom("tuple_end", TUPEL_END>>>4);
    public static final Atom ATOM_NULL = new Atom("nil", NULL>>>4);
    public static final Atom ATOM_STR_8 = new Atom("string8", STR_8>>>4);
    public static final Atom ATOM_STR_16 = new Atom("string", STR_16>>>4);
    public static final Atom ATOM_MAP = new Atom("map", MAP>>>4); // key, val, key, val, ....
    public static final Atom ATOM_DATE = new Atom("date", DATE>>>4); // long
    public static final Atom ATOM_ARR = new Atom("array", ARR >>> 4); // tupel array

    /**
     *
     * @param type
     * @return 0 - 1 byte, 1 = 2 byte, 2 = 4 byte, 3 = 8 byte
     */
    public static byte extractNumBytes(byte type) {
        return (byte) ((type&0b110000)>>>4);
    }

    public static String arrayToString(Object o) {
        int len = Array.getLength(o);
        Class c = o.getClass().getComponentType();
        String res = "[ ";
        for (int i = 0; i < len; i++) {
            if ( c == byte.class ) res+=Array.getByte(o,i);
            if ( c == boolean.class ) res+=Array.getBoolean(o, i);
            if ( c == char.class ) res+=Array.getChar(o, i);
            if ( c == short.class ) res+=Array.getShort(o, i);
            if ( c == int.class ) res+=Array.getInt(o, i);
            if ( c == long.class ) res+=Array.getLong(o, i);
            if ( c == float.class ) res+=Array.getFloat(o, i);
            if ( c == double.class ) res+=Array.getDouble(o, i);
            if ( i < len-1)
                res+=",";
        }
        res += " ]";
        return res;
    }

    public static String objectToString(Object o) {
        if ( o.getClass().isArray() )
            return arrayToString(o);
        if ( o instanceof String ) {
            return "\""+o+"\"";
        }
        if ( o instanceof Character )
            return "'"+o+"'("+(int)((Character) o).charValue()+")";
//        if ( o instanceof Byte )
//            return "b"+o;
//        if ( o instanceof Short )
//            return "s"+o;
//        if ( o instanceof Integer )
//            return "i"+o;
//        if ( o instanceof Long )
//            return "L"+o;
//        if ( o instanceof Double )
//            return "d"+o;
        return ""+o;
    }

    interface PrettyPrintable {
        public void prettyPrint(PrintStream out, String indent);
    }

    public static class Tupel implements PrettyPrintable, LeanMap {
        Object content[];
        Object id;
        boolean isStrMap;

        public Tupel(Object id, Object[] content, boolean isStrMap) {
            this.content = content;
            this.id = id;
            this.isStrMap = isStrMap;
        }

        public Object getId() {
            return id;
        }

        public boolean isStrMap() {
            return isStrMap;
        }

        public Object[] getContent() {
            return content;
        }

        public void prettyPrint(PrintStream out, String indent) {
            if (id instanceof PrettyPrintable) {
                ((PrettyPrintable) id).prettyPrint(out,indent+"  ");
            }
            else
                out.print(objectToString(id));
            out.println(" {");
            for (int i = 0; i < content.length; i++) {
                Object o = content[i];
                if ( id == ATOM_MAP || isStrMap ) {
                    if ( (i&1) == 1 ) {
                        if (o instanceof PrettyPrintable)
                            ((PrettyPrintable) o).prettyPrint(out,indent+"  ");
                        else
                            out.print(objectToString(o));
                        out.println();
                    } else {
                        out.print(indent+"  ");
                        if (o instanceof PrettyPrintable)
                            ((PrettyPrintable) o).prettyPrint(out,indent+"  ");
                        else
                            out.print(objectToString(o));
                        out.print(" : ");
                    }
                } else {
                    out.print(indent+"  ");
                    if (o instanceof PrettyPrintable)
                        ((PrettyPrintable) o).prettyPrint(out,indent+"  ");
                    else
                        out.print(objectToString(o));
                    out.println();
                }
            }
            out.print(indent + "}");
        }

        @Override
        public String toString() {
            return "Tupel{ id= "+id+", "+
                    "content=" + Arrays.deepToString(content) +
                    '}';
        }

        public Object get(Object key) {
            for (int i = 0; i < content.length; i+=2) {
                Object o = content[i];
                if ( o.equals(key) ) {
                    return content[i];
                }
            }
            return NOT_FOUND;
        }

    }
    
    public static class Atom implements PrettyPrintable{
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

        @Override
        public void prettyPrint(PrintStream out, String indent) {
            out.print("#" + name + "(" + id + ")");
        }
    }


    public static void main( String a[] ) {
        MixOut out = new MixOut();
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

        out.writeTupelHeader(1,false);
        out.writeAtom(ATOM_STR_8); // type of tupel (optional/hint)
        out.writeArray("hallO".getBytes(), 0, 5);

        out.writeTupelHeader(10,false);
        out.writeAtom(ATOM_MAP);
        out.writeString("key"); out.writeString("value");
        out.writeString("key1");
            out.writeTupelHeader(4,true);
            out.writeString("person");
            out.writeString("name");out.writeString("Rüdiger");
            out.writeString("city");out.writeString("Frankfurt");
        out.writeString("wide"); out.writeString("üölPÖÄ");
        out.writeString("date"); out.writeDate(new Date());
        out.writeString("key2"); out.writeInt(INT_32, 23423);

        out.writeAtom(ATOM_TUPEL_END);



        System.out.println("POK"+out.pos);

        MixIn in = new MixIn(out.bytez, 0);
        Object read = null;
        ArrayList doc = new ArrayList();
        do {
            read = in.readValue();
            doc.add(read);
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
        new Tupel("doc", doc.toArray(),false).prettyPrint(System.out,"");
    }

}
