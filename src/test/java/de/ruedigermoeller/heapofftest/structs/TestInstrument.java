package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.Align;
import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.Templated;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;

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
 * Date: 10.07.13
 * Time: 01:24
 * To change this template use File | Settings | File Templates.
 */
public class TestInstrument extends FSTStruct {

    public static TestInstrument createInstrumentTemplate() {
        // build template
        TestInstrument ti = new TestInstrument();
        // max 4 legs
        ti.legs = new TestInstrumentLeg[] { new TestInstrumentLeg(), null, null, null };
        return ti;
    }

    public static TestInstrument createInstrumentTemplateOnHeap() {
        // build template
        TestInstrument ti = new TestInstrument();
        // max 4 legs
        ti.legs = new TestInstrumentLeg[4];
        return ti;
    }

    @Align(8)
    protected long instrId;
    protected StructString mnemonic = new StructString(9);
    protected StructString description = new StructString(10);
    protected TestMarket market = new TestMarket();
    @Templated() @Align(4)
    protected int numLegs;
    @Templated()
    public TestInstrumentLeg[] legs = null;

    public StructString getMnemonic() {
        return mnemonic;
    }

    public TestMarket getMarket() {
        return market;
    }

    public TestInstrumentLeg legs(int i) {
        return legs[i];
    }

    public long getInstrId() {
        return instrId;
    }

    public void setInstrId(long instrId) {
        this.instrId = instrId;
    }

    public int getNumLegs() {
        return numLegs;
    }

    public void setNumLegs(int numLegs) {
        this.numLegs = numLegs;
    }

    public void addLeg( TestInstrumentLeg leg ) {
        if ( leg.getInstrument().getNumLegs() > 0 )
            throw new RuntimeException("cannot nest strategy instruments");
        legs(numLegs++, leg);
    }

    public void legs(int i, TestInstrumentLeg leg) {
        legs[i] = leg;
    }

    public int getAccumulatedQty() {
        int result = 1;
        int maxIter = numLegs;
        for ( int i = 0; i < maxIter; i++ ) {
            result+=legs(i).getLegQty();
        }
        return result;
    }

    public int getAccumulatedQtyOff() {
        int result = 1;
        final int maxIter = numLegs;
        if (maxIter==0) {
            return result;
        }
        final TestInstrumentLeg lp = legs(0);
        final int siz = lp.getByteSize();
        for ( int i = 0; i < maxIter; i++ ) {
            result+=lp.getLegQty();
            lp.next(siz);
        }
        return result;
    }

    public StructString getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "TestInstrument{" +
                "instrId=" + instrId +
                ", mnemonic=" + mnemonic +
                ", description=" + description +
                ", market=" + market +
                ", numLegs=" + numLegs +
                '}';
    }
}
