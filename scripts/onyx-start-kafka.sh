#!/bin/bash
set -e

export CONFLUENT_HOME=~/confluent-7.6.1
export PATH=$PATH:$CONFLUENT_HOME/bin

# Start Zookeeper/Kafka
confluent local services kafka start
