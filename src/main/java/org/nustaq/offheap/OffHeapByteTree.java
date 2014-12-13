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

import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.bytesource.LeftCutStringByteSource;
import org.nustaq.offheap.bytez.malloc.MallocBytez;
import org.nustaq.offheap.bytez.malloc.MallocBytezAllocator;

/**
 * Created by ruedi on 29.06.14.
 *
 * A specialized offheap map [byte[] => long] using a tree structure internally.
 * Nodes consist of sparse arrays.
 */
public class OffHeapByteTree {

    static int estimateMBytesForIndex(int keylen, int numOfKeys) {
        return Math.max(10, (2 * keylen * numOfKeys) / 1000 / 1000 );
    }

    MallocBytezAllocator alloc = new MallocBytezAllocator();
    MallocBytez base;

    long baseOff = 8;

    long root;
    int tableCount;
    int keyLen = 0;

    FullPArray arrFull = new FullPArray();
    PArray arrs[] = { null, new PArray(1,1), new PArray(4,2), new PArray(16,3), new PArray(32,4), new PArray(64,5) };
    ArrWrap arrWrap  = new ArrWrap();

    public OffHeapByteTree(int keyLen, int sizeMB) {
        this.keyLen = keyLen;
        base = (MallocBytez) alloc.alloc(1024l*1024l*sizeMB);
        root = arrFull.newArr();
    }

    public void free() {
        alloc.freeAll();
    }

    public void dumpStats() {
        System.out.println("mem used:"+baseOff/1024/1024+"MB");
        for (int i = 0; i < arrs.length; i++) {
            PArray arr = arrs[i];
            if (arr!=null) {
                System.out.println("pa "+i+" tag "+arr.tag+" entries:"+arr.numEntries+" count:"+arr.count+" reuse:"+arr.reUsed+" freelist:"+arr.freeListIndex);
            }
        }
        System.out.println("fa root count:"+arrFull.count+" freelist:"+arrFull.freeListIndex);
    }

    public long put(ByteSource key, long value ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("invalid key length. Expect "+keyLen);
        return put(key, 0, root, value, 0, 0 );
    }

    long put( ByteSource key, long index, long arr, long toPut, long parentArray, int indexInParent ) {
        byte b = key.get(index);
        int i = ((int)b + 256) & 0xff;
        long lookup = arrWrap.getAt(arr, i);
        if ( index == keyLen - 1 ) {
            boolean success = arrWrap.put(arr, i, toPut);
            if ( ! success ) {
                if ( parentArray == 0 )
                    throw new RuntimeException("No");
                arr = arrWrap.stepUp(arr);
                arrWrap.put(parentArray,indexInParent,arr);
                arrWrap.put(arr, i, toPut);
            }
            return lookup;
        }
        if ( lookup == 0 ) { // no entry for current byte
            long newA = arrWrap.newArr();
            boolean success = arrWrap.put(arr, i, newA);
            if ( ! success ) {
                if ( parentArray == 0 )
                    throw new RuntimeException("No");
                arr = arrWrap.stepUp(arr);
                arrWrap.put(parentArray,indexInParent,arr);
                arrWrap.put(arr, i, newA);
            }
            return put( key, index+1, newA, toPut, arr, i);
        }
        return put( key, index+1, lookup, toPut, arr, i);
    }

    public long get( ByteSource key ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("invalid key length. Expect "+keyLen);
        return get(key, 0, root);
    }

    long get(ByteSource key, long index, long arr) {
        byte b = key.get(index);
        int i = ((int) b + 256) & 0xff;
        long lookup = arrWrap.getAt(arr, i);
        if (index == keyLen - 1) {
            return lookup;
        }
        if (lookup == 0) {
            return 0;
        }
        return get(key, index + 1, lookup);
    }

    public void remove( ByteSource key ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("invalid key length. Expect "+keyLen);
        remove(key, 0, root );
    }

    // return true if empty
    boolean remove( ByteSource key, long index, long arr) {
        byte b = key.get(index);
        int i = ((int)b + 256) & 0xff;
        long lookup = arrWrap.getAt(arr, i);
        if ( index == keyLen - 1 ) {
            if ( lookup != 0) {
                // remove entry from arrwrap
                boolean empty = arrWrap.remove(arr,i);
                if ( empty )
                    arrWrap.addFree(arr);
                return empty;
            }
            return false;
        }
        if ( lookup == 0 ) { // if no match earlier, directly return
            return false;
        }
        boolean res = remove(key, index + 1, lookup);
        if ( res ) {
            res = arrWrap.remove(arr,i);
            if ( res && arr != root )
                arrWrap.addFree(arr);
            return res;
        }
        return res;
    }

