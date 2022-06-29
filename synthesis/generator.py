
import numpy as np
import tensorflow as tf
# import tensorflow_io as tfio
from tensorflow import keras
from tensorflow.keras import layers
from tensorflow_addons import layers as addon_layers

DESIRED_SAMPLES = 8192
LEARNING_RATE_GEN = 1e-5
LEARNING_RATE_DISC = 1e-6
BATCH_SIZE = 16

mse = keras.losses.MeanSquaredError()
mae = keras.losses.MeanAbsoluteError()

# Custom keras layer for on-the-fly audio to spectrogram conversion
class MelSpec(layers.Layer):
    def __init__(
            self,
            frame_length=1024,
            frame_step=256,
            fft_length=None,
            sampling_rate=22050,
            num_mel_channels=80,
            freq_min=125,
            freq_max=7600,
            **kwargs,
    ):
        super().__init__(**kwargs)
        self.frame_length = frame_length
        self.frame_step = frame_step
        self.fft_length = fft_length
        self.sampling_rate = sampling_rate
        self.num_mel_channels = num_mel_channels
        self.freq_min = freq_min
        self.freq_max = freq_max
        # Defining mel filter. This filter will be multiplied with the STFT output
        self.mel_filterbank = tf.signal.linear_to_mel_weight_matrix(
            num_mel_bins=self.num_mel_channels,
            num_spectrogram_bins=self.frame_length // 2 + 1,
            sample_rate=self.sampling_rate,
            lower_edge_hertz=self.freq_min,
            upper_edge_hertz=self.freq_max,
        )

    def call(self, audio, training=True):
        return np.random.rand(self.frame_step, 32, self.num_mel_channels)

    def get_config(self):
        config = super(MelSpec, self).get_config()
        config.update(
            {
                "frame_length": self.frame_length,
                "frame_step": self.frame_step,
                "fft_length": self.fft_length,
                "sampling_rate": self.sampling_rate,
                "num_mel_channels": self.num_mel_channels,
                "freq_min": self.freq_min,
                "freq_max": self.freq_max,
            }
        )
        return config

def residual_stack(input, filters):
    """Convolutional residual stack with weight normalization.

    Args:
        filter: int, determines filter size for the residual stack.

    Returns:
        Residual stack output.
    """
    c1 = addon_layers.WeightNormalization(
        layers.Conv1D(filters, 3, dilation_rate=1, padding="same"), data_init=False
    )(input)
    lrelu1 = layers.LeakyReLU()(c1)
    c2 = addon_layers.WeightNormalization(
        layers.Conv1D(filters, 3, dilation_rate=1, padding="same"), data_init=False
    )(lrelu1)
    add1 = layers.Add()([c2, input])

    lrelu2 = layers.LeakyReLU()(add1)
    c3 = addon_layers.WeightNormalization(
        layers.Conv1D(filters, 3, dilation_rate=3, padding="same"), data_init=False
    )(lrelu2)
    lrelu3 = layers.LeakyReLU()(c3)
    c4 = addon_layers.WeightNormalization(
        layers.Conv1D(filters, 3, dilation_rate=1, padding="same"), data_init=False
    )(lrelu3)
    add2 = layers.Add()([add1, c4])

    lrelu4 = layers.LeakyReLU()(add2)
    c5 = addon_layers.WeightNormalization(
        layers.Conv1D(filters, 3, dilation_rate=9, padding="same"), data_init=False
    )(lrelu4)
    lrelu5 = layers.LeakyReLU()(c5)
    c6 = addon_layers.WeightNormalization(
        layers.Conv1D(filters, 3, dilation_rate=1, padding="same"), data_init=False
    )(lrelu5)
    add3 = layers.Add()([c6, add2])

    return add3

# Dilated convolutional block consisting of the Residual stack
def conv_block(input, conv_dim, upsampling_factor):
    """Dilated Convolutional Block with weight normalization.

    Args:
        conv_dim: int, determines filter size for the block.
        upsampling_factor: int, scale for upsampling.

    Returns:
        Dilated convolution block.
    """
    conv_t = addon_layers.WeightNormalization(
        layers.Conv1DTranspose(conv_dim, 16, upsampling_factor, padding="same"),
        data_init=False,
    )(input)
    lrelu1 = layers.LeakyReLU()(conv_t)
    res_stack = residual_stack(lrelu1, conv_dim)
    lrelu2 = layers.LeakyReLU()(res_stack)
    return lrelu2

def generator_loss(real_pred, fake_pred):
    """Loss function for the generator.

    Args:
        real_pred: Tensor, output of the ground truth wave passed through the discriminator.
        fake_pred: Tensor, output of the generator prediction passed through the discriminator.

    Returns:
        Loss for the generator.
    """
    gen_loss = []
    for i in range(len(fake_pred)):
        gen_loss.append(mse(tf.ones_like(fake_pred[i][-1]), fake_pred[i][-1]))

    return tf.reduce_mean(gen_loss)

# Splitting the dataset into training and testing splits
wavs = tf.io.gfile.glob("LJSpeech-1.1/wavs/*.wav")
print(f"Number of audio files: {len(wavs)}")

# Mapper function for loading the audio. This function returns two instances of the wave
def preprocess(filename):
    audio = tf.audio.decode_wav(tf.io.read_file(filename), 1, DESIRED_SAMPLES).audio
    return audio, audio


# Create tf.data.Dataset objects and apply preprocessing
train_dataset = tf.data.Dataset.from_tensor_slices((wavs,))
train_dataset = train_dataset.map(preprocess, num_parallel_calls=tf.data.AUTOTUNE)

# input_shape = (None, 1)
input_shape = (32, 80)
inp = keras.Input(input_shape)
# x = MelSpec()(inp)
x = layers.Conv1D(512, 7, padding="same")(inp)
x = layers.LeakyReLU()(x)
x = conv_block(x, 256, 8)
x = conv_block(x, 128, 8)
x = conv_block(x, 64, 2)
x = conv_block(x, 32, 2)
x = addon_layers.WeightNormalization(
    layers.Conv1D(1, 7, padding="same", activation="tanh")
)(x)

gen_optimizer = keras.optimizers.Adam(
    LEARNING_RATE_GEN, beta_1=0.5, beta_2=0.9, clipnorm=1
)

model = keras.Model(inp, x)
model.summary()
model.compile(gen_optimizer, generator_loss)
model.save("generator-model")

model.fit(train_dataset.shuffle(200).batch(BATCH_SIZE).prefetch(tf.data.AUTOTUNE), epochs=1)
model.save("generator-model")

audio_sample = tf.random.uniform([128, 50, 80])
pred = model.predict(audio_sample, batch_size=32, verbose=1)
print(pred.shape)