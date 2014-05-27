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

public class ObjectOrientedDataType implements Serializable {

    // encapsulate like a boss ..

    private String aString="";
    private boolean isCapableOfDoingAnythingMeaningful;
    public ObjectOrientedDataType() {}

    public ObjectOrientedDataType(String valueString) {
        if ( valueString == null ) {
            isCapableOfDoingAnythingMeaningful = false;
            aString = "";
        } else {
            this.aString = valueString;
        }
    }

    public boolean equals( Object o ) {
        if ( o instanceof ObjectOrientedDataType) {
            ObjectOrientedDataType dt = (ObjectOrientedDataType) o;
            return dt.aString.equals(aString) && dt.isCapableOfDoingAnythingMeaningful == isCapableOfDoingAnythingMeaningful;
        }
        return super.equals(o);
    }

    public int hashCode() {
        return aString.hashCode();
    }

    public String toString() {
        return aString;
    }
}
