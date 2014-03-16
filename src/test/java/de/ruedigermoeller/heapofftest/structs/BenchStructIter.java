package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.FSTTypedStructAllocator;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;
import de.ruedigermoeller.heapofftest.gcbenchmarks.BasicGCBench;
import de.ruedigermoeller.serialization.testclasses.HtmlCharter;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 10.07.13
 * Time: 01:10
 * To change this template use File | Settings | File Templates.
 */
public class BenchStructIter {

    public static final int SIZE = 500000;

    static void fillInstruments(StructArray<TestInstrument> arr) {
        int size = arr.size();
        for ( int i = 0; i < size; i++ ) {
            TestInstrument testInstrument = arr.get(i);
            if ( testInstrument == null ) { // if on heap, no prefilled array
                testInstrument = TestInstrument.createInstrumentTemplateOnHeap();
                arr.set(i,testInstrument);
            }
            testInstrument.setInstrId(i);
            testInstrument.getMnemonic().setString("I"+i);
            testInstrument.getMarket().getMnemonic().setString((i % 2) == 0 ? "XEUR" : "XETR");
            if (testInstrument.isOffHeap()) {
                testInstrument.setNumLegs(i%4);
            }
            for ( int j=0; j < i%4; j++ ) {
                try {
                    if (arr.isOffHeap()) {
                        TestInstrumentLeg leg = testInstrument.legs(j);
                        leg.setLegQty(i%4);
                        leg.getInstrument().getMnemonic().setString("I"+j);
                        testInstrument = arr.get(i);
                    } else {
                        TestInstrumentLeg leg = new TestInstrumentLeg();
                        leg.setLegQty(i%4);
                        leg.getInstrument().getMnemonic().setString("I"+j);
                        testInstrument.addLeg(leg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static long test(Runnable r) {
//        r.run();
        long tim = System.currentTimeMillis();
        r.run();
        return (System.currentTimeMillis()-tim);
    }

    final static StructArray<TestInstrument> offheap[] = new StructArray[] { null };
    final static StructArray<TestInstrument> onheap[] = new StructArray[] { null };

    public static void main( String arg[] ) {

        final HtmlCharter charter = new HtmlCharter("./structiter.html");
        charter.openDoc();
        charter.text("<i>intel i7 3770K 3,5 ghz, 4 core, 8 threads</i>");
        charter.text("<i>" + System.getProperty("java.runtime.version") + "," + System.getProperty("java.vm.name") + "," + System.getProperty("os.name") + "</i>");
//
        final FSTTypedStructAllocator<TestInstrument> alloc = new FSTTypedStructAllocator<TestInstrument>(TestInstrument.createInstrumentTemplate(),SIZE);


        charter.heading("Instantiation Time (Default Collector)");
        charter.text("Duration to allocate and initialize an array[" + SIZE + "] of complex Objects (TestInstrument containing a list of embedded TestInstrumentLeg Objects).");
        charter.text("Note: Instantiation time differs depending on GC-Algorithm, difference in Full GC stays across DefaultGC, CMS and G1. " +
                "G1 and Default GC are significantly faster in on-heap allocation. <br>");
        charter.openChart("time(ms) smaller is better");

        Runnable onHeapInst = new Runnable() {
            public void run() {
                StructArray<TestInstrument> instruments = new StructArray<TestInstrument>(SIZE, new TestInstrument());
                fillInstruments(instruments);
                System.out.println("allocated " + instruments.size() + " instruments");
                onheap[0] = instruments;
            }
        };
        Runnable offHeapInst = new Runnable() {
            public void run() {
                StructArray<TestInstrument> instruments = alloc.newArray(SIZE);
                fillInstruments(instruments);
                System.out.println("allocated " + instruments.size() + " instruments using " + instruments.getByteSize() / 1000 / 1000 + " MB clid:" + instruments.getStructElemClassId());
                offheap[0] = instruments;
            }
        };
        onHeapInst.run();
        offHeapInst.run();
        onheap[0] = null; offheap[0] = null;
        long initialFull = BasicGCBench.benchFullGC();


        long oninstTime = test(onHeapInst);
        System.out.println("duration on heap instantiation "+oninstTime);
        charter.chartBar("On Heap (excl. GC cost)",(int)oninstTime,20,"#7070a0");
//        BasicGCBench.benchFullGC();

        long fullGCOn = BasicGCBench.benchFullGC();
        onheap[0] = null;

        long instTime = test(offHeapInst);
        charter.chartBar("Structs (*)",(int)instTime,20,"#70a070");
        System.out.println("duration Structs instantiation "+instTime);

        long fullGCOff = BasicGCBench.benchFullGC();

        charter.closeChart();

        charter.openChart("Time of Full GC (ms)");
        charter.chartBar("On heap", (int) (fullGCOn-initialFull), 50, "#7070a0");
        charter.chartBar("Structs", (int) Math.max(1,(fullGCOff-initialFull)),50,"#7070a0");
        charter.closeChart();

        long oninstTime1 = test( onHeapInst );
        System.out.println("duration on heap instantiation #3 "+oninstTime1);

        FSTTypedStructAllocator<LargeIntArray> arrAlloc = new FSTTypedStructAllocator<LargeIntArray>(new LargeIntArray());
        final int iterMul = 20;
        final LargeIntArray offIntArr = arrAlloc.newStruct();
        final LargeIntArray onIntArr = new LargeIntArray();

//        BasicGCBench.benchFullGC();

        for ( int xx = 0; xx < 5; xx++ ) {
            System.out.println();

            if ( xx == 4 ) {
                charter.heading("Iterate primitive Arrays");
                charter.text("benchmark performance of calculating the sum of all elements of an embedded int array.<br>");
                charter.openChart("");
            }
            long onInt = test( new Runnable() {
                public void run() {
                    int sum = 0;
                    for ( int j = 0; j < 100; j++ ) {
                        sum+=onIntArr.calcSumStraight();
                    }
                    System.out.println("sum onheap "+sum);
                }
            });
            System.out.println("duration onheap int iteration "+onInt);
            if  (xx==4) {
                charter.chartBar("on heap", (int) onInt,10,"#7070a0");
            }

            long offInt = test( new Runnable() {
                public void run() {
                    int sum = 0;
                    for ( int j = 0; j < 100; j++ ) {
                        sum+=offIntArr.calcSumStraight();
                    }
                    System.out.println("sum ofheap "+sum);
                }
            });
            System.out.println("duration structs int iter "+offInt);
            if  (xx==4) {
                charter.chartBar("structs identical code", (int) offInt,10,"#70a070");
            }

            long offIntP = test( new Runnable() {
                public void run() {
                    int sum = 0;
                    for ( int j = 0; j < 100; j++ ) {
                        sum+=offIntArr.calcSumPointered();
                    }
                    System.out.println("sum ofheap pointered "+sum);
                }
            });
            System.out.println("duration structs int pointered "+offIntP);
            if  (xx==4) {
                charter.chartBar("structs using pointers instead of get(index)", (int) offIntP,10,"#70a070");
                charter.closeChart();
            }

            if ( xx == 4 ) {
                charter.heading("1st level member access");
                charter.text("iterate structure array and access an integer member variable of each.<br>");
                charter.openChart("");
            }
            long onCalcLegs = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = onheap[0];
                    int sum = 0;
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        for ( int i = 0; i < instruments.size(); i++ ) {
                            sum+=instruments.get(i).getNumLegs();
                        }
                    }
                    System.out.println("sum onheap "+sum);
                }
            });
            System.out.println("duration naive on heap iteration int access "+onCalcLegs);
            if  (xx==4) {
                charter.chartBar("on heap get(index).getIntVar()", (int) onCalcLegs,20,"#7070a0");
            }

            long offCalcLegs = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        for ( int i = 0; i < instruments.size(); i++ ) {
                            sum+=instruments.get(i).getNumLegs();
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration naive structs iteration int access "+offCalcLegs);
            if  (xx==4) {
                charter.chartBar("structs get(index).getIntVar()", (int) offCalcLegs,20,"#70a070");
            }

            long offCalcLegsP = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        TestInstrument p = instruments.createPointer(0);
                        int siz = p.getElementInArraySize();
                        for ( int i = 0; i < instruments.size(); i++ ) {
                            sum+=p.getNumLegs();
                            p.next(siz);
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration naive structs iteration int access "+offCalcLegsP);
            if  (xx==4) {
                charter.chartBar("structs pointer.getIntVar()", (int) offCalcLegsP,20,"#70a070");
                charter.closeChart();

                charter.heading("3rd level embedded substructure access");
                charter.text("iterate structure array, access each element of an embedded substructure's int array.<br>");
                charter.openChart("");
            }

            long onCalcQty = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = onheap[0];
                    int sum = 0;
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        final int size = instruments.size();
                        for ( int i = 0; i < size; i++ ) {
                            sum+=instruments.get(i).getAccumulatedQty();
                        }
                    }
                    System.out.println("sum onheap "+sum);
                }
            });
            System.out.println("duration on heap iteration calcQty "+onCalcQty);
            if  (xx==4) {
                charter.chartBar("on heap version", (int) onCalcQty,100,"#7070a0");
            }

            long offCalcQty = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        for ( int i = 0; i < instruments.size(); i++ ) {
                            sum+=instruments.get(i).getAccumulatedQty();
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration naive structs iteration calcQty "+offCalcQty);
            if  (xx==4) {
                charter.chartBar("structs identical code to onheap", (int) offCalcQty,100,"#70a070");
            }

//            long offCalcQty1 = test( new Runnable() {
//                public void run() {
//                    StructArray<TestInstrument> instruments = offheap[0];
//                    int sum = 0;
//                    for ( int j = 0; j < iterMul; j++ ) {
//                        sum = 0;
//                        for (StructArray<TestInstrument>.StructArrIterator<TestInstrument> iterator = instruments.iterator(); iterator.hasNext(); ) {
//                            TestInstrument next = iterator.next();
//                            sum+=next.getAccumulatedQty();
//                        }
//                    }
//                    System.out.println("sum offheap "+sum);
//                }
//            });
//            System.out.println("duration iterator off heap iteration calcQty "+offCalcQty1);

            long offCalcQty2 = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.size();
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        TestInstrument p = instruments.createPointer(0);
                        for (int i=0; i < count; i++ ) {
                            sum+=p.getAccumulatedQtyOff();
                            p.next(siz);
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration opt pointer structs iteration calcQty "+offCalcQty2);
            if  (xx==4) {
                charter.chartBar("structs using pointers for iteration", (int) offCalcQty2,100,"#70a070");
            }

            long offCalcQty4 = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.size();
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        TestInstrument p = instruments.createPointer(0);
                        TestInstrumentLeg legp = (TestInstrumentLeg) p.legs(0).detach();
                        final int legSiz = legp.getByteSize();
                        final long legoff = legp.___offset - p.___offset; // relative offset of first TestInstrumentLeg of StructArray<TestInstrumentLeg>
                        for (int i=0; i < count; i++ ) {
                            int legs = p.getNumLegs();
                            sum++;
                            for ( int k = 0; k < legs; k++ ) {
                                sum+=legp.getLegQty();
                                legp.next(legSiz);
                            }
                            p.next(siz);
                            legp.___offset=p.___offset+legoff;
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration opt hacked pointer structs iteration inline calcQty "+offCalcQty4);
            if  (xx==4) {
                charter.chartBar("structs using pointers for all accesses + inlined method", (int) offCalcQty4,100,"#70a070");
                charter.closeChart();

                charter.heading("Linear search for first instrument in array with name.equals(given string)");
                charter.text("iterate structure array, access a string enbedded in each element and compare it.<br>");
                charter.openChart("");
            }

            long stringSearchAccessOon = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = onheap[0];
                    final int count = instruments.size();
                    StructString str = new StructString("I999999");
                    for ( int j = 0; j < iterMul; j++ ) {
                        for (int i=0; i < count; i++ ) {
                            if ( str.equals(instruments.get(i).getMnemonic())) {
//                                System.out.println("found "+i);
                                break;
                            }
                        }
                    }
                }
            });
            if  (xx==4) {
                charter.chartBar("on heap", (int) stringSearchAccessOon,10,"#7070a0");
            }
            System.out.println("duration onheap string compare iteration "+stringSearchAccessOon);

            long stringSearchAccessOff0 = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    final int count = instruments.size();
                    StructString str = new StructString("I999999");
                    for ( int j = 0; j < iterMul; j++ ) {
                        for (int i=0; i < count; i++ ) {
                            if ( str.equals(instruments.get(i).getMnemonic())) {
//                                System.out.println("found "+i);
                                break;
                            }
                        }
                    }
                }
            });
            if  (xx==4) {
                charter.chartBar("structs identical code", (int) stringSearchAccessOff0,10,"#70a070");
            }
            System.out.println("duration onheap string compare iteration "+stringSearchAccessOff0);

            long stringSearchAccessOff = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.size();
                    StructString str = new StructString("I999999");
                    for ( int j = 0; j < iterMul; j++ ) {
                        TestInstrument p = instruments.createPointer(0);
                        for (int i=0; i < count; i++ ) {
                            if ( str.equals(p.getMnemonic())) {
//                                System.out.println("found "+i);
                                break;
                            }
                            p.next(siz);
                        }
                    }
                }
            });
            if  (xx==4) {
                charter.chartBar("structs using pointers ", (int) stringSearchAccessOff,10,"#70a070");
            }
            System.out.println("duration opt DIRECT pointer string compare iteration "+stringSearchAccessOff);

            long stringSearchAccessOff1 = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.size();
                    StructString str = new StructString("I999999");
                    for ( int j = 0; j < iterMul; j++ ) {
                        StructString sp = (StructString) instruments.createPointer(0).getMnemonic().detach();
                        for (int i=0; i < count; i++ ) {
                            if ( str.equals(sp)) {
                                break;
                            }
                            sp.next(siz);
                        }
                    }
                }
            });
            if  (xx==4) {
                charter.chartBar("structs using direct pointer to embedded string", (int) stringSearchAccessOff1,10,"#70a070");
                charter.closeChart();
            }
            System.out.println("duration opt DIRECT String pointer string compare iteration "+stringSearchAccessOff1);

        }
        charter.closeDoc();
//        int size = instruments.size();
//        for (int i = 0; i < size; i++) {
//            System.out.println(instruments.get(i));
//        }

//        TestInstrument instrument = fac.toStruct(TestInstrument.createInstrumentTemplate());
//        instrument.setInstrId(99);
//        instrument.getMnemonic().setString("AA");
//        instrument.getMarket().getMnemonic().setString("XEUR");
//        instrument.getMarket().getCloses().setTime(System.currentTimeMillis());
//        instrument.getMarket().getOpens().setTime(System.currentTimeMillis()-12*60*60*1000);
//        System.out.println(instrument.toString());
//        charter.closeChart();

    }
}
