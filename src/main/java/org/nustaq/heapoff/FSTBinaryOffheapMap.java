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
 *
 * In case entries are updated frequently with values of different size, avoid fragementation
 * by adding extra space to each entry. Override getEntryLengthForContentLength for this.
 */
public class FSTBinaryOffheapMap {

    public static final long MB = 1024 * 1024;
    public static final long GB = 1024 * MB;
    public static final int FILE_HEADER_LEN = 4;

    final static int HEADER_TAG = 0xe5e1; // can be used to recover corrupted data

    private BytezByteSource tmpValueBytez;

    protected OffHeapByteTree index;
    protected Bytez memory;
    protected MallocBytezAllocator alloc;
    protected int numElem;
    protected int keyLen;
    protected long bytezOffset = FILE_HEADER_LEN;

    protected long freeList[] = new long[500];
    protected int freeListIndex = 0;

    public FSTBinaryOffheapMap(int keyLen, long size, int indexSizeMB) {
        init(keyLen,size,indexSizeMB);
    }

    protected void init(int keyLen, long size, int indexSizeMB) {
        index = new OffHeapByteTree(keyLen,indexSizeMB);
        alloc = new MallocBytezAllocator();
        memory = alloc.alloc(size);
        tmpValueBytez = new BytezByteSource(memory,0,0);
        this.keyLen = keyLen;
    }

    @Override
    protected void finalize() throws Throwable {
        free();
    }

    public void free() {
        alloc.freeAll();
        alloc = null;
    }

    public void putBinary( ByteSource key, ByteSource value ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("key must have length "+keyLen);
        long put = index.get(key);
        if ( put != 0 ) {
            int lenFromHeader = getLenFromHeader(put);
            if (value.length() <= lenFromHeader) {
                // replace
                setEntry(put, lenFromHeader, value);
                index.put(key, put);
                return;
            }
            // set removed and fall through to add
            removeEntry(put);
        } else {
            // add
            incElems();
        }
        index.put(key,addEntry(value));
    }

    protected void removeEntry(long offset) {
        if ( freeListIndex >= freeList.length ) {
            compactFreeList();
            if ( freeListIndex*3/2 >= freeList.length ) { // still not significant free space  ?
                long newFree[] = new long[Math.min(freeList.length * 2, Integer.MAX_VALUE - 1)];
                System.arraycopy(freeList, 0, newFree, 0, freeListIndex);
                freeList = newFree;
            }
        }
        freeList[freeListIndex++] = offset;
        memory.put(offset+4,(byte)1);
    }

    private void compactFreeList() {
        int newFreeIndex = 0;
        for (int i = 0; i < freeListIndex; i++) {
            long l = freeList[i];
            if ( l > 0 ) {
                freeList[newFreeIndex++] = l;
            }
        }
        freeListIndex = newFreeIndex;
    }

    protected void setEntry(long off, int entryLen, ByteSource value) {
        writeEntryHeader(off,entryLen,(int)value.length(),false);
        off += getHeaderLen();
        for ( int i = 0; i < value.length(); i++ ) {
            memory.put( off++, value.get(i) );
        }
    }

    protected long addEntry(ByteSource value) {
        long valueLength = value.length();
        for (int i = freeListIndex-1; i >= 0; i--) {
            long l = freeList[i];
            if ( l > 0 && getLenFromHeader(l) >= valueLength) {
                freeList[i] = 0;
                long res = l;
                writeEntryHeader(l,getLenFromHeader(l),(int) valueLength,false);
                l += getHeaderLen();
                for ( int ii = 0; ii < valueLength; ii++ ) {
                    memory.put( l++, value.get(ii) );
                }
                freeListIndex--;
                return res;
            }
        }
        if ( memory.length() <= value.length()+ getHeaderLen())
            throw new RuntimeException("store is full "+numElem);
        int entryLen = getEntryLengthForContentLength(value.length());
        long res = bytezOffset;
        writeEntryHeader(bytezOffset, entryLen,(int)value.length(),false);
        bytezOffset += getHeaderLen();
        long off = bytezOffset;
        for ( int i = 0; i < value.length(); i++ ) {
            memory.put( off++, value.get(i) );
        }
        bytezOffset+=entryLen;
        return res;
    }

    /**
     * get an entry. the returned ByteSource must be processed immediately as it will be reused
     * internally on next get
     * Warning: Concurrent Modification (e.g. add remove elements during iteration) is NOT SUPPORTED
     * and NOT CHECKED. Collect keys to change inside iteration and perform changes after iteration is
     * finished.
     * @param key
     * @return
     */
    public BytezByteSource getBinary( ByteSource key ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("key must have length "+keyLen);
        long aLong = index.get(key);
        if ( aLong == 0 ) {
            return null;
        }
        long off = aLong;
        int len = getContentLenFromHeader(off);
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
        long rem = index.get(key);
        if ( rem != 0 ) {
            index.remove(key);
            decElems();
            removeEntry(rem);
        }
    }

    protected void decElems() {
        numElem--;
    }

    protected void incElems() {
        numElem++;
        memory.putInt(0, numElem);
    }

    /**
     * called upon add, allows to reserve extra space for later growth per entry
     * @param lengthOfEntry
     * @return
     */
    protected int getEntryLengthForContentLength(long lengthOfEntry) {
        return (int) lengthOfEntry;
    }

    protected void writeEntryHeader( long offset, int entryLen, int contentLen, boolean removed ) {
        memory.putInt( offset, entryLen );
        memory.put( offset + 4, (byte) (removed ? 1 : 0));
        memory.putInt( offset + 8, contentLen);
        memory.putInt( offset + 12, HEADER_TAG);
    }

    protected int getHeaderLen() {
        return 4+4+4+4; // 0-3 len, 4 removed flag, 5-7 free, 8-11 content len, 12-15 magic num
    }

    protected int getLenFromHeader(long off) {
        return memory.getInt(off);
    }

    protected int getContentLenFromHeader(long off) {
        return memory.getInt(off+8);
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

            @Override
            public void remove() {
                throw new RuntimeException("unimplemented");
            }
        };
    }

    public long getFreeMem() {
        return memory.length()-bytezOffset;
    }

    public int getSize() {
        return numElem;
    }

    public void dumpIndexStats() {
        index.dumpStats();
    }
}
