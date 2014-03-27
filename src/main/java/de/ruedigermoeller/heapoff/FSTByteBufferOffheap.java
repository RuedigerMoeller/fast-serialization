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
 * Date: 12.12.12
 * Time: 01:12
 * To change this template use File | Settings | File Templates.
 */
package de.ruedigermoeller.heapoff;

import de.ruedigermoeller.serialization.*;
import de.ruedigermoeller.serialization.annotations.Conditional;
import de.ruedigermoeller.serialization.annotations.Flat;
import de.ruedigermoeller.serialization.annotations.Predict;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Core offheap implementation. The offheap is initialized with a fixed size. Multi threaded access is granted using
 * createAccess(). Concurrent read operations perform ok'ish (low blocked time), concurrent write is possible but threads probably will be blocked
 * often.
 *
 * Objects added to the heap have to implement Serializable.
 *
 * Use this as a base for higher level collection implementation. In order to get dynamically growing offheaps, consider
 * writing a collection wrapper managing ~1GB slices of FSTByteBufferOffheap objects.
 *
 * Each object can be stored with a 'tag'. When iterating the off heap, it is possible to only read the Tag Object to improve
 * performance. E.g. if you write a lot of bloaty 'Person' objects, you'd like to add {name, firstname} as a tag so you can search
 * the offheap without the need to completely deserialize every object. In general iterating this for search should be replaced by
 * some kind indexing at a higher level if the amount of data is huge.
 *
 * Searching using iteration (Tag's only) processes ~500.000 objects per second (core I7 3 Ghz).
 *
 * You can provide your own Buffer object in order to use this on a memory mapped file.
 *
 * Note there is no 'overwrite' operation. You only can add objects, so there is the need to 'reorganize' a heap buffer from
 * time to time.
 */
public class FSTByteBufferOffheap {

    static String DUMMY = "FSTDUMMY";
    public static final int HEADER_SIZE = 8;

    String lock = "Lock";
    ByteBuffer buffer;
    FSTConfiguration conf = null;

    int lastPosition = 0, currPosition = 8; // last object and addposition

    FSTObjectInput.ConditionalCallback alwaysSkip = new FSTObjectInput.ConditionalCallback() {
        @Override
        public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field) {
            return true;
        }
    };

    public FSTByteBufferOffheap(int sizeMB) throws IOException {
        this(sizeMB, null);
    }

    /**
     * Warning: the configuration object passed cannot be shared with another offheap instance !!
     * @param sizeMB
     * @param conf
     * @throws IOException
     */
    public FSTByteBufferOffheap(int sizeMB, FSTConfiguration conf) throws IOException {
        this(ByteBuffer.allocateDirect(sizeMB * 1000 * 1000), conf);
    }

    public FSTByteBufferOffheap(ByteBuffer buffer) throws IOException {
        this(buffer,null);
    }
    /**
     * Warning: the configuration object passed cannot be shared with another offheap instance or stream !!
     * @param buffer
     * @param conf
     * @throws IOException
     */
    public FSTByteBufferOffheap(ByteBuffer buffer, FSTConfiguration conf) throws IOException {
        this.buffer = buffer;
        if ( conf == null ) {
            conf = FSTConfiguration.createDefaultConfiguration();
            conf.setPreferSpeed(true);
        }
        this.conf = conf;
        conf.registerSerializer(ByteBufferEntry.class, new FSTBasicObjectSerializer() {

            @Override
            public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
                out.defaultWriteObject(toWrite, clzInfo);
            }

            @Override
            public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
                MyFSTInput inp = (MyFSTInput) in;
                ByteBufferEntry entry = inp.getAccess().currentEntry;
                entry.content = null;
                in.defaultReadObject(referencee, serializationInfo, entry);
                return entry;
            }
        }, false);
    }

    public FSTConfiguration getConf() {
        return conf;
    }

    /**
     * @return a heap access object. An OffHeapReadAccess can be used to access the OffHeap from a single thread. In order
     * to access concurrent, each Thread must have its own instance of OffHeapAccess.
     * Try to cache the access Object, as the creation is expensive.
     */
    public OffHeapAccess createAccess() {
        return new OffHeapAccess();
    }

    public int getLastPosition() {
        return lastPosition;
    }

    /**
     * @return
     */
    public int getSize() {
        return currPosition;
    }

    public OffHeapIterator iterator() {
        return new OffHeapIterator(lastPosition);
    }

    static class MyFSTInput extends FSTObjectInput {

        private final OffHeapAccess acc;

        public MyFSTInput(OffHeapAccess acc, FSTConfiguration conf) throws IOException {
            super(conf);
            this.acc = acc;
        }

        public OffHeapAccess getAccess() {
            return acc;
        }
    }

    public static class GetObjectBufferResult {
        int handle;
        int len; // len of pure object
        ByteBuffer buff;
        Object lock;

        // will point to object start
        public ByteBuffer slice() {
            synchronized (lock) {
                buff.position(handle+ HEADER_SIZE);
                int prevLimit = buff.limit();
                buff.limit(handle+ HEADER_SIZE +len);
                ByteBuffer slice = buff.slice();
                buff.limit(prevLimit);
                return slice;
            }
        }

        public void slice(byte toCopyTo[], int offset) {
            synchronized (lock) {
                buff.position(handle+HEADER_SIZE);
                buff.get(toCopyTo,offset,len);
            }
        }

        public int getPosition() {
            return handle+ HEADER_SIZE;
        }

        public int getLen() {
            return len;
        }

    }

    public class OffHeapAccess {

        protected ByteBufferEntry currentEntry = new ByteBufferEntry();
        protected FSTObjectInput in;
        protected FSTObjectOutput out;
        byte tmpBuf[];

        public OffHeapAccess() {
            try {
                in = new MyFSTInput(this,conf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        byte[] getTmpBuf(int siz) {
            if ( tmpBuf == null || tmpBuf.length < siz ) {
                tmpBuf = new byte[siz];
            }
            return tmpBuf;
        }

        public void getObjectBuffer( int handle, GetObjectBufferResult res ) {
            res.len = buffer.getInt(handle);
            buffer.position(handle+ HEADER_SIZE);
            res.buff = buffer;
            res.lock = lock;
        }

        public ByteBufferEntry getEntry(int handle) throws Exception {
            currentEntry.length = buffer.getInt(handle);
            currentEntry.prevPosition = buffer.getInt(handle+4);
            byte[] buf = getTmpBuf(currentEntry.length);
            synchronized (lock) {
                buffer.position(handle+ HEADER_SIZE);
                buffer.get(buf);
            }
            in.resetForReuseUseArray(buf, currentEntry.length);
            return (ByteBufferEntry) in.readObject(ByteBufferEntry.class);
        }

        public Object getObject( int handle ) throws Exception {
            in.setConditionalCallback(null);
            return getEntry(handle).content;
        }

        public Object getTag( int handle ) throws Exception {
            in.setConditionalCallback(alwaysSkip);
            return getEntry(handle).tag;
        }

        int prepareOut(Object toSave, Object tag) throws IOException {
            if ( out == null ) {
                out = new FSTObjectOutput(conf);
            }
            currentEntry.tag = tag;
            currentEntry.content = toSave;
            out.resetForReUse();
            out.writeObject(currentEntry,currentEntry.getClass());
            return out.getWritten();
        }

        public int add(Object toSave, Object tag) throws IOException {
            if ( out == null ) {
                out = new FSTObjectOutput(conf);
            }
            prepareOut(toSave,tag);
            synchronized (lock) {
                // first 4 bytes are length
                int res = currPosition;
                buffer.putInt(res,out.getWritten());
                buffer.putInt(res+4,lastPosition);
                lastPosition = res;
                buffer.position(currPosition + HEADER_SIZE); // length
                buffer.put(out.getBuffer(),0,out.getWritten());
                currPosition = buffer.position();
                return res;
            }
        }

    }

    public class OffHeapIterator extends OffHeapAccess implements Iterator {
        int position;
        int currentPosition;

        OffHeapIterator(int position) {
            super();
            this.position = position;
        }

        @Override
        public boolean hasNext() {
            return position > 0;
        }

        public Object getCurrentTag() {
            return currentEntry.tag;
        }

        /**
         * @return null if iterated using next(), the saved object if iterated using nextEntry
         */
        public Object getCurrentEntry() {
            return currentEntry.content;
        }

        public int getCurrentPositiion() {
            return currentPosition;
        }

        /**
         *
         * @param callback - if null always read the full object (tag+value), else
         *                 the callback is called to decide wether to decode the content or not
         * @return
         */
        public Object nextEntry(FSTObjectInput.ConditionalCallback callback) {
            try {
                in.setConditionalCallback(callback);
                currentEntry = getEntry(position);
                currentPosition = position;
                position = currentEntry.prevPosition;
                return currentEntry.tag;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @return the 'tag' of the next entry. Use nextEntry to obtain the full entry (
         */
        @Override
        public Object next() {
            try {
                in.setConditionalCallback(alwaysSkip);
                currentEntry = getEntry(position);
                position = currentEntry.prevPosition;
                return currentEntry.tag;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void remove() {
            throw new RuntimeException("not implemented");
        }

    }

    @Predict(ByteBufferEntry.class) @Flat
    static public class ByteBufferEntry implements Serializable {
        transient int length; // pure object length
        transient int prevPosition;
        public @Conditional Object content;
        public @Flat Object tag;
    }


}
