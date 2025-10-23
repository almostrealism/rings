# AlmostRealism Development Docker Environment

This directory contains Docker configuration for running the AlmostRealism development environment in a container with all necessary tools.

## What's Included

- **Ubuntu 22.04 ARM64** - Base image for ARM systems
- **Java 17** - OpenJDK for running the project
- **Maven** - Build tool with optimized memory settings
- **GCC/G++** - C/C++ compilers for native code generation
- **Claude Code CLI** - AI-powered development assistant
- **OpenCL** - Headers and libraries for GPU acceleration
- **Git** - Version control

## Prerequisites

- Docker installed on your ARM system
- Docker Compose (usually included with Docker Desktop)

## Quick Start

### Build the Image

```bash
cd /Users/michael/AlmostRealism/rings/devtools
docker-compose build
```

### Start the Container

```bash
docker-compose up -d
```

### Enter the Container

```bash
docker-compose exec rings-dev bash
```

Or use a shorter command:
```bash
docker exec -it rings-dev-environment bash
```

### Stop the Container

```bash
docker-compose down
```

## Usage

### Building the Project

Once inside the container:

```bash
# Navigate to the project root
cd /workspace/AlmostRealism/common

# Build the entire project
mvn clean install

# Build a specific module
mvn clean install -pl algebra

# Run tests
mvn test
```

### Using Claude Code CLI

Inside the container:

```bash
# Start Claude Code CLI
claude code

# Or run a specific command
claude code --help
```

### Working with the Raytracer

```bash
cd /workspace/AlmostRealism/rings/raytracer

# Run specific tests
mvn test -Dtest=SphereTest

# Run with custom timeout
mvn test -Dtest=SimpleRenderTest -DforkedProcessTimeoutInSeconds=300
```

## Volume Mounts

The docker-compose.yml mounts the following:

- **Project files**: `/Users/michael/AlmostRealism` → `/workspace/AlmostRealism`
- **Maven cache**: Persisted in Docker volume for faster builds
- **Claude config**: Persisted in Docker volume for CLI settings

## Memory Configuration

The container is configured with:
- **MAVEN_OPTS**: `-Xmx4096m -XX:MaxMetaspaceSize=1024m`

Adjust these in `docker-compose.yml` if you experience memory issues.

## Customization

### Increase Memory Limits

Edit `docker-compose.yml`:

```yaml
environment:
  - MAVEN_OPTS=-Xmx8192m -XX:MaxMetaspaceSize=2048m
```

### Expose Ports

Uncomment the ports section in `docker-compose.yml`:

```yaml
ports:
  - "8080:8080"
```

### Use Host Network

Uncomment the network_mode line in `docker-compose.yml`:

```yaml
network_mode: host
```

## Troubleshooting

### Container Won't Start

Check platform compatibility:
```bash
docker-compose build --no-cache
```

### Out of Memory Errors

Increase Maven memory in `docker-compose.yml` or run with fewer parallel threads:
```bash
mvn clean install -T 1
```

### Permission Issues

The container runs as user `developer`. If you need root access:
```bash
docker-compose exec -u root rings-dev bash
```

### Native Library Issues

The container includes GCC for native code generation. If you encounter architecture mismatches, ensure you're using the ARM64 platform:

```bash
docker-compose build --platform linux/arm64
```

## Development Workflow

1. **Start container**: `docker-compose up -d`
2. **Enter container**: `docker-compose exec rings-dev bash`
3. **Make changes**: Edit files on your host (they're mounted into the container)
4. **Build**: `mvn clean install` inside the container
5. **Test**: `mvn test -pl <module>` inside the container
6. **Exit**: `exit`
7. **Stop container**: `docker-compose down` (when done for the day)

## Persistent Data

The following are preserved across container restarts:
- Maven dependencies (`.m2` directory)
- Claude CLI configuration
- Git configuration

Your source code changes are made directly on the host filesystem and are immediately visible in the container.

## Tips

- Use `docker-compose logs -f` to see container output
- Use `docker-compose restart` to restart without rebuilding
- Use `docker system prune` to clean up unused Docker resources
- Keep the container running and just exec into it multiple times rather than starting/stopping frequently
