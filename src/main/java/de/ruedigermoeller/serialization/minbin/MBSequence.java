package de.ruedigermoeller.serialization.minbin;

import java.util.ArrayList;

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
public class MBSequence {
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
