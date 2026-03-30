# ARM64 base image
FROM --platform=linux/arm64 ubuntu:22.04

# Avoid interactive prompts during build
ENV DEBIAN_FRONTEND=noninteractive

# Workaround for Ubuntu ARM ports GPG signature issues
# This is a known intermittent issue with the ports.ubuntu.com mirror
# TODO  This concerns me as I believe it makes it easy to install hacked images
RUN apt-get update --allow-insecure-repositories || true && \
    apt-get install -y --allow-unauthenticated ca-certificates gnupg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get update

# Install base dependencies
RUN apt-get install -y \
    # Build tools
    build-essential \
    gcc \
    g++ \
    make \
    cmake \
    # Java 17
    openjdk-17-jdk \
    maven \
    # Python and pip
    python3 \
    python3-pip \
    python3-venv \
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
    # Playwright browser dependencies (Chromium/Firefox/WebKit)
    libnss3 \
    libnspr4 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libdbus-1-3 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libasound2 \
    libpango-1.0-0 \
    libcairo2 \
    libpangocairo-1.0-0 \
    libatspi2.0-0 \
    libx11-6 \
    libx11-xcb1 \
    libxcb1 \
    libxext6 \
    libxcursor1 \
    libxi6 \
    libxtst6 \
    libglib2.0-0 \
    libgtk-3-0 \
    libgdk-pixbuf2.0-0 \
    libxshmfence1 \
    libglu1-mesa \
    libenchant-2-2 \
    libsecret-1-0 \
    libhyphen0 \
    libmanette-0.2-0 \
    libflite1 \
    libgstreamer1.0-0 \
    libgstreamer-plugins-base1.0-0 \
    libgstreamer-plugins-bad1.0-0 \
    gstreamer1.0-libav \
    && rm -rf /var/lib/apt/lists/*

# Set Java environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Install Node.js and npm (for Playwright and general JS tooling)
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

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

# Configure npm to use user-local prefix (avoids permission issues with global installs)
RUN mkdir -p ~/.npm-global && \
    npm config set prefix ~/.npm-global

# Add npm global bin to PATH
ENV PATH="/home/developer/.npm-global/bin:${PATH}"

# Install Claude Code CLI and OpenAI Codex CLI via npm
RUN npm install -g @anthropic-ai/claude-code @openai/codex

# Set up bash prompt and environment
RUN echo 'export PS1="\u@\h:\w$ "' >> ~/.bashrc && \
    echo 'alias ll="ls -alh"' >> ~/.bashrc && \
    echo 'alias pip=pip3' >> ~/.bashrc && \
    echo 'alias python=python3' >> ~/.bashrc && \
    echo 'export EDITOR=vim' >> ~/.bashrc && \
    echo 'export PATH="$HOME/.npm-global/bin:$PATH"' >> ~/.bashrc

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
    python3 --version && \
    pip3 --version && \
    node --version && \
    npm --version && \
    claude --version && \
    codex --version && \
    tmux -V

# Set entrypoint to fix permissions on startup
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]

# Default command
CMD ["/bin/bash"]
