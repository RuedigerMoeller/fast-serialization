package org.nustaq.offheap.structs;

import org.nustaq.offheap.bytez.BasicBytez;
import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.malloc.MallocBytez;
import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.offheap.structs.unsafeimpl.FSTStructFactory;
import org.nustaq.serialization.util.FSTUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
 * Foundation, InFSTStrc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 01.07.13
 * Time: 20:20
 * To change this template use File | Settings | File Templates.
 */

/**
 * Base class of all structs. Inherit this to define your own structs.
 * Refer to the documentation in the wiki regarding limitations of struct classes/members
 */
public class FSTStruct implements Serializable {

    transient public long ___offset;
    transient public Bytez ___bytes;
    transient public FSTStructFactory ___fac;
    transient public int ___elementSize;
    transient public FSTStructChange tracker;

    /**
     * must include bytearray offset in case of unsafe use
     *
     * @param off
     */
    protected void setAbsoluteOffset(long off) {
        ___offset = off;
    }

    /**
     * @return internal offset, use getIndexInBase to get the start index of the struct within the base byte array
     */
    protected long getAbsoluteOffset() {
        return ___offset;
    }

    /**
     * @return the start index of this struct within the underlying byte array. getByteSize returns len.
     * In case of offheap, the address of the first valid byte is returned
     */
    public long getOffset() {
        return (___offset);
    }

    /**
     * @deprecated use getOffset
     * see getOffset
     */
    public long getIndexInBase() {
        return (___offset);
    }

    /**
     * @return the number of bytes used by the struct pointed to
     */
    public int getByteSize() {
        if (!isOffHeap()) {
            return 0;
        }
        return ___bytes.getInt(___offset);
    }

    public Class getPointedClass() {
        if (!isOffHeap())
            throw new RuntimeException("cannot call on heap");
        Class clazz = ___fac.getClazz(getClzId());
        if (clazz == null) {
            return FSTStruct.class;
        }
        return clazz;
    }

    public int getClzId() {
        if (!isOffHeap())
            throw new RuntimeException("cannot call on heap");
        return ___bytes.getInt(___offset + 4);
    }

    public boolean pointsToNull() {
        return getClzId() <= 0;
    }

    protected void addOffset(long off) {
        ___offset += off;
    }

    protected void setBase(Bytez base) {
        ___bytes = base;
    }

    /**
     * @return the underlying byte array.
     */
    public Bytez getBase() {
        return ___bytes;
    }

    public FSTStructFactory getFac() {
        return ___fac;
    }

    /**
     * set this struct pointer to base array at given offset (=bufoff+index)
     *
     * @param base
     * @param offset direct offset to byte array element (=FSTStruct.bufoff+array index)
     */
    public void baseOn(byte base[], long offset, FSTStructFactory fac) {
        ___bytes = new HeapBytez(base); ___offset = offset; ___fac = fac;
    }

    public void baseOn(Bytez base, long offset, FSTStructFactory fac) {
        ___bytes = base; ___offset = offset; ___fac = fac;
    }

    /**
     * set this struct pointer to base array at given index
     *
     * @param base
     * @param index
     */
    public void baseOn(byte base[], int index) {
        ___bytes = new HeapBytez(base); ___offset = index;
        if (___fac == null)
            ___fac = FSTStructFactory.getInstance();
    }

    public void baseOn(Bytez base, int index) {
        ___bytes = base; ___offset = index;
        if (___fac == null)
            ___fac = FSTStructFactory.getInstance();
    }

    public boolean isIdenticTo(FSTStruct other) {
        return other.getBase().equals(___bytes) && other.getAbsoluteOffset() == ___offset;
    }

    public int hashCode() {
        if (!isOffHeap())
            return onHeapHashcode();
        return ___bytes.getInt(___offset) ^ ___bytes.getInt(___offset + getByteSize() - 4);
    }

