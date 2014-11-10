package offheap;

import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 09.11.14.
 */
public class BinaryStringOffHeapTest extends StringOffHeapTest {

    @Override
    protected FSTConfiguration createConfiguration() {
        return FSTConfiguration.createFastBinaryConfiguration();
    }
}
