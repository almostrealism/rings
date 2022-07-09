"""
Title: Timeseries forecasting for weather prediction
Authors: [Prabhanshu Attri](https://prabhanshu.com/github), [Yashika Sharma](https://github.com/yashika51), [Kristi Takach](https://github.com/ktakattack), [Falak Shah](https://github.com/falaktheoptimist)
Date created: 2020/06/23
Last modified: 2020/07/20
Description: This notebook demonstrates how to do timeseries forecasting using a LSTM model.
"""

"""
## Setup
This example requires TensorFlow 2.3 or higher.
"""

import pandas as pd
import matplotlib.pyplot as plt
import tensorflow as tf
from tensorflow import keras
import numpy as np

split_fraction = 0.715
step = 1

# past = 120
past = 1200
# future = 72
future = 720

learning_rate = 0.001
batch_size = 256
epochs = 5

sequence_length = int(past / step)

DESIRED_SAMPLES = 220500

## Data ##########################################################################
wavs = tf.io.gfile.glob("LJSpeech-1.1/wavs/*.wav")
print(f"Number of audio files: {len(wavs)}")


# Mapper function for loading the audio. This function returns two instances of the wave
def preprocess(filename):
    audio = tf.audio.decode_wav(tf.io.read_file(filename), 1, DESIRED_SAMPLES).audio
    audio = audio[:, 0]
    return audio

data = []

for wav in wavs[0:10]:
    data.extend(preprocess(wav).numpy().tolist())

test_out = np.reshape(np.asarray([data]), (-1, 1))
tf.io.write_file("output/timeseries-training.wav", tf.audio.encode_wav(test_out, 22050))
print("Preprocessed waves")

features = pd.DataFrame(data)
print(features.head())

train_split = int(split_fraction * int(features.shape[0]))
print(f"Train split: {train_split}")

train_data = features.loc[0 : train_split - 1]
val_data = features.loc[train_split:]

"""
# Training dataset

The training dataset labels starts from the 792nd observation (720 + 72).
"""

start = past + future
end = start + train_split

x_train = train_data[[0]].values
y_train = features.iloc[start:end][[0]]

print(len(x_train))
print(len(y_train))

sequence_length = int(past / step)

dataset_train = keras.preprocessing.timeseries_dataset_from_array(
    x_train,
    y_train,
    sequence_length=sequence_length,
    sampling_rate=step,
    batch_size=batch_size,
)

# dataset_train_in = tf.keras.preprocessing.timeseries_dataset_from_array(
#     x_train, None, sequence_length=sequence_length)
# dataset_train_target = tf.keras.preprocessing.timeseries_dataset_from_array(
#     y_train, None, sequence_length=sequence_length)
#
# dataset_train = zip(dataset_train_in, dataset_train_target)

"""
## Validation dataset

The validation dataset must not contain the last 792 rows as we won't have label data for
those records, hence 792 must be subtracted from the end of the data.

The validation label dataset must start from 792 after train_split, hence we must add
past + future (792) to label_start.
"""

x_end = len(val_data) - past - future

label_start = train_split + past + future

x_val = val_data.iloc[:x_end][[i for i in range(1)]].values
y_val = features.iloc[label_start:][[0]]

dataset_val = keras.preprocessing.timeseries_dataset_from_array(
    x_val,
    y_val,
    sequence_length=sequence_length,
    sampling_rate=step,
    batch_size=batch_size,
)

# for v in dataset_train_in.take(1):
#     inputs = v
# for v in dataset_train_target.take(1):
#     targets = v

for batch in dataset_train.take(1):
    inputs, targets = batch

print("Input shape:", inputs.numpy().shape)
print("Target shape:", targets.numpy().shape)

# Model ##########################################################################

inputs = keras.layers.Input(shape=(inputs.shape[1], inputs.shape[2]))
lstm_out = keras.layers.LSTM(128)(inputs)
outputs = keras.layers.Dense(1, activation="tanh")(lstm_out)

model = keras.Model(inputs=inputs, outputs=outputs)
model.compile(optimizer=keras.optimizers.Adam(learning_rate=learning_rate), loss="mse")
model.summary()

"""
We'll use the `ModelCheckpoint` callback to regularly save checkpoints, and
the `EarlyStopping` callback to interrupt training when the validation loss
is not longer improving.
"""

path_checkpoint = "../models/timeseries_checkpoint.h5"
es_callback = keras.callbacks.EarlyStopping(monitor="val_loss", min_delta=0, patience=5)

modelckpt_callback = keras.callbacks.ModelCheckpoint(
    monitor="val_loss",
    filepath=path_checkpoint,
    verbose=1,
    save_weights_only=True,
    save_best_only=True,
)

history = model.fit(
    dataset_train,
    epochs=epochs,
    # validation_data=dataset_val,
    # callbacks=[es_callback, modelckpt_callback],
)

model.save("models/timeseries_model.h5")

def show_plot(plot_data, delta, title):
    labels = ["History", "True Future", "Model Prediction"]
    marker = [".-", "rx", "go"]
    time_steps = list(range(-(plot_data[0].shape[0]), 0))
    if delta:
        future = delta
    else:
        future = 0

    plt.title(title)
    for i, val in enumerate(plot_data):
        if i:
            plt.plot(future, plot_data[i], marker[i], markersize=10, label=labels[i])
        else:
            plt.plot(time_steps, plot_data[i].flatten(), marker[i], label=labels[i])
    plt.legend()
    plt.xlim([time_steps[0], (future + 5) * 2])
    plt.xlabel("Time-Step")
    plt.show()
    return


for x, y in dataset_val.take(5):
    print(f'input: {len(x)}x{len(x[0])}')
    p = model.predict(x)

    print(f'output: {len(p)}')
    print(p)
    show_plot(
        [x[0][:, 0].numpy(), y[0].numpy(), p[0]],
        12,
        "Single Step Prediction",
    )