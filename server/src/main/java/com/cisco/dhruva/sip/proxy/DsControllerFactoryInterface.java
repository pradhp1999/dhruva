/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsSipProxyManager.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

/**
 * The interface implementation is called by the ProxyManager whenever it's notified of a new
 * request. The ProxyController returned is used to control the processing of this transaction
 */

// MEETPASS
@FunctionalInterface
public interface DsControllerFactoryInterface {
  /**
   * Called by ProxyManager whenever it receives a new request, i.e., whenever a new transaction has
   * been created by the TransactionManager. This _excludes_ transactions created due to merged
   * requests
   *
   * @param request request being processed
   */
  DsControllerInterface getController(DsSipServerTransaction server,
                                      DsSipRequest request,
                                      ProxyAdaptorFactoryInterface pf,
                                      AppInterface app);
}
