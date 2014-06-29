package org.nustaq.heapoff;

import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.heapoff.bytez.bytesource.LeftCutStringByteSource;
import org.nustaq.heapoff.bytez.malloc.MallocBytez;
import org.nustaq.heapoff.bytez.malloc.MallocBytezAllocator;

/**
 * Created by ruedi on 29.06.14.
 */
public class OffHeapByteTree {

    MallocBytezAllocator alloc = new MallocBytezAllocator();
    MallocBytez base = (MallocBytez) alloc.alloc(1024l*1024l*1204l);

    long baseOff = 8;

    long root;
    int tableCount;
    int keyLen = 0;

    FullPArray arrFull = new FullPArray();
    PArray     arr4 = new PArray(4);
    PArray     arr32 = new PArray(32);
    ArrWrap arrWrap = new ArrWrap();

    public OffHeapByteTree(int keyLen) {
        this.keyLen = keyLen;
        root = arrFull.newArr();
    }

    public long put(ByteSource key, long value ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("invalid key length. Expect "+keyLen);
        return put(key, 0, root, value, 0, 0 );
    }

    public void clean() {
        clean(0,root);
    }

    // return true if empty
    boolean clean( long index, long arr ) {
        if ( index == keyLen - 1 ) {
            for (int i = 0; i < 256; i++) {
                long o = arrWrap.getAt(arr, i);
                if ( o != 0 ) {
                    return false;
                }
            }
            return true;
        }
        boolean hadOne = false;
        for (int i = 0; i < 256; i++) {
            long subArr = arrWrap.getAt(arr, i);
            if ( subArr != 0 ) {
                if (clean(index + 1, subArr)) {
                    arrWrap.addFree(subArr);
                } else {
                    hadOne = true;
                }
            }
        }
        return ! hadOne;
    }

