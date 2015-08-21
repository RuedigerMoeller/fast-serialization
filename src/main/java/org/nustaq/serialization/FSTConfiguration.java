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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.UTF8JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.serialization.coders.*;
import org.nustaq.serialization.util.FSTInputStream;
import org.nustaq.serialization.util.FSTUtil;
import org.nustaq.serialization.serializers.*;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

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
 * Holds a serialization configuration/metadata.
 * Reuse this class !!! construction is very expensive. (just keep static instances around or use thread locals)
 *
 */
public class FSTConfiguration {

    static enum ConfType {
        DEFAULT, UNSAFE, MINBIN, JSON, JSONPRETTY
    }

    /**
     * if all attempts fail to find a class this guy is asked.
     * Can be used in case e.g. dynamic classes need get generated.
     */
    public static interface LastResortClassRessolver {
        public Class getClass( String clName );
    }

    StreamCoderFactory streamCoderFactory = new FSTDefaultStreamCoderFactory(this);

    String name;

    ConfType type = ConfType.DEFAULT;
    FSTClazzInfoRegistry serializationInfoRegistry = new FSTClazzInfoRegistry();
    HashMap<Class,List<SoftReference>> cachedObjects = new HashMap<Class, List<SoftReference>>(97);
    FSTClazzNameRegistry classRegistry = new FSTClazzNameRegistry(null);
    boolean preferSpeed = false; // hint to prefer speed over size in case, currently ignored.
    boolean shareReferences = true;
    volatile ClassLoader classLoader = getClass().getClassLoader();
    boolean forceSerializable = false; // serialize objects which are not instanceof serializable using default serialization scheme.
    FSTClassInstantiator instantiator = new FSTDefaultClassInstantiator();

    Object coderSpecific;
    LastResortClassRessolver lastResortResolver;

    boolean forceClzInit = false; // always execute default fields init, even if no transients

    // cache fieldinfo. This can be shared with derived FSTConfigurations in order to reduce footprint
    static class FieldKey {
        Class clazz;
        String fieldName;

        public FieldKey(Class clazz, String fieldName) {
            this.clazz = clazz;
            this.fieldName = fieldName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FieldKey fieldKey = (FieldKey) o;

            if (!clazz.equals(fieldKey.clazz)) return false;
            return fieldName.equals(fieldKey.fieldName);

        }

        @Override
        public int hashCode() {
            int result = clazz.hashCode();
            result = 31 * result + fieldName.hashCode();
            return result;
        }
    }
    ConcurrentHashMap<FieldKey,FSTClazzInfo.FSTFieldInfo> fieldInfoCache;

    /**
     * debug helper
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * debug helper
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
/////////////////////////////////////
    // cross platform stuff only

    int cpAttrIdCount = 0;
    // contains symbol => full qualified name
    private HashMap<String, String> minbinNames = new HashMap<>();
    // may contain symbol => cached binary output
    private HashMap<String, byte[]> minBinNamesBytez = new HashMap<>();
    // contains full qualified name => symbol
    private HashMap<String, String> minbinNamesReverse = new HashMap<>();
    private boolean crossPlatform = false; // if true do not support writeObject/readObject etc.

    // non-final for testing
    public static boolean isAndroid = System.getProperty("java.runtime.name", "no").toLowerCase().contains("android");

    // end cross platform stuff only
    /////////////////////////////////////

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

    /**
     * Warning: MinBin contains full metainformation (fieldnames,..), so its way slower than the other configs.
     * It should be used in case of cross language (e.g. java - javascript) serialization only.
     * Additionally you can read MinBin serialized streams without access to original classes.
     *
     * See MBPrinter on an example on how to read a MinBin stream without having access to
     * original classes. Useful for cross language serialization/long term archiving.
     *
     * Warning: MinBin serialization ('binary JSon') is much slower than the other
     * serialization configurations.
     *
     * @return a configuration to encode MinBin format.
     */
    public static FSTConfiguration createMinBinConfiguration() {
        return createMinBinConfiguration(null);
    }

