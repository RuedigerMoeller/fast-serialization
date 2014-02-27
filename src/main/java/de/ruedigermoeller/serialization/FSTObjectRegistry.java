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
package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.util.*;

import java.util.IdentityHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 11.11.12
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public final class FSTObjectRegistry {

    public static final int OBJ_DIVISOR = 16;
    boolean disabled = false;
    FSTIdentity2IdMap objects = new FSTIdentity2IdMap(97); // object => id
    FSTInt2ObjectMap idToObject = new FSTInt2ObjectMap(17);
    FSTObject2IntMap equalsMap = new FSTObject2IntMap(17,true); // object => handle

    FSTConfiguration conf;
    FSTClazzInfoRegistry reg;
    Object reuseMap[] = new Object[4000];
    private int highestPos;

    public FSTObjectRegistry(FSTConfiguration conf) {
        this.conf = conf;
        disabled = !conf.isShareReferences();
        reg = conf.getCLInfoRegistry();
    }

    public void clearFully() {
        objects.clear();
        idToObject.clear();
        equalsMap.clear();
        disabled = !conf.isShareReferences();
        FSTUtil.clear(reuseMap);
    }

    public void clearForRead() {
        idToObject.clear();
        disabled = !conf.isShareReferences();
        FSTUtil.clear(reuseMap,highestPos);
        highestPos = 0;
    }

    public void clearForWrite() {
        objects.clear();
        equalsMap.clear();
        disabled = !conf.isShareReferences();
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
        } else {
            idToObject.get(handle);
        }
        return idToObject.get(FSTConfiguration.getInt(handle));
    }

    public void replace(Object old, Object replaced, int streamPos) {
        int pos = streamPos / OBJ_DIVISOR;
        final Object[] reuseMap = this.reuseMap;
        if ( pos < reuseMap.length ) {
            if ( this.reuseMap[pos] == old ) {
                this.reuseMap[pos] = replaced;
            } else {
                if ( this.reuseMap[pos] == null ) // BUG !!
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
    public int registerObjectForWrite(Object o, boolean dontCheckEqual, int streamPosition, FSTClazzInfo clzInfo, int reUseType[]) {
        if (disabled) {
            return Integer.MIN_VALUE;
        }
        final Class clazz = o.getClass();
        if ( clzInfo == null ) { // array oder enum oder primitive
            clzInfo = reg.getCLInfo(clazz);
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
        boolean reUseEquals = !dontCheckEqual && clzInfo != null && !(clazz.isArray() || clazz.isPrimitive());
        if ( reUseEquals ) {
            reUseEquals = reUseEquals && (clzInfo.isEqualIsIdentity() || clzInfo.isEqualIsBinary());
            if (  reUseEquals ) {
                int integer = equalsMap.get(o);
                if ( integer != Integer.MIN_VALUE ) {
                    reUseType[0] = 1;
                    return integer;
                }
            }
        }
//        objects.put(o, streamPosition); // done with putorget above
        if ( reUseEquals ) {
            equalsMap.put(o,streamPosition);
        }
        return Integer.MIN_VALUE;
    }

    boolean isReuseEqualsByIdentity(Class aClass, FSTClazzInfo serializationInfo) {
        return serializationInfo.isEqualIsIdentity();
    }

    boolean isReuseByCopy(Class aClass, FSTClazzInfo serializationInfo) {
        return serializationInfo.isEqualIsBinary();
    }

    public int getObjectSize() {
        return objects.size();
    }

}