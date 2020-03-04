// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import java.util.HashMap;
import java.util.Map;

/*
  ///////////////////////////////////////////////////////////////////////////////////

   // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support

   ////////////////////////////////////
   // B=base    BT=base with To tag  //
   // V=via                          //
   // C, A, N= CANCEL, ACK, or NONE  //
   // P= PRACK                       //
   ////////////////////////////////////


   There are four tables with keys as follows:

     client: V-(B|BT)-('C'|'N')

     server: B-'N'
     merged: V-B-'N'
     cancel: V-B-'C'

     // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
     prack: V-B-'P'

  ///////////////////////////////////////////////////////////////////////////////////

    Client Side Actions
    -------------------

    1. Matching a response

    -response has To tag
        -INVITE response
            -construct V-BT-'N'
            -look in client table
                -found:
                    -pass response to client transaction
                -not found:
                    -construct V-B-'N'
                    -look in client table
                        -found:
                            -provisional response
                                -pass response to client transaction
                            -final response
                                -client transaction has no To tag
                                    -set client transaction To tag to response To tag
                                    -pass response to client transaction
                                -client transaction has To tag
                                    -client transaction To tag matches response To tag
                                        -pass response to client transaction
                                    -client transaction To tag doesn't match response To tag
                                        -multiple final response handling enabled globally
                                            -multiple final response handling enabled for  transaction
                                                -create clone of found transaction
                                                -set To tag of new transaction to response To tag
                                                -add new transaction to client table (V-BT-'N')
                                                -call multipleFinalResponse of new transaction
                                            -multiple final response handling not enabled for transaction
                                                -call finalResponse of found transaction
                                        -multiple final response handling not enabled globally
                                            -call finalResponse of found transaction
                        -not found:
                            -stray response
        -non-INVITE response
            -construct V-BT-'N'
            -look in client table
                -found:
                    -pass response to client transaction
                -not found:
                    -construct V-B-'N'
                    -look in client table
                        -found:
                            -set client transaction To tag to response To tag
                            -pass response to client transaction
                        -not found:
                            -stray response
    -response has no To tag
        -construct V-B-'N'
        -look in client table
            -found:
                -pass response to client transaction
            -not found:
                -stray response

  ///////////////////////////////////////////////////////////////////////////////////

    Server Side Actions
    -------------------

   1. Request processing (non-CANCEL, non-ACK, and non-PRACK)

     -construct V-B-'N'
     -look in merge table
           -not found:
               -construct B-'N'
               -look in server table
                   -not found: new transaction(B-'N')
                   -found:
                       -via parts match  : orig rtx
                       -via parts not match: create new merged (V-B-'N')
           -found: merge rtx

   2. ACK Processing (INVITE-Associated)

     -construct V-B-'N'
     -look in merge table
           -not found:
               -construct B-'N'
               -look in server table
                   -not found: stray ACK
                   -found: ACK for non merged request
           -found: ACK for merged request

   3. CANCEL Processing (INVITE-Associated)

     -construct V-B-'C'
     -look cancel table
           -not found: new CANCEL
           -found: CANCEL rtx
     -construct V-B-'N'
     -look in merge table
           -not found:
               -construct B-'N'
               -look in server table
                   -via parts match: found txn being cancelled
                   -via parts not match: stray cancel
           -found: found merged txn being cancelled

   4. PRACK Processing (INVITE-Associated)

     -construct V-B-'P'
     -look prack table
           -not found: new PRACK
           -found: PRACK rtx
     -construct V-B-'N'
     -look in merge table
           -not found:
               -construct B-'N'
               -look in server table
                   -via parts match: found txn being pracked
                   -via parts not match: stray prack
           -found: found merged txn being pracked

  ///////////////////////////////////////////////////////////////////////////////////

*/

/**
 * This class provides a common table for client and server transactions. It is also used to
 * construct transaction keys.
 */
public final class DsSipTransactionTable implements java.io.Serializable {
  private static final boolean m_smallMaps;

