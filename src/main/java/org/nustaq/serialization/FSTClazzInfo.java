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

import org.nustaq.offheap.structs.Align;
import org.nustaq.serialization.annotations.*;
import org.nustaq.serialization.util.FSTUtil;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 03.11.12
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */
public final class FSTClazzInfo {

    public static final Comparator<FSTFieldInfo> defFieldComparator = new Comparator<FSTFieldInfo>() {
        @Override
        public int compare(FSTFieldInfo o1, FSTFieldInfo o2) {
            int res = 0;

            if ( o1.getVersion() != o2.getVersion() ) {
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
                res = o1.getField().getName().compareTo(o2.getField().getName());
            if (res == 0) {
                return o1.getField().getDeclaringClass().getName().compareTo(o2.getField().getDeclaringClass().getName());
            }
            return res;
        }
    };
    Class[] predict;
    private boolean ignoreAnn;
    HashMap<String, FSTFieldInfo> fieldMap = new HashMap<String, FSTFieldInfo>(15); // all fields
    Method writeReplaceMethod, readResolveMethod;
    HashMap<Class, FSTCompatibilityInfo> compInfo = new HashMap<Class, FSTCompatibilityInfo>(7);

    boolean requiresCompatibleMode;
    boolean externalizable;
    boolean flat; // never share instances of this class
    boolean isAsciiNameShortString = false;
    boolean requiresInit = false;
    boolean hasTransient;
    FSTObjectSerializer ser;
    FSTFieldInfo fieldInfo[]; // serializable fields

    Class clazz;
    Object[] enumConstants;
    Constructor cons;
    int clzId = -1;
    int structSize = 0;


    FSTClazzInfoRegistry reg;
    FSTConfiguration conf;
    protected FSTClassInstantiator instantiator; // initialized from FSTConfiguration in constructor
    boolean crossPlatform;

    public FSTClazzInfo(FSTConfiguration conf, Class clazz, FSTClazzInfoRegistry infoRegistry, boolean ignoreAnnotations) {
        this.conf = conf; // fixme: historically was not bound to conf but now is. Remove redundant state + refs (note: may still be useful because of less pointerchasing)
        crossPlatform = conf.isCrossPlatform();
        this.clazz = clazz;
        enumConstants = clazz.getEnumConstants();
        reg = infoRegistry;
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
            if (!reg.isStructMode()) {
                if ( conf.isForceSerializable() || getSer() != null ) {
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
            Predict annotation = (Predict) clazz.getAnnotation(Predict.class);
            if (annotation != null) {
                predict = annotation.value();
            }
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

        if (useCompatibleMode() && crossPlatform && getSer() == null && !clazz.isEnum())
            throw new RuntimeException("cannot support legacy JDK serialization methods in crossplatform mode. Define a serializer for this class " + clazz.getName());
    }

    byte[] bufferedName;

    public byte[] getBufferedName() {
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

    public boolean isAsciiNameShortString() {
        return isAsciiNameShortString;
    }

    public int getClzId() {
        return clzId;
    }

    public void setClzId(int clzId) {
        this.clzId = clzId;
    }

    public int getNumBoolFields() {
        FSTFieldInfo[] fis = getFieldInfo();
        for (int i = 0; i < fis.length; i++) {
            FSTFieldInfo fstFieldInfo = fis[i];
            if (fstFieldInfo.getType() != boolean.class) {
                return i;
            }
        }
        return fis.length;
    }

    public boolean isExternalizable() {
        return externalizable;
    }

    public final boolean isFlat() {
        return flat;
    }

    public final Class[] getPredict() {
        return predict;
    }

    public final Object newInstance(boolean doesRequireInit) {
        return instantiator.newInstance(clazz, cons, doesRequireInit || requiresInit, conf.isForceSerializable() );
    }

    /**
     * Sideeffect: sets hasTransient
     *
     * @param c
     * @param res
     * @return
     */
    public final List<Field> getAllFields(Class c, List<Field> res) {
        if (res == null) {
            res = new ArrayList<Field>();
        }
        if (c == null) {
            return res;
        }
        List<Field> c1 = Arrays.asList(c.getDeclaredFields());
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
        return getAllFields(c.getSuperclass(), res);
    }

    private boolean isTransient(Class c, Field field) {
        if (Modifier.isTransient(field.getModifiers()))
            return true;
        while (c.getName().indexOf("$") >= 0) {
            c = c.getSuperclass(); // patch fuer reallive queries, kontraktor spore
        }
        if ( field.getName().startsWith("this$") && c.getAnnotation(AnonymousTransient.class) != null )
            return true;
        return (c.getAnnotation(Transient.class) != null && field.getAnnotation(Serialize.class) == null);
    }

    public final FSTFieldInfo[] getFieldInfo() {
        return fieldInfo;
    }

    public final FSTFieldInfo[] getFieldInfoFiltered(Class... toRemove) {
        FSTFieldInfo[] fis = getFieldInfo();
        int count = 0;
        for (int i = 0; i < fis.length; i++) {
            FSTFieldInfo fi = fis[i];
            boolean skip = false;
            for (int j = 0; j < toRemove.length; j++) {
                Class aClass = toRemove[j];
                if (fi.getField().getDeclaringClass() == aClass) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                count++;
            }
        }
        FSTFieldInfo res[] = new FSTFieldInfo[count];
        count = 0;
        for (int i = 0; i < fis.length; i++) {
            FSTFieldInfo fi = fis[i];
            boolean skip = false;
            for (int j = 0; j < toRemove.length; j++) {
                Class aClass = toRemove[j];
                if (fi.getField().getDeclaringClass() == aClass) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                res[count++] = fis[i];
            }
        }
        return res;
    }

    public final FSTFieldInfo getFieldInfo(String name, Class declaringClass) {
        if (declaringClass == null) {
            return fieldMap.get(name);
        }
        return fieldMap.get(declaringClass.getName() + "#" + name);
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
            fieldMap.put(field.getDeclaringClass().getName() + "#" + field.getName(), fieldInfo[i]);
            fieldMap.put(field.getName(), fieldInfo[i]);
        }

        // compatibility info sort order
        Comparator<FSTFieldInfo> infocomp = new Comparator<FSTFieldInfo>() {
            @Override
            public int compare(FSTFieldInfo o1, FSTFieldInfo o2) {
                int res = 0;
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
                    if (declaringClass == null && declaringClass1 != null) {
                        return -1;
                    }
                    if (res == 0) {
                        return declaringClass.getName().compareTo(declaringClass1.getName());
                    }
                }
                return res;
            }
        };


        if (!reg.isStructMode()) {
            Class curCl = c;
            fields.clear();
            while (curCl != Object.class) {
                ObjectStreamClass os = null;
                try {
                    os = ObjectStreamClass.lookup(curCl);
                } catch (Exception e) {
                    throw FSTUtil.rethrow(e);
                }
                if (os != null) {
                    final ObjectStreamField[] fi = os.getFields();
                    List<FSTFieldInfo> curClzFields = new ArrayList<FSTFieldInfo>();
                    if (fi != null) {
                        for (int i = 0; i < fi.length; i++) {
                            ObjectStreamField objectStreamField = fi[i];
                            String ff = objectStreamField.getName();
                            final FSTFieldInfo fstFieldInfo = fieldMap.get(curCl.getName() + "#" + ff);
                            if (fstFieldInfo != null && fstFieldInfo.getField() != null) {
                                curClzFields.add(fstFieldInfo);
                                fields.add(fstFieldInfo.getField());
                            } else {
                                // TODO: throw exception ? (needs testing)
                            }
                        }
                    }
                    Collections.sort(curClzFields, infocomp);
                    FSTCompatibilityInfo info = new FSTCompatibilityInfo(curClzFields, curCl);
                    compInfo.put(curCl, info);
                    if (info.needsCompatibleMode()) {
                        requiresCompatibleMode = true;
                    }
                }
                curCl = curCl.getSuperclass();
            }
        }

        // default sort order
        Comparator<FSTFieldInfo> comp = defFieldComparator;
        if (!reg.isStructMode())
            Arrays.sort(fieldInfo, comp);
        int off = 8; // object header: length + clzId
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTFieldInfo fstFieldInfo = fieldInfo[i];
            Align al = fstFieldInfo.getField().getAnnotation(Align.class);
            if (al != null) {
                fstFieldInfo.align = al.value();
                int alignOff = fstFieldInfo.align(off);
                fstFieldInfo.alignPad = alignOff - off;
                off = alignOff;
            }
            fstFieldInfo.setStructOffset(off);
            off += fstFieldInfo.getStructSize();
        }
        structSize = off;
        writeReplaceMethod = FSTUtil.findDerivedMethod(
            c, "writeReplace", null, Object.class);
        readResolveMethod = FSTUtil.findDerivedMethod(
            c, "readResolve", null, Object.class);
        if (writeReplaceMethod != null) {
            writeReplaceMethod.setAccessible(true);
        }
        if (readResolveMethod != null) {
            readResolveMethod.setAccessible(true);
        }
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTFieldInfo fstFieldInfo = fieldInfo[i];
            fstFieldInfo.indexId = i;
        }
    }

