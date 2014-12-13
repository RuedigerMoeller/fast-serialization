/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nustaq.offheap.structs.structtypes;

import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.offheap.structs.NoAssist;

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
        int l = len;
        int h = 0;
        if (l > 0) {
            for (int i = 0; i < len; i++) {
                h = 31 * h + chars(i);
            }
        }
        return h;
    }

    @NoAssist
    public boolean equals( Object o ) {
        if ( o instanceof StructString ) {
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

    @Override @NoAssist
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
