import tensorflow_datasets as tfds
import tensorflow as tf

import time
import numpy as np
import matplotlib.pyplot as plt

examples, metadata = tfds.load('ted_hrlr_translate/pt_to_en', with_info=True,
                               as_supervised=True)
train_examples, val_examples = examples['train'], examples['validation']

[en.numpy() for pt, en in train_examples.take(10)]

tokenizer_en = tfds.deprecated.text.SubwordTextEncoder.build_from_corpus(
    (en.numpy() for pt, en in train_examples), target_vocab_size=2**13)

tokenizer_pt = tfds.deprecated.text.SubwordTextEncoder.build_from_corpus(
    (pt.numpy() for pt, en in train_examples), target_vocab_size=2**13)

sample_string = 'Transformer is complicated.'

tokenized_string = tokenizer_en.encode(sample_string)
print ('Tokenized string is {}'.format(tokenized_string))

original_string = tokenizer_en.decode(tokenized_string)
print ('The original string: {}'.format(original_string))

for ts in tokenized_string:
    print ('{} ----> {}'.format(ts, tokenizer_en.decode([ts])))

assert original_string == sample_string

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


val_dataset = val_examples.map(tf_encode)
val_dataset = val_dataset.filter(filter_max_length).padded_batch(BATCH_SIZE)

pt_batch, en_batch = next(iter(val_dataset))
print(pt_batch)
print(en_batch)

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
print (pos_encoding.shape)

# plt.pcolormesh(pos_encoding[0], cmap='RdBu')
# plt.xlabel('Depth')
# plt.xlim((0, 512))
# plt.ylabel('Position')
# plt.colorbar()
# plt.show()

# Attention
query = tf.keras.layers.Input(shape=(None,3,))
value = tf.keras.layers.Input(shape=(4,2,))
key = tf.keras.layers.Input(shape=(4,3,))

x = tf.keras.layers.Attention()([query, value, key])
model = tf.keras.models.Model(inputs=[query, value, key], outputs=x)
model.summary()

temp_k = tf.constant([[[10,0,0],
                       [0,10,0],
                       [0,0,10],
                       [0,0,10]]], dtype=tf.float32)  # (4, 3)

temp_v = tf.constant([[[   1,0],
                       [  10,0],
                       [ 100,5],
                       [1000,6]]], dtype=tf.float32)  # (4, 2)

temp_q = tf.constant([[[0, 10, 0]]], dtype=tf.float32)  # (1, 3)
print(model.predict([temp_q,temp_v,temp_k]))

temp_q = tf.constant([[[0, 0, 10]]], dtype=tf.float32)  # (1, 3)
print(model.predict([temp_q,temp_v,temp_k]))

temp_q = tf.constant([[[10, 10, 0]]], dtype=tf.float32)  # (1, 3)
print(model.predict([temp_q,temp_v,temp_k]))

temp_q = tf.constant([[[0, 0, 10], [0, 10, 0], [10, 10, 0]]], dtype=tf.float32)  # (3, 3)
print(model.predict([temp_q,temp_v,temp_k]))

enc = tf.keras.layers.Input(shape=(40,512,))
tar = tf.keras.layers.Input(shape=(38,512,))

x = tf.keras.layers.Attention()([tar, enc, enc],mask = [None,None])
model = tf.keras.models.Model(inputs=[tar, enc], outputs=x)
model.summary()

# Multiheaded attention
num_heads = None
query = tf.keras.layers.Input(shape=(num_heads,None,3,))
value = tf.keras.layers.Input(shape=(num_heads,4,2,))
key = tf.keras.layers.Input(shape=(num_heads,4,3,))

x = tf.keras.layers.Attention()([query, value, key])
model = tf.keras.models.Model(inputs=[query, value, key], outputs=x)
model.summary()

q = np.array([[[[0, 0, 10],     # head 1
                [0, 10, 0],
                [10, 10, 0]],
               [[0, 0, 20],     # head 2
                [0, 20, 0],
                [10, 10, 0]]
               ]])
