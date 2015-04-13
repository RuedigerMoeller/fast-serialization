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
package org.nustaq.offheap.bytez.bytesource;

import org.nustaq.offheap.bytez.ByteSource;

/**
 * Created by ruedi on 27.06.14.
 */
public class AsciiStringByteSource implements ByteSource {

    protected String string;
    protected int off;
    protected int len;

    public AsciiStringByteSource(String arr) {
        this.string = arr;
        off = 0;
        len = arr.length();
    }

    public AsciiStringByteSource(String arr, int off) {
        this.string = arr;
        this.off = off;
        len = string.length()-off;
    }

    public AsciiStringByteSource(String arr, int off, int len) {
        this.string = arr;
        this.off = off;
        this.len = len;
    }

    @Override
    public byte get(long index) {
        return (byte) string.charAt(((int) (index + off)));
    }

    @Override
    public long length() {
        return len;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        if ( string.length() > len )
            throw new RuntimeException("key value too long");
        this.string = string;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getOff() {
        return off;
    }

    public void setOff(int off) {
        this.off = off;
    }

    public String toString() {
        return string.substring(off,off+len);
    }
}
