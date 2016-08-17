### Almost Realism Digital Artist Libraries ###

🔶 Paused  🔵 Active  🍏 New  💜 Planned

| | Tool | Description | More Info & Help |
|---|---|---|---|
|🔶| **Common** | General logging, error handling, IO and multimedia tools. | Requires **Jackson Databind**, **Trove**, **JEP**, and **Log4J**. Required for all Tools. |
|🔵| **GraphPersist** | Expands on Apache's BeanUtils to provide simple persistence for POJOs with no annotations required. When used with lambdas in Java 1.8, the resulting code is clean and usually maintainable by the data science team rather than the software engineers, allowing developers to get back to the fun stuff and forget talking to DBAs all the time. | Requires **Common** and **C3PO**. |
|🔵| **TreeView** | Service for displaying tree data in the browser. | Requires **Common**. |
|🔶| **MatrixView** | Service for displaying tabular data in the browser. | Requires **Common**. |
|🔶| **FlowTree** | Distributed parallel processing system for computer graphics, physics and more. | Requires **Common**. |
|🔶| **Rings** | Pure Java Raytracer and related graphics libraries. | Requires **FlowTree** and **TreeView**. |
|🔶| **PhotonField** | Pure Java Pathtracer. | Requires **Rings**. |
|🍏| **Explorer** | Pure Java rigid body physics simulator, particle system generator and terrain generator. | Requires **Rings**. |
|🍏| **Replicator** | 3D object generator for recursive, generative, and fractal structures. | Requires **Rings**. |
|🔵| **FeedGrowth** | Feedback audio generator. | Requires **Replicator**. |
|🔶| **GlitchFarm** | Granular audio synthesizer. | Requires **FeedGrowth**. |
|💜| **Metamerise** | Embedded device operations for interactive art installations. | Requires **FeedGrowth**. |


If you want to use the tools provided here, or you are interested in the concept of open source
art pieces and want to contribute please contact ashesfall@almostrealism.com for help.

```
#!java

Copyright 2016 Michael Murray

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

### More Details ###

The simplicity of these libraries will hopefully make up for the fact that I have limitted time to write documentation. Please feel free to contact me with questions if you have them.

#### GraphPersist ####

All GraphPersist queries for the running JVM are stored in a root QueryLibrary. You can add queries in just two lines of code which specify the mapping of columns to bean properties and a factory lambda to construct the object which will contain the results.

```
#!java
InputStream fieldMap = QueryLibraryTest.class.getResourceAsStream("TestEntity.properties");
QueryLibrary.root().addQuery(TestEntity.class, SQLSelect.prepare(fieldMap,
									() -> { return new TestEntity(); }));
```