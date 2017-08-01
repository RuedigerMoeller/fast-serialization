package ser;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BasicReuseTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testStreamReuse() throws Exception {
        FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();

        String expected = "Hello, World!";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FSTObjectOutput fstObjectOutput = configuration.getObjectOutput(baos);
        try {
            fstObjectOutput.writeObject(expected);
        } finally {
            fstObjectOutput.flush();
        }
        byte[] serializedData = baos.toByteArray();
        FSTObjectInput input = configuration.getObjectInput(new ByteArrayInputStream(serializedData));
        Object read = input.readObject();
        Assert.assertEquals(expected, read);

        FSTObjectInput secondInput = configuration.getObjectInput(new ByteArrayInputStream(new byte[0]));
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Only read");
        secondInput.readObject();
    }
}