    // sparse array
    class PArray {

        PArray(int numEntries, int tag) {
            this.numEntries = numEntries;
            TABLE_SIZE = numEntries*8+numEntries*2+4; // first is tag (short index, long value)
            this.tag = tag;
        }

        int numEntries;
        int TABLE_SIZE;
        int count;
        int reUsed;
        int tag;

        protected long freeList[] = new long[500];
        protected int freeListIndex = 0;

        private void addFree(long offset) {
            for ( int i = 4; i < TABLE_SIZE; i++)
                base.put(offset+i, (byte)0);
            if ( base.getInt(offset) != tag ) { // check tag
                throw new RuntimeException("bad");
            }
            if ( freeListIndex >= freeList.length ) {
                if ( freeListIndex*3/2 >= freeList.length ) { // still not significant free space  ?
                    long newFree[] = new long[Math.min(freeList.length * 2, Integer.MAX_VALUE - 1)];
                    System.arraycopy(freeList, 0, newFree, 0, freeListIndex);
                    freeList = newFree;
                }
            }
            freeList[freeListIndex++] = offset;
        }

        boolean put(long arr, int index, long value) {
            if (value==0) {
                throw new RuntimeException("0 value not allowed");
            }
            long off = arr + 4;
            for ( int i = 0; i < numEntries; i++ ) {
                final short key = base.getShort(off);
                final long debugVal = base.getLong(off + 2);
                if (key == index) { // found empty entry
                    base.putLong(off + 2, value);
                    return true;
                }
                off+=10;
            } // not found => add
            off = arr + 4;
            for ( int i = 0; i < numEntries; i++ ) {
                final long readvalue = base.getLong(off + 2);
                if (readvalue == 0) { // found empty entry
                    base.putShort(off, (short) index);
                    base.putLong(off + 2, value);
                    return true;
                }
                off+=10;
            }
            return false;
        }

        private long getAt(long arr, int index) {
            long off = arr + 4;
            for ( int i = 0; i < numEntries; i++ ) {
                if (base.getShort(off) == index) { // found empty entry
                    return base.getLong(off+2);
                }
                off+=10;
            }
            return 0;
        }

        long newArr() {
            if ( freeListIndex > 0 ) {
                reUsed++;
                return freeList[--freeListIndex];
            }
            if ( baseOff+TABLE_SIZE >= base.length() ) {
                if ( freeListIndex == 0 )
                    grow();
                else
                    return newArr();
            }
            tableCount++;
            long res = baseOff;
            base.putInt(res,tag);
            baseOff += TABLE_SIZE;
            count++;
            return res;
        }

        public void copyTo(PArray destAcc, long arrSrc, long arrDest) {
            long off = arrSrc + 4;
            for ( int i = 0; i < numEntries; i++ ) {
                int index = base.getShort(off);
                long value = base.getLong(off+2);
                if ( value >= 0 ) {
                    destAcc.put(arrDest, index, value);
                }
                off+=10;
            }
        }

        public void copyTo(FullPArray destAcc, long arrSrc, long arrDest) {
            long off = arrSrc + 4;
            for ( int i = 0; i < numEntries; i++ ) {
                int index = base.getShort(off);
                long value = base.getLong(off+2);
                if ( value >= 0 ) {
                    destAcc.put(arrDest, index, value);
                }
                off+=10;
            }
        }

        public void dump(long arr) {
            long off = arr + 4;
            for ( int i = 0; i < numEntries; i++ ) {
                int index = base.getShort(off);
                long value = base.getLong(off+2);
                if ( value > 0 ) {
                    System.out.println(" "+index+" => "+value);
                }
                off+=10;
            }
        }

        // ret true if empty
        public boolean remove(long arr, int key) {
            long off = arr + 4;
            boolean empty = true;
            for ( int i = 0; i < numEntries; i++ ) {
                int index = base.getShort(off);
                long value = base.getLong(off+2);
                if ( index == key ) {
                    base.putShort(off, (short) 0);
                    base.putLong(off + 2, 0l);
                } else {
                    if ( index != 0 ) {
                        empty = false;
                    }
                }
                off+=10;
            }
            return empty;
        }
    }

    // full array
    class FullPArray {

        public static final int TABLE_SIZE = 256*8+4; // first is tag

        protected long freeList[] = new long[500];
        protected int freeListIndex = 0;
        int count = 0;

