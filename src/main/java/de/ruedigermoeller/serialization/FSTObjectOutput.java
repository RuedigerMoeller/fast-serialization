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

import de.ruedigermoeller.serialization.util.FSTUtil;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 03.11.12
 * Time: 12:26
 * To change this template use File | Settings | File Templates.
 */

/**
 * replacement of ObjectOutputStream
 */
public class FSTObjectOutput implements ObjectOutput {

    static final byte ONE_OF = -18;
    static final byte BIG_BOOLEAN_FALSE = -17;
    static final byte BIG_BOOLEAN_TRUE = -16;
    static final byte BIG_LONG = -10;
    static final byte BIG_INT = -9;
    static final byte COPYHANDLE = -8;
    static final byte HANDLE = -7;
    static final byte ENUM = -6;
    static final byte ARRAY = -5;
    static final byte STRING = -4;
    static final byte TYPED = -3; // var class == object written class
    //static final byte PRIMITIVE_ARRAY = -2;
    static final byte NULL = -1;
    static final byte OBJECT = 0;
    protected FSTEncoder codec;

    public FSTClazzNameRegistry clnames; // immutable
    protected FSTConfiguration conf; // immutable

    protected FSTObjectRegistry objects;
    protected int curDepth = 0;
    protected int writeExternalWriteAhead = 8000; // max size an external may occupy FIXME: document this, create annotation to configure this
    
    /**
     * Creates a new FSTObjectOutput stream to write data to the specified
     * underlying output stream.
     * uses Default Configuration singleton
     */
    public FSTObjectOutput(OutputStream out) {
        this(out, FSTConfiguration.getDefaultConfiguration());
    }

    /**
     * Creates a new FSTObjectOutput stream to write data to the specified
     * underlying output stream. The counter <code>written</code> is
     * set to zero.
     * Don't create a FSTConfiguration with each stream, just create one global static configuration and reuse it.
     * FSTConfiguration is threadsafe.
     *
     * @param out the underlying output stream, to be saved for later
     *            use.
     */
    public FSTObjectOutput(OutputStream out, FSTConfiguration conf) {
        this.conf = conf;
        codec = new FSTStreamEncoder(conf);
        codec.setOutstream(out);

        objects = (FSTObjectRegistry) conf.getCachedObject(FSTObjectRegistry.class);
        if ( objects == null ) {
            objects = new FSTObjectRegistry(conf);
            objects.disabled = !conf.isShareReferences();
        } else {
            objects.clearForWrite();
        }
        clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
        if ( clnames == null ) {
            clnames = new FSTClazzNameRegistry(conf.getClassRegistry(), conf);
        } else {
            clnames.clear();
        }
    }

    /**
     * serialize without an underlying stream, the resulting byte array of writing to
     * this FSTObjectOutput can be accessed using getBuffer(), the size using getWritten().
     *
     * Don't create a FSTConfiguration with each stream, just create one global static configuration and reuse it.
     * FSTConfiguration is threadsafe.
     * @param conf
     * @throws IOException
     */
    public FSTObjectOutput(FSTConfiguration conf) {
        this(null,conf);
        codec.setOutstream(null);
    }

    /**
     * serialize without an underlying stream, the resulting byte array of writing to
     * this FSTObjectOutput can be accessed using getBuffer(), the size using getWritten().
     *
     * uses default configuration singleton
     *
     * @throws IOException
     */
    public FSTObjectOutput() {
        this(null, FSTConfiguration.getDefaultConfiguration());
        codec.setOutstream(null);
    }

    /**
     * Flushes this data output stream. This forces any buffered output
     * bytes to be written out to the stream.
     * <p/>
     * The <code>flush</code> method of <code>DataOutputStream</code>
     * calls the <code>flush</code> method of its underlying output stream.
     *
     * @throws java.io.IOException if an I/O error occurs.
     * @see java.io.FilterOutputStream#out
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException {
        codec.flush();
    }

    static ByteArrayOutputStream empty = new ByteArrayOutputStream(0);

    boolean closed = false;
    @Override
    public void close() throws IOException {
        flush();
        closed = true;
        codec.close();
        resetAndClearRefs();
        conf.returnObject(objects,clnames);
    }


    /**
     * since the stock writeXX methods on InputStream are final, i can't ensure sufficient bufferSize on the output buffer
     * before calling writeExternal. Default value is 5000 bytes. If you make use of the externalizable interface
     * and write larger Objects a) cast the ObjectOutput in readExternal to FSTObjectOutput and call ensureFree on this
     * in your writeExternal method or b) statically set a sufficient maximum using this method.
     */
    public int getWriteExternalWriteAhead() {
        return writeExternalWriteAhead;
    }

    /**
     * since the stock writeXX methods on InputStream are final, i can't ensure sufficient bufferSize on the output buffer
     * before calling writeExternal. Default value is 5000 bytes. If you make use of the externalizable interface
     * and write larger Objects a) cast the ObjectOutput in readExternal to FSTObjectOutput and call ensureFree on this
     * in your writeExternal method or b) statically set a sufficient maximum using this method.
     * @param writeExternalWriteAhead
     */
    public void setWriteExternalWriteAhead(int writeExternalWriteAhead) {
        this.writeExternalWriteAhead = writeExternalWriteAhead;
    }

    public void ensureFree(int bytes) throws IOException {
        codec.ensureFree(bytes);
    }

    //////////////////////////////////////////////////////////////////////////
    //
    // ObjectOutput interface impl
    //
    @Override
    public void writeObject(Object obj) throws IOException {
        writeObject(obj,(Class[])null);
    }

