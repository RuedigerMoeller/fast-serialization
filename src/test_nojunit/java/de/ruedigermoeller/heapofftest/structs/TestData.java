package de.ruedigermoeller.heapofftest.structs;

import org.nustaq.offheap.structs.Align;
import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.offheap.structs.Templated;
import org.nustaq.offheap.structs.structtypes.StructArray;
import org.nustaq.offheap.structs.structtypes.StructInt;
import org.nustaq.offheap.structs.structtypes.StructString;

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
 * Date: 07.07.13
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class TestData extends FSTStruct {

    protected boolean bool;
    protected byte a = -1;
    protected short b = 11111;
    protected char c = 22222;
    protected int d = 333333333;
    protected long e = 444444444444l;
    protected float f = 5555555555.55f;
    protected double g = 66666666666.66;

    @Override
    public String toString() {
        return "TestData{" +
                ", a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                ", e=" + e +
                ", f=" + f +
                ", g=" + g +
                ", string=" + getString() +
                '}';
    }

    protected StructString string = new StructString("Hallo",50);
    protected StructArray<TestData> dataStructArray;
    protected TestData nested;

    public byte getA() {
        return a;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public short getB() {
        return b;
    }

    public char getC() {
        return c;
    }

    public int getD() {
        return d;
    }

    public long getE() {
        return e;
    }

    public float getF() {
        return f;
    }

    public double getG() {
        return g;
    }

    protected byte[] arrA = "blablablablabla".getBytes();
    protected short[] arrb = { 11111, 22222, 3333 };
    protected char[] arrc = { 22221, 22221, 22222, 1 };
    protected int[] arrd = { 33333331, 33333332, 33333333, 1, 1 } ;
    protected long[] arre = { 444444444441l, 444444444442l, 444444444443l, 1,1 ,1 };
    protected float[] arrf = { 5555555555.51f, 5555555555.52f, 5555555555.53f, 1,1,1,1 };
    protected double[] arrg = { 66666666666.61, 66666666666.62,66666666666.63,1,1,1,1,1};
    protected boolean[] arrBool = {true,false,true,false};
    @Align(8)
    protected FSTStruct objArray[] = new FSTStruct[] { null, new StructString(5), null, new StructString(20), new StructInt(17), null };
    @Templated protected FSTStruct templatedObjArray[] = new FSTStruct[] { new StructString("Oh",5), null, null, null };
    protected StructString typedArray[] = new StructString[] { null, new StructString("One"), new StructString("Two"), new StructString("3", 10), new StructString("Four") };


    public boolean arrBool(int index) { return arrBool[index]; }
    public byte arrA(int index) { return arrA[index]; }
    public short arrb(int index) { return arrb[index]; }
    public char arrc(int index) { return arrc[index]; }
    public int arrd(int index) { return arrd[index]; }
    public long arre(int index) { return arre[index]; }
    public float arrf(int index) { return arrf[index]; }
    public double arrg(int index) { return arrg[index]; }
    public FSTStruct objArray(int index) { return objArray[index]; }
    public FSTStruct templatedObjArray(int index) { return templatedObjArray[index]; }
    public StructString typedArray(int index) { return typedArray[index]; }

    public void arrBool(int index, boolean val) { arrBool[index] = val; }
    public void arrA(int index, byte val) { arrA[index] = val; }
    public void arrb(int index, short val ) { arrb[index] = val; }
    public void arrc(int index, char val ) { arrc[index] = val; }
    public void arrd(int index, int val) { arrd[index] = val; }
    public void arre(int index, long val) { arre[index] = val; }
    public void arrf(int index, float val) { arrf[index] = val; }
    public void arrg(int index, double val ) { arrg[index] = val; }
    public void objArray(int index, FSTStruct val) { objArray[index] = val; }
    public void templatedObjArray(int index, FSTStruct val) { templatedObjArray[index] = val; }
    public void typedArray(int index, StructString val) { typedArray[index] = val; }

    public int arrALen() { return arrA.length; }
    public int arrbLen() { return arrb.length; }
    public int arrcLen() { return arrc.length; }
    public int arrdLen() { return arrd.length; }
    public int arreLen() { return arre.length; }
    public int arrfLen() { return arrf.length; }
    public int arrgLen() { return arrg.length; }
    public int objArrayLen() { return objArray.length; }
    public int templatedObjArrayLen() { return templatedObjArray.length; }
    public int typedArrayLen() { return typedArray.length; }

    // special methods
    public int objArrayElementSize() {
        return -1;
    }

    public int objArrayStructIndex() {
        return -1;
    }

    public int objArrayIndex() {
        return -1;
    }

    public FSTStruct objArrayPointer() {
        return null;
    }


    public StructArray<TestData> getDataStructArray() {
        return dataStructArray;
    }

    public StructString getString() {
        return string;
    }

    public void setString( StructString s ) {
        string = s;
    }

    public TestData getNested() {
        return nested;
    }

    public void setNested(TestData nested) {
        this.nested = nested;
    }

    public int arrBoolLen() {
        return arrBool.length;
    }
}

