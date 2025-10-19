# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Rings is a multimedia generation framework that provides a compact and expressive language for defining multimedia generation pipelines using conventional DSP and machine learning. It functions as a headless Digital Audio Workstation (DAW) and rendering engine for visual and sonic media, targeting heterogeneous compute devices (CPU, GPU) locally and in the cloud.

**Key Dependency**: This project depends heavily on [Almost Realism Common](https://github.com/almostrealism/common) (ar-common version 0.71), which provides core computational abstractions and hardware acceleration support.

## Build System

This is a multi-module Maven project (Java 17).

### Common Commands

**Build entire project:**
```bash
mvn clean install
```

**Build specific module:**
```bash
mvn clean install -pl audio
mvn clean install -pl audio-space
mvn clean install -pl pattern
```

**Run all tests:**
```bash
mvn test
```

**Run tests for a specific module:**
```bash
mvn test -pl audio
mvn test -pl audio-space
```

**Run a single test class:**
```bash
mvn test -pl audio -Dtest=CellListTests
```

**Run a single test method:**
Never attempt to run a single test method. It would be great if you could, but the presence of a
pound sign causes the shell to require approval before executing the command, which interrupts
automated workflows. You will just have to live with the fact that you can only run entire test
classes. If this really bothers you, create a dedicated test class that only contains the single
test method you want to run (or create a stub that you can use to call the test method as a
workaround).

**Skip tests during build:**
```bash
mvn clean install -DskipTests
```

## Module Architecture

The project is organized into 17 Maven modules with clear separation of concerns:

### Core Audio Modules
- **audio**: Core audio processing, Cell abstractions, filters, tone system, and CellFeatures interface
- **pattern**: Pattern/note system (PatternNote, PatternElement, PatternFeatures) for musical composition
- **audio-space**: AudioScene system - high-level DAW-like arrangement and composition API

### ML & Generation
- **rings-ml**: Machine learning integrations (CLIP, UNet, Diffusion Transformers, ONNX)
- **tensorflow**: TensorFlow integration
- **torch**: PyTorch integration
- **generate**: Generation utilities
- **replicate**: Replicate API integration

### Visual & Rendering
- **visual**: Core visual rendering abstractions
- **visual-space**: Visual space management
- **shading**: Shading and material system
- **gl**: OpenGL integration
- **raytracer**: Ray tracing implementation
- **photonfield**: Photon mapping and light simulation

### UI & Utilities
- **swing**: Swing UI components
- **treeview**: Tree view components for data visualization
- **keyframing**: Animation keyframing system
- **absorption**: Absorption simulation
- **remote**: Remote operation support
- **remote-server**: Server for remote operations
- **rings**: Main integration module (currently empty)

## Core Architectural Concepts

### Cell and Temporal

The foundational abstraction is the **Cell** interface (from ar-common), representing a signal processing stage that performs arbitrary computation. Cells can be connected to form signal processing graphs.

- **Cell<T>**: A computational node in the signal graph that produces output of type T
- **Temporal**: Interface for Cells with time-varying state
- **Receptor<T>**: Accepts input from other Cells

### CellFeatures Interface

The **CellFeatures** interface (audio module) provides a fluent DSL for building audio processing pipelines:

- `w()`: Load audio files or create wave sources (sine, etc.)
- `f()`: Apply filters (high-pass `hp()`, low-pass `lp()`)
- `d()`: Add delay cells with configurable parameters
- `m()`: Map/transform cell outputs
- `o()`: Direct output to files
- `sum()`: Mix multiple cells together
- `sec()/min()`: Generate audio for specified duration
- `grid()`: Create temporal grids of cell choices

**Example**: `w("sample.wav").f(i -> hp(2000, 0.1)).o(i -> new File("output.wav")).sec(6)`

### Pattern System

The pattern system (pattern module) provides music-like composition abstractions:

- **PatternNote**: Defines a note with tuning, scale position, and audio sources
- **PatternElement**: Places a PatternNote at a specific time position
- **PatternFeatures**: Interface for rendering patterns to audio
- **AudioSceneContext**: Provides context for pattern rendering (scale, timing, destination)
- **NoteAudioContext**: Manages note voicing and sample libraries

### AudioScene

**AudioScene** (audio-space module) is the highest-level API, functioning like a DAW:

- Manages multi-channel arrangement with BPM, measures, and beats
- Integrates chord progressions, pattern systems, automation, effects, and mixdown
- Uses **ProjectedGenome** for parameterizing the entire scene (evolutionary/genetic algorithms)
- Key managers:
  - **GlobalTimeManager**: Handles timing and measure resets
  - **ChordProgressionManager**: Controls harmonic progression
  - **PatternSystemManager**: Manages patterns across channels
  - **AutomationManager**: Handles parameter automation
  - **EfxManager**: Effects processing
  - **MixdownManager**: Multi-channel mixing and reverb

### AudioLibrary

**AudioLibrary** manages a collection of audio samples with:
- Hierarchical file structure via FileWaveDataProviderTree
- Sample analysis and similarity matching
- Integration with pattern system for sample selection

### Signal Processing

Rings compiles signal processing operations to target CPU or GPU:
- **PackedCollection**: Core data structure for audio (from ar-common)
- **Producer/Evaluable**: Lazy computation graph building (from ar-common)
- **OperationList**: Groups operations for batch execution
- Pipeline operations are compiled at runtime for hardware acceleration

## Key Dependencies

From pom.xml:
- **ar-common** (0.71): Core computational abstractions
- **ar-flowtree** (0.27): Flow tree processing
- **JOGL** (2.3.2): OpenGL bindings
- **Jackson** (2.16.1): JSON processing
- **gRPC** (1.53.0): Remote procedure calls
- **ONNX Runtime** (1.22.0): ML inference
- **JUnit** (4.12): Testing

## Development Patterns

### Creating Audio Pipelines

Three approaches, from low to high level:
1. **Direct Cell usage**: Implement Cell/Temporal interfaces and use CellFeatures DSL
2. **Pattern-based**: Use PatternNote and PatternElement with PatternFeatures.render()
3. **AudioScene**: Create AudioScene, configure genome, and use runner()

### Working with Tests

- Tests are in each module's `src/test/java` directory
- Tests implement feature interfaces (CellFeatures, PatternFeatures) for DSL access
- Test data references `Library/` directory (audio samples)
- JUnit 4 is used (`@Test` annotations)

### Hardware Acceleration

Operations are automatically compiled for GPU when available. The framework:
- Builds computation graphs via Producer/Evaluable
- Compiles to OpenCL/native code at runtime
- Manages memory across CPU/GPU boundaries via PackedCollection

### Sample Rate

Default sample rate is `OutputLine.sampleRate` (typically 44100 Hz). Most components accept sample rate as a constructor parameter or use the global default.

## Native Library Management

### CRITICAL: Do NOT modify Java/Extensions directory

**NEVER** delete or modify files in `/Users/michael/Library/Java/Extensions/`:
- This directory contains native libraries (.dylib files) compiled at runtime
- The framework automatically regenerates libraries when needed
- Manually deleting these files does NOT solve architecture issues
- Libraries are cached for performance and should not be tampered with

### Test Timeouts

**DO NOT use the system `timeout` command** with Maven tests:
- The system `timeout` binary may cause architecture issues (x86_64 vs arm64)
- This can force Maven to run under Rosetta emulation
- Native library compilation will then target the wrong architecture

**INSTEAD:** Use Maven's built-in `forkedProcessTimeoutInSeconds` configuration:
- Already configured in the root pom.xml (default: 120 seconds)
- Applies to all test modules automatically
- Works correctly with native architecture

**To adjust timeout for specific tests:**
```bash
# Run with custom timeout (300 seconds = 5 minutes)
mvn test -pl raytracer -Dtest=SomeTest -DforkedProcessTimeoutInSeconds=300
```

The default timeout of 120 seconds (2 minutes) is sufficient for most tests.

## Code Style and Formatting

### Line Endings

**CRITICAL**: This repository uses Unix-style line endings (LF) exclusively.

- **NEVER** use Windows-style CRLF line endings (`\r\n`)
- **ALWAYS** use Unix-style LF line endings (`\n`)
- All files (Java, Markdown, XML, etc.) must use LF only

**IMPORTANT FOR FILE CREATION AND EDITING:**
- When using the **Write** tool to create new files, ensure the content string uses `\n` only, never `\r\n`
- When using the **Edit** tool, preserve existing LF line endings
- After creating or modifying any file, verify line endings with:
  ```bash
  file <filename>  # Should show "UTF-8 text" not "with CRLF"
  ```
- If you accidentally introduce CRLF separators, remove them immediately with:
  ```bash
  sed -i '' 's/\r$//' <filename>
  ```

**Root Cause Prevention:**
- Content prepared for the Write tool must have line endings explicitly verified before writing
- Never copy/paste content that might contain CRLF from external sources
- When constructing multi-line strings, use only `\n` as the line separator

This is standard for macOS/Linux development and critical for consistent git diffs and cross-platform compatibility.

## Important Notes

- **Cell** interface is from ar-common, not defined in this repo
- **Producer**, **Evaluable**, **Factor** are core computation abstractions from ar-common
- Many modules (gl, raytracer, photonfield) are for visual rendering, not audio
- The "rings" module itself is currently empty (main integration is in audio-space)
- Test files often require sample audio files in a `Library/` directory
- Results are typically written to a `results/` directory

## Tuning and Scales

The tone system (audio module) provides:
- **KeyboardTuning**: Maps musical notes to frequencies (DefaultKeyboardTuning, EqualTemperamentTuning)
- **WesternChromatic**: Chromatic note enumeration (C1, D1, E1, etc.)
- **WesternScales**: Scale factories (major, minor, etc.)
- Tuning is applied throughout the pattern system for note rendering
