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
package org.nustaq.serialization;

import org.nustaq.serialization.util.FSTIdentity2IdMap;
import org.nustaq.serialization.util.FSTInt2ObjectMap;
import org.nustaq.serialization.util.FSTUtil;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 11.11.12
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public final class FSTObjectRegistry {

    public static final int OBJ_DIVISOR = 16;
    public static int POS_MAP_SIZE = 1000; // reduce this for testing

    boolean disabled = false;
    FSTIdentity2IdMap objects = new FSTIdentity2IdMap(11); // object => id
    FSTInt2ObjectMap idToObject = new FSTInt2ObjectMap(11);

    Object reuseMap[] = new Object[POS_MAP_SIZE];
    private int highestPos = -1;

    public FSTObjectRegistry(FSTConfiguration conf) {
        disabled = !conf.isShareReferences();
    }

    public void clearForRead(FSTConfiguration conf) {
        disabled = !conf.isShareReferences();
        if ( !disabled ) {
            if ( idToObject.mKeys.length > 6 * idToObject.size() && idToObject.size() > 0 ) {
                // use fastClear method to avoid creating new FSTInt2ObjectMap in order to reduce memory footprint
                idToObject.fastClear();
            } else {
                idToObject.clear();
            }
            if ( highestPos > -1 )
                FSTUtil.clear( reuseMap, highestPos + 1 );
        }
        highestPos = -1;
    }

    public void clearForWrite(FSTConfiguration conf) {
        disabled = !conf.isShareReferences();
        if ( ! disabled ) {
            if ( objects.size() > 0 && objects.keysLength() > 6 * objects.size() ) {
            	// use fastClear method to avoid creating new FSTIdentity2IdMap in order to reduce memory footprint
                objects.fastClear();
            } else {
                objects.clear();
            }
        }
    }

    public Object getReadRegisteredObject(int handle) {
        if (disabled) {
            return null;
        }
        int pos = handle / OBJ_DIVISOR;
        if ( pos < reuseMap.length ) {
            if ( reuseMap[pos] == null ) {
                return null;
            } else {
                Object candidate = idToObject.get(handle);
                if ( candidate == null )
                    return reuseMap[pos];
                return candidate;
            }
        }
        return idToObject.get(handle);
    }

    public void replace(Object old, Object replaced, int streamPos) {
        int pos = streamPos / OBJ_DIVISOR;
        final Object[] reuseMap = this.reuseMap;
        if ( pos < reuseMap.length ) {
            if ( this.reuseMap[pos] == old ) {
                this.reuseMap[pos] = replaced;
            } else {
                if ( this.reuseMap[pos] == null || reuseMap[pos] == old )
                {
                    this.reuseMap[pos] = replaced;
                } else {
                    idToObject.put(streamPos,replaced);
                }
            }
        } else {
            idToObject.put(streamPos, replaced);
        }
    }

    public void registerObjectForRead(Object o, int streamPosition) {
        if (disabled /*|| streamPosition <= lastRegisteredReadPos*/) {
            return;
        }
//        System.out.println("POK REGISTER AT READ:"+streamPosition+" : "+o);
        int pos = streamPosition / OBJ_DIVISOR;
        Object[] reuseMap = this.reuseMap;
        if ( pos < reuseMap.length ) {
            highestPos = pos > highestPos ? pos : highestPos;
            if ( this.reuseMap[pos] == null ) {
                this.reuseMap[pos] = o;
            } else {
                idToObject.put(streamPosition,o);
            }
        } else {
            idToObject.put(streamPosition,o);
        }
    }

    /**
     * add an object to the register, return handle if already present. Called during write only
     *
     * @param o
     * @param streamPosition
     * @return 0 if added, handle if already present
     */
    public int registerObjectForWrite(Object o, int streamPosition, FSTClazzInfo clzInfo, int reUseType[]) {
        if (disabled) {
            return Integer.MIN_VALUE;
        }
//        System.out.println("REGISTER AT WRITE:"+streamPosition+" "+o.getClass().getSimpleName());
//        final Class clazz = o.getClass();
        if ( clzInfo == null ) { // array oder enum oder primitive
            // unused ?
//            clzInfo = reg.getCLInfo(clazz);
        } else if ( clzInfo.isFlat() ) {
            return Integer.MIN_VALUE;
        }
        int handle = objects.putOrGet(o,streamPosition);
        if ( handle >= 0 ) {
//            if ( idToObject.get(handle) == null ) { // (*) (can get improved)
//                idToObject.add(handle, o);
//            }
            reUseType[0] = 0;
            return handle;
        }
        return Integer.MIN_VALUE;
    }

    public int getObjectSize() {
        return objects.size();
    }

}
