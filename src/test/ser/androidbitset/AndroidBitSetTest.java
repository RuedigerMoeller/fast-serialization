package ser.androidbitset;

import com.cedarsoftware.util.DeepEquals;
import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import java.util.Objects;

import static org.junit.Assert.assertTrue;

/**
 * Created by ruedi on 05/02/15.
 */
public class AndroidBitSetTest {

    @Test
    public void test() {
        FSTConfiguration androidConf = FSTConfiguration.createAndroidDefaultConfiguration();
        androidConf.registerClass(AndroidBItSet.class);

        // some trickery to read bitset from androidbitset
        FSTConfiguration jdkConf = FSTConfiguration.createAndroidDefaultConfiguration();
        jdkConf.registerClass(JDKBitSet.class);


        final AndroidBItSet abs = new AndroidBItSet(777);
        Object androidOriginal = new Object[] {abs,"dummy"};
        abs.set(666, 700, true);
        byte androidSerialized[] = androidConf.asByteArray( androidOriginal );

        Object read = jdkConf.asObject(androidSerialized);
        byte reverse[] = jdkConf.asByteArray(read);
        
        Object androidBS = androidConf.asObject(reverse);
        
        assertTrue(DeepEquals.deepEquals(androidBS, androidOriginal));
    }
}
