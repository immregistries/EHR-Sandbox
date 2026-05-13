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

After installing dependencies execute ``mvn clean install`` to generate war file `target/ehr.war`

## Environment Variables

[example.env](example.env) Provides a working example and skeleton of Environment Variables to set up with H2 databases

Use ``cp example.env .env``
then configure the variables in `.env`.

If you want to change the JWT Secret in the `.env` use  ```openssl rand -hex 32``` to generate one.

## Compilation

```bash
mvn clean install
```

.war file will be located in `target/ehr.war`

## Dev

Compile in dev mode ``mvn clean install -Pdev``, for a quicker build, the UI will not be included.
, default run port is 8080, to change use `SERVER_PORT` variable in `.env

Compile and run the .war

```bash
mvn clean install
set -a;
source .env;   
set +a;
java -jar target/ehr.war
```

[//]: # (Run with Spring)

[//]: # (boot and example)

[//]: # (parameters:)

[//]: # (``mvn clean spring-boot:run -Pdev``)

