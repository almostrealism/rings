import os
import csv
import numpy as np
from PIL import Image, ImageDraw

def generate_images(num_images, image_size, save_dir, csv_file):
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)

    images = []
    labels = []
    with open(csv_file, 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(['image_path', 'label'])

        for i in range(num_images // 2):
            # Create and save circle images
            image = Image.new('L', (image_size, image_size), 0)  # 'L' mode is for grayscale
            draw = ImageDraw.Draw(image)
            radius = np.random.randint(5, image_size // 2)
            x = np.random.randint(radius, image_size - radius)
            y = np.random.randint(radius, image_size - radius)
            draw.ellipse((x - radius, y - radius, x + radius, y + radius), fill=255)
            image_path = os.path.join(save_dir, f'circle_{i}.png')
            image.save(image_path)
            writer.writerow([image_path, 0])

            # Create and save square images
            image = Image.new('L', (image_size, image_size), 0)
            draw = ImageDraw.Draw(image)
            side = np.random.randint(5, image_size // 2)
            x = np.random.randint(0, image_size - side)
            y = np.random.randint(0, image_size - side)
            draw.rectangle((x, y, x + side, y + side), fill=255)
            image_path = os.path.join(save_dir, f'square_{i}.png')
            image.save(image_path)
            writer.writerow([image_path, 1])

    return images, labels

# Parameters
num_images = 500
image_size = 54
save_dir = 'generated_images'
csv_file = 'dataset.csv'

generate_images(num_images, image_size, save_dir, csv_file)
print(f'Generated and saved {num_images} images')
