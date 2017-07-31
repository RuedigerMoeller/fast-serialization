package ser;

import com.cedarsoftware.util.DeepEquals;
import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.offheap.bytez.malloc.MallocBytez;
import org.nustaq.offheap.bytez.malloc.MallocBytezAllocator;
import org.nustaq.serialization.*;
import org.nustaq.serialization.simpleapi.OffHeapCoder;
import org.nustaq.serialization.simpleapi.OnHeapCoder;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by ruedi on 09.11.14.
 */
public class RawMemTest extends BasicFSTTest {

    @Override
    public void setUp() throws Exception {
        FSTConfiguration conf = FSTConfiguration.createUnsafeBinaryConfiguration();
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
        FSTConfiguration conf = FSTConfiguration.createUnsafeBinaryConfiguration();
        Object deser = null;
        byte[] ser = null;

        System.out.println("binary");
        deser = smallBench(conf, original, deser);
        deser = smallBench(conf, original, deser);
        assertTrue(DeepEquals.deepEquals(original, deser));

        System.out.println("default");
        conf = FSTConfiguration.createDefaultConfiguration();
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
        while ( System.currentTimeMillis() - tim < 1000 ) {
            count++;
            ser = conf.asByteArray(original);
            deser = conf.asObject(ser);
        }
        System.out.println("BIN COUNT:"+count);
        return deser;
    }

    static Object smallClazz = CarBench.setupSampleObject();

    @Test
    public void testOffHeapCoder() throws Exception {
        testOffHeapCoder0(true);
        System.out.println("----------unshared----------");
        testOffHeapCoder0(false);
    }

    public void testOffHeapCoder0( boolean shared ) throws Exception {
        OffHeapCoder coder = new OffHeapCoder(shared,
                CarBench.Car.class, CarBench.Engine.class, CarBench.Model.class,
                CarBench.Accel.class, CarBench.PerformanceFigures.class,
                CarBench.FueldData.class, CarBench.OptionalExtras.class);
//        OffHeapCoder coder = new OffHeapCoder();

        MallocBytezAllocator alloc = new MallocBytezAllocator();
        MallocBytez bytez = (MallocBytez) alloc.alloc(1000 * 1000);

        ohbench(original, coder, bytez);
        ohbench(original, coder, bytez);
        Object deser = ohbench(original, coder, bytez);
        assertTrue(DeepEquals.deepEquals(original, deser));

        System.out.println("-----");
        ohbench(smallClazz, coder, bytez);
        ohbench(smallClazz, coder, bytez);
        deser = ohbench(smallClazz, coder, bytez);
        assertTrue(DeepEquals.deepEquals(smallClazz, deser));

        boolean lenEx = false;
        try {
            coder.toMemory(original, bytez.getBaseAdress(), 10);
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
        while ( System.currentTimeMillis() - tim < 1000 ) {
            count++;
            coder.toMemory(toSer, bytez.getBaseAdress(), (int) bytez.length());
            deser = coder.toObject(bytez.getBaseAdress(), (int) bytez.length());
        }
        System.out.println("offheap enc COUNT:"+count);
        return deser;
    }

    @Test
    public void testOnHeapCoder() throws Exception {
        testOnHeapCoder0(true);
        System.out.println("unshared ..");
        testOnHeapCoder0(false);
    }

    public void testOnHeapCoder0(boolean shared) throws Exception {
        OnHeapCoder coder =new OnHeapCoder(shared,
                CarBench.Car.class, CarBench.Engine.class, CarBench.Model.class,
                CarBench.Accel.class, CarBench.PerformanceFigures.class,
                CarBench.FueldData.class, CarBench.OptionalExtras.class);

        byte arr[] = new byte[1000000];
        int len = coder.toByteArray(original, arr, 0, (int) arr.length);

        Object deser = coder.toObject(arr, 0, (int) arr.length);
        assertTrue(DeepEquals.deepEquals(deser,original));

        onhbench(original, coder, arr, 0);
        onhbench(original, coder, arr, 0);
        deser = onhbench(original, coder, arr, 0);
        assertTrue(DeepEquals.deepEquals(original, deser));

        System.out.println("-----");
        deser = onhbench(smallClazz, coder, arr, 0);
        deser = onhbench(smallClazz, coder, arr, 0);
        assertTrue(DeepEquals.deepEquals(smallClazz, deser));

        boolean lenEx = false;
        try {
            coder.toByteArray(original, arr, 0, 10);
        } catch (Exception e) {
            lenEx = true;
        }

        Assert.assertTrue(lenEx);
    }

    @Test
    public void testEnums() throws Exception {
        super.testEnums();
    }

    protected Object onhbench(Object toSer, OnHeapCoder coder, byte[] bytez, int off) throws Exception {
        long tim = System.currentTimeMillis();
        int count = 0;
        Object deser = null;
        while ( System.currentTimeMillis() - tim < 1000 ) {
            count++;
            coder.toByteArray(toSer, bytez, off, (int) bytez.length);
            deser = coder.toObject(bytez, off, (int) bytez.length);
        }
        System.out.println("onheap enc COUNT:"+count);
        return deser;
    }

}
