// Copyright (c) 2005-2006, 2014 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.slf4j.event.Level;

/**
 * This class serves as the wrapper trust manager to decide for the trusted clients and servers. It
 * wraps the default trust managers, that are initiated while loading the different trust
 * certificates from the trust store, the user defined trust manager, which maintains a list of
 * trusted clients and servers and user registered trust interface, which tells whether the peer is
 * trusted, not trusted or should be trusted always. If all, the default trust managers, the user
 * defined trust manager and user registered trust interface trust the specified client or server,
 * then only this trust manager trust the specified client or server. Otherwise the client or server
 * is treated as untrusted.
 *
 * @since SIP User Agent Java v5.0
 */
public class DsTrustManagerImpl implements X509TrustManager {

  private boolean certServiceTrustManagerEnabled = false;
  private boolean softFailEnabled = false;
  private boolean disableAcceptedIssuers = true;

  /**
   * Construct the Trust Manager with the specified trust store <code>trustStore</code>, user
   * defined trust manager <code>sslManager</code> and no callback trust interface <code>
   * trustInterface</code>. The trust store contains the trusted root certificates. In this case,
   * the specified keystore <code>rustStore</code> is assumed to be generated using default <code>
   * DsSSLContext.KA_SUNX509</code> key Algorithm, and SSL/TLS provider is assumed to be default
   * <code>DsSSLContext.SUNJSSE</code> SSL/TLS provider is used.
   *
   * @param trustStore the keystore containing the trusted certificates that need to be retrieved as
   *     trusted root certificates.
   * @param sslManager the user defined trust manager that should be queried before deciding the
   *     trust relation of the client or server.
   * @throws Exception
   */
  public DsTrustManagerImpl(KeyStore trustStore, DsSSLTrustManager sslManager) throws Exception {
    this(trustStore, DsSSLContext.KA_SUNX509, DsSSLContext.SUNJSSE, sslManager, null, false);
  }

  /**
   * Construct the Trust Manager with the specified trust store <code>trustStore</code>, user
   * defined trust manager <code>sslManager</code> and user defined trust interface <code>
   * trustInterface</code>. The trust store contains the trusted root certificates. In this case,
   * the specified keystore <code>rustStore</code> is assumed to be generated using default <code>
   * DsSSLContext.KA_SUNX509</code> key Algorithm, and SSL/TLS provider is assumed to be default
   * <code>DsSSLContext.SUNJSSE</code> SSL/TLS provider is used.
   *
   * @param trustStore the keystore containing the trusted certificates that need to be retrieved as
   *     trusted root certificates.
   * @param sslManager the user defined trust manager that should be queried before deciding the
   *     trust relation of the client or server.
   * @param trustInterface the trust interface which will be called back for dynamically deciding
   *     the trust relationship between the peers.
   * @throws Exception
   */
  public DsTrustManagerImpl(
      KeyStore trustStore, DsSSLTrustManager sslManager, DsTrustInterface trustInterface)
      throws Exception {
    this(
        trustStore,
        DsSSLContext.KA_SUNX509,
        DsSSLContext.SUNJSSE,
        sslManager,
        trustInterface,
        false);
  }

  /**
   * Construct the Trust Manager with the specified trust store <code>trustStore</code>, user
   * defined trust manager <code>sslManager</code> and no callback trust interface <code>
   * trustInterface</code>. The trust store contains the trusted root certificates. If the specified
   * key algorithm <code>keyAlgorithm</code> is null, then the default <code>DsSSLContext.KA_SUNX509
   * </code> key Algorithm is used. If the specified SSL/TLS provider <code>tlsProvider</code> is
   * null, then the default <code>DsSSLContext.SUNJSSE</code> SSL/TLS provider is used.
   *
   * @param trustStore the keystore containing the trusted certificates that need to be retrieved as
   *     trusted root certificates.
   * @param keyAlgorithm the key generating algorithm used in the specified keystore <code>
   *     trustStore</code>
   * @param tlsProvider the provider name for the TLS/SSL implementation
   * @param sslManager the user defined trust manager that should be queried before deciding the
   *     trust relation of the client or server.
   * @throws Exception
   */
  public DsTrustManagerImpl(
      KeyStore trustStore, String keyAlgorithm, String tlsProvider, DsSSLTrustManager sslManager)
      throws Exception {
    this(trustStore, keyAlgorithm, tlsProvider, sslManager, null, false);
  }

