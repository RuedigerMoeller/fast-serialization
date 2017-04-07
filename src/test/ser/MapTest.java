package ser;

import org.junit.Test;
import org.nustaq.serialization.util.FSTIdentity2IdMap;

/**
 * Created by moelrue on 12/12/14.
 */
public class MapTest {

    @Test
    public void testFST() {

        String strings[] = new String[5000];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = "" + Math.random();
        }


        FSTIdentity2IdMap map = new FSTIdentity2IdMap(97);

        // warm
        int iters = 50000;
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

    private static void testExistPut(String[] strings, FSTIdentity2IdMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            int fieldId = map.putOrGet(string, i);
//            if ( fieldId != i ) {
//                throw new RuntimeException("möp 1 "+i+" "+fieldId);
//            }
        }
    }

    private static void testPut(String[] strings, FSTIdentity2IdMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            map.put(string, i);
        }
    }

    private static void testGet(String[] strings, FSTIdentity2IdMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            int fieldId = map.get(string);
            if (fieldId != i) {
                throw new RuntimeException("möp 2 " + i + " " + fieldId);
            }
        }
    }

    private static void testWrongGet(FSTIdentity2IdMap map) {
        for (int i = 0; i < 1000; i++) {
            int fieldId = map.get("pok" + i);
            if (fieldId != Integer.MIN_VALUE) {
                throw new RuntimeException("möp 3 " + i + " " + fieldId);
            }
        }
    }

    private static void testNewPut(String[] strings, FSTIdentity2IdMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (map.putOrGet(string, i) > 0) {
                throw new RuntimeException("möp");
            }
        }
    }

}
