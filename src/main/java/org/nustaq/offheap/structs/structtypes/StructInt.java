/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nustaq.offheap.structs.structtypes;

import org.nustaq.offheap.structs.FSTStruct;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 24.07.13
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class StructInt extends FSTStruct {

    protected int value;

    public StructInt(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public void set(int val) {
        value = val;
    }

    public boolean equals( Object o ) {
        if ( o instanceof StructInt ) {
            return ((StructInt) o).get() == value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value;
    }

}