    long put( ByteSource key, long index, long arr, long toPut, long parentArray, int indexInParent ) {
        byte b = key.get(index);
        int i = ((int)b + 256) & 0xff;
        long lookup = arrWrap.getAt(arr, i);
        if ( index == keyLen - 1 ) {
            if ( lookup != 0 ) {
                arrWrap.dump(arr);
            }
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
        if ( lookup == 0 ) {
            long newA = arrWrap.newArr();
            arrWrap.put(arr, i, newA);
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


    class PArray {

        PArray(int numEntries) {
            this.numEntries = numEntries;
            TABLE_SIZE = numEntries*8+numEntries*2+4; // first is tag (short index, long value)
        }

        int numEntries;
        int TABLE_SIZE;
        int count;

        protected long freeList[] = new long[500];
        protected int freeListIndex = 0;

        private void addFree(long offset) {
            for ( int i = 4; i < TABLE_SIZE; i++)
                base.put(offset+i, (byte)0);
            if ( base.getInt(offset) != numEntries ) { // check tag
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
            long off = arr + 4;
            for ( int i = 0; i < numEntries; i++ ) {
                if (base.getShort(off) == index) { // found empty entry
                    base.putLong(off + 2, value);
                    return true;
                }
                off+=10;
            } // not found => add
            off = arr + 4;
            for ( int i = 0; i < numEntries; i++ ) {
                if (base.getLong(off + 2) == 0) { // found empty entry
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
                return freeList[--freeListIndex];
            }
            if ( baseOff+TABLE_SIZE >= base.length() ) {
                clean();
                if ( freeListIndex == 0 )
                    throw new RuntimeException("index is full. Increase index size. Index Tables: " + tableCount);
                else
                    return newArr();
            }
            tableCount++;
            long res = baseOff;
            base.putInt(res,numEntries);
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
    }

    class FullPArray {

        public static final int TABLE_SIZE = 256*8+4; // first is tag

        protected long freeList[] = new long[500];
        protected int freeListIndex = 0;
        int count = 0;

        private void addFree(long offset) {
            for ( int i = 0; i < TABLE_SIZE/8; i++)
                base.putLong(offset+i*8,0);
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
                clean();
                if ( freeListIndex == 0 )
                    throw new RuntimeException("index is full. Increase index size. Index Tables: " + tableCount);
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

        public void dump(long arr) {
            for ( int i = 0; i < 256; i++ ) {
                long at = getAt(arr, i);
                if ( at != 0 ) {
                    System.out.println(" "+i+" => "+at);
                }
            }
        }
    }

    class ArrWrap {

        void addFree(long offset) {
            int tag = base.getShort(offset);
            switch (tag) {
                case 0:
                    arrFull.addFree(offset);
                    break;
                case 4:
                    arr4.addFree(offset);
                    break;
                case 32:
                    arr4.addFree(offset);
                    break;
                default:
                    throw new RuntimeException("fatal");
            }
        }

        boolean put(long arr, int i, long value) {
            int tag = base.getShort(arr);
            switch (tag) {
                case 0:
                    return arrFull.put(arr,i,value);
                case 4:
                    return arr4.put(arr,i,value);
                case 32:
                    return arr32.put(arr,i,value);
                default:
                    throw new RuntimeException("fatal");
            }
        }

        long getAt(long arr, int i) {
            int tag = base.getShort(arr);
            switch (tag) {
                case 0:
                    return arrFull.getAt(arr, i);
                case 4:
                    return arr4.getAt(arr, i);
                case 32:
                    return arr32.getAt(arr, i);
                default:
                    throw new RuntimeException("fatal");
            }
        }

        long newArr() {
            return arr4.newArr();
        }

        public long stepUp(long arr) {
            int tag = base.getShort(arr);
            switch (tag) {
                case 0:
                    throw new RuntimeException("famous last words: cannot happen");
                case 4:
                    long res0 = arr32.newArr();
                    arr4.copyTo(arr32,arr,res0);
                    arr4.addFree(arr);
//                    System.out.println("src => ");
//                    arr4.dump(arr);
//                    System.out.println("dst => ");
//                    arr32.dump(res0);
                    return res0;
                case 32:
                    long res1 = arrFull.newArr();
                    arr32.copyTo(arrFull,arr, res1);
//                    arr32.addFree(arr);
//                    System.out.println("src => ");
//                    arr32.dump(arr);
//                    System.out.println("dst => ");
//                    arrFull.dump(res1);
                    return res1;
                default:
                    throw new RuntimeException("fatal");
            }
        }

        public void dump(long arr) {
            int tag = base.getShort(arr);
            System.out.println("TAG:"+tag);
            switch (tag) {
                case 0:
                    arrFull.dump(arr); break;
                case 4:
                    arr4.dump(arr); break;
                case 32:
                    arr32.dump(arr); break;
                default:
                    throw new RuntimeException("fatal");
            }
        }
    }


//    public Long remove( ByteSource key ) {
//        if ( key.length() != keyLen )
//            throw new RuntimeException("invalid key length. Expect "+keyLen);
//        return remove(key, 0, arr);
//    }

//    Long remove( ByteSource key, long index, Object arr[] ) {
//        byte b = key.get(index);
//        int i = ((int)b + 256) & 0xff;
//        Object lookup = arr[i];
//        if ( index == keyLen - 1 ) {
//            arr[i] = null;
//            return (Long) lookup;
//        }
//        if ( lookup == null ) {
//            return null;
//        }
//        return remove(key, index + 1, (Object[]) lookup);
//    }

    public static void main(String a[]) {
        int klen = 12;
        OffHeapByteTree bt = new OffHeapByteTree(klen);

        long tim = System.currentTimeMillis();
        LeftCutStringByteSource kwrap = new LeftCutStringByteSource(null, 0, klen);
        int MAX = 1000000;
//        int MAX = 5*1000000;
        for ( int i = 0; i < MAX; i++ ) {
            String key = "test:"+i;
//            String key = ""+Math.random();
//            if ( key.length() > klen )
//                key = key.substring(klen);
            kwrap.setString(key);
            long put = bt.put(kwrap, (long) i);
            if ( put != 0 )
                System.out.println("err "+i+"'"+key+"'"+put);
        }
        long dur = System.currentTimeMillis() - tim+1;
        System.out.println("PUT need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println(" used MB "+bt.baseOff/1024/1024);

        tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            String key = "test:"+i;
            kwrap.setString(key);
            bt.put(kwrap, (long) i);
        }
        dur = System.currentTimeMillis() - tim+1;
        System.out.println("PUT need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println(" used MB "+bt.baseOff/1024/1024);

        tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            String key = "test:"+i;
            kwrap.setString(key);
            long put = bt.get(kwrap);
            if ( put != i )
                System.out.println("err");
        }
        dur = System.currentTimeMillis() - tim+1;
        System.out.println("GET need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println(" used MB "+bt.baseOff/1024/1024);

        for ( int i = 0; i < 5; i++ ) {
            System.gc();
            System.out.println("mem "+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024/1024+" MB");
        }

//        tim = System.currentTimeMillis();
//        for ( int i = 0; i < MAX; i++ ) {
//            String key = "test:"+i;
//            kwrap.setString(key);
//            Long put = bt.remove(kwrap);
//            if ( put.longValue() != i )
//                System.out.println("err");
//        }
//        bt.clean();
//        System.out.println("ba count " + bacount);
//        dur = System.currentTimeMillis() - tim+1;
//        System.out.println("REMOVE need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");

    }

}

