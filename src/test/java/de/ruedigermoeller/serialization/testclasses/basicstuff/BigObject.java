package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.testclasses.HasDescription;
import de.ruedigermoeller.serialization.testclasses.enterprise.ManyClasses;
import de.ruedigermoeller.serialization.testclasses.enterprise.Trader;
import de.ruedigermoeller.serialization.testclasses.jdkcompatibility.ExternalizableTest;

import java.io.Serializable;

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
 * Date: 05.10.13
 * Time: 00:40
 * To change this template use File | Settings | File Templates.
 */
public class BigObject implements Serializable, HasDescription {
//        runner.runAll(FrequentPrimitives.getArray(200));
//        runner.runAll(new StringPerformance());
//        runner.runAll(new FrequentCollections());
//        runner.runAll(new LargeNativeArrays());
//        runner.runAll("new Primitives(0).createPrimArray() jva ser bug");
//        runner.runAll(new PrimitiveArrays().createPrimArray());
//        runner.runAll(new CommonCollections());
//        runner.runAll(Trader.generateTrader(101, true));
//        runner.runAll(ManyClasses.getArray() );
//        runner.runAll(new ExternalizableTest());

    Object[] aLot = {
            FrequentPrimitives.getArray(10),
            "A",
            "B",
            "C",
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(),
            null,
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            "A",
            "B",
            "C",
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            null,
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(),
            null,
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            "A",
            "B",
            "C",
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            "A",
            "B",
            "C",
            FrequentPrimitives.getArray(10),
            null,
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            null,
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(),
            null,
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            null,
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            null,
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            null,
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            null,
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            null,            null,
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            null,
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            null,
            "A",
            "B",
            "C",
            "AA",
            "BA",
            "CA",
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            null,
            new StringPerformance(),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            null,
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(),
            "AA",
            "BA",
            "CA",
            new FrequentCollections(),
            new CommonCollections(),
            null,
            "new Primitives(0).createPrimArray() jva ser bug",
            new PrimitiveArrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
    };

    {
        for (int i = 0; i < aLot.length; i++) {
            Object o = aLot[i];
            if ( o == null )
                aLot[i] = aLot[i-1];
        }
    }

    @Override
    public String getDescription() {
        return "A bigger object graph consisting of some of the test objects at once";
    }
}
