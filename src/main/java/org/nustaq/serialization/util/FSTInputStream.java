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
package org.nustaq.serialization.util;

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

    public int chunk_size = 5000;
    public static ThreadLocal<byte[]> cachedBuffer = new ThreadLocal<byte[]>();
    public byte buf[];
    public int pos;
    public int count; // avaiable valid read bytes
    InputStream in;
    boolean fullyRead = false;
    public boolean byteBacked = false;

    public FSTInputStream(InputStream in) {
        initFromStream(in);
    }

    public void initFromStream(InputStream in) {
        fullyRead = false;
        byteBacked = false;
        pos = 0;
        this.in = in;
        if (buf == null) {
            buf = cachedBuffer.get();
            if (buf == null) {
                buf = new byte[chunk_size];
                cachedBuffer.set(buf);
            }
        }
        readNextChunk(in);
    }

    public boolean isFullyRead() {
        return fullyRead && pos >= count;
    }

    public void readNextChunk(InputStream in) {
        int read;
        try {
            if (buf.length < count + chunk_size) {
                ensureCapacity(Math.max(buf.length * 2, count + chunk_size));
            }
            read = in.read(buf, count, chunk_size);
            if (read > 0) {
                count += read;
            } else {
                fullyRead = true;
            }
        } catch (Exception iex) {
            fullyRead = true;
        }
    }

    public void ensureCapacity(int siz) {
        if (buf.length < siz) {
            byte newBuf[] = new byte[siz];
            System.arraycopy(buf, 0, newBuf, 0, buf.length);
            buf = newBuf;
            if (siz < 10 * 1024 * 1024) { // issue 19, don't go overboard with buffer caching
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

    public int read() {
        if (pos < count) {
            return (buf[pos++] & 0xff);
        }
        readNextChunk(in);
        if (fullyRead)
            return -1;
        return -1;
    }

    public int read(byte b[], int off, int len) {
        if (fullyRead)
            return -1;
        while (pos + len >= count) {
            readNextChunk(in);
            if (fullyRead)
                break;
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
        fullyRead = false;
        byteBacked = false;
    }

    public void close() throws IOException {
        in.close();
    }

    public void ensureReadAhead(int bytes) {
        if ( byteBacked )
            return;
        int targetCount = pos + bytes;
        while (!fullyRead && count < targetCount) {
            readNextChunk(in);
        }
    }
}
