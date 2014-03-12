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

import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;
import de.ruedigermoeller.serialization.util.FSTIdentity2IdMap;
import de.ruedigermoeller.serialization.util.FSTInt2ObjectMap;
import de.ruedigermoeller.serialization.util.FSTObject2IntMap;
import de.ruedigermoeller.serialization.util.FSTUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 00:34
 * To change this template use File | Settings | File Templates.
 */
public class FSTClazzNameRegistry {

    FSTIdentity2IdMap clzToId;
    FSTInt2ObjectMap idToClz;
    FSTClazzNameRegistry parent;
    FSTConfiguration conf;
    int classIdCount = 3;


    public FSTClazzNameRegistry(FSTClazzNameRegistry par, FSTConfiguration conf) {
        parent = par;
        this.conf = conf;
        if ( parent != null ) {
            classIdCount = parent.classIdCount+1;
            clzToId = new FSTIdentity2IdMap(13);
            idToClz = new FSTInt2ObjectMap(13);
        } else {
            clzToId = new FSTIdentity2IdMap(FSTObject2IntMap.adjustSize(400));
            idToClz = new FSTInt2ObjectMap(FSTObject2IntMap.adjustSize(400));
        }
    }

    public void clear() {
        clzToId.clear();
        idToClz.clear();
        classIdCount = 3;
        if ( parent != null ) {
            classIdCount = parent.classIdCount+1;
        }
    }

    // for read => always increase handle (wg. replaceObject)
    void registerClass( Class c ) {
        if ( getIdFromClazz(c) != Integer.MIN_VALUE ) {
            return;
        }
        registerClassNoLookup(c);
    }

    private void registerClassNoLookup(Class c) {
        addClassMapping(c, classIdCount++);
        Class pred[] = conf.getCLInfoRegistry().getCLInfo(c).getPredict();
        if ( pred != null ) {
            for (int i = 0; i < pred.length; i++) {
                Class aClass = pred[i];
                registerClass(aClass);
            }
        }
    }

    // for read => always increase handle (wg. replaceObject)
    public void registerClass( Class c, int code) {
        if ( getIdFromClazz(c) != Integer.MIN_VALUE ) {
            return;
        }
        addClassMapping(c, code);
    }

    protected void addClassMapping( Class c, int id ) {
        clzToId.put(c, id);
        FSTClazzInfo clInfo = conf.getCLInfoRegistry().getCLInfo(c);
        idToClz.put(id, clInfo);
        if ( parent == null ) {
            clInfo.setClzId(id);
        }
    }

    public int getIdFromClazz(Class c) {
        int res = Integer.MIN_VALUE;
        if ( parent != null ) {
            res = parent.getIdFromClazz(c);
        }
        if ( res == Integer.MIN_VALUE ) {
            res = clzToId.get(c);
        }
        return res;
    }

    public void encodeClass(FSTObjectOutput out, FSTClazzInfo ci) throws IOException {
        int clzId = ci.getClzId();
        if ( clzId >= 0 ) {
            out.writeCShort((short) clzId); // > 2 !!
        } else {
            if ( ci.isAsciiName ) {
                final Class aClass = ci.getClazz();
                int clid = getIdFromClazz(aClass);
                if ( clid != Integer.MIN_VALUE ) {
                    out.writeCShort((short) clid); // > 2 !!
                } else {
                    out.writeCShort((short) 1); // no direct cl id ascii enc
                    final byte[] bufferedName = ci.getBufferedName();
                    out.writeFByte((char) bufferedName.length);
                    out.writeFByteArr(bufferedName,bufferedName.length);
                    registerClassNoLookup(aClass);
                }
            } else {
                encodeClass(out,ci.getClazz());
            }
        }
    }

    public void encodeClass(FSTObjectOutput out, Class c) throws IOException {
        int clid = getIdFromClazz(c);
        if ( clid != Integer.MIN_VALUE ) {
            out.writeCShort((short) clid); // > 2 !!
        } else {
            encodeClassName(out, c);
        }
    }

    private void encodeClassName(FSTObjectOutput out, Class c) throws IOException {
        out.writeCShort((short) 0); // no direct cl id
        out.writeStringUTF(c.getName());
        registerClassNoLookup(c);
    }

    public FSTClazzInfo decodeClass(FSTObjectInput in) throws IOException, ClassNotFoundException {
        short c = in.readCShort();
        if ( c < 3 ) {
            // full class name
            String clName;
            if ( c==0)
                clName = in.readStringUTF();
            else
                clName = in.readStringAsc();
            Class cl = classForName(clName);
            registerClassNoLookup(cl);
            return conf.getCLInfoRegistry().getCLInfo(cl);
        } else {
            FSTClazzInfo aClass = getClazzFromId(c);
            if ( aClass == null ) {
                throw new RuntimeException("unable to decode class from code "+c);
            }
            return aClass;
        }
    }

    HashMap<String,Class> classCache = new HashMap<String, Class>(200);
    AtomicBoolean classCacheLock = new AtomicBoolean(false);
    public Class classForName(String clName) throws ClassNotFoundException {
        if ( parent != null ) {
            return parent.classForName(clName);
        }
        while( ! classCacheLock.compareAndSet(false,true) );
        Class res = classCache.get(clName);
        if ( res == null ) {
            try {
                res = Class.forName(clName, false, conf.getClassLoader() );
            } catch ( Throwable th ) {
                if ( clName.endsWith("_Struct") ) // hack to define struct proxys on the fly if sent from another process
                {
                    try {
                        clName = clName.substring(0,clName.length()-"_Struct".length());
                        Class onHeapStructClz = classCache.get(clName);
                        if ( onHeapStructClz == null )
                            onHeapStructClz = Class.forName(clName);
                        res = FSTStructFactory.getInstance().getProxyClass(onHeapStructClz);
                    } catch (Throwable th1) {
                        throw FSTUtil.rethrow(th1);
                    }
                } else {
                    throw new RuntimeException("CLASSNAME:"+clName,th);
                }
            }
            if ( res != null ) {
                classCache.put(clName, res);
            }
        }
        classCacheLock.set(false);
        return res;
    }

    public void registerClazzFromOtherLoader( Class cl ) {
        while( ! classCacheLock.compareAndSet(false,true) );
        classCache.put(cl.getName(),cl);
        classCacheLock.set(false);
    }

    public FSTClazzInfo getClazzFromId(int c) {
        FSTClazzInfo res = null;
        if ( parent != null ) {
            res = parent.getClazzFromId(c);
        }
        if ( res == null ) {
            return (FSTClazzInfo) idToClz.get(c);
        } else {
            return res;
        }
    }


}
