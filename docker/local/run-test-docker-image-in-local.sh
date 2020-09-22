docker run -d  --privileged \
    -v `pwd`/run-test-group-1.sh:/usr/local/run-test.sh \
    -e APP_DYNAMICS_ENABLED=FALSE \
    -e fedRampEnabled=true \
    -e CassandraHostAddress=host.docker.internal \
    -e cassandraHostAddress=host.docker.internal \
    -e buildTime=$(date +%s) \
    -e helloWorldPublicUrl=http://192.168.0.20:8980/api/v1 \
    --name dhruva-tests \
    dhruva-tests:invalidServiceTag \
  sh -c "ls -lrth /usr/local/; chmod 777 /usr/local/da.sh;/usr/local/run-test.sh"
