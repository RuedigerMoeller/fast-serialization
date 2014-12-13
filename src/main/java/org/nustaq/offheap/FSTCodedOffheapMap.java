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

import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.bytesource.BytezByteSource;

import java.util.Iterator;

/**
 * An offheap map adding mechanics to encode and decode keys and values.
 *
 * Again remember keys have to be of fixed size and should be as short as possible.
 * Performance and memory consumtpion is best if significant digits are at the
 * end of a key (e.g. "year_montH_day" instead "day_month_year".
 *
 * This class can be used for manual implementation of encoding/decoding.
 *
 * See StringOffHeapMap for an example on how to do efficient wrapping/encoding of keys
 */
public abstract class FSTCodedOffheapMap<K,V> extends FSTBinaryOffheapMap {

    public FSTCodedOffheapMap(int keyLen, long sizeMemBytes, int numberOfElems) {
        super(keyLen, sizeMemBytes, numberOfElems);
    }

    public FSTCodedOffheapMap(String mappedFile, int keyLen, long sizeMemBytes, int numberOfElems) throws Exception {
        super(mappedFile, keyLen, sizeMemBytes, numberOfElems);
    }

    //    protected abstract K decodeKey(ByteSource key);

    public abstract ByteSource encodeKey(K key);

    public abstract ByteSource encodeValue(V value);

    public abstract V decodeValue(BytezByteSource val);

    public V get( K key ) {
        if ( key == null )
            return null;
        ByteSource bkey = encodeKey(key);
        BytezByteSource val = getBinary(bkey);
        if ( val == null )
            return null;
        try {
            return decodeValue(val);
        }catch (Exception ex) {
            System.out.println("ERROR in "+(val.getOff()-getHeaderLen())+" "+printBinaryKey(bkey)+" "+index.get(bkey));
            throw new RuntimeException(ex);
        }
    }


    public void put( K key, V value ) {
        ByteSource bkey = encodeKey(key);
        ByteSource decoded = encodeValue(value);
        putBinary(bkey, decoded);
    }

    public void remove( K key ) {
        ByteSource bkey = encodeKey(key);
        removeBinary(bkey);
    }

    /**
     * Warning: Concurrent Modification (e.g. add remove elements during iteration) is NOT SUPPORTED
     * and NOT CHECKED. Collect keys to change inside an iteration and perform changes on the map after iteration is
     * finished.
     * @return an iterator on the values contained in this map.
     */
    public Iterator<V> values() {
        final Iterator<ByteSource> iter = binaryValues();
        return new Iterator() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public V next() {
                BytezByteSource next = (BytezByteSource) iter.next();
                return decodeValue(next);
            }

            @Override
            public void remove() {
                throw new RuntimeException("unimplemented");
            }
        };
    }


}
