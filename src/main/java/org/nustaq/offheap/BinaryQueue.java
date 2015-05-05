package org.nustaq.offheap;

import org.nustaq.offheap.bytez.ByteSink;
import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.onheap.HeapBytez;

/**
 * an automatically growing binary queue organized as a ringbuffer.
 */
public class BinaryQueue {

    Bytez storage = new HeapBytez(1024);

    long addIndex = 0;
    long pollIndex = 0;

    public void add(ByteSource source) {
        add(source,0, source.length());
    }

    public void add(ByteSource source, long sourceoff, long sourcelen) {
        if ( sourcelen > remaining() ) {
            HeapBytez newStorage = new HeapBytez((int) Math.max(capacity()*2,capacity()+sourcelen));
            long len = poll(newStorage, 0, size());
            pollIndex = 0;
            addIndex = len;
            storage = newStorage;
            add(source,sourceoff,sourcelen);
            return;
        }
        if ( addIndex + sourcelen <= storage.length() ) {
            // fixme: move bulk transfer method to byteSource (would be easy with 1.8 default methods)
            for ( int i=0; i < sourcelen; i++ ) {
                storage.put(addIndex++,source.get(i));
            }
        } else {
            for ( int i=0; i < sourcelen; i++ ) {
                storage.put(addIndex++,source.get(i));
                if ( addIndex >= storage.length() )
                    addIndex -= storage.length();
            }
        }
    }

    public long remaining() {
        return capacity()-size();
    }

    public long poll(ByteSink destination, long destoff, long destlen) {
        long count = 0;
        while ( pollIndex != addIndex && count < destlen ) {
            destination.put(destoff+count++,storage.get(pollIndex++));
            if ( pollIndex >= storage.length() ) {
                pollIndex = 0;
            }
        }
        return count;
    }

    /**
     * @return number of pollable bytes
     */
    public long size() {
        return addIndex>=pollIndex ? addIndex-pollIndex : addIndex+capacity()-pollIndex;
    }

    public long capacity() {
        return storage.length();
    }

}
