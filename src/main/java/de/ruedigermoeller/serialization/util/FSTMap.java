package de.ruedigermoeller.serialization.util;

import java.util.HashMap;

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
 * Date: 26.02.13
 * Time: 18:35
 * To change this template use File | Settings | File Templates.
 */
public class FSTMap {
    public static final int INIT_SIZ = 65536;
    final int GAP = 4;

    Object keys[];
    int    hash[];
    int siz = 0;
    int collisionIndex;
    int mask;

    public FSTMap() {
        allocWithSize(INIT_SIZ);
    }

    // siz = power of 2
    void allocWithSize(int siz) {
        this.siz = siz;
        int len = siz * GAP + (siz * GAP) / 2;
        keys = new Object[len*2];
        hash = new int[len];
        mask = siz-1;
    }

    public Object get(Object key) {
        final int hc = key.hashCode();
        int hashIdx = (hc &mask)*GAP<<1;
        int loopCnt = 0;
        while( true ) {
            if (keys[hashIdx] == null ) {
                return null;
            }
            final int hidx2 = hashIdx >>> 1;
            if ( hc == hash[hidx2] ) {
                if ( keys[hashIdx].equals(key) ) {
                    return keys[hashIdx+1];
                } else { // collision, try next
                    hashIdx+=2;
                    loopCnt++;
                    if (loopCnt==GAP) {
                        hashIdx = siz * GAP;
                    }
                }
            } else {
                // collision, try next
                hashIdx+=2;
                loopCnt++;
                if (loopCnt==GAP) {
                    hashIdx = siz * GAP;
                }
            }
        }
    }

    public void put( Object key, Object val ) {
        int hc = key.hashCode();
        int hashIdx = (hc &mask)*GAP<<1;
        int loopCnt = 0;
        while( true ) {
            if (keys[hashIdx] == null ) {
                keys[hashIdx+1] = val;
                keys[hashIdx] = key;
                hash[hashIdx/2] = hc;
                return;
            }
            if ( hc == hash[hashIdx/2] ) {
                if ( keys[hashIdx].equals(key) ) {
                    keys[hashIdx+1] = val;
                    return;
                } else { // collision, try next
                    hashIdx+=2;
                    loopCnt++;
                    if (loopCnt==GAP) {
                        hashIdx = siz * GAP;
                    }
                }
            } else {
                // collision, try next
                hashIdx+=2;
                loopCnt++;
                if (loopCnt==GAP) {
                    hashIdx = siz * GAP;
                }
            }
        }
    }

    public static final int LOOP = 1000;
    public static void main(String[] arg) {
            int count = INIT_SIZ/2;
            FSTMap map = new FSTMap();
            HashMap<Object,Integer> hm = new HashMap<Object, Integer>(INIT_SIZ);
            Object obs[] = new Object[count];
            Object obs1[] = new Object[count];

            for ( int i = 0; i < count;  i++ ) {
                obs[i] = ""+i;
                obs1[i] = "POK"+i+"POK";
            }

            System.out.println("---");
            long tim = System.currentTimeMillis();
            for ( int jj = 0; jj < LOOP;  jj++ )
                for ( int i = 0; i < count;  i++ ) {
                    map.put(obs[i],obs1[i]);
                }
            System.out.println("fst add"+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int jj = 0; jj < LOOP;  jj++ )
                for ( int i = 0; i < count;  i++ ) {
                    hm.put(obs[i],i);
                }
            System.out.println("hmap add"+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int jj = 0; jj < LOOP;  jj++ )
                for ( int i = 0; i < count;  i++ ) {
                    if ( map.get(obs[i]) == null ) {
                        System.out.println("bug "+i);
                    }
                }
            System.out.println("fst read "+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int jj = 0; jj < LOOP;  jj++ )
                for ( int i = 0; i < count;  i++ ) {
                    if ( hm.get(obs[i]) == null ) {
                        System.out.println("bug "+i);
                    }
                }
            System.out.println("hmap read "+(System.currentTimeMillis()-tim));
    }
}
