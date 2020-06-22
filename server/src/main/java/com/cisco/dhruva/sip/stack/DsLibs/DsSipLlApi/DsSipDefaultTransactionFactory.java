// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransactionKey;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

/**
 * This class is the default transaction factory. The transaction manager uses a factory to create
 * client and server transactions. If the user does not set a factory via
 * DsSipTransactionManger.setTransactionFactory, this default factory will be used.
 */
public class DsSipDefaultTransactionFactory implements DsSipTransactionFactory {
  /**
   * Constructs the default implementation of the transaction factory to create client and server
   * transactions.
   *
   * <p>This constructor sets the transaction interface factory to null. In this case the client and
   * server transactions will use their default client and server transaction interfaces (which, for
   * the most part, simply print log statements).
   */
  public DsSipDefaultTransactionFactory() {
    m_interfaceFactory = null;
  }

  /**
   * Constructs the default implementation of the transaction factory to create client and server
   * transactions.
   *
   * @param factory This DsSipTransactionInterface factory is used to create client and server
   *     transaction interfaces. Client and server transaction interfaces are used to the notify the
   *     user code of events of interest.
   */
  public DsSipDefaultTransactionFactory(DsSipTransactionInterfaceFactory factory) {
    m_interfaceFactory = factory;
  }

  /*
   * javadoc inherited
   */
  public DsSipServerTransaction createServerTransaction(
      DsSipRequest request,
      DsSipTransactionKey keyWithVia,
      DsSipTransactionKey keyNoVia,
      boolean isOriginal)
      throws DsException {
    DsSipServerTransactionInterface callback =
        (m_interfaceFactory == null)
            ? null
            : m_interfaceFactory.createServerTransactionInterface(request);

    if (request.getMethodID() == DsSipConstants.INVITE) {
      return new DsSipServerTransactionIImpl(
          request, keyWithVia, keyNoVia, callback, null, isOriginal);
    } else {
      return new DsSipServerTransactionImpl(
          request, keyWithVia, keyNoVia, callback, null, isOriginal);
    }
  }

  /*
   * javadoc inherited
   */
  public DsSipClientTransaction createClientTransaction(
      DsSipRequest request,
      DsSipClientTransportInfo clientTransportInfo,
      DsSipClientTransactionInterface clientInterface)
      throws DsException {
    if (request.getMethodID() == DsSipConstants.INVITE) {
      return new DsSipClientTransactionIImpl(request, clientTransportInfo, clientInterface);
    } else {
      return new DsSipClientTransactionImpl(request, clientTransportInfo, clientInterface);
    }
  }

  /**
   * Set the transaction interface factory.
   *
   * @param factory This DsSipTransactionInterface factory is used to create client and server
   *     transaction interfaces. Client and server transaction interfaces are used to the notify the
   *     user code of events of interest.
   */
  public synchronized void setTransactionInterfaceFactory(
      DsSipTransactionInterfaceFactory factory) {
    m_interfaceFactory = factory;
  }

  private DsSipTransactionInterfaceFactory m_interfaceFactory;
}
