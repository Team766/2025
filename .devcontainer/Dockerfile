FROM mcr.microsoft.com/devcontainers/java:1-17-bookworm

RUN apt update && \
    export DEBIAN_FRONTEND=noninteractive && \
    apt install -y --no-install-recommends \
        screen \
    && rm -rf /var/lib/apt/lists/*