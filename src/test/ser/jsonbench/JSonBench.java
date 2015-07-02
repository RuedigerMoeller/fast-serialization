package ser.jsonbench;

import org.nustaq.serialization.FSTConfiguration;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by ruedi on 02/07/15.
 */
public class JSonBench {

    public static void javaxTest(JsonObject obj) {
        try (ByteArrayOutputStream oos = new ByteArrayOutputStream();
             JsonWriter writer = Json.createWriter(oos)) {
            writer.writeObject(obj);
            byte b[] = oos.toByteArray();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(b)) {
                JsonReader reader = Json.createReader(bais);
                JsonObject jsonObject = reader.readObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static FSTConfiguration conf = FSTConfiguration.createJsonConfiguration(false, false);
    public static void fstJson(Serializable ser) {
        byte[] bytes = conf.asByteArray(ser);
        Object o = conf.asObject(bytes);
    }


    public static class SerObj implements Serializable {
        int num;
        double dbl;
        String str;

        public SerObj() {
            num = 999;
            dbl = 1.234;
            str = "Hello JSON";
        }
    }

    public static void main( String a[] ) {

        JsonObject json = Json.createObjectBuilder().add("num",999).add("dbl",1.234).add("str", "Hello JSON").build();

        SerObj ser = new SerObj();
        conf.registerCrossPlatformClassMappingUseSimpleName(SerObj.class);

        while( true ) {
            long now = System.currentTimeMillis();
            for ( int i = 0; i < 10_000; i++ ) {
                javaxTest(json);
            }
            System.out.println("javax: "+(System.currentTimeMillis()-now));

            now = System.currentTimeMillis();
            for ( int i = 0; i < 10_000; i++ ) {
                fstJson(ser);
            }
            System.out.println("fst: "+(System.currentTimeMillis()-now));

        }

    }

}
