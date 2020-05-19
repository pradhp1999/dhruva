/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;

/**
 * Created by IntelliJ IDEA. User: rrachumallu Date: Aug 20, 2003 Time: 1:31:31 PM To change this
 * template use Options | File Templates.
 */
public final class LBTo extends LBHashBased {

  public LBTo() {}

  protected void setKey() {
    try {
      key = request.getToHeaderValidate().getURI().toByteString();
    } catch (DsSipParserException | DsSipParserListenerException e) {
      // e.printStackTrace();  //To change body of catch statement use Options | File Templates.
    }
  }
}
