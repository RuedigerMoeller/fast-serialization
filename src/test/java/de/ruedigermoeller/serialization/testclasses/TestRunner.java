package de.ruedigermoeller.serialization.testclasses;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.testclasses.basicstuff.*;
import de.ruedigermoeller.serialization.testclasses.docusample.FSTTestApp;
import de.ruedigermoeller.serialization.testclasses.enterprise.*;
import de.ruedigermoeller.serialization.testclasses.jdkcompatibility.*;
import de.ruedigermoeller.serialization.testclasses.libtests.*;
import de.ruedigermoeller.serialization.testclasses.remoting.ShortRemoteCall;
import de.ruedigermoeller.serialization.util.FSTUtil;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 12.11.12
 * Time: 02:43
 * To change this template use File | Settings | File Templates.
 */
public class TestRunner {

    static {
        System.setProperty("fst.unsafe","true");
    }

    SerTest kryotest = new KryoTest("Kryo 2.23");
    SerTest kryoUnsTest = new KryoUnsafeTest("Kryo 2.23 UnsafeIn/Output");
    SerTest speedFST = new FSTTest("FST (preferSpeed=true, Unsafe enabled)",true,true);
    SerTest defFST = new FSTTest("FST (Unsafe enabled)",true,false);
    SerTest defFSTNoUns = new FSTTest("FST ",false,false);
    SerTest defser = new JavaSerTest("Java built in");
//    SerTest gridgain = new GridGainTest("GridGain 4.5"); cannot redistribute ..

    Class testClass;
    public SerTest[] runAll( Object toSer ) throws IOException, InterruptedException {
        testClass = toSer.getClass();
        if ( toSer instanceof Swing) {
            ((Swing) toSer).showInFrame("Original");
            ((Swing) toSer).init();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        System.out.println();
        System.out.println();
        System.out.println("************** Running all with "+toSer.getClass().getName()+" **********************************");
        SerTest tests[] = { speedFST, defFST, kryoUnsTest, defFSTNoUns, kryotest, defser };
//        SerTest tests[] = { speedFST, kryoUnsTest, defFST, kryotest};
//        SerTest tests[] = { speedFST, kryotest, kryoUnsTest };
//        SerTest tests[] = { speedFST, kryoUnsTest };
//        SerTest tests[] = { defFST };
//        SerTest tests[] = { defFST, defFSTNoUns, kryotest };
//        SerTest tests[] = { defFSTNoUns, defFST};
//        SerTest tests[] = { kryotest };
        if ( toSer instanceof BigObject ) {
            SerTest.Run/=100;
            SerTest.WarmUP/=100;
        }
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            test.run(toSer);
        }
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            test.dumpRes();
        }
        if ( toSer instanceof BigObject ) {
            SerTest.Run*=100;
            SerTest.WarmUP*=100;
        }

        charter.heading("Test Class: "+testClass.getSimpleName());
        Object testIns = null;
        try {
            if ( testClass.isArray() ) {
                testIns = testClass.getComponentType().newInstance();
            } else
                testIns = testClass.newInstance();
            if ( testIns instanceof HasDescription ) {
                charter.text(((HasDescription) testIns).getDescription());
                charter.text("");
            }
        } catch (Exception e) {
        }

        charter.openChart("Read Time (micros)");
        int fac = 3;
        boolean cont = true;
        while( cont ) {
            cont = false;
            for (int i = 0; i < tests.length; i++) {
                SerTest test = tests[i];
                int val = (int)(test.timRead*1000/SerTest.Run);
                if ( val/fac > 130 ) {
                    fac++;
                    cont = true;
                }
            }
        }
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            charter.chartBar(test.title, (int)(test.timRead*1000/SerTest.Run), fac, test.getColor());
        }
        charter.closeChart();

        charter.openChart("Write Time (micros)");
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            charter.chartBar(test.title, (int)(test.timWrite*1000/SerTest.Run), fac, test.getColor());
        }
        charter.closeChart();

        charter.openChart("Size (byte)");
        fac = 500;
        cont = true;
        while( cont ) {
            cont = false;
            for (int i = 0; i < tests.length; i++) {
                SerTest test = tests[i];
                int val = test.bout.size();
                if ( val/fac > 70 ) {
                    fac++;
                    cont = true;
                }
            }
        }
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            charter.chartBar(test.title, test.bout.size(), fac, test.getColor());
        }
        charter.closeChart();

        return tests;
    }
    HtmlCharter charter = new HtmlCharter("./result.html");


    public static void main( String[] arg ) throws Exception {
        try {
            ReadResolve.main(null);
            SpecialsTest.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TestRunner runner = new TestRunner();
        runner.charter.setAsc(new AsciiCharter("./result.txt"));


        runner.charter.openDoc();
        runner.charter.text("<i>intel i7 3770K 3,4 ghz, 4 core, 8 threads</i>");
        runner.charter.text("<i>"+System.getProperty("java.runtime.version")+","+System.getProperty("java.vm.name")+","+System.getProperty("os.name")+"</i>");

//        SerTest.WarmUP = 20000; SerTest.Run = SerTest.WarmUP*1+1;
        SerTest.WarmUP = 200; SerTest.Run = 3000;
        runner.runAll(FrequentPrimitives.getArray(200));
        runner.runAll(FrequentPrimitivesExternalizable.getArray(200));
        runner.runAll(new FrequentCollections());
        runner.runAll(new LargeNativeArrays());
        runner.runAll(new StringPerformance(0));
        runner.runAll(new Primitives(0).createPrimArray());
        runner.runAll(new PrimitiveArrays().createPrimArray());
        runner.runAll(new CommonCollections());
        runner.runAll(Trader.generateTrader(101, true));
        runner.runAll(ManyClasses.getArray() );
        runner.runAll(new ExternalizableTest());
        runner.runAll(new BigObject("dummy"));
        runner.runAll(HeavyNesting.createNestedObject(1000));
        runner.charter.closeDoc();
        FSTTestApp.main(new String[0]);
    }
}
