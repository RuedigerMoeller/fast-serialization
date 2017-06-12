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

import org.nustaq.serialization.annotations.AnonymousTransient;
import org.nustaq.serialization.annotations.Conditional;
import org.nustaq.serialization.annotations.Flat;
import org.nustaq.serialization.annotations.OneOf;
import org.nustaq.serialization.annotations.Predict;
import org.nustaq.serialization.annotations.Serialize;
import org.nustaq.serialization.annotations.Transient;
import org.nustaq.serialization.annotations.Version;
import org.nustaq.serialization.util.FSTMap;
import org.nustaq.serialization.util.FSTUtil;

import java.io.Externalizable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 03.11.12
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */
public final class FSTClazzInfo {

    // cache constructor per class (big saving in permspace)
    static final boolean BufferConstructorMeta = true;
    // cache and share class.getDeclaredFields amongst all fstconfigs
    private static final boolean BufferFieldMeta = true;

    /**
     * cache + share j.reflect.Field. This can be cleared in case it gets too fat/leaks mem (e.g. class reloading)
     */
    private static final ConcurrentHashMap<Class, Field[]> sharedFieldSets = new ConcurrentHashMap<>();

    static final Comparator<FSTFieldInfo> defFieldComparator = new Comparator<FSTFieldInfo>() {
        @Override
        public int compare(FSTFieldInfo o1, FSTFieldInfo o2) {
            int res = 0;

            if (o1.getVersion() != o2.getVersion()) {
                return o1.getVersion() < o2.getVersion() ? -1 : 1;
            }

            // order: version, boolean, primitives, conditionals, object references
            if (o1.getType() == boolean.class && o2.getType() != boolean.class) {
                return -1;
            }
            if (o1.getType() != boolean.class && o2.getType() == boolean.class) {
                return 1;
            }

            if (o1.isConditional() && !o2.isConditional()) {
                res = 1;
            } else if (!o1.isConditional() && o2.isConditional()) {
                res = -1;
            } else if (o1.isPrimitive() && !o2.isPrimitive()) {
                res = -1;
            } else if (!o1.isPrimitive() && o2.isPrimitive())
                res = 1;
//                if (res == 0) // 64 bit / 32 bit issues
//                    res = (int) (o1.getMemOffset() - o2.getMemOffset());
            if (res == 0)
                res = o1.getType().getSimpleName().compareTo(o2.getType().getSimpleName());
            if (res == 0)
                res = o1.getName().compareTo(o2.getName());
            if (res == 0) {
                return o1.getField().getDeclaringClass().getName().compareTo(o2.getField().getDeclaringClass().getName());
            }
            return res;
        }
    };
    private final boolean ignoreAnn;
    private FSTMap<String, FSTFieldInfo> fieldMap;
    private FSTMap<Class, FSTCompatibilityInfo> compInfo;

    private boolean requiresCompatibleMode;
    private boolean externalizable;
    private boolean flat; // never share instances of this class
    boolean isAsciiNameShortString = false;
    private boolean requiresInit = false;
    private boolean hasTransient;
    private FSTObjectSerializer ser;
    private FSTFieldInfo fieldInfo[]; // serializable fields

    final Class clazz;
    private final Object[] enumConstants;
    private Constructor cons;
    private int clzId = -1;


    final FSTConfiguration conf;
    private final FSTClassInstantiator instantiator; // initialized from FSTConfiguration in constructor

