package com.cisco.dhruva.transport;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedExceptionAction;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.PKIXRevocationChecker.Option;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.EnumSet;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.jetbrains.annotations.NotNull;

public class DhruvaTrustManager implements X509TrustManager {

  private static Logger logger = DhruvaLoggerFactory.getLogger(DhruvaTrustManager.class);
  private static String trustStoreFile;
  private static String trustStoreType;
  private static String trustStorePassword;
  private static String javaHome;
  private static boolean softFailEnabled;
  private static boolean enableOcsp;
  private final X509TrustManager trustManager;

  static {
    initTransportProperties();
  }

  public static DhruvaTrustManager getSystemTrustManager() throws Exception {
    return createSystemTrustManager();
  }

  private static void initTransportProperties() {
    System.setProperty(
        "com.sun.security.ocsp.timeout", String.valueOf(DsNetwork.getOcspResponseTimeoutSeconds()));
    trustStoreFile = DsNetwork.getTrustStoreFilePath();
    trustStoreType = DsNetwork.getTrustStoreType();
    trustStorePassword = DsNetwork.getTrustStorePassword();
    softFailEnabled = DsNetwork.isTlsCertRevocationSoftFailEnabled();
    enableOcsp = DsNetwork.isTlsOcspEnabled();
    javaHome = System.getProperty("java.home");
  }

  public DhruvaTrustManager(X509TrustManager trustManager) {
    this.trustManager = trustManager;
  }

  private static DhruvaTrustManager createSystemTrustManager() throws Exception {

    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    KeyStore ts = getCacertsKeyStore();

    CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");
    PKIXRevocationChecker rc = (PKIXRevocationChecker) cpb.getRevocationChecker();
    rc.setOptions(getRevocationOptions());

    PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(ts, new X509CertSelector());
    pkixParams.addCertPathChecker(rc);
    tmf.init(new CertPathTrustManagerParameters(pkixParams));
    TrustManager[] tms = tmf.getTrustManagers();

    for (TrustManager tm : tms) {
      if (tm instanceof X509TrustManager) {
        logger.info(
            "Initializing trust manager with {} certs as trust anchors",
            ((X509TrustManager) tm).getAcceptedIssuers().length);
        return new DhruvaTrustManager((X509TrustManager) tm);
      }
    }

    throw new RuntimeException("Unable to find system trust manager");
  }

  @NotNull
  private static EnumSet<Option> getRevocationOptions() {

    EnumSet<Option> options = EnumSet.of(Option.ONLY_END_ENTITY);

    if (softFailEnabled) {
      options.add(Option.SOFT_FAIL);
    }
    if (!enableOcsp) {
      options.add(Option.PREFER_CRLS);
    }
    return options;
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
    if (DsNetwork.getIsAcceptedIssuersEnabled()) {
      return trustManager.getAcceptedIssuers();
    } else {
      return new X509Certificate[0];
    }
  }

  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN"})
  private static KeyStore getCacertsKeyStore() throws Exception {
    File storeFile;
    FileInputStream fis = null;
    final String sep = File.separator;
    KeyStore ks;
    try {
      if (trustStoreFile != null) {
        storeFile = new File(trustStoreFile);
        fis = getFileInputStream(storeFile);
      } else {
        storeFile = new File(javaHome + sep + "lib" + sep + "security" + sep + "jssecacerts");
        if ((fis = getFileInputStream(storeFile)) == null) {
          storeFile = new File(javaHome + sep + "lib" + sep + "security" + sep + "cacerts");
          fis = getFileInputStream(storeFile);
        }
      }

      if (fis != null) {
        trustStoreFile = storeFile.getPath();
      } else {
        trustStoreFile = "No File Available, using empty keystore.";
      }

      ks = KeyStore.getInstance(trustStoreType);

      char[] trustStorePass = null;
      if (trustStorePassword.length() != 0) {
        trustStorePass = trustStorePassword.toCharArray();
      }

      // if trustStoreFile is NONE, fis will be null
      ks.load(fis, trustStorePass);
    } finally {
      if (fis != null) {
        fis.close();
      }
    }

    return ks;
  }

  private static FileInputStream getFileInputStream(final File file) throws Exception {
    return AccessController.doPrivileged(
        (PrivilegedExceptionAction<FileInputStream>)
            () -> {
              try {
                if (file.exists()) {
                  return new FileInputStream(file);
                } else {
                  return null;
                }
              } catch (FileNotFoundException e) {
                return null;
              }
            });
  }
}
