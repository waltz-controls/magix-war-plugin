FROM tomcat:9-jre11-openjdk-slim

COPY target/magix.war /usr/local/tomcat/webapps