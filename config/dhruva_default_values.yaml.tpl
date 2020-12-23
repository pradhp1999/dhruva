Dhruva:
  config:
    sipListenPoints: '[{"name":"DhruvaUDPNetwork","transport":"UDP","port":5070,"recordRoute":true}]'
    MetricsNamespacePrefix: '{{ .MeetPaas.clusterName }}.dhruva'
    MetricsPort: '8125'
    MachineAccountCredential: "DFHO.rcsv.35.CTDU.dijm.36.CDYZ.tdej.0479"
    aggrMetricsKafkaAddressPropertyName: lmabufKafkaBootstrapServers
    aggrMetricsKafkaTopicName: metrics_app_{{ .MeetPaas.internalSiteName }}_dhruva
    aggrMetricsViaKafkaEnabled: 'true'
    appLogsKafkaTopic: logs_app_{{ .MeetPaas.internalSiteName }}_dhruva
    avroSchemaFallbackEnabled: 'true'
    avroSchemaJson: '{
      "subject": "LmaEventSchema",
      "version": 1,
      "id": 3,
      "schema": "{\"type\":\"record\",\"name\":\"LmaEventSchema\",\"doc\":\"Used by LMA team for log aggregation\",\"fields\":[{\"name\":\"hostname\",\"type\":[\"null\",\"string\"],\"doc\":\"Hostname of machine sending event\",\"default\":null},{\"name\":\"payload\",\"type\":[\"null\",\"string\"],\"doc\":\"Contains the payload to transport\",\"default\":null},{\"name\":\"payload_type\",\"type\":[\"null\",\"string\"],\"doc\":\"Identifies the content type of the payload. E.g.: log, metric\",\"default\":null},{\"name\":\"payload_format\",\"type\":[\"null\",\"string\"],\"doc\":\"Identifies the format of the payload string. E.g.: json, none\",\"default\":null},{\"name\":\"tags\",\"type\":[{\"type\":\"array\",\"items\":\"string\"},\"null\"],\"doc\":\"A list of values that otherwise describe this event - e.g. security, access\",\"default\":[]},{\"name\":\"source\",\"type\":[\"null\",\"string\"],\"doc\":\"identifies the emitter e.g. mercury, locus\",\"default\":null},{\"name\":\"servicelevel\",\"type\":[\"null\",\"string\"],\"doc\":\"e.g. production, integration\",\"default\":null},{\"name\":\"datacenter\",\"type\":[\"null\",\"string\"],\"doc\":\"data center of log event e.g. achm, achm2\",\"default\":null},{\"name\":\"loglevel\",\"type\":[\"null\",\"string\"],\"doc\":\"log level e.g. DEBUG, INFO\",\"default\":null},{\"name\":\"environment\",\"type\":[\"null\",\"string\"],\"doc\":\"e.g. the application environment e.g. a6, a7\",\"default\":null},{\"name\":\"type\",\"type\":[\"null\",\"string\"],\"doc\":\"app (same for all logs emitted by logback)\",\"default\":null}]}"
      }'
    avroSchemaMetricsFallbackEnabled: 'true'
    avroSchemaTimeoutSecs: 10
    clientKeystorePass: changeit
    clientKeystorePath: /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/client.jks
    clusterid: '{{ .MeetPaas.clusterName }}'
    dataCenterIdentifier: '{{ .MeetPaas.meetClusterName }}'
    directMetricsToInfluxDBEnabled: 'false'
    domain: meetapi.webex.com
    environment: '{{ .MeetPaas.clusterName }}'
    influxDBName: metrics
    influxDBRetentionPolicy: default
    influxDBUrl: null
    influxMaxConcurrentRequests: '20'
    influxOperationTimeout: '100'
    influxUseCircuitBreaker: 'true'
    influxWriteOperationTimeout: '100'
    kafkaBootstrapServers: '{{ .MeetPaas.kafka.hostPortAddress }}'
    kafkaDiagHostPortAddress: '{{ .MeetPaas.kafka.hostPortAddress }}'
    kafkaHostPortAddress: '{{ .MeetPaas.kafka.hostPortAddress }}'
    kafkaMeetHostPortAddress: '{{ .MeetPaas.kafka.hostPortAddress }}'
    kafkaMetricsEnabled: 'true'
    kafkaTopicMetricsDeliveryInfoCurrentDC: diagnostic_events
    LOGBACK_HOSTNAME: ${HOSTNAME}
    logstash_tags: dhruva
    metricsKafkaClusterConfig: '{{ .MeetPaas.kafka.hostPortAddress }}'
    metricsKafkaHostPortAddress: metricsKafkaClusterConfig
    metricsPublicUrl: null
    metricsSiteUrl: https://metrics{{ .MeetPaas.messageClusterAddress }}/metrics/api/v1
    metricsUrl: https://metrics{{ .MeetPaas.messageClusterAddress }}/metrics/api/v1
    metrics_environment: '{{ .MeetPaas.clusterName }}'
    logback_include_caller_data: 'false'
    logback_kafka_appender_enabled: 'true'
    logback_metrics_kafka_appender_enabled: 'false'
    jedisPoolHealthCheckMonitorEnabled: 'false'
    enableLettuceRedisDataSourceForAuthCache: 'false'
    enableLettuceRedisDataSourceForFlsCache: 'false'
    enableLettuceRedisDataSourceForOrgCache: 'false'
    enableLettuceRedisDataSourceForUserCache: 'false'
    dhruva_configprefix: elasticache_dhruva
    flsCacheRedisDataSource_configprefix: elasticache_fls
    orgCacheRedisDataSource_configprefix: elasticache_commonidentity
    userCacheRedisDataSource_configprefix: elasticache_commonidentity
    authCacheRedisDataSource_configprefix: elasticache_commonidentity
    elasticache_dhruva_sslEnabled: 'false'
    elasticache_dhruva_startTlsEnabled: 'false'
    elasticache_dhruva_host: null
    elasticache_dhruva_commandTimeoutMillis: 2000
    elasticache_commonidentity_sslEnabled: 'false'
    elasticache_commonidentity_startTlsEnabled: 'false'
    elasticache_commonidentity_host: null
    elasticache_commonidentity_commandTimeoutMillis: 2000
    elasticache_fls_sslEnabled: 'false'
    elasticache_fls_startTlsEnabled: 'false'
    elasticache_fls_host: null
    elasticache_fls_commandTimeoutMillis: 2000
    elasticache_ratelimit_sslEnabled: 'false'
    elasticache_ratelimit_startTlsEnabled: 'false'
    elasticache_ratelimit_host: null
    elasticache_ratelimit_commandTimeoutMillis: 2000
    stdout_level_threshold: 'OFF'
  deploy:
    APPLICATION_NAME: dhruva
    CATALINA_OPTS: null
    canary: 'false'
    JAVA_OPTS: null
    l2sipClusterAddress: 'fedex.webex.com'
  secret:
    elasticache_dhruva_password: null
