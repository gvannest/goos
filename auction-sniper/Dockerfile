# First stage : build
# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:22 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper and configuration files
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle

# Copy the project files
COPY . .

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew build

# Second stage : run
# Use the minimal base image as the final runtime environment
FROM gcr.io/distroless/base-debian12 AS base

# Copy the Java 22 runtime from base image
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:22 $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /app

# Copy only the built JAR file from the build stage
COPY --from=build /app/app/build/libs/app.jar /app/app.jar

# Use a non-root user
USER 1000

# Development stage
FROM base AS development

# Expose the debug port
EXPOSE 5005

# Run the application with remote debugging enabled
CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app/app.jar"]

# Production stage
FROM base AS production

# Run the application
CMD ["java", "-jar", "/app/app.jar"]