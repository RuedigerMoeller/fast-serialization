package ser.androidbitset;

import com.cedarsoftware.util.DeepEquals;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import java.util.Objects;

import static org.junit.Assert.assertTrue;

/**
 * Created by ruedi on 05/02/15.
 */
public class AndroidBitSetTest {

    @Test
    public void testBitSet() {
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


    @Test @Ignore
    // in case number and names of instance fields of reader/writer are different,
    // this fails as code below implicitely assumes, fields of writer == fields of reader
    // unfortunately one can use defaultWriteObject at writer side but use getFields at reader side
    // in readObject(). if then fields differ, code below reads BS and fails.
    // Its impossible to fix that except by always using putField + getField for
    // JDK compatibility classes, however this will waste lots of performance. As
    // it would be necessary to *always* write full metainformation (a map of fieldName => value pairs).
    // Strategy will be to rely on custom serializers for now, as reading/writing a full hashmap for
    // each defaultWriteObject/readObject is overkill. Additionally as JDK assumes defaultWriteObject is
    // binary compatible to writeObject (which is trueish for fast-ser), the need to write hashmaps would
    // transition/leak also to default serailization.
    // see #53.
    public void testBigInt() {
        FSTConfiguration androidConf = FSTConfiguration.createAndroidDefaultConfiguration();
        androidConf.registerClass(AndroidBigInt.class);

        // some trickery to read bitset from androidbitset
        FSTConfiguration jdkConf = FSTConfiguration.createAndroidDefaultConfiguration();
        jdkConf.registerClass(JDKBigInt.class);


        final AndroidBigInt abs = new AndroidBigInt(-4, new byte[] { 1,2,3,4,5,6,7 });
        Object androidOriginal = new Object[] {abs,"dummy"};
        byte androidSerialized[] = androidConf.asByteArray( androidOriginal );

        Object read = jdkConf.asObject(androidSerialized);
        byte reverse[] = jdkConf.asByteArray(read);

        Object androidBS = androidConf.asObject(reverse);

        assertTrue(DeepEquals.deepEquals(androidBS, androidOriginal));
    }

}
