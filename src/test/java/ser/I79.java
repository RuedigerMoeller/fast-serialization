package ser;

import org.junit.*;
import org.nustaq.serialization.*;
import org.nustaq.serialization.simpleapi.*;

import java.io.*;

/**
 * Created by moelrue on 15.07.2015.
 */
public class I79 {

    static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    @Test
    public void fstObj() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Serializable[] os = new Serializable[]{"mysh zzx", 1, 'c'};

        try (FSTObjectOutput fos = new FSTObjectOutput(out,conf)) {
            for (Serializable o : os) {
                fos.writeObject(o);
            }
        }

        byte[] buf = out.toByteArray();
        InputStream in = new ByteArrayInputStream(buf);

        FSTObjectInput oin = new FSTObjectInput(in,conf);
        for (Object o : os) {
            Object obj = oin.readObject();
            System.out.println(obj);
            Assert.assertEquals(o, obj);
        }
    }
}
