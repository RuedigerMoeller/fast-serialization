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

import de.ruedigermoeller.serialization.util.FSTOutputStream;
import de.ruedigermoeller.serialization.util.FSTUtil;
import sun.misc.*;

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
public class FSTObjectOutput extends DataOutputStream implements ObjectOutput {


    private static final boolean UNSAFE_MEMCOPY_ARRAY_INT = true;
    private static final boolean UNSAFE_MEMCOPY_ARRAY_LONG = true;
    private static final boolean UNSAFE_WRITE_CINT_ARR = true;
    private static final boolean UNSAFE_WRITE_CINT = true;
    private static final boolean UNSAFE_WRITE_FINT = true;
    private static final boolean UNSAFE_WRITE_FLONG = true;
    private static final boolean UNSAFE_WRITE_UTF = true;

    final static long bufoff;
    final static long choff;
    final static long intoff;
    final static long longoff;
    final static long intscal;
    final static long longscal;
    final static long chscal;

    static {
        Unsafe unsafe = FSTUtil.getUnsafe();
        if ( unsafe != null ) {
            bufoff = unsafe.arrayBaseOffset(byte[].class);
            intoff = unsafe.arrayBaseOffset(int[].class);
            longoff = unsafe.arrayBaseOffset(long[].class);
            longscal = unsafe.arrayIndexScale(long[].class);
            intscal = unsafe.arrayIndexScale(int[].class);
            chscal = unsafe.arrayIndexScale(char[].class);
            choff = unsafe.arrayBaseOffset(char[].class);
        } else {
            longoff = 0;
            longscal = 0;
            bufoff = 0;
            intoff = 0;
            intscal = 0;
            choff = 0;
            chscal = 0;
        }
    }


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

    public FSTClazzNameRegistry clnames; // immutable
    protected FSTConfiguration conf; // immutable

    protected FSTObjectRegistry objects;
    protected FSTOutputStream buffout;
    protected FSTSerialisationListener listener;

    protected int curDepth = 0;

    protected int writeExternalWriteAhead = 8000; // max size an external may occupy FIXME: document this, create annotation to configure this
    protected Unsafe unsafe;

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
        super(null);
        unsafe = FSTUtil.unsafe;
        this.conf = conf;

        buffout = (FSTOutputStream) conf.getCachedObject(FSTOutputStream.class);
        if ( buffout == null ) {
            buffout = new FSTOutputStream(1000,out);
        } else {
            buffout.reset();
            buffout.setOutstream(out);
        }
        this.out = buffout;

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
     * Don't create a FSTConfiguration with each stream, just create one global static configuration and reuseit.
     * FSTConfiguration is threadsafe.
     * @param conf
     * @throws IOException
     */
    public FSTObjectOutput(FSTConfiguration conf) {
        this(null,conf);
        buffout.setOutstream(buffout);
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
        buffout.setOutstream(buffout);
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
        buffout.flush();
    }

    static ByteArrayOutputStream empty = new ByteArrayOutputStream(0);

