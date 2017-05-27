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

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 20.11.12
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
public class FSTIdentity2IdMap {
    private static final int RESERVE = 4;
    private static final int MAX_DEPTH = 4;
    static int[] prim = {
                            3, 5, 7, 11, 13, 17, 19, 23, 29, 37, 67, 97, 139,
                            211, 331, 641, 1097, 1531, 2207, 3121, 5059, 7607, 10891,
                            15901, 19993, 30223, 50077, 74231, 99991, 150001, 300017,
                            1000033, 1500041, 200033, 3000077, 5000077, 10000019

    };

    static int adjustSize(int size) {
        for (int i = 0; i < prim.length - 1; i++) {
            if (size < prim[i]) {
                return prim[i] + RESERVE;
            }
        }
        return size + RESERVE;
    }

    private static final int GROFAC = 2;

    private int mask;
    public Object[] mKeys;
    private int klen;
    private int mValues[];
    private int mNumberOfElements;
    private FSTIdentity2IdMap next;
    private List linearScanList; // in case of too deep nesting, this one is filled and linear search is applied
    private List<Integer> linearScanVals; // in case of too deep nesting, this one is filled and linear search is applied

    public FSTIdentity2IdMap(int initialSize) {
        if (initialSize < 2) {
            initialSize = 2;
        }

        initialSize = adjustSize(initialSize * GROFAC);

        mKeys = new Object[initialSize];
        mValues = new int[initialSize];
        mNumberOfElements = 0;
        mask = (Integer.highestOneBit(initialSize) << 1) - 1;
        klen = initialSize - 4;
    }

    public int size() {
        if ( linearScanList != null )
            return linearScanList.size();
        return mNumberOfElements + (next != null ? next.size() : 0);
    }

    final public int putOrGet(Object key, int value) {
        int hash = calcHash(key);
        return putOrGetHash(key, value, hash, this, 0);
    }

