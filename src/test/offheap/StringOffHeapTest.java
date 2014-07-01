package offheap;

import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.heapoff.FSTAsciiStringOffheapMap;
import org.nustaq.heapoff.OffHeapByteTree;
import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by ruedi on 30.06.14.
 */
public class StringOffHeapTest {

    static class TestRec implements Serializable {
        int x = 13;
        int y = 133;
        String id;
        String someRandomString = "pok pok pok pok pok";

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


    @Test
    public void testPutGetIter() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerClass(TestRec.class);
        int klen = 16;
        int MAX = 10000000;

        int indexSizeMB = OffHeapByteTree.estimateMBytesForIndex(klen, MAX);
        System.out.println("esitmated index size "+ indexSizeMB +" MB");

        FSTAsciiStringOffheapMap store = new FSTAsciiStringOffheapMap(klen, 2*FSTAsciiStringOffheapMap.GB, indexSizeMB, conf);

        long tim = System.currentTimeMillis();
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
        Assert.assertTrue(store.getSize() == MAX);

        tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {

            TestRec rec = (TestRec) store.get("test:"+i);
            if ( rec == null || rec.getX() != i )
                throw new RuntimeException("error");
        }

        dur = System.currentTimeMillis() - tim;
        System.out.println("get need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");

        long freeMem = store.getFreeMem();
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
        Assert.assertTrue(store.getFreeMem() == freeMem); // ensure in place update happened

        store.remove("test:13");
        store.remove("test:999");
        store.remove("unknown");

        freeMem = store.getFreeMem();
        TestRec value = new TestRec();
        value.someRandomString = "somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger";
        value.someRandomString += value.someRandomString;
        store.put("test:999", value);
        TestRec readVal = (TestRec) store.get("test:999");
        Assert.assertTrue(readVal.someRandomString.equals(value.someRandomString));
        Assert.assertTrue(store.getFreeMem() != freeMem); // ensure adding update happened

        tim = System.currentTimeMillis();
        Iterator values = store.values();
        int iterCnt = 0;
        while( values.hasNext() ) {
            Object read = values.next();
            Assert.assertTrue(read != null);
            iterCnt++;
        }
        dur = System.currentTimeMillis() - tim+1;
        System.out.println("iter "+ dur +" for "+iterCnt+" recs. "+(MAX/dur)+" per ms ");
        Assert.assertTrue(iterCnt == store.getSize());

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
        dur = System.currentTimeMillis() - tim+1;
        System.out.println("bin iter "+ dur +" for "+iterCnt+" recs. "+(MAX/dur)+" per ms ");
        Assert.assertTrue(iterCnt == store.getSize());
        store.free();
    }

    @Test
    public void randomTest() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerClass(TestRec.class);
        int klen = 16;
        int MAX = 1000000;

        int indexSizeMB = OffHeapByteTree.estimateMBytesForIndex(klen, MAX);
        System.out.println("esitmated index size "+ indexSizeMB +" MB");

        ArrayList<String> keys = new ArrayList<>();
        FSTAsciiStringOffheapMap store = new FSTAsciiStringOffheapMap(klen, 2*FSTAsciiStringOffheapMap.GB, indexSizeMB, conf);
        TestRec val = new TestRec();

        while( true ) {
            System.out.println("put");
            for (int i = 0; i < MAX; i++) {
                String key = "" + Math.random();
                if (key.length() > klen)
                    key = key.substring(0, klen);
                keys.add(key);
                val.setId(key);
                store.put(key, val);
            }

            store.dumpIndexStats();

            System.out.println("remove");
            for (int i = 0; i < keys.size(); i++) {
                String s = keys.get(i);
                TestRec rec = (TestRec) store.get(s);
                if ( rec != null ) {
                    store.remove(s);
                    Assert.assertTrue(rec.getId().equals(s));
                }
            }
            keys.clear();
            store.dumpIndexStats();
            System.out.println("-----------------------------------");
            System.out.println("-----------------------------------");

//            store.put("p",new TestRec());
//            System.out.println("p-----------------------------------");
//            store.dumpIndexStats();
//            Object pokpokpok = store.get("p");
//            System.out.println("g-----------------------------------");
//            store.dumpIndexStats();
//            System.out.printf(""+pokpokpok);
        }

    }
}
