import time
import torch
import torch.nn as nn


class ConvolutionTest(nn.Module):
    def __init__(self, input_dim, conv_size, conv_filters):
        super(ConvolutionTest, self).__init__()
        self.conv = nn.Conv2d(1, conv_filters, kernel_size=conv_size)

    def forward(self, x):
        return self.conv(x)

def train(model, input_tensor, epochs, steps_per_epoch):
    optimizer = torch.optim.Adam(model.parameters())
    criterion = nn.MSELoss()

    model.train()
    for epoch in range(epochs):
        for step in range(steps_per_epoch):
            optimizer.zero_grad()
            output = model(input_tensor)
            loss = criterion(output, torch.randn_like(output))
            loss.backward()
            optimizer.step()

if __name__ == "__main__":
    dim = 64
    filters = 8
    conv_size = 3
    epochs = 100
    steps_per_epoch = 1000

    model = ConvolutionTest(input_dim=dim, conv_size=conv_size, conv_filters=filters)

    input_tensor = torch.rand(1, 1, dim, dim)  # Batch size 1, 1 channel, dim x dim

    start_time = time.time()
    train(model, input_tensor, epochs, steps_per_epoch)
    print("Training time: {:.2f} seconds".format(time.time() - start_time))
