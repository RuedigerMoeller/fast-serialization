package com.ruedigermoeller.heapofftest.structs;

import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.offheap.structs.structtypes.StructString;

import java.util.TimeZone;

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
 * Time: 01:29
 * To change this template use File | Settings | File Templates.
 */
public class TestTimeZone extends FSTStruct {

    protected StructString zoneId = new StructString("GMT", 10);

    public StructString getZoneId() {
        return zoneId;
    }

    public void setZoneId(StructString zoneId) {
        this.zoneId = zoneId;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(zoneId.toString());
    }

}
