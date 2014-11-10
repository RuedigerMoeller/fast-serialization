package offheap;

import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.offheap.FSTAsciiStringOffheapMap;
import org.nustaq.offheap.OffHeapByteTree;
import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.bytesource.AsciiStringByteSource;
import org.nustaq.serialization.FSTConfiguration;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by ruedi on 30.06.14.
 */
public class StringOffHeapTest {

    @Test
    public void testIndex() {
        int count = 10;
        while( count-- > 0 ) {
            System.out.println("start");
            OffHeapByteTree bt = new OffHeapByteTree(16, 10);
            for (int i = 0; i < MAX; i++) {
                String key = "" + Math.random() + "0000000000000000000";
                key = key.substring(0, klen);
                bt.put(new AsciiStringByteSource(key), i + 1);
            }
            bt.dumpStats();
            bt.free();
            System.out.println("done");
        }

    }

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
    public void testKeys() throws Exception {
        new File("/tmp/test1.mmf").delete();
        FSTConfiguration conf = createConfiguration();
        conf.registerClass(TestRec.class);

        int count = 10;
        FSTAsciiStringOffheapMap<String> store = new FSTAsciiStringOffheapMap<>("/tmp/test1.mmf", klen, 2 * FSTAsciiStringOffheapMap.GB, MAX, conf);
        while( count-->0 ) {

            for (int i = 0; i < MAX; i++) {
                String key = "" + Math.random() + "0000000000000000000";
                key = key.substring(0, klen);
                store.put(key, key);
            }
            store.dumpIndexStats();

            ArrayList<String> toRem = new ArrayList<>();
            for (java.util.Iterator iterator = store.values(); iterator.hasNext(); ) {
                String next = (String) iterator.next();
                if (Math.random() > .5) {
                    toRem.add(next);
                }
            }
            for (int i = 0; i < toRem.size(); i++) {
                String s = toRem.get(i);
                store.remove(s);
            }
            store.dumpIndexStats();

        }
        store.free();

        System.out.println();
        System.out.println("reload ..");
        System.out.println();
        count = 10;
        while( count-->0 ) {

            store = new FSTAsciiStringOffheapMap<>("/tmp/test1.mmf", klen, 2 * FSTAsciiStringOffheapMap.GB, MAX, conf);
            for (int i = 0; i < MAX; i++) {
                String key = "" + Math.random() + "0000000000000000000";
                key = key.substring(0, klen);
                store.put(key, key);
            }
            store.dumpIndexStats();

            for (java.util.Iterator iterator = store.values(); iterator.hasNext(); ) {
                String next = (String) iterator.next();
                if (Math.random() > .5)
                    store.remove(next);
            }
            store.dumpIndexStats();
            store.free();

        }
    }

    @Test
    public void hardcore() throws Exception {
        new File("/tmp/test.mmf").delete();
        fillMemMapped();
        FSTConfiguration conf = createConfiguration();
        conf.registerClass(TestRec.class);

        int count = 10;
//        while (true)
        while (count-- > 0)
        {
            FSTAsciiStringOffheapMap<TestRec> store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2 * FSTAsciiStringOffheapMap.GB, MAX, conf);
            checkIter(store);
            mutateRandom(MAX, store);
            checkIter(store);
            store.free();

            store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2 * FSTAsciiStringOffheapMap.GB, MAX, conf);
            store.dumpIndexStats();
            addRemRandom(store);
            checkIter(store);
            store.free();

