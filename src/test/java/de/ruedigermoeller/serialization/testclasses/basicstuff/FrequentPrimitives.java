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
 * Time: 00:54
 * To change this template use File | Settings | File Templates.
 */
public class FrequentPrimitives implements Serializable, HasDescription {

    static String staticString = "Should not serialize this";
    final static String finalStaticString = "Should not serialize this. Should not serialize this. Should not serialize this. Should not serialize this. Should not serialize this.";

    public static FrequentPrimitives[] getArray(int siz) {
        FrequentPrimitives[] instance = new FrequentPrimitives[siz];
        for (int i = 0; i < instance.length; i++) {
            instance[i] = new FrequentPrimitives(i);
        }
        return instance;
    }

    public FrequentPrimitives()
    {
    }

    public FrequentPrimitives(int index) {
        // avoid benchmarking identity references instead of StringPerf
        str = "R.Moeller"+index;
        str1 = "R.Moeller1"+index;
    }

    private String str;
    private String str1;
    private boolean b0 = true;
    private boolean b1 = false;
    private boolean b2 = true;
    private int test1 = 123456;
    private int test2 = 234234;
    private int test3 = 456456;
    private int test4 = -234234344;
    private int test5 = -1;
    private int test6 = 0;
    private long l1 = -38457359987788345l;
    private long l2 = 0l;
    private double d = 122.33;

    @Override
    public String getDescription() {
        return "A class with a typical distribution of primitve fields: 2 short Strings, 3 boolean, 6 ints, 2 long, one double.";
    }
}
