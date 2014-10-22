package org.nustaq.serialization.minbin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

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
 * Date: 13.04.2014
 * Time: 15:36
 * To change this template use File | Settings | File Templates.
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
            values = new HashMap<>();
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
