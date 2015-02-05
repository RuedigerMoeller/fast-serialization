package ser;

import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 12.12.14.
 */
public class BasicBinaryUnsafeTest extends BasicFSTTest {

    @Override
    protected FSTConfiguration getTestConfiguration() {
        FSTConfiguration.isAndroid = false;
        return FSTConfiguration.createFastBinaryConfiguration();
    }

}
