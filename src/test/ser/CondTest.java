package ser;

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

    static class CTest implements Serializable {

        int aNum;

        @Conditional
        ArrayList conditional;
        String aString;
        CTest other;

        public CTest() {
            this.aNum = 0;
            conditional = new ArrayList();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        CTest cTest = new CTest();
        cTest.other = new CTest();
        cTest.aNum = 3;
        cTest.conditional.add(new CTest());
        cTest.conditional.add(cTest.other);

        byte b[] = conf.asByteArray(cTest);

        FSTObjectInput oin = new FSTObjectInput(new ByteArrayInputStream(b), conf);
        oin.setConditionalCallback(new ConditionalCallback() {
            @Override
            public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field) {
                return ((CTest) halfDecoded).aNum != 3;
            }
        });
        CTest o = (CTest) oin.readObject();
    }
}