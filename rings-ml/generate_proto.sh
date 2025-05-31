#!/bin/sh

# This must run after the maven build, or
# the proto dependencies will not yet be
# available in the target directory
python -m grpc_tools.protoc \
  -Itarget/proto-dependencies \
  --python_out=src/main/python \
  --grpc_python_out=src/main/python \
  target/proto-dependencies/collections.proto