    public FSTClazzInfo(FSTConfiguration conf, Class clazz, boolean ignoreAnnotations) {
        this.conf = conf; // fixme: historically was not bound to conf but now is. Remove redundant state + refs (note: may still be useful because of less pointerchasing)
        this.clazz = clazz;
        enumConstants = clazz.getEnumConstants();
        ignoreAnn = ignoreAnnotations;
        createFields(clazz);

        instantiator = conf.getInstantiator(clazz);
        if (Externalizable.class.isAssignableFrom(clazz)) {
            externalizable = true;
            cons = instantiator.findConstructorForExternalize(clazz);
        } else if (Serializable.class.isAssignableFrom(clazz) || clazz == Object.class) {
            externalizable = false;
            cons = instantiator.findConstructorForSerializable(clazz);
        } else {
            if (!conf.isStructMode()) {
                if (conf.isForceSerializable() || getSer() != null) {
                    externalizable = false;
                    cons = instantiator.findConstructorForSerializable(clazz);
                } else {
                    throw new RuntimeException("Class " + clazz.getName() + " does not implement Serializable or externalizable");
                }
            } else {
                cons = instantiator.findConstructorForSerializable(clazz);
            }
        }
        if (!ignoreAnnotations) {
            flat = clazz.isAnnotationPresent(Flat.class);
        }

        if (cons != null) {
            cons.setAccessible(true);
        }

        final String name = clazz.getName();
        if (name.length() < 127) {
            isAsciiNameShortString = true;
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) > 127) {
                    isAsciiNameShortString = false;
                    break;
                }
            }
        }

        requiresInit = isExternalizable() || useCompatibleMode() || hasTransient || conf.isForceClzInit();
    }

    private byte[] bufferedName;

    byte[] getBufferedName() {
        if (bufferedName == null) {
            bufferedName = getClazz().getName().getBytes();
        }
        return bufferedName;
    }

    @Override
    public String toString() {
        return "FSTClazzInfo{" +
                "clazz=" + clazz +
                '}';
    }

    int getClzId() {
        return clzId;
    }

    void setClzId(int clzId) {
        this.clzId = clzId;
    }

    boolean isExternalizable() {
        return externalizable;
    }

    final boolean isFlat() {
        return flat;
    }

    final Object newInstance() {
        return instantiator.newInstance(clazz, cons, requiresInit, conf.isForceSerializable());
    }

    /**
     * Sideeffect: sets hasTransient
     *
     * @param c
     * @param res
     * @return
     */
    private List<Field> getAllFields(Class c, List<Field> res) {
        synchronized (sharedFieldSets) {
            if (res == null) {
                res = new ArrayList<>();
            }
            if (c == null) {
                return res;
            }
            Field[] declaredFields = BufferFieldMeta && !conf.isStructMode() ? sharedFieldSets.get(c) : null;
            if (declaredFields == null) {
                declaredFields = c.getDeclaredFields();
                if (BufferFieldMeta && !conf.isStructMode())
                    sharedFieldSets.put(c, declaredFields);
            }
            List<Field> c1 = Arrays.asList(declaredFields);
            Collections.reverse(c1);
            for (int i = 0; i < c1.size(); i++) {
                Field field = c1.get(i);
                res.add(0, field);
            }
            for (int i = 0; i < res.size(); i++) {
                Field field = res.get(i);
                if (Modifier.isStatic(field.getModifiers()) || isTransient(c, field)) {
                    if (isTransient(c, field)) {
                        hasTransient = true;
                    }
                    res.remove(i);
                    i--;
                }
            }
            List<Field> allFields = getAllFields(c.getSuperclass(), res);
            return new ArrayList<>(allFields);
        }
    }

    private boolean isTransient(Class c, Field field) {
        if (Modifier.isTransient(field.getModifiers()))
            return true;
        while (c.getName().indexOf("$") >= 0) {
            c = c.getSuperclass(); // patch fuer reallive queries, kontraktor spore
        }
        return field.getName().startsWith("this$") && c.getAnnotation(AnonymousTransient.class) != null || (c.getAnnotation(Transient.class) != null && field.getAnnotation(Serialize.class) == null);
    }

    final FSTFieldInfo[] getFieldInfo() {
        return fieldInfo;
    }

    final FSTFieldInfo getFieldInfo(String name, Class declaringClass) {
        if (fieldMap != null) {
            if (declaringClass == null) {
                return fieldMap.get(name);
            }
            return fieldMap.get(declaringClass.getName() + "#" + name); //FIXME: THIS IS VERY SLOW (only used by JSON / compatibility mode)
        } else {
            synchronized (this) {
                fieldMap = buildFieldMap();
                return getFieldInfo(name, declaringClass);
            }
        }
    }

    private FSTMap buildFieldMap() {
        FSTMap res = new FSTMap<>(fieldInfo.length);
        for (int i = 0; i < fieldInfo.length; i++) {
            Field field = fieldInfo[i].getField();
            if (field != null) {
                res.put(field.getDeclaringClass().getName() + "#" + field.getName(), fieldInfo[i]);
                res.put(field.getName(), fieldInfo[i]);
            }
        }
        return res;
    }

    private void createFields(Class c) {
        if (c.isInterface() || c.isPrimitive()) {
            return;
        }
        List<Field> fields = getAllFields(c, null);
        fieldInfo = new FSTFieldInfo[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            fieldInfo[i] = createFieldInfo(field);
        }

        // compatibility info sort order
        Comparator<FSTFieldInfo> infocomp = new Comparator<FSTFieldInfo>() {
            @Override
            public int compare(FSTFieldInfo o1, FSTFieldInfo o2) {
                int res;
                res = o1.getType().getSimpleName().compareTo(o2.getType().getSimpleName());
                if (res == 0)
                    res = o1.getType().getName().compareTo(o2.getType().getName());
                if (res == 0) {
                    Class declaringClass = o1.getType().getDeclaringClass();
                    Class declaringClass1 = o2.getType().getDeclaringClass();
                    if (declaringClass == null && declaringClass1 == null) {
                        return 0;
                    }
                    if (declaringClass != null && declaringClass1 == null) {
                        return 1;
                    }
                    if (declaringClass == null) {
                        return -1;
                    }
                    return declaringClass.getName().compareTo(declaringClass1.getName());
                }
                return res;
            }
        };

        if (!conf.isStructMode()/* && requiresCompatibilityData */) {
            getCompInfo();
            fieldMap = buildFieldMap();
            Class curCl = c;
            fields.clear();
            while (curCl != Object.class) {
                ObjectStreamClass os = null;
                try {
                    os = ObjectStreamClass.lookup(curCl);
                } catch (Exception e) {
                    FSTUtil.<RuntimeException>rethrow(e);
                }
                if (os != null) {
                    final ObjectStreamField[] fi = os.getFields();
                    List<FSTFieldInfo> curClzFields = new ArrayList<>();
                    if (fi != null) {
                        for (int i = 0; i < fi.length; i++) {
                            ObjectStreamField objectStreamField = fi[i];
                            String ff = objectStreamField.getName();
                            final FSTFieldInfo fstFieldInfo = fieldMap.get(curCl.getName() + "#" + ff);
                            if (fstFieldInfo != null && fstFieldInfo.getField() != null) {
                                curClzFields.add(fstFieldInfo);
                                fields.add(fstFieldInfo.getField());
                            } else {
                                FSTFieldInfo fake = new FSTFieldInfo(null, null, true);
                                fake.type = objectStreamField.getType();
                                fake.fakeName = objectStreamField.getName();
                                curClzFields.add(fake);
                            }
                        }
                    }
                    Collections.sort(curClzFields, infocomp);
                    FSTCompatibilityInfo info = new FSTCompatibilityInfo(curClzFields, curCl);
                    getCompInfo().put(curCl, info);
                    if (info.needsCompatibleMode()) {
                        requiresCompatibleMode = true;
                    }
                }
                curCl = curCl.getSuperclass();
            }
        }

        // default sort order
        Comparator<FSTFieldInfo> comp = defFieldComparator;
        if (!conf.isStructMode())
            Arrays.sort(fieldInfo, comp);
        int off = 8; // object header: length + clzId
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTFieldInfo fstFieldInfo = fieldInfo[i];
            fstFieldInfo.setStructOffset(off);
            off += fstFieldInfo.getStructSize();
        }
        /*writeReplaceMethod = FSTUtil.findDerivedMethod(
            c, "writeReplace", null, Object.class);
        readResolveMethod = FSTUtil.findDerivedMethod(
            c, "readResolve", null, Object.class);
        if (writeReplaceMethod != null) {
            writeReplaceMethod.setAccessible(true);
        }
        if (readResolveMethod != null) {
            readResolveMethod.setAccessible(true);
        }*/
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTFieldInfo fstFieldInfo = fieldInfo[i];
            fstFieldInfo.indexId = i;
        }
    }

    boolean useCompatibleMode() {
        return requiresCompatibleMode;
    }


    private static final AtomicInteger fiCount = new AtomicInteger(0);
    private static final AtomicInteger missCount = new AtomicInteger(0);

    private FSTFieldInfo createFieldInfo(Field field) {
        FSTConfiguration.FieldKey key = null;
        if (conf.fieldInfoCache != null) {
            key = new FSTConfiguration.FieldKey(field.getDeclaringClass(), field.getName());
            FSTFieldInfo res = conf.fieldInfoCache.get(key);
            if (res != null) {
                fiCount.incrementAndGet();
                return res;
            }
        }
        field.setAccessible(true);
        Predict predict = field.getAnnotation(Predict.class); // needs to be iognored cross platform
        FSTFieldInfo result = new FSTFieldInfo(predict != null ? predict.value() : null, field, ignoreAnn);
        if (conf.fieldInfoCache != null) {
            conf.fieldInfoCache.put(key, result);
        }
        missCount.incrementAndGet();
        return result;
    }

    public final Class getClazz() {
        return clazz;
    }

    Object[] getEnumConstants() {
        return enumConstants;
    }

    FSTMap<Class, FSTCompatibilityInfo> getCompInfo() {
        if (compInfo == null)
            compInfo = new FSTMap<>(3); // just avoid edge case NPE's
        return compInfo;
    }

    public final static class FSTFieldInfo {

        final static int BOOL = 1;
        final static int BYTE = 2;
        final static int CHAR = 3;
        final static int SHORT = 4;
        final static int INT = 5;
        final static int LONG = 6;
        final static int FLOAT = 7;
        final static int DOUBLE = 8;

        Class possibleClasses[];
        FSTClazzInfo lastInfo; // cache last class stored (can save a hash lookup)
        String oneOf[] = null;

        int arrayDim;
        Class arrayType;
        boolean flat = false;
        boolean isConditional = false;

        final Field field;
        Class type;
        boolean integral = false;
        boolean primitive = false;
        boolean isArr = false;
        byte version;
        int integralType;
        long memOffset = -1;

        int structOffset = 0;
        int indexId; // position in serializable fields array

        // hack required for compatibility with ancient JDK mechanics (cross JDK, e.g. Android <=> OpenJDK ).
        // in rare cases, a field used in putField is not present as a real field
        // in this case only these of a fieldinfo are set
        String fakeName;

        public FSTFieldInfo(Class[] possibleClasses, Field fi, boolean ignoreAnnotations) {
            this.possibleClasses = possibleClasses;
            field = fi;
            if (fi == null) {
                isArr = false;
            } else {
                isArr = field.getType().isArray();
                type = fi.getType();
                primitive = type.isPrimitive();
                if (FSTUtil.unFlaggedUnsafe != null) {
                    fi.setAccessible(true);
                    if (!Modifier.isStatic(fi.getModifiers())) {
                        try {
                            memOffset = (int) FSTUtil.unFlaggedUnsafe.objectFieldOffset(fi);
                        } catch (Throwable th) {
                            //throw FSTUtil.rethrow(th);
                        }
                    }
                }
            }
            if (isArray()) {
                String clName = field.getType().getName();
                arrayDim = 1 + clName.lastIndexOf('[');
                arrayType = calcComponentType(field.getType());
            }
            calcIntegral();
            if (fi != null && !ignoreAnnotations) {
                version = fi.isAnnotationPresent(Version.class) ? fi.getAnnotation(Version.class).value() : 0;
                flat = fi.isAnnotationPresent(Flat.class);
                isConditional = fi.isAnnotationPresent(Conditional.class);
                if (isIntegral()) {
                    isConditional = false;
                }
                OneOf annotation = fi.getAnnotation(OneOf.class);
                if (annotation != null) {
                    oneOf = annotation.value();
                }
            }

        }

        public byte getVersion() {
            return version;
        }

        void setStructOffset(int structOffset) {
            this.structOffset = structOffset;
        }

        String[] getOneOf() {
            return oneOf;
        }

        boolean isConditional() {
            return isConditional;
        }

        Class calcComponentType(Class c) {
            if (c.isArray()) {
                return calcComponentType(c.getComponentType());
            }
            return c;
        }

        public final Class getType() {
            return type;
        }

        boolean isArray() {
            return isArr;
        }

        Class getArrayType() {
            return arrayType;
        }

        Class[] getPossibleClasses() {
            return possibleClasses;
        }

        void setPossibleClasses(Class[] possibleClasses) {
            this.possibleClasses = possibleClasses;
        }

        public Field getField() {
            return field;
        }

        void calcIntegral() {
            if (field == null) {
                return;
            }
            if (isArray()) {
                integral = isIntegral(getArrayType());
            } else {
                integral = isIntegral(field.getType());

                Class type = field.getType();
                integralType = getIntegralCode(type);
            }
        }

        static int getIntegralCode(Class type) {
            if (type == boolean.class) {
                return BOOL;
            } else if (type == byte.class) {
                return BYTE;
            } else if (type == char.class) {
                return CHAR;
            } else if (type == short.class) {
                return SHORT;
            } else if (type == int.class) {
                return INT;
            } else if (type == long.class) {
                return LONG;
            } else if (type == float.class) {
                return FLOAT;
            } else if (type == double.class) {
                return DOUBLE;
            }
            return 0;
        }

        /**
         * only set if is not an array, but a direct native field type
         *
         * @return
         */
        int getIntegralType() {
            return integralType;
        }

        boolean isIntegral(Class type) {
            return type.isPrimitive();
        }

        /**
         * @return wether this is primitive or an array of primitives
         */
        boolean isIntegral() {
            return integral;
        }

        String getDesc() {
            return field != null ? "<" + field.getName() + " of " + field.getDeclaringClass().getSimpleName() + ">" : "<undefined referencee>";
        }

        public String toString() {
            return getDesc();
        }

        boolean isFlat() {
            return flat;
        }

        int getStructSize() {
            if (type == boolean.class || type == byte.class)
                return 1;
            if (type == char.class || type == short.class)
                return 2;
            if (type == int.class || type == float.class)
                return 4;
            if (type == long.class || type == double.class)
                return 8;
            if (isArray()) {
                if (isIntegral())
                    return 8; // pointer+length
                else // object array
                    return 16; // pointer+length+elemsiz+pointertype
            }
            return 4;
        }

        boolean isPrimitive() {
            return primitive;
        }

        final int getByteValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getByte(obj, memOffset);
            }
            return field.getByte(obj);
        }

        final int getCharValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getChar(obj, memOffset);
            }
            return field.getChar(obj);
        }

        final int getShortValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getShort(obj, memOffset);
            }
            return field.getShort(obj);
        }

        final boolean getBooleanValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getBoolean(obj, memOffset);
            }
            return field.getBoolean(obj);
        }

        /**
         * Warning: crashes if not an object ref !
         * use getField().get() for a safe version ..
         *
         * @param obj
         * @return
         * @throws IllegalAccessException
         */
        final Object getObjectValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getObject(obj, memOffset);
            }
            return field.get(obj);
        }

        final float getFloatValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getFloat(obj, memOffset);
            }
            return field.getFloat(obj);
        }

        final void setCharValue(Object newObj, char c) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putChar(newObj, memOffset, c);
                return;
            }
            field.setChar(newObj, c);
        }

        final void setShortValue(Object newObj, short i1) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putShort(newObj, memOffset, i1);
                return;
            }
            field.setShort(newObj, i1);
        }

        final void setObjectValue(Object target, Object value) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putObject(target, memOffset, value);
                return;
            }
            field.set(target, value);
        }

        final void setFloatValue(Object newObj, float l) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putFloat(newObj, memOffset, l);
                return;
            }
            field.setFloat(newObj, l);
        }

        final void setDoubleValue(Object newObj, double l) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putDouble(newObj, memOffset, l);
                return;
            }
            field.setDouble(newObj, l);
        }

        final void setLongValue(Object newObj, long i1) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putLong(newObj, memOffset, i1);
                return;
            }
            field.setLong(newObj, i1);
        }

        final long getLongValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getLong(obj, memOffset);
            }
            return field.getLong(obj);
        }

        final double getDoubleValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getDouble(obj, memOffset);
            }
            return field.getDouble(obj);
        }

        final void setIntValue(Object newObj, int i1) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putInt(newObj, memOffset, i1);
                return;
            }
            field.setInt(newObj, i1);
        }

        final int getIntValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getInt(obj, memOffset);
            }
            return field.getInt(obj);
        }

        final void setBooleanValue(Object newObj, boolean i1) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putBoolean(newObj, memOffset, i1);
                return;
            }
            field.setBoolean(newObj, i1);
        }

        final void setByteValue(Object newObj, byte b) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putByte(newObj, memOffset, b);
                return;
            }
            field.setByte(newObj, b);
        }

        public String getName() {
            return field != null ? field.getName() : fakeName;
        }
    }

    /**
     * sideeffecting: if no ser is found, next lookup will return null immediate
     *
     * @return
     */
    FSTObjectSerializer getSer() {
        if (ser == null) {
            if (clazz == null) {
                return null;
            }
            ser = getSerNoStore();
            if (ser == null) {
                ser = FSTSerializerRegistry.NULL;
            }
        }
        if (ser == FSTSerializerRegistry.NULL) {
            return null;
        }
        return ser;
    }

    // no sideffecting lookup
    private FSTObjectSerializer getSerNoStore() {
        return conf.getCLInfoRegistry().getSerializerRegistry().getSerializer(clazz);
    }

    static class FSTCompatibilityInfo {
        Method writeMethod, readMethod;
        final List<FSTFieldInfo> infos;
        final Class clazz;
        FSTFieldInfo infoArr[];

        FSTCompatibilityInfo(List<FSTFieldInfo> inf, Class c) {
            readClazz(c);
            infos = inf;
            clazz = c;
        }

        public List<FSTFieldInfo> getFields() {
            return infos;
        }

        FSTFieldInfo[] getFieldArray() {
            if (infoArr == null) {
                List<FSTClazzInfo.FSTFieldInfo> fields = getFields();
                final FSTFieldInfo[] fstFieldInfos = new FSTFieldInfo[fields.size()];
                fields.toArray(fstFieldInfos);
                Arrays.sort(fstFieldInfos, defFieldComparator);
                infoArr = fstFieldInfos;
            }
            return infoArr;
        }

        boolean needsCompatibleMode() {
            return writeMethod != null || readMethod != null;
        }

        void readClazz(Class c) {
            writeMethod = FSTUtil.findPrivateMethod(c, "writeObject",
                    new Class<?>[]{ObjectOutputStream.class},
                    Void.TYPE);
            readMethod = FSTUtil.findPrivateMethod(c, "readObject",
                    new Class<?>[]{ObjectInputStream.class},
                    Void.TYPE);
            if (writeMethod != null) {
                writeMethod.setAccessible(true);
            }
            if (readMethod != null) {
                readMethod.setAccessible(true);
            }
        }

        Method getReadMethod() {
            return readMethod;
        }

        Method getWriteMethod() {
            return writeMethod;
        }
    }
}
