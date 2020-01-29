package com.cisco.dhruva.DsLibs.DsUtil;

import com.cisco.dhruva.DsLibs.DsSecurity.DsCert.SubjectAltName;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.SSLSession;
import org.apache.logging.log4j.Level;

public class DsTlsUtil {

  private static final String TLS_SESSION_KEY_PEER_TRUSTED = "peerTrusted";

  private static final String[] TLS_TRUSTED_DOMAINS = {"sip.webex.com"};
  private static boolean isTestMode = false;
  private static boolean testTrustStatus = false;

  private static final String TRUSTED_DOMAINS =
      DsConfigManager.getProperty(
          DsConfigManager.TRUSTED_DOMAINS, DsConfigManager.TRUSTED_DOMAINS_DEFAULT);

  /*
     This method verifies the certificate chain using the trust manager configured for the network.
     If successfull, the peer is marked trusted in SSL session, otherwise untrusted.
  */
  public static void setPeerVerificationStatus(
      SSLSession sslSession, DsSSLBindingInfo bindingInfo, boolean isPeerClient) {

    DsSSLContext context = bindingInfo.getNetwork().getSSLContext();

    // if cert chain is already verified during handshake
    if ((isPeerClient && context.getNeedClientAuth())
        || (!isPeerClient && (context.getTrustOverrideManager() == null))) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "DsTlsUtil.setPeerVerificationStatus()["
              + sslSession.getPeerHost()
              + "]: setting clientTrusted to true");
      sslSession.putValue(TLS_SESSION_KEY_PEER_TRUSTED, true);
      return;
    }

    try {
      X509Certificate[] x509Certs = (X509Certificate[]) sslSession.getPeerCertificates();
      if (x509Certs == null) {
        throw new Exception("No client cert chain to verify");
      }

      PublicKey key = x509Certs[0].getPublicKey();
      String keyAlgorithm = key.getAlgorithm();
      String authType;
      if (keyAlgorithm.equals("RSA")) {
        authType = "RSA";
      } else if (keyAlgorithm.equals("DSA")) {
        authType = "DSA";
      } else if (keyAlgorithm.equals("EC")) {
        authType = "EC";
      } else {
        authType = "UNKNOWN";
      }

      context.getTrustManagerImpl().trust(x509Certs, isPeerClient, authType);
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "DsTlsUtil.setPeerVerificationStatus()["
              + sslSession.getPeerHost()
              + "]: setting clientTrusted to true");
      sslSession.putValue(TLS_SESSION_KEY_PEER_TRUSTED, true);
    } catch (Exception e) {
      DsLog4j.connectionCat.log(
          Level.INFO,
          "DsTlsUtil.setPeerVerificationStatus()["
              + sslSession.getPeerHost()
              + "]: setting clientTrusted to false as exception during client verification. "
              + e);
      sslSession.putValue(TLS_SESSION_KEY_PEER_TRUSTED, false);
    }
  }

  /*
     This method returns the trust staus from the TLS session
  */
  public static boolean isPeerTrusted(SSLSession sslSession) {
    boolean isPeerTrusted = false;

    if (isTestMode) {
      return testTrustStatus;
    }

    try {
      isPeerTrusted = (boolean) sslSession.getValue(TLS_SESSION_KEY_PEER_TRUSTED);
    } catch (Exception e) {
      DsLog4j.connectionCat.log(
          Level.INFO,
          "DsTlsUtil.isPeerTrusted()[" + sslSession.getPeerHost() + "]: exception: " + e);
    }

    DsLog4j.connectionCat.log(
        Level.DEBUG,
        "DsTlsUtil.isPeerTrusted()[" + sslSession.getPeerHost() + "]: returning " + isPeerTrusted);
    return isPeerTrusted;
  }

  /*
  This method checks if the configured array of domains is present in incoming TLS connection
  SAN entries
  */

  public static boolean isPeerDomainTrusted(DsSSLBindingInfo bindingInfo) {
    List<SubjectAltName> sanList = bindingInfo.getPeerSubjectAltName();
    String peerCommonName = bindingInfo.getPeerCommonName();
    boolean isDomainTrusted = false;
    if (sanList != null && !sanList.isEmpty()) {
      List<String> sanNames = new ArrayList<>();
      for (int i = 0; i < sanList.size(); i++) {
        SubjectAltName san = sanList.get(i);
        sanNames.add(san.getSanName());
      }

      String[] trustedDomains = TRUSTED_DOMAINS.split(",");
      List trustedList = Arrays.asList(trustedDomains);
      for (String s : trustedDomains) {
        if (sanNames.contains(s)) {
          isDomainTrusted = true;
          break;
        }
      }

      if (trustedList.contains(peerCommonName)) {
        isDomainTrusted = true;
      }
    }
    return isDomainTrusted;
  }

  public static void enableTestMode(boolean isTestMode, boolean testTrustStatus) {
    DsTlsUtil.isTestMode = isTestMode;
    DsTlsUtil.testTrustStatus = testTrustStatus;
  }
}
