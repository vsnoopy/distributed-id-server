# Start from a base image
FROM amazoncorretto:17.0.7-alpine

# Set the working directory in the docker image
WORKDIR /app

# Server JAR file
ARG SERVER_JAR_FILE=/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar

# Copy to container
COPY ${SERVER_JAR_FILE} /app/server.jar

# Copy the TLS certificates to the container
COPY /tls ./tls

# Copy the security policy file to the container
COPY ssl_security.policy .

# Install net-tools so we can use netcat to check if RMI is running and listening
RUN apk update && apk add net-tools

# Expose port 1099
EXPOSE 1099

# Run the application
ENTRYPOINT ["java", "-jar", "server.jar", "-n=1099", "-v"]