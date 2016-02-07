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
package org.nustaq.offheap.structs;

import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.BytezAllocator;
import org.nustaq.offheap.bytez.malloc.MallocBytezAllocator;
import org.nustaq.offheap.bytez.onheap.HeapBytezAllocator;
import org.nustaq.offheap.structs.structtypes.StructArray;
import org.nustaq.offheap.structs.unsafeimpl.FSTStructFactory;
import org.nustaq.serialization.util.FSTUtil;

/**
 * Date: 04.10.13
 * Time: 20:51
 * implemenst struct instances. By default heap is used to obtain underlying byte arrays.
 * By supplementing a BytezAllocator, structs can also be created offheap.
 */
public class FSTStructAllocator {
    public static boolean DUMP_ALLOC = false;
    protected int chunkSize;
    protected Bytez chunk;
    protected int chunkIndex;
    BytezAllocator alloc = new HeapBytezAllocator();
    int chunkObjCount = 0;
//    BytezAllocator alloc = new MallocBytezAllocator();

    protected FSTStructAllocator() {

    }

    /**
     * @param b
     * @param index
     * @return a new allocated pointer matching struct type stored in b[]
     */
    public static FSTStruct createStructPointer(Bytez b, int index) {
        return FSTStructFactory.getInstance().getStructPointerByOffset(b, index).detach();
    }

    /**
     * @param onHeapTemplate
     * @param <T>
     * @return return a byte array based struct instance for given on-heap template. Allocates a new byte[] with each call
     */
    public static <T extends FSTStruct> T toStruct(T onHeapTemplate) {
        return FSTStructFactory.getInstance().toStruct(onHeapTemplate);
    }

    /**
     * @param b
     * @param index
     * @return a pointer matching struct type stored in b[] from the thread local cache
     */
    public static FSTStruct getVolatileStructPointer(Bytez b, int index) {
        return (FSTStruct) FSTStructFactory.getInstance().getStructPointerByOffset(b, index);
    }

    /**
     * @param clazz
     * @param <C>
     * @return a newly allocated pointer matching. use baseOn to point it to a meaningful location
     */
    public static <C extends FSTStruct> C newPointer(Class<C> clazz) {
        try {
            return (C) allocInstance(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static <C extends FSTStruct> Object allocInstance(Class<C> clazz) throws Exception {
        return FSTUtil.getUnsafe().allocateInstance(FSTStructFactory.getInstance().getProxyClass(clazz));
//        return FSTStructFactory.getInstance().getProxyClass(clazz).newInstance();
    }

    /**
     * Create a Heap-Byte Array Structallocator with given chunk size in bytes. If allocated structs are larger than the given size, a new bytearray is
     * created for the allocation.
     * @param chunkSizeBytes
     */
    public FSTStructAllocator(int chunkSizeBytes) {
        this.chunkSize = chunkSizeBytes;
    }

    /**
     * optionally uses another allocator (e.g. for Real Off Heap with MallocBytez)
     *
     * Warning: Currently there is not automatic free for OffHeap Bytez, so only use for
     * statically allocated structures. You can call free() to release all offheap structures
     * allocated by this instance, however you'll get crashes if there are still references
     * from y<our application to the free'd memory.
     * A good approach is to keep only large static structures OffHeap (refdata, buffers) so there is no need for freeing.
     * Also IO related single big blobs apply for Off Heap.
     *
     * This does not apply for HeapBytez, which are handled by GC as usual. Here a chunk is freed, if there is no Struct
     * Object in the Heap referencing the unerlying byte array. If you use large chunk sizes, this can be a problem
     * because a single stuct can prevent a large chunk from being freed. So in general for On-Heap use as small
     * as possible chunks, off heap use large chunk sizes and free manually.
     *
     * @param chunkSizeBytes - set to a reasonable size, memory is allocated in thes chunk sizes. If very small, each
     *                       struct alloc will result in its individual chunk being allocated.
     * @param allocator
     */
    public FSTStructAllocator(int chunkSizeBytes, BytezAllocator allocator ) {
        this.chunkSize = chunkSizeBytes;
        alloc = allocator;
    }

    /**
     * create a new struct array of same type as template
     * @param size
     * @return
     */
    public <X extends FSTStruct> StructArray<X> newArray(int size, X templ) {
        return newArray(size,templ,alloc);
    }

    /**
     * create a new struct array of same type as template
     * @param size
     * @return
     */
    public <X extends FSTStruct> StructArray<X> newArray(int size, X templ, BytezAllocator alloc) {
        StructArray<X> aTemplate = new StructArray<X>(size, templ);
        int siz = getFactory().calcStructSize(aTemplate);
        try {
            if ( siz < chunkSize )
                return newStruct(aTemplate);
            else {
                return getFactory().toStruct(aTemplate,alloc);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * frees associated offheap memory, in case making objects created INVALID (Access violation!).
     * is a noop for on heap byte array allocator
     */
    public void free() {
        alloc.freeAll();
    }

    /**
     * allocate a Struct instance from an arbitrary template instance.
     *
     * @param aTemplate
     * @param <S>
     * @return
     */
    public <S extends FSTStruct> S newStruct(S aTemplate) {
        return newStruct(aTemplate,alloc);
    }

    public <S extends FSTStruct> S newStruct(S aTemplate, BytezAllocator alloc) {
        aTemplate = getFactory().toStruct(aTemplate);
        if (aTemplate.getByteSize()>=chunkSize)
            return (S)aTemplate.createCopy();
        int byteSize = aTemplate.getByteSize();
        synchronized (this) {
            if (chunk == null || chunkIndex+byteSize >= chunk.length()) {
                chunk = alloc.alloc(chunkSize);
                if ( DUMP_ALLOC )
                System.out.println("[Allocator] sum allocated "+MallocBytezAllocator.alloced.get()/1024/1024+" MB");
                chunkIndex = 0;
                chunkObjCount = 0;
            }
//            FSTStruct.unsafe.copyMemory(aTemplate.___bytes, aTemplate.___offset, chunk, FSTStruct.bufoff + chunkIndex, byteSize);
            aTemplate.___bytes.copyTo(chunk, chunkIndex, aTemplate.___offset, byteSize);
            S res = (S) getFactory().createStructWrapper(chunk, chunkIndex );
            chunkIndex+=byteSize;
            chunkObjCount++;
            return res;
        }
    }

    protected FSTStructFactory getFactory() {
        return FSTStructFactory.getInstance();
    }


}
