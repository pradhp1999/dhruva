// Copyright (c) 2005-2007, 2014-2015 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.bouncycastle.crypto.fips.FipsStatus;

/**
 * This class defines the context for the SSL sockets and encapsulates information regarding the key
 * stores, trust certificates, secure protocol, SSL provider and key algorithms, that will in turn
 * define the context for the SSL sockets to provide secure communication.
 */
public class DsSSLContext {
  /** Transport Layer Security protocol name constant. */
  public static final String TLS = "TLS";

  /** Secure Socket Layer protocol name constant. */
  public static final String SSL = "SSL";

  /** Default TLS/SSL implementation provider. */
  public static final String SUNJSSE = "SunJSSE";

  /** Default Key Store provider. */
  public static final String SUN = "SUN";

  /** Default Key Algorithm. */
  public static final String KA_SUNX509 = "SunX509";

  /** Default Key Store Type. */
  public static final String KS_JKS = "jks";

  /** PKCS12 Key Store Type. */
  public static final String KS_PKCS12 = "PKCS12";

  // CAFFEINE 3.1 bug fix - CSCsk29536: TLS socket connection not established with CUPS/CUCM
  /** String array of the protocol versions to be enabled for use on the socket connection. */
  private String[] enabledSocketProtocols = null;

  private boolean certServiceTrustManagerEnabled;
  private boolean isServerAuthEnabled;
  private static boolean fips;
  private static String FIPS_PROVIDER_NAME;
  private SSLContext context;

  public static boolean isFIPS() {
    if (FIPS_PROVIDER_NAME == null) {
      try {
        initializeSslMode();
      } catch (DsSSLException ex) {
        return false;
      }
    }

    return fips;
  }

  public static void initializeSslMode() throws DsSSLException {
    if (FIPS_PROVIDER_NAME != null) {
      return;
    }

    try {
      // Force loading ssl-fips provider
      SSLContext ctx = SSLContext.getInstance(TLS);
    } catch (Exception ex) {
      System.out.println("initializeSslMode: " + ex.getMessage());
    }

    /*  TODO: Take care of FIPS mode
        fips = Provider.isFIPS();
        if (fips) {
          FIPS_PROVIDER_NAME =
              DsConfigManager.getProperty(
                  DsConfigManager.PROP_FIPS_PROVIDER, DsConfigManager.PROP_FIPS_PROVIDER_DEFAULT);
        } else {
          FIPS_PROVIDER_NAME = "UNKNOWN";
        }
    */

    System.out.println("initializeSslMode: FipsStatus.isReady: " + FipsStatus.isReady());
  }

  /**
   * The protocols parameter passed to the setEnabledProtocols method of SSLSocket specifies the
   * protocol versions to be enabled for use on the connection.
   *
   * <p>Also see http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/JSSERefGuide.html
   *
   * @param protos SSLv2 SSL version 2 protocol<br>
   *     SSLv3 SSL version 3 protocol<br>
   *     TLSv1 TLS version 1 protocol (defined in RFC 2246)<br>
   *     SSLv2Hello Currently, the SSLv3 and TLSv1 protocols allow you to send SSLv3 and TLSv1
   *     hellos encapsulated in an SSLv2 format hello. For more details on the reasons for allowing
   *     this compatibility in these protocols, see Appendix E in RFC 2246: The TLS Protocol Version
   *     1.0.<br>
   *     Note that some SSL/TLS servers do not support the v2 hello format and require that client
   *     hellos conform to the SSLv3 or TLSv1 client hello formats.<br>
   *     The SSLv2Hello option controls the SSLv2 encapsulation. If SSLv2Hello is disabled on the
   *     client, then all outgoing messages will conform to the SSLv3/TLSv1 client hello format. If
   *     SSLv2Hello is disabled on the server, then all incoming messages must conform to the
   *     SSLv3/TLSv1 client hello format.
   */
  public void setSocketEnabledProtocols(String[] protos) {
    enabledSocketProtocols = protos;
  }

  /**
   * Returns whether this context requires for the client authentication.
   *
   * @return true if the client authentication is required, false otherwise.
   */
  public boolean getNeedClientAuth() {
    return needClientAuth;
  }

  /**
   * Sets whether this context should require for the client authentication. If set to true, then
   * the incoming requests from the client is authenticated first. In this case the client will send
   * its certificate(s) and server will check if this client is trusted based on client
   * certificate(s). If set to false, then the client will not be authenticated and no information,
   * for the client certificate(s), would be available.
   *
   * @param clientAuth true if the client authentication should be required, false otherwise.
   */
  public void setNeedClientAuth(boolean clientAuth) {
    needClientAuth = clientAuth;
    if (trustManagerImpl != null) {
      trustManagerImpl.setSoftFailEnabled(!needClientAuth);
    }
  }

  /**
   * Returns an array of all the supported cipher suite names for this context. All the sockets
   * created through this context would support these cipher suites
   *
   * @return an array of supported cipher suite names
   */
  public String[] getSupportedCipherSuites() {
    return supportedCiphers;
  }

  /**
   * Returns an array of all the enabled cipher suite names for this context. This list of cipher
   * suites will be less than or equal to the supported cipher suites.
   *
   * @return an array of all the cipher suites which are currently enabled for this context.
   */
  public String[] getEnabledCipherSuites() {
    return enabledCiphers;
  }

  public boolean getIsServerAuthEnabled() {
    return isServerAuthEnabled;
  }

