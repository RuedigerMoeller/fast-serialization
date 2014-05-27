package de.ruedigermoeller.serialization.testclasses.enterprise;


import java.io.*;

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

public class ObjectOrientedInt implements Serializable {

    int value;

    public ObjectOrientedInt() {}

    public ObjectOrientedInt(int value) {
        this.value = value;
    }

    public boolean equals( Object o ) {
        if ( o instanceof ObjectOrientedInt) {
            ObjectOrientedInt dt = (ObjectOrientedInt) o;
            return dt.value == value;
        }
        return super.equals(o);
    }

    public int hashCode() {
        return value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
