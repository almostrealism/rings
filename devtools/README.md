# Development Sandboxes

Docker-based development environment with 4 isolated sandboxes for parallel development and testing.

## What's Included

- **Ubuntu 22.04 ARM64** - Base Linux environment
- **Java 17** - OpenJDK with Maven (4GB heap, 1GB metaspace)
- **Build Tools** - GCC, G++, Make, CMake
- **Claude Code CLI** - AI-powered development assistant
- **OpenCL** - GPU acceleration support
- **tmux** - Terminal multiplexer for persistent sessions
- **Git, Vim, Nano** - Standard development tools

## Quick Start

```bash
# Start all sandboxes
docker-compose up -d

# Connect to a sandbox
docker exec -it dev-sandbox-a bash
docker exec -it dev-sandbox-b bash
docker exec -it dev-sandbox-c bash
docker exec -it dev-sandbox-d bash

# Stop all sandboxes
docker-compose down

# Rebuild after Dockerfile changes
docker-compose build
docker-compose up -d
```

## Sandbox Configuration

Each sandbox mounts a different workspace:

- **dev-sandbox-a** → `/Users/michael/AlmostRealism/sandboxA/`
- **dev-sandbox-b** → `/Users/michael/AlmostRealism/sandboxB/`
- **dev-sandbox-c** → `/Users/michael/AlmostRealism/sandboxC/`
- **dev-sandbox-d** → `/Users/michael/AlmostRealism/sandboxD/`

All sandboxes share the Claude config volume and use host networking.

## Working Directory

Inside each container, the working directory is `/workspace/project` which maps to the sandbox-specific mount.

## Using tmux for Persistent Sessions

tmux allows processes to continue running after you disconnect, essential for long-running builds and tests.

### Basic Workflow

```bash
# Connect to sandbox
docker exec -it dev-sandbox-a bash

# Start named tmux session
tmux new -s build

# Run long process
mvn clean install

# Detach (keeps running): Ctrl+b then d
# Close terminal - process continues

# Reconnect later
docker exec -it dev-sandbox-a bash
tmux attach -t build
```

### Essential Commands

```bash
tmux ls                      # List sessions
tmux new -s <name>           # New session
tmux attach -t <name>        # Reattach session
tmux kill-session -t <name>  # Kill session
```

### Inside tmux

- `Ctrl+b d` - Detach (leave running)
- `Ctrl+b c` - New window
- `Ctrl+b n` - Next window
- `Ctrl+b p` - Previous window
- `Ctrl+b %` - Split vertical
- `Ctrl+b "` - Split horizontal

### Pre-configured Features

- Mouse support enabled
- 10,000 line scrollback buffer
- 256 color support
- Window numbering starts at 1

## Common Tasks

```bash
# Build project
mvn clean install

# Build specific module
mvn clean install -pl audio

# Run tests
mvn test -pl audio-space

# Run specific test
mvn test -pl audio -Dtest=CellListTests#export

# Skip tests
mvn clean install -DskipTests
```

## Tips

- Keep containers running - just exec in/out as needed
- Use separate tmux sessions for builds, tests, and interactive work
- Use different sandboxes for parallel builds or testing different branches
- Files edited on host are immediately visible in containers (volume mounts)
- Maven cache and Claude config persist across container restarts

## Troubleshooting

**Out of memory during build:**
```bash
mvn clean install -T 1  # Single-threaded build
```

**Need root access:**
```bash
docker exec -it -u root dev-sandbox-a bash
```

**Clear Docker cache:**
```bash
docker-compose down
docker system prune
docker-compose build --no-cache
docker-compose up -d
```