  public void setIsServerAuthEnabled(boolean enabled) {
    this.isServerAuthEnabled = enabled;
  }
  /**
   * Returns SSLEngine using the SSLContext created
   *
   * @return SSLEngine
   */
  public SSLEngine getSSLEngine() {
    SSLEngine engine = context.createSSLEngine();
    engine.setEnabledCipherSuites(enabledCiphers);
    engine.setEnabledProtocols(enabledSocketProtocols);
    if (needClientAuth) {
      engine.setNeedClientAuth(needClientAuth);
    } else {
      engine.setWantClientAuth(true);
    }

    return engine;
  }

  /**
   * Sets an array of cipher suites which should be enabled for this context. This list of cipher
   * suites should be less than or equal to the supported cipher suites.
   *
   * @param ciphers an array of all the cipher suites which should be currently enabled for this
   *     context.
   */
  public void setEnabledCipherSuites(String[] ciphers) {
    enabledCiphers = ciphers;
  }

  public void setDisableAcceptedIssuers(boolean disableAcceptedIssuers) {
    // custom trust manager will not be present for FIPS mode
    if (trustManagerImpl != null) {
      trustManagerImpl.setDisableAcceptedIssuers(disableAcceptedIssuers);
    }
  }

  /**
   * Returns an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. This socket is configured for the kind security defined for this context.
   *
   * @param host the server host
   * @param port the server port
   * @return the created socket
   * @throws IOException if the connection can't be established
   * @throws UnknownHostException if the specified host couldn't be found
   */
  public SSLSocket createSocket(String host, int port, DsNetwork network)
      throws UnknownHostException, IOException {
    SSLSocket socket = null;
    try {
      socket = (SSLSocket) socketFactory.createSocket();
      if (network == null) {
        network = DsNetwork.getDefault();
      }
      socket.connect(new InetSocketAddress(host, port), network.getTcpConnectionTimeout());
      init(socket);
      // We are setting SO_TIMEOUT so that the handshake does not get stuck, we reset it back to
      // default value of 0
      socket.setSoTimeout(network.getTlsHandshakeTimeout());
      socket.startHandshake();
      socket.setSoTimeout(0);
    } catch (IOException e) {
      if ((socket != null) && !(socket.isClosed())) {
        socket.close();
        throw e;
      }
    }
    return socket;
  }

  /**
   * Returns an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. This socket is configured for the kind security defined for this context.
   *
   * @param host the server host
   * @param port the server port
   * @return the created socket
   * @throws IOException if the connection can't be established
   */
  public SSLSocket createSocket(InetAddress host, int port, DsNetwork network) throws IOException {
    SSLSocket socket = null;
    try {
      socket = (SSLSocket) socketFactory.createSocket();
      if (network == null) {
        network = DsNetwork.getDefault();
      }
      socket.connect(new InetSocketAddress(host, port), network.getTcpConnectionTimeout());
      init(socket);
      // We are setting SO_TIMEOUT so that the handshake does not get stuck, we reset it back to
      // default value of 0
      socket.setSoTimeout(network.getTlsHandshakeTimeout());
      socket.startHandshake();
      socket.setSoTimeout(0);
    } catch (IOException e) {
      if ((socket != null) && !(socket.isClosed())) {
        socket.close();
        throw e;
      }
    }
    return socket;
  }

  /**
   * Returns an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. The client is bound to the specified network address <code>clientHost</code> and port
   * <code>clientPort</code>. This socket is configured for the kind security defined for this
   * context.
   *
   * @param host the server host
   * @param port the server port
   * @param clientHost the client host
   * @param clientPort the client port
   * @return the created socket
   * @throws IOException if the connection can't be established
   * @throws UnknownHostException if the specified host couldn't be found
   */
  public SSLSocket createSocket(
      String host, int port, InetAddress clientHost, int clientPort, DsNetwork network)
      throws UnknownHostException, IOException {
    SSLSocket socket = null;
    try {
      socket = (SSLSocket) socketFactory.createSocket();
      socket.bind(new InetSocketAddress(clientHost, clientPort));
      if (network == null) {
        network = DsNetwork.getDefault();
      }
      socket.connect(new InetSocketAddress(host, port), network.getTcpConnectionTimeout());
      init(socket);
      // We are setting SO_TIMEOUT so that the handshake does not get stuck, we reset it back to
      // default value of 0
      socket.setSoTimeout(network.getTlsHandshakeTimeout());
      socket.startHandshake();
      socket.setSoTimeout(0);
    } catch (IOException e) {
      if ((socket != null) && !(socket.isClosed())) {
        socket.close();
        throw e;
      }
    }
    return socket;
  }

  /**
   * Returns an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. The client is bound to the specified network address <code>clientHost</code> and port
   * <code>clientPort</code>. This socket is configured for the kind security defined for this
   * context.
   *
   * @param host the server host
   * @param port the server port
   * @param clientHost the client host
   * @param clientPort the client port
   * @return the created socket
   * @throws IOException if the connection can't be established
   */
  public SSLSocket createSocket(
      InetAddress host, int port, InetAddress clientHost, int clientPort, DsNetwork network)
      throws IOException {
    SSLSocket socket = null;
    try {
      socket = (SSLSocket) socketFactory.createSocket();
      socket.bind(new InetSocketAddress(clientHost, clientPort));
      if (network == null) {
        network = DsNetwork.getDefault();
      }
      socket.connect(new InetSocketAddress(host, port), network.getTcpConnectionTimeout());
      init(socket);
      // We are setting SO_TIMEOUT so that the handshake does not get stuck, we reset it back to
      // default value of 0
      socket.setSoTimeout(network.getTlsHandshakeTimeout());
      socket.startHandshake();
      socket.setSoTimeout(0);
    } catch (IOException e) {
      if ((socket != null) && !(socket.isClosed())) {
        socket.close();
        throw e;
      }
    }
    return socket;
  }

