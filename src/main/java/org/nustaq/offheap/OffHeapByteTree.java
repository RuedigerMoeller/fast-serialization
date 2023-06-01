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
package org.nustaq.offheap;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.onheap.HeapBytez;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruedi on 29.06.14.
 *
 * rewrite as old version was unmaintainable and poorly documented + segfaulted on some very
 * specific operation + data permutations
 *
 * Temporary on-heap slowish implementation (so index for offheap objects is on heap currently)
 *
 */
public class OffHeapByteTree {

    Object2LongOpenHashMap<ByteSource> map = new Object2LongOpenHashMap<>();

    // dummy impl
    public static long estimateMBytesForIndex(int keyLen, int numberOfElems) {
        return -1;
    }

    public OffHeapByteTree(int keyLen, long aVoid) {
    }

    public void put(ByteSource byteKey, long off) {
        HeapBytez heapBytez = getTmpHeapBytez(byteKey, new byte[(int) byteKey.length()]);
        map.put(heapBytez,off);
    }

    byte tmp[];
    private byte[] getTmp(int len) {
        if ( tmp == null || tmp.length != len ) {
            tmp = new byte[len];
        }
        return tmp;
    }

    public long get(ByteSource byteKey) {
        HeapBytez heapBytez = getTmpHeapBytez(byteKey, getTmp((int) byteKey.length()));
        Long aLong = map.get(heapBytez);
        if ( aLong == null ) {
            return 0;
        }
        return aLong;
    }

    static ThreadLocal<HeapBytez> tmpbtz = new ThreadLocal<HeapBytez>() {
        @Override
        protected HeapBytez initialValue() {
            return new HeapBytez(new byte[0]);
        }
    };
    protected HeapBytez getTmpHeapBytez(ByteSource byteKey, byte[] base) {
        for (int i = 0; i < base.length; i++) {
            base[i] = byteKey.get(i);
        }
        final HeapBytez heapBytez = tmpbtz.get();
        heapBytez.setBase(base,0,base.length);
        return new HeapBytez(base);
    }

    public void remove(ByteSource byteKey) {
        HeapBytez heapBytez = getTmpHeapBytez(byteKey, getTmp((int) byteKey.length()));
        map.remove(heapBytez);
    }

    public void dumpStats() {
        System.out.println("SIZE:"+map.size());
    }

    public void free() {
    }
}



