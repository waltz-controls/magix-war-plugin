FROM tomcat:9-jre11-openjdk-slim

COPY elastic-apm-agent-1.32.0.jar /usr/local/tomcat

COPY setenv.sh /usr/local/tomcat/bin

COPY target/magix.war /usr/local/tomcat/webapps