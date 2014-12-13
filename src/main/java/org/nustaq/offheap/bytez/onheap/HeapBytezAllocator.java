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
package org.nustaq.offheap.bytez.onheap;

import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.BytezAllocator;

/**
 * Date: 16.11.13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */
public class HeapBytezAllocator implements BytezAllocator {
    @Override
    public Bytez alloc(long len) {
        return new HeapBytez(new byte[(int) len]);
    }

    @Override
    public void free(Bytez bytes) {

    }

    @Override
    public void freeAll() {
        // nothing to do, GC
    }
}
