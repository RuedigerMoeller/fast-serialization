package ser;

import com.cedarsoftware.util.DeepEquals;
import org.nustaq.serialization.*;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * Created by ruedi on 23/05/15.
 */
public class Play implements Serializable {

    public Play() {
    }

    String str[];

    public static class SimpleClass implements Serializable {
        String name = "You";
        double aDouble = 13.3456;
        int integers[] = { 1,2,3,4,5 };
        short shorts[] = { 1,2,3,4,5 };

        Object objects[] = { 1,2,"Bla", new Point(1,2) };
    }

    public static class SampleClass implements Serializable {
        String name = "You";
        List myList = new ArrayList(Arrays.asList( 1, 2, "Hello", new Date()));
        Map<Integer,String> myMap = new HashMap<>();

        {
            myMap.put(1,"Some String");
        }
    }

    public static void main(String[] args) {
        FSTObjectRegistry.POS_MAP_SIZE = 1;
        FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();

        Object p = new SimpleClass();
        System.out.println(conf.asJsonString(p));
        byte[] bytes = conf.asByteArray(p);
        Object deser = conf.asObject(bytes);
        System.out.println(DeepEquals.deepEquals(p,deser));
    }
}
