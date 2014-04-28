package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.minbin.MBIn;
import de.ruedigermoeller.serialization.minbin.MBOut;
import de.ruedigermoeller.serialization.minbin.MBPrinter;
import de.ruedigermoeller.serialization.minbin.MinBin;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
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
//        out.writeString(cl.getName());
    }

    @Override
    public void writeClass(FSTClazzInfo clInf) {
        // already written in write tag
//        out.writeString(clInf.getClazz().getName());
    }

    @Override
    public void writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo) {
        out.writeTag(subInfo.getField().getName());
    }
    
    @Override
    public boolean writeTag(byte tag, Object infoOrObject, long somValue) throws IOException {
        switch (tag) {
            case FSTObjectOutput.NULL:
                out.writeTag(null);
                break;
            case FSTObjectOutput.TYPED:
            case FSTObjectOutput.OBJECT:
                if (((FSTClazzInfo)infoOrObject).getClazz() == String.class )
                    break;
                if (((FSTClazzInfo)infoOrObject).getClazz() == Double.class )
                    break;
                if (((FSTClazzInfo)infoOrObject).getClazz() == Float.class )
                    break;
                FSTClazzInfo clzInfo = (FSTClazzInfo) infoOrObject;
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
            case FSTObjectOutput.BIG_LONG:
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_INT:
                break; // ignore, header created by writing int. FIXME: won't work
            case FSTObjectOutput.BIG_BOOLEAN_FALSE:
                break; // ignore, header created by writing byte. FIXME: won't work
            case FSTObjectOutput.BIG_BOOLEAN_TRUE:
                break; // ignore, header created by writing byte. FIXME: won't work
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
                throw new RuntimeException("not supported");
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

    static class MixTester implements Serializable {
//        boolean x;
//        double dda[] = {1112323.342,11234,-11234,114234.3,11234443453.1};
//        double d = 2334534.223434;
//        String s = "Hallo";
//        Object strOb = "StrObj";
//        Integer bigInt = 234;
//        Object obs[] = { 34,55d };
//        int arr[] = {1,2,3,4,5,6};
//        ArrayList l = new ArrayList();
//        HashMap mp = new HashMap();
//        short sh = 34;
//        int in = 34234;
//        boolean y;
//        Dimension _da[] = {new Dimension(1,2),new Dimension(3,4)};
//        int iiii[][][] = new int[][][] { { {1,2,3}, {4,5,6} }, { {7,8,9}, {10,11,12} } };
        Object iii = new int[][] {{1,2,3},{4,5,6}};
//        Dimension dim[][][] = new Dimension[][][] {
//            {
//                { new Dimension(11,10) },
//                { new Dimension(9,10), new Dimension(1666661,11) }
//            }
//        };

        public MixTester() {
//            l.add("asdasd");
//            l.add(3425);
//            l.add(new Rectangle(1,2,3,4));
//            mp.put("name", 9999);
//            mp.put(349587, "number");
//            mp.put(3497, new Dimension[] {new Dimension(0,0), new Dimension(1,1)} );
        }
    }

    static class SimpleTest implements Serializable {
        //        boolean x;
        byte b = 10;
        byte ba[] = {1,2,3,4,5};
        char c = 'A';
        char ca[] = { 'a', 'b', 34534 };
        short s = 12323;
        short sa[] = {12323,234,-234,4234,23444};
        int i = 10;
        int ia[] = {112323,1234,-1234,14234,123444};
        long l = 2000l;
        long la[] = {1112323,11234,-11234,114234,11234443453l};
        double d = 13.0;
        double da[] = {1112323.342,11234,-11234,114234.3,11234443453.1};
        String ascii = "asdbdjfhsfwoewfhiwef";
        String fatString = "Rüdiger Möller";
        Double bigDA[] = {1d,23d,345d};
        Object obs[] = { 34, 32d };

    }

    public static void main(String arg[]) throws IOException, ClassNotFoundException {

        FSTConfiguration conf = FSTConfiguration.createCrossPlatformConfiguration();
        conf.registerCrossPlatformClassMapping( new String[][] {
                { "mixtest", MixTester.class.getName() },
                { "rect", Rectangle.class.getName() },
                { "dim", Dimension.class.getName() },
                { "dim[3]", Dimension[][][].class.getName() },
                { "dim[2]", Dimension[][].class.getName() },
                { "dim[1]", Dimension[].class.getName() },
                { "int[2]", int[][].class.getName() },
                { "int[3]", int[][][].class.getName() },
        } );
        FSTObjectOutput out = new FSTObjectOutput(conf);
        out.writeObject(new MixTester());
        MBPrinter.printMessage(out.getBuffer(), System.out);

        FSTObjectInput fin = new FSTObjectInput(conf);
        fin.resetForReuseUseArray(out.getBuffer(),out.getWritten());
        Object deser = fin.readObject();
        System.out.println("");
        System.out.println("SIZE "+out.getWritten());

    }


}
