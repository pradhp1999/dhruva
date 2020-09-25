package com.cisco.dhruva.config.sip;

import com.cisco.dhruva.sip.bean.SIPListenPoint;
import com.cisco.dhruva.sip.bean.SIPProxy;
import com.cisco.dhruva.transport.TLSAuthenticationType;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.JsonUtilFactory;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DhruvaSIPConfigProperties {

  public static final String SIP_LISTEN_POINTS = "sipListenPoints";

  public static final String SIP_PROXY = "sipProxy";

  public static final Transport DEFAULT_TRANSPORT = Transport.UDP;

  // MeetPass TODO
  // Env is not read properly, hence setting it here to true
  public static final boolean DEFAULT_RECORD_ROUTE_ENABLED = true;

  public static final boolean DEFAULT_PROXY_ERROR_AGGREGATOR_ENABLED = false;

  public static final boolean DEFAULT_PROXY_CREATE_DNSSERVERGROUP_ENABLED = false;

  public static final boolean DEFAULT_PROXY_PROCESS_ROUTE_HEADER_ENABLED = false;

  private static final String USE_REDIS_AS_CACHE = "useRedis";

  private Logger logger = DhruvaLoggerFactory.getLogger(DhruvaSIPConfigProperties.class);

  public static final TLSAuthenticationType DEFAULT_TRANSPORT_AUTH = TLSAuthenticationType.MTLS;

  private static final String SIP_CERTIFICATE = "sipCertificate";

  private static final String SIP_PRIVATE_KEY = "sipPrivateKey";

  private static final String UDP_EVENTLOOP_THREAD_COUNT = "dhruva.network.udpEventloopThreadCount";

  private static final Integer DEFAULT_UDP_EVENTLOOP_THREAD_COUNT = 1;

  private static final String TLS_EVENTLOOP_THREAD_COUNT = "dhruva.network.tlsEventloopThreadCount";

  private static final Integer DEFAULT_TLS_EVENTLOOP_THREAD_COUNT = 20;

  private static final String CONNECTION_CACHE_CONNECTION_IDLE_TIMEOUT_SECONDS =
      "dhruva.network.connectionCache.connectionIdleTimeout";

  private static final Integer DEFAULT_CONNECTION_CACHE_CONNECTION_IDLE_TIMEOUT_MINUTES = 14400;

  private static final String TLS_CIPHERS = "dhruva.sipTlsCipherSuites";


  public static int DEFAULT_PORT_UDP = 5060;

  @Autowired private Environment env;

  public List<SIPListenPoint> getListeningPoints() {

    String configuredListeningPoints = env.getProperty(SIP_LISTEN_POINTS);

    List<SIPListenPoint> listenPoints;

    if (configuredListeningPoints != null) {
      try {
        listenPoints =
            Arrays.asList(
                JsonUtilFactory.getInstance(JsonUtilFactory.JsonUtilType.LOCAL)
                    .toObject(configuredListeningPoints, SIPListenPoint[].class));
      } catch (Exception e) {
        // TODO shoould we generate an Alarm
        logger.error(
            "Error converting JSON ListenPoint configuration provided in the environment , default listenpoint will be choosen ",
            e);
        listenPoints = getDefaultListenPoints();
      }

    } else {
      listenPoints = getDefaultListenPoints();
    }

    logger.info("Listen points from the {} configuration {}", SIP_LISTEN_POINTS, listenPoints);

    return listenPoints;
  }

  private List<SIPListenPoint> getDefaultListenPoints() {

    List<SIPListenPoint> listenPoints = new ArrayList<>();

    SIPListenPoint udpListenPoint = new SIPListenPoint.SIPListenPointBuilder().build();

    listenPoints.add(udpListenPoint);

    return listenPoints;
  }

  private SIPProxy getSIPProxy() {

    String configuredSipProxy = env.getProperty(SIP_PROXY);

    SIPProxy proxy;

    if (configuredSipProxy != null) {
      try {
        proxy =
            JsonUtilFactory.getInstance(JsonUtilFactory.JsonUtilType.LOCAL)
                .toObject(configuredSipProxy, SIPProxy.class);
      } catch (Exception e) {
        logger.error(
            "Error converting JSON sipProxy configuration provided in the environment , default sipProxy will be choosen ",
            e);
        proxy = getDefaultSIPProxy();
      }

    } else {
      proxy = getDefaultSIPProxy();
    }

    logger.info("sip proxy config from the {} configuration {}", SIP_PROXY, proxy);

    return proxy;
  }

  private SIPProxy getDefaultSIPProxy() {
    return new SIPProxy.SIPProxyBuilder().build();
  }

  public boolean useRedisAsCache() {
    return env.getProperty(USE_REDIS_AS_CACHE, Boolean.class, true);
  }

  public int getDhruvaDnsCacheMaxSize() {
    int defaultCacheSize = 1_000;
    int cacheSize = env.getProperty("DhruvaDnsCacheMaxSize", Integer.class, defaultCacheSize);
    return cacheSize > 0 ? cacheSize : defaultCacheSize;
  }

  public long dnsCacheRetentionTimeMillis() {
    long defaultTime = 0L;
    long retTime = env.getProperty("DhruvaDnsRetentionTimeMillis", Long.class, defaultTime);
    return retTime > 0L ? retTime : defaultTime;
  }

  public String getSipCertificate() {
    return env.getProperty(SIP_CERTIFICATE);
  }

  public String getSipPrivateKey() {
    return env.getProperty(SIP_PRIVATE_KEY);
  }

  public int getUdpEventPoolThreadCount() {
    return env.getProperty(
        UDP_EVENTLOOP_THREAD_COUNT, Integer.class, DEFAULT_UDP_EVENTLOOP_THREAD_COUNT);
  }

  public int getTlsEventPoolThreadCount() {
    return env.getProperty(
        TLS_EVENTLOOP_THREAD_COUNT, Integer.class, DEFAULT_TLS_EVENTLOOP_THREAD_COUNT);
  }

  public int getConnectionCacheConnectionIdleTimeout() {
    return env.getProperty(
        CONNECTION_CACHE_CONNECTION_IDLE_TIMEOUT_SECONDS,
        Integer.class,
        DEFAULT_CONNECTION_CACHE_CONNECTION_IDLE_TIMEOUT_MINUTES);
  }

  public List<String> getCiphers() {

    String ciphers = env.getProperty(TLS_CIPHERS, String.class);
    if (ciphers == null || ciphers.isEmpty()) {
      return CipherSuites.allowedCiphers;
    } else {
      return Collections.unmodifiableList(
          CipherSuites.getAllowedCiphers(Arrays.asList(ciphers.split(","))));
    }

  }
}