    public int onHeapHashcode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FSTStruct) {
            FSTStruct other = (FSTStruct) obj;
            final int len = getByteSize();
            if (other.getByteSize() == len) {
                long ix = getOffset();
                long ox = other.getOffset();
                for (int i = 0; i < len; i++) {
                    if (___bytes.get(i + ix) != other.___bytes.get(i + ox))
                        return false;
                }
                return true;
            } else
                return false;
        }
        return super.equals(obj);
    }

    public boolean isOffHeap() {
        return ___fac != null;
    }

    public int getElementInArraySize() {
        return ___elementSize;
    }

    public boolean isStructArrayPointer() {
        return ___elementSize > 0;
    }

    public boolean isNull() {
        return getClzId() <= 0;
    }

    /**
     * important: iterators, array access and access to substructures of a struct always return
     * pointers, which are cached per thread (one instance only!). To keep a pointer to a struct (e.g. search struct array and find an
     * element which should be kept as result) you need to detach it (removed from cache).
     * This method allocates a pointer.
     */
    public <T extends FSTStruct> T detach() {
        if (isOffHeap()) {
            ___fac.detach(this);
        }
        return (T) this;
    }

    /**
     * move offsets and memorybase to given pointer and return the pointer.
     * Eases dereferencing of nested structs without object creation.
     * If the given pointer is null, a new one will be created (alloc)
     * <pre>
     *   MyStruct st = FSTStructFactory.getInstance().createEmptyStructPointer(FSTStruct.class);
     *   otherStruct.getEmbeddedMyStruct().to(st);
     * </pre>
     */
    public <T extends FSTStruct> T detachTo(T pointer) {
        if ( pointer == null ) {
            return detach();
        }
        if (isOffHeap()) {
            pointer.___fac = ___fac;
            pointer.___bytes = ___bytes;
            pointer.___elementSize = ___elementSize;
            pointer.___offset = ___offset;
            return pointer;
        }
        return (T) this;
    }

    /**
     * Warning: no bounds checking. Moving the pointer outside the underlying Bytez will cause access violations
     */
    public final void next() {
        if (___elementSize > 0)
            ___offset += ___elementSize;
        else
            throw new RuntimeException("not pointing to a struct array");
    }

    /**
     * Warning: no bounds checking. Moving the pointer outside the underlying Bytez will cause access violations
     */
    public final void next(int offset) {
        ___offset += offset;
    }

    /**
     * Warning: no bounds checking. Moving the pointer outside the underlying Bytez will cause access violations
     */
    public final void previous() {
        if (___elementSize > 0)
            ___offset -= ___elementSize;
        else
            throw new RuntimeException("not pointing to a struct array");
    }

    public <T extends FSTStruct> T cast(Class<T> to) {
        int clzId = ___fac.getClzId(to);
        if (this.getClass().getSuperclass() == to)
            return (T) this;
        FSTStruct res = ___fac.createStructPointer(___bytes, (int) (___offset), clzId);
        res.___elementSize = ___elementSize;
        return (T) res;
    }

    /**
     * @return a volatile pointer of the exact type this points to
     */
    public <T extends FSTStruct> T cast() {
        int clzId = getClzId();
        if (___fac.getClazz(clzId) == getClass().getSuperclass())
            return (T) this;
        FSTStruct res = ___fac.getStructPointerByOffset(___bytes, ___offset);
        res.___elementSize = ___elementSize;
        return (T) res;
    }

    public byte getByte() {
        return ___bytes.get(___offset);
    }

    public void setByte(byte i) {
        ___bytes.put(___offset, i);
    }

    public char getChar() {
        return ___bytes.getChar(___offset);
    }

    public short getShort() {
        return ___bytes.getShort(___offset);
    }

    public void setShort(short i) {
        ___bytes.putShort(___offset, i);
    }

    public int getInt() {
        return ___bytes.getInt(___offset);
    }

    public void setInt(int i) {
        ___bytes.putInt(___offset, i);
    }

    public long getLong() {
        return ___bytes.getLong(___offset);
    }

    public void setLong(long i) {
        ___bytes.putLong(___offset, i);
    }

    public float getFloat() {
        return ___bytes.getFloat(___offset);
    }

    public double getDouble() {
        return ___bytes.getDouble(___offset);
    }

    public void getBytes(byte[] target, int startIndexInTarget, int len) {
        if (!isOffHeap()) {
            throw new RuntimeException("must be offheap to call this");
        }
        ___bytes.getArr(___offset, target, startIndexInTarget, len);
    }

    public void getBytes(Bytez target, int startIndexInTarget) {
        if (!isOffHeap()) {
            throw new RuntimeException("must be offheap to call this");
        }
        if (target.length() < startIndexInTarget + getByteSize()) {
            throw new RuntimeException("ArrayIndexOutofBounds byte len:" + target.length() + " start+size:" + (startIndexInTarget + getByteSize()));
        }
//        unsafe.copyMemory(___bytes,___offset, target, bufoff+startIndexInTarget, getByteSize());
        ___bytes.copyTo(target, startIndexInTarget, ___offset, getByteSize());
    }

    public void setBytes(byte[] source, int sourceIndex, int len) {
        if (!isOffHeap()) {
            throw new RuntimeException("must be offheap to call this");
        }
        ___bytes.set(___offset, source, sourceIndex, len);
    }

    public void setBytes(ByteSource source, long sourceIndex, int len) {
        if (!isOffHeap()) {
            throw new RuntimeException("must be offheap to call this");
        }
        if (source instanceof BasicBytez) {
            ((BasicBytez) source).copyTo(___bytes, ___offset, sourceIndex, len);
        } else {
            for (long i = 0; i < len; i++)
                ___bytes.put(___offset + i, source.get(i + sourceIndex));
        }
    }

    public void setBytes(Bytez source, long sourceIndex, int len) {
        if (!isOffHeap()) {
            throw new RuntimeException("must be offheap to call this");
        }
//        unsafe.copyMemory(source, bufoff+sourceIndex, ___bytes, ___offset, len);
        source.copyTo(___bytes, ___offset, sourceIndex, len);
    }

    /**
     * returns a complete copy of this object allocating a new Bytez capable of holding the data.
     *
     * @return
     */
    public FSTStruct createCopy() {
        if (!isOffHeap()) {
            throw new RuntimeException("must be offheap to call this");
        }
        byte b[] = new byte[getByteSize()];
        HeapBytez res = new HeapBytez(b);
        getBytes(res, 0);
        return ___fac.createStructWrapper(res, 0);
    }

    /**
     * works only if change tracking is enabled
     */
    public FSTStruct startChangeTracking() {
        tracker = new FSTStructChange();
        return this;
    }

    /**
     * works only if change tracking is enabled
     */
    public FSTStructChange finishChangeTracking() {
        tracker.snapshotChanges((int) getOffset(), getBase());
        FSTStructChange res = tracker;
        tracker = null;
        return res;
    }

    public boolean isChangeTracking() {
        return tracker != null;
    }

    public FSTStruct toOffHeap() {
        if (isOffHeap())
            return this;
        return FSTStructFactory.getInstance().toStruct(this);
    }

    //////////////////////////////////////////////
    // helper to support stringy web protocols

    public Object getFieldValues() {
        throw new RuntimeException("only supported for structs");
    }

    //////////////////////////////////////////////
    // support for copyFree ByteBuffer conversion

    static Field address = null;
    static Field capacity = null;

    static ThreadLocal<ByteBuffer> tmpBuf = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {
            List<Field> fields = new ArrayList<>();
            ByteBuffer tmpSend = ByteBuffer.allocateDirect(0);
            FSTUtil.getAllFields( fields, tmpSend.getClass() );
            if ( address == null || capacity == null ) {
                for (int i = 0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    if ( field.getName().equals("address") ) {
                        address = field;
                    } else if ( field.getName().equals("capacity") ) {
                        capacity = field;
                    }
                }
                address.setAccessible(true);
                capacity.setAccessible(true);
            }
            return tmpSend;
        }
    };

    /**
     * @return a temporary per thread instance of ByteBuffer pointing to this structs data. The length of the
     * buffer is same as the length of this struct.
     *
     * The instance returned is valid until the currentThread calls this method next time.
     *
     * Can be used to feed e.g. NIO api. Use ByteBuffer.duplicate() to obtain a non-temporary instance
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public ByteBuffer asByteBufferTemporary() {
        if ( getBase() instanceof MallocBytez ) {
            MallocBytez base = (MallocBytez) getBase();
            ByteBuffer bb = tmpBuf.get();
            try {
                address.setLong(bb, base.getBaseAdress() + getOffset());
                capacity.setInt( bb, getByteSize());
            } catch (IllegalAccessException e) {
                throw FSTUtil.rethrow(e);
            }
            bb.limit((int) (getOffset() + getByteSize()));
            bb.position((int) getOffset());
            return tmpBuf.get();
        } else { // assume HeapBytez. Allocates
            return ByteBuffer.wrap(getBase().asByteArray(), (int) getOffset(), getByteSize());
        }
    }

}
