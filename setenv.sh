#!/bin/bash

export JAVA_OPTS="$JAVA_OPTS \
                  -javaagent:/usr/local/tomcat/elastic-apm-agent-1.32.0.jar \
                  -Delastic.apm.service_name=magix \
                  -Delastic.apm.server_urls=http://helm-apm-server-default-apm-server.kube-system.svc.cluster.local:8200 \
                  -Delastic.apm.secret_token= \
                  -Delastic.apm.environment=production \
                  -Delastic.apm.application_packages=de.hzg.wpi.waltz"