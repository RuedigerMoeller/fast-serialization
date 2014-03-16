package de.ruedigermoeller.heapofftest.gcbenchmarks;

import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.structtypes.StructMap;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;

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
 * Date: 29.06.13
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
public class BasicGCBench {
    FSTStructFactory fac = new FSTStructFactory();

    public BasicGCBench() {
        fac.registerClz(StructString.class);
        fac.registerClz(StructMap.class);
    }

    public static long benchFullGC() {
        long dur = 0;
        long res = 0;
        for ( int i = 0; i < 7; i++ ) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long tim = System.currentTimeMillis();
            System.gc();
            dur = System.currentTimeMillis() - tim;
            System.out.println("FULL GC TIME "+ dur +" mem:"+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000/1000+" MB");
            if ( i >= 3 )
                res += dur;
        }
        return res/4;
    }
}
