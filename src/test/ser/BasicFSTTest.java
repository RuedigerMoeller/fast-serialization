package ser;

import com.cedarsoftware.util.DeepEquals;
import org.nustaq.serialization.*;
import org.junit.Test;
import org.nustaq.serialization.annotations.Version;

import java.awt.*;
import java.io.*;
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
        FSTObjectRegistry.POS_MAP_SIZE = 1;
        out = new FSTObjectOutput(getTestConfiguration());
        in = new FSTObjectInput(getTestConfiguration());
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

        Object _aBoolean = new boolean[]{true,false};
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
        Empty empty = new Empty();
    }

    static class Empty implements Serializable {
    }

    static class Bl implements Serializable {
        boolean b1,b2,b3;
    }
    
    public static class Strings implements Serializable {
        String empty = "";
        String nil = null;
        String asc = "qpowerijdsfjgkdfg3409589275458965412354doigfoi-.,#+";
        String junk = "ÄÖÜÄß";
        String aputin = "диноросс Роберт Шлегель предлагает смягчить «антипиратский» закон ко второму чтении, в частности блокировать ссылки с нелегальным видео не по IP, а по URL-адресам. Профильному думскому комитету предстоит выбор между двумя противоположными поправками, предусматривающими распространение закона либо только на кино, либо на все произведения искусства. Эксперты уверены, что новация приведет к снижению потребления легального, а не контрафактного контента.";
        String junk1 = junk;
        Bl bl = new Bl();
    }

    static class AscStrings implements Serializable {
        String empty = "";
        String nil = null;
        String asc = "qpowerijdsfjgkdfg3409589275458965412354doigfoi-.,#+";
        String junk = "psdokf spdfoksp spdofk spdfk spdo spdfk psdokf psdkf psdokf psdkf pdkof psodkf psdokf psdokf psodkf psodkf ";
        String junk1 = junk;
        Bl bl = new Bl();
    }

    static class VersioningOld implements Serializable {

        boolean bool = true;
        int primitive = 66;
        String originalOne = "OOPASDKAPSODKPASODKBla";
        HashMap originalMap = new HashMap();

    }

    static class VersioningV1 implements Serializable {

        boolean bool = false;
        int primitive = 13;
        String originalOne = "Bla";
        HashMap originalMap = new HashMap();
        @Version(1)
        String newString1 = "paoskdasd";

    }

    static class VersioningV4 implements Serializable {

        boolean bool = true;
        int primitive = 13;
        String originalOne = "Blasdasda";
        HashMap originalMap = new HashMap();

        @Version(1)
        String newString1 = "paoskdasd";
        @Version(2)
        int x = 123;
        @Version(2)
        int y = 1656;

        @Version(3)
        boolean b0;
        @Version(3)
        boolean b1;

        @Version(4)
        HashMap veryNew = new HashMap();

    }

    static class Versioning implements Serializable {

        boolean bool = false;
        int primitive = 13;
        String originalOne = "Bla";
        HashMap originalMap = new HashMap();

        @Version(1) String newString1 = "paoskdasd";

        @Version(2) int x = 123;
        @Version(2) int y = 1656;

        @Version(3) boolean b0;
        @Version(3) boolean b1;

        @Version(4) HashMap veryNew = new HashMap();

        @Version(5) boolean a,b,c,d,e,f,g,h,i,j,k,l,m,n;
        @Version(5) HashMap veryNew1 = new HashMap();

        public Versioning() {
            originalMap.put("A","BBBBB");
            b0 = true;
            veryNew.put("pok", new Object[] { 1 , 2, 3, 324 });
            veryNew1.put("pok1", new Object[] { 111 , 2, 3, 324 });
            a = f = i = true;
        }

    }

    @Test
    public void testVersioning() {
        Versioning v = new Versioning();

        FSTConfiguration conf = getTestConfiguration();
        conf.registerClass(Versioning.class);

        byte[] bytes = conf.asByteArray(v);
        Versioning res = (Versioning) conf.asObject(bytes);

        assertTrue(DeepEquals.deepEquals(v,res));


        FSTConfiguration old = getTestConfiguration();
        old.registerClass(VersioningOld.class);
        VersioningOld vold = new VersioningOld();
        vold.originalMap.put("uz","aspdokasd");
        bytes = old.asByteArray(vold);

        Versioning newReadFromOld = (Versioning) conf.asObject(bytes);
        assertTrue(newReadFromOld.originalOne.equals("OOPASDKAPSODKPASODKBla"));

        FSTConfiguration oldv4 = getTestConfiguration();
        oldv4.registerClass(VersioningV4.class);
        VersioningV4 oldv4Inst = new VersioningV4();
        oldv4Inst.veryNew.put("uz","aspdokasd");
        bytes = oldv4.asByteArray(oldv4Inst);

        Versioning newReadFromV4 = (Versioning) conf.asObject(bytes);
        assertTrue(newReadFromV4.veryNew.get("uz").equals("aspdokasd"));

    }


    public static class Issue84 implements Serializable{

        private static final long serialVersionUID = 4180807184662357818L;

        public Issue84(Object code) {
            this.code = code;
        }

        private Object code;
    }

    public static class CodeSRO implements Serializable {

        private static final long         serialVersionUID = -5384611645588792010L;

        private Integer                   id;

        private String                    code;

        public CodeSRO(Integer id, String code) {
            this.id = id;
            this.code = code;
        }
    }

    public static class CodeSRONew implements Serializable {

        private static final long         serialVersionUID = -5384611645588792010L;

        public CodeSRONew(Integer id1, String code1, String extCode) {
            id = id1;
            code = code1;
            this.extCode = extCode;
        }

        private Integer                   id;

        private String                    code;

        @Version(1)
        private String                    extCode;
    }

    @Test
    public void testVersioningIssue84() {

        Object[] oldClz = { new Issue84(new CodeSRO(13,"13")), 1 };

        FSTConfiguration oldConf = getTestConfiguration();
        oldConf.registerClass(CodeSRO.class);

        byte[] bytes = oldConf.asByteArray(oldClz);
        Object res = oldConf.asObject(bytes);

        // assure default works
        assertTrue(DeepEquals.deepEquals(oldClz, res));

        // trick to use different class for reading by prereistering
        FSTConfiguration newConf = getTestConfiguration();
        newConf.registerClass(CodeSRONew.class);

        Object newClz = newConf.asObject(bytes);
        System.out.println(newClz);

    }



    protected FSTConfiguration getTestConfiguration() {
        FSTConfiguration.isAndroid = false;
        return FSTConfiguration.createDefaultConfiguration();
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
    public void testWeirdArray() throws Exception {
        WeirdArrays obj = new WeirdArrays();
        out.writeObject(obj);
        in.resetForReuseUseArray(lastBinary = out.getCopyOfWrittenBuffer());
        out.flush();
        WeirdArrays res = (WeirdArrays) in.readObject();
        assertTrue(DeepEquals.deepEquals(obj,res));
    }

    @Test
    public void testSimpleCollections() throws Exception {
        HashMap obj = new HashMap();
        ArrayList li = new ArrayList(); li.add("zero"); li.add(null); li.add("second");
        obj.put("x", li);
        obj.put("y", li);
        obj.put("yy", null);
        obj.put(null, "asd");
        obj.put(3,"99999");
        out.writeObject(obj);

        final byte[] copyOfWrittenBuffer = out.getCopyOfWrittenBuffer();
        in.resetForReuseUseArray(copyOfWrittenBuffer);
        out.flush();
        HashMap res = (HashMap) in.readObject();
        assertTrue(res.get("x") == res.get("y"));
        assertTrue(DeepEquals.deepEquals(obj,res));
    }

//    @Test
//    public void testUTFString() throws Exception {
//        Play obj = new Play();
//        out.writeObject(obj);
//        in.resetForReuseUseArray(out.getCopyOfWrittenBuffer());
//        out.flush();
//        Object res = in.readObject();
//        assertTrue(DeepEquals.deepEquals(obj,res));
//    }


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
        Basics res = (Basics) in.readObject();

        // note: fix false alarm with 1.7_71 + newer 1.8. (because stacktrace not serialized ofc)
        Object[] exceptions1 = res.exceptions;
        Object[] exceptions2 = obj.exceptions;
        res.exceptions = obj.exceptions = null;

        for (int i = 1; i < exceptions1.length; i++) {
            assertTrue( exceptions1[i].getClass() == exceptions2[i].getClass() );
        }
        assertTrue(DeepEquals.deepEquals(obj, res));
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
    public void security() {
        FSTConfiguration conf = FSTConfiguration
            .createDefaultConfiguration()
            .setVerifier(new FSTConfiguration.ClassSecurityVerifier() {
                @Override // need to stick to 1.7, could be lambda
                public boolean allowClassDeserialization(Class cl) {
                    if ( cl.getPackage().getName().startsWith("java.awt") )
                        return false;
                    return true;
                }
        });

        try {
            byte[] hallos = conf.asByteArray(new Dimension(13,13));
            conf.asObject(hallos);
            assertTrue(false);
        } catch (Exception ex) {
            // success
            System.out.println(ex.getMessage());
        }
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

    public static class SubClassedAList extends ArrayList implements Externalizable {

        public SubClassedAList() {
            super();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(size());
            for (int i = 0; i < size(); i++) {
                out.writeObject(get(i));
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            int len = in.readInt();
            for (int i = 0; i < len; i++) {
                add(in.readObject());
            }
        }

        public SubClassedAList $(Object o) {
            add(o);
            return this;
        }
    }

    @Test
    public void testExternalizableOverride() {
        FSTConfiguration conf = getTestConfiguration();
        Object original[] = {"A", new SubClassedAList().$("A").$("B").$("C"), "Ensure stream not corrupted" };
        Object deser = conf.asObject(conf.asByteArray(original));
        assertTrue( DeepEquals.deepEquals(original, deser) );
    }

    static class NotSer {
        int x;
        int y;
        SubClassedAList al;

        private NotSer(int x, int y) {
            this.x = x;
            this.y = y;
            al = new SubClassedAList().$("A").$("B").$("C");
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    static class NotSerSub extends NotSer {

        transient boolean pubConsCalled = false;

        public NotSerSub() {
            super(0,0);
            pubConsCalled = true;
        }

        private NotSerSub(int x, int y) {
            super(x, y);
        }
    }

    // fails if objenesis is used
    @Test
    public void testNotSerializable() {
        FSTConfiguration conf = getTestConfiguration().setForceSerializable(true);
        NotSer ser = new NotSer(11,12);
        assertTrue(DeepEquals.deepEquals(ser, conf.asObject(conf.asByteArray(ser))) );
        NotSerSub sersub = new NotSerSub(11,12);
        final Object deser = conf.asObject(conf.asByteArray(sersub));
        assertTrue(DeepEquals.deepEquals(sersub, deser) );
        assertTrue(((NotSerSub) deser).pubConsCalled);
    }


    public static class TestArray extends ArrayList<Integer> implements Externalizable {

        transient public byte[] bytes1, bytes2;
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(2);
            out.writeInt(548);
            out.writeInt(348);
            byte[] bytes = new byte[1024];
            for (int i = 0; i < 548; i++) {
                bytes[i] = 1;
            }
            out.write(bytes, 0, 548);
            bytes = new byte[1024];
            for (int i = 0; i < 348; i++) {
                bytes[i] = 2;
            }
            out.write(bytes, 0, 348);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            int v1 = in.readInt();
            int v2 = in.readInt();
            int v3 = in.readInt();
            bytes1 = new byte[v2];
            in.readFully(bytes1);
            bytes2 = new byte[v3];
            in.readFully(bytes2);
            int v4 = 0;
        }
    }

    @Test
    public void fastRoundTrip()
        throws IOException, ClassNotFoundException {
        TestArray list = new TestArray();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        FSTObjectOutput objOut = new FSTObjectOutput(os);
        objOut.writeObject(list);
        objOut.close();

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        FSTObjectInput objIn = new FSTObjectInput(is);
//        objIn.setReadExternalReadAHead(16000);
        TestArray res = (TestArray) objIn.readObject();
        for (int i = 0; i < res.bytes1.length; i++) {
            assertTrue(res.bytes1[i] == 1);
        }
        for (int i = 0; i < res.bytes2.length; i++) {
            assertTrue(res.bytes2[i] == 2);
        }
        assertTrue(res.bytes1[547] == 1 && res.bytes2[347] == 2);


    }

    static class T implements Serializable {
        String s;  int i;  T1 t1;
        public T() {}
        public T(int dummy) { s = "pok"; i = 100; t1 = new T1(); }
    }

    static class T1 implements Serializable {
        String s;  int i;
        public T1() {}
        public T1(int dummy) { s = "pok1"; i = 101; }
    }

    public static class TSer extends FSTBasicObjectSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.defaultWriteObject(toWrite,clzInfo);
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            T t = new T();
            in.defaultReadObject(referencee,serializationInfo,t);
            return t;
        }
    }

    public static class T1Ser extends FSTBasicObjectSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.defaultWriteObject(toWrite, clzInfo);
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            T1 t = new T1();
            in.defaultReadObject(referencee,serializationInfo,t);
            return t;
        }
    }

    @Test
    public void testNestedSerializers() {
        FSTConfiguration conf = getTestConfiguration();
        conf.registerSerializer(T.class, new TSer(), true);
        conf.registerSerializer(T1.class, new T1Ser(), true);
        Object p = new T(1);
        byte[] bytes = conf.asByteArray(p);
        Object deser = conf.asObject(bytes);
        assertTrue(DeepEquals.deepEquals(p,deser));
    }

    @org.junit.After
    public void tearDown() throws Exception {

    }
}
