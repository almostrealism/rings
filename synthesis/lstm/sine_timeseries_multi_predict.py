import tensorflow as tf
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

DESIRED_SAMPLES = 220500 / 2
time_steps = 8000
time_steps_out = 22050 // 5

model = tf.keras.models.load_model('models/sine_timeseries_multi_model.h5')
model.summary()

wavs = tf.io.gfile.glob("LJSpeech-1.1/wavs/*.wav")
print(f"Number of audio files: {len(wavs)}")

def preprocess(filename):
    audio = tf.audio.decode_wav(tf.io.read_file(filename), 1, DESIRED_SAMPLES).audio
    # audio = audio[:, 0]
    return audio

data = []

for wav in wavs[1:2]:
    data.extend(preprocess(wav).numpy().tolist())

def create_dataset(X, y, time_steps=1):
    Xs, ys = [], []
    for i in range(len(X) - time_steps):
        v = X.iloc[i:(i + time_steps)].values
        Xs.append(v)
        ys.append(y.iloc[i + time_steps])
    return np.array(Xs), np.array(ys)

def tail(values, lookback):
    v = values[-(lookback+1):]

    time = np.arange(0, len(v), 1)

    df = pd.DataFrame(dict(sine=v), index=time, columns=['sine'])
    df.head()

    test = df.iloc[0:len(df)]

    # reshape to [samples, time_steps, n_features]
    X, y = create_dataset(test, test.sine, time_steps)
    return X

initial_length = len(data)
print(f'data length: {initial_length}')

duration = 22050 * 4

for i in range(duration // time_steps_out):
    X_test = np.asarray([data[-time_steps:]])
    y_pred = model.predict(X_test)
    data.extend(np.expand_dims(y_pred[0], axis=1).tolist())
    print(f'Data extended to : {len(data)}')

    if (i + 1) % 10 == 0:
        print(f'{i + 1} samples processed')

tf.io.write_file("../output/sine-timeseries-multi-prediction.wav", tf.audio.encode_wav(data, 22050))
print("Wrote wav")

plt.plot(np.arange(0, initial_length), data[0:initial_length], marker='.', label="true")
plt.plot(np.arange(initial_length, len(data)), data[initial_length:], 'r', label="prediction")
plt.ylabel('Value')
plt.xlabel('Time Step')
plt.legend()
plt.show()