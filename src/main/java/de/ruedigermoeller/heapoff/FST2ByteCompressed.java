package de.ruedigermoeller.heapoff;

import de.ruedigermoeller.serialization.FSTConfiguration;

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
 * Date: 20.06.13
 * Time: 20:27
 * To change this template use File | Settings | File Templates.
 */

/**
 * saves an object graph into a byte array. It does not save that much memory in terms of heap usage,
 * however GC pauses will reduce massively because FULL GC with 1GB of byte[] is much faster than
 * a full GC on say 5 million objects. The objects saved should have some complexity (e.g. several strings
 * and sub-objects (e.g. value types). Putting plain objects consisting mainly of primitive types will not pay off.
 *
 * @param <T>
 */
public class FST2ByteCompressed<T> extends FSTCompressed<T> {

    FSTConfiguration conf;
    byte buf[];

    FST2ByteCompressed(FSTConfiguration conf, Class clazz) {
        this.conf = conf;
        this.clazz = clazz;
    }

    @Override
    protected void storeArray(byte[] buffer, int written) {
        buf = new byte[written];
        System.arraycopy(buffer,0,buf,0,written);
    }

    @Override
    protected FSTConfiguration getConf() {
        return conf;
    }

    @Override
    public byte[] getArray() {
        return buf;
    }

    @Override
    public int getLen() {
        return buf.length;
    }

    @Override
    public int getOffset() {
        return 0;
    }

}
