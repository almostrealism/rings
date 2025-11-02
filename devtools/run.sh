#!/bin/bash

# AlmostRealism Docker Development Environment Helper Script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Helper functions
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
}

# Build the image
build() {
    print_info "Building Docker image for ARM64..."
    docker-compose build --platform linux/arm64
    print_info "Build complete!"
}

# Start the container
start() {
    print_info "Starting development container..."
    docker-compose up -d
    print_info "Container started!"
    print_info "Use './run.sh shell' to enter the container"
}

# Stop the container
stop() {
    print_info "Stopping development container..."
    docker-compose down
    print_info "Container stopped!"
}

# Restart the container
restart() {
    print_info "Restarting development container..."
    docker-compose restart
    print_info "Container restarted!"
}

# Enter the container shell
shell() {
    print_info "Entering development container..."
    docker-compose exec rings-dev bash
}

# Show container status
status() {
    print_info "Container status:"
    docker-compose ps
}

# Show container logs
logs() {
    docker-compose logs -f
}

# Clean up (remove container and volumes)
clean() {
    print_warn "This will remove the container and all cached data (Maven, Claude config)"
    read -p "Are you sure? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Cleaning up..."
        docker-compose down -v
        print_info "Cleanup complete!"
    else
        print_info "Cleanup cancelled"
    fi
}

# Rebuild from scratch
rebuild() {
    print_info "Rebuilding from scratch..."
    docker-compose down
    docker-compose build --no-cache --platform linux/arm64
    docker-compose up -d
    print_info "Rebuild complete!"
}

# Show help
show_help() {
    cat << EOF
AlmostRealism Docker Development Environment

Usage: ./run.sh [command]

Commands:
    build       Build the Docker image
    start       Start the development container
    stop        Stop the development container
    restart     Restart the development container
    shell       Enter the container shell
    status      Show container status
    logs        Show container logs (follow mode)
    clean       Remove container and volumes
    rebuild     Rebuild from scratch (no cache)
    help        Show this help message

Examples:
    ./run.sh build      # Build the image
    ./run.sh start      # Start the container
    ./run.sh shell      # Enter the container
    ./run.sh stop       # Stop the container

Quick start:
    ./run.sh build && ./run.sh start && ./run.sh shell
EOF
}

# Main command router
main() {
    check_docker

    case "${1:-help}" in
        build)
            build
            ;;
        start)
            start
            ;;
        stop)
            stop
            ;;
        restart)
            restart
            ;;
        shell)
            shell
            ;;
        status)
            status
            ;;
        logs)
            logs
            ;;
        clean)
            clean
            ;;
        rebuild)
            rebuild
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            echo
            show_help
            exit 1
            ;;
    esac
}

main "$@"
