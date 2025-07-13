package org.almostrealism.audioml.model;

import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.proto.SignatureDef;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TInt64;

import java.util.Map;
import java.util.HashMap;

public class ModelHandler implements AutoCloseable {
	// Model indices - match the C++ definitions
	private static final int T5_IDS_IN_IDX = 0;
	private static final int T5_ATTNMASK_IN_IDX = 1;
	private static final int T5_AUDIO_LEN_IN_IDX = 2;
	private static final int T5_CROSSATTN_OUT_IDX = 0;
	private static final int T5_GLOBALCOND_OUT_IDX = 2;

	private static final int DIT_CROSSATTN_IN_IDX = 0;
	private static final int DIT_GLOBALCOND_IN_IDX = 1;
	private static final int DIT_X_IN_IDX = 2;
	private static final int DIT_T_IN_IDX = 3;
	private static final int DIT_OUT_IDX = 0;

	private SavedModelBundle t5Model;
	private SavedModelBundle ditModel;
	private SavedModelBundle autoencoderModel;

	// Model operation signatures
	private String t5SignatureName;
	private String ditSignatureName;
	private String autoencoderSignatureName;

	// Input/output tensor names (will be determined from model inspection)
	private Map<Integer, String> t5InputNames = new HashMap<>();
	private Map<Integer, String> t5OutputNames = new HashMap<>();
	private Map<Integer, String> ditInputNames = new HashMap<>();
	private Map<Integer, String> ditOutputNames = new HashMap<>();
	private String autoencoderInputName;
	private String autoencoderOutputName;

	public void loadModels(String basePath) {
		// Load models from SavedModel format
		t5Model = SavedModelBundle.load(basePath + "/conditioners_float32", "serve");
		ditModel = SavedModelBundle.load(basePath + "/dit_model", "serve");
		autoencoderModel = SavedModelBundle.load(basePath + "/autoencoder_model", "serve");

		// Initialize signature names (may need to be adjusted based on actual models)
		t5SignatureName = firstKey(t5Model.metaGraphDef().getSignatureDefMap());
		ditSignatureName = firstKey(ditModel.metaGraphDef().getSignatureDefMap());
		autoencoderSignatureName = firstKey(autoencoderModel.metaGraphDef().getSignatureDefMap());

		// Extract input/output tensor names from the models
		initializeModelSignatures();
	}

	private void initializeModelSignatures() {
		// Extract tensor names from SignatureDefs
		// T5 model inputs
		t5InputNames.put(T5_IDS_IN_IDX, getInputTensorName(t5Model, t5SignatureName, 0));
		t5InputNames.put(T5_ATTNMASK_IN_IDX, getInputTensorName(t5Model, t5SignatureName, 1));
		t5InputNames.put(T5_AUDIO_LEN_IN_IDX, getInputTensorName(t5Model, t5SignatureName, 2));

		// T5 model outputs
		t5OutputNames.put(T5_CROSSATTN_OUT_IDX, getOutputTensorName(t5Model, t5SignatureName, 0));
		t5OutputNames.put(T5_GLOBALCOND_OUT_IDX, getOutputTensorName(t5Model, t5SignatureName, 2));

		// DiT model inputs
		ditInputNames.put(DIT_CROSSATTN_IN_IDX, getInputTensorName(ditModel, ditSignatureName, 0));
		ditInputNames.put(DIT_GLOBALCOND_IN_IDX, getInputTensorName(ditModel, ditSignatureName, 1));
		ditInputNames.put(DIT_X_IN_IDX, getInputTensorName(ditModel, ditSignatureName, 2));
		ditInputNames.put(DIT_T_IN_IDX, getInputTensorName(ditModel, ditSignatureName, 3));

		// DiT model output
		ditOutputNames.put(DIT_OUT_IDX, getOutputTensorName(ditModel, ditSignatureName, 0));

		// Autoencoder model
		autoencoderInputName = getInputTensorName(autoencoderModel, autoencoderSignatureName, 0);
		autoencoderOutputName = getOutputTensorName(autoencoderModel, autoencoderSignatureName, 0);
	}

	private String getInputTensorName(SavedModelBundle model, String signatureName, int index) {
		SignatureDef sig = model.metaGraphDef().getSignatureDefOrThrow(signatureName);
		return (String) sig.getInputsMap().keySet().toArray()[index];
	}

	private String getOutputTensorName(SavedModelBundle model, String signatureName, int index) {
		SignatureDef sig = model.metaGraphDef().getSignatureDefOrThrow(signatureName);
		return (String) sig.getOutputsMap().keySet().toArray()[index];
	}

	public Map<String, Tensor> runT5Model(long[] ids, long[] attentionMask, float audioLenSec) {
		Session session = t5Model.session();
		try (
				TInt64 idsT = TInt64.tensorOf(Shape.of(1, ids.length), buffer -> {
					for (int i = 0; i < ids.length; i++) {
						buffer.setLong(ids[i], 0, i);
					}
				});
				TInt64 attentionMaskT = TInt64.tensorOf(Shape.of(1, attentionMask.length), buffer -> {
					for (int i = 0; i < attentionMask.length; i++) {
						buffer.setLong(attentionMask[i], 0, i);
					}
				});
				TFloat32 audioLenT = TFloat32.scalarOf(audioLenSec)
		) {
			Result result = session.runner()
					.feed(t5InputNames.get(T5_IDS_IN_IDX), idsT)
					.feed(t5InputNames.get(T5_ATTNMASK_IN_IDX), attentionMaskT)
					.feed(t5InputNames.get(T5_AUDIO_LEN_IN_IDX), audioLenT)
					.fetch(t5OutputNames.get(T5_CROSSATTN_OUT_IDX))
					.fetch(t5OutputNames.get(T5_GLOBALCOND_OUT_IDX))
					.run();

			Map<String, Tensor> outputs = new HashMap<>();
			outputs.put("crossAttn", result.get(0));
			outputs.put("globalCond", result.get(1));

			return outputs;
		}
	}

	public Tensor runDitModel(Tensor x, Tensor t, Tensor crossAttn, Tensor globalCond) {
		return ditModel.session().runner()
				.feed(ditInputNames.get(DIT_X_IN_IDX), x)
				.feed(ditInputNames.get(DIT_T_IN_IDX), t)
				.feed(ditInputNames.get(DIT_CROSSATTN_IN_IDX), crossAttn)
				.feed(ditInputNames.get(DIT_GLOBALCOND_IN_IDX), globalCond)
				.fetch(ditOutputNames.get(DIT_OUT_IDX))
				.run()
				.get(0);
	}

	public Tensor runAutoencoderModel(Tensor input) {
		return autoencoderModel.session().runner()
				.feed(autoencoderInputName, input)
				.fetch(autoencoderOutputName)
				.run()
				.get(0);
	}

	@Override
	public void close() {
		if (t5Model != null) t5Model.close();
		if (ditModel != null) ditModel.close();
		if (autoencoderModel != null) autoencoderModel.close();
	}

	private static String firstKey(Map<String, SignatureDef> map) {
		return map.keySet().iterator().next();
	}
}