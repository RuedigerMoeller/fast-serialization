package ser;

import org.junit.Test;
import org.nustaq.serialization.util.FSTIdentity2IdMap;

import java.util.IdentityHashMap;

/**
 * Created by moelrue on 12/12/14.
 */
public class Map1Test {

    @Test
    public void testJDK() {

        String strings[] = new String[5000];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = "" + Math.random();
        }


        IdentityHashMap map = new IdentityHashMap(97);

        // warm
        int iters = 50_000;
        for (int j = 0; j < iters; j++) {
            testNewPut(strings, map);
            map.clear();
        }

        long tim = System.currentTimeMillis();
        for (int j = 0; j < iters; j++) {
            testNewPut(strings, map);
            map.clear();
        }

        testNewPut(strings, map);
        testGet(strings, map);
        map.clear();
        testPut(strings, map);
        testGet(strings, map);
        testWrongGet(map);
        long now = System.currentTimeMillis();
        System.out.println("time new " + (now - tim));

        tim = System.currentTimeMillis();
        for (int j = 0; j < iters; j++) {
//            testExistPut(strings, map);
            testPut(strings, map);
        }
        now = System.currentTimeMillis();
        System.out.println("time exist put " + (now - tim));

        tim = System.currentTimeMillis();
        for (int j = 0; j < iters; j++) {
            testGet(strings, map);
        }
        now = System.currentTimeMillis();
        System.out.println("time exist " + (now - tim));


    }

    private static void testExistPut(String[] strings, IdentityHashMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            Integer fieldId = (Integer) map.putIfAbsent(string, i);
//            if ( fieldId != i ) {
//                throw new RuntimeException("möp 1 "+i+" "+fieldId);
//            }
        }
    }

    private static void testPut(String[] strings, IdentityHashMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            map.put(string, i);
        }
    }

    private static void testGet(String[] strings, IdentityHashMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            Integer fieldId = (Integer) map.get(string);
            if (fieldId != i) {
                throw new RuntimeException("möp 2 " + i + " " + fieldId);
            }
        }
    }

    private static void testWrongGet(IdentityHashMap map) {
        for (int i = 0; i < 1000; i++) {
            Integer fieldId = (Integer) map.get("pok" + i);
            if (fieldId != null) {
                throw new RuntimeException("möp 3 " + i + " " + fieldId);
            }
        }
    }

    private static void testNewPut(String[] strings, IdentityHashMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            Object o = map.putIfAbsent(string, i);
            if (o != null) {
                throw new RuntimeException("möp");
            }
        }
    }
}
