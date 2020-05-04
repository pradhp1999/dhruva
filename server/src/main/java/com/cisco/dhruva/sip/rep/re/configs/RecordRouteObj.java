/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.re.configs;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRecordRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;

/* Created by IntelliJ IDEA.
 * User: rrachuma
 * Date: May 4, 2005
 * Time: 5:30:20 PM
 * To change this template use Options | File Templates.
 */
public class RecordRouteObj {
  private String m_direction = null;
  private String m_recordRouteAddress = null;
  private int m_recordRoutePort = -1;
  private int m_recordRouteTransport = -1;
  DsSipRecordRouteHeader m_recordRouteHeader = null;

  public RecordRouteObj(
      String direction, String recordRouteAddress, int recordRoutePort, int recordRouteTransport)
      throws DsSipParserListenerException, DsSipParserException {
    m_direction = direction;
    m_recordRouteAddress = recordRouteAddress;
    m_recordRoutePort = recordRoutePort;
    m_recordRouteTransport = recordRouteTransport;
    m_recordRouteHeader =
        new DsSipRecordRouteHeader(
            new DsByteString(
                "<sip:"
                    + m_direction
                    + "@"
                    + m_recordRouteAddress
                    + ":"
                    + m_recordRoutePort
                    + ";transport="
                    + m_recordRouteTransport
                    + ";lr>"));
  }

  public String getDirection() {
    return m_direction;
  }

  public String getRecordRouteAddress() {
    return m_recordRouteAddress;
  }

  public int getRecordRoutePort() {
    return m_recordRoutePort;
  }

  public int getRecordRouteTransport() {
    return m_recordRouteTransport;
  }

  public DsSipRecordRouteHeader getRecordRouteHeader() {
    return m_recordRouteHeader;
  }
}
