package de.ruedigermoeller.heapofftest;

import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTClazzInfoRegistry;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.util.FSTUtil;
import sun.misc.Unsafe;

import java.util.Date;

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
 * Date: 24.02.13
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
public class AnotherOffheapThingy<T> {
    static class Sample extends Date {
        int x = 11;
        int y = 12;
        String s = "Hallo";
    }

    public static <T> T copyObj(Unsafe un, T test, int siz) {
        byte b[] = new byte[(int) siz];
        un.copyMemory(test,0, b, 0, siz);
        return (T)b;
    }

    public static void main( String arg[] ) {
        System.setProperty("fst.unsafe","true");
        Unsafe un = FSTUtil.getUnsafe();
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        FSTClazzInfo cl = conf.getClassInfo(Sample.class);
        Sample test = new Sample();
        System.out.println("fioff "+cl.getFieldInfo("fastTime",Date.class).getMemOffset() );
        System.out.println("fioff "+cl.getFieldInfo("x",Sample.class).getMemOffset() );

        test.x = 9999;

        Sample other = new Sample();

        long pos = cl.getFieldInfo("x", Sample.class).getMemOffset();
        long siz = cl.getFieldInfo("s", Sample.class).getMemOffset()+8;
        byte b[] = new byte[(int) siz];
        un.copyMemory(test,0, b, 0, siz);

        other = copyObj(un,test, (int) siz);

    }
}
