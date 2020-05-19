/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.controller.util;

import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.sip.DsUtil.DsReConstants;
import com.cisco.dhruva.sip.controller.AppParamsInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipNameAddressHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsURI;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ParseProxyParamUtil {

  // Order in which the transport is selected.
  private static final Transport Transports[] = {Transport.TLS, Transport.TCP, Transport.UDP};
  protected static final Trace LOG = Trace.getTrace(ParseProxyParamUtil.class.getName());

  private ParseProxyParamUtil() {}

  public static AppParamsInterface getAppParamsInterface(DsSipRequest request) {

    return () -> {
      try {
        return getParsedProxyParams(
            request, DsReConstants.MY_URI, false, DsReConstants.DELIMITER_STR);
      } catch (DsException e) {
        LOG.error("Unable to get parsed proxy params for MY_URI.", e);
      }
      return null;
    };
  }

  public static Map<String, String> getParsedProxyParams(
      DsSipRequest request, int type, boolean decompress, String delimiter) throws DsException {

    DsByteString userPortion = null;
    DsSipNameAddressHeader header = null;
    switch (type) {
      case DsReConstants.MY_URI:
        userPortion = getUserPortionFromUri(request.lrFix(null));
        break;
      case DsReConstants.R_URI:
        userPortion = getUserPortionFromUri(request.getURI());
        break;
      case DsReConstants.P_A_ID:
        header =
            (DsSipNameAddressHeader) request.getHeaderValidate(DsSipConstants.P_ASSERTED_IDENTITY);
        if (header != null) {
          userPortion = getUserPortionFromUri(header.getURI());
        }
        break;
      case DsReConstants.ROUTE:
        header = (DsSipNameAddressHeader) request.getHeaderValidate(DsSipConstants.ROUTE);
        if (header != null) {
          userPortion = getUserPortionFromUri(header.getURI());
        }
        break;
      default:
        break;
    }

    if (userPortion == null) {
      return null;
    }
    if (decompress) {
      userPortion = CompressorUtil.deCompress(userPortion);
    }
    HashMap<String, String> parsedProxyParams = new HashMap<>();
    String nameValue;
    StringTokenizer st = new StringTokenizer(userPortion.toString(), delimiter);
    while (st.hasMoreTokens()) {
      nameValue = st.nextToken();
      parseNameValue(nameValue, parsedProxyParams);
    }
    return parsedProxyParams;
  }

  private static void parseNameValue(String nameValue, HashMap<String, String> params) {
    int i = nameValue.indexOf(DsReConstants.EQUAL_CHAR);
    if (i >= 0) {
      String name = nameValue.substring(0, i);
      String value = nameValue.substring(i + 1);
      params.put(name, value);
    } else {
      params.put(nameValue, nameValue);
    }
  }

  private static DsByteString getUserPortionFromUri(DsURI uri) throws DsException {
    if (uri == null) {
      return null;
    }
    DsByteString userPortion = null;
    if (uri.isSipURL()) {
      userPortion = ((DsSipURL) uri).getUser();
    } else {
      userPortion = uri.getSchemeData();
    }
    return DsSipURL.getUnescapedString(userPortion);
  }

  public static Transport getNetworkTransport(DsNetwork network) {
    Transport networkTransport = Transport.NONE;
    for (int i = 0; i < Transports.length; i++) {
      if (network != null
          && DsControllerConfig.getCurrent().getInterface(Transports[i], network) != null) {
        networkTransport = Transports[i];
        break;
      }
    }
    return networkTransport;
  }
}
