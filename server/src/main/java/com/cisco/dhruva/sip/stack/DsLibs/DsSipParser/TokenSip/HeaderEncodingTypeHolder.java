// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;

public class HeaderEncodingTypeHolder {

  // todo set defaults in the constructor
  int m_type;
  DsByteString m_name;
  boolean m_fixedFormat;
  int m_fixedFormatType;

  HeaderEncodingTypeHolder(int type, boolean format) {
    m_type = type;
    m_fixedFormat = format;
  }

  HeaderEncodingTypeHolder(int type, boolean format, int fixedFormatType) {
    m_type = type;
    m_fixedFormat = format;
    m_fixedFormatType = fixedFormatType;
  }

  HeaderEncodingTypeHolder(DsByteString name) {
    m_name = name;
    m_type = DsSipConstants.UNKNOWN_HEADER;
    m_fixedFormat = false;
  }

  public int getType() {
    return m_type;
  }

  public boolean isFixedFormat() {
    return m_fixedFormat;
  }

  public int getFixedFormatType() {
    return m_fixedFormatType;
  }

  public DsByteString getName() {
    return m_name;
  }
}
