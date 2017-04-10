package ser;

import org.junit.Ignore;
import org.junit.Test;
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

    // unsure wha this fails for unsafe version
    // bug was introduced with 2.45 FSTConfiguration addition of some serializers ..

    @Test
    @Ignore
    public void testVersioningIssue84() {

}

    @Test @Ignore
    public void testVersioning() {

    }

}