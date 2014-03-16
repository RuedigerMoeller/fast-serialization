package de.ruedigermoeller.serialization.testclasses_old.blog;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
 * Time: 19:30
 * To change this template use File | Settings | File Templates.
 */
public class BlogBenchExternalizable implements Externalizable {

    public BlogBenchExternalizable() {
    }

    public BlogBenchExternalizable(int index) {
        // avoid benchmarking identity references instead of StringPerf
        str = "Some Value "+index;
        str1 = "Very Other Value "+index;
        switch (index%3) {
            case 0: str2 = "Default Value"; break;
            case 1: str2 = "Other Default Value"; break;
            case 2: str2 = "Non-Default Value "+index; break;
        }
    }

    private String str;
    private String str1;
    private String str2;
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

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(str);
        out.writeUTF(str1);
        out.writeUTF(str2);
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
        str2 = in.readUTF();
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