  /**
   * Construct the Trust Manager with the specified trust store <code>trustStore</code>, user
   * defined trust manager <code>sslManager</code> and user defined trust interface <code>
   * trustInterface</code>. The trust store contains the trusted root certificates. If the specified
   * key algorithm <code>keyAlgorithm</code> is null, then the default <code>DsSSLContext.KA_SUNX509
   * </code> key Algorithm is used. If the specified SSL/TLS provider <code>tlsProvider</code> is
   * null, then the default <code>DsSSLContext.SUNJSSE</code> SSL/TLS provider is used.
   *
   * @param trustStore the keystore containing the trusted certificates that need to be retrieved as
   *     trusted root certificates.
   * @param keyAlgorithm the key generating algorithm used in the specified keystore <code>
   *     trustStore</code>
   * @param tlsProvider the provider name for the TLS/SSL implementation
   * @param sslManager the user defined trust manager that should be queried before deciding the
   *     trust relation of the client or server.
   * @param trustInterface the trust interface which will be called back for dynamically deciding
   *     the trust relationship between the peers.
   * @throws Exception
   */
  public DsTrustManagerImpl(
      KeyStore trustStore,
      String keyAlgorithm,
      String tlsProvider,
      DsSSLTrustManager sslManager,
      DsTrustInterface trustInterface,
      boolean certServiceTrustManagerEnabled)
      throws Exception {
    this.trustStore = trustStore;
    this.keyAlgorithm = (null == keyAlgorithm) ? DsSSLContext.KA_SUNX509 : keyAlgorithm;
    this.tlsProvider = tlsProvider;

    this.sslManager = sslManager;
    this.trustInterface = trustInterface;
    this.certServiceTrustManagerEnabled = certServiceTrustManagerEnabled;
    DsLog4j.authCat.info(
        new StringBuffer("DsTrustManagerImpl settting trustInterface \n")
            .append(trustInterface)
            .toString());
    init();
  }

  /**
   * Given the partial or complete certificate chain provided by the peer, build a certificate path
   * to a trusted root and return if it can be validated and is trusted for client SSL
   * authentication based on the authentication type. The authentication type is determined by the
   * actual certificate used. For instance, if RSAPublicKey is used, the authType should be "RSA".
   * Checking is case-sensitive.
   *
   * @param chain the peer certificate chain.
   * @param authType the authentication type based on the client certificate .
   * @throws IllegalArgumentException if null or zero-length chain is passed in for the chain
   *     parameter or if null or zero-length string is passed in for the authType parameter.
   * @throws CertificateException if the certificate chain is not trusted by this TrustManager.
   */
  public void checkClientTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    DsLog4j.authCat.log(
        Level.INFO,
        new StringBuffer(
                "checkClientTrusted checking sslTrustOverrideManager client interface this ")
            .append(this)
            .toString());

    DsLog4j.authCat.log(
        Level.INFO,
        new StringBuffer(
                "checkClientTrusted checking sslTrustOverrideManager client interface  trustinterface")
            .append(sslTrustOverrideManager)
            .toString());

    if (null != sslTrustOverrideManager) {
      boolean trustOverride = false;
      trustOverride = sslTrustOverrideManager.isClientTrusted(chain);
      DsLog4j.authCat.log(
          Level.INFO,
          new StringBuffer("checking sslTrustOverrideManager client interface\n")
              .append(trustOverride)
              .toString());

      if (trustOverride == true) return;
    }

    // if fail is ok, what is the use of doing a validation
    if (softFailEnabled) {
      return;
    }

    boolean trust = trust(chain, true, authType);

