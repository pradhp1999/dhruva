package com.cisco.dhruva.transport;

import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class DhruvaTrustManager implements X509TrustManager {

  private static Logger logger = DhruvaLoggerFactory.getLogger(DhruvaTrustManager.class);
  private final X509TrustManager trustManager;

  public static DhruvaTrustManager getSystemTrustManager() throws Exception {
    return createSystemTrustManager();
  }

  public DhruvaTrustManager(X509TrustManager trustManager) {
    this.trustManager = trustManager;
  }

  private static DhruvaTrustManager createSystemTrustManager() throws Exception {
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init((KeyStore) null);

    TrustManager tms[] = tmf.getTrustManagers();

    for (int i = 0; i < tms.length; i++) {
      if (tms[i] instanceof X509TrustManager) {
        logger.info(
            "Initializing trust manager with {} certs as trust anchors",
            ((X509TrustManager) tms[i]).getAcceptedIssuers().length);
        return new DhruvaTrustManager((X509TrustManager) tms[i]);
      }
    }

    throw new RuntimeException("Unable to find system trust manager");
  }

  @Override
  public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
      throws CertificateException {
    trustManager.checkClientTrusted(x509Certificates, s);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
      throws CertificateException {
    trustManager.checkServerTrusted(x509Certificates, s);
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return trustManager.getAcceptedIssuers();
  }
}
