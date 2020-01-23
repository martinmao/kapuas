#!/usr/bin/env bash

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_92.jdk/Contents/Home

exec_jar=target/maldini-boot-starter-0.0.1-SNAPSHOT.jar

if [[ ! -f "$exec_jar" ]];then
cd ../
mvn clean package -Dmaven.test.skip=true
cd maldini-boot-starter
fi
java -jar ${exec_jar} \
-Dloader.main=org.scleropages.maldini.ApplicationStarter \
-Djdk.tls.client.protocols=TLSv1.2 \
-Dhttps.protocols=TLSv1.2 \
org.springframework.boot.loader.PropertiesLauncher \
--spring.config.location=src/main/resources/application.properties \
--zookeeper.client.embedded-server.enabled=false \
--logging.level.org.scleropages=info \
--logging.level.org.springframework.orm.jpa.JpaTransactionManager=info \
--logging.level.org.springframework.transaction.interceptor=info \
--logging.level.org.springframework.web=info \
--logging.level.org.apache.shiro=info \
--logging.level.org.activiti.engine.impl.persistence.entity=info \
--logging.level.org.apache.cxf.services=info \
--logging.level.org.springframework.jdbc=info \
--logging.level.org.springframework.data.elasticsearch.client.WIRE=info \
--spring.jpa.show-sql=false \
--server.port=18080 \
