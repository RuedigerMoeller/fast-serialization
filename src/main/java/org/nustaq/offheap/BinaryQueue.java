package org.nustaq.offheap;

import org.nustaq.offheap.bytez.ByteSink;
import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.onheap.HeapBytez;

/**
 * an automatically growing byte queue organized as a ringbuffer.
 *
 * poll* methods don't throw excpetions but return incomplete or no result instead if
 * the queue's content is not sufficient.
 * read* methods throw excpetions in case the q contains not enough bytes (need to use available() to
 * ensure data is present).
 *
 * add methods add to the queue. If the adding outperforms reading/polling, the queue is growed automatically.
 *
 */
public class BinaryQueue {

    Bytez storage;

    long addIndex = 0;
    long pollIndex = 0;

    public BinaryQueue() {
        this(1024);
    }

    public BinaryQueue(int qsize) {
        storage = new HeapBytez(qsize);
    }

    /**
     * add bytes to the queue. Again by using (reusable) Wrapper classes any kind of memory
     * (offheap, byte arrays, nio bytebuffer, memory mapped) can be added.
     *
     * @param source
     */
    public void add(ByteSource source) {
        add(source,0, source.length());
    }

    /**
     * add bytes to the queue. Again by using (reusable) Wrapper classes any kind of memory
     * (offheap, byte arrays, nio bytebuffer, memory mapped) can be added.
     *
     * @param source
     * @param sourceoff
     * @param sourcelen
     */
    public void add(ByteSource source, long sourceoff, long sourcelen) {
        if ( sourcelen > remaining() ) {
            grow(sourcelen);
            add(source,sourceoff,sourcelen);
            return;
        }
        for ( int i=0; i < sourcelen; i++ ) {
            storage.put(addIndex++,source.get(i+sourceoff));
            if ( addIndex >= storage.length() )
                addIndex -= storage.length();
        }
    }

    public void addInt(int written)
    {
        add((byte) ((written >>> 0) & 0xFF));
        add((byte) ((written >>> 8) & 0xFF));
        add((byte) ((written >>> 16) & 0xFF));
        add((byte) ((written >>> 24) & 0xFF));
    }

    public void add(byte b) {
        if ( 1 > remaining() ) {
            grow(1);
            add(b);
            return;
        }
        storage.put(addIndex++, b);
        if ( addIndex >= storage.length() )
            addIndex -= storage.length();
    }

    protected void grow(long sourcelen) {
        HeapBytez newStorage = new HeapBytez((int) Math.max(capacity()*2,capacity()+sourcelen+available()));
        long len = poll(newStorage, 0, available());
        pollIndex = 0;
        addIndex = len;
        storage = newStorage;
    }

    /**
     * @return number of bytes free for an add operation
     */
    public long remaining() {
        return capacity() - available();
    }

    /**
     * read up to destlen bytes (if available).
     * Note you can use HeapBytez (implements ByteSink) in order to read to a regular byte array.
     * HeapBytez wrapper can be reused to avoid unnecessary allocation.
     * Also possible is ByteBufferBasicBytes to read into a ByteBuffer and MallocBytes or MMFBytes to
     * read into Unsafe alloc'ed off heap memory or persistent mem mapped memory regions.
     *
     * @param destination
     * @param destoff
     * @param destlen
     * @return
     */
    public long poll(ByteSink destination, long destoff, long destlen) {
        long count = 0;
        try {
            while (pollIndex != addIndex && count < destlen) {
                destination.put(destoff + count++, storage.get(pollIndex++));
                if (pollIndex >= storage.length()) {
                    pollIndex = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * convenience method to read len byte array. Throws an excpetion if not enough data is present
     *
     * @param len
     * @return
     */
    public byte[] readByteArray(int len) {
        if ( available() < len ) {
            throw new RuntimeException("not enough data available, check available() > len before calling");
        }
        byte b[] = new byte[len];
        int count = 0;
        while ( pollIndex != addIndex && count < len ) {
            b[count++] = storage.get(pollIndex++);
            if ( pollIndex >= storage.length() ) {
                pollIndex = 0;
            }
        }
        return b;
    }

    /**
     * read an int. throws an exception if not enough data is present
     * @return
     */
    public int readInt() {
        if ( available() < 4 ) {
            throw new RuntimeException("not enough data available, check available() > 4 before calling");
        }
        int ch1 = poll();
        int ch2 = poll();
        int ch3 = poll();
        int ch4 = poll();
        return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
    }

    /**
     * @return -1 or the next byte unsigned value
     */
    public int poll() {
        int result = -1;
        if ( pollIndex != addIndex ) {
            result = (storage.get(pollIndex++)+256)&0xff;
            if ( pollIndex >= storage.length() ) {
                pollIndex = 0;
            }
        }
        return result;
    }

    /**
     * 'unread' len bytes
     * @param len
     */
    public void back( int len ) {
        if ( pollIndex >= len )
            pollIndex -= len;
        else
            pollIndex = pollIndex + capacity() - len;

    }

    /**
     * @return number of readable bytes
     */
    public long available() {
        return addIndex>=pollIndex ? addIndex-pollIndex : addIndex+capacity()-pollIndex;
    }

    /**
     * @return size of underlying ringbuffer
     */
    public long capacity() {
        return storage.length();
    }

    @Override
    public String toString() {
        return "BinaryQueue{" +
                "storage=" + storage +
                ", addIndex=" + addIndex +
                ", pollIndex=" + pollIndex +
                '}';
    }
}
