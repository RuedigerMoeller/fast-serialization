package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.minbin.MBOut;
import de.ruedigermoeller.serialization.minbin.MBPrinter;
import de.ruedigermoeller.serialization.minbin.MinBin;

import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;

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
 * Date: 30.03.2014
 * Time: 18:47
 * To change this template use File | Settings | File Templates.
 */
public class FSTMixEncoder implements FSTEncoder {

    MBOut out = new MBOut();
    OutputStream outputStream;
    FSTConfiguration conf;
    private int offset = 0;

    public FSTMixEncoder(FSTConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public void writeRawBytes(byte[] bufferedName, int off, int length) throws IOException {
        out.writeArray(bufferedName, off, length);
    }

    /**
     * does not write class tag and length
     *
     * @param array
     * @param start
     * @param length @throws java.io.IOException
     */
    @Override
    public void writePrimitiveArray(Object array, int start, int length) throws IOException {
        out.writeArray(array, start, length);
    }

    @Override
    public void writeStringUTF(String str) throws IOException {
        out.writeTag(str);
    }

    @Override
    public void writeFShort(short c) throws IOException {
        out.writeInt(MinBin.INT_16,c);
    }

    @Override
    public void writeFChar(char c) throws IOException {
        out.writeInt(MinBin.CHAR, c);
    }

    @Override
    public void writeFByte(int v) throws IOException {
        out.writeInt(MinBin.INT_8, v);
    }

    @Override
    public void writeFInt(int anInt) throws IOException {
        out.writeInt(MinBin.INT_32,anInt);
    }

    @Override
    public void writeFLong(long anInt) throws IOException {
        out.writeInt(MinBin.INT_64,anInt);
    }

    @Override
    public void writeFFloat(float value) throws IOException {
        out.writeTag(value);
    }

    @Override
    public void writeFDouble(double value) throws IOException {
        out.writeTag(value);
    }

    @Override
    public int getWritten() {
        return out.getWritten()+offset;
    }

    @Override
    public void skip(int i) {
        throw new RuntimeException("not supported");
    }

    /**
     * close and flush to underlying stream if present. The stream is also closed
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        if ( outputStream != null ) {
            
            outputStream.close();
        }
    }

    @Override
    public void reset() {
        offset = 0;
        out.reset();
    }

    @Override
    public void reset(byte[] bytez) {
        offset = 0;
        out.reset(bytez);
    }

    /**
     * resets stream (positions are lost)
     *
     * @throws java.io.IOException
     */
    @Override
    public void flush() throws IOException {
        if ( outputStream != null ) {
            outputStream.write(out.getBytez(),0,out.getWritten());
            offset = out.getWritten();
            out.resetPosition();
        }
    }

    /**
     * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
     *
     * @param position
     * @param v
     */
    @Override
    public void writeInt32At(int position, int v) {
        throw new RuntimeException("not supported");
    }

    /**
     * if output stream is null, just encode into a byte array
     *
     * @param outstream
     */
    @Override
    public void setOutstream(OutputStream outstream) {
        this.outputStream = outstream;
    }

    @Override
    public void ensureFree(int bytes) throws IOException {

    }

    @Override
    public byte[] getBuffer() {
        return out.getBytez();
    }

    @Override
    public void registerClass(Class possible) {

    }

    @Override
    public void writeClass(Class cl) {
       // already written in write tag
    }

    @Override
    public void writeClass(FSTClazzInfo clInf) {
        // already written in write tag
    }

    @Override
    public void writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo) {
        out.writeTag(subInfo.getField().getName());
    }
    
