# Generate Module

Audio generation tools using diffusion transformers and autoencoders for text-to-audio and sample-based audio generation.

## Components

### AudioGenerator

Text-conditioned audio generation using a diffusion transformer with autoencoder. Supports two modes:

**Pure Generation**: Generate audio from text prompts starting from random noise
**Sample-Based Generation**: Interpolate existing samples in latent space and refine with diffusion (similar to img2img)

#### Pure Generation Example

```java
try (AudioGenerator generator = new AudioGenerator(modelsPath)) {
    generator.setAudioDurationSeconds(3.0);
    generator.generateAudio("crispy snare drum", seed, "output.wav");
}
```

#### Sample-Based Generation Example

```java
try (AudioGenerator generator = new AudioGenerator(modelsPath)) {
    generator.setAudioDurationSeconds(3.0);

    // Add samples
    WaveData snare1 = WaveData.load(new File("snare1.wav"));
    WaveData snare2 = WaveData.load(new File("snare2.wav"));
    generator.addAudio(snare1.getData());
    generator.addAudio(snare2.getData());

    // Configure strength (0.0 = preserve samples, 1.0 = full diffusion)
    generator.setStrength(0.5);

    // Create position vector for interpolation
    PackedCollection<?> position = new PackedCollection<>(shape(8))
        .fill(new Random()::nextGaussian);

    // Generate
    generator.generateAudioFromSamples(position, "punchy snare", seed, "output.wav");
}
```

#### Sample-Based Generation: Strength Parameter

Controls the balance between preservation and generation:

- **0.0 - 0.3**: Subtle refinement, stays close to samples
- **0.3 - 0.5**: Balanced (recommended)
- **0.5 - 0.7**: Creative variations
- **0.7 - 1.0**: Aggressive generation

#### Key Methods

**Configuration:**
- `setAudioDurationSeconds(double)` - Set output duration
- `setStrength(double)` - Set diffusion strength for sample-based generation
- `setComposerDimension(int)` - Set position vector dimensionality (default: 8)
- `setProgressMonitor(DoubleConsumer)` - Monitor generation progress

**Sample Management:**
- `addAudio(PackedCollection<?>)` - Add raw audio samples
- `addFeatures(PackedCollection<?>)` - Add pre-encoded latent features

**Generation:**
- `generateAudio(String prompt, long seed)` - Pure generation
- `generateAudioFromSamples(PackedCollection<?> position, String prompt, long seed)` - Sample-based generation

### AudioModulator

Fast sample interpolation without diffusion. Useful for quick morphing and exploration.

```java
try (AudioModulator modulator = new AudioModulator(modelsPath)) {
    // Add samples
    modulator.addAudio(sample1.getData());
    modulator.addAudio(sample2.getData());

    // Create position vector
    PackedCollection<?> position = new PackedCollection<>(shape(8))
        .fill(new Random()::nextGaussian);

    // Generate (direct interpolation, no diffusion)
    modulator.generateAudio(position, "output.wav");
}
```

**Comparison:**

| Feature | AudioModulator | AudioGenerator (Sample-Based) |
|---------|---------------|-------------------------------|
| Diffusion | No | Yes |
| Text conditioning | No | Yes |
| Speed | Fast | Slower |
| Creativity | Limited to interpolation | Explores beyond samples |
| Use case | Quick morphing | High-quality generation |

### AudioComposer

Core interpolation engine used by both AudioGenerator and AudioModulator. Manages weighted combination of encoded audio samples.

```java
AudioComposer composer = new AudioComposer(autoencoder, dimension, seed);
composer.addAudio(cp(audioData));  // Add samples

PackedCollection<?> position = new PackedCollection<>(shape(dimension))
    .fill(random::nextGaussian);

// Get interpolated latent
Producer<PackedCollection<?>> interpolated = composer.getResultant(cp(position));
```

## Implementation Details

### Matched Noise Addition (Sample-Based Generation)

The key innovation in sample-based generation is **matched noise addition**, which properly initializes diffusion from an interpolated latent:

