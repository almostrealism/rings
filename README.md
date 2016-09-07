# Almost Realism Digital Artist Libraries #

If you want to use the tools provided here, or you are interested in the concept of open source
art pieces and want to contribute please contact ashesfall@almostrealism.com for help.

```
#!java

Copyright 2004-2016 Michael Murray

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

### Latest News ###
| Date   | Tool      | News |
|--------|-----------|------|
| 7/7/16 | **Rings** | Begun work on converting the ray tracer to provide an export to **TensorFlow**. |

The simplicity of these libraries will hopefully make up for the fact that I have limited time to write documentation. Please feel free to contact me with questions if you have them.

### Common ###

**Common** contains useful annotations that help other programmers determine what kind of unit tests are needed for what kind of code.

Types that hold state are annotated with the **ModelEntity** annotation.
```
#!java
@ModelEntity
public class BasicGeometry implements Positioned, Oriented, Scaled {
...
```

Types that are stateless are annotated with the **Stateless** annotation.
```
#!java
@Stateless
public class Intersections {
```

If a type represents a function or set of functions, where the required unit test would be an evaluation of some use cases of the function, it can be annotated with **Function**.

```
#!java
@Function
public class ColorMultiplier implements ColorProducer {
```

While writing tests after developing code directly, the programmer can start with making tests for working with stateful entities and then move on to the stateless entities that reference them.

### GraphPersist ###

All GraphPersist queries for the running JVM are stored in a root QueryLibrary. You can add queries in just two lines of code which specify the mapping of columns to bean properties and a factory lambda to construct the object which will contain the results.

```
#!java
		InputStream fieldMap = QueryLibraryTest.class.getResourceAsStream("TestEntity.properties");
		QueryLibrary.root().addQuery(TestEntity.class, SQLSelect.prepare(
									"select * from testdata where id > 1",
									fieldMap, () -> { return new TestEntity(); }));	
		System.out.println("There are " + QueryLibrary.root().get(getDB(), TestEntity.class).size() + " results";
```

## Show Your Support ##

If you are interested in helping me continue this work, donations can be submitted via [PayPal](https://paypal.me/discomike) and are **appreciated**. I also create music with some of the tools here, and if you are interested in supporting that you can join my [Patreon](https://www.patreon.com/user?u=3646756) page. I am also looking for a full time software job so please make sure to check out my [LinkedIn](https://www.linkedin.com/in/ashesfall) page.

## Package Overview ##
|   | Tool | Description | More Info & Help |
|---|---|---|---|
|💜| **Metamerise** | Embedded device operations for interactive art installations. | Requires **FeedGrowth**. |
|🍎| **GlitchFarm** | Granular audio synthesizer. | Requires **FeedGrowth**. |
|🔵| **FeedGrowth** | Feedback audio generator with included OpenGL visualizer. | Requires **OptimalZoo**, **Replicator** and **PhotonField**. |
|🍏| **Explorer** | Pure Java rigid body physics simulator, particle system generator and terrain generator. | Requires **Replicator**, **OSGEO** and **JEP**. |
|🍏| **Replicator** | 3D object generator for recursive, generative, and fractal structures. | Requires **OptimalZoo** and **Rings**. |
|🔶| **ArtifactDetector** | A tool for automatically detecting artifacts in scanned manuscript images. | Requires **Rings**. |
|🍎| **PhotonField** | Pure Java Pathtracer. | Requires **Rings**. |
|🔶| **Rings** | Pure Java Raytracer and related graphics libraries. | Requires **FlowTree**, **MatrixView** and **JOGL**. |
|🍎| **OptimalZoo** | A system for optimizing the parameters of a health function using a genetic algorithm. | Requires **FlowTree**. |
|🔶| **FlowTree** | Distributed parallel processing system for computer graphics, physics and more. *This project requires a retired apache project (**Slide**) and will be upgraded to use a different WebDav library in the future. Also, this project uses HSQLDB optionally (by default), but can be configured to use any SQL relational database.* | Requires **TreeView**, **HSQLDB** and **Apache Slide**. |
|🔶| **MatrixView** | Service for displaying tabular data in the browser. | Requires **Common**. |
|🔵| **TreeView** | Service for displaying tree data in the browser. | Requires **Common**. |
|🔵| **GraphPersist** | Expands on Apache's BeanUtils to provide simple persistence for POJOs with no annotations required. When used with lambdas in Java 1.8, the resulting code is clean and usually maintainable by the data science team rather than the software engineers, allowing developers to get back to the fun stuff and forget talking to DBAs all the time. | Requires **Common**, **C3PO** and **Apache Bean Utils**. |
|🔶| **Common** | General logging, error handling, IO and multimedia tools. Notably, this contains a lot of conveniences for dealing with common frames of reference in the real world such as spatial relations, temporal relations, image and texture data, etc. | Requires **Jackson Databind**, **Trove** and **Log4J**. |

|   |   |   |   |   |
|---|---|---|---|---|
|🔶 Releasable & Paused | 🔵 Active & Releasable Soon | 🍏 New | 🍎 Neglected & In Need of Collaborators | 💜 Planned |

## Maintainence Schedule ##

Below is a table that indicates how quickly I can release updated version for each tool if reported issues require it or the community requests it.

|   |   |
|---|---|
| **Common** | 2 Weeks |
| **GraphPersist** | 2 Weeks |
| **TreeView** | 4 Weeks |
| **MatrixView** | 10 Weeks |
| **FlowTree** | 10 Weeks |
| **OptimalZoo** | 10 Weeks |
| **Rings** | 12 Weeks |
| **PhotonField** | 12 Weeks |
| **ArtifactDetector** | 15 Weeks |
| **Replicator** | 15 Weeks |
| **Explorer** | 15 Weeks |
| **FeedGrowth** | 18 Weeks |
| **GlitchFarm** | 18 Weeks |
| **Metamerise** | 18 Weeks |