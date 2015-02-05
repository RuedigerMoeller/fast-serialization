package ser.androidbitset;

import com.cedarsoftware.util.DeepEquals;
import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

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


        AndroidBItSet androidOriginal = new AndroidBItSet(777);
        androidOriginal.set(666, 700, true);
        byte androidSerialized[] = androidConf.asByteArray(androidOriginal);

        JDKBitSet read = (JDKBitSet) jdkConf.asObject(androidSerialized);
        byte reverse[] = jdkConf.asByteArray(read);
        
        AndroidBItSet androidBS = (AndroidBItSet) androidConf.asObject(reverse);
        
        assertTrue(DeepEquals.deepEquals(androidBS, androidOriginal));
    }
}
