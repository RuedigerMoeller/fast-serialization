package de.ruedigermoeller.heapofftest.structs.single;

import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.FSTStructAllocator;

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
 * Date: 16.11.13
 * Time: 19:54
 * To change this template use File | Settings | File Templates.
 */
public class BasicArrays extends FSTStruct {

    protected byte[] arrA = "blablablablabla".getBytes();
    protected short[] arrb = { 11, 22, 33 };
    protected char[] arrc = { 22221, 22221, 22222, 1 };
    protected int[] arrd = { 33333331, 33333332, 33333333, 1, 1 } ;
    protected long[] arre = { 444444444441l, 444444444442l, 444444444443l, 1,1 ,1 };
    protected float[] arrf = { 5555555555.51f, 5555555555.52f, 5555555555.53f, 1,1,1,1 };
    protected double[] arrg = { 66666666666.61, 66666666666.62,66666666666.63,1,1,1,1,1};
    protected boolean[] arrBool = {false,true,true,false};

    public boolean arrBool(int index) { return arrBool[index]; }
    public byte arrA(int index) { return arrA[index]; }
    public short arrb(int index) { return arrb[index]; }
    public char arrc(int index) { return arrc[index]; }
    public int arrd(int index) { return arrd[index]; }
    public long arre(int index) { return arre[index]; }
    public float arrf(int index) { return arrf[index]; }
    public double arrg(int index) { return arrg[index]; }

    public void arrBool(int index, boolean val) { arrBool[index] = val; }
    public void arrA(int index, byte val) { arrA[index] = val; }
    public void arrb(int index, short val ) { arrb[index] = val; }
    public void arrc(int index, char val ) { arrc[index] = val; }
    public void arrd(int index, int val) { arrd[index] = val; }
    public void arre(int index, long val) { arre[index] = val; }
    public void arrf(int index, float val) { arrf[index] = val; }
    public void arrg(int index, double val ) { arrg[index] = val; }

    public int arrBoolLen() { return arrBool.length; }
    public int arrALen() { return arrA.length; }
    public int arrbLen() { return arrb.length; }
    public int arrcLen() { return arrc.length; }
    public int arrdLen() { return arrd.length; }
    public int arreLen() { return arre.length; }
    public int arrfLen() { return arrf.length; }
    public int arrgLen() { return arrg.length; }

    public String toString() { return arrb(0)+","+arrb(1)+","+arrb(2); }

    private static void compareTestData(BasicArrays data, BasicArrays data1) {

        for ( int i = 0; i < data.arrbLen(); i++ ) {
            if ( data1.arrb(i) != data.arrb(i) ) {
                throw new RuntimeException("at elem "+i);
            }
        }
        for ( int i = 0; i < data.arrcLen(); i++ ) {
            if ( data1.arrc(i) != data.arrc(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrdLen(); i++ ) {
            if ( data1.arrd(i) != data.arrd(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrALen(); i++ ) {
            if ( data1.arrA(i) != data.arrA(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrgLen(); i++ ) {
            if ( data1.arrg(i) != data.arrg(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrBoolLen(); i++ ) {
            if ( data1.arrBool(i) != data.arrBool(i) ) {
                for ( int ii = 0; ii < data.arrBoolLen(); ii++ ) {
                    System.out.println(""+ii+" "+data.arrBool(ii));
                }
                for ( int ii = 0; ii < data.arrBoolLen(); ii++ ) {
                    System.out.println(""+ii+" "+data1.arrBool(ii));
                }
                System.out.println("0:"+data+" 1:"+data1);
                throw new RuntimeException("len "+data.arrBoolLen());
            }
        }
        System.out.println("success");
    }

    public static void main(String a[]) {

        FSTStructAllocator alloc = new FSTStructAllocator(50000);

        BasicArrays prim = new BasicArrays();
        BasicArrays off = alloc.newStruct(prim);
        BasicArrays off1 = alloc.newStruct(off);

        compareTestData(prim, off);
        compareTestData(prim, off1);
    }

}
