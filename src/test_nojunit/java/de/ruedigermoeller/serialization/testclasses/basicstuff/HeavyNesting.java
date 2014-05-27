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

public class HeavyNesting implements Serializable, HasDescription {

    public static HeavyNesting createNestedObject( int count ) {
        HeavyNesting cur = new HeavyNesting();
        HeavyNesting res = cur;
        for ( int i = 0; i<count;i++ ) {
            cur.next = new HeavyNesting(i);
            cur = cur.next;
        }
        return res;
    }

    HeavyNesting next;
    int count;

    public HeavyNesting() {
    }

    public HeavyNesting(int c) {
        this.count = c;
    }

    @Override
    public String getDescription() {
        return "Heavily nested Objects";
    }
}
