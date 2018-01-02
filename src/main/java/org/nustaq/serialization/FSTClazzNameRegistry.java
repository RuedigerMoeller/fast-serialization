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
import org.nustaq.serialization.util.FSTObject2IntMap;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 00:34
 *
 * maps classnames => id and vice versa.
 *
 * FSTConfiguration holds a parent containing default mappings (e.g. created by
 * registerClass ). A stream instance then creates a temporary instance to record/id encode
 * classes dynamically during serialization. This way a class name is only written once per
 * object graph.
 *
 * This class is thread safe,
 *
 */
public class FSTClazzNameRegistry {

    public static final int LOWEST_CLZ_ID = 3;
    public static final int FIRST_USER_CLZ_ID = 1000;

    FSTIdentity2IdMap clzToId;
    FSTClazzInfo idToClz[];
    FSTClazzNameRegistry parent;
    int classIdCount = LOWEST_CLZ_ID;

    public FSTClazzNameRegistry(FSTClazzNameRegistry par) {
        parent = par;
        if ( parent != null ) {
            classIdCount = Math.max(FIRST_USER_CLZ_ID,parent.classIdCount+1);
            clzToId = new FSTIdentity2IdMap(13);
            idToClz = new FSTClazzInfo[31];
        } else {
            clzToId = new FSTIdentity2IdMap(FSTObject2IntMap.adjustSize(400));
            idToClz = new FSTClazzInfo[200];
        }
    }

    public void clear() {
        if ( clzToId.size() > 0 ) {
            clzToId.clear();
            //idToClz.clear();
        }
        classIdCount = LOWEST_CLZ_ID;
        if ( parent != null ) {
            classIdCount = Math.max(FIRST_USER_CLZ_ID,parent.classIdCount+1);
        }
    }

    // for read => always increase handle (wg. replaceObject)
    public void registerClass(Class c,FSTConfiguration conf) {
        if ( getIdFromClazz(c) != Integer.MIN_VALUE ) {
            return;
        }
        registerClassNoLookup(c,null,conf);
    }

    private void registerClassNoLookup(Class c,FSTClazzInfo cli, FSTConfiguration conf) {
        addClassMapping(c, classIdCount++,cli,conf);
    }

    public void registerClass( Class c, int code, FSTConfiguration conf) {
        if ( getIdFromClazz(c) != Integer.MIN_VALUE ) {
            return;
        }
        addClassMapping(c, code, null,conf);
    }

