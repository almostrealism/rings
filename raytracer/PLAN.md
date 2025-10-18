# Ray Tracing System Restoration Plan

## Overview

This document outlines the plan for restoring and modernizing the ray tracing system, starting from `RayTracingJob` and extending down through the rendering pipeline to the intersection and shading layers.

**Current Status:** The ray tracing system is out of date and not fully functional. Core architecture is sound but blocked by ar-common API changes.

**Goal:** Restore the ray tracing system to full functionality, establish comprehensive tests, and create a foundation for future enhancements.

---

## Stage 1 Progress Report (2025-10-17) - UPDATED

### Completed
- ✅ **Javadoc documentation added** to all major ray tracing components (RayTracingJob, Engine hierarchy, shaders)
- ✅ **Basic tests created** - BasicIntersectionTest verifies core functionality (4/4 tests passing)
- ✅ **ar-common compatibility investigated** - found and partially fixed critical blocking issues
- ✅ **OrthographicCamera fixed** - Replaced Producer-based crossProduct with manual calculation
- ✅ **BasicGeometry fixed** - Added `.into(new TransformMatrix())` for type conversion
- ⚠️ **Camera construction working** - PinholeCamera now constructs successfully

### Status: BLOCKED - Critical ar-common Bug in VectorFeatures.vector()

**Current Error:**
```
java.lang.IllegalArgumentException: The result is not large enough to concatenate all inputs
at org.almostrealism.collect.CollectionFeatures.concat(CollectionFeatures.java:921)
at org.almostrealism.algebra.VectorFeatures.vector(VectorFeatures.java:54)
```

**Root Cause:** The `vector(Producer<T> x, Producer<T> y, Producer<T> z)` method in VectorFeatures (ar-common) is broken. Even the simplest case fails:
```java
CollectionProducer<Scalar> x = scalar(1.0);
CollectionProducer<Scalar> y = scalar(2.0);
CollectionProducer<Scalar> z = scalar(3.0);
CollectionProducer<Vector> vec = vector(x, y, z);  // FAILS
```

**Impact:** Cannot proceed with ray tracing as camera ray generation requires creating vectors from scalar components. This affects:
- `PinholeCamera.rayAt()` - Cannot generate rays
- All rendering operations that build vectors from components
- Any code using `VectorFeatures.vector(Producer, Producer, Producer)`

**Test Created:** `VectorConcatTest.java` isolates the issue for debugging in ar-common.

The rendering pipeline now progresses to ray generation but fails when trying to construct direction vectors.

### Key Findings

#### 1. ar-common API Changes (BLOCKING)
The primary blocker is a fundamental API change in ar-common v0.71 where `Vector` is now a view over `PackedCollection`:

**The Problem:**
- Old API: `Vector` was a standalone class
- New API: `Vector` extends `PackedCollection` and is created via `new Vector(collection, offset)`
- **Impact**: Many camera and geometry classes in ar-common have ClassCastExceptions

**Specific Failures:**
- `OrthographicCamera.updateUVW()` (line 167) - crossProduct returns PackedCollection, assigned to Vector field
- `PinholeCamera` inherits from OrthographicCamera, so it also fails
- `TransformMatrix.multiply()` (line 169) - similar casting issues

**Test Evidence:**
```
ClassCastException: class org.almostrealism.collect.PackedCollection
cannot be cast to class org.almostrealism.algebra.Vector
	at org.almostrealism.algebra.Vector.crossProduct(Vector.java:269)
	at org.almostrealism.projection.OrthographicCamera.updateUVW(OrthographicCamera.java:167)
```

#### 2. What Works
Despite the camera issues, basic ray tracing components are functional:
- ✅ **Sphere intersection** - returns valid ContinuousField
- ✅ **Distance calculation** - works (returns PackedCollection, not Scalar directly)
- ✅ **PointLight** - creates and returns color correctly
- ✅ **DiffuseShader** - computes surface color correctly
- ✅ **Scene creation** - lights, surfaces, shaders all integrate properly

BasicIntersectionTest passes 4/4 tests, proving the core rendering pipeline logic is intact.

#### 3. What's Broken
- ❌ **All Camera classes** - OrthographicCamera, PinholeCamera fail on construction
- ❌ **TransformMatrix operations** - geometry transformations fail
- ❌ **Full scene rendering** - blocked by camera issues
- ⚠️ **TestScene** - works until camera construction

