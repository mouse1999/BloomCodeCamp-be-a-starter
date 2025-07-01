# ----------------------------------------------------------------------------------------------------
# Stage 1: Build the Spring Boot application
# ----------------------------------------------------------------------------------------------------
FROM eclipse-temurin:17.0.15_6-jdk-ubi9-minimal AS builder

# Set explicit Java paths (critical for Gradle toolchain detection)
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

# Install required utilities
RUN microdnf install -y findutils shadow-utils && \
    microdnf clean all

# Verify Java installation
RUN java -version && \
    javac -version && \
    echo "JAVA_HOME: $JAVA_HOME" && \
    ls -la $JAVA_HOME/bin/java

# Set working directory
WORKDIR /backend

# --- GRADLE CONFIGURATION ---
# Copy Gradle files first for caching
COPY build.gradle settings.gradle ./
COPY gradlew .
COPY gradle ./gradle


# Make gradlew executable
RUN chmod +x gradlew

# Verify environment
RUN ./gradlew --version

# Download dependencies (cached unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon


# Copy source code and build
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon --stacktrace

# ----------------------------------------------------------------------------------------------------
# Stage 2: Runtime
# ----------------------------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-ubi9-minimal

# Non-root user setup
RUN groupadd --system spring && \
    useradd --system --no-create-home --gid spring spring && \
    mkdir -p /backend && \
    chown spring:spring /backend

USER spring
WORKDIR /backend

# Copy built JAR
ARG JAR_FILE=/backend/build/libs/*.jar
COPY --from=builder ${JAR_FILE} app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]