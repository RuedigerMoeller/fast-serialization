package de.ruedigermoeller.serialization.testclasses;

import de.ruedigermoeller.serialization.testclasses.docusample.FSTTestApp;
import de.ruedigermoeller.serialization.testclasses.jdkcompatibility.*;
import de.ruedigermoeller.serialization.testclasses.libtests.*;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 12.11.12
 * Time: 02:43
 * To change this template use File | Settings | File Templates.
 */
public class TestRunner {

    SerTest speedFST = new FSTTest("FST (preferSpeed=true, Unsafe enabled)",true,true);
    SerTest defFST = new FSTTest("FST (Unsafe enabled)",true,false);
    SerTest defFSTNoUns = new FSTTest("FST ",false,false);
    SerTest defser = new JavaSerTest("Java built in");

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
        SerTest tests[] = { defFSTNoUns};
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            test.run(toSer);
        }
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            test.dumpRes();
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


    // full test has been move to a separate repository, just run some special checking ..
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

        SerTest.WarmUP = 200; SerTest.Run = 3000;
        runner.runAll(new ExternalizableTest());
        runner.charter.closeDoc();
        FSTTestApp.main(new String[0]);
    }
}
