#!/bin/bash
set -e

# Setup server environment variables
export REDIS_HOST=localhost
export REDIS_PORT=63799
export REDIS_TRUSTSTORE=./server/tls/truststore.jks
export REDIS_TRUSTSTORE_PASSWORD=temppass
export REDIS_KEYSTORE=./server/tls/client-keystore.p12
export REDIS_KEYSTORE_PASSWORD=temppass
export REDIS_PASSWORD=password
export RMI_KEYSTORE=./server/tls/Server_Keystore
export RMI_KEYSTORE_PASSWORD=password
export RMI_POLICY=./server/ssl_security.policy
#query user for the IP of zookeeper
read -p "Enter the host:port of the Zookeeper server: " ZOO
read -p "Enter the host:port of the Kafka server: " KAFKA
export ZOOKEEPER_SERVER=$ZOO
export KAFKA_SERVER=$KAFKA
# Get the IP address of the host machine
ID_SERVER_HOST=$(hostname -I | awk '{print $1}')
export ID_SERVER_HOST
export ID_SERVER_PORT=1099