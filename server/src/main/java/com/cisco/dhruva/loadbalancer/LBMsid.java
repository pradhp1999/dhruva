/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderInterface;

/**
 * Created by IntelliJ IDEA. User: rrachumallu Date: Aug 20, 2003 Time: 1:31:31 PM To change this
 * template use Options | File Templates.
 */
public final class LBMsid extends LBHashBasedMsid {

  @Override
  public void setKey() {
    try {
      DsSipHeaderInterface msConversationIdHeader =
          request.getHeader(new DsByteString("Ms-Conversation-ID"));
      if (msConversationIdHeader == null) {
        Log.info(
            "Ms-Conversation-ID is not present in the headers, req_uri will be used for getting hash key");
        key = request.getURI().toByteString();
      } else {
        key = msConversationIdHeader.getValue();
      }
    } catch (Exception e) {
      Log.error(e.getStackTrace());
    }
  }
}
