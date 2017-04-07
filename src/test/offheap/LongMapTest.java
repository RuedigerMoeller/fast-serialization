package offheap;

import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.offheap.FSTLongOffheapMap;

/**
 * Created by ruedi on 15.11.14.
 */
public class LongMapTest {

    @Test
    public void testLongMap() {
        FSTLongOffheapMap<String> longMap = new FSTLongOffheapMap<String>(FSTLongOffheapMap.GB,1000000);

        for ( int i = 0; i < 1000000; i++) {
            longMap.put((long) i,"HalliHallo"+i);
        }

        for ( int i = 0; i < 1000000; i++) {
            String s = longMap.get((long) i);
            Assert.assertTrue(s.equals("HalliHallo"+i));
        }

        for ( int i = 0; i < 1000000; i++) {
            longMap.remove((long) i);
            Assert.assertTrue(longMap.get((long) i) == null);
        }

        longMap.free();
    }
}
