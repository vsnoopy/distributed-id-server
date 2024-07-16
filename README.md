# Identity Server


## Team Members
- Jake Vercella
- Heidi Zhang
- Team 5


## Overview

These projects, 3 and 4, are an extension of project 2.  Project 2 was the implementation of a
simple Identity Server with as distributed client-server architecture with a single datastore.

The project 3 phase requires the extending of project 2 by improving reliability.  This is
achieved by implementing a more robust distributed design that includes multiple servers
with each having its own datastore.  As per the projects requirements, the clients is to
connect to a specific server who has been elected to be the coordinator.  The group of
servers that make up the distributed system are charged with electing the coordinator server.
Besides servicing clients, the coordinator is responsible for make sure the other servers
in the group maintain the state of the coordinator with a period.
We leveraged ZooKeeper for dynamic server discovery and leader election, and we used Kafka
for server synchronization with the leader.

The project 4 phase requires the extending of project 3 by improving performance.  This
objective is achieved allowing the clients to connect to any server in the group, unlike
project 3, where the client connected only to the coordinator.  Though the client can connect
to any server in the server group, the server group must still must elect a coordinator to
manage the maintaining a consistent state among all members of the server group.
In project 4, we leveraged ZooKeeper for dynamic server discovery, but did not have a need
for leader election. We still left it in the code, but it is not used. We used Kafka for
server synchronization.
...

Beyond technologies that provide support for key core functionalities of the Identity Server
(and client) mentioned above, other technologies were leveraged to building and test.  These
technologies include Maven and Docker.


## Manifest

### Source Code
- Client Module:
  - `./client/src/main/java/p3/client/cmd/IdClient.java`: Client main class
  - `./client/src/test/java/p3/ClientTest.java`: Client unit tests
  - `./client/src/main/java/p3/client/cmd/IntTestCases.java`: Integration test cases
- Server Module:
  - `./server/src/test/java/p3/ServerTest.java`: Server unit tests
  - `./server/src/main/java/p3/server/cmd/IdServer.java`: Server main class
  - `./server/src/main/java/p3/server/cmd/Commands.java`: Command logic and processing
  - `./server/src/main/java/p3/server/storage/IdentityRecordProto.java`:  Proto generated file for IdentityRecord objects
  - `./server/src/main/java/p3/server/storage/IdentityRecordHelpers.java`: Helper functions for IdentityRecord objects
  - `./server/src/main/java/p3/server/storage/RedisDatabase.java`: Redis database middleware
  - `./server/src/main/java/p3/server/storage/KafkaConfig.java`: Configuration for Kafka producer and consumer
  - `./server/src/main/java/p3/server/storage/ServerKafkaConsumer.java`: Kafka consumer for server synchronization
- Common Module:
  - `./common/src/test/java/p3/XColorTest.java`: XColor unit tests
  - `./common/src/test/java/p3/XUtilTest.java`: XUtil unit tests
  - `./common/src/main/java/p3/common/util/XColor.java`: Color utility class
  - `./common/src/main/java/p3/common/util/XUtil.java`: Utility class
  - `./common/src/main/java/p3/common/api/RmiApiChannel.java`: RMI API channel interface

### Build Files
- `./Makefile`: Makefile for building and running the project
- `./scripts/build.sh`: Builds the project using maven
- `./scripts/server-env.sh`: Sets environment variables for the server locally
- `./scripts/docker-run.sh`: Runs a specified amount of idServer instances in Docker, along with other services
- `./scripts/gen-redis-certs.sh`: Generates Redis TLS certificates
- `./scripts/clean.sh`: Cleans the project using maven
- `./server/proto/IdentityRecord.proto`: Proto file for IdentityRecord objects
- `./server/proto/buf.gen.yml`: Protobuf-generate configuration file
- `./server/pom.xml`: Maven build file for the server
- `./client/pom.xml`: Maven build file for the client
- `./common/pom.xml`: Maven build file for the common module
- `./client/Dockerfile`: Dockerfile for the client
- `./server/Dockerfile`: Dockerfile for the server
- `./redis/Dockerfile:`: Dockerfile for Redis
- `./docker-compose.yml`: Docker Compose file for redis, server, and client (client for integration tests)
- `./docker-compose-base.yml`: base compose file for redis, server, and client (scripted compose file)

