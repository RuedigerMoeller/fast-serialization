package ser;

import com.cedarsoftware.util.DeepEquals;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.minbin.MBPrinter;
import org.junit.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by ruedi on 29.04.14.
 */
public class BasicMinBinTest extends BasicFSTTest {
    @Override
    public void setUp() throws Exception {
        FSTConfiguration conf = FSTConfiguration.createMinBinConfiguration();
        conf.registerCrossPlatformClassMapping( new String[][] {
                        {"senum", "ser.Basics$SampleEnum"},
                        {"special", "ser.Basics$SpecialEnum"},
                }
        );
        out = new FSTObjectOutput(conf);
        in = new FSTObjectInput(conf);
    }

    @Override @Test
    public void testStrings() throws Exception {
        super.testStrings();
    }

    @Override
    public void testPrimitives() throws Exception {
        super.testPrimitives();
        MBPrinter.printMessage(lastBinary);
        writeTmp("testprim.minbin", lastBinary);
    }

    @Override @Test
    public void testPrimitiveArray() throws Exception {
        PrimitiveArray obj = new PrimitiveArray();
        out.writeObject(obj);
        in.resetForReuseUseArray(lastBinary = out.getCopyOfWrittenBuffer());
        out.flush();
        PrimitiveArray res = (PrimitiveArray) in.readObject();
        assertTrue(res.aLong0 == res.aRef);
        assertTrue(res.aRef1 == res.mix[1]);
        assertTrue(DeepEquals.deepEquals(obj.mix[1],res.mix[1])); // bool int mix up on BigNums ..
    }

    public static class Long4JS implements Serializable {
        long aLong = 12345;
        Long bigLong = 3456789934857l;
        long arr[] = { 123456, 1234567890l };
        Long bigArr[] = { 45678901234l, 1234567890l };
    }

    @Test
    public void testLongHateJSNumbers() throws Exception {
        Long4JS obj = new Long4JS();
        out.writeObject(obj);
        in.resetForReuseUseArray(lastBinary = out.getCopyOfWrittenBuffer());
        out.flush();
        Long4JS res = (Long4JS) in.readObject();
        assertTrue(DeepEquals.deepEquals(obj,res));
        writeTmp("long4js.minbin",lastBinary);
    }

    @Test
    public void testException() throws Exception { super.testException(); }

    @Test
    public void testEnums() throws Exception {
        Basics obj = new Basics(123);
        out.writeObject(obj);
        in.resetForReuseUseArray(out.getCopyOfWrittenBuffer());
        out.flush();
        Basics res = (Basics) in.readObject();

        // note: fix false alarm with 1.7_71 + newer 1.8. (because stacktrace not serialized ofc)
        Object[] exceptions1 = res.exceptions;
        Object[] exceptions2 = obj.exceptions;
        res.exceptions = obj.exceptions = null;

        for (int i = 1; i < exceptions1.length; i++) {
            assertTrue( exceptions1[i].getClass() == exceptions2[i].getClass() );
        }
//        assertTrue(DeepEquals.deepEquals(obj, res));
    }

    @Override @Test
    public void testSimpleCollections() throws Exception {
        HashMap obj = new HashMap();
        ArrayList li = new ArrayList(); li.add("zero"); li.add("second");
        obj.put("x", li);
        obj.put("y", li);
        obj.put(3,"99999");
        out.writeObject(obj);

        MBPrinter.printMessage(out.getBuffer(), System.out);
        writeTmp("simplecollections.minbin",out.getBuffer());

        final byte[] copyOfWrittenBuffer = out.getCopyOfWrittenBuffer();
        in.resetForReuseUseArray(copyOfWrittenBuffer);
        out.flush();
        HashMap res = (HashMap) in.readObject();
        assertTrue(res.get("x") == res.get("y"));
        assertTrue(DeepEquals.deepEquals(obj, res));
    }

    private void writeTmp(String name, byte[] bytez) {
        new File("/tmp/jstest").mkdirs();
        try {
            FileOutputStream fout = new FileOutputStream("/tmp/jstest/"+ name);
            fout.write(bytez);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}