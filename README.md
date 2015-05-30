fast-serialization
==================

* 100% JDK Serialization compatible drop-in replacement (Ok, might be 99% ..) up to 10 times faster than JDK
* besides binary serialization, en/decode any Serializable object graph to JSON (incl. shared references) (since 2.29)
* Android compatible since version >= 2.17 (use ```FSTConfiguration.createAndroidConfiguration()``` both on server and client side. The configuration object has to be passed into FSTObjectIn/Output constructors)
* OffHeap Maps, Persistent OffHeap maps
* FSTStructs is very similar to IBM's packed objects. Difference is: You can run it with Oracle JDK today.
* Apache 2.0 license since 2.17

###Docs:

[Fast JDK-compatible Serialization](https://github.com/RuedigerMoeller/fast-serialization/wiki/Serialization)

[Json Serialization](https://github.com/RuedigerMoeller/fast-serialization/wiki/JSON-serialization)

[OffHeap + Persistent Maps](https://github.com/RuedigerMoeller/fast-serialization/wiki/Off-Heap-Maps,-Persistent-Maps)

[MinBin cross platform binary format](https://github.com/RuedigerMoeller/fast-serialization/wiki/MinBin)

[Kson: a JSon extension](https://github.com/RuedigerMoeller/fast-serialization/wiki/KSon)

[Struct Emulation](https://github.com/RuedigerMoeller/fast-serialization/wiki/Structs) (currently not covered by tests)

###mvn

**note:** maven.org might lag 1 day behind after releasing.

2.0 version
```.xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>2.29</version>
</dependency>
```

Older version (different package name, 1.6 compatible ..). Fixes are not backported anymore, unsupported.
```.xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>1.63</version>
</dependency>
```

###how to build 

* master contains dev branch/trunk.
* 1.x contains old version
* The maven build should work out of the box and reproduces the artifact hosted on maven.org
* To use the gradle build, you need to configure the proxy server in settings.properties (or just set empty if you do not sit behind a proxy).

<b>Note</b> that instrumentation done for fst-structs works only if debug info is turned on during compile. Reason is that generating methods at runtime with javassist fails (probably a javassist bug ..). 
<b>This does not affect the serialization implementation. </b>

<b>JDK 1.6 Build</b>
1.x build since v1.62 are still jdk 6 compatible