    boolean closed = false;
    @Override
    public void close() throws IOException {
        flush();
        closed = true;
        super.close();
        resetAndClearRefs();
        conf.returnObject(buffout,objects,clnames);
    }


//    private void reUse(OutputStream out) {
//        buffout.reset();
//        buffout.setOutstream(out);
//        objects.clear();
//        clnames.clear();
//        written = 0;
//    }

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
        buffout.ensureFree(bytes);
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        writeObject(obj,(Class[])null);
    }

    public void writeObject(Object obj, Class... possibles) throws IOException {
        curDepth++;
        try {
            if ( possibles != null ) {
                for (int i = 0; i < possibles.length; i++) {
                    Class possible = possibles[i];
                    clnames.registerClass(possible);
                }
            }
            writeObjectInternal(obj, possibles);
        } finally {
            buffout.flush();
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

    public FSTSerialisationListener getListener() {
        return listener;
    }

    /**
     * note this might slowdown serialization significatly
     * @param listener
     */
    public void setListener(FSTSerialisationListener listener) {
        this.listener = listener;
    }

    int tmp[] = {0};
    // splitting this slows down ...
    protected void writeObjectWithContext(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite) throws IOException {
        int startPosition = 0;
        boolean dontShare = objects.disabled;
        if (listener != null) {
            startPosition = getWritten();
            listener.objectWillBeWritten(toWrite,startPosition);
        }

        try {
            if ( toWrite == null ) {
                writeFByte(NULL);
                return;
            }
            final Class clazz = toWrite.getClass();
            if ( clazz == String.class ) {
                String[] oneOf = referencee.getOneOf();
                if ( oneOf != null ) {
                    for (int i = 0; i < oneOf.length; i++) {
                        String s = oneOf[i];
                        if ( s.equals(toWrite) ) {
                            writeFByte(ONE_OF);
                            writeFByte(i);
                            return;
                        }
                    }
                }
                if (dontShare) {
                    writeFByte(STRING);
                    writeStringUTFDef((String) toWrite);
                    return;
                }
            } else if ( clazz == Integer.class ) { writeFByte(BIG_INT); writeCInt(((Integer) toWrite).intValue()); return;
            } else if ( clazz == Long.class ) { writeFByte(BIG_LONG); writeCLong(((Long) toWrite).longValue()); return;
            } else if ( clazz == Boolean.class ) { writeFByte(((Boolean) toWrite).booleanValue() ? BIG_BOOLEAN_TRUE : BIG_BOOLEAN_FALSE); return;
            } else if (clazz.isArray()) {
                writeFByte(ARRAY);
                writeArray(referencee, toWrite);
                return;
            } else if ( toWrite instanceof Enum ) {
                writeFByte(ENUM);
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
                    writeClass(toWrite);
                }
                writeCInt(((Enum) toWrite).ordinal());
                return;
            }

            FSTClazzInfo serializationInfo = getFstClazzInfo(referencee, clazz);
            // check for identical / equal objects
            FSTObjectSerializer ser = serializationInfo.getSer();
            if ( ! dontShare && ! referencee.isFlat() && ! serializationInfo.isFlat() && ( ser == null || !ser.alwaysCopy() ) ) {
                boolean needsEqualMap = serializationInfo.isEqualIsBinary() || serializationInfo.isEqualIsIdentity();
                int handle = objects.registerObjectForWrite(toWrite, !needsEqualMap, written, serializationInfo, tmp);
                // determine class header
                if ( handle >= 0 ) {
                    final boolean isIdentical = tmp[0] == 0; //objects.getReadRegisteredObject(handle) == toWrite;
                    if ( isIdentical || serializationInfo.isEqualIsIdentity()) {
                        writeFByte(HANDLE);
                        writeCInt(handle);
                        return;
                    } else if ( serializationInfo.isEqualIsBinary() ) {
                        writeFByte(COPYHANDLE);
                        writeCInt(handle);
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
                int pos = written;
                writeObjectHeader(serializationInfo, referencee, toWrite);
                // write object depending on type (custom, externalizable, serializable/java, default)
                ser.writeObject(this, toWrite, serializationInfo, referencee, pos);
            }
        } finally {
            if ( listener != null )
                listener.objectHasBeenWritten(toWrite,startPosition,getWritten());
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
            buffout.ensureFree(writeExternalWriteAhead);
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
        if ( unsafe != null ) {
            writeObjectFieldsUnsafe(toWrite, serializationInfo, fieldInfo, !conf.preferSpeed);
        } else {
            writeObjectFieldsSafe(toWrite, serializationInfo, fieldInfo, !conf.preferSpeed);
        }
    }

    private void writeObjectFieldsSafe(Object toWrite, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, final boolean compact) throws IOException {
        try {
            int booleanMask = 0;
            int boolcount = 0;
            final int length = fieldInfo.length;
            int j = 0;
            for (;; j++) {
                if ( j == length ) {
                    if ( boolcount > 0 ) {
                        writeFByte(booleanMask<<(8-boolcount));
                    }
                    break;
                }
                final FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[j];
                if ( subInfo.getType() != boolean.class ) {
                    if ( boolcount > 0 ) {
                        writeFByte(booleanMask<<(8-boolcount));
                    }
                    break;
                } else {
                    if ( boolcount == 8 ) {
                        writeFByte(booleanMask<<(8-boolcount));
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
                    if ( compact ) {
                        switch (integralType) {
                            case FSTClazzInfo.FSTFieldInfo.BYTE:   writeFByte(subInfo.getByteValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.CHAR:   writeCChar((char) subInfo.getCharValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.SHORT:  writeCShort((short) subInfo.getShortValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.INT:    writeCInt(subInfo.getIntValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.LONG:   writeCLong(subInfo.getLongValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.FLOAT:  writeCFloat(subInfo.getFloatValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.DOUBLE: writeCDouble(subInfo.getDoubleValue(toWrite)); break;
                        }
                    } else {
                        switch (integralType) {
                            case FSTClazzInfo.FSTFieldInfo.BYTE:   writeFByte(subInfo.getByteValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.CHAR:   writeFChar((char) subInfo.getCharValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.SHORT:  writeFShort((short) subInfo.getShortValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.INT:    writeFInt(subInfo.getIntValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.LONG:   writeFLong(subInfo.getLongValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.FLOAT:  writeFFloat(subInfo.getFloatValue(toWrite)); break;
                            case FSTClazzInfo.FSTFieldInfo.DOUBLE: writeFDouble(subInfo.getDoubleValue(toWrite)); break;
                        }
                    }
                } else if (subInfo.isConditional())
                {
                    final int conditional = buffout.pos;
                    buffout.pos +=4;
                    written+=4;
                    // object
                    Object subObject = subInfo.getObjectValue(toWrite);
                    if ( subObject == null ) {
                        writeFByte(NULL);
                    } else {
                        writeObjectWithContext(subInfo, subObject);
                    }
                    int v = buffout.pos;
                    buffout.buf[conditional] = (byte) ((v >>> 24) & 0xFF);
                    buffout.buf[conditional+1] = (byte) ((v >>> 16) & 0xFF);
                    buffout.buf[conditional+2] = (byte) ((v >>>  8) & 0xFF);
                    buffout.buf[conditional+3] = (byte) ((v >>> 0) & 0xFF);
                } else {
                    // object
                    Object subObject = subInfo.getObjectValue(toWrite);
                    if ( subObject == null ) {
                        writeFByte(NULL);
                    } else {
                        writeObjectWithContext(subInfo, subObject);
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            throw FSTUtil.rethrow(ex);
        }
    }

    private void writeObjectFieldsUnsafe(Object toWrite, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, final boolean compact) throws IOException {
        try {
            int booleanMask = 0;
            int boolcount = 0;
            final int length = fieldInfo.length;
            int j = 0;
            for (; j < length; j++) {
                final FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[j];
                if ( subInfo.getType() != boolean.class ) {
                    if ( boolcount > 0 ) {
                        writeFByteUnsafe(booleanMask<<(8-boolcount));
                    }
                    break;
                } else {
                    if ( boolcount == 8 ) {
                        writeFByteUnsafe(booleanMask<<(8-boolcount));
                        boolcount = 0; booleanMask = 0;
                    }
                    boolean booleanValue = unsafe.getBoolean(toWrite,subInfo.memOffset);
                    booleanMask = booleanMask<<1;
                    booleanMask = (booleanMask|(booleanValue?1:0));
                    boolcount++;
                }
            }
            for (int i = j; i < length; i++)
            {
                final FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if ( subInfo.isPrimitive() ) {
                    // speed unsafe
                    int integralType = subInfo.getIntegralType();
                    if (integralType == FSTClazzInfo.FSTFieldInfo.INT ) {
                        // inline
                        if ( compact ) {
                            writeCIntUnsafe(unsafe.getInt(toWrite,subInfo.memOffset));
                        } else {
                            int val = unsafe.getInt(toWrite,subInfo.memOffset);
                            buffout.ensureFree(4);
                            unsafe.putInt(buffout.buf,buffout.pos+bufoff,val);
                            buffout.pos += 4;
                            written += 4;
                        }
                    } else if ( integralType == FSTClazzInfo.FSTFieldInfo.LONG ) {
                        // inline
                        if ( compact ) {
                            writeCLong(unsafe.getLong(toWrite,subInfo.memOffset));
                        } else {
                            long lval = unsafe.getLong(toWrite,subInfo.memOffset);
                            buffout.ensureFree(8);
                            unsafe.putLong(buffout.buf,buffout.pos+bufoff,lval);
                            buffout.pos += 8;
                            written += 8;
                        }
                    } else {
                        if ( compact ) {
                            switch (integralType) {
                                case FSTClazzInfo.FSTFieldInfo.BYTE:  writeFByteUnsafe(unsafe.getByte(toWrite,subInfo.memOffset)); break;
                                case FSTClazzInfo.FSTFieldInfo.CHAR:  writeCChar(unsafe.getChar(toWrite,subInfo.memOffset)); break;
                                case FSTClazzInfo.FSTFieldInfo.SHORT: writeCShort(unsafe.getShort(toWrite,subInfo.memOffset)); break;
                                case FSTClazzInfo.FSTFieldInfo.FLOAT: writeCFloatUnsafe(unsafe.getFloat(toWrite,subInfo.memOffset)); break;
                                case FSTClazzInfo.FSTFieldInfo.DOUBLE: writeCDoubleUnsafe(unsafe.getDouble(toWrite,subInfo.memOffset)); break;
                            }
                        } else {
                            switch (integralType) {
                                case FSTClazzInfo.FSTFieldInfo.BYTE:  writeFByteUnsafe(unsafe.getByte(toWrite,subInfo.memOffset)); break;
                                case FSTClazzInfo.FSTFieldInfo.CHAR:  writeFChar(unsafe.getChar(toWrite,subInfo.memOffset)); break;
                                case FSTClazzInfo.FSTFieldInfo.SHORT: writeFShort(unsafe.getShort(toWrite,subInfo.memOffset)); break;
                                case FSTClazzInfo.FSTFieldInfo.FLOAT: writeFFloat(unsafe.getFloat(toWrite,subInfo.memOffset)); break;
                                case FSTClazzInfo.FSTFieldInfo.DOUBLE: writeFDoubleUnsafe(unsafe.getDouble(toWrite,subInfo.memOffset)); break;
                            }
                        }
                    }
                } else if (subInfo.isConditional())
                {
                    final int conditional = buffout.pos;
                    buffout.pos +=4;
                    written+=4;
                    // object
                    Object subObject = subInfo.getObjectValueUnsafe(toWrite);
                    if ( subObject == null ) {
                        writeFByteUnsafe(NULL);
                    } else {
                        writeObjectWithContext(subInfo, subObject);
                    }
                    int v = buffout.pos;
                    buffout.buf[conditional] = (byte) ((v >>> 24) & 0xFF);
                    buffout.buf[conditional+1] = (byte) ((v >>> 16) & 0xFF);
                    buffout.buf[conditional+2] = (byte) ((v >>>  8) & 0xFF);
                    buffout.buf[conditional+3] = (byte) ((v >>> 0) & 0xFF);
                } else {
                    // object
                    Object subObject = subInfo.getObjectValueUnsafe(toWrite);
                    if ( subObject == null ) {
                        writeFByteUnsafe(NULL);
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
                        writeFByte(booleanMask<<(8-boolcount));
                        boolcount = 0; booleanMask = 0;
                    }
                }
                if ( subInfo.isIntegral() && !isarr) {
                    if ( subInfType == boolean.class ) {
                        if ( boolcount == 8 ) {
                            writeFByte(booleanMask<<(8-boolcount));
                            boolcount = 0; booleanMask = 0;
                        }
                        boolean booleanValue = ((Boolean)fields.get(subInfo.getField().getName())).booleanValue();
                        booleanMask = booleanMask<<1;
                        booleanMask = (booleanMask|(booleanValue?1:0));
                        boolcount++;
                    } else
                    if ( subInfType == int.class ) {
                        writeCInt(((Number) fields.get(subInfo.getField().getName())).intValue());
                    } else
                    if ( subInfType == long.class ) {
                        writeCLong(((Number) fields.get(subInfo.getField().getName())).longValue());
                    } else
                    if ( subInfType == byte.class ) {
                        writeFByte(((Number) fields.get(subInfo.getField().getName())).byteValue());
                    } else
                    if ( subInfType == char.class ) {
                        writeCChar((char) ((Number) fields.get(subInfo.getField().getName())).intValue());
                    } else
                    if ( subInfType == short.class ) {
                        writeCShort(((Number) fields.get(subInfo.getField().getName())).shortValue());
                    } else
                    if ( subInfType == float.class ) {
                        writeCFloat(((Number)fields.get(subInfo.getField().getName())).floatValue());
                    } else
                    if ( subInfType == double.class ) {
                        writeCDouble(((Number)fields.get(subInfo.getField().getName())).doubleValue());
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
            writeFByte(booleanMask<<(8-boolcount));
            boolcount = 0; booleanMask = 0;
        }
    }

    protected void writeObjectHeader(final FSTClazzInfo clsInfo, final FSTClazzInfo.FSTFieldInfo referencee, final Object toWrite) throws IOException {
        if (clsInfo.isEqualIsBinary() ) {
            writeFByte(OBJECT);
            writeClass(clsInfo);
            return;
        }
        //fixme:move to Clazzinfo
//        if ( toWrite instanceof Serializable == false && ! conf.isIgnoreSerialInterfaces() ) {
//            throw new RuntimeException(toWrite.getClass().getName()+" is not serializable. referenced by "+referencee.getDesc());
//        }
        if ( toWrite.getClass() == referencee.getType()
            && ! clsInfo.useCompatibleMode() )
        {
            writeFByte(TYPED);
        } else {
            final Class[] possibleClasses = referencee.getPossibleClasses();
            if ( possibleClasses == null ) {
                writeFByte(OBJECT);
                writeClass(clsInfo);
            } else {
                final int length = possibleClasses.length;
                for (int j = 0; j < length; j++) {
                    final Class possibleClass = possibleClasses[j];
                    if ( possibleClass == toWrite.getClass() ) {
                        writeFByte(j+1);
                        return;
                    }
                }
                writeFByte(OBJECT);
                writeClass(clsInfo);
            }
        }
    }

    protected void writeArray(FSTClazzInfo.FSTFieldInfo referencee, Object array) throws IOException {
        if ( array == null ) {
            writeClass(Object.class);
            writeCInt(-1);
            return;
        }

        final int len = Array.getLength(array);
        writeClass(array);
        writeCInt(len);
        Class<?> componentType = array.getClass().getComponentType();
        if ( ! componentType.isArray() ) {
            if ( componentType == byte.class ) {
                writeFByteArr((byte[]) array);
            } else
            if ( componentType == char.class ) {
                writeCCharArr((char[]) array);
            } else
            if ( componentType == short.class ) {
                writeFShortArr((short[]) array);
            } else
            if ( componentType == int.class ) {
                if ( referencee.isThin() ) {
                    writeFIntThin((int[]) array);
                } else
                if ( referencee.isCompressed() ) {
                    writeIntArrCompressed((int[]) array);
                } else
                {
                    writeFIntArr((int[]) array);
                }
            } else
            if ( componentType == double.class ) {
                writeFDoubleArr((double[]) array);
            } else
            if ( componentType == float.class ) {
                writeFFloatArr((float[]) array);
            } else
            if ( componentType == long.class ) {
                writeFLongArr((long[]) array);
            } else
            if ( componentType == boolean.class ) {
                writeFBooleanArr((boolean[]) array);
            } else {
                Object arr[] = (Object[])array;
                if ( referencee.isThin() ) {
                    for ( int i = 0; i < len; i++ )
                    {
                        Object toWrite = arr[i];
                        if ( toWrite != null ) {
                            writeCInt(i);
                            writeObjectWithContext(referencee, toWrite);
                        }
                    }
                    writeCInt(len);
                } else {
                    for ( int i = 0; i < len; i++ )
                    {
                        Object toWrite = arr[i];
                        if ( toWrite == null ) {
                            writeFByte(NULL);
                        } else {
                            writeObjectWithContext(referencee, toWrite);
                        }
                    }
                }
//                Class[] possibleClasses = null;
//                if ( referencee.getPossibleClasses() == null ) {
//                    possibleClasses = new Class[5];
//                } else {
//                    possibleClasses = Arrays.copyOf(referencee.getPossibleClasses(),referencee.getPossibleClasses().length+5);
//                }
//                FSTClazzInfo.FSTFieldInfo newFI = new FSTClazzInfo.FSTFieldInfo(false, possibleClasses, null);
//                for ( int i = 0; i < len; i++ )
//                {
//                    Object toWrite = Array.get(array, i);
//                    writeObjectWithContext(newFI, toWrite);
//                    if ( toWrite != null ) {
//                        newFI.setPossibleClasses(addToPredictionArray(newFI.getPossibleClasses(), toWrite.getClass()));
//                    }
//                }
            }
        } else {
            Object[] arr = (Object[])array;
            FSTClazzInfo.FSTFieldInfo ref1 = new FSTClazzInfo.FSTFieldInfo(referencee.getPossibleClasses(), null, conf.getCLInfoRegistry().isIgnoreAnnotations());
            for ( int i = 0; i < len; i++ ) {
                Object subArr = arr[i];
                if ( subArr != null ) {
                    if ( ! FSTUtil.isPrimitiveArray(subArr.getClass()) ) {
                        objects.registerObjectForWrite(subArr, true, written, null, tmp); // fixme: shared refs
                    }
                }
                writeArray(ref1, subArr);
            }
        }
    }

    public void writeFBooleanArr(boolean[] array) throws IOException {
        boolean[] arr = (boolean[])array;
        for ( int i = 0; i < arr.length; i++ )
            writeBoolean(arr[i]);
    }

    public void writeFLongArr(long[] array) throws IOException {
        long[] arr = (long[])array;
        if ( unsafe != null && UNSAFE_MEMCOPY_ARRAY_LONG) {
            writeFLongArrayUnsafe(arr);
        } else {
        for ( int i = 0; i < arr.length; i++ )
            writeFLong(arr[i]);
        }
    }

    public void writeFFloatArr(float[] array) throws IOException {
        float[] arr = (float[])array;
        for ( int i = 0; i < array.length; i++ )
            writeFFloat(arr[i]);
    }

    public void writeFDoubleArr(double[] array) throws IOException {
        double[] arr = array;
        for ( int i = 0; i < arr.length; i++ )
            writeFDouble(arr[i]);
    }

    public void writeFShortArr(short[] array) throws IOException {
        short[] arr = array;
        for ( int i = 0; i < array.length; i++ )
            writeFShort(arr[i]);
    }

    public void writeFCharArr(char[] array) throws IOException {
        writeCCharArr(array);
    }

    public void writeCCharArr(char[] array) throws IOException {
        char[] arr = (char[])array;
        for ( int i = 0; i < arr.length; i++ )
            writeCChar(arr[i]);
    }

    public void writeFByteArr(byte[] array) throws IOException {
        byte[] arr = (byte[])array;
        write(arr);
    }

    public void writeFLongArrayUnsafe(long[] arr) throws IOException {
        int length = arr.length;
        buffout.ensureFree((int) (longscal * length));
        final byte buf[] = buffout.buf;
        long siz = length * longscal;
        unsafe.copyMemory(arr, longoff, buf, buffout.pos + bufoff, siz);
        buffout.pos += siz;
        written += siz;
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

    public int writeStringCompressed(String str) throws IOException {
        final int strlen = str.length();

        writeCInt(strlen);
        buffout.ensureFree(strlen*3);
        int fourBitCnt = 0;

        final byte[] bytearr = buffout.buf;
        int count = buffout.pos;
        int initpos = count;
        int compressBuffIdx = count;

        for (int i=0; i<strlen; i++) {
            final int c = str.charAt(i);
            if ( c < 254 ) {
                int mapped = charMap[c];
                if ( mapped < 16 )
                {
                    fourBitCnt++;
                } else {
                    if ( fourBitCnt > 5 ) {
                        count = reEncodeStr(str, fourBitCnt, bytearr, compressBuffIdx, i);
                    }
                    fourBitCnt = 0;
                    compressBuffIdx = count+1;
                }
            } else {
                if ( fourBitCnt > 5 ) {
                    count = reEncodeStr(str, fourBitCnt, bytearr, compressBuffIdx, i);
                }
                fourBitCnt = 0;
                compressBuffIdx = count+1;
            }
            if ( c < 254 ) {
                bytearr[count++] = (byte)c;
            } else {
                bytearr[count++] = (byte) 255;
                bytearr[count++] = (byte) ((c >>> 8) & 0xFF);
                bytearr[count++] = (byte) ((c >>> 0) & 0xFF);
            }
        }
        if ( fourBitCnt > 5 ) {
            count = reEncodeStr(str, fourBitCnt, bytearr, compressBuffIdx, str.length());
        }
        int required = count - initpos;
        written += required;
        buffout.pos = count;
        return required;
    }

    private int reEncodeStr(String str, int minMaxCount, byte[] bytearr, int compressBuffIdx, int i) {
        bytearr[compressBuffIdx++] = (byte)254;
        bytearr[compressBuffIdx++] = (byte)minMaxCount;
        int bc = 0;
        for ( int ii=minMaxCount; ii>0; ii-- ) {
            int cc = charMap[str.charAt(i-ii)];
            if ( (bc&1) == 0 ) {
                bytearr[compressBuffIdx] = (byte)cc;
                if ( bc == minMaxCount-1 ) {
                    compressBuffIdx++;
                }
            } else {
                bytearr[compressBuffIdx++] |= cc<<4;
            }
            bc++;
        }
        return compressBuffIdx;
    }

    public void writeStringUTF(String str) throws IOException {
        if ( conf.isPreferSpeed() ) {
            writeStringUTFSpeed(str);
            return;
        }
        if ( unsafe != null && UNSAFE_WRITE_UTF) {
            writeStringUTFUnsafe(str);
            return;
        }

        writeStringUTFDef(str);
    }

    // save condition testing
    protected void writeStringUTFDef(String str) throws IOException {
        final int strlen = str.length();

        writeCInt(strlen);
        buffout.ensureFree(strlen*3);
        final byte[] bytearr = buffout.buf;
        int count = buffout.pos;

        for (int i=0; i<strlen; i++) {
            final char c = str.charAt(i);
            bytearr[count++] = (byte)c;
            if ( c >= 255) {
                bytearr[count-1] = (byte)255;
                bytearr[count++] = (byte) ((c >>> 8) & 0xFF);
                bytearr[count++] = (byte) ((c >>> 0) & 0xFF);
            }
        }
        written += count-buffout.pos;
        buffout.pos = count;
    }

    char charBuf[];
    public void writeStringUTFSpeed(String str) throws IOException {
        final int strlen = str.length();
        if ( unsafe != null && UNSAFE_WRITE_UTF) {
            writeFIntUnsafe(strlen);
            int added = (int) (chscal * strlen);
            buffout.ensureFree(added);
            if (charBuf == null || charBuf.length < strlen) {
                charBuf = new char[strlen];
            }
            str.getChars(0,strlen,charBuf,0);
            unsafe.copyMemory(charBuf,choff,buffout.buf,buffout.pos+bufoff,strlen*chscal);
            written += added;
            buffout.pos += added;
//            int count = buffout.pos+bufoff;
//            for (int i=0; i<strlen; i++) {
//                final char c = str.charAt(i);
//                unsafe.putChar(buffout.buf,count,c);
//                written += chscal;
//                buffout.pos += chscal;
//                count+=chscal;
//            }
            return;
        } else {
            writeFInt(strlen);
            buffout.ensureFree(strlen*2);

            final byte[] bytearr = buffout.buf;
            int count = buffout.pos;
            for (int i=0; i<strlen; i++) {
                final int c = str.charAt(i);
                bytearr[count++] = (byte) ((c >>> 0) & 0xFF);
                bytearr[count++] = (byte) ((c >>> 8) & 0xFF);
                written += 2;
            }
            buffout.pos = count;
        }
    }

    public void writeStringUTFUnsafe(String str) throws IOException {
        final byte buf[] = buffout.buf;
        final int strlen = str.length();

        if ( UNSAFE_WRITE_CINT )
            writeCIntUnsafe(strlen);
        else
            writeCInt(strlen);
        buffout.ensureFree(strlen*3);

        final byte[] bytearr = buffout.buf;
        long count = buffout.pos+bufoff;

        for (int i=0; i<strlen; i++) {
            final int c = str.charAt(i);
            if ( c < 255 ) {
                unsafe.putByte(bytearr,count++,(byte)c);
                written++;
            } else {
                unsafe.putByte(bytearr,count++, (byte) 255);
                unsafe.putByte(bytearr,count++, (byte) ((c >>> 8) & 0xFF));
                unsafe.putByte(bytearr,count++, (byte) ((c >>> 0) & 0xFF));
                written += 3;
            }
        }
        buffout.pos = (int) (count-bufoff);
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

    public static Class[] addToPredictionArray(Class[] possibleClasses, Class aClass) {
        if ( possibleClasses == null ) {
            return new Class[] {aClass};
        }
        for (int i = 0; i < possibleClasses.length; i++) {
            Class possibleClass = possibleClasses[i];
            if ( aClass == possibleClass ) {
                return possibleClasses;
            }
            if ( possibleClass == null ) {
                possibleClasses[i] = aClass;
                return possibleClasses;
            }
        }
        Class[] newPoss = Arrays.copyOf(possibleClasses,possibleClasses.length+5);
        newPoss[possibleClasses.length] = aClass;
        return newPoss;
    }

    public void writeCShort(short c) throws IOException {
        if ( c < 255 && c >= 0 ) {
            writeFByte(c);
        } else {
            writeFByte(255);
            writeFShort(c);
        }
    }

    public void writeCChar(char c) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if ( c < 255 && c >= 0 ) {
            buffout.ensureFree(1);
            buffout.buf[buffout.pos++] = (byte)c;
            written++;
        } else {
            buffout.ensureFree(3);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) 255;
            buf[count++] = (byte) ((c >>>  8) & 0xFF);
            buf[count++] = (byte) ((c >>> 0) & 0xFF);
            buffout.pos += 3;
            written += 3;
        }
    }

    public void writeFChar( int v ) throws IOException {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>>  0) & 0xFF);
        buf[count++] = (byte) ((v >>> 8) & 0xFF);
        buffout.pos += 2;
        written += 2;
    }

    public void writeFShort( int v ) throws IOException {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>>  8) & 0xFF);
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buffout.pos += 2;
        written += 2;
    }

    public final void writeFByte( int v ) throws IOException {
        buffout.ensureFree(1);
        buffout.buf[buffout.pos++] = (byte)v;
        written++;
    }

    public final void writeFByteUnsafe( int v ) throws IOException {
        buffout.ensureFree(1);
        final byte buf[] = buffout.buf;
        unsafe.putByte(buf, buffout.pos + bufoff, (byte) v);
        buffout.pos++;
        written++;
    }

    public final void writeFIntUnsafe( int v ) throws IOException {
        buffout.ensureFree(4);
        final byte buf[] = buffout.buf;
        unsafe.putInt(buf,buffout.pos+bufoff,v);
        buffout.pos += 4;
        written += 4;
    }

    public void writeFInt( int v ) throws IOException {
        if ( unsafe != null && UNSAFE_WRITE_FINT) {
            writeFIntUnsafe(v);
            return;
        }
        buffout.ensureFree(4);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buf[count++] = (byte) ((v >>>  8) & 0xFF);
        buf[count++] = (byte) ((v >>> 16) & 0xFF);
        buf[count++] = (byte) ((v >>> 24) & 0xFF);
        buffout.pos += 4;
        written += 4;
    }

    public void writeFLongUnsafe( long v ) throws IOException {
        buffout.ensureFree(8);
        final byte buf[] = buffout.buf;
        unsafe.putLong(buf,buffout.pos+bufoff,v);
        buffout.pos += 8;
        written += 8;
    }

    public void writeFLong( long v ) throws IOException {
        if ( unsafe != null && UNSAFE_WRITE_FLONG) {
            writeFLongUnsafe(v);
            return;
        }
        buffout.ensureFree(8);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buf[count++] = (byte) ((v >>>  8) & 0xFF);
        buf[count++] = (byte) ((v >>> 16) & 0xFF);
        buf[count++] = (byte) ((v >>> 24) & 0xFF);
        buf[count++] = (byte)(v >>> 32);
        buf[count++] = (byte)(v >>> 40);
        buf[count++] = (byte)(v >>> 48);
        buf[count++] = (byte)(v >>> 56);
        buffout.pos += 8;
        written += 8;
    }

    public void writeFIntThin( int v[] ) throws IOException {
        final int length = v.length;
        for (int i = 0; i < length; i++) {
            final int anInt = v[i];
            if ( anInt != 0 ) {
                writeCInt(i);
                writeCInt(anInt);
            }
        }
        writeCInt(length); // stop marker
    }

    public void writeIntArrCompressed( int v[] ) throws IOException {
        final int length = v.length;
        int min = Integer.MAX_VALUE; int max = Integer.MIN_VALUE;
        int sizeNorm = 0, sizDiff = 0, sizOffs = 0, sizeThin = 0;
        for (int i = 0; i < length; i++) {
            final int anInt = v[i];
            if ( anInt != 0 ) {
                sizeThin++;
                if ( i > 128 ) {
                    sizeThin+=2;
                }
            }
            if ( anInt < 127 ) {
                sizeNorm++;
                if (anInt!=0) {
                    sizeThin++;
                }
            } else if ( anInt < 32767 ) {
                sizeNorm+=3;
                sizeThin+=3;
            } else {
                sizeNorm+=5;
                sizeThin+=3;
            }
            min = Math.min(anInt,min);
            max = Math.max(anInt,max);
            if ( i > 0 ) {
                int diff = Math.abs(anInt-v[i-1]);
                if ( diff < 127 ) {
                    sizDiff++;
                } else if ( diff < 32767 ) {
                    sizDiff+=3;
                } else {
                    sizDiff+=5;
                }
            }
        }
        int range = Math.abs(max-min);
        if ( range < 127 ) {
            sizOffs = v.length;
        } else if ( range < 32767 ) {
            sizOffs = v.length*2;
        } else {
            sizOffs = v.length*5;
        }
        if ( sizDiff <= sizeNorm && sizDiff <= sizeThin && sizDiff <= sizOffs ) {
            writeFByte(0);
            writeDiffArr(v);
        } else
        if ( sizeNorm <= sizDiff && sizeNorm <= sizeThin && sizeNorm <= sizOffs ) {
            writeFByte(1);
            writeCIntArr(v);
        } else
        if ( sizeThin <= sizeNorm && sizeThin <= sizDiff && sizeThin <= sizOffs ) {
            writeFByte(2);
            writeFIntThin(v);
        } else
        if ( sizOffs <= sizeNorm && sizOffs <= sizeThin && sizOffs <= sizDiff ) {
            writeFByte(3);
            writeShortOffsArr(min,v);
        }
    }

    private void writeShortOffsArr(int min, int[] v) throws IOException {
        writeCInt(min);
        for (int i = 0; i < v.length; i++) {
            writeFShort(v[i] - min);
        }
    }

    private void writeDiffArr(int[] v) throws IOException {
        writeCInt(v[0]);
        for (int i = 1; i < v.length; i++) {
            writeCInt(v[i]-v[i-1]);
        }
    }

    public void writePlainIntArrUnsafe(int v[]) throws IOException {
        int length = v.length;
        buffout.ensureFree(4*length);
        final byte buf[] = buffout.buf;
        int siz = (int) (length * intscal);
        unsafe.copyMemory(v, intoff, buf, buffout.pos + bufoff, siz);
        buffout.pos += siz;
        written += siz;
    }

    public void writeFIntArr(int v[]) throws IOException {
        if ( unsafe != null && UNSAFE_MEMCOPY_ARRAY_INT) {
            writePlainIntArrUnsafe(v);
            return;
        }
        final int free = 4 * v.length;
        buffout.ensureFree(free);
        final byte[] buf = buffout.buf;
        int count = buffout.pos;
        for (int i = 0; i < v.length; i++) {
            final int anInt = v[i];
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
        }
        written += count-buffout.pos;
        buffout.pos = count;
    }

    public void writeCIntArr(int v[]) throws IOException {
        if (unsafe!=null && UNSAFE_WRITE_CINT_ARR) {
            writeCIntArrUnsafe(v);
            return;
        }
        final int free = 5 * v.length;
        buffout.ensureFree(free);
        final byte[] buf = buffout.buf;
        int count = buffout.pos;
        for (int i = 0; i < v.length; i++) {
            final int anInt = v[i];
            if ( anInt > -127 && anInt <=127 ) {
                buffout.buf[count++] = (byte)anInt;
                written++;
            } else
            if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
                buf[count++] = -128;
                buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
                written+=3;
            } else {
                buf[count++] = -127;
                buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
                buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
                written += 5;
            }
        }
        buffout.pos = count;
    }

    public void writeCIntArrUnsafe(int v[]) throws IOException {
        final int free = 5 * v.length;
        buffout.ensureFree(free);
        final byte buf[] = buffout.buf;
        long count = buffout.pos+bufoff;
        for (int i = 0; i < v.length; i++) {
            final int anInt = v[i];
            if ( anInt > -127 && anInt <=127 ) {
                unsafe.putByte(buf,count++,(byte)anInt);
            } else
            if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
                unsafe.putByte(buf,count++,(byte)-128);
                unsafe.putByte(buf,count++,(byte) ((anInt >>>  8) & 0xFF));
                unsafe.putByte(buf,count++,(byte) ((anInt >>>  0) & 0xFF));
            } else {
                unsafe.putByte(buf,count++,(byte)-127);
                unsafe.putByte(buf,count++,(byte) ((anInt >>> 24) & 0xFF));
                unsafe.putByte(buf,count++,(byte) ((anInt >>> 16) & 0xFF));
                unsafe.putByte(buf,count++,(byte) ((anInt >>>  8) & 0xFF));
                unsafe.putByte(buf,count++,(byte) ((anInt >>>  0) & 0xFF));
            }
        }
        int i = (int) (count - bufoff);
        written += i-buffout.pos;
        buffout.pos = i;
    }

    public void writeCInt(int anInt) throws IOException {
        if ( unsafe != null && UNSAFE_WRITE_CINT ) {
            writeCIntUnsafe(anInt);
            return;
        }
        // -128 = short byte, -127 == 4 byte
        if ( anInt > -127 && anInt <=127 ) {
            if ( buffout.buf.length <= buffout.pos +1 )
            {
                buffout.ensureFree(1);
            }
            buffout.buf[buffout.pos++] = (byte)anInt;
            written++;
        } else
        if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
            if ( buffout.buf.length <= buffout.pos +2 )
            {
                buffout.ensureFree(3);
            }
            final byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = -128;
            buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buffout.pos += 3;
            written += 3;
        } else {
            buffout.ensureFree(5);
            final byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = -127;
            buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
            buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buffout.pos = count;
            written += 5;
        }
    }

    private void writeCIntUnsafe(int anInt) throws IOException {
        buffout.ensureFree(5);
        final byte buf[] = buffout.buf;
        long count = buffout.pos+bufoff;
        if ( anInt > -127 && anInt <=127 ) {
            unsafe.putByte(buf,count,(byte)anInt);
            buffout.pos++;
            written++;
        } else
        if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
            unsafe.putByte(buf,count++,(byte)-128);
            unsafe.putByte(buf,count++,(byte) ((anInt >>>  8) & 0xFF));
            unsafe.putByte(buf,count++,(byte) ((anInt >>> 0) & 0xFF));
            buffout.pos += 3;
            written += 3;
        } else {
            unsafe.putByte(buf,count++,(byte)-127);
            unsafe.putByte(buf,count++,(byte) ((anInt >>> 24) & 0xFF));
            unsafe.putByte(buf,count++,(byte) ((anInt >>> 16) & 0xFF));
            unsafe.putByte(buf,count++,(byte) ((anInt >>> 8) & 0xFF));
            unsafe.putByte(buf,count++,(byte) ((anInt >>> 0) & 0xFF));
            buffout.pos += 5;
            written += 5;
        }

    }

    /** Writes a 4 byte float. */
    public void writeCFloat (float value) throws IOException {
        writeFInt(Float.floatToIntBits(value));
    }

    /** Writes a 4 byte float. */
    public void writeCFloatUnsafe(float value) throws IOException {
        writeFIntUnsafe(Float.floatToIntBits(value));
    }

    /** Writes a 4 byte float. */
    public void writeFFloat (float value) throws IOException {
        writeFInt(Float.floatToIntBits(value));
    }
    public void writeCDouble (double value) throws IOException {
        writeFLong(Double.doubleToLongBits(value));
    }

    public void writeCDoubleUnsafe(double value) throws IOException {
        writeFLongUnsafe(Double.doubleToLongBits(value));
    }

    public void writeFDouble (double value) throws IOException {
        writeFLong(Double.doubleToLongBits(value));
    }

    public void writeFDoubleUnsafe(double value) throws IOException {
        buffout.ensureFree(8);
        final byte buf[] = buffout.buf;
        unsafe.putDouble(buf,buffout.pos+bufoff,value);
        buffout.pos += 8;
        written += 8;
//        writeFLongUnsafe(Double.doubleToLongBits(value));
    }

    public void writeCLongUnsafe(long anInt) throws IOException {
// -128 = short byte, -127 == 4 byte
        if ( anInt > -126 && anInt <=127 ) {
            writeFByteUnsafe((int) anInt);
        } else
        if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
            writeFByteUnsafe(-128);
            writeFShort((int) anInt);
        } else if ( anInt >= Integer.MIN_VALUE && anInt <= Integer.MAX_VALUE ) {
            writeFByteUnsafe(-127);
            writeFIntUnsafe((int) anInt);
        } else {
            writeFByteUnsafe(-126);
            writeFLongUnsafe(anInt);
        }
    }

    public void writeCLong(long anInt) throws IOException {
// -128 = short byte, -127 == 4 byte
        if ( anInt > -126 && anInt <=127 ) {
            writeFByte((int) anInt);
        } else
        if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
            writeFByte(-128);
            writeFShort((int) anInt);
        } else if ( anInt >= Integer.MIN_VALUE && anInt <= Integer.MAX_VALUE ) {
            writeFByte(-127);
            writeFInt((int) anInt);
        } else {
            writeFByte(-126);
            writeFLong(anInt);
        }
    }

    /**
     * for internal use only, the state of the outputstream is not reset properly
     */
    void reset() {
        written = 0;
        buffout.reset();
        unsafe = FSTUtil.unsafe;
    }

    void resetAndClearRefs() {
        reset();
        objects.clearForWrite();
        clnames.clear();
    }

    /**
     * if out == null => automatically create/reuse a bytebuffer
     *
     * @param out
     */
    public void resetForReUse( OutputStream out ) {
        unsafe = FSTUtil.unsafe;
        if ( closed )
            throw new RuntimeException("Can't reuse closed stream");
        reset();
        if ( out != null ) {
            buffout.setOutstream(out);
        } else {
            this.out = buffout;
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
        unsafe = FSTUtil.unsafe;
        if ( closed )
            throw new RuntimeException("Can't reuse closed stream");
        reset();
        this.out = buffout;
        buffout.reset(out);
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
                FSTObjectOutput.this.writeFByte(val);
            }

            @Override
            public void write(byte[] buf) throws IOException {
                buffout.ensureFree(buf.length);
                FSTObjectOutput.this.write(buf);
            }

            @Override
            public void write(byte[] buf, int off, int len) throws IOException {
                buffout.ensureFree(len);
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
                buffout.ensureFree(1);
                FSTObjectOutput.this.writeBoolean(val);
            }

            @Override
            public void writeByte(int val) throws IOException {
                FSTObjectOutput.this.writeFByte(val);
            }

            @Override
            public void writeShort(int val) throws IOException {
                FSTObjectOutput.this.writeFShort(val);
            }

            @Override
            public void writeChar(int val) throws IOException {
                FSTObjectOutput.this.writeFChar(val);
            }

            @Override
            public void writeInt(int val) throws IOException {
                FSTObjectOutput.this.writeFInt(val);
            }

            @Override
            public void writeLong(long val) throws IOException {
                FSTObjectOutput.this.writeFLong(val);
            }

            @Override
            public void writeFloat(float val) throws IOException {
                FSTObjectOutput.this.writeCFloat(val);
            }

            @Override
            public void writeDouble(double val) throws IOException {
                FSTObjectOutput.this.writeFDouble(val);
            }

            @Override
            public void writeBytes(String str) throws IOException {
                buffout.ensureFree(str.length()*4);
                FSTObjectOutput.this.writeBytes(str);
            }

            @Override
            public void writeChars(String str) throws IOException {
                buffout.ensureFree(str.length()*4);
                FSTObjectOutput.this.writeChars(str);
            }

            @Override
            public void writeUTF(String str) throws IOException {
                FSTObjectOutput.this.writeStringUTF(str);
            }
        };

        return out;
    }

    public FSTObjectRegistry getObjectMap() {
        return objects;
    }

    /**
     * @return the written buffer reference. use getWritten() to obtain the valid length of written bytes.
     */
    public byte[] getBuffer() {
        return buffout.buf;
    }

    /**
     * @return a copy of written bytes
     */
    public byte[] getCopyOfWrittenBuffer() {
        byte res [] = new byte[written];
        byte[] buffer = getBuffer();
        System.arraycopy(buffer,0,res,0,written);
        return res;
    }

    public FSTConfiguration getConf() {
        return conf;
    }

    public static void main(String arg[]) throws IOException {
        ByteArrayOutputStream out1 = new ByteArrayOutputStream(5000);
        FSTObjectOutput ou = new FSTObjectOutput(out1, FSTConfiguration.createDefaultConfiguration() );
        String str = "word frequencies, tend to vary. More recent analyses show that letter frequencies, like word frequencies, tend to vary, both by writer and by subject. This is a standard text sequence which might get packed. One cannot write an essay about x-rays without using frequent Xs, and the essay will have an especially strange letter frequency if the essay is about the frequent use of x-rays to treat zebras in Qatar.";
        System.out.println(str.length()+" => "+ou.writeStringCompressed(str));
        String str1 = "Kann auch mal ein deutscher Text sein, oder ?";
        System.out.println(str1.length()+" => "+ou.writeStringCompressed(str1));
        String str2 = "Imagine is a song written and performed by English musician John Lennon. The best selling single of his solo career, its lyrical statement is one of idealistic collectivism. It challenges the listener to imagine a world at peace, without the divisiveness and barriers of borders, religious denominations and nationalities, and to consider the possibility that the focus of humanity should be living a life unattached to material possessions. Lennon and Yoko Ono co-produced the song and album of the same name with Phil Spector. One month after the September 1971 release of the LP, Lennon released Imagine as a single in the United States; the song peaked at number 3 on the Billboard Hot 100 and the album became the most commercially successful and critically acclaimed of his solo career. Lennon released \"Imagine\" as a single in the United Kingdom in 1975, and the song has since sold more than 1.6 million copies in the UK. It earned a Grammy Hall of Fame Award, was inducted into the Rock and Roll Hall of Fame's 500 Songs that Shaped Rock and Roll, and Rolling Stone ranked it number 3 in their list of \"The 500 Greatest Songs of All Time\". (Full article...)";
        System.out.println(str2.length()+" => "+ou.writeStringCompressed(str2));
        String str3 = "standard default waiting state init finish end";
        System.out.println(str3.length()+" => "+ou.writeStringCompressed(str3));
        ou.close();

        FSTObjectInput inp = new FSTObjectInput(new ByteArrayInputStream(out1.toByteArray()),ou.getConf() );
        String ins = inp.readStringCompressed();
        System.out.println(str.equals(ins));
        System.out.println(str1.equals(inp.readStringCompressed()));
        System.out.println(str2.equals(inp.readStringCompressed()));
        System.out.println(str3.equals(inp.readStringCompressed()));
    }

    public int getWritten() {
        return written;
    }
}