        private void addFree(long offset) {
            for ( int i = 0; i < TABLE_SIZE/8; i++)
                base.putLong(offset+i*8,0);
            if (freeListIndex>0&&freeList[freeListIndex-1]==offset)
                throw new RuntimeException("double release");
            if ( freeListIndex >= freeList.length ) {
                if ( freeListIndex*3/2 >= freeList.length ) { // still not significant free space  ?
                    long newFree[] = new long[Math.min(freeList.length * 2, Integer.MAX_VALUE - 1)];
                    System.arraycopy(freeList, 0, newFree, 0, freeListIndex);
                    freeList = newFree;
                }
            }
            freeList[freeListIndex++] = offset;
        }

        boolean put(long arr, int i, long value) {
            base.putLong(4+arr+i*8, value);
            return true;
        }

        private long getAt(long arr, int i) {
            return base.getLong(4+arr + i * 8);
        }

        long newArr() {
            if ( freeListIndex > 0 ) {
                freeListIndex--;
                return freeList[freeListIndex+1];
            }
            if ( baseOff+TABLE_SIZE >= base.length() ) {
                if ( freeListIndex == 0 )
                    grow();
                else
                    return newArr();
            }
            tableCount++;
            long res = baseOff;
            base.putInt(res, 0);
            baseOff += TABLE_SIZE;
            count++;
            return res;
        }

        // return true if empty
        public boolean remove(long arr, int key) {
            base.putLong(4+arr+key*8, 0);
            return isEmpty(arr);
        }

        private boolean isEmpty(long arr) {
            for ( int i = 0; i < 256; i++) {
                if ( getAt(arr,i) != 0 )
                    return false;
            }
            return true;
        }

        public void dump(long arr) {
            for ( int i = 0; i < 256; i++ ) {
                long at = getAt(arr, i);
                if ( at != 0 ) {
                    System.out.println(" "+i+" => "+at);
                }
            }
        }

    }

    private void grow() {
        final Bytez newBase = alloc.alloc(base.length()*2);
        base.copyTo(newBase,0,0,base.length());
        alloc.free(base);
        base = (MallocBytez) newBase;
        System.out.println("index grew to "+base.length()/1024/1024);
    }

    class ArrWrap {

        void addFree(long offset) {
            int tag = base.getShort(offset);
            switch (tag) {
                case 0:
                    arrFull.addFree(offset);
                    break;
                default:
                    arrs[tag].addFree(offset);
                    break;
            }
        }

        boolean put(long arr, int i, long value) {
            int tag = base.getShort(arr);
            switch (tag) {
                case 0:
                    return arrFull.put(arr,i,value);
                default:
                    return arrs[tag].put(arr, i, value);
            }
        }

        // ret true if empty
        boolean remove(long arr, int key) {
            int tag = base.getShort(arr);
            switch (tag) {
                case 0:
                    return arrFull.remove(arr, key);
                default:
                    return arrs[tag].remove(arr, key);
            }
        }

        long getAt(long arr, int i) {
            int tag = base.getShort(arr);
            switch (tag) {
                case 0:
                    return arrFull.getAt(arr, i);
                default:
                    return arrs[tag].getAt(arr, i);
            }
        }

        long newArr() {
            return arrs[1].newArr();
        }

        public long stepUp(long arr) {
            int tag = base.getShort(arr);
            switch (tag) {
                case 0:
                    throw new RuntimeException("famous last words: cannot happen");
                case 5:
                    long res1 = arrFull.newArr();
                    arrs[tag].copyTo(arrFull,arr, res1);
                    arrs[tag].addFree(arr);
                    return res1;
                default:
                    long res0 = arrs[tag+1].newArr();
                    arrs[tag].copyTo(arrs[tag+1], arr, res0);
                    arrs[tag].addFree(arr);
//                    System.out.println("src => ");
//                    arr16.dump(arr);
//                    System.out.println("dst => ");
//                    arr32.dump(res0);
                    return res0;
            }
//            throw new RuntimeException("?");
        }

        public void dump(long arr) {
            int tag = base.getShort(arr);
            System.out.println("TAG:"+tag);
            switch (tag) {
                case 0:
                    arrFull.dump(arr); break;
                default:
                    arrs[tag].dump(arr); break;
            }
        }

    }

    public static void dumpBT(OffHeapByteTree bt) {
        for (int i = 0; i < bt.arrs.length; i++) {
            PArray arr = bt.arrs[i];
            if (arr!=null) {
                System.out.println("pa "+i+" tag "+arr.tag+" entries:"+arr.numEntries+" count:"+arr.count+" reuse:"+arr.reUsed+" freelist:"+arr.freeListIndex);
            }
        }
    }

}

