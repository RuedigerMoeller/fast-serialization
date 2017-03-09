package ser;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;

import java.util.Arrays;

/**
 * Created by moelrue on 27.10.2015.
 */
public class Issue91 {

    public static void main(String[] args) throws Exception {
        FSTConfiguration config = FSTConfiguration.createDefaultConfiguration();
        FSTObjectOutput out = config.getObjectOutput();
        out.writeObject("foobar");
        byte[] savedBuffer = out.getCopyOfWrittenBuffer();
        byte[] reallySavedBuffer = Arrays.copyOf(savedBuffer, savedBuffer.length);

        System.out.println(Arrays.equals(savedBuffer, reallySavedBuffer));

        out.resetForReUse();
        out.writeObject("blah");
        byte[] secondBuffer = out.getCopyOfWrittenBuffer();

        System.out.println(Arrays.equals(savedBuffer, reallySavedBuffer));

        config.getObjectInput(savedBuffer);
        config.getObjectInputCopyFrom(secondBuffer, 0, secondBuffer.length);
        System.out.println(Arrays.equals(savedBuffer, reallySavedBuffer));
    }
}