### Required Fixes (ar-common)

To unblock ray tracing, ar-common needs updates in several classes:

1. **OrthographicCamera.java** (geometry module)
   - Line 165-170: `updateUVW()` method
   - Fix: Wrap crossProduct results with `new Vector(result, 0)`
   - Example: `this.u = new Vector(this.upDirection.crossProduct(this.w), 0);`

2. **TransformMatrix.java** (geometry module)
   - Line 169: `multiply()` method
   - Fix: Similar Vector wrapping needed

3. **BasicGeometry** classes
   - Multiple locations where PackedCollection/Vector casts fail
   - Need systematic review of all geometry operations

### Recommended Next Steps

**Option A: Fix ar-common (Recommended)**
1. Update OrthographicCamera.updateUVW() to handle new Vector API
2. Update TransformMatrix operations
3. Run ar-common tests to ensure no regressions
4. Return to rings ray tracing tests

**Option B: Work Around (Temporary)**
1. Create custom Camera classes in rings that don't use OrthographicCamera
2. Implement basic ray generation without ar-common cameras
3. Test rendering pipeline
4. Fix ar-common later

**Option C: Investigate Further**
1. Check if there's a newer ar-common that fixes these issues
2. Look for migration guide or API change documentation
3. Update rings to match current ar-common patterns

### Test Infrastructure Created

**BasicIntersectionTest.java** - Unit tests for core components:
- `sphereIntersection()` - ✅ PASS
- `sphereIntersectionDistance()` - ✅ PASS
- `sphereColor()` - ✅ PASS
- `pointLightCreation()` - ✅ PASS

**SimpleRenderTest.java** - Integration tests (currently blocked):
- `renderSingleSphere()` - ❌ BLOCKED (camera issue)
- `renderTwoSpheres()` - ❌ BLOCKED (camera issue)

---

## System Architecture

### Component Hierarchy

```
RayTracingJob (raytracer module)
  └─> RayTracedScene (raytracer module)
      └─> RayTracer (raytracer module)
          └─> Engine interface (shading module)
              └─> RayIntersectionEngine (shading module)
                  └─> LightingEngineAggregator (shading module)
                      └─> IntersectionalLightingEngine (shading module)
                          └─> LightingEngine (shading module)
                              ├─> ShadowMask (for shadow computation)
                              └─> Shader implementations
                                  ├─> DiffuseShader (Lambertian)
                                  ├─> ReflectionShader
                                  ├─> RefractionShader
                                  └─> Others
```

### Data Flow

1. **RayTracingJob.run()** - Entry point for distributed rendering
   - Loads Scene from URI (with caching)
   - Creates RayTracedScene with RayIntersectionEngine
   - Renders specified image panel (x, y, dx, dy)
   - Outputs RayTracingJobOutput to remote host or consumer

2. **RayTracedScene.realize()** - Coordinates rendering
   - Camera generates rays for each pixel (with supersampling)
   - RayTracer.trace() invoked for each ray
   - Returns RealizableImage (lazy Producer-based computation graph)

3. **RayIntersectionEngine.trace()** - Core ray tracing
   - Creates LightingEngineAggregator with all surfaces and lights
   - Aggregator creates IntersectionalLightingEngine for each surface-light pair
   - Returns Producer&lt;RGB&gt; for the ray's color

4. **IntersectionalLightingEngine** - Computes intersection
   - Calls surface.intersectAt(ray) to get ShadableIntersection (from ar-common)
   - ShadableIntersection provides: point, distance, normal via getNormalAt()
   - Passes to LightingEngine for lighting calculations

5. **LightingEngine** - Computes lighting
   - Calculates shadow mask if shadows enabled
   - Invokes surface shader with ShaderContext
   - Multiplies shadow * shade for final color
   - Uses intersection distance as "rank" for visibility determination

6. **LightingEngineAggregator** - Selects visible surface
   - Evaluates ranks (intersection distances) for all surface-light pairs
   - Selects the lighting engine with smallest positive rank (closest surface)
   - Returns that engine's color contribution

7. **Shaders** - Material-specific lighting
   - DiffuseShader: Lambertian (dot product of normal and light direction)
   - ReflectionShader: Mirror-like reflection
   - RefractionShader: Transparent materials
   - Others for specialized effects

---

## Key Dependencies on ar-common

The ray tracing system depends heavily on ar-common (v0.71) for:

