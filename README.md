<a name="readme-top"></a>

[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![Apache License][license-shield]][license-url]

<h3 align="center">Almost Realism Multimedia Generation Framework</h3>

## Goal of the Rings Project

Rings provides a compact and expressive language for defining multimedia generation pipelines,
which use conventional digital signal processing as well as machine learning, that can be
compiled at runtime to target heterogeneous compute devices both locally and in the cloud.
Rings is essentially a kind of headless Digital Audio Workstation (DAW) and rendering engine
for visual and sonic media that can be used either as a framework for experimenting and
creating generative art directly or as a tool to build interactive multimedia applications.

This project is very much a work in progress (contributions are welcome), and since the
documentation is very limited its recommended that you get in touch with us if you want
to use it so that you can get the support that you need one on one. Contact information is
found at the bottom of this document.

## Development Environment

### Prerequisites

- **Java 17**: Required for building and running the project
- **Maven 3.x**: Build tool for the multi-module project
- **Docker & Docker Compose**: For containerized development (optional but recommended)

### Building the Project

This is a multi-module Maven project. To build the entire project:

```bash
mvn clean install
```

To build a specific module:

```bash
mvn clean install -pl audio
mvn clean install -pl audio-space
```

To skip tests during build:

```bash
mvn clean install -DskipTests
```

### Running Tests

Run all tests:

```bash
mvn test
```

Run tests for a specific module:

```bash
mvn test -pl audio
```

Run a single test class:

```bash
mvn test -pl audio -Dtest=CellListTests
```

Run a single test method:

```bash
mvn test -pl audio -Dtest=CellListTests#export
```

### Development Containers

The project includes a Docker-based development environment with 4 isolated sandboxes (A, B, C, D). Each sandbox provides a complete development environment with Java 17, Maven, build tools, and Claude Code CLI.

**Starting the containers:**

```bash
cd devtools
docker-compose up -d
```

**Connecting to a sandbox:**

```bash
docker exec -it dev-sandbox-a bash
docker exec -it dev-sandbox-b bash
docker exec -it dev-sandbox-c bash
docker exec -it dev-sandbox-d bash
```

**Stopping the containers:**

```bash
docker-compose down
```

### Using tmux for Persistent Sessions

The dev containers include **tmux**, a terminal multiplexer that allows you to run processes that persist even after you close your terminal. This is essential for long-running builds, tests, or interactive development sessions.

**Basic tmux workflow:**

```bash
# Connect to a container
docker exec -it dev-sandbox-a bash

# Start a new tmux session with a name
tmux new -s build

# Run your long-running process (e.g., Maven build)
mvn clean install

# Detach from tmux session (keeps process running)
# Press: Ctrl+b, then d

# Close your terminal - the process continues running

# Later: reconnect to the container
docker exec -it dev-sandbox-a bash

# Reattach to your tmux session
tmux attach -t build
```

**Useful tmux commands:**

- `tmux ls` - List all active sessions
- `tmux new -s <name>` - Create a new named session
- `tmux attach -t <name>` - Reattach to a session
- `tmux kill-session -t <name>` - Terminate a session
- `Ctrl+b d` - Detach from current session
- `Ctrl+b c` - Create new window within session
- `Ctrl+b n` - Next window
- `Ctrl+b p` - Previous window
- `Ctrl+b %` - Split pane vertically
- `Ctrl+b "` - Split pane horizontally

The containers are pre-configured with mouse support and 10,000 line scrollback buffer for easier navigation.

## To use the libraries

Add Maven Repository:

        <repositories>
                <repository>
                        <id>flowtree</id>
                        <name>Almost Realism Flowtree/name>
                        <url>https://maven.pkg.github.com/almostrealism/flowtree</url>
                        <releases><enabled>true</enabled></releases>
                        <snapshots><enabled>true</enabled></snapshots>
                </repository>
                <repository>
                        <id>rings</id>
                        <name>Almost Realism Rings/name>
                        <url>https://maven.pkg.github.com/almostrealism/rings</url>
                        <releases><enabled>true</enabled></releases>
                        <snapshots><enabled>true</enabled></snapshots>
                </repository>
        </repositories>

Add ar-rings dependency:

        <dependency>
            <groupId>org.almostrealism</groupId>
            <artifactId>ar-rings</artifactId>
            <version>0.42</version>
        </dependency>

## Using Rings