  /**
   * Returns an SSL socket layered over an existing socket connected to the named host, at the given
   * port. This constructor can be used when tunneling SSL through a proxy or when negotiating the
   * use of SSL over an existing socket. The host and port refer to the logical peer destination.
   * This socket is configured for the kind security defined for this context.
   *
   * @param host the server host
   * @param port the server port
   * @param socket the existing socket
   * @param autoClose close the underlying socket when this socket is closed
   * @return the created socket
   * @throws IOException if the connection can't be established
   * @throws UnknownHostException if the specified host couldn't be found
   */
  public SSLSocket createSocket(
      Socket socket, String host, int port, boolean autoClose, DsNetwork network)
      throws UnknownHostException, IOException {
    SSLSocket client = null;
    try {
      client = (SSLSocket) socketFactory.createSocket(socket, host, port, autoClose);
      init(client);
      int origSoTimeout = socket.getSoTimeout();
      if (network == null) {
        network = DsNetwork.getDefault();
      }
      client.setSoTimeout(network.getTlsHandshakeTimeout());
      client.startHandshake();
      client.setSoTimeout(origSoTimeout);
    } catch (IOException e) {
      if ((client != null) && !(client.isClosed())) {
        client.close();
        throw e;
      }
    }
    return client;
  }

  /**
   * Returns an SSL socket layered over an existing socket connected to the named host, at the given
   * port. This constructor can be used when tunneling SSL through a proxy or when negotiating the
   * use of SSL over an existing socket. The host and port refer to the logical peer destination.
   * This socket is configured for the kind security defined for this context.
   *
   * @param host the server host
   * @param port the server port
   * @param socket the existing socket
   * @param autoClose close the underlying socket when this socket is closed
   * @param serverMode whether to use this layered socket as server mode.
   * @return the created socket
   * @throws IOException if the connection can't be established
   * @throws UnknownHostException if the specified host couldn't be found
   */
  public SSLSocket createSocket(
      Socket socket,
      String host,
      int port,
      boolean autoClose,
      boolean serverMode,
      DsNetwork network)
      throws UnknownHostException, IOException {
    SSLSocket client = null;
    try {
      client = (SSLSocket) socketFactory.createSocket(socket, host, port, autoClose);
      init(client);
      if (serverMode) {
        if (needClientAuth) {
          client.setNeedClientAuth(needClientAuth);
        } else {
          client.setWantClientAuth(true);
        }

        client.setUseClientMode(false);
      } else {
        int origSoTimeout = socket.getSoTimeout();
        if (network == null) {
          network = DsNetwork.getDefault();
        }
        client.setSoTimeout(network.getTlsHandshakeTimeout());
        client.startHandshake();
        client.setSoTimeout(origSoTimeout);
      }
    } catch (IOException e) {
      if ((client != null) && !(client.isClosed())) {
        client.close();
        throw e;
      }
    }
    return client;
  }

  /**
   * Returns an SSL server socket which uses all network interfaces on the host, and is bound to the
   * specified port. This socket is configured for the kind security defined for this context.
   *
   * @param port the port to listen to.
   * @return the created socket
   * @throws IOException if there is a network error
   */
  public SSLServerSocket createServerSocket(int port) throws IOException {
    SSLServerSocket socket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
    init(socket);
    return socket;
  }

  /**
   * Returns an SSL server socket which uses all network interfaces on the host, is bound to a the
   * specified port, and uses the specified connection backlog. This socket is configured for the
   * kind security defined for this context.
   *
   * @param port the port to listen to.
   * @param backlog the number of connections that can be queued
   * @return the created socket
   * @throws IOException if there is a network error
   */
  public SSLServerSocket createServerSocket(int port, int backlog) throws IOException {
    SSLServerSocket socket =
        (SSLServerSocket) serverSocketFactory.createServerSocket(port, backlog);
    init(socket);
    return socket;
  }

  /**
   * Returns an SSL server socket which uses only the specified network interface on the local host,
   * is bound to a the specified port, and uses the specified connection backlog. This socket is
   * configured for the kind security defined for this context.
   *
   * @param port the port to listen to.
   * @param backlog the number of connections that can be queued
   * @param address the network interface address to use
   * @return the created socket
   * @throws IOException if there is a network error
   */
  public SSLServerSocket createServerSocket(int port, int backlog, InetAddress address)
      throws IOException {
    SSLServerSocket socket =
        (SSLServerSocket) serverSocketFactory.createServerSocket(port, backlog, address);
    init(socket);
    return socket;
  }

  /**
   * Registers the trust interface which will be called back for dynamically deciding the trust
   * relationship between the peers.
   *
   * @param trustInterface the trust interface which will be called back for dynamically deciding
   *     the trust relationship between the peers.
   */
  public void setTrustInterface(DsTrustInterface trustInterface) {
    if (trustManagerImpl != null) trustManagerImpl.setTrustInterface(trustInterface);
  }

  /**
   * Returns the trust interface which is called back for dynamically deciding the trust
   * relationship between the peers.
   *
   * @return the trust interface which is called back for dynamically deciding the trust
   *     relationship between the peers.
   */
  public DsTrustInterface getTrustInterface() {
    return (trustManagerImpl != null) ? trustManagerImpl.getTrustInterface() : null;
  }

  /**
   * Registers the trusted peer manager interface which maintains a list of trusted peers and will
   * be called back for retrieving the trust relationship between the peers.
   *
   * @param sslManager the trusted peer manager interface which maintains a list of trusted peers
   *     and will be called back for retrieving the trust relationship between the peers.
   */
  public void setTrustedPeerManager(DsSSLTrustManager sslManager) {
    if (trustManagerImpl != null) trustManagerImpl.setTrustedPeerManager(sslManager);
  }

