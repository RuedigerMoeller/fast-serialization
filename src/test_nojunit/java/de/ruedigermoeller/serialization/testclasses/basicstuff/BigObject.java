package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.testclasses.HasDescription;
import de.ruedigermoeller.serialization.testclasses.enterprise.ManyClasses;
import de.ruedigermoeller.serialization.testclasses.enterprise.Trader;
import de.ruedigermoeller.serialization.testclasses.jdkcompatibility.ExternalizableTest;

import java.io.Serializable;

/**
 Copyright [2014] Ruediger Moeller

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class BigObject implements Serializable, HasDescription {
//        runner.runAll(FrequentPrimitives.getArray(200));
//        runner.runAll(new StringPerformance(0));
//        runner.runAll(new FrequentCollections());
//        runner.runAll(new LargeNativeIntArrays());
//        runner.runAll("new Primitives(0).createPrimArray() jva ser bug");
//        runner.runAll(new Arrays().createPrimArray());
//        runner.runAll(new CommonCollections());
//        runner.runAll(Trader.generateTrader(101, true));
//        runner.runAll(ManyClasses.getArray() );
//        runner.runAll(new ExternalizableTest());

    Object[] aLot;

    public BigObject() {
    }

    // move init to constructor to avoid skewing results
    public BigObject(String dummy) {
        aLot = new Object[] {
            FrequentPrimitives.getArray(10),
            "A",
            "B",
            "C",
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(0),
            null,
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            "A",
            "B",
            "C",
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            null,
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(0),
            null,
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            "A",
            "B",
            "C",
            new Arrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            "A",
            "B",
            "C",
            FrequentPrimitives.getArray(10),
            null,
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            Trader.generateTrader(13, true),
            null,
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(0),
            null,
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            null,
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            null,
            new Arrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            null,
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            null,
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
            null,
            FrequentPrimitives.getArray(10),
            null,            null,
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            null,
            new Arrays(),
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
            new StringPerformance(0),
            new FrequentCollections(),
            new CommonCollections(),
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            Trader.generateTrader(13, true),
            null,
            new ManyClasses(123),
            new ExternalizableTest(),
            FrequentPrimitives.getArray(10),
            new StringPerformance(0),
            "AA",
            "BA",
            "CA",
            new FrequentCollections(),
            new CommonCollections(),
            null,
            "new Primitives(0).createPrimArray() jva ser bug",
            new Arrays(),
            Trader.generateTrader(13, true),
            new ManyClasses(123),
            new ExternalizableTest(),
        };

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
