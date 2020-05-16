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
package org.nustaq.offheap;

import org.nustaq.logging.FSTLogger;
import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.bytesource.AsciiStringByteSource;
import org.nustaq.offheap.bytez.bytesource.BytezByteSource;
import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.bytesource.LeftCutStringByteSource;
import org.nustaq.offheap.bytez.bytesource.UTFStringByteSource;
import org.nustaq.offheap.bytez.malloc.MMFBytez;
import org.nustaq.offheap.bytez.malloc.MallocBytez;
import org.nustaq.offheap.bytez.malloc.MallocBytezAllocator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ConcurrentModificationException;
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

    // FIXME: Testcase for mem overflow

    public static final long MB = 1024 * 1024;
    public static final long GB = 1024 * MB;
    public static final int CUSTOM_FILEHEADER_LEN = 8000; // 8k for application
    public static final int CORE_HEADER_LEN = 8;
    public static final int FILE_HEADER_LEN = CORE_HEADER_LEN +CUSTOM_FILEHEADER_LEN; // 0 - numelems, 4 - magic num

    final static int HEADER_TAG = 0xe5e1; // can be used to recover corrupted data
    public static final int KEY_OFFSET_IN_HEADER = 16;

    private BytezByteSource tmpValueBytez;

    protected OffHeapByteTree index;
    protected Bytez memory;
    protected Bytez customHeader;
    protected MallocBytezAllocator alloc;
    protected int numElem;
    protected int keyLen;
    protected long bytezOffset;
    protected FreeList freeList;// FIXME: missing merge/split of different block sizes
    protected String mappedFile;
    protected int mutationCount;

    public FSTBinaryOffheapMap(String mappedFile, int keyLen, long sizeMemBytes, int numberOfElems) throws Exception {
        initFromFile(mappedFile, keyLen, sizeMemBytes, numberOfElems);
    }

    Thread debug;
    private void checkThread() {
//        if ( debug == null )
//            debug = Thread.currentThread();
//        else {
//            if ( debug != Thread.currentThread() )
//                throw new RuntimeException( "unexpected concurrency "+debug.getName()+" curr:" + Thread.currentThread().getName() );
//        }
    }

    public Bytez getCustomFileHeader() {
        return customHeader;
    }

    protected void initFromFile(String file, int keyLen, long sizeMemBytes, int numberOfElems) throws Exception {
        checkThread();
        numElem = 0;
        bytezOffset = FILE_HEADER_LEN;
        freeList = new FreeList(); // FIXME: missing merge/split of different block sizes
        this.mappedFile = file;
        if ( new File(file).exists() )
            resetMem(file, new File(file).length());
        else
            resetMem(file, sizeMemBytes);
        this.keyLen = keyLen;
        if ( memory.getInt(4) != HEADER_TAG || memory.getInt(0) <= 0 ) {
            // newly created or empty file
            index = new OffHeapByteTree(keyLen,OffHeapByteTree.estimateMBytesForIndex(keyLen,numberOfElems));
            memory.putInt(4,HEADER_TAG);
            System.out.println("new file detected "+file);
        } else {
            // FIXME: be more resilent in case of corruption ..
            numElem = memory.getInt(0);
            index = new OffHeapByteTree(keyLen,OffHeapByteTree.estimateMBytesForIndex(keyLen,numElem*2));
            long off = FILE_HEADER_LEN;
            int elemCount = 0;
            BytezByteSource byteIter = new BytezByteSource(memory,0,0);
            long tim = System.currentTimeMillis();
            while (elemCount < numElem) {
                int len = getLenFromHeader(off);
                boolean removed = memory.get(off+4) != 0;
                if ( ! removed ) {
                    elemCount++;
                    byteIter.setOff(off + KEY_OFFSET_IN_HEADER); // 16 = offset of key in header
                    byteIter.setLen(keyLen);
                    index.put(byteIter, off);
                    bytezOffset = off+getHeaderLen()+len;
                } else {
                    addToFreeList(off);
                }
                off+= getHeaderLen() + len;
            }
            FSTLogger.getLogger(getClass()).log(FSTLogger.Level.INFO,"boot "+numElem+" records in "+(System.currentTimeMillis()-tim)+"ms",null);
        }
    }

    private void resetMem(String file, long sizeMemBytes) throws Exception {
        checkThread();
        mutationCount++;
        memory = new MMFBytez(file,sizeMemBytes,false);
        customHeader = memory.slice(CORE_HEADER_LEN, CUSTOM_FILEHEADER_LEN);
        tmpValueBytez = new BytezByteSource(memory,0,0);
    }

    public FSTBinaryOffheapMap(int keyLen, long sizeMemBytes, int numberOfElems) {
        init(keyLen, sizeMemBytes, numberOfElems);
    }

    protected void init(int keyLen, long sizeMemBytes, int numberOfElems) {
        checkThread();
        numElem = 0;
        bytezOffset = FILE_HEADER_LEN;
        freeList = new FreeList(); // FIXME: missing merge/split of different block sizes
        alloc = new MallocBytezAllocator();
        memory = alloc.alloc(sizeMemBytes);
        customHeader = memory.slice(CORE_HEADER_LEN,CUSTOM_FILEHEADER_LEN);
        tmpValueBytez = new BytezByteSource(memory,0,0);
        this.keyLen = keyLen;
        index = new OffHeapByteTree(keyLen,OffHeapByteTree.estimateMBytesForIndex(keyLen,numberOfElems));
        memory.putInt(4, HEADER_TAG);
    }

    @Override
    protected void finalize() throws Throwable {
        free();
    }

    public void free() {
        checkThread();
        mutationCount++;
        if ( alloc != null ) {
            alloc.freeAll();
            alloc = null;
        }
        if ( memory instanceof MMFBytez ) {
            ((MMFBytez) memory).freeAndClose();
            memory = null;
        }
//        index.free();
        index = null;
    }

    public void putBinary( ByteSource key, ByteSource value ) {
        checkThread();
        if ( key.length() != keyLen )
            throw new RuntimeException("key must have length "+keyLen);
        mutationCount++;
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
            index.put(key, addEntry(key, value));
            removeEntry(put);
        } else {
            // add
            index.put(key, addEntry(key, value));
            incElems();
        }
    }

    protected void removeEntry(long offset) {
        checkThread();
        mutationCount++;
        addToFreeList(offset);
        memory.put(offset + 4, (byte) 1);
    }

    protected void addToFreeList(long offset) {
        freeList.addToFree(offset, getLenFromHeader(offset) + getHeaderLen());
    }

    protected void setEntry(long off, int entryLen, ByteSource value) {
        checkThread();
        mutationCount++;
        writeEntryHeader(off, entryLen, (int) value.length(), false);
        off += getHeaderLen();
        for ( int i = 0; i < value.length(); i++ ) {
            memory.put( off++, value.get(i) );
        }
    }

    protected long addEntry(ByteSource key, ByteSource value) {
        checkThread();
        mutationCount++;
        long valueLength = value.length();
        long newOffset = freeList.findFreeBlock( (int) valueLength + getHeaderLen() );
        if ( newOffset > 0) {
//            System.out.println("reuse len "+getLenFromHeader(newOffset)+" at "+newOffset+" entrylen "+(getLenFromHeader(newOffset)+getHeaderLen()));
            writeEntryHeader(newOffset,getLenFromHeader(newOffset),(int) valueLength,false);
            long l = newOffset;
            // put key
            for ( int ii = 0; ii < keyLen; ii++ ) {
                memory.put( 16+l+ii, key.get(ii) );
            }
            l += getHeaderLen();
            // put value
            for ( int ii = 0; ii < valueLength; ii++ ) {
                memory.put( l++, value.get(ii) );
            }
            return newOffset;
        }
        int entryLen = getEntryLengthForContentLength(value.length());
        // size to power of 2
        entryLen = freeList.computeLen(entryLen+getHeaderLen())-getHeaderLen();
        if ( memory.length() <= bytezOffset+entryLen+getHeaderLen()) {
            resizeStore(bytezOffset + entryLen + getHeaderLen());
//            return addEntry(key,value); // fixme loses one freelist entry
        }
        long res = bytezOffset;
        writeEntryHeader(bytezOffset, entryLen,(int)value.length(),false);
        // put key
        for ( int ii = 0; ii < keyLen; ii++ ) {
            memory.put( 16+bytezOffset+ii, key.get(ii) );
        }
        long off = bytezOffset+getHeaderLen();
        for ( int i = 0; i < value.length(); i++ ) {
            memory.put( off++, value.get(i) );
        }
        bytezOffset+=entryLen+getHeaderLen();
        return res;
    }

    private void resizeStore(long required) {
        resizeStore(required,GB);
    }

    /**
     * PRIVILEGED method. You gotta know what your doing here ..
     *
     * currently a very expensive operation .. frees everything, resize file and remap.
     * Remapping involves rebuild of index.
     * @param required
     */
    public void resizeStore(long required, long maxgrowbytes) {
        if ( mappedFile == null )
            throw new RuntimeException("store is full. Required: "+required);
        if ( required <= memory.length() )
            return;
        mutationCount++;
        long newSize = Math.min(required*2,required+maxgrowbytes);
        System.out.println("resizing underlying "+mappedFile+" to "+newSize+" numElem:"+numElem);
        long tim = System.currentTimeMillis();
        ((MMFBytez) memory).freeAndClose();
        memory = null;
        try {
            resetMem(mappedFile, newSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("resizing done in "+(System.currentTimeMillis()-tim)+" numElemAfter:"+numElem);
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
        checkThread();
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
        checkThread();
        if ( key.length() != keyLen )
            throw new RuntimeException("key must have length "+keyLen);
        mutationCount++;
        long rem = index.get(key);
        if ( rem != 0 ) {
            index.remove(key);
            decElems();
            removeEntry(rem);
        }
    }

    protected void decElems() {
        numElem--;
        memory.putInt(0, numElem);
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
        checkThread();
        mutationCount++;
        memory.putInt( offset, entryLen );
        memory.put( offset + 4, (byte) (removed ? 1 : 0));
        memory.putInt( offset + 8, contentLen);
        memory.putInt( offset + 12, HEADER_TAG);
    }

    protected int getHeaderLen() {
        return 4+4+4+4+keyLen; // 0-3 len, 4 removed flag, 5-7 free, 8-11 content len, 12-15 magic num, 16... key
    }

    // overall content size (excl header)
    protected int getLenFromHeader(long off) {
        return memory.getInt(off);
    }

    protected int getContentLenFromHeader(long off) {
        return memory.getInt(off+8);
    }

    public Iterator<ByteSource> binaryValues() {
        checkThread();
        return new Iterator<ByteSource>() {
            long off = FILE_HEADER_LEN;
            int elemCount = 0;
            int mutSnap = mutationCount;

            BytezByteSource byteIter = new BytezByteSource(memory,0,0);

            @Override
            public boolean hasNext() {
                return elemCount < numElem;
            }

            @Override
            public ByteSource next() {
                checkThread();
                int contentLen = getContentLenFromHeader(off);
                int len = getLenFromHeader(off);
                boolean removed = memory.get(off+4) != 0;
                off+= getHeaderLen();
                while ( removed ) {
                    off += len;
                    len = getLenFromHeader(off);
                    contentLen = getContentLenFromHeader(off);
                    removed = memory.get(off+4) != 0;
                    off+= getHeaderLen();
                }
                elemCount++;
                byteIter.setOff(off);
                byteIter.setLen(contentLen);
                off+=len;
                if ( mutSnap != mutationCount )
                    throw new ConcurrentModificationException("in offheap map snap:"+mutSnap+" current:"+mutationCount);
                return byteIter;
            }

            @Override
            public void remove() {
                throw new RuntimeException("unimplemented");
            }
        };
    }

    public String printBinaryKey(ByteSource key) {
        StringBuilder res = new StringBuilder();
        for ( int i = 0; i < key.length(); i++ ) {
            byte b = key.get(i);
            if ( b > 31 ) {
                res.append((char) b);
            } else {
                res.append('_');
            }
        }
        return res.toString();
    }

    public KeyValIter binaryKeys() {
        checkThread();
        return new KeyValIter() {
            long off = FILE_HEADER_LEN;
            int elemCount = 0;
            int mutSnap = mutationCount;
            BytezByteSource byteIter = new BytezByteSource(memory,0,0);
            BytezByteSource byteVal = new BytezByteSource(memory,0,0);
            long valueAddress;

            @Override
            public boolean hasNext() {
                return elemCount < numElem;
            }

            @Override
            public ByteSource next() {
                checkThread();
                int len = getLenFromHeader(off);
                int contentLen = getContentLenFromHeader(off);
                boolean removed = memory.get(off+4) != 0;
                off+= getHeaderLen();
                while ( removed ) {
                    off += len;
                    len = getLenFromHeader(off);
                    contentLen = getContentLenFromHeader(off);
                    removed = memory.get(off+4) != 0;
                    off+= getHeaderLen();
                }
                elemCount++;
                valueAddress = off;
                byteVal.setOff(off);
                byteVal.setLen(contentLen);
                byteIter.setOff(off-getHeaderLen()+16);
                byteIter.setLen(keyLen);
                off+=len;
                if ( mutSnap != mutationCount )
                    throw new ConcurrentModificationException("in offheap map snap:"+mutSnap+" current:"+mutationCount);
                return byteIter;
            }

            @Override
            public void remove() {
                throw new RuntimeException("unimplemented");
            }

            @Override
            public ByteSource getValueBytes() {
                return byteVal;
            }

            @Override
            public long getValueAddress() {
                return valueAddress;
            }
        };
    }

    public long getFreeMem() {
        return memory.length()-bytezOffset;
    }

    public long getUsedMem() {
        return bytezOffset;
    }

    public int getCapacityMB() { return (int) (memory.length()/1024l/1024l); }

    public int getSize() {
        return numElem;
    }

    public void dumpIndexStats() {
        index.dumpStats();
    }

    public String getFileName() {
        return mappedFile;
    }

    public static interface KeyValIter extends Iterator<ByteSource> {
        public ByteSource getValueBytes();
        public long getValueAddress();
    }

    public static void main(String[] args) throws Exception {
        FSTAsciiStringOffheapMap map = new FSTAsciiStringOffheapMap("/tmp/omap.bin",64,10000, 10);
        if ( false ) {
            for ( int i = 0; i < 1000; i++ ) {
                map.put(""+i, "Hello"+i);
            }
        }
        for ( int i = 0; i < 1000; i++ ) {
            System.out.println(map.get("" + i));
        }
    }
}
