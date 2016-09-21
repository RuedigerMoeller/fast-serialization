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

import org.nustaq.serialization.coders.Unknown;
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

    public static boolean REGISTER_ENUMS_READ = false; // do not register enums on read. Flag is saver in case things brake somewhere
    public static ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);
    
    protected FSTDecoder codec;

    protected FSTObjectRegistry objects;

    protected Stack<String> debugStack;
    protected int curDepth;

    protected ArrayList<CallbackEntry> callbacks;
//    FSTConfiguration conf;
    // mirrored from conf
    protected boolean ignoreAnnotations;
    protected FSTClazzInfoRegistry clInfoRegistry;
    // done
    protected ConditionalCallback conditionalCallback;
    protected int readExternalReadAHead = 8000;
    protected VersionConflictListener versionConflictListener;

    protected FSTConfiguration conf;

    // copied values from conf
    protected boolean isCrossPlatform;

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

    protected void setCodec(FSTDecoder codec) {
        this.codec = codec;
    }

    protected static class CallbackEntry {
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

    protected void initRegistries(FSTConfiguration conf) {
        ignoreAnnotations = conf.getCLInfoRegistry().isIgnoreAnnotations();
        clInfoRegistry = conf.getCLInfoRegistry();

        objects = (FSTObjectRegistry) conf.getCachedObject(FSTObjectRegistry.class);
        if (objects == null) {
            objects = new FSTObjectRegistry(conf);
        } else {
            objects.clearForRead(conf);
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
        return getCodec().readIntByte();
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
        return getCodec().available();
    }

    protected void processValidation() throws InvalidObjectException {
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
                FSTUtil.<RuntimeException>rethrow(ex);
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
            FSTUtil.<RuntimeException>rethrow(th);
        } finally {
            curDepth--;
        }
        return null;
    }

    protected FSTClazzInfo.FSTFieldInfo infoCache;
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
            FSTUtil.<RuntimeException>rethrow(t);
        }
        return null;
    }

    public Object readObjectWithHeader(FSTClazzInfo.FSTFieldInfo referencee) throws Exception {
        FSTClazzInfo clzSerInfo;
        Class c;
        final int readPos = getCodec().getInputPos();
        byte code = getCodec().readObjectHeaderTag();  // NOTICE: THIS ADVANCES THE INPUT STREAM...
        if (code == FSTObjectOutput.OBJECT ) {
            // class name
            clzSerInfo = readClass();
            c = clzSerInfo.getClazz();
            if ( c.isArray() )
                return readArrayNoHeader(referencee,readPos,c);
            // fall through
        } else if ( code == FSTObjectOutput.TYPED ) {
            c = referencee.getType();
            clzSerInfo = getClazzInfo(c, referencee);
        } else if ( code >= 1 ) {
            try {
                c = referencee.getPossibleClasses()[code - 1];
                clzSerInfo = getClazzInfo(c, referencee);
            } catch (Throwable th) {
                clzSerInfo = null; c = null;
                FSTUtil.<RuntimeException>rethrow(th);
            }
        } else {
            Object res = instantiateSpecialTag(referencee, readPos, code);
            return res;
        }
        try {
            FSTObjectSerializer ser = clzSerInfo.getSer();
            if (ser != null) {
                Object res = instantiateAndReadWithSer(c, ser, clzSerInfo, referencee, readPos);
                getCodec().readArrayEnd(clzSerInfo);
                return res;
            } else {
                Object res = instantiateAndReadNoSer(c, clzSerInfo, referencee, readPos);
                return res;
            }
        } catch (Exception e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    protected Object instantiateSpecialTag(FSTClazzInfo.FSTFieldInfo referencee, int readPos, byte code) throws Exception {
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
//                case FSTObjectOutput.BIG_INT: { return instantiateBigInt(); }
                case FSTObjectOutput.BIG_LONG: { return Long.valueOf(getCodec().readFLong()); }
                case FSTObjectOutput.BIG_BOOLEAN_FALSE: { return Boolean.FALSE; }
                case FSTObjectOutput.BIG_BOOLEAN_TRUE: { return Boolean.TRUE; }
                case FSTObjectOutput.ONE_OF: { return referencee.getOneOf()[getCodec().readFByte()]; }
//                case FSTObjectOutput.NULL: { return null; }
                case FSTObjectOutput.DIRECT_ARRAY_OBJECT: {
                    Object directObject = getCodec().getDirectObject();
                    objects.registerObjectForRead(directObject,readPos);
                    return directObject;
                }
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
//                case FSTObjectOutput.STRING: return getCodec().readStringUTF();
                case FSTObjectOutput.HANDLE: {
                    Object res = instantiateHandle(referencee);
                    getCodec().readObjectEnd();
                    return res;
                }
                case FSTObjectOutput.ARRAY: {
                    Object res = instantiateArray(referencee, readPos);
                    return res;
                }
                case FSTObjectOutput.ENUM: { return instantiateEnum(referencee, readPos); }
            }
            throw new RuntimeException("unknown object tag "+code);
        }
    }

    protected FSTClazzInfo getClazzInfo(Class c, FSTClazzInfo.FSTFieldInfo referencee) {
        FSTClazzInfo clzSerInfo;
        FSTClazzInfo lastInfo = referencee.lastInfo;
        if ( lastInfo != null && lastInfo.clazz == c && lastInfo.conf == conf) {
            clzSerInfo = lastInfo;
        } else {
            clzSerInfo = clInfoRegistry.getCLInfo(c, conf);
            referencee.lastInfo = clzSerInfo;
        }
        return clzSerInfo;
    }

    protected Object instantiateHandle(FSTClazzInfo.FSTFieldInfo referencee) throws IOException {
        int handle = getCodec().readFInt();
        Object res = objects.getReadRegisteredObject(handle);
        if (res == null) {
            throw new IOException("unable to ressolve handle " + handle + " " + referencee.getDesc() + " " + getCodec().getInputPos() );
        }
        return res;
    }

    protected Object instantiateArray(FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws Exception {
        Object res = readArray(referencee, readPos); // NEED TO PASS ALONG THE POS FOR THE ARRAY

        /*
            registerObjectForRead alerady gets called by readArray (and with the proper pos now).  that said, I'm unclear
            on the intent of the if ( ! referencee.isFlat() ) so I wanted to comment on that

        if ( ! referencee.isFlat() ) {
            objects.registerObjectForRead(res, readPos);
        }
        */

        return res;
    }

    protected Object instantiateEnum(FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws IOException, ClassNotFoundException {
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
        if ( REGISTER_ENUMS_READ ) {
            if ( ! referencee.isFlat() ) { // should be unnecessary
                objects.registerObjectForRead(res, readPos);
            }
        }
        return res;
    }

    protected Object instantiateBigInt() throws IOException {
        int val = getCodec().readFInt();
        return Integer.valueOf(val);
    }

    protected Object instantiateAndReadWithSer(Class c, FSTObjectSerializer ser, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws Exception {
        boolean serInstance = false;
        Object newObj = ser.instantiate(c, this, clzSerInfo, referencee, readPos);
        if (newObj == null) {
            newObj = clzSerInfo.newInstance(getCodec().isMapBased());
        } else
            serInstance = true;
        if (newObj == null) {
            throw new IOException(referencee.getDesc() + ":Failed to instantiate '" + c.getName() + "'. Register a custom serializer implementing instantiate or define empty constructor..");
        }
        if ( newObj == FSTObjectSerializer.REALLY_NULL ) {
            newObj = null;
        } else {
            if (newObj.getClass() != c && ser == null ) {
                // for advanced trickery (e.g. returning non-serializable from FSTSerializer)
                // this hurts. so in case of FSTSerializers incoming clzInfo will refer to the
                // original class, not the one actually instantiated
                c = newObj.getClass();
                clzSerInfo = clInfoRegistry.getCLInfo(c, conf);
            }
            if ( ! referencee.isFlat() && ! clzSerInfo.isFlat() && !ser.alwaysCopy()) {
                objects.registerObjectForRead(newObj, readPos);
            }
            if ( !serInstance )
                ser.readObject(this, newObj, clzSerInfo, referencee);
        }
        getCodec().consumeEndMarker(); //=> bug when writing objects unlimited
        return newObj;
    }

    protected Object instantiateAndReadNoSer(Class c, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws Exception {
        Object newObj;
        newObj = clzSerInfo.newInstance(getCodec().isMapBased());
        if (newObj == null) {
            throw new IOException(referencee.getDesc() + ":Failed to instantiate '" + c.getName() + "'. Register a custom serializer implementing instantiate or define empty constructor.");
        }
        //fixme: code below improves unshared decoding perf, however disables to run mixed mode (clients can decide)
        //actually would need 2 flags for encode/decode
        //tested with json mixed mode does not work anyway ...
        final boolean needsRefLookup = conf.shareReferences && !referencee.isFlat() && !clzSerInfo.isFlat();
        // previously :
//        final boolean needsRefLookup = !referencee.isFlat() && !clzSerInfo.isFlat();
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

    protected Object handleReadRessolve(FSTClazzInfo serializationInfo, Object newObj) throws IllegalAccessException {
        Object rep = null;
        try {
            rep = serializationInfo.getReadResolveMethod().invoke(newObj);
        } catch (InvocationTargetException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        newObj = rep;//FIXME: support this in call
        return newObj;
    }

    protected void readObjectCompatibleRecursive(FSTClazzInfo.FSTFieldInfo referencee, Object toRead, FSTClazzInfo serializationInfo, Class cl) throws Exception {
        FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = serializationInfo.getCompInfo().get(cl);
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
                FSTUtil.<RuntimeException>rethrow(e);
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
                            final Object val = fieldMap.get(fstFieldInfo.getName());
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
            readObjectFields(referencee,serializationInfo,serializationInfo.getFieldInfo(),newObj,0,-1); // -1 flag to indicate no object end should be called
        } catch (Exception e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    protected void readObjectFields(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo, Object newObj, int startIndex, int version) throws Exception {
        
        if ( getCodec().isMapBased() ) {
            readFieldsMapBased(referencee, serializationInfo, newObj);
            if ( version >= 0 && newObj instanceof Unknown == false)
                getCodec().readObjectEnd();
            return;
        }
        if ( version < 0 )
            version = 0;
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
        int debug = getCodec().readVersionTag();// just consume '0'
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
        int len = getCodec().getObjectHeaderLen(); // check if len is known in advance
        if ( len < 0 )
            len = Integer.MAX_VALUE;
        int count = 0;
        boolean isUnknown = newObj.getClass() == Unknown.class; // json
        boolean inArray = isUnknown && getCodec().inArray();    // json externalized/custom serialized
        getCodec().startFieldReading(newObj);
        // fixme: break up this loop into separate impls.
        while( count < len ) {
            if ( inArray ) {
                // unknwon json object written by externalize or custom serializer
                Object o = readObjectWithHeader(null);
                if ( o != null && getCodec().isEndMarker(o.toString()) )
                    return;
                ((Unknown)newObj).add(o);
                continue;
            }
            name= getCodec().readStringUTF();
            //int debug = getCodec().getInputPos();
            if ( len == Integer.MAX_VALUE && getCodec().isEndMarker(name) )
                return;
            count++;
            if (isUnknown) {
                FSTClazzInfo.FSTFieldInfo fakeField = new FSTClazzInfo.FSTFieldInfo(null, null, true);
                fakeField.fakeName = name;
                Object toSet = readObjectWithHeader(fakeField);
                ((Unknown)newObj).set(name, toSet);
            } else {
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
                    } else {
                        Object toSet = readObjectWithHeader(fieldInfo);
                        toSet = getCodec().coerceElement(fieldInfo.getType(), toSet);
                        fieldInfo.setObjectValue(newObj, toSet);
                    }
                }
            }
        }
    }

    protected boolean skipConditional(Object newObj, int conditional, FSTClazzInfo.FSTFieldInfo subInfo) {
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
                        res.put(subInfo.getName(), val);
                    }
                    if (subInfoType == byte.class) {
                        res.put(subInfo.getName(), getCodec().readFByte());
                    } else if (subInfoType == char.class) {
                        res.put(subInfo.getName(), getCodec().readFChar());
                    } else if (subInfoType == short.class) {
                        res.put(subInfo.getName(), getCodec().readFShort());
                    } else if (subInfoType == int.class) {
                        res.put(subInfo.getName(), getCodec().readFInt());
                    } else if (subInfoType == double.class) {
                        res.put(subInfo.getName(), getCodec().readFDouble());
                    } else if (subInfoType == float.class) {
                        res.put(subInfo.getName(), getCodec().readFFloat());
                    } else if (subInfoType == long.class) {
                        res.put(subInfo.getName(), getCodec().readFLong());
                    }
                } else {
                    // object
                    Object subObject = readObjectWithHeader(subInfo);
                    res.put(subInfo.getName(), subObject);
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

    protected Object readArray(FSTClazzInfo.FSTFieldInfo referencee, int pos) throws Exception {
        Object classOrArray = getCodec().readArrayHeader();
        if (pos < 0)
            pos = getCodec().getInputPos();
        if ( classOrArray instanceof Class == false )
            return classOrArray;
        if ( classOrArray == null )
            return null;
        Object o = readArrayNoHeader(referencee, pos, (Class) classOrArray);
        getCodec().readArrayEnd(null);
        return o;
    }

    protected Object readArrayNoHeader(FSTClazzInfo.FSTFieldInfo referencee, int pos, Class arrCl) throws Exception {
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
                    value = getCodec().coerceElement(arrType, value);
                    arr[i] = value;
                }
                getCodec().readObjectEnd();
            }
            return array;
        } else { // multidim array
            Object array[] = (Object[]) Array.newInstance(arrType, len);
            if ( ! referencee.isFlat() ) {
                objects.registerObjectForRead(array, pos);
            }
            FSTClazzInfo.FSTFieldInfo ref1 = new FSTClazzInfo.FSTFieldInfo(referencee.getPossibleClasses(), null, clInfoRegistry.isIgnoreAnnotations());
            for (int i = 0; i < len; i++) {
                Object subArray = readArray(ref1, -1);
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

    protected void resetAndClearRefs() {
        try {
            reset();
            objects.clearForRead(conf);
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
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
        objects.clearForRead(conf);
        callbacks = null; //fix memory leak on reuse from default FstConfiguration
    }

    public void resetForReuseCopyArray(byte bytes[], int off, int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        getCodec().reset();
        objects.clearForRead(conf);
        getCodec().resetToCopyOf(bytes, off, len);
    }

    public void resetForReuseUseArray(byte bytes[]) throws IOException {
        resetForReuseUseArray(bytes, bytes.length);
    }

    public void resetForReuseUseArray(byte bytes[], int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        objects.clearForRead(conf);
        getCodec().resetWith(bytes, len);
    }

    public final int readFInt() throws IOException {
        return getCodec().readFInt();
    }

    protected boolean closed = false;
    @Override
    public void close() throws IOException {
        closed = true;
        resetAndClearRefs();
        conf.returnObject(objects);
        getCodec().close();
    }

    ////////////////////////////////////////////////////// epic compatibility hack /////////////////////////////////////////////////////////

    protected MyObjectStream fakeWrapper; // some jdk classes hash for ObjectStream, so provide the same instance always

    protected ObjectInputStream getObjectInputStream(final Class cl, final FSTClazzInfo clInfo, final FSTClazzInfo.FSTFieldInfo referencee, final Object toRead) throws IOException {
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
                            Class[] typeParameters = constructor.getParameterTypes();
                            if ( typeParameters != null && typeParameters.length == 1 && typeParameters[0] == int.class) {
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
                        // object has been written with writeFields, is no read with defaultReadObjects,
                        // need to autoapply map to object vars.
                        // this might be redundant in case readObject() pulls a getFields() .. (see bitset testcase)
                        for (Iterator<String> iterator = fieldMap.keySet().iterator(); iterator.hasNext(); ) {
                            String key = iterator.next();
                            FSTClazzInfo.FSTFieldInfo fieldInfo = clInfo.getFieldInfo(key, null);// in case fieldName is not unique => cannot recover/fix
                            if ( fieldInfo != null ) {
                                fieldInfo.setObjectValue(toRead,fieldMap.get(key));
                            }
                        }
                    } else {
                        FSTObjectInput.this.readObjectFields(
                                referencee,
                                clInfo,
                                clInfo.getCompInfo().get(cl).getFieldArray(),
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
                    FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = clInfo.getCompInfo().get(cl);
                    if (tag==99) { // came from defaultwriteobject
                        // Note: in case number and names of instance fields of reader/writer are different,
                        // this fails as code below implicitely assumes, fields of writer == fields of reader
                        // unfortunately one can use defaultWriteObject at writer side but use getFields at reader side
                        // in readObject(). if then fields differ, code below reads BS and fails.
                        // Its impossible to fix that except by always using putField + getField for
                        // JDK compatibility classes, however this will waste lots of performance. As
                        // it would be necessary to *always* write full metainformation (a map of fieldName => value pairs)
                        // see #53
                        fieldMap = new HashMap<String, Object>();
                        FSTObjectInput.this.readCompatibleObjectFields(referencee, clInfo, fstCompatibilityInfo.getFieldArray(), fieldMap);
                        getCodec().readVersionTag(); // consume dummy version tag as created by defaultWriteObject
                    } else if (tag == 66) { // has been written from writeObjectCompatible without writeMethod
                        fieldMap = new HashMap<String, Object>();
                        FSTObjectInput.this.readCompatibleObjectFields(referencee, clInfo, fstCompatibilityInfo.getFieldArray(), fieldMap);
                        getCodec().readVersionTag(); // consume dummy version tag as created by defaultWriteObject
                    } else {
                        fieldMap = (HashMap<String, Object>) FSTObjectInput.this.readObjectInternal(HashMap.class);
                    }
                } catch (Exception e) {
                    FSTUtil.<RuntimeException>rethrow(e);
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

    protected static class MyObjectStream extends ObjectInputStream {

        ObjectInputStream wrapped;
        ArrayDeque<ObjectInputStream> wrappedStack = new ArrayDeque<ObjectInputStream>();

        public void push( ObjectInputStream in ) {
            wrappedStack.push(in);
            wrapped = in;
        }

        public void pop() {
            wrapped = wrappedStack.pop();
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
