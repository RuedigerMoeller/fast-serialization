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
    OutputStream outstream;

    public FSTOutputStream(OutputStream out) {
        this(32000,out);
    }

    public FSTOutputStream(int size, OutputStream out) {
        buf = new byte[size];
        outstream = out;
    }

    public OutputStream getOutstream() {
        return outstream;
    }

    public void setOutstream(OutputStream outstream) {
        this.outstream = outstream;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public final void ensureFree(int free) throws IOException {
        // inline ..
        if (pos+free - buf.length > 0)
            grow(pos+free);
    }

    public final void ensureCapacity(int minCapacity) throws IOException {
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity * 2;
        if (oldCapacity > 50*1024*1024) // for large object graphs, grow more carefully
            newCapacity = minCapacity+1024*1024*20;
        else if ( oldCapacity < 1001 ) {
            newCapacity = 4000; // large step initially
        }
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;

        try {
            buf = Arrays.copyOf(buf, newCapacity);
        } catch (OutOfMemoryError ome) {
            System.out.println("OME resize from "+buf.length+" to "+newCapacity+" clearing caches ..");
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

    public void copyTo(OutputStream out) throws IOException {
        out.write(buf, 0, pos);
    }

    public void reset() {
        pos = 0;
    }

    public byte toByteArray()[] {
        return Arrays.copyOf(buf, pos);
    }

    public int size() {
        return pos;
    }

    public void close() throws IOException {
        flush();
        if ( outstream != this )
            outstream.close();
    }

    public void flush() throws IOException {
        if ( pos > 0 && outstream != null && outstream != this) {
            copyTo(outstream);
            reset();
        }
        if ( outstream != this )
            outstream.flush();
    }

    public void reset(byte[] out) {
        reset();
        buf = out;
    }
}
