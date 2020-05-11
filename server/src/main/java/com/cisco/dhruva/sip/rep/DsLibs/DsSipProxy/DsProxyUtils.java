/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.re.controllers.DsProxyResponseGenerator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderList;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipProxyRequireHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRecordRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponseCode;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTag;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipToHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipUnsupportedHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipViaHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsURI;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
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
  private static final Trace Log = Trace.getTrace(DsProxyUtils.class.getName());

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
      if (Log.on && Log.isInfoEnabled()) Log.info("Created default AES KeyGen");

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
          if (Log.on && Log.isDebugEnabled()) Log.debug("Created Open JCE KeyGen");
        } catch (Throwable e2) {
          Log.error("Cannot create a AES KeyGen", e2);
          throw new NoSuchAlgorithmException("Cannot create DES KeyGen:" + e2.getMessage());
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
   * This does a few sanity checks on the messages received by the proxy, namely, it check 1.
   * Proxy-Require (this should probably be moved to Controller code 2. It checks for loops (a
   * problem for the app server) 3. It checks Max-Forwards (this should probably be done in the Low
   * Level
   *
   * @param request request to validate
   */
  protected static DsSipResponse validateRequest(DsSipRequest request, boolean checkProxyRequire) {

    if (checkProxyRequire) {
      // check any Proxy-Require headers
      // should this be moved to ProxyController?
      try {
        DsSipHeaderList requireList = request.getHeadersValidate(DsSipConstants.PROXY_REQUIRE);

        if (requireList != null) {

          if (Log.on && Log.isInfoEnabled()) Log.info("Proxy-Require header present:");

          DsSipProxyRequireHeader header;
          DsByteString extension;
          boolean allSupported = true;

          int size = requireList.size();
          int i;

          for (i = 0; i < size; i++) {
            // Combine lower loop with this loop, may want to use list
            // iterator again - JPS
            header = (DsSipProxyRequireHeader) requireList.get(i);
            // extension = header.getOptionTag();
            extension = header.getValue();

            if (!DsSupportedExtensions.isSupported(extension)) {
              allSupported = false;
              break;
            }
          }

          if (!allSupported) {

            DsSipResponse response =
                DsProxyResponseGenerator.createResponse(
                    DsSipResponseCode.DS_RESPONSE_BAD_EXTENSION, request);

            for (; i < size; i++) {
              // May want to use list iterator here - JPS
              header = (DsSipProxyRequireHeader) requireList.get(i);
              extension = header.getValue();
              if (!DsSupportedExtensions.isSupported(extension)) {
                DsSipUnsupportedHeader unsup = new DsSipUnsupportedHeader(extension);
                DsSipHeaderList headerList = response.getHeaders(DsSipUnsupportedHeader.sID);
                if (headerList == null) {
                  response.addHeader(unsup, false, false);
                } else {
                  headerList.addLast(unsup);
                }
              }
            }

            return response;
          }
        }
      } catch (Exception e) {
        Log.error("Error processing a request with Proxy-Require", e);
        try {
          return DsProxyResponseGenerator.createResponse(
              DsSipResponseCode.DS_RESPONSE_INTERNAL_SERVER_ERROR, request);
        } catch (Throwable t) {
          Log.error("Error generating response", t);
        }
        return null;
      }
    }

    return null;
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
    if (Log.on && Log.isDebugEnabled())
      Log.debug("Entering recognize(" + host + ", " + port + ", " + transport + ", " + myURL + ")");
    boolean b =
        (host.equals(myURL.getHost())
            && port == myURL.getPort()
            && transport == myURL.getTransportParam());
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving recognize(), returning " + b);
    return b;
  }
}
