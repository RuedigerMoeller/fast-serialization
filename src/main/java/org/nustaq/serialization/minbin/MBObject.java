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
package org.nustaq.serialization.minbin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Date: 13.04.2014
 * Time: 15:36
 *
 * Generic object used in case a reading application does not know about the classes
 * used by the writing application
 */
public class MBObject implements Serializable {
    static Iterator<String> emptyIter = new Iterator<String>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public String next() {
            return null;
        }

        @Override
        public void remove() {

        }
    };

    HashMap<String,Object> values;
    Object typeInfo;

    public MBObject(Object typeInfo) {
        this.typeInfo = typeInfo;
    }

    public Object getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(Object typeInfo) {
        this.typeInfo = typeInfo;
    }

    public Object get(String key) {
        if ( values != null )
            return values.get(key);
        return null;
    }

    public MBObject put(String key, Object val) {
        if ( values == null )
            values = new HashMap<String,Object>();
        values.put(key,val);
        return this;
    }
    
    public int size() {
        return values == null ? 0 : values.size();
    }
    
    public Iterator<String> keyIterator() {
        if ( values == null ) {
            return emptyIter;
        }
        return values.keySet().iterator();
    }
}
