fast-serialization
==================

* up to 10 times faster 100% JDK Serialization compatible drop-in replacement (Ok, might be 99% ..). As an example: Lambda Serialization which came with 1.8 worked instantly.
* Android compatible since version >= 2.17 (use ```FSTConfiguration.createAndroidDefaultConfiguration()``` both on server and client side. The configuration object has to be passed into FSTObjectIn/Output constructors)
* OffHeap Maps, Persistent OffHeap maps
* FSTStructs is very similar to IBM's packed objects. Difference is: You can run it with Oracle JDK today.
* optionally en/decode any Serializable object graph to JSON (incl. shared references) (since 2.29) for interop
* Apache 2.0 license since 2.17

### Docs:

[Fast JDK-compatible Serialization](https://github.com/RuedigerMoeller/fast-serialization/wiki/Serialization)

[Json Serialization](https://github.com/RuedigerMoeller/fast-serialization/wiki/JSON-serialization)

[OffHeap + Persistent Maps](https://github.com/RuedigerMoeller/fast-serialization/wiki/Off-Heap-Maps,-Persistent-Maps)

[MinBin cross platform binary format](https://github.com/RuedigerMoeller/fast-serialization/wiki/MinBin)

[Kson: a JSon extension](https://github.com/RuedigerMoeller/fast-serialization/wiki/KSon)

### mvn

**note:** maven.org might lag 1 day behind after releasing.

**3.0.0 version** (requires **java 14**, "--add-modules jdk.incubator.foreign" option on compiler and runtime)

```.xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>3.0.0</version>
</dependency>
```

**2.0 version** (java 8)
```.xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>2.56</version>
</dependency>
```
**jdk1.6** compatible build of fst 2.x 

```.xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>2.48-jdk-6</version>
</dependency>
```

1.x version (different package name, 1.6 compatible ..). Fixes are not backported anymore, unsupported.
```.xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>1.63</version>
</dependency>
```

### Who uses FST ?

I am not actively tracking use, maven.org reports more than **14000** downloads from **6000** distinct IP accesses triggered by maven builds world wide per month.

Notable also:

* used in production in [Eurex Exchange](http://www.eurexchange.com/exchange-en/)'s trading back end's middleware
* [JUptr.io](http://www.juptr.io/)'s distributed system / NLP engine uses FST
* Popular Apache Wicket supplementals use FST to speed up Wicket

![alt tag](https://raw.githubusercontent.com/RuedigerMoeller/fast-serialization/master/fst.png)

### how to build 

* master contains dev branch/trunk.
* 1.x contains old version
* The maven build should work out of the box and reproduces the artifact hosted on maven.org
* To use the gradle build, you need to configure the proxy server in settings.properties (or just set empty if you do not sit behind a proxy).

<b>Note</b> that instrumentation done for fst-structs works only if debug info is turned on during compile. Reason is that generating methods at runtime with javassist fails (probably a javassist bug ..). 
<b>This does not affect the serialization implementation. </b>

<b>JDK 1.6 Build</b>
1.x build since v1.62 are still jdk 6 compatible
