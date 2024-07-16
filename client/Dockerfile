# Start from a base image
FROM openjdk:8

# Set the working directory in the docker image
WORKDIR /app

# Client JAR file
ARG CLIENT_JAR_FILE=target/client-1.0-SNAPSHOT-jar-with-dependencies.jar

# Copy to container
COPY ${CLIENT_JAR_FILE} /app/client.jar

# Copy the TLS folder to the container
COPY tls ./client/tls

# Copy the security policy file to the container
COPY ssl_security.policy ./client

# Run the application in test mode
ENTRYPOINT ["java", "-jar", "client.jar", "-s=localhost", "--test"]
