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

package org.nustaq.offheap.bytez.malloc;

import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.BytezAllocator;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
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
