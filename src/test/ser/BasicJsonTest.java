package ser;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 23.05.2015.
 */
public class BasicJsonTest extends BasicFSTTest {

    @Override
    protected FSTConfiguration getTestConfiguration() {
        FSTConfiguration.isAndroid = false;
        return FSTConfiguration.createJsonConfiguration();
    }

    @Test
    public void testVersioning() {
        // versioning not supported with json
    }
}
