package ser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectInputNoShared;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.FSTObjectOutputNoShared;

/**
 * Created by pmarx on 05.12.2016.
 */
public class GitHub159 {

    public static class TransientField implements Serializable {
        private transient String transientString;
        private String string;

        public TransientField(String string) {
            this.string = string;
            this.transientString = string;
        }

        @Override
        public String toString() {
            return transientString;
        }

        private void writeObject(ObjectOutputStream os) throws IOException {
            os.defaultWriteObject();
        }

        private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
            is.defaultReadObject();
            this.transientString = string;
        }
    }

    public static void main(String[] args) throws Exception {
        encodeDecode(URI.create("https://github.com/RuedigerMoeller/fast-serialization/issues/159"));
        encodeDecode(URI.create("https://github.com/RuedigerMoeller/fast-serialization/issues/159").toURL());
        encodeDecode(new TransientField(UUID.randomUUID().toString()));
    }

    private static void encodeDecode(Object object) throws Exception {
        try {
            FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
            conf.setShareReferences(false);
            FSTObjectInput input = new FSTObjectInputNoShared(conf);
            FSTObjectOutput output = new FSTObjectOutputNoShared(conf);

            output.writeObject(object);
            byte[] bytes = output.getCopyOfWrittenBuffer();

            input.resetForReuseUseArray(bytes);
            Object read = input.readObject();

            System.out.println(read);
            System.out.println(object.equals(read));
        } catch (Throwable e) {
            System.err.println(e.getClass() + ": " + e.getMessage());
        }
    }
}
