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

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 20.11.12
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
public class FSTObject2IntMap<K>
{
    static int[] prim = {
            3,5,7, 11, 13, 17, 19, 23, 29, 37, 67, 97, 139,
            211, 331, 641, 1097, 1531, 2207, 3121, 5059, 7607, 10891,
            15901, 19993, 30223, 50077, 74231,99991, 150001, 300017,
            1000033,1500041,200033,3000077,5000077,10000019

    };
    private static final int GROFAC = 2;

    static int adjustSize(int size) {
        for (int i = 0; i < prim.length-1; i++) {
            if ( size < prim[i] ) {
                return prim[i];
            }
        }
        return size;
    }

    public Object  mKeys[];
    public int     mValues[];
    public int     mNumberOfElements;
    FSTObject2IntMap<K> next;
    boolean checkClazzOnEquals = false;

    public FSTObject2IntMap(int initialSize, boolean checkClassOnequals )
    {
        if (initialSize < 2)
        {
            initialSize = 2;
        }

        initialSize = adjustSize(initialSize*2);

        mKeys = new Object[initialSize];
        mValues = new int[initialSize];
        mNumberOfElements = 0;
        this.checkClazzOnEquals = checkClassOnequals;
    }

    public int size()
    {
        return mNumberOfElements + (next != null ? next.size():0);
    }

    final public void put(K key, int value)
    {
        int hash = key.hashCode() & 0x7FFFFFFF;
        putHash(key, value, hash, this);
    }

    final void putHash(K key, int value, int hash, FSTObject2IntMap<K> parent) {
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

        int idx = hash % mKeys.length;

        if (mKeys[idx] == null ) // new
        {
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx]   = key;
        }
        else if (mKeys[idx].equals(key) && (!checkClazzOnEquals||mKeys[idx].getClass() == key.getClass()) )    // overwrite
        {
            mValues[idx] = value;
        } else {
            putNext(hash, key, value);
        }
    }

    final K removeHash(K key, int hash)
    {
        final int idx = hash % mKeys.length;

        final Object mKey = mKeys[idx];
        if (mKey == null ) // not found
        {
//            hit++;
            return null;
        }
        else if (mKey.equals(key) && (!checkClazzOnEquals||mKeys[idx].getClass() == key.getClass()) )  // found
        {
//            hit++;
            K val = (K) mKeys[idx];
            mValues[idx] = 0; mKeys[idx] = null;
            mNumberOfElements--;
            return val;
        } else {
            if ( next == null ) {
                return null;
            }
//            miss++;
            return next.removeHash(key, hash);
        }
    }

    final void putNext(final int hash, final K key, final int value) {
        if ( next == null ) {
            int newSiz = mNumberOfElements/3;
            next = new FSTObject2IntMap<K>(newSiz,checkClazzOnEquals);
        }
        next.putHash(key,value,hash, this);
    }

    final public int get(final K key) {
        final int hash = key.hashCode() & 0x7FFFFFFF;
        //return getHash(key,hash); inline =>
        final int idx = hash % mKeys.length;

        final Object mapsKey = mKeys[idx];
        if (mapsKey == null ) // not found
        {
            return Integer.MIN_VALUE;
        }
        else if (mapsKey.equals(key) && (!checkClazzOnEquals||mapsKey.getClass() == key.getClass()) )  // found
        {
            return mValues[idx];
        } else {
            if ( next == null ) {
                return Integer.MIN_VALUE;
            }
            int res = next.getHash(key, hash);
            return res;
        }
        // <== inline
    }

    static int miss = 0;
    static int hit = 0;
    final int getHash(final K key, final int hash)
    {
        final int idx = hash % mKeys.length;

        final Object mapsKey = mKeys[idx];
        if (mapsKey == null ) // not found
        {
            return Integer.MIN_VALUE;
        }
        else if (mapsKey.equals(key) && (!checkClazzOnEquals||mapsKey.getClass() == key.getClass()))  // found
        {
            return mValues[idx];
        } else {
            if ( next == null ) {
                return Integer.MIN_VALUE;
            }
            int res = next.getHash(key, hash);
            return res;
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

        for (int n = 0; n < oldTabKey.length; n++)
        {
            if (oldTabKey[n] != null)
            {
                put((K)oldTabKey[n], oldTabVal[n]);
            }
        }
        if ( next != null ) {
            FSTObject2IntMap oldNext = next;
            next = null;
            oldNext.rePut(this);
        }
    }

    private void rePut(FSTObject2IntMap<K> kfstObject2IntMap) {
        for (int i = 0; i < mKeys.length; i++) {
            Object mKey = mKeys[i];
            if ( mKey != null ) {
                kfstObject2IntMap.put((K) mKey,mValues[i]);
            }
        }
        if ( next != null ) {
            next.rePut(kfstObject2IntMap);
        }
    }

    public void clear() {
        if ( size() == 0 ) {
            return;
        }
        FSTUtil.clear(mKeys);
        FSTUtil.clear(mValues);
        mNumberOfElements = 0;
        if ( next != null ) {
            next.clear();
        }
    }

    public static void main( String arg[] ) {
        for ( int jj = 0; jj < 100; jj++ ) {
            int count = 500000; hit = miss = 0;
            FSTObject2IntMap map = new FSTObject2IntMap(count/10,false);
            HashMap<Object,Integer> hm = new HashMap<Object, Integer>(count/10);
            Object obs[] = new Object[count];

            for ( int i = 0; i < count;  i++ ) {
//                obs[i] = ""+i;
                obs[i] = ""+Math.random();
            }

            long tim = System.currentTimeMillis();
            for ( int i = 0; i < count;  i++ ) {
                map.put(obs[i],i);
            }
            System.out.println("-----------fst PUT "+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int i = 0; i < count;  i++ ) {
                hm.put(obs[i],i);
            }
            System.out.println("hmap PUT "+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int j = 0; j < 10;  j++ ) {
                for ( int i = 0; i < count;  i++ ) {
                    if ( map.get(obs[i]) != i ) {
                        //System.out.println("bug "+i);
                    }
                }
            }
            System.out.println("fst GET "+(System.currentTimeMillis()-tim)+" "+map.size());

            tim = System.currentTimeMillis();
            for ( int j = 0; j < 10;  j++ ) {
                for ( int i = 0; i < count;  i++ ) {
                    if ( hm.get(obs[i]) != i ) {
                        //System.out.println("bug "+i);
                    }
                }
            }
            System.out.println("hmap GET "+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int j = 0; j < 10;  j++ ) {
                for ( int i = 0; i < count;  i++ ) {
                    if ( map.get("Poki") == i ) {
                        //System.out.println("bug "+i);
                    }
                }
            }
            System.out.println("fst FAIL "+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            Integer someInt = -134;
            for ( int j = 0; j < 10;  j++ ) {
                for ( int i = 0; i < count;  i++ ) {
                    if ( hm.get("Poki") == someInt ) {
                        //System.out.println("bug "+i);
                    }
                }
            }

            System.out.println("hmap FAIL "+(System.currentTimeMillis()-tim));
        }
    }
}