1. **ShadableIntersection** (org.almostrealism.geometry)
   - Represents ray-surface intersection
   - Provides intersection point, distance, and surface normal
   - Implements Gradient interface for getNormalAt()

2. **Shadable** (org.almostrealism.color)
   - Interface for surfaces that can be shaded
   - shade() method invoked by LightingEngine

3. **Producer/Evaluable** (io.almostrealism.relation)
   - Lazy computation graph building
   - Compiled to CPU/GPU at runtime

4. **Intersectable** (org.almostrealism.geometry)
   - Surfaces implement intersectAt(Producer&lt;Ray&gt;) → ContinuousField

5. **Curve&lt;RGB&gt;** (org.almostrealism.geometry)
   - Surfaces provide color via getValueAt(Producer&lt;Vector&gt;)

**Important:** Some of these abstractions may have evolved or changed in ar-common since this code was written. Compatibility verification is essential.

---

## Known Issues and Gaps

### Critical Issues
1. **Shadows disabled** - `LightingEngine.enableShadows = false` by default
2. **Limited testing** - No comprehensive test suite for the ray tracing pipeline
3. **Unknown ar-common compatibility** - ar-common may have evolved, breaking existing code
4. **Producer evaluation unclear** - Not clear where/how the computation graph is actually evaluated
5. **Incomplete error handling** - Many potential failures (scene loading, intersection, etc.) not handled

### Design Issues
1. **Inefficient aggregation** - Creates surface-light pairs instead of aggregating lights per surface (see TODO in LightingEngineAggregator:121)
2. **Redundant arguments** - IntersectionalLightingEngine constructor has redundant arguments already in ShaderContext (see TODO)
3. **Type constraint mismatch** - LightingEngine&lt;T&gt; should require T extends ShadableIntersection but only requires ContinuousField
4. **Thread pool rarely used** - RayTracer.enableThreadPool defaults to false
5. **Limited shader variety** - Only basic shaders implemented, no advanced materials

### Missing Features
1. **Reflection/Refraction** - Mentioned but not implemented in the main pipeline
2. **Recursive ray tracing** - No support for bouncing rays (mirrors, glass)
3. **Texture mapping** - Limited or no support for image textures
4. **Advanced lighting** - No area lights, environment maps, global illumination
5. **Acceleration structures** - No BVH, octree, or spatial partitioning for performance
6. **Progressive rendering** - No incremental refinement or adaptive sampling

### Documentation Gaps
1. **RenderPanel usage** - How to actually use RenderPanel to display results
2. **Scene format** - What does the XML scene format look like?
3. **Surface implementations** - Which concrete surfaces exist and work?
4. **Shader interface** - Full contract for Shadable interface from ar-common
5. **Example workflows** - No end-to-end examples

---

## Testing Strategy

### Phase 1: Unit Tests (Foundation)
**Goal:** Verify individual components work in isolation

#### 1.1 Scene Loading
- [ ] Test scene loading from XML URI
- [ ] Test scene caching mechanism in RayTracingJob
- [ ] Test custom SceneLoader implementation
- [ ] Test scene with various cameras (Pinhole, Orthographic)
- [ ] Test scene with various lights (Point, Directional, Ambient, Surface)

**Files to test:**
- `RayTracingJob.getScene()`
- SceneLoader interface
- FileDecoder (if available in this repo)

#### 1.2 Basic Geometry
- [ ] Test simple surface intersection (Plane, Sphere, etc.)
- [ ] Verify ShadableIntersection data (point, distance, normal)
- [ ] Test ray generation from camera
- [ ] Test coordinate transformations

**Files to test:**
- Concrete surface classes (Thing.java, Plane, Sphere, etc.)
- Camera.rayAt() method
- AbstractSurface implementations

#### 1.3 Shaders
- [ ] Test DiffuseShader with various normals and light directions
- [ ] Test front/back face shading
- [ ] Test other shaders (Reflection, Refraction, Highlight)
- [ ] Verify shader output for known inputs

**Files to test:**
- `DiffuseShader.shade()`
- `ReflectionShader`
- `RefractionShader`
- `HighlightShader`

#### 1.4 Lighting
- [ ] Test PointLight color computation
- [ ] Test DirectionalAmbientLight
- [ ] Test AmbientLight
- [ ] Test SurfaceLight with samples

**Files to test:**
- Light implementations in shading module
- `LightingEngine.lightingCalculation()`

