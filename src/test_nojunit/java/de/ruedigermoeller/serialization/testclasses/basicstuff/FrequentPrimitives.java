package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.testclasses.HasDescription;

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

public class FrequentPrimitives implements Serializable, HasDescription {

    static String staticString = "Should not serialize this";
    final static String finalStaticString = "Should not serialize this. Should not serialize this. Should not serialize this. Should not serialize this. Should not serialize this.";

    public static FrequentPrimitives[] getArray(int siz) {
        FrequentPrimitives[] instance = new FrequentPrimitives[siz];
        for (int i = 0; i < instance.length; i++) {
            instance[i] = new FrequentPrimitives(i);
        }
        return instance;
    }

    public FrequentPrimitives()
    {
    }

    public FrequentPrimitives(int index) {
        // avoid benchmarking identity references instead of StringPerf
        str = "R.Moeller"+index;
        str1 = "R.Moeller1"+index;
    }

    private String str;
    private String str1;
    private boolean b0 = true;
    private boolean b1 = false;
    private boolean b2 = true;
    private int test1 = 123456;
    private int test2 = 234234;
    private int test3 = 456456;
    private int test4 = -234234344;
    private int test5 = -1;
    private int test6 = 0;
    private long l1 = -38457359987788345l;
    private long l2 = 0l;
    private double d = 122.33;

    @Override
    public String getDescription() {
        return "A array[10] of objects with a typical distribution of primitve fields: 2 short Strings, 3 boolean, 6 ints, 2 long, one double.";
    }
}
