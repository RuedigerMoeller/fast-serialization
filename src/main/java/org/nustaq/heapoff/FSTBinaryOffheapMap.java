package org.nustaq.heapoff;

import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.heapoff.bytez.bytesource.BytezByteSource;
import org.nustaq.heapoff.bytez.Bytez;
import org.nustaq.heapoff.bytez.malloc.MallocBytezAllocator;

import java.util.Iterator;

/**
 * Baseclass of offheap maps.
 * FST OffHeap Maps enable to store key/value pairs in offheap memory. Additionally it provides
 * and iterator interface for all values. In order to also iterate keys, add each key to its value object,
 * as this OffHeap map does not support iteration of keys out of the box.
 *
 * See subclasses for directly applicable classes (E.g. FSTStringOffheapMap)
 *
 * The base is a generic bytesource to bytesource map. Note that key should be as short as possible (4-20 bytes) and
 * should have their most modified digit at the last character of their value.
 * e.g. [0,0,0,0,123,44] where '44' changes with each new key. Else on-heap memory consumption will grow.
 * Performance of lookup degrades with growing key size.
 */
public class FSTBinaryOffheapMap {

    public static final long MB = 1024 * 1024;
    public static final long GB = 1024 * MB;
    public static final int FILE_HEADER_LEN = 4;

    private BytezByteSource tmpValueBytez;

    protected ByteTree index;
    protected Bytez memory;
    protected MallocBytezAllocator alloc;
    protected int numElem;
    protected int keyLen;
    protected long bytezOffset = FILE_HEADER_LEN;

    public FSTBinaryOffheapMap(int keyLen, long size) {
        init(keyLen,size);
    }

    protected void init(int keyLen, long size) {
        index = new ByteTree(keyLen);
        alloc = new MallocBytezAllocator();
        memory = alloc.alloc(size);
        tmpValueBytez = new BytezByteSource(memory,0,0);
        this.keyLen = keyLen;
    }

    public void putBinary( ByteSource key, ByteSource value ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("key must have length "+keyLen);
        Long put = index.put(key, bytezOffset);
        if ( put == null )
            incElems();
        if ( memory.length() <= value.length()+ getHeaderLen())
            throw new RuntimeException("store is full "+numElem);
        createBinaryHeader(key, value);
        bytezOffset += getHeaderLen();
        for ( int i = 0; i < value.length(); i++ ) {
            memory.put( bytezOffset++, value.get(i) );
        }
    }

    /**
     * get an entry. the returned ByteSource must be processed immediately as it will be reused
     * internally on next get
     * @param key
     * @return
     */
    public BytezByteSource getBinary( ByteSource key ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("key must have length "+keyLen);
        Long aLong = index.get(key);
        if ( aLong == null ) {
            return null;
        }
        long off = aLong.longValue();
        int len = getLenFromHeader(off);
        off+= getHeaderLen();
        tmpValueBytez.setLen(len);
        tmpValueBytez.setOff(off);
        return tmpValueBytez;
    }

    /**
     * remove the key from the binary map
     * @param key
     */
    public void removeBinary( ByteSource key ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("key must have length "+keyLen);
        Long rem = index.remove(key);
        if ( rem != null ) {
            decElems();
            memory.put(rem.longValue()+4,(byte)1);
        }
    }

    protected void decElems() {
        numElem--;
    }

    protected void incElems() {
        numElem++;
        memory.putInt(0, numElem);
    }

    protected void createBinaryHeader(ByteSource key, ByteSource value) {
        memory.putInt(bytezOffset, (int) value.length());
        memory.put(bytezOffset+4, (byte)0);
    }

    protected int getHeaderLen() {
        return 4+4; // 0-3 len, 4 removed flag, 5-7 free
    }

    protected int getLenFromHeader(long off) {
        return memory.getInt(off);
    }

    public Iterator<ByteSource> binaryValues() {
        return new Iterator<ByteSource>() {
            long off = FILE_HEADER_LEN;
            int elemCount = 0;
            BytezByteSource byteIter = new BytezByteSource(memory,0,0);

            @Override
            public boolean hasNext() {
                return elemCount < numElem;
            }

            @Override
            public ByteSource next() {
                int len = getLenFromHeader(off);
                boolean removed = memory.get(off+4) != 0;
                off+= getHeaderLen();
                while ( removed ) {
                    off += len;
                    len = getLenFromHeader(off);
                    removed = memory.get(off+4) != 0;
                    off+= getHeaderLen();
                }
                elemCount++;
                byteIter.setOff(off);
                byteIter.setLen(len);
                off+=len;
                return byteIter;
            }
        };
    }

    public long getFreeMem() {
        return memory.length()-bytezOffset;
    }

}