    public int getStructSize() {
        return structSize;
    }

    public boolean useCompatibleMode() {
        return requiresCompatibleMode || writeReplaceMethod != null || readResolveMethod != null;
    }


    private FSTFieldInfo createFieldInfo(Field field) {
        field.setAccessible(true);
        Predict predict = crossPlatform ? null : field.getAnnotation(Predict.class); // needs to be iognored cross platform
        return new FSTFieldInfo(predict != null ? predict.value() : null, field, ignoreAnn);
    }

    public final Method getReadResolveMethod() {
        return readResolveMethod;
    }

    public final Method getWriteReplaceMethod() {
        return writeReplaceMethod;
    }

    public final Class getClazz() {
        return clazz;
    }

    public Object[] getEnumConstants() {
        return enumConstants;
    }

    public final static class FSTFieldInfo {

        final public static int BOOL = 1;
        final public static int BYTE = 2;
        final public static int CHAR = 3;
        final public static int SHORT = 4;
        final public static int INT = 5;
        final public static int LONG = 6;
        final public static int FLOAT = 7;
        final public static int DOUBLE = 8;

        Class possibleClasses[];
        FSTClazzInfo lastInfo;
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
        int align = 0;
        int alignPad = 0;
        byte[] bufferedName; // cache byte rep of field name (used for cross platform)

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
                version = (byte) (fi.isAnnotationPresent(Version.class) ? fi.getAnnotation(Version.class).value() : 0);
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

