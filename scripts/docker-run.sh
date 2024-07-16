#!/bin/bash
set -e

# Prompt the user for the number of servers to start
# shellcheck disable=SC2162
read -p "Enter the number of servers to start: " num_servers

# Base port number for servers
base_server_port=1099

# Copy the base docker-compose file to a new file
cp docker-compose-base.yml docker-compose.yml

# Generate the services in the docker-compose file
# shellcheck disable=SC2004
for ((i=1; i<$num_servers; i++)); do
  # Calculate the port numbers for this server and Redis instance
  server_port=$((base_server_port + i))

  # Add the Redis service
  echo "  redis$i:
    container_name: redis$i
    extends:
      service: redis0
    volumes:
      - redis-data$i:/data" >> docker-compose.yml

  # Add the server service
  echo "  server$i:
    container_name: server$i
    extends:
      service: server0
    ports: !override
      - \"$server_port:1099\"
    environment:
      ID_SERVER_HOST: localhost
      ID_SERVER_PORT: $server_port
      REDIS_HOST: redis$i" >> docker-compose.yml
done

# Generate the volume declarations in the docker-compose file
echo "volumes:" >> docker-compose.yml
# shellcheck disable=SC2004
for ((i=0; i<$num_servers; i++)); do
  echo "  redis-data$i: {}" >> docker-compose.yml
done