docker run -d  --privileged \
    -e TOMCAT_http_port=8980 \
    -e jolokia_PORT=8882 \
    -e TOMCAT_jmx_port=8883 \
    -e TOMCAT_VERSION=${TOMCAT_TEST_VERSION:-9} \
    -e JAVA_VERSION=${JAVA_TEST_VERSION:-8} \
    -e APP_DYNAMICS_ENABLED=FALSE \
    -e fedRampEnabled=true \
    -e jedisPoolHealthCheckMonitorEnabled=false \
    -e buildTime=$(date +%s) \
    -p 18980:8980 \
    -p 5070:5070 \
    --name dhruva \
    dhruva:invalidServiceTag
