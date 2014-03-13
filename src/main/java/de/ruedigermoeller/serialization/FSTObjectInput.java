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

import de.ruedigermoeller.serialization.serializers.FSTStringSerializer;
import de.ruedigermoeller.serialization.util.FSTInputStream;
import de.ruedigermoeller.serialization.util.FSTInt2ObjectMap;
import de.ruedigermoeller.serialization.util.FSTUtil;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 04.11.12
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
/**
 * replacement of ObjectInputStream
 */
public class FSTObjectInput extends DataInputStream implements ObjectInput {

    private static final boolean UNSAFE_COPY_ARRAY_LONG = true;
    private static final boolean UNSAFE_COPY_ARRAY_INT = true;
    private static final boolean UNSAFE_READ_CINT_ARR = true;
    private static final boolean UNSAFE_READ_CINT = true;
    private static final boolean UNSAFE_READ_FINT = true;
    private static final boolean UNSAFE_READ_FLONG = true;
    private static final boolean UNSAFE_READ_UTF = true;

    final static long bufoff;
    final static long choff;
    final static long intoff;
    final static long longoff;
    final static long intscal;
    final static long longscal;
    final static long chscal;
    static ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);

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

    public FSTClazzNameRegistry clnames;
    FSTObjectRegistry objects;

    Stack<String> debugStack;
    int curDepth;

    ArrayList<CallbackEntry> callbacks;
    FSTConfiguration conf;
    FSTInputStream input;
    // mirrored from conf
    boolean ignoreAnnotations;
    FSTClazzInfoRegistry clInfoRegistry;
    boolean preferSpeed;
    // done
    ConditionalCallback conditionalCallback;
    int readExternalReadAHead = 8000;
    
    public FSTConfiguration getConf() {
        return conf;
    }


    static class CallbackEntry {
        ObjectInputValidation cb;
        int prio;

        CallbackEntry(ObjectInputValidation cb, int prio) {
            this.cb = cb;
            this.prio = prio;
        }
    }

    public static interface ConditionalCallback {
        public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field);
    }

    public FSTObjectInput() throws IOException {
        this(empty, FSTConfiguration.getDefaultConfiguration());
    }

    public FSTObjectInput(FSTConfiguration conf) {
        this(empty, conf);
    }

    /**
     * Creates a FSTObjectInput that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public FSTObjectInput(InputStream in) throws IOException {
        this(in,FSTConfiguration.getDefaultConfiguration());
    }

    /**
     * Creates a FSTObjectInput that uses the specified
     * underlying InputStream.
     *
     * Don't create a FSTConfiguration with each stream, just create one global static configuration and reuseit.
     * FSTConfiguration is threadsafe.
     *
     * @param in the specified input stream
     */
    public FSTObjectInput(InputStream in, FSTConfiguration conf) {
//        super(in);
//        input = new InputStreamWrapper(this.in);
        super(new FSTInputStream(in));
        input = (FSTInputStream) this.in;
        this.conf = conf;
        initRegistries();
    }

    public Class getClassForName(String name) throws ClassNotFoundException {
        return clnames.classForName(name);
    }

    void initRegistries() {
        ignoreAnnotations = conf.getCLInfoRegistry().isIgnoreAnnotations();
        clInfoRegistry = conf.getCLInfoRegistry();
        preferSpeed = conf.isPreferSpeed();

        objects = (FSTObjectRegistry) conf.getCachedObject(FSTObjectRegistry.class);
        if (objects == null) {
            objects = new FSTObjectRegistry(conf);
        } else {
            objects.clearForRead();
        }
        clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
        if (clnames == null) {
            clnames = new FSTClazzNameRegistry(conf.getClassRegistry(), conf);
        } else {
            clnames.clear();
        }
    }

    public ConditionalCallback getConditionalCallback() {
        return conditionalCallback;
    }

    public void setConditionalCallback(ConditionalCallback conditionalCallback) {
        this.conditionalCallback = conditionalCallback;
    }

    public int getReadExternalReadAHead() {
        return readExternalReadAHead;
    }

    /**
     * since the stock readXX methods on InputStream are final, i can't ensure sufficient readAhead on the inputStream
     * before calling readExternal. Default value is 16000 bytes. If you make use of the externalizable interfac
     * and write larger Objects a) cast the ObjectInput in readExternal to FSTObjectInput and call ensureReadAhead on this
     * in your readExternal method b) set a sufficient maximum using this method before serializing.
     * @param readExternalReadAHead
     */
    public void setReadExternalReadAHead(int readExternalReadAHead) {
        this.readExternalReadAHead = readExternalReadAHead;
    }

    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        try {
            return readObject((Class[]) null);
        } catch (Exception e) {
            dumpDebugStack();
            throw new IOException(e);
        }
    }

    void processValidation() throws InvalidObjectException {
        if (callbacks == null) {
            return;
        }
        Collections.sort(callbacks, new Comparator<CallbackEntry>() {
            @Override
            public int compare(CallbackEntry o1, CallbackEntry o2) {
                return o2.prio - o1.prio;
            }
        });
        for (int i = 0; i < callbacks.size(); i++) {
            CallbackEntry callbackEntry = callbacks.get(i);
            try {
                callbackEntry.cb.validateObject();
            } catch (Exception ex) {
                throw FSTUtil.rethrow(ex);
            }
        }
    }

    private void dumpDebugStack() {
//        if (DEBUGSTACK) {
//            for (int i = 0; i < debugStack.size(); i++) {
//                String s = debugStack.get(i);
//                System.err.println(i + ":" + s);
//            }
//        }
    }

    public Object readObject(Class... possibles) throws Exception {
        curDepth++;
        try {
            if (possibles != null) {
                for (int i = 0; i < possibles.length; i++) {
                    Class possible = possibles[i];
                    clnames.registerClass(possible);
                }
            }
            Object res = readObjectInternal(possibles);
            processValidation();
            return res;
        } catch (Throwable th) {
            dumpDebugStack();
            throw FSTUtil.rethrow(th);
        } finally {
            curDepth--;
        }
    }

    FSTClazzInfo.FSTFieldInfo infoCache;
    public Object readObjectInternal(Class... expected) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException {
//        System.out.println("read:"+input.pos);
//        if ( curDepth == 0 ) {
//            throw new RuntimeException("do not call this directly. only for internal use (incl. Serializers)");
//        }
        try {
            FSTClazzInfo.FSTFieldInfo info = infoCache;
            infoCache = null;
            if (info == null )
                info = new FSTClazzInfo.FSTFieldInfo(expected, null, ignoreAnnotations);
            else
                info.possibleClasses = expected;
            Object res = readObjectWithHeader(info);
            infoCache = info;
            return res;
        } catch (Throwable t) {
            throw FSTUtil.rethrow(t);
        }
    }

    public Object readObjectWithHeader(FSTClazzInfo.FSTFieldInfo referencee) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
        FSTClazzInfo clzSerInfo;
        Class c;
        final int readPos = input.pos-input.off; // incase of pointing to larger array => general issue in both in and output stream when start is !=0 !
        byte code = readFByte();
        if (code == FSTObjectOutput.OBJECT ) {
            // class name
            clzSerInfo = readClass();
            c = clzSerInfo.getClazz();
        } else if ( code == FSTObjectOutput.TYPED ) {
            c = referencee.getType();
            clzSerInfo = getClazzInfo(c, referencee);
        } else if ( code >= 1 ) {
            c = referencee.getPossibleClasses()[code - 1];
            clzSerInfo = getClazzInfo(c, referencee);
        } else {
            return instantiateSpecialTag(referencee, readPos, code);
        }
