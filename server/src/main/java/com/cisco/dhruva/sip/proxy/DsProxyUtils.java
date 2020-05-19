/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsProxyUtils.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import javax.crypto.KeyGenerator;

/** Contains certain wrappers that compensate for problems with DsSipObject APIs */
public class DsProxyUtils {

  private static final String SUN_JCE_PROVIDER = "com.sun.crypto.provider.SunJCE";
  private static final String OPEN_JCE_PROVIDER = "au.net.aba.crypto.provider.ABAProvider";

  // the name of this Logger is non-standard to work around an
  // obscure log4j bug that causes the proxy to hang when
  // debug command is issues under certain conditions
  private static final Logger Log = DhruvaLoggerFactory.getLogger(DsProxyUtils.class);

  /**
   * Removes the top value of the header hdr
   *
   * @param msg SIP message
   * @param hdr header name
   */
  public static DsSipHeaderInterface removeTopHeader(DsSipMessage msg, int hdr) {
    if (msg == null) return null;
    return msg.removeHeader(hdr);
  }

  public static void fixRequestForForking(
      DsSipRequest request, boolean stripVia, boolean stripRecordRoute) {
    request.setFinalised(false);

    if (stripVia) {
      removeTopVia(request);
    }

    if (stripRecordRoute) {
      removeTopRecordRoute(request);
    }

    try {
      request.lrUnescape();
    } catch (Exception e) {
      Log.warn("Exception encountered while trying to unescape request", e);
    }

    request.setBindingInfo(new DsBindingInfo());
  }

  public static DsSipRequest cloneRequestForForking(
      DsSipRequest originalRequest, boolean stripVia, boolean stripRecordRoute) {
    DsSipRequest clone = (DsSipRequest) originalRequest.clone();
    fixRequestForForking(clone, stripVia, stripRecordRoute);
    return clone;
  }

  public static KeyGenerator getAESKeyGenerator() throws NoSuchAlgorithmException {
    // , NoSuchPaddingException, InvalidKeyException{
    KeyGenerator keyGen;

    try {
      // first try to use preconfigured crypto provider
      keyGen = KeyGenerator.getInstance("AES");
      Log.info("Created default AES KeyGen");

    } catch (NoSuchAlgorithmException e) {
      Log.warn("Error getting default KeyGen for AES", e);
      try {

        // if none configured, try to use the standard Sun provider
        // (still must be installed)
        Class sunJceClass = Class.forName(SUN_JCE_PROVIDER);
        Provider sunJCE = (Provider) sunJceClass.newInstance();
        Security.addProvider(sunJCE);

        keyGen = KeyGenerator.getInstance("AES");
      } catch (Throwable e1) {
        Log.warn("Error getting Sun's KeyGen for AES", e1);
        try {
          // if none configured, try to use the Open JCE provider
          // (still must be installed)
          Class sunJceClass = Class.forName(OPEN_JCE_PROVIDER);
          Provider openJCE = (Provider) sunJceClass.newInstance();
          Security.addProvider(openJCE);

          keyGen = KeyGenerator.getInstance("AES");
          Log.debug("Created Open JCE KeyGen");
        } catch (Throwable e2) {
          Log.error("Cannot create a AES KeyGen", e2);
          throw new NoSuchAlgorithmException("Cannot create AES KeyGen:" + e2.getMessage());
        }
      }
    }
    return keyGen;
  }

  /**
   * Removes the top value of the header hdr
   *
   * @param msg SIP message
   * @param hdr header name
   */
  public static void removeHeader(DsSipMessage msg, int hdr) {
    if (msg == null) return;

    msg.removeHeaders(hdr);
  }

  public static int getResponseClass(DsSipResponse response) {
    return response.getStatusCode() / 100;
  }

  /**
   * Sends a final response without creating ProxyTransaction This is used to send error responses
   * to requests that fail sanity checks performed in DsProxyUtils.validateRequest().
   *
   * @param server request in question
   * @param response to send
   * @return DsSipServerTransaction transaction to return to Low Level
   */
  protected static DsSipServerTransaction sendErrorResponse(
      DsSipServerTransaction server, DsSipResponse response) throws DsException, IOException {
    DsSipToHeader to = response.getToHeaderValidate();

    // To header is not null if we got to here
    if (to.getTag() == null) {
      to.setTag(DsSipTag.generateTag());
    }

    server.sendResponse(response);

    return server;
  }

  protected static void removeTopVia(DsSipMessage msg) {
    removeTopHeader(msg, DsSipViaHeader.sID);
  }

  protected static void removeTopRecordRoute(DsSipMessage msg) {
    removeTopHeader(msg, DsSipRecordRouteHeader.sID);
  }

  public static boolean recognize(DsURI uri, DsSipURL myURL) {
    boolean b = false;

    if (uri.isSipURL()) {
      DsSipURL url = (DsSipURL) uri;
      b = recognize(url, myURL);
    }
    return b;
  }

  public static boolean recognize(DsSipURL url, DsSipURL myURL) {
    boolean b = false;

    if (url.getMAddrParam() != null) {
      b = recognize(url.getMAddrParam(), url.getPort(), url.getTransportParam(), myURL);
    } else {
      b = recognize(url.getHost(), url.getPort(), url.getTransportParam(), myURL);
    }
    return b;
  }

  public static boolean recognize(
      DsByteString host, int port, Transport transport, DsSipURL myURL) {
    Log.debug("Entering recognize(" + host + ", " + port + ", " + transport + ", " + myURL + ")");
    boolean b =
        (host.equals(myURL.getHost())
            && port == myURL.getPort()
            && transport == myURL.getTransportParam());
    Log.debug("Leaving recognize(), returning " + b);
    return b;
  }
}
