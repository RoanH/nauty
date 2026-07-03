# nauty
This is a limited scope high performance Java port of [nauty and Traces](https://pallini.di.uniroma1.it/), primarily focussed on the graph canonical label computation logic rather than general automorphism group computation. The main goal of this port is to replace the JNI bindings I wrote for my [CPQ-native Index](https://github.com/RoanH/CPQ-native-index) project so it becomes easier to integrate into my more general graph database library [gMark](https://github.com/RoanH/gMark) and to remove the need for a C compiler (particularly on Windows). For my particular workload with a huge volume of primarily small graphs to canonicalise eliminating the JNI and data marshalling overhead also results in a significant runtime performance gain of around 10%. Some of the C specific performance optimalisations have been replaced with ones better suited for Java.

The current port hard codes many of the configurations options supported by the by the original version of nauty, notably:

- Only sparse nauty is available.
- Only digraphs are supported.
- Canonisation is always enabled.
- The random schreier method is not implemented.

## Maven artifact [![Maven Central](https://img.shields.io/maven-central/v/dev.roanh.nauty/nauty)](https://mvnrepository.com/artifact/dev.roanh.nauty/nauty)
nauty is available on Maven central as [an artifact](https://mvnrepository.com/artifact/dev.roanh.nauty/nauty) so it can be included directly in another Java project using Gradle or Maven. A hosted version of the javadoc for nauty can be found at [nauty.docs.roanh.dev](https://nauty.docs.roanh.dev/), though note that many original nauty methods are not documented.

##### Gradle 
```groovy
repositories{
	mavenCentral()
}

dependencies{
	implementation 'dev.roanh.nauty:nauty:1.0'
}
```

##### Maven
```xml
<dependency>
	<groupId>dev.roanh.nauty</groupId>
	<artifactId>nauty</artifactId>
	<version>1.0</version>
</dependency>
```

## Nauty API
A more Java style API is provided via the [NautyApi](nauty/src/dev/roanh/nauty/api/NautyApi.java) class, though currently there is only one method. Instances of Nauty/NautyApi are not thread safe, but distinct instances have dedicated working memory and state so the library can be used in multi-threaded scenario's.

## License
Respecting the original license choice for nauty, this library is also released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Development of nauty
This repository contain an [Eclipse](https://www.eclipse.org/) & [Gradle](https://gradle.org/) project. Development work can be done using the Eclipse IDE or using any other Gradle compatible IDE. Unit testing is employed to test core functionality, CI will also check for regressions using these tests. A hosted version of the javadoc for nauty can be found at [nauty.docs.roanh.dev](https://nauty.docs.roanh.dev/), though note that many original nauty methods are not documented.

## History
Project development started: 16th of May, 2026.
