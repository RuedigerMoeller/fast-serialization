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
 * abstraction of byte arrays similar to ByteBuffer without the need to create temp objects in order to get long,int,.. views
 * additionally supports volatile read/write (for byte[] based backing buffers only !)
 */
public interface Bytez extends BasicBytez {

    public Bytez slice(long off, int len);

    public boolean compareAndSwapInt( long offset, int expect, int newVal);
    public boolean compareAndSwapLong( long offset, long expect, long newVal);

    public byte[] toBytes(long startIndex, int len);
    /**
     * @return return the underlying byte array, not supported by MallocBytez !. Use getArr to extract data by copy instead.
     */
    public byte[] asByteArray();

    /**
     * @return the start index inside the byte array returned by asByteArray, not supported by MallocBytez
     */
    public int getBAOffsetIndex();
    /**
     * @return the length inside the byte array returned by asByteArray, not supported by MallocBytez
     */
    public int getBALength();

}
