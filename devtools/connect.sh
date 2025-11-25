#!/bin/bash

# Script to attach to or create a tmux session named "claude" in a container
# Usage: ./tmux-session.sh CONTAINER_NAME
# Examples:
#   ./tmux-session.sh dev-sandbox-a

if [ -z "$1" ]; then
    echo "Error: Container name required"
    echo "Usage: $0 CONTAINER_NAME"
    echo ""
    echo "Example:"
    echo "  $0 dev-sandbox-a"
    exit 1
fi

CONTAINER_NAME=$1

# Check if container is running
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "Error: Container '${CONTAINER_NAME}' is not running"
    echo ""
    echo "Start the containers with: docker-compose up -d"
    exit 1
fi

# Try to attach to existing session, or create a new one if it doesn't exist
docker exec -it "${CONTAINER_NAME}" bash -c "tmux attach -t claude 2>/dev/null || tmux new -s claude"
