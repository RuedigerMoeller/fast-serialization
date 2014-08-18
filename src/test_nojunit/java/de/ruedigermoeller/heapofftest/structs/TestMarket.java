package de.ruedigermoeller.heapofftest.structs;

import org.nustaq.offheap.structs.FSTStruct;
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
 * Date: 10.07.13
 * Time: 01:26
 * To change this template use File | Settings | File Templates.
 */
public class TestMarket extends FSTStruct {

    protected StructString mnemonic = new StructString(5);
    protected TestDate opens = new TestDate(0);
    protected TestDate closes = new TestDate(0);
    protected TestTimeZone zone = new TestTimeZone();

    public StructString getMnemonic() {
        return mnemonic;
    }

    public TestDate getOpens() {
        return opens;
    }

    public TestDate getCloses() {
        return closes;
    }

    @Override
    public String toString() {
        return "TestMarket{" +
                "mnemonic=" + mnemonic +
                ", opens=" + opens +
                ", closes=" + closes +
                ", zone=" + zone +
                '}';
    }
}
