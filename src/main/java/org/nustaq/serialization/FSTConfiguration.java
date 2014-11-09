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
package org.nustaq.serialization;

import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.serialization.coders.FSTMinBinDecoder;
import org.nustaq.serialization.coders.FSTMinBinEncoder;
import org.nustaq.serialization.coders.FSTStreamDecoder;
import org.nustaq.serialization.coders.FSTStreamEncoder;
import org.nustaq.serialization.util.FSTInputStream;
import org.nustaq.serialization.util.FSTUtil;
import org.nustaq.serialization.serializers.*;

import java.awt.*;
import java.io.*;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 18.11.12
 * Time: 20:41
 *
 * Holds a serialization configuration. Reuse this class !!! construction is very expensive. (just keep a static instances around)
 */
public final class FSTConfiguration {

    StreamCoderFactory streamCoderFactory = new StreamCoderFactory() {
        @Override
        public FSTEncoder createStreamEncoder() {
            return new FSTStreamEncoder(FSTConfiguration.this);
        }

        @Override
        public FSTDecoder createStreamDecoder() {
            return new FSTStreamDecoder(FSTConfiguration.this);
        }
    };
    
    FSTClazzInfoRegistry serializationInfoRegistry = new FSTClazzInfoRegistry(this);
    HashMap<Class,List<SoftReference>> cachedObjects = new HashMap<Class, List<SoftReference>>(97);
    FSTClazzNameRegistry classRegistry = new FSTClazzNameRegistry(null, this);
    boolean preferSpeed = false; // hint to prefer speed over size in case, currently ignored.
    boolean shareReferences = true;
    ClassLoader classLoader = getClass().getClassLoader();
    boolean forceSerializable = false; // serialize objects which are not instanceof serializable using default serialization scheme.

    /////////////////////////////////////
    // cross platform stuff only

    int cpAttrIdCount = 0;
    HashMap<String, Integer> crossPlatformAttrIds = new HashMap<>();
    HashMap<Integer, String> crossPlatformAttrIdsReverse = new HashMap<>();
    // contains symbol => full qualified name
    private HashMap<String, String> crossPlatformNames = new HashMap<>();
    // may contain symbol => cached binary output
    private HashMap<String, byte[]> crossPlatformNamesBytez = new HashMap<>();
    // contains full qualified name => symbol
    private HashMap<String, String> crossPlatformNamesReverse = new HashMap<>();
    private boolean crossPlatform = false; // if true do not support writeObject/readObject etc.

    // end cross platform stuff only
    /////////////////////////////////////

    public static Integer getInt(int i) {
        if ( i >= 0 && i < intObjects.length ) {
            return intObjects[i];
        }
        return Integer.valueOf(i);
    }

    public static Integer intObjects[];
    {
        if ( intObjects == null ) {
            intObjects = new Integer[30000];
            for (int i = 0; i < intObjects.length; i++) {
                intObjects[i] = Integer.valueOf(i);
            }
        }
    }

