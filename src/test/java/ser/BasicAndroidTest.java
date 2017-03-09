package ser;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 12.12.14.
 */
public class BasicAndroidTest extends BasicFSTTest {

    @Override
    protected FSTConfiguration getTestConfiguration() {
        FSTConfiguration.isAndroid = true;
        return FSTConfiguration.createAndroidDefaultConfiguration();
    }

    @Override @Test
    public void testSelfRef() {
        super.testSelfRef();
    }

    @Override @Test
    public void testSelfRefArr() {
        super.testSelfRefArr();
    }
}
