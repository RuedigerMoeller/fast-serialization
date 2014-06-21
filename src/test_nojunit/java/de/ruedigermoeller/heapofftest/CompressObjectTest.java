package de.ruedigermoeller.heapofftest;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;

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
 * Date: 20.06.13
 * Time: 20:41
 * To change this template use File | Settings | File Templates.
 */
public class CompressObjectTest {

    static Object ref;
    static void printGCStats() {
        long totalGarbageCollections = 0;
        long garbageCollectionTime = 0;

        for(GarbageCollectorMXBean gc :
                ManagementFactory.getGarbageCollectorMXBeans()) {

            long count = gc.getCollectionCount();

            if(count >= 0) {
                totalGarbageCollections += count;
            }

            long time = gc.getCollectionTime();

            if(time >= 0) {
                garbageCollectionTime += time;
            }
        }

        System.out.println("Total Garbage Collections: "
                + totalGarbageCollections);
        System.out.println("Total Garbage Collection Time (ms): "
                + garbageCollectionTime);
    }

    static void registerGCListener() {
            ((NotificationEmitter) ManagementFactory.getMemoryMXBean()).addNotificationListener(new NotificationListener() {
                @Override
                public void handleNotification(Notification notification, Object handback) {
                    CompositeData cd = (CompositeData) notification.getUserData();
                    MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);
                    printGCStats();
                }
            }, null, null);
            List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();

            MemoryPoolMXBean maxHeap = null;
            long maxSiz = 0;
            for (int i = 0; i < beans.size(); i++) {
                MemoryPoolMXBean memoryPoolMXBean = beans.get(i);
                if (memoryPoolMXBean.getType() == MemoryType.HEAP) {
                    long siz = memoryPoolMXBean.getUsage().getMax();
                    System.out.println("Heap found: " + memoryPoolMXBean.getName() + " " + memoryPoolMXBean.getType());
                    if (siz > maxSiz) {
                        maxSiz = siz;
                        maxHeap = memoryPoolMXBean;
                    }
                }
            }

            if (maxHeap == null) {
                System.out.println("no heap found");
            } else {
                maxHeap.setCollectionUsageThreshold(1000l);
            }
    }

    public static void testExampleOrder() {
        FSTCompressor comp = new FSTCompressor();
        registerGCListener();

        try {
            System.out.println("start");

            ArrayList<FSTCompressed<OffHeapTest.ExampleOrder>> list = new ArrayList<FSTCompressed<OffHeapTest.ExampleOrder>>();

            ArrayList list1 = new ArrayList();
            ref = list1;

            for ( int i = 0; i < 10000000; i++) {
                OffHeapTest.ExampleOrder obj = new OffHeapTest.ExampleOrder();
//                list.add(comp.compress2Byte(obj));
                list1.add(obj);
            }

            System.out.println("finished add");

            while( true ) {
                System.gc();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public static void testSimple() {
        FSTCompressor comp = new FSTCompressor();
        registerGCListener();

        try {
            FSTCompressed<OffHeapTest.ExampleOrder> order = comp.compress2Byte(new OffHeapTest.ExampleOrder());
            System.out.println(order.get().text+" "+order.getLen());

            FSTCompressed<Trader> trader = comp.compress2Byte(Trader.generateTrader(101,true));
            System.out.println(trader.get().getBusinessUnitName()+" "+trader.getLen());

            Thread.sleep(10000);
            System.out.println("start");

            ArrayList<FSTCompressed<SimpleOrder>> list = new ArrayList<FSTCompressed<SimpleOrder>>();

            ArrayList<SimpleOrder> list1 = new ArrayList<SimpleOrder>();
            ref = list1;

            for ( int i = 0; i < 10000000; i++) {
                SimpleOrder obj = SimpleOrder.generateOrder(i);
                obj.getOrderQty().setValue(i);
                list.add(comp.compress2Byte(obj));
//                list1.add(obj);
            }

            System.out.println("finished add");

            while( true ) {
                System.gc();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void main(String arg[]) {
        testExampleOrder();
    }


}
