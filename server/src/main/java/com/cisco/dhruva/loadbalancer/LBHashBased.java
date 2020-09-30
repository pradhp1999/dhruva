/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME: $RCSfile: LBHashBased.java,v $
//
// MODULE:  loadbalancer
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
//
// MODIFICATIONS:
//
//
//////////////////////////////////////////////////////////////////////////////
package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.util.log.Trace;
import java.util.ArrayList;

/**
 * <p>This class implements the Hash-Based load balancer.<br>
 * The element with the highest q-value is selected as the next hop.
 * If multiple elements have the same highest q-value, a hash algorithm
 * using the request uri as the key is performed to ramdomly but deterministically
 * chose one of the <em>n</em> elements that have the same q-value.
 * If the chosen element is a <code>ServerGroupPlaceholder</code>, a new
 * <code>LBInterface is internally created for that sub server group and the process
 * is recursively repeated until a <code>NextHop</code> is chosen.
 */
public class LBHashBased extends LBBase {

  protected static Trace Log = Trace.getTrace(LBHashBased.class.getName());

  protected final ServerGroupElementInterface selectElement(DsByteString varKey) {
    ServerGroupElementInterface selectedElement = null;
    ArrayList list = new ArrayList();
    float highestQ = -1;
    for (Object value : domainsToTry) {
      ServerGroupElementInterface sge = (ServerGroupElementInterface) value;
      if (Float.compare(highestQ, -1) == 0) {
        highestQ = sge.getQValue();
        list.add(sge);
      } else if (Float.compare(sge.getQValue(), highestQ) == 0) {
        list.add(sge);
      } else break;
    }
    StringBuffer output = new StringBuffer();
    if (Log.on && Log.isInfoEnabled()) {
      for (Object o : list) {
        output.append(o.toString());
        output.append(", ");
      }
      Log.info("list of elements in order on which load balancing is done : " + output);
    }

    if (list.size() == 1) selectedElement = (ServerGroupElementInterface) list.get(0);
    else {

      DsByteString hashKey = (varKey != null) ? varKey : key;

      if (Log.on && Log.isInfoEnabled()) Log.info("Hashing on " + hashKey);
      int index = DsHashAlgorithm.selectIndex(hashKey, list.size());
      if (index != -1) {
        if (Log.on && Log.isInfoEnabled()) Log.info("Index selected " + index);
        selectedElement = (ServerGroupElementInterface) list.get(index);
      }
    }
    return selectedElement;
  }

  protected final ServerGroupElementInterface selectElement() {
    return selectElement(null);
  }

  protected void setKey() {
    key = request.getURI().toByteString();
  }
}
