# Stage 1: build the runnable Spring Boot JAR.
# This image includes Maven and a Java 17 JDK (the compiler and development tools).
# "AS build" names this stage so the runtime stage can copy its output.
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working folder inside the build container. Docker creates it if needed.
# Following RUN and relative COPY destinations use this folder.
WORKDIR /build

# Copy Maven's project definition from the build context into /build/pom.xml.
# The build context is the folder passed to docker build (usually ".").
COPY pom.xml .

# Copy application code and resources, including application.properties and Flyway SQL.
# Test sources are not copied; .dockerignore also excludes local files such as .env.
COPY src/main ./src/main

# Download dependencies, compile Java, and package the runnable JAR in /build/target.
# --batch-mode: run without interactive prompts.
# --no-transfer-progress: keep dependency download logs shorter.
# -Dmaven.test.skip=true: skip compiling and running tests for this image build.
# CI runs the PostgreSQL tests separately before building the Docker image.
RUN mvn --batch-mode --no-transfer-progress -Dmaven.test.skip=true package

# Stage 2: create the final image used to run the application.
# JRE = Java runtime; this stage does not need Maven or the Java compiler.
# "jammy" means the image uses Ubuntu 22.04 as its operating-system base.
# A new FROM starts a separate stage; build-stage files do not carry over automatically.
FROM eclipse-temurin:17-jre-jammy

# All following relative paths in this stage start from /app.
WORKDIR /app

# Copy only the runnable JAR from the named build stage and rename it /app/app.jar.
# Build tools and application source files stay out of the final image.
COPY --from=build /build/target/task-tracker-0.0.1-SNAPSHOT.jar app.jar

# Run Java as user ID 10001 and group ID 10001, rather than root.
USER 10001:10001

# Document the port the application listens on INSIDE the container.
# EXPOSE does not publish it to your computer; compose.yaml supplies the port mapping.
EXPOSE 8080

# Default command when a container starts: run the JAR with the JVM timezone set to UTC.
# JSON array (exec form) launches Java without a shell, helping signal handling.
# Database settings are provided at runtime by Compose, not baked into the image.
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "app.jar"]
