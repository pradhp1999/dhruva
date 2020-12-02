Dhruva:
  config:
    sipListenPoints: '[{"name":"DhruvaTlsPublic","hostIPAddress":"${POD_IP}","transport":"TLS","port":11501,"recordRoute":true,"attachExternalIP":true,"tlsAuthType":"NONE"},{"name":"DhruvaUdpPublic","hostIPAddress":"${POD_IP}","transport":"UDP","port":11500,"recordRoute":true,"attachExternalIP":true},{"name":"DhruvaTlsPrivate","hostIPAddress":"${POD_IP}","transport":"TLS","port":5070,"recordRoute":true,"tlsAuthType":"NONE"}]'
    avroSchemaMetricsURL: https://prod-kafka-schema-registry.prodksr.wbx2.com:8082/subjects/LmaEventSchema/versions/1
    avroSchemaURL: https://prod-kafka-schema-registry.prodksr.wbx2.com:8082/subjects/LmaEventSchema/versions/1
    metricsPublicUrl: https://metrics-intb.ciscospark.com/metrics/api/v1
    elasticache_dhruva_host: '{{ .MeetPaas.redisClusters.app.hostPort }}'
    elasticache_ratelimit_host: '{{ .MeetPaas.redisClusters.app.hostPort }}'
    elasticache_commonidentity_host: '{{ .MeetPaas.redisClusters.ci.hostPort }}'
    elasticache_fls_host: '{{ .MeetPaas.redisClusters.ci.hostPort }}'
    kafkaConnectionTimeoutMillis: '540000'
    kafkaReconnectBackOffMillis: '50'
    kafkaSecurityProtocol: PLAINTEXT
    lmabufKafkaBootstrapServers: '{{ .MeetPaas.kafka.hostPortAddress }}'
    lmabufKafkaMaxBlockMs: '2000'
    logstash_pii_sampling_rate: '1.0'
    influxDBUrl: https://influxdb.int.{{ .MeetPaas.clusterManagerAddress }}:8086
    serviceLevel: integration
    hostPortEnabled: 'true'
    hostIpOrFqdn: dhruva.{{ .MeetPaas.clusterName }}.int.meetapi.webex.com
  deploy:
    CATALINA_OPTS: '-Dcom.sun.management.jmxremote
                        -Dcom.sun.management.jmxremote.host=127.0.0.1
                        -Dcom.sun.management.jmxremote.port=${NOMAD_PORT_jmx}
                        -Dcom.sun.management.jmxremote.ssl=false
                        -Dcom.sun.management.jmxremote.authenticate=false
                        -Djava.security.egd=file:///dev/./urandom
                        -Dfile.encoding=UTF-8
                        -Dcom.sun.management.jmxremote.host=127.0.0.1
                        -Dcom.sun.management.jmxremote=true
                        -Dcom.sun.management.jmxremote.host=${POD_IP}
                        -Djava.rmi.server.hostname=${POD_IP}
                        -Dcom.sun.management.jmxremote.local.only=false
                        -Dcom.sun.management.jmxremote.rmi.port=${NOMAD_PORT_jmx}'
    JAVA_OPTS: '${JAVA_OPTS} -Xms1024m -Xmx1024m
                        -XX:CompressedClassSpaceSize=256m
                        -XX:MetaspaceSize=64m
                        -XX:MaxMetaspaceSize=128m
                        -XX:OnOutOfMemoryError="sh /on-oome-do-kill.sh -p %p -k ${killOnOutOfMemoryErrorEnabled} -m ${emitMetricOnOOM}"
                        -Xss1m
                        -Dorg.apache.catalina.startup.EXIT_ON_INIT_FAILURE=true
                        -Dtomcat.maxConnections=1024
                        -Dtomcat.maxThreads=64
                        -Dtomcat.minSpareThreads=64
                        -Dtomcat.asyncTimeout=10000
                        -Dappdynamics.analytics.agent.url=http://localhost:9095/v2/sinks/bt
                        -Dappdynamics.jvm.shutdown.mark.node.as.historical=true
                        -Dappdynamics.agent.uniqueHostId=${HOSTNAME}
                        -Dspring.jmx.enabled=false
                        -Xms2048m -Xmx2048m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+AlwaysPreTouch
                        -Dtomcat.maxThreads=200
                        -Dtomcat.minSpareThreads=200
                        -Dtomcat.maxConnections=10000
                        -Djdk.tls.ephemeralDHKeySize=2048'
  secret: !!set {}