    static AtomicBoolean conflock = new AtomicBoolean(false);
    static FSTConfiguration singleton;
    public static FSTConfiguration getDefaultConfiguration() {
        do { } while ( !conflock.compareAndSet(false, true) );
        try {
            if (singleton == null)
                singleton = createDefaultConfiguration();
            return singleton;
        } finally {
            conflock.set(false);
        }
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static FSTConfiguration createCrossPlatformConfiguration() {
        final FSTConfiguration res = createDefaultConfiguration();
        res.setCrossPlatform(true);
        res.setStreamCoderFactory(new StreamCoderFactory() {
            @Override
            public FSTEncoder createStreamEncoder() {
                return new FSTMinBinEncoder(res);
            }

            @Override
            public FSTDecoder createStreamDecoder() {
                return new FSTMinBinDecoder(res);
            }
        });

        // override some serializers
        FSTSerializerRegistry reg = res.serializationInfoRegistry.serializerRegistry;
        reg.putSerializer(EnumSet.class, new FSTCPEnumSetSerializer(), true);
        reg.putSerializer(Throwable.class, new FSTCPThrowableSerializer(), true);

        // for crossplatform fallback does not work => register default serializers for collections and subclasses
        reg.putSerializer(AbstractCollection.class, new FSTCollectionSerializer(), true); // subclass should register manually
        reg.putSerializer(AbstractMap.class, new FSTMapSerializer(), true); // subclass should register manually

        res.registerCrossPlatformClassMapping(new String[][]{
                {"map", HashMap.class.getName()},
                {"list", ArrayList.class.getName()},
                {"long", Long.class.getName()},
                {"integer", Integer.class.getName()},
                {"short", Short.class.getName()},
                {"byte", Byte.class.getName()},
                {"char", Character.class.getName()},
                {"float", Float.class.getName()},
                {"double", Double.class.getName()},
                {"date", Date.class.getName()},
                {"enumSet", "java.util.RegularEnumSet"},
                {"array", "[Ljava.lang.Object;"},
                {"String[]", "[Ljava.lang.String;"},
                {"Double[]", "[Ljava.lang.Double;"},
                {"Float[]", "[Ljava.lang.Float;"},
                {"double[]", "[D"},
                {"float[]", "[F"}
        });
        return res;
    }

    public static FSTConfiguration createDefaultConfiguration() {
        FSTConfiguration conf = new FSTConfiguration();
        conf.addDefaultClazzes();
        // serializers
        FSTSerializerRegistry reg = conf.serializationInfoRegistry.serializerRegistry;
        reg.putSerializer(Class.class, new FSTClassSerializer(), false);
        reg.putSerializer(String.class, new FSTStringSerializer(), false);
        reg.putSerializer(Byte.class, new FSTBigNumberSerializers.FSTByteSerializer(), false);
        reg.putSerializer(Character.class, new FSTBigNumberSerializers.FSTCharSerializer(), false);
        reg.putSerializer(Short.class, new FSTBigNumberSerializers.FSTShortSerializer(), false);
        reg.putSerializer(Float.class, new FSTBigNumberSerializers.FSTFloatSerializer(), false);
        reg.putSerializer(Double.class, new FSTBigNumberSerializers.FSTDoubleSerializer(), false);

        reg.putSerializer(Date.class, new FSTDateSerializer(), false);
        reg.putSerializer(StringBuffer.class, new FSTStringBufferSerializer(), true);
        reg.putSerializer(StringBuilder.class, new FSTStringBuilderSerializer(), true);
        reg.putSerializer(EnumSet.class, new FSTEnumSetSerializer(), true);

        // for most cases don't register for subclasses as in many cases we'd like to fallback to JDK implementation
        // (e.g. TreeMap) in order to guarantee complete serialization
        reg.putSerializer(ArrayList.class, new FSTArrayListSerializer(), false); // subclass should register manually
//        reg.putSerializer(ArrayList.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(Vector.class, new FSTCollectionSerializer(), false); // EXCEPTION !!! subclass should register manually
        reg.putSerializer(LinkedList.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(HashSet.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(HashMap.class, new FSTMapSerializer(), false); // subclass should register manually
        reg.putSerializer(LinkedHashMap.class, new FSTMapSerializer(), false); // subclass should register manually
        reg.putSerializer(Hashtable.class, new FSTMapSerializer(), false); // subclass should register manually
        reg.putSerializer(ConcurrentHashMap.class, new FSTMapSerializer(), true); // subclass should register manually
        reg.putSerializer(FSTStruct.class, new FSTStructSerializer(), true); // subclasses also use this
        return conf;
    }

    public void registerSerializer(Class clazz, FSTObjectSerializer ser, boolean alsoForAllSubclasses ) {
        serializationInfoRegistry.serializerRegistry.putSerializer(clazz, ser, alsoForAllSubclasses);
    }

    public static FSTConfiguration createStructConfiguration() {
        FSTConfiguration conf = new FSTConfiguration();
        conf.setStructMode(true);
        return conf;
    }

    private FSTConfiguration() {

    }

    public StreamCoderFactory getStreamCoderFactory() {
        return streamCoderFactory;
    }

    /**
     * allows to use subclassed stream codecs. Can also be used to change class loading behaviour, as
     * clasForName is part of a codec's interface.
     *
     * e.g. new StreamCoderFactory() {
     *   @Override
     *   public FSTEncoder createStreamEncoder() {
     *      return new FSTStreamEncoder(FSTConfiguration.this);
     *   }
     *
     *   @Override
     *   public FSTDecoder createStreamDecoder() {
     *      return new FSTStreamDecoder(FSTConfiguration.this) { public Class classForName(String name) { ... }  } ;
     *   }
     * };
     *
     * You need to work with thread locals most probably as the factory is ~global (assigned to fstconfiguration shared amongst
     * streams)
     *
     * @param streamCoderFactory
     */
    public void setStreamCoderFactory(StreamCoderFactory streamCoderFactory) {
        this.streamCoderFactory = streamCoderFactory;
    }

    /**
     * reuse heavy weight objects. If a FSTStream is closed, objects are returned and can be reused by new stream instances.
     * the objects are held in soft references, so there should be no memory issues. FIXME: point of contention !
     * @param cached
     */
    public void returnObject( Object cached ) {
        try {
            while (!cacheLock.compareAndSet(false, true)) {
                // empty
            }
            List<SoftReference> li = cachedObjects.get(cached.getClass());
            if ( li == null ) {
                li = new ArrayList<SoftReference>();
                cachedObjects.put(cached.getClass(),li);
            }
            if ( li.size() < 5 )
                li.add(new SoftReference(cached));
        } finally {
            cacheLock.set(false);
        }
    }

    public boolean isPreferSpeed() {
        return preferSpeed;
    }

    /**
     * this options lets FST favour speed of encoding over size of the encoded object.
     * Warning: this option alters the format of the written stream, so both reader and writer should have
     * the same setting, else exceptions will occur
     * @param preferSpeed
     */
    public void setPreferSpeed(boolean preferSpeed) {
        this.preferSpeed = preferSpeed;
    }

    /**
     * for optimization purposes, do not use to benchmark processing time or in a regular program as
     * this methods creates a temporary binaryoutputstream and serializes the object in order to measure the
     * size.
     */
    public int calcObjectSizeBytesNotAUtility( Object obj ) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        FSTObjectOutput ou = new FSTObjectOutput(bout,this);
        ou.writeObject(obj, obj.getClass());
        ou.close();
        return bout.toByteArray().length;
    }

    /**
     * patch default serializer lookup. set to null to delete.
     * Should be set prior to any serialization going on (serializer lookup is cached).
     * @param del
     */
    public void setSerializerRegistryDelegate( FSTSerializerRegistryDelegate del ) {
        serializationInfoRegistry.setSerializerRegistryDelegate(del);
    }

    AtomicBoolean cacheLock = new AtomicBoolean(false);
    public Object getCachedObject( Class cl ) {
        try  {
            while (!cacheLock.compareAndSet(false, true)) {
                // empty
            }
            List<SoftReference> li = cachedObjects.get(cl);
            if ( li == null ) {
                return null;
            }
            for (int i = li.size()-1; i >= 0; i--) {
                SoftReference softReference = li.get(i);
                Object res = softReference.get();
                li.remove(i);
                if ( res != null ) {
                    return res;
                }
            }
        } finally {
            cacheLock.set(false);
        }
        return null;
    }

    public boolean isForceSerializable() {
        return forceSerializable;
    }

    /**
     * treat unserializable classes same as if they would be serializable.
     * @param forceSerializable
     */
    public FSTConfiguration setForceSerializable(boolean forceSerializable) {
        this.forceSerializable = forceSerializable;
        return this;
    }

    /**
     * clear cached softref's and ThreadLocal. Use if you won't read/write objects anytime soon.
     */
    public void clearCaches() {
        try {
            FSTInputStream.cachedBuffer.set(null);
            while (!cacheLock.compareAndSet(false, true)) {
                // empty
            }
            cachedObjects.clear();
        } finally {
            cacheLock.set( false );
        }
    }

    public boolean isShareReferences() {
        return shareReferences;
    }

    /**
     * if false, identical objects will get serialized twice. Gains speed as long there are no double objects/cyclic references (typical for small snippets as used in e.g. RPC)
     * @param shareReferences
     */
    public void setShareReferences(boolean shareReferences) {
        this.shareReferences = shareReferences;
    }

    /**
     * attention: id should be > CUSTOM_ID_BASE
     * @param c
     */
    public void registerClass( Class ... c) {
        for (int i = 0; i < c.length; i++) {
            classRegistry.registerClass(c[i]);
        }
    }

    void addDefaultClazzes() {
        classRegistry.registerClass(String.class);
        classRegistry.registerClass(Byte.class);
        classRegistry.registerClass(Short.class);
        classRegistry.registerClass(Integer.class);
        classRegistry.registerClass(Long.class);
        classRegistry.registerClass(Float.class);
        classRegistry.registerClass(Double.class);
        classRegistry.registerClass(BigDecimal.class);
        classRegistry.registerClass(BigInteger.class);
        classRegistry.registerClass(Character.class);
        classRegistry.registerClass(Boolean.class);
        classRegistry.registerClass(TreeMap.class);
        classRegistry.registerClass(HashMap.class);
        classRegistry.registerClass(ArrayList.class);
        classRegistry.registerClass(ConcurrentHashMap.class);
        classRegistry.registerClass(Color.class);
        classRegistry.registerClass(Dimension.class);
        classRegistry.registerClass(Point.class);
        classRegistry.registerClass(Rectangle.class);
        classRegistry.registerClass(Font.class);
        classRegistry.registerClass(URL.class);
        classRegistry.registerClass(Date.class);
        classRegistry.registerClass(java.sql.Date.class);
        classRegistry.registerClass(SimpleDateFormat.class);
        classRegistry.registerClass(TreeSet.class);
        classRegistry.registerClass(LinkedList.class);
        classRegistry.registerClass(SimpleTimeZone.class);
        classRegistry.registerClass(GregorianCalendar.class);
        classRegistry.registerClass(Vector.class);
        classRegistry.registerClass(Hashtable.class);
        classRegistry.registerClass(BitSet.class);
        classRegistry.registerClass(Locale.class);

        classRegistry.registerClass(StringBuffer.class);
        classRegistry.registerClass(StringBuilder.class);

        classRegistry.registerClass(Object.class);
        classRegistry.registerClass(Object[].class);
        classRegistry.registerClass(Object[][].class);
        classRegistry.registerClass(Object[][][].class);
        classRegistry.registerClass(Object[][][][].class);
        classRegistry.registerClass(Object[][][][][].class);
        classRegistry.registerClass(Object[][][][][][].class);
        classRegistry.registerClass(Object[][][][][][][].class);

        classRegistry.registerClass(byte[].class);
        classRegistry.registerClass(byte[][].class);
        classRegistry.registerClass(byte[][][].class);
        classRegistry.registerClass(byte[][][][].class);
        classRegistry.registerClass(byte[][][][][].class);
        classRegistry.registerClass(byte[][][][][][].class);
        classRegistry.registerClass(byte[][][][][][][].class);

        classRegistry.registerClass(char[].class);
        classRegistry.registerClass(char[][].class);
        classRegistry.registerClass(char[][][].class);
        classRegistry.registerClass(char[][][][].class);
        classRegistry.registerClass(char[][][][][].class);
        classRegistry.registerClass(char[][][][][][].class);
        classRegistry.registerClass(char[][][][][][][].class);

        classRegistry.registerClass(short[].class);
        classRegistry.registerClass(short[][].class);
        classRegistry.registerClass(short[][][].class);
        classRegistry.registerClass(short[][][][].class);
        classRegistry.registerClass(short[][][][][].class);
        classRegistry.registerClass(short[][][][][][].class);
        classRegistry.registerClass(short[][][][][][][].class);

        classRegistry.registerClass(int[].class);
        classRegistry.registerClass(int[][].class);
        classRegistry.registerClass(int[][][].class);
        classRegistry.registerClass(int[][][][].class);
        classRegistry.registerClass(int[][][][][].class);
        classRegistry.registerClass(int[][][][][][].class);
        classRegistry.registerClass(int[][][][][][][].class);

        classRegistry.registerClass(float[].class);
        classRegistry.registerClass(float[][].class);
        classRegistry.registerClass(float[][][].class);
        classRegistry.registerClass(float[][][][].class);
        classRegistry.registerClass(float[][][][][].class);
        classRegistry.registerClass(float[][][][][][].class);
        classRegistry.registerClass(float[][][][][][][].class);

        classRegistry.registerClass(double[].class);
        classRegistry.registerClass(double[][].class);
        classRegistry.registerClass(double[][][].class);
        classRegistry.registerClass(double[][][][].class);
        classRegistry.registerClass(double[][][][][].class);
        classRegistry.registerClass(double[][][][][][].class);
        classRegistry.registerClass(double[][][][][][][].class);

    }

    public FSTClazzNameRegistry getClassRegistry() {
        return classRegistry;
    }

    public FSTClazzInfoRegistry getCLInfoRegistry() {
        return serializationInfoRegistry;
    }

    /**
     * mark the given class as being replaceable by an equal instance.
     * E.g. if A=Integer(1) is written and later on an B=Integer(1) is written, after deserializing A == B.
     * This is safe for a lot of immutable classes (A.equals(B) transformed to A == B), e.g. for Number subclasses
     * and String class. See also the EqualnessIsIdentity Annotation
     */
    // needs more testing
//    public void registerAsEqualnessReplaceable(Class cl) {
//        getCLInfoRegistry().getCLInfo(cl).equalIsIdentity = true;
//    }

    // needs more testing
//    public void registerAsFlat(Class cl) {
//        getCLInfoRegistry().getCLInfo(cl).flat = true;
//    }

    /**
     * mark the given class as being replaced by a copy of an equal instance.
     * E.g. if A=Dimension(10,10) is written and later on an B=Dimension(10,10) is written, after deserializing B will be copied from A without writing the data of B.
     * This is safe for 99% of the classes e.g. for Number subclasses
     * and String class. See also the EqualnessIsBinary Annotation
     * Note that in addition to equalsness, it is required that A.class == B.class.
     * WARNING: adding collection classes might decrease performance significantly (trade cpu efficiency against size)
     */
     // needs more testing
//    public void registerAsEqualnessCopyable(Class cl) {
//        getCLInfoRegistry().getCLInfo(cl).equalIsBinary = true;
//    }


    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public FSTClazzInfo getClassInfo(Class type) {
        return serializationInfoRegistry.getCLInfo(type);
    }

    ThreadLocal<FSTObjectOutput> output = new ThreadLocal<FSTObjectOutput>() {
        @Override
        protected FSTObjectOutput initialValue() {
            return new FSTObjectOutput(FSTConfiguration.this);
        }
    };

    ThreadLocal<FSTObjectInput> input = new ThreadLocal<FSTObjectInput>() {
        @Override
        protected FSTObjectInput initialValue() {
            try {
                return new FSTObjectInput(FSTConfiguration.this);
            } catch (Exception e) {
                throw FSTUtil.rethrow(e);
            }
        }
    };

    /**
     * utility for thread safety and reuse. Do not close the resulting stream. However you should close
     * the given InputStream 'in'
     * @param in
     * @return
     */
    public FSTObjectInput getObjectInput( InputStream in ) {
        FSTObjectInput fstObjectInput = input.get();
        try {
            fstObjectInput.resetForReuse(in);
            return fstObjectInput;
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }

    public FSTObjectInput getObjectInput( byte arr[]) {
        return getObjectInput(arr, arr.length);
    }

    /**
     * take the given array as input. the array is NOT copied
     * @param arr
     * @param len
     * @return
     */
    public FSTObjectInput getObjectInput( byte arr[], int len ) {
        FSTObjectInput fstObjectInput = input.get();
        try {
            fstObjectInput.resetForReuseUseArray(arr,len);
            return fstObjectInput;
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }

    /**
     * utility for thread safety and reuse. Do not close the resulting stream. However you should close
     * the given OutputStream 'out'
     * @param out - can be null (temp bytearrays stream is created then)
     * @return
     */
    public FSTObjectOutput getObjectOutput(OutputStream out) {
        FSTObjectOutput fstObjectOutput = output.get();
        fstObjectOutput.resetForReUse(out);
        return fstObjectOutput;
    }

    /**
     * @return a recycled outputstream reusing its last recently used byte[] buffer
     */
    public FSTObjectOutput getObjectOutput() {
        return getObjectOutput((OutputStream)null);
    }

    public FSTObjectOutput getObjectOutput(byte[] outByte) {
        FSTObjectOutput fstObjectOutput = output.get();
        fstObjectOutput.resetForReUse(outByte);
        return fstObjectOutput;
    }

    /**
     * ignores all serialization related interfaces (Serializable, Externalizable) and serializes all classes using the
     * default scheme. Warning: this is a special mode of operation which fail serializing/deserializing many standard
     * JDK classes.
     *
     * @param ignoreSerialInterfaces
     */
    public void setStructMode(boolean ignoreSerialInterfaces) {
        serializationInfoRegistry.setStructMode(ignoreSerialInterfaces);
    }

    /**
     * @see setStructMode()
     * @return
     */
    public boolean isStructMode() {
        return serializationInfoRegistry.isStructMode();
    }

    public FSTClazzInfo getClazzInfo(Class rowClass) {
        return getCLInfoRegistry().getCLInfo(rowClass);
    }

    public void setCrossPlatform(boolean crossPlatform) {
        this.crossPlatform = crossPlatform;
    }

    public boolean isCrossPlatform() {
        return crossPlatform;
    }

    public <T> T deepCopy(T metadata) {
        return (T) asObject(asByteArray(metadata));
    }

    public static interface StreamCoderFactory {
        FSTEncoder createStreamEncoder();
        FSTDecoder createStreamDecoder();
    }
    
    public FSTEncoder createStreamEncoder() {
        return streamCoderFactory.createStreamEncoder();
    }

    public FSTDecoder createStreamDecoder() {
        return streamCoderFactory.createStreamDecoder();
    }

    AtomicBoolean cplock = new AtomicBoolean(false);
    public void registerCrossPlatformClassBinaryCache( String fulLQName, byte[] binary ) {
        try {
            while (cplock.compareAndSet(false, true)) { } // spin
            crossPlatformNamesBytez.put(fulLQName, binary);
        } finally {
            cplock.set(false);
        }
    }

    public byte[] getCrossPlatformBinaryCache(String symbolicName) {
        try {
            while ( cplock.compareAndSet(false, true)) { } // spin
            return crossPlatformNamesBytez.get(symbolicName);
        } finally {
            cplock.set(false);
        }
    }

    /**
     * init right after creation of configuration, not during operation as it is not threadsafe regarding mutation
     * @param keysAndVals { { "symbolicName", "fullQualifiedClazzName" }, .. }
     */
    public void registerCrossPlatformClassMapping( String[][] keysAndVals ) {
        for (int i = 0; i < keysAndVals.length; i++) {
            String[] keysAndVal = keysAndVals[i];
            registerCrossPlatformClassMapping( keysAndVal[0], keysAndVal[1] );
        }
    }

    public void registerCrossPlatformClassMapping( String shortName,  String fqName ) {
        crossPlatformNames.put( shortName, fqName);
        crossPlatformNamesReverse.put( fqName, shortName);
    }

    /**
     * init right after creation of configuration, not during operation as it is not threadsafe regarding mutation
     */
    public void registerCrossPlatformClassMappingUseSimpleName( Class[] classes ) {
        registerCrossPlatformClassMappingUseSimpleName(new ArrayList<>(Arrays.asList(classes)));
    }

    public void registerCrossPlatformClassMappingUseSimpleName( List<Class> classes ) {
        for (int i = 0; i < classes.size(); i++) {
            Class clz = classes.get(i);
            crossPlatformNames.put( clz.getSimpleName(), clz.getName() );
            crossPlatformNamesReverse.put( clz.getName(), clz.getSimpleName() );
        }
    }

    /**
     * Note: UNUSED, not yet implemented
     * init right after creation of configuration, not during operation as it is not threadsafe regarding mutation
     * @param names { "varName", .. } for each of these an id will be written instead of full name.
     */
    public void registerCrossPlatformAttributeNames( String ... names ) {
        for (int i = 0; i < names.length; i++) {
            crossPlatformAttrIds.put( names[i], cpAttrIdCount);
            crossPlatformAttrIdsReverse.put( cpAttrIdCount, names[i]);
        }
    }

    public String getCrossPlatformAttributeName( Integer id ) {
        return crossPlatformAttrIdsReverse.get(id);
    }

    public Integer getCrossPlatformAttributeId( String id ) {
        return crossPlatformAttrIds.get(id);
    }

    /**
     * get cross platform symbolic class identifier
     * @param cl
     * @return
     */
    public String getCPNameForClass( Class cl ) {
        String res = crossPlatformNamesReverse.get(cl.getName());
        if (res == null) {
            return cl.getName();
        }
        return res;
    }

    public String getClassForCPName( String name ) {
        String res = crossPlatformNames.get(name);
        if (res == null) {
            return name;
        }
        return res;
    }

    /**
     * convenience
     */
    public Object asObject( byte b[] ) {
        try {
            return getObjectInput(b).readObject();
        } catch (Exception e) {
            throw FSTUtil.rethrow(e);
        }
    }

    /**
     * convenience. (object must be serialiable unless fstconfiguration with appropriate settings is used)
     */
    public byte[] asByteArray( Object object ) {
        FSTObjectOutput objectOutput = getObjectOutput();
        try {
            objectOutput.writeObject(object);
            return objectOutput.getCopyOfWrittenBuffer();
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }


}