v = np.array([[[[   1,0],      # head 1
                [  10,0],
                [ 100,5],
                [1000,6]],
               [[   1, 0],     # head 2
                [   2, 0],
                [   3,15],
                [   4,20]]]])
k = np.array([[[[10,0,0],      # head 1
                [0,10,0],
                [0,0,10],
                [0,0,10]],
               [[10,0,5],      # head 2
                [0,10,0],
                [0,5,10],
                [5,0,10]]]])

print(model.predict([q,v,k]))

class MultiHeadAttention(tf.keras.layers.Layer):
    def __init__(self, d_model = 512, num_heads = 8, causal=False, dropout=0.0):
        super(MultiHeadAttention, self).__init__()

        assert d_model % num_heads == 0
        depth = d_model // num_heads

        self.w_query = tf.keras.layers.Dense(d_model)
        self.split_reshape_query = tf.keras.layers.Reshape((-1,num_heads,depth))
        self.split_permute_query = tf.keras.layers.Permute((2,1,3))

        self.w_value = tf.keras.layers.Dense(d_model)
        self.split_reshape_value = tf.keras.layers.Reshape((-1,num_heads,depth))
        self.split_permute_value = tf.keras.layers.Permute((2,1,3))

        self.w_key = tf.keras.layers.Dense(d_model)
        self.split_reshape_key = tf.keras.layers.Reshape((-1,num_heads,depth))
        self.split_permute_key = tf.keras.layers.Permute((2,1,3))

        self.attention = tf.keras.layers.Attention(causal=causal, dropout=dropout)
        self.join_permute_attention = tf.keras.layers.Permute((2,1,3))
        self.join_reshape_attention = tf.keras.layers.Reshape((-1,d_model))

        self.dense = tf.keras.layers.Dense(d_model)

    def call(self, inputs, mask=None, training=None):
        q = inputs[0]
        v = inputs[1]
        k = inputs[2] if len(inputs) > 2 else v

        query = self.w_query(q)
        query = self.split_reshape_query(query)
        query = self.split_permute_query(query)

        value = self.w_value(v)
        value = self.split_reshape_value(value)
        value = self.split_permute_value(value)

        key = self.w_key(k)
        key = self.split_reshape_key(key)
        key = self.split_permute_key(key)

        if mask is not None:
            if mask[0] is not None:
                mask[0] = tf.keras.layers.Reshape((-1,1))(mask[0])
                mask[0] = tf.keras.layers.Permute((2,1))(mask[0])
            if mask[1] is not None:
                mask[1] = tf.keras.layers.Reshape((-1,1))(mask[1])
                mask[1] = tf.keras.layers.Permute((2,1))(mask[1])

        attention = self.attention([query, value, key], mask=mask)
        attention = self.join_permute_attention(attention)
        attention = self.join_reshape_attention(attention)

        x = self.dense(attention)

        return x

# Encoder and Decoder

class EncoderLayer(tf.keras.layers.Layer):
    def __init__(self,  d_model = 512, num_heads = 8, dff = 2048, dropout = 0.0):
        super(EncoderLayer, self).__init__()

        self.multi_head_attention =  MultiHeadAttention(d_model, num_heads)
        self.dropout_attention = tf.keras.layers.Dropout(dropout)
        self.add_attention = tf.keras.layers.Add()
        self.layer_norm_attention = tf.keras.layers.LayerNormalization(epsilon=1e-6)

        self.dense1 = tf.keras.layers.Dense(dff, activation='relu')
        self.dense2 = tf.keras.layers.Dense(d_model)
        self.dropout_dense = tf.keras.layers.Dropout(dropout)
        self.add_dense = tf.keras.layers.Add()
        self.layer_norm_dense = tf.keras.layers.LayerNormalization(epsilon=1e-6)

    def call(self, inputs, mask=None, training=None):
        # print(mask)
        attention = self.multi_head_attention([inputs,inputs,inputs], mask = [mask,mask])
        attention = self.dropout_attention(attention, training = training)
        x = self.add_attention([inputs , attention])
        x = self.layer_norm_attention(x)
        # x = inputs

        ## Feed Forward
        dense = self.dense1(x)
        dense = self.dense2(dense)
        dense = self.dropout_dense(dense, training = training)
        x = self.add_dense([x , dense])
        x = self.layer_norm_dense(x)

        return x

