package ser;

import com.cedarsoftware.util.DeepEquals;
import org.junit.Ignore;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import static org.junit.Assert.assertTrue;

/**
 * Created by ruedi on 23.05.2015.
 */
public class BasicJsonTest extends BasicFSTTest {

    @Override
    protected FSTConfiguration getTestConfiguration() {
        FSTConfiguration.isAndroid = false;
        return FSTConfiguration.createJsonConfiguration(false,true);
    }

    @Test
    public void testVersioning() {
        // versioning not supported with json
    }

    @Ignore
    public void testVersioningIssue84() {
    }

    @Test @Override
    public void testBigNums() throws Exception {
        BigNums obj = new BigNums();
        out.writeObject(obj);
        in.resetForReuseUseArray(out.getCopyOfWrittenBuffer());
        out.flush();
        BigNums res = (BigNums) in.readObject();
        //assertTrue(DeepEquals.deepEquals(obj, res)); Json inherently cannot differ e.g. Long(1) vs Integer(1).
        assertTrue(res.ugly[2][0]);
        assertTrue(res._aDouble2a[0] == -88.0);
        assertTrue(res._aChar2a[1] == Character.MAX_VALUE);
    }

    @Test @Override // again BigNum type failures unavoidable for Json
    public void testPrimitiveArray() throws Exception {
        PrimitiveArray obj = new PrimitiveArray();
        out.writeObject(obj);
        in.resetForReuseUseArray(lastBinary = out.getCopyOfWrittenBuffer());
        out.flush();
        PrimitiveArray res = (PrimitiveArray) in.readObject();
        assertTrue(res.aLong0 == res.aRef);
        assertTrue(res.aRef1 == res.mix[1]);

        assertTrue(DeepEquals.deepEquals(obj.aBoolean,res.aBoolean));
        assertTrue(DeepEquals.deepEquals(obj.aByte,res.aByte));
        assertTrue(DeepEquals.deepEquals(obj.aShort0,res.aShort0));
        assertTrue(DeepEquals.deepEquals(obj.aChar0,res.aChar0));
        assertTrue(DeepEquals.deepEquals(obj.aInt0,res.aInt0));
        assertTrue(DeepEquals.deepEquals(obj.aInt1,res.aInt1));
        assertTrue(DeepEquals.deepEquals(obj.aLong0,res.aLong0));
        assertTrue(DeepEquals.deepEquals(obj.aFloat0,res.aFloat0));
        assertTrue(DeepEquals.deepEquals(obj.aDouble,res.aDouble));
        assertTrue(DeepEquals.deepEquals(obj.aRef,res.aRef));
        assertTrue(DeepEquals.deepEquals(obj._aBoolean,res._aBoolean));
        assertTrue(DeepEquals.deepEquals(obj._aByte,res._aByte));
        assertTrue(DeepEquals.deepEquals(obj._aShort0,res._aShort0));
        assertTrue(DeepEquals.deepEquals(obj._aChar0,res._aChar0));
        assertTrue(DeepEquals.deepEquals(obj._aInt0,res._aInt0));
        assertTrue(DeepEquals.deepEquals(obj._aInt1,res._aInt1));
        assertTrue(DeepEquals.deepEquals(obj._aLong0,res._aLong0));
        assertTrue(DeepEquals.deepEquals(obj._aFloat0,res._aFloat0));
        assertTrue(DeepEquals.deepEquals(obj._aDouble,res._aDouble));
    }
}