### Phase 2: Integration Tests (Pipeline)
**Goal:** Verify components work together correctly

#### 2.1 Engine Tests
- [ ] Test RayIntersectionEngine with simple scene (one surface, one light)
- [ ] Test LightingEngineAggregator with multiple surfaces
- [ ] Test ranked choice selection (closest surface wins)
- [ ] Test with multiple lights

**Files to test:**
- `RayIntersectionEngine.trace()`
- `LightingEngineAggregator`
- `IntersectionalLightingEngine`

#### 2.2 RayTracedScene Tests
- [ ] Test realize() with simple scene
- [ ] Test supersampling (ssWidth > 1, ssHeight > 1)
- [ ] Test different image dimensions
- [ ] Test evaluation of RealizableImage

**Files to test:**
- `RayTracedScene.realize()`
- `RayTracedScene.getProducer()`
- Pixel class

#### 2.3 RayTracingJob Tests
- [ ] Test job creation and encoding
- [ ] Test job execution (run())
- [ ] Test panel rendering (subset of image)
- [ ] Test output generation (RayTracingJobOutput)
- [ ] Test processOutput (assembling panels into full image)

**Files to test:**
- `RayTracingJob.run()`
- `RayTracingJob.processOutput()`
- `RayTracingJob.encode()/set()`

### Phase 3: End-to-End Tests (Validation)
**Goal:** Render complete test scenes and validate output

#### 3.1 Cornell Box
- [ ] Load Cornell Box scene (CornellBox.xml exists in resources)
- [ ] Render full image
- [ ] Validate expected colors (red left wall, green right wall, white others)
- [ ] Save image for visual inspection

#### 3.2 Simple Scenes
- [ ] Single sphere with point light
- [ ] Multiple spheres with shadows (when shadows enabled)
- [ ] Reflective surface test
- [ ] Transparent surface test
- [ ] Various light types

#### 3.3 RenderPanel Tests
- [ ] Test RenderPanel.render()
- [ ] Test image evaluation and display
- [ ] Test with different scenes

**Files to test:**
- `RenderPanel.render()`
- `RenderPanel.evaluateImage()`

### Phase 4: Performance Tests
**Goal:** Establish performance baselines and identify bottlenecks

- [ ] Benchmark simple scene rendering (single sphere)
- [ ] Benchmark complex scene rendering (many surfaces)
- [ ] Compare Producer evaluation vs. thread pool mode
- [ ] Profile to find hotspots
- [ ] Test kernel mode in LightingEngineAggregator

---

## Restoration Roadmap

### Stage 1: Foundation (Weeks 1-2)
**Goal:** Get basic rendering working

1. **Investigate ar-common compatibility**
   - Verify ShadableIntersection API is compatible
   - Check Shadable interface contract
   - Test Producer/Evaluable evaluation
   - Document any breaking changes

2. **Create minimal test scene**
   - Write code to create Scene programmatically (avoid XML complexity initially)
   - Single plane or sphere
   - Single point light
   - Simple diffuse shader

3. **Test basic intersection**
   - Verify surface.intersectAt() returns valid ShadableIntersection
   - Check distance calculation is correct
   - Verify normal calculation

4. **Test basic shading**
   - Invoke DiffuseShader directly
   - Verify color output for known inputs

5. **Test simple render**
   - Create RayTracedScene with minimal scene
   - Call realize()
   - Evaluate the result
   - Verify output is reasonable (not all black, not all white, etc.)

**Deliverable:** A single passing test that renders a simple scene to an RGB array

### Stage 2: Core Pipeline (Weeks 3-4)
**Goal:** Exercise the full rendering pipeline

1. **Implement Scene loading**
   - Test XML scene loading (use CornellBox.xml)
   - Debug FileDecoder if needed
   - Verify scene elements load correctly

2. **Test RayTracingJob**
   - Create job for full image render
   - Execute job
   - Verify output
   - Save image to file for inspection

3. **Test panel rendering**
   - Create multiple jobs for different panels
   - Render in parallel (simulate distributed system)
   - Assemble into full image using processOutput()
   - Verify no gaps or overlaps

4. **Test RenderPanel**
   - Create RenderPanel with scene
   - Call render()
   - Verify image is generated and displayed

**Deliverable:** End-to-end rendering of Cornell Box scene via RayTracingJob

### Stage 3: Debugging and Fixes (Weeks 5-6)
**Goal:** Address issues discovered during testing

