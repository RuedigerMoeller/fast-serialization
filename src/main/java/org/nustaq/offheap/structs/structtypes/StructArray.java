package org.nustaq.offheap.structs.structtypes;

import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.offheap.structs.NoAssist;
import org.nustaq.offheap.structs.Templated;

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
 * Date: 30.06.13
 * Time: 01:49
 * To change this template use File | Settings | File Templates.
 */

/**
 * An array which elements are stored 'off heap'. Note that in current version (and documentation below) 'off-heap' means
 * that objects are stored in flat byte arrays. Those byte arrays are actually allocated on the regular java heap. This
 * is not exactly the definition of "off-heap" as this term usually means that raw 'Unsafe' memory is allocated.
 * However using some tweaks it might be possible to back FSTStructs by real off-heap in a furture version if the
 * need arises.
 *
 * @param <E>
 */
public class StructArray<E extends FSTStruct> extends FSTStruct {

    @Templated()
    protected Object[] elems = {null};
    transient protected E template;

    /**
     * initializes with a template. When off heaped, all elements are filled with a copy of that template.
     * Warning: if this is used, this class behaves different off-heap compared to on-heap. On Heap the template
     * element is ignored.
     *
     * @param size
     * @param template
     */
    @NoAssist
    public StructArray(int size, E template) {
        if ( size < 1 ) {
            throw new RuntimeException("minimum size is 1");
        }
        this.elems = new Object[size];
        elems[0] = template;
    }

    /**
     * this will be avaiable for top level struct arrays only, embedded structarrays
     * will return null;
     * @return
     */
    @NoAssist
    public E getTemplate() {
        return template;
    }

    /**
     * accessor to the elements
     * @param i
     * @return
     */
    protected Object elems(int i) {
        return elems[i];
    }

    /**
     * accessor to elements
     * @param i
     * @param val
     */
    protected void elems( int i, Object val ) {
        elems[i] = val;
    }

    /**
     * length of this struct array
     * @return
     */
    protected int elemsLen() {
        return elems.length;
    }

    /**
     * @return a volatile warpper object to the '0' element of the structs array. Need to call detach() to get a permanent
     * reference
     */
    protected Object elemsPointer() {
        return null; // generated
    }

    protected int elemsStructIndex() {
        return -1; // generated
    }

    @NoAssist
    public E get( int i ) {
        return (E) elems(i); // workaround javassist limit: no generics
    }

    @NoAssist
    public void set(int i, E value ) {
        elems(i,value); // workaround javassist limit: no generics
    }

    @NoAssist
    public int size() {
        return elemsLen();
    }

    @NoAssist
    public StructArrIterator<E> iterator() {
        return new StructArrIterator<E>();
    }

    @NoAssist
    public E createPointer(int index) {
        if ( ! isOffHeap() )
            throw new RuntimeException("must be offheap to call this");
        E res = (E) elemsPointer();
        res.___elementSize = res.getByteSize();
        res.___offset+=index*res.___elementSize;
        return res;
    }

    @Override
    public String toString() {
        return "StructArray{" +
                "elemSize=" + getStructElemSize() +
                ", size=" + size() +
                '}';
    }

    @NoAssist
    public int getStructElemSize() {
        if (isOffHeap())
            return ___bytes.getInt( elemsStructIndex()+8 );
        else
            return -1;
    }

    @NoAssist
    public int getStructElemClassId() {
        if (isOffHeap())
            return ___bytes.getInt( elemsStructIndex()+12 );
        else
            return -1;
    }

    /**
     * only avaiable at top level
     * @param currentIndex
     */
    public void clear(int currentIndex) {
        if ( template == null ) {
            throw new RuntimeException("not avaiable in embedded struct arrays. Use set(i,template) instead.");
        }
        set(currentIndex,template);
    }

    public void ___setTemplate(E template) {
        this.template = template;
    }

    public class StructArrIterator<T extends FSTStruct> implements Iterator<T> {

        T current;
        final long maxPos;
        final int eSiz;
        final Bytez bytes;
        boolean hasNextElem = true;

        StructArrIterator() {
            bytes = ___bytes;
            this.eSiz = getStructElemSize();
            current = (T) createPointer(0);
            current.___offset-=eSiz;
            maxPos = size()*eSiz + get(0).___offset;
            hasNextElem = current.___offset < maxPos;
        }

        @Override
        public final boolean hasNext() {
            return hasNextElem;
        }

        @Override
        public final T next() {
            current.___offset+=eSiz;
            hasNextElem = current.___offset+eSiz < maxPos;
            return current;
        }

        public final T next(final int offset) {
            current.___offset+=offset;
            hasNextElem = current.___offset+eSiz < maxPos;
            return current;
        }

        public int getElementSize() {
            return eSiz;
        }

        @Override
        public void remove() {
            throw new RuntimeException("unsupported operation");
        }
    }
}
