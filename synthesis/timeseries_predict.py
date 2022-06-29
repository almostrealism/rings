import tensorflow as tf
import numpy as np

d_model = 1200
base_model = tf.keras.models.load_model('models/timeseries_model.h5')
# base_model.summary()

DESIRED_SAMPLES = 220500

def preprocess(filename):
    audio = tf.audio.decode_wav(tf.io.read_file(filename), 1, DESIRED_SAMPLES).audio
    audio = audio[:, 0]
    return audio

wavs = tf.io.gfile.glob("LJSpeech-1.1/wavs/*.wav")

for wav in wavs[1:2]:
    data = preprocess(wav).numpy().tolist()

prediction = data[0:4000]

for i in range(300):
    predict = base_model.predict([prediction[-d_model:]])

    if (len(predict) > 1 | len(predict[0]) > 1):
        print(f'predict len: {len(predict)}, {len(predict[0])}')

    prediction.append(predict[0][0].item())

    if (i + 1) % 1000 == 0:
        print(f'{i} iterations')

print(prediction)

prediction = np.reshape(np.asarray([prediction]), (-1, 1))
tf.io.write_file("output/timeseries-prediction.wav", tf.audio.encode_wav(prediction, 22050))
print("Wrote wav")