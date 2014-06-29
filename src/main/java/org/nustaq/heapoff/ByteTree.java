package org.nustaq.heapoff;

import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.heapoff.bytez.Bytez;
import org.nustaq.heapoff.bytez.bytesource.LeftCutStringByteSource;
import org.nustaq.heapoff.bytez.malloc.MallocBytez;
import org.nustaq.heapoff.bytez.malloc.MallocBytezAllocator;

/**
 * Created by ruedi on 27.06.14.
 */
public class ByteTree { // FIXME: get gc friendly by not using object[] but directmem

    static int bacount = 0;

    Object arr[] = new Object[256];
    int keyLen = 0;


    public ByteTree(int keyLen) {
        this.keyLen = keyLen;
    }

    public Long put(ByteSource key, Long value ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("invalid key length. Expect "+keyLen);
        return put(key, 0, arr, value );
    }

    public void clean() {
        clean(0,arr);
    }

    // return true if empty
    boolean clean( long index, Object arr[] ) {
        if ( index == keyLen - 1 ) {
            for (int i = 0; i < arr.length; i++) {
                Object o = arr[i];
                if ( o != null ) {
                    return false;
                }
            }
            return true;
        }
        boolean hadOne = false;
        for (int i = 0; i < arr.length; i++) {
            if ( arr[i] != null ) {
                if (clean(index + 1, (Object[]) arr[i])) {
                    arr[i] = null;
                    bacount--;
                } else {
                    hadOne = true;
                }
            }
        }
        return ! hadOne;
    }

    Long put( ByteSource key, long index, Object arr[], Long toPut ) {
        byte b = key.get(index);
        int i = ((int)b + 256) & 0xff;
        Object lookup = arr[i];
        if ( index == keyLen - 1 ) {
            arr[i] = toPut;
            return (Long) lookup;
        }
        if ( lookup == null ) {
            arr[i] = new Object[256];
            bacount++;
            return put( key, index+1, (Object[]) arr[i], toPut);
        }
        return put( key, index+1, (Object[]) lookup, toPut);
    }

    public Long get( ByteSource key ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("invalid key length. Expect "+keyLen);
        return get(key,0, arr);
    }

    Long get( ByteSource key, long index, Object arr[] ) {
        byte b = key.get(index);
        int i = ((int)b + 256) & 0xff;
        Object lookup = arr[i];
        if ( index == keyLen - 1 ) {
            return (Long) lookup;
        }
        if ( lookup == null ) {
            return null;
        }
        return get( key, index+1, (Object[]) lookup);
    }

    public Long remove( ByteSource key ) {
        if ( key.length() != keyLen )
            throw new RuntimeException("invalid key length. Expect "+keyLen);
        return remove(key, 0, arr);
    }

    Long remove( ByteSource key, long index, Object arr[] ) {
        byte b = key.get(index);
        int i = ((int)b + 256) & 0xff;
        Object lookup = arr[i];
        if ( index == keyLen - 1 ) {
            arr[i] = null;
            return (Long) lookup;
        }
        if ( lookup == null ) {
            return null;
        }
        return remove(key, index + 1, (Object[]) lookup);
    }

    public static void main(String a[]) {
        int klen = 12;
        ByteTree bt = new ByteTree(klen);

        long tim = System.currentTimeMillis();
        LeftCutStringByteSource kwrap = new LeftCutStringByteSource(null, 0, klen);
//        int MAX = 1;
        int MAX = 5*1000000;
        for ( int i = 0; i < MAX; i++ ) {
            String key = "test:"+i;
            kwrap.setString(key);
            Long put = bt.put(kwrap, (long) i);
            if ( put != null )
                System.out.println("err");
        }
        long dur = System.currentTimeMillis() - tim+1;
        System.out.println("PUT need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println("ba count "+bacount);

        tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            String key = "test:"+i;
            kwrap.setString(key);
            bt.put(kwrap, (long) i);
        }
        dur = System.currentTimeMillis() - tim+1;
        System.out.println("PUT need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println("ba count "+bacount);

        tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            String key = "test:"+i;
            kwrap.setString(key);
            Long put = bt.get(kwrap);
            if ( put.longValue() != i )
                System.out.println("err");
        }
        dur = System.currentTimeMillis() - tim+1;
        System.out.println("GET need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println("ba count "+bacount);

        for ( int i = 0; i < 5; i++ ) {
            System.gc();
            System.out.println("mem "+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024/1024+" MB");
        }

        tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            String key = "test:"+i;
            kwrap.setString(key);
            Long put = bt.remove(kwrap);
            if ( put.longValue() != i )
                System.out.println("err");
        }
        bt.clean();
        System.out.println("ba count " + bacount);
        dur = System.currentTimeMillis() - tim+1;
        System.out.println("REMOVE need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");

    }

}
