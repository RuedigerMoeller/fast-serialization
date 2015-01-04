fast-serialization
==================

* 100% JDK Serialization compatible drop-in replacement (Ok, might be 99% ..).
* OffHeap Maps, Persistent OffHeap maps
* FSTStructs is very similar to IBM's packed objects. Difference is: You can run it with Oracle JDK today.
* should be Android compatible since version >= 2.17
* Apache 2.0 license since 2.17


Project Page http://ruedigermoeller.github.io/fast-serialization/  (out of pure childishness)

###[For serialization: Read at least first chapter !](https://github.com/RuedigerMoeller/fast-serialization/wiki/Serialization)

###[Full Documentation](https://github.com/RuedigerMoeller/fast-serialization/wiki)

###mvn

**note:** maven.org might lag 1 day behind after releasing.

2.0 version
```.xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>2.19</version>
</dependency>
```

Older version (slightly faster, different package name, 1.6 compatible ..)
```.xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>1.63</version>
</dependency>
```

###how to build 

* master contains dev branch/trunk.
* 1.x contains old version (still maintained)
* The maven build should work out of the box and reproduces the artifact hosted on maven.org
* To use the gradle build, you need to configure the proxy server in settings.properties (or just set empty if you do not sit behind a proxy).

<b>Note</b> that instrumentation done for fst-structs works only if debug info is turned on during compile. Reason is that generating methods at runtime with javassist fails (probably a javassist bug ..). 
<b>This does not affect the serialization implementation. </b>

<b>JDK 1.6 Build</b>
1.x build since v1.62 are still jdk 6 compatible
