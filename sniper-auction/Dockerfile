# Use an official OpenJDK 21 runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper and configuration files
COPY app .
COPY gradle .
COPY gradlew .
COPY gradlew.bat .

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew build

# Run the application
CMD ["./gradlew run"]