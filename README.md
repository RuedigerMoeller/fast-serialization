fast-serialization
==================


this project was hosted in googlecode, documentation is still not fully moved. (see http://fast-serialization.googlecode.com)

  * **Fast Serialization** reimplements Java Serialization with focus on speed, size and compatibility. This allows the use of FST with minimal code change. 
  * **FSTStructs** implements a struct emulation to avoid de-/encoding completely. Use case is high performance message oriented software, huge (>32GB) of static offheap data, data exchange with other languages, reduction of FullGC by 'flattening' or offheaping complex object graphs, Control of data locality (CPU cache friendly) for high performance computational tasks, allocation free java programs. 

#### features:

  * Faster serialization and smaller object size. 
  * **drop-in replacement**. Does not require special getters/setters/Constructors/Interfaces to serialize a class. Extends Outputstream, implements `ObjectInput/ObjectOutput`. Few code changes required.
  * Full support of JDK-serialization features such as Externalizable writeObject/readObject/readReplace/validation/putField/getField, hooks etc.. If an object is serializable with JDK, it should be serializable with FST without any further work.
  * preserves links inside the serialized object graph same as JDK default serialization
  * custom optimization using *annotations*, custom serializers
  * conditional decoding (skip decoding parts of an object/stream in case)

Supports java 1.7+
  * Fast serialization should be adaptable to 1.6 (no use of 1.7 features)
  * FST-structs does require 1.7 API

#### Maven, Download

download non-maven build at the releases section:
https://github.com/RuedigerMoeller/fast-serialization/releases/

Note: fixes+updates are delivered with some delay to maven central, check downloads first
for newest version
```xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>1.40</version>
</dependency>
```

#### State of this project

FST Serialization should be pretty stable and JDK-serialization compatible today. It is used in a large distributed [high performance application](http://java-is-the-new-c.blogspot.de/2013/12/big-data-reactive-way.html). Additionally a lot of remaining corner cases (mostly JDK-serialization-compatibility-issues) have been fixed when replacing JDK-serialization in a prime-faces webflow webapp (lot's of rarely used constructs in there).

#### Documentation

see [Wiki](https://github.com/RuedigerMoeller/fast-serialization/wiki), some documents are still not moved/ported from https://code.google.com/p/fast-serialization/. Read them there.

Also see this [blog post](http://java-is-the-new-c.blogspot.de/2013/10/still-using-externalizable-to-get.html).

#### Limits
  * specialized on java-to-java, no inter language operability.
  * no support for versioning. Reader and writer need to have identical versions of the classes serialized.
  * reads/writes to a temporary buffer in memory to speed up things. This might impose a problem when reading/writing huge objects > 100MB
  * emulating JDK built in `Externalizable` is somewhat tricky. FST needs to speculate on the maximum size of a single externalizable instance. Default setting is 8kb. In case you use `Externalizable` and write large binary data from one single `Externalizable` instance without return control to the serializer, you have to increase both `externalReadAhead` and `externalWriteAhead` at the `FSTObjectOutput` and `FSTObjectInput` class. You might also consider to just replace `Externalizable` by `Serializable`. In fact FST frequently does `Serializable` faster than built in JDK `Externalizable` implementations.

#### Benchmark

I measure different typical use cases (both classes with lots of native data and structural complex object graphs). The benchmarks assures, that only serialization speed is measured. Creation of `ByteArrayStreams` etc. is not included in the benchmark times. Additionally for each library best efforts are made to use them in the best possible way (e.g. reuse heavy weight objects if possible).

[current results](https://github.com/RuedigerMoeller/fast-serialization/wiki/Benchmark)
<br><br>

### History

**newer releases are documented at releases section here. Below old history kept for the records**

*V 1.37* (26-Feb-2014)

 Many bugfixes in serialization
 * fixed issue with serialize/deserialize inbetween 32bit and 64bit VM's
 * unsafe turned off by default (use -Dfst.unsafe=true to enable, be careful. Only use with well tested software !)
 * fix: order of serialization could mess up when encoding legacy style serialized objects
 * fix: classes which only contain boolean fields caused errors
 * fix: void.class is also a primitive class
 * Issue 23 sample app runs (hardcore spring webflow serialization with tons of corner cases)

*V 1.36* (19-Jan-2014)

 * Serialization: Fix for Issue 21 (multidimensional arrays with null value array )

*V 1.35* (02-Jan-2014)

 Serialization: 
 * Fix: Serializing LinkedHashMap caused NPE
 * Fix: Reading Enumeration with elements consisting of anonymous classes failed
 * Fix: cached byte array size capped at 10MB to avoid OOM when serializing > 100MB object graph at once

 Structs:
 * Minor stuff related to other projects. Improved classloaders, StructSerializer


*V 1.34* (02-Dec-2013)

 * Serialization: fixed minor issues
 * New "Bytez" + "{{{BytezAllocator}}}" memory abstraction (similar to bytebuffer). Currently real off-heap ({{{MallocBytez}}}) and on-heap byte-arrays ({{{HeapBytez}}}) are supported. Warning: Bytez does not do bounds check (even on heap).
 * Structs: Using "Bytez" abstraction, structs can be created on- and off-heap. Beware, off heap memory must be freed explicitely (see {{{MallocBytezAllocator}}}). Off-heap structs can be created using new Constructors on the {{{FSTStructAllocator}}} class.

*V 1.28* (21-Oct-2013)

 * fixed Issue 15 and Issue 16 

*V 1.27* (10-Oct-2013)

 * mavenized

*V 1.26* (10-Oct-2013)

 performance fix: due to a invalid byte[] allocation, read performance degraded for small objects (<500 bytes) and when FSTObjectInput was new'ed instead of using recommended FSTConfiguration.getObjectInput.

*V 1.25* (08-Oct-2013)

 hotfix: Argh! IndexOutOfBounds in rare cases ..

*V 1.23/1.24* (07-Oct-2013)

Serialization

 * v1.24: 32 years after the release of VC20, modulo ist still an expensive operation even with a jit'ed language running on virtual cores reordering stuff in a whacky 20 stage pipeline ;-)
 * Bugfix: Issue 12 + Testcase.
 * many small optimizations
 * lockless synchronization of shared objects for multithreaded server usage
 * added configuration singleton to enable simple use without having to construct and maintain a {{{FSTConfiguration}}} object
 * unsafe field access is enabled by default now. Reflection has degraded somewhat in newer 1.7 updates. Never had a problem with unsafe field access in production systems. On platforms without unsafe, FST falls back to reflection. Use of unsafe can be disabled manually.
 * general code cleanup+some more comments
 * Upgraded Kryo version for Benchmarks to 2.2.2.  

FST-Struct:

 * {{{FSTStructAllocator}}} has been split into a generic allocator and a template-bound allocator class.
 * smaller fixes. Basics are pretty stable and used in various other projects of significant size.
 * helper methods on {{{FSTAllocator}}} to ease creation of wrapper objects ('pointer'). 

 
*V 1.22* (08-Sep-2013)

 * Bugfix: When reusing {{{FSTObjectInput}}} with an offset'ed byte array, instance id's where computed wrong.

*V 1.21* (28-Aug-2013)

 * Serialization: major bugfix I don't wanna talk about ;-). In unsafe mode a reverse directed copy memory copied to source instead of destination, so testcases still reported success (affected {{{int[], float[], long[]}}} when unsafe was enabled).
 * Structs: some new methods at allocator & FSTStruct base class. Wiki documentation update

*V 1.20* (18-Aug-2013)

 * Structs: fields are ordered inside byte array as defined in class
 * Structs: Bug when pointing to a subclass byte array with a wrapper typed as superclass

no changes for serialization

*V 1.19* (17-Aug-2013)

*Serialization*

 *  {{{resetForReuseUseArray(byte bytes[], int off, int len)}}} ignored offset (always defaulted to 0)

*Structs*

 * {{{StructString.equals()}}} had a bug
 * Structs now respect 'volatile' modifier
 * Support for CAS (with special signature methods, see http://code.google.com/p/fast-serialization/source/browse/trunk/src/main/java/de/ruedigermoeller/heapoff/structs/unsafeimpl/FSTStructFactory.java line ~117 for special methods recognized by instrumentation
 

*V 1.18* (12-Aug-2013)

 * FST-Structs: When subclassing struct-classes from another struct class, superclass methods did not get instrumented, so with 1.17 only direct subclasses of FSTStruct worked. Fixed.

*V 1.17* (09-Aug-2013)

 * gtcoleman found+fixed another readResolve issue:
 http://code.google.com/p/fast-serialization/issues/detail?id=9&can=1

*V 1.16* (05-Aug-2013)

 * bugfix. Regression: readResolve, writeReplave were ignored (uncommented)
 * compiled to be 1.6 compatible. Note that the struct emulation will not work with 1.6. FSTSerialization works
 * Initial release of FST-structs struct emulation lib

*V 1.15* (28-Jun-2013)

 * major bugfix. Somehow managed to introduce a regression: 'transient' was ignored.

*V 1.14* (27-Jun-2013)

 * major bugfix. Static's were serialized sometimes which could crash VM if unsafe was enabled. 

*V 1.13* (18-Jun-2013)

 * added "preferSpeedOverSize" option in FST Configuration. int/long are not bit-compressed then allowing direct memory copy instead of iteration. Especially when using serialization in IPC or hi-end LAN's, performance is more important than size. You choose.
 * Fixed FSTOffHeap, was totally broken .. will invest in more testcases here :)
 * some tweaks to win a micro here and there

*V 1.12* (18-Jun-2013)

 * fixed a cyclic flush in special cases (offheap is broken currently .. fix is underway)
 * minor tweaks to improve locality (CPU cache)
 * updated offheap bench

*V 1.11* (17-Jun-2013)

 * fixed failure in Unsafe usage. Depending on class loading order, access violation could happen when unsafe usage was turned on.
 * array read/write with unsafe enabled has been sped up a lot
 * removed Gridgain from performance test (slow, can't redistribute binaries), a result with {{{FST 1.10,Kryo2.21,GridGain4.5}}} is here: http://fast-serialization.googlecode.com/files/result-v1.1.html
 * integer arrays are not compressed by default anymore (to costly with littel results). You need to use @Compress if you want an int array to be compressed. @Plain annotation has been removed.

*V 1.10* (15-Jun-2013)

 * huge improvement in performance. FST outperforms kryo by 20 to 100% now.
 * Multithreading improvements: FSTConfiguration is Threadsafe now and can be shared globally. Utility methods for threadsafe instance management have been added to FSTConfiguration.
 * Reuse of Streams is bugfree now :-)
 * added {{{ClassSerializer}}} contributed by M.Wendel. Thx =)
 * documentation update on multithreading and optimal use of FST pending

*V 1.02* (11-Jun-2013)

 * added {{{@OneOf}}} annotation to signal a certain variable is likely to contain certain strings (faster read, smaller size)
 * fixed issue with minimal/compatible configuration and {{{ConcurrentHashmap}}} with JDK 1.7_21
 * added possibility to install a delegate determining which serializer to choose ({{{FSTConfiguration}}}). Useful when doing specialities such as passing remote object references/proxies 

*V 1.01*

 * major bugfix. Random failure in case of objects with same System.identityHashCode

*V. 1.0*

  * bugfixes
  * use of unsafe is now turned off by default set system property (*-Dfst.unsafe=true*) to let fst use unsafe operations. Consider using unsafe mode in production when your software is stable. The use of unsafe mode speeds up between 0%..20% depending on the structure of serialized objects.
  * ongoing implementation of cross language serialization java <=> c++ <=> python. implementation of this feature is stalled because i'm working on my fast-cast lib currently. Not usable (can only read fst-java streams from python currently)

*V. 0.9*

  * Offheap Queue concurrent encoding/decoding mode (preserving queue order), let them multicore CPUs work !

*V. 0.8*

  * Offheap Queue, Offheap Map, straight Offheap
  * fix for nested externals (performs slowish, further investigation needed)

*V. 07*

  * heap offloading core classes
  * FSTConfiguration can now be used multithreaded (2-10% performance loss in some benchmarks, will be fixed)

*V 0.6*

more annotations, use of unsafe. Removed some inlined methods. Shifted some inlined decoding to Serializers, which gave a small performance loss. Some benches got better, some worse.
*you can turn off usage of Unsafe with java -Dfst.nounsafe=true*

String compression, array compression.

Bug Fixes

*V 0.5*

initial release
Benchmark: http://fast-serialization.googlecode.com/files/result-0.5.html


