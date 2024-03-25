#! /bin/bash
mvn clean install -Pprod;
docker build . -t ehr-sandbox --platform=linux/amd64;
docker save ehr-sandbox -o target/ehr-sandbox-image.tar