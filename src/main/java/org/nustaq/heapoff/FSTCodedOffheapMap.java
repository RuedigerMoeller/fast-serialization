package org.nustaq.heapoff;

import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.heapoff.bytez.bytesource.BytezByteSource;

import java.util.Iterator;

/**
 * An offheap map adding mechanics to encode and decode keys and values.
 *
 * Again remember keys have to be of fixed size and should be as short as possible.
 * Performance and memory consumtpion is best if significant digits are at the
 * end of a key (e.g. "year_montH_day" instead "day_month_year".
 *
 * This class can be used, if want to implement manual encoding/decoding of values.
 *
 * See StringOffHeapMap for an example on how to do efficient wrapping/encoding of keys
 */
public abstract class FSTCodedOffheapMap<K,V> extends FSTBinaryOffheapMap {

    public FSTCodedOffheapMap(int keyLen, long size, int indexSizeMB) {
        super(keyLen, size,indexSizeMB);
    }

    @Override
    protected void init(int keyLen, long size, int indexSizeMB) {
        super.init(keyLen, size,indexSizeMB);
    }

    protected abstract ByteSource encodeKey(K key);

    protected abstract ByteSource encodeValue(V value);

    protected abstract V decodeValue(BytezByteSource val);

    public V get( K key ) {
        ByteSource bkey = encodeKey(key);
        BytezByteSource val = getBinary(bkey);
        if ( val == null )
            return null;
        return decodeValue(val);
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
