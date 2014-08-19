package ser;

import com.cedarsoftware.util.DeepEquals;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.minbin.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

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
 * Date: 13.04.2014
 * Time: 13:41
 * To change this template use File | Settings | File Templates.
 */
public class MinBinTest {

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @Test
    public void tagTest() {
        MBOut out = new MBOut();
        String hallo = "Hallo";
        out.writeTag(hallo);
        out.writeTag(null);
        out.writeTag(123.123d);
        double da[] = {1.23123d,123123.566546,-123812.23123};
        out.writeTag(da);
        MBSequence seq = new MBSequence("seq").add(hallo).add(null).add(123.123d).add(da).add(new Byte((byte) 1));
        MBObject me = new MBObject("me").put("prename", "Ruediger").put("name", "Moeller").put("misc", new int[]{7, 16, 45}).put("test", seq);
        out.writeTag(me);

        MBIn in = new MBIn(out.getBytez(),0);
        Object res = in.readTag(in.readIn());
        assertTrue(DeepEquals.deepEquals(res, hallo));
        assertTrue(in.readTag(in.readIn()) == null);
        Double doub = (Double) in.readTag(in.readIn());
        assertTrue(doub.doubleValue() == 123.123d);
        assertTrue(DeepEquals.deepEquals(in.readTag(in.readIn()), da) );
        Object meRead = in.readObject();
        assertTrue(DeepEquals.deepEquals(meRead, me) );
        MBPrinter.printMessage(me, System.out);

        final String name = "tagtest.minbin";
        final byte[] bytez = out.getBytez();

        writeTmp(name, bytez);
    }

    @Test
    public void jsTest() {
        MBOut out = new MBOut();
        MBSequence sequence = new MBSequence("aSeqType")
                .add("1",2,3,4,"fünf",6,7);
        MBObject ob = new MBObject("myType")
                .put("prename", "Ruediger")
                .put("name", "Moeller")
                .put("nüll", null)
                .put("misc", new int[]{7, 16, 45})
                .put("test", 13)
                .put("floatval", 1.234f)
                .put("doubleval", 1.234567d)
                .put("seField", sequence);
        out.writeTag(ob);

        MBIn in = new MBIn(out.getBytez(),0);
        Object res = in.readTag(in.readIn());

        MBPrinter.printMessage(ob,System.out);

        writeTmp("jstest.minbin", out.getBytez());
    }

    private void writeTmp(String name, byte[] bytez) {
        new File("/tmp/jstest").mkdirs();
        try {
            FileOutputStream fout = new FileOutputStream("/tmp/jstest/"+ name);
            fout.write(bytez);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ARecord implements Serializable {
        String name;
        String profession;
        int postalCode;

        public ARecord(String name, String profession, int postalCode) {
            this.name = name;
            this.profession = profession;
            this.postalCode = postalCode;
        }

    }

    public static class MinBinDemo implements Serializable {
        HashMap aMap;
        List aList;
        int i[] = {1,2,3};

        public MinBinDemo() {
            aMap = new HashMap();
            aMap.put("x", new ARecord("Heinz","butcher",56555));
            aMap.put("xy", new ARecord("Daphne","unknwon",43355));
            aList = new ArrayList<>();
            aList.add(aMap);
            aList.add("Second Item");
        }
    }

    @Test
    public void demo() {
        FSTConfiguration conf = FSTConfiguration.createCrossPlatformConfiguration();
        conf.registerCrossPlatformClassMappingUseSimpleName( Arrays.asList(
                MinBinDemo.class,
                ARecord.class
        ));
        new MBPrinter().printMessage(conf.asByteArray(new MinBinDemo()));
    }

    @Test
    public void primitiveTest() {
        MBOut out = new MBOut();
        out.writeInt(MinBin.INT_8,Byte.MAX_VALUE);
        out.writeInt(MinBin.INT_8,Byte.MIN_VALUE);
        out.writeInt(MinBin.CHAR, Character.MIN_VALUE);
        out.writeInt(MinBin.CHAR, Character.MAX_VALUE);
        out.writeInt(MinBin.INT_16,Short.MIN_VALUE);
        out.writeInt(MinBin.INT_16,Short.MAX_VALUE);
        out.writeInt(MinBin.INT_32,Integer.MIN_VALUE);
        out.writeInt(MinBin.INT_32,Integer.MAX_VALUE);
        out.writeInt(MinBin.INT_64,Long.MIN_VALUE);
        out.writeInt(MinBin.INT_64,Long.MAX_VALUE);
        int[] primitiveArray = {1, 2, 3, 4, 5, 6, 7, Integer.MIN_VALUE, Integer.MAX_VALUE};
        out.writeArray(primitiveArray,0,primitiveArray.length);
        
        MBIn in = new MBIn(out.getBytez(),0);
        
        System.out.println(in.readInt());
        System.out.println(in.readInt());
        
        System.out.println(in.readInt());
        System.out.println(in.readInt());
        
        System.out.println(in.readInt());
        System.out.println(in.readInt());

        System.out.println(in.readInt());
        System.out.println(in.readInt());

        System.out.println(in.readInt());
        System.out.println(in.readInt());

        Object arr = in.readArray();
        assertTrue(DeepEquals.deepEquals(arr, primitiveArray));
        
    }

}
