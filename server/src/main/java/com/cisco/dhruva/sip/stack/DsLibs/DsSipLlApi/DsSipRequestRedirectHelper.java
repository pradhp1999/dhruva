// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsUnitOfWork;
import java.io.IOException;
import org.apache.logging.log4j.Level;

/**
 * This class extends DsSipRequestQueueHelper. Subclasses of DsSipRequestQueueHelper override the
 * action taken when the request queue is full.
 *
 * <p>This class will respond to the request with a
 * DsSipResponseCodes.DS_RESPONSE_SERVICE_UNAVAILABLE response if work is bumped from the queue.
 */
public class DsSipRequestRedirectHelper extends DsSipRequestQueueHelper {
  /** The contact data. */
  private String m_contactData; // = null;

  /**
   * Create a request discard helper. The helper uses the queue 'queue_name' if it exists; otherwise
   * it creates it.
   *
   * @param queue_name the name of the queue to use or create
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param nworkers the number of workers to use for the request queue
   * @param discard_policy the discard policy to use (newest or oldest)
   * @param max_size the maximum size of the queue
   * @param discard_interface the interface to invoke when the queue is full
   * @param redirect_host host for contact header
   * @param redirect_port port for contact header
   * @param redirect_protocol protocol for contact header
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestRedirectHelper(
      String queue_name,
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      int nworkers,
      int discard_policy,
      int max_size,
      String redirect_host,
      int redirect_port,
      int redirect_protocol)
      throws DsException {
    super(queue_name, request_interface, discard_interface, nworkers, discard_policy, max_size);
    constructContactData(redirect_host, redirect_port, redirect_protocol);
  }

  /**
   * Create a request discard helper. The helper uses the anonymous queue.
   *
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param nworkers the number of workers to use for the request queue
   * @param discard_policy the discard policy to use (newest or oldest)
   * @param max_size the maximum size of the queue
   * @param discard_interface the interface to invoke when the queue is full
   * @param redirect_host host for contact header
   * @param redirect_port port for contact header
   * @param redirect_protocol protocol for contact header
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestRedirectHelper(
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      int nworkers,
      int discard_policy,
      int max_size,
      String redirect_host,
      int redirect_port,
      int redirect_protocol)
      throws DsException {
    super(request_interface, discard_interface, nworkers, discard_policy, max_size);
    constructContactData(redirect_host, redirect_port, redirect_protocol);
  }
  /**
   * Create a request discard helper. The helper uses the queue 'queue_name' if it exists; otherwise
   * it creates it. Uses default queue params.
   *
   * @param queue_name the name of the queue to use or create
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @param redirect_host host for contact header
   * @param redirect_port port for contact header
   * @param redirect_protocol protocol for contact header
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestRedirectHelper(
      String queue_name,
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      String redirect_host,
      int redirect_port,
      int redirect_protocol)
      throws DsException {
    super(queue_name, request_interface, discard_interface);
    constructContactData(redirect_host, redirect_port, redirect_protocol);
  }
  /**
   * Create a request discard helper. The helper uses the anonymous queue and default queue params.
   *
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @param redirect_host host for contact header
   * @param redirect_port port for contact header
   * @param redirect_protocol protocol for contact header
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestRedirectHelper(
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      String redirect_host,
      int redirect_port,
      int redirect_protocol)
      throws DsException {
    super(request_interface, discard_interface);
    constructContactData(redirect_host, redirect_port, redirect_protocol);
  }

  /**
   * Construct the constant part of the contact header to be used.
   *
   * @param host host for contact header
   * @param port port for contact header
   * @param protocol protocol for contact header
   */
  public void constructContactData(String host, int port, int protocol) {
    // build header
    m_contactData =
        (new StringBuffer(64)
                .append(':')
                .append(port)
                .append(";maddr=")
                .append(host)
                .append(";transport=")
                .append(DsSipTransportType.getTypeAsString(protocol)))
            .append(">")
            .toString();
  }

  private DsSipContactHeader constructContactHeader(DsSipRequest request)
      throws DsSipParserException, DsException {
    StringBuffer contactValue =
        new StringBuffer(64)
            .append("<sip:")
            .append(DsByteString.toString(request.getRequestURIHost()))
            .append(m_contactData);
    return new DsSipContactHeader(new DsByteString(contactValue.toString()));
  }

  /**
   * This abstract method is overridden to create the kind of work to enqueue.
   *
   * @param transaction the transaction to wrap
   * @return the unit of work to enqueue
   */
  protected DsUnitOfWork createWork(DsSipServerTransaction transaction) {
    return new QueueHelperUOW(transaction);
  }

  /**
   * Subclasses of DsSipRequestQueueHelper.QueueHelperUOW differ in how they dispose of (abort)
   * work.
   *
   * <p>This subclass aborts work by sending a reject response.
   */
  public class QueueHelperUOW extends DsSipRequestQueueHelper.QueueHelperUOW {

    /**
     * Constructs the unit of work for the request redirect helper class with the specified server
     * transaction <code>server_trans</code>.
     *
     * @param server_trans the server transaction associated with this unit of work.
     */
    public QueueHelperUOW(DsSipServerTransaction server_trans) {
      super(server_trans);
    }

    /** Abort the unit of work by send a response and calling the supplied discard interface. */
    public void abort() {
      try {
        // the user code doesn't need to hear about the ACK
        m_serverTransaction.setInterface(null);

        DsSipResponse response =
            new DsSipResponse(
                DsSipResponseCode.DS_RESPONSE_MOVED_TEMPORARILY,
                m_serverTransaction.getRequest(),
                null,
                null);
        response.addHeader(constructContactHeader(m_serverTransaction.getRequest()));
        m_serverTransaction.sendResponse(response);
      } catch (DsException dse) {
        if (DsLog4j.transMCat.isEnabled(Level.WARN))
          DsLog4j.transMCat.warn("QueueHelperUOW.abort: Exception sending redirect", dse);
      } catch (IOException ioe) {
        if (DsLog4j.transMCat.isEnabled(Level.WARN))
          DsLog4j.transMCat.warn("QueueHelperUOW.abort: Exception sending redirect", ioe);
      }

      // this will call the client's discard interface
      //   to inform user code that we are redirecting a request
      super.abort();
    }
  }
}
