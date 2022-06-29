import tensorflow_datasets as tfds
import tensorflow as tf

import time
import numpy as np
import matplotlib.pyplot as plt

## Data #######################################################################
examples, metadata = tfds.load('ted_hrlr_translate/pt_to_en', with_info=True,
                               as_supervised=True)
train_examples, val_examples = examples['train'], examples['validation']

[en.numpy() for pt, en in train_examples.take(10)]

tokenizer_en = tfds.deprecated.text.SubwordTextEncoder.build_from_corpus(
    (en.numpy() for pt, en in train_examples), target_vocab_size=2**13)

tokenizer_pt = tfds.deprecated.text.SubwordTextEncoder.build_from_corpus(
    (pt.numpy() for pt, en in train_examples), target_vocab_size=2**13)

def encode(lang1, lang2):
    lang1 = [tokenizer_pt.vocab_size] + tokenizer_pt.encode(
        lang1.numpy()) + [tokenizer_pt.vocab_size+1]

    lang2 = [tokenizer_en.vocab_size] + tokenizer_en.encode(
        lang2.numpy()) + [tokenizer_en.vocab_size+1]

    return lang1, lang2

def tf_encode(pt, en):
    result_pt, result_en = tf.py_function(encode, [pt, en], [tf.int64, tf.int64])
    result_pt.set_shape([None])
    result_en.set_shape([None])
    return result_pt, result_en

BUFFER_SIZE = 20000
BATCH_SIZE = 64
MAX_LENGTH = 40

def filter_max_length(x, y, max_length=MAX_LENGTH):
    return tf.logical_and(tf.size(x) <= max_length,
                          tf.size(y) <= max_length)

train_dataset = train_examples.map(tf_encode)
train_dataset = train_dataset.filter(filter_max_length)
# cache the dataset to memory to get a speedup while reading from it.
train_dataset = train_dataset.cache()
train_dataset = train_dataset.shuffle(BUFFER_SIZE).padded_batch(BATCH_SIZE)
train_dataset = train_dataset.prefetch(tf.data.experimental.AUTOTUNE)
print('train_dataset.element_spec:', train_dataset.element_spec)


val_dataset = val_examples.map(tf_encode)
val_dataset = val_dataset.filter(filter_max_length).padded_batch(BATCH_SIZE)

pt_batch, en_batch = next(iter(val_dataset))
print(pt_batch)
print(en_batch)

num_batches = 0
for (batch, (_,_)) in enumerate(train_dataset):
    num_batches = batch

val_batches = 0
for (batch, (_,_)) in enumerate(val_dataset):
    val_batches = batch
###############################################################################

print(f'num_batches: {num_batches}, val_batches: {val_batches}')
print(f'train_dataset shape: {train_dataset.element_spec}')
print(f'test_dataset shape: {train_dataset.element_spec}')

d_model = 512
dff=2048
maximum_position_encoding = 10000
input_vocab_size = tokenizer_pt.vocab_size + 2
target_vocab_size = tokenizer_en.vocab_size + 2

def get_angles(pos, i, d_model):
    angle_rates = 1 / np.power(10000, (2 * (i//2)) / np.float32(d_model))
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

scaling_factor = tf.keras.backend.constant(np.sqrt(d_model), shape = (1,1,1))

# Encoder ##################################
input = tf.keras.layers.Input(shape=(None,))

x = tf.keras.layers.Embedding(input_vocab_size, d_model)(input) #, mask_zero=True

## positional encoding
x = tf.keras.layers.Multiply()([x,scaling_factor])
pos = positional_encoding(maximum_position_encoding, d_model)
x = tf.keras.layers.Add()([x, pos[: , :tf.shape(x)[1], :]] )

## self-attention
query = tf.keras.layers.Dense(d_model)(x)
value = tf.keras.layers.Dense(d_model)(x)
key = tf.keras.layers.Dense(d_model)(x)
attention = tf.keras.layers.Attention()([query, value, key])                   # , mask=[query._keras_mask, value._keras_mask]
attention = tf.keras.layers.Dense(d_model)(attention)

x = tf.keras.layers.Add()([x , attention])
x = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

## Feed Forward
dense = tf.keras.layers.Dense(dff, activation='relu')(x)
dense = tf.keras.layers.Dense(d_model)(dense)
x = tf.keras.layers.Add()([x , dense])                                          # residual connection
encoder = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

# Decoder ##################################
target = tf.keras.layers.Input(shape=(None,))

x = tf.keras.layers.Embedding(target_vocab_size, d_model )(target) # , mask_zero=True

## positional encoding
x = tf.keras.layers.Multiply()([x,scaling_factor])
pos = positional_encoding(maximum_position_encoding, d_model)
x = tf.keras.layers.Add()([x, pos[: , :tf.shape(x)[1], :] ])

## self-attention
query = tf.keras.layers.Dense(d_model)(x)
value = tf.keras.layers.Dense(d_model)(x)
key = tf.keras.layers.Dense(d_model)(x)
attention = tf.keras.layers.Attention(causal = True)([query, value, key])       # , mask=[query._keras_mask, value._keras_mask]
attention = tf.keras.layers.Dense(d_model)(attention)

x = tf.keras.layers.Add()([x , attention])                                      # residual connection
x = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

## encoder-decoder attention
query = tf.keras.layers.Dense(d_model)(x)
value = tf.keras.layers.Dense(d_model)(encoder)
key = tf.keras.layers.Dense(d_model)(encoder)
attention = tf.keras.layers.Attention()([query, value, key])                    # , mask=[query._keras_mask, value._keras_mask]
attention = tf.keras.layers.Dense(d_model)(attention)

x = tf.keras.layers.Add()([x , attention])                                      # residual connection
x = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

## Feed Forward
dense = tf.keras.layers.Dense(dff, activation='relu')(x)
dense = tf.keras.layers.Dense(d_model)(dense)
x = tf.keras.layers.Add()([x , dense])                                          # residual connection
decoder = tf.keras.layers.LayerNormalization(epsilon=1e-6)(x)

######################################################

x = tf.keras.layers.Dense(target_vocab_size)(decoder)

base_model = tf.keras.models.Model(inputs=[input,target], outputs=x)
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

    return tf.reduce_sum(_loss)/tf.reduce_sum(mask)


metrics = [loss, masked_loss, tf.keras.metrics.SparseCategoricalAccuracy()]

base_model.compile(optimizer=optimizer, loss = loss, metrics = metrics) # masked_


def generator(data_set):
    while True:
        for pt_batch, en_batch in data_set:
            yield ( [pt_batch , en_batch[:, :-1] ] , en_batch[:, 1:] )

def training_map(pt, en):
    return [pt , en[:-1]] , en[1:]

# def tf_gen(pt, en):
#   input_pt, input_en, output_en = tf.py_function(gen, [pt, en], [tf.int64, tf.int64, tf.int64])
#   input_pt.set_shape([None])
#   input_en.set_shape([None])
#   output_en.set_shape([None])
#   return [input_pt, input_en], output_en


# history = base_model.fit(x = train_dataset.map(training_map), epochs=20)
history = base_model.fit(x = generator(train_dataset), validation_data = generator(val_dataset), epochs=20, steps_per_epoch = num_batches, validation_steps = val_batches)

for i in range(10):
    translation = [tokenizer_en.vocab_size]
    for _ in range(40):
        print(f"Predicting with {[pt_batch[i:i+1],np.asarray([translation])]}")
        predict = base_model.predict([pt_batch[i:i+1],np.asarray([translation])])
        translation.append(np.argmax(predict[-1,-1]))
        if translation[-1] == tokenizer_en.vocab_size + 1:
            break

    real_translation = []
    for w in en_batch[:,1:][i].numpy():
        if w == tokenizer_en.vocab_size + 1:
            break
        real_translation.append(w)
    print(tokenizer_en.decode(real_translation))
    print(tokenizer_en.decode(translation[1:-1]))
    print("")