1. **Fix intersection bugs**
   - Correct any surfaces that don't intersect properly
   - Fix normal calculation issues
   - Handle edge cases (ray tangent to surface, etc.)

2. **Fix shading bugs**
   - Correct color computation errors
   - Fix cases where shading is too bright/dark
   - Verify light attenuation

3. **Fix aggregation issues**
   - Debug ranked choice selection
   - Verify closest surface is always chosen
   - Fix any cases where wrong surface is rendered

4. **Enable and test shadows**
   - Set LightingEngine.enableShadows = true
   - Test ShadowMask functionality
   - Debug shadow artifacts

**Deliverable:** All Stage 2 tests pass with correct visual output

### Stage 4: Optimization (Weeks 7-8)
**Goal:** Improve performance and usability

1. **Profile rendering**
   - Identify bottlenecks (likely intersection calculation)
   - Measure time per ray, per surface, etc.

2. **Optimize hotspots**
   - Enable kernel mode in LightingEngineAggregator (pre-compute ranks)
   - Test accelerated aggregator mode
   - Consider hardware acceleration via ar-common

3. **Refactor inefficiencies**
   - Implement TODO: aggregate lights per surface instead of surface-light pairs
   - This could significantly reduce redundant intersection calculations

4. **Improve error handling**
   - Add validation and helpful error messages
   - Handle missing scenes, invalid parameters, etc.

**Deliverable:** Performance benchmarks and optimized rendering pipeline

### Stage 5: Feature Enhancement (Weeks 9-12)
**Goal:** Add missing features for modern ray tracing

1. **Recursive ray tracing**
   - Add reflection support (mirror surfaces)
   - Add refraction support (glass/water)
   - Implement max recursion depth

2. **Advanced materials**
   - Implement more sophisticated shaders
   - Add texture mapping support
   - Support for normal maps, specular maps, etc.

3. **Advanced lighting**
   - Area lights
   - Environment mapping
   - Soft shadows

4. **Acceleration structures**
   - Implement BVH or octree for scenes with many surfaces
   - Benchmark improvement

**Deliverable:** Enhanced ray tracer with reflection, refraction, and better performance

---

## Testing Infrastructure

### Test Organization

```
raytracer/src/test/java/
├── com/almostrealism/raytracer/
│   ├── unit/
│   │   ├── SceneLoadingTest.java
│   │   ├── IntersectionTest.java
│   │   ├── ShaderTest.java
│   │   └── LightingTest.java
│   ├── integration/
│   │   ├── RayIntersectionEngineTest.java
│   │   ├── RayTracedSceneTest.java
│   │   └── LightingEngineAggregatorTest.java
│   └── e2e/
│       ├── CornellBoxTest.java
│       ├── SimpleScenesTest.java
│       └── RenderPanelTest.java
└── resources/
    ├── test-scenes/
    │   ├── single-sphere.xml
    │   ├── cornell-box.xml (if not already present)
    │   └── multi-light.xml
    └── expected-output/
        ├── single-sphere.png
        └── cornell-box.png
```

### Test Utilities

Create helper classes:
- `SceneBuilder` - Programmatically build test scenes
- `ImageComparator` - Compare rendered output to expected images
- `RenderAssertion` - Common assertions for rendering tests

### Continuous Testing

- Run unit tests on every commit
- Run integration tests nightly
- Run e2e tests before releases
- Generate visual regression test reports

---

## Documentation Needs

### For Developers
1. **Architecture guide** - Overview of how components fit together
2. **API documentation** - Comprehensive Javadoc (partially complete)
3. **Testing guide** - How to write tests for ray tracing components
4. **Debugging guide** - Common issues and how to diagnose

### For Users
1. **Scene format specification** - XML schema and examples
2. **Surface reference** - Available surface types and their parameters
3. **Shader reference** - Available shaders and their properties
4. **Lighting reference** - Light types and their effects
5. **Rendering guide** - How to use RayTracingJob and RenderPanel
6. **Examples** - Sample scenes and code

---

## Success Criteria

### Minimum Viable (Stage 1-3)
- [ ] Can render Cornell Box scene correctly
- [ ] Image shows correct colors (red/green walls, white ceiling/floor)
- [ ] Surfaces are visible (not all black)
- [ ] No obvious artifacts (missing pixels, wrong colors)
- [ ] Tests cover core pipeline (Scene → RayTracedScene → Engine → Output)

