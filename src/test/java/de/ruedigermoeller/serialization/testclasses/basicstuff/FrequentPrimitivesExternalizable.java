package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.annotations.Flat;
import de.ruedigermoeller.serialization.testclasses.HasDescription;

import java.io.*;

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
 * Time: 12:28
 * To change this template use File | Settings | File Templates.
 */
@Flat
public class FrequentPrimitivesExternalizable implements Externalizable, Serializable, HasDescription {

    static String staticString = "Should not serialize this";
    final static String finalStaticString = "Should not serialize this. Should not serialize this. Should not serialize this. Should not serialize this. Should not serialize this.";

    public static FrequentPrimitivesExternalizable[] getArray(int siz) {
        FrequentPrimitivesExternalizable[] instance = new FrequentPrimitivesExternalizable[siz];
        for (int i = 0; i < instance.length; i++) {
            instance[i] = new FrequentPrimitivesExternalizable(i);
        }
        return instance;
    }

    public FrequentPrimitivesExternalizable()
    {
    }

    public FrequentPrimitivesExternalizable(int index) {
        // avoid benchmarking identity references instead of StringPerf
        str = "R.Moeller"+index;
        str1 = "R.Moeller1"+index;
    }

    @Flat private String str;
    @Flat private String str1;
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
        return "A class with a typical distribution of primitve fields: 2 short Strings, 3 boolean, 6 ints, 2 long, one double. But implementing Externalizable";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(str);
        out.writeUTF(str1);
        out.writeBoolean(b0);
        out.writeBoolean(b1);
        out.writeBoolean(b2);
        out.writeInt(test1);
        out.writeInt(test2);
        out.writeInt(test3);
        out.writeInt(test4);
        out.writeInt(test5);
        out.writeInt(test6);
        out.writeLong(l1);
        out.writeLong(l2);
        out.writeDouble(d);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        str = in.readUTF();
        str1 = in.readUTF();
        b0 = in.readBoolean();
        b1 = in.readBoolean();
        b2 = in.readBoolean();
        test1 = in.readInt();
        test2 = in.readInt();
        test3 = in.readInt();
        test4 = in.readInt();
        test5 = in.readInt();
        test6 = in.readInt();
        l1 = in.readLong();
        l2 = in.readLong();
        d = in.readDouble();
    }
}

