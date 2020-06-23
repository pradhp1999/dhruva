// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import java.io.IOException;

/**
 * Implement this interface and pass it to the transaction manager in order to get notified about
 * new requests that arrive on the network.
 */
@FunctionalInterface
public interface DsSipRequestInterface {
  /**
   * This method should be implemented to allow higher layers to receive incoming requests which
   * arrive into the transaction manager from the transport layer. When a new request arrives, the
   * transaction manager creates a server transaction for the new request and calls this interface.
   * This callback is only called for user pre-defined pre-set methods. The INVITE and BYE
   * interfaces MUST have been implemented and passed in via the Transaction Managers
   * setRequestInterface(). As request retransmissions are handled in the lower layer, the request()
   * interface method is only called once per unique request.
   *
   * <p>Unless in stateless (proxy-server) mode, the user code must respond to all requests. In
   * proxy server mode, the user must call DsSipServerTransaction.abort to remove the transaction
   * from the transaction manager.
   *
   * <p><b>THE USER DOES NOT CALL abort(), OR SEND A RESPONSE FOR UNWANTED TRANSACTIONS THE
   * TRANSACTION MANAGER WILL NOT UNMANAGE THE TRANSACTION AND A MEMORY LEAK WILL RESULT.</b>
   *
   * @param transaction the server transaction created for the new incoming request
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  void request(DsSipServerTransaction transaction);
}
