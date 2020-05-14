/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.config.sip.controller;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipPathHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;

public class PathObj {
  private DsNetwork m_direction = null;
  private String m_pathAddress = null;
  private int m_pathPort = -1;
  private int m_pathTransport = -1;
  private DsSipPathHeader m_pathHeader = null;

  public PathObj(DsNetwork direction, String pathAddress, int pathPort, int pathTransport)
      throws DsSipParserException, DsSipParserListenerException {
    m_direction = direction;
    m_pathAddress = pathAddress;
    m_pathPort = pathPort;
    m_pathTransport = pathTransport;
    m_pathHeader =
        new DsSipPathHeader(
            new DsByteString(
                "<sip:"
                    + m_pathAddress
                    + ":"
                    + m_pathPort
                    + ";transport="
                    + m_pathTransport
                    + ";lr>"));
  }

  public DsNetwork getDirection() {
    return m_direction;
  }

  public String getPathAddress() {
    return m_pathAddress;
  }

  public int getPathPort() {
    return m_pathPort;
  }

  public int getPathTransport() {
    return m_pathTransport;
  }

  public DsSipPathHeader getPathHeader() {
    return (DsSipPathHeader) m_pathHeader.clone();
  }
}
