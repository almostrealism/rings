import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow_addons import layers as addon_layers
import pandas as pd
import seaborn as sns
from pylab import rcParams
import matplotlib.pyplot as plt

DESIRED_SAMPLES = 220500 / 2

sns.set(style='whitegrid', palette='muted', font_scale=1.5)
rcParams['figure.figsize'] = 16, 10

wavs = tf.io.gfile.glob("LJSpeech-1.1/wavs/*.wav")
print(f"Number of audio files: {len(wavs)}")

def preprocess(filename):
    audio = tf.audio.decode_wav(tf.io.read_file(filename), 1, DESIRED_SAMPLES).audio
    audio = audio[:, 0]
    return audio

data = []

for wav in wavs[0:1]:
    data.extend(preprocess(wav).numpy().tolist())

time = np.arange(0, len(data), 1)

df = pd.DataFrame(dict(sine=data), index=time, columns=['sine'])
df.head()

train_size = int(len(df) * 0.8)
test_size = len(df) - train_size
train, test = df.iloc[0:train_size], df.iloc[train_size:len(df)]

def create_dataset(X, y, time_steps=1):
    Xs, ys = [], []
    for i in range(len(X) - time_steps):
        v = X.iloc[i:(i + time_steps)].values
        Xs.append(v)
        ys.append(y.iloc[i + time_steps])
    return np.array(Xs), np.array(ys)

time_steps = 2048

# reshape to [samples, time_steps, n_features]
X_train, y_train = create_dataset(train, train.sine, time_steps)
X_test, y_test = create_dataset(test, test.sine, time_steps)

print(f'training input shape: {X_train.shape}, training target shape: {y_train.shape}')
print(f'test input shape: {X_test.shape}, test target shape: {y_test.shape}')

inp = keras.Input((X_train.shape[1], X_train.shape[2]))
# input_shape = (16384, 16384)
# x = tf.random.normal(input_shape)
x = keras.layers.Conv1D(2048, 4,
                dilation_rate=512,
                padding='causal',
                activation='tanh')(inp)
x = keras.layers.Conv1D(1024, 2,
                        dilation_rate=256,
                        padding='causal',
                        activation='tanh')(x)
x = keras.layers.Conv1D(512, 2,
                        dilation_rate=128,
                        padding='causal',
                        activation='tanh')(x)
# x = keras.layers.Conv1D(128, 2,
#                         dilation_rate=64,
#                         padding='valid',
#                         activation='tanh')(x)
x = keras.layers.Activation('relu')(x)
x = keras.layers.Convolution1D(1, 1, activation='relu')(x)
x = keras.layers.Convolution1D(1, 1)(x)
x = keras.layers.Flatten()(x)
x = keras.layers.Dense(256, activation='softmax')(x)

# x = keras.layers.LSTM(128, input_shape=(X_train.shape[1], X_train.shape[2]))(inp)
# x = keras.layers.Dense(1)(x)
# x = addon_layers.WeightNormalization(
#     keras.layers.Conv1D(1, 7, padding="same", activation="tanh")
# )(x)

model = keras.Model(inp, x)
# model.compile(loss='mean_squared_error', optimizer=keras.optimizers.Adam(0.001))
model.compile(loss='categorical_crossentropy', optimizer='sgd',
              metrics=['accuracy'])
model.summary()

history = model.fit(
    X_train, y_train,
    epochs=1,
    batch_size=32,
    validation_split=0.1,
    verbose=1,
    shuffle=False
)

model.save("models/sine_timeseries_model.h5")

print(f'Training shape:')
print(f'Testing with X_test shape: {X_test.shape}')
y_pred = model.predict(X_test)
print(f'Prediction with shape: {y_pred.shape}')

plt.plot(np.arange(0, len(y_train)), y_train, 'g', label="history")
plt.plot(np.arange(len(y_train), len(y_train) + len(y_test)), y_test, marker='.', label="true")
plt.plot(np.arange(len(y_train), len(y_train) + len(y_test)), y_pred, 'r', label="prediction")
plt.ylabel('Value')
plt.xlabel('Time Step')
plt.legend()
plt.show()