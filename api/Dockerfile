# Use OpenJDK 17 as the base image
FROM openjdk:11-ea-17-jre

# Set the working directory
WORKDIR /app

# Copy the compiled Java application JAR file to the container
COPY target/my-app.jar .

# Set the command to run the Java application
CMD ["java", "-jar", "my-app.jar"]
