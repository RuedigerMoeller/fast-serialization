package offheap;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.simpleapi.FSTCoder;
import org.nustaq.serialization.simpleapi.OnHeapCoder;

/**
 * Created by ruedi on 09.11.14.
 */
public class BinaryStringOffHeapTest extends StringOffHeapTest {

    @Override
    protected FSTCoder createCoder() {
        return new OnHeapCoder();
    }
}
