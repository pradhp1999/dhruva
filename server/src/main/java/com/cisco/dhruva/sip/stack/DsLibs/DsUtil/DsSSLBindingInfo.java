// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert.DsCertificateHelper;
import com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert.SubjectAltName;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.util.log.Trace;
import java.net.InetAddress;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.apache.logging.log4j.Logger;

/**
 * Defines the binding info for the TLS transport type. This class contains the SSL session
 * information in addition to the binding info. The certificate chain of the peer can be queried
 * through <code>getPeerCertificateChain()</code>.
 *
 * @since SIP User Agent Java v5.0
 */
public class DsSSLBindingInfo extends DsBindingInfo {
  private SSLSession session;
  static Logger log = Trace.getLogger(DsSSLBindingInfo.class.getName());

  /**
   * Constructs the binding info and it needs the binding info to be set by using the relevant
   * setter methods.
   */
  public DsSSLBindingInfo() {
    super();
  }

  /**
   * Constructs the binding info and it needs the binding info to be set by using the relevant
   * setter methods.
   *
   * @param session the SSL session information
   */
  public DsSSLBindingInfo(SSLSession session) {
    super();
    this.session = session;
  }

  /**
   * Constructs the binding info with the specified address information.
   *
   * @param addr the remote address
   * @param port the remote port number
   * @param session the SSL session information
   */
  public DsSSLBindingInfo(String addr, int port, SSLSession session) {
    super(addr, port, DsSipTransportType.TLS);
    this.session = session;
  }

  /**
   * Constructs the binding info with the specified remote address information.
   *
   * @param remote_addr remote InetAddress
   * @param remote_port remote port
   * @param session the SSL session information
   */
  public DsSSLBindingInfo(InetAddress remote_addr, int remote_port, SSLSession session) {
    this(remote_addr, remote_port, false, session);
  }

  /**
   * Constructs the SSL binding info with the specified remote address information.
   *
   * @param remote_addr remote InetAddress
   * @param remote_port remote port
   * @param pending_closure if set to <code>true</code> the connection is closing
   * @param session the SSL session information
   */
  public DsSSLBindingInfo(
      InetAddress remote_addr, int remote_port, boolean pending_closure, SSLSession session) {
    super(remote_addr, remote_port, DsSipTransportType.TLS, pending_closure);
    this.session = session;
  }

  /**
   * Constructs the binding info with the specified local and remote address information.
   *
   * @param local_addr the local address
   * @param local_port the local port number
   * @param remote_addr the remote address
   * @param remote_port the remote port number
   * @param session the SSL session information
   */
  public DsSSLBindingInfo(
      InetAddress local_addr,
      int local_port,
      InetAddress remote_addr,
      int remote_port,
      SSLSession session) {
    this(local_addr, local_port, remote_addr, remote_port, false, session);
  }

  /**
   * Constructs the binding info with the specified local and remote address information.
   *
   * @param remote_addr remote InetAddress
   * @param remote_port remote port
   * @param local_addr local InetAddress
   * @param local_port local port
   * @param pending_closure if set to <code>true</code> the connection is closing
   * @param session the SSL session information
   */
  public DsSSLBindingInfo(
      InetAddress local_addr,
      int local_port,
      InetAddress remote_addr,
      int remote_port,
      boolean pending_closure,
      SSLSession session) {
    super(
        local_addr, local_port, remote_addr, remote_port, DsSipTransportType.TLS, pending_closure);
    this.session = session;
  }