//        if (DEBUGSTACK) {
//            debugStack.push("" + referencee.getDesc() + " code:" + code);
//            debugStack.push("" + referencee.getDesc() + " " + c);
//        }
        try {
            FSTObjectSerializer ser = clzSerInfo.getSer();
            if (ser != null) {
                return instantiateAndReadWithSer(c, ser, clzSerInfo, referencee, readPos);
            } else {
                return instantiateAndReadNoSer(c, clzSerInfo, referencee, readPos);
            }
        } catch (Exception e) {
            throw FSTUtil.rethrow(e);
        }
    }

    private Object instantiateSpecialTag(FSTClazzInfo.FSTFieldInfo referencee, int readPos, byte code) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if ( code == FSTObjectOutput.STRING ) { // faster than switch ..
            return readStringUTFDef();
        } else if ( code == FSTObjectOutput.ENUM ) {
            return instantiateEnum(referencee, readPos);
        } else if ( code == FSTObjectOutput.NULL ) {
            return null;
        } else
        {
            switch (code) {
                case FSTObjectOutput.BIG_INT: { return instantiateBigInt(); }
                case FSTObjectOutput.BIG_LONG: { return new Long(readCLong()); }
                case FSTObjectOutput.BIG_BOOLEAN_FALSE: { return Boolean.FALSE; }
                case FSTObjectOutput.BIG_BOOLEAN_TRUE: { return Boolean.TRUE; }
                case FSTObjectOutput.ONE_OF: { return referencee.getOneOf()[readFByte()]; }
                case FSTObjectOutput.NULL: { return null; }
                case FSTObjectOutput.STRING: return readStringUTFDef();
                case FSTObjectOutput.HANDLE: { return instantiateHandle(referencee); }
                case FSTObjectOutput.COPYHANDLE: { return instantiateCopyHandle(); }
                case FSTObjectOutput.ARRAY: { return instantiateArray(referencee, readPos); }
                case FSTObjectOutput.ENUM: { return instantiateEnum(referencee, readPos); }
            }
            throw new RuntimeException("unknown object tag "+code);
        }
    }

    private FSTClazzInfo getClazzInfo(Class c, FSTClazzInfo.FSTFieldInfo referencee) {
        FSTClazzInfo clzSerInfo;
        if ( referencee.lastInfo != null && referencee.lastInfo.clazz == c) {
            clzSerInfo = referencee.lastInfo;
        } else {
            clzSerInfo = clInfoRegistry.getCLInfo(c);
        }
        return clzSerInfo;
    }

    private Object instantiateHandle(FSTClazzInfo.FSTFieldInfo referencee) throws IOException {
        int handle = readCInt();
        Object res = objects.getReadRegisteredObject(handle);
        if (res == null) {
            throw new IOException("unable to ressolve handle " + handle + " " + referencee.getDesc() + " " + input.pos);
        }
        return res;
    }

    private Object instantiateCopyHandle() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int handle = readCInt(); // = streamposition
        Object res = objects.getReadRegisteredObject(handle);
        if (res == null) {
            throw new IOException("unable to ressolve handle " + handle);
        }
        Object copy = copy(res, handle);
        //objects.registerObjectForWrite(copy,true, readPos, null); //fixme:no need
        return copy;
    }

    private Object instantiateArray(FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Object res = readArray(referencee);
        if ( ! referencee.isFlat() ) {
            objects.registerObjectForRead(res, readPos);
        }
        return res;
    }

    private Object instantiateEnum(FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws IOException, ClassNotFoundException {
        FSTClazzInfo clzSerInfo;
        Class c;
        clzSerInfo = readClass();
        c = clzSerInfo.getClazz();
        int ordinal = readCInt();
        Object[] enumConstants = clzSerInfo.getEnumConstants();
        if ( enumConstants == null ) {
            // pseudo enum of anonymous classes tom style ?
            return null;
        }
        Object res = enumConstants[ordinal];
        if ( ! referencee.isFlat() ) { // fixme: unnecessary
            objects.registerObjectForRead(res, readPos);
        }
        return res;
    }

    private Object instantiateBigInt() throws IOException {
        int val = readCInt();
        if (val >= 0 && val < FSTConfiguration.intObjects.length) {
            return FSTConfiguration.intObjects[val];
        }
        return new Integer(val);
    }

    private Object instantiateAndReadWithSer(Class c, FSTObjectSerializer ser, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        boolean serInstance = false;
        Object newObj = ser.instantiate(c, this, clzSerInfo, referencee, readPos);
        if (newObj == null) {
            newObj = clzSerInfo.newInstance();
        } else
            serInstance = true;
        if (newObj == null) {
            throw new IOException(referencee.getDesc() + ":Failed to instantiate '" + c.getName() + "'. Register a custom serializer implementing instantiate.");
        }
        if (newObj.getClass() != c) {//FIXME
            c = newObj.getClass();
            clzSerInfo = clInfoRegistry.getCLInfo(c);
        }
        if ( ! referencee.isFlat() && ! clzSerInfo.isFlat() && !ser.alwaysCopy()) {
            objects.registerObjectForRead(newObj, readPos);
        }
        if ( !serInstance )
            ser.readObject(this, newObj, clzSerInfo, referencee);
        return newObj;
    }

    private Object instantiateAndReadNoSer(Class c, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Object newObj;
        newObj = clzSerInfo.newInstance();
        if (newObj == null) {
            throw new IOException(referencee.getDesc() + ":Failed to instantiate '" + c.getName() + "'. Register a custom serializer implementing instantiate.");
        }
        if ( ! referencee.isFlat() && ! clzSerInfo.isFlat() ) {
            objects.registerObjectForRead(newObj, readPos);
        }
        if ( clzSerInfo.isExternalizable() )
        {
            ensureReadAhead(readExternalReadAHead);
            ((Externalizable)newObj).readExternal(this);
        } else if (clzSerInfo.useCompatibleMode())
        {
            Object replaced = readObjectCompatible(referencee, clzSerInfo, newObj);
            if (replaced != null && replaced != newObj) {
                objects.replace(newObj, replaced, readPos);
                newObj = replaced;
            }
        } else {
            FSTClazzInfo.FSTFieldInfo[] fieldInfo = clzSerInfo.getFieldInfo();
            readObjectFields(referencee, clzSerInfo, fieldInfo, newObj);
        }
        return newObj;
    }

    protected Object copy(Object res, int streamPosition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object copy = conf.getCopier().copy(res, conf);
        if (copy == null) {
            return defaultCopy(res, streamPosition);
        }
        return copy;
    }

    ByteArrayOutputStream copyStream;
    FSTInt2ObjectMap<byte[]> mCopyHash;

    protected Object defaultCopy(Object toCopy, int streamPosition) throws IOException, ClassNotFoundException {
        final byte[] buf = input.buf;
        final int pos = streamPosition;
//        if ( mCopyHash == null ) {
//            mCopyHash = new FSTInt2ObjectMap<byte[]>(11);
//        }
//        buf = mCopyHash.get(streamPosition);
//        if ( buf == null ) {
//            if ( copyStream == null ) {
//                copyStream = new ByteArrayOutputStream(50);
//            }
//            copyStream.reset();
//            FSTObjectOutput fstObjectOutput = new FSTObjectOutput(copyStream, conf);
//            fstObjectOutput.objects.disabled = true;
//            fstObjectOutput.writeObject(toCopy, null);
//            fstObjectOutput.close();
//            buf = copyStream.toByteArray();
//            mCopyHash.add(streamPosition,buf);
//        }
        try {
            input.push(buf, pos, buf.length);
            Object res = readObject(null);
            input.pop();
            return res;
        } catch (Exception e) {
            throw FSTUtil.rethrow(e);
        }
    }


    protected Object readObjectCompatible(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, Object newObj) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class cl = serializationInfo.getClazz();
        readObjectCompatibleRecursive(referencee, newObj, serializationInfo, cl);
        if (newObj != null &&
                serializationInfo.getReadResolveMethod() != null) {
            Object rep = null;
            try {
                rep = serializationInfo.getReadResolveMethod().invoke(newObj);
            } catch (InvocationTargetException e) {
                throw FSTUtil.rethrow(e);
            }
//                if (unshared && rep.getClass().isArray()) { //FIXME
//                    rep = cloneArray(rep);
//                }
//                if (rep != newObj) {
//                    handles.setObject(passHandle, obj = rep);
//                }
//            System.out.println("READ RESSOLVE CALLED REPLACED " + newObj.getClass() + " by " + rep.getClass() + " pos:" + input.pos);
            newObj = rep;//FIXME: support this in call
        }
        return newObj;
    }

    protected void readObjectCompatibleRecursive(FSTClazzInfo.FSTFieldInfo referencee, Object toRead, FSTClazzInfo serializationInfo, Class cl) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = serializationInfo.compInfo.get(cl);
        if (!Serializable.class.isAssignableFrom(cl)) {
            return;
        }
        readObjectCompatibleRecursive(referencee, toRead, serializationInfo, cl.getSuperclass());
        if (fstCompatibilityInfo != null && fstCompatibilityInfo.getReadMethod() != null) {
            try {
                ObjectInputStream objectInputStream = getObjectInputStream(cl, serializationInfo, referencee, toRead);
                fstCompatibilityInfo.getReadMethod().invoke(toRead, objectInputStream);
                fakeWrapper.pop();
            } catch (Exception e) {
                throw FSTUtil.rethrow(e);
            }
        } else {
            if (fstCompatibilityInfo != null) {
                readObjectFields(referencee, serializationInfo, fstCompatibilityInfo.getFieldArray(), toRead);
            }
        }
    }

    public void defaultReadObject(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, Object newObj)
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        readObjectFields(referencee,serializationInfo,serializationInfo.getFieldInfo(),newObj);
    }

    void readObjectFields(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, Object newObj) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        if ( FSTUtil.unsafe != null ) {
            if (preferSpeed)
                readObjectFieldsUnsafeSpeed(referencee,serializationInfo,fieldInfo,newObj);
            else
                readObjectFieldsUnsafeCompact(referencee,serializationInfo,fieldInfo,newObj);
        } else {
            readObjectFieldsSafe(referencee,serializationInfo,fieldInfo,newObj);
        }
    }

    void readObjectFieldsUnsafeCompact(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, Object newObj) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        int booleanMask = 0;
        int boolcount = 8;
        final int length = fieldInfo.length;
        int conditional = 0;
        try {
            for (int i = 0; i < length; i++) {
                FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if (subInfo.isPrimitive()) {
                    final Class subInfTzpe = subInfo.getType();
                    if (subInfTzpe == boolean.class) {
                        if (boolcount == 8) {
                            booleanMask = ((int) readFByte() + 256) &0xff;
                            boolcount = 0;
                        }
                        boolean val = (booleanMask & 128) != 0;
                        booleanMask = booleanMask << 1;
                        boolcount++;
                        subInfo.setBooleanValue(newObj, val);
                    } else {
                        if (subInfTzpe == int.class) {
                            subInfo.setIntValueUnsafe(newObj, readCIntUnsafe());
                        } else if (subInfTzpe == long.class) {
                            subInfo.setLongValueUnsafe(newObj, readCLong());
                        } else if (subInfTzpe == byte.class) {
                            subInfo.setByteValue(newObj, readFByte());
                        } else if (subInfTzpe == char.class) {
                            subInfo.setCharValue(newObj, readCChar());
                        } else if (subInfTzpe == short.class) {
                            subInfo.setShortValue(newObj,  readCShort());
                        } else if (subInfTzpe == double.class) {
                            subInfo.setDoubleValueUnsafe(newObj, readCDouble());
                        } else if (subInfTzpe == float.class) {
                            subInfo.setFloatValue(newObj, readCFloat());
                        }
                    }
                } else {
                    if ( subInfo.isConditional() ) {
                        if ( conditional == 0 ) {
                            conditional = readFIntUnsafe();
                            if ( skipConditional(newObj, conditional, subInfo) ) {
                                input.pos = conditional;
                                continue;
                            }
                        }
                    }
                    // object
                    Object subObject = readObjectWithHeader(subInfo);
                    subInfo.setObjectValueUnsafe(newObj, subObject);
                }
            }
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    void readObjectFieldsUnsafeSpeed(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, Object newObj) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        int booleanMask = 0;
        int boolcount = 8;
        final int length = fieldInfo.length;
        int conditional = 0;
        final Unsafe unsafe = FSTUtil.unsafe;
        try {
            for (int i = 0; i < length; i++) {
                FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if (subInfo.isPrimitive()) {
                    final Class subInfTzpe = subInfo.getType();
                    if (subInfTzpe == boolean.class) {
                        if (boolcount == 8) {
                            booleanMask = ((int) readFByte() + 256) &0xff;
                            boolcount = 0;
                        }
                        boolean val = (booleanMask & 128) != 0;
                        booleanMask = booleanMask << 1;
                        boolcount++;
                        subInfo.setBooleanValue(newObj, val);
                    } else {
                        // speed unsafe
                        if (subInfTzpe == int.class) {
                            // inline serializationInfo.setIntValueUnsafe(newObj, subInfo, readFIntUnsafe());
                            ensureReadAhead(4);
                            int res = unsafe.getInt(input.buf,input.pos+bufoff);
                            input.pos += 4;
                            FSTUtil.unsafe.putInt(newObj,subInfo.memOffset,res);
                        } else if (subInfTzpe == long.class) {
                            // inline serializationInfo.setLongValueUnsafe(newObj, subInfo, readFLongUnsafe());
                            ensureReadAhead(8);
                            long res = unsafe.getLong(input.buf, input.pos + bufoff);
                            input.pos += 8;
                            FSTUtil.unsafe.putLong(newObj, subInfo.memOffset, res);
                        } else if (subInfTzpe == byte.class) {
                            subInfo.setByteValue(newObj, readFByte());
                        } else if (subInfTzpe == char.class) {
                            subInfo.setCharValue(newObj, readFChar());
                        } else if (subInfTzpe == short.class) {
                            subInfo.setShortValue(newObj, readFShort());
                        } else if (subInfTzpe == double.class) {
                            subInfo.setDoubleValueUnsafe(newObj, readFDoubleUnsafe());
                        } else if (subInfTzpe == float.class) {
                            subInfo.setFloatValue(newObj, readFFloat());
                        }
                    }
                } else {
                    if ( subInfo.isConditional() ) {
                        if ( conditional == 0 ) {
                            conditional = readFIntUnsafe();
                            if ( skipConditional(newObj, conditional, subInfo) ) {
                                input.pos = conditional;
                                continue;
                            }
                        }
                    }
                    // object
                    Object subObject = readObjectWithHeader(subInfo);
                    subInfo.setObjectValueUnsafe(newObj, subObject);
                }
            }
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    protected void readObjectFieldsSafe(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, Object newObj) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        int booleanMask = 0;
        int boolcount = 8;
        final int length = fieldInfo.length;
        int conditional = 0;
        for (int i = 0; i < length; i++) {
            try {
                FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if (subInfo.isPrimitive()) {
                    int integralType = subInfo.getIntegralType();
                    if (integralType == FSTClazzInfo.FSTFieldInfo.BOOL) {
                        if (boolcount == 8) {
                            booleanMask = ((int) readFByte() + 256) &0xff;
                            boolcount = 0;
                        }
                        boolean val = (booleanMask & 128) != 0;
                        booleanMask = booleanMask << 1;
                        boolcount++;
                        subInfo.setBooleanValue(newObj, val);
                    } else {
                        if (preferSpeed) {
                            if (integralType==FSTClazzInfo.FSTFieldInfo.INT) {
                                subInfo.setIntValue(newObj, readFInt());
                            } else if ( integralType == FSTClazzInfo.FSTFieldInfo.LONG ) {
                                subInfo.setLongValue(newObj, readFLong());
                            } else {
                                switch (integralType) { // fixme: change also unsafee variant
                                    case FSTClazzInfo.FSTFieldInfo.BYTE:   subInfo.setByteValue(newObj, readFByte()); break;
                                    case FSTClazzInfo.FSTFieldInfo.CHAR:   subInfo.setCharValue(newObj, readFChar()); break;
                                    case FSTClazzInfo.FSTFieldInfo.SHORT:  subInfo.setShortValue(newObj, readFShort()); break;
    //                                case FSTClazzInfo.FSTFieldInfo.INT:    subInfo.setIntValue(newObj, readFInt()); break;
    //                                case FSTClazzInfo.FSTFieldInfo.LONG:   subInfo.setLongValue(newObj, readFLong()); break;
                                    case FSTClazzInfo.FSTFieldInfo.FLOAT:  subInfo.setFloatValue(newObj, readFFloat()); break;
                                    case FSTClazzInfo.FSTFieldInfo.DOUBLE: subInfo.setDoubleValue(newObj, readFDouble()); break;
                                }
                            }
                        } else {
                            if (integralType==FSTClazzInfo.FSTFieldInfo.INT) {
                                subInfo.setIntValue(newObj, readCInt());
                            } else if ( integralType == FSTClazzInfo.FSTFieldInfo.LONG ) {
                                subInfo.setLongValue(newObj, readCLong());
                            } else {
                                switch (integralType) {
                                    case FSTClazzInfo.FSTFieldInfo.BYTE:   subInfo.setByteValue(newObj, readFByte()); break;
                                    case FSTClazzInfo.FSTFieldInfo.CHAR:   subInfo.setCharValue(newObj, readCChar()); break;
                                    case FSTClazzInfo.FSTFieldInfo.SHORT:  subInfo.setShortValue(newObj, readCShort()); break;
//                                    case FSTClazzInfo.FSTFieldInfo.INT:    subInfo.setIntValue(newObj, readCInt()); break;
//                                    case FSTClazzInfo.FSTFieldInfo.LONG:   subInfo.setLongValue(newObj, readCLong()); break;
                                    case FSTClazzInfo.FSTFieldInfo.FLOAT:  subInfo.setFloatValue(newObj, readCFloat()); break;
                                    case FSTClazzInfo.FSTFieldInfo.DOUBLE: subInfo.setDoubleValue(newObj, readCDouble()); break;
                                }
                            }
                        }
                    }
                } else {
                    if ( subInfo.isConditional() ) {
                        if ( conditional == 0 ) {
                            conditional = readFInt();
                            if ( skipConditional(newObj, conditional, subInfo) ) {
                                input.pos = conditional;
                                continue;
                            }
                        }
                    }
                    // object
                    Object subObject = readObjectWithHeader(subInfo);
                    subInfo.setObjectValue(newObj, subObject);
                }
            } catch (IllegalAccessException ex) {
                throw new IOException(ex);
            }
        }
    }

    private boolean skipConditional(Object newObj, int conditional, FSTClazzInfo.FSTFieldInfo subInfo) {
        if ( conditionalCallback != null ) {
            return conditionalCallback.shouldSkip(newObj,conditional,subInfo.getField());
        }
        return false;
    }

    protected void readCompatibleObjectFields(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, Map res) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        int booleanMask = 0;
        int boolcount = 8;
        for (int i = 0; i < fieldInfo.length; i++) {
            try {
                FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if (subInfo.isIntegral() && !subInfo.isArray()) {
                    final Class subInfoType = subInfo.getType();
                    if (subInfoType == boolean.class) {
                        if (boolcount == 8) {
                            booleanMask = ((int) readFByte() + 256) &0xff;
                            boolcount = 0;
                        }
                        boolean val = (booleanMask & 128) != 0;
                        booleanMask = booleanMask << 1;
                        boolcount++;
                        res.put(subInfo.getField().getName(), val);
                    }
                    if (subInfoType == byte.class) {
                        res.put(subInfo.getField().getName(), readFByte());
                    } else if (subInfoType == char.class) {
                        res.put(subInfo.getField().getName(), readCChar());
                    } else if (subInfoType == short.class) {
                        res.put(subInfo.getField().getName(), readCShort());
                    } else if (subInfoType == int.class) {
                        res.put(subInfo.getField().getName(), readCInt());
                    } else if (subInfoType == double.class) {
                        res.put(subInfo.getField().getName(), readCDouble());
                    } else if (subInfoType == float.class) {
                        res.put(subInfo.getField().getName(), readCFloat());
                    } else if (subInfoType == long.class) {
                        res.put(subInfo.getField().getName(), readCLong());
                    }
                } else {
                    // object
                    Object subObject = readObjectWithHeader(subInfo);
                    res.put(subInfo.getField().getName(), subObject);
                }
            } catch (IllegalAccessException ex) {
                throw new IOException(ex);
            }
        }
    }

    final void ensureReadAhead( int bytes ) throws IOException {
        // currently complete object is read ahead
    }

    public String readStringCompressed() throws IOException {
        int len = readCInt();
        char[] charBuf = getCharBuf(len * 3);
        ensureReadAhead(len * 3);
        byte buf[] = input.buf;
        int count = input.pos;
        int chcount = 0;
        while ( chcount < len ) {
            char head = (char) ((buf[count++] + 256) &0xff);
            if (head >= 0 && head < 254) {
                charBuf[chcount++] = head;
            } else {
                if ( head == 254 ) {
                    int nibbles = ((buf[count++] + 256) &0xff);
                    for ( int ii = 0; ii < nibbles; ii++) {
                        int bufVal = ((buf[count]+256)&0xff);
                        if ((ii&1) == 0 ) {
                            charBuf[chcount++] = FSTObjectOutput.enc.charAt(bufVal &0xf);
                            if (ii==nibbles-1) {
                                count++;
                            }
                        } else {
                            charBuf[chcount++] = FSTObjectOutput.enc.charAt((bufVal>>>4)&0xf);
                            count++;
                        }
                    }
                } else {
                    int ch1 = ((buf[count++] + 256) &0xff);
                    int ch2 = ((buf[count++] + 256) &0xff);
                    charBuf[chcount++] = (char) ((ch1 << 8) + (ch2 << 0));
                }
            }
        }
        input.pos = count;
        return new String(charBuf, 0, chcount);
    }

    char chBufS[];

    char[] getCharBuf(int siz) {
        char chars[] = chBufS;
        if (chars == null || chars.length < siz) {
            chars = new char[Math.max(siz,15)];
            chBufS = chars;
        }
        return chars;
    }

    public String readStringUTF() throws IOException {
        if ( preferSpeed ) {
            return readStringUTFSpeed();
        }
        if ( FSTUtil.unsafe != null && UNSAFE_READ_UTF ) {
            return readStringUTFUnsafe();
        }
        return readStringUTFDef();
    }


    byte ascStringCache[];

    /**
     * len < 127 !!!!!
     * @return
     * @throws IOException
     */
    public String readStringAsc() throws IOException {
        int len = readFByte();
        if (ascStringCache == null || ascStringCache.length < len)
            ascStringCache = new byte[len];
        ensureReadAhead(len);
        System.arraycopy(input.buf,input.pos,ascStringCache,0,len);
        input.pos+=len;
        return new String(ascStringCache,0,0,len);
    }

    private String readStringUTFDef() throws IOException {
        int len = readCInt();
        char[] charBuf = getCharBuf(len*3);
        ensureReadAhead(len * 3);
        byte buf[] = input.buf;
        int count = input.pos;
        int chcount = 0;
        for (int i = 0; i < len; i++) {
            char head = (char) ((buf[count++] + 256) &0xff);
            if (head < 255) {
                charBuf[chcount++] = head;
            } else {
                int ch1 = ((buf[count++] + 256) &0xff);
                int ch2 = ((buf[count++] + 256) &0xff);
                charBuf[chcount++] = (char) ((ch1 << 8) + (ch2 << 0));
            }
        }
        input.pos = count;
        return new String(charBuf, 0, chcount);
    }

    public String readStringUTFSpeed() throws IOException {
        if ( FSTUtil.unsafe != null && UNSAFE_READ_UTF ) {
            Unsafe unsafe = FSTUtil.unsafe;
            int len = readFIntUnsafe();
            char[] charBuf = getCharBuf(len*2);
            ensureReadAhead(len * 2);
            byte buf[] = input.buf;
            int count = (int) (input.pos+bufoff);
            int chcount = (int) choff;
            unsafe.copyMemory(buf,count,charBuf,chcount,len*chscal);
//            for (int i = 0; i < len; i++) {
//                char c = unsafe.getChar(buf,count);
//                unsafe.putChar(charBuf,chcount,c);
//                chcount+=chscal;
//                count+=chscal;
//            }
            input.pos += len*chscal;
            return new String(charBuf, 0, len);
        } else {
            int len = readFInt();
            char[] charBuf = getCharBuf(len*2);
            ensureReadAhead(len * 2);
            byte buf[] = input.buf;
            int count = input.pos;
            int chcount = 0;
            for (int i = 0; i < len; i++) {
                int ch2 = ((buf[count++] + 256) &0xff);
                int ch1 = ((buf[count++] + 256) &0xff);
                charBuf[chcount++] = (char) ((ch1 << 8) + (ch2 << 0));
            }
            input.pos = count;
            return new String(charBuf, 0, chcount);
        }
    }

    private String readStringUTFUnsafe() throws IOException {
        final Unsafe unsafe = FSTUtil.unsafe;
        int len = 0;
        if ( UNSAFE_READ_CINT )
            len = readCIntUnsafe();
        else
            len = readCInt();
        char[] charBuf = getCharBuf(len*3);
        ensureReadAhead(len * 3);
        final byte buf[] = input.buf;
        long count = input.pos+bufoff;
        long chcount = choff;
        for (int i = 0; i < len; i++) {
            char head = (char) ((unsafe.getByte(buf,count++) + 256) &0xff);
            if (head >= 0 && head < 255) {
                unsafe.putChar(charBuf,chcount,head);
                chcount+=chscal;
            } else {
                int ch1 = ((unsafe.getByte(buf,count++) + 256) &0xff);
                int ch2 = ((unsafe.getByte(buf,count++) + 256) &0xff);
                unsafe.putChar(charBuf,chcount,(char) ((ch1 << 8) + (ch2 << 0)));
                chcount+=chscal;
            }
        }
        input.pos = (int) (count-bufoff);
        return new String(charBuf, 0, (int) ((chcount-choff)/chscal));
    }

    /**
     * expects len elements. 'F' always means fast (uncompressed) 'C' means compressed (maybe). Util method, does not register
     * read object. for compatibility + symmetry chars are read compressed.
     * FIXME, compressed, fast read, and byteorder is mixed up a lot (though works)
     * @param componentType
     * @return
     */
    public Object readFPrimitiveArray( Class componentType, int len ) {
        try {
            Object array = Array.newInstance(componentType, len);
            if (componentType == byte.class) {
                byte[] arr = (byte[]) array;
                ensureReadAhead(arr.length); // fixme: move this stuff to the stream !
                read(arr);
                return arr;
            } else if (componentType == char.class) {
                char[] arr = (char[]) array;
                for (int j = 0; j < len; j++) {
                    arr[j] = readCChar();
                }
                return arr;
            } else if (componentType == short.class) {
                short[] arr = (short[]) array;
                ensureReadAhead(arr.length*2);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFShort();
                }
                return arr;
            } else if (componentType == int.class) {
                final int[] arr = (int[]) array;
                if ( FSTUtil.unsafe != null && UNSAFE_COPY_ARRAY_INT) {
                    readPlainIntArrUnsafe(arr);
                } else {
                    readFIntArr(len, arr);
                }
                return arr;
            } else if (componentType == float.class) {
                float[] arr = (float[]) array;
                ensureReadAhead(arr.length*4);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFFloat();
                }
                return arr;
            } else if (componentType == double.class) {
                double[] arr = (double[]) array;
                ensureReadAhead(arr.length*8);
                for (int j = 0; j < len; j++) {
                    arr[j] = readFDouble();
                }
                return arr;
            } else if (componentType == long.class) {
                long[] arr = (long[]) array;
                ensureReadAhead(arr.length*8);
                if ( FSTUtil.unsafe != null && UNSAFE_COPY_ARRAY_LONG) {
                    readLongArrUnsafe(arr);
                } else {
                    for (int j = 0; j < len; j++) {
                        arr[j] = readFLong();
                    }
                }
                return arr;
            } else if (componentType == boolean.class) {
                boolean[] arr = (boolean[]) array;
                ensureReadAhead(arr.length);
                for (int j = 0; j < len; j++) {
                    arr[j] = readBoolean();
                }
                return arr;
            } else {
                throw new RuntimeException("unexpected primitive type " + componentType);
            }
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    protected Object readArray(FSTClazzInfo.FSTFieldInfo referencee) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class arrCl = readClass().getClazz();
        final int len = readCInt();
        if (len == -1) {
            return null;
        }
        Class arrType = arrCl.getComponentType();
        if (!arrCl.getComponentType().isArray()) {
            Object array = Array.newInstance(arrType, len);
            objects.registerObjectForRead(array, input.pos - input.off );
            if (arrCl.getComponentType().isPrimitive()) {
                if (arrType == byte.class) {
                    byte[] arr = (byte[]) array;
                    ensureReadAhead(arr.length); // fixme: move this stuff to the stream !
                    read(arr);
                } else if (arrType == char.class) {
                    char[] arr = (char[]) array;
                    for (int j = 0; j < len; j++) {
                        arr[j] = readCChar();
                    }
                } else if (arrType == short.class) {
                    short[] arr = (short[]) array;
                    ensureReadAhead(arr.length*2);
                    for (int j = 0; j < len; j++) {
                        arr[j] = readFShort();
                    }
                } else if (arrType == int.class) {
                    final int[] arr = (int[]) array;
                    if ( referencee.isThin() ) {
                        readThinArray(len, arr);
                    } else if (referencee.isCompressed() ) {
                        readCompressedArray(len,arr);
                    } else {
                        if ( FSTUtil.unsafe != null && UNSAFE_COPY_ARRAY_INT) {
                            readPlainIntArrUnsafe(arr);
                        } else {
                            readFIntArr(len, arr);
                        }
                    }
                } else if (arrType == float.class) {
                    float[] arr = (float[]) array;
                    ensureReadAhead(arr.length*4);
                    for (int j = 0; j < len; j++) {
                        arr[j] = readFFloat();
                    }
                } else if (arrType == double.class) {
                    double[] arr = (double[]) array;
                    ensureReadAhead(arr.length*8);
                    for (int j = 0; j < len; j++) {
                        arr[j] = readFDouble();
                    }
                } else if (arrType == long.class) {
                    long[] arr = (long[]) array;
                    ensureReadAhead(arr.length*8);
                    if ( FSTUtil.unsafe != null && UNSAFE_COPY_ARRAY_LONG) {
                        readLongArrUnsafe(arr);
                    } else {
                        for (int j = 0; j < len; j++) {
                            arr[j] = readFLong();
                        }
                    }
                } else if (arrType == boolean.class) {
                    boolean[] arr = (boolean[]) array;
                    ensureReadAhead(arr.length);
                    for (int j = 0; j < len; j++) {
                        arr[j] = readBoolean();
                    }
                } else {
                    throw new RuntimeException("unexpected primitive type " + arrType);
                }
            } else {
                Object arr[] = (Object[]) array;
                if ( referencee.isThin() ) {
                    for (int i = 0; i < len; i++) {
                        int idx = readCInt();
                        if ( idx == len ) {
                            break;
                        }
                        Object subArray = readObjectWithHeader(referencee);
                        arr[idx] = subArray;
                    }
                } else
                {
                    for (int i = 0; i < len; i++) {
                        Object value = readObjectWithHeader(referencee);
                        arr[i] = value;
                    }
                }

//                Class[] possibleClasses = null;
//                if ( referencee.getPossibleClasses() == null ) {
//                    possibleClasses = new Class[5];
//                } else {
//                    possibleClasses = Arrays.copyOf(referencee.getPossibleClasses(), referencee.getPossibleClasses().length + 5);
//                }
//                FSTClazzInfo.FSTFieldInfo newFI = new FSTClazzInfo.FSTFieldInfo(false, possibleClasses, null);
//                for ( int i=0; i < len; i++ ) {
//                    Object value = readObjectWithHeader(newFI);
//                    Array.set(array, i, value);
//                    if ( value != null ) {
//                        newFI.setPossibleClasses( FSTObjectOutput.addToPredictionArray(newFI.getPossibleClasses(), value.getClass()));
//                    }
//                }
            }
            return array;
        } else {
            Object array[] = (Object[]) Array.newInstance(arrType, len);
            if (!FSTUtil.isPrimitiveArray(arrType) && ! referencee.isFlat() ) {
                objects.registerObjectForRead(array, input.pos - input.off);
            }
//            if ( false && referencee.isThin() ) {
//                for (int i = 0; i < len; i++) {
//                    int idx = readCInt();
//                    if ( idx == len ) {
//                        break;
//                    }
//                    Object subArray = readArray(new FSTClazzInfo.FSTFieldInfo(referencee.getPossibleClasses(), null, conf.getCLInfoRegistry().isIgnoreAnnotations()));
//                    array[idx] = subArray;
//                }
//            } else
            {
                FSTClazzInfo.FSTFieldInfo ref1 = new FSTClazzInfo.FSTFieldInfo(referencee.getPossibleClasses(), null, clInfoRegistry.isIgnoreAnnotations());
                for (int i = 0; i < len; i++) {
                    Object subArray = readArray(ref1);
                    array[i] = subArray;
                }
            }
            return array;
        }
    }

    public void readLongArrUnsafe(long[] arr) {
        final byte buf[] = input.buf;
        int siz = (int) (arr.length * longscal);
        FSTUtil.unsafe.copyMemory(buf, input.pos + bufoff, arr, longoff, siz);
        input.pos += siz;
    }

    public void readPlainIntArrUnsafe(int[] arr) {
        final byte buf[] = input.buf;
        int siz = (int) (arr.length * intscal);
        FSTUtil.unsafe.copyMemory(buf,input.pos+bufoff,arr,intoff,siz);
        input.pos += siz;
    }

    public void readCompressedArray(int len, int arr[]) throws IOException {
        int type = readFByte();
        switch (type) {
            case 0: readDiffArr(len,arr); break;
            case 1: readCIntArr(len, arr); break;
            case 2: readThinArray(len, arr); break;
            case 3: readOffsShortArr(len, arr); break;
        }
    }

    private void readOffsShortArr(int len, int[] arr) throws IOException {
        int min = readCInt();
        for ( int i=0; i < len; i++) {
            arr[i]= min + readShort();
        }
    }

    private void readDiffArr(int len, int[] arr) throws IOException {
        int start = readCInt();
        arr[0] = start;
        for ( int i=1; i < len; i++) {
            arr[i]= arr[i-1] + readCInt();
        }
    }

    private void readThinArray(int len, int[] arr) throws IOException {
        for (int i = 0; i < len; i++) {
            final int index = readCInt();
            if ( index == len ) {
                break;
            }
            final int val = readCInt();
            arr[index] = val;
        }
    }

    public void registerObject(Object o, int streamPosition, FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo referencee) {
        if ( ! objects.disabled && !referencee.isFlat() && ! info.isFlat() ) {
            objects.registerObjectForRead(o, streamPosition);
        }
    }

    public FSTClazzInfo readClass() throws IOException, ClassNotFoundException {
        return clnames.decodeClass(this);
    }

    public int _readCInt() throws IOException {
        byte head = readFByte();
        // -128 = short byte, -127 == 4 byte
        if (head > -127 && head <= 127) {
            return head;
        }
        if (head == -128) {
            return readShort();
        } else {
            return readFInt();
        }
    }

    void resetAndClearRefs() {
        try {
            reset();
            objects.clearForRead();
            clnames.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset() throws IOException {
        input.reset();
    }

    public void resetForReuse(InputStream in) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        input.reset();
        clnames.clear();
        input.initFromStream(in);
        objects.clearForRead(); clnames.clear();
    }

    public void resetForReuseCopyArray(byte bytes[], int off, int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        input.reset();
        objects.clearForRead();
        clnames.clear();
        input.ensureCapacity(len);
        input.count = len;
        System.arraycopy(bytes, off, input.buf, 0, len);
    }

    public void resetForReuseUseArray(byte bytes[]) throws IOException {
        resetForReuseUseArray(bytes, 0, bytes.length);
    }

    public void resetForReuseUseArray(byte bytes[], int off, int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        input.reset();
        objects.clearForRead(); clnames.clear();
        input.count = len+off;
        input.buf = bytes;
        input.pos = off;
        input.off = off;
    }

    public final int readFIntUnsafe() throws IOException {
        ensureReadAhead(8);
        final Unsafe unsafe = FSTUtil.unsafe;
        final byte buf[] = input.buf;
        int res = unsafe.getInt(buf,input.pos+bufoff);
        input.pos += 4;
        return res;
    }

    public final int readFInt() throws IOException {
        if ( FSTUtil.unsafe != null && UNSAFE_READ_FINT ) {
            return readFIntUnsafe();
        }
        ensureReadAhead(4);
        int count = input.pos;
        final byte buf[] = input.buf;
        int ch4 = (buf[count++]+256)&0xff;
        int ch3 = (buf[count++]+256)&0xff;
        int ch2 = (buf[count++]+256)&0xff;
        int ch1 = (buf[count++]+256)&0xff;
        input.pos = count;
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public void readFIntArr(int len, int[] arr) throws IOException {
        ensureReadAhead(4 * len);
        final byte buf[] = input.buf;
        int count = input.pos;
        for (int j = 0; j < len; j++) {
            int ch4 = (buf[count++]+256)&0xff;
            int ch3 = (buf[count++]+256)&0xff;
            int ch2 = (buf[count++]+256)&0xff;
            int ch1 = (buf[count++]+256)&0xff;
            arr[j] = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }
        input.pos = count;
    }

    private void readCIntArr(int len, int[] arr) throws IOException {
        if ( FSTUtil.unsafe != null && UNSAFE_READ_CINT_ARR ) {
            readCIntArrUnsafe(len,arr);
            return;
        }
        ensureReadAhead(5 * len);
        final byte buf[] = input.buf;
        int count = input.pos;
        for (int j = 0; j < len; j++) {
            final byte head = buf[count++];
            // -128 = short byte, -127 == 4 byte
            if (head > -127 && head <= 127) {
                arr[j] = head;
                continue;
            }
            if (head == -128) {
                final int ch1 = (buf[count++]+256)&0xff;
                final int ch2 = (buf[count++]+256)&0xff;
                arr[j] = (short)((ch1 << 8) + (ch2 << 0));
                continue;
            } else {
                int ch1 = (buf[count++]+256)&0xff;
                int ch2 = (buf[count++]+256)&0xff;
                int ch3 = (buf[count++]+256)&0xff;
                int ch4 = (buf[count++]+256)&0xff;
                arr[j] = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
            }
        }
        input.pos = count;
    }

    private void readCIntArrUnsafe(final int len, final int[] arr) throws IOException {
        final Unsafe unsafe = FSTUtil.unsafe;
        ensureReadAhead(5 * len);
        final byte buf[] = input.buf;
        long count = input.pos+bufoff;
        final int max = (int) (len * intscal + intoff);
        int cn = input.pos;
        for (long j = intoff; j < max; j+=intscal) {
            final byte head = unsafe.getByte(buf,count++);
            cn++;
            // -128 = short byte, -127 == 4 byte
            if (head > -127 && head <= 127) {
                unsafe.putInt(arr,j,head);
                continue;
            }
            if (head == -128) {
                final int ch1 = (unsafe.getByte(buf,count++)+256)&0xff;
                final int ch2 = (unsafe.getByte(buf,count++)+256)&0xff;
                unsafe.putInt(arr,j,(short)((ch1 << 8) + (ch2 << 0)));
                cn+=2;
                continue;
            } else {
                int ch1 = (unsafe.getByte(buf,count++)+256)&0xff;
                int ch2 = (unsafe.getByte(buf,count++)+256)&0xff;
                int ch3 = (unsafe.getByte(buf,count++)+256)&0xff;
                int ch4 = (unsafe.getByte(buf,count++)+256)&0xff;
                cn+=4;
                unsafe.putInt(arr,j,((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0)));
            }
        }
        input.pos = cn;
    }

    public final int readCInt() throws IOException {
        ensureReadAhead(5);
        final byte buf[] = input.buf;
        int count = input.pos;
        final byte head = buf[count++];
        // -128 = short byte, -127 == 4 byte
        if (head > -127 && head <= 127) {
            input.pos = count;
            return head;
        }
        if (head == -128) {
            final int ch1 = (buf[count++]+256)&0xff;
            final int ch2 = (buf[count++]+256)&0xff;
            input.pos = count;
            return (short)((ch1 << 8) + (ch2 << 0));
        } else {
            int ch1 = (buf[count++]+256)&0xff;
            int ch2 = (buf[count++]+256)&0xff;
            int ch3 = (buf[count++]+256)&0xff;
            int ch4 = (buf[count++]+256)&0xff;
            input.pos = count;
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }
    }

    private final int readCIntUnsafe() throws IOException {
        final Unsafe unsafe = FSTUtil.unsafe;
        ensureReadAhead(5);
        final byte buf[] = input.buf;
        long count = input.pos+bufoff;
        final byte head = unsafe.getByte(buf,count++);
        // -128 = short byte, -127 == 4 byte
        if (head > -127 && head <= 127) {
            input.pos = (int) (count-bufoff);
            return head;
        }
        if (head == -128) {
            final int ch1 = (unsafe.getByte(buf,count++)+256)&0xff;
            final int ch2 = (unsafe.getByte(buf,count++)+256)&0xff;
            input.pos = (int) (count-bufoff);
            return (short)((ch1 << 8) + (ch2 << 0));
        } else {
            int ch1 = (unsafe.getByte(buf,count++)+256)&0xff;
            int ch2 = (unsafe.getByte(buf,count++)+256)&0xff;
            int ch3 = (unsafe.getByte(buf,count++)+256)&0xff;
            int ch4 = (unsafe.getByte(buf,count++)+256)&0xff;
            input.pos = (int) (count-bufoff);
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }
    }

    public double readFDouble() throws IOException {
        return Double.longBitsToDouble(readFLong());
    }

    public double readFDoubleUnsafe() throws IOException {
        ensureReadAhead(8);
        final Unsafe unsafe = FSTUtil.unsafe;
        final byte buf[] = input.buf;
        double res = unsafe.getDouble(buf,input.pos+bufoff);
        input.pos += 8;
        return res;
//        return Double.longBitsToDouble(readFLongUnsafe());
    }

    public final byte readFByte() throws IOException {
        ensureReadAhead(1);
        return input.buf[input.pos++];
    }

//    public long readFLongUnsafe() throws IOException {
//        ensureReadAhead(8);
//        final Unsafe unsafe = FSTUtil.unsafe;
//        final byte buf[] = input.buf;
//        long count = input.pos+bufoff;
//        long ch8 = (unsafe.getByte(buf,count++)+256)&0xff;
//        long ch7 = (unsafe.getByte(buf,count++)+256)&0xff;
//        long ch6 = (unsafe.getByte(buf,count++)+256)&0xff;
//        long ch5 = (unsafe.getByte(buf,count++)+256)&0xff;
//        long ch4 = (unsafe.getByte(buf,count++)+256)&0xff;
//        long ch3 = (unsafe.getByte(buf,count++)+256)&0xff;
//        long ch2 = (unsafe.getByte(buf,count++)+256)&0xff;
//        long ch1 = (unsafe.getByte(buf,count++)+256)&0xff;
//        input.pos += 8;
//        return ((ch1 << 56) + (ch2 << 48) + (ch3 << 40)+ (ch4 << 32)+(ch5 << 24) + (ch6 << 16) + (ch7 << 8) + (ch8 << 0));
//    }

    public long readFLongUnsafe() throws IOException {
        ensureReadAhead(8);
        final Unsafe unsafe = FSTUtil.unsafe;
        final byte buf[] = input.buf;
        long res = unsafe.getLong(buf,input.pos+bufoff);
        input.pos += 8;
        return res;
    }

    public long readFLong() throws IOException {
        if ( FSTUtil.unsafe != null && UNSAFE_READ_FLONG ) {
            return readFLongUnsafe();
        }
        ensureReadAhead(8);
        int count = input.pos;
        final byte buf[] = input.buf;
        long ch8 = (buf[count++]+256)&0xff;
        long ch7 = (buf[count++]+256)&0xff;
        long ch6 = (buf[count++]+256)&0xff;
        long ch5 = (buf[count++]+256)&0xff;
        long ch4 = (buf[count++]+256)&0xff;
        long ch3 = (buf[count++]+256)&0xff;
        long ch2 = (buf[count++]+256)&0xff;
        long ch1 = (buf[count++]+256)&0xff;
        input.pos = count;
        return ((ch1 << 56) + (ch2 << 48) + (ch3 << 40)+ (ch4 << 32)+(ch5 << 24) + (ch6 << 16) + (ch7 << 8) + (ch8 << 0));
    }

    public long readCLong() throws IOException {
        ensureReadAhead(9);
        byte head = readFByte();
        // -128 = short byte, -127 == 4 byte
        if (head > -126 && head <= 127) {
            return head;
        }
        if (head == -128) {
            return readShort();
        } else if (head == -127) {
            return readFInt();
        } else {
            return readFLong();
        }
    }

    public char readFChar() throws IOException {
        ensureReadAhead(2);
        int count = input.pos;
        final byte buf[] = input.buf;
        int ch2 = (buf[count++]+256)&0xff;
        int ch1 = (buf[count++]+256)&0xff;
        input.pos = count;
        return (char) ((ch1 << 8) + (ch2 << 0));
    }

    public char readCChar() throws IOException {
        ensureReadAhead(3);
        char head = (char) ((readFByte() + 256) &0xff);
        // -128 = short byte, -127 == 4 byte
        if (head >= 0 && head < 255) {
            return head;
        }
        return readChar();
    }

    /**
     * Reads a 4 byte float.
     */
    public float readCFloat() throws IOException {
        return Float.intBitsToFloat(readFInt());
    }

    public float readFFloat() throws IOException {
        return Float.intBitsToFloat(readFInt());
    }

    /**
     * Reads an 8 bytes double.
     */
    public double readCDouble() throws IOException {
        ensureReadAhead(8);
        return Double.longBitsToDouble(readFLong());
    }

    public short readFShort() throws IOException {
        ensureReadAhead(2);
        int count = input.pos;
        final byte buf[] = input.buf;
        int ch1 = (buf[count++]+256)&0xff;
        int ch2 = (buf[count++]+256)&0xff;
        input.pos = count;
        return (short) ((ch1 << 8) + (ch2 << 0));
    }

    public short readCShort() throws IOException {
        ensureReadAhead(3);
        int head = ((int) readFByte() + 256) &0xff;
        if (head >= 0 && head < 255) {
            return (short) head;
        }
        return readShort();
    }

    boolean closed = false;
    @Override
    public void close() throws IOException {
        super.close();
        closed = true;
        resetAndClearRefs();
        conf.returnObject(objects, clnames);
    }

    ////////////////////////////////////////////////////// epic compatibility hack /////////////////////////////////////////////////////////

    MyObjectStream fakeWrapper; // some jdk classes hash for ObjectStream, so provide the same instance always

    ObjectInputStream getObjectInputStream(final Class cl, final FSTClazzInfo clInfo, final FSTClazzInfo.FSTFieldInfo referencee, final Object toRead) throws IOException {
        ObjectInputStream wrapped = new ObjectInputStream() {
            @Override
            public Object readObjectOverride() throws IOException, ClassNotFoundException {
                try {
                    return FSTObjectInput.this.readObjectInternal(referencee.getPossibleClasses());
                } catch (IllegalAccessException e) {
                    throw new IOException(e);
                } catch (InstantiationException e) {
                    throw new IOException(e);
                }
            }

            @Override
            public Object readUnshared() throws IOException, ClassNotFoundException {
                try {
                    return FSTObjectInput.this.readObjectInternal(referencee.getPossibleClasses()); // fixme
                } catch (IllegalAccessException e) {
                    throw new IOException(e);
                } catch (InstantiationException e) {
                    throw new IOException(e);
                }
            }

            @Override
            public void defaultReadObject() throws IOException, ClassNotFoundException {
                try {
                    FSTObjectInput.this.readObjectFields(referencee, clInfo, clInfo.compInfo.get(cl).getFieldArray(), toRead); // FIXME: only fields of current class
                } catch (IllegalAccessException e) {
                    throw new IOException(e);
                } catch (InstantiationException e) {
                    throw new IOException(e);
                }
            }

            HashMap<String, Object> fieldMap;

            @Override
            public GetField readFields() throws IOException, ClassNotFoundException {
                try {
                    FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = clInfo.compInfo.get(cl);
                    if (fstCompatibilityInfo.isAsymmetric()) {
                        fieldMap = new HashMap<String, Object>();
                        FSTObjectInput.this.readCompatibleObjectFields(referencee, clInfo, fstCompatibilityInfo.getFieldArray(), fieldMap);
                    } else {
                        fieldMap = (HashMap<String, Object>) FSTObjectInput.this.readObjectInternal(HashMap.class);
                    }
                } catch (IllegalAccessException e) {
                    throw new IOException(e);
                } catch (InstantiationException e) {
                    throw new IOException(e);
                }
                return new GetField() {
                    @Override
                    public ObjectStreamClass getObjectStreamClass() {
                        return ObjectStreamClass.lookup(cl);
                    }

                    @Override
                    public boolean defaulted(String name) throws IOException {
                        return fieldMap.get(name) == null;
                    }

                    @Override
                    public boolean get(String name, boolean val) throws IOException {
                        if (fieldMap.get(name) == null) {
                            return val;
                        }
                        return ((Boolean) fieldMap.get(name)).booleanValue();
                    }

                    @Override
                    public byte get(String name, byte val) throws IOException {
                        if (fieldMap.get(name) == null) {
                            return val;
                        }
                        return ((Byte) fieldMap.get(name)).byteValue();
                    }

                    @Override
                    public char get(String name, char val) throws IOException {
                        if (fieldMap.get(name) == null) {
                            return val;
                        }
                        return ((Character) fieldMap.get(name)).charValue();
                    }

                    @Override
                    public short get(String name, short val) throws IOException {
                        if (fieldMap.get(name) == null) {
                            return val;
                        }
                        return ((Short) fieldMap.get(name)).shortValue();
                    }

                    @Override
                    public int get(String name, int val) throws IOException {
                        if (fieldMap.get(name) == null) {
                            return val;
                        }
                        return ((Integer) fieldMap.get(name)).intValue();
                    }

                    @Override
                    public long get(String name, long val) throws IOException {
                        if (fieldMap.get(name) == null) {
                            return val;
                        }
                        return ((Long) fieldMap.get(name)).longValue();
                    }

                    @Override
                    public float get(String name, float val) throws IOException {
                        if (fieldMap.get(name) == null) {
                            return val;
                        }
                        return ((Float) fieldMap.get(name)).floatValue();
                    }

                    @Override
                    public double get(String name, double val) throws IOException {
                        if (fieldMap.get(name) == null) {
                            return val;
                        }
                        return ((Double) fieldMap.get(name)).doubleValue();
                    }

                    @Override
                    public Object get(String name, Object val) throws IOException {
                        Object res = fieldMap.get(name);
                        if (res == null) {
                            return val;
                        }
                        return res;
                    }
                };
            }

            @Override
            public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException, InvalidObjectException {
                if (callbacks == null) {
                    callbacks = new ArrayList<CallbackEntry>();
                }
                callbacks.add(new CallbackEntry(obj, prio));
            }

            @Override
            public int read() throws IOException {
                ensureReadAhead(1);
                return FSTObjectInput.this.readFByte();
            }

            @Override
            public int read(byte[] buf, int off, int len) throws IOException {
                ensureReadAhead(len);
                return FSTObjectInput.this.read(buf, off, len);
            }

            @Override
            public int available() throws IOException {
                return FSTObjectInput.this.available();
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public boolean readBoolean() throws IOException {
                ensureReadAhead(1);
                return FSTObjectInput.this.readBoolean();
            }

            @Override
            public byte readByte() throws IOException {
                ensureReadAhead(1);
                return FSTObjectInput.this.readFByte();
            }

            @Override
            public int readUnsignedByte() throws IOException {
                ensureReadAhead(1);
                return FSTObjectInput.this.readUnsignedByte();
            }

            @Override
            public char readChar() throws IOException {
                ensureReadAhead(2);
                return FSTObjectInput.this.readChar();
            }

            @Override
            public short readShort() throws IOException {
                ensureReadAhead(2);
                return FSTObjectInput.this.readShort();
            }

            @Override
            public int readUnsignedShort() throws IOException {
                ensureReadAhead(2);
                return FSTObjectInput.this.readUnsignedShort();
            }

            @Override
            public int readInt() throws IOException {
                return FSTObjectInput.this.readFInt();
            }

            @Override
            public long readLong() throws IOException {
                ensureReadAhead(8);
                return FSTObjectInput.this.readFLong();
            }

            @Override
            public float readFloat() throws IOException {
                ensureReadAhead(4);
                return FSTObjectInput.this.readFloat();
            }

            @Override
            public double readDouble() throws IOException {
                ensureReadAhead(8);
                return FSTObjectInput.this.readDouble();
            }

            @Override
            public void readFully(byte[] buf) throws IOException {
                ensureReadAhead(buf.length);
                FSTObjectInput.this.readFully(buf);
            }

            @Override
            public void readFully(byte[] buf, int off, int len) throws IOException {
                ensureReadAhead(len);
                FSTObjectInput.this.readFully(buf, off, len);
            }

            @Override
            public int skipBytes(int len) throws IOException {
                ensureReadAhead(len);
                return FSTObjectInput.this.skipBytes(len);
            }

            @Override
            public String readUTF() throws IOException {
                return FSTObjectInput.this.readStringUTF();
            }

            @Override
            public String readLine() throws IOException {
                ensureReadAhead(1000);
                return FSTObjectInput.this.readLine();
            }

            @Override
            public int read(byte[] b) throws IOException {
                ensureReadAhead(b.length);
                return FSTObjectInput.this.read(b);
            }

            @Override
            public long skip(long n) throws IOException {
                ensureReadAhead((int) n);
                return FSTObjectInput.this.skip(n);
            }

            @Override
            public void mark(int readlimit) {
                FSTObjectInput.this.mark(readlimit);
            }

            @Override
            public void reset() throws IOException {
                FSTObjectInput.this.reset();
            }

            @Override
            public boolean markSupported() {
                return FSTObjectInput.this.markSupported();
            }
        };
        if ( fakeWrapper == null ) {
            fakeWrapper = new MyObjectStream();
        }
        fakeWrapper.push(wrapped);
        return fakeWrapper;
    }

    static class MyObjectStream extends ObjectInputStream {

        ObjectInputStream wrapped;
        ObjectInputStream wrappedArr[] = new ObjectInputStream[30]; // if this is not sufficient use another lib ..
        int idx = 0;

        public void push( ObjectInputStream in ) {
            wrappedArr[idx++] = in;
            wrapped = in;
        }

        public void pop() {
            idx--;
            wrapped = wrappedArr[idx];
        }

        MyObjectStream() throws IOException, SecurityException {
            this.wrapped = wrapped;
        }

        @Override
        public Object readObjectOverride() throws IOException, ClassNotFoundException {
            return wrapped.readObject();
        }

        @Override
        public Object readUnshared() throws IOException, ClassNotFoundException {
            return wrapped.readUnshared();
        }

        @Override
        public void defaultReadObject() throws IOException, ClassNotFoundException {
            wrapped.defaultReadObject();
        }

        @Override
        public ObjectInputStream.GetField readFields() throws IOException, ClassNotFoundException {
            return wrapped.readFields();
        }

        @Override
        public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException, InvalidObjectException {
            wrapped.registerValidation(obj,prio);
        }

        @Override
        public int read() throws IOException {
            return wrapped.read();
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            return wrapped.read(buf, off, len);
        }

        @Override
        public int available() throws IOException {
            return wrapped.available();
        }

        @Override
        public void close() throws IOException {
            wrapped.close();
        }

        @Override
        public boolean readBoolean() throws IOException {
            return wrapped.readBoolean();
        }

        @Override
        public byte readByte() throws IOException {
            return wrapped.readByte();
        }

        @Override
        public int readUnsignedByte() throws IOException {
            return wrapped.readUnsignedByte();
        }

        @Override
        public char readChar() throws IOException {
            return wrapped.readChar();
        }

        @Override
        public short readShort() throws IOException {
            return wrapped.readShort();
        }

        @Override
        public int readUnsignedShort() throws IOException {
            return wrapped.readUnsignedShort();
        }

        @Override
        public int readInt() throws IOException {
            return wrapped.readInt();
        }

        @Override
        public long readLong() throws IOException {
            return wrapped.readLong();
        }

        @Override
        public float readFloat() throws IOException {
            return wrapped.readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            return wrapped.readDouble();
        }

        @Override
        public void readFully(byte[] buf) throws IOException {
            wrapped.readFully(buf);
        }

        @Override
        public void readFully(byte[] buf, int off, int len) throws IOException {
            wrapped.readFully(buf, off, len);
        }

        @Override
        public int skipBytes(int len) throws IOException {
            return wrapped.skipBytes(len);
        }

        @Override
        public String readUTF() throws IOException {
            return wrapped.readUTF();
        }

        @Override
        public String readLine() throws IOException {
            return wrapped.readLine();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return wrapped.read(b);
        }

        @Override
        public long skip(long n) throws IOException {
            return wrapped.skip(n);
        }

        @Override
        public void mark(int readlimit) {
            wrapped.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            wrapped.reset();
        }

        @Override
        public boolean markSupported() {
            return wrapped.markSupported();
        }
    }

    public FSTInputStream getInput() {
        return input;
    }
}
