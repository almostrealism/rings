/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial.test;

import com.almostrealism.spatial.EditableSpatialWaveDetails;
import com.almostrealism.spatial.SpatialValue;
import com.almostrealism.spatial.SphericalBrush;
import com.almostrealism.spatial.TemporalSpatialContext;
import org.almostrealism.algebra.Vector;
import org.almostrealism.collect.PackedCollection;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests for the spatial drawing feature components.
 */
public class SpatialDrawingTest {

	/**
	 * Tests that position() and inverse() are true inverses.
	 */
	@Test
	public void testCoordinateRoundTrip() {
		TemporalSpatialContext context = new TemporalSpatialContext();
		context.setDuration(10.0);

		// Test various time/frequency combinations
		double[] times = {0.0, 1.0, 5.0, 9.5};
		double[] frequencies = {0.0, 0.25, 0.5, 0.75, 1.0};

		for (double time : times) {
			for (double freq : frequencies) {
				// Convert to position
				double internalTime = context.getSecondsToTime().applyAsDouble(time);
				Vector position = context.position(internalTime, 0, 0, freq);

				// Convert back to coordinates
				TemporalSpatialContext.TemporalCoordinates coords = context.inverse(position);

				// Verify round-trip accuracy
				Assert.assertEquals("Time mismatch for t=" + time + ", f=" + freq,
						time, coords.time(), 0.001);
				Assert.assertEquals("Frequency mismatch for t=" + time + ", f=" + freq,
						freq, coords.frequency(), 0.001);
				Assert.assertEquals("Layer mismatch", 0, coords.layer());
			}
		}
	}

	/**
	 * Tests that inverse() correctly extracts layer information.
	 */
	@Test
	public void testInverseWithLayers() {
		TemporalSpatialContext context = new TemporalSpatialContext();

		for (int layer = 0; layer < 5; layer++) {
			Vector position = context.position(1.0, 0, layer, 0.5);
			TemporalSpatialContext.TemporalCoordinates coords = context.inverse(position);
			Assert.assertEquals("Layer mismatch", layer, coords.layer());
		}
	}

	/**
	 * Tests SphericalBrush generates correct number of points.
	 */
	@Test
	public void testSphericalBrushDensity() {
		SphericalBrush brush = new SphericalBrush();
		brush.setDensity(100.0); // 100 points per second
		brush.setRadius(10.0);

		// At 60fps (duration ~0.016s), with pressure 1.0, expect ~2 points
		List<SpatialValue<?>> values = brush.stroke(new Vector(0, 0, 0), 1.0, 0.016);
		Assert.assertTrue("Expected 1-3 points, got " + values.size(),
				values.size() >= 1 && values.size() <= 3);

		// With 1 second duration, expect ~100 points
		values = brush.stroke(new Vector(0, 0, 0), 1.0, 1.0);
		Assert.assertEquals("Expected ~100 points", 100, values.size());

		// With half pressure, expect ~50 points
		values = brush.stroke(new Vector(0, 0, 0), 0.5, 1.0);
		Assert.assertEquals("Expected ~50 points", 50, values.size());
	}

	/**
	 * Tests SphericalBrush generates points within radius.
	 */
	@Test
	public void testSphericalBrushRadius() {
		SphericalBrush brush = new SphericalBrush();
		double radius = 10.0;
		brush.setRadius(radius);
		brush.setDensity(1000.0);

		Vector center = new Vector(100, 100, 0);
		List<SpatialValue<?>> values = brush.stroke(center, 1.0, 1.0);

		for (SpatialValue<?> value : values) {
			Vector pos = value.getPosition();
			double distance = Math.sqrt(
					Math.pow(pos.getX() - center.getX(), 2) +
					Math.pow(pos.getY() - center.getY(), 2) +
					Math.pow(pos.getZ() - center.getZ(), 2)
			);
			Assert.assertTrue("Point outside radius: " + distance, distance <= radius);
		}
	}

