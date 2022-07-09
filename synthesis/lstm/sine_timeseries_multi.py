import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow_addons import layers as addon_layers
import pandas as pd
import seaborn as sns
from pylab import rcParams
import matplotlib.pyplot as plt

DESIRED_SAMPLES = 220500

sns.set(style='whitegrid', palette='muted', font_scale=1.5)
rcParams['figure.figsize'] = 16, 10

wavs = tf.io.gfile.glob("LJSpeech-1.1/wavs/*.wav")
print(f"Number of audio files: {len(wavs)}")

def preprocess(filename):
    audio = tf.audio.decode_wav(tf.io.read_file(filename), 1, DESIRED_SAMPLES).audio
    audio = audio[:, 0]
    return audio

data = []

for wav in wavs[0:2]:
    data.extend(preprocess(wav).numpy().tolist())

time = np.arange(0, len(data), 1)

df = pd.DataFrame(dict(sine=data), index=time, columns=['sine'])
df.head()

train_size = int(len(df) * 0.8)
test_size = len(df) - train_size
train, test = df.iloc[0:train_size], df.iloc[train_size:len(df)]

def create_dataset(X, y, time_steps=1, time_steps_out=1):
    Xs, ys = [], []
    for i in range(len(X) - time_steps - time_steps_out):
        v = X.iloc[i:(i + time_steps)].values
        Xs.append(v)
        ys.append(y.iloc[i + time_steps:i + time_steps + time_steps_out].values)
    return np.array(Xs), np.array(ys)

time_steps = 8000
time_steps_out = 22050 // 5

# reshape to [samples, time_steps, n_features]
X_train, y_train = create_dataset(train, train.sine, time_steps, time_steps_out)
X_test, y_test = create_dataset(test, test.sine, time_steps, time_steps_out)

print(f'training input shape: {X_train.shape}, training target shape: {y_train.shape}')
print(f'test input shape: {X_test.shape}, test target shape: {y_test.shape}')

inp = keras.Input((X_train.shape[1], X_train.shape[2]))
x = keras.layers.LSTM(128, input_shape=(X_train.shape[1], X_train.shape[2]))(inp)
x = keras.layers.Dense(time_steps_out)(x)
# x = addon_layers.WeightNormalization(
#     keras.layers.Conv1D(1, 7, padding="same", activation="tanh")
# )(x)

model = keras.Model(inp, x)
model.compile(loss='mean_squared_error', optimizer=keras.optimizers.Adam(0.001))
model.summary()

history = model.fit(
    X_train, y_train,
    epochs=5,
    batch_size=32,
    validation_split=0.1,
    verbose=1,
    shuffle=False
)

model.save("models/sine_timeseries_multi_model.h5")