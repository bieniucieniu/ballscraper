# Stage 1: Build the native binary
FROM eclipse-temurin:17-jdk-focal AS build

# Build argument to specify the target architecture
# Supported values: LinuxX64, LinuxArm64
ARG TARGET_PLATFORM=LinuxX64

WORKDIR /app

# 1. Copy Amper wrapper and project configuration to cache dependencies
COPY amper amper.bat project.yaml ./
COPY server/module.yaml ./server/
COPY server/targets/server-linux/module.yaml ./server/targets/server-linux/
COPY server/targets/server-macos/module.yaml ./server/targets/server-macos/
COPY lib/gitlab/module.yaml ./lib/gitlab/
COPY lib/jira/module.yaml ./lib/jira/

# 2. Resolve dependencies to cache them in a Docker layer
RUN ./amper task :server-linux:resolveDependencies${TARGET_PLATFORM}

# 3. Copy all source code
COPY . .

# 4. Build the native Linux release binary for the specified platform
RUN ./amper task :server-linux:link${TARGET_PLATFORM}Release

# 5. Locate the compiled binary and move it to a predictable location
RUN find build/tasks -name "*.kexe" -type f -exec cp {} /app/server-app \;

# Stage 2: Create a minimal runtime image
FROM debian:stable-slim

WORKDIR /app

# Install CA certificates for HTTPS requests (needed for GitLab/Jira APIs)
RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

# Copy the compiled native binary from the build stage
COPY --from=build /app/server-app ./server

# Expose Ktor default port
EXPOSE 8080

# Run the application
CMD ["./server"]
