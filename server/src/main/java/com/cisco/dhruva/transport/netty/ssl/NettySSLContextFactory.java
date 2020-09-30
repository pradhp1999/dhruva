/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.transport.netty.ssl;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class NettySSLContextFactory {

  public enum SSLContextType {
    CLIENT,
    SERVER
  }

  public static NettySSLContextFactory getInstance() {
    return new NettySSLContextFactory();
  }

  private NettySSLContextFactory() {}

  public SslContext createSslContext(SSLContextType sslContextType, DsNetwork dsNetwork)
      throws Exception {
    switch (sslContextType) {
      case CLIENT:
        return clientSslContext(dsNetwork);

      case SERVER:
        return serverSslContext(dsNetwork);
      default:
        throw new Exception("Invalid SSLContextType");
    }
  }

  private SslContext serverSslContext(DsNetwork dsNetwork) throws Exception {

    SslContextBuilder sslContextBuilder =
        SslContextBuilder.forServer(getCertStream(dsNetwork), getPrivateKeyStream(dsNetwork));

    configureSslContext(sslContextBuilder, dsNetwork);
    sslContextBuilder.clientAuth(
        dsNetwork.getClientAuthRequired() ? ClientAuth.REQUIRE : ClientAuth.NONE);

    return sslContextBuilder.build();
  }

  private SslContext clientSslContext(DsNetwork dsNetwork) throws Exception {
    SslContextBuilder sslContextBuilder =
        SslContextBuilder.forClient()
            .keyManager(getCertStream(dsNetwork), getPrivateKeyStream(dsNetwork));
    configureSslContext(sslContextBuilder, dsNetwork);
    return sslContextBuilder.build();
  }

  private void configureSslContext(SslContextBuilder sslContextBuilder, DsNetwork dsNetwork)
      throws Exception {
    sslContextBuilder
        .sslProvider(dsNetwork.getSSlProvider())
        .ciphers(dsNetwork.getCiphers())
        .trustManager(dsNetwork.getTrustManager())
        .protocols(dsNetwork.getProtocols());
  }

  private InputStream getCertStream(DsNetwork dsNetwork) {
    String certChain = dsNetwork.getSipCertificate();
    if (certChain == null) {
      throw new NullPointerException(
          "CertChain is null, cannot create SSLContext for TLS, Please configure proper Certificate using \"sipCertificate\" property");
    }
    return new ByteArrayInputStream(certChain.getBytes());
  }

  private InputStream getPrivateKeyStream(DsNetwork dsNetwork) {
    String privateKey = dsNetwork.getSipPrivateKey();
    if (privateKey == null) {
      throw new NullPointerException(
          "Private key is null,  cannot create SSLContext for TLS, Please configure proper PrivateKey using \"sipPrivateKeys\" property");
    }
    return new ByteArrayInputStream(privateKey.getBytes());
  }
}
