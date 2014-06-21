package org.nustaq.heapoff.structs.structtypes;

import org.nustaq.heapoff.structs.FSTArrayElementSizeCalculator;
import org.nustaq.heapoff.structs.FSTStruct;
import org.nustaq.heapoff.structs.NoAssist;
import org.nustaq.heapoff.structs.unsafeimpl.FSTStructFactory;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

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
 * Date: 29.06.13
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */

/**
 * a simple open addressed hashmap, which allows read access when embedded in structs. Note it is fixed size.
 * @param <K>
 * @param <V>
 */
public class StructMap<K extends FSTStruct,V  extends FSTStruct> extends FSTStruct implements FSTArrayElementSizeCalculator {

    transient FSTStruct keyTemplate, valueTemplate;
    protected transient FSTStruct pointer;

    protected FSTStruct keys[];
    protected FSTStruct vals[];
    protected int    size;

    public StructMap( FSTStruct keyTemplate, FSTStruct valueTemplate, int numElems ) {
        init(keyTemplate, valueTemplate, numElems);
    }

    @NoAssist
    protected void init(FSTStruct keyTemplate, FSTStruct valueTemplate, int numElems) {
        numElems = Math.max(3, numElems);
        keys    = new FSTStruct[numElems*2];
        vals = new FSTStruct[numElems*2];
        this.keyTemplate = keyTemplate;
        this.valueTemplate = valueTemplate;
    }

    public StructMap( FSTStruct keyTemplate, FSTStruct valueTemplate, Map<K,V> elems ) {
        this(keyTemplate,valueTemplate,elems.size());
        for (Iterator iterator = elems.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry next = (Map.Entry) iterator.next();
            put((K)next.getKey(),(V)next.getValue());
        }
    }

    protected int keysStructIndex() {
        return -1; // generated
    }

    protected int valsStructIndex() {
        return -1; // generated
    }

    protected int locateIndex(Object key)
    {
        if (size >= getCapacity()-1)
        {
            throw new RuntimeException("Map is full");
        }
        if ( pointer != null /*?? special for instantiation ?*/ || isOffHeap() ) {
            long arrbase = ___offset+keysStructIndex();
            int kvlen = ___bytes.getInt(arrbase+4);
            int kelemsiz = ___bytes.getInt(arrbase+8);
            if ( pointer == null ) {
                pointer = ___fac.createStructPointer(___bytes,0,___bytes.getInt(arrbase+12) );
            }

            int pos = ((key.hashCode() & 0x7FFFFFFF) % kvlen);
            pointer.___offset = ___offset+___bytes.getInt(arrbase)+pos*kelemsiz;
            while ( pointer.getInt() > 0 )
            {
                if (key.equals(pointer))
                    break;
                pos++;
                pointer.next(kelemsiz);
                if ( pos >= kvlen ) {
                    pos = 0;
                    pointer.___offset = ___offset+___bytes.getInt(arrbase);
                }
            }
            return pos;
        } else {
            int kvlen = keysLen();
            int pos = ((key.hashCode() & 0x7FFFFFFF) % kvlen);
            Object o = keys(pos);
            while ( o != null )
            {
                if (key.equals(o))
                    break;
                pos++;
                if ( pos >= kvlen )
                    pos = 0;
                o = keys(pos);
            }
            return pos;
        }
    }

    public int size()
    {
        return size;
    }

    public V get(Object key)
    {
        int    pos = locateIndex(key);
        Object res = keys(pos) != null ? vals(pos) : null;
        return (V) res;
    }

    public V put(K key, V value)
    {
        if ( key == null ) {
            throw new RuntimeException("Illegal Argument key is null");
        }
        if ( value == null ) {
            throw new RuntimeException("Illegal Argument value is null");
        }
        Object tmp = null;
        if (key != null)
        {
            int     pos    = locateIndex(key);
            if ( keys(pos) == null )
                size++;

            tmp = vals(pos);
            setKeyValue(pos, key, value);
        }
        return (V) tmp;
    }

    protected void setKeyValue(int i, K key, V value)
    {
        keys(i, key);
        vals(i, value);
    }

    public int getCapacity()
    {
        return keysLen();
    }

    public Object keys(int i) {
        return keys[i];
    }

    public Object vals(int i) {
        return vals[i];
    }

    public void keys(int i, FSTStruct v) {
        keys[i] = v;
    }

    public void vals(int i, FSTStruct v) {
        vals[i] = v;
    }

    public int keysLen() {
        return keys.length;
    }

    public int valsLen() {
        return vals.length;
    }

    @Override
    public int getElementSize(Field arrayRef, FSTStructFactory fac) {
        if ( keyTemplate != null && "keys".equals(arrayRef.getName()) ) {
            return fac.align(fac.calcStructSize(keyTemplate),fac.SIZE_ALIGN);
        }
        if ( valueTemplate != null && "vals".equals(arrayRef.getName()) ) {
            return fac.align(fac.calcStructSize(valueTemplate),fac.SIZE_ALIGN);
        }
        return -1;
    }

    @Override
    public Class<? extends FSTStruct> getElementType(Field arrayRef, FSTStructFactory fac) {
        if ( keyTemplate != null && "keys".equals(arrayRef.getName()) ) {
            return keyTemplate.getClass();
        }
        if ( valueTemplate != null && "vals".equals(arrayRef.getName()) ) {
            return valueTemplate.getClass();
        }
        return null;
    }
}
