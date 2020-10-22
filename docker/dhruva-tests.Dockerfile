FROM dockerhub.cisco.com/webexkubed-docker/wbx3_java_base:2020-09-12_01-08-27
LABEL maintainer="dhruva app team"
ADD docker/env.sh /env.sh
ADD integration/target/dhruva-integration-tests.jar /usr/local/dhruva-integration-tests.jar
