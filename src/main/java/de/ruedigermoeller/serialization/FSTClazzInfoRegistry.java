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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 03.11.12
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class FSTClazzInfoRegistry {

    HashMap mInfos = new HashMap(97);
    FSTSerializerRegistry serializerRegistry = new FSTSerializerRegistry();
    boolean ignoreAnnotations = false;
    final AtomicBoolean rwLock = new AtomicBoolean(false);
    private boolean structMode = false;


    public static void addAllReferencedClasses(Class cl, ArrayList<String> names) {
        HashSet<String> names1 = new HashSet<String>();
        addAllReferencedClasses(cl, names1, new HashSet<String>());
        names.addAll(names1);
    }

    static void addAllReferencedClasses(Class cl, HashSet<String> names, HashSet<String> topLevelDone) {
        if ( cl == null || topLevelDone.contains(cl.getName()) )
            return;
        topLevelDone.add(cl.getName());
        Field[] declaredFields = cl.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field declaredField = declaredFields[i];
            Class<?> type = declaredField.getType();
            if ( ! type.isPrimitive() && ! type.isArray() ) {
                names.add(type.getName());
                addAllReferencedClasses(type,names,topLevelDone);
            }
        }
        Class[] declaredClasses = cl.getDeclaredClasses();
        for (int i = 0; i < declaredClasses.length; i++) {
            Class declaredClass = declaredClasses[i];
            if ( ! declaredClass.isPrimitive() && ! declaredClass.isArray() ) {
                names.add(declaredClass.getName());
                addAllReferencedClasses(declaredClass, names,topLevelDone);
            }
        }
        Method[] declaredMethods = cl.getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {
            Method declaredMethod = declaredMethods[i];
            Class<?> returnType = declaredMethod.getReturnType();
            if ( ! returnType.isPrimitive() && ! returnType.isArray() ) {
                names.add(returnType.getName());
                addAllReferencedClasses(returnType, names,topLevelDone);
            }
            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            for (int j = 0; j < parameterTypes.length; j++) {
                Class<?> parameterType = parameterTypes[j];
                if ( ! parameterType.isPrimitive() && ! parameterType.isArray() ) {
                    names.add(parameterType.getName());
                    addAllReferencedClasses(parameterType, names,topLevelDone);
                }
            }
        }

        Class[] classes = cl.getDeclaredClasses();
        for (int i = 0; i < classes.length; i++) {
            Class aClass = classes[i];
            if ( ! aClass.isPrimitive() && ! aClass.isArray() ) {
                names.add(aClass.getName());
                addAllReferencedClasses(aClass, names,topLevelDone);
            }
        }

        Class enclosingClass = cl.getEnclosingClass();
        if ( enclosingClass != null ) {
            names.add(enclosingClass.getName());
            addAllReferencedClasses(enclosingClass,names,topLevelDone);
        }

        names.add(cl.getName());
        addAllReferencedClasses(cl.getSuperclass(), names,topLevelDone);
        Class[] interfaces = cl.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if ( ! anInterface.isPrimitive() && ! anInterface.isArray() ) {
                names.add(anInterface.getName());
                addAllReferencedClasses(anInterface, names,topLevelDone);
            }
        }
    }

    public FSTClazzInfoRegistry() {
    }

    public FSTClazzInfo getCLInfo(Class c) {
        while(!rwLock.compareAndSet(false,true));
        FSTClazzInfo res = (FSTClazzInfo) mInfos.get(c);
        if ( res == null ) {
            if ( c == null ) {
                rwLock.set(false);
                throw new NullPointerException("Class is null");
            }
            res = new FSTClazzInfo(c, this, ignoreAnnotations);
            mInfos.put( c, res );
        }
        rwLock.set(false);
        return res;
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
