package de.ruedigermoeller.heapoff.structs.structtypes;

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
 * Date: 28.06.13
 * Time: 20:50
 *
 */

import de.ruedigermoeller.heapoff.structs.Align;
import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.NoAssist;

/**
 * this class can be used to represent strings in structs. the content is rewritable, but cannot grow.
 * max length is determined once you create the struct object.
 */
public class StructString extends FSTStruct implements Comparable {

    protected int len = 0;
    protected char chars[];

    public StructString(int size) {
        chars = new char[size];
        len = 0;
    }

    public StructString(String init, int size) {
        chars = new char[size];
        len = 0;
        setString(init);
    }

    public StructString(String s) {
        chars = s.toCharArray();
        len = s.length();
    }

    /**
     * modify content of this StructString. The length of the new String must not exceed
     * the length of internal char array
     * @param s
     */
    public void setString(String s) {
        if ( s == null ) {
            setLen(0);
            return;
        }
        if ( s.length() > charsLen() ) {
            throw new RuntimeException("String length exceeds buffer size. String len "+s.length()+" charsLen:"+charsLen());
        }
        for (int i=0; i < s.length(); i++) {
            chars(i,s.charAt(i));
        }
        len = s.length();
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public void chars(int i, char val) {
        chars[i] = val;
    }

    public char chars(int i) {
        return chars[i];
    }

    public int compareTo(StructString str) {
        int l1 = len;
        int l2 = str.getLen();
        int max = Math.min(l1, l2);
        int i = 0;
        while (i < max) {
            char c1 = chars(i);
            char c2 = str.chars(i);
            if (c1 != c2) {
                return c1 - c2;
            }
            i++;
        }
        return l1 - l2;
    }

    public int compareToString(String str) {
        int l1 = len;
        int l2 = str.length();
        int max = Math.min(l1, l2);
        int i = 0;
        while (i < max) {
            char c1 = chars(i);
            char c2 = str.charAt(i);
            if (c1 != c2) {
                return c1 - c2;
            }
            i++;
        }
        return l1 - l2;
    }

    public int charsLen() {
        return chars.length;
    }

    public String toString() {
        // fixme: optimize this by direct copy
        char ch[] = new char[len];
        for ( int i=0; i < len; i++ ) {
            ch[i] = chars(i);
        }
        return new String(ch);
    }

    @Override
    public int hashCode() {
        // well that's a dummy implementation ..
        if ( len > 6)
            return chars(0)+chars(3)<<16+chars(len-1)<<32+chars(len-3)<<48;
        else if ( len > 1)
            return chars(0)+chars(1)<<16+chars(len-1)<<32+chars(len-2)<<48;
        else if ( len > 0 )
            return chars(0);
        return 97979797;
    }

    @NoAssist
    public boolean equals( Object o ) {
        if ( o instanceof StructString) {
            StructString ss = (StructString) o;
            if ( ss.getLen() != getLen() ) {
                return false;
            }
            for ( int i = 0; i < ss.getLen(); i++ ) {
                if ( ss.chars(i) != chars(i) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Object o) {
        if ( o instanceof StructString) {
            return compareTo((StructString)o);
        }
        return -1;
    }

    @Override @NoAssist
    public Object getFieldValues() {
        return toString();
    }
}
