#!/bin/bash
set -e

# Build project

#Start Redis with ssl enabled in background
redis-server --tls-port 63799 --tls-cert-file ./redis/tls/redis.crt --tls-key-file ./redis/tls/redis.key --tls-ca-cert-file ./redis/tls/ca.crt --requirepass password --daemonize yes

# Start IdServer
source scripts/server-env.sh && java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar -n 1099
