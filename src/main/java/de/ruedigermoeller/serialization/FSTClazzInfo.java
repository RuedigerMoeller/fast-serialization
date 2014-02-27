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

import de.ruedigermoeller.heapoff.structs.Align;
import de.ruedigermoeller.serialization.annotations.*;
import de.ruedigermoeller.serialization.util.FSTUtil;

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
    boolean equalIsIdentity;
    boolean equalIsBinary;
    boolean externalizable;
    boolean flat; // never share instances of this class
    FSTObjectSerializer ser;
    FSTFieldInfo fieldInfo[]; // serializable fields
    Class clazz;
    Constructor cons;
    int structSize = 0;

    FSTClazzInfoRegistry reg;

    public FSTClazzInfo(Class clazz, FSTClazzInfoRegistry infoRegistry, boolean ignoreAnnotations) {
        this.clazz = clazz;
        reg = infoRegistry;
        ignoreAnn = ignoreAnnotations;
        createFields(clazz);
        if ( !reg.isStructMode() ) {
            if (Externalizable.class.isAssignableFrom(clazz)) {
                externalizable = true;
                cons = FSTUtil.findConstructorForExternalize(clazz);
            } else {
                externalizable = false;
                cons = FSTUtil.findConstructorForSerializable(clazz);
            }
            if ( ! ignoreAnnotations ) {
                Predict annotation = (Predict) clazz.getAnnotation(Predict.class);
                if (annotation != null) {
                    predict = annotation.value();
                }
                equalIsIdentity = clazz.isAnnotationPresent(EqualnessIsIdentity.class);
                equalIsBinary = clazz.isAnnotationPresent(EqualnessIsBinary.class);
                flat = clazz.isAnnotationPresent(Flat.class);
            }
        }

        if (cons != null) {
            cons.setAccessible(true);
        }
    }

    public int getNumBoolFields() {
        FSTFieldInfo[] fis = getFieldInfo();
        for (int i = 0; i < fis.length; i++) {
            FSTFieldInfo fstFieldInfo = fis[i];
            if ( fstFieldInfo.getType() != boolean.class ) {
                return i;
            }
        }
        return fis.length;
    }

    public boolean isExternalizable() {
        return externalizable;
    }

    public final boolean isEqualIsBinary() {
        return equalIsBinary;
    }

    public final boolean isEqualIsIdentity() {
        return equalIsIdentity;
    }

    public final boolean isFlat() {
        return flat;
    }

    public final Class[] getPredict() {
        return predict;
    }

    public final Object newInstance() {
        try {
            if ( FSTUtil.unsafe != null ) {
                return FSTUtil.unsafe.allocateInstance(clazz);
            }
            return cons.newInstance();
        } catch (Throwable ignored) {
            return null;
        }
    }

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
            res.add(0,field);
        }
        for (int i = 0; i < res.size(); i++) {
            Field field = res.get(i);
            if ( Modifier.isStatic(field.getModifiers()) || isTransient(c,field) )
            {
                res.remove(i);
                i--;
            }
        }
        return getAllFields(c.getSuperclass(), res);
    }

    private boolean isTransient(Class c, Field field) {
        if ( Modifier.isTransient(field.getModifiers()) )
            return true;
        while ( c.getName().indexOf("$") >= 0 ) {
            c = c.getSuperclass(); // patch fuer reallive queries
        }
        return (c.getAnnotation(Transient.class) != null && field.getAnnotation(Serialize.class) == null);
    }

    public final FSTFieldInfo[] getFieldInfo() {
        return fieldInfo;
    }

    public final FSTFieldInfo[] getFieldInfoFiltered(Class ... toRemove) {
        FSTFieldInfo[] fis = getFieldInfo();
        int count = 0;
        for (int i = 0; i < fis.length; i++) {
            FSTFieldInfo fi = fis[i];
            boolean skip = false;
            for (int j = 0; j < toRemove.length; j++) {
                Class aClass = toRemove[j];
                if ( fi.getField().getDeclaringClass() == aClass ) {
                    skip = true;
                    break;
                }
            }
            if ( ! skip ) {
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
                if ( fi.getField().getDeclaringClass() == aClass ) {
                    skip = true;
                    break;
                }
            }
            if ( ! skip ) {
                res[count++] = fis[i];
            }
        }
        return res;
    }

    public final FSTFieldInfo getFieldInfo(String name, Class declaringClass) {
        if ( declaringClass == null ) {
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

        // comp info sort order
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


        if ( ! reg.isStructMode() ) {
            Class curCl = c;
            fields.clear();
            while (curCl != Object.class) {
                ObjectStreamClass os = null;
                try {
                    os = ObjectStreamClass.lookup(curCl);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
        if ( ! reg.isStructMode() )
            Arrays.sort(fieldInfo, comp);
        int off = 8; // object header: length + clzId
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTFieldInfo fstFieldInfo = fieldInfo[i];
            Align al = fstFieldInfo.getField().getAnnotation(Align.class);
            if ( al != null ) {
                fstFieldInfo.align = al.value();
                int alignOff = fstFieldInfo.align(off);
                fstFieldInfo.alignPad = alignOff-off;
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
    }

    public int getStructSize() {
        return structSize;
    }

    public boolean useCompatibleMode() {
        return requiresCompatibleMode || writeReplaceMethod != null || readResolveMethod != null;
    }


    private FSTFieldInfo createFieldInfo(Field field) {
        field.setAccessible(true);
        Predict predict = field.getAnnotation(Predict.class);
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
        boolean thin = false;
        boolean isCompressed = false;
        boolean isConditional = false;

        final Field field;
        Class type;
        boolean integral = false;
        boolean primitive = false;
        boolean isArr = false;
        int integralType;
        long memOffset = -1;

        int structOffset = 0;
        int align = 0;
        int alignPad = 0;

        public FSTFieldInfo(Class[] possibleClasses, Field fi, boolean ignoreAnnotations) {
            this.possibleClasses = possibleClasses;
            field = fi;
            if (fi == null) {
                isArr = false;
            } else {
                isArr = field.getType().isArray();
                type = fi.getType();
                primitive = type.isPrimitive();
                if ( FSTUtil.unFlaggedUnsafe != null ) {
                    fi.setAccessible(true);
                    if ( ! Modifier.isStatic(fi.getModifiers()) ) {
                        try {
                            memOffset = (int)FSTUtil.unFlaggedUnsafe.objectFieldOffset(fi);
//                            int x = 1;
                        } catch ( Throwable th ) {
//                            int y = 1;
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
            if ( fi != null && ! ignoreAnnotations ) {
                flat = fi.isAnnotationPresent(Flat.class);
                thin = fi.isAnnotationPresent(Thin.class);
                isConditional = fi.isAnnotationPresent(Conditional.class);
                isCompressed = fi.isAnnotationPresent(Compress.class);
                if (isIntegral()) {
                    isConditional = false;
                }
                OneOf annotation = fi.getAnnotation(OneOf.class);
                if ( annotation != null ) {
                    oneOf = annotation.value();
                }
            }

        }

        public int align(int off) {
            while( (off/align)*align != off )
                off++;
            return off;
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

        public boolean isCompressed() {
            return isCompressed;
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

        public boolean isThin() {
            return thin;
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

        public void setPossibleClasses(Class[] possibleClasses) {
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
                if ( type == boolean.class ) {
                    integralType = BOOL;
                } else
                if ( type == byte.class ) {
                    integralType = BYTE;
                } else
                if ( type == char.class ) {
                    integralType = CHAR;
                } else
                if ( type == short.class ) {
                    integralType = SHORT;
                } else
                if ( type == int.class ) {
                    integralType = INT;
                } else
                if ( type == long.class ) {
                    integralType = LONG;
                } else
                if ( type == float.class ) {
                    integralType = FLOAT;
                } else
                if ( type == double.class ) {
                    integralType = DOUBLE;
                }
            }
        }

        /**
         * only set if is not an array, but a direct native field type
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
            if ( arrayType == boolean.class || arrayType == byte.class )
                return 1;
            if ( arrayType == char.class || arrayType == short.class )
                return 2;
            if ( arrayType == int.class || arrayType == float.class )
                return 4;
            if ( arrayType == long.class || arrayType == double.class )
                return 8;
            return 0; // object => cannot decide
        }

        public int getStructSize() {
            if ( type == boolean.class || type == byte.class )
                return 1;
            if ( type == char.class || type == short.class )
                return 2;
            if ( type == int.class || type == float.class )
                return 4;
            if ( type == long.class || type == double.class )
                return 8;
            if ( isArray() ) {
                if ( isIntegral() )
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
            if (memOffset >= 0 ) {
                return FSTUtil.unFlaggedUnsafe.getByte(obj,memOffset);
            }
            return field.getByte(obj);
        }

        public final int getCharValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0) {
                return FSTUtil.unFlaggedUnsafe.getChar(obj,memOffset);
            }
            return field.getChar(obj);
        }

        public final int getShortValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0 ) {
                return FSTUtil.unFlaggedUnsafe.getShort(obj,memOffset);
            }
            return field.getShort(obj);
        }

        public final int getIntValueUnsafe(Object obj) throws IllegalAccessException {
            return FSTUtil.unFlaggedUnsafe.getInt(obj,memOffset);
        }


        public final long getLongValueUnsafe(Object obj) throws IllegalAccessException {
            return FSTUtil.unFlaggedUnsafe.getLong(obj,memOffset);
        }

        public final boolean getBooleanValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0 ) {
                return FSTUtil.unFlaggedUnsafe.getBoolean(obj,memOffset);
            }
            return field.getBoolean(obj);
        }

        public final Object getObjectValueUnsafe(Object obj) throws IllegalAccessException {
            return FSTUtil.unsafe.getObject(obj,memOffset);
        }

        public final Object getObjectValue(Object obj) throws IllegalAccessException {
            return field.get(obj);
        }

        public final float getFloatValue(Object obj) throws IllegalAccessException {
            if (memOffset >= 0 ) {
                return FSTUtil.unFlaggedUnsafe.getFloat(obj,memOffset);
            }
            return field.getFloat(obj);
        }

        public final double getDoubleValueUnsafe(Object obj) throws IllegalAccessException {
            return FSTUtil.unsafe.getDouble(obj,memOffset);
        }

        public final void setCharValue(Object newObj, char c) throws IllegalAccessException {
            if (memOffset >= 0 ) {
                FSTUtil.unFlaggedUnsafe.putChar(newObj,memOffset,c);
                return;
            }
            field.setChar(newObj, c);
        }

        public final void setShortValue(Object newObj, short i1) throws IllegalAccessException {
            if (memOffset >= 0 ) {
                FSTUtil.unFlaggedUnsafe.putShort(newObj,memOffset,i1);
                return;
            }
            field.setShort(newObj, i1);
        }

        public final void setObjectValueUnsafe(Object newObj, Object i1) throws IllegalAccessException {
            FSTUtil.unsafe.putObject(newObj,memOffset,i1);
        }

        public final void setObjectValue(Object newObj, Object i1) throws IllegalAccessException {
            field.set(newObj, i1);
        }

        public final void setFloatValue(Object newObj,  float l) throws IllegalAccessException {
            if (memOffset >= 0  ) {
                FSTUtil.unFlaggedUnsafe.putFloat(newObj,memOffset,l);
                return;
            }
            field.setFloat(newObj, l);
        }

        public final void setDoubleValueUnsafe(Object newObj, double l) throws IllegalAccessException {
            FSTUtil.unsafe.putDouble(newObj,memOffset,l);
        }

        public final void setDoubleValue(Object newObj, double l) throws IllegalAccessException {
            field.setDouble(newObj, l);
        }

        public final void setIntValueUnsafe(Object newObj, int i1) throws IllegalAccessException {
            FSTUtil.unsafe.putInt(newObj,memOffset,i1);
        }

        public final void setLongValueUnsafe(Object newObj, long i1) throws IllegalAccessException {
            FSTUtil.unsafe.putLong(newObj,memOffset,i1);
        }

        public final void setLongValue(Object newObj, long i1) throws IllegalAccessException {
            field.setLong(newObj, i1);
        }

        public final long getLongValue(Object obj) throws IllegalAccessException {
            return field.getLong(obj);
        }

        public final double getDoubleValue(Object obj) throws IllegalAccessException {
            return field.getDouble(obj);
        }

        public final void setIntValue(Object newObj, int i1) throws IllegalAccessException {
            field.setInt(newObj, i1);
        }

        public final int getIntValue(Object obj) throws IllegalAccessException {
            return field.getInt(obj);
        }

        public final void setBooleanValue(Object newObj, boolean i1) throws IllegalAccessException {
            if (memOffset >= 0 ) {
                FSTUtil.unFlaggedUnsafe.putBoolean(newObj,memOffset,i1);
                return;
            }
            field.setBoolean(newObj, i1);
        }

        public final void setByteValue(Object newObj, byte b) throws IllegalAccessException {
            if (memOffset >= 0 ) {
                FSTUtil.unFlaggedUnsafe.putByte(newObj,memOffset,b);
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
                Arrays.sort(fstFieldInfos,defFieldComparator);
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
