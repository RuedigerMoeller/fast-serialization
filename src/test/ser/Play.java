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
        String a = "bla bla bla bla bla bla bla bla bla bla bla bla bla ";
        Object b = a;
    }

    public static class EmptyClass implements Serializable {
        int x = 123123;
    }

    public static void main(String[] args) {
        FSTObjectRegistry.POS_MAP_SIZE = 1;
        FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();

        conf.registerCrossPlatformClassMappingUseSimpleName(
                SampleClass.class,
                Object[].class,
                Object[][].class,
                int[][].class,
                int[][][].class
        );


//        Object p = new SimpleClass();
        Object p = new SampleClass();
        System.out.println(conf.asJsonString(p));
        byte[] bytes = conf.asByteArray(p);
        Object deser = conf.asObject(bytes);
        System.out.println(DeepEquals.deepEquals(p,deser));
//        while( true )
//            sb(conf);
    }

    protected static void sb(FSTConfiguration conf) {
        long tim = System.currentTimeMillis();
        EmptyClass ec = new EmptyClass();
        for ( int i = 0; i < 1_000_000; i++ ) {
            byte[] bytes = conf.asByteArray(ec);
            Object deser = conf.asObject(bytes);
            if ( deser == null ) {
                System.out.println("POK");
            }
        }
        long dur = System.currentTimeMillis()-tim;
        System.out.println("dur:"+dur);
    }
}