    final int putOrGetHash(Object key, int value, int hash, FSTIdentity2IdMap parent, int depth ) {
        if ( linearScanList != null ) {
            for (int i = 0; i < linearScanList.size(); i++) {
                Object o = linearScanList.get(i);
                if ( o == key ) {
                    return linearScanVals.get(i);
                }
            }
            linearScanList.add(key);
            linearScanVals.add(value);
            return Integer.MIN_VALUE;
        }
        if (mNumberOfElements * GROFAC > mKeys.length) {
            if (parent != null) {
                if ((parent.mNumberOfElements + mNumberOfElements) * GROFAC > parent.mKeys.length) {
                    parent.resize(parent.mKeys.length * GROFAC);
                    return parent.putOrGet(key, value);
                } else {
                    resize(mKeys.length * GROFAC);
                }
            } else {
                resize(mKeys.length * GROFAC);
            }
        }

        Object[] mKeys = this.mKeys;
//        int idx = calcIndexFromHash(hash, mKeys);
        int idx = calcIndexFromHash(hash, mKeys);

        Object mKeyAtIdx = mKeys[idx];
        if (mKeyAtIdx == null) // new
        {
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx] = key;
            return Integer.MIN_VALUE;
        } else if (mKeyAtIdx == key)    // present
        {
            return mValues[idx];
        } else {
            Object mKeyAtIdxPlus1 = mKeys[idx + 1];
            if (mKeyAtIdxPlus1 == null) // new
            {
                mNumberOfElements++;
                mValues[idx + 1] = value;
                mKeys[idx + 1] = key;
                return Integer.MIN_VALUE;
            } else if (mKeyAtIdxPlus1 == key)    // present
            {
                return mValues[idx + 1];
            } else {
                Object mKeysAtIndexPlus2 = mKeys[idx + 2];
                if (mKeysAtIndexPlus2 == null) // new
                {
                    mNumberOfElements++;
                    mValues[idx + 2] = value;
                    mKeys[idx + 2] = key;
                    return Integer.MIN_VALUE;
                } else if (mKeysAtIndexPlus2 == key)    // present
                {
                    return mValues[idx + 2];
                } else {
                    return putOrGetNext(hash, key, value, depth+1);
                }
            }
        }
    }

    final int putOrGetNext(final int hash, final Object key, final int value, int depth) {
        if (next == null) { // new
            int newSiz = mKeys.length / 10;
            next = new FSTIdentity2IdMap(newSiz);
            if ( depth > MAX_DEPTH ) {
                next.linearScanVals = new ArrayList(3);
                next.linearScanList = new ArrayList(3);
            }
            next.putHash(key, value, hash, this, depth);
            return Integer.MIN_VALUE;
        }
        return next.putOrGetHash(key, value, hash, this, depth+1);
    }

    final public void put(Object key, int value) {
        int hash = calcHash(key);
        putHash(key, value, hash, this, 0);
    }

    final void putHash(Object key, int value, int hash, FSTIdentity2IdMap parent, int depth) {
        if ( linearScanList != null ) {
            for (int i = 0; i < linearScanList.size(); i++) {
                Object o = linearScanList.get(i);
                if ( o == key ) {
                    linearScanVals.set(i, value);
                    return;
                }
            }
            linearScanList.add(key);
            linearScanVals.add(value);
            return;
        }
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

        Object[] mKeys = this.mKeys;
        int idx = calcIndexFromHash(hash, mKeys);

        if (mKeys[idx] == null) // new
        {
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx] = key;
        } else if (mKeys[idx] == key)    // overwrite
        {
//            bloom|=hash;
            mValues[idx] = value;
        } else {
            if (mKeys[idx + 1] == null) // new
            {
                mNumberOfElements++;
                mValues[idx + 1] = value;
                mKeys[idx + 1] = key;
            } else if (mKeys[idx + 1] == key)    // overwrite
            {
//                bloom|=hash;
                mValues[idx + 1] = value;
            } else {
                if (mKeys[idx + 2] == null) // new
                {
                    mNumberOfElements++;
                    mValues[idx + 2] = value;
                    mKeys[idx + 2] = key;
                } else if (mKeys[idx + 2] == key)    // overwrite
                {
//                    bloom|=hash;
                    mValues[idx + 2] = value;
                } else {
                    putNext(hash, key, value, depth+1);
                }
            }
        }
    }

    final void putNext(final int hash, final Object key, final int value, int depth) {
        if (next == null) {
            int newSiz = mKeys.length / 10;
            next = new FSTIdentity2IdMap(newSiz);
            if ( depth > MAX_DEPTH ) {
                next.linearScanVals = new ArrayList(3);
                next.linearScanList = new ArrayList(3);
            }
        }
        next.putHash(key, value, hash, this, depth+1);
    }

    final public int get(final Object key) {
        int hash = calcHash(key);
        return getHash(key, hash);
    }

    final int getHash(final Object key, final int hash) {
        if ( linearScanList != null ) {
            for (int i = 0; i < linearScanList.size(); i++) {
                Object o = linearScanList.get(i);
                if ( o == key ) {
                    return linearScanVals.get(i);
                }
            }
            return Integer.MIN_VALUE;
        }

        final int idx = calcIndexFromHash(hash, mKeys);

        Object mapsKey = mKeys[idx];
        if (mapsKey == null) // not found
        {
            return Integer.MIN_VALUE;
        } else if (mapsKey == key)  // found
        {
            return mValues[idx];
        } else {
            mapsKey = mKeys[idx + 1];
            if (mapsKey == null) // not found
            {
                return Integer.MIN_VALUE;
            } else if (mapsKey == key)  // found
            {
                return mValues[idx + 1];
            } else {
                mapsKey = mKeys[idx + 2];
                if (mapsKey == null) // not found
                {
                    return Integer.MIN_VALUE;
                } else if (mapsKey == key)  // found
                {
                    return mValues[idx + 2];
                } else {
                    if (next == null) {
                        return Integer.MIN_VALUE;
                    }
                    return next.getHash(key, hash);
                }
            }
        }
    }

    final void resize(int newSize) {
        newSize = adjustSize(newSize);
        Object[] oldTabKey = mKeys;
        int[] oldTabVal = mValues;

        mKeys = new Object[newSize];
        mValues = new int[newSize];
        mNumberOfElements = 0;
        mask = (Integer.highestOneBit(newSize) << 1) - 1;
        klen = newSize - RESERVE;

        for (int n = 0; n < oldTabKey.length; n++) {
            if (oldTabKey[n] != null) {
                put(oldTabKey[n], oldTabVal[n]);
            }
        }
        if (next != null) {
            FSTIdentity2IdMap oldNext = next;
            next = null;
            oldNext.rePut(this);
        }
    }

    private void rePut(FSTIdentity2IdMap kfstObject2IntMap) {
        if ( linearScanList != null ) {
            int size = linearScanList.size();
            for (int i = 0; i < size; i++) {
                Object key = linearScanList.get(i);
                int value = linearScanVals.get(i);
                kfstObject2IntMap.put(key, value);
            }
            return;
        }

        for (int i = 0; i < mKeys.length; i++) {
            Object mKey = mKeys[i];
            if (mKey != null) {
                kfstObject2IntMap.put(mKey, mValues[i]);
            }
        }
        if (next != null) {
            next.rePut(kfstObject2IntMap);
        }
    }

    final int calcIndexFromHash(int hash, Object[] mKeys) {
        int res = hash & mask;
        while (res >= klen) {
            res = res >>> 1;
        }
        return res;
    }

    private static int calcHash(Object x) {
        int h = System.identityHashCode(x);
//        return h>>2;
        return ((h << 1) - (h << 8)) & 0x7fffffff;
    }

    public void clear() {
        if (size() == 0) {
            return;
        }
        if ( linearScanList != null ) {
            linearScanList.clear();
            linearScanVals.clear();
        }
        FSTUtil.clear(mKeys);
        FSTUtil.clear(mValues);
//        Arrays.fill(mKeys,null);
//        Arrays.fill(mValues,0);
        mNumberOfElements = 0;
        if (next != null) {
            next.clear();
        }
    }

    public void dump() {
        for (int i = 0; i < mKeys.length; i++) {
            Object mKey = mKeys[i];
            if (mKey != null) {
                System.out.println("" + mKey + " => " + mValues[i]);
            }
        }
        if (next != null)
            next.dump();
    }

    public int keysLength() {
        return mKeys.length;
    }
}