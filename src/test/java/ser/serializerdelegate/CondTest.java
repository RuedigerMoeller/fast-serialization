package ser.serializerdelegate;

import org.nustaq.serialization.*;
import org.nustaq.serialization.FSTObjectInput.*;
import org.nustaq.serialization.annotations.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Created by moelrue on 07.07.2015.
 */
public class CondTest {

    static class Unregisterd implements Serializable {
        int x = 3;
    }

    static class CTest implements Serializable {

        int aNum;

        ArrayList conditional1;
        @Conditional ArrayList conditional0;
        String aString;
        CTest other;
        @Conditional Object unregistered;

        public CTest() {
            this.aNum = 0;
            conditional1 = new ArrayList();
            conditional0 = new ArrayList();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

        conf.registerClass(CTest.class,Unregisterd.class);

        CTest cTest = new CTest();
        cTest.other = new CTest();
        cTest.aNum = 3;
        cTest.unregistered = new Unregisterd();
        cTest.conditional0.add(new CTest());
        cTest.conditional0.add(cTest.unregistered);
        cTest.conditional0.add(cTest.other);
        cTest.conditional1.add(new Unregisterd());

        byte b[] = conf.asByteArray(cTest);

        FSTObjectInput oin = new FSTObjectInput(new ByteArrayInputStream(b), conf);
        oin.setConditionalCallback(new ConditionalCallback() {
            @Override
            public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field) {
                return ((CTest) halfDecoded).aNum == 3;
            }
        });
        CTest o = (CTest) oin.readObject();
        System.out.println();
    }
}