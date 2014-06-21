package de.ruedigermoeller.heapofftest;

import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.annotations.Flat;
import de.ruedigermoeller.serialization.testclasses.HtmlCharter;
import de.ruedigermoeller.serialization.testclasses.enterprise.Trader;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;

import com.cedarsoftware.util.DeepEquals;

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
 * Date: 14.12.12
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */

// no junit .. just dislike jar jungles ..
public class OffHeapTest {


    private static void search(FSTByteBufferOffheap off, final Object toSearch, int[] count) {
        long tim = System.currentTimeMillis();
        FSTByteBufferOffheap.OffHeapIterator it = off.iterator();
        int counter = 0;
        try {
            while( it.hasNext() ) {
                counter++;
                Object tag = it.nextEntry(new FSTObjectInput.ConditionalCallback() {
                    @Override
                    public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field) {
                        FSTByteBufferOffheap.ByteBufferEntry be = (FSTByteBufferOffheap.ByteBufferEntry) halfDecoded;
                        return !toSearch.equals(be.tag);
                    }
                });
                if ( it.getCurrentEntry() != null ) {
                    System.out.println("found ! "+it.getCurrentTag()+" "+Thread.currentThread().getName());
                }
            }
        } finally {
            System.out.println("count "+counter);
        }
        if ( count != null ) {
            synchronized (count) {
                count[0]++;
            }
        }
        System.out.println("search time (no break) "+(System.currentTimeMillis()-tim)+" "+Thread.currentThread().getName());
    }

    @Flat
    public static class ExampleOrder implements Serializable {
        @Flat String product = "ALV";
        long contract = 28374645556l;
        char buySell = 'B';
        int qty = 1000;
        double limit = 22.545;
        @Flat String text = "Bla bla bla Bla";
        @Flat String member = "CBKFR";
        @Flat String owner = "XYZEXTRD013";
        byte restriction = 1;
        byte type = 3;
        @Flat Date validity = new Date();
    }

    public static void benchOffHeap(boolean header, final FSTByteBufferOffheap off, HtmlCharter charter, String tit) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {

        final ExampleOrder or = new ExampleOrder();
        final int iters = 4000000;
        charter.openChart(tit+" FSTByteBufferOffheap core operations performance (=> is better)");

        if ( header ) {
            charter.text("<br><br>The following instance is added 4.000.000 times to the offheap. Each object added to the heap can have a tag. The tag can be used " +
                    "to retrieve an object faster, because with 'tag-iteration' only the tag gets decoded. With full iteration, all 4 million objects get decoded during iteration.");
            charter.text("<pre><code>    public static class ExampleOrder implements Serializable {\n" +
                    "        String product = \"ALV\";\n" +
                    "        long contract = 28374645556l;\n" +
                    "        char buySell = 'B';\n" +
                    "        int qty = 1000;\n" +
                    "        double limit = 22.545;\n" +
                    "        String text = \"Bla bla bla Bla\";\n" +
                    "        String member = \"CBKFR\";\n" +
                    "        String owner = \"XYZEXTRD013\";\n" +
                    "        byte restriction = 1;\n" +
                    "        byte type = 3;\n" +
                    "        Date validity = new Date();\n" +
                    "    }" +
                    "</code></pre>" +
                    "thereafter the offheap is iterated (single/multithreaded) by tag and by element.<br>");
        }
        FSTByteBufferOffheap.OffHeapAccess acc = off.createAccess();
        long tim = System.currentTimeMillis();
        final Integer MYTAG = 999;

        for ( int i = 0; i < iters; i++ ) {
            if ( i == 1237162 ) {
                acc.add(or, MYTAG);
            } else {
                acc.add(or, null );
            }
        }

        long dur = System.currentTimeMillis() - tim;

        charter.chartBar("add ExampleOrder (objects/s)", (int) ((iters/dur)*1000),100000,"#8080ff");
        System.out.println("TIM add "+ dur +" per ms "+(iters/dur));
        System.out.println("siz " + off.getLastPosition() / 1000 / 1000);


        tim = System.currentTimeMillis();
        search(off, MYTAG, null);
        dur = System.currentTimeMillis() - tim;
        charter.chartBar("search tag based (objects/s)", (int) ((iters/dur)*1000),100000,"#8080ff");
        System.out.println("TIM search" + dur + " per ms " + (iters / dur));
        System.out.println("siz " + off.getLastPosition() / 1000 / 1000);


        tim = System.currentTimeMillis();
        FSTByteBufferOffheap.OffHeapIterator it = off.iterator();
        while( it.hasNext() ) {
            it.nextEntry(null);
        }
        dur = System.currentTimeMillis() - tim;
        charter.chartBar("iterate ExampleOrder (objects/s)", (int) ((iters/dur)*1000),100000,"#8080ff");

        tim = System.currentTimeMillis();
        final int sum[] = { 0 };
        final int count[] = { 0 };
        int threads = 4;
        for ( int i = 0; i < threads; i++) {
            new Thread("search "+i) {
                public void run() {
                    search(off, MYTAG, count);
                }
            }.start();
        }

        while( count[0] != threads) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
        dur = System.currentTimeMillis() - tim;

        charter.chartBar(threads+" thread <b>concurrent</b> search tag based (objects/s)", (int) (threads*iters/dur)*1000,100000,"#ff8080");


        tim = System.currentTimeMillis();
        count[0] = 0;
        for ( int i = 0; i < threads; i++) {
            new Thread("search "+i) {
                public void run() {
                    FSTByteBufferOffheap.OffHeapIterator it = off.iterator();
                    while( it.hasNext() ) {
                        it.nextEntry(null);
                    }
                    if ( count != null ) { //bad habit sync .. does not matter here
                        synchronized (count) {
                            count[0]++;
                        }
                    }
                }
            }.start();
        }

        while( count[0] != threads) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
        dur = System.currentTimeMillis() - tim;

        charter.chartBar(threads+" thread <b>concurrent</b> iterate ExampleOrder (objects/s)", (int) (threads*iters/dur)*1000,100000,"#ff8080");

        charter.closeChart();
    }

    private static void simpleTest(FSTByteBufferOffheap off) throws Exception {
        FSTByteBufferOffheap.OffHeapAccess access = off.createAccess();
        // simple test:
        Object[] toSave = {
                new Object[] {Trader.generateTrader(111, true), new ExampleOrder()},
                "Hallo1",
                "hallo2",
                "Oha"
        };
        int handles[] = new int[toSave.length];
        for (int i = 0; i < handles.length; i++) {
            handles[i] = access.add(toSave[i],"tag "+i);
        }

        Object loaded[] = new Object[handles.length];
        for (int i = 0; i < loaded.length; i++) {
            loaded[i] = access.getObject(handles[i]);
            if ( ! DeepEquals.deepEquals(toSave[i], loaded[i]) ) {
                System.out.println("2 BUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUG !!");
            }
        }
        int count = loaded.length-1;
        FSTByteBufferOffheap.OffHeapIterator iterator = off.iterator();
        while( iterator.hasNext() ) {
            Object atag = iterator.nextEntry(null);
            Object content = iterator.getCurrentEntry();
            System.out.println("pok "+atag+" "+content);
            if ( ! ("tag "+count).equals(atag) )
                System.out.println("1 BUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUG !!");
            count--;
        }
        if ( count != -1 )
            System.out.println("0 BUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUG !!");
    }

    public static void main( String arg[]) throws Exception {
        HtmlCharter charter = new HtmlCharter("./offheap.html");
        charter.openDoc();

        System.setProperty("fst.unsafe", "true");

        FSTByteBufferOffheap off = new FSTByteBufferOffheap(1000);
        off.getConf().registerClass(ExampleOrder.class);

        simpleTest(off);
//        simpleTest(off);


        benchOffHeap(true, off, charter, "Direct ByteBuffer");



        // create off heap on memory mapped file:
        RandomAccessFile randomFile = new RandomAccessFile("./mappedfile.bin", "rw");
        randomFile.setLength(1000*1000*1000);
        FileChannel channel = randomFile.getChannel();
        MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1000 * 1000 * 1000);
        FSTByteBufferOffheap off1 = new FSTByteBufferOffheap(buf);
        off1.getConf().registerClass(ExampleOrder.class);
        benchOffHeap(false, off1, charter, "Memory mapped File:");
        randomFile.close();
        charter.closeDoc();
    }


}
