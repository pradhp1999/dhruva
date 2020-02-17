// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.IOException;
import java.util.Map;
import org.apache.logging.log4j.Level;

/**
 * This class is the base class for request queue helper classes. It provides a convenient way for
 * an application to enqueue new incoming requests by inserting an instance of this class between
 * user code and the transaction manager. After it is constructed, the helper should be registered
 * with the transaction manager. When the helper receives a new server transactions from the
 * transaction manager, it enqueues the server transactions in a work queue. A configurable number
 * of worker threads service the queue by calling the supplied DsSipRequestInterface. If the work
 * queue grows beyond its configured maximum size, the helper will perform the abort action defined
 * by the class. The abort action for the base class is to call an alternate DsSipRequestInterface.
 *
 * <p>Instances of this class can be formed into groups which share queues. A group is formed by
 * using a common queue name in the constructor. If the queue is found, it is used, otherwise it is
 * created.
 */
public abstract class DsSipRequestQueueHelper implements DsSipRequestInterface {
  /** The default number of worker threads. */
  public static final int DEFAULT_WORKERS = 20;

  /** The default max queue length. */
  public static final int DEFAULT_QUEUE_LENGTH = 2000;

  /** if the queue is full, send response for the oldest request. */
  public static final short DISCARD_OLDEST = DsWorkQueue.DISCARD_OLDEST;

  /** if the queue is full, send response for newest request. */
  public static final short DISCARD_NEWEST = DsWorkQueue.DISCARD_NEWEST;

  /** the request interface to forward requests to. */
  protected DsSipRequestInterface m_requestInterface;

  /** when we discard something this is how to tell the user. */
  protected DsSipRequestInterface m_discardInterface;

  /** The work queue to use. */
  private DsWorkQueue m_workQueue;
  /** The name of the work queue. */
  private String m_queueName;

  /** if the user doesn't specify a work queue by name, use this one. */
  private static DsWorkQueue m_defaultWorkQueue; // = null;

  /** if <code>true</code>, the user code is telling us we're overloaded. */
  private boolean m_overloaded; // = false;

  /** <code>true</code> to bypass the queue completely. */
  private boolean m_bypassQueue; // = false;

  /**
   * Create a request queue helper. The helper uses the queue 'queue_name' if it exists; otherwise
   * it creates it.
   *
   * @param queue_name the name of the queue to use or create
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @param nworkers the number of workers to use for the request queue
   * @param discard_policy the discard policy to use (newest or oldest)
   * @param max_size the maximum size of the queue
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestQueueHelper(
      String queue_name,
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      int nworkers,
      int discard_policy,
      int max_size)
      throws DsException {
    if (request_interface == null) {
      throw new DsException("The supplied DsSipRequestInterface is null");
    }
    m_requestInterface = request_interface;
    m_discardInterface = discard_interface;

    // get the global queue map and lock
    Map queue_table = null;
    synchronized (queue_table = DsWorkQueue.getQueueTable()) {
      if (queue_name == null) {
        if (m_defaultWorkQueue == null) {
          m_defaultWorkQueue = (DsWorkQueue) queue_table.get(DsWorkQueue.REQUEST_IN_QNAME);

          // if the default queue hasn't been created
          if (m_defaultWorkQueue == null) {
            // create it  (it puts itself in the map)
            validateQueueParams(nworkers, discard_policy, max_size);
            m_defaultWorkQueue = new DsWorkQueue(DsWorkQueue.REQUEST_IN_QNAME, max_size, nworkers);
            m_defaultWorkQueue.setDiscardPolicy((short) discard_policy);
          }
          DsConfigManager.registerQueue((DsQueueInterface) m_defaultWorkQueue);
        }
        m_queueName = DsWorkQueue.REQUEST_IN_QNAME;
        m_workQueue = m_defaultWorkQueue;
      } else // the user has specified a queue name
      {
        m_queueName = queue_name;
        m_workQueue = (DsWorkQueue) queue_table.get(queue_name);

        // if the default queue hasn't been created
        if (m_workQueue == null) {
          // create it  (it puts itself in the map)
          validateQueueParams(nworkers, discard_policy, max_size);
          m_workQueue = new DsWorkQueue(queue_name, max_size, nworkers);
          DsConfigManager.registerQueue((DsQueueInterface) m_workQueue);
          m_workQueue.setDiscardPolicy((short) discard_policy);
        }
      }
    }
  }

  /**
   * Create a request queue helper. The helper uses the anonymous queue.
   *
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @param nworkers the number of workers to use for the request queue
   * @param discard_policy the discard policy to use (newest or oldest)
   * @param max_size the maximum size of the queue
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestQueueHelper(
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface,
      int nworkers,
      int discard_policy,
      int max_size)
      throws DsException {
    this(null, request_interface, discard_interface, nworkers, discard_policy, max_size);
  }

  /**
   * Create a request queue helper. The helper uses the queue 'queue_name' if it exists; otherwise
   * it creates it. It uses default queue params.
   *
   * @param queue_name the name of the queue to use or create
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestQueueHelper(
      String queue_name,
      DsSipRequestInterface request_interface,
      DsSipRequestInterface discard_interface)
      throws DsException {
    this(
        queue_name,
        request_interface,
        discard_interface,
        DEFAULT_WORKERS,
        DISCARD_NEWEST,
        DEFAULT_QUEUE_LENGTH);
  }

  /**
   * Create a request queue helper. The helper uses an anonymous queue and default queue parameters.
   *
   * @param request_interface this request interface will be called for all server transaction
   *     received from the transaction manager.
   * @param discard_interface the interface to invoke when the queue is full
   * @throws DsException if the specified <code>request_interface</code> is null
   */
  public DsSipRequestQueueHelper(
      DsSipRequestInterface request_interface, DsSipRequestInterface discard_interface)
      throws DsException {
    this(
        null,
        request_interface,
        discard_interface,
        DEFAULT_WORKERS,
        DISCARD_NEWEST,
        DEFAULT_QUEUE_LENGTH);
  }

