FROM dockerhub.cisco.com/webexkubed-docker/wbx3_java_base:2020-08-05_23-58-09
LABEL maintainer "dhruva app team"
ADD docker/env.sh /env.sh
ADD integration/target/dhruva-integration-tests.jar /usr/local/dhruva-integration-tests.jar
