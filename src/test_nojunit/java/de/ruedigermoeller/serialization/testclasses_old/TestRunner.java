package de.ruedigermoeller.serialization.testclasses_old;

import de.ruedigermoeller.heapofftest.BenchStructs;
import de.ruedigermoeller.heapofftest.OffHeapMapTest;
import de.ruedigermoeller.heapofftest.OffHeapTest;
import de.ruedigermoeller.heapofftest.structs.StructTest;
import de.ruedigermoeller.serialization.testclasses.libtests.*;
import de.ruedigermoeller.serialization.testclasses_old.basicstuff.BugReport2;
import de.ruedigermoeller.serialization.testclasses_old.docusample.FSTTestApp;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 12.11.12
 * Time: 02:43
 * To change this template use File | Settings | File Templates.
 */
public class TestRunner {

    // full test has been moved to a separate repository, just run some special checking here .. (pls no why-not-junit mails) )
    public static void main( String[] arg ) throws Exception {
        try {
            de.ruedigermoeller.serialization.testclasses_old.jdkcompatibility.ReadResolve.main(null);
            de.ruedigermoeller.serialization.testclasses_old.jdkcompatibility.SpecialsTest.main(null);
            BugReport2.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StructTest.main(arg);
        BenchStructs.main(arg);
        OffHeapTest.main(arg);
        FSTTestApp.main(new String[0]);
    }
}
