/*
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 */

package org.nustaq.serialization.util;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 20.11.12
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
public class FSTIdentity2IdMap
{
    private static final int RESERVE = 4;
    static int[] prim = {
            3,5,7, 11, 13, 17, 19, 23, 29, 37, 67, 97, 139,
            211, 331, 641, 1097, 1531, 2207, 3121, 5059, 7607, 10891,
            15901, 19993, 30223, 50077, 74231,99991, 150001, 300017,
            1000033,1500041,200033,3000077,5000077,10000019

    };

    static int adjustSize(int size) {
        for (int i = 0; i < prim.length-1; i++) {
            if ( size < prim[i] ) {
                return prim[i]+RESERVE;
            }
        }
        return size+RESERVE;
    }

    private static final int GROFAC = 2;

    private int mask;
    public Object[]  mKeys;
    private int klen;
    private int     mValues[];
    private int     mNumberOfElements;
    private FSTIdentity2IdMap next;

    public FSTIdentity2IdMap(int initialSize)
    {
        if (initialSize < 2)
        {
            initialSize = 2;
        }

        initialSize = adjustSize(initialSize*GROFAC);

        mKeys = new Object[initialSize];
        mValues = new int[initialSize];
        mNumberOfElements = 0;
        mask = (Integer.highestOneBit(initialSize)<<1)-1;
        klen = initialSize-4;
    }

    public int size()
    {
        return mNumberOfElements + (next != null ? next.size():0);
    }

    final public int putOrGet(Object key, int value)
    {
        int hash = calcHash(key);
        return putOrGetHash(key, value, hash, this);
    }

