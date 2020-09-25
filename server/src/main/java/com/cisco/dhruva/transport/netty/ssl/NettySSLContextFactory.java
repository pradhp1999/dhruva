/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.transport.netty.ssl;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.TLSAuthenticationType;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.ByteArrayInputStream;

public class NettySSLContextFactory {

  private SslContext sslContext;

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
    }
    throw new Exception("Invalid SSLContextType");
  }

  private SslContext serverSslContext(DsNetwork dsNetwork) throws Exception {

    String privateKey = dsNetwork.getSipPrivateKey();
    String certChain = dsNetwork.getSipCertificate();

    TLSAuthenticationType tlsAuthType = dsNetwork.getTlsAuthType();

    if (privateKey == null || certChain == null) {
      throw new NullPointerException("CertChain or Private Key is null");
    }
    SslContextBuilder sslContextBuilder =
        SslContextBuilder.forServer(
            new ByteArrayInputStream(certChain.getBytes()),
            new ByteArrayInputStream(privateKey.getBytes()));

    sslContextBuilder
        .sslProvider(dsNetwork.getSSlProvider())
        .clientAuth(dsNetwork.getClientAuthRequired() ? ClientAuth.REQUIRE : ClientAuth.NONE)
        .ciphers(dsNetwork.getCiphers())
        .trustManager(dsNetwork.getTrustManager());

    return sslContextBuilder.build();
  }

  private SslContext clientSslContext(DsNetwork dsNetwork) {
    return null;
  }
}
