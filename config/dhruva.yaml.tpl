APPLICATION_NAME: '{{ .Dhruva.deploy.APPLICATION_NAME }}'
sipListenPoints: '{{ .Dhruva.config.sipListenPoints }}'
MetricsNamespacePrefix: '{{ .Dhruva.config.MetricsNamespacePrefix }}'
MetricsPort: '{{ .Dhruva.config.MetricsPort }}'
aggrMetricsKafkaAddressPropertyName: '{{ .Dhruva.config.aggrMetricsKafkaAddressPropertyName }}'
aggrMetricsKafkaTopicName: '{{ .Dhruva.config.aggrMetricsKafkaTopicName }}'
aggrMetricsViaKafkaEnabled: '{{ .Dhruva.config.aggrMetricsViaKafkaEnabled }}'
appLogsKafkaTopic: '{{ .Dhruva.config.appLogsKafkaTopic }}'
avroSchemaFallbackEnabled: '{{ .Dhruva.config.avroSchemaFallbackEnabled }}'
avroSchemaJson: '{{ .Dhruva.config.avroSchemaJson }}'
avroSchemaMetricsFallbackEnabled: '{{ .Dhruva.config.avroSchemaMetricsFallbackEnabled }}'
avroSchemaMetricsURL: '{{ .Dhruva.config.avroSchemaMetricsURL }}'
avroSchemaURL: '{{ .Dhruva.config.avroSchemaURL }}'
avroSchemaTimeoutSecs: '{{ .Dhruva.config.avroSchemaTimeoutSecs }}'
canary: '{{ .Dhruva.deploy.canary }}'
clientKeystorePass: '{{ .Dhruva.config.clientKeystorePass }}'
clientKeystorePath: '{{ .Dhruva.config.clientKeystorePath }}'
clusterid: '{{ .Dhruva.config.clusterid }}'
dataCenterIdentifier: '{{ .Dhruva.config.dataCenterIdentifier }}'
domain: '{{ .Dhruva.config.domain }}'
environment: '{{ .Dhruva.config.environment }}'
kafkaConnectionTimeoutMillis: '{{ .Dhruva.config.kafkaConnectionTimeoutMillis }}'
kafkaBootstrapServers: '{{ .Dhruva.config.kafkaBootstrapServers }}'
kafkaDiagHostPortAddress: '{{ .Dhruva.config.kafkaDiagHostPortAddress }}'
kafkaHostPortAddress: '{{ .Dhruva.config.kafkaHostPortAddress }}'
kafkaMeetHostPortAddress: '{{ .Dhruva.config.kafkaMeetHostPortAddress }}'
kafkaReconnectBackOffMillis: '{{ .Dhruva.config.kafkaReconnectBackOffMillis }}'
kafkaSecurityProtocol: '{{ .Dhruva.config.kafkaSecurityProtocol }}'
LOGBACK_HOSTNAME: '{{ .Dhruva.config.LOGBACK_HOSTNAME }}'
logstash_pii_sampling_rate: '{{ .Dhruva.config.logstash_pii_sampling_rate }}'
logstash_tags: '{{ .Dhruva.config.logstash_tags }}'
metricsKafkaClusterConfig: '{{ .Dhruva.config.metricsKafkaClusterConfig }}'
metricsKafkaHostPortAddress: '{{ .Dhruva.config.metricsKafkaHostPortAddress }}'
metricsPublicUrl: '{{ .Dhruva.config.metricsPublicUrl }}'
metricsSiteUrl: '{{ .Dhruva.config.metricsSiteUrl }}'
metricsUrl: '{{ .Dhruva.config.metricsUrl }}'
metrics_environment: '{{ .Dhruva.config.metrics_environment }}'
logback_kafka_appender_enabled: '{{ .Dhruva.config.logback_kafka_appender_enabled }}'
logback_metrics_kafka_appender_enabled: '{{ .Dhruva.config.logback_metrics_kafka_appender_enabled }}'
enableLettuceRedisDataSourceForAuthCache: '{{ .Dhruva.config.enableLettuceRedisDataSourceForAuthCache }}'
enableLettuceRedisDataSourceForFlsCache: '{{ .Dhruva.config.enableLettuceRedisDataSourceForFlsCache }}'
enableLettuceRedisDataSourceForOrgCache: '{{ .Dhruva.config.enableLettuceRedisDataSourceForOrgCache }}'
enableLettuceRedisDataSourceForUserCache: '{{ .Dhruva.config.enableLettuceRedisDataSourceForUserCache }}'
dhruva_configprefix: '{{ .Dhruva.config.dhruva_configprefix }}'
flsCacheRedisDataSource_configprefix: '{{ .Dhruva.config.flsCacheRedisDataSource_configprefix }}'
orgCacheRedisDataSource_configprefix: '{{ .Dhruva.config.orgCacheRedisDataSource_configprefix }}'
userCacheRedisDataSource_configprefix: '{{ .Dhruva.config.userCacheRedisDataSource_configprefix }}'
authCacheRedisDataSource_configprefix: '{{ .Dhruva.config.authCacheRedisDataSource_configprefix }}'
elasticache_dhruva_sslEnabled: '{{ .Dhruva.config.elasticache_dhruva_sslEnabled }}'
elasticache_dhruva_startTlsEnabled: '{{ .Dhruva.config.elasticache_dhruva_startTlsEnabled }}'
elasticache_dhruva_host: '{{ .Dhruva.config.elasticache_dhruva_host }}'
elasticache_dhruva_commandTimeoutMillis: '{{ .Dhruva.config.elasticache_dhruva_commandTimeoutMillis }}'
elasticache_commonidentity_sslEnabled: '{{ .Dhruva.config.elasticache_commonidentity_sslEnabled }}'
elasticache_commonidentity_startTlsEnabled: '{{ .Dhruva.config.elasticache_commonidentity_startTlsEnabled }}'
elasticache_commonidentity_host: '{{ .Dhruva.config.elasticache_commonidentity_host }}'
elasticache_commonidentity_commandTimeoutMillis: '{{ .Dhruva.config.elasticache_commonidentity_commandTimeoutMillis }}'
jedisPoolHealthCheckMonitorEnabled: '{{ .Dhruva.config.jedisPoolHealthCheckMonitorEnabled }}'
elasticache_fls_sslEnabled: '{{ .Dhruva.config.elasticache_fls_sslEnabled }}'
elasticache_fls_startTlsEnabled: '{{ .Dhruva.config.elasticache_fls_startTlsEnabled }}'
elasticache_fls_host: '{{ .Dhruva.config.elasticache_fls_host }}'
elasticache_fls_commandTimeoutMillis: '{{ .Dhruva.config.elasticache_fls_commandTimeoutMillis }}'
elasticache_ratelimit_sslEnabled: '{{ .Dhruva.config.elasticache_ratelimit_sslEnabled }}'
elasticache_ratelimit_startTlsEnabled: '{{ .Dhruva.config.elasticache_ratelimit_startTlsEnabled }}'
elasticache_ratelimit_host: '{{ .Dhruva.config.elasticache_ratelimit_host }}'
elasticache_ratelimit_commandTimeoutMillis: '{{ .Dhruva.config.elasticache_ratelimit_commandTimeoutMillis }}'
lmabufKafkaBootstrapServers: '{{ .Dhruva.config.lmabufKafkaBootstrapServers }}'
lmabufKafkaMaxBlockMs: '{{ .Dhruva.config.lmabufKafkaMaxBlockMs }}'
stdout_level_threshold: '{{ .Dhruva.config.stdout_level_threshold }}'
serviceLevel: '{{ .Dhruva.config.serviceLevel }}'
CATALINA_OPTS: '{{ .Dhruva.deploy.CATALINA_OPTS }}'
JAVA_OPTS: '{{ .Dhruva.deploy.JAVA_OPTS }}'