  static {
    m_smallMaps =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_SMALL_MAPS, DsConfigManager.PROP_SMALL_MAPS_DEFAULT);
  }

  /** Constructor - creates the underlying maps. */
  DsSipTransactionTable() {
    // These aren't synchronized -- instead the the accessors are
    // sizes are prime numbers - jsm

    if (m_smallMaps) {
      m_clientTransactionMap = new HashMap(8);
      m_serverTransactionMap = new HashMap(8);
      m_cancelTransactionMap = new HashMap(8);
      m_mergedTransactionMap = new HashMap(8);
      // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      m_prackTransactionMap = new HashMap(8);
    } else {
      m_clientTransactionMap = new HashMap(32768);
      m_serverTransactionMap = new HashMap(32768);
      m_cancelTransactionMap = new HashMap(256);
      m_mergedTransactionMap = new HashMap(64);
      // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      m_prackTransactionMap = new HashMap(2048);
    }
  }

  /**
   * Gets the combined client and server map sizes.
   *
   * @return the combined client and server map sizes
   */
  synchronized int size() {
    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
    return m_clientTransactionMap.size()
        + m_serverTransactionMap.size()
        + m_mergedTransactionMap.size()
        + m_cancelTransactionMap.size()
        + m_prackTransactionMap.size();
  }

  /**
   * Gets the total number of client transactions inserted for the life of the UA stack.
   *
   * @return the total number of client transactions created for the life of the UA stack.
   */
  synchronized int getTotalClientTransactionCount() {
    return m_clientTransactionsCreated;
  }

  /**
   * Gets the total number of server transactions inserted for the life of the UA stack.
   *
   * @return the total number of server transactions created for the life of the UA stack.
   */
  synchronized int getTotalServerTransactionCount() {
    return m_serverTransactionsCreated;
  }

  /**
   * Gets the current number of client transactions.
   *
   * @return the current number of client transactions
   */
  synchronized int getCurrentClientTransactionCount() {
    return m_clientTransactionMap.size();
  }

  /**
   * Gets the current number of server transaction entries.
   *
   * @return the current number of server transactions
   */
  synchronized int getCurrentServerTransactionCount() {
    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
    return m_serverTransactionMap.size()
        + m_mergedTransactionMap.size()
        + m_cancelTransactionMap.size()
        + m_prackTransactionMap.size();
  }

  // CAFFEINE 2.0 DEVELOPMENT - Adding the support for SIP Statistics counters.
  //   The design document is EDCS-306362.
  // While merging with DS latest code, "shodow" was replaced by "previous"
  /**
   * Returns the total number of client transactions that were in progress plus the client
   * transactions that had reached the terminated state at the time of last reset (performed by the
   * Administrator from the command line).
   */
  synchronized int getTotalClientTransactionCountReset() {
    return m_clientTransactionsCreatedReset;
  }

  /**
   * Returns the total number of server transactions that were in progress plus the server
   * transactions that had reached the terminated state at the time of last reset (performed by the
   * Administrator from the command line).
   */
  synchronized int getTotalServerTransactionCountReset() {
    return m_serverTransactionsCreatedReset;
  }

  /**
   * This method takes a snapshot of the "absolute" or SNMP view of the transaction count into
   * "previous" transaction count.
   */
  synchronized void takeSnapshotForTransactionCount() {
    m_serverTransactionsCreatedReset = m_serverTransactionsCreated;
    m_clientTransactionsCreatedReset = m_clientTransactionsCreated;
  }

  /**
   * Adds the server transaction to the table.
   *
   * @param key the key to use
   * @param server_transaction the server transaction to add
   */
  void addServerTransaction(DsSipTransactionKey key, DsSipServerTransaction server_transaction) {
    synchronized (m_serverMapLock) {
      ++m_serverTransactionsCreated;
      if (server_transaction.isMerged()) {
        m_mergedTransactionMap.put(key, server_transaction);
      } else {
        m_serverTransactionMap.put(key, server_transaction);
      }
    }
  }

  /**
   * Adds the transaction to the table with the key gotten from the client transaction.
   *
   * @param client_transaction the client transaction to add
   */
  void addClientTransaction(DsSipClientTransaction client_transaction) throws DsException {
    synchronized (m_clientTransactionMap) {
      DsSipTransactionKey key = client_transaction.getKey();
      DsSipClientTransaction displaced =
          (DsSipClientTransaction) m_clientTransactionMap.put(key, client_transaction);
      if (displaced != null) {
        m_clientTransactionMap.put(key, displaced);
        throw new DsException("Client transaction already in table: " + key);
      } else {
        ++m_clientTransactionsCreated;
      }
    }
  }

  /**
   * Replace a client transaction.
   *
   * @param transaction the new client transaction
   */
  void replaceClientTransaction(DsSipClientTransaction transaction) {
    synchronized (m_clientTransactionMap) {
      m_clientTransactionMap.put(transaction.getKey(), transaction);
      ++m_clientTransactionsCreated;
    }
  }

  /**
   * Adds the transaction to the table with the keys gotten from the server transaction.
   *
   * @param server_transaction the server transaction to add
   */
  void addServerTransaction(DsSipServerTransaction server_transaction) {
    addServerTransaction(server_transaction.getKey(), server_transaction);
  }

  boolean removeServerTransaction(DsSipServerTransaction st) {
    if (st.getMethodID() == DsSipConstants.CANCEL) {
      return removeCancelTransaction(st.getKey());
    }

    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
    if (st.getMethodID() == DsSipConstants.PRACK) {
      if (st.isProxyServerMode()) {
        return removeServerTransaction(st.getKey());
      }

      return removePrackTransaction(st.getKey());
    }

    if (st.isMerged()) {
      return removeMergedTransaction(st.getKey());
    }

    return removeServerTransaction(st.getKey());
  }

  DsSipServerTransaction findServerTransaction(DsSipRequest request) throws DsException {
    DsSipServerTransaction transaction = null;
    DsSipTransactionKey key = null;

    if (request == null || (null == (key = request.getKey()))) return null;

    if (request.getMethodID() == DsSipConstants.CANCEL) {
      synchronized (m_cancelTransactionMap) {
        transaction = (DsSipServerTransaction) m_cancelTransactionMap.get(key);
      }
    }
    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
    else if (request.getMethodID() == DsSipConstants.PRACK) {
      if (DsSipTransactionManager.isProxyServerMode()) {
        transaction = findMergedTransaction(key);
        if (transaction == null) {
          transaction = findServerTransaction(key);
          if (transaction != null) {
            if (!key.viaEquals(transaction.getKey())) {
              transaction = null;
            }
          }
        }
      } else {
        synchronized (m_prackTransactionMap) {
          transaction = (DsSipServerTransaction) m_prackTransactionMap.get(key);
        }
      }
    } else {
      // if we're a proxy server, we don't send merged request responses --
      //    instead the user code gets the server transaction so, we have
      //    to be able to find it again for them here in the merged table
      if (DsSipTransactionManager.isProxyServerMode()) {
        transaction = findMergedTransaction(key);
        if (transaction == null) {
          transaction = findServerTransaction(key);
          if (transaction != null) {
            if (!key.viaEquals(transaction.getKey())) {
              transaction = null;
            }
          }
        }
      } else {
        transaction = findServerTransaction(key);
      }
    }
    return transaction;
  }

  /**
   * Remove a server transaction.
   *
   * @param key the key of the server transaction to remove
   * @return <code>true</code> if the transaction existed, otherwise <code>false</code>
   */
  boolean removeServerTransaction(DsSipTransactionKey key) {
    synchronized (m_serverMapLock) {
      Object transaction = m_serverTransactionMap.remove(key);
      return (transaction != null);
    }
  }

  boolean removeCancelTransaction(DsSipTransactionKey key) {
    synchronized (m_cancelTransactionMap) {
      Object transaction = m_cancelTransactionMap.remove(key);
      return (transaction != null);
    }
  }
  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  boolean removePrackTransaction(DsSipTransactionKey key) {
    synchronized (m_prackTransactionMap) {
      Object transaction = m_prackTransactionMap.remove(key);
      return (transaction != null);
    }
  }

  boolean removeMergedTransaction(DsSipTransactionKey key) {
    synchronized (m_serverMapLock) {
      Object transaction = m_mergedTransactionMap.remove(key);
      return (transaction != null);
    }
  }

  /**
   * Try to remove a client transaction.
   *
   * @param key the transaction key constructed via <code>transactionKey</code>
   * @return <code>true</code> if the transaction existed, <code>false</code> otherwise
   */
  boolean removeClientTransaction(DsSipTransactionKey key) {
    synchronized (m_clientTransactionMap) {
      Object transaction = m_clientTransactionMap.remove(key);
      return (transaction != null);
    }
  }

  boolean removeClientTransaction(DsSipClientTransaction ct) {
    return removeClientTransaction(ct.getKey());
  }

  /**
   * Try to find a client transaction in the transaction table.
   *
   * @param key the transaction key constructed via <code>transactionKey</code>
   * @return the client transaction if found, otherwise returns null
   */
  DsSipClientTransaction findClientTransaction(DsSipTransactionKey key) {
    synchronized (m_clientTransactionMap) {
      return (DsSipClientTransaction) m_clientTransactionMap.get(key);
    }
  }

  /**
   * Try to find a server transaction in the transaction table.
   *
   * @param key the transaction key constructed via <code>transactionKey</code>
   * @return the server transaction if found, otherwise returns null
   */
  DsSipServerTransaction findServerTransaction(DsSipTransactionKey key) {
    synchronized (m_serverMapLock) {
      return (DsSipServerTransaction) m_serverTransactionMap.get(key);
    }
  }

  DsSipServerTransaction findMergedTransaction(DsSipTransactionKey key) {
    synchronized (m_serverMapLock) {
      if (m_mergedTransactionMap.size() != 0) {
        return (DsSipServerTransaction) m_mergedTransactionMap.get(key);
      }
    }
    return null;
  }

  //
  // -construct V-B-'C'
  //      -look cancel table
  //            -not found: new CANCEL
  //            -found: CANCEL rtx
  //
  DsSipServerTransaction findOrCreateCancelTransaction(
      DsSipCancelMessage cancel, DsSipTransactionFactory factory) throws DsException {
    DsSipServerTransaction transaction = null;
    // -construct V-B-'C'
    DsSipTransactionKey transactionKey = cancel.getKey();
    transactionKey.setKeyContext(
        DsSipTransactionKey.INCOMING
            | DsSipTransactionKey.USE_VIA
            | DsSipTransactionKey.USE_METHOD);
    synchronized (m_cancelTransactionMap) {
      //      -look cancel table
      transaction = (DsSipServerTransaction) m_cancelTransactionMap.get(transactionKey);

      if (transaction == null) {
        //            -not found: new CANCEL
        transaction = factory.createServerTransaction(cancel, transactionKey, null, true);
        // the user code doesn't need to hear about events on cancel server transactions
        transaction.setInterface(null);
        m_cancelTransactionMap.put(transactionKey, transaction);
      } else {
        //            -found: CANCEL rtx
      }
    }

    return transaction;
  }

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  //
  // -construct V-B-'P'
  //      -look prack table
  //            -not found: new PRACK
  //            -found: PRACK rtx
  //
  DsSipServerTransaction findOrCreatePrackTransaction(
      DsSipPRACKMessage prack, DsSipTransactionFactory factory) throws DsException {
    DsSipServerTransaction transaction = null;
    // -construct V-B-'P'
    DsSipTransactionKey transactionKey = prack.getKey();
    transactionKey.setKeyContext(
        DsSipTransactionKey.INCOMING
            | DsSipTransactionKey.USE_VIA
            | DsSipTransactionKey.USE_METHOD);
    synchronized (m_prackTransactionMap) {
      //      -look prack table
      transaction = (DsSipServerTransaction) m_prackTransactionMap.get(transactionKey);

      if (transaction == null) {
        transaction = factory.createServerTransaction(prack, transactionKey, null, true);
        m_prackTransactionMap.put(transactionKey, transaction);
      } else {
        //            -found: PRACK rtx
      }
    }

    return transaction;
  }

  //
  //      -construct V-B-'N'
  //      -look in merge table
  //            -not found:
  //                -construct B-'N'
  //                -look in server table
  //                    -not found: new transaction(B-'N')
  //                    -found:
  //                        -via parts match: orig rtx
  //                        -via parts not match: create new merged (V-B-'N')
  //            -found: merge rtx
  //
  DsSipServerTransaction findOrCreateServerTransaction(
      DsSipRequest request, DsSipTransactionFactory factory) throws DsException {
    DsSipServerTransaction transaction = null;
    //      -construct V-B-'N'
    DsSipTransactionKey transactionKey = request.getKey();
    transactionKey.setKeyContext(
        DsSipTransactionKey.INCOMING
            | DsSipTransactionKey.USE_VIA
            | (m_useRequestURI ? DsSipTransactionKey.USE_URI : DsSipTransactionKey.NONE));

    synchronized (m_serverMapLock) {
      //      -look in merge table
      transaction = findMergedTransaction(transactionKey);
      //            -not found:
      if (transaction == null) {
        //                -construct B-'N'
        // transactionKey.setUseVia(false);
        /*--
                        request.setKeyContext(false,            // use via
                                                     m_useRequestURI,  // use ruri
                                                     false);           // is lookup
        --*/
        transactionKey.setKeyContext(
            DsSipTransactionKey.INCOMING
                | (m_useRequestURI ? DsSipTransactionKey.USE_URI : DsSipTransactionKey.NONE));
        //                -look in server table
        transaction = findServerTransaction(transactionKey);
        if (transaction == null) {
          //                    -not found: new transaction(B-'N')
          transaction = factory.createServerTransaction(request, transactionKey, null, true);
          addServerTransaction(transactionKey, transaction);
        }
        //                    -found:
        else {
          if (!transactionKey.viaEquals(transaction.getKey())) {
            //                        -via parts not match: create new merged (V-B-'N')
            // transactionKey.setUseVia(true);
            /*--
                                    request.setKeyContext(true,             // use via
                                                                 m_useRequestURI,  // use ruri
                                                                 false);           // is lookup
            --*/
            transactionKey.setKeyContext(
                DsSipTransactionKey.INCOMING
                    | DsSipTransactionKey.USE_VIA
                    | (m_useRequestURI ? DsSipTransactionKey.USE_URI : DsSipTransactionKey.NONE));

            transaction =
                factory.createServerTransaction(
                    request, transactionKey, null, false); // isOriginal == false (merged)
            addServerTransaction(transactionKey, transaction);
          } else {
            //                        -via parts match: orig rtx
          }
        }
      } else {
        //            -found: merge rtx
      }
    }
    return transaction;
  }

  /**
   * Tell the table whether or not use use the request URI when constructing keys for server
   * transactions.
   *
   * @param use_req_uri if set to true the table will use the request URI when constructing server
   *     transaction keys
   */
  public static void setUseRequestURI(boolean use_req_uri) {
    m_useRequestURI = use_req_uri;
  }

  /**
   * Returns the use request URI value.
   *
   * @return whether or not the RURI should be used as part of the transaction key
   */
  public static boolean getUseRequestURI() {
    return m_useRequestURI;
  }

  /**
   * Re-map the client transaction associated with this key.
   *
   * @param old_key the current key for the transaction
   * @return <code>true</code> if there was a transaction associated with 'old_key', otherwise
   *     returns <code>false</code>
   */
  synchronized boolean remapClientTransaction(DsSipTransactionKey old_key) {
    boolean ret_value = false;
    DsSipClientTransaction client_transaction =
        (DsSipClientTransaction) m_clientTransactionMap.remove(old_key);
    if (client_transaction == null) {
      return false;
    } else {
      ret_value = true;
    }
    m_clientTransactionMap.put(client_transaction.getKey(), client_transaction);
    return ret_value;
  }

  /*--
      private void startSizeDebugMonitor()
      {
          new Thread()
              {
                  public void run()
                  {
                      while(true)
                      {
                          System.out.println("current client transaction count = " + getCurrentClientTransactionCount());
                          System.out.println("current server transaction count = " + getCurrentServerTransactionCount());
                          try {
                              Thread.sleep(1000);
                          } catch (Exception exc) {}
                      }
                  }
              }.start();
      }
  --*/
  //    /**
  //     * Debug the contents of the transaction tables
  //     *
  //     */
  /*--
      private void startContentDebugMonitor()
      {
          new Thread()
              {
                  public void run()
                  {
                      while(true)
                      {
                          {
                              java.util.Iterator values_it = m_clientTransactionMap.values().iterator();
                              System.out.println("Client Transactions: ");
                              System.out.println("<><><><><><><><><><>");
                              while(values_it.hasNext())
                              {
                                  System.out.println();
                                  System.out.println(values_it.next());
                                  System.out.println();
                              }
                              values_it = m_serverTransactionMap.values().iterator();
                              System.out.println("Server Transactions: ");
                              System.out.println("<><><><><><><><><><>");
                              while(values_it.hasNext())
                              {
                                  System.out.println();
                                  System.out.println(values_it.next());
                                  System.out.println();
                              }
                          }
                          try {
                              Thread.sleep(3000);
                          } catch (Exception exc) {}
                      }
                  }
              }.start();
      }
  */

  /**
   * Dump the content of transaction table. Useful for debugging.
   *
   * @return a debug string
   * @deprecated This method is for debugging purpose only and is unsupported.
   */
  public String dump() {
    StringBuffer buf = new StringBuffer(128);
    buf.append("---------------------------------------------------\n");
    buf.append("-- DsSipTransactionTable.dump  --------------------\n");
    buf.append("---------------------------------------------------\n");
    synchronized (this) {
      java.util.Iterator values_it = m_clientTransactionMap.values().iterator();
      buf.append("Client Transactions: \n");
      buf.append("<><><><><><><><><><>\n");
      while (values_it.hasNext()) {
        buf.append(((DsSipTransaction) values_it.next()).getAsString());
        buf.append("\n");
      }

      values_it = m_serverTransactionMap.values().iterator();
      buf.append("Server Transactions: \n");
      buf.append("<><><><><><><><><><>\n");
      while (values_it.hasNext()) {
        buf.append(((DsSipTransaction) values_it.next()).getAsString());
        buf.append("\n");
      }

      values_it = m_mergedTransactionMap.values().iterator();
      buf.append("Merged Server Transactions: \n");
      buf.append("<><><><><><><><><><>\n");
      while (values_it.hasNext()) {
        buf.append(((DsSipTransaction) values_it.next()).getAsString());
        buf.append("\n");
      }

      values_it = m_cancelTransactionMap.values().iterator();
      buf.append("Cancel Server Transactions: \n");
      buf.append("<><><><><><><><><><>\n");
      while (values_it.hasNext()) {
        buf.append(((DsSipTransaction) values_it.next()).getAsString());
        buf.append("\n");
      }

      // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      values_it = m_prackTransactionMap.values().iterator();
      buf.append("Prack Server Transactions: \n");
      buf.append("<><><><><><><><><><>\n");
      while (values_it.hasNext()) {
        buf.append(((DsSipTransaction) values_it.next()).getAsString());
        buf.append("\n");
      }

      buf.append("current client transaction count = " + getCurrentClientTransactionCount() + "\n");
      buf.append("current server transaction count = " + getCurrentServerTransactionCount() + "\n");
    }

    String ret = buf.toString();

    return ret;
  }

  private static boolean m_useRequestURI; // = false;

  private Map m_clientTransactionMap; // = null;
  private Map m_serverTransactionMap; // = null;
  private Map m_cancelTransactionMap; // = null;
  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  private Map m_prackTransactionMap; // = null;

  // keep the merged transaction separate so we can optimize
  //  for the case where the number of merged transactions is 0
  private Map m_mergedTransactionMap; // = null;

  /** The object to lock on for server map access. */
  private transient Object m_serverMapLock = new Object();

  private int m_clientTransactionsCreated; // = 0;
  private int m_serverTransactionsCreated; // = 0;

  // qfang - 02.02.06 - These two counters were added as part of statistics
  // enhancement in caffeine 1.0 (EDCS-306362), which correspond to the
  // so-called "Shadow" counters. However, DS original stack changed the
  // meaning of "previous" counters to essentially that of Caffeine's "Shadow"
  // as the result of merge effort, following counters are considered "previous"
  // counter.
  /* The number of transactions up till last snapshot */
  private int m_clientTransactionsCreatedReset = 0;
  private int m_serverTransactionsCreatedReset = 0;
}
