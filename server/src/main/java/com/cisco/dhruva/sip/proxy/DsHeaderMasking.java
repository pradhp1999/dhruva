/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.config.sip.controller.MaskObj;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderList;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipViaHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.security.MessageDigest;

/**
 * Created by IntelliJ IDEA. User: rrachuma Date: Jul 25, 2005 Time: 12:58:54 PM To change this
 * template use File | Settings | File Templates.
 */
public class DsHeaderMasking {
  protected static Logger Log = DhruvaLoggerFactory.getLogger(DsHeaderMasking.class);

  private DsControllerConfig m_ControllerConfig;

  private DsSipHeaderList viaList; // list of removed vias when running statelessly

  public DsHeaderMasking(DsControllerConfig config) {
    Log.debug("Entering DsHeaderMasking constructor");

    m_ControllerConfig = config;

    Log.debug("Leaving DsHeaderMasking constructor");
  }

  public void decryptHeaders(DsSipMessage msg) {
    Log.debug("Entering decryptHeaders()");

    if (m_ControllerConfig.isMasked(msg.getNetwork().getName(), MaskObj.VIA)) {
      doViaDecryption(msg);
      Log.debug("Past via decryption");
    }

    Log.debug("Leaving decryptHeaders()");
  }

  /**
   * Method declaration
   *
   * @param msg
   * @see
   */
  public void encryptHeaders(DsSipMessage msg) {

    Log.debug("Entering encryptHeaders()");

    if (m_ControllerConfig.isMasked(msg.getNetwork().getName(), MaskObj.VIA)) {
      doViaEncryption(msg);
      Log.debug("Past via encryption");
    }

    Log.debug("Leaving encryptHeaders()");
  }

  /**
   * Method declaration
   *
   * @param msg
   * @see
   */
  private void doViaEncryption(DsSipMessage msg) {
    Log.debug("Entering doViaEncryption()");
    if (msg.isRequest() == true) {
      if (msg.getHeader(DsSipConstants.VIA) == null) {
        // This is OK.  It just means that we created the request.
        return;
      }

      if (m_ControllerConfig.isStateful()) {
        viaList = msg.getHeaders(DsSipViaHeader.sID);
        msg.removeHeaders(DsSipViaHeader.sID);

        Log.debug("removed Via list and saved: " + viaList);
      }
    }
    Log.debug("Leaving doViaEncryption()");
  }

  /**
   * Method declaration
   *
   * @param msg
   * @see
   */
  private void doViaDecryption(DsSipMessage msg) {
    Log.debug("Entering doViaDecryption()");

    Log.debug("Via Decryption on:\n" + msg);

    if (msg.isResponse() == true) {
      DsSipHeaderList headerList = null;
      try {
        headerList = msg.getHeadersValidate(DsSipViaHeader.sID);
      } catch (DsException e) {
        Log.warn("Error in parsing via headers");
        return;
      }

      if (m_ControllerConfig.isStateful()) {
        if (viaList != null) {
          Log.debug("Via Decryption: stateful");
          if (headerList != null) headerList.addAll(viaList, false, true);
          else msg.addHeaders(viaList);

          Log.debug("Via Decryption: Added Via List:" + viaList);
        }
      }
    }
    Log.debug("Leaving doViaDecryption()");
  }

  /**
   * Method declaration
   *
   * @param msg
   * @throws Exception
   * @see
   */
  protected void replaceViaDecryption(DsSipMessage msg, MessageDigest md5) throws Exception {
    Log.debug("Entering replaceViaDecryption()");

    if (msg.isResponse() == true) {
      if (m_ControllerConfig.isStateful() == true && viaList != null) {
        Log.debug("Calling doViaDecryption");
        msg.removeHeaders(DsSipViaHeader.sID);
        doViaDecryption(msg);
      }
    }
    Log.debug("Leaving replaceViaDecryption()");
  }
}
