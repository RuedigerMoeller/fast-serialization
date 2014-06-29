package org.nustaq.heapoff;

import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.heapoff.bytez.bytesource.LeftCutStringByteSource;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created by ruedi on 27.06.14.
 */
public class FSTAsciiStringOffheapMap<V> extends FSTSerializedOffheapMap<String,V> {

    LeftCutStringByteSource tmpKey;

    public FSTAsciiStringOffheapMap(int keyLen, long size, FSTConfiguration conf) {
        super(keyLen, size, conf);
    }

    @Override
    protected void init(int keyLen, long size) {
        super.init(keyLen, size);
        tmpKey = new LeftCutStringByteSource(null,0,keyLen);
    }

    protected ByteSource encodeKey(String key) {
        if ( key.length() >= tmpKey.length() )
            throw new RuntimeException("key too long");
        tmpKey.setString(key);
        return tmpKey;
    }

    public static void main( String a[] ) {
        FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();
        conf.registerClass(TestRec.class);
        FSTAsciiStringOffheapMap store = new FSTAsciiStringOffheapMap(16, 8*GB, conf);
        long tim = System.currentTimeMillis();
//        int MAX = 5*1000000;
        int MAX = 1000000;
        TestRec val = new TestRec();
        for ( int i = 0; i < MAX; i++ ) {
            val.setX(i);
            String key = "test:" + i;
            val.setId(key);
            store.put(key, val );
        }
        long dur = System.currentTimeMillis() - tim;
        System.out.println("put need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println("free: "+store.getFreeMem()/1024/1024);

        tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {

            TestRec rec = (TestRec) store.get("test:"+i);
            if ( rec == null || rec.getX() != i )
                throw new RuntimeException("error");
        }

        dur = System.currentTimeMillis() - tim;
        System.out.println("get need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");

        tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {

            String key = "test:" + i;
            TestRec rec = (TestRec) store.get(key);
            if ( rec == null || rec.getX() != i )
                throw new RuntimeException("error");
            rec.someRandomString = "#"+i+"#"+i+"#"+i;
            store.put(key,rec);
        }

        dur = System.currentTimeMillis() - tim;
        System.out.println("update need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");

        store.remove("test:13");
        store.remove("test:999");
        store.remove("unknown");
        TestRec value = new TestRec();
        value.someRandomString = "somewhatlonger+somewhatlonger+somewhatlonger";
        store.put("test:999", value);
        TestRec readVal = (TestRec) store.get("test:999");
        if ( !readVal.someRandomString.equals(value.someRandomString))
            throw new RuntimeException("wrong stuff");

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
    static class TestRec implements Serializable{
        int x = 13;
        String id;
        String someRandomString = "pok";

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

}