    if (sslManager != null) {
      trust = (trust && sslManager.isClientTrusted(chain));
    }
    if (!trust) throw new CertificateException("Client Not Trusted");
  }

  /**
   * Given the partial or complete certificate chain provided by the peer, build a certificate path
   * to a trusted root and return if it can be validated and is trusted for server SSL
   * authentication based on the authentication type. The authentication type is the key exchange
   * algorithm portion of the cipher suites represented as a String, such as "RSA", "DHE_DSS". Note:
   * for some exportable cipher suites, the key exchange algorithm is determined at run time during
   * the handshake. For instance, for TLS_RSA_EXPORT_WITH_RC4_40_MD5, the authType should be
   * RSA_EXPORT when an ephemeral RSA key is used for the key exchange, and RSA when the key from
   * the server certificate is used. Checking is case-sensitive.
   *
   * @param chain the peer certificate chain
   * @param authType the authentication type based on the server certificate.
   * @throws IllegalArgumentException if null or zero-length chain is passed in for the chain
   *     parameter or if null or zero-length string is passed in for the authType parameter.
   * @throws CertificateException if the certificate chain is not trusted by this TrustManager.
   */
  public void checkServerTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    boolean result = false;
    DsLog4j.authCat.log(
        Level.INFO,
        new StringBuffer(
                "checkServerTrusted checking sslTrustOverrideManager server interface auth type \n")
            .append(this)
            .toString());

    DsLog4j.authCat.log(
        Level.INFO,
        new StringBuffer(
                "checkServerTrusted checking sslTrustOverrideManager server interface auth type \n")
            .append(trustInterface)
            .toString());

    if (null != sslTrustOverrideManager) {
      boolean trustOverride = false;
      trustOverride = sslTrustOverrideManager.isServerTrusted(chain);
      DsLog4j.authCat.log(
          Level.INFO,
          new StringBuffer("checking sslTrustOverrideManager server interface\n")
              .append(trustOverride)
              .toString());

      if (trustOverride == true) return;
    }

    boolean trust = trust(chain, false, authType);

    if (sslManager != null) {
      trust = (trust && sslManager.isServerTrusted(chain));
    }
    if (!trust) throw new CertificateException("Server Not Trusted");
  }

  /**
   * Return an array of certificate authority certificates which are trusted for authenticating
   * peers.
   *
   * @return the acceptable CA issuer certificates
   */
  public X509Certificate[] getAcceptedIssuers() {
    if (disableAcceptedIssuers) {
      return new X509Certificate[0];
    } else {
      return certificates;
    }
  }

  /**
   * Registers the trust interface which will be called back for dynamically deciding the trust
   * relationship between the peers.
   *
   * @param trustInterface the trust interface which will be called back for dynamically deciding
   *     the trust relationship between the peers.
   */
  public void setTrustInterface(DsTrustInterface trustInterface) {
    this.trustInterface = trustInterface;
  }

  /**
   * Returns the trust interface which is called back for dynamically deciding the trust
   * relationship between the peers.
   *
   * @return the trust interface which is called back for dynamically deciding the trust
   *     relationship between the peers.
   */
  public DsTrustInterface getTrustInterface() {
    return trustInterface;
  }

  /**
   * Registers the trusted peer manager interface which maintains a list of trusted peers and will
   * be called back for retrieving the trust relationship between the peers.
   *
   * @param sslManager the trusted peer manager interface which maintains a list of trusted peers
   *     and will be called back for retrieving the trust relationship between the peers.
   */
  public void setTrustedPeerManager(DsSSLTrustManager sslManager) {
    this.sslManager = sslManager;
  }

  /**
   * Returns the trusted peer manager interface which maintains a list of trusted peers and is
   * called back for retrieving the trust relationship between the peers.
   *
   * @return the trusted peer manager interface which maintains a list of trusted peers and is
   *     called back for retrieving the trust relationship between the peers.
   */
  public DsSSLTrustManager getTrustedPeerManager() {
    return sslManager;
  }

  /**
   * Registers the trust override manager which overrides client and server certificate validation
   *
   * @param sslTrustOverrideManager the trusted override manager which overrides client and server
   *     certificate validation
   */
  public void setTrustOverrideManager(DsSSLTrustManager sslTrustOverrideManager) {
    this.sslTrustOverrideManager = sslTrustOverrideManager;
  }

  /**
   * Returns the trusted peer manager interface which maintains a list of trusted peers and is
   * called back for retrieving the trust relationship between the peers.
   *
   * @return the trusted peer manager interface which maintains a list of trusted peers and is
   *     called back for retrieving the trust relationship between the peers.
   */
  public DsSSLTrustManager getTrustOverrideManager() {
    return sslTrustOverrideManager;
  }

  /**
   * Loads the trusted certificates from the input file specified by the <code>trustStoreFile</code>
   * file path. If a password is specified and is not null, it is used to check the integrity of the
   * trust certificates. Otherwise, the integrity of the trust certificates is not checked. If
   * trusted certificates have already been loaded, it is reinitialized and loaded again from the
   * input file specified by the file path.
   *
   * @param trustStore the keystore containing the trusted certificates that need to be retrieved.
   * @param password the (optional) password used to retrieve the trusted certificates contained in
   *     the keystore.
   * @throws Exception
   */
  public void loadTrustStore(KeyStore trustStore, char[] password) throws Exception {
    this.trustStore = trustStore;
    reInit();
  }

  /**
   * Loads the trusted certificates from the input file specified by the <code>trustStoreFile</code>
   * file path. If a password is specified and is not null, it is used to check the integrity of the
   * trust certificates. Otherwise, the integrity of the trust certificates is not checked. If
   * trusted certificates have already been loaded, it is reinitialized and loaded again from the
   * input file specified by the file path.
   *
   * @param trustStoreFile the file path from which the trusted certificates need to be loaded.
   * @param password the (optional) password used to check the integrity of trusted certificates
   *     contained in the file.
   * @throws Exception
   */
  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN"})
  public void loadTrustStore(String trustStoreFile, char[] password) throws Exception {
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(trustStoreFile);
      loadTrustStore(stream, password);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException ioe) {
          // ignore
        }
      }
    }
  }

  /**
   * Loads the trusted certificates from the input file specified by the <code>trustStoreFile</code>
   * file path. If a password is specified and is not null, it is used to check the integrity of the
   * trust certificates. Otherwise, the integrity of the trust certificates is not checked. If
   * trusted certificates have already been loaded, it is reinitialized and loaded again from the
   * input file specified by the file path.
   *
   * @param stream the input stream from which the trusted certificates need to be loaded.
   * @param password the (optional) password used to check the integrity of trusted certificates
   *     contained in the input stream.
   * @throws Exception
   */
  public void loadTrustStore(InputStream stream, char[] password) throws Exception {
    if (trustStore == null) {
      trustStore =
          (null == tlsProvider)
              ? KeyStore.getInstance(keyAlgorithm)
              : KeyStore.getInstance(keyAlgorithm, tlsProvider);
    }
    trustStore.load(stream, password);
    reInit();
  }

  /**
   * Stores the keystore, containing various trusted certificates, to the given output stream, and
   * protects its integrity with the given password.
   *
   * @param stream the output stream to which the keystore, containing various trusted certificates,
   *     is written.
   * @param password the password to generate the keystore integrity check
   * @throws KeyStoreException if the keystore has not been initialized or loaded
   * @throws IOException if there was an I/O problem with data
   * @throws NoSuchAlgorithmException if the appropriate data integrity algorithm could not be found
   * @throws CertificateException if any of the certificates included in the keystore data could not
   *     be stored
   */
  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_OUT"})
  public void storeTrustStore(OutputStream stream, char[] password)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    if (null != trustStore) {
      trustStore.store(stream, password);
    }
  }

  /**
   * Stores the keystore, containing various trusted certificates, to the given output stream, and
   * protects its integrity with the given password.
   *
   * @param trustStoreFile the name of the output file to which the keystore, containing various
   *     trusted certificates, is written.
   * @param password the password to generate the keystore integrity check
   * @throws KeyStoreException if the keystore has not been initialized or loaded
   * @throws IOException if there was an I/O problem with data
   * @throws NoSuchAlgorithmException if the appropriate data integrity algorithm could not be found
   * @throws CertificateException if any of the certificates included in the keystore data could not
   *     be stored
   */
  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_OUT"})
  public void storeTrustStore(String trustStoreFile, char[] password)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    FileOutputStream stream = null;

    try {
      stream = new FileOutputStream(trustStoreFile);
      storeTrustStore(stream, password);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException ioe) {
          // ignore
        }
      }
    }
  }

  /**
   * Adds the specified certificate as a trusted certificate and implies that the corresponding
   * subject is now trusted for future communications.
   *
   * @param certificate the new trusted certificate.
   */
  public void addTrustedCertificate(X509Certificate certificate) {
    if (null == certificate) {
      return;
    }
    try {
      if (DsLog4j.authCat.isEnabled(Level.INFO))
        DsLog4j.authCat.log(
            Level.INFO,
            new StringBuffer("Adding trusted certificate:\n").append(certificate).toString());
      synchronized (this) {
        trustStore.setCertificateEntry(generateAlias(trustStore), certificate);
        reInit();
      }
      if (DsLog4j.authCat.isEnabled(Level.INFO))
        DsLog4j.authCat.log(
            Level.INFO,
            new StringBuffer("Added trusted certificate:\n").append(certificate).toString());
    } catch (Exception exc) {
      if (DsLog4j.authCat.isEnabled(Level.WARN))
        DsLog4j.authCat.log(
            Level.WARN,
            new StringBuffer("Exception while adding trusted certificate:\n")
                .append(certificate)
                .append("\n")
                .append(exc.getMessage())
                .toString());
    }
  }

  /**
   * Adds the specified certificate chain as trusted certificates and implies that the subject of
   * the top certificate is now trusted for future communications.
   *
   * @param chain the new trusted certificate chain.
   */
  public void addTrustedCertificate(X509Certificate[] chain) {
    if (null == chain || chain.length < 1) {
      return;
    }
    addTrustedCertificate(chain[0]);
  }

  /**
   * Initializes the trust manager factory and the trust managers from the trust store and also
   * initializes the trust certificates list.
   *
   * @throws Exception
   */
  private void init() throws Exception {
    trustManagerFactory =
        (null == tlsProvider)
            ? TrustManagerFactory.getInstance(keyAlgorithm)
            : TrustManagerFactory.getInstance(keyAlgorithm, tlsProvider);
    reInit();
  }

  /**
   * Reinitializes the trust managers and trust certificates list.
   *
   * @throws Exception
   */
  private void reInit() throws Exception {
    trustManagerFactory.init(trustStore);
    if (certServiceTrustManagerEnabled) {
      CertServiceTrustMangerFactory certServiceTrustMangerFactory =
          getCertServiceTrustMangerFactory();
      if (certServiceTrustMangerFactory != null) {
        trustManagers = certServiceTrustMangerFactory.getTrustManagers();
      }
    } else {
      trustManagers = trustManagerFactory.getTrustManagers();
    }
    initCertificates();
  }

  private CertServiceTrustMangerFactory getCertServiceTrustMangerFactory() throws Exception {
    try {
      Class certServiceFactory =
          Class.forName("com.cisco.certservice.DsCertServiceTrustManagerFactory");
      return (CertServiceTrustMangerFactory) certServiceFactory.newInstance();

    } catch (Exception e) {
      DsLog4j.authCat.error(
          "Error loading Cert Service TrustManger , TLS Connections Will fail ", e);
      throw e;
    }
  }

  /** Initializes the trust certificates list from the trust store. */
  private void initCertificates() {
    if (null != trustManagers) {
      ArrayList certList = new ArrayList();
      X509TrustManager manager = null;
      for (int i = 0; i < trustManagers.length; i++) {
        manager = (X509TrustManager) trustManagers[i];
        if (null != manager) {
          X509Certificate[] certs = manager.getAcceptedIssuers();
          for (int j = 0; j < certs.length; j++) {
            certList.add((X509Certificate) certs[j]);
          } // for_
        } // if_
      } // for_
      // cache the Accepted issuers certificates
      certificates = (X509Certificate[]) certList.toArray(new X509Certificate[certList.size()]);
      certList.clear();
      certList = null;
    } // if_
  }

  /**
   * Tells whether the given certificate chain, from a client or server, is trusted or not. It
   * firsts asks the X509 trust managers, initialized from the trustStore. If trusted then returns
   * true, otherwise asks the trust interface, if registered. The trust interface can return any of
   * the the following three options:<br>
   * DsTrustInterface.TRUST_NO - Don't trust this certificate chain<br>
   * DsTrustInterface.TRUST_NOW - Trust this certificate chain, this time<br>
   * DsTrustInterface.TRUST_ALWAYS - Always trust this certificate chain and don't ask again.<br>
   *
   * @param chain the certificate chain which specifies the identity of the subject which may or may
   *     not be trusted
   * @param client if true then client needs to be authenticated, otherwise server needs to be
   *     authenticated.
   * @return <code>true</code> it the certificate chain is trusted, <code>false</code> otherwise.
   */
  public boolean trust(X509Certificate[] chain, boolean client, String authType)
      throws CertificateException {
    boolean result = false;
    if (null != trustManagers) {
      X509TrustManager manager = null;
      for (int i = 0; i < trustManagers.length; i++) {
        manager = (X509TrustManager) trustManagers[i];
        if (null != manager) {
          if (client) manager.checkClientTrusted(chain, authType);
          else manager.checkServerTrusted(chain, authType);
          result = true;
          break;
        }
      }
    }
    if (!result && null != trustInterface) {
      result = callBackTrust(chain, client);
    }
    return result;
  }

  /**
   * Asks the trust interface, if registered, whether the given certificate chain, from a client or
   * server, is trusted or not. The trust interface can return any of the the following three
   * options:<br>
   * DsTrustInterface.TRUST_NO - Don't trust this certificate chain<br>
   * DsTrustInterface.TRUST_NOW - Trust this certificate chain, this time<br>
   * DsTrustInterface.TRUST_ALWAYS - Always trust this certificate chain and don't ask again.<br>
   *
   * @param chain the certificate chain which specifies the identity of the subject which may or may
   *     not be trusted
   * @param client if true then client needs to be authenticated, otherwise server needs to be
   *     authenticated.
   * @return <code>true</code> it the certificate chain is trusted, <code>false</code> otherwise.
   */
  private boolean callBackTrust(X509Certificate[] chain, boolean client) {
    short retVal =
        (client) ? trustInterface.isClientTrusted(chain) : trustInterface.isServerTrusted(chain);
    DsLog4j.authCat.log(
        Level.INFO,
        new StringBuffer("checking trustInterface callBackTrust  interface\n")
            .append(retVal)
            .toString());
    return trust(retVal, chain);
  }

  /**
   * If the option is:<br>
   * DsTrustInterface.TRUST_NO - Does nothing and returns false<br>
   * DsTrustInterface.TRUST_NOW - Does nothing and returns true<br>
   * DsTrustInterface.TRUST_ALWAYS - Adds the certificate chain in the trusted certificate list and
   * returns true<br>
   */
  private boolean trust(short option, X509Certificate[] chain) {
    boolean result = false;
    switch (option) {
      case DsTrustInterface.TRUST_NO:
        result = false;
        DsLog4j.authCat.log(
            Level.INFO,
            new StringBuffer("checking trust DsTrustInterface.TRUST_NO").append(result).toString());
        break;
      case DsTrustInterface.TRUST_NOW:
        result = true;
        DsLog4j.authCat.log(
            Level.INFO,
            new StringBuffer("checking trust DsTrustInterface.TRUST_NOW")
                .append(result)
                .toString());
        break;
      case DsTrustInterface.TRUST_ALWAYS:
        result = true;
        DsLog4j.authCat.log(
            Level.INFO,
            new StringBuffer("checking trust DsTrustInterface.TRUST_ALWAYS")
                .append(result)
                .toString());
        addTrustedCertificate(chain);
        break;
    }
    return result;
  }

  /** Generates a unique alias for adding trust certificate in the trust store. */
  private static String generateAlias(KeyStore trustStore) throws KeyStoreException {
    String alias = null;
    do {
      alias = "ds_trust_" + ++counter;
    } while (trustStore.containsAlias(alias));
    return alias;
  }

  public void setSoftFailEnabled(boolean softFailEnabled) {
    this.softFailEnabled = softFailEnabled;
  }

  public void setDisableAcceptedIssuers(boolean disableAcceptedIssuers) {
    this.disableAcceptedIssuers = disableAcceptedIssuers;
  }

  // I commented out this debugging method
  // because static analysis complains. - jsm
  // Please leave it here commented out.

  //    /**
  //     * Returns a string representation of the certificate chain
  //     */
  //    private String formatCertificates(X509Certificate[] chain)
  //    {
  //        if (chain == null) return "";
  //        StringBuffer buffer = new StringBuffer();
  //        X509Certificate cert = null;
  //
  //        for(int i = 0; i < chain.length; i++ )
  //        {
  //            cert = chain[i];
  //            buffer.append("Certificate [" +i +"]\n");
  //            buffer.append(cert.toString());
  //            buffer.append("\n\n");
  //        }
  //
  //        return buffer.toString();
  //    }

  ////////////////////////////////////////////////////////////////////////////////
  // Data members
  ////////////////////////////////////////////////////////////////////////////////
  private KeyStore trustStore;
  private TrustManager[] trustManagers;
  private X509Certificate[] certificates;
  private TrustManagerFactory trustManagerFactory;
  private DsSSLTrustManager sslManager;
  private DsSSLTrustManager sslTrustOverrideManager;
  private DsTrustInterface trustInterface;

  private String keyAlgorithm;
  private String tlsProvider;

  private static long counter = 0;
} // ends class
