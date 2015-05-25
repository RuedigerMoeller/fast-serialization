package ser;

import com.cedarsoftware.util.DeepEquals;
import org.nustaq.serialization.*;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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
        int anInt;
        int integers[] = { 1,2,3,4,5 };
        short shorts[] = { 1,2,3,4,5 };

//        Object objects[] = { 1,2,"Bla", new Point(1,2) };
    }

    public static class SampleClass implements Serializable {
        List myList = new ArrayList();
        Map<Integer,String> myMap = new HashMap<>();
        {
            myMap.put(1,"Some String");
            myList.add(1); myList.add(2); myList.add("Hello");// myList.add(new Date());
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        FSTObjectRegistry.POS_MAP_SIZE = 1;
        FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();
        conf.registerCrossPlatformClassMappingUseSimpleName(SimpleClass.class);

        Object p = new SampleClass();

        byte[] bytes = conf.asByteArray(p);
        System.out.println(new String(bytes,"UTF-8"));
        Object deser = conf.asObject(bytes);

        System.out.println(DeepEquals.deepEquals(p,deser));
    }
}
