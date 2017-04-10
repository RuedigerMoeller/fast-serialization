package ser;

import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;
import java.io.IOException;
import java.time.Duration;

/**
 * Created by ruedi on 10.04.17.
 */
public class I177 {

    @Test
    public void test() throws IOException {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        Object[] x = { Duration.ofDays(1), Duration.ofDays(2) };
        Object[] res = (Object[]) conf.asObject(conf.asByteArray(x));
        Assert.assertTrue( x[0].equals(res[0]) && x[1].equals(res[1]));
    }
}
