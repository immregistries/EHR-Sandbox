# EHR-Sandbox

NOTE: This Repository is in the process of being restructured to improve clarity, branch organization will be revised,
and the Docker
deployment kit on the deployment-kit branch is in the process of being fixed and upgraded.

Currently only H2 database is supported

The EHR Sandbox is a testing tool developed by AIRA and NIST along with the IIS Sandbox tool.
It's purpose is to simulate the behaviour of a Electronic health record (EHR).

## Dependencies

This project relies on dependencies not hosted on maven repository, the GitHub repositories for the dependencies are
specified in `dependencies.json`

This script allows the quick installation of the dependencies

```bash
mkdir ../temp-dependencies;
./dependencies.sh build ../temp-dependencies
```

To force rebuild and checking out the git revision use ``-f`` flag

```bash
./dependencies.sh build ../temp-dependencies -f
```

## Compilation

Execute ``mvn clean install`` to generate war file in base directory

## Environment Variables

[example.env](example.env) Provides a working example and skeleton of Environment Variables to set up with H2 databases

Use
```cp example.env .env```
then configure the variables

```openssl rand -hex 32``` to gene

## Dev

Compile in dev mode ``mvn clean install -Pdev``
, default run port is 9091, to change use ``java -jar -Dserver.port=9091``

Export docker
image ``mvn clean install -Pprod`` ``docker build . -t ehr-sandbox``
``docker save ehr-sandbox -o ehr-sandbox-image.tar``

Run with Spring
boot and example
parameters:
``mvn clean spring-boot:run -Pdev "-Dspring-boot.run.arguments=--server.port=9091 --server.servlet.context-path=/ehr --spring.datasource.url=jdbc:h2:file:../data-h2/ehr"``

