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
import java.lang.reflect.*;
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

    public static ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);
    
    protected FSTDecoder codec;

    FSTObjectRegistry objects;

    Stack<String> debugStack;
    int curDepth;

    ArrayList<CallbackEntry> callbacks;
//    FSTConfiguration conf;
    // mirrored from conf
    boolean ignoreAnnotations;
    FSTClazzInfoRegistry clInfoRegistry;
    // done
    ConditionalCallback conditionalCallback;
    int readExternalReadAHead = 8000;
    VersionConflictListener versionConflictListener;

    FSTConfiguration conf;

    // copied values from conf
    boolean isCrossPlatform;

    public FSTConfiguration getConf() {
        return conf;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        getCodec().readPlainBytes(b, off, len);

    }

    @Override
    public int skipBytes(int n) throws IOException {
        getCodec().skip(n);
        return n;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return getCodec().readFByte() == 0 ? false : true;
    }

    @Override
    public byte readByte() throws IOException {
        return getCodec().readFByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return ((int) getCodec().readFByte()+256) & 0xff;
    }

    @Override
    public short readShort() throws IOException {
        return getCodec().readFShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return ((int)readShort()+65536) & 0xffff;
    }

    @Override
    public char readChar() throws IOException {
        return getCodec().readFChar();
    }

    @Override
    public int readInt() throws IOException {
        return getCodec().readFInt();
    }

    @Override
    public long readLong() throws IOException {
        return getCodec().readFLong();
    }

    @Override
    public float readFloat() throws IOException {
        return getCodec().readFFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return getCodec().readFDouble();
    }

    @Override
    public String readLine() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String readUTF() throws IOException {
        return getCodec().readStringUTF();
    }

    public FSTDecoder getCodec() {
        return codec;
    }

    void setCodec(FSTDecoder codec) {
        this.codec = codec;
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
        this(emptyStream, FSTConfiguration.getDefaultConfiguration());
    }

    public FSTObjectInput(FSTConfiguration conf) {
        this(emptyStream, conf);
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
        setCodec(conf.createStreamDecoder());
        getCodec().setInputStream(in);
        isCrossPlatform = conf.isCrossPlatform();
        initRegistries(conf);
        this.conf = conf;
    }

    public Class getClassForName(String name) throws ClassNotFoundException {
        return getCodec().classForName(name);
    }

    void initRegistries(FSTConfiguration conf) {
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
        return getCodec().readFByte();
    }

    @Override
    public int read(byte[] b) throws IOException {
        getCodec().readPlainBytes(b, 0, b.length);
        return b.length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        getCodec().readPlainBytes(b, off, len);
        return b.length;
    }

    @Override
    public long skip(long n) throws IOException {
        getCodec().skip((int) n);
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
        if ( isCrossPlatform ) {
            return readObjectInternal(null); // not supported cross platform
        }
        try {
            if (possibles != null && possibles.length > 1 ) {
                for (int i = 0; i < possibles.length; i++) {
                    Class possible = possibles[i];
                    getCodec().registerClass(possible);
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
        final int readPos = getCodec().getInputPos();
        byte code = getCodec().readObjectHeaderTag();
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
            String res = getCodec().readStringUTF();
            objects.registerObjectForRead(res, readPos);
            return res;
        } else if ( code == FSTObjectOutput.BIG_INT ) {
            return instantiateBigInt();
        } else if ( code == FSTObjectOutput.NULL ) {
            return null;
        } else
        {
            switch (code) {
                case FSTObjectOutput.BIG_INT: { return instantiateBigInt(); }
                case FSTObjectOutput.BIG_LONG: { return Long.valueOf(getCodec().readFLong()); }
                case FSTObjectOutput.BIG_BOOLEAN_FALSE: { return Boolean.FALSE; }
                case FSTObjectOutput.BIG_BOOLEAN_TRUE: { return Boolean.TRUE; }
                case FSTObjectOutput.ONE_OF: { return referencee.getOneOf()[getCodec().readFByte()]; }
                case FSTObjectOutput.NULL: { return null; }
                case FSTObjectOutput.DIRECT_OBJECT: {
                    Object directObject = getCodec().getDirectObject();
                    if (directObject.getClass() == byte[].class) { // fixme. special for minibin, move it there
                        if ( referencee != null && referencee.getType() == boolean[].class )
                        {
                            byte[] ba = (byte[]) directObject;
                            boolean res[] = new boolean[ba.length];
                            for (int i = 0; i < res.length; i++) {
                                res[i] = ba[i] != 0;
                            }
                            directObject = res;
                        }
                    }
                    objects.registerObjectForRead(directObject,readPos);
                    return directObject;
                }
                case FSTObjectOutput.STRING: return getCodec().readStringUTF();
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
        int handle = getCodec().readFInt();
        Object res = objects.getReadRegisteredObject(handle);
        if (res == null) {
            throw new IOException("unable to ressolve handle " + handle + " " + referencee.getDesc() + " " + getCodec().getInputPos() );
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
        int ordinal = getCodec().readFInt();
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
        int val = getCodec().readFInt();
        return Integer.valueOf(val);
    }

    private Object instantiateAndReadWithSer(Class c, FSTObjectSerializer ser, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        boolean serInstance = false;
        Object newObj = ser.instantiate(c, this, clzSerInfo, referencee, readPos);
        if (newObj == null) {
            newObj = clzSerInfo.newInstance(getCodec().isMapBased());
        } else
            serInstance = true;
        if (newObj == null) {
            throw new IOException(referencee.getDesc() + ":Failed to instantiate '" + c.getName() + "'. Register a custom serializer implementing instantiate.");
        }
        if (newObj.getClass() != c && ser == null ) {
            // for advanced trickery (e.g. returning non-serializable from FSTSerializer)
            // this hurts. so in case of FSTSerializers incoming clzInfo will refer to the
            // original class, not the one actually instantiated
            c = newObj.getClass();
            clzSerInfo = clInfoRegistry.getCLInfo(c);
        }
        if ( ! referencee.isFlat() && ! clzSerInfo.isFlat() && !ser.alwaysCopy()) {
            objects.registerObjectForRead(newObj, readPos);
        }
        if ( !serInstance )
            ser.readObject(this, newObj, clzSerInfo, referencee);
        getCodec().consumeEndMarker(); //=> bug when writing objects unlimited
        return newObj;
    }

    protected Object instantiateAndReadNoSer(Class c, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws Exception {
        Object newObj;
        newObj = clzSerInfo.newInstance(getCodec().isMapBased());
        if (newObj == null) {
            throw new IOException(referencee.getDesc() + ":Failed to instantiate '" + c.getName() + "'. Register a custom serializer implementing instantiate.");
        }
        final boolean needsRefLookup = !referencee.isFlat() && !clzSerInfo.isFlat();
        if (needsRefLookup) {
            objects.registerObjectForRead(newObj, readPos);
        }
        if ( clzSerInfo.isExternalizable() )
        {
            int tmp = readPos;
            getCodec().ensureReadAhead(readExternalReadAHead);
            ((Externalizable)newObj).readExternal(this);
            getCodec().readExternalEnd();
            if ( clzSerInfo.getReadResolveMethod() != null ) {
                final Object prevNew = newObj;
                newObj = handleReadRessolve(clzSerInfo, newObj);
                if ( newObj != prevNew && needsRefLookup ) {
                    objects.replace(prevNew, newObj, tmp);
                }
            }
        } else if (clzSerInfo.useCompatibleMode())
        {
            Object replaced = readObjectCompatible(referencee, clzSerInfo, newObj);
            if (replaced != null && replaced != newObj) {
                objects.replace(newObj, replaced, readPos);
                newObj = replaced;
            }
        } else {
            FSTClazzInfo.FSTFieldInfo[] fieldInfo = clzSerInfo.getFieldInfo();
            readObjectFields(referencee, clzSerInfo, fieldInfo, newObj,0,0);
        }
        return newObj;
    }


    protected Object readObjectCompatible(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, Object newObj) throws Exception {
        Class cl = serializationInfo.getClazz();
        readObjectCompatibleRecursive(referencee, newObj, serializationInfo, cl);
        if (newObj != null &&
                serializationInfo.getReadResolveMethod() != null) {
            newObj = handleReadRessolve(serializationInfo, newObj);
        }
        return newObj;
    }

    private Object handleReadRessolve(FSTClazzInfo serializationInfo, Object newObj) throws IllegalAccessException {
        Object rep = null;
        try {
            rep = serializationInfo.getReadResolveMethod().invoke(newObj);
        } catch (InvocationTargetException e) {
            throw FSTUtil.rethrow(e);
        }
        newObj = rep;//FIXME: support this in call
        return newObj;
    }

    protected void readObjectCompatibleRecursive(FSTClazzInfo.FSTFieldInfo referencee, Object toRead, FSTClazzInfo serializationInfo, Class cl) throws Exception {
        FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = serializationInfo.compInfo.get(cl);
        if (!Serializable.class.isAssignableFrom(cl)) {
            return; // ok here, as compatible mode will never be triggered for "forceSerializable"
        }
        readObjectCompatibleRecursive(referencee, toRead, serializationInfo, cl.getSuperclass());
        if (fstCompatibilityInfo != null && fstCompatibilityInfo.getReadMethod() != null) {
            try {
                int tag = readByte(); // expect 55
                if ( tag == 66 ) {
                    // no write method defined, but read method defined ...
                    // expect defaultReadObject
                    getCodec().moveTo(getCodec().getInputPos() - 1); // need to push back tag, cause defaultWriteObject on writer side does not write tag
//                    input.pos--;
                }
                ObjectInputStream objectInputStream = getObjectInputStream(cl, serializationInfo, referencee, toRead);
                fstCompatibilityInfo.getReadMethod().invoke(toRead, objectInputStream);
                fakeWrapper.pop();
            } catch (Exception e) {
                throw FSTUtil.rethrow(e);
            }
        } else {
            if (fstCompatibilityInfo != null) {
                int tag = readByte();
                if ( tag == 55 )
                {
                    // came from writeMethod, but no readMethod defined => assume defaultWriteObject
                    tag = readByte(); // consume tag of defaultwriteobject (99)
                    if ( tag == 77 ) // came from putfield
                    {
                        HashMap<String, Object> fieldMap = (HashMap<String, Object>) FSTObjectInput.this.readObjectInternal(HashMap.class);
                        final FSTClazzInfo.FSTFieldInfo[] fieldArray = fstCompatibilityInfo.getFieldArray();
                        for (int i = 0; i < fieldArray.length; i++) {
                            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldArray[i];
                            final Object val = fieldMap.get(fstFieldInfo.getField().getName());
                            if ( val != null ) {
                                fstFieldInfo.setObjectValue(toRead,val);
                            }
                        }
                        return;
                    }
                }
                readObjectFields(referencee, serializationInfo, fstCompatibilityInfo.getFieldArray(), toRead,0,0);
            }
        }
    }

    public void defaultReadObject(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, Object newObj)
    {
        try {
            readObjectFields(referencee,serializationInfo,serializationInfo.getFieldInfo(),newObj,0,0);
        } catch (Exception e) {
            throw FSTUtil.rethrow(e);
        }
    }

    void readObjectFields(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, Object newObj, int startIndex, int version) throws Exception {
        
        if ( getCodec().isMapBased() ) {
            readFieldsMapBased(referencee, serializationInfo, newObj);
            return;
        }
        int booleanMask = 0;
        int boolcount = 8;
        final int length = fieldInfo.length;
        int conditional = 0;
        for (int i = startIndex; i < length; i++) {
            try {
                FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if (subInfo.getVersion() > version ) {
                    int nextVersion = getCodec().readVersionTag();
                    if ( nextVersion == 0 ) // old object read
                    {
                        oldVersionRead(newObj);
                        return;
                    }
                    if ( nextVersion != subInfo.getVersion() ) {
                        throw new RuntimeException("read version tag "+nextVersion+" fieldInfo has "+subInfo.getVersion());
                    }
                    readObjectFields(referencee,serializationInfo,fieldInfo,newObj,i,nextVersion);
                    return;
                }
                if (subInfo.isPrimitive()) {
                    int integralType = subInfo.getIntegralType();
                    if (integralType == FSTClazzInfo.FSTFieldInfo.BOOL) {
                        if (boolcount == 8) {
                            booleanMask = ((int) getCodec().readFByte() + 256) &0xff;
                            boolcount = 0;
                        }
                        boolean val = (booleanMask & 128) != 0;
                        booleanMask = booleanMask << 1;
                        boolcount++;
                        subInfo.setBooleanValue(newObj, val);
                    } else {
                        switch (integralType) {
                            case FSTClazzInfo.FSTFieldInfo.BYTE:   subInfo.setByteValue(newObj, getCodec().readFByte()); break;
                            case FSTClazzInfo.FSTFieldInfo.CHAR:   subInfo.setCharValue(newObj, getCodec().readFChar()); break;
                            case FSTClazzInfo.FSTFieldInfo.SHORT:  subInfo.setShortValue(newObj, getCodec().readFShort()); break;
                            case FSTClazzInfo.FSTFieldInfo.INT:    subInfo.setIntValue(newObj, getCodec().readFInt()); break;
                            case FSTClazzInfo.FSTFieldInfo.LONG:   subInfo.setLongValue(newObj, getCodec().readFLong()); break;
                            case FSTClazzInfo.FSTFieldInfo.FLOAT:  subInfo.setFloatValue(newObj, getCodec().readFFloat()); break;
                            case FSTClazzInfo.FSTFieldInfo.DOUBLE: subInfo.setDoubleValue(newObj, getCodec().readFDouble()); break;
                        }
                    }
                } else {
                    if ( subInfo.isConditional() ) {
                        if ( conditional == 0 ) {
                            conditional = getCodec().readPlainInt();
                            if ( skipConditional(newObj, conditional, subInfo) ) {
                                getCodec().moveTo(conditional);
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
        getCodec().readVersionTag(); // just consume '0'
    }

    public VersionConflictListener getVersionConflictListener() {
        return versionConflictListener;
    }

    /**
     * see @Version annotation
     * @param versionConflictListener
     */
    public void setVersionConflictListener(VersionConflictListener versionConflictListener) {
        this.versionConflictListener = versionConflictListener;
    }

    protected void oldVersionRead(Object newObj) {
        if ( versionConflictListener != null )
            versionConflictListener.onOldVersionRead(newObj);
    }

    protected void readFieldsMapBased(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, Object newObj) throws Exception {
        String name; 
        int len = getCodec().getObjectHeaderLen();
        if ( len < 0 )
            len = Integer.MAX_VALUE;
        int count = 0;
        while( count < len ) {
            name= getCodec().readStringUTF();
            if ( len == Integer.MAX_VALUE && getCodec().isEndMarker(name) )
                return;
            count++;
            FSTClazzInfo.FSTFieldInfo fieldInfo = serializationInfo.getFieldInfo(name, null);
            if (fieldInfo == null) {
                System.out.println("warning: unknown field: " + name + " on class " + serializationInfo.getClazz().getName());
            } else {
                if (fieldInfo.isPrimitive()) {
                    // direct primitive field
                    switch (fieldInfo.getIntegralType()) {
                        case FSTClazzInfo.FSTFieldInfo.BOOL:
                            fieldInfo.setBooleanValue(newObj, getCodec().readFByte() == 0 ? false : true);
                            break;
                        case FSTClazzInfo.FSTFieldInfo.BYTE:
                            fieldInfo.setByteValue(newObj, getCodec().readFByte());
                            break;
                        case FSTClazzInfo.FSTFieldInfo.CHAR:
                            fieldInfo.setCharValue(newObj, getCodec().readFChar());
                            break;
                        case FSTClazzInfo.FSTFieldInfo.SHORT:
                            fieldInfo.setShortValue(newObj, getCodec().readFShort());
                            break;
                        case FSTClazzInfo.FSTFieldInfo.INT:
                            fieldInfo.setIntValue(newObj, getCodec().readFInt());
                            break;
                        case FSTClazzInfo.FSTFieldInfo.LONG:
                            fieldInfo.setLongValue(newObj, getCodec().readFLong());
                            break;
                        case FSTClazzInfo.FSTFieldInfo.FLOAT:
                            fieldInfo.setFloatValue(newObj, getCodec().readFFloat());
                            break;
                        case FSTClazzInfo.FSTFieldInfo.DOUBLE:
                            fieldInfo.setDoubleValue(newObj, getCodec().readFDouble());
                            break;
                        default:
                            throw new RuntimeException("unkown primitive type " + fieldInfo);
                    }
//                } else if ( fieldInfo.isArray() && fieldInfo.getType().getComponentType().isPrimitive() ) {
//                    Object arr = codec.readFPrimitiveArray(null, fieldInfo.getType().getComponentType(), -1);
//                    fieldInfo.setObjectValue(newObj,arr); // fixme: ref lookup
                } else {
                    Object toSet = readObjectWithHeader(fieldInfo);
                    fieldInfo.setObjectValue(newObj, toSet);
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
                            booleanMask = ((int) getCodec().readFByte() + 256) &0xff;
                            boolcount = 0;
                        }
                        boolean val = (booleanMask & 128) != 0;
                        booleanMask = booleanMask << 1;
                        boolcount++;
                        res.put(subInfo.getField().getName(), val);
                    }
                    if (subInfoType == byte.class) {
                        res.put(subInfo.getField().getName(), getCodec().readFByte());
                    } else if (subInfoType == char.class) {
                        res.put(subInfo.getField().getName(), getCodec().readFChar());
                    } else if (subInfoType == short.class) {
                        res.put(subInfo.getField().getName(), getCodec().readFShort());
                    } else if (subInfoType == int.class) {
                        res.put(subInfo.getField().getName(), getCodec().readFInt());
                    } else if (subInfoType == double.class) {
                        res.put(subInfo.getField().getName(), getCodec().readFDouble());
                    } else if (subInfoType == float.class) {
                        res.put(subInfo.getField().getName(), getCodec().readFFloat());
                    } else if (subInfoType == long.class) {
                        res.put(subInfo.getField().getName(), getCodec().readFLong());
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

    public String readStringUTF() throws IOException {
        return getCodec().readStringUTF();
    }

    /**
     * len < 127 !!!!!
     * @return
     * @throws IOException
     */
    public String readStringAsc() throws IOException {
        return getCodec().readStringAsc();
    }

    protected Object readArray(FSTClazzInfo.FSTFieldInfo referencee) throws Exception {
        int pos = getCodec().getInputPos();
        Class arrCl = getCodec().readArrayHeader();
        if ( arrCl == null )
            return null;
        return readArrayNoHeader(referencee, pos, arrCl);
    }

    private Object readArrayNoHeader(FSTClazzInfo.FSTFieldInfo referencee, int pos, Class arrCl) throws Exception {
        final int len = getCodec().readFInt();
        if (len == -1) {
            return null;
        }
        Class arrType = arrCl.getComponentType();
        if (!arrCl.getComponentType().isArray()) {
            Object array = Array.newInstance(arrType, len);
            if ( ! referencee.isFlat() )
                objects.registerObjectForRead(array, pos );
            if (arrCl.getComponentType().isPrimitive()) {
                return getCodec().readFPrimitiveArray(array, arrType, len);
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
        return getCodec().readClass();
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
        getCodec().reset();
    }

    public void resetForReuse(InputStream in) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        getCodec().reset();
        getCodec().setInputStream(in);
        objects.clearForRead(); 
    }

    public void resetForReuseCopyArray(byte bytes[], int off, int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        getCodec().reset();
        objects.clearForRead();
        getCodec().resetToCopyOf(bytes, off, len);
    }

    public void resetForReuseUseArray(byte bytes[]) throws IOException {
        resetForReuseUseArray(bytes, bytes.length);
    }

    public void resetForReuseUseArray(byte bytes[], int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        objects.clearForRead();
        getCodec().resetWith(bytes, len);
    }

    public final int readFInt() throws IOException {
        return getCodec().readFInt();
    }

    boolean closed = false;
    @Override
    public void close() throws IOException {
        closed = true;
        resetAndClearRefs();
        conf.returnObject(objects);
        getCodec().close();
    }

    ////////////////////////////////////////////////////// epic compatibility hack /////////////////////////////////////////////////////////

    MyObjectStream fakeWrapper; // some jdk classes hash for ObjectStream, so provide the same instance always

    ObjectInputStream getObjectInputStream(final Class cl, final FSTClazzInfo clInfo, final FSTClazzInfo.FSTFieldInfo referencee, final Object toRead) throws IOException {
        ObjectInputStream wrapped = new ObjectInputStream() {
            @Override
            public Object readObjectOverride() throws IOException, ClassNotFoundException {
                try {
                    byte b = FSTObjectInput.this.readByte();
                    if ( b != FSTObjectOutput.SPECIAL_COMPATIBILITY_OBJECT_TAG ) {
                        Constructor<?>[] constructors = OptionalDataException.class.getDeclaredConstructors();
                        FSTObjectInput.this.pushBack(1);
                        for (int i = 0; i < constructors.length; i++) {
                            Constructor constructor = constructors[i];
                            TypeVariable[] typeParameters = constructor.getTypeParameters();
                            if ( typeParameters != null && typeParameters.length == 1 && constructor.getParameterTypes()[0] == int.class) {
                                constructor.setAccessible(true);
                                OptionalDataException ode;
                                try {
                                    ode = (OptionalDataException) constructor.newInstance(0);
                                    throw ode;
                                } catch (InvocationTargetException e) {
                                    break;
                                }
                            }
                        }
                        throw new EOFException("if your code relies on this, think");
                    }
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
                    int tag = readByte();
                    if ( tag == 77 ) // came from writeFields
                    {
                        fieldMap = (HashMap<String, Object>) FSTObjectInput.this.readObjectInternal(HashMap.class);
                    } else {
                        FSTObjectInput.this.readObjectFields(
                                referencee,
                                clInfo,
                                clInfo.compInfo.get(cl).getFieldArray(),
                                toRead,
                                0,
                                0
                        ); // FIXME: only fields of current class
                    }
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }

            HashMap<String, Object> fieldMap;

            @Override
            public GetField readFields() throws IOException, ClassNotFoundException {
                int tag = readByte();
                try {
                    FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = clInfo.compInfo.get(cl);
                    if (tag==99) { // came from defaultwriteobject
                        fieldMap = new HashMap<String, Object>();
                        FSTObjectInput.this.readCompatibleObjectFields(referencee, clInfo, fstCompatibilityInfo.getFieldArray(), fieldMap);
                        getCodec().readVersionTag(); // consume dummy version tag as created by defaultWriteObject
                    } else {
                        fieldMap = (HashMap<String, Object>) FSTObjectInput.this.readObjectInternal(HashMap.class);
                    }
                } catch (Exception e) {
                    throw FSTUtil.rethrow(e);
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
                return getCodec().readFByte();
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
                return getCodec().readFByte();
            }

            @Override
            public int readUnsignedByte() throws IOException {
                return FSTObjectInput.this.readUnsignedByte();
            }

            @Override
            public char readChar() throws IOException {
                return getCodec().readFChar();
            }

            @Override
            public short readShort() throws IOException {
                return getCodec().readFShort();
            }

            @Override
            public int readUnsignedShort() throws IOException {
                return FSTObjectInput.this.readUnsignedShort();
            }

            @Override
            public int readInt() throws IOException {
                return getCodec().readFInt();
            }

            @Override
            public long readLong() throws IOException {
                return getCodec().readFLong();
            }

            @Override
            public float readFloat() throws IOException {
                return getCodec().readFFloat();
            }

            @Override
            public double readDouble() throws IOException {
                return getCodec().readFDouble();
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
                return getCodec().readStringUTF();
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

    protected void pushBack(int i) {
        getCodec().pushBack(i);
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