    protected static FSTConfiguration createMinBinConfiguration(ConcurrentHashMap<FieldKey, FSTClazzInfo.FSTFieldInfo> shared) {
        final FSTConfiguration res = createDefaultConfiguration(shared);
        res.setCrossPlatform(true);
        res.type = ConfType.MINBIN;
        res.setStreamCoderFactory(new MinBinStreamCoderFactory(res));

        // override some serializers
        FSTSerializerRegistry reg = res.serializationInfoRegistry.getSerializerRegistry();
        reg.putSerializer(EnumSet.class, new FSTCPEnumSetSerializer(), true);
        reg.putSerializer(Throwable.class, new FSTCPThrowableSerializer(), true);

        // for crossplatform fallback does not work => register default serializers for collections and subclasses
        reg.putSerializer(AbstractCollection.class, new FSTCollectionSerializer(), true); // subclass should register manually
        reg.putSerializer(AbstractMap.class, new FSTMapSerializer(), true); // subclass should register manually

        res.registerCrossPlatformClassMapping(new String[][]{
                {"map", HashMap.class.getName()},
                {"list", ArrayList.class.getName()},
                {"set", HashSet.class.getName()},
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
        res.registerSerializer( BigDecimal.class, new FSTJSonSerializers.BigDecSerializer(), true );
        return res;
    }

    public static FSTConfiguration
    createJsonConfiguration() {
        return createJsonConfiguration(false, true);
    }


    public static FSTConfiguration createJsonConfiguration(boolean prettyPrint, boolean shareReferences ) {
        return createJsonConfiguration(prettyPrint,shareReferences,null);
    }

    protected static FSTConfiguration createJsonConfiguration(boolean prettyPrint, boolean shareReferences, ConcurrentHashMap<FieldKey,FSTClazzInfo.FSTFieldInfo> shared ) {
        if ( prettyPrint && shareReferences ) {
            throw new RuntimeException("cannot use prettyPrint with shared refs to 'true'. Set shareRefs to false.");
        }
        return constructJsonConf(prettyPrint, shareReferences, shared);
    }

    public static class JacksonAccessWorkaround extends UTF8JsonGenerator {
        public JacksonAccessWorkaround(IOContext ctxt, int features, ObjectCodec codec, OutputStream out) {
            super(ctxt, features, codec, out);
        }

        public JacksonAccessWorkaround(IOContext ctxt, int features, ObjectCodec codec, OutputStream out, byte[] outputBuffer, int outputOffset, boolean bufferRecyclable) {
            super(ctxt, features, codec, out, outputBuffer, outputOffset, bufferRecyclable);
        }

        public int getOutputTail() {
            return _outputTail;
        }
    }

    private static FSTConfiguration constructJsonConf(boolean prettyPrint, boolean shareReferences, ConcurrentHashMap<FieldKey, FSTClazzInfo.FSTFieldInfo> shared) {
        final FSTConfiguration conf = createMinBinConfiguration(shared);
        conf.type = prettyPrint ? ConfType.JSONPRETTY : ConfType.JSON;
        JsonFactory fac;
        if ( prettyPrint ) {
            fac = new JsonFactory() {
                protected JsonGenerator _createUTF8Generator(OutputStream out, IOContext ctxt) throws IOException {
                    UTF8JsonGenerator gen = new JacksonAccessWorkaround(ctxt,
                            _generatorFeatures, _objectCodec, out);
                    if (_characterEscapes != null) {
                        gen.setCharacterEscapes(_characterEscapes);
                    }
                    SerializableString rootSep = _rootValueSeparator;
                    if (rootSep != DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR) {
                        gen.setRootValueSeparator(rootSep);
                    }
                    return gen;
                }
                @Override
                public JsonGenerator createGenerator(OutputStream out) throws IOException {
                    return super.createGenerator(out).setPrettyPrinter(new DefaultPrettyPrinter());
                }
            }
            .disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        } else {
            fac = new JsonFactory() {
                protected JsonGenerator _createUTF8Generator(OutputStream out, IOContext ctxt) throws IOException {
                    UTF8JsonGenerator gen = new JacksonAccessWorkaround(ctxt,
                            _generatorFeatures, _objectCodec, out);
                    if (_characterEscapes != null) {
                        gen.setCharacterEscapes(_characterEscapes);
                    }
                    SerializableString rootSep = _rootValueSeparator;
                    if (rootSep != DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR) {
                        gen.setRootValueSeparator(rootSep);
                    }
                    return gen;
                }
            };
            fac.disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
               .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        }
        conf.setCoderSpecific(fac);
        conf.setStreamCoderFactory(new JSonStreamCoderFactory(conf));
        conf.setShareReferences(shareReferences);
        conf.setLastResortResolver(new LastResortClassRessolver() {
            @Override
            public Class getClass(String clName) {
                return Unknown.class;
            }
        });
        return conf;
    }

    /**
     * debug only, very slow (creates config with each call). Creates new conf so custom serializers are ignored.
     *
     * @param o
     */
    public static void prettyPrintJson(Object o) {
        FSTConfiguration conf = constructJsonConf(true, true, null);
        System.out.println(conf.asJsonString(o));
    }
    /**
     *
     * Configuration for use on Android. Its binary compatible with getDefaultConfiguration().
     * So one can write on server with getDefaultConf and read on mobile client with getAndroidConf().
     *
     * @return
     */
     public static FSTConfiguration createAndroidDefaultConfiguration() {
        return createAndroidDefaultConfiguration(null);
     }

     protected static FSTConfiguration createAndroidDefaultConfiguration(ConcurrentHashMap<FieldKey,FSTClazzInfo.FSTFieldInfo> shared) {
        final Objenesis genesis = new ObjenesisStd();
        FSTConfiguration conf = new FSTConfiguration(shared) {
            @Override
            public FSTClassInstantiator getInstantiator(Class clazz) {
                return new FSTObjenesisInstantiator(genesis,clazz);
            }
        };
        initDefaultFstConfigurationInternal(conf);
        if ( isAndroid ) {
            try {
                conf.registerSerializer(Class.forName("com.google.gson.internal.LinkedTreeMap"), new FSTMapSerializer(), true);
            } catch (ClassNotFoundException e) {
                //silent
            }
            try {
                conf.registerSerializer(Class.forName("com.google.gson.internal.LinkedHashTreeMap"), new FSTMapSerializer(), true);
            } catch (ClassNotFoundException e) {
                //silent
            }
        }
        return conf;
    }

    public static FSTConfiguration createConfiguration(ConfType ct, boolean shareRefs) {
        return createConfiguration(ct,shareRefs);
    }

    protected static FSTConfiguration createConfiguration(ConfType ct, boolean shareRefs,ConcurrentHashMap<FieldKey, FSTClazzInfo.FSTFieldInfo> shared ) {
        FSTConfiguration res;
        switch (ct) {
            case DEFAULT:
                res = createDefaultConfiguration(shared);
                break;
            case MINBIN:
                res = createMinBinConfiguration(shared);
                break;
            case UNSAFE:
                res = createFastBinaryConfiguration(shared);
                break;
            case JSON:
                res = createJsonConfiguration( false, shareRefs, shared);
                break;
            case JSONPRETTY:
                res = createJsonConfiguration( true, shareRefs, shared);
                break;
            default:
                throw new RuntimeException("unsupported conftype for factory method");
        }
        res.setShareReferences(shareRefs);
        return res;
    }
    /**
     * the standard FSTConfiguration.
     * - safe (no unsafe r/w)
     * - platform independent byte order
     * - moderate compression
     *
     * note that if you are just read/write from/to byte arrays, its faster
     * to use DefaultCoder.
     *
     * This should be used most of the time.
     *
     * @return
     */
    public static FSTConfiguration createDefaultConfiguration() {
        return createDefaultConfiguration(null);
    }

    protected static FSTConfiguration createDefaultConfiguration(ConcurrentHashMap<FieldKey,FSTClazzInfo.FSTFieldInfo> shared) {
        if (isAndroid) {
            return createAndroidDefaultConfiguration(shared);
        }
        FSTConfiguration conf = new FSTConfiguration(shared);
        return initDefaultFstConfigurationInternal(conf);
    }

    protected static FSTConfiguration initDefaultFstConfigurationInternal(FSTConfiguration conf) {
        conf.addDefaultClazzes();
        // serializers
        FSTSerializerRegistry reg = conf.getCLInfoRegistry().getSerializerRegistry();
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
        reg.putSerializer(ArrayList.class, new FSTArrayListSerializer(), false);
        reg.putSerializer(Vector.class, new FSTCollectionSerializer(), false);
        reg.putSerializer(LinkedList.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(HashSet.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(HashMap.class, new FSTMapSerializer(), false); // subclass should register manually
        reg.putSerializer(LinkedHashMap.class, new FSTMapSerializer(), false); // subclass should register manually
        reg.putSerializer(Hashtable.class, new FSTMapSerializer(), true);
        reg.putSerializer(ConcurrentHashMap.class, new FSTMapSerializer(), true);
        reg.putSerializer(FSTStruct.class, new FSTStructSerializer(), true);

        // serializers for classes failing in fst JDK emulation (e.g. Android<=>JDK)
        reg.putSerializer(BigInteger.class, new FSTBigIntegerSerializer(), true);
        return conf;
    }

    /**
     * Returns a configuration using Unsafe to read write data.
     * - platform dependent byte order
     * - no value compression attempts
     * - makes heavy use of Unsafe, which can be dangerous in case
     *   of version conflicts
     *
     * Use only in case it makes a significant difference and you absolutely need the performance gain.
     * Performance gains depend on data. There are cases where this is even slower,
     * in some scenarios (many native arrays) it can be several times faster.
     * see also OffHeapCoder, OnHeapCoder.
     *
     */
    public static FSTConfiguration createFastBinaryConfiguration() {
        return createFastBinaryConfiguration(null);
    }

    protected static FSTConfiguration createFastBinaryConfiguration(ConcurrentHashMap<FieldKey, FSTClazzInfo.FSTFieldInfo> shared) {
        if ( isAndroid )
            throw new RuntimeException("not supported under android platform, use default configuration");
        final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration(shared);
        conf.type = ConfType.UNSAFE;
        conf.setStreamCoderFactory(new FBinaryStreamCoderFactory(conf));
        return conf;
    }

    /**
     * register a custom serializer for a given class or the class and all of its subclasses.
     * Serializers must be configured identical on read/write side and should be set before
     * actually making use of the Configuration.
     *
     * @param clazz
     * @param ser
     * @param alsoForAllSubclasses
     */
    public void registerSerializer(Class clazz, FSTObjectSerializer ser, boolean alsoForAllSubclasses ) {
        serializationInfoRegistry.getSerializerRegistry().putSerializer(clazz, ser, alsoForAllSubclasses);
    }

    public boolean isForceClzInit() {
        return forceClzInit;
    }

    public LastResortClassRessolver getLastResortResolver() {
        return lastResortResolver;
    }

    public void setLastResortResolver(LastResortClassRessolver lastResortResolver) {
        this.lastResortResolver = lastResortResolver;
    }

    /**
     * always execute default fields init, even if no transients (so would get overwritten anyway)
     * required for lossy codecs (kson)
     *
     * @param forceClzInit
     * @return
     */
    public FSTConfiguration setForceClzInit(boolean forceClzInit) {
        this.forceClzInit = forceClzInit;
        return this;
    }

    public FSTClassInstantiator getInstantiator(Class clazz) {
        return instantiator;
    }

    public void setInstantiator(FSTClassInstantiator instantiator) {
        this.instantiator = instantiator;
    }

    public <T> T getCoderSpecific() {
        return (T) coderSpecific;
    }

    public void setCoderSpecific(Object coderSpecific) {
        this.coderSpecific = coderSpecific;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * special configuration used internally for struct emulation
     * @return
     */
    public static FSTConfiguration createStructConfiguration() {
        FSTConfiguration conf = new FSTConfiguration(null);
        conf.setStructMode(true);
        return conf;
    }

    protected FSTConfiguration(ConcurrentHashMap<FieldKey,FSTClazzInfo.FSTFieldInfo> sharedFieldInfos) {
        this.fieldInfoCache = sharedFieldInfos;
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

    /**
     * ignored currently
     * @return
     */
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
//     */
    public FSTConfiguration setForceSerializable(boolean forceSerializable) {
        this.forceSerializable = forceSerializable;
        return this;
    }

    /**
     * clear global deduplication caches. Useful for class reloading scenarios, else counter productive as
     * j.reflect.Fiwld + Construtors will be instantiated more than once per class.
     */
    public static void clearGlobalCaches() {
        FSTClazzInfo.sharedFieldSets.clear();
        FSTDefaultClassInstantiator.constructorMap.clear();
    }

    /**
     * clear cached softref's and ThreadLocal.
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
     *
     * Cycles and Objects referenced more than once will not be detected (if set to false).
     * Additionally JDK compatibility is not supported (read/writeObject and stuff). Use case is highperformance
     * serialization of plain cycle free data (e.g. messaging). Can perform significantly faster (20-40%).
     *
     * @param shareReferences
     *
     */
    public void setShareReferences(boolean shareReferences) {
        this.shareReferences = shareReferences;
    }

    /**
     *
     * Preregister a class (use at init time). This avoids having to write class names.
     * Its a very simple and effective optimization (frequently > 2 times faster for small objects).
     *
     * Read and write side need to have classes preregistered in the exact same order.
     *
     * The list does not have to be complete. Just add your most frequently serialized classes here
     * to get significant gains in speed and smaller serialized representation size.
     *
     */
    public void registerClass( Class ... c) {
        for (int i = 0; i < c.length; i++) {
            classRegistry.registerClass(c[i],this);
            try {
                Class ac = Class.forName("[L"+c[i].getName()+";");
                classRegistry.registerClass(ac,this);
            } catch (ClassNotFoundException e) {
                // silent
            }
        }
    }

    void addDefaultClazzes() {
        classRegistry.registerClass(String.class,this);
        classRegistry.registerClass(Byte.class,this);
        classRegistry.registerClass(Short.class,this);
        classRegistry.registerClass(Integer.class,this);
        classRegistry.registerClass(Long.class,this);
        classRegistry.registerClass(Float.class,this);
        classRegistry.registerClass(Double.class,this);
        classRegistry.registerClass(BigDecimal.class,this);
        classRegistry.registerClass(BigInteger.class,this);
        classRegistry.registerClass(Character.class,this);
        classRegistry.registerClass(Boolean.class,this);
        classRegistry.registerClass(TreeMap.class,this);
        classRegistry.registerClass(HashMap.class,this);
        classRegistry.registerClass(ArrayList.class,this);
        classRegistry.registerClass(ConcurrentHashMap.class,this);
        classRegistry.registerClass(URL.class,this);
        classRegistry.registerClass(Date.class,this);
        classRegistry.registerClass(java.sql.Date.class,this);
        classRegistry.registerClass(SimpleDateFormat.class,this);
        classRegistry.registerClass(TreeSet.class,this);
        classRegistry.registerClass(LinkedList.class,this);
        classRegistry.registerClass(SimpleTimeZone.class,this);
        classRegistry.registerClass(GregorianCalendar.class,this);
        classRegistry.registerClass(Vector.class,this);
        classRegistry.registerClass(Hashtable.class,this);
        classRegistry.registerClass(BitSet.class,this);
        classRegistry.registerClass(Locale.class,this);

        classRegistry.registerClass(StringBuffer.class,this);
        classRegistry.registerClass(StringBuilder.class,this);

        classRegistry.registerClass(Object.class,this);
        classRegistry.registerClass(Object[].class,this);
        classRegistry.registerClass(Object[][].class,this);
        classRegistry.registerClass(Object[][][].class,this);

        classRegistry.registerClass(byte[].class,this);
        classRegistry.registerClass(byte[][].class,this);

        classRegistry.registerClass(char[].class,this);
        classRegistry.registerClass(char[][].class,this);

        classRegistry.registerClass(short[].class,this);
        classRegistry.registerClass(short[][].class,this);

        classRegistry.registerClass(int[].class,this);
        classRegistry.registerClass(int[][].class,this);

        classRegistry.registerClass(float[].class,this);
        classRegistry.registerClass(float[][].class,this);

        classRegistry.registerClass(double[].class,this);
        classRegistry.registerClass(double[][].class,this);

    }

    public FSTClazzNameRegistry getClassRegistry() {
        return classRegistry;
    }

    public FSTClazzInfoRegistry getCLInfoRegistry() {
        return serializationInfoRegistry;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public FSTClazzInfo getClassInfo(Class type) {
        return serializationInfoRegistry.getCLInfo(type, this);
    }

    /**
     * utility for thread safety and reuse. Do not close the resulting stream. However you should close
     * the given InputStream 'in'
     * @param in
     * @return
     */
    public FSTObjectInput getObjectInput( InputStream in ) {
        FSTObjectInput fstObjectInput = getIn();
        try {
            fstObjectInput.resetForReuse(in);
            return fstObjectInput;
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    public FSTObjectInput getObjectInput() {
        return getObjectInput((InputStream)null);
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
        FSTObjectInput fstObjectInput = getIn();
        try {
            fstObjectInput.resetForReuseUseArray(arr,len);
            return fstObjectInput;
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    /**
     * take the given array and copy it to input. the array IS copied
     * @param arr
     * @param len
     * @return
     */
    public FSTObjectInput getObjectInputCopyFrom( byte arr[],int off, int len ) {
        FSTObjectInput fstObjectInput = getIn();
        try {
            fstObjectInput.resetForReuseCopyArray(arr, off, len);
            return fstObjectInput;
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }


    protected FSTObjectInput getIn() {
        FSTObjectInput fstObjectInput = (FSTObjectInput) streamCoderFactory.getInput().get();
        if ( fstObjectInput == null ) {
            streamCoderFactory.getInput().set(new FSTObjectInput(this));
            return getIn();
        }
        fstObjectInput.conf = this;
        fstObjectInput.getCodec().setConf(this);
        return fstObjectInput;
    }

    protected FSTObjectOutput getOut() {
        FSTObjectOutput fstOut = (FSTObjectOutput) streamCoderFactory.getOutput().get();
        if ( fstOut == null || fstOut.closed ) {
            streamCoderFactory.getOutput().set(new FSTObjectOutput(this));
            return getOut();
        }
        fstOut.conf = this;
        fstOut.getCodec().setConf(this);
        return fstOut;
    }

    /**
     * utility for thread safety and reuse. Do not close the resulting stream. However you should close
     * the given OutputStream 'out'
     * @param out - can be null (temp bytearrays stream is created then)
     * @return
     */
    public FSTObjectOutput getObjectOutput(OutputStream out) {
        FSTObjectOutput fstObjectOutput = getOut();
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
        FSTObjectOutput fstObjectOutput = getOut();
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
     * special for structs
     * @return
     */
    public boolean isStructMode() {
        return serializationInfoRegistry.isStructMode();
    }

    public FSTClazzInfo getClazzInfo(Class rowClass) {
        return getCLInfoRegistry().getCLInfo(rowClass, this);
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
        ThreadLocal getInput();
        ThreadLocal getOutput();
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
            minBinNamesBytez.put(fulLQName, binary);
        } finally {
            cplock.set(false);
        }
    }

    public byte[] getCrossPlatformBinaryCache(String symbolicName) {
        try {
            while ( cplock.compareAndSet(false, true)) { } // spin
            return minBinNamesBytez.get(symbolicName);
        } finally {
            cplock.set(false);
        }
    }

    /**
     * init right after creation of configuration, not during operation as it is not threadsafe regarding mutation
     * currently only for minbin serialization
     *
     * @param keysAndVals { { "symbolicName", "fullQualifiedClazzName" }, .. }
     */
    public FSTConfiguration registerCrossPlatformClassMapping( String[][] keysAndVals ) {
        for (int i = 0; i < keysAndVals.length; i++) {
            String[] keysAndVal = keysAndVals[i];
            registerCrossPlatformClassMapping(keysAndVal[0], keysAndVal[1]);
        }
        return this;
    }

    public FSTConfiguration registerCrossPlatformClassMapping( String shortName,  String fqName ) {
        minbinNames.put(shortName, fqName);
        minbinNamesReverse.put(fqName, shortName);
        return this;
    }

    /**
     * shorthand for registerCrossPlatformClassMapping(_,_)
     * @param shortName - class name in json type field
     * @param clz - class
     * @return
     */
    public FSTConfiguration cpMap(String shortName, Class clz) {
        return registerCrossPlatformClassMapping(shortName,clz.getName());
    }

    /**
     * init right after creation of configuration, not during operation as it is not threadsafe regarding mutation
     */
    public FSTConfiguration registerCrossPlatformClassMappingUseSimpleName( Class ... classes ) {
        registerCrossPlatformClassMappingUseSimpleName(Arrays.asList(classes));
        return this;
    }

    public FSTConfiguration registerCrossPlatformClassMappingUseSimpleName( List<Class> classes ) {
        for (int i = 0; i < classes.size(); i++) {
            Class clz = classes.get(i);
            minbinNames.put(clz.getSimpleName(), clz.getName());
            minbinNamesReverse.put(clz.getName(), clz.getSimpleName());
            try {
                if (!clz.isArray() ) {
                    Class ac = Class.forName("[L"+clz.getName()+";");
                    minbinNames.put(clz.getSimpleName()+"[]", ac.getName());
                    minbinNamesReverse.put(ac.getName(), clz.getSimpleName()+"[]");
                }
            } catch (ClassNotFoundException e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }
        }
        return this;
    }

    /**
     * get cross platform symbolic class identifier
     * @param cl
     * @return
     */
    public String getCPNameForClass( Class cl ) {
        String res = minbinNamesReverse.get(cl.getName());
        if (res == null) {
            if (cl.isAnonymousClass()) {
                return getCPNameForClass(cl.getSuperclass());
            }
            return cl.getName();
        }
        return res;
    }

    public String getClassForCPName( String name ) {
        String res = minbinNames.get(name);
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
            System.out.println("unable to decode:" +new String(b,0) );
            try {
                getObjectInput(b).readObject();
            } catch (Exception e1) {
                // debug hook
            }
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    /**
     * convenience. (object must be serializable)
     */
    public byte[] asByteArray( Object object ) {
        FSTObjectOutput objectOutput = getObjectOutput();
        try {
            objectOutput.writeObject(object);
            return objectOutput.getCopyOfWrittenBuffer();
        } catch (IOException e) {
            try {
//                FSTConfiguration.prettyPrintJson(object); endless cycle !
            } catch (Exception ee) {
                //
            }
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    /**
     * Warning: avoids allocation + copying.
     * The returned byteArray is a direct pointer to underlying buffer.
     * the int length[] is expected to have at least on element.
     * The buffer can be larger than written data, therefore length[0] will contain written length.
     *
     * The buffer content must be used (e.g. sent to network, copied to offheap) before doing another
     * asByteArray on the current Thread.
     */
    public byte[] asSharedByteArray( Object object, int length[] ) {
        FSTObjectOutput objectOutput = getObjectOutput();
        try {
            objectOutput.writeObject(object);
            length[0] = objectOutput.getWritten();
            return objectOutput.getBuffer();
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    /**
     * utility/debug method. Use "asByteArray" for programmatic use as the
     * byte array will already by UTF-8 and ready to be sent on network.
     *
     * @param o
     * @return
     */
    public String asJsonString(Object o) {
        if ( getCoderSpecific() instanceof JsonFactory == false ) {
            return "can be called on JsonConfiguration only";
        } else {
            try {
                return new String(asByteArray(o),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }
        }
        return null;
    }

    /**
     * helper to write series of objects to streams/files > Integer.MAX_VALUE.
     * it
     *  - serializes the object
     *  - writes the length of the serialized object to the stream
     *  - the writes the serialized object data
     *
     * on reader side (e.g. from a blocking socketstream, the reader then
     *  - reads the length
     *  - reads [length] bytes from the stream
     *  - deserializes
     *
     * @see decodeFromStream
     *
     * @param out
     * @param toSerialize
     * @throws IOException
     */
    public void encodeToStream( OutputStream out, Object toSerialize ) throws IOException {
        FSTObjectOutput objectOutput = getObjectOutput(); // could also do new with minor perf impact
        objectOutput.writeObject(toSerialize);
        int written = objectOutput.getWritten();
        out.write((written >>> 0) & 0xFF);
        out.write((written >>> 8) & 0xFF);
        out.write((written >>> 16) & 0xFF);
        out.write((written >>> 24) & 0xFF);

        // copy internal buffer to bufferedoutput
        out.write(objectOutput.getBuffer(), 0, written);
        objectOutput.flush();
    }

    /**
     * @see encodeToStream
     *
     * @param in
     * @return
     * @throws Exception
     */
    public Object decodeFromStream( InputStream in ) throws Exception {
        int read = in.read();
        if ( read < 0 )
            throw new EOFException("stream is closed");
        int ch1 = (read + 256) & 0xff;
        int ch2 = (in.read()+ 256) & 0xff;
        int ch3 = (in.read() + 256) & 0xff;
        int ch4 = (in.read() + 256) & 0xff;
        int len = (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
        if ( len <= 0 )
            throw new EOFException("stream is corrupted");
        byte buffer[] = new byte[len]; // this could be reused !
        while (len > 0) {
            len -= in.read(buffer, buffer.length - len, len);
        }
        return getObjectInput(buffer).readObject();
    }

    /**
     * @return a configzration sharing as much as possible state with the callee. Its valid to register
     * different serializers at the derived configzration obtained.
     */
    public FSTConfiguration deriveConfiguration() {
        if ( fieldInfoCache == null ) {
            fieldInfoCache = new ConcurrentHashMap<>();
        }
        FSTConfiguration derived = createConfiguration(type, shareReferences, fieldInfoCache);
        // try to share as much as possible to save memory
        // note: the creation of those objects in createConfiguration() is unnecessary,
        // would need a specials lean creation method to avoid that (+init overhead)
        //
        // still no good test coverage. Problematic distribution of state and references all across the
        // code (to reduce pointer chasing) makes it problematic to implement stuff like this (errors might occur on nasty edge cases)
        derived.fieldInfoCache = fieldInfoCache;

        // sharing does not work. need manual clean up
//        derived.output = output;
//        derived.input = input;

//        cannot derive => hard link to conf in anonymous
//        derived.streamCoderFactory = streamCoderFactory;
//        derived.instantiator = instantiator;
//        derived.lastResortResolver = lastResortResolver;

        // avoid concurrent registering later on !
        derived.minbinNames = minbinNames;
        derived.minBinNamesBytez = minBinNamesBytez;
        derived.minbinNamesReverse = minbinNamesReverse;

        // errors with websockets ..
//        derived.classRegistry = classRegistry;
        return derived;
    }

    @Override
    public String toString() {
        return "FSTConfiguration{" +
                   "name='" + name + '\'' +
                   '}';
    }

    protected static class MinBinStreamCoderFactory implements StreamCoderFactory {
        private final FSTConfiguration conf;

        public MinBinStreamCoderFactory(FSTConfiguration conf) {
            this.conf = conf;
        }

        @Override
        public FSTEncoder createStreamEncoder() {
            return new FSTMinBinEncoder(conf);
        }

        @Override
        public FSTDecoder createStreamDecoder() {
            return new FSTMinBinDecoder(conf);
        }

        static ThreadLocal input = new ThreadLocal();
        static ThreadLocal output = new ThreadLocal();

        @Override
        public ThreadLocal getInput() {
            return input;
        }

        @Override
        public ThreadLocal getOutput() {
            return output;
        }
    }

    protected static class FSTDefaultStreamCoderFactory implements FSTConfiguration.StreamCoderFactory {
        private FSTConfiguration fstConfiguration;

        public FSTDefaultStreamCoderFactory(FSTConfiguration fstConfiguration) {this.fstConfiguration = fstConfiguration;}

        @Override
        public FSTEncoder createStreamEncoder() {
            return new FSTStreamEncoder(fstConfiguration);
        }

        @Override
        public FSTDecoder createStreamDecoder() {
            return new FSTStreamDecoder(fstConfiguration);
        }

        static ThreadLocal input = new ThreadLocal();
        static ThreadLocal output = new ThreadLocal();

        @Override
        public ThreadLocal getInput() {
            return input;
        }

        @Override
        public ThreadLocal getOutput() {
            return output;
        }

    }

    protected static class JSonStreamCoderFactory implements StreamCoderFactory {
        protected final FSTConfiguration conf;

        public JSonStreamCoderFactory(FSTConfiguration conf) {
            this.conf = conf;
        }

        @Override
        public FSTEncoder createStreamEncoder() {
            return new FSTJsonEncoder(conf);
        }

        @Override
        public FSTDecoder createStreamDecoder() {
            return new FSTJsonDecoder(conf);
        }

        static ThreadLocal input = new ThreadLocal();
        static ThreadLocal output = new ThreadLocal();

        @Override
        public ThreadLocal getInput() {
            return input;
        }

        @Override
        public ThreadLocal getOutput() {
            return output;
        }
    }

    protected static class FBinaryStreamCoderFactory implements StreamCoderFactory {
        protected final FSTConfiguration conf;

        public FBinaryStreamCoderFactory(FSTConfiguration conf) {
            this.conf = conf;
        }

        @Override
        public FSTEncoder createStreamEncoder() {
            return new FSTBytezEncoder(conf, new HeapBytez(new byte[4096]));
        }

        @Override
        public FSTDecoder createStreamDecoder() {
            return new FSTBytezDecoder(conf);
        }

        static ThreadLocal input = new ThreadLocal();
        static ThreadLocal output = new ThreadLocal();

        @Override
        public ThreadLocal getInput() {
            return input;
        }

        @Override
        public ThreadLocal getOutput() {
            return output;
        }

    }
}
