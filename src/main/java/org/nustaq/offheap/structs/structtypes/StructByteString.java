package org.nustaq.offheap.structs.structtypes;

import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.offheap.structs.NoAssist;

/**
 * Created by ruedi on 08.12.2014.
 */
public class StructByteString extends FSTStruct implements Comparable {

    protected int len = 0;
    protected byte chars[];

    public StructByteString(int size) {
        chars = new byte[size];
        len = 0;
    }

    public StructByteString(String init, int size) {
        chars = new byte[size];
        len = 0;
        setString(init);
    }

    public StructByteString(String s) {
        chars = s.getBytes();
        len = s.length();
    }

    /**
     * modify content of this StructString. The length of the new String must not exceed
     * the length of internal char array
     *
     * @param s
     */
    public void setString(String s) {
        if (s == null) {
            setLen(0);
            return;
        }
        if (s.length() > charsLen()) {
            throw new RuntimeException("String length exceeds buffer size. String len " + s.length() + " charsLen:" + charsLen());
        }
        for (int i = 0; i < s.length(); i++) {
            chars(i, (byte) (s.charAt(i)&0xff));
        }
        len = s.length();
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public void chars(int i, byte val) {
        chars[i] = val;
    }

    public byte chars(int i) {
        return chars[i];
    }

    public int compareTo(StructByteString str) {
        int l1 = len;
        int l2 = str.getLen();
        int max = Math.min(l1, l2);
        int i = 0;
        while (i < max) {
            byte c1 = chars(i);
            byte c2 = str.chars(i);
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
            char c1 = (char) ((chars(i)+256)&0xff);
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
        byte ch[] = new byte[len];
        for (int i = 0; i < len; i++) {
            ch[i] = chars(i);
        }
        return new String(ch,0);
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
    public boolean equals(Object o) {
        if (o instanceof StructByteString) {
            StructByteString ss = (StructByteString) o;
            if (ss.getLen() != getLen()) {
                return false;
            }
            for (int i = 0; i < ss.getLen(); i++) {
                if (ss.chars(i) != chars(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    @NoAssist
    public int compareTo(Object o) {
        if (o instanceof StructByteString) {
            return compareTo((StructByteString) o);
        }
        return -1;
    }

    @Override
    @NoAssist
    public Object getFieldValues() {
        return toString();
    }

}