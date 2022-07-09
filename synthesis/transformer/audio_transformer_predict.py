import tensorflow_datasets as tfds
import tensorflow as tf
from tensorflow.python.ops.numpy_ops import np_config

import time
import numpy as np
import matplotlib.pyplot as plt

# base_model = tf.keras.models.load_model('models/audio_transformer_model.h5')

prediction = [0.0, 1.0, -1.0]
prediction = np.reshape(np.asarray([prediction]), (-1, 1))
tf.io.write_file(
    "audio_transformer_prediction.wav", tf.audio.encode_wav(prediction, 22050)
)
print("Wrote wav")