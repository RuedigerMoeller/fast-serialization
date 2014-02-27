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
 * Date: 14.12.12
 * Time: 20:34
 * To change this template use File | Settings | File Templates.
 */
package de.ruedigermoeller.heapoff;

import de.ruedigermoeller.serialization.FSTConfiguration;

import java.io.IOException;
import java.util.*;

/**
 * UNRELEASED UNTESTED !
 *
 * a simple Map implementation based on OffHeapBuffer. Access is not threadsafe and the offheapbuffer
 * is not grown automatically when the limit is reached. Note that every put adds a new Entry to the OffHeapBuffer,
 * so its wise to check using contains before putting an Object (except you explicitely want to overwrite the
 * existing Object). The maps build an in Java-Heap hashmap Key=>OffHeapIndex. The values are then stored
 * offheap.
 * Access is single threaded only, you need to do your own synchronization in order to access this from multiple threads.
 *
 * All objects put need to implement Serializable
 * @param <K>
 * @param <V>
 */
public class FSTOffHeapMap<K,V> extends AbstractMap<K,V> {

    HashMap<K,Integer> hmap;
    FSTByteBufferOffheap heap;
    FSTByteBufferOffheap.OffHeapAccess access;

    public FSTOffHeapMap(FSTByteBufferOffheap heap) {
        this.heap = heap;
        hmap = new HashMap<K, Integer>();
        access = heap.createAccess();
    }

    public FSTOffHeapMap(int sizeMB) throws IOException {
        heap = new FSTByteBufferOffheap(sizeMB);
        hmap = new HashMap<K, Integer>();
        access = heap.createAccess();
    }

    public FSTConfiguration getConf() {
        return getHeap().getConf();
    }

    @Override
    public int size() {
        return hmap.size();
    }

    @Override
    public boolean isEmpty() {
        return hmap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return hmap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        Iterator<K> iterator = hmap.keySet().iterator();
        while( iterator.hasNext() ) {
            V v = get(iterator.next());
            if ( v != null && v.equals(value) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        Integer integer = hmap.get(key);
        try {
            Object res = access.getObject(integer);
            if ( res == null ) {
                return null;
            }
            return (V) res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public V put(K key, V value) {
        try {
            if ( hmap.containsKey(key) ) {
                return get(key);
            }
            int res = access.add(value,null);
            hmap.put(key,res);
            return value;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public V remove(Object key) {
        Object res = get(key);
        hmap.remove(key);
        return (V)res;
    }

    @Override
    public void clear() {
        hmap.clear();
    }

    private final class FSTOffHeapMapEntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            final Iterator it = hmap.keySet().iterator();
            return new Iterator<Entry<K, V>>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    final K next = (K) it.next();
                    return new Entry<K, V>() {
                        V cachedV;
                        @Override
                        public K getKey() {
                            return next;
                        }

                        @Override
                        public V getValue() {
                            if ( cachedV == null ) {
                                cachedV = (V) get(next);
                            }
                            return cachedV;
                        }

                        @Override
                        public V setValue(V value) {
                            throw new RuntimeException("not implemented");
                        }

                        @Override
                        public boolean equals(Object o) {
                            boolean res = false;
                            if ( o instanceof Entry) {
                                Entry e = (Entry) o;
                                if ( getValue() != null ) {
                                    return getKey().equals(((Entry) o).getKey()) && getValue().equals(((Entry) o).getValue());
                                } else {
                                    return getKey().equals(((Entry) o).getKey()) && ((Entry) o).getValue() == null;
                                }
                            }
                            return false;
                        }

                        @Override
                        public int hashCode() {
                            return (getKey()==null   ? 0 : getKey().hashCode()) ^
                                   (getValue()==null ? 0 : getValue().hashCode());
                        }
                    };
                }

                @Override
                public void remove() {
                    throw new RuntimeException("not implemented");
                }
            };
        }

        public boolean remove(Object o) {
            throw new RuntimeException("not implemented");
        }

        public int size() {
            return hmap.size();
        }

        public void clear() {
            hmap.clear();
        }

    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new FSTOffHeapMapEntrySet();
    }

    ////////////////////////////////////////////////////////
    // additions
    ///////////////////////////////////////////////////////

    public FSTByteBufferOffheap getHeap() {
        return heap;
    }


}