	/**
	 * Tests EditableSpatialWaveDetails creation and basic properties.
	 */
	@Test
	public void testEditableSpatialWaveDetailsCreation() {
		int timeFrames = 100;
		int frequencyBins = 256;
		double sampleRate = 44100;
		double freqSampleRate = 100;

		EditableSpatialWaveDetails canvas = new EditableSpatialWaveDetails(
				timeFrames, frequencyBins, sampleRate, freqSampleRate);

		Assert.assertFalse("Should not be modified initially", canvas.isModified());
		Assert.assertNotNull("Wave should not be null", canvas.getWave());
		Assert.assertEquals("Frame count mismatch", timeFrames, canvas.getWave().getFreqFrameCount());
		Assert.assertEquals("Bin count mismatch", frequencyBins, canvas.getWave().getFreqBinCount());
	}

	/**
	 * Tests applying brush strokes to EditableSpatialWaveDetails.
	 */
	@Test
	public void testApplyBrushStrokes() {
		int timeFrames = 100;
		int frequencyBins = 256;
		double sampleRate = 44100;
		double freqSampleRate = 100;

		EditableSpatialWaveDetails canvas = new EditableSpatialWaveDetails(
				timeFrames, frequencyBins, sampleRate, freqSampleRate);

		TemporalSpatialContext context = new TemporalSpatialContext();
		context.setDuration(1.0); // 1 second duration

		// Create a direct SpatialValue at a known position (time=0.5s, freq=0.5)
		// This bypasses the brush to test applyValues directly
		double internalTime = context.getSecondsToTime().applyAsDouble(0.5);
		Vector center = context.position(internalTime, 0, 0, 0.5);

		// Create a single value with known intensity (log(2) ≈ 0.693)
		List<SpatialValue<?>> values = List.of(
				new SpatialValue<>(center, Math.log(2.0), 0.5, true)
		);

		// Verify coordinates before applying
		TemporalSpatialContext.TemporalCoordinates coords = context.inverse(center);
		int expectedFrame = (int) (coords.time() * freqSampleRate);
		int expectedBin = (int) (coords.frequency() * frequencyBins);
		Assert.assertTrue("Frame index should be valid: " + expectedFrame,
				expectedFrame >= 0 && expectedFrame < timeFrames);
		Assert.assertTrue("Bin index should be valid: " + expectedBin,
				expectedBin >= 0 && expectedBin < frequencyBins);

		// Apply to canvas
		canvas.applyValues(values, context);

		Assert.assertTrue("Canvas should be modified", canvas.isModified());

		// Verify the specific cell was written
		PackedCollection freqData = canvas.getWave().getFreqData();
		int dataIndex = expectedFrame * frequencyBins + expectedBin;
		double written = freqData.toDouble(dataIndex);
		Assert.assertTrue("Expected cell should have non-zero value, got: " + written +
				" at frame=" + expectedFrame + ", bin=" + expectedBin, written > 0);
	}

	/**
	 * Tests clear functionality.
	 */
	@Test
	public void testClear() {
		EditableSpatialWaveDetails canvas = new EditableSpatialWaveDetails(
				100, 256, 44100, 100);

		TemporalSpatialContext context = new TemporalSpatialContext();
		context.setDuration(1.0);

		// Apply some strokes
		SphericalBrush brush = new SphericalBrush();
		Vector center = context.position(0.5, 0, 0, 0.5);
		canvas.applyValues(brush.stroke(center, 1.0, 0.1), context);

		Assert.assertTrue("Canvas should be modified", canvas.isModified());

		// Clear
		canvas.clear();

		Assert.assertFalse("Canvas should not be modified after clear", canvas.isModified());

		// Verify data is zeroed
		PackedCollection freqData = canvas.getWave().getFreqData();
		for (int i = 0; i < freqData.getMemLength(); i++) {
			Assert.assertEquals("Data should be zero after clear", 0.0, freqData.toDouble(i), 0.0001);
		}
	}

	/**
	 * Tests that getSeries returns the frequency data.
	 */
	@Test
	public void testGetSeries() {
		EditableSpatialWaveDetails canvas = new EditableSpatialWaveDetails(
				100, 256, 44100, 100);

		List<PackedCollection> series = canvas.getSeries(0);
		Assert.assertNotNull("Series should not be null", series);
		Assert.assertFalse("Series should not be empty", series.isEmpty());
		Assert.assertNotNull("First series element should not be null", series.get(0));
	}
}
