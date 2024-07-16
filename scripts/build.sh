#!/bin/bash
set -e

#Build common
cd common || exit
mvn clean install
cd .. || exit

#Build server
cd server || exit
mvn clean install
cd .. || exit

#Build client
cd client || exit
mvn clean install
cd .. || exit

