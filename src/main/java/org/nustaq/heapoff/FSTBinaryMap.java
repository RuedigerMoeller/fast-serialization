package org.nustaq.heapoff;

import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.heapoff.bytez.bytesource.BytezByteSource;
import org.nustaq.heapoff.bytez.bytesource.LeftCutStringByteSource;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.heapoff.bytez.Bytez;
import org.nustaq.heapoff.bytez.malloc.MallocBytezAllocator;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created by ruedi on 25.06.14.
 */
public class FSTBinaryMap {

    FSTConfiguration conf;

    public static final long MB = 1024 * 1024;
    public static final long GB = 1024 * MB;
    ByteTree index;
    Bytez memory;
    long bytezOffset = 0;
    MallocBytezAllocator alloc;
    LeftCutStringByteSource tmpKey;
    int numElem;

    public FSTBinaryMap(int keyLen, FSTConfiguration conf) {
        this.conf = conf;
        init(keyLen);
    }

    void init(int keyLen) {
        index = new ByteTree(keyLen);
        alloc = new MallocBytezAllocator();
        memory = alloc.alloc(8*GB);
        tmpKey = new LeftCutStringByteSource(null,0,keyLen);
    }

    public void put( String key, Serializable value ) {
        keyFromString(key,tmpKey);
        byte[] bytes = conf.asByteArray(value);
        Long put = index.put(tmpKey, bytezOffset);
        if ( put == null )
            numElem++;
        if ( memory.length() <= bytes.length+ getHeaderLen())
            throw new RuntimeException("store is full "+numElem);
        createHeader(bytes, key, value);
        bytezOffset += getHeaderLen();
        memory.set(bytezOffset,bytes,0,bytes.length);
        bytezOffset += bytes.length;
    }

    protected void createHeader(byte[] bytes, String key, Serializable value) {
        memory.putInt(bytezOffset, bytes.length);
        memory.put(bytezOffset+4, (byte)0);
    }

    protected int getHeaderLen() {
        return 4+4; // 0-3 len, 4 removed flag, 5-7 free
    }

    protected int getLenFromHeader(long off) {
        return memory.getInt(off);
    }

    public void remove( String key ) {
        keyFromString(key,tmpKey);
        Long rem = index.remove(tmpKey);
        if ( rem != null ) {
            numElem--;
            memory.put(rem.longValue()+4,(byte)1);
        }
    }

    public Iterator values() {
        return new Iterator() {
            long off = 0;
            int elemCount = 0;
            @Override
            public boolean hasNext() {
                return elemCount < numElem;
            }

            @Override
            public Object next() {
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
                byte[] bytes = memory.toBytes(off, len);
                off += len;
                return conf.asObject(bytes);
            }
        };
    }

    public Iterator<ByteSource> binaryValues() {
        return new Iterator<ByteSource>() {
            long off = 0;
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

    public Serializable get( String key ) {
        keyFromString(key,tmpKey);
        Long aLong = index.get(tmpKey);
        if ( aLong == null ) {
            return null;
        }
        long off = aLong.longValue();
        int len = getLenFromHeader(off);
        off+= getHeaderLen();
        byte[] bytes = memory.toBytes(off, len);
        return (Serializable) conf.asObject(bytes);
    }

    public long getFreeMem() {
        return memory.length()-bytezOffset;
    }

    void keyFromString( String key, LeftCutStringByteSource target ) {
        if ( key.length() >= tmpKey.length() )
            throw new RuntimeException("key too long");
        target.setString(key);
    }

    static class TestRec implements Serializable{
        int x = 13;
        String id;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static void main( String a[] ) {
        FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();
        conf.registerClass(TestRec.class);
        FSTBinaryMap store = new FSTBinaryMap(16, conf);
        long tim = System.currentTimeMillis();
//        int MAX = 5*1000000;
        int MAX = 1000000;
        TestRec val = new TestRec();
        for ( int i = 0; i < MAX; i++ ) {
            val.setX(i);
            String key = "test:" + i;
            val.setId(key);
            store.put(key, val );
//            if ( (i % 1000) == 0) {
//                System.out.println("i "+i);
//            }
        }
        long dur = System.currentTimeMillis() - tim;
        System.out.println("need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println("free: "+store.getFreeMem()/1024/1024);

        tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {

            TestRec rec = (TestRec) store.get("test:"+i);
            if ( rec == null || rec.getX() != i )
                throw new RuntimeException("error");
//            if ( (i % 1000) == 0) {
//                System.out.println("i "+i);
//            }
        }

        dur = System.currentTimeMillis() - tim;
        System.out.println("need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");

        store.remove("test:13");
        store.remove("test:999");
        store.remove("unknown");

        tim = System.currentTimeMillis();
        Iterator values = store.values();
        int iterCnt = 0;
        while( values.hasNext() ) {
            Object read = values.next();
            if ( read == null ) {
                System.out.println("ERROR");
            }
            iterCnt++;
        }
        dur = System.currentTimeMillis() - tim;
        System.out.println("iter "+ dur +" for "+iterCnt+" recs. "+(MAX/dur)+" per ms ");

        tim = System.currentTimeMillis();
        Iterator<ByteSource> bvalues = store.binaryValues();
        iterCnt = 0;
        while( bvalues.hasNext() ) {
            ByteSource read = bvalues.next();
            if ( read == null ) {
                System.out.println("ERROR");
            }
            iterCnt++;
        }
        dur = System.currentTimeMillis() - tim;
        System.out.println("bin iter "+ dur +" for "+iterCnt+" recs. "+(MAX/dur)+" per ms ");

    }

}