  /**
   * Return the certificate chain presented by the peer.
   *
   * @return an ordered array of peer X.509 certificates, with the peer's own certificate first
   *     followed by any certificate authorities.
   * @throws SSLPeerUnverifiedException if peer identification is not available either because no
   *     certificate, or the particular cipher suite being used does not support authentication, or
   *     no peer authentication was established during SSL handshaking.
   */
  public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
    return (null == session) ? null : (X509Certificate[]) session.getPeerCertificates();
  }

  /**
   * Return the certificate presented by the peer.
   *
   * @return peer's Certificate
   * @throws SSLPeerUnverifiedException if peer identification is not available either because no
   *     certificate, or the particular cipher suite being used does not support authentication, or
   *     no peer authentication was established during SSL handshaking.
   */
  public X509Certificate getPeerCertificate() throws SSLPeerUnverifiedException {
    return (null == session) ? null : (X509Certificate) session.getPeerCertificates()[0];
  }

  /**
   * Returns the name of the SSL cipher suite which is used for all connections in the session. This
   * defines the level of protection provided to the data sent on the connection, including the kind
   * of encryption used and most aspects of how authentication is done. The cipher suite names as
   * defined in each supported SSL protocol version definition, and include:<br>
   * SSL_RSA_WITH_RC4_128_MD5 - a non-exportable SSL version 3 cipher suite supporting 128 bit RC4
   * encryption keys and full RSA key sizes.<br>
   * SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA - a non-exportable SSL version 3 cipher suite supporting 168
   * bit DES encryption keys. (The effective strength of this cipher is only 112 bits.)<br>
   * SSL_CK_RC4_128_EXPORT40_WITH_MD5 - an exportable SSL version 2 cipher suite using weakened RC4
   * encryption and limited RSA key sizes.<br>
   * SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA - an exportable SSL version 3 cipher suite using weakened
   * DES encryption, and which doesn't support authentication of servers.<br>
   * SSL_RSA_WITH_NULL_MD5 - an exportable SSL version 3 cipher suite using no encryption and full
   * RSA key sizes.<br>
   *
   * @return the name of the session's cipher suite
   */
  public String getCipherSuite() {
    return (null == session) ? null : session.getCipherSuite();
  }

  /**
   * Returns the host name of the peer in this session. That is, for the server, this is the
   * client's host, and for the client it is the server's host. The name may not be a fully
   * qualified host name or even a host name at all as in a string encoding of the peer's network
   * address. If such a name is desired, it might be resolved through a name service based on the
   * value returned by this method. This value is not authenticated and should not be relied upon.
   *
   * @return the host name of the peer in this session.
   */
  public String getPeerHost() {
    return (null == session) ? null : session.getPeerHost();
  }

  /**
   * Updates this binding info as per the specified binding info.
   *
   * @param new_info the source binding info
   */
  public void update(DsBindingInfo new_info) {
    super.update(new_info);
    session = null;
    try {
      DsSSLBindingInfo info = (DsSSLBindingInfo) new_info;
      session = info.session;
    } catch (ClassCastException cce) {
    }
  }
  /**
   * Copy source data to this object.
   *
   * @param new_info the source object
   */
  protected void clone(DsSSLBindingInfo new_info) {
    super.clone(new_info);
    new_info.session = session;
  }

  /**
   * Returns a new copy of this object.
   *
   * @return a new copy of this object
   */
  public Object clone() {
    DsSSLBindingInfo new_info = new DsSSLBindingInfo();
    clone(new_info);
    return new_info;
  }

  /**
   * Return the string representation of this SSL binding information.
   *
   * @return the string representation of this SSL binding information
   */
  public String toString() {
    return (super.toString() + session);
  }

  /**
   * Sets the SSL Session for this binding information.
   *
   * @param session the SSL session information
   */
  public void setSession(SSLSession session) {
    this.session = session;
  }

  public SSLSession getSession() {
    return session;
  }

  public String getPeerCommonName() {
    String cn = null;
    try {
      X509Certificate certificate = getPeerCertificate();
      if (certificate != null) {
        cn = DsCertificateHelper.getCommonName(certificate.getSubjectDN().toString());
      }
    } catch (Exception e) {
      log.error("Error in parsing CN from Peer Certificate ", e);
    }
    return cn;
  }

  public List<SubjectAltName> getPeerSubjectAltName() {
    List<SubjectAltName> subjectAltName = null;
    try {
      X509Certificate certificate = getPeerCertificate();
      if (certificate != null) {
        subjectAltName = DsCertificateHelper.getSubjectAltName(certificate);
      }
    } catch (SSLPeerUnverifiedException e) {
      log.error("Error in parsing SAN from Peer Certficate ", e);
    }
    return subjectAltName;
  }
}
