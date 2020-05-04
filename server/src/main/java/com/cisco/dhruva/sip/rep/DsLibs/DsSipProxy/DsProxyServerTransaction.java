/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.re.configs.DsControllerConfig;
import com.cisco.dhruva.sip.re.configs.ServiceRouteObj;
import com.cisco.dhruva.sip.rep.re.util.DsReConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderList;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipPathHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRecordRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipServiceRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.util.ArrayList;
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

  protected static Trace Log = Trace.getTrace(DsProxyServerTransaction.class.getName());

  protected DsProxyServerTransaction(
      DsProxyTransaction proxy, DsSipServerTransaction trans, DsSipRequest request) {
    serverTransaction = trans;
    this.proxy = proxy;

    DsSipHeaderList viaHeaders = request.getHeaders(DsSipConstants.VIA);
    if (viaHeaders != null) numVias = viaHeaders.size();

    if (Log.on && Log.isDebugEnabled()) Log.debug("Counted number of Vias: " + numVias);

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

    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering respond()");

    // send the response
    try {
      if (response != null) {

        DsSipHeaderList vias = response.getHeaders(DsSipConstants.VIA);
        int numResponseVias = vias != null ? vias.size() : 0;

        if (Log.on && Log.isDebugEnabled()) {
          Log.debug("numResponseVias=" + numResponseVias + ", numVias=" + numVias);
        }

        for (int x = numResponseVias; x > numVias; x--) {
          vias.removeFirstHeader();
        }

        if (Log.on && Log.isDebugEnabled())
          Log.debug("About to send response through SipServerTransaction");

        // replaced by the full binding info object
        // response.setNetwork(network);
        // response.getBindingInfo().setConnectionId(connId);
        response.setBindingInfo(bindingInfo);

        if (DsControllerConfig.getCurrent().doRecordRoute()) setRecordRouteInterface(response);

        if (response.getMethodID() == DsSipConstants.REGISTER && response.getResponseClass() == 2) {
          if (DsControllerConfig.getCurrent().doAddServiceRoute())
            addServiceRouteInterface(response);
          else if (DsControllerConfig.getCurrent().doModifyServiceRoute())
            modifyServiceRouteInterface(response);
        }
      }

      serverTransaction.sendResponse(response);

      this.response = response;
      if (Log.on && Log.isDebugEnabled()) Log.debug("Just sent a response");

      if (response != null && response.getResponseClass() == 2 && !okResponseSent) {
        // remove Transaction after a while even
        // if the ACK to 200 has not been received
        //	serverTransaction.setTn(60000);
        okResponseSent = true;
        if (Log.on && Log.isDebugEnabled()) Log.debug("Tn timer set for ServerTransaction");
      }

    } catch (Exception e) {
      throw new DsDestinationUnreachableException("Error sending a response", e);
    }
  }

  private void modifyServiceRouteInterface(DsSipResponse msg) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering modifyServiceRouteInterface()");
    DsSipHeaderList srList = null;

    try {
      srList = msg.getHeadersValidate(DsSipConstants.SERVICE_ROUTE);
      if (srList != null) {
        boolean compress = msg.shouldCompress();
        DsTokenSipDictionary encode = msg.shouldEncode();

        int routeIndex = pathIndexFromEnd;

        if ((routeIndex >= 0) && (routeIndex < srList.size())) {
          DsSipServiceRouteHeader srHeader = (DsSipServiceRouteHeader) srList.get(routeIndex);
          DsSipURL currentSRURL = (DsSipURL) srHeader.getNameAddress().getURI();
          setSRHelper(msg, currentSRURL, compress, encode);
        }
      }
    } catch (DsException e) {
      Log.error("error in parsing service route headers", e);
    }

    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving modifyServiceRouteInterface()");
  }

  private void addServiceRouteInterface(DsSipResponse msg) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering addServiceRouteInterface()");

    DsControllerConfig config = DsControllerConfig.getCurrent();
    ArrayList list = config.getServiceRouteAdd(bindingInfo.getNetwork().getName());
    if (list != null) {
      try {
        /*
        DsSipHeaderList pathHdrs = msg.getHeaders(DsSipConstants.PATH);
        DsSipHeaderList svcHeaders = new DsSipHeaderList(DsSipConstants.SERVICE_ROUTE);
        if (pathHdrs != null)
        {
            DsSipHeader svcHeader = null;
            DsSipHeader pathHdr = pathHdrs.getFirstHeader();
            while (pathHdr != null)
            {
                svcHeader = DsSipHeader.createHeader(DsSipConstants.BS_SERVICE_ROUTE, pathHdr.getValue());
                svcHeaders.addFirst(svcHeader);
                pathHdr = (DsSipHeader) pathHdr.getNext();
            }
        }
        */

        DsSipHeaderList pathHdrs = msg.getHeadersValidate(DsSipConstants.PATH);
        DsSipHeaderList svcHeaders = new DsSipHeaderList(DsSipConstants.SERVICE_ROUTE);
        if (pathHdrs != null) {
          DsSipServiceRouteHeader svcHeader;
          DsSipPathHeader pathHdr = (DsSipPathHeader) pathHdrs.getFirstHeader();
          while (pathHdr != null) {
            svcHeader =
                new DsSipServiceRouteHeader(pathHdr.getNameAddress(), pathHdr.getParameters());
            svcHeaders.addFirst(svcHeader);
            pathHdr = (DsSipPathHeader) pathHdr.getNext();
          }
        }

        for (int i = 0; i < list.size(); i++) {
          svcHeaders.addLast(((ServiceRouteObj) list.get(i)).getServiceRouteHeader());
        }

        if (Log.on && Log.isDebugEnabled())
          Log.debug("Adding service routes " + svcHeaders.toString());
        msg.addHeaders(svcHeaders);
      } catch (DsException e) {
        Log.error("error in parsing path headers headers", e);
      }
    }
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving addServiceRouteInterface()");
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
    if (currentRRURL != null) {
      DsControllerConfig config = DsControllerConfig.getCurrent();
      String network = null;
      String name =
          config.checkRecordRoutes(
              currentRRURL.getUser(),
              currentRRURL.getHost(),
              currentRRURL.getPort(),
              currentRRURL.getTransportParam());
      if (name != null) {
        // todo optimize when get a chance
        if (Log.on && Log.isDebugEnabled())
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
              break;
            }
            t = st.nextToken(DsReConstants.DELIMITER_STR);
          }
          currentRRURL.setUser(
              DsSipURL.getEscapedString(new DsByteString(user), DsSipURL.USER_ESCAPE_BYTES));
        } else {
          network = msg.getNetwork().getName();
        }

        if (Log.on && Log.isDebugEnabled())
          Log.debug(
              "Outgoing network of the message for which record route has to be modified : "
                  + network);
        DsSipRecordRouteHeader recordRouteInterfaceHeader =
            config.getRecordRouteInterface(network, false);

        if (recordRouteInterfaceHeader == null) {
          if (Log.on && Log.isDebugEnabled())
            Log.debug("Did not find the Record Routing Interface!");
          return;
        }

        DsSipURL recordRouteInterface = (DsSipURL) recordRouteInterfaceHeader.getURI();

        currentRRURL.setHost(recordRouteInterface.getHost());

        if (recordRouteInterface.hasPort()) {
          currentRRURL.setPort(recordRouteInterface.getPort());
        } else {
          currentRRURL.removePort();
        }

        if (recordRouteInterface.hasTransport()) {
          currentRRURL.setTransportParam(recordRouteInterface.getTransportParam());
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
        if (Log.on && Log.isDebugEnabled())
          Log.debug("Modified Record route URL to : " + currentRRURL);
      }
    }
  }

  private void setSRHelper(
      DsSipMessage msg, DsSipURL currentSRURL, boolean compress, DsTokenSipDictionary tokDic) {
    if (currentSRURL != null) {
      DsControllerConfig config = DsControllerConfig.getCurrent();
      String network = null;
      String name =
          config.checkPaths(
              currentSRURL.getUser(),
              currentSRURL.getHost(),
              currentSRURL.getPort(),
              currentSRURL.getTransportParam());
      if (name != null) {
        // todo optimize when get a chance
        DsByteString u = currentSRURL.getUser();
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
              break;
            }
            t = st.nextToken(DsReConstants.DELIMITER_STR);
          }

          /*
          if(user.startsWith(PR_STR1))
              user = SR_STR1 + user.substring(PR_STR1.length());
          else if(user.endsWith(PR_STR3))
              user = user.substring(0,user.length() - PR_STR3.length()) + SR_STR3;
          else
              user = user.replaceFirst(PR_STR2,SR_STR2);
          */

          if (user.startsWith(DsReConstants.PR_TOKEN))
            user = DsReConstants.SR_TOKEN + user.substring(DsReConstants.PR_TOKEN.length());
          else if (user.endsWith(DsReConstants.PR_TOKEN1))
            user =
                user.substring(0, user.length() - DsReConstants.PR_TOKEN1.length())
                    + DsReConstants.SR_TOKEN1;
          else user = user.replaceFirst(DsReConstants.PR_TOKEN2, DsReConstants.SR_TOKEN2);

          currentSRURL.setUser(
              DsSipURL.getEscapedString(new DsByteString(user), DsSipURL.USER_ESCAPE_BYTES));
        } else {
          network = msg.getNetwork().getName();
        }

        DsSipServiceRouteHeader serviceRouteInterfaceHeader =
            config.getServiceRouteInterface(network);

        if (serviceRouteInterfaceHeader == null) {
          if (Log.on && Log.isDebugEnabled())
            Log.debug("Did not find the Service Route Interface!");
          return;
        }

        DsSipURL serviceRouteInterface = (DsSipURL) serviceRouteInterfaceHeader.getURI();

        currentSRURL.setHost(serviceRouteInterface.getHost());

        if (serviceRouteInterface.hasPort()) {
          currentSRURL.setPort(serviceRouteInterface.getPort());
        } else {
          currentSRURL.removePort();
        }

        if (serviceRouteInterface.hasTransport()) {
          currentSRURL.setTransportParam(serviceRouteInterface.getTransportParam());
        } else {
          currentSRURL.removeTransportParam();
        }
        if (compress) {
          currentSRURL.setCompParam(DsSipConstants.BS_SIGCOMP);
        } else {
          currentSRURL.removeCompParam();
          if (null != tokDic) {
            currentSRURL.setParameter(DsTokenSipConstants.s_TokParamName, tokDic.getName());
          } else {
            currentSRURL.removeParameter(DsTokenSipConstants.s_TokParamName);
          }
        }
      }
    }
  }

  private void setRecordRouteInterfaceStateless(DsSipMessage msg) throws DsException {
    DsSipHeaderList rrHeaders = msg.getHeadersValidate(DsSipConstants.RECORD_ROUTE);
    boolean compress = msg.shouldCompress();
    DsTokenSipDictionary encode = msg.shouldEncode();
    if (rrHeaders != null && rrHeaders.size() > 0) {
      for (int headerCount = 0; headerCount < rrHeaders.size(); headerCount++) {
        DsSipRecordRouteHeader recordRouteHeader =
            (DsSipRecordRouteHeader) rrHeaders.get(headerCount);
        setRRHelper(msg, (DsSipURL) recordRouteHeader.getURI(), compress, encode);
      }
    }
  }

  /*
       private void setRecordRouteListener(DsSipRecordRouteHeader rrHeader,
                                           DsSipRecordRouteHeader listener,
                                           boolean sigcomp,
                                           DsTokenSipDictionary tokDic) {
         if (Log.on && Log.isDebugEnabled()) {
           Log.debug("Entering Listener");
         }

         if (rrHeader == null) {
           if (Log.on && Log.isDebugEnabled()) {
             Log.debug("In setRecordRouteListener, header was null, not setting anything.");
           }
           return;
         }

         DsSipURL tempURL = (DsSipURL) rrHeader.getNameAddress().getURI();
         DsSipURL listenerURL = (DsSipURL) listener.getURI();
         tempURL.setMAddrParam(listenerURL.getHost());


         if (listenerURL.hasPort()) {
           tempURL.setPort(listenerURL.getPort());
         }
         else {
           tempURL.removePort();
         }

         if (listenerURL.hasTransport()) {
           tempURL.setTransportParam(listenerURL.getTransportParam());
         }
         else {
           tempURL.removeTransportParam();
         }

         if (sigcomp) {
           tempURL.setCompParam(DsSipConstants.BS_SIGCOMP);
         }
         else {
           tempURL.removeCompParam();
           if (null != tokDic) {
             tempURL.setParameter(DsTokenSipConstants.s_TokParamName, tokDic.getName());
           }
           else {
             tempURL.removeParameter(DsTokenSipConstants.s_TokParamName);
           }
         }

         if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving setRecordRouteListener()");
       }
  */

}
