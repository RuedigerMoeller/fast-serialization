package ser;

import com.cedarsoftware.util.DeepEquals;
import org.junit.Test;
import org.nustaq.serialization.*;

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

    @Test
    public void test() {
        FSTConfiguration conf = FSTConfiguration.createFastBinaryConfiguration();
        Object original = new Object[] {
            new BasicFSTTest.Primitives(),
            new BasicFSTTest.Primitives(),
            new BasicFSTTest.PrimitiveArray(),
            new BasicFSTTest.AscStrings(),
            new BasicFSTTest.Strings(),
            new BasicFSTTest.Bl(),
            new boolean[]{true, false, true},
            new int[]{1, 2, 3,1, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 31, 2, 3},
            new BasicFSTTest.Primitives(),
            new BasicFSTTest.Primitives(),
            new BasicFSTTest.PrimitiveArray(),
            new BasicFSTTest.Strings(),
            new BasicFSTTest.Bl(),
            new boolean[]{true, false, true},
        };
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

        System.out.println("Default LEN:"+FSTConfiguration.createDefaultConfiguration().asByteArray(original).length);
    }

    protected Object smallBench(FSTConfiguration conf, Object original, Object deser) {
        byte[] ser = null;
        int count = 0;
        long tim = System.currentTimeMillis();
        int len[] = { 0 };
        while ( System.currentTimeMillis() - tim < 2000 ) {
            count++;
            ser = conf.asSharedByteArray(original, len);
            deser = conf.asObject(ser);
        }
        System.out.println("BIN COUNT:"+count);
        return deser;
    }
}
