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

package org.nustaq.offheap.bytez;

/**
 * Date: 16.11.13
 * Time: 12:37
 *
 * An Allocator instantiates byte sources and can free them again (if necessary)
 */
public interface BytezAllocator {

    public Bytez alloc(long len);
    public void free( Bytez bytes );
    public void freeAll();

}
