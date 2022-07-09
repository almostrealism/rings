import tensorflow as tf
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

DESIRED_SAMPLES = 220500 / 2
time_steps = 4000

model = tf.keras.models.load_model('models/sine_timeseries_model.h5')
model.summary()

wavs = tf.io.gfile.glob("LJSpeech-1.1/wavs/*.wav")
print(f"Number of audio files: {len(wavs)}")

def preprocess(filename):
    audio = tf.audio.decode_wav(tf.io.read_file(filename), 1, DESIRED_SAMPLES).audio
    # audio = audio[:, 0]
    return audio

data = []

for wav in wavs[100:200]:
    data.extend(preprocess(wav).numpy().tolist())

tf.io.write_file("../output/validate.wav", tf.audio.encode_wav(data, 22050))
#
# def create_dataset(X, y, time_steps=1):
#     Xs, ys = [], []
#     for i in range(len(X) - time_steps):
#         v = X.iloc[i:(i + time_steps)].values
#         Xs.append(v)
#         ys.append(y.iloc[i + time_steps])
#     return np.array(Xs), np.array(ys)
#
# def tail(values, lookback):
#     v = values[-(lookback+1):]
#
#     time = np.arange(0, len(v), 1)
#
#     df = pd.DataFrame(dict(sine=v), index=time, columns=['sine'])
#     df.head()
#
#     test = df.iloc[0:len(df)]
#
#     # reshape to [samples, time_steps, n_features]
#     X, y = create_dataset(test, test.sine, time_steps)
#     return X
#
# initial_length = len(data)
# print(f'data length: {initial_length}')
#
# for i in range(22050 * 2):
#     X_test = np.asarray([data[-time_steps:]])
#     # print(f'Testing with X_test shape: {X_test.shape}')
#     y_pred = model.predict(X_test)
#     # print(f'Prediction with shape: {y_pred.shape}')
#     data.append(y_pred[0])
#
#     if (i + 1) % 100 == 0:
#         print(f'{i + 1} samples processed')
#
# tf.io.write_file("output/sine-timeseries-prediction.wav", tf.audio.encode_wav(data, 22050))
# print("Wrote wav")
#
# plt.plot(np.arange(0, initial_length), data[0:initial_length], marker='.', label="true")
# plt.plot(np.arange(initial_length, len(data)), data[initial_length:], 'r', label="prediction")
# plt.ylabel('Value')
# plt.xlabel('Time Step')
# plt.legend()
# plt.show()