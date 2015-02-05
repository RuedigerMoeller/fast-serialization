package ser;

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
}
