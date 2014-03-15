package de.ruedigermoeller.heapofftest;

import de.ruedigermoeller.heapoff.FSTOffHeapMap;
import de.ruedigermoeller.serialization.testclasses.HasDescription;
import de.ruedigermoeller.serialization.testclasses.HtmlCharter;

import java.io.IOException;
import java.io.Serializable;

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
 * Date: 18.06.13
 * Time: 21:06
 * To change this template use File | Settings | File Templates.
 */
public class OffHeapMapTest {

    public static class SmallThing implements Serializable {
        int ageSum = 10;
        String bla = "bla";

    }

    public static void benchMap(HtmlCharter charter) throws IOException {


        FSTOffHeapMap<Integer,OffHeapTest.ExampleOrder> map = new FSTOffHeapMap<Integer, OffHeapTest.ExampleOrder>(1000);
        map.getConf().registerClass(OffHeapTest.ExampleOrder.class);
        OffHeapTest.ExampleOrder o = new OffHeapTest.ExampleOrder();
        int numelem = 4000000;
        long tim = System.currentTimeMillis();
        for ( int i = 0; i < numelem; i++) {
            map.put( i, o);
        }
        tim = System.currentTimeMillis()-tim;
        charter.openChart("Off Heap Map (FSTOffHeapMap) size:"+map.getHeap().getSize()/1000/1000+" MB "+numelem+" ExampleOrder's (=> is better)");
        charter.chartBar("put ExampleOrder objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");


        tim = System.currentTimeMillis();
        for ( int i = 0; i < numelem; i++) {
            Object something = map.get(i);
        }
        tim = System.currentTimeMillis()-tim;
        charter.chartBar("get ExampleOrder objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");

        int count = 0;
        tim = System.currentTimeMillis();
        for (OffHeapTest.ExampleOrder next : map.values()) {
            count++; // avoid VM optimization
        }
        tim = System.currentTimeMillis()-tim;
        charter.chartBar("iterate ExampleOrder objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");

        charter.closeChart();

        FSTOffHeapMap<String,SmallThing> map1 = new FSTOffHeapMap<String, SmallThing>(1000);
        map1.getConf().registerClass(OffHeapTest.ExampleOrder.class);

        SmallThing p = new SmallThing();
        numelem = 8000000;
        tim = System.currentTimeMillis();
        for ( int i = 0; i < numelem; i++) {
            map1.put("" + i, p);
        }
        tim = System.currentTimeMillis()-tim;
        charter.openChart("Off Heap Map (FSTOffHeapMap) size:"+map1.getHeap().getSize()/1000/1000+" MB "+numelem+" SmallThing's (=> is better)");
        charter.chartBar("put SmallThing objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");


        tim = System.currentTimeMillis();
        for ( int i = 0; i < numelem; i++) {
            Object something = map1.get("" + i);
        }
        tim = System.currentTimeMillis()-tim;
        charter.chartBar("get SmallThing objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");

        count = 0;
        tim = System.currentTimeMillis();
        for (SmallThing next : map1.values()) {
            count++; // avoid VM optimization
        }
        tim = System.currentTimeMillis()-tim;
        charter.chartBar("iterate SmallThing objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");

        charter.closeChart();

    }

}
