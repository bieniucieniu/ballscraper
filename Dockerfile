# Stage 1: Build the native binary
FROM eclipse-temurin:17-jdk-focal AS build

WORKDIR /app

# 1. Copy Amper wrapper and project configuration to resolve dependencies
COPY amper amper.bat project.yaml ./
COPY server/module.yaml ./server/
COPY server/server-shared/module.yaml ./server/server-shared/
COPY server/targets/server-native/module.yaml ./server/targets/server-native/

# 2. Resolve dependencies to cache them in a Docker layer
# This downloads Amper itself and the Ktor dependencies
RUN ./amper task :server-native:resolveDependenciesLinuxX64

# 3. Copy the source code
COPY server/src ./server/src

# 4. Run the final build task
RUN ./amper task :server-native:linkLinuxX64Release

# Stage 2: Create a minimal runtime image
FROM debian:stable-slim

WORKDIR /app

# Copy the compiled native binary from the build stage
COPY --from=build /app/build/tasks/_server_targets_server_native_linkLinuxX64Release/server.kexe ./server

# Expose the port the Ktor server is listening on
EXPOSE 8080

# Run the native binary
CMD ["./server"]
