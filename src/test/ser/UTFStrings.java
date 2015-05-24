package ser;

import com.cedarsoftware.util.DeepEquals;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by ruedi on 23/05/15.
 */
public class UTFStrings implements Serializable {

    public UTFStrings() {
    }

    String str[];

    static class T implements Serializable {
        Dimension dim[][][] = new Dimension[][][] {{{new Dimension(11,10)},{new Dimension(9,10),new Dimension(1666661,11)}}};
        public T() {}

        public T(int dummy) {
        }
    }

    public static void main(String[] args) {
        FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();
        T p = new T(1);
        conf.prettyPrintJson(p);
        byte[] bytes = conf.asByteArray(p);
        Object deser = conf.asObject(bytes);
        System.out.println(DeepEquals.deepEquals(p,deser));
    }
}
