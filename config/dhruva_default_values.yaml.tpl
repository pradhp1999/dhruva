Dhruva:
  config:
    sipListenPoints: '[{"name":"DhruvaUDPNetwork","transport":"UDP","port":5070,"recordRoute":false}]'
    MetricsNamespacePrefix: '{{ .MeetPaas.clusterName }}.dhruva'
    MetricsPort: '8125'
    aggrMetricsKafkaAddressPropertyName: lmabufKafkaBootstrapServers
    aggrMetricsKafkaTopicName: metrics_app_{{ .MeetPaas.internalSiteName }}_dhruva
    aggrMetricsViaKafkaEnabled: 'true'
    dataCenterIdentifier: '{{ .MeetPaas.meetClusterName }}'
    domain: meetapi.webex.com
    environment: '{{ .MeetPaas.clusterName }}'
    kafkaBootstrapServers: '{{ .MeetPaas.kafka.hostPortAddress }}'
    kafkaDiagHostPortAddress: '{{ .MeetPaas.kafka.hostPortAddress }}'
    kafkaHostPortAddress: '{{ .MeetPaas.kafka.hostPortAddress }}'
    kafkaMeetHostPortAddress: '{{ .MeetPaas.kafka.hostPortAddress }}'
    metricsKafkaClusterConfig: '{{ .MeetPaas.kafka.hostPortAddress }}'
    metricsKafkaHostPortAddress: metricsKafkaClusterConfig
    metricsPublicUrl: null
    metricsSiteUrl: https://metrics{{ .MeetPaas.messageClusterAddress }}/metrics/api/v1
    metricsUrl: https://metrics{{ .MeetPaas.messageClusterAddress }}/metrics/api/v1
    metrics_environment: '{{ .MeetPaas.clusterName }}'
    elasticache_dhruva_sslEnabled: 'false'
    elasticache_dhruva_startTlsEnabled: 'false'
    elasticache_dhruva_host: null
    elasticache_dhruva_commandTimeoutMillis: 500
  deploy:
    APPLICATION_NAME: dhruva
    CATALINA_OPTS: null
    JAVA_OPTS: null
  secret:
    elasticache_dhruva_password: null