  /**
   * Used in the constructor to validate the queue parameters.
   *
   * @throws DsException if the queue params are invalid
   */
  private void validateQueueParams(int nworkers, int discard_policy, int max_size)
      throws DsException {
    if (nworkers < 0) {
      throw new DsException("Invalid number of workers [" + nworkers + "], should be > 0");
    }
    if ((discard_policy != DISCARD_NEWEST) && (discard_policy != DISCARD_OLDEST)) {
      throw new DsException(
          "Invalid discard policy ["
              + discard_policy
              + "], should be "
              + DISCARD_NEWEST
              + " or "
              + DISCARD_OLDEST);
    }
    if (max_size < 0) {
      throw new DsException("Invalid max size [" + max_size + "], should be > 0");
    }
  }

  /**
   * Sets the overload state.
   *
   * @param overloaded if the client wishes to indicate that it is overloaded set to <code>true
   *     </code> otherwise set to <code>false</code>
   */
  public void setOverloaded(boolean overloaded) {
    m_overloaded = overloaded;
  }

  /**
   * Sets the bypass queue state.
   *
   * @param bypass if set to <code>true</code> the queue is completely bypassed. Behaves like a
   *     queue of length zero.
   */
  public void setBypassQueue(boolean bypass) {
    m_bypassQueue = bypass;
  }

  /**
   * This is the interface invoked by the transaction manager. If the queue is full, the unit of
   * work is aborted. Abort is defined by subclasses of QueueHelperUOW.
   *
   * @param server_transaction the new server transaction created by the transaction manager
   */
  public void request(DsSipServerTransaction server_transaction) {
    DsUnitOfWork uow = createWork(server_transaction);
    if (m_bypassQueue) {
      uow.process();
    } else {
      if ((uow = m_workQueue.nqueue(uow)) != null) {
        // if something fell off of the queue, send the provided response code
        if (DsLog4j.transMCat.isEnabled(Level.INFO))
          DsLog4j.transMCat.log(Level.INFO, "queue is full; aborting a unit of work");
        uow.abort();
      }
    }
  }

  /**
   * Overridden to create the kind of work to enqueue.
   *
   * @param transaction the transaction to wrap
   * @return the unit of work to enqueue
   */
  protected DsUnitOfWork createWork(DsSipServerTransaction transaction) {
    return new QueueHelperUOW(transaction);
  }

  /**
   * Returns the maximum size of the Queue. Its the size beyond which the implementing Queue will
   * start dropping the queue elements.
   *
   * @return the maximum size of the Queue.
   */
  public int getQueueMaxSize() {
    return m_workQueue.getMaxSize();
  }

