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

import org.nustaq.serialization.util.FSTMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 03.11.12
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class FSTClazzInfoRegistry {

    FSTMap mInfos = new FSTMap(97);
    FSTSerializerRegistry serializerRegistry = new FSTSerializerRegistry();
    boolean ignoreAnnotations = false;
    final AtomicBoolean rwLock = new AtomicBoolean(false);
    private boolean structMode = false;

    public static void addAllReferencedClasses(Class cl, ArrayList<String> names, String filter) {
        HashSet<String> names1 = new HashSet<String>();
        addAllReferencedClasses(cl, names1, new HashSet<String>(),filter);
        names.addAll(names1);
    }

    static void addAllReferencedClasses(Class cl, HashSet<String> names, HashSet<String> topLevelDone, String filter) {
        if ( cl == null || topLevelDone.contains(cl.getName()) || !cl.getName().startsWith(filter))
            return;
        topLevelDone.add(cl.getName());
        Field[] declaredFields = cl.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field declaredField = declaredFields[i];
            Class<?> type = declaredField.getType();
            if ( ! type.isPrimitive() && ! type.isArray() ) {
                names.add(type.getName());
                addAllReferencedClasses(type,names,topLevelDone,filter);
            }
        }
        Class[] declaredClasses = cl.getDeclaredClasses();
        for (int i = 0; i < declaredClasses.length; i++) {
            Class declaredClass = declaredClasses[i];
            if ( ! declaredClass.isPrimitive() && ! declaredClass.isArray() ) {
                names.add(declaredClass.getName());
                addAllReferencedClasses(declaredClass, names,topLevelDone,filter);
            }
        }
        Method[] declaredMethods = cl.getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {
            Method declaredMethod = declaredMethods[i];
            Class<?> returnType = declaredMethod.getReturnType();
            if ( ! returnType.isPrimitive() && ! returnType.isArray() ) {
                names.add(returnType.getName());
                addAllReferencedClasses(returnType, names,topLevelDone,filter);
            }
            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            for (int j = 0; j < parameterTypes.length; j++) {
                Class<?> parameterType = parameterTypes[j];
                if ( ! parameterType.isPrimitive() && ! parameterType.isArray() ) {
                    names.add(parameterType.getName());
                    addAllReferencedClasses(parameterType, names,topLevelDone,filter);
                }
            }
        }

        Class[] classes = cl.getDeclaredClasses();
        for (int i = 0; i < classes.length; i++) {
            Class aClass = classes[i];
            if ( ! aClass.isPrimitive() && ! aClass.isArray() ) {
                names.add(aClass.getName());
                addAllReferencedClasses(aClass, names,topLevelDone,filter);
            }
        }

        Class enclosingClass = cl.getEnclosingClass();
        if ( enclosingClass != null ) {
            names.add(enclosingClass.getName());
            addAllReferencedClasses(enclosingClass,names,topLevelDone,filter);
        }

        names.add(cl.getName());
        addAllReferencedClasses(cl.getSuperclass(), names,topLevelDone,filter);
        Class[] interfaces = cl.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if ( ! anInterface.isPrimitive() && ! anInterface.isArray() ) {
                names.add(anInterface.getName());
                addAllReferencedClasses(anInterface, names,topLevelDone,filter);
            }
        }
    }

    public FSTClazzInfoRegistry() {
    }

    public FSTClazzInfo getCLInfo(Class c, FSTConfiguration conf) {
    	return getCLInfo(c, conf, null);
    }
    
    public FSTClazzInfo getCLInfo(Class c, FSTConfiguration conf, long[] fieldsUniqueId) {
        while(!rwLock.compareAndSet(false,true));
        try {
            FSTClazzInfo res = (FSTClazzInfo) mInfos.get(c);
            if (res == null) {
                if (c == null) {
                    throw new NullPointerException("Class is null");
                }
                if ( conf.getVerifier() != null ) {
                    if ( ! conf.getVerifier().allowClassDeserialization(c) )
                        throw new RuntimeException("tried to deserialize forbidden class "+c.getName() );
                }
                res = new FSTClazzInfo(conf, c, this, ignoreAnnotations, fieldsUniqueId);
                mInfos.put(c, res);
            }
            return res;
        } finally {
            rwLock.set(false);
        }
    }

    public FSTSerializerRegistry getSerializerRegistry() {
        return serializerRegistry;
    }

    public final boolean isIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    public void setIgnoreAnnotations(boolean ignoreAnnotations) {
        this.ignoreAnnotations = ignoreAnnotations;
    }


    public void setSerializerRegistryDelegate(FSTSerializerRegistryDelegate delegate) {
        serializerRegistry.setDelegate(delegate);
    }

    public FSTSerializerRegistryDelegate getSerializerRegistryDelegate() {
        return serializerRegistry.getDelegate();
    }

    public void setStructMode(boolean structMode) {
        this.structMode = structMode;
    }

    public boolean isStructMode() {
        return structMode;
    }
}