1. **Calculate start step** from strength: `startStep = floor(strength × NUM_STEPS)`
2. **Get target sigma** at that step: `targetSigma = sigmas[startStep]`
3. **Add matched noise**: `noisyLatent = interpolatedLatent + targetSigma × noise`
4. **Run diffusion** from `startStep` to `NUM_STEPS`

This ensures the noise level matches what the diffusion model expects at that step, avoiding artifacts and preserving sample characteristics.

### Architecture

**Pure Generation:**
```
Text → Tokenize → Conditioners → Random Noise → Diffusion → Latent → Decoder → Audio
```

**Sample-Based Generation:**
```
Samples → Encoder → Latents
                      ↓
Position Vector → Interpolation → Add Matched Noise → Diffusion → Decoder → Audio
                      ↑
                Text → Conditioners
```

### Technical Specifications

**Latent Space:**
- Audio: `[2, 524288]` (stereo, ~11 seconds at 44.1kHz)
- Latent: `[64, 256]` (16,384 values)
- Compression: ~64x

**Diffusion:**
- Steps: 8 (default)
- Schedule: Sigmoid of linspace from -6.0 to 2.0
- Sampler: Ping-pong (rectified flow)
- Sample rate: 44.1kHz

**Memory:**
- Per sample: ~64KB (latent) + D×64KB (weights)
- Example: 3 samples, D=8 → ~1.7MB

## Best Practices

### Sample Selection
- Use **similar samples** for coherent interpolations (e.g., all snares)
- Use **diverse samples** for creative exploration (e.g., snare + clap + rim)
- Ensure consistent sample rates (44.1kHz)

### Strength Tuning
- Start with **0.3-0.5** for most use cases
- Use **0.2-0.3** for subtle variations
- Use **0.6-0.8** for creative transformations
- Avoid **0.9-1.0** unless you want mostly pure generation

### Text Prompts
- Reinforce sample type: "crispy snare drum", "punchy kick"
- Use descriptive adjectives: "sharp", "deep", "tight", "warm"
- Try contradictory prompts for creative tension: "soft aggressive snare"

### Position Vectors
- **Random Gaussian** vectors for exploration
- **Interpolate positions** for smooth morphing
- **Zero vector** for neutral interpolation

## Troubleshooting

### Output Too Noisy
- Reduce strength (try 0.3-0.4)
- Check input samples are clean
- Verify sample rate is 44.1kHz

### Output Loses Sample Characteristics
- Reduce strength (try 0.2-0.3)
- Add more samples for stronger influence
- Use prompts that reinforce sample type

### NaN Values
- Check input audio has no NaN/inf values
- Reduce strength
- Verify ONNX model files are not corrupted
- Enable `HardwareFeatures.outputMonitoring` for diagnostics

## Running the Example

```bash
# Pure generation
java org.almostrealism.ml.audio.AudioGeneratorExample \
    /path/to/models \
    /path/to/output \
    "crispy snare drum"

# Sample-based generation
java org.almostrealism.ml.audio.AudioGeneratorExample \
    /path/to/models \
    /path/to/output \
    "punchy snare" \
    snare1.wav snare2.wav snare3.wav
```

The example generates:
- **Pure mode**: 5 variations with different seeds
- **Sample mode**: 12 variations across 4 strength levels (0.2, 0.4, 0.6, 0.8) with 3 variations each

## Model Requirements

AudioGenerator requires four ONNX models and a weights directory:
- `conditioners.onnx` - Text conditioning model
- `encoder.onnx` - Audio encoder
- `decoder.onnx` - Audio decoder
- `dit.onnx` - Diffusion transformer (optional, can use pure Java implementation)
- `weights/` - DIT model weights (required if not using ONNX DIT)

AudioModulator only requires encoder/decoder models.

## See Also

- `ConditionalAudioSystem` - Base class providing autoencoder and conditioning
- `DiffusionTransformer` - Pure Java DIT implementation
- `OnnxAutoEncoder` - ONNX-based audio autoencoder