        public byte[] getBufferedName() {
            return bufferedName;
        }

        public void setBufferedName(byte[] bufferedName) {
            this.bufferedName = bufferedName;
        }

        public int align(int off) {
            while ((off / align) * align != off)
                off++;
            return off;
        }

        public int getIndexId() {
            return indexId;
        }

        public int getStructOffset() {
            return structOffset;
        }

        public void setStructOffset(int structOffset) {
            this.structOffset = structOffset;
        }

        public String[] getOneOf() {
            return oneOf;
        }

        public long getMemOffset() {
            return memOffset;
        }

        public int getAlign() {
            return align;
        }

        public int getAlignPad() {
            return alignPad;
        }

        public boolean isConditional() {
            return isConditional;
        }

        public FSTClazzInfo getLastInfo() {
            return lastInfo;
        }

        public void setLastInfo(FSTClazzInfo lastInfo) {
            this.lastInfo = lastInfo;
        }

        Class calcComponentType(Class c) {
            if (c.isArray()) {
                return calcComponentType(c.getComponentType());
            }
            return c;
        }

        public boolean isVolatile() {
            return Modifier.isVolatile(getField().getModifiers());
        }

        public final Class getType() {
            return type;
        }

        public boolean isArray() {
            return isArr;
        }

        public int getArrayDepth() {
            return arrayDim;
        }

        public Class getArrayType() {
            return arrayType;
        }

        public Class[] getPossibleClasses() {
            return possibleClasses;
        }

        void setPossibleClasses(Class[] possibleClasses) {
            this.possibleClasses = possibleClasses;
        }

        public Field getField() {
            return field;
        }

        public void calcIntegral() {
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

        public static int getIntegralCode(Class type) {
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
        public int getIntegralType() {
            return integralType;
        }

        public boolean isIntegral(Class type) {
            return type.isPrimitive();
        }

        /**
         * @return wether this is primitive or an array of primitives
         */
        public boolean isIntegral() {
            return integral;
        }

        public String getDesc() {
            return field != null ? "<" + field.getName() + " of " + field.getDeclaringClass().getSimpleName() + ">" : "<undefined referencee>";
        }

        public String toString() {
            return getDesc();
        }

        public boolean isFlat() {
            return flat;
        }

        public int getComponentStructSize() {
            if (arrayType == boolean.class || arrayType == byte.class)
                return 1;
            if (arrayType == char.class || arrayType == short.class)
                return 2;
            if (arrayType == int.class || arrayType == float.class)
                return 4;
            if (arrayType == long.class || arrayType == double.class)
                return 8;
            return 0; // object => cannot decide
        }

        public int getStructSize() {
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

        public boolean isPrimitive() {
            return primitive;
        }

        public final int getByteValue(Object obj) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getByte(obj, memOffset);
            }
            return field.getByte(obj);
        }

        public final int getCharValue(Object obj) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getChar(obj, memOffset);
            }
            return field.getChar(obj);
        }

