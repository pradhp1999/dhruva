// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponseCode;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRetryAfterHeader;
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
 * DsSipResponseCodes.DS_RESPONSE_SERVICE_UNAVAILABLE response if work is bumped from the queue. The
 * provided discard interface will be called as well for user code logging purposes.
 */
public class DsSipRequestRejectHelper extends DsSipRequestQueueHelper {
  /** The retry after header. */
  private DsSipRetryAfterHeader m_retryAfter = null;

  /**
   * Create a request discard helper. The helper uses the queue 'queue_name' if it exists; otherwise
   * it creates it.
   *
   * @param queue_name the name of the queue to use or create
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @param nworkers the number of workers to use for the request queue
   * @param discard_policy the discard policy to use (newest or oldest)
   * @param max_size the maximum size of the queue
   * @param retry_after time for the SIP retry after header
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestRejectHelper(
      String queue_name,
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      int nworkers,
      int discard_policy,
      int max_size,
      int retry_after)
      throws DsException {
    super(queue_name, request_interface, discard_interface, nworkers, discard_policy, max_size);
    constructRetryAfter(retry_after);
  }

  /**
   * Create a request discard helper. The helper uses the anonymous queue.
   *
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @param nworkers the number of workers to use for the request queue
   * @param discard_policy the discard policy to use (newest or oldest)
   * @param max_size the maximum size of the queue
   * @param retry_after time for the SIP retry after header
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestRejectHelper(
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      int nworkers,
      int discard_policy,
      int max_size,
      int retry_after)
      throws DsException {
    super(request_interface, discard_interface, nworkers, discard_policy, max_size);
    constructRetryAfter(retry_after);
  }

  /**
   * Create a request discard helper. The helper uses the queue 'queue_name' if it exists; otherwise
   * it creates it. Use default queue parameters.
   *
   * @param queue_name the name of the queue to use or create
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @param retry_after time for the SIP retry after header
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestRejectHelper(
      String queue_name,
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      int retry_after)
      throws DsException {
    super(queue_name, request_interface, discard_interface);
    constructRetryAfter(retry_after);
  }
  /**
   * Create a request discard helper. The helper uses the anonymous queue and default queue
   * parameters.
   *
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @param retry_after time for the SIP retry after header
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestRejectHelper(
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      int retry_after)
      throws DsException {
    super(request_interface, discard_interface);
    constructRetryAfter(retry_after);
  }

  /**
   * Construct the retry after header to be used for reject responses.
   *
   * @param retry_after the retry after time
   */
  public void constructRetryAfter(int retry_after) {
    m_retryAfter = new DsSipRetryAfterHeader(retry_after);
  }

  /*
   * javadoc inherited
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
     * Constructs the unit of work for the request reject helper class with the specified server
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
                DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE,
                m_serverTransaction.getRequest(),
                null,
                null);
        response.addHeader(m_retryAfter);
        m_serverTransaction.sendResponse(response);
      } catch (DsException dse) {
        if (DsLog4j.transMCat.isEnabled(Level.WARN))
          DsLog4j.transMCat.warn(
              "QueueHelperUOW.abort: Exception sending "
                  + DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE
                  + " response",
              dse);
      } catch (IOException ioe) {
        if (DsLog4j.transMCat.isEnabled(Level.WARN))
          DsLog4j.transMCat.warn(
              "QueueHelperUOW.abort: Exception sending "
                  + DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE
                  + " response",
              ioe);
      }

      // this will call the client's discard interface
      //   to inform user code that we are rejecting a request
      super.abort();
    }
  }
}
