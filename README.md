<a name="readme-top"></a>

[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![Apache License][license-shield]][license-url]

<h3 align="center">Almost Realism Multimedia Generation Framework</h3>

### Goal of the Rings Project

A compact and expressive language for defining multimedia generation pipelines, which use
conventional digital signal processing as well as machine learning, that can be compiled
at runtime to target heterogeneous compute devices both locally and in the cloud.

### To use the libraries

Add Maven Repository:

        <repositories>
                <repository>
                        <id>rings</id>
                        <name>Almost Realism Rings/name>
                        <url>https://maven.pkg.github.com/almostrealism/rings</url>
                        <releases><enabled>true</enabled></releases>
                        <snapshots><enabled>true</enabled></snapshots>
                </repository>
        </repositories>

Add ar-audio dependency:

        <dependency>
            <groupId>org.almostrealism</groupId>
            <artifactId>ar-audio</artifactId>
            <version>0.33</version>
        </dependency>

### Tutorial

To generate multimedia you can first create an **AudioScene**.

```Java
    public class MyMultimediaPipeline {
	    public static void main(String args[]) {
			new MyMultimediaPipeline().runScene();
		}
		
		public void runScene() {
			double bpm = 120.0;
			int sourceCount = 4;
			int delayLayerCount = 3;
			int sampleRate = 44100;
			
			AudioScene scene = new AudioScene<>(null, bpm, sourceCount, delayLayerCount, sampleRate);
		}
    }
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### What are the terms of the LICENSE?

Copyright 2024  Michael Murray

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

<p align="right">(<a href="#readme-top">back to top</a>)</p>


## Contact

Michael Murray - [@ashesfall](https://twitter.com/ashesfall) - michael@almostrealism.com

[![LinkedIn][linkedin-shield]][linkedin-url]

Original Project Link: [https://github.com/almostrealism/rings](https://github.com/almostrealism/rings)

<p align="right">(<a href="#readme-top">back to top</a>)</p>


[contributors-shield]: https://img.shields.io/github/contributors/almostrealism/rings.svg?style=for-the-badge
[contributors-url]: https://github.com/almostrealism/rings/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/almostrealism/rings.svg?style=for-the-badge
[forks-url]: https://github.com/almostrealism/rings/network/members
[stars-shield]: https://img.shields.io/github/stars/almostrealism/rings.svg?style=for-the-badge
[stars-url]: https://github.com/almostrealism/rings/stargazers
[issues-shield]: https://img.shields.io/github/issues/almostrealism/rings.svg?style=for-the-badge
[issues-url]: https://github.com/almostrealism/rings/issues
[license-shield]: https://img.shields.io/github/license/almostrealism/rings.svg?style=for-the-badge
[license-url]: https://github.com/almostrealism/rings/blob/master/LICENSE
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/ashesfall
