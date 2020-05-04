/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

/**
 * The interface implementation is called by the ProxyManager whenever it's notified of a new
 * request. The ProxyController returned is used to control the processing of this transaction
 */
public interface DsControllerFactoryInterface {
  /**
   * Called by ProxyManager whenever it receives a new request, i.e., whenever a new transaction has
   * been created by the TransactionManager. This _excludes_ transactions created due to merged
   * requests
   *
   * @param request request being processed
   */
  public DsControllerInterface getController(DsSipServerTransaction server, DsSipRequest request);
}
