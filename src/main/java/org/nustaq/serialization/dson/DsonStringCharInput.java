package org.nustaq.serialization.dson;

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
 * Date: 20.12.13
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */

/**
 * implementation of char input on top of a String
 */
public class DsonStringCharInput implements DsonCharInput {
    String s;
    int pos;
    int end;

    public DsonStringCharInput(String s) {
        this.s = s;
        pos = 0;
        end = s.length();
    }

    public DsonStringCharInput(String s, int pos, int len) {
        this.s = s;
        this.pos = pos;
        this.end = pos+len;
    }

    /**
     * @return char or -1 for eof
     */
    @Override
    public int readChar() {
        if ( pos >= end )
            return -1;
        return s.charAt(pos++);
    }

    @Override
    public int peekChar() {
        if ( pos >= end )
            return -1;
        return s.charAt(pos);
    }

    @Override
    public int position() {
        return pos;
    }

    @Override
    public int back(int num) {
        return pos--;
    }

    @Override
    public String getString(int pos, int length) {
        return s.substring(Math.max(0,pos),Math.min(s.length(),pos+length));
    }
}