  /**
   * Returns the trusted override manager overrides the client and server certificate validation
   *
   * @return trust override manager
   */
  public DsSSLTrustManager getTrustOverrideManager() {
    return (trustManagerImpl != null) ? trustManagerImpl.getTrustOverrideManager() : null;
  }

  /**
   * Registers the trust override manager which overrides client and server certificate validation
   *
   * @param sslTrustOverrideManager the trusted override manager which overrides client and server
   *     certificate validation
   */
  public void setTrustOverrideManager(DsSSLTrustManager sslTrustOverrideManager) {
    if (trustManagerImpl != null) trustManagerImpl.setTrustOverrideManager(sslTrustOverrideManager);
  }

  /**
   * Returns the trusted peer manager interface which maintains a list of trusted peers and is
   * called back for retrieving the trust relationship between the peers.
   *
   * @return the trusted peer manager interface which maintains a list of trusted peers and is
   *     called back for retrieving the trust relationship between the peers.
   */
  public DsSSLTrustManager getTrustedPeerManager() {
    return (trustManagerImpl != null) ? trustManagerImpl.getTrustedPeerManager() : null;
  }

  /**
   * Function sets the socketfactory to a mock socketfactory to be used in DsSipTlsHandshakeTimeout
   * Module Test (not to be used by any source code)
   */
  public void setSocketFactory(SSLSocketFactory socketFactory) {
    this.socketFactory = socketFactory;
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
    if (trustManagerImpl != null) trustManagerImpl.loadTrustStore(trustStore, password);
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
  public void loadTrustStore(String trustStoreFile, char[] password) throws Exception {
    if (trustManagerImpl != null) trustManagerImpl.loadTrustStore(trustStoreFile, password);
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
    if (trustManagerImpl != null) trustManagerImpl.loadTrustStore(stream, password);
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
  public void storeTrustStore(OutputStream stream, char[] password)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    if (trustManagerImpl != null) trustManagerImpl.storeTrustStore(stream, password);
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
  public void storeTrustStore(String trustStoreFile, char[] password)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    if (trustManagerImpl != null) trustManagerImpl.storeTrustStore(trustStoreFile, password);
  }

  /**
   * Adds the specified certificate as a trusted certificate and implies that the corresponding
   * subject is now trusted for future communications.
   *
   * @param certificate the new trusted certificate.
   */
  public void addTrustedCertificate(X509Certificate certificate) {
    if (trustManagerImpl != null) trustManagerImpl.addTrustedCertificate(certificate);
  }

  /**
   * Adds the specified certificate chain as trusted certificates and implies that the subject of
   * the top certificate is now trusted for future communications.
   *
   * @param chain the new trusted certificate chain.
   */
  public void addTrustedCertificate(X509Certificate[] chain) {
    if (trustManagerImpl != null) trustManagerImpl.addTrustedCertificate(chain);
  }

  /**
   * Constructs an SSL context by loading the key store specified in the system property
   * "ds.ssl.keyStore" value. It also looks for the key store password in the system property
   * "ds.ssl.keyStorePassword".<br>
   * </br> The key store specified in the system property is assumed to be of "JKS" key store type
   * and all the containing keys are secured with the same key store password specified in the
   * system property "ds.ssl.keyStorePassword".<br>
   * </br> Also if the system property "ds.ssl.trustStore" is specified then this value is used as
   * the trust store file location. The SSL context gets initialized with all the trust certificates
   * contained in this trust store file. It uses the following default values: <br>
   * </br> protocol TLS<br>
   * </br> key algorithm SunX509<br>
   * </br> TLS provider SunJSSE<br>
   * </br> Key Store Provider SUN<br>
   * </br>
   *
   * @throws DsSSLException if an error occurs during the initialization process.
   */
  public DsSSLContext() throws DsSSLException {
    this(
        DsConfigManager.getProperty(DsConfigManager.PROP_KEY_STORE),
        DsConfigManager.getProperty(DsConfigManager.PROP_KEY_STORE_PASS),
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE),
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE_PASS));
  }

  /**
   * Constructs an SSL context by loading the key store specified by the key store file path. The
   * specified key store is assumed to be of "JKS" key store type and and all the containing keys
   * are secured with the same key store password.<br>
   * Also if the system property "ds.ssl.trustStore" is specified then this value is used as the
   * trust store file location. The SSL context gets initialized with all the trust certificates
   * contained in this trust store file.<br>
   * It uses the following default values: <br>
   * </br> protocol TLS<br>
   * </br> key algorithm SunX509<br>
   * </br> TLS provider SunJSSE<br>
   * </br> Key Store Provider SUN<br>
   * </br>
   *
   * @param keyStoreFile the Key Store file path. This file contains key information.
   * @param password the password for the specified key store
   * @throws DsSSLException if an error occurs during the initialization process.
   */
  public DsSSLContext(String keyStoreFile, String password) throws DsSSLException {

    this(
        keyStoreFile,
        password,
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE),
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE_PASS));
  }

  /**
   * Constructs an SSL context by loading the key store specified by the key store file path. The
   * specified key store is assumed to be of "JKS" key store type and and all the containing keys
   * are secured with the same key store password.<br>
   * Also if the system property "ds.ssl.trustStore" is specified then this value is used as the
   * trust store file location. The SSL context gets initialized with all the trust certificates
   * contained in this trust store file.<br>
   * It uses the following default values: <br>
   * </br> protocol TLS<br>
   * </br> key algorithm SunX509<br>
   * </br> TLS provider SunJSSE<br>
   * </br> Key Store Provider SUN<br>
   * </br>
   *
   * @param keyStoreFile the Key Store file path. This file contains key information.
   * @param password the password for the specified key store
   * @param trustManager the user implemented trust manager which decides if the peer is trusted or
   *     not
   * @throws DsSSLException if an error occurs during the initialization process.
   */
  public DsSSLContext(String keyStoreFile, String password, DsSSLTrustManager trustManager)
      throws DsSSLException {
    this(
        keyStoreFile,
        password,
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE),
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE_PASS),
        trustManager);
  }

  /**
   * Constructs an SSL context by loading the key store specified by the key store file path. The
   * specified key store is assumed to be of "JKS" key store type and and all the containing keys
   * are secured with the same key store password. It uses the following default values: <br>
   * </br> protocol TLS<br>
   * </br> key algorithm SunX509<br>
   * </br> TLS provider SunJSSE<br>
   * </br> Key Store Provider SUN<br>
   * </br>
   *
   * @param keyStoreFile the Key Store file path. This file contains key information.
   * @param password the password for the specified key store
   * @param keyStoreType the key Store type.
   * @throws DsSSLException if an error occurs during the initialization process.
   */
  public DsSSLContext(
      String keyStoreFile,
      String password,
      String keyStoreType,
      boolean certServiceTrustManagerEnabled)
      throws DsSSLException {
    this(
        keyStoreFile,
        password,
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE),
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE_PASS),
        keyStoreType,
        (String) null,
        KA_SUNX509,
        KA_SUNX509,
        (String) null,
        (String) null,
        TLS,
        null,
        certServiceTrustManagerEnabled);
  }

  /**
   * Constructs an SSL context by loading the key store specified by the key store file path. The
   * specified key store is assumed to be of "JKS" key store type and and all the containing keys
   * are secured with the same key store password. It uses the following default values: <br>
   * </br> protocol TLS<br>
   * </br> key algorithm SunX509<br>
   * </br> TLS provider SunJSSE<br>
   * </br> Key Store Provider SUN<br>
   * </br>
   *
   * @param keyStoreFile the Key Store file path. This file contains key information.
   * @param password the password for the specified key store
   * @param trustStoreFile the trust Store file path. This file contains the trust certificates.
   * @param trustStorePassword the password for the specified trust store.
   * @throws DsSSLException if an error occurs during the initialization process.
   */
  public DsSSLContext(
      String keyStoreFile, String password, String trustStoreFile, String trustStorePassword)
      throws DsSSLException {
    this(keyStoreFile, password, trustStoreFile, trustStorePassword, (DsSSLTrustManager) null);
  }

  /**
   * Constructs an SSL context by loading the key store specified by the key store file path. The
   * specified key store is assumed to be of "JKS" key store type and and all the containing keys
   * are secured with the same key store password. It uses the following default values: <br>
   * </br> protocol TLS<br>
   * </br> key algorithm SunX509<br>
   * </br> TLS provider SunJSSE<br>
   * </br> Key Store Provider SUN<br>
   * </br>
   *
   * @param keyStoreFile the Key Store file path. This file contains key information.
   * @param password the password for the specified key store
   * @param trustStoreFile the trust Store file path. This file contains the trust certificates.
   * @param trustManager the user implemented trust manager which decides if the peer is trusted or
   *     not
   * @throws DsSSLException if an error occurs during the initialization process.
   */
  public DsSSLContext(
      String keyStoreFile, String password, String trustStoreFile, DsSSLTrustManager trustManager)
      throws DsSSLException {
    this(
        keyStoreFile,
        password,
        trustStoreFile,
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE_PASS),
        trustManager);
  }

  /**
   * Constructs an SSL context by loading the key store specified by the key store file path. The
   * specified key store is assumed to be of "JKS" key store type and and all the containing keys
   * are secured with the same key store password. It uses the following default values: <br>
   * </br> protocol TLS<br>
   * </br> key algorithm SunX509<br>
   * </br> TLS provider SunJSSE<br>
   * </br> Key Store Provider SUN<br>
   * </br>
   *
   * @param keyStoreFile the Key Store file path. This file contains key information.
   * @param password the password for the specified key store
   * @param trustStoreFile the trust Store file path. This file contains the trust certificates.
   * @param trustStorePassword the password for the specified trust store.
   * @param trustManager the user implemented trust manager which decides if the peer is trusted or
   *     not
   * @throws DsSSLException if an error occurs during the initialization process.
   */
  public DsSSLContext(
      String keyStoreFile,
      String password,
      String trustStoreFile,
      String trustStorePassword,
      DsSSLTrustManager trustManager)
      throws DsSSLException {
    this(
        keyStoreFile,
        password,
        trustStoreFile,
        trustStorePassword,
        (String) null,
        (String) null,
        KA_SUNX509,
        KA_SUNX509,
        (String) null,
        (String) null,
        TLS,
        trustManager,
        false);
  }

  /**
   * Constructs an SSL context by loading the key store specified by the key store file path. All
   * the keys in the key store should be secured with the same specified key store password. If
   * trust
   *
   * @param keyStoreFile the Key Store file path. This file contains key information.
   * @param password the password for the specified key store
   * @param trustStoreFile the trust Store file path. This file contains the trust certificates.
   * @param keyStoreType the key store type
   * @param keyAlgorithm the key generating algorithm used in the specified key store
   * @param ksProvider the provider name for the Key Store implementation
   * @param tlsProvider the provider name for the TLS/SSL implementation
   * @param protocol the security protocol to be used (TLS/SSL)
   * @param trustManager the user implemented trust manager which decides if the peer is trusted or
   *     not
   * @throws DsSSLException if an error occurs during the initialization process.
   */
  public DsSSLContext(
      String keyStoreFile,
      String password,
      String trustStoreFile,
      String keyStoreType,
      String keyAlgorithm,
      String ksProvider,
      String tlsProvider,
      String protocol,
      DsSSLTrustManager trustManager)
      throws DsSSLException {
    this(
        keyStoreFile,
        password,
        trustStoreFile,
        DsConfigManager.getProperty(DsConfigManager.PROP_TRUST_STORE_PASS),
        keyStoreType,
        (String) null,
        keyAlgorithm,
        KA_SUNX509,
        ksProvider,
        tlsProvider,
        protocol,
        trustManager,
        false);
  }

  /**
   * Constructs an SSL context by loading the key store specified by the key store file path. All
   * the keys in the key store should be secured with the same specified key store password. If
   * trust
   *
   * @param keyStoreFile the Key Store file path. This file contains key information.
   * @param password the password for the specified key store
   * @param trustStoreFile the trust Store file path. This file contains the trust certificates.
   * @param trustStorePassword the password for the specified trust store.
   * @param keyStoreType the key store type
   * @param trustStoreType the trust store type
   * @param keyAlgorithm the key generating algorithm used in the specified key store
   * @param trustAlgorithm the key generating algorithm used in the specified trust store.
   * @param ksProvider the provider name for the Key Store implementation
   * @param tlsProvider the provider name for the TLS/SSL implementation
   * @param protocol the security protocol to be used (TLS/SSL)
   * @param trustManager the user implemented trust manager which decides if the peer is trusted or
   *     not
   * @throws DsSSLException if an error occurs during the initialization process.
   */
  public DsSSLContext(
      String keyStoreFile,
      String password,
      String trustStoreFile,
      String trustStorePassword,
      String keyStoreType,
      String trustStoreType,
      String keyAlgorithm,
      String trustAlgorithm,
      String ksProvider,
      String tlsProvider,
      String protocol,
      DsSSLTrustManager trustManager,
      boolean certServiceTrustManagerEnabled)
      throws DsSSLException {
    // If keyStore is null then get the value from "javax.net.ssl.keyStore"
    // System property.

    this.keyStoreFile =
        (keyStoreFile != null)
            ? keyStoreFile
            : DsConfigManager.getProperty("javax.net.ssl.keyStore");

    if (null == keyStoreFile) {
      throw new DsSSLException("The keystore can not be null");
    }

    // If keyStore password is null then get the value from "javax.net.ssl.keyStorePassword"
    // System property.
    if (password == null) {
      password = DsConfigManager.getProperty("javax.net.ssl.keyStorePassword");
    }
    this.password = (password != null) ? password.toCharArray() : null;

    // If trustStore is null then get the value from "javax.net.ssl.trustStore"
    // System property.
    this.trustStoreFile =
        (trustStoreFile != null)
            ? trustStoreFile
            : DsConfigManager.getProperty("javax.net.ssl.trustStore");

    // If trustStore password is null then get the value from "javax.net.ssl.trustStorePassword"
    // System property.
    if (trustStorePassword == null) {
      trustStorePassword = DsConfigManager.getProperty("javax.net.ssl.trustStorePassword");
    }
    this.trustStorePassword =
        (trustStorePassword != null) ? trustStorePassword.toCharArray() : null;

    // If keyStore type is null then get the value from "javax.net.ssl.keyStoreType"
    // System property.
    this.keyStoreType =
        (keyStoreType != null)
            ? keyStoreType
            : DsConfigManager.getProperty("javax.net.ssl.keyStoreType");

    // If it still is not specified, then get the default
    if (null == this.keyStoreType) {
      this.keyStoreType = KeyStore.getDefaultType();
    }
    // If trustStore type is null then get the value from "javax.net.ssl.trustStoreType"
    // System property.
    this.trustStoreType =
        (trustStoreType != null)
            ? trustStoreType
            : DsConfigManager.getProperty("javax.net.ssl.trustStoreType");
    // If it still is not specified, then get the default
    if (null == this.trustStoreType) {
      this.trustStoreType = KeyStore.getDefaultType();
    }

    this.keyAlgorithm = keyAlgorithm;
    this.trustAlgorithm = trustAlgorithm;
    this.ksProvider = ksProvider;
    this.tlsProvider = tlsProvider;
    this.protocol = protocol;
    this.certServiceTrustManagerEnabled = certServiceTrustManagerEnabled;

    init(trustManager);
  }

  /** Initializes the SSL context. */
  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN"})
  private void init(DsSSLTrustManager trustManager) throws DsSSLException {
    // CAFFEINE 3.1 bug fix - CSCsk29536: TLS socket connection not established with CUPS/CUCM
    FileInputStream ksFile = null;
    try {
      KeyManagerFactory keyManagerFactory = null;
      KeyStore keyStore = null;

      keyManagerFactory = KeyManagerFactory.getInstance(keyAlgorithm);

      keyStore =
          isFIPS()
              ? KeyStore.getInstance(keyStoreType, FIPS_PROVIDER_NAME)
              : KeyStore.getInstance(keyStoreType);
      ksFile = new FileInputStream(keyStoreFile);
      keyStore.load(ksFile, password);

      keyManagerFactory.init(keyStore, password);

      if (null != trustStoreFile) {
        ksFile = new FileInputStream(trustStoreFile);
        keyStore = KeyStore.getInstance(trustStoreType);
        keyStore.load(ksFile, trustStorePassword);
        ksFile.close();
      }

      context = SSLContext.getInstance(protocol);
      if (isFIPS()) {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KA_SUNX509);
        trustManagerFactory.init(keyStore);
        context.init(
            keyManagerFactory.getKeyManagers(),
            trustManagerFactory.getTrustManagers(),
            SecureRandom.getInstance("DEFAULT", FIPS_PROVIDER_NAME));
      } else {
        DsTrustManagerImpl[] managers = new DsTrustManagerImpl[1];
        managers[0] =
            trustManagerImpl =
                new DsTrustManagerImpl(
                    keyStore,
                    trustAlgorithm,
                    null,
                    trustManager,
                    null,
                    this.certServiceTrustManagerEnabled);
        context.init(keyManagerFactory.getKeyManagers(), managers, null);
      }

      serverSocketFactory = (SSLServerSocketFactory) context.getServerSocketFactory();
      socketFactory = (SSLSocketFactory) context.getSocketFactory();

      this.enabledCiphers = serverSocketFactory.getDefaultCipherSuites();
      this.supportedCiphers = serverSocketFactory.getSupportedCipherSuites();

      // this.printClientCiphers(socketFactory);
      // this.printServerCiphers(serverSocketFactory);
    } catch (NoSuchProviderException nspe) {
      throw new DsSSLException(
          " The specified provider [" + tlsProvider + "] has not been configured/registered");
    } catch (NoSuchAlgorithmException nsae) {
      throw new DsSSLException(
          "The specified protocol ["
              + protocol
              + "] or algorithm ["
              + keyAlgorithm
              + " ] is not available from the specified provider ["
              + tlsProvider
              + "]");
    } catch (KeyStoreException kse) {
      throw new DsSSLException(
          "Error occurred while initializing through the specified key store ["
              + keyStoreFile
              + "]");
    } catch (UnrecoverableKeyException uke) {
      throw new DsSSLException(
          "The key information couldn't be recovered from the specified key store ["
              + keyStoreFile
              + "]");
    } catch (FileNotFoundException fnfe) {
      throw new DsSSLException("Couldn't find the specified key store [" + keyStoreFile + "]");
    } catch (IOException ioe) {
      throw new DsSSLException("Either keystore is tampered with or the password is incorrect");
    } catch (KeyManagementException kme) {
      throw new DsSSLException(kme);
    } catch (Exception exc) {
      throw new DsSSLException(exc.getMessage(), exc);
    } catch (AssertionError exc) {
      throw new DsSSLException(
          "Exception occurred while initializing the SSL Context: ",
          new Exception(exc.getMessage()));
    } finally {
      if (ksFile != null) {
        try {
          ksFile.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
    // Get the default settings for the client Authentication.
    needClientAuth =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_CLIENT_AUTH, DsConfigManager.PROP_CLIENT_AUTH_DEFAULT);
  }

  private void init(SSLSocket socket) {
    socket.setEnabledCipherSuites(enabledCiphers);

    // CAFFEINE 3.1 bug fix - CSCsk29536: TLS socket connection not established with CUPS/CUCM
    if (enabledSocketProtocols != null) {
      socket.setEnabledProtocols(enabledSocketProtocols);
    }

    try {
      int tosValue =
          DsConfigManager.getProperty(
              DsConfigManager.PROP_TOS_VALUE, DsConfigManager.PROP_TOS_VALUE_DEFAULT);
      if (!(tosValue < 0 || tosValue > 255)) {
        socket.setTrafficClass(tosValue);

        DsLog4j.socketCat.debug("IPTypeOfService: " + socket.getTrafficClass());
      }
    } catch (SocketException e) {

      DsLog4j.socketCat.error(
          "INVALID TOS Value: "
              + DsConfigManager.getProperty(
                  DsConfigManager.PROP_TOS_VALUE, DsConfigManager.PROP_TOS_VALUE_DEFAULT),
          e);
    }

    // this.printClientCiphers(socketFactory);
    // this.printServerCiphers(serverSocketFactory);
  }

  private void init(SSLServerSocket socket) {
    socket.setEnabledCipherSuites(enabledCiphers);
    socket.setNeedClientAuth(needClientAuth);

    // CAFFEINE 3.1 bug fix - CSCsk29536: TLS socket connection not established with CUPS/CUCM
    if (enabledSocketProtocols != null) {
      socket.setEnabledProtocols(enabledSocketProtocols);
    }

    // this.printClientCiphers(socketFactory);
    // this.printServerCiphers(serverSocketFactory);
  }

  public DsTrustManagerImpl getTrustManagerImpl() {
    return trustManagerImpl;
  }

  // Data members.
  private SSLServerSocketFactory serverSocketFactory;
  private SSLSocketFactory socketFactory;

  private String keyStoreFile;
  private String trustStoreFile;
  private char[] password;
  private char[] trustStorePassword;

  private String keyStoreType;
  private String trustStoreType;
  private String keyAlgorithm;
  private String trustAlgorithm;

  private String tlsProvider;
  private String ksProvider;
  private String protocol;

  private DsTrustManagerImpl trustManagerImpl;

  private boolean needClientAuth;
  private String[] supportedCiphers;
  private String[] enabledCiphers;

  // I commented out these debugging methods
  // because static analysis complains. - jsm
  // Please leave them here commented out.

  //    ///// Debugging methods//////////
  //    private static void printManagers(TrustManager[] managers)
  //    {
  //        if ( null != managers)
  //        {
  //            for (int i = 0; i < managers.length; i++)
  //            {
  //                System.out.println("Manager = " + i + "\n");
  //                printCertificates(managers[i]);
  //            }
  //        }
  //    }
  //
  //    private static void printManagers(KeyManager[] managers)
  //    {
  //        if ( null != managers)
  //        {
  //            for (int i = 0; i < managers.length; i++)
  //            {
  //                System.out.println("Manager = " + i + "\n");
  //                printKeys(managers[i]);
  //            }
  //        }
  //    }
  //
  //    private static void printKeys(KeyManager manager)
  //    {
  //        String keyAlgorithm = "duke";
  //        if (null != manager)
  //        {
  //            X509Certificate [] certs = ((X509KeyManager)manager).getCertificateChain("duke");
  //            for (int i =0; i < certs.length; i++)
  //            {
  //                System.out.println("Cert Chain " + i +" =\n" + certs[i].toString());
  //
  //                Principal p =   certs[i].getIssuerDN();
  //                Principal[] pp = new Principal[1];
  //                pp[0] = p;
  //// --                System.out.println("Choose Client = " +
  // ((X509KeyManager)manager).chooseClientAlias(keyAlgorithm,pp, null));
  //// --                System.out.println("Choose Server = " +
  // ((X509KeyManager)manager).chooseServerAlias(keyAlgorithm,pp, null));
  //                String[] strings = ((X509KeyManager)manager).getClientAliases(keyAlgorithm,pp);
  //                if ( strings != null)
  //                {
  //                    for (int j =0; j < strings.length; j++)
  //                    {
  //                        System.out.println("Client Alias " + j + strings[j]);
  //                    }
  //                }
  //                strings = ((X509KeyManager)manager).getServerAliases(keyAlgorithm,pp);
  //                if ( strings != null)
  //                {
  //                    for (int j =0; j < strings.length; j++)
  //                    {
  //                        System.out.println("Server Alias " + j + strings[j]);
  //                    }
  //                }
  //            }
  //
  //            PrivateKey pk = ((X509KeyManager)manager).getPrivateKey("duke");
  //            System.out.println("Private Key " + pk);
  //
  //        }
  //
  //    }
  //
  //    private static void printCertificates(TrustManager manager)
  //    {
  //        if (null != manager)
  //        {
  //            X509Certificate [] certs = ((X509TrustManager)manager).getAcceptedIssuers();
  //            for (int i =0; i < certs.length; i++)
  //            {
  //                System.out.println("Cert Chain " + i +" =\n" + certs[i].toString());
  //            }
  //        }
  //
  //    }
  //
  //    private void printServerCiphers(SSLServerSocketFactory factory)
  //    {
  //        String[] ciphers = factory.getDefaultCipherSuites();
  //        System.out.println("Server Default Ciphers\n");
  //        for (int i =0; i < ciphers.length; i++)
  //        {
  //            System.out.println("Cipher " + i + " = " + ciphers[i]);
  //        }
  //        ciphers = factory.getSupportedCipherSuites();
  //        System.out.println("Server Supported Ciphers\n");
  //        for (int j =0; j < ciphers.length; j++)
  //        {
  //            System.out.println("Cipher " + j + " = " + ciphers[j]);
  //        }
  //    }
  //    private void printClientCiphers(SSLSocketFactory factory)
  //    {
  //        String[] ciphers = factory.getDefaultCipherSuites();
  //        System.out.println("Client Default Ciphers\n");
  //        for (int i =0; i < ciphers.length; i++)
  //        {
  //            System.out.println("Cipher " + i + " = " + ciphers[i]);
  //        }
  //        ciphers = factory.getSupportedCipherSuites();
  //        System.out.println("Client Supported Ciphers\n");
  //        for (int j =0; j < ciphers.length; j++)
  //        {
  //            System.out.println("Cipher " + j + " = " + ciphers[j]);
  //        }
  //    }
  //
  //    private void printSSocket(SSLServerSocket s)
  //    {
  //        String[] ciphers = s.getEnabledCipherSuites ();
  //        System.out.println("Server Socket Enabled Ciphers\n");
  //        for (int i =0; i < ciphers.length; i++)
  //        {
  //            System.out.println("Cipher " + i + " = " + ciphers[i]);
  //        }
  //        ciphers = s.getSupportedCipherSuites();
  //        System.out.println("Server Socket Supported Ciphers\n");
  //        for (int j =0; j < ciphers.length; j++)
  //        {
  //            System.out.println("Cipher " + j + " = " + ciphers[j]);
  //        }
  //
  //        System.out.println(" Session Creation = " +s.getEnableSessionCreation());
  //        System.out.println(" Need Client Auth = " +s.getNeedClientAuth());
  //        System.out.println(" Use Client Mode = " +s.getUseClientMode());
  //
  //    }
  //
  //    private void printSocket(SSLSocket s)
  //    {
  //        String[] ciphers = s.getEnabledCipherSuites ();
  //        System.out.println("Client Socket Enabled Ciphers\n");
  //        for (int i =0; i < ciphers.length; i++)
  //        {
  //            System.out.println("Cipher " + i + " = " + ciphers[i]);
  //        }
  //        ciphers = s.getSupportedCipherSuites();
  //        System.out.println("Client Socket Supported Ciphers\n");
  //        for (int j =0; j < ciphers.length; j++)
  //        {
  //            System.out.println("Cipher " + j + " = " + ciphers[j]);
  //        }
  //        System.out.println(" Client - Session Creation = " +s.getEnableSessionCreation());
  //        System.out.println(" Client - Need Client Auth = " +s.getNeedClientAuth());
  //        System.out.println(" Client - Use Client Mode = " +s.getUseClientMode());
  //    }
} // ends class DsSSLContext