class DecoderLayer(tf.keras.layers.Layer):
    def __init__(self,  d_model = 512, num_heads = 8, dff = 2048, dropout = 0.0):
        super(DecoderLayer, self).__init__()

        self.multi_head_attention1 =  MultiHeadAttention(d_model, num_heads, causal = True)
        self.dropout_attention1 = tf.keras.layers.Dropout(dropout)
        self.add_attention1 = tf.keras.layers.Add()
        self.layer_norm_attention1 = tf.keras.layers.LayerNormalization(epsilon=1e-6)

        self.multi_head_attention2 =  MultiHeadAttention(d_model, num_heads)
        self.dropout_attention2 = tf.keras.layers.Dropout(dropout)
        self.add_attention2 = tf.keras.layers.Add()
        self.layer_norm_attention2 = tf.keras.layers.LayerNormalization(epsilon=1e-6)


        self.dense1 = tf.keras.layers.Dense(dff, activation='relu')
        self.dense2 = tf.keras.layers.Dense(d_model)
        self.dropout_dense = tf.keras.layers.Dropout(dropout)
        self.add_dense = tf.keras.layers.Add()
        self.layer_norm_dense = tf.keras.layers.LayerNormalization(epsilon=1e-6)

    def call(self, inputs, mask=None, training=None):
        # print(mask)
        attention = self.multi_head_attention1([inputs[0],inputs[0],inputs[0]], mask = [mask[0],mask[0]])
        attention = self.dropout_attention1(attention, training = training)
        x = self.add_attention1([inputs[0] , attention])
        x = self.layer_norm_attention1(x)

        attention = self.multi_head_attention2([x, inputs[1],inputs[1]], mask = [mask[0],mask[1]])
        attention = self.dropout_attention2(attention, training = training)
        x = self.add_attention1([x , attention])
        x = self.layer_norm_attention1(x)


        ## Feed Forward
        dense = self.dense1(x)
        dense = self.dense2(dense)
        dense = self.dropout_dense(dense, training = training)
        x = self.add_dense([x , dense])
        x = self.layer_norm_dense(x)

        return x

class Encoder(tf.keras.layers.Layer):
    def __init__(self, input_vocab_size, num_layers = 4, d_model = 512, num_heads = 8, dff = 2048, maximum_position_encoding = 10000, dropout = 0.0):
        super(Encoder, self).__init__()

        self.d_model = d_model

        self.embedding = tf.keras.layers.Embedding(input_vocab_size, d_model, mask_zero=True)
        self.pos = positional_encoding(maximum_position_encoding, d_model)

        self.encoder_layers = [ EncoderLayer(d_model = d_model, num_heads = num_heads, dff = dff, dropout = dropout) for _ in range(num_layers)]

        self.dropout = tf.keras.layers.Dropout(dropout)

    def call(self, inputs, mask=None, training=None):
        x = self.embedding(inputs)
        # positional encoding
        x *= tf.math.sqrt(tf.cast(self.d_model, tf.float32))  # scaling by the sqrt of d_model, not sure why or if needed??
        x += self.pos[: , :tf.shape(x)[1], :]

        x = self.dropout(x, training=training)

        #Encoder layer
        embedding_mask = self.embedding.compute_mask(inputs)
        for encoder_layer in self.encoder_layers:
            x = encoder_layer(x, mask = embedding_mask)

        return x

    def compute_mask(self, inputs, mask=None):
        return self.embedding.compute_mask(inputs)

