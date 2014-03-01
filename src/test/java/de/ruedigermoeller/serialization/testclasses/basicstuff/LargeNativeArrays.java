package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.testclasses.HasDescription;

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
 * Date: 15.06.13
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class LargeNativeArrays implements Serializable, HasDescription {
    @Override
    public String getDescription() {
        return "measures performance serializing a large int array, a large long array and a large double array filled with random values. Note that random values destroy any value compression, so the increased size of kryo in this test will not be observable in most real world data.";
    }

    int ints[];
    long longs[];
    double doubles[];
    public LargeNativeArrays() {
        int N = 1300;
        ints = new int[N*2];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = (int) (Math.random()*Integer.MAX_VALUE*2-Integer.MAX_VALUE);
        }
        longs = new long[N];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = (long) (Math.random()*Long.MAX_VALUE*2-Long.MAX_VALUE);
        }
        doubles = new double[N];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = Math.random()*Double.MAX_VALUE*2-Double.MAX_VALUE;
        }
    }
}
