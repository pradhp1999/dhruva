// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;

public class DsTokenSipParamEncoder {
  boolean m_Quoted = false;
  DsByteString m_prependValue = DsSipConstants.BS_SEMI;

  DsTokenSipParamEncoder(int flags) {
    if ((flags & DsTokenSipConstants.TOKEN_SIP_PARAM_PREPEND_SEMI)
        == DsTokenSipConstants.TOKEN_SIP_PARAM_PREPEND_SEMI) {
      m_prependValue = DsSipConstants.BS_SEMI;
    } else if ((flags & DsTokenSipConstants.TOKEN_SIP_PARAM_PREPEND_COMMA)
        == DsTokenSipConstants.TOKEN_SIP_PARAM_PREPEND_COMMA) {
      m_prependValue = DsSipConstants.BS_COMMA;
    } else if ((flags & DsTokenSipConstants.TOKEN_SIP_PARAM_PREPEND_QUESTION)
        == DsTokenSipConstants.TOKEN_SIP_PARAM_PREPEND_QUESTION) {
      m_prependValue = DsSipConstants.BS_QUESTION;
    } else {
      m_prependValue = DsByteString.BS_EMPTY_STRING;
    }

    if ((flags & DsTokenSipConstants.TOKEN_SIP_PARAM_QUOTED)
        == DsTokenSipConstants.TOKEN_SIP_PARAM_QUOTED) {
      m_Quoted = true;
    }
  }

  boolean isQuoted() {
    return m_Quoted;
  }

  DsByteString getPrependValue() {
    return m_prependValue;
  }
}
