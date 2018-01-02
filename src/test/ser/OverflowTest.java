package ser;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.junit.Ignore;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public class OverflowTest {

    @Test @Ignore
    public void test() throws Throwable {
        FSTConfiguration fc = FSTConfiguration.createDefaultConfiguration();

        byte[] b;
        int len;
        try (FSTObjectOutput foo = new FSTObjectOutput(fc)) {
            foo.writeObject(new Outer());
            b = foo.getBuffer();
            len = foo.getWritten();
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(b, 0, len);
        FSTObjectInput foi = fc.getObjectInput(bais);
        foi.readObject();
    }

    // Up to about stream offset 16K there is this other table.  This needs to
    // be big enough to break past that and also produce enough stack frames to
    // overflow.
    private static final int OUTER_CT = 30000;

    public static class Outer implements Externalizable {
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            for (int i = 0; i < OUTER_CT; ++i) {
                out.writeObject(new Inner());
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            for (int i = 0; i < OUTER_CT; ++i) {
                in.readObject();
            }
        }
    }

    public static class Inner implements Externalizable {
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            // An array of length 0 produces a period of 7 and we want to
            // target 23 which is the hash table size as picked up in the
            // debugger
            out.writeObject(new byte[16]);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            in.readObject();
        }
    }
}