The basic building block that features of Rings are built on the **Cell**.
This is an interface (provided by [Almost Realism Common](https://github.com/almostrealism/common))
which represents a signal processing stage that can perform arbitrary
computation and can be connected to other **Cell**s to form a signal
processing graph. This concept can be leveraged in many ways, but the
way it is used in Rings specifically is to generate media. To allow this
media to be structured temporally, **Cell** implementations which have
time-varying state can additionally implement the **Temporal** interface.

There are a few ways to approach using Rings. These are, broadly:

1. Use built in capabilities of **CellFeatures**, and custom implementations of
the **Cell** and/or **Temporal** interfaces to create a media generation process
directly.
2. Use provided abstractions like **PatternNote** and **PatternElement** to define
compositions in a way that is more akin to writing or arranging music and then
use Rings to render those compositions.
3. Use the **AudioScene** wrapper to define a system for generating compositions
and routing them in a multichannel system akin to a Digital Audio Workstation.

These are not mutually exclusive, and can be combined in fairly arbitrary ways.

### Defining Simple Generators

Load audio and apply a high pass filter.

```Java
    public class MyMultimediaPipeline implements CellFeatures {
        public static void main(String args[]) {
            new MyMultimediaPipeline().filter();
        }
	
        public void filter() {
			Supplier<Runnable> r =
                    // Load the sample
					w("Library/Snare Gold 1.wav")
                            // Direct the audio to a high pass filter
							.f(i -> hp(2000, 0.1))
                            // Direct the output to a file
							.o(i -> new File("results/filter-delay-cell.wav"))
                            // Create a pipeline that will generate 6 seconds of audio
							.sec(6);
			
			// Compile and run the media pipeline
            // (this will make a best effort at
            // hardware acceleration, including
            // using the GPU if available)
			r.get().run();
        }
    }
```

### Arranging and Rendering Patterns

A simple example of how to define, render, and save a pattern.

```Java
    public class MyMultimediaPipeline implements
            CellFeatures, SamplingFeatures, PatternFeatures {
        public static void main(String args[]) {
            new MyMultimediaPipeline().sineAndSnare();
        }
	
        public void sineAndSnare() {
            // Define the shared parameters, including how notes should be
            // tuned and a root for the scale and the synth
            double duration = 8.0;
            KeyboardTuning tuning = new DefaultKeyboardTuning();
            WesternChromatic root = WesternChromatic.C3;
    
            // Settings for the synth note
            double amp = 0.25;
            int frames = (int) (2.0 * sampleRate);
    
            // Source for the synth note
            StatelessSource sine = (params, frequency) -> sampling(sampleRate, () -> {
                CollectionProducer<PackedCollection<?>> f =
                        multiply(c(tuning.getTone(root).asHertz()), frequency);
                CollectionProducer<PackedCollection<?>> t =
                        integers(0, frames).divide(sampleRate);
                return sin(t.multiply(2 * Math.PI).multiply(f)).multiply(amp);
            });
    
            // Define the synth note
            StatelessSourceNoteAudio audio =
                    new StatelessSourceNoteAudio(sine, root, 2.0);
            PatternNote sineNote = new PatternNote(List.of(audio));
            sineNote.setTuning(tuning);
    
            // Define a sampler note that will use the parameter 0.5
            // to choose which source to voice
            PatternNote choiceNote = new PatternNote(0.5);
            choiceNote.setTuning(tuning);
    
            // Setup context for rendering the audio, including the scale,
            // the way to translate positions into audio frames, and the
            // destination for the audio
            AudioSceneContext sceneContext = new AudioSceneContext();
            sceneContext.setFrameForPosition(pos -> (int) (pos * sampleRate));
            sceneContext.setScaleForPosition(pos -> WesternScales.major(root, 1));
            sceneContext.setDestination(new PackedCollection<>((int) (duration * sampleRate)));
    
            // Setup context for voicing the notes, including the library
            // of samples to use (choiceNote will select from those)
            NoteAudioContext audioContext = new NoteAudioContext();
            audioContext.setNextNotePosition(pos -> duration);
            audioContext.setAudioSelection((choice) ->
                    NoteAudioProvider.create("Library/Snare Gold 1.wav",
                            WesternChromatic.D3, tuning));
    
            // Create the elements of the composition, leveraging
            // the notes that have been defined in multiple places
            // to create a pattern of 4 elements
            List<PatternElement> elements = new ArrayList<>();
            elements.add(new PatternElement(sineNote, 0.0));
            elements.add(new PatternElement(choiceNote, 2.5));
            elements.add(new PatternElement(sineNote, 4.0));
            elements.add(new PatternElement(choiceNote, 6.5));
    
            // Adjust the position on the major scale for each of the
            // elements in the composition
            elements.get(0).setScalePosition(List.of(0.0));
            elements.get(1).setScalePosition(List.of(0.3));
            elements.get(2).setScalePosition(List.of(0.5));
            elements.get(3).setScalePosition(List.of(0.5));
    
            // Render the composition
            render(sceneContext, audioContext, elements, true, 0.0);
    
            // Save the composition to a file
            new WaveData(sceneContext.getDestination(), sampleRate)
                    .save(new File("results/sine-and-snare.wav"));
        }
    }
```

### Working with AudioScene

You can also create an **AudioScene** to generate audio in a more structured way.


```Java
    public class MyMultimediaPipeline {
	    public static void main(String args[]) {
			new MyMultimediaPipeline().runScene();
		}

        public void runScene() {
            // Settings for the scene
            double bpm = 120.0;
            int sourceCount = 4;
            int delayLayerCount = 3;
            int sampleRate = 44100;
    
            // Create the scene
            AudioScene scene = new AudioScene<>(bpm, sourceCount, delayLayerCount, sampleRate);
    
            // Load a library of material to use for creating notes to use
            // in the patterns that make up the arrangement
            scene.setLibrary(new AudioLibrary(new File("/Users/michael/Music/Samples"), sampleRate));
    
            // Create a random parameterization of the scene
            ProjectedGenome random = scene.getGenome().random();
            scene.assignGenome(random);
    
            // Create a destination for the output audio
            WaveOutput output = new WaveOutput(() -> new File("scene.wav"), 24, sampleRate, -1, false);
    
            // Generate the media pipeline
            Supplier<Runnable> process = scene.runner(new MultiChannelAudioOutput(output)).iter(30 * sampleRate);
    
            // Compile and run the pipeline
            process.get().run();
    
            // Save the resulting audio
            output.write().get().run();
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