    final int putOrGetHash(Object key, int value, int hash, FSTIdentity2IdMap parent) {
        if (mNumberOfElements*GROFAC > mKeys.length)
        {
            if ( parent != null ) {
                if ( (parent.mNumberOfElements+mNumberOfElements)*GROFAC > parent.mKeys.length ) {
                    parent.resize(parent.mKeys.length*GROFAC);
                    return parent.putOrGet(key,value);
                } else {
                    resize(mKeys.length * GROFAC);
                }
            } else {
                resize(mKeys.length * GROFAC);
            }
        }

        Object[] mKeys = this.mKeys;
//        int idx = calcIndexFromHash(hash, mKeys);
        int idx = calcIndexFromHash(hash,mKeys);

        Object mKeyAtIdx = mKeys[idx];
        if (mKeyAtIdx == null ) // new
        {
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx]   = key;
            return Integer.MIN_VALUE;
        }
        else if (mKeyAtIdx==key)    // present
        {
            return mValues[idx];
        } else {
            Object mKeyAtIdxPlus1 = mKeys[idx + 1];
            if (mKeyAtIdxPlus1 == null ) // new
            {
                mNumberOfElements++;
                mValues[idx+1] = value;
                mKeys[idx+1]   = key;
                return Integer.MIN_VALUE;
            }
            else if (mKeyAtIdxPlus1==key)    // present
            {
                return mValues[idx+1];
            } else {
                Object mKeysAtIndexPlus2 = mKeys[idx + 2];
                if (mKeysAtIndexPlus2 == null ) // new
                {
                    mNumberOfElements++;
                    mValues[idx+2] = value;
                    mKeys[idx+2]   = key;
                    return Integer.MIN_VALUE;
                }
                else if (mKeysAtIndexPlus2==key)    // present
                {
                    return mValues[idx+2];
                } else {
                    return putOrGetNext(hash, key, value);
                }
            }
        }
    }

    final int putOrGetNext(final int hash, final Object key, final int value) {
        if ( next == null ) { // new
            int newSiz = mKeys.length/10;
            next = new FSTIdentity2IdMap(newSiz);
            next.putHash(key,value,hash,this);
            return Integer.MIN_VALUE;
        }
        return next.putOrGetHash(key,value,hash, this);
    }

    final public void put(Object key, int value)
    {
        int hash = calcHash(key);
        putHash(key, value, hash, this);
    }

    final void putHash(Object key, int value, int hash, FSTIdentity2IdMap parent) {
        if (mNumberOfElements*GROFAC > mKeys.length)
        {
            if ( parent != null ) {
                if ( (parent.mNumberOfElements+mNumberOfElements)*GROFAC > parent.mKeys.length ) {
                    parent.resize(parent.mKeys.length*GROFAC);
                    parent.put(key,value);
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

        if (mKeys[idx] == null ) // new
        {
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx]   = key;
        }
        else if (mKeys[idx]==key)    // overwrite
        {
//            bloom|=hash;
            mValues[idx] = value;
        } else {
            if (mKeys[idx+1] == null ) // new
            {
                mNumberOfElements++;
                mValues[idx+1] = value;
                mKeys[idx+1]   = key;
            }
            else if (mKeys[idx+1]==key)    // overwrite
            {
//                bloom|=hash;
                mValues[idx+1] = value;
            } else {
                if (mKeys[idx+2] == null ) // new
                {
                    mNumberOfElements++;
                    mValues[idx+2] = value;
                    mKeys[idx+2]   = key;
                }
                else if (mKeys[idx+2]==key)    // overwrite
                {
//                    bloom|=hash;
                    mValues[idx+2] = value;
                } else {
                    putNext(hash, key, value);
                }
            }
        }
    }

    final void putNext(final int hash, final Object key, final int value) {
        if ( next == null ) {
            int newSiz = mKeys.length/10;
            next = new FSTIdentity2IdMap(newSiz);
        }
        next.putHash(key,value,hash, this);
    }

    final public int get(final Object key) {
        int hash = calcHash(key);
        return getHash(key,hash);
    }

    final int getHash(final Object key, final int hash)
    {
        final int idx = calcIndexFromHash(hash, mKeys);

        Object mapsKey = mKeys[idx];
        if (mapsKey == null ) // not found
        {
            return Integer.MIN_VALUE;
        }
        else if (mapsKey == key)  // found
        {
            return mValues[idx];
        } else {
            mapsKey = mKeys[idx+1];
            if (mapsKey == null ) // not found
            {
                return Integer.MIN_VALUE;
            }
            else if (mapsKey == key)  // found
            {
                return mValues[idx+1];
            } else {
                mapsKey = mKeys[idx+2];
                if (mapsKey == null ) // not found
                {
                    return Integer.MIN_VALUE;
                }
                else if (mapsKey == key)  // found
                {
                    return mValues[idx+2];
                } else {
                    if ( next == null ) {
                        return Integer.MIN_VALUE;
                    }
                    return next.getHash(key, hash);
                }
            }
        }
    }

    final void resize(int newSize)
    {
        newSize = adjustSize(newSize);
        Object[]    oldTabKey = mKeys;
        int[] oldTabVal = mValues;

        mKeys = new Object[newSize];
        mValues           = new int[newSize];
        mNumberOfElements = 0;
        mask = (Integer.highestOneBit(newSize)<<1)-1;
        klen = newSize-RESERVE;

        for (int n = 0; n < oldTabKey.length; n++)
        {
            if (oldTabKey[n] != null)
            {
                put(oldTabKey[n], oldTabVal[n]);
            }
        }
        if ( next != null ) {
            FSTIdentity2IdMap oldNext = next;
            next = null;
            oldNext.rePut(this);
        }
    }

    private void rePut(FSTIdentity2IdMap kfstObject2IntMap) {
        for (int i = 0; i < mKeys.length; i++) {
            Object mKey = mKeys[i];
            if ( mKey != null ) {
                kfstObject2IntMap.put(mKey, mValues[i]);
            }
        }
        if ( next != null ) {
            next.rePut(kfstObject2IntMap);
        }
    }

    final int calcIndexFromHash(int hash, Object[] mKeys) {
        int res = hash & mask;
        while ( res >= klen ) {
            res = res>>>1;
        }
        return res;
    }

    private static int calcHash(Object x) {
        int h = System.identityHashCode(x);
//        return h>>2;
        return ((h << 1) - (h << 8));
    }

    public void clear() {
        if ( size() == 0 ) {
            return;
        }
        FSTUtil.clear(mKeys);
        FSTUtil.clear(mValues);
//        Arrays.fill(mKeys,null);
//        Arrays.fill(mValues,0);
        mNumberOfElements = 0;
        if ( next != null ) {
            next.clear();
        }
    }

    public static void main( String arg[] ) {
        String strings[] = new String[5000];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = ""+Math.random();
        }


        FSTIdentity2IdMap map = new FSTIdentity2IdMap(97);

        // warm
        for ( int j = 0; j < 50000; j++ ) {
            testNewPut(strings,map);
            map.clear();
        }

        long tim = System.currentTimeMillis();
        for ( int j = 0; j < 50000; j++ ) {
            testNewPut(strings, map);
            map.clear();
        }

        testNewPut(strings, map);
        testGet(strings, map);
        map.clear();
        testPut(strings, map);
        testGet(strings, map);
        testWrongGet(map);
        long now = System.currentTimeMillis();
        System.out.println("time new "+(now-tim));

        tim = System.currentTimeMillis();
        for ( int j = 0; j < 50000; j++ ) {
//            testExistPut(strings, map);
            testPut(strings, map);
        }
        now = System.currentTimeMillis();
        System.out.println("time exist put "+(now-tim));

        tim = System.currentTimeMillis();
        for ( int j = 0; j < 50000; j++ ) {
            testGet(strings, map);
        }
        now = System.currentTimeMillis();
        System.out.println("time exist "+(now-tim));


    }

    private static void testExistPut(String[] strings, FSTIdentity2IdMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            int fieldId = map.putOrGet(string, i);
//            if ( fieldId != i ) {
//                throw new RuntimeException("möp 1 "+i+" "+fieldId);
//            }
        }
    }

    private static void testPut(String[] strings, FSTIdentity2IdMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            map.put(string, i);
        }
    }

    private static void testGet(String[] strings, FSTIdentity2IdMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            int fieldId = map.get(string);
            if ( fieldId != i ) {
                throw new RuntimeException("möp 2 "+i+" "+fieldId);
            }
        }
    }

    private static void testWrongGet(FSTIdentity2IdMap map) {
        for (int i = 0; i < 1000; i++) {
            int fieldId = map.get(new String("pok"+i));
            if ( fieldId != Integer.MIN_VALUE ) {
                throw new RuntimeException("möp 3 "+i+" "+fieldId);
            }
        }
    }

    private static void testNewPut(String[] strings, FSTIdentity2IdMap map) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if ( map.putOrGet(string,i) > 0 ) {
                throw new RuntimeException("möp");
            }
        }
    }

    public void dump() {
        for (int i = 0; i < mKeys.length; i++) {
            Object mKey = mKeys[i];
            if ( mKey != null ) {
                System.out.println(""+mKey+" => "+mValues[i]);
            }
        }
        if ( next != null )
            next.dump();
    }
}