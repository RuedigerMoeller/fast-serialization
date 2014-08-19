package org.nustaq.offheap.bytez.malloc;

import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.BytezAllocator;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

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
 * Date: 17.11.13
 * Time: 01:16
 * To change this template use File | Settings | File Templates.
 */
public class MallocBytezAllocator implements BytezAllocator {

    public static AtomicLong alloced = new AtomicLong(0);

    ArrayList<MallocBytez> allocated = new ArrayList<MallocBytez>();

    @Override
    public Bytez alloc(long len) {
        MallocBytez mallocBytez = new MallocBytez(MallocBytez.unsafe.allocateMemory(len), len);
        mallocBytez.clear();
        allocated.add(mallocBytez);
        alloced.getAndAdd(len);
        return mallocBytez;
    }

    @Override
    public void free(Bytez bytes) {
        if ( bytes instanceof MallocBytez && allocated.contains(bytes) ) {
            allocated.remove(bytes);
            alloced.getAndAdd(-bytes.length());
            ((MallocBytez) bytes).free();
        }
    }

    @Override
    public void freeAll() {
        for (int i = 0; i < allocated.size(); i++) {
            MallocBytez mallocBytez = allocated.get(i);
            mallocBytez.free();
        }
        allocated.clear();
    }
}
