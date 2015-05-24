package ser;

import com.cedarsoftware.util.DeepEquals;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.FSTObjectRegistry;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by ruedi on 23/05/15.
 */
public class Play implements Serializable {

    public Play() {
    }

    String str[];

    static class T implements Serializable {

        Object mix1 =
        new Object[] {
            null,
            new int[][]{ { 348535,-34534345,348 }, null, {34544,677576777,-347563453} }, new byte[]{ -13,34, 127,3,23,5,0,11 },
            null,
            new Object[] {-1,(char)33333,(short)-12312,(int)123313,(long)293847293847l,null,(double)10.1233,(float)2345.234}
        };
        public T() {}

        public T(int dummy) {
        }
    }

    public static void main(String[] args) {
        FSTObjectRegistry.POS_MAP_SIZE = 1;
        FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();
//        Object p = new T(1);
        Object p = new BigDecimal(123);
//        Object p = new Object[] {"A", new BasicFSTTest.SubClassedAList().$("A").$("B").$("C"), "Ensure stream not corrupted" };
        conf.prettyPrintJson(p);
        byte[] bytes = conf.asByteArray(p);
        Object deser = conf.asObject(bytes);
        System.out.println(DeepEquals.deepEquals(p,deser));
    }
}
