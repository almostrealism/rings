# Multi-architecture base image (supports ARM64/AMD64)
FROM --platform=linux/arm64 ubuntu:22.04

# Avoid interactive prompts during build
ENV DEBIAN_FRONTEND=noninteractive

# Install base dependencies
RUN apt-get update && apt-get install -y \
    # Build tools
    build-essential \
    gcc \
    g++ \
    make \
    cmake \
    # Java 17
    openjdk-17-jdk \
    maven \
    # Version control
    git \
    # Utilities
    curl \
    wget \
    unzip \
    vim \
    nano \
    # Terminal multiplexer for persistent sessions
    tmux \
    # OpenCL support (for GPU acceleration)
    ocl-icd-opencl-dev \
    opencl-headers \
    # Additional dependencies
    ca-certificates \
    gnupg \
    lsb-release \
    && rm -rf /var/lib/apt/lists/*

# Set Java environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Install Node.js and npm (for Claude CLI installation)
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

# Install Claude Code CLI via npm
RUN npm install -g @anthropic-ai/claude-code

# Create workspace directory
WORKDIR /workspace

# Set up Maven options for better performance
ENV MAVEN_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"

# Configure git (can be overridden with volume mounts)
RUN git config --global core.autocrlf input && \
    git config --global init.defaultBranch main

# Create a non-root user for development
RUN useradd -m -s /bin/bash developer && \
    usermod -aG sudo developer && \
    echo "developer ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# Create .m2 and .config directories with correct ownership before switching users
RUN mkdir -p /home/developer/.m2 /home/developer/.config/claude && \
    chown -R developer:developer /home/developer/.m2 /home/developer/.config

# Copy entrypoint script
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Switch to developer user
USER developer
WORKDIR /home/developer

# Set up bash prompt and environment
RUN echo 'export PS1="\u@\h:\w$ "' >> ~/.bashrc && \
    echo 'alias ll="ls -alh"' >> ~/.bashrc && \
    echo 'export EDITOR=vim' >> ~/.bashrc

# Configure tmux for better usability
RUN echo '# Enable mouse support' >> ~/.tmux.conf && \
    echo 'set -g mouse on' >> ~/.tmux.conf && \
    echo '' >> ~/.tmux.conf && \
    echo '# Set scrollback buffer size' >> ~/.tmux.conf && \
    echo 'set -g history-limit 10000' >> ~/.tmux.conf && \
    echo '' >> ~/.tmux.conf && \
    echo '# Start window numbering at 1' >> ~/.tmux.conf && \
    echo 'set -g base-index 1' >> ~/.tmux.conf && \
    echo '' >> ~/.tmux.conf && \
    echo '# Enable 256 color support' >> ~/.tmux.conf && \
    echo 'set -g default-terminal "screen-256color"' >> ~/.tmux.conf

# Verify installations
RUN java -version && \
    mvn -version && \
    gcc --version && \
    node --version && \
    npm --version && \
    claude --version && \
    tmux -V

# Set entrypoint to fix permissions on startup
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]

# Default command
CMD ["/bin/bash"]
