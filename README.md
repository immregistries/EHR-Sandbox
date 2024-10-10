# EHR-Sandbox

NOTE: This Repository is in the process of being restructured to improve quality, branch organization, and the Docker
deployment kit on the deployment-kit branch are in the process of being fixed and upgraded

The EHR Sandbox is a testing tool developed by AIRA and NIST along with the IIS Sandbox tool.
It's purpose is to simulate the behaviour of a Electronic health record (EHR).

Execute ``mvn clean install`` to generate war file in base directory

Compile in dev mode ``mvn clean install -Pdev``
, default run port is 9091, to change use `` java -jar -Dserver.port=9091``

Export docker
image ``mvn clean install -Pprod`` ``docker build . -t ehr-sandbox`` ``docker save ehr-sandbox -o ehr-sandbox-image.tar``

Run with Spring
boot and example
parameters: ``mvn clean spring-boot:run -Pdev "-Dspring-boot.run.arguments=--server.port=9091 --server.servlet.context-path=/ehr --spring.datasource.url=jdbc:h2:file:../data-h2/ehr"``