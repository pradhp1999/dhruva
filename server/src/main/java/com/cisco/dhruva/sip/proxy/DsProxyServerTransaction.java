/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsProxyServerTransaction.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.sip.hostPort.HostPortUtil;
import com.cisco.dhruva.sip.DsUtil.DsReConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * This class represents the ServerTransaction on the receiving side of ProxyTransaction. Note that
 * more than one such transaction may exist due to merged requests.
 */
public class DsProxyServerTransaction {

  private DsSipServerTransaction serverTransaction;
  private DsProxyTransaction proxy;

  /** request received */
  private DsSipRequest request;
  /** last response sent */
  private DsSipResponse response;

  private int rrIndexFromEnd;
  private int pathIndexFromEnd;

  private int numVias = 0;

  private boolean okResponseSent = false;

  // replaced by the full binding info object
  // protected DsNetwork network; // remember the network a request was received on
  // protected DsByteString connId;

  protected DsBindingInfo bindingInfo;
  private static final int UNINITIALIZED = -1;

  protected static Logger Log = DhruvaLoggerFactory.getLogger(DsProxyServerTransaction.class);

  protected DsProxyServerTransaction(
      DsProxyTransaction proxy, DsSipServerTransaction trans, DsSipRequest request) {
    serverTransaction = trans;
    this.proxy = proxy;

    DsSipHeaderList viaHeaders = request.getHeaders(DsSipConstants.VIA);
    if (viaHeaders != null) numVias = viaHeaders.size();

    Log.debug("Counted number of Vias: " + numVias);

    // replaced by the full binding info object
    // network = request.getNetwork();
    // connId = request.getBindingInfo().getConnectionId();
    if (DsControllerConfig.getCurrent().isStateful()) {
      int recordRouteHeaderCount = UNINITIALIZED;
      DsSipHeaderList recordRouteHeaders = request.getHeaders(DsSipRecordRouteHeader.sID);

      if (recordRouteHeaders == null) {
        rrIndexFromEnd = 0;
      } else {

        DsSipHeaderList recordRouteHeadersClone = (DsSipHeaderList) recordRouteHeaders.clone();
        try {
          recordRouteHeadersClone.validate();
          recordRouteHeaderCount = recordRouteHeadersClone.size();
        } catch (DsSipParserException e) {
          Log.warn("Exception in parsing Record-Route header ", e);
        } catch (DsSipParserListenerException e) {
          Log.warn("Exception in parsers listener while parsing Record-Route header ", e);
        }
        if (recordRouteHeaderCount != UNINITIALIZED) {
          rrIndexFromEnd = recordRouteHeaderCount;
        } else {
          rrIndexFromEnd = recordRouteHeaders.size(); // at least it will be in a
          // moment
        }
      }

      DsSipHeaderList pathHeaders = request.getHeaders(DsSipPathHeader.sID);
      if (pathHeaders == null) {
        pathIndexFromEnd = 0;
      } else {
        pathIndexFromEnd = pathHeaders.size();
      }
    } else {
      rrIndexFromEnd = -1; // not valid in stateless
      pathIndexFromEnd = -1;
    }

    bindingInfo = (DsBindingInfo) request.getBindingInfo().clone();
  }

  /** Quick hack for the App server */
  public void updateServerTransaction(DsSipServerTransaction serverTrans) {
    serverTransaction = serverTrans;
  }

  protected DsSipServerTransaction getTransaction() {
    return serverTransaction;
  }

  public void respond(DsSipResponse response) throws DsDestinationUnreachableException {

    Log.debug("Entering respond()");

    // send the response
    try {
      if (response != null) {

        DsSipHeaderList vias = response.getHeaders(DsSipConstants.VIA);
        int numResponseVias = vias != null ? vias.size() : 0;

        Log.debug("numResponseVias=" + numResponseVias + ", numVias=" + numVias);

        for (int x = numResponseVias; x > numVias; x--) {
          assert vias != null;
          vias.removeFirstHeader();
        }

        // replaced by the full binding info object
        // response.setNetwork(network);
        // response.getBindingInfo().setConnectionId(connId);
        response.setBindingInfo(bindingInfo);

        if (DsControllerConfig.getCurrent().doRecordRoute()) setRecordRouteInterface(response);
      }

      serverTransaction.sendResponse(response);

      this.response = response;

      if (response != null && response.getResponseClass() == 2 && !okResponseSent) {
        // remove Transaction after a while even
        // if the ACK to 200 has not been received
        //	serverTransaction.setTn(60000);
        okResponseSent = true;
        Log.debug("Tn timer set for ServerTransaction");
      }

    } catch (Exception e) {
      throw new DsDestinationUnreachableException("Error sending a response" + e);
    }
  }

  protected DsSipResponse getResponse() {
    return response;
  }

  /** This is used to handle the special case with INVITE 200OK retransmissions */
  protected void retransmit200()
      throws DsInvalidStateException, DsDestinationUnreachableException, DsException, IOException {

    if (response != null && DsProxyUtils.getResponseClass(response) == 2) {
      // respond(response);
      serverTransaction.sendResponse(response);
    } else throw new DsInvalidStateException("Cannot retransmit in this state");
  }

