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

package org.nustaq.serialization.util;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 20.11.12
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
public class FSTObject2IntMap<K> {
    static final int[] prim = {
                            3, 5, 7, 11, 13, 17, 19, 23, 29, 37, 67, 97, 139,
                            211, 331, 641, 1097, 1531, 2207, 3121, 5059, 7607, 10891,
                            15901, 19993, 30223, 50077, 74231, 99991, 150001, 300017,
                            1000033, 1500041, 200033, 3000077, 5000077, 10000019

    };
    private static final int GROFAC = 2;

    public static int adjustSize(int size) {
        for (int i = 0; i < prim.length - 1; i++) {
            if (size < prim[i]) {
                return prim[i];
            }
        }
        return size;
    }

    private Object mKeys[];
    private int mValues[];
    private int mNumberOfElements;
    private FSTObject2IntMap<K> next;
    private boolean checkClazzOnEquals = false;

    private FSTObject2IntMap(int initialSize, boolean checkClassOnequals) {
        if (initialSize < 2) {
            initialSize = 2;
        }

        initialSize = adjustSize(initialSize * 2);

        mKeys = new Object[initialSize];
        mValues = new int[initialSize];
        mNumberOfElements = 0;
        this.checkClazzOnEquals = checkClassOnequals;
    }

    public int size() {
        return mNumberOfElements + (next != null ? next.size() : 0);
    }

    final public void put(K key, int value) {
        int hash = key.hashCode() & 0x7FFFFFFF;
        putHash(key, value, hash, this);
    }

    private void putHash(K key, int value, int hash, FSTObject2IntMap<K> parent) {
        if (mNumberOfElements * GROFAC > mKeys.length) {
            if (parent != null) {
                if ((parent.mNumberOfElements + mNumberOfElements) * GROFAC > parent.mKeys.length) {
                    parent.resize(parent.mKeys.length * GROFAC);
                    parent.put(key, value);
                    return;
                } else {
                    resize(mKeys.length * GROFAC);
                }
            } else {
                resize(mKeys.length * GROFAC);
            }
        }

        int idx = hash % mKeys.length;

        if (mKeys[idx] == null) // new
        {
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx] = key;
        } else if (mKeys[idx].equals(key) && (!checkClazzOnEquals || mKeys[idx].getClass() == key.getClass()))    // overwrite
        {
            mValues[idx] = value;
        } else {
            putNext(hash, key, value);
        }
    }

    private void putNext(final int hash, final K key, final int value) {
        if (next == null) {
            int newSiz = mNumberOfElements / 3;
            next = new FSTObject2IntMap<>(newSiz, checkClazzOnEquals);
        }
        next.putHash(key, value, hash, this);
    }

    final public int get(final K key) {
        final int hash = key.hashCode() & 0x7FFFFFFF;
        //return getHash(key,hash); inline =>
        final int idx = hash % mKeys.length;

        final Object mapsKey = mKeys[idx];
        if (mapsKey == null) // not found
        {
            return Integer.MIN_VALUE;
        } else if (mapsKey.equals(key) && (!checkClazzOnEquals || mapsKey.getClass() == key.getClass()))  // found
        {
            return mValues[idx];
        } else {
            if (next == null) {
                return Integer.MIN_VALUE;
            }
            return next.getHash(key, hash);
        }
        // <== inline
    }

    private int getHash(final K key, final int hash) {
        final int idx = hash % mKeys.length;

        final Object mapsKey = mKeys[idx];
        if (mapsKey == null) // not found
        {
            return Integer.MIN_VALUE;
        } else if (mapsKey.equals(key) && (!checkClazzOnEquals || mapsKey.getClass() == key.getClass()))  // found
        {
            return mValues[idx];
        } else {
            if (next == null) {
                return Integer.MIN_VALUE;
            }
            return next.getHash(key, hash);
        }
    }

    private void resize(int newSize) {
        newSize = adjustSize(newSize);
        Object[] oldTabKey = mKeys;
        int[] oldTabVal = mValues;

        mKeys = new Object[newSize];
        mValues = new int[newSize];
        mNumberOfElements = 0;

        for (int n = 0; n < oldTabKey.length; n++) {
            if (oldTabKey[n] != null) {
                put((K) oldTabKey[n], oldTabVal[n]);
            }
        }
        if (next != null) {
            FSTObject2IntMap oldNext = next;
            next = null;
            oldNext.rePut(this);
        }
    }

    private void rePut(FSTObject2IntMap<K> kfstObject2IntMap) {
        for (int i = 0; i < mKeys.length; i++) {
            Object mKey = mKeys[i];
            if (mKey != null) {
                kfstObject2IntMap.put((K) mKey, mValues[i]);
            }
        }
        if (next != null) {
            next.rePut(kfstObject2IntMap);
        }
    }
}