    @Override
    public boolean writeTag(byte tag, Object infoOrObject, long somValue, Object toWrite) throws IOException {
        switch (tag) {
            case FSTObjectOutput.HANDLE:
                out.writeTagHeader(MinBin.HANDLE);
                out.writeInt(MinBin.INT_32,somValue);
                return true;
            case FSTObjectOutput.NULL:
                out.writeTag(null);
                return true;
            case FSTObjectOutput.TYPED:
            case FSTObjectOutput.OBJECT:
                FSTClazzInfo clzInfo = (FSTClazzInfo) infoOrObject;
                if (clzInfo.getClazz() == String.class )
                    break;
                if (clzInfo.getClazz() == Double.class )
                    break;
                if (clzInfo.getClazz() == Float.class )
                    break;
                if (clzInfo.getClazz() == Byte.class )
                    break;
                if (clzInfo.getClazz() == Short.class )
                    break;
                if (clzInfo.getClazz() == Integer.class )
                    break;
                if (clzInfo.getClazz() == Long.class )
                    break;
                if (clzInfo.getClazz() == Character.class )
                    break;
                if (clzInfo.getClazz() == Boolean.class )
                    break;
//                if ( clzInfo.getClazz() == Byte.class || clzInfo.getClazz() == Short.class || clzInfo.getClazz() == Character.class ) {
//                    out.writeObject(toWrite);
//                    return true;
//                }
                if ( clzInfo.getSer()!=null ) {
                    out.writeTagHeader(MinBin.SEQUENCE);
                    out.writeTag(classToString(clzInfo.getClazz()));
                    out.writeIntPacked(-1); // END Marker required
                } else
                {
                    out.writeTagHeader(MinBin.OBJECT);
                    out.writeTag(classToString(clzInfo.getClazz()));
                    out.writeIntPacked(clzInfo.getFieldInfo().length);
                }
                break;
            case FSTObjectOutput.ONE_OF:
                throw new RuntimeException("not implemented");
            case FSTObjectOutput.STRING:
                break; // ignore, header created by calling writeUTF
            case FSTObjectOutput.BIG_BOOLEAN_FALSE:
                out.writeTag(Boolean.FALSE);
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_BOOLEAN_TRUE:
                out.writeTag(Boolean.TRUE);
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_LONG:
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_INT:
                break; // ignore, header created by writing int. FIXME: won't work
            case FSTObjectOutput.ARRAY:
                Class<?> clz = infoOrObject.getClass();
                Class<?> componentType = clz.getComponentType();
                if ( clz.isArray() && componentType.isPrimitive() )
                {
                    if ( componentType == double.class ) {
                        out.writeTagHeader(MinBin.SEQUENCE);
                        out.writeTag(classToString(clz));
                        int length = Array.getLength(infoOrObject);
                        out.writeIntPacked(length);
                        for ( int i = 0; i < length; i++ ) {
                            out.writeTag(Array.getDouble(infoOrObject,i));
                        }
                    } else if ( componentType == float.class ) {
                        out.writeTagHeader(MinBin.SEQUENCE);
                        out.writeTag(classToString(clz));
                        int length = Array.getLength(infoOrObject);
                        out.writeIntPacked(length);
                        for ( int i = 0; i < length; i++ ) {
                            out.writeTag(Array.getFloat(infoOrObject, i));
                        }
                    } else {
                        out.writeArray(infoOrObject, 0, Array.getLength(infoOrObject));
                    }
                    return true;
                } else {
                    out.writeTagHeader(MinBin.SEQUENCE);
                    out.writeTag(classToString(clz));
                }
                break;
            case FSTObjectOutput.ENUM:
                out.writeTagHeader(MinBin.SEQUENCE);
                boolean isEnumClass = toWrite.getClass().isEnum();
                Class c = toWrite.getClass();
                if (!isEnumClass) {
                    // weird stuff ..
                    while (c != null && !c.isEnum()) {
                        c = toWrite.getClass().getEnclosingClass();
                    }
                    if (c == null) {
                        throw new RuntimeException("Can't handle this enum: " + toWrite.getClass());
                    }
                    out.writeTag(classToString(c));
                } else {
                    out.writeTag(classToString(c));
                }
                out.writeIntPacked(1);
                out.writeObject(toWrite.toString());
                return true;
            default:
                throw new RuntimeException("unexpected tag "+tag);
        }
        return false;
    }

    protected String classToString(Class clz) {
        return conf.getCPNameForClass(clz);
    }

    public void externalEnd(FSTClazzInfo clz) {
        if ( clz == null || (clz.getSer() instanceof FSTCrossPlatformSerialzer && ((FSTCrossPlatformSerialzer) clz.getSer()).writeTupleEnd()) )
            out.writeTag(MinBin.END_MARKER);
    }

    @Override
    public boolean isWritingAttributes() {
        return true;
    }


    /**
     * Created with IntelliJ IDEA.
     * User: ruedi
     * Date: 12.11.12
     * Time: 03:13
     * To change this template use File | Settings | File Templates.
     */
    public static class Primitives implements Serializable {

        String hidden;

        public enum SpecialEnum {
            ONE() {
                public void run() {
                    System.out.println("One");
                }
            },
            TWO() {
                public void run() {
                    System.out.println("One");
                }
            },
            THREE() {
                public void run() {
                    System.out.println("One");
                }
            }
            ;

            public abstract void run();
            SpecialEnum() {};
        }

