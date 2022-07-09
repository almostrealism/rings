import tensorflow_datasets as tfds
import tensorflow as tf
from tensorflow.python.ops.numpy_ops import np_config

import time
import numpy as np
import matplotlib.pyplot as plt

d_model = 512
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
    print(f'audio: {audio.shape}')
    audio = audio[:, 0]
    return audio, audio


# Create tf.data.Dataset objects and apply preprocessing
train_examples = tf.data.Dataset.from_tensor_slices((wavs,))
train_examples = train_examples.map(preprocess, num_parallel_calls=tf.data.AUTOTUNE)


def encode(v1, v2):
    return v1, v2


def tf_encode(pt, en):
    result_pt, result_en = tf.py_function(encode, [pt, en], [tf.float32, tf.float32])
    result_pt.set_shape([None])
    result_en.set_shape([None])

    return result_pt, result_en


train_dataset = train_examples.map(tf_encode)
train_dataset = train_dataset.cache()
train_dataset = train_dataset.shuffle(BUFFER_SIZE).padded_batch(BATCH_SIZE)
train_dataset = train_dataset.prefetch(tf.data.experimental.AUTOTUNE)

num_batches = 0
for (batch, (_, _)) in enumerate(train_dataset):
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

# Encoder ##################################
# input = tf.keras.layers.Input(shape=(None,))
# x = tf.keras.layers.Embedding(input_vocab_size, d_model)(input) #, mask_zero=True
""""
input = tf.keras.layers.Input(shape=(None,))
discrete = tf.keras.layers.Discretization(
    bin_boundaries=None,
    num_bins=DISCRETE_BINS,
    epsilon=0.01,
    output_mode="int",
    sparse=False,
)
discrete.adapt([-1.0, 1.0])
x = discrete(input)
x = tf.keras.layers.Embedding(DISCRETE_BINS, d_model)(x)  # , mask_zero=True

## positional encoding
x = tf.keras.layers.Multiply()([x, scaling_factor])
pos = positional_encoding(maximum_position_encoding, d_model)
x = tf.keras.layers.Add()([x, pos[:, :tf.shape(x)[1], :]])

## self-attention
query = tf.keras.layers.Dense(d_model)(x)
value = tf.keras.layers.Dense(d_model)(x)
key = tf.keras.layers.Dense(d_model)(x)
attention = tf.keras.layers.Attention()([query, value, key])  # , mask=[query._keras_mask, value._keras_mask]
attention = tf.keras.layers.Dense(d_model)(attention)

x = tf.keras.layers.Add()([x, attention])
x = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

## Feed Forward
dense = tf.keras.layers.Dense(dff, activation='relu')(x)
dense = tf.keras.layers.Dense(d_model)(dense)
x = tf.keras.layers.Add()([x, dense])  # residual connection
encoder = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)
"""

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
attention = tf.keras.layers.Attention(causal=True)([query, value, key])  # , mask=[query._keras_mask, value._keras_mask]
attention = tf.keras.layers.Dense(d_model)(attention)

x = tf.keras.layers.Add()([x, attention])  # residual connection
x = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

"""
## encoder-decoder attention
query = tf.keras.layers.Dense(d_model)(x)
value = tf.keras.layers.Dense(d_model)(encoder)
key = tf.keras.layers.Dense(d_model)(encoder)
attention = tf.keras.layers.Attention()([query, value, key])  # , mask=[query._keras_mask, value._keras_mask]
attention = tf.keras.layers.Dense(d_model)(attention)

x = tf.keras.layers.Add()([x, attention])  # residual connection
x = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)
"""

## Feed Forward
dense = tf.keras.layers.Dense(dff, activation='relu')(x)
dense = tf.keras.layers.Dense(d_model)(dense)
x = tf.keras.layers.Add()([x, dense])  # residual connection
decoder = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

######################################################

# x = tf.keras.layers.Dense(target_vocab_size)(decoder)
# x = tf.keras.layers.Dense(DISCRETE_BINS)(decoder)
x = tf.keras.layers.Dense(DESIRED_SAMPLES)(decoder)

# base_model = tf.keras.models.Model(inputs=[input, target], outputs=x)
base_model = tf.keras.models.Model(inputs=target, outputs=x)
base_model.summary()

optimizer = tf.keras.optimizers.Adam(0.001, beta_1=0.9, beta_2=0.98,
                                     epsilon=1e-9)

loss = tf.keras.losses.SparseCategoricalCrossentropy(
    from_logits=True, reduction='none')


def masked_loss(y_true, y_pred):
    mask = tf.math.logical_not(tf.math.equal(y_true, 0))
    _loss = loss(y_true, y_pred)

    mask = tf.cast(mask, dtype=_loss.dtype)
    _loss *= mask

    return tf.reduce_sum(_loss) / tf.reduce_sum(mask)


metrics = [loss, masked_loss, tf.keras.metrics.SparseCategoricalAccuracy()]

base_model.compile(optimizer=optimizer, loss=loss, metrics=metrics)  # masked_


# def generator(data_set):
#     while True:
#         for pt_batch, en_batch in data_set:
#             yield ([pt_batch, en_batch[:, :-1]], en_batch[:, 1:])

def generator(data_set):
    while True:
        for pt_batch, en_batch in data_set:
            yield (en_batch[:, :-1], en_batch[:, 1:])


def training_map(pt, en):
    return [pt, en[:-1]], en[1:]


# history = base_model.fit(x = generator(train_dataset), validation_data = generator(val_dataset), epochs=20, steps_per_epoch = num_batches, validation_steps = val_batches)
# history = base_model.fit(x=generator(train_dataset), epochs=5, steps_per_epoch=num_batches)
history = base_model.fit(x=generator(train_dataset), epochs=1, steps_per_epoch=num_batches)
base_model.save("models/audio_transformer_model.h5")

translation = [0.0, -1.0, 0.0, 1.0, 0.0, 1.0, 0.0, -1.0]
for i in range(1):
    print(f"Predicting with {[np.asarray([translation])]}")
    predict = base_model.predict(np.asarray([translation]))
    print(f"Predict = {predict}")