    @Override
    public void write(int b) throws IOException {
        codec.writeFByte(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        codec.writeFByteArr(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        codec.writeFByteArr(b,off,len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        codec.writeFByte(v?1:0);
    }

    @Override
    public void writeByte(int v) throws IOException {
        codec.writeFByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        codec.writeFShort((short) v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        codec.writeFChar((char) v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        codec.writeFInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        codec.writeFLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        codec.writeFFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        codec.writeFDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        codec.writeFByteArr(s.getBytes());
    }

    @Override
    public void writeChars(String s) throws IOException {
        codec.writeFCharArr(s.toCharArray());
    }

    @Override
    public void writeUTF(String s) throws IOException {
        codec.writeStringUTF(s);
    }

    //
    // .. end interface impl
    /////////////////////////////////////////////////////
    
    public void writeObject(Object obj, Class... possibles) throws IOException {
        curDepth++;
        try {
            if ( possibles != null && possibles.length > 1 ) {
                for (int i = 0; i < possibles.length; i++) {
                    Class possible = possibles[i];
                    clnames.registerClass(possible);
                }
            }
            writeObjectInternal(obj, possibles);
        } finally {
            codec.flush();
            curDepth--;
        }
    }

    FSTClazzInfo.FSTFieldInfo refs[] = new FSTClazzInfo.FSTFieldInfo[20];

    //avoid creation of dummy ref
    FSTClazzInfo.FSTFieldInfo getCachedFI( Class... possibles ) {
        if ( curDepth >= refs.length ) {
            return new FSTClazzInfo.FSTFieldInfo(possibles, null, true);
        } else {
            FSTClazzInfo.FSTFieldInfo inf = refs[curDepth];
            if ( inf == null ) {
                inf = new FSTClazzInfo.FSTFieldInfo(possibles, null, true);
                refs[curDepth] = inf;
                return inf;
            }
            inf.setPossibleClasses(possibles);
            return inf;
        }
    }

    public void writeObjectInternal(Object obj, Class... possibles) throws IOException {
        if ( curDepth == 0 ) {
            throw new RuntimeException("not intended to be called from external application. Use public writeObject instead");
        }
        FSTClazzInfo.FSTFieldInfo info = getCachedFI(possibles);
        curDepth++;
        writeObjectWithContext(info, obj);
        curDepth--;
    }

    /**
     * hook for debugging profiling. empty impl, you need to subclass to make use of this hook
     * @param obj
     * @param streamPosition
     */
    protected void objectWillBeWritten( Object obj, int streamPosition ) {
//        System.out.println("write:"+obj.getClass()+" "+streamPosition);
    }

    /**
     * hook for debugging profiling. empty impl, you need to subclass to make use of this hook
     * @param obj
     * @param oldStreamPosition
     * @param streamPosition
     */
    protected void objectHasBeenWritten( Object obj, int oldStreamPosition, int streamPosition ) {
//        System.out.println("ewrite:"+obj.getClass()+" "+streamPosition);
    }
    
    int tmp[] = {0};
    // splitting this slows down ...
    protected void writeObjectWithContext(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite) throws IOException {
        int startPosition = codec.getWritten();
        boolean dontShare = objects.disabled;
        objectWillBeWritten(toWrite,startPosition);

        try {
            if ( toWrite == null ) {
                codec.writeFByte(NULL);
                return;
            }
            final Class clazz = toWrite.getClass();
            if ( clazz == String.class ) {
                String[] oneOf = referencee.getOneOf();
                if ( oneOf != null ) {
                    for (int i = 0; i < oneOf.length; i++) {
                        String s = oneOf[i];
                        if ( s.equals(toWrite) ) {
                            codec.writeFByte(ONE_OF);
                            codec.writeFByte(i);
                            return;
                        }
                    }
                }
                if (dontShare) {
                    codec.writeFByte(STRING);
                    codec.writeStringUTF((String) toWrite);
                    return;
                }
            } else if ( clazz == Integer.class ) {
                codec.writeFByte(BIG_INT);
                codec.writeFInt(((Integer) toWrite).intValue()); return;
            } else if ( clazz == Long.class ) {
                codec.writeFByte(BIG_LONG);
                codec.writeFLong(((Long) toWrite).longValue()); return;
            } else if ( clazz == Boolean.class ) {
                codec.writeFByte(((Boolean) toWrite).booleanValue() ? BIG_BOOLEAN_TRUE : BIG_BOOLEAN_FALSE); return;
            } else if (clazz.isArray()) {
                codec.writeFByte(ARRAY);
                writeArray(referencee, toWrite);
                return;
            } else if ( (referencee.getType() != null && referencee.getType().isEnum()) || toWrite instanceof Enum ) {
                codec.writeFByte(ENUM);
                boolean isEnumClass = toWrite.getClass().isEnum();
                if ( ! isEnumClass ) {
                    // weird stuff ..
                    Class c = toWrite.getClass();
                    while ( c != null && ! c.isEnum() ) {
                        c = toWrite.getClass().getEnclosingClass();
                    }
                    if ( c == null ) {
                        throw new RuntimeException("Can't handle this enum: "+toWrite.getClass());
                    }
                    writeClass(c);
                } else {
                    writeClass(getFstClazzInfo(referencee,toWrite.getClass()));
                }
                codec.writeFInt(((Enum) toWrite).ordinal());
                return;
            }

            FSTClazzInfo serializationInfo = getFstClazzInfo(referencee, clazz);
            // check for identical / equal objects
            FSTObjectSerializer ser = serializationInfo.getSer();
            if ( ! dontShare && ! referencee.isFlat() && ! serializationInfo.isFlat() && ( ser == null || !ser.alwaysCopy() ) ) {
                boolean needsEqualMap = serializationInfo.isEqualIsBinary() || serializationInfo.isEqualIsIdentity();
                int handle = objects.registerObjectForWrite(toWrite, !needsEqualMap, codec.getWritten(), serializationInfo, tmp);
                // determine class header
                if ( handle >= 0 ) {
                    final boolean isIdentical = tmp[0] == 0; //objects.getReadRegisteredObject(handle) == toWrite;
                    if ( isIdentical || serializationInfo.isEqualIsIdentity()) {
                        codec.writeFByte(HANDLE);
                        codec.writeFInt(handle);
                        return;
                    } else if ( serializationInfo.isEqualIsBinary() ) {
                        codec.writeFByte(COPYHANDLE);
                        codec.writeFInt(handle);
                        // unneccessary objects.registerObjectForWrite(toWrite, true, written,serializationInfo); // enforce new id, in case another reference to toWrite exists
                        return;
                    }
                }
            }
            if ( ser == null ) {
                // handle write replace
                if ( ! dontShare ) {
                    if ( serializationInfo.getWriteReplaceMethod() != null ) {
                        Object replaced = null;
                        try {
                            replaced = serializationInfo.getWriteReplaceMethod().invoke(toWrite);
                        } catch (Exception e) {
                            throw FSTUtil.rethrow(e);
                        }
                        if ( replaced != toWrite ) {
                            toWrite = replaced;
                            serializationInfo = getClassInfoRegistry().getCLInfo(toWrite.getClass());
                            // fixme: update object map
                        }
                    }
                    // clazz uses some JDK special stuff (frequently slow)
                    if ( serializationInfo.useCompatibleMode() ) {
                        writeObjectCompatible(referencee, toWrite, serializationInfo);
                        return;
                    }
                }
                writeObjectHeader(serializationInfo, referencee, toWrite);
                defaultWriteObject(toWrite, serializationInfo);
            } else {
                // Object header (nothing written till here)
                int pos = codec.getWritten();
                writeObjectHeader(serializationInfo, referencee, toWrite);
                // write object depending on type (custom, externalizable, serializable/java, default)
                ser.writeObject(this, toWrite, serializationInfo, referencee, pos);
            }
        } finally {
            objectHasBeenWritten(toWrite,startPosition,codec.getWritten());
        }
    }

    /**
     * if class is same as last referenced, returned cached clzinfo, else do a lookup
     */
    protected FSTClazzInfo getFstClazzInfo(FSTClazzInfo.FSTFieldInfo referencee, Class clazz) {
        FSTClazzInfo serializationInfo = null;
        FSTClazzInfo lastInfo = referencee.lastInfo;
        if ( lastInfo != null && lastInfo.getClazz() == clazz ) {
            serializationInfo = lastInfo;
        } else {
            serializationInfo = getClassInfoRegistry().getCLInfo(clazz);
            referencee.lastInfo = serializationInfo;
        }
        return serializationInfo;
    }

    public void defaultWriteObject(Object toWrite, FSTClazzInfo serializationInfo) throws IOException {
        if ( serializationInfo.isExternalizable() ) {
            codec.ensureFree(writeExternalWriteAhead);
            ((Externalizable) toWrite).writeExternal(this);
        } else {
            FSTClazzInfo.FSTFieldInfo[] fieldInfo = serializationInfo.getFieldInfo();
            writeObjectFields(toWrite, serializationInfo, fieldInfo);
        }
    }

    protected void writeObjectCompatible(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite, FSTClazzInfo serializationInfo) throws IOException {
        // Object header (nothing written till here)
        writeObjectHeader(serializationInfo, referencee, toWrite);
        Class cl = serializationInfo.getClazz();
        writeObjectCompatibleRecursive(referencee,toWrite,serializationInfo,cl);
    }

    private void writeObjectCompatibleRecursive(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite, FSTClazzInfo serializationInfo, Class cl) throws IOException {
        FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = serializationInfo.compInfo.get(cl);
        if ( ! Serializable.class.isAssignableFrom(cl) ) {
            return;
        }
        writeObjectCompatibleRecursive(referencee,toWrite,serializationInfo,cl.getSuperclass());
        if ( fstCompatibilityInfo != null && fstCompatibilityInfo.getWriteMethod() != null ) {
            try {
                fstCompatibilityInfo.getWriteMethod().invoke(toWrite,getObjectOutputStream(cl, serializationInfo,referencee,toWrite));
            } catch (Exception e) {
                throw FSTUtil.rethrow(e);
            }
        } else {
            if ( fstCompatibilityInfo != null ) {
                writeObjectFields(toWrite, serializationInfo, fstCompatibilityInfo.getFieldArray());
            }
        }
    }

    private void writeObjectFields(Object toWrite, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo) throws IOException {
        try {
            int booleanMask = 0;
            int boolcount = 0;
            final int length = fieldInfo.length;
            int j = 0;
            for (;; j++) {
                if ( j == length ) {
                    if ( boolcount > 0 ) {
                        codec.writeFByte(booleanMask << (8 - boolcount));
                    }
                    break;
                }
                final FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[j];
                if ( subInfo.getIntegralType() != subInfo.BOOL ) {
                    if ( boolcount > 0 ) {
                        codec.writeFByte(booleanMask << (8 - boolcount));
                    }
                    break;
                } else {
                    if ( boolcount == 8 ) {
                        codec.writeFByte(booleanMask << (8 - boolcount));
                        boolcount = 0; booleanMask = 0;
                    }
                    boolean booleanValue = subInfo.getBooleanValue( toWrite);
                    booleanMask = booleanMask<<1;
                    booleanMask = (booleanMask|(booleanValue?1:0));
                    boolcount++;
                }
            }
            for (int i = j; i < length; i++)
            {
                final FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if ( subInfo.isPrimitive() ) {
                    // speed safe
                    int integralType = subInfo.getIntegralType();
                    switch (integralType) {
                        case FSTClazzInfo.FSTFieldInfo.BYTE:
                            codec.writeFByte(subInfo.getByteValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.CHAR:
                            codec.writeFChar((char) subInfo.getCharValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.SHORT:
                            codec.writeFShort((short) subInfo.getShortValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.INT:
                            codec.writeFInt(subInfo.getIntValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.LONG:
                            codec.writeFLong(subInfo.getLongValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.FLOAT:
                            codec.writeFFloat(subInfo.getFloatValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.DOUBLE:
                            codec.writeFDouble(subInfo.getDoubleValue(toWrite)); break;
                    }
                } else if (subInfo.isConditional())
                {
                    final int conditional = codec.getWritten();
                    codec.skip(4);
                    // object
                    Object subObject = subInfo.getObjectValue(toWrite);
                    if ( subObject == null ) {
                        codec.writeFByte(NULL);
                    } else {
                        writeObjectWithContext(subInfo, subObject);
                    }
                    int v = codec.getWritten();
                    codec.writeInt32At(conditional,v);
                } else {
                    // object
                    Object subObject = subInfo.getObjectValue(toWrite);
                    if ( subObject == null ) {
                        codec.writeFByte(NULL);
                    } else {
                        writeObjectWithContext(subInfo, subObject);
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            throw FSTUtil.rethrow(ex);
        }
    }

    // write identical to other version, but take field values from hashmap
    private void writeCompatibleObjectFields(Object toWrite, Map fields, FSTClazzInfo.FSTFieldInfo[] fieldInfo) throws IOException {
        int booleanMask = 0;
        int boolcount = 0;
        for (int i = 0; i < fieldInfo.length; i++) {
            try {
                FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                boolean isarr = subInfo.isArray();
                Class subInfType = subInfo.getType();
                if ( subInfType != boolean.class || isarr) {
                    if ( boolcount > 0 ) {
                        codec.writeFByte(booleanMask << (8 - boolcount));
                        boolcount = 0; booleanMask = 0;
                    }
                }
                if ( subInfo.isIntegral() && !isarr) {
                    if ( subInfType == boolean.class ) {
                        if ( boolcount == 8 ) {
                            codec.writeFByte(booleanMask << (8 - boolcount));
                            boolcount = 0; booleanMask = 0;
                        }
                        boolean booleanValue = ((Boolean)fields.get(subInfo.getField().getName())).booleanValue();
                        booleanMask = booleanMask<<1;
                        booleanMask = (booleanMask|(booleanValue?1:0));
                        boolcount++;
                    } else
                    if ( subInfType == int.class ) {
                        codec.writeFInt(((Number) fields.get(subInfo.getField().getName())).intValue());
                    } else
                    if ( subInfType == long.class ) {
                        codec.writeFLong(((Number) fields.get(subInfo.getField().getName())).longValue());
                    } else
                    if ( subInfType == byte.class ) {
                        codec.writeFByte(((Number) fields.get(subInfo.getField().getName())).byteValue());
                    } else
                    if ( subInfType == char.class ) {
                        codec.writeFChar((char) ((Number) fields.get(subInfo.getField().getName())).intValue());
                    } else
                    if ( subInfType == short.class ) {
                        codec.writeFShort(((Number) fields.get(subInfo.getField().getName())).shortValue());
                    } else
                    if ( subInfType == float.class ) {
                        codec.writeFFloat(((Number) fields.get(subInfo.getField().getName())).floatValue());
                    } else
                    if ( subInfType == double.class ) {
                        codec.writeFDouble(((Number) fields.get(subInfo.getField().getName())).doubleValue());
                    }
                } else {
                    // object
                    Object subObject = fields.get(subInfo.getField().getName());
                    writeObjectWithContext(subInfo, subObject);
                }
            } catch (Exception ex) {
                throw FSTUtil.rethrow(ex);
            }
        }
        if ( boolcount > 0 ) {
            codec.writeFByte(booleanMask << (8 - boolcount));
            boolcount = 0; booleanMask = 0;
        }
    }

    protected void writeObjectHeader(final FSTClazzInfo clsInfo, final FSTClazzInfo.FSTFieldInfo referencee, final Object toWrite) throws IOException {
        if (clsInfo.isEqualIsBinary() ) {
            codec.writeFByte(OBJECT);
            writeClass(clsInfo);
            return;
        }
        if ( toWrite.getClass() == referencee.getType()
                && ! clsInfo.useCompatibleMode() )
        {
            codec.writeFByte(TYPED);
        } else {
            final Class[] possibleClasses = referencee.getPossibleClasses();
            if ( possibleClasses == null ) {
                codec.writeFByte(OBJECT);
                writeClass(clsInfo);
            } else {
                final int length = possibleClasses.length;
                for (int j = 0; j < length; j++) {
                    final Class possibleClass = possibleClasses[j];
                    if ( possibleClass == toWrite.getClass() ) {
                        codec.writeFByte(j + 1);
                        return;
                    }
                }
                codec.writeFByte(OBJECT);
                writeClass(clsInfo);
            }
        }
    }

    protected void writeArray(FSTClazzInfo.FSTFieldInfo referencee, Object array) throws IOException {
        if ( array == null ) {
            writeClass(Object.class);
            codec.writeFInt(-1);
            return;
        }

        final int len = Array.getLength(array);
        writeClass(array);
        codec.writeFInt(len);
        Class<?> componentType = array.getClass().getComponentType();
        if ( ! componentType.isArray() ) {
            if ( componentType == byte.class ) {
                codec.writeFByteArr((byte[]) array, 0, len);
            } else
            if ( componentType == char.class ) {
                codec.writeFCharArr((char[]) array);
            } else
            if ( componentType == short.class ) {
                codec.writeFShortArr((short[]) array);
            } else
            if ( componentType == int.class ) {
                codec.writeFIntArr((int[]) array);
            } else
            if ( componentType == double.class ) {
                codec.writeFDoubleArr((double[]) array);
            } else
            if ( componentType == float.class ) {
                codec.writeFFloatArr((float[]) array);
            } else
            if ( componentType == long.class ) {
                codec.writeFLongArr((long[]) array);
            } else
            if ( componentType == boolean.class ) {
                codec.writeFBooleanArr((boolean[]) array);
            } else {
                Object arr[] = (Object[])array;
                for ( int i = 0; i < len; i++ )
                {
                    Object toWrite = arr[i];
                    if ( toWrite == null ) {
                        codec.writeFByte(NULL);
                    } else {
                        writeObjectWithContext(referencee, toWrite);
                    }
                }
            }
        } else {
            Object[] arr = (Object[])array;
            FSTClazzInfo.FSTFieldInfo ref1 = new FSTClazzInfo.FSTFieldInfo(referencee.getPossibleClasses(), null, conf.getCLInfoRegistry().isIgnoreAnnotations());
            for ( int i = 0; i < len; i++ ) {
                Object subArr = arr[i];
                if ( subArr != null ) {
                    if ( ! FSTUtil.isPrimitiveArray(subArr.getClass()) ) {
                        objects.registerObjectForWrite(subArr, true, codec.getWritten(), null, tmp); // fixme: shared refs
                    }
                }
                writeArray(ref1, subArr);
            }
        }
    }

    static int charMap[] = new int[256];
    static String enc = "e itsanhurdmwgvl";//fbkopjxczyq";
    static {
        charMap[32] = 0;
        for (int i = 0; i < charMap.length; i++) {
            charMap[i] =999;
        }
        for (int i=0; i < enc.length();i++) {
            charMap[enc.charAt(i)] = i;
        }
    }

    public void writeStringUTF(String str) throws IOException {

        codec.writeStringUTF(str);
    }

    public final void writeClass(Class cl) throws IOException {
        clnames.encodeClass(this,cl);
    }

    public final void writeClass(FSTClazzInfo clInf) throws IOException {
        clnames.encodeClass(this,clInf);
    }

    public final void writeClass(Object toWrite) throws IOException {
        clnames.encodeClass(this,toWrite.getClass());
    }

    void resetAndClearRefs() {
        codec.reset();
        objects.clearForWrite();
        clnames.clear();
    }

    /**
     * if out == null => automatically create/reuse a bytebuffer
     *
     * @param out
     */
    public void resetForReUse( OutputStream out ) {
        if ( closed )
            throw new RuntimeException("Can't reuse closed stream");
        codec.reset();
        if ( out != null ) {
            codec.setOutstream(out);
        }
        objects.clearForWrite();
        clnames.clear();
    }

    /**
     * reset keeping the last used byte[] buffer
     */
    public void resetForReUse() {
        resetForReUse((OutputStream)null);
    }

    public void resetForReUse( byte[] out ) {
        if ( closed )
            throw new RuntimeException("Can't reuse closed stream");
        codec.reset();
        codec.reset(out);
        objects.clearForWrite();
        clnames.clear();
    }

    public FSTClazzInfoRegistry getClassInfoRegistry() {
        return conf.getCLInfoRegistry();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////// java serialization compatibility ////////////////////////////////////////////

    /**
     *
     * @param cl - class or superclass of currently serialized obj, write declared fields of this class only
     * @param clinfo
     * @param referencee
     * @param toWrite
     * @return
     * @throws IOException
     */
    public ObjectOutputStream getObjectOutputStream(final Class cl, final FSTClazzInfo clinfo, final FSTClazzInfo.FSTFieldInfo referencee, final Object toWrite) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream() {
            @Override
            public void useProtocolVersion(int version) throws IOException {
            }

            @Override
            protected void writeObjectOverride(Object obj) throws IOException {
                FSTObjectOutput.this.writeObjectInternal(obj, referencee.getPossibleClasses());
            }

            @Override
            public void writeUnshared(Object obj) throws IOException {
                writeObjectOverride(obj); // fixme
            }

            @Override
            public void defaultWriteObject() throws IOException {
                FSTClazzInfo newInfo = clinfo;
                Object replObj = toWrite;
                if ( newInfo.getWriteReplaceMethod() != null ) {
                    System.out.println("WARNING: WRITE REPLACE NOT FULLY SUPPORTED");
                    try {
                        Object replaced = newInfo.getWriteReplaceMethod().invoke(replObj);
                        if ( replaced != null && replaced != toWrite ) {
                            replObj = replaced;
                            newInfo = getClassInfoRegistry().getCLInfo(replObj.getClass());
                        }
                    } catch (Exception e) {
                        throw FSTUtil.rethrow(e);
                    }
                }
                FSTObjectOutput.this.writeObjectFields(replObj, newInfo, newInfo.compInfo.get(cl).getFieldArray());
            }

            PutField pf;
            HashMap<String,Object> fields = new HashMap<String, Object>(); // fixme: init lazy
            @Override
            public PutField putFields() throws IOException {
                if ( pf == null ) {
                    pf = new PutField() {
                        @Override
                        public void put(String name, boolean val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, byte val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, char val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, short val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, int val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, long val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, float val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, double val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, Object val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void write(ObjectOutput out) throws IOException {
                            throw new IOException("cannot act compatible, use a custom serializer for this class");
                        }
                    };
                }
                return pf;
            }

            @Override
            public void writeFields() throws IOException {
                FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = clinfo.compInfo.get(cl);
                if ( fstCompatibilityInfo.isAsymmetric() ) {
                    FSTObjectOutput.this.writeCompatibleObjectFields(toWrite, fields, fstCompatibilityInfo.getFieldArray());
                } else {
                    FSTObjectOutput.this.writeObjectInternal(fields, HashMap.class);
                }
            }

            @Override
            public void reset() throws IOException {
                throw new IOException("cannot act compatible, use a custom serializer for this class");
            }

            @Override
            public void write(int val) throws IOException {
                codec.writeFByte(val);
            }

            @Override
            public void write(byte[] buf) throws IOException {
                codec.ensureFree(buf.length);
                FSTObjectOutput.this.write(buf);
            }

            @Override
            public void write(byte[] buf, int off, int len) throws IOException {
                codec.ensureFree(len);
                FSTObjectOutput.this.write(buf, off, len);
            }

            @Override
            public void flush() throws IOException {
                FSTObjectOutput.this.flush();
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public void writeBoolean(boolean val) throws IOException {
                codec.ensureFree(1);
                FSTObjectOutput.this.writeBoolean(val);
            }

            @Override
            public void writeByte(int val) throws IOException {
                codec.writeFByte(val);
            }

            @Override
            public void writeShort(int val) throws IOException {
                codec.writeFShort((short) val);
            }

            @Override
            public void writeChar(int val) throws IOException {
                codec.writeFChar((char) val);
            }

            @Override
            public void writeInt(int val) throws IOException {
                codec.writeFInt(val);
            }

            @Override
            public void writeLong(long val) throws IOException {
                codec.writeFLong(val);
            }

            @Override
            public void writeFloat(float val) throws IOException {
                codec.writeFFloat(val);
            }

            @Override
            public void writeDouble(double val) throws IOException {
                codec.writeFDouble(val);
            }

            @Override
            public void writeBytes(String str) throws IOException {
                codec.ensureFree(str.length() * 4);
                FSTObjectOutput.this.writeBytes(str);
            }

            @Override
            public void writeChars(String str) throws IOException {
                codec.ensureFree(str.length() * 4);
                FSTObjectOutput.this.writeChars(str);
            }

            @Override
            public void writeUTF(String str) throws IOException {
                codec.writeStringUTF(str);
            }
        };

        return out;
    }

    public FSTObjectRegistry getObjectMap() {
        return objects;
    }

    /**
     * @return the written buffer reference. use getWritten() to obtain the length of written bytes.
     */
    public byte[] getBuffer() {
        return codec.getBuffer();
    }

    /**
     * @return a copy of written bytes
     */
    public byte[] getCopyOfWrittenBuffer() {
        byte res [] = new byte[codec.getWritten()];
        byte[] buffer = getBuffer();
        System.arraycopy(buffer,0,res,0,codec.getWritten());
        return res;
    }

    public FSTConfiguration getConf() {
        return conf;
    }

    public int getWritten() {
        return codec.getWritten();
    }
}