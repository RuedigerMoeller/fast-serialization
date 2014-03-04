/*
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 */
package de.ruedigermoeller.serialization.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 27.11.12
 * Time: 00:35
 * To change this template use File | Settings | File Templates.
 */
public final class FSTInputStream extends InputStream {

    public int chunk_size = 1000;
    public static ThreadLocal<byte[]> cachedBuffer = new ThreadLocal<byte[]>();
    public byte buf[];
    public  int pos;
    public  int off;
    public  int count; // avaiable valid read bytes
    InputStream in;

    public FSTInputStream(InputStream in) {
        initFromStream(in);
    }

    public void initFromStream(InputStream in) {
        try {
            this.in = in;
            if ( in instanceof ByteArrayInputStream ) {
                int available = in.available();
                buf = cachedBuffer.get();
                if ( buf == null || buf.length < available) {
                    buf = new byte[available];
                    cachedBuffer.set(buf);
                }
                in.read(buf,0,available);
                count = available;
                return;
            }
            if (buf==null) {
                buf = cachedBuffer.get();
                if ( buf == null ) {
                    buf = new byte[chunk_size];
                    cachedBuffer.set(buf);
                }
            }
            int read = in.read(buf);
            count+=read;
            while( read != -1 ) {
                try {
                    if ( buf.length < count+chunk_size ) {
                        ensureCapacity(buf.length*2);
                    }
                    read = in.read(buf,count,chunk_size);
                    if ( read > 0 )
                        count += read;
                } catch ( IndexOutOfBoundsException iex ) {
                    read = -1; // many stream impls break contract
                }
            }
            in.close();
        } catch (IOException e) {
            FSTUtil.rethrow(e);
        }
    }

    public void ensureCapacity(int siz) {
        if ( buf.length < siz ) {
            byte newBuf[] = new byte[siz];
            System.arraycopy(buf,0,newBuf,0,buf.length);
            buf = newBuf;
            if ( siz < 10*1024*1024) { // issue 19, don't go overboard with buffer caching
                cachedBuffer.set(buf);
            }
        }
    }

    public FSTInputStream(byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    public FSTInputStream(byte buf[], int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
    }

    byte streamStack[][] = new byte[30][];
    int countStack[] = new int[30];
    int posStack[] = new int[30];
    int offStack[] = new int[30];
    int sp = 0;

    public void push(byte[] inb, int newpos, int newCount) {
        streamStack[sp] = buf;
        countStack[sp] = count;
        posStack[sp] = pos;
        offStack[sp] = off;
        sp++;
        buf = inb; pos = newpos;
        count = newCount;
    }

    public void pop() {
        sp--;
        buf = streamStack[sp];
        count = countStack[sp];
        pos = posStack[sp];
        off = posStack[sp];
    }

    public int read() {
        if  (pos < count) {
            return (buf[pos++] & 0xff);
        }
        return -1;
    }

    public int read(byte b[], int off, int len) {
        if (pos >= count) {
            return -1;
        }
        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    public long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }
        pos += k;
        return k;
    }

    public int available() {
        return count - pos;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readAheadLimit) {
    }

    public void reset() {
        count = 0;
        pos = 0;
        off = 0;
    }

    public void close() throws IOException {
        in.close();
    }

}