  /**
   * Sets the maximum size of the implementing Queue. Its the size beyond which the implementing
   * Queue will start dropping the queue elements.
   *
   * @param max_size the max size of the queue
   */
  public void setQueueMaxSize(int max_size) {
    m_workQueue.setMaxSize(max_size);
  }

  /**
   * Sets the threshold value(in percents, relative to the queue maximum size) of the Queue. Its the
   * size when reached, the user should be notified so that he/she can either increase the queue
   * maximum size limit or should be ready to expect queue element drop offs once the queue maximum
   * size is reached.
   *
   * @param pct the threshold size of the queue.
   */
  public void setQueueThreshold(int pct) {
    m_workQueue.setThresholdSize(pct);
  }

  /**
   * Returns the percent threshold size of the Queue. Its the size when reached, the user should be
   * notified so that he/she can either increase the queue maximum size limit or should be ready to
   * expect queue element drop offs once the queue maximum size is reached.
   *
   * @return the threshold size of the implementing queue.
   */
  public int getQueueThreshold() {
    return m_workQueue.getThresholdSize();
  }

  /**
   * Sets the discard policy of the Queue. The discard policy can be either DISCARD_NEWEST or
   * DISCARD_OLDEST.
   *
   * @param discard_policy the policy of what to discard if the queue should become full
   */
  public void setQueueDiscardPolicy(short discard_policy) {
    m_workQueue.setDiscardPolicy(discard_policy);
  }

  /**
   * Returns the discard policy of the Queue. The discard policy can be either DISCARD_NEWEST or
   * DISCARD_OLDEST.
   *
   * @return the discard policy of the Queue.
   */
  public short getQueueDiscardPolicy() {
    return m_workQueue.getDiscardPolicy();
  }

  /**
   * Returns the maximum number of worker threads that can operate on the implementing Queue. The
   * active number of workers can be less than or equal to this number but will not exceed this
   * number.
   *
   * @return the maximum number of worker threads that can operate on the implementing Queue.
   */
  public int getMaxNoOfWorkers() {
    return m_workQueue.getMaxThreads();
  }

  /**
   * Sets the maximum number of worker threads that can operate on the implementing Queue. The
   * active number of workers can be less than or equal to this number but will not exceed this
   * number.
   *
   * @param size the maximum number of worker threads that can operate on the implementing Queue.
   */
  public void setMaxNoOfWorkers(int size) {
    m_workQueue.setMaxThreadCount(size);
  }

  /**
   * This is the unit of work for the request queue helper class. Processing a unit of work in this
   * context amounts to calling the proper request interface.
   */
  public class QueueHelperUOW implements DsUnitOfWork {
    /**
     * Constructs the unit of work for the request queue helper class with the specified server
     * transaction <code>server_trans</code>.
     *
     * @param server_trans the server transaction associated with this unit of work.
     */
    public QueueHelperUOW(DsSipServerTransaction server_trans) {
      m_serverTransaction = server_trans;
    }

    public void run() {
      process();
    }
    /** Process the work by passing it to the request interface. */
    public void process() {
      try {
        if (m_overloaded) {
          abort();
          return;
        }
        if (m_requestInterface != null) {
          m_requestInterface.request(m_serverTransaction);
        }
      } catch (DsException dse) {
        DsLog4j.transMCat.warn(
            "QueueHelperUOW.process: Exception calling DsSipRequestInterface.request", dse);
      } catch (IOException ioe) {
        DsLog4j.transMCat.warn(
            "QueueHelperUOW.process: Exception calling DsSipRequestInterface.request", ioe);
      }
    }

    /** The default is to take no action, on our own, just call the user's discard interface. */
    public void abort() {
      try {
        if (m_discardInterface != null) {
          m_discardInterface.request(m_serverTransaction);
        }
      } catch (DsException dse) {
        DsLog4j.transMCat.warn(
            "QueueHelperUOW.process: Exception calling discard interface DsSipRequestInterface.request",
            dse);
      } catch (IOException ioe) {
        DsLog4j.transMCat.warn(
            "QueueHelperUOW.process: Exception calling discard interface DsSipRequestInterface.request",
            ioe);
      }
    }

    DsSipServerTransaction m_serverTransaction;
  }
}
