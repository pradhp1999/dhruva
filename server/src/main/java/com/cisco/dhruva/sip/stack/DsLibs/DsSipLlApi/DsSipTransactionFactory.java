// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransactionKey;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

/**
 * This interface is used to control the type of transaction created throughout the UA stack. The
 * default factory (DsSipDefaultTransactionFactory) simply creates objects of type
 * DsSipClientTransaction and DsSipServerTransaction.
 *
 * @see DsSipDefaultTransactionFactory
 */
public interface DsSipTransactionFactory {
  /**
   * Factory method for creating a server transaction.
   *
   * @param request the incoming request
   * @param keyWithVia the transaction key constructed with the Via or null if this key should be
   *     calculated here
   * @param keyNoVia the transaction key constructed without the Via or null if this key should be
   *     calculated here
   * @param isOriginal a boolean indicate whether this transaction is the the one created for the
   *     original request or whether it was created for a merged request
   * @return the new server transaction
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  DsSipServerTransaction createServerTransaction(
      DsSipRequest request,
      DsSipTransactionKey keyWithVia,
      DsSipTransactionKey keyNoVia,
      boolean isOriginal)
      throws DsException;

  /**
   * Factory method for a client transaction.
   *
   * @param request Handle of message to be sent to server.
   * @param clientInterface Optional callback interface to user-level callbacks.
   * @param transactionParams Optional. Reserved for future use.
   * @return the new client transaction
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  DsSipClientTransaction createClientTransaction(
      DsSipRequest request,
      DsSipClientTransactionInterface clientInterface,
      DsSipTransactionParams transactionParams)
      throws DsException;

  /**
   * Factory method for a client transaction.
   *
   * @param request Handle of message to be sent to server.
   * @param clientInterface Optional callback interface to user-level callbacks.
   * @param clientTransportInfo If the client wishes to use transport information other than that
   *     held by transport layer, DsSipClientTransportInfo is implemented and passed to this
   *     constructor
   * @return the new client transaction
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  DsSipClientTransaction createClientTransaction(
      DsSipRequest request,
      DsSipClientTransportInfo clientTransportInfo,
      DsSipClientTransactionInterface clientInterface)
      throws DsException;
}
