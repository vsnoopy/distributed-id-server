#!/bin/bash
set -e

cd common || exit
mvn clean
cd ..  || exit

cd server || exit
mvn clean
cd ..

cd client || exit
mvn clean
cd ..  || exit