        public enum SampleEnum {
            None("","None",0),
            Complete("C","Complete",1),
            Complete_GiveUp_Allowed("D","Complete Give-Up Allowed",2),
            Complete_Position_Transaction_Allowed("E","Complete Position Transaction Allowed",3),
            Designated("G","Designated",4),
            Predesignated("P","Predesignated",5),
            Predesignated_GiveUp_Allowed("Q","Predesignated Give-Up Allowed",6),
            Predesignated_Position_Transaction_Allowed("R","Predesignated Position Transaction Allowed",7),
            GiveUp_Allowed("X","Give-Up Allowed",8),
            Position_Transaction_Allowed("Y","Position Transaction Allowed",9);

            String value;
            String stringRepresentation;
            int nativeEnumValue;

            SampleEnum(String value, String stringRepresentation, int nativeEnumValue)
            {
                this.value=value;
                this.stringRepresentation = stringRepresentation;
                this.nativeEnumValue = nativeEnumValue;
            }
        }

        char w = 234, x = 33344;
        byte y = -34, z = 126;
        short sh0 = 127;

        SpecialEnum specEn = SpecialEnum.TWO;

        int gg = -122;
        int zz = 99999;
        int ii = -23424;
        int jj = 0;
        int kk = Integer.MIN_VALUE;
        int hh = Integer.MAX_VALUE;

        long lll = 123;
        long mmm = 99999;

        double dq = 300.0;
        float t = 300.0f;

        boolean a0 = true;
        boolean a1 = false;
        boolean a2 = false;
        boolean a3 = true;

        Integer i0 = 1, i1 = 2, i3 = 23894, i4 = 238475638;
        Double  d1 = 2334234.0;
        Boolean bol1 = Boolean.TRUE;
        Boolean bol2 = new Boolean(false);

        Date date = new Date(1);
        Date date1 = new Date(2);

        SampleEnum en1 = SampleEnum.Predesignated_GiveUp_Allowed;
        EnumSet<SampleEnum> enSet = EnumSet.of(SampleEnum.Predesignated,SampleEnum.Complete);

        String st;

        String st1;
        String st2;
        String st3;
        String st4;
        String st5;
        String st6;
        String st7;

        StyleSheet on = null;
        URL on1 = null;
        File on2 = null;

// commented this and moved to FST test cases as this is not a performance test but a test for feature completeness
//    Object exceptions[] = {
//        null, new Exception("test"), new ArrayIndexOutOfBoundsException(), new RuntimeException(new IllegalArgumentException("Blub"))
//    };

        public Primitives() {
        }

        public Primitives(int num) {
            st = "String"+num+"äöü";
            st1 = "String1"+num;
            st2 = st+"1"+num;
            hidden = "Visible";
            st3 = "visible its a hurdle this may be its a hurdle "+num;
            st4 = "etwas deutsch läuft.. ";
            st5 = st+"1"+num;
            st6 = "Some english, text; fragment. "+num;
            st7 = st6+" paokasd 1";
// see comments above
//        try {
//            throw new IOException();
//        } catch (Exception ex) {
//            exceptions[0] = ex;
//        }
        }

    }

    public static void main(String arg[]) throws IOException, ClassNotFoundException {

        FSTConfiguration conf = FSTConfiguration.createCrossPlatformConfiguration();
        conf.registerCrossPlatformClassMapping( new String[][] {
                { "mixtest", Primitives.class.getName() },
                { "rect", Rectangle.class.getName() },
                { "dim", Dimension.class.getName() },
                { "dim[3]", Dimension[][][].class.getName() },
                { "dim[2]", Dimension[][].class.getName() },
                { "dim[1]", Dimension[].class.getName() },
                { "int[2]", int[][].class.getName() },
                { "int[3]", int[][][].class.getName() },
        } );
        FSTObjectOutput out = new FSTObjectOutput(conf);

        HashMap obj = new HashMap();
        ArrayList li = new ArrayList(); li.add("zero"); li.add("second");
        obj.put("x", li);
//        obj.put("in", new int[]{1,2,3,4});
//        obj.put("y", li);
        obj.put(4,"99999");

//        out.writeObject(obj);
        Primitives obj1 = new Primitives(13);
        out.writeObject(obj1);
//        out.writeObject(new int[][] {{99,98,97}, {77,76,75}});
        MBPrinter.printMessage(out.getBuffer(), System.out);

        FSTObjectInput fin = new FSTObjectInput(conf);
        fin.resetForReuseUseArray(out.getBuffer(),out.getWritten());
        Object deser = fin.readObject();
        System.out.println("");
        System.out.println("SIZE "+out.getWritten());

    }

    public boolean isPrimitiveArray(Object array, Class<?> componentType) {
        return componentType.isPrimitive() && array instanceof double[] == false && array instanceof float[] == false;
    }

    public boolean isTagMultiDimSubArrays() {
        return true;
    }

}