            store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2 * FSTAsciiStringOffheapMap.GB, MAX, conf);
            store.dumpIndexStats();
            checkIter(store);
            mutateRandom(MAX, store);
            addRemRandom(store);
            checkIter(store);
            store.free();
        }
    }

    private void addRemRandom(FSTAsciiStringOffheapMap<TestRec> store) {
        long tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            if ( Math.random() > .5 ) {
                store.remove("test:"+((int)(Math.random()*MAX)));
            } else {
                int idx = (int) (Math.random()*MAX);
                TestRec rec = new TestRec();
                rec.setX(idx);
                String key = "test:" + idx;
                rec.setId(key);
                store.put(key, rec);
            }
        }
        long dur = System.currentTimeMillis() - tim+1;
        System.out.println("add/rem needed "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
    }

    private void checkIter(FSTAsciiStringOffheapMap<TestRec> store) {
        int count = 0;
        for (Iterator<TestRec> iterator = store.values(); iterator.hasNext(); ) {
            TestRec next = iterator.next();
            count++;
            if ( next == null )
                System.out.println("FAIL:"+count);
            Assert.assertTrue(next.getId().equals("test:" + next.getX()));
        }
    }

    @Test
    public void fillAndReloadMemMapped() throws Exception {
        new File("/tmp/test.mmf").delete();
        fillMemMapped();
        FSTConfiguration conf = createConfiguration();
        conf.registerClass(TestRec.class);

        FSTAsciiStringOffheapMap<TestRec> store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);

        Assert.assertTrue(store.getSize() == MAX);
        checkStore(store);

        store.free();

        store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        mutateRandom(MAX,store);
        checkStore(store);
        store.free();

        store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        checkStore(store);
        store.free();

        store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        store.remove("test:13");
        System.out.println("store size after remove " + store.getSize());
        store.free();

        store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        System.out.println("store size after reload "+store.getSize());
        Assert.assertTrue(store.getSize() == MAX - 1);
        TestRec val = new TestRec();
        val.setX(13);
        val.someRandomString ="pok";
        val.setId("test:13");
        store.put("test:13",val);
        Assert.assertTrue(store.getSize() == MAX);
        checkStore(store);
        store.free();

        store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        fillAll(store);
        store.free();

        store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        removeAll(MAX,store);
        System.out.println("store size after remove "+store.getSize());
        store.free();

        store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        fillAll(store);
        checkStore(store);
        store.free();

        store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        checkStore(store);
        store.free();
    }

    private void checkStore(FSTAsciiStringOffheapMap<TestRec> store) {
        long tim = System.currentTimeMillis();
        for ( int i = 0; i < store.getSize(); i++ ) {
            String key = "test:" + i;
            TestRec testRec = store.get(key);
            if ( testRec == null )
                System.out.println("fail:"+i);
            boolean condition = testRec.getX() == i;
            Assert.assertTrue(condition);
        }
        long dur = System.currentTimeMillis() - tim+1;
        System.out.println("check needed "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
    }

    int klen = 16;
    int MAX = 200000;

    @Test
    public void fillMemMapped() throws Exception {
        FSTConfiguration conf = createConfiguration();
        conf.registerClass(TestRec.class);
        new File("/tmp/test.mmf").delete();

        FSTAsciiStringOffheapMap<TestRec> store = new FSTAsciiStringOffheapMap<>("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);

        fillAll(store);
        for ( int i = 0; i < store.getSize(); i++ ) {
            String key = "test:" + i;
            Assert.assertTrue(store.get(key).getX() == i);
        }
        Assert.assertTrue(store.getSize() == MAX);
        store.free();
    }

    protected FSTConfiguration createConfiguration() {
        return FSTConfiguration.createDefaultConfiguration();
    }

    private void fillAll(FSTAsciiStringOffheapMap<TestRec> store) {
        long tim = System.currentTimeMillis();
        TestRec val = new TestRec();
        for ( int i = store.getSize(); i < MAX; i++ ) {
            val.setX(i);
            String key = "test:" + i;
            val.setId(key);
            store.put(key, val );
        }
        long dur = System.currentTimeMillis() - tim+1;
        System.out.println("put need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println("free: "+store.getFreeMem()/1024/1024);
    }

    @Test
    public void removeSomeMemMapped() throws Exception {
        FSTConfiguration conf = createConfiguration();
        conf.registerClass(TestRec.class);
        int klen = 16;
        new File("/tmp/test.mmf").delete();
        FSTAsciiStringOffheapMap store = new FSTAsciiStringOffheapMap("/tmp/test.mmf", klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        fillAll(store);
        System.out.println("size pre "+store.getSize());
        removeAll(MAX, store);
        store.free();
    }

    private void removeAll(int MAX, FSTAsciiStringOffheapMap store) {
        long tim = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            String key = "test:" + i;
            store.remove(key);
        }
        long dur = System.currentTimeMillis() - tim+1;
        System.out.println("remove need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        System.out.println("free: " + store.getFreeMem() / 1024 / 1024);
        System.out.println("size "+store.getSize());
        Assert.assertTrue(store.getSize() == 0);
    }

    String longString = "somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger+somewhatlonger";

    @Test
    public void testPutGetIter() {
        FSTConfiguration conf = createConfiguration();
        conf.registerClass(TestRec.class);
        int klen = 16;
        int MAX = 100000;

        FSTAsciiStringOffheapMap store = new FSTAsciiStringOffheapMap(klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);

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
            rec.someRandomString = "#"+i+"#"+i+"#"+i+"######";
            store.put(key,rec);
        }
        dur = System.currentTimeMillis() - tim;
        System.out.println("update inplace need "+ dur +" for "+MAX+" recs. "+(MAX/dur)+" per ms ");
        Assert.assertTrue(store.getFreeMem() == freeMem); // ensure in place update happened

        store.remove("test:13");
        store.remove("test:999");
        store.remove("unknown");

        freeMem = store.getFreeMem();
        TestRec value = new TestRec();
        value.someRandomString = longString;
        value.someRandomString += value.someRandomString;
        value.setId("test:999");
        store.put("test:999", value);
        TestRec readVal = (TestRec) store.get("test:999");
        Assert.assertTrue(readVal.someRandomString.equals(value.someRandomString));
        Assert.assertTrue(store.getFreeMem() != freeMem); // ensure adding update happened

        mutateRandom(MAX, store);

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

    private void mutateRandom(int MAX, FSTAsciiStringOffheapMap store) {
        long tim;
        long dur;
        for ( int ii = 0; ii < 10; ii++) {
            tim = System.currentTimeMillis();
            for (int i = 0; i < MAX; i++) {
                try {
                    String key = "test:" + i;
                    TestRec rec = (TestRec) store.get(key);
                    if (rec != null ) {
                        rec.someRandomString = longString.substring(0, (int) (Math.random() * longString.length()));
                        store.put(key, rec);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.assertTrue(false);
                }
            }
            dur = System.currentTimeMillis() - tim;
            System.out.println("update random need " + dur + " for " + MAX + " recs. " + (MAX / dur) + " per ms ");
        }

        System.out.println("UsedMem "+store.getUsedMem()/1024/1024+" MB elems "+store.getSize()+" per elem bytes "+store.getUsedMem()/store.getSize());
    }

    @Test
    public void randomTest() {
        FSTConfiguration conf = createConfiguration();
        conf.registerClass(TestRec.class);
        int klen = 16;
        int MAX = 10000;

        ArrayList<String> keys = new ArrayList<>();
        FSTAsciiStringOffheapMap store = new FSTAsciiStringOffheapMap(klen, 2*FSTAsciiStringOffheapMap.GB, MAX, conf);
        TestRec val = new TestRec();

        //while( true )
        {
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
