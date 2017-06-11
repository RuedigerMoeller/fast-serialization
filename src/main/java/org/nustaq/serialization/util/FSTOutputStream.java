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
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 19.11.12
 * Time: 10:00
 * To change this template use File | Settings | File Templates.
 */
public final class FSTOutputStream extends OutputStream {

    /**
     * The buffer where data is stored.
     */
    public byte buf[];
    /**
     * The number of valid bytes in the buffer.
     */
    public int pos;
    private OutputStream outstream;
    private int off;

    public FSTOutputStream(OutputStream out) {
        this(4000, out);
    }

    public FSTOutputStream(int size, OutputStream out) {
        buf = new byte[size];
        outstream = out;
    }

    public void setOutstream(OutputStream outstream) {
        this.outstream = outstream;
    }

    public final void ensureFree(int free) throws IOException {
        // inline ..
        if (pos + free - buf.length > 0)
            grow(pos + free);
    }

    private void ensureCapacity(int minCapacity) throws IOException {
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity * 2;
        if (oldCapacity > 50 * 1024 * 1024) // for large object graphs, grow more carefully
            newCapacity = minCapacity + 1024 * 1024 * 20;
        else if (oldCapacity < 1001) {
            newCapacity = 4000; // large step initially
        }
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;

        try {
            buf = Arrays.copyOf(buf, newCapacity);
        } catch (OutOfMemoryError ome) {
            System.out.println("OME resize from " + buf.length + " to " + newCapacity + " clearing caches ..");
            throw new RuntimeException(ome);
        }
    }

    public void write(int b) throws IOException {
        ensureCapacity(pos + 1);
        buf[pos] = (byte) b;
        pos += 1;
    }

    public void write(byte b[], int off, int len) throws IOException {
        ensureCapacity(pos + len);
        System.arraycopy(b, off, buf, pos, len);
        pos += len;
    }

    /**
     * only works if no flush has been triggered (aka only write one object per stream instance)
     *
     * @param out
     * @throws IOException
     */
    private void copyTo(OutputStream out) throws IOException {
        out.write(buf, 0, pos);
    }

    public void reset() {
        pos = 0;
        off = 0;
    }

    public byte toByteArray()[] {
        return Arrays.copyOf(buf, pos);
    }

    public int size() {
        return pos;
    }

    public void close() throws IOException {
        flush();
        if (outstream != this)
            outstream.close();
    }

    public void flush() throws IOException {
        if (pos > 0 && outstream != null && outstream != this) {
            copyTo(outstream);
            off = pos;
            reset();
        }
        if (outstream != this && outstream != null)
            outstream.flush();
    }

    public void reset(byte[] out) {
        reset();
        buf = out;
    }

    // return offset of pos to stream position
    public int getOff() {
        return off;
    }
}