### Redis Files
- `./redis/Dockerfile:`: Dockerfile for Redis
- `./redis/docker-redis-entry.sh`: Entrypoint script for Redis
- `./redis/tls/redis.crt`: Redis certificate
- `./redis/tls/server.key`: Redis server key
- `./redis/tls/client.crt`: Redis client certificate
- `./redis/tls/ca.crt`: Redis CA certificate
- `./redis/tls/openssl.cnf`: OpenSSL configuration file
- `./redis/tls/server.crt`: Redis server certificate
- `./redis/tls/redis.key`: Redis key
- `./redis/tls/ca.txt`: Redis CA text file
- `./redis/tls/redis.dh`: Redis DH file
- `./redis/tls/ca.key`: Redis CA key
- `./redis/tls/client.key`: Redis client key

### Other Files
- `./README.md`: The file you are reading now
- `./server/ssl_security.policy`: Security policy for the server
- `./client/ssl_security.policy`: Security policy for the client
- `./server/tls/client-keystore.p12`: Server client keystore (for redis)
- `./server/tls/Server_Keystore`: Server keystore (for RMI)
- `./server/tls/truststore.jks`: Server truststore (for redis)
- `./client/tls/Client_Truststore`: Client truststore (for redis)


## Building the project
In order to compile the project, you will need to have Maven installed.
You can build the project by running the following command:
```bash
make build
```

To remove compiled files, use:
```bash
make clean
```

To generate Redis certificates, use (these are pre-generated in the repo):
```bash
make redis_certs
```

## Deploying the project
Deploy project services in docker:
```bash
make docker-deploy
```

Stopping project services in docker:
```bash
make down
```

Alternatively you can deploy the services locally.

Install services locally:
```bash
make onyx-install
```

Start kafka locally:
```bash
make onyx-kafka
```

Start zookeeper locally:
```bash
make onyx-zookeeper
```

Start idServer locally:
```bash
make onyx-idserver
```

The Identity Client can be run using the following command:
```bash
java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -s <zookeeper IP> -n [zookeeper port] <COMMAND>
```

To display the help menu for the client and list available commands, use the following:
```bash
java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -h
```

List all make targets:
```bash
make help
```

## Testing
Execute integration tests:
```bash
make intg-test
```
In addition to our integration tests, we also preformed manual testing to ensure the
correctness of our implementation.


## Features and usage
The distributed Identity Server provides the following features:
- All the same great features as the original Identity Server
- Improved reliability with multiple servers
- Improved performance with client connection to any server
- Server synchronization with Kafka
- Dynamic server discovery with ZooKeeper
- And more!

See usage information above for how to run the client and server (Deploying the project).

## Known Bugs
No known bugs at this time.

## Demo
You can view a demo of the project [here](https://www.youtube.com/watch?v=do7MNZxEzAc)

## Reflection

These projects have been great and rewarding opportunity to implement enough features of a
distributed system to allow for us to gain a meaningful understanding of what is involved
in the design of real world distributed system. Overall we are happy with the results of
our implementation. We were able to distribute the system across multiple servers, implement
server synchronization, and provide dynamic server discovery.

From Heidi:

Having a teammate that has a seemingly professional level understanding of technologies like
ZooKeeper, Kafka, Docker, Maven, etc., provided my insight into what I can describe as learning
the difference between being a programmer and being a software engineer.  Though having a
teammate whose software engineering skills far surpassed mine was challenging, I feel that I
was exposed to the next level of software engineering.

Having implemented or having tried to implement, in the early phase of the projects, features
like vector clocks, coordinator election, server synchronization, etc., and trying to debug the
code I had written, I can know fully appreciate the power of technologies like ZooKeeper and
Kafka.  One can benefit greatly if one can overcome the initial learning curve.

I feel these project are great opportunity for me since I can return to the projects this summer
and review in great detail how my teammate pulled all the pieces to orchestrate a cohesive
solution.

## Sources used
https://docs.confluent.io/home/overview.html
https://zookeeper.apache.org/doc/current/index.html
