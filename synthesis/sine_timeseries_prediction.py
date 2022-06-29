import tensorflow as tf
import numpy as np

d_model = 1200
base_model = tf.keras.models.load_model('models/sine_timeseries_model.h5')
# base_model.summary()

DESIRED_SAMPLES = 220500

def preprocess(filename):
    audio = tf.audio.decode_wav(tf.io.read_file(filename), 1, DESIRED_SAMPLES).audio
    # audio = audio[:, 0]
    return audio

wavs = tf.io.gfile.glob("LJSpeech-1.1/wavs/*.wav")

for wav in wavs[1:2]:
    data = preprocess(wav);

prediction = np.asarray([data[0:1000]])
print(prediction.shape)
prediction = base_model.predict([data[0:1000]])
print(prediction.shape)

# print(prediction)

prediction = np.reshape(np.asarray([prediction]), (-1, 1))
tf.io.write_file("output/sine-timeseries-prediction.wav", tf.audio.encode_wav(prediction, 22050))
print("Wrote wav")