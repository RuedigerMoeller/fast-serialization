package ser;

import com.cedarsoftware.util.DeepEquals;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.Boolean;
import java.util.ArrayList;
import java.util.HashMap;

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
 * Date: 28.03.2014
 * Time: 21:43
 * To change this template use File | Settings | File Templates.
 */
public class BasicFSTTest {

    protected FSTObjectOutput out;
    protected FSTObjectInput in;
    
    @org.junit.Before
    public void setUp() throws Exception {
        out = new FSTObjectOutput();
        in = new FSTObjectInput();
    }

    public static class Primitives implements Serializable {

        boolean aBoolean = true;
        
        byte aByte0 = -13;
        byte aByte1 = Byte.MIN_VALUE;
        byte aByte2 = Byte.MAX_VALUE;

        short aShort0 = -13345;
        short aShort1 = Short.MIN_VALUE;
        short aShort2 = Short.MAX_VALUE;

        char aChar0 = 35345;
        char aChar1 = Character.MIN_VALUE;
        char aChar2 = Character.MAX_VALUE;

        int aInt0 = -35345;
        int aInt1 = Integer.MIN_VALUE;
        int aInt2 = Integer.MAX_VALUE;

        long aLong0 = -35435345l;
        long aLong1 = Long.MIN_VALUE;
        long aLong2 = Long.MAX_VALUE;
        long aLong3 = 12l;
        long aLong4 = -12l;
        long aLong5 = 13455;
        long aLong6 = -13455;

        float aFloat0 = -35435345.002f;
        float aFloat1 = Float.MIN_VALUE;
        float aFloat2 = Float.MAX_VALUE;

        double aDouble0 = -35435345.002d;
        double aDouble1 = Double.MIN_VALUE;
        double aDouble2 = Double.MAX_VALUE;
    }

    public static class PrimitiveArray implements Serializable {

        boolean aBoolean[] = {true,false};
        byte aByte[] = { -13,34, 127,3,23,5,0,11 };
        short aShort0[] = { -13345,345,25645,23,-424};
        char aChar0[] = { 35345,2,3,345,345,345,34566};
        int aInt0[] = { 348535,-34534345,348,127,126,128,129,-127,-126,-128,-129,34544,677576777,-347563453};
        int aInt1[][] = { { 348535,-34534345,348 }, null, {34544,677576777,-347563453} };
        long aLong0[] = { -35435345l,3948573945l,3,4,-66,-127,-128 };
        float aFloat0[] = { -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };
        double aDouble[] = { -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };
        Object aRef = aLong0;

//        Object _aBoolean = new boolean[]{true,false};
        Object _aByte = new byte[]{ -13,34, 127,3,23,5,0,11 };
        Object _aShort0 = new short[]{ -13345,345,25645,23,-424};
        Object _aChar0 = new char[]{ 35345,2,3,345,345,345,34566};
        Object _aInt0 = new int[]{ 348535,-34534345,348,34544,677576777,-347563453};
        Object _aInt1 = new int[][]{ { 348535,-34534345,348 }, null, {34544,677576777,-347563453} };
        Object _aLong0 =new long[] { -35435345l,3948573945l,3,4,-66,-127,-128 };
        Object _aFloat0 = new float[]{ -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };
        Object _aDouble = new double[]{ -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };

        Object mix[] = {
                null,
                new int[][]{ { 348535,-34534345,348 }, null, {34544,677576777,-347563453} }, new byte[]{ -13,34, 127,3,23,5,0,11 },
                null,
                new Object[] {(byte)-1,(char)33333,(short)-12312,(int)123313,(long)293847293847l,null,(double)10.1233,(float)2345.234}
        };
        Object mix1 = new Object[] {
                null,
                new int[][]{ { 348535,-34534345,348 }, null, {34544,677576777,-347563453} }, new byte[]{ -13,34, 127,3,23,5,0,11 },
                null,
                new Object[] {(byte)-1,(char)33333,(short)-12312,(int)123313,(long)293847293847l,null,(double)10.1233,(float)2345.234}
        };
        Object aRef1 = mix[1];
    }

    public static class BigNums implements Serializable {

        Boolean _aBoolean = false;
        Boolean ugly[][] = {{true,false},null,{true,false,null}};

        Byte _aByte0 = -13;
        Object _aByte1 = Byte.MIN_VALUE;
        Byte _aByte2 = Byte.MAX_VALUE;
        Byte aByteA2[] = { Byte.MAX_VALUE  };

        Short _aShort0 = -1334;
        Object _aShort1 = Short.MIN_VALUE;
        Short _aShort2 = Short.MAX_VALUE;
        Short _aShort2a[] = {0,null,Short.MAX_VALUE};

        Character _aChar0 = 35345;
        Object _aChar1 = Character.MIN_VALUE;
        Character _aChar2 = Character.MAX_VALUE;
        Character _aChar2a[] = {null,Character.MAX_VALUE};


        Integer _aInt0 = 35345;
        Object _aInt1 = Integer.MIN_VALUE;
        Integer _aInt2 = Integer.MAX_VALUE;
        Integer _aInt2a[] = {Integer.MAX_VALUE};

        Long _aLong0 = -34564567l;
        Object _aLong1 = Long.MIN_VALUE;
        Long _aLong2 = Long.MAX_VALUE;
        Long _aLong2a[] = {Long.MAX_VALUE};

