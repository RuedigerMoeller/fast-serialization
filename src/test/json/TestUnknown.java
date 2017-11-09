package json;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by ruedi on 09.11.17.
 */
public class TestUnknown {

    @Test
    public void testU() throws IOException {
        FSTConfiguration jsonConfiguration = FSTConfiguration.createJsonConfiguration(false, false);
        byte[] bytes = Files.readAllBytes(new File("./src/test/json/testjson.json").toPath());
        jsonConfiguration.asObject(bytes);
    }
}
