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

import org.nustaq.serialization.util.FSTUtil;

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

    public static Object NULL_PLACEHOLDER = new Object() { public String toString() { return "NULL_PLACEHOLDER"; }};
    public static final byte SPECIAL_COMPATIBILITY_OBJECT_TAG = -19; // see issue 52
    public static final byte ONE_OF = -18;
    public static final byte BIG_BOOLEAN_FALSE = -17;
    public static final byte BIG_BOOLEAN_TRUE = -16;
    public static final byte BIG_LONG = -10;
    public static final byte BIG_INT = -9;
    public static final byte DIRECT_ARRAY_OBJECT = -8;
    public static final byte HANDLE = -7;
    public static final byte ENUM = -6;
    public static final byte ARRAY = -5;
    public static final byte STRING = -4;
    public static final byte TYPED = -3; // var class == object written class
    public static final byte DIRECT_OBJECT = -2;
    public static final byte NULL = -1;
    public static final byte OBJECT = 0;
    protected FSTEncoder codec;

    protected FSTConfiguration conf; // immutable, should only be set by FSTConf mechanics

    protected FSTObjectRegistry objects;
    protected int curDepth = 0;
    protected int writeExternalWriteAhead = 8000; // max size an external may occupy FIXME: document this, create annotation to configure this

    protected FSTSerialisationListener listener;

    // double state to reduce pointer chasing
    protected boolean dontShare;
    protected final FSTClazzInfo stringInfo;

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
        setCodec(conf.createStreamEncoder());
        getCodec().setOutstream(out);

        objects = (FSTObjectRegistry) conf.getCachedObject(FSTObjectRegistry.class);
        if ( objects == null ) {
            objects = new FSTObjectRegistry(conf);
            objects.disabled = !conf.isShareReferences();
        } else {
            objects.clearForWrite(conf);
        }
        dontShare = objects.disabled;
        stringInfo = getClassInfoRegistry().getCLInfo(String.class, conf);
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
        getCodec().setOutstream(null);
    }

    /**
     * serialize without an underlying stream, the resulting byte array of writing to
     * this FSTObjectOutput can be accessed using getBuffer(), the size using getWritten().
     * Note once you call close or flush, the tmp byte array is lost. (grab array before flushing/closing) 
     *
     * uses default configuration singleton
     *
     * @throws IOException
     */
    public FSTObjectOutput() {
        this(null, FSTConfiguration.getDefaultConfiguration());
        getCodec().setOutstream(null);
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
        getCodec().flush();
        resetAndClearRefs();
    }

    protected static ByteArrayOutputStream empty = new ByteArrayOutputStream(0);

    protected boolean closed = false;
    @Override
    public void close() throws IOException {
        flush();
        closed = true;
        getCodec().close();
        resetAndClearRefs();
        conf.returnObject(objects);
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
        getCodec().ensureFree(bytes);
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
        getCodec().writeFByte(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        getCodec().writePrimitiveArray(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        getCodec().writePrimitiveArray(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        getCodec().writeFByte(v ? 1 : 0);
    }

    @Override
    public void writeByte(int v) throws IOException {
        getCodec().writeFByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        getCodec().writeFShort((short) v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        getCodec().writeFChar((char) v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        getCodec().writeFInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        getCodec().writeFLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        getCodec().writeFFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        getCodec().writeFDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        byte[] bytes = s.getBytes();
        getCodec().writePrimitiveArray(bytes, 0, bytes.length);
    }

    @Override
    public void writeChars(String s) throws IOException {
        char[] chars = s.toCharArray();
        getCodec().writePrimitiveArray(chars, 0, chars.length);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        getCodec().writeStringUTF(s);
    }

    //
    // .. end interface impl
    /////////////////////////////////////////////////////
    
    public void writeObject(Object obj, Class... possibles) throws IOException {
        curDepth++;
        if ( possibles != null && possibles.length > 1 ) {
            for (int i = 0; i < possibles.length; i++) {
                Class possible = possibles[i];
                getCodec().registerClass(possible);
            }
        }
        writeObjectInternal(obj, null, possibles);
    }

    protected FSTClazzInfo.FSTFieldInfo refs[] = new FSTClazzInfo.FSTFieldInfo[20];

    //avoid creation of dummy ref
    protected FSTClazzInfo.FSTFieldInfo getCachedFI( Class... possibles ) {
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

    /**
     *
     * @param obj
     * @param ci
     * @param possibles
     * @return last FSTClazzInfo if class is plain reusable (not replaceable, needs compatible mode)
     * @throws IOException
     */
    public FSTClazzInfo writeObjectInternal(Object obj, FSTClazzInfo ci, Class... possibles) throws IOException {
        if ( curDepth == 0 ) {
            throw new RuntimeException("not intended to be called from external application. Use public writeObject instead");
        }
        FSTClazzInfo.FSTFieldInfo info = getCachedFI(possibles);
        curDepth++;
        FSTClazzInfo fstClazzInfo = writeObjectWithContext(info, obj, ci);
        curDepth--;
        if ( fstClazzInfo == null )
            return null;
        return fstClazzInfo.useCompatibleMode() ? null:fstClazzInfo;
    }

    public FSTSerialisationListener getListener() {
        return listener;
    }

    /**
     * note this might slow down serialization significantly
     * * @param listener
     */
    public void setListener(FSTSerialisationListener listener) {
        this.listener = listener;
    }

    /**
     * hook for debugging profiling. register a FSTSerialisationListener to use
     * @param obj
     * @param streamPosition
     */
    protected void objectWillBeWritten( Object obj, int streamPosition ) {
        if (listener != null) {
            listener.objectWillBeWritten(obj, streamPosition);
        }
    }

    /**
     * hook for debugging profiling. empty impl, you need to subclass to make use of this hook
     * @param obj
     * @param oldStreamPosition
     * @param streamPosition
     */
    protected void objectHasBeenWritten( Object obj, int oldStreamPosition, int streamPosition ) {
        if (listener != null) {
            listener.objectHasBeenWritten(obj, oldStreamPosition, streamPosition);
        }
    }
    protected FSTClazzInfo writeObjectWithContext(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite) throws IOException {
        return writeObjectWithContext(referencee,toWrite,null);
    }

    protected int tmp[] = {0};
    // splitting this slows down ...
    protected FSTClazzInfo writeObjectWithContext(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite, FSTClazzInfo ci) throws IOException {
        int startPosition = 0;
        try {
            if ( toWrite == null ) {
                getCodec().writeTag(NULL, null, 0, toWrite, this);
                return null;
            }
            startPosition = getCodec().getWritten();
            objectWillBeWritten(toWrite, startPosition);
            final Class clazz = toWrite.getClass();
            if ( clazz == String.class ) {
                String[] oneOf = referencee.getOneOf();
                if ( oneOf != null ) {
                    for (int i = 0; i < oneOf.length; i++) {
                        String s = oneOf[i];
                        if ( s.equals(toWrite) ) {
                            getCodec().writeTag(ONE_OF, oneOf, i, toWrite, this);
                            getCodec().writeFByte(i);
                            return null;
                        }
                    }
                }
                // shortpath
                if (! dontShare && writeHandleIfApplicable(toWrite, stringInfo))
                    return stringInfo;
                getCodec().writeTag(STRING, toWrite, 0, toWrite, this);
                getCodec().writeStringUTF((String) toWrite);
                return null;
            } else if ( clazz == Integer.class ) {
                getCodec().writeTag(BIG_INT, null, 0, toWrite, this);
                getCodec().writeFInt(((Integer) toWrite).intValue());
                return null;
            } else if ( clazz == Long.class ) {
                getCodec().writeTag(BIG_LONG, null, 0, toWrite, this);
                getCodec().writeFLong(((Long) toWrite).longValue());
                return null;
            } else if ( clazz == Boolean.class ) {
                getCodec().writeTag(((Boolean) toWrite).booleanValue() ? BIG_BOOLEAN_TRUE : BIG_BOOLEAN_FALSE, null, 0, toWrite, this); return null;
            } else if ( (referencee.getType() != null && referencee.getType().isEnum()) || toWrite instanceof Enum ) {
                return writeEnum(referencee, toWrite);
            }

            FSTClazzInfo serializationInfo = ci == null ? getFstClazzInfo(referencee, clazz) : ci;

            // check for identical / equal objects
            FSTObjectSerializer ser = serializationInfo.getSer();
            if ( ! dontShare && ! referencee.isFlat() && ! serializationInfo.isFlat() && ( ser == null || !ser.alwaysCopy() ) ) {
                if (writeHandleIfApplicable(toWrite, serializationInfo))
                    return serializationInfo;
            }
            if (clazz.isArray()) {
                if (getCodec().writeTag(ARRAY, toWrite, 0, toWrite, this))
                    return serializationInfo; // some codecs handle primitive arrays like an primitive type
                writeArray(referencee, toWrite);
                getCodec().writeArrayEnd();
            } else if ( ser == null ) {
                // default write object wihtout custom serializer
                // handle write replace
                //if ( ! dontShare ) GIT ISSUE 80
            	FSTClazzInfo originalInfo = serializationInfo;
                {	
                    if ( serializationInfo.getWriteReplaceMethod() != null ) {
                        Object replaced = null;
                        try {
                            replaced = serializationInfo.getWriteReplaceMethod().invoke(toWrite);
                        } catch (Exception e) {
                            FSTUtil.<RuntimeException>rethrow(e);
                        }
                        if ( replaced != toWrite ) {
                            toWrite = replaced;
                            serializationInfo = getClassInfoRegistry().getCLInfo(toWrite.getClass(), conf);
                            // fixme: update object map ?
                        }
                    }
                    // clazz uses some JDK special stuff (frequently slow)
                    if ( serializationInfo.useCompatibleMode() && ! serializationInfo.isExternalizable() ) {
                        writeObjectCompatible(referencee, toWrite, serializationInfo);
                        return originalInfo;
                    }
                }
                if (! writeObjectHeader(serializationInfo, referencee, toWrite) ) { // skip in case codec can write object as primitive
                    defaultWriteObject(toWrite, serializationInfo);
                    if ( serializationInfo.isExternalizable() )
                        getCodec().externalEnd(serializationInfo);
                }
                return originalInfo;
            } else { // object has custom serializer
                // Object header (nothing written till here)
                if (! writeObjectHeader(serializationInfo, referencee, toWrite) ) { // skip in case code can write object as primitive
                    int pos = getCodec().getWritten();
                    // write object depending on type (custom, externalizable, serializable/java, default)
                    ser.writeObject(this, toWrite, serializationInfo, referencee, pos);
                    getCodec().externalEnd(serializationInfo);
                }
            }
            return serializationInfo;
        } finally {
            objectHasBeenWritten(toWrite, startPosition, getCodec().getWritten());
        }
    }


    protected FSTClazzInfo writeEnum(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite) throws IOException {
        if ( ! getCodec().writeTag(ENUM, toWrite, 0, toWrite, this) ) {
            boolean isEnumClass = toWrite.getClass().isEnum();
            if (!isEnumClass) {
                // anonymous enum subclass
                Class c = toWrite.getClass();
                c = toWrite.getClass().getSuperclass();
                if (c == null) {
                    throw new RuntimeException("Can't handle this enum: " + toWrite.getClass());
                }
                getCodec().writeClass(c);
            } else {
                FSTClazzInfo fstClazzInfo = getFstClazzInfo(referencee, toWrite.getClass());
                getCodec().writeClass(fstClazzInfo);
                getCodec().writeFInt(((Enum) toWrite).ordinal());
                return fstClazzInfo;
            }
            getCodec().writeFInt(((Enum) toWrite).ordinal());
        }
        return null;
    }

    protected boolean writeHandleIfApplicable(Object toWrite, FSTClazzInfo serializationInfo) throws IOException {
        int writePos = getCodec().getWritten();
        int handle = objects.registerObjectForWrite(toWrite, writePos, serializationInfo, tmp);
        // determine class header
        if ( handle >= 0 ) {
            final boolean isIdentical = tmp[0] == 0; //objects.getReadRegisteredObject(handle) == toWrite;
            if ( isIdentical ) {
//                        System.out.println("POK writeHandle"+handle+" "+toWrite.getClass().getName());
                if ( ! getCodec().writeTag(HANDLE, null, handle, toWrite, this) )
                    getCodec().writeFInt(handle);
                return true;
            }
        }
        return false;
    }

    /**
     * if class is same as last referenced, returned cached clzinfo, else do a lookup
     */
    protected FSTClazzInfo getFstClazzInfo(FSTClazzInfo.FSTFieldInfo referencee, Class clazz) {
        FSTClazzInfo serializationInfo = null;
        FSTClazzInfo lastInfo = referencee.lastInfo;
        if ( lastInfo != null && lastInfo.getClazz() == clazz && lastInfo.conf == conf ) {
            serializationInfo = lastInfo;
        } else {
            serializationInfo = getClassInfoRegistry().getCLInfo(clazz, conf);
            referencee.lastInfo = serializationInfo;
        }
        return serializationInfo;
    }

    public void defaultWriteObject(Object toWrite, FSTClazzInfo serializationInfo) throws IOException {
        if ( serializationInfo.isExternalizable() ) {
            getCodec().ensureFree(writeExternalWriteAhead);
            ((Externalizable) toWrite).writeExternal(this);
        } else {
            FSTClazzInfo.FSTFieldInfo[] fieldInfo = serializationInfo.getFieldInfo();
            writeObjectFields(toWrite, serializationInfo, fieldInfo, 0, 0);
        }
    }

    protected void writeObjectCompatible(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite, FSTClazzInfo serializationInfo) throws IOException {
        // Object header (nothing written till here)
        writeObjectHeader(serializationInfo, referencee, toWrite);
        Class cl = serializationInfo.getClazz();
        writeObjectCompatibleRecursive(referencee,toWrite,serializationInfo,cl);
    }

    protected void writeObjectCompatibleRecursive(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite, FSTClazzInfo serializationInfo, Class cl) throws IOException {
        FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = serializationInfo.getCompInfo().get(cl);
        if ( ! Serializable.class.isAssignableFrom(cl) ) {
            return; // ok here, as compatible mode will never be triggered for "forceSerializable"
        }
        writeObjectCompatibleRecursive(referencee,toWrite,serializationInfo,cl.getSuperclass());
        if ( fstCompatibilityInfo != null && fstCompatibilityInfo.getWriteMethod() != null ) {
            try {
                writeByte(55); // tag this is written with writeMethod
                fstCompatibilityInfo.getWriteMethod().invoke(toWrite,getObjectOutputStream(cl, serializationInfo,referencee,toWrite));
            } catch (Exception e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }
        } else {
            if ( fstCompatibilityInfo != null ) {
                writeByte(66); // tag this is written from here no writeMethod
                writeObjectFields(toWrite, serializationInfo, fstCompatibilityInfo.getFieldArray(), 0, 0 );
            }
        }
    }

    protected void writeObjectFields(Object toWrite, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, int startIndex, int version) throws IOException {
        try {
            int booleanMask = 0;
            int boolcount = 0;
            final int length = fieldInfo.length;
            int j = startIndex;
            if ( ! getCodec().isWritingAttributes() ) { // pack bools into bits in case it's not a chatty codec
                for (;; j++) {
                    if ( j == length || fieldInfo[j].getVersion() != version ) {
                        if ( boolcount > 0 ) {
                            getCodec().writeFByte(booleanMask << (8 - boolcount));
                        }
                        break;
                    }
                    final FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[j];
                    if ( subInfo.getIntegralType() != FSTClazzInfo.FSTFieldInfo.BOOL) {
                        if ( boolcount > 0 ) {
                            getCodec().writeFByte(booleanMask << (8 - boolcount));
                        }
                        break;
                    } else {
                        if ( boolcount == 8 ) {
                            getCodec().writeFByte(booleanMask << (8 - boolcount));
                            boolcount = 0; booleanMask = 0;
                        }
                        boolean booleanValue = subInfo.getBooleanValue( toWrite);
                        booleanMask = booleanMask<<1;
                        booleanMask = (booleanMask|(booleanValue?1:0));
                        boolcount++;
                    }
                }
            }
            for (int i = j; i < length; i++)
            {
                final FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if (subInfo.getVersion() != version ) {
                    getCodec().writeVersionTag(subInfo.getVersion());
                    writeObjectFields(toWrite, serializationInfo, fieldInfo, i, subInfo.getVersion());
                    return;
                }
                getCodec().writeAttributeName(subInfo);
                if ( subInfo.isPrimitive() ) {
                    // speed safe
                    int integralType = subInfo.getIntegralType();
                    switch (integralType) {
                        case FSTClazzInfo.FSTFieldInfo.BOOL:
                            getCodec().writeFByte(subInfo.getBooleanValue(toWrite) ? 1 : 0); break;
                        case FSTClazzInfo.FSTFieldInfo.BYTE:
                            getCodec().writeFByte(subInfo.getByteValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.CHAR:
                            getCodec().writeFChar((char) subInfo.getCharValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.SHORT:
                            getCodec().writeFShort((short) subInfo.getShortValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.INT:
                            getCodec().writeFInt(subInfo.getIntValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.LONG:
                            getCodec().writeFLong(subInfo.getLongValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.FLOAT:
                            getCodec().writeFFloat(subInfo.getFloatValue(toWrite)); break;
                        case FSTClazzInfo.FSTFieldInfo.DOUBLE:
                            getCodec().writeFDouble(subInfo.getDoubleValue(toWrite)); break;
                    }
                } else if (subInfo.isConditional())
                {
                    final int conditional = getCodec().getWritten();
                    getCodec().skip(4);
                    // object
                    Object subObject = subInfo.getObjectValue(toWrite);
                    if ( subObject == null ) {
                        getCodec().writeTag(NULL, null, 0, toWrite, this);
                    } else {
                        writeObjectWithContext(subInfo, subObject);
                    }
                    int v = getCodec().getWritten();
                    getCodec().writeInt32At(conditional, v);
                } else {
                    // object
                    Object subObject = subInfo.getObjectValue(toWrite);
                    if ( subObject == null ) {
                        getCodec().writeTag(NULL, null, 0, toWrite, this);
                    } else {
                        writeObjectWithContext(subInfo, subObject);
                    }
                }
            }
            getCodec().writeVersionTag((byte) 0);
            getCodec().writeFieldsEnd(serializationInfo);
        } catch (IllegalAccessException ex) {
            FSTUtil.<RuntimeException>rethrow(ex);
        }

    }

    // write identical to other version, but take field values from hashmap (because of annoying putField/getField feature)
    protected void writeCompatibleObjectFields(Object toWrite, Map fields, FSTClazzInfo.FSTFieldInfo[] fieldInfo) throws IOException {
        int booleanMask = 0;
        int boolcount = 0;
        for (int i = 0; i < fieldInfo.length; i++) {
            try {
                FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                boolean isarr = subInfo.isArray();
                Class subInfType = subInfo.getType();
                if ( subInfType != boolean.class || isarr) {
                    if ( boolcount > 0 ) {
                        getCodec().writeFByte(booleanMask << (8 - boolcount));
                        boolcount = 0; booleanMask = 0;
                    }
                }
                if ( subInfo.isIntegral() && !isarr) {
                    if ( subInfType == boolean.class ) {
                        if ( boolcount == 8 ) {
                            getCodec().writeFByte(booleanMask << (8 - boolcount));
                            boolcount = 0; booleanMask = 0;
                        }
                        boolean booleanValue = ((Boolean)fields.get(subInfo.getName())).booleanValue();
                        booleanMask = booleanMask<<1;
                        booleanMask = (booleanMask|(booleanValue?1:0));
                        boolcount++;
                    } else
                    if ( subInfType == int.class ) {
                        getCodec().writeFInt(((Number) fields.get(subInfo.getName())).intValue());
                    } else
                    if ( subInfType == long.class ) {
                        getCodec().writeFLong(((Number) fields.get(subInfo.getName())).longValue());
                    } else
                    if ( subInfType == byte.class ) {
                        getCodec().writeFByte(((Number) fields.get(subInfo.getName())).byteValue());
                    } else
                    if ( subInfType == char.class ) {
                        getCodec().writeFChar((char) ((Number) fields.get(subInfo.getName())).intValue());
                    } else
                    if ( subInfType == short.class ) {
                        getCodec().writeFShort(((Number) fields.get(subInfo.getName())).shortValue());
                    } else
                    if ( subInfType == float.class ) {
                        getCodec().writeFFloat(((Number) fields.get(subInfo.getName())).floatValue());
                    } else
                    if ( subInfType == double.class ) {
                        getCodec().writeFDouble(((Number) fields.get(subInfo.getName())).doubleValue());
                    }
                } else {
                    // object
                    Object subObject = fields.get(subInfo.getName());
                    writeObjectWithContext(subInfo, subObject);
                }
            } catch (Exception ex) {
                FSTUtil.<RuntimeException>rethrow(ex);
            }
        }
        if ( boolcount > 0 ) {
            getCodec().writeFByte(booleanMask << (8 - boolcount));
        }
    }

    /**
     * 
     * @param clsInfo
     * @param referencee
     * @param toWrite
     * @return true if header already wrote object
     * @throws IOException
     */
    protected boolean writeObjectHeader(final FSTClazzInfo clsInfo, final FSTClazzInfo.FSTFieldInfo referencee, final Object toWrite) throws IOException {
        if ( toWrite.getClass() == referencee.getType()
                && ! clsInfo.useCompatibleMode() )
        {
            return getCodec().writeTag(TYPED, clsInfo, 0, toWrite, this);
        } else {
            final Class[] possibleClasses = referencee.getPossibleClasses();
            if ( possibleClasses == null ) {
                if ( !getCodec().writeTag(OBJECT, clsInfo, 0, toWrite, this) ) {
                    getCodec().writeClass(clsInfo);
                    return false;
                } else {
                    return true;
                }
            } else {
                final int length = possibleClasses.length;
                for (int j = 0; j < length; j++) {
                    final Class possibleClass = possibleClasses[j];
                    if ( possibleClass == toWrite.getClass() ) {
                        getCodec().writeFByte(j + 1);
                        return false;
                    }
                }
                if (!getCodec().writeTag(OBJECT, clsInfo, 0, toWrite, this) ) {
                    getCodec().writeClass(clsInfo);
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    // incoming array is already registered
    protected void writeArray(FSTClazzInfo.FSTFieldInfo referencee, Object array) throws IOException {
        if ( array == null ) {
            getCodec().writeClass(Object.class);
            getCodec().writeFInt(-1);
            return;
        }
        final int len = Array.getLength(array);
        Class<?> componentType = array.getClass().getComponentType();
        getCodec().writeClass(array.getClass());
        getCodec().writeFInt(len);
        if ( ! componentType.isArray() ) {
            if (getCodec().isPrimitiveArray(array, componentType)) {
                getCodec().writePrimitiveArray(array, 0, len);
            } else { // objects
                Object arr[] = (Object[])array;
                Class lastClz = null;
                FSTClazzInfo lastInfo = null;
                for ( int i = 0; i < len; i++ )
                {
                    Object toWrite = arr[i];
                    if ( toWrite != null ) {
                        lastInfo = writeObjectWithContext(referencee, toWrite, lastClz == toWrite.getClass() ? lastInfo : null);
                        lastClz = toWrite.getClass();
                    } else
                        writeObjectWithContext(referencee, toWrite, null);
                }
            }
        } else { // multidim array. FIXME shared refs to subarrays are not tested !!!
            Object[] arr = (Object[])array;
            FSTClazzInfo.FSTFieldInfo ref1 = new FSTClazzInfo.FSTFieldInfo(referencee.getPossibleClasses(), null, conf.getCLInfoRegistry().isIgnoreAnnotations());
            for ( int i = 0; i < len; i++ ) {
                Object subArr = arr[i];
                boolean needsWrite = true;
                if ( getCodec().isTagMultiDimSubArrays() ) {
                    if ( subArr == null ) {
                        needsWrite = !getCodec().writeTag(NULL, null, 0, null, this);
                    } else {
                        needsWrite = !getCodec().writeTag(ARRAY, subArr, 0, subArr, this);
                    }
                }
                if ( needsWrite ) {
                    writeArray(ref1, subArr);
                    getCodec().writeArrayEnd();
                }
            }
        }
    }

    public void writeStringUTF(String str) throws IOException {
        getCodec().writeStringUTF(str);
    }

    protected void resetAndClearRefs() {
        getCodec().reset(null);
        objects.clearForWrite(conf);
    }

    /**
     * if out == null => automatically create/reuse a bytebuffer
     *
     * @param out
     */
    public void resetForReUse( OutputStream out ) {
        if ( closed )
            throw new RuntimeException("Can't reuse closed stream");
        getCodec().reset(null);
        if ( out != null ) {
            getCodec().setOutstream(out);
        }
        objects.clearForWrite(conf);
    }

    /**
     * reset keeping the last used byte[] buffer
     */
    public void resetForReUse() {
        resetForReUse((byte[])null);
    }

    public void resetForReUse( byte[] out ) {
        if ( closed )
            throw new RuntimeException("Can't reuse closed stream");
        getCodec().reset(out);
        objects.clearForWrite(conf);
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
                getCodec().writeFByte( SPECIAL_COMPATIBILITY_OBJECT_TAG );
                FSTObjectOutput.this.writeObjectInternal(obj, null, referencee.getPossibleClasses());
            }

            @Override
            public void writeUnshared(Object obj) throws IOException {
                writeObjectOverride(obj); // fixme
            }

            @Override
            public void defaultWriteObject() throws IOException {
                writeByte(99); // tag defaultwriteObject
                FSTClazzInfo newInfo = clinfo;
                Object replObj = toWrite;
                if ( newInfo.getWriteReplaceMethod() != null ) {
                    System.out.println("WARNING: WRITE REPLACE NOT FULLY SUPPORTED");
                    try {
                        Object replaced = newInfo.getWriteReplaceMethod().invoke(replObj);
                        if ( replaced != null && replaced != toWrite ) {
                            replObj = replaced;
                            newInfo = getClassInfoRegistry().getCLInfo(replObj.getClass(), conf);
                        }
                    } catch (Exception e) {
                        FSTUtil.<RuntimeException>rethrow(e);
                    }
                }
                FSTObjectOutput.this.writeObjectFields(replObj, newInfo, newInfo.getCompInfo().get(cl).getFieldArray(),0,0);
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
                writeByte(77); // tag writeFields
//                FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = clinfo.compInfo.get(cl);
//                if ( fstCompatibilityInfo.isAsymmetric() ) {
//                    FSTObjectOutput.this.writeCompatibleObjectFields(toWrite, fields, fstCompatibilityInfo.getFieldArray());
//                } else {
                FSTObjectOutput.this.writeObjectInternal(fields, null, HashMap.class);
//                }
            }

            @Override
            public void reset() throws IOException {
                throw new IOException("cannot act compatible, use a custom serializer for this class");
            }

            @Override
            public void write(int val) throws IOException {
                getCodec().writeFByte(val);
            }

            @Override
            public void write(byte[] buf) throws IOException {
                FSTObjectOutput.this.write(buf);
            }

            @Override
            public void write(byte[] buf, int off, int len) throws IOException {
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
                FSTObjectOutput.this.writeBoolean(val);
            }

            @Override
            public void writeByte(int val) throws IOException {
                getCodec().writeFByte(val);
            }

            @Override
            public void writeShort(int val) throws IOException {
                getCodec().writeFShort((short) val);
            }

            @Override
            public void writeChar(int val) throws IOException {
                getCodec().writeFChar((char) val);
            }

            @Override
            public void writeInt(int val) throws IOException {
                getCodec().writeFInt(val);
            }

            @Override
            public void writeLong(long val) throws IOException {
                getCodec().writeFLong(val);
            }

            @Override
            public void writeFloat(float val) throws IOException {
                getCodec().writeFFloat(val);
            }

            @Override
            public void writeDouble(double val) throws IOException {
                getCodec().writeFDouble(val);
            }

            @Override
            public void writeBytes(String str) throws IOException {
                FSTObjectOutput.this.writeBytes(str);
            }

            @Override
            public void writeChars(String str) throws IOException {
                FSTObjectOutput.this.writeChars(str);
            }

            @Override
            public void writeUTF(String str) throws IOException {
                getCodec().writeStringUTF(str);
            }
        };

        return out;
    }

    public FSTObjectRegistry getObjectMap() {
        return objects;
    }

    /**
     * @return the written buffer reference. use getWritten() to obtain the length of written bytes. WARNING:
     * if more than one objects have been written, an implicit flush is triggered, so the buffer only contains
     * the last written object. getWritten() then has a larger size than the buffer length.
     * only usable if one single object is written to the stream (e.g. messaging)
     *
     * note: in case of non-standard underlyings (e.g. serializing to direct offheap or DirectBuffer, this method
     * might cause creation of a byte array and a copy.
     */
    public byte[] getBuffer() {
        return getCodec().getBuffer();
    }

    /**
     * @return a copy of written bytes. 
     * Warning: if the stream has been flushed, this will fail with an exception.
     * a flush is triggered after each 1st level writeObject.
     *
     * note: in case of non-stream based serialization (directbuffer, offheap mem) getBuffer will return a copy anyways.
     */
    public byte[] getCopyOfWrittenBuffer() {
        if ( ! getCodec().isByteArrayBased() ) {
            return getBuffer();
        }
        byte res [] = new byte[getCodec().getWritten()];
        byte[] buffer = getBuffer();
        System.arraycopy(buffer,0,res,0, getCodec().getWritten());
        return res;
    }

    public FSTConfiguration getConf() {
        return conf;
    }

    /**
     * @return the number of bytes written to this stream. This also is the number of
     * valid bytes in the buffer one obtains from the various getBuffer, getCopyOfBuffer methods.
     * Warning: if the stream has been flushed (done after each 1st level object write),
     * the buffer will be smaller than the value given here or contain invalid bytes.
     */
    public int getWritten() {
        return getCodec().getWritten();
    }

    public void writeClassTag(Class aClass) {
        getCodec().writeClass(aClass);
    }

    public FSTEncoder getCodec() {
        return codec;
    }

    protected void setCodec(FSTEncoder codec) {
        this.codec = codec;
    }
}