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

package de.ruedigermoeller.serialization.util;

import java.util.*;


public class FSTInt2ObjectMap<V>
{

    public int  mKeys[];
    public Object  mValues[];
    public int     mNumberOfElements;
    FSTInt2ObjectMap<V> next;
    private static final int GROWFAC = 2;

    public FSTInt2ObjectMap(int initialSize)
    {
        if (initialSize < 2)
        {
            initialSize = 2;
        }

        initialSize = FSTObject2IntMap.adjustSize(initialSize * 2);

        mKeys = new int[initialSize];
        mValues = new Object[initialSize];
        mNumberOfElements = 0;
    }

    public int size()
    {
        return mNumberOfElements + (next != null ? next.size():0);
    }

    final public void put(int key, V value)
    {
        int hash = key & 0x7FFFFFFF;
        if ( key == 0 && value == null) {
            throw new RuntimeException("key value pair not supported "+key+" "+value);
        }
        putHash(key, value, hash, this);
    }

    final void putHash(int key, V value, int hash, FSTInt2ObjectMap<V> parent) {
        if (mNumberOfElements*GROWFAC > mKeys.length)
        {
            if ( parent != null ) {
                if ( (parent.mNumberOfElements+mNumberOfElements)*GROWFAC > parent.mKeys.length ) {
                    parent.resize(parent.mKeys.length*GROWFAC);
                    parent.put(key,value);
                    return;
                } else {
                    resize(mKeys.length * GROWFAC);
                }
            } else {
                resize(mKeys.length * GROWFAC);
            }
        }

        int idx = hash % mKeys.length;

        if (mKeys[idx] == 0 && mValues[idx] == null ) // new
        {
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx]   = key;
        }
        else if (mKeys[idx] == key )  // overwrite
        {
            mValues[idx] = value;
        } else {
            putNext(hash, key, value);
        }
    }

    final void putNext(int hash, int key, V value) {
        if ( next == null ) {
            int newSiz = mNumberOfElements/3;
            next = new FSTInt2ObjectMap<V>(newSiz);
        }
        next.putHash(key,value,hash,this);
    }

    final public V get(int key) {
        int hash = key & 0x7FFFFFFF;
        return getHash(key,hash);
    }

    static int miss = 0;
    static int hit = 0;
    final V getHash(int key, int hash)
    {
        final int idx = hash % mKeys.length;

        final int mKey = mKeys[idx];
        final Object mValue = mValues[idx];
        if (mKey == 0 && mValue == null) // not found
        {
//            hit++;
            return null;
        }
        else if (mKey == key)  // found
        {
//            hit++;
            return (V) mValue;
        } else {
            if ( next == null ) {
                return null;
            }
//            miss++;
            return next.getHash(key,hash);
        }
    }

    final void resize(int newSize)
    {
        newSize = FSTObject2IntMap.adjustSize(newSize);
        int[]    oldTabKey = mKeys;
        Object[] oldTabVal = mValues;

        mKeys = new int[newSize];
        mValues           = new Object[newSize];
        mNumberOfElements = 0;

        for (int n = 0; n < oldTabKey.length; n++)
        {
            if (oldTabKey[n] != 0 || oldTabVal[n] != null)
            {
                put(oldTabKey[n], (V)oldTabVal[n]);
            }
        }
        if ( next != null ) {
            FSTInt2ObjectMap oldNext = next;
            next = null;
            oldNext.rePut(this);
        }
    }

    private void rePut(FSTInt2ObjectMap<V> kfstObject2IntMap) {
        for (int i = 0; i < mKeys.length; i++) {
            int mKey = mKeys[i];
            if ( mKey != 0 || mValues[i] != null  ) {
                kfstObject2IntMap.put(mKey,(V)mValues[i]);
            }
        }
        if ( next != null ) {
            next.rePut(kfstObject2IntMap);
        }
    }

    public void clear() {
        if ( size() == 0 )
            return;
        FSTUtil.clear(mKeys);
        FSTUtil.clear(mValues);
        mNumberOfElements = 0;
        if ( next != null ) {
            next.clear();
        }
    }

    public static void main( String arg[] ) {
        for ( int jj = 0; jj < 100; jj++ ) {
            int count = 10; hit = miss = 0;
            FSTInt2ObjectMap<Object> map = new FSTInt2ObjectMap<Object>(count/10);
            HashMap<Object,Integer> hm = new HashMap<Object, Integer>(count/10);
            int obs[] = new int[count];

            map.put(0,"Hallo");
            System.out.println(map.get(0));

            for ( int i = 0; i < count;  i++ ) {
                obs[i] = i;
            }

            long tim = System.currentTimeMillis();
            for ( int i = 0; i < count;  i++ ) {
                map.put(obs[i],i);
            }
            System.out.println("fst "+(System.currentTimeMillis()-tim));
            System.out.println(map.get(0));

            tim = System.currentTimeMillis();
            for ( int i = 0; i < count;  i++ ) {
                hm.put(obs[i],i);
            }
            System.out.println("hmap "+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int j = 0; j < 10;  j++ ) {
                for ( int i = 0; i < count;  i++ ) {
                    if ( ! map.get(obs[i]).equals(i) ) {
                        System.out.println("bug "+i);
                    }
                    if ( map.get(i) == null ) {
                        System.out.println("bug "+i);
                    }
                }
            }
            System.out.println("h"+hit+" m "+miss);
            System.out.println("fst read "+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int j = 0; j < 10;  j++ ) {
                for ( int i = 0; i < count;  i++ ) {
                    if ( hm.get(obs[i]) != i ) {
                        System.out.println("bug "+i);
                    }
                }
            }
            System.out.println("hmap read "+(System.currentTimeMillis()-tim));
        }
    }

    public void dump() {
        for (int i = 0; i < mKeys.length; i++) {
            int mKey = mKeys[i];
            if ( mKey > 0 )
                System.out.println(""+mKey+" => "+mValues[i]);
        }
        if ( next != null )
            next.dump();
    }
}