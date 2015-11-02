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
package org.nustaq.offheap.structs.unsafeimpl;

import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.BytezAllocator;
import org.nustaq.offheap.bytez.onheap.HeapBytezAllocator;
import org.nustaq.offheap.structs.*;
import org.nustaq.offheap.structs.structtypes.StructArray;
import org.nustaq.offheap.structs.structtypes.StructByteString;
import org.nustaq.offheap.structs.structtypes.StructString;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.util.FSTInt2ObjectMap;
import org.nustaq.serialization.util.FSTUtil;
import javassist.*;
import javassist.Modifier;
import javassist.bytecode.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * manages + generates struct instrumented classes
 */
public class FSTStructFactory {

    public static int SIZE_ALIGN = 2;
    static FSTStructFactory instance;

    public static FSTStructFactory getInstance() {
        if (instance==null) {
            instance = new FSTStructFactory(); // fixme: should be final
        }
        return instance;
    }

    public static final int MAX_CLASSES = 1000;
    static FSTConfiguration conf = FSTConfiguration.createStructConfiguration();

    ClassPool defaultPool;
    Loader proxyLoader;
    ClassLoader parentLoader;

    {
        defaultPool = new ClassPool(null) {
            @Override
            public CtClass get(String classname) throws NotFoundException {
                if ( rawByteClassDefs.containsKey(classname)) {
                    try {
                        return makeClass(new ByteArrayInputStream(rawByteClassDefs.get(classname)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return super.get(classname);
            }
        };
        defaultPool.appendSystemPath();

        proxyLoader = new Loader(FSTStructFactory.class.getClassLoader(), defaultPool)
        {
            protected Class loadClassByDelegation(String name)
                    throws ClassNotFoundException
            {
                try { return delegateToParent(name); } catch (Exception ex) {
                    return null;
                }
            }
        };
    }

    ConcurrentHashMap<Class, Class> proxyClzMap = new ConcurrentHashMap<Class, Class>();
    FSTStructGeneration structGen = new FSTByteArrayUnsafeStructGeneration();
    ConcurrentHashMap<String,byte[]> rawByteClassDefs = new ConcurrentHashMap<String, byte[]>();
    boolean autoRegister = true;

    BytezAllocator allocator = new HeapBytezAllocator();

    public FSTStructFactory() {
        registerClz(FSTStruct.class);
        registerClz(StructString.class);
        registerClz(StructArray.class);
        registerClz(StructByteString.class);
    }

    public void registerRawClass( String name, byte bytes[] ) {
        rawByteClassDefs.put(name,bytes);
    }

    public <T> Class<T> createStructClz( Class<T> clazz ) throws Exception {
        //FIXME: ensure FSTStruct is superclass, check protected, no private methods+fields
        if ( Modifier.isFinal(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers()) ) {
            throw new RuntimeException("Cannot add final classes to structs "+clazz.getName());
        }
        if ( clazz.getName().endsWith("_Struct") ) {
            throw new RuntimeException("cannot create Struct on Struct class. Class "+clazz+" is already instrumented" );
        }
        String proxyName = clazz.getName()+"_Struct";
        Class present = null;
        try {
            present = proxyLoader.loadClass(proxyName);
        } catch (ClassNotFoundException ex) {
            //
        }
        if ( present != null )
            return present;
        ClassPool pool = defaultPool;
        CtClass newClz = pool.makeClass(proxyName);
        CtClass orig = null;
        if ( rawByteClassDefs.get(clazz.getName()) != null ) {
            orig = pool.makeClass( new ByteArrayInputStream(rawByteClassDefs.get(clazz.getName())));
        } else {
            orig = pool.getOrNull(clazz.getName());
            if ( orig == null ) {
                pool.insertClassPath(new ClassClassPath(clazz));
                orig = pool.get(clazz.getName());
                if (orig == null)
                {
                    throw new RuntimeException("unable to locate class byte code for "+clazz.getName());
                }
            }
        }
        newClz.setSuperclass(orig);

        final FSTClazzInfo clInfo = conf.getClassInfo(clazz);

        CtMethod[] methods = orig.getMethods();
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            final Class curClz = Class.forName( method.getDeclaringClass().getName() );
            boolean allowed = ((method.getModifiers() & AccessFlag.ABSTRACT) == 0 ) &&
                    (method.getModifiers() & AccessFlag.NATIVE) == 0 &&
                    (method.getModifiers() & AccessFlag.FINAL) == 0 &&
                    (   !method.getDeclaringClass().getName().equals(FSTStruct.class.getName())
                       ||method.getName().equals("getFieldValues")) &&
                    ! method.getDeclaringClass().getName().equals(Object.class.getName());
            allowed &= method.getAnnotation(NoAssist.class) == null;
            allowed &= (method.getModifiers() & AccessFlag.STATIC) == 0;
            if ( allowed && (method.getModifiers() & AccessFlag.FINAL) != 0 && ! method.getDeclaringClass().getName().equals("java.lang.Object") ) {
                throw new RuntimeException("final methods are not allowed for struct classes:"+method.getName());
            }
            if ( allowed && (method.getModifiers() & AccessFlag.PRIVATE) != 0 && ! method.getDeclaringClass().getName().equals("java.lang.Object")) {
                throw new RuntimeException("private methods are not allowed for struct classes:"+method.getName());
            }
            if ( allowed ) {
                ClassMap mp = new ClassMap();
                mp.fix(clazz.getName());
                mp.fix(clazz.getSuperclass().getName()); // ?? only depth 2 ??
                method = new CtMethod(method,newClz,mp);
                String methName = method.getName();
                // array access:
                //      void [name](int, type)
                //      [type] [name](int)
                FSTClazzInfo.FSTFieldInfo arrayFi = checkForSpecialArrayMethod(clInfo, method, "", null, null);
                // array length:
                //      int [name]Len()
                FSTClazzInfo.FSTFieldInfo lenfi = checkForSpecialArrayMethod(clInfo, method, "Len", CtClass.intType, new CtClass[0]);
                // get byte index of array data:
                //      int [name]Index()
                FSTClazzInfo.FSTFieldInfo indexfi = checkForSpecialArrayMethod(clInfo, method, "Index", CtClass.intType, new CtClass[0]);
                // get size of array element:
                //      int [name]ElementSize()
                FSTClazzInfo.FSTFieldInfo elemlen = checkForSpecialArrayMethod(clInfo, method, "ElementSize", CtClass.intType, new CtClass[0]);
                // CREATE non volatile pointer to array[0] element:
                //      type [name]Pointer() OR type [name]Pointer(pointerToSetup) (for reuse)
                FSTClazzInfo.FSTFieldInfo pointerfi = checkForSpecialArrayMethod(clInfo, method, "Pointer", null, null);
                // get byte index to structure or array header element:
                //      type [name]StructIndex()
                FSTClazzInfo.FSTFieldInfo structIndex = checkForSpecialMethod(clInfo, method, "StructIndex", CtClass.intType, new CtClass[0], false);
                // set with CAS
                //      boolean [name]CAS(expectedValue,value)
                FSTClazzInfo.FSTFieldInfo casAcc = checkForSpecialMethod(clInfo, method, "CAS", CtClass.booleanType, null, false);

                if ( casAcc != null ) {
                    structGen.defineStructSetCAS(casAcc, clInfo, method);
                    newClz.addMethod(method);
                } else
                if ( pointerfi != null ) {
                    structGen.defineArrayPointer(pointerfi, clInfo, method);
                    newClz.addMethod(method);
                } else
                if ( structIndex != null ) {
                    structGen.defineFieldStructIndex(structIndex, clInfo, method);
                    newClz.addMethod(method);
                } else
                if ( indexfi != null ) {
                    structGen.defineArrayIndex(indexfi, clInfo, method);
                    newClz.addMethod(method);
                } else
                if ( elemlen != null ) {
                    structGen.defineArrayElementSize(elemlen, clInfo, method);
                    newClz.addMethod(method);
                } else
                if (  arrayFi != null ) {
                    structGen.defineArrayAccessor(arrayFi, clInfo, method);
                    newClz.addMethod(method);
                } else if ( methName.endsWith("Len") && lenfi != null )
                {
                    structGen.defineArrayLength(lenfi, clInfo, method);
                    newClz.addMethod(method);
                } else {
                    if ( methName.equals("getFieldValues") &&
                            ( (clInfo.getClazz().getSuperclass().getName().equals("de.nustaq.reallive.impl.RLStructRow")) // oops
                            || (curClz != FSTStruct.class) )
                       ) {
                        FSTClazzInfo.FSTFieldInfo[] fieldInfo = clInfo.getFieldInfo();
                        StringBuilder body = new StringBuilder("{  return new Object[] { ");
                        for (int j = 0; j < fieldInfo.length; j++) {
                            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[j];
                            int modifiers = fstFieldInfo.getField().getModifiers();
                            if ( (java.lang.reflect.Modifier.isProtected(modifiers) ||
                                  java.lang.reflect.Modifier.isPublic(modifiers)) &&
                                  !java.lang.reflect.Modifier.isStatic(modifiers)
                                )
                            {
                                body.append( "\""+fstFieldInfo.getName()+"\", " );
                                Class type = fstFieldInfo.getType();
                                if ( FSTStruct.class.isAssignableFrom(type) ) {
                                    body.append(fstFieldInfo.getName()).append(".getFieldValues()");
                                } else {
                                    if ( type.isPrimitive() ) {
                                        if ( long.class == type ) {
                                            body.append("new Long("+fstFieldInfo.getName()+")");
                                        } else if ( float.class == type ||double.class == type ) {
                                            body.append("new Double("+fstFieldInfo.getName()+")");
                                        } else {
                                            body.append("new Integer("+fstFieldInfo.getName()+")");
                                        }
                                    } else {
                                        body.append(fstFieldInfo.getName());
                                    }
                                }
                                if ( j != fieldInfo.length-1 )
                                    body.append(",");
                            }
                        }
                        body.append("}; }");
                        method.setBody(body.toString());
                    }
                    newClz.addMethod(method);
                    method.instrument( new ExprEditor() {
                        @Override
                        public void edit(FieldAccess f) throws CannotCompileException {
                            try {
                                if ( ! f.isStatic() ) {
                                    CtClass type = null;
                                    type = f.getField().getType();
                                    FSTClazzInfo.FSTFieldInfo fieldInfo = clInfo.getFieldInfo(f.getFieldName(), null);
                                    if ( fieldInfo == null ) {
                                        return;
                                    }
                                    if ( f.isReader() ) {
                                        structGen.defineStructReadAccess(f, type, fieldInfo);
                                    } else if ( f.isWriter() ) {
                                        structGen.defineStructWriteAccess(f, type, fieldInfo);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }

        return (Class<T>) loadProxyClass(clazz, pool, newClz);
    }

    FSTClazzInfo.FSTFieldInfo checkForSpecialArrayMethod( FSTClazzInfo clzInfo, CtMethod method, String postFix, Object returnType, CtClass requiredArgs[] ) {
        return checkForSpecialMethod(clzInfo, method, postFix, returnType, requiredArgs, true);
    }

    FSTClazzInfo.FSTFieldInfo checkForSpecialMethod(FSTClazzInfo clzInfo, CtMethod method, String postFix, Object returnType, CtClass requiredArgs[], boolean array) {
        int len = postFix.length();
        String methName = method.getName();
        if ( ! methName.endsWith(postFix) ) {
            return null;
        }
        FSTClazzInfo.FSTFieldInfo res = clzInfo.getFieldInfo(methName.substring(0, methName.length() - len), null);
        if ( res == null ) {
            return null;
        }
        if ( array && res.isArray() && res.getArrayType().isArray() ) {
            throw new RuntimeException("nested arrays not supported "+res.getDesc());
        }
        if ( array && !res.isArray() ) {
            //throw new RuntimeException("expect array type for field "+res.getDesc()+" special method:"+method);
            // just ignore
            return null;
        }
        if ( res.isArray() || ! array ) {
            if ( returnType instanceof Class ) {
                try {
                    if ( ! method.getReturnType().getName().equals(((Class) returnType).getName()) ) {
                        throw new RuntimeException("expected method "+method+" to return "+returnType );
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            } else if ( returnType instanceof CtClass ) {
                try {
                    if ( ! method.getReturnType().equals(returnType) ) {
                        throw new RuntimeException("expected method "+method+" to return "+returnType );
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            }
            return res;
        }
        return null;
    }

    private <T> Class loadProxyClass(Class<T> clazz, ClassPool pool, final CtClass cc) throws ClassNotFoundException {
        Class ccClz;
        Loader cl = new Loader(clazz.getClassLoader(), pool) {
            protected Class loadClassByDelegation(String name)
                    throws ClassNotFoundException
            {
                if ( name.equals(cc.getName()) )
                    return null;
                return delegateToParent(name);
            }
        };
        ccClz = cl.loadClass(cc.getName());
        return ccClz;
    }

    public Class getProxyClass(Class clz) throws Exception {
//        synchronized (this)
        {
            Class res = proxyClzMap.get(clz);
            if ( res == null ) {
                res = createStructClz(clz);
                proxyClzMap.put(clz,res);
            }
            return res;
        }
    }

    public <T extends FSTStruct> T createWrapper(Class<T> onHeap, Bytez bytes, long index) throws Exception {
        Class proxy = getProxyClass(onHeap);
        T res = (T) FSTUtil.getUnsafe().allocateInstance(proxy);
        res.baseOn(bytes, index, this);
        return res;
    }

    public <T extends FSTStruct> T createEmptyStructPointer(Class<T> onHeap) {
        try {
            return createWrapper(onHeap,null,0);
        } catch (Exception e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    /**
     * allocates a StructAccessor ("pointer") matching the struct data expected in the byte
     * array at given position. The resulting pointer object is not "volatile" (not a cached instance)
     * @param b
     * @param index
     * @return
     */
    public FSTStruct createStructWrapper(Bytez b, long index) {
        int clzId = b.getInt(index + 4);
        return createStructPointer(b, index, clzId);
    }

    /**
     * allocates a StructAccessor ("pointer") matching the struct data expected in the byte
     * array at given position with given classId. The resulting pointer object is not "volatile" (not a cached instance).
     * The class id should match the Struct stored in the byte array. (classId must be the correct struct or a superclass of it)
     * @param b
     * @param index
     * @return
     */
    public FSTStruct createStructPointer(Bytez b, long index, int clzId) {
//        synchronized (this) // FIXME FIXME FIXME: contention point
        // desynced expecting class registering happens on startup
        {
            Class clazz = mIntToClz.get(clzId);
            if (clazz==null)
                throw new RuntimeException("unregistered class "+clzId);
            try {
                return (FSTStruct) createWrapper(clazz, b, index);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public FSTStruct createTypedArrayBasePointer(Bytez base, long objectBaseOffset /*offset of object containing array*/, int arrayStructIndex /*position of array header in struct*/) {
        int arrayElementZeroindex = base.getInt(objectBaseOffset + arrayStructIndex);
        int elemSiz = base.getInt(objectBaseOffset+arrayStructIndex+8);
        int len = base.getInt(objectBaseOffset+arrayStructIndex+4);
        int clId = base.getInt(objectBaseOffset+arrayStructIndex+12);
        FSTStruct structPointer = null;
        if ( clId <= 0 ) { // untyped array
            structPointer = new FSTStruct();
            structPointer.baseOn(base,objectBaseOffset+arrayElementZeroindex,this);
        } else {
            structPointer = createStructPointer(base, (int) (objectBaseOffset+arrayElementZeroindex), clId);
        }
        structPointer.___elementSize = elemSiz;
        return structPointer;
    }

    public void fillTypedArrayBasePointer(FSTStruct result, Bytez base, long objectBaseOffset /*offset of object containing array*/, int arrayStructIndex /*position of array header in struct*/) {
        int arrayElementZeroindex = base.getInt(objectBaseOffset+arrayStructIndex);
        int elemSiz = base.getInt(objectBaseOffset+arrayStructIndex+8);
//        int len = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+4);
//        int clId = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+12);
        result.baseOn(base, objectBaseOffset + arrayElementZeroindex, this);
        result.___elementSize = elemSiz;
    }

    public void fillPrimitiveArrayBasePointer(FSTStruct result, Bytez base, long objectBaseOffset /*offset of object containing array*/, int arrayStructIndex /*position of array header in struct*/) {
        int arrayElementZeroindex = base.getInt(objectBaseOffset+arrayStructIndex);
        result.baseOn(base,objectBaseOffset+arrayElementZeroindex,this);
    }

    public FSTStruct createPrimitiveArrayBasePointer(Bytez base, long objectBaseOffset /*offset of object containing array*/, int arrayStructIndex /*position of array header in struct*/) {
        int arrayElementZeroindex = base.getInt(objectBaseOffset+arrayStructIndex);
//        int len = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+4);
        FSTStruct structPointer = new FSTStruct();
        structPointer.baseOn(base,objectBaseOffset+arrayElementZeroindex,this);
        return structPointer;
    }

    public <T extends FSTStruct> StructArray<T> toStructArray(int size, T onHeap) {
        StructArray<T> arr = new StructArray<T>(size,onHeap);
        return toStruct(arr);
    }

    public <T extends FSTStruct> T toStruct(T onHeap) {
        return toStruct(onHeap,allocator);
    }

    public <T extends FSTStruct> T toStruct(T onHeap, BytezAllocator alloc) {
        if ( onHeap.isOffHeap() ) {
            return onHeap;
        }
        try {
            Bytez b = toByteArray(onHeap, alloc);
            return (T)createWrapper(onHeap.getClass(),b,0);
        } catch (Exception e) {
            if ( e instanceof RuntimeException )
                throw (RuntimeException)e;
            else
                throw new RuntimeException(e);
        }
    }

    ThreadLocal<Object[]> cachedWrapperMap = new ThreadLocal<Object[]>() {
        @Override
        protected Object[] initialValue() {
            return new Object[MAX_CLASSES];
        }
    };

    public void detach(FSTStruct structPointer) {
        int id = structPointer.getClzId();
        Object o = cachedWrapperMap.get()[id];
        if ( o == structPointer )
            cachedWrapperMap.get()[id] = null;
    }

    public FSTStruct getStructPointerByOffset(Bytez b, long offset) {
        if ( b.length() < offset+8 )
            throw new RuntimeException("array to short "+b.length()+" offset "+offset);
        int clzId = b.getInt(offset+4);
        int ptr = b.getInt(offset);
        if (clzId <= 0) {
            return null;
        }
        Object[] wrapperMap = cachedWrapperMap.get();
        Object res = wrapperMap[clzId];
        if ( res != null ) {
            ((FSTStruct)res).baseOn(b, offset, this);
            return (FSTStruct) res;
        }
        res = createStructPointer(b, (int) (offset), clzId);
        wrapperMap[clzId] = res;
        return (FSTStruct) res;
    }

    /**
     * returns a struct wrapper for given structured object from the thread local wrapper cache.
     * @param b
     * @param index
     * @return
     */
    public FSTStruct getStructPointer(Bytez b, long index) {
        return getStructPointerByOffset(b,index);
    }

    public static int align(int val, int align) {
        while( val%align != 0 )
            val++;
        return val;
    }

    public int calcStructSize(FSTStruct onHeapStruct) {
        try {
            if ( onHeapStruct == null ) {
                return 0;
            }
            if (onHeapStruct.isOffHeap())
                return onHeapStruct.getByteSize();
            int siz = 8;
            FSTClazzInfo clInfo = conf.getClassInfo(onHeapStruct.getClass());
            FSTClazzInfo.FSTFieldInfo fis[] = clInfo.getFieldInfo();
            for (int i = 0; i < fis.length; i++) {
                FSTClazzInfo.FSTFieldInfo fi = fis[i];
                if ( fi.getField().getDeclaringClass() == FSTStruct.class )
                    continue;
                int modifiers = fi.getField().getModifiers();
                if ( ! Modifier.isProtected(modifiers) && ! Modifier.isPublic(modifiers) )
                    throw new RuntimeException("all fields of a structable class must be public or protected. Field:"+fi.getName()+" in class "+fi.getField().getDeclaringClass().getName() );
                // FIXME: check for null refs, check for FSTStruct subclasses
                if ( fi.getType().isArray() ) {
                    if ( fi.getType().getComponentType().isArray() ) {
                        throw new RuntimeException("nested arrays not supported");
                    }
                    // if array is @aligned, add align-1 to size (overestimation), because I don't know the exact position of the array data here
                    // embedded object data is currently not aligned, only the header position respects the @align
                    if ( fi.isIntegral() ) { // prim array
                        Object objectValue = fi.getObjectValue(onHeapStruct);
                        if ( objectValue == null ) {
                            throw new RuntimeException("arrays in struct templates must not be null !");
                        }
                        siz += Array.getLength(objectValue) * fi.getComponentStructSize() + fi.getStructSize() + fi.getAlignPad()+(fi.getAlign()>0?fi.getAlign()-1:0);
                    } else { // object array
                        Object objectValue[] = (Object[]) fi.getObjectValue(onHeapStruct);
                        if (objectValue==null) {
                            siz+=fi.getStructSize()+fi.getAlignPad()+(fi.getAlign()>0?fi.getAlign()-1:0);
                        } else {
                            int elemSiz = computeElemSize(onHeapStruct,objectValue, fi);
                            siz += Array.getLength(objectValue) * elemSiz + fi.getStructSize() + fi.getAlignPad()+(fi.getAlign()>0?fi.getAlign()-1:0);
                        }
                    }
                } else if ( fi.isIntegral() ) { // && ! array
                    siz += fi.getStructSize();
                } else { // objectref
                    FSTStruct obj = (FSTStruct) fi.getObjectValue(onHeapStruct);
                    siz += fi.getStructSize()+calcStructSize(obj)+fi.getAlignPad();
                }
            }
            if ( onHeapStruct instanceof FSTEmbeddedBinary) {
                siz+=((FSTEmbeddedBinary) onHeapStruct).getEmbeddedSizeAdditon(this);
            }
            return siz;
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    protected int computeElemSize(Object container, Object[] objectValue, FSTClazzInfo.FSTFieldInfo fi) {
        if ( container instanceof FSTArrayElementSizeCalculator) {
            int res = ((FSTArrayElementSizeCalculator) container).getElementSize(fi.getField(),this);
            if ( res >= 0 )
                return res;
        }
        Templated annotation = fi.getField().getAnnotation(Templated.class);
        if ( annotation != null ) {
            Object template = objectValue[0];
            return align(calcStructSize((FSTStruct) template),SIZE_ALIGN);
        }
        int elemSiz = 0;
        for (int j = 0; j < objectValue.length; j++) {
            Object o = objectValue[j];
            if ( o != null )
                elemSiz=Math.max( elemSiz, calcStructSize((FSTStruct) o) );
        }
        return align(elemSiz,SIZE_ALIGN);
    }

    FSTInt2ObjectMap<Class> mIntToClz = new FSTInt2ObjectMap<Class>(97); // id to onheap class
    HashMap<Class,Integer> mClzToInt = new HashMap<Class,Integer>(); // reverse

    int idCount = 1;
    public void registerClz(Class ... classes) {
        for (int i = 0; i < classes.length; i++) {
            Class c = classes[i];
            if ( mClzToInt.containsKey(c) ) {
                continue;
            }
            int id = idCount++;
            mIntToClz.put(id,c);
            mClzToInt.put(c,id);
            try {
                getProxyClass(c);
            } catch (Exception e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }
        }
    }

    // register from top to bottom to avoid interference with application (fastcast)
    public void registerSystemClz(byte startVal, Class ... classes) {
        for (int i = 0; i < classes.length; i++) {
            Class c = classes[i];
            if ( mClzToInt.containsKey(c) ) {
                continue;
            }
            int id = startVal--;
            mIntToClz.put(id,c);
            mClzToInt.put(c,id);
            try {
                getProxyClass(c);
            } catch (Exception e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }
        }
    }

    public void registerClzId(Class c, int id) {
        mIntToClz.put(id,c);
        mClzToInt.put(c,id);
    }

    public int getClzId(Class c) {
        Integer integer = mClzToInt.get(c);
        if (autoRegister && integer == null && c != null ) {
            if ( c.getName().endsWith("_Struct") )
                return getClzId(c.getSuperclass());
            registerClz(c);
            return getClzId(c);
        }
        return integer == null ? 0: integer;
    }

    public Class getClazz(int clzId) {
        return mIntToClz.get(clzId);
    }

    public Bytez toByteArray(FSTStruct onHeapStruct) {
        return toByteArray(onHeapStruct,allocator);
    }

    public Bytez toByteArray(FSTStruct onHeapStruct, BytezAllocator allocator) {
        try {
            int sz = align(calcStructSize(onHeapStruct),SIZE_ALIGN);
            Bytez b = allocator.alloc(sz);
            toByteArray(onHeapStruct,b,0);
            return b;
        } catch (Exception e) {
            if ( e instanceof RuntimeException )
                throw (RuntimeException)e;
            else
                throw new RuntimeException(e);
        }
    }

    static class ForwardEntry {

        ForwardEntry(int pointerPos, Object forwardObject, FSTClazzInfo.FSTFieldInfo fsfi) {
            this.pointerPos = pointerPos;
            this.forwardObject = forwardObject;
            fi = fsfi;
        }

        FSTClazzInfo.FSTFieldInfo fi;
        int pointerPos;
        Object forwardObject;
        FSTStruct template;
    }


    public int toByteArray(FSTStruct onHeapStruct, Bytez bytes, int index) throws Exception {
        ArrayList<ForwardEntry> positions = new ArrayList<ForwardEntry>();
        if ( onHeapStruct == null ) {
            return index;
        }
        if (onHeapStruct.isOffHeap()) {
//            unsafe.copyMemory(onHeapStruct.___bytes,onHeapStruct.___offset,bytes,FSTStruct.bufoff+index,onHeapStruct.getByteSize());
            onHeapStruct.___bytes.copyTo(bytes, index, onHeapStruct.___offset, onHeapStruct.getByteSize());
            return onHeapStruct.getByteSize();
        }
        int initialIndex =index;
        Class<? extends FSTStruct> aClass = onHeapStruct.getClass();
        int clzId = getClzId(aClass);
        bytes.putInt(index+4,clzId);
        index+=8;
        FSTClazzInfo clInfo = conf.getClassInfo(aClass);
        FSTClazzInfo.FSTFieldInfo fis[] = clInfo.getFieldInfo();
        for (int i = 0; i < fis.length; i++) {
            FSTClazzInfo.FSTFieldInfo fi = fis[i];
            if ( fi.getField().getDeclaringClass() == FSTStruct.class )
                continue;
            index+=fi.getAlignPad();
            if ( fi.getType().isArray() ) {
                if ( fi.getType().getComponentType().isArray() ) {
                    throw new RuntimeException("nested arrays not supported");
                }
                if ( fi.isIntegral() ) { // prim array
                    Object objectValue = fi.getObjectValue(onHeapStruct);
                    positions.add(new ForwardEntry(index,objectValue,fi));
                    index += fi.getStructSize();
                } else { // object array
                    Object objArr[] = (Object[]) fi.getObjectValue(onHeapStruct);
                    if ( objArr == null ) {
                        bytes.putInt(index, -1);
                        index+=fi.getStructSize();
                    } else {
                        Templated takeFirst = fi.getField().getAnnotation(Templated.class);
                        ForwardEntry fe = new ForwardEntry(index, objArr, fi);
                        if ( takeFirst != null ) {
                            fe.template = (FSTStruct) objArr[0];
                        }
                        positions.add(fe);
                        index += fi.getStructSize();
                        int elemSiz = computeElemSize(onHeapStruct,objArr,fi);
                        bytes.putInt(index-8,elemSiz);
                    }
                }
            } else if ( fi.isIntegral() ) { // && ! array
                Class type = fi.getType();
                int structIndex = fi.getStructOffset();
                if ( index != structIndex+initialIndex ) {
                    throw new RuntimeException("internal error. please file an issue");
                }
                if ( type == boolean.class ) {
                    bytes.putBool(index, fi.getBooleanValue(onHeapStruct));
                } else
                if ( type == byte.class ) {
                    bytes.put(index, (byte) fi.getByteValue(onHeapStruct));
                } else
                if ( type == char.class ) {
                    bytes.putChar(index, (char) fi.getCharValue(onHeapStruct));
                } else
                if ( type == short.class ) {
                    bytes.putShort(index, (short) fi.getShortValue(onHeapStruct));
                } else
                if ( type == int.class ) {
                    bytes.putInt(index, fi.getIntValue(onHeapStruct));
                } else
                if ( type == long.class ) {
                    bytes.putLong( index, fi.getLongValue(onHeapStruct) );
                } else
                if ( type == float.class ) {
                    bytes.putFloat(index, fi.getFloatValue(onHeapStruct));
                } else
                if ( type == double.class ) {
                    bytes.putDouble(index, fi.getDoubleValue(onHeapStruct));
                } else {
                    throw new RuntimeException("this is an error");
                }
                index += fi.getStructSize();
            } else { // objectref
                Object obj = fi.getObjectValue(onHeapStruct);
                int structIndex = fi.getStructOffset();
                if ( index != structIndex+initialIndex ) {
                    throw new RuntimeException("internal error. please file an issue");
                }
                if ( obj == null ) {
                    bytes.putInt(index, -1);
                    bytes.putInt(index+4, -1);
                    index+=fi.getStructSize();
                } else {
                    Object objectValue = fi.getObjectValue(onHeapStruct);
                    positions.add(new ForwardEntry(index,objectValue,fi));
                    index += fi.getStructSize();
                }
            }
        }
        for ( int i=0; i < positions.size(); i++) {
            ForwardEntry en = positions.get(i);
            Object o = en.forwardObject;
            if ( o == null ) {
                throw new RuntimeException("this is a bug");
            }
            Class c = o.getClass();
            if (c.isArray()) {
                if ( en.fi.getAlign() > 0 ) {
                    while( (index%en.fi.getAlign()) != 0 ) {
                        index++;
                    }
                }
                long siz = 0;
                if ( c == byte[].class ) {
                    siz = Array.getLength(o);
                    bytes.set(index,(byte[])o,0, (int) siz);
                } else if ( c == boolean[].class ) {
                    siz = Array.getLength(o);
                    bytes.setBoolean(index, (boolean[]) o, 0, (int) siz);
                } else if ( c == char[].class ) {
                    siz = Array.getLength(o);
                    bytes.setChar(index, (char[]) o, 0, (int) siz);
                    siz *= 2;
                } else if ( c == short[].class ) {
                    siz = Array.getLength(o); // * FSTUtil.chscal;
                    bytes.setShort(index, (short[]) o, 0, (int) siz);
                    siz *= 2;
                } else if ( c == int[].class ) {
                    siz = Array.getLength(o); // * FSTUtil.intscal;
                    bytes.setInt(index, (int[]) o, 0, (int) siz);
                    siz *= 4;
                } else if ( c == long[].class ) {
                    siz = Array.getLength(o); // * FSTUtil.longscal;
                    bytes.setLong(index, (long[]) o, 0, (int) siz);
                    siz *= 8;
                } else if ( c == float[].class ) {
                    siz = Array.getLength(o); // * FSTUtil.floatscal;
                    bytes.setFloat(index, (float[]) o, 0, (int) siz);
                    siz *= 4;
                } else if ( c == double[].class ) {
                    siz = Array.getLength(o); // * FSTUtil.doublescal;
                    bytes.setDouble(index, (double[]) o, 0, (int) siz);
                    siz *= 8;
                } else {
                    Object[] objArr = (Object[]) o;
                    int elemSiz = bytes.getInt(en.pointerPos+8);
                    siz = Array.getLength(o) * elemSiz;
                    int tmpIndex = index;
                    Bytez templatearr = null;
                    boolean hasClzId = false;
                    if ( onHeapStruct instanceof FSTArrayElementSizeCalculator ) {
                        Class elemClz = ((FSTArrayElementSizeCalculator)onHeapStruct).getElementType(en.fi.getField(),this);
                        if ( elemClz != null ) {
                            int clid = getClzId(elemClz);
                            bytes.putInt(en.pointerPos + 12, clid);
                            hasClzId = true;
                        }
                    }
                    if (en.template != null) {
                        templatearr = toByteArray(en.template); // fixme: unnecessary alloc
                        if ( ! hasClzId ) {
                            bytes.putInt(en.pointerPos + 12, getClzId(en.template.getClass()));
                            hasClzId = true;
                        }
                    }
                    for (int j = 0; j < objArr.length; j++) {
                        Object objectValue = objArr[j];
                        if ( templatearr != null ) {
//                            unsafe.copyMemory(templatearr,FSTStruct.bufoff,bytes,FSTStruct.bufoff+tmpIndex,templatearr.length);
                            templatearr.copyTo(bytes,tmpIndex,0,templatearr.length());
                            tmpIndex+=elemSiz;
                        } else {
                            if ( objectValue == null ) {
                                bytes.putInt(tmpIndex + 4, -1);
                                tmpIndex += elemSiz;
                            } else {
                                toByteArray((FSTStruct) objectValue, bytes, tmpIndex);
                                bytes.putInt(tmpIndex, elemSiz); // need to patch size in case of smaller objects in obj array
                                tmpIndex += elemSiz;
                                if ( !hasClzId ) {
                                    bytes.putInt(en.pointerPos + 12, getClzId(en.fi.getArrayType()));
                                    hasClzId = true;
                                }
                            }
                        }
                    }
                }
                bytes.putInt(en.pointerPos, index - initialIndex); // offset to real data
                bytes.putInt(en.pointerPos + 4, Array.getLength(o)); // array len
                index+=siz;
            } else { // object ref or objarray elem
                int newoffset = toByteArray((FSTStruct) o, bytes, index);
                bytes.putInt(en.pointerPos, index - initialIndex);
                index = newoffset;
            }
        }
        if ( onHeapStruct instanceof FSTEmbeddedBinary ) {
            FSTEmbeddedBinary embeddedBinary = (FSTEmbeddedBinary) onHeapStruct;
            index = embeddedBinary.insertEmbedded(this, bytes, index);
        }
        bytes.putInt(initialIndex, index - initialIndex); // set object size
        return index;
    }

    public int getShallowStructSize(Class clz) {
        return conf.getClassInfo(clz).getStructSize();
    }

    Class classForName( String name ) throws ClassNotFoundException {
        try {
            return Class.forName(name);
        } catch ( ClassNotFoundException ex ) {
            if ( parentLoader != null ) {
                return parentLoader.loadClass(name);
            }
            throw ex;
        }
    }

    public ClassLoader getParentLoader() {
        return parentLoader;
    }

    public void setParentLoader(ClassLoader parentLoader) {
        this.parentLoader = parentLoader;
    }
}
