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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 04.11.12
 * Time: 11:53
 */
/**
 * replacement of ObjectInputStream
 */
public class FSTObjectInput implements ObjectInput {

    static ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);
    
    protected FSTDecoder codec;

    FSTObjectRegistry objects;

    Stack<String> debugStack;
    int curDepth;

    ArrayList<CallbackEntry> callbacks;
    FSTConfiguration conf;
    // mirrored from conf
    boolean ignoreAnnotations;
    FSTClazzInfoRegistry clInfoRegistry;
    // done
    ConditionalCallback conditionalCallback;
    int readExternalReadAHead = 8000;
    
    public FSTConfiguration getConf() {
        return conf;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        codec.readPlainBytes(b,off,len);

    }

    @Override
    public int skipBytes(int n) throws IOException {
        codec.skip(n);
        return n;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return codec.readFByte() == 0 ? false : true;
    }

    @Override
    public byte readByte() throws IOException {
        return codec.readFByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return ((int)codec.readFByte()+256) & 0xff;
    }

    @Override
    public short readShort() throws IOException {
        return codec.readFShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return ((int)readShort()+65536) & 0xffff;
    }

    @Override
    public char readChar() throws IOException {
        return codec.readFChar();
    }

    @Override
    public int readInt() throws IOException {
        return codec.readFInt();
    }

    @Override
    public long readLong() throws IOException {
        return codec.readFLong();
    }

    @Override
    public float readFloat() throws IOException {
        return codec.readFFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return codec.readFDouble();
    }

    @Override
    public String readLine() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String readUTF() throws IOException {
        return codec.readStringUTF();
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
        this(in, FSTConfiguration.getDefaultConfiguration());
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
        codec = conf.createStreamDecoder();
        codec.setInputStream(in);
        this.conf = conf;
        initRegistries();
    }

    public Class getClassForName(String name) throws ClassNotFoundException {
        return codec.classForName(name);
    }

    void initRegistries() {
        ignoreAnnotations = conf.getCLInfoRegistry().isIgnoreAnnotations();
        clInfoRegistry = conf.getCLInfoRegistry();

        objects = (FSTObjectRegistry) conf.getCachedObject(FSTObjectRegistry.class);
        if (objects == null) {
            objects = new FSTObjectRegistry(conf);
        } else {
            objects.clearForRead();
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
            throw new IOException(e);
        }
    }

    @Override
    public int read() throws IOException {
        return codec.readFByte();
    }

    @Override
    public int read(byte[] b) throws IOException {
        codec.readPlainBytes(b,0,b.length);
        return b.length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        codec.readPlainBytes(b,off,len);
        return b.length;
    }

    @Override
    public long skip(long n) throws IOException {
        codec.skip((int) n);
        return n;
    }

    @Override
    public int available() throws IOException {
        return 0;
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

    public Object readObject(Class... possibles) throws Exception {
        curDepth++;
        try {
            if (possibles != null && possibles.length > 1 ) {
                for (int i = 0; i < possibles.length; i++) {
                    Class possible = possibles[i];
                    codec.registerClass(possible);
                }
            }
            Object res = readObjectInternal(possibles);
            processValidation();
            return res;
        } catch (Throwable th) {
            throw FSTUtil.rethrow(th);
        } finally {
            curDepth--;
        }
    }

    FSTClazzInfo.FSTFieldInfo infoCache;
    public Object readObjectInternal(Class... expected) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException {
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

    public Object readObjectWithHeader(FSTClazzInfo.FSTFieldInfo referencee) throws Exception {
        FSTClazzInfo clzSerInfo;
        Class c;
        final int readPos = codec.getInputPos();
        byte code = codec.readObjectHeaderTag();
        if (code == FSTObjectOutput.OBJECT ) {
            // class name
            clzSerInfo = readClass();
            c = clzSerInfo.getClazz();
            if ( c.isArray() )
                return readArrayNoHeader(referencee,readPos,c);
        } else if ( code == FSTObjectOutput.TYPED ) {
            c = referencee.getType();
            clzSerInfo = getClazzInfo(c, referencee);
        } else if ( code >= 1 ) {
            c = referencee.getPossibleClasses()[code - 1];
            clzSerInfo = getClazzInfo(c, referencee);
        } else {
            return instantiateSpecialTag(referencee, readPos, code);
        }
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

    private Object instantiateSpecialTag(FSTClazzInfo.FSTFieldInfo referencee, int readPos, byte code) throws Exception {
        if ( code == FSTObjectOutput.STRING ) { // faster than switch, note: currently string tag not used by all codecs ..
            String res = codec.readStringUTF();
            objects.registerObjectForRead(res, readPos);
            return res;
        } else if ( code == FSTObjectOutput.ENUM ) {
            return instantiateEnum(referencee, readPos);
        } else if ( code == FSTObjectOutput.NULL ) {
            return null;
        } else
        {
            switch (code) {
                case FSTObjectOutput.BIG_INT: { return instantiateBigInt(); }
                case FSTObjectOutput.BIG_LONG: { return new Long(codec.readFLong()); }
                case FSTObjectOutput.BIG_BOOLEAN_FALSE: { return Boolean.FALSE; }
                case FSTObjectOutput.BIG_BOOLEAN_TRUE: { return Boolean.TRUE; }
                case FSTObjectOutput.ONE_OF: { return referencee.getOneOf()[codec.readFByte()]; }
                case FSTObjectOutput.NULL: { return null; }
                case FSTObjectOutput.DIRECT_OBJECT: {
                    Object directObject = codec.getDirectObject();
                    objects.registerObjectForRead(directObject,readPos);
                    return directObject;
                }
                case FSTObjectOutput.STRING: return codec.readStringUTF();
                case FSTObjectOutput.HANDLE: { return instantiateHandle(referencee); }
                case FSTObjectOutput.ARRAY: { return instantiateArray(referencee, readPos); }
                case FSTObjectOutput.ENUM: { return instantiateEnum(referencee, readPos); }
            }
            throw new RuntimeException("unknown object tag "+code);
        }
    }

    private FSTClazzInfo getClazzInfo(Class c, FSTClazzInfo.FSTFieldInfo referencee) {
        FSTClazzInfo clzSerInfo;
        FSTClazzInfo lastInfo = referencee.lastInfo;
        if ( lastInfo != null && lastInfo.clazz == c) {
            clzSerInfo = lastInfo;
        } else {
            clzSerInfo = clInfoRegistry.getCLInfo(c);
        }
        return clzSerInfo;
    }

    private Object instantiateHandle(FSTClazzInfo.FSTFieldInfo referencee) throws IOException {
        int handle = codec.readFInt();
        Object res = objects.getReadRegisteredObject(handle);
        if (res == null) {
            throw new IOException("unable to ressolve handle " + handle + " " + referencee.getDesc() + " " + codec.getInputPos() );
        }
        return res;
    }

    private Object instantiateArray(FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws Exception {
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
        int ordinal = codec.readFInt();
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
        int val = codec.readFInt();
        if (val >= 0 && val < FSTConfiguration.intObjects.length) {
            return FSTConfiguration.intObjects[val];
        }
        return new Integer(val);
    }

    private Object instantiateAndReadWithSer(Class c, FSTObjectSerializer ser, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        boolean serInstance = false;
        Object newObj = ser.instantiate(c, this, clzSerInfo, referencee, readPos);
        if (newObj == null) {
            newObj = clzSerInfo.newInstance(codec.isMapBased());
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
        codec.consumeEndMarker();
        return newObj;
    }

    protected Object instantiateAndReadNoSer(Class c, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws Exception {
        Object newObj;
        newObj = clzSerInfo.newInstance(codec.isMapBased());
        if (newObj == null) {
            throw new IOException(referencee.getDesc() + ":Failed to instantiate '" + c.getName() + "'. Register a custom serializer implementing instantiate.");
        }
        if ( ! referencee.isFlat() && ! clzSerInfo.isFlat() ) {
            objects.registerObjectForRead(newObj, readPos);
        }
        if ( clzSerInfo.isExternalizable() )
        {
            codec.ensureReadAhead(readExternalReadAHead);
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


    protected Object readObjectCompatible(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, Object newObj) throws Exception {
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
            newObj = rep;//FIXME: support this in call
        }
        return newObj;
    }

    protected void readObjectCompatibleRecursive(FSTClazzInfo.FSTFieldInfo referencee, Object toRead, FSTClazzInfo serializationInfo, Class cl) throws Exception {
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
    {
        try {
            readObjectFields(referencee,serializationInfo,serializationInfo.getFieldInfo(),newObj);
        } catch (Exception e) {
            FSTUtil.rethrow(e);
        }
    }

    void readObjectFields(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, Object newObj) throws Exception {
        
        if ( codec.isMapBased() ) {
            readFieldsMapBased(referencee, serializationInfo, newObj);
            return;
        }
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
                            booleanMask = ((int) codec.readFByte() + 256) &0xff;
                            boolcount = 0;
                        }
                        boolean val = (booleanMask & 128) != 0;
                        booleanMask = booleanMask << 1;
                        boolcount++;
                        subInfo.setBooleanValue(newObj, val);
                    } else {
                        switch (integralType) {
                            case FSTClazzInfo.FSTFieldInfo.BYTE:   subInfo.setByteValue(newObj, codec.readFByte()); break;
                            case FSTClazzInfo.FSTFieldInfo.CHAR:   subInfo.setCharValue(newObj, codec.readFChar()); break;
                            case FSTClazzInfo.FSTFieldInfo.SHORT:  subInfo.setShortValue(newObj, codec.readFShort()); break;
                            case FSTClazzInfo.FSTFieldInfo.INT:    subInfo.setIntValue(newObj, codec.readFInt()); break;
                            case FSTClazzInfo.FSTFieldInfo.LONG:   subInfo.setLongValue(newObj, codec.readFLong()); break;
                            case FSTClazzInfo.FSTFieldInfo.FLOAT:  subInfo.setFloatValue(newObj, codec.readFFloat()); break;
                            case FSTClazzInfo.FSTFieldInfo.DOUBLE: subInfo.setDoubleValue(newObj, codec.readFDouble()); break;
                        }
                    }
                } else {
                    if ( subInfo.isConditional() ) {
                        if ( conditional == 0 ) {
                            conditional = codec.readPlainInt();
                            if ( skipConditional(newObj, conditional, subInfo) ) {
                                codec.moveTo(conditional);
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

    protected void readFieldsMapBased(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, Object newObj) throws Exception {
        String name; 
        int len = codec.getObjectHeaderLen();
        if ( len < 0 )
            len = Integer.MAX_VALUE;
        int count = 0;
        while( count < len ) {
            name=codec.readStringUTF();
//            System.out.println("read name "+name);
            count++;
            FSTClazzInfo.FSTFieldInfo fieldInfo = serializationInfo.getFieldInfo(name,null);
            if ( fieldInfo == null ) {
                System.out.println("warning: unknown field: "+name+" on class "+serializationInfo.getClazz().getName());
            } else {
                if ( fieldInfo.isPrimitive() ) {
                    // direct primitive field
                    switch ( fieldInfo.getIntegralType() ) {
                        case FSTClazzInfo.FSTFieldInfo.BOOL:   fieldInfo.setBooleanValue(newObj, codec.readFByte() == 0 ? false:true); break;
                        case FSTClazzInfo.FSTFieldInfo.BYTE:   fieldInfo.setByteValue(newObj, codec.readFByte()); break;
                        case FSTClazzInfo.FSTFieldInfo.CHAR:   fieldInfo.setCharValue(newObj, codec.readFChar()); break;
                        case FSTClazzInfo.FSTFieldInfo.SHORT:  fieldInfo.setShortValue(newObj, codec.readFShort()); break;
                        case FSTClazzInfo.FSTFieldInfo.INT:    fieldInfo.setIntValue(newObj, codec.readFInt()); break;
                        case FSTClazzInfo.FSTFieldInfo.LONG:   fieldInfo.setLongValue(newObj, codec.readFLong()); break;
                        case FSTClazzInfo.FSTFieldInfo.FLOAT:  fieldInfo.setFloatValue(newObj, codec.readFFloat()); break;
                        case FSTClazzInfo.FSTFieldInfo.DOUBLE: fieldInfo.setDoubleValue(newObj, codec.readFDouble()); break;
                        default: throw new RuntimeException("unkown primitive type "+fieldInfo);
                    }
//                } else if ( fieldInfo.isArray() && fieldInfo.getType().getComponentType().isPrimitive() ) {
//                    Object arr = codec.readFPrimitiveArray(null, fieldInfo.getType().getComponentType(), -1);
//                    fieldInfo.setObjectValue(newObj,arr); // fixme: ref lookup
                } else {
                    Object toSet = readObjectWithHeader(fieldInfo);
                    fieldInfo.setObjectValue(newObj,toSet);
                }
            }
        }
    }

    private boolean skipConditional(Object newObj, int conditional, FSTClazzInfo.FSTFieldInfo subInfo) {
        if ( conditionalCallback != null ) {
            return conditionalCallback.shouldSkip(newObj,conditional,subInfo.getField());
        }
        return false;
    }

    protected void readCompatibleObjectFields(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, Map res) throws Exception {
        int booleanMask = 0;
        int boolcount = 8;
        for (int i = 0; i < fieldInfo.length; i++) {
            try {
                FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if (subInfo.isIntegral() && !subInfo.isArray()) {
                    final Class subInfoType = subInfo.getType();
                    if (subInfoType == boolean.class) {
                        if (boolcount == 8) {
                            booleanMask = ((int) codec.readFByte() + 256) &0xff;
                            boolcount = 0;
                        }
                        boolean val = (booleanMask & 128) != 0;
                        booleanMask = booleanMask << 1;
                        boolcount++;
                        res.put(subInfo.getField().getName(), val);
                    }
                    if (subInfoType == byte.class) {
                        res.put(subInfo.getField().getName(), codec.readFByte());
                    } else if (subInfoType == char.class) {
                        res.put(subInfo.getField().getName(), codec.readFChar());
                    } else if (subInfoType == short.class) {
                        res.put(subInfo.getField().getName(), codec.readFShort());
                    } else if (subInfoType == int.class) {
                        res.put(subInfo.getField().getName(), codec.readFInt());
                    } else if (subInfoType == double.class) {
                        res.put(subInfo.getField().getName(), codec.readFDouble());
                    } else if (subInfoType == float.class) {
                        res.put(subInfo.getField().getName(), codec.readFFloat());
                    } else if (subInfoType == long.class) {
                        res.put(subInfo.getField().getName(), codec.readFLong());
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
        codec.ensureReadAhead(bytes);
    }

    public String readStringUTF() throws IOException {
        return codec.readStringUTF();
    }

    /**
     * len < 127 !!!!!
     * @return
     * @throws IOException
     */
    public String readStringAsc() throws IOException {
        return codec.readStringAsc();
    }

    protected Object readArray(FSTClazzInfo.FSTFieldInfo referencee) throws Exception {
        int pos = codec.getInputPos();
        Class arrCl = codec.readArrayHeader();
        if ( arrCl == null )
            return null;
        return readArrayNoHeader(referencee, pos, arrCl);
    }

    private Object readArrayNoHeader(FSTClazzInfo.FSTFieldInfo referencee, int pos, Class arrCl) throws Exception {
        final int len = codec.readFInt();
        if (len == -1) {
            return null;
        }
        Class arrType = arrCl.getComponentType();
        if (!arrCl.getComponentType().isArray()) {
            Object array = Array.newInstance(arrType, len);
            if ( ! referencee.isFlat() )
                objects.registerObjectForRead(array, pos );
            if (arrCl.getComponentType().isPrimitive()) {
                return codec.readFPrimitiveArray(array, arrType,len);
            } else { // Object Array
                Object arr[] = (Object[]) array;
                for (int i = 0; i < len; i++) {
                    Object value = readObjectWithHeader(referencee);
                    arr[i] = value;
                }
            }
            return array;
        } else { // multidim array
            Object array[] = (Object[]) Array.newInstance(arrType, len);
            if ( ! referencee.isFlat() ) {
                objects.registerObjectForRead(array, pos);
            }
            FSTClazzInfo.FSTFieldInfo ref1 = new FSTClazzInfo.FSTFieldInfo(referencee.getPossibleClasses(), null, clInfoRegistry.isIgnoreAnnotations());
            for (int i = 0; i < len; i++) {
                Object subArray = readArray(ref1);
                array[i] = subArray;
            }
            return array;
        }
    }

    public void registerObject(Object o, int streamPosition, FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo referencee) {
        if ( ! objects.disabled && !referencee.isFlat() && (info == null || ! info.isFlat() ) ) {
            objects.registerObjectForRead(o, streamPosition);
        }
    }

    public FSTClazzInfo readClass() throws IOException, ClassNotFoundException {
        return codec.readClass();
    }

    void resetAndClearRefs() {
        try {
            reset();
            objects.clearForRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset() throws IOException {
        codec.reset();
    }

    public void resetForReuse(InputStream in) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        codec.reset();
        codec.setInputStream(in);
        objects.clearForRead(); 
    }

    public void resetForReuseCopyArray(byte bytes[], int off, int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        codec.reset();
        objects.clearForRead();
        codec.resetToCopyOf(bytes, off, len);
    }

    public void resetForReuseUseArray(byte bytes[]) throws IOException {
        resetForReuseUseArray(bytes, bytes.length);
    }

    public void resetForReuseUseArray(byte bytes[], int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        objects.clearForRead();
        codec.resetWith(bytes, len);
    }

    public final int readFInt() throws IOException {
        return codec.readFInt();
    }

    boolean closed = false;
    @Override
    public void close() throws IOException {
        closed = true;
        resetAndClearRefs();
        conf.returnObject(objects);
        codec.close();
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
                } catch (Exception e) {
                    FSTUtil.rethrow(e);
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
                } catch (Exception e) {
                    FSTUtil.rethrow(e);
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
                codec.ensureReadAhead(1);
                return codec.readFByte();
            }

            @Override
            public int read(byte[] buf, int off, int len) throws IOException {
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
                return FSTObjectInput.this.readBoolean();
            }

            @Override
            public byte readByte() throws IOException {
                return codec.readFByte();
            }

            @Override
            public int readUnsignedByte() throws IOException {
                return FSTObjectInput.this.readUnsignedByte();
            }

            @Override
            public char readChar() throws IOException {
                return codec.readFChar();
            }

            @Override
            public short readShort() throws IOException {
                return codec.readFShort();
            }

            @Override
            public int readUnsignedShort() throws IOException {
                return FSTObjectInput.this.readUnsignedShort();
            }

            @Override
            public int readInt() throws IOException {
                return codec.readFInt();
            }

            @Override
            public long readLong() throws IOException {
                return codec.readFLong();
            }

            @Override
            public float readFloat() throws IOException {
                return codec.readFFloat();
            }

            @Override
            public double readDouble() throws IOException {
                return codec.readFDouble();
            }

            @Override
            public void readFully(byte[] buf) throws IOException {
                FSTObjectInput.this.readFully(buf);
            }

            @Override
            public void readFully(byte[] buf, int off, int len) throws IOException {
                FSTObjectInput.this.readFully(buf, off, len);
            }

            @Override
            public int skipBytes(int len) throws IOException {
                return FSTObjectInput.this.skipBytes(len);
            }

            @Override
            public String readUTF() throws IOException {
                return codec.readStringUTF();
            }

            @Override
            public String readLine() throws IOException {
                return FSTObjectInput.this.readLine();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return FSTObjectInput.this.read(b);
            }

            @Override
            public long skip(long n) throws IOException {
                return FSTObjectInput.this.skip(n);
            }

            @Override
            public void mark(int readlimit) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public void reset() throws IOException {
                FSTObjectInput.this.reset();
            }

            @Override
            public boolean markSupported() {
                return false;
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

}
