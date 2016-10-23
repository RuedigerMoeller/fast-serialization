package ser;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 24/05/16.
 */
public class JSonUnmodifiableTest extends UmodifiableTest {

    @Override @Test
    public void testUnmodifiableMap() {
        super.testUnmodifiableMap();
    }

    @Test
    public void testUnmodifiableOrderedMap() {
        super.testUnmodifiableOrderedMap();
    }

    @Override @Test
    public void testUnmodifiableList() {
        super.testUnmodifiableList();
    }

    @Override
    protected FSTConfiguration getConfiguration() {
        return FSTConfiguration.createJsonNoRefConfiguration();
    }

    @Override @Test
    public void testUnmodifiableLinkedList() {
        super.testUnmodifiableLinkedList();
    }

    @Override @Test
    public void testUnmodifiableLinkedHashMap() {
        super.testUnmodifiableLinkedHashMap();
    }
}