    protected void addClassMapping( Class c, int id, FSTClazzInfo clInfo, FSTConfiguration conf ) {
        clzToId.put(c, id);
        if (clInfo==null)
            clInfo = conf.getCLInfoRegistry().getCLInfo(c, conf);
        if (idToClz.length<=id)
        {
            final FSTClazzInfo[] tmp = new FSTClazzInfo[id + 100];
            System.arraycopy(idToClz,0,tmp,0,idToClz.length);
            idToClz = tmp;
        }
        idToClz[id] = clInfo;
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

    public void encodeClass(FSTEncoder out, FSTClazzInfo ci) throws IOException {
        int clzId = ci.getClzId();
        if ( clzId >= 0 ) {
            out.writeFShort((short) clzId); // > 2 !!
        } else {
            if ( ci.isAsciiNameShortString) {
                final Class aClass = ci.getClazz();
                int clid = getIdFromClazz(aClass);
                if ( clid != Integer.MIN_VALUE ) {
                    out.writeFShort((short) clid); // > 2 !!
                } else {
                    // ugly hack, also making assumptions about
                    // on how the encoder works internally
                    final byte[] bufferedName = ci.getBufferedName();
                    out.writeFShort((short) 1); // no direct cl id ascii enc
                    out.writeFInt((char) bufferedName.length);
                    out.writeRawBytes(bufferedName,0,bufferedName.length);
                    registerClassNoLookup(aClass,ci,ci.conf);
                }
            } else {
                encodeClass(out,ci.getClazz());
            }
        }
    }

    public void encodeClass(FSTEncoder out, Class c) throws IOException {
        int clid = getIdFromClazz(c);
        if ( clid != Integer.MIN_VALUE ) {
            out.writeFShort((short) clid); // > 2 !!
        } else {
            encodeClassName(out, c, out.getConf() );
        }
    }

    private void encodeClassName(FSTEncoder out, Class c, FSTConfiguration conf) throws IOException {
        out.writeFShort((short) 0); // no direct cl id
        out.writeStringUTF(c.getName());
        registerClassNoLookup(c,null,conf);
    }

    public FSTClazzInfo decodeClass(FSTDecoder in, FSTConfiguration conf) throws IOException, ClassNotFoundException {
        short c = in.readFShort();
        if ( c < LOWEST_CLZ_ID ) {
            // full class name
            String clName;
            if ( c==0) {
                clName = in.readStringUTF();
            }
            else {
                clName = in.readStringAsc();
            }
            Class cl = classForName(clName,conf);
            final FSTClazzInfo clInfo = conf.getCLInfoRegistry().getCLInfo(cl, conf);
            registerClassNoLookup(cl,clInfo,conf);
            return clInfo;
        } else {
            FSTClazzInfo aClass = getClazzFromId(c);
            if ( aClass == null ) {
                throw new RuntimeException("unable to find class for code "+c);
            }
            return aClass;
        }
    }

    HashMap<String,Class> classCache = new HashMap<String, Class>(200);
    AtomicBoolean classCacheLock = new AtomicBoolean(false);
    public Class classForName(String clName, FSTConfiguration conf) throws ClassNotFoundException {
        if ( parent != null ) {
            return parent.classForName(clName,conf);
        }
        try {
            while (!classCacheLock.compareAndSet(false, true)) ;
            Class res = classCache.get(clName);
            if (res == null) {
                try {
                    res = Class.forName(clName, false, conf.getClassLoader());
                } catch (Throwable th) {
                    if (clName.endsWith("_Struct")) // hack to define struct proxys on the fly if sent from another process
                    {
                        throw new RuntimeException("code commented out, check source");
//                        try {
//                            clName = clName.substring(0, clName.length() - "_Struct".length());
//                            Class onHeapStructClz = classCache.get(clName);
//                            if (onHeapStructClz == null)
//                                onHeapStructClz = Class.forName(clName, false, conf.getClassLoader() );
//                            res = FSTStructFactory.getInstance().getProxyClass(onHeapStructClz);
//                        } catch (Throwable th1) {
//                            FSTUtil.<RuntimeException>rethrow(th1);
//                        }
                    } else if ( clName.endsWith("_ActorProxy") ) {
                        // same as above for actors. As there is a custom serializer defined for actors, just instantiate
                        // actor clazz
                        String clName0 = clName;
                        clName = clName.substring(0, clName.length() - "_ActorProxy".length());
                        Class actorClz = classCache.get(clName);
                        if (actorClz == null) {
                            try {
                                actorClz = Class.forName(clName, false, conf.getClassLoader());
                            } catch (ClassNotFoundException clf) {
                                if ( conf.getLastResortResolver() != null ) {
                                    Class aClass = conf.getLastResortResolver().getClass(clName0);
                                    if ( aClass != null )
                                        return aClass;
                                }
                                FSTUtil.<RuntimeException>rethrow(clf);
                            }
                        }
                        return actorClz;
                    } else {
                        if ( conf.getLastResortResolver() != null ) {
                            Class aClass = conf.getLastResortResolver().getClass(clName);
                            if ( aClass != null )
                                return aClass;
                        }
                        throw new RuntimeException("class not found CLASSNAME:" + clName + " loader:"+conf.getClassLoader(), th);
                    }
                }
                if (res != null) {
                    classCache.put(clName, res);
                }
            }
            return res;
        } finally {
            classCacheLock.set(false);
        }
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
            if ( c < 0 || c >= idToClz.length )
                return null;
            return idToClz[c];
        } else {
            return res;
        }
    }


}
