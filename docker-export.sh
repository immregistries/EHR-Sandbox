#! /bin/bash
mvn clean install -Pprod;
docker build . -t ehr-sandbox;
docker save ehr-sandbox -o target/ehr-sandbox-image.tar