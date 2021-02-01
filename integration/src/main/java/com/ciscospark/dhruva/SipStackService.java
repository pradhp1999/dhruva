package com.ciscospark.dhruva;

import com.cisco.wx2.certs.common.util.KeyStoreHolder;
import com.cisco.wx2.certs.common.util.X509CertificateGenerator;
import com.ciscospark.dhruva.util.Token;
import com.google.common.io.Resources;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.cafesip.sipunit.SipStack;
import org.springframework.stereotype.Component;

@Component
public class SipStackService {
  private static SipStack sipStack;
  private Properties properties;
  private String value = "password";
  private DhruvaTestProperties testPro = new DhruvaTestProperties();

  private int testUdpPort = testPro.getTestUdpPort();
  private int testTlsPort = testPro.getTestTlsPort();
  private String testHost = testPro.getTestAddress();

  public SipStackService() {}

  @PostConstruct
  public void init() throws Exception {

    createKeyStoreHolder();
    properties = new Properties();
    properties.setProperty("javax.sip.STACK_NAME", "TestDhruva");
    properties.setProperty("javax.sip.IP_ADDRESS", testHost);
    properties.setProperty("javax.net.ssl.keyStore", KeyStoreHolder.KEY_STORE_JKS);
    properties.setProperty("javax.net.ssl.keyStorePassword", value);
    properties.setProperty("javax.net.ssl.trustStore", KeyStoreHolder.TRUST_STORE_JKS);
    properties.setProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS", "TLSv1.2");
  }

  public SipStack getSipStackUdp() throws Exception {
    if (sipStack != null) {
      sipStack.dispose();
    }
    properties.setProperty("javax.sip.IP_ADDRESS", testHost);
    sipStack = new SipStack(Token.UDP, testUdpPort, properties);
    return sipStack;
  }

  public SipStack getSipStackTls() throws Exception {
    if (sipStack != null) {
      sipStack.dispose();
    }
    properties.setProperty("javax.sip.IP_ADDRESS", testHost);
    sipStack = new SipStack(Token.TLS, testTlsPort, properties);
    return sipStack;
  }

  // generates certs and creates key store for integration test , trustore is chosen from resource
  // directory
  private KeyStoreHolder createKeyStoreHolder() throws Exception {

    String subject = "CN=dhruva-int-cert";
    X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
    KeyPair clientKeys = X509CertificateGenerator.generateKeyPair();

    // start yesterday
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -1);
    Date startDate = calendar.getTime();

    // expire in 3 days
    calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, 3);
    Date endDate = calendar.getTime();

    X509Certificate[] chain =
            certificateGenerator.generateCertificateChain(
                    subject, null, clientKeys, startDate, endDate);

    KeyStoreHolder keyStoreHolder = new KeyStoreHolder("password");
    String trustStore =
            Resources.toString(Resources.getResource("trustStore.pem"), StandardCharsets.UTF_8);
    keyStoreHolder.createTrustStore(trustStore);
    keyStoreHolder.setKeyEntry(chain, clientKeys.getPrivate());
    keyStoreHolder.persistStores();

    return keyStoreHolder;
  }
}