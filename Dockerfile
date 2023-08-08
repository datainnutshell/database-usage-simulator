# Use the official AdoptOpenJDK base image with Java 11
FROM adoptopenjdk/openjdk11:latest AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the project files into the container
COPY . .

# Build your Scala application using the Gradle wrapper
RUN ./gradlew clean shadowjar --no-daemon

# Use a smaller base image for the runtime environment
FROM adoptopenjdk/openjdk11:slim

# Set the working directory inside the container
WORKDIR /app

# Copy the application build artifacts from the previous stage
COPY --from=build /app/app/build/libs/app-all.jar app.jar

# Expose the port your application listens on (if needed)
EXPOSE 9000

# Start your Scala application
CMD ["java", "-jar", "app.jar", "-Dconfig.resource=application-akka.conf"]
