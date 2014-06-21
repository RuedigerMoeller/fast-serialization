package de.ruedigermoeller.heapofftest.structs;

import org.nustaq.heapoff.structs.FSTStruct;

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
 * Time: 01:38
 * To change this template use File | Settings | File Templates.
 */
public class TestInstrumentLeg extends FSTStruct {
    protected TestInstrument instrument = new TestInstrument();
    protected int legQty = 1;

    public TestInstrumentLeg() {
        this.instrument = instrument;
        this.legQty = legQty;
    }

    public TestInstrument getInstrument() {
        return instrument;
    }

    public int getLegQty() {
        return legQty;
    }

    public void setLegQty(int legQty) {
        this.legQty = legQty;
    }
}
