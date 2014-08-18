package de.ruedigermoeller.heapofftest.structs.single;

import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.offheap.structs.FSTStructAllocator;

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
 * Time: 18:51
 * To change this template use File | Settings | File Templates.
 */
public class BasicPrimitives extends FSTStruct {

    protected boolean bool = true;
    protected byte a = -1;
    protected short b = 11111;
    protected char c = 22222;
    protected int d = 333333333;
    protected long e = 444444444444l;
    protected float f = 5555555555.55f;
    protected double g = 66666666666.66;

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public byte getA() {
        return a;
    }

    public void setA(byte a) {
        this.a = a;
    }

    public short getB() {
        return b;
    }

    public void setB(short b) {
        this.b = b;
    }

    public char getC() {
        return c;
    }

    public void setC(char c) {
        this.c = c;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public long getE() {
        return e;
    }

    public void setE(long e) {
        this.e = e;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    @Override
    public String toString() {
        return "TestData{" +
                ", bool=" + bool +
                ", a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                ", e=" + e +
                ", f=" + f +
                ", g=" + g +
                '}';
    }

    public static void main(String a[]) {

        FSTStructAllocator alloc = new FSTStructAllocator(50000);

        BasicPrimitives prim = new BasicPrimitives();
        BasicPrimitives off = alloc.newStruct(prim);
        check(prim, off);

        prim = alloc.newStruct(off);

        prim.setBool(false);
        off.setBool(false);
        check(prim, off);

        prim.setA((byte) 99);
        off.setA((byte) 99);
        check(prim, off);

        prim.setG(999999);
        off.setG(999999);
        check(prim, off);

        prim.setB((byte) 99);
        off.setB((byte) 99);
        check(prim, off);

        prim.setC('c');
        off.setC('c');
        check(prim, off);

        prim.setE(999999);
        off.setE(999999);
        check(prim, off);

        prim.setD(999999);
        off.setD(999999);
        check(prim, off);

        prim.setF(999999);
        off.setF(999999);
        check(prim, off);

    }

    private static void check(BasicPrimitives prim, BasicPrimitives off) {
        if ( !prim.toString().equals(off.toString()) ) {
            System.out.println("------------------------------------------------ ERROR");
        } else {
            System.out.println("success");
        }
    }

}
