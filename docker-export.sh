#! /bin/bash
mvn clean install -Pprod;
docker build . -t clemhen/ehr-sandbox --platform=linux/amd64;
docker save clemhen/ehr-sandbox -o target/ehr-sandbox-image.tar