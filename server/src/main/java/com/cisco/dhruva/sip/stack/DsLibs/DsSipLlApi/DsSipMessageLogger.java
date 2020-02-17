// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsUnitOfWork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsWorkQueue;

/**
 * This class wraps a queue to perform logging notification in another thread.
 *
 * @deprecated Use {@link DsMessageLoggingInterface DsMessageLoggingInterface}.
 */
public class DsSipMessageLogger implements DsSipMessageLoggingInterface {
  /** The name the the logger queue is referred to by. */
  private static final String Q_NAME = DsWorkQueue.LOGGER_QNAME;
  /** The length of the queue. */
  private static final int Q_LENGTH = 2000;
  /** The number of threads servicing the logger queue. */
  private static final int Q_N_WORKERS = 2;

  /** Create a message logger with the null user code message logging interface */
  DsSipMessageLogger() {
    this(null);
  }

  /**
   * Create a message logger with the given user code message logging interface
   *
   * @param cb the user code's message logging interface
   */
  DsSipMessageLogger(DsSipMessageLoggingInterface cb) {
    setLoggingInterface(cb);
  }

  /*
   * javadoc inherited
   */
  public final synchronized void logRequest(int reason, byte direction, byte[] request) {
    if (m_queue != null) {
      m_queue.nqueue(new LoggerByteCallback(reason, direction, request, true));
    }
  }

  /*
   * javadoc inherited
   */
  public final synchronized void logResponse(int reason, byte direction, byte[] response) {
    if (m_queue != null) {
      m_queue.nqueue(new LoggerByteCallback(reason, direction, response, false));
    }
  }

  /*
   * javadoc inherited
   */
  public final synchronized void logRequest(int reason, byte direction, DsSipRequest request) {
    if (m_queue != null) {
      m_queue.nqueue(new LoggerCallback(reason, direction, request));
    }
  }

  /*
   * javadoc inherited
   */
  public final synchronized void logResponse(int reason, byte direction, DsSipResponse response) {
    if (m_queue != null) {
      m_queue.nqueue(new LoggerCallback(reason, direction, response));
    }
  }

  /**
   * Sets the logging interface.
   *
   * @param cb the user code's message logging interface -- a value of null will destroy the
   *     underlying queue and prevent further messages from being passed to the previous logging
   *     interface
   */
  synchronized void setLoggingInterface(DsSipMessageLoggingInterface cb) {
    if (cb == null) {
      if (m_queue != null) {
        m_queue.destroy();
        m_queue = null;
      }
    } else {
      if (m_queue == null) {
        initq();
      }
    }

    m_cb = cb;
  }

  private void initq() {
    m_queue = new DsWorkQueue(Q_NAME, Q_LENGTH, Q_N_WORKERS);
    m_queue.setDiscardPolicy(DsWorkQueue.GROW_WITHOUT_BOUND);
    DsConfigManager.registerQueue(m_queue);
  }

  private final synchronized void doLog(int reason, byte direction, DsSipRequest request) {
    if (m_cb != null) {
      m_cb.logRequest(reason, direction, request);
    }
  }

  private final synchronized void doLog(int reason, byte direction, DsSipResponse response) {
    if (m_cb != null) {
      m_cb.logResponse(reason, direction, response);
    }
  }

  private final synchronized void doLogRequest(int reason, byte direction, byte[] request) {
    if (m_cb != null) {
      m_cb.logRequest(reason, direction, request);
    }
  }

  private final synchronized void doLogResponse(int reason, byte direction, byte[] response) {
    if (m_cb != null) {
      m_cb.logResponse(reason, direction, response);
    }
  }

  /** Callback unit of work. */
  class LoggerCallback implements DsUnitOfWork {
    LoggerCallback(int reason, byte direction, DsSipMessage message) {
      m_direction = direction;
      m_reason = reason;
      m_message = message;
    }

    public void run() {
      process();
    }

    public void process() {
      if (m_message.isRequest()) {
        doLog(m_reason, m_direction, (DsSipRequest) m_message);
      } else {
        doLog(m_reason, m_direction, (DsSipResponse) m_message);
      }
    }

    public void abort() {
      // grow without bounds
    }

    private byte m_direction;
    private int m_reason;
    private DsSipMessage m_message;
  }

  /** Callback unit of work. */
  class LoggerByteCallback implements DsUnitOfWork {
    LoggerByteCallback(int reason, byte direction, byte[] message, boolean request) {
      m_direction = direction;
      m_reason = reason;
      m_message = message;
      m_request = request;
    }

    public void run() {
      process();
    }

    public void process() {
      if (m_request) {
        doLogRequest(m_reason, m_direction, m_message);
      } else {
        doLogResponse(m_reason, m_direction, m_message);
      }
    }

    public void abort() {
      // grow without bounds
    }

    private byte m_direction;
    private int m_reason;
    private byte[] m_message;
    private boolean m_request = false;
  }

  private DsWorkQueue m_queue;
  private DsSipMessageLoggingInterface m_cb;
}
