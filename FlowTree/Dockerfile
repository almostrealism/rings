# Use an official Python runtime as a parent image
FROM python:2.7-slim

# Set the working directory to /app
WORKDIR /root

# Copy the current directory contents into the container at /app
ADD . /root

# Add airflow home for install
ENV AIRFLOW_HOME=/airflow

# Install any needed packages specified in requirements.txt
RUN apt-get update && apt-get install -yf --no-install-recommends apt-utils
RUN apt-get install gcc -yf
RUN pip install --upgrade
RUN pip install -U pip setuptools

RUN pip install -r requirements.txt
RUN apt-get install -yf build-essential
RUN apt-get install -yf nano
RUN apt-get install -yf vim-tiny
RUN apt-get install -yf git

# Install cron
RUN apt-get -y install cron

# Run app.py when the container launches
CMD ["python", "init.py"]