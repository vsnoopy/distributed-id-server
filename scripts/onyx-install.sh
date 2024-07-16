#!/bin/bash
set -e

# Download Kafka/Zookeeper
curl -O https://packages.confluent.io/archive/7.6/confluent-7.6.1.tar.gz
tar xzf confluent-7.6.1.tar.gz
export CONFLUENT_HOME=~/confluent-7.6.1
export PATH=$PATH:$CONFLUENT_HOME/bin

# add configuration for Kafka
echo "tickTime=3000" >> $CONFLUENT_HOME/etc/kafka/zookeeper.properties
sed -i 's/clientPort=2181/clientPort=2182/g' $CONFLUENT_HOME/etc/kafka/zookeeper.properties
echo "broker.rack=rack1" >> $CONFLUENT_HOME/etc/kafka/server.properties

# Download Zookeeper for IdServers
wget https://dlcdn.apache.org/zookeeper/zookeeper-3.9.2/apache-zookeeper-3.9.2-bin.tar.gz
tar xzf apache-zookeeper-3.9.2-bin.tar.gz
