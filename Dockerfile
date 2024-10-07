FROM bitnami/tomcat:10.0 as tomcat

RUN rm -rf /opt/bitnami/tomcat/webapps/* && \
    rm -rf /opt/bitnami/tomcat/webapps_default/*

#RUN mkdir /opt/bitnami/tomcat/webapps/ROOT && \
#    echo '<% response.sendRedirect("/ehr/#/home"); %>' > /opt/bitnami/tomcat/webapps/ROOT/index.jsp
# Set up auto redirection from root
RUN mkdir /opt/bitnami/tomcat/webapps_default/ROOT
RUN echo '<% response.sendRedirect("/ehr/#/home"); %>' > /opt/bitnami/tomcat/webapps_default/ROOT/index.jsp

USER root
RUN mkdir ~/data-h2 && chown -R 1001:1001 ~/data-h2
RUN mkdir -p /target && chown -R 1001:1001 target
USER 1001

COPY --chown=1001:1001 catalina.properties /opt/bitnami/tomcat/conf/catalina.properties
COPY --chown=1001:1001 target/ehr.war /opt/bitnami/tomcat/webapps_default/ehr.war

ENV TOMCAT_PASSWORD="28y341834uf8u3bfppkaebiThisIsSomehtingThatShouldBeModified917628\][3p1[l41[ppu398nmjq09o321"