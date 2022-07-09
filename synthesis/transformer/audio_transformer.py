import tensorflow_datasets as tfds
import tensorflow as tf
from tensorflow.python.ops.numpy_ops import np_config

import time
import numpy as np
import matplotlib.pyplot as plt

# d_model = 512
d_model = 1024
dff = 2048
maximum_position_encoding = 10000

BUFFER_SIZE = 20000
BATCH_SIZE = 64
# DESIRED_SAMPLES = 8192
DESIRED_SAMPLES = 512
DISCRETE_BINS = 4096

np_config.enable_numpy_behavior()

## Data ##########################################################################
wavs = tf.io.gfile.glob("LJSpeech-1.1/wavs/*.wav")
print(f"Number of audio files: {len(wavs)}")


# Mapper function for loading the audio. This function returns two instances of the wave
def preprocess(filename):
    audio = tf.audio.decode_wav(tf.io.read_file(filename), 1, DESIRED_SAMPLES).audio
    audio = audio[:, 0]
    return audio


# Create tf.data.Dataset objects and apply preprocessing
train_dataset = tf.data.Dataset.from_tensor_slices((wavs,))
train_dataset = train_dataset.map(preprocess, num_parallel_calls=tf.data.AUTOTUNE)
train_dataset = train_dataset.cache()
train_dataset = train_dataset.shuffle(BUFFER_SIZE).padded_batch(BATCH_SIZE)
train_dataset = train_dataset.prefetch(tf.data.experimental.AUTOTUNE)

num_batches = 0
for (batch, (_)) in enumerate(train_dataset):
    num_batches = batch

print(f'Training dataset: {train_dataset.element_spec}, num_batches: {num_batches}')


###################################################################################

def get_angles(pos, i, d_model):
    angle_rates = 1 / np.power(10000, (2 * (i // 2)) / np.float32(d_model))
    return pos * angle_rates


def positional_encoding(position, d_model):
    angle_rads = get_angles(np.arange(position)[:, np.newaxis],
                            np.arange(d_model)[np.newaxis, :],
                            d_model)

    # apply sin to even indices in the array; 2i
    angle_rads[:, 0::2] = np.sin(angle_rads[:, 0::2])

    # apply cos to odd indices in the array; 2i+1
    angle_rads[:, 1::2] = np.cos(angle_rads[:, 1::2])

    pos_encoding = angle_rads[np.newaxis, ...]

    return tf.cast(pos_encoding, dtype=tf.float32)


pos_encoding = positional_encoding(50, 512)

scaling_factor = tf.keras.backend.constant(np.sqrt(d_model), shape=(1, 1, 1))

# Decoder ##################################
# target = tf.keras.layers.Input(shape=(None,))
# x = tf.keras.layers.Embedding(target_vocab_size, d_model )(target) # , mask_zero=True
target = tf.keras.layers.Input(shape=(None,))
discrete = tf.keras.layers.Discretization(
    bin_boundaries=None,
    num_bins=DISCRETE_BINS,
    epsilon=0.01,
    output_mode="int",
    sparse=False,
)
discrete.adapt([-1.0, 1.0])
x = discrete(target)
x = tf.keras.layers.Embedding(DISCRETE_BINS, d_model)(x)  # , mask_zero=True

## positional encoding
x = tf.keras.layers.Multiply()([x, scaling_factor])
pos = positional_encoding(maximum_position_encoding, d_model)
x = tf.keras.layers.Add()([x, pos[:, :tf.shape(x)[1], :]])

## self-attention
query = tf.keras.layers.Dense(d_model)(x)
value = tf.keras.layers.Dense(d_model)(x)
key = tf.keras.layers.Dense(d_model)(x)
attention = tf.keras.layers.Attention(causal=True)([query, value, key])
attention = tf.keras.layers.Dense(d_model)(attention)

x = tf.keras.layers.Add()([x, attention])  # residual connection
x = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

## Feed Forward
dense = tf.keras.layers.Dense(dff, activation='relu')(x)
dense = tf.keras.layers.Dense(d_model)(dense)
x = tf.keras.layers.Add()([x, dense])
decoder = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

######################################################

# x = tf.keras.layers.Dense(target_vocab_size)(decoder)
# x = tf.keras.layers.Dense(DISCRETE_BINS)(decoder)
x = tf.keras.layers.GlobalAveragePooling1D()(decoder)
x = tf.keras.layers.Dense(1)(x)

# base_model = tf.keras.models.Model(inputs=[input, target], outputs=x)
base_model = tf.keras.models.Model(inputs=target, outputs=x)
base_model.summary()

optimizer = tf.keras.optimizers.Adam(0.001, beta_1=0.9, beta_2=0.98,
                                     epsilon=1e-9)

base_model.compile(optimizer=optimizer, loss="mse")

def generator(data_set):
    while True:
        for batch in data_set:
            yield (batch[:, :-1], batch[:, 1:])

def training_map(pt, en):
    return [pt, en[:-1]], en[1:]

history = base_model.fit(x=generator(train_dataset), epochs=1, steps_per_epoch=num_batches)
base_model.save("models/audio_transformer_model.h5")

prediction = [0.0]

for i in range(220500):
    test = []
    if len(prediction) > d_model:
        test = prediction[-d_model:]
    else:
        test = prediction

    predict = base_model.predict(np.asarray([test]))

    if (len(predict) > 1 | len(predict[0]) > 1):
        print(f'predict len: {len(predict)}, {len(predict[0])}')

    prediction.append(predict[0][0])

prediction = np.reshape(np.asarray([prediction]), (-1, 1))
tf.io.write_file("audio_transformer_prediction.wav", tf.audio.encode_wav(prediction, 22050))
print("Wrote wav")