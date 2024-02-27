FROM bitnami/tomcat:9.0 as tomcat

RUN rm -rf /opt/bitnami/tomcat/webapps/ROOT && \
    rm -rf /opt/bitnami/tomcat/webapps_default/ROOT

USER root
RUN mkdir -p /target && chown -R 1001:1001 target
USER 1001

COPY --chown=1001:1001 catalina.properties /opt/bitnami/tomcat/conf/catalina.properties
COPY --chown=1001:1001 target/ehr.war /opt/bitnami/tomcat/webapps_default/ehr.war

#ENV ALLOW_EMPTY_PASSWORD=yes
ENV TOMCAT_PASSWORD="28y341834uf8u3bfppkaebiThisIsSomehtingThatShouldBeModified917628\][3p1[l41[ppu398nmjq09o321"