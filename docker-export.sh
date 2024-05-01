#! /bin/bash
mvn clean install -Pprod;
docker build . -t clemhen/ehr-sandbox:local --platform=linux/amd64;
docker save clemhen/ehr-sandbox:local -o target/ehr-sandbox-image.tar