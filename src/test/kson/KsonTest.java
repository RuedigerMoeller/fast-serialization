package kson;

import com.cedarsoftware.util.DeepEquals;
import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.kson.Kson;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Created by ruedi on 03.08.14.
 */
public class KsonTest {

    public static class SomePojoConfig {
        String aString;
        HashMap<String,PojoConfigItem> aMap;
        Map<PojoConfigItem,String> objectMap;
        List<OtherPojoConfigItem> aList;
        List untypedList;
    }

    public static class OtherPojoConfigItem {
        String nameList[];
        int someValues[];
    }

    public static class PojoConfigItem {
        String str;
        int someValue;
    }

    @Test
    public void testPojoConf() throws Exception {
        Kson kk = new Kson()
            .map("test", SomePojoConfig.class)
            .map("pojo", PojoConfigItem.class)
            .map("other", OtherPojoConfigItem.class);
        SomePojoConfig result = (SomePojoConfig) kk.readObject( new File("./src/test/kson/test.kson"));
        Assert.assertTrue(result.aList.get(1).nameList[0].equals("Short"));
        Assert.assertTrue(result.untypedList.size() == 2);

        String res = kk.writeObject(result);
        System.out.println(res);

        Object reRead = kk.readObject(res);

        String rereadString = kk.writeObject(reRead);
        System.out.println(rereadString);

        Assert.assertTrue(res.equals(rereadString));
        Assert.assertTrue(DeepEquals.deepEquals(result, reRead));

        res = kk.writeJSonObject(result, true);
        System.out.println(res);

    }

    @Test
    public void testJSonConf() throws Exception
    {
        Kson kk = new Kson()
            .map("test", SomePojoConfig.class)
            .map("pojo", PojoConfigItem.class)
            .map("other", OtherPojoConfigItem.class);
        SomePojoConfig result = (SomePojoConfig) kk.readObject( new File("./src/test/kson/test.json"), "test" );

        String res = kk.writeJSonObject(result,true);
        System.out.println(res);
        Object reRead = kk.readObject(res);
        Assert.assertTrue(DeepEquals.deepEquals(result,reRead));
    }

    @Test
    public void testJSonInetSample() throws Exception {
        KsonCustomer cust = new KsonCustomer();
        cust.setName("John Doe");
        cust.setId(1);
        cust.getPhoneNumbers().add(new KsonPhoneNumber("home", "345 34592-0"));
        cust.getPhoneNumbers().add(new KsonPhoneNumber("work", "345 34592-1"));

        Kson kk = new Kson().map("customer", KsonCustomer.class).map("phone", KsonPhoneNumber.class);
        System.out.println(kk.writeObject(cust));
        System.out.println(kk.writeJSonObject(cust, false));

    }

    @Test
    public void testBasics() throws Exception {
        Kson kk = new Kson()
                .map("arraytest", KKPrimitiveArray.class)
                .map("bignums", KKBigNums.class);
        serDeserObject(kk, new KKPrimitiveArray().setVals());
        serDeserObject(kk, new KKBigNums().setVals());
    }

    protected void serDeserObject(Kson kk, Object toser) throws Exception {
        final String written = kk.writeObject(toser);
        System.out.println(written);
        Object deser = kk.readObject(written);

        Assert.assertTrue(DeepEquals.deepEquals(deser, toser));

        final String json = kk.writeJSonObject(toser, true);
        System.out.println(json);
        deser = kk.readObject(json);

        Assert.assertTrue(DeepEquals.deepEquals(deser,toser));
    }

    public static class KKPrimitiveArray implements Serializable { // Dson can't handle multidim

        boolean aBoolean[];
        byte aByte[];
        short aShort0[];
        char aChar0[];
        int aInt0[];
        long aLong0[];
        float aFloat0[];
        double aDouble[];

        public KKPrimitiveArray setVals() {
            boolean aBoolean[] = {true,false};
            byte aByte[] = { -13,34, 127,3,23,5,0,11 };
            short aShort0[] = { -13345,345,25645,23,-424};
            char aChar0[] = { 35345,2,3,345,345,345,34566};
            int aInt0[] = { 348535,-34534345,348,127,126,128,129,-127,-126,-128,-129,34544,677576777,-347563453};
            long aLong0[] = { -35435345l,3948573945l,3,4,-66,-127,-128 };
            float aFloat0[] = { -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };
            double aDouble[] = { -35435345.34534f,3948573945.34534f,3.34534f,4.34534f,-66.34534f,-127.34534f,-128.34534f };

            this.aBoolean = aBoolean;
            this.aByte = aByte;
            this.aShort0 = aShort0;
            this.aChar0 = aChar0;
            this.aInt0 = aInt0;
            this.aLong0 = aLong0;
            this.aFloat0 = aFloat0;
            this.aDouble = aDouble;
            return this;
        }

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

    public static class KKBigNums implements Serializable { // can't handle nested/multidim arrays
        int i = 10;
        Boolean _aBoolean ;

        Byte _aByte0;
//        Object _aByte1;
        Byte _aByte2 ;
        Byte aByteA2[];

        Short _aShort0;
        Short _aShort3;
        Short _aShort1;
        Short _aShort2;
        Short _aShort2a[];

        Character _aChar0;
        Character _aChar1;
        Character _aChar2;
        Character _aChar2a[];

        Integer _aInt0;
//        Object _aInt1; see below
        Integer _aInt2;
        Integer _aInt2a[];

        Long _aLong0;
        Object _aLong1;
        Long _aLong2;
        Long _aLong2a[];

        Float _aFloat0;
//        Object _aFloat1; will result in Double and test failure, unfixable
        Float _aFloat2;
        Float _aFloat2a[];

        Double _aDouble0;
        Object _aDouble1;
        Double _aDouble2;
        Double _aDouble2a[];

        public KKBigNums setVals() {
            _aBoolean = true;

            _aByte0 = -13;
//            _aByte1 = Byte.MIN_VALUE;
            _aByte2 = Byte.MAX_VALUE;
            aByteA2 = new Byte[] { Byte.MIN_VALUE  };

            _aShort0 = -1334;
            _aShort3 = null;
            _aShort1 = Short.MIN_VALUE;
            _aShort2 = Short.MAX_VALUE;
            _aShort2a = new Short[]{0,null,Short.MAX_VALUE};

            _aChar0 = 35345;
            _aChar1 = Character.MIN_VALUE;
            _aChar2 = Character.MAX_VALUE;
            _aChar2a = new Character[] {null,Character.MAX_VALUE};

            _aInt0 = 35345;
//            _aInt1 = Integer.MIN_VALUE;
            _aInt2 = Integer.MAX_VALUE;
            _aInt2a = new Integer[] {Integer.MIN_VALUE};

            _aLong0 = -34564567l;
            _aLong1 = Long.MIN_VALUE;
            _aLong2 = Long.MAX_VALUE;
            _aLong2a = new Long[]{Long.MAX_VALUE};

            _aFloat0 = -123.66f;
//            _aFloat1 = Float.MIN_VALUE;
            _aFloat2 = Float.MAX_VALUE;
            _aFloat2a = new Float[]{-8.7f,Float.MAX_VALUE};

            _aDouble0 = 123.66d;
            _aDouble1 = Double.MIN_VALUE;
            _aDouble2 = Double.MAX_VALUE;
            _aDouble2a = new Double[]{-88.0,Double.MAX_VALUE};
            return this;
        }
    }



}