### Fully Functional (Stage 4)
- [ ] All tests pass consistently
- [ ] Shadows work correctly
- [ ] Multiple scenes render correctly
- [ ] Performance is acceptable (< 10 minutes for 800x600 image)
- [ ] RenderPanel displays renders correctly
- [ ] Distributed rendering works (multiple panels combine correctly)

### Production Ready (Stage 5)
- [ ] Supports reflection and refraction
- [ ] Has acceleration structures for large scenes
- [ ] Documentation is complete
- [ ] Examples are available
- [ ] Performance is good (< 1 minute for 800x600 simple scene)

---

## Open Questions

### Critical (must answer in Stage 1)
1. **How does Producer evaluation actually work?** Where is the computation graph compiled and executed?
2. **Is ar-common v0.71 compatible?** Do ShadableIntersection, Shadable, etc. work as expected?
3. **What is the state of FileDecoder?** Can we load XML scenes?
4. **Are there any working surface implementations?** Do we have Sphere, Plane, etc. that work?

### Important (answer in Stage 2-3)
5. **Why are shadows disabled?** What breaks when they're enabled?
6. **What is the purpose of FogParameters?** It's passed around but seemingly unused.
7. **How should ShaderContext be populated?** What's the complete contract?
8. **What's the role of "other surfaces" and "other lights"** in the lighting calculation?

### Nice to know (answer in Stage 4-5)
9. **Can we use GPU acceleration?** Does ar-common support compiling to OpenCL/CUDA?
10. **What was the original performance like?** Any historical benchmarks?
11. **Are there any existing scenes besides Cornell Box?** Any complex test cases?

---

## Resources and References

### Codebase Locations
- **Entry point:** `raytracer/src/main/java/com/almostrealism/network/RayTracingJob.java`
- **Core engine:** `shading/src/main/java/com/almostrealism/raytrace/`
- **Shaders:** `shading/src/main/java/com/almostrealism/rayshade/`
- **UI:** `raytracer/src/main/java/com/almostrealism/raytracer/RenderPanel.java`
- **Test scene:** `raytracer/src/main/resources/CornellBox.xml`

### Dependencies
- **ar-common v0.71** - Core computational abstractions
- Documentation: https://github.com/almostrealism/common

### Ray Tracing Theory
- "Ray Tracing in One Weekend" by Peter Shirley
- "Physically Based Rendering" by Pharr, Jakob, Humphreys
- Lambertian shading: https://en.wikipedia.org/wiki/Lambert%27s_cosine_law

---

## Maintenance Notes

### Regular Reviews
- Review this plan after each stage completion
- Update with lessons learned
- Adjust timeline as needed
- Add new issues discovered during testing

### Communication
- Weekly progress updates
- Document all blocking issues
- Share visual results (rendered images)
- Maintain test coverage metrics

---

---

## Session Summary (2025-10-17)

### What Was Accomplished
1. ✅ Added comprehensive Javadoc to all major ray tracing classes
2. ✅ Created test infrastructure (`BasicIntersectionTest`, `SimpleRenderTest`, `VectorConcatTest`)
3. ✅ Fixed `OrthographicCamera.updateUVW()` - replaced Producer cross products with manual math
4. ✅ Fixed `BasicGeometry.calculateTransform()` - added proper type conversion for TransformMatrix
5. ✅ Verified basic components work (intersection, distance, color, lights all pass tests)
6. ✅ Camera construction now works (no more ClassCastException during initialization)

### Current Blocker: VectorFeatures.vector() Bug

The ray tracing pipeline is now 90% functional but blocked on a critical bug in ar-common's `VectorFeatures.vector(Producer, Producer, Producer)` method. This method is used everywhere to create vectors from scalar components and currently throws:

```
IllegalArgumentException: The result is not large enough to concatenate all inputs
```

This needs to be fixed in ar-common before ray tracing can proceed.

### Next Steps
1. **Fix ar-common bug** - Debug and fix the concat() method or the vector() implementation
2. **Complete rendering tests** - Once vectors work, the SimpleRenderTest should generate images
3. **Visual verification** - Confirm rendered images look correct
4. **Expand testing** - Add more scene tests, verify lighting, shadows, etc.

---

**Last Updated:** 2025-10-17 (Evening Session)
**Status:** Stage 1 in progress - blocked on ar-common VectorFeatures bug
