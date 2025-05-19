package org.almostrealism.ml.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.collect.PackedCollection;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class WeightLoader {
	private final String weightDir;
	private final Map<String, WeightInfo> weightInfoMap;
	private final Map<String, String> javaMapping;

	public WeightLoader(String weightDir) throws IOException {
		this.weightDir = weightDir;
		this.weightInfoMap = loadWeightMetadata(weightDir);
		this.javaMapping = loadJavaMapping(weightDir);
	}

	private Map<String, WeightInfo> loadWeightMetadata(String dir) throws IOException {
		File metadataFile = new File(dir, "weights_metadata.json");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(metadataFile);

		Map<String, WeightInfo> result = new HashMap<>();

		root.fields().forEachRemaining(entry -> {
			String name = entry.getKey();
			JsonNode info = entry.getValue();

			int[] shape = new int[info.get("shape").size()];
			for (int i = 0; i < shape.length; i++) {
				shape[i] = info.get("shape").get(i).asInt();
			}

			String dtype = info.get("dtype").asText();
			String file = info.get("file").asText();

			result.put(name, new WeightInfo(shape, dtype, file));
		});

		return result;
	}

	private Map<String, String> loadJavaMapping(String dir) throws IOException {
		File mappingFile = new File(dir, "java_mapping.json");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(mappingFile);

		Map<String, String> result = new HashMap<>();

		root.fields().forEachRemaining(entry -> {
			result.put(entry.getKey(), entry.getValue().asText());
		});

		return result;
	}

	public PackedCollection<?> loadWeight(String name) throws IOException {
		if (!weightInfoMap.containsKey(name)) {
			throw new IllegalArgumentException("Unknown weight: " + name);
		}

		WeightInfo info = weightInfoMap.get(name);
		File weightFile = new File(weightDir, info.file);

		// Read .npy file
		try (RandomAccessFile raf = new RandomAccessFile(weightFile, "r");
			 FileChannel channel = raf.getChannel()) {

			// Skip NPY header (we already know the shape and dtype)
			// This is a simplified version - actual NPY parsing is more complex
			// We'd need to properly parse the NPY header in a real implementation
			raf.seek(128); // Skip header (simplified)

			// Create PackedCollection with appropriate shape
			PackedCollection<?> result = new PackedCollection<>(info.shape);

			// Create buffer for data
			ByteBuffer buffer = ByteBuffer.allocate((int)channel.size() - 128);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			channel.read(buffer, 128);
			buffer.flip();

			// Fill PackedCollection based on dtype
			if (info.dtype.equals("float32")) {
				for (int i = 0; i < result.getShape().getTotalSize(); i++) {
					float value = buffer.getFloat();
					result.setMem(i, value);
				}
			} else if (info.dtype.equals("float64")) {
				for (int i = 0; i < result.getShape().getTotalSize(); i++) {
					double value = buffer.getDouble();
					result.setMem(i, value);
				}
			} else {
				throw new IllegalArgumentException("Unsupported dtype: " + info.dtype);
			}

			return result;
		}
	}

	public void loadWeightsIntoModel(DiffusionTransformer model) throws IOException {
		Map<String, PackedCollection<?>> weights = new HashMap<>();

		// Load all weights
		for (String name : weightInfoMap.keySet()) {
			if (javaMapping.containsKey(name)) {
				String javaName = javaMapping.get(name);
				weights.put(javaName, loadWeight(name));
			}
		}

		// Set weights in model
		model.loadWeights(weights);
	}

	private static class WeightInfo {
		public final int[] shape;
		public final String dtype;
		public final String file;

		public WeightInfo(int[] shape, String dtype, String file) {
			this.shape = shape;
			this.dtype = dtype;
			this.file = file;
		}
	}
}