        public final int getShortValue(Object obj) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getShort(obj, memOffset);
            }
            return field.getShort(obj);
        }

        public final int getIntValueUnsafe(Object obj) throws IllegalAccessException {
            return FSTUtil.unFlaggedUnsafe.getInt(obj, memOffset);
        }


        public final long getLongValueUnsafe(Object obj) throws IllegalAccessException {
            return FSTUtil.unFlaggedUnsafe.getLong(obj, memOffset);
        }

        public final boolean getBooleanValue(Object obj) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
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
        public final Object getObjectValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getObject(obj, memOffset);
            }
            return field.get(obj);
        }

        public final float getFloatValue(Object obj) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getFloat(obj, memOffset);
            }
            return field.getFloat(obj);
        }

        public final void setCharValue(Object newObj, char c) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putChar(newObj, memOffset, c);
                return;
            }
            field.setChar(newObj, c);
        }

        public final void setShortValue(Object newObj, short i1) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putShort(newObj, memOffset, i1);
                return;
            }
            field.setShort(newObj, i1);
        }

        public final void setObjectValue(Object target, Object value) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putObject(target, memOffset, value);
                return;
            }
            field.set(target, value);
        }

        public final void setFloatValue(Object newObj, float l) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putFloat(newObj, memOffset, l);
                return;
            }
            field.setFloat(newObj, l);
        }

        public final void setDoubleValue(Object newObj, double l) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putDouble(newObj, memOffset, l);
                return;
            }
            field.setDouble(newObj, l);
        }

        public final void setLongValue(Object newObj, long i1) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putLong(newObj, memOffset, i1);
                return;
            }
            field.setLong(newObj, i1);
        }

        public final long getLongValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getLong(obj, memOffset);
            }
            return field.getLong(obj);
        }

        public final double getDoubleValue(Object obj) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getDouble(obj, memOffset);
            }
            return field.getDouble(obj);
        }

        public final void setIntValue(Object newObj, int i1) throws IllegalAccessException {
            if (memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putInt(newObj, memOffset, i1);
                return;
            }
            field.setInt(newObj, i1);
        }

        public final int getIntValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getInt(obj, memOffset);
            }
            return field.getInt(obj);
        }

        public final void setBooleanValue(Object newObj, boolean i1) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putBoolean(newObj, memOffset, i1);
                return;
            }
            field.setBoolean(newObj, i1);
        }

        public final void setByteValue(Object newObj, byte b) throws IllegalAccessException {
            if (!FSTConfiguration.isAndroid && memOffset >= 0) {
                FSTUtil.unFlaggedUnsafe.putByte(newObj, memOffset, b);
                return;
            }
            field.setByte(newObj, b);
        }

    }

    public FSTObjectSerializer getSer() {
        if (ser == null) {
            if (clazz == null) {
                return null;
            }
            ser = reg.serializerRegistry.getSerializer(clazz);
            if (ser == null) {
                ser = FSTSerializerRegistry.NULL;
            }
        }
        if (ser == FSTSerializerRegistry.NULL) {
            return null;
        }
        return ser;
    }

    static class FSTCompatibilityInfo {
        Method writeMethod, readMethod;
        ObjectStreamClass objectStreamClass;
        List<FSTFieldInfo> infos;
        Class clazz;
        FSTFieldInfo infoArr[];

        public FSTCompatibilityInfo(List<FSTFieldInfo> inf, Class c) {
            readClazz(c);
            infos = inf;
            clazz = c;
        }

        public List<FSTFieldInfo> getFields() {
            return infos;
        }

        public FSTFieldInfo[] getFieldArray() {
            if (infoArr == null) {
                List<FSTClazzInfo.FSTFieldInfo> fields = getFields();
                final FSTFieldInfo[] fstFieldInfos = new FSTFieldInfo[fields.size()];
                fields.toArray(fstFieldInfos);
                Arrays.sort(fstFieldInfos, defFieldComparator);
                infoArr = fstFieldInfos;
            }
            return infoArr;
        }

        public Class getClazz() {
            return clazz;
        }

        public boolean needsCompatibleMode() {
            return writeMethod != null || readMethod != null;
        }

        public void readClazz(Class c) {
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

        public Method getReadMethod() {
            return readMethod;
        }

        public void setReadMethod(Method readMethod) {
            this.readMethod = readMethod;
        }

        public Method getWriteMethod() {
            return writeMethod;
        }

        public void setWriteMethod(Method writeMethod) {
            this.writeMethod = writeMethod;
        }

        public boolean isAsymmetric() {
            return (getReadMethod() == null && getWriteMethod() != null) || (getWriteMethod() == null && getReadMethod() != null);
        }

    }


}
