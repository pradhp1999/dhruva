FROM dockerhub.cisco.com/webexkubed-docker/wbx3-tomcat:2020-09-09_17-47-03
LABEL maintainer "dhruva app team"
ADD docker/env.sh /env.sh
ADD server/target/dhruva-server.war /usr/local/tomcat/webapps/ROOT.war
