package ser;

import com.cedarsoftware.util.DeepEquals;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.minbin.MBPrinter;
import org.junit.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by ruedi on 29.04.14.
 */
public class BasicMinBinTest extends BasicFSTTest {
    @Override
    public void setUp() throws Exception {
        FSTConfiguration conf = FSTConfiguration.createCrossPlatformConfiguration();
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
        writeTmp("testprim.minbin",lastBinary);
    }

    @Override @Test
    public void testPrimitiveArray() throws Exception {
        super.testPrimitiveArray();
        writeTmp("testprimarray.minbin",lastBinary);
    }

    @Test
    public void testException() throws Exception { super.testException(); }

    @Test
    public void testEnums() throws Exception {
        Basics obj = new Basics(123);
        out.writeObject(obj);

        MBPrinter.printMessage(out.getBuffer(), System.out);

        writeTmp("enums.minbin",out.getBuffer());

        in.resetForReuseUseArray(out.getCopyOfWrittenBuffer());
        out.flush();
        Object res = in.readObject();
        assertTrue(DeepEquals.deepEquals(obj,res));
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