class Decoder(tf.keras.layers.Layer):
    def __init__(self, target_vocab_size, num_layers = 4, d_model = 512, num_heads = 8, dff = 2048, maximum_position_encoding = 10000, dropout = 0.0):
        super(Decoder, self).__init__()

        self.d_model = d_model

        self.embedding = tf.keras.layers.Embedding(target_vocab_size, d_model, mask_zero=True)
        self.pos = positional_encoding(maximum_position_encoding, d_model)

        self.decoder_layers = [ DecoderLayer(d_model = d_model, num_heads = num_heads, dff = dff, dropout = dropout)  for _ in range(num_layers)]

        self.dropout = tf.keras.layers.Dropout(dropout)

    def call(self, inputs, mask=None, training=None):
        x = self.embedding(inputs[0])
        # positional encoding
        x *= tf.math.sqrt(tf.cast(self.d_model, tf.float32))  # scaling by the sqrt of d_model, not sure why or if needed??
        x += self.pos[: , :tf.shape(x)[1], :]

        x = self.dropout(x, training=training)

        #Decoder layer
        embedding_mask = self.embedding.compute_mask(inputs[0])
        for decoder_layer in self.decoder_layers:
            x = decoder_layer([x,inputs[1]], mask = [embedding_mask, mask])

        return x

    # Comment this out if you want to use the masked_loss()
    def compute_mask(self, inputs, mask=None):
        return self.embedding.compute_mask(inputs[0])

# Transformer model

num_layers = 4
d_model = 128
dff = 512
num_heads = 8

dropout_rate = 0.1

input_vocab_size = tokenizer_pt.vocab_size + 2
target_vocab_size = tokenizer_en.vocab_size + 2


input = tf.keras.layers.Input(shape=(None,))
target = tf.keras.layers.Input(shape=(None,))
encoder = Encoder(input_vocab_size, num_layers = num_layers, d_model = d_model, num_heads = num_heads, dff = dff, dropout = dropout_rate)
decoder = Decoder(target_vocab_size, num_layers = num_layers, d_model = d_model, num_heads = num_heads, dff = dff, dropout = dropout_rate)

x = encoder(input)
x = decoder([target, x] , mask = encoder.compute_mask(input))
#  tf.keras.layers.Masking ??
x = tf.keras.layers.Dense(target_vocab_size)(x)

model = tf.keras.models.Model(inputs=[input, target], outputs=x)
model.summary()

pt_batch, en_batch = next(iter(val_dataset))
plt.pcolormesh(model.predict([pt_batch,en_batch])[5],cmap='RdBu')
plt.colorbar()

print(model.predict([pt_batch,en_batch]).shape)

class CustomSchedule(tf.keras.optimizers.schedules.LearningRateSchedule):
    def __init__(self, d_model, warmup_steps=4000):
        super(CustomSchedule, self).__init__()

        self.d_model = d_model
        self.d_model = tf.cast(self.d_model, tf.float32)

        self.warmup_steps = warmup_steps

    def __call__(self, step):
        arg1 = tf.math.rsqrt(step)
        arg2 = step * (self.warmup_steps ** -1.5)
        return tf.math.rsqrt(self.d_model) * tf.math.minimum(arg1, arg2)

optimizer = tf.keras.optimizers.Adam(CustomSchedule(d_model), beta_1=0.9, beta_2=0.98,
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

model.compile(optimizer=optimizer, loss = loss, metrics = metrics) # masked_

num_batches = 0
for (batch, (_,_)) in enumerate(train_dataset):
    num_batches = batch
print(num_batches)

val_batches = 0
for (batch, (_,_)) in enumerate(val_dataset):
    val_batches = batch
print(val_batches)

def generator(data_set):
    while True:
        for pt_batch, en_batch in data_set:
            yield ( [pt_batch , en_batch[:, :-1] ] , en_batch[:, 1:] )


history = model.fit(x = generator(train_dataset), validation_data = generator(val_dataset), epochs=20, steps_per_epoch = num_batches, validation_steps = val_batches)

pt_batch, en_batch = next(iter(val_dataset))
plt.pcolormesh(model.predict([pt_batch,en_batch])[5],cmap='RdBu')
plt.colorbar()

print(model.predict([pt_batch,en_batch]).shape)
plt.show()

for i in range(10):
    translation = [tokenizer_en.vocab_size]
    for _ in range(40):
        predict = model.predict([pt_batch[i:i+1],np.asarray([translation])])
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

