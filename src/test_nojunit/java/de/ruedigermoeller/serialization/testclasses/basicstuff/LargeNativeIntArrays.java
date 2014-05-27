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

public class LargeNativeIntArrays implements Serializable, HasDescription {
    @Override
    public String getDescription() {
        return "measures performance serializing a large int array, a large long array and a large double array filled with random values. Note that random values destroy any value compression, so the increased size of kryo in this test will not be observable in most real world data.";
    }

    public LargeNativeIntArrays() {
    }
    
    int ints[];
    long longs[];
    double doubles[];
    public LargeNativeIntArrays(int dummy) {
        int N = 1300;
        ints = new int[N*2];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = (int) (Math.random()*Integer.MAX_VALUE*2-Integer.MAX_VALUE);
        }
        longs = new long[N];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = (long) (Math.random()*Long.MAX_VALUE*2-Long.MAX_VALUE);
        }
        doubles = new double[N];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = Math.random()*Double.MAX_VALUE*2-Double.MAX_VALUE;
        }
    }
}
