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
import java.util.ArrayList;

/**
 * Date: 13.04.2014
 * Time: 15:36
 * same as MBObject but for sequences of objects.
 */
public class MBSequence implements Serializable {
    ArrayList content;
    Object typeInfo;

    public MBSequence(Object typeInfo) {
        this.typeInfo = typeInfo;
    }

    public int size() {
        return content == null ? 0 : content.size(); 
    }
    
    public Object get(int index) {
        if ( content == null )
            throw new IndexOutOfBoundsException("size "+size()+" index:"+index);
        return content.get(index);
    }
    
    public void set( int index, Object value ) {
        if ( content == null )
            throw new IndexOutOfBoundsException("size "+size()+" index:"+index);
        content.set(index,value);
    }
    
    public MBSequence add( Object ... o ) {
        if (content == null) {
            content = new ArrayList();
        }
        if ( o == null ) {
            o = new Object[] { null };
        }
        for (int i = 0; i < o.length; i++) {
            content.add(o[i]);
        }
        return this;
    }

    public Object getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(Object typeInfo) {
        this.typeInfo = typeInfo;
    }
}
