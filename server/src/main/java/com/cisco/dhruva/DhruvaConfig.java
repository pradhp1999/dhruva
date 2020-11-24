package com.cisco.dhruva;

import com.cisco.dhruva.common.dns.*;
import com.cisco.dhruva.common.dns.metrics.DnsReporter;
import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.metric.InfluxClient;
import com.cisco.dhruva.common.metric.MetricClient;
import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.service.SipServerLocatorService;
import com.ciscospark.server.Wx2ConfigAdapter;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnWebApplication
public class DhruvaConfig extends Wx2ConfigAdapter {

  @Autowired DhruvaSIPConfigProperties dhruvaSIPConfigProperties;

  @Autowired private Environment env;

  private static final long DEFAULT_CACHE_TIMEOUT = 10;

  public static final String MACHINE_ACCOUNT_CREDENTIAL = "MachineAccountCredential";

  @Override
  public String getServiceName() {
    return "Dhruva";
  }

  @Override
  public String getMetricsNamespace() {
    return "dhruva";
  }

  @Override
  public String getUserAgent() {
    return "Dhruva/1.0";
  }

  @Bean
  public ExecutorService getExecutorService() {
    return new ExecutorService("DhruvaSipServer");
  }

  @Bean
  public MetricClient getMetricClient() {
    return new InfluxClient();
  }

  @Bean
  public Integer defaultCacheTimeout() {
    return (int) TimeUnit.MINUTES.toSeconds(DEFAULT_CACHE_TIMEOUT);
  }

  @Bean
  @Lazy
  public DnsInjectionService dnsInjectionService() {
    // TODO check for redis
    return DnsInjectionService.memoryBackedCache();
  }

  @Bean
  public DnsReporter dnsReporter() {
    return new DnsMetricsReporter();
  }

  @Bean
  public SipServerLocatorService sipServerLocatorService() {
    return new SipServerLocatorService(dhruvaSIPConfigProperties);
  }

  @Bean
  public DnsReporter dnsMetricsReporter() {
    return new DnsMetricsReporter();
  }

  @Bean
  public DnsLookup dnsLookup() {
    return DnsResolvers.newBuilder()
        .cacheSize(dhruvaSIPConfigProperties.getDhruvaDnsCacheMaxSize())
        .dnsLookupTimeoutMillis(dhruvaSIPConfigProperties.dnsCacheRetentionTimeMillis())
        .retentionDurationMillis(dhruvaSIPConfigProperties.dnsCacheRetentionTimeMillis())
        .metered(getApplicationContext().getBean(DnsMetricsReporter.class))
        .build();
  }

  //  @Bean
  //  public MeetingRegistryClientFactory meetingRegistryClientFactory() {
  //    return MeetingRegistryClientFactory.builder(configProperties())
  //        .authorizationProvider(meetingRegistryAuthorizationProvider())
  //        .baseUrl(configProperties().getMeetingRegistryServicePublicUrl())
  //        .timeoutPolicy(timeoutPolicy())
  //        .build();
  //  }
  //
  //  @Bean
  //  public AuthorizationProvider meetingRegistryAuthorizationProvider() {
  //    return createDhruvaClientAuthorizationProvider(MeetingRegistryClient.OAUTH_SCOPE_READ);
  //  }

  //  private AuthorizationProvider createDhruvaClientAuthorizationProvider(String... scopes) {
  //    AuthorizationProvider authProvider =
  //        BearerAuthorizationProvider.builder()
  //            .commonIdentityClientFactory(commonIdentityClientFactory())
  //            .orgId(OrgId.fromString(getDhruvaOrgId()))
  //            .userId(getDhruvaMachineAccountUser())
  //            .password(getDhruvaMachineAccountPassword())
  //            .scope(Joiner.on(" ").join(scopes))
  //            .clientId(getDhruvaClientId())
  //            .clientSecret(getDhruvaClientSecret())
  //            .build();
  //
  //    try {
  //      String auth = authProvider.getAuthorization();
  //      if (auth != null && auth.length() < 512) {
  //        log.warn(
  //            "Check that machine account is using a self-contained token, length = {}, scopes =
  // {}",
  //            auth.length(),
  //            scopes);
  //      }
  //    } catch (Exception ignore) {
  //      log.info("Unable to get machine account authorization, scopes = {}", scopes);
  //    }
  //
  //    return authProvider;
  //  }

  private String getDhruvaClientSecret() {
    return "506bdb44b82cca6a9115c3aad3a83a477c3558e352a347a2d91153afb7e302e6";
  }

  private String getDhruvaClientId() {
    return "C9e6d83d9a4fde244fd84fe30d1df661c0b58e10341826234aee7a8a57f813395";
  }

  private String getDhruvaMachineAccountPassword() {
    return env.getProperty("MachineAccountCredential");
    // return "DFHO.rcsv.35.CTDU.dijm.36.CDYZ.tdej.0479";
  }

  private String getDhruvaMachineAccountUser() {
    return "CP-PROD-ACCOUNT-5";
  }

  private String getDhruvaOrgId() {
    return "6078fba4-49d9-4291-9f7b-80116aab6974";
  }
}
