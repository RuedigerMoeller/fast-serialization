package dson;

import com.cedarsoftware.util.DeepEquals;
import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.serialization.annotations.Serialize;
import org.nustaq.serialization.dson.Dson;
import ser.BasicFSTTest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ruedi on 03.08.14.
 */
public class DsonTest {

    public static class SomePojoConfig implements Serializable {
        String aString = "aString";
        Date aDate = new Date();
        HashMap aMap0 = new HashMap();
        HashMap aMap = new HashMap();
        List aList = new ArrayList<>();
        List aList1 = new ArrayList<>();
    }

    public static class DSonPrimitiveArray implements Serializable { // Dson can't handle multidim

        boolean aBoolean[] = {true,false};
        byte aByte[] = { -13,34, 127,3,23,5,0,11 };
        short aShort0[] = { -13345,345,25645,23,-424};
        char aChar0[] = { 35345,2,3,345,345,345,34566};
        int aInt0[] = { 348535,-34534345,348,127,126,128,129,-127,-126,-128,-129,34544,677576777,-347563453};
        long aLong0[] = { -35435345l,3948573945l,3,4,-66,-127,-128 };
        float aFloat0[] = { -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };
        double aDouble[] = { -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };

        //        Object _aBoolean = new boolean[]{true,false};
        // => results in arrays of bignums, excluded from test but ok'ish for real world
//        Object _aByte = new byte[]{ -13,34, 127,3,23,5,0,11 };
//        Object _aShort0 = new short[]{ -13345,345,25645,23,-424};
//        Object _aChar0 = new char[]{ 35345,2,3,345,345,345,34566};
//        Object _aInt0 = new int[]{ 348535,-34534345,348,34544,677576777,-347563453};
//        Object _aLong0 =new long[] { -35435345l,3948573945l,3,4,-66,-127,-128 };
//        Object _aFloat0 = new float[]{ -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };
//        Object _aDouble = new double[]{ -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };

    }

    public static class DsonBigNums implements Serializable { // can't handle nested/multidim arrays

        Boolean _aBoolean = false;

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


    @org.junit.Before
    public void setUp() {
        Dson.defaultMapper
            .map("Config",SomePojoConfig.class)
            .map("Big", DsonBigNums.class)
            .map("Arr", DSonPrimitiveArray.class)
            .map("Prim", BasicFSTTest.Primitives.class);
    }

    @Test
    public void writeTest() throws Exception {
        SomePojoConfig cfg = new SomePojoConfig();
        cfg.aMap0.put( "A", 1);
        cfg.aMap0.put( "B", 2);
        cfg.aMap0.put( "C", 3);
        cfg.aList1.add("bla");cfg.aList1.add("bla1");cfg.aList1.add("bla2");cfg.aList1.add("bla99");

        cfg.aMap.put( new SomePojoConfig(), "hello");
        cfg.aMap.put( "Hooray", new BasicFSTTest.Primitives() );
        cfg.aList.add(new DSonPrimitiveArray());
        cfg.aList.add(new DsonBigNums());

        Object  testObject = cfg;
        String dsonString = Dson.getInstance().writeObject(testObject);
        System.out.println(dsonString);
        Object o = Dson.getInstance().readObject(dsonString);
        Assert.assertTrue(DeepEquals.deepEquals(o,testObject));
        System.out.println(o);
    }



}
