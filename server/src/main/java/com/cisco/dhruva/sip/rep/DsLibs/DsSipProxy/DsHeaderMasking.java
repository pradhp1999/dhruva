/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.re.configs.DsControllerConfig;
import com.cisco.dhruva.sip.re.configs.MaskObj;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderList;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipViaHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.log.Trace;
import java.security.MessageDigest;
import org.apache.logging.log4j.Level;

/**
 * Created by IntelliJ IDEA. User: rrachuma Date: Jul 25, 2005 Time: 12:58:54 PM To change this
 * template use File | Settings | File Templates.
 */
public class DsHeaderMasking {
  protected static Trace Log = Trace.getTrace(DsHeaderMasking.class.getName());

  private DsControllerConfig m_ControllerConfig;

  private DsSipHeaderList viaList; // list of removed vias when running statelessly

  public DsHeaderMasking(DsControllerConfig config) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering DsHeaderMasking constructor");

    m_ControllerConfig = config;

    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving DsHeaderMasking constructor");
  }

  public void decryptHeaders(DsSipMessage msg) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering decryptHeaders()");

    if (m_ControllerConfig.isMasked(msg.getNetwork().getName(), MaskObj.VIA)) {
      doViaDecryption(msg);
      if (Log.on && Log.isDebugEnabled()) Log.debug("Past via decryption");
    }

    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving decryptHeaders()");
  }

  /**
   * Method declaration
   *
   * @param msg
   * @see
   */
  public void encryptHeaders(DsSipMessage msg) {

    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering encryptHeaders()");

    if (m_ControllerConfig.isMasked(msg.getNetwork().getName(), MaskObj.VIA)) {
      doViaEncryption(msg);
      if (Log.on && Log.isDebugEnabled()) Log.debug("Past via encryption");
    }

    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving encryptHeaders()");
  }

  /**
   * Method declaration
   *
   * @param msg
   * @see
   */
  private void doViaEncryption(DsSipMessage msg) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering doViaEncryption()");
    if (msg.isRequest() == true) {
      if (msg.getHeader(DsSipConstants.VIA) == null) {
        // This is OK.  It just means that we created the request.
        return;
      }

      if (m_ControllerConfig.isStateful()) {
        viaList = msg.getHeaders(DsSipViaHeader.sID);
        msg.removeHeaders(DsSipViaHeader.sID);

        if (Log.on && Log.isDebugEnabled())
          if (Log.isDebugEnabled()) Log.debug("removed Via list and saved: " + viaList);
      }
    }
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving doViaEncryption()");
  }

  /**
   * Method declaration
   *
   * @param msg
   * @see
   */
  private void doViaDecryption(DsSipMessage msg) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering doViaDecryption()");

    if (Log.on && Log.isDebugEnabled())
      if (Log.isDebugEnabled()) Log.debug("Via Decryption on:\n" + msg);

    if (msg.isResponse() == true) {
      DsSipHeaderList headerList = null;
      try {
        headerList = msg.getHeadersValidate(DsSipViaHeader.sID);
      } catch (DsException e) {
        if (Log.on && Log.isEnabled(Level.WARN)) Log.warn("Error in parsing via headers");
        return;
      }

      if (m_ControllerConfig.isStateful()) {
        if (viaList != null) {
          if (Log.on && Log.isDebugEnabled()) Log.debug("Via Decryption: stateful");
          if (headerList != null) headerList.addAll(viaList, false, true);
          else msg.addHeaders(viaList);

          if (Log.on && Log.isDebugEnabled())
            if (Log.isDebugEnabled()) Log.debug("Via Decryption: Added Via List:" + viaList);
        }
      }
    }
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving doViaDecryption()");
  }

  /**
   * Method declaration
   *
   * @param msg
   * @throws Exception
   * @see
   */
  protected void replaceViaDecryption(DsSipMessage msg, MessageDigest md5) throws Exception {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering replaceViaDecryption()");

    if (msg.isResponse() == true) {
      if (m_ControllerConfig.isStateful() == true && viaList != null) {
        if (Log.on && Log.isDebugEnabled()) Log.debug("Calling doViaDecryption");
        msg.removeHeaders(DsSipViaHeader.sID);
        doViaDecryption(msg);
      }
    }
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving replaceViaDecryption()");
  }
}
