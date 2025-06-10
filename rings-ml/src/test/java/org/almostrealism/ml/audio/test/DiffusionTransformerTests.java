/*
 * Copyright 2025 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.ml.audio.test;

import org.almostrealism.collect.PackedCollection;
import org.almostrealism.ml.StateDictionary;
import org.almostrealism.ml.audio.DiffusionTransformerFeatures;
import org.almostrealism.model.CompiledModel;
import org.almostrealism.model.Model;
import org.almostrealism.model.SequentialBlock;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

public class DiffusionTransformerTests implements DiffusionTransformerFeatures, TestFeatures {

    /**
    * Tests fourierFeatures against reference data generated from the actual
    * stable-audio-tools FourierFeatures class. This ensures our Java implementation
    * matches the real Python behavior including the 2π factor, matrix multiplication,
    * and correct concatenation order.
    */
    @Test
    public void fourierFeaturesCompare() throws Exception {
        String referenceDir = "/Users/michael/Documents/AlmostRealism/models/fourier_features";

        // Load reference data using StateDictionary
        StateDictionary referenceData = new StateDictionary(referenceDir);
        referenceData.keySet()
                .forEach(key -> System.out.println("\t" + key + " " + referenceData.get(key).getShape()));

        // Extract test configuration
        PackedCollection<?> testConfig = referenceData.get("test_config");
        int batchSize = (int) testConfig.valueAt(0);
        int inFeatures = (int) testConfig.valueAt(1);
        int outFeatures = (int) testConfig.valueAt(2);

        log("FourierFeatures test configuration:");
        log("  batchSize=" + batchSize + ", inFeatures=" + inFeatures + ", outFeatures=" + outFeatures);

        // Load test data
        PackedCollection<?> input = referenceData.get("input");
        PackedCollection<?> expectedOutput = referenceData.get("expected_output");
        PackedCollection<?> weight = referenceData.get("weight");

        // Load intermediate values for debugging
        PackedCollection<?> fIntermediate = referenceData.get("f_intermediate");
        PackedCollection<?> expectedCosValues = referenceData.get("cos_values");
        PackedCollection<?> expectedSinValues = referenceData.get("sin_values");

        assertNotNull("Input not found", input);
        assertNotNull("Expected output not found", expectedOutput);
        assertNotNull("Weight not found", weight);
        assertNotNull("F intermediate not found", fIntermediate);

        log("FourierFeatures weight shapes:");
        log("  Input: " + input.getShape());
        log("  Weight: " + weight.getShape());
        log("  Expected output: " + expectedOutput.getShape());
        log("  F intermediate: " + fIntermediate.getShape());

        // Verify weight shape matches Python FourierFeatures [out_features // 2, in_features]
        assertEquals("Weight should have shape [" + (outFeatures / 2) + ", " + inFeatures + "]",
                outFeatures / 2, weight.getShape().length(0));
        assertEquals("Weight should have shape [" + (outFeatures / 2) + ", " + inFeatures + "]",
                inFeatures, weight.getShape().length(1));

        // Create test model with FourierFeatures
        Model model = new Model(shape(batchSize, inFeatures));
        SequentialBlock main = model.sequential();

        // Add FourierFeatures block
        main.add(fourierFeatures(batchSize, inFeatures, outFeatures, weight));

        // Compile and run the model
        CompiledModel compiled = model.compile(false);
        PackedCollection<?> actualOutput = compiled.forward(input);

        log("Expected output total: " + expectedOutput.doubleStream().map(Math::abs).sum());
        log("Actual output total: " + actualOutput.doubleStream().map(Math::abs).sum());
        log("F intermediate total: " + fIntermediate.doubleStream().map(Math::abs).sum());

        assertEquals(expectedOutput.getShape().getTotalSize(),
                actualOutput.getShape().getTotalSize());

        double diff = compare(expectedOutput, actualOutput);
        log("FourierFeatures difference between expected and actual output = " + diff);
        assertTrue("FourierFeatures output does not match Python reference within tolerance", diff < 1e-5);

        // Additional verification: Check a few individual values for debugging
        log("Debugging individual values:");
        log("Input[0]: " + input.valueAt(0, 0));
        log("Weight[0,0]: " + weight.valueAt(0, 0));
        log("F intermediate[0]: " + fIntermediate.valueAt(0, 0));
        log("Expected cos[0]: " + expectedCosValues.valueAt(0, 0));
        log("Expected sin[0]: " + expectedSinValues.valueAt(0, 0));
        log("Expected output[0] (cos): " + expectedOutput.valueAt(0, 0));
        log("Expected output[" + (outFeatures/2) + "] (sin): " + expectedOutput.valueAt(0, outFeatures/2));
        log("Actual output[0] (cos): " + actualOutput.valueAt(0, 0));
        log("Actual output[" + (outFeatures/2) + "] (sin): " + actualOutput.valueAt(0, outFeatures/2));
    }

    /**
    * Tests fourierFeatures with simple known values to verify basic mathematical operations.
    * This is a sanity check to ensure the 2π factor, matrix multiplication, and
    * concatenation order are working correctly.
    */
    @Test
    public void fourierFeaturesBasic() {
        int batchSize = 1;
        int inFeatures = 1;
        int outFeatures = 4;  // Simple case

        // Create simple test inputs
        PackedCollection<?> input = new PackedCollection<>(shape(batchSize, inFeatures));
        input.setValueAt(0.5, 0, 0);  // Simple input value

        // Create simple weight matrix [outFeatures/2, inFeatures] = [2, 1]
        PackedCollection<?> weight = new PackedCollection<>(shape(outFeatures / 2, inFeatures));
        weight.setValueAt(1.0, 0, 0);  // First frequency
        weight.setValueAt(2.0, 1, 0);  // Second frequency

        // Create test model
        Model model = new Model(shape(batchSize, inFeatures));
        SequentialBlock main = model.sequential();
        main.add(fourierFeatures(batchSize, inFeatures, outFeatures, weight));

        CompiledModel compiled = model.compile(false);
        PackedCollection<?> output = compiled.forward(input);

        // Manual calculation for verification
        // f = 2 * π * input @ weight.T
        // f[0] = 2 * π * 0.5 * 1.0 = π
        // f[1] = 2 * π * 0.5 * 2.0 = 2π
        // output = [cos(π), cos(2π), sin(π), sin(2π)] = [-1, 1, 0, 0] (approximately)

        double expectedCos1 = Math.cos(Math.PI);        // ≈ -1
        double expectedCos2 = Math.cos(2 * Math.PI);    // ≈ 1
        double expectedSin1 = Math.sin(Math.PI);        // ≈ 0
        double expectedSin2 = Math.sin(2 * Math.PI);    // ≈ 0

        log("FourierFeatures basic test:");
        log("Input: " + input.valueAt(0, 0));
        log("Weight[0]: " + weight.valueAt(0, 0) + ", Weight[1]: " + weight.valueAt(1, 0));
        log("Expected: [" + expectedCos1 + ", " + expectedCos2 + ", " + expectedSin1 + ", " + expectedSin2 + "]");
        log("Actual: [" + output.valueAt(0, 0) + ", " + output.valueAt(0, 1) +
                ", " + output.valueAt(0, 2) + ", " + output.valueAt(0, 3) + "]");

        assertEquals("First cos value", expectedCos1, output.valueAt(0, 0));
        assertEquals("Second cos value", expectedCos2, output.valueAt(0, 1));
        assertEquals("First sin value", expectedSin1, output.valueAt(0, 2));
        assertEquals("Second sin value", expectedSin2, output.valueAt(0, 3));
    }
    
    /**
     * Tests the input projection component (preprocess_conv + residual) against reference data
     * from the actual DiT model. This is critical because it's one of the first operations
     * in the forward pass, so any errors here propagate through the entire model.
     */
    @Test
    public void inputProjectionCompare() throws Exception {
        String referenceDir = "/Users/michael/Documents/AlmostRealism/models/input_projection";
    
        // Load reference data using StateDictionary
        StateDictionary referenceData = new StateDictionary(referenceDir);
        referenceData.keySet()
                .forEach(key -> System.out.println("\t" + key + " " + referenceData.get(key).getShape()));
    
        // Extract test configuration
        PackedCollection<?> testConfig = referenceData.get("test_config");
        int batchSize = (int) testConfig.valueAt(0);
        int ioChannels = (int) testConfig.valueAt(1);
        int audioSeqLen = (int) testConfig.valueAt(2);
    
        log("Input projection test configuration:");
        log("  batchSize=" + batchSize + ", ioChannels=" + ioChannels + ", audioSeqLen=" + audioSeqLen);
    
        // Load test data
        PackedCollection<?> input = referenceData.get("input");
        PackedCollection<?> expectedOutput = referenceData.get("expected_output");
        PackedCollection<?> convOutput = referenceData.get("conv_output");
        PackedCollection<?> inputProjWeight = referenceData.get("model.model.preprocess_conv.weight");
    
        assertNotNull("Input not found", input);
        assertNotNull("Expected output not found", expectedOutput);
        assertNotNull("Conv output not found", convOutput);
        assertNotNull("Input projection weight not found", inputProjWeight);
    
        log("Input projection shapes:");
        log("  Input: " + input.getShape());
        log("  Weight: " + inputProjWeight.getShape());
        log("  Expected output: " + expectedOutput.getShape());
    
        // Verify weight shape matches expected 1D conv weight [out_channels, in_channels, kernel_size]
        // For DiT: [io_channels, io_channels, 1]
        assertEquals("Weight should have 3 dimensions for 1D conv", 3, inputProjWeight.getShape().getDimensions());
        assertEquals("Weight out_channels should match io_channels", ioChannels, inputProjWeight.getShape().length(0));
        assertEquals("Weight in_channels should match io_channels", ioChannels, inputProjWeight.getShape().length(1));
        assertEquals("Weight kernel_size should be 1", 1, inputProjWeight.getShape().length(2));
    
        // Create test model with input projection (conv1d + residual)
        Model model = new Model(shape(batchSize, ioChannels, audioSeqLen));
        SequentialBlock main = model.sequential();
    
        // Add input projection exactly as in DiffusionTransformer.buildModel()
        main.add(residual(convolution1d(batchSize, ioChannels, ioChannels, audioSeqLen,
                1, 0, inputProjWeight, null)));
    
        // Compile and run the model
        CompiledModel compiled = model.compile(false);
        PackedCollection<?> actualOutput = compiled.forward(input);
    
        log("Input total: " + input.doubleStream().map(Math::abs).sum());
        log("Expected conv output total: " + convOutput.doubleStream().map(Math::abs).sum());
        log("Expected final output total: " + expectedOutput.doubleStream().map(Math::abs).sum());
        log("Actual output total: " + actualOutput.doubleStream().map(Math::abs).sum());
    
        assertEquals(expectedOutput.getShape().getTotalSize(),
                actualOutput.getShape().getTotalSize());
    
        double diff = compare(expectedOutput, actualOutput);
        log("Input projection difference between expected and actual output = " + diff);
        assertTrue("Input projection output does not match Python reference within tolerance", diff < 1e-5);
    
        // Additional verification: Check that this is indeed a residual connection
        // The output should be input + conv(input), not just conv(input)
        double inputSum = input.doubleStream().sum();
        double outputSum = actualOutput.doubleStream().sum();
        log("Input sum: " + inputSum);
        log("Output sum: " + outputSum);
        
        // The output should include the input due to residual connection
        // so output sum should be related to input sum (though not exactly equal due to conv)
        assertTrue("Output should be different from input (conv should add something)",
                Math.abs(inputSum - outputSum) > 1e-8);
    }
}