        Float _aFloat0 = 123.66f;
        Object _aFloat1 = Float.MIN_VALUE;
        Float _aFloat2 = Float.MAX_VALUE;
        Float _aFloat2a[] = {-8.7f,Float.MAX_VALUE};

        Double _aDouble0 = 123.66d;
        Object _aDouble1 = Double.MIN_VALUE;
        Double _aDouble2 = Double.MAX_VALUE;
        Double _aDouble2a[] = {-88.0,Double.MAX_VALUE};
    }

    static class Bl implements Serializable {
        boolean b1,b2,b3;
    }
    
    static class Strings implements Serializable {
        String empty = "";
        String nil = null;
        String asc = "qpowerijdsfjgkdfg3409589275458965412354doigfoi-.,#+";
        String junk = "ÄÖÜÄß";
        String aputin = "диноросс Роберт Шлегель предлагает смягчить «антипиратский» закон ко второму чтении, в частности блокировать ссылки с нелегальным видео не по IP, а по URL-адресам. Профильному думскому комитету предстоит выбор между двумя противоположными поправками, предусматривающими распространение закона либо только на кино, либо на все произведения искусства. Эксперты уверены, что новация приведет к снижению потребления легального, а не контрафактного контента.";
        String junk1 = junk;
        Bl bl = new Bl();
    }

    @Test
    public void testPrimitives() throws Exception {
        Primitives obj = new Primitives();
        out.writeObject(obj);
        in.resetForReuseUseArray(lastBinary=out.getCopyOfWrittenBuffer());
        out.flush();
        Object res = in.readObject();
        assertTrue(DeepEquals.deepEquals(obj,res));
    }

    @Test
    public void testPrimitiveArray() throws Exception {
        PrimitiveArray obj = new PrimitiveArray();
        out.writeObject(obj);
        in.resetForReuseUseArray(lastBinary = out.getCopyOfWrittenBuffer());
        out.flush();
        PrimitiveArray res = (PrimitiveArray) in.readObject();
        assertTrue(res.aLong0 == res.aRef);
        assertTrue(res.aRef1 == res.mix[1]);
        assertTrue(DeepEquals.deepEquals(obj,res));
    }

    @Test
    public void testSimpleCollections() throws Exception {
        HashMap obj = new HashMap();
        ArrayList li = new ArrayList(); li.add("zero"); li.add("second");
        obj.put("x", li);
        obj.put("y", li);
        obj.put(3,"99999");
        out.writeObject(obj);

        final byte[] copyOfWrittenBuffer = out.getCopyOfWrittenBuffer();
        in.resetForReuseUseArray(copyOfWrittenBuffer);
        out.flush();
        HashMap res = (HashMap) in.readObject();
        assertTrue(res.get("x") == res.get("y"));
        assertTrue(DeepEquals.deepEquals(obj,res));
    }

    @Test
    public void testBigNums() throws Exception {
        BigNums obj = new BigNums();
        out.writeObject(obj);
        in.resetForReuseUseArray(out.getCopyOfWrittenBuffer());
        out.flush();
        Object res = in.readObject();
        assertTrue(DeepEquals.deepEquals(obj,res));
    }

    @Test
    public void testException() throws Exception {
        Object exceptions[] = {
                null, new Exception("test"), new ArrayIndexOutOfBoundsException(), new RuntimeException(new IllegalArgumentException("Blub"))
        };
        try {
            throw new IOException();
        } catch (Exception ex) {
            exceptions[0] = ex;
        }
        out.writeObject(exceptions);
        in.resetForReuseUseArray(out.getCopyOfWrittenBuffer());
        out.flush();
        Object res[] = (Object[]) in.readObject();
//        assertTrue(DeepEquals.deepEquals(obj,res));
        for (int i = 0; i < res.length; i++) {
            Object ex = res[i];
            String message = ((Throwable) exceptions[i]).getMessage();
            String message1 = ((Throwable) ex).getMessage();
            assertTrue(DeepEquals.deepEquals(message,message1));
        }
    }

    @Test
    public void testEnums() throws Exception {
        Basics obj = new Basics(123);
        out.writeObject(obj);
        in.resetForReuseUseArray(out.getCopyOfWrittenBuffer());
        out.flush();
        Object res = in.readObject();
        assertTrue(DeepEquals.deepEquals(obj,res));
    }

    protected byte[] lastBinary;
    @Test
    public void testStrings() throws Exception {
        Strings obj = new Strings();
        out.writeObject(obj);
        in.resetForReuseUseArray(out.getCopyOfWrittenBuffer());
        out.flush();
        Strings res = (Strings) in.readObject();
        assertTrue(DeepEquals.deepEquals(res.junk, res.junk1));
        assertTrue(DeepEquals.deepEquals(obj,res));
    }

    @Test
    public void testFlush() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(1000*1000);
        FSTObjectOutput fout = new FSTObjectOutput(bout);
        Strings obj = new Strings();
        fout.writeObject(obj);
        fout.writeObject(new byte[1000*1000*10]);
        fout.writeObject(obj);
        fout.close();

        FSTObjectInput fin = new FSTObjectInput(new ByteArrayInputStream(bout.toByteArray()));
        Strings res = (Strings) fin.readObject();
        fin.readObject();
        Strings res1 = (Strings) fin.readObject();
        assertTrue(res == res1);
        assertTrue(DeepEquals.deepEquals(obj,res));
    }
    
    @org.junit.After
    public void tearDown() throws Exception {

    }
}
