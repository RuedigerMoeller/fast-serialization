fast-serialization
==================


Project Page http://ruedigermoeller.github.io/fast-serialization/  (out of pure childishness)

##Documentation

[Quick Start](https://github.com/RuedigerMoeller/fast-serialization/wiki/Serialization)
[All Pages](https://github.com/RuedigerMoeller/fast-serialization/wiki)
this project was hosted in googlecode, documentation is still not fully moved. (see http://fast-serialization.googlecode.com)

###how to build 

* The maven build should work out of the box and reproduces the artifact hosted on maven.org
* To use the gradle build, you need to configure the proxy server in settings.properties (or just set empty if you do not sit behind a proxy).

<b>Note</b> that instrumentation done for fst-structs works only if debug info is turned on during compile. Reason is that generating methods at runtime with javassist fails (probably a javassist bug ..). 
<b>This does not affect the serialization implementation. </b>
