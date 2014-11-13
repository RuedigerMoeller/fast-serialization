package ser;

import com.cedarsoftware.util.DeepEquals;
import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.offheap.bytez.malloc.MallocBytez;
import org.nustaq.offheap.bytez.malloc.MallocBytezAllocator;
import org.nustaq.serialization.*;
import org.nustaq.serialization.simpleapi.OffHeapCoder;

import java.io.IOException;
import java.io.Serializable;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by ruedi on 09.11.14.
 */
public class RawMemTest extends BasicFSTTest {

    @Override
    public void setUp() throws Exception {
        FSTConfiguration conf = FSTConfiguration.createFastBinaryConfiguration();
        out = new FSTObjectOutput(conf);
        in = new FSTObjectInput(conf);
    }

    static Object original = new Object[] {
        new BasicFSTTest.Primitives(),
        new BasicFSTTest.Primitives(),
        new BasicFSTTest.PrimitiveArray(),
        new BasicFSTTest.AscStrings(),
        new BasicFSTTest.Strings(),
        new BasicFSTTest.Bl(),
        new boolean[]{true, false, true},
        new int[]{1, 2, 3,1, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, },
        new BasicFSTTest.Primitives(),
        new BasicFSTTest.Primitives(),
        new BasicFSTTest.PrimitiveArray(),
        new BasicFSTTest.Strings(),
        new BasicFSTTest.Bl(),
        new boolean[]{true, false, true},
    };

    @Test
    public void test() {
        FSTConfiguration conf = FSTConfiguration.createFastBinaryConfiguration();
        Object deser = null;
        byte[] ser = null;

        System.out.println("binary");
        deser = smallBench(conf, original, deser);
        deser = smallBench(conf, original, deser);
        deser = smallBench(conf, original, deser);
        deser = smallBench(conf, original, deser);
        deser = smallBench(conf, original, deser);
        assertTrue(DeepEquals.deepEquals(original, deser));

        System.out.println("default");
        conf = FSTConfiguration.createDefaultConfiguration();
        deser = smallBench(conf, original, deser);
        deser = smallBench(conf, original, deser);
        deser = smallBench(conf, original, deser);
        deser = smallBench(conf, original, deser);
        deser = smallBench(conf, original, deser);
        assertTrue(DeepEquals.deepEquals(original, deser));

        System.out.println("Default LEN:"+FSTConfiguration.createDefaultConfiguration().asByteArray(original).length);
    }

    protected Object smallBench(FSTConfiguration conf, Object original, Object deser) {
        byte[] ser = null;
        int count = 0;
        long tim = System.currentTimeMillis();
        int len[] = { 0 };
        while ( System.currentTimeMillis() - tim < 2000 ) {
            count++;
            ser = conf.asByteArray(original);
            deser = conf.asObject(ser);
        }
        System.out.println("BIN COUNT:"+count);
        return deser;
    }

    static class SimpleTest implements Serializable {
        String x = "pok";
        int i1 = 12345, i2 = 14;
        double a = 23984.234;
    }

    static Object smallClazz = new SimpleTest();

    @Test
    public void testOffHeapCoder() throws Exception {
        OffHeapCoder coder = new OffHeapCoder(SimpleTest.class);

        MallocBytezAllocator alloc = new MallocBytezAllocator();
        MallocBytez bytez = (MallocBytez) alloc.alloc(1000 * 1000);

        ohbench(original, coder, bytez);
        ohbench(original, coder, bytez);
        ohbench(original, coder, bytez);
        ohbench(original, coder, bytez);
        Object deser = ohbench(original, coder, bytez);
        assertTrue(DeepEquals.deepEquals(original, deser));

        System.out.println("-----");
        ohbench(smallClazz, coder, bytez);
        ohbench(smallClazz, coder, bytez);
        ohbench(smallClazz, coder, bytez);
        ohbench(smallClazz, coder, bytez);
        deser = ohbench(smallClazz, coder, bytez);
        assertTrue(DeepEquals.deepEquals(smallClazz, deser));

        boolean lenEx = false;
        try {
            coder.writeObject(original,bytez.getBaseAdress(),10);
        } catch (Exception e) {
            lenEx = true;
        }

        Assert.assertTrue(lenEx);

        alloc.freeAll();
    }

    protected Object ohbench(Object toSer, OffHeapCoder coder, MallocBytez bytez) throws Exception {
        long tim = System.currentTimeMillis();
        int count = 0;
        Object deser = null;
        while ( System.currentTimeMillis() - tim < 2000 ) {
            count++;
            coder.writeObject(toSer, bytez.getBaseAdress(), (int) bytez.length());
            deser = coder.readObject(bytez.getBaseAdress(),(int)bytez.length());
        }
        System.out.println("offheap enc COUNT:"+count);
        return deser;
    }
}
