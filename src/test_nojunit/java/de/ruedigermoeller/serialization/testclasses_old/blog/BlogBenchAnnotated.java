package de.ruedigermoeller.serialization.testclasses_old.blog;

import de.ruedigermoeller.serialization.annotations.Flat;
import de.ruedigermoeller.serialization.annotations.OneOf;
import de.ruedigermoeller.serialization.annotations.Predict;

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
 * Date: 10.10.13
 * Time: 21:26
 * To change this template use File | Settings | File Templates.
 */
@Flat @Predict(BlogBenchAnnotated.class)
public class BlogBenchAnnotated  implements Serializable {

    public BlogBenchAnnotated(int index) {
        // avoid benchmarking identity references instead of StringPerf
        str = "Some Value "+index;
        str1 = "Very Other Value "+index;
        switch (index%3) {
            case 0: str2 = "Default Value"; break;
            case 1: str2 = "Other Default Value"; break;
            case 2: str2 = "Non-Default Value "+index; break;
        }
    }

    @Flat private String str;
    @Flat private String str1;
    @OneOf({"Default Value","Other Default Value"})
    @Flat private String str2;
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

}

