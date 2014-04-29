package ser;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

/**
 * Created by ruedi on 29.04.14.
 */
public class BasicMinBinTest extends BasicFSTTest {
    @Override
    public void setUp() throws Exception {
        FSTConfiguration conf = FSTConfiguration.createCrossPlatformConfiguration();
        out = new FSTObjectOutput(conf);
        in = new FSTObjectInput(conf);
    }
}
