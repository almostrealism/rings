#!/bin/bash

# Script to attach to or create a tmux session in a container
# Usage: ./connect.sh CONTAINER_NAME [SESSION_NAME]
# Examples:
#   ./connect.sh dev-sandbox-a           # Uses default session name "claude"
#   ./connect.sh dev-sandbox-a mysession # Uses custom session name "mysession"

if [ -z "$1" ]; then
    echo "Error: Container name required"
    echo "Usage: $0 CONTAINER_NAME [SESSION_NAME]"
    echo ""
    echo "Examples:"
    echo "  $0 dev-sandbox-a           # Uses default session name 'claude'"
    echo "  $0 dev-sandbox-a mysession # Uses custom session name 'mysession'"
    exit 1
fi

CONTAINER_NAME=$1
SESSION_NAME=${2:-claude}

# Check if container is running
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "Error: Container '${CONTAINER_NAME}' is not running"
    echo ""
    echo "Start the containers with: docker-compose up -d"
    exit 1
fi

# Try to attach to existing session, or create a new one if it doesn't exist
docker exec -it "${CONTAINER_NAME}" bash -c "tmux attach -t ${SESSION_NAME} 2>/dev/null || tmux new -s ${SESSION_NAME}"
