package de.ruedigermoeller.serialization.mix;

import com.cedarsoftware.util.DeepEquals;

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

    // global Atom instances 
    public static final Atom ATOM_TUPEL_END = new Atom("tuple_end", TUPEL_END>>>4);
    public static final Atom ATOM_NULL = new Atom("nil", NULL>>>4);
    public static final Atom ATOM_STR_8 = new Atom("string8", STR_8>>>4);
    public static final Atom ATOM_STR_16 = new Atom("string", STR_16>>>4);

    /**
     *
     * @param type - int tag
     * @return 0 - 1 byte, 1 = 2 byte, 2 = 4 byte, 3 = 8 byte
     */
    static byte extractNumBytes(byte type) {
        return (byte) ((type&0b110000)>>>4);
    }

    public static byte[] toBytes(Tupel t) {
        MixOut out = new MixOut();
        t.write(out);
        byte res[] = new byte[out.getWritten()];
        System.arraycopy(out.getBytez(), 0, res, 0, out.getWritten());
        return res;
    }

    public static Tupel fromBytes(byte[] b) {
        MixIn in = new MixIn(b,0);
        return (Tupel) in.readValue();
    }
    
    public static class Tupel {
        public static final Object NOT_FOUND = "..__NOT_FOUND__..";
        Object content[];
        Object id;
        boolean isStrMap;

        public Tupel(Object id, Object ... content) {
            this( id, content, true);
        }

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

        @Override
        public String toString() {
            return "Tupel{ id= "+id+", "+
                    "content=" + Arrays.deepToString(content) +
                    '}';
        }

        public void write( MixOut out ) {
            out.writeTupelHeader(content.length,isStrMap);
            writeObject(id,out);
            for (int i = 0; i < content.length; i++) {
                Object o = content[i];
                writeObject(o,out);
            }
        }

        protected void writeObject(Object object, MixOut out) {
            if ( object instanceof String )
                out.writeString((String) object);
            else if ( object instanceof Number ) { // double not supported
                out.writeIntPacked(((Number) object).longValue());
            } else if ( object instanceof Atom) {
                ((Atom) object).write(out);
            } else if ( object instanceof Tupel) {
                ((Tupel) object).write(out);
            } else throw new RuntimeException("cant write object of type "+object.getClass().getName());
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

        public Object get(int index) {
            return content[index];
        }
        
        public int size() {
            return content.length;
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
        
        public void write( MixOut out ) {
            out.writeAtom(this);
        }
        
        public String getName() {
            return name;
        }
    }

}
