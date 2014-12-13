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

import java.util.HashMap;

/**
 * Date: 26.02.13
 * Time: 18:35
 * To change this template use File | Settings | File Templates.
 */
public class FSTMap {
    public static final int INIT_SIZ = 65536;
    final int GAP = 4;

    Object keys[];
    int hash[];
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
        keys = new Object[len * 2];
        hash = new int[len];
        mask = siz - 1;
    }

    public Object get(Object key) {
        final int hc = key.hashCode();
        int hashIdx = (hc & mask) * GAP << 1;
        int loopCnt = 0;
        while (true) {
            if (keys[hashIdx] == null) {
                return null;
            }
            final int hidx2 = hashIdx >>> 1;
            if (hc == hash[hidx2]) {
                if (keys[hashIdx].equals(key)) {
                    return keys[hashIdx + 1];
                } else { // collision, try next
                    hashIdx += 2;
                    loopCnt++;
                    if (loopCnt == GAP) {
                        hashIdx = siz * GAP;
                    }
                }
            } else {
                // collision, try next
                hashIdx += 2;
                loopCnt++;
                if (loopCnt == GAP) {
                    hashIdx = siz * GAP;
                }
            }
        }
    }

    public void put(Object key, Object val) {
        int hc = key.hashCode();
        int hashIdx = (hc & mask) * GAP << 1;
        int loopCnt = 0;
        while (true) {
            if (keys[hashIdx] == null) {
                keys[hashIdx + 1] = val;
                keys[hashIdx] = key;
                hash[hashIdx / 2] = hc;
                return;
            }
            if (hc == hash[hashIdx / 2]) {
                if (keys[hashIdx].equals(key)) {
                    keys[hashIdx + 1] = val;
                    return;
                } else { // collision, try next
                    hashIdx += 2;
                    loopCnt++;
                    if (loopCnt == GAP) {
                        hashIdx = siz * GAP;
                    }
                }
            } else {
                // collision, try next
                hashIdx += 2;
                loopCnt++;
                if (loopCnt == GAP) {
                    hashIdx = siz * GAP;
                }
            }
        }
    }

    public static final int LOOP = 1000;

    public static void main(String[] arg) {
        int count = INIT_SIZ / 2;
        FSTMap map = new FSTMap();
        HashMap<Object, Integer> hm = new HashMap<Object, Integer>(INIT_SIZ);
        Object obs[] = new Object[count];
        Object obs1[] = new Object[count];

        for (int i = 0; i < count; i++) {
            obs[i] = "" + i;
            obs1[i] = "POK" + i + "POK";
        }

        System.out.println("---");
        long tim = System.currentTimeMillis();
        for (int jj = 0; jj < LOOP; jj++)
            for (int i = 0; i < count; i++) {
                map.put(obs[i], obs1[i]);
            }
        System.out.println("fst add" + (System.currentTimeMillis() - tim));

        tim = System.currentTimeMillis();
        for (int jj = 0; jj < LOOP; jj++)
            for (int i = 0; i < count; i++) {
                hm.put(obs[i], i);
            }
        System.out.println("hmap add" + (System.currentTimeMillis() - tim));

        tim = System.currentTimeMillis();
        for (int jj = 0; jj < LOOP; jj++)
            for (int i = 0; i < count; i++) {
                if (map.get(obs[i]) == null) {
                    System.out.println("bug " + i);
                }
            }
        System.out.println("fst read " + (System.currentTimeMillis() - tim));

        tim = System.currentTimeMillis();
        for (int jj = 0; jj < LOOP; jj++)
            for (int i = 0; i < count; i++) {
                if (hm.get(obs[i]) == null) {
                    System.out.println("bug " + i);
                }
            }
        System.out.println("hmap read " + (System.currentTimeMillis() - tim));
    }
}