  public void setRecordRouteInterface(DsSipMessage msg) throws DsException {
    Log.debug("Entering setRecordRouteInterface()");

    if (msg.getHeaders(DsSipRecordRouteHeader.sID) != null) {
      if (rrIndexFromEnd >= 0) {
        // stateful, just flip your own
        setRecordRouteInterfaceStateful(msg);
      } else {
        // stateless, must flip them all
        setRecordRouteInterfaceStateless(msg);
      }
    }
  }

  // REDDY_RR_CHANGE
  private void setRecordRouteInterfaceStateful(DsSipMessage msg) throws DsException {
    // int interfacing = (m_RequestDirection == DsControllerConfig.INBOUND) ?
    // DsControllerConfig.OUTBOUND : DsControllerConfig.INBOUND;
    DsSipHeaderList rrList = null;

    rrList = msg.getHeadersValidate(DsSipConstants.RECORD_ROUTE);

    boolean compress = msg.shouldCompress();
    DsTokenSipDictionary encode = msg.shouldEncode();

    int routeIndex = rrList.size() - rrIndexFromEnd - 1;

    if ((routeIndex >= 0) && (routeIndex < rrList.size())) {
      DsSipRecordRouteHeader rrHeader = (DsSipRecordRouteHeader) rrList.get(routeIndex);
      DsSipURL currentRRURL = (DsSipURL) rrHeader.getNameAddress().getURI();
      setRRHelper(msg, currentRRURL, compress, encode);
    }
  }

  private void setRRHelper(
      DsSipMessage msg, DsSipURL currentRRURL, boolean compress, DsTokenSipDictionary tokDic) {

    DsControllerConfig config = DsControllerConfig.getCurrent();
    String currentRRURLHost = null;

    if (currentRRURL != null) {

      // get the network corresponding to the host portion in RR. If host contains externalIP,
      // get the localIP to know the network accordingly
      currentRRURLHost = HostPortUtil.reverseHostInfoToLocalIp(currentRRURL).toString();

      String network = null;
      String name =
          config.checkRecordRoutes(
              currentRRURL.getUser(),
              new DsByteString(currentRRURLHost),
              currentRRURL.getPort(),
              currentRRURL.getTransportParam());

      if (name != null) {
        // todo optimize when get a chance
        Log.debug("Record Route URL to be modified : " + currentRRURL);
        DsByteString u = currentRRURL.getUser();
        String user = null;
        if (u != null) {
          try {
            user = DsSipURL.getUnescapedString(u).toString();
          } catch (DsException e) {
            Log.error("Error in unescaping the RR URI user portion", e);
            user = u.toString();
          }
        }
        if (user != null) {
          StringTokenizer st = new StringTokenizer(user);
          String t = st.nextToken(DsReConstants.DELIMITER_STR);
          while (t != null) {
            if (t.startsWith(DsReConstants.NETWORK_TOKEN)) {
              network = t.substring(DsReConstants.NETWORK_TOKEN.length());
              user = user.replaceFirst(t, DsReConstants.NETWORK_TOKEN + name);
              Log.debug("Replace Record-route host from {} to {}", t, name);
              break;
            }
            t = st.nextToken(DsReConstants.DELIMITER_STR);
          }
          currentRRURL.setUser(
              DsSipURL.getEscapedString(new DsByteString(user), DsSipURL.USER_ESCAPE_BYTES));
        } else {
          network = msg.getNetwork().getName();
        }

        Log.debug(
            "Outgoing network of the message for which record route has to be modified : "
                + network);
        DsSipRecordRouteHeader recordRouteInterfaceHeader =
            config.getRecordRouteInterface(network, false);

        if (recordRouteInterfaceHeader == null) {
          Log.debug("Did not find the Record Routing Interface!");
          return;
        }

        DsSipURL RRUrl = (DsSipURL) recordRouteInterfaceHeader.getURI();

        // replace local IP with External IP for public network when modifying user portion of RR
        currentRRURL.setHost(HostPortUtil.convertLocalIpToHostInfo(RRUrl));

        if (RRUrl.hasPort()) {
          currentRRURL.setPort(RRUrl.getPort());
        } else {
          currentRRURL.removePort();
        }

        if (RRUrl.hasTransport()) {
          currentRRURL.setTransportParam(RRUrl.getTransportParam());
        } else {
          currentRRURL.removeTransportParam();
        }
        if (compress) {
          currentRRURL.setCompParam(DsSipConstants.BS_SIGCOMP);
        } else {
          currentRRURL.removeCompParam();
          if (null != tokDic) {
            currentRRURL.setParameter(DsTokenSipConstants.s_TokParamName, tokDic.getName());
          } else {
            currentRRURL.removeParameter(DsTokenSipConstants.s_TokParamName);
          }
        }
        Log.debug("Modified Record route URL to : " + currentRRURL);
      }
    }
  }

  private void setRecordRouteInterfaceStateless(DsSipMessage msg) throws DsException {
    DsSipHeaderList rrHeaders = msg.getHeadersValidate(DsSipConstants.RECORD_ROUTE);
    boolean compress = msg.shouldCompress();
    DsTokenSipDictionary encode = msg.shouldEncode();
    if (rrHeaders != null && rrHeaders.size() > 0) {
      for (Object rrHeader : rrHeaders) {
        DsSipRecordRouteHeader recordRouteHeader = (DsSipRecordRouteHeader) rrHeader;
        setRRHelper(msg, (DsSipURL) recordRouteHeader.getURI(), compress, encode);
      }
    }
  }
}
