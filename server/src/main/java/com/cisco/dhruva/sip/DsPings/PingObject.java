package com.cisco.dhruva.sip.DsPings;

/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
/**
 * *****************************************************************************
 *
 * <p>FILENAME : PingObject.java
 *
 * <p>MODULE : DsPings
 *
 * <p>COPYRIGHT :
 *
 * <p>========================copyright 2000 dynamicsoft Inc.=======================
 *
 * <p>=============================all rights reserved==============================
 *
 * <p>*****************************************************************************
 */
import com.cisco.dhruva.loadbalancer.LBFactory;
import com.cisco.dhruva.sip.DsUtil.EndPoint;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRetryAfterHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.DateFormat;
import java.util.Date;

/**
 * this class contains information about a particular PingObject like host, port,
 *
 * <p>listeners for this PingObject, status and the number of attempts
 */
public class PingObject extends EndPoint implements DsSelectable {

  private static final Trace Log = Trace.getTrace(PingObject.class.getName());
  private static int defaultRetryAfter = 0;

  private boolean pingOn = false;

  private boolean currentlyBeingPinged = false;

  private Date retryAfter = null;

  private DsPingsConfigParams configParams = null;

  protected SelectionKey selectionKey;

  private int numTries;

  private Date timeStamp = null;

  private Thread responseHandler = null;

  private Object socket = null;

  private SocketChannel socketChannel = null;

  public final Object pingObjectOpaque = new Object();

  private long endPointLastUsedAt = 0;

  private ByteBuffer buffer = ByteBuffer.allocateDirect(2 * 1024);
  private Charset charset = Charset.defaultCharset();
  private CharsetDecoder decoder = charset.newDecoder();
  private CharBuffer charBuffer;

  /**
   * Constructor to initialize host, port and current state for this PingObject
   *
   * @param host host address of the PingObject
   * @param port port number of the PingObject
   * @param protocol the transport type of the PingObject
   */
  public PingObject(
      DsByteString network,
      DsByteString host,
      int port,
      Transport protocol,
      DsPingsConfigParams configParams,
      Boolean dnsServerGroup) {

    super(network, host, port, protocol);

    if (Log.on && Log.isTraceEnabled()) {
      Log.trace("Entering PingObject()");
    }

    this.configParams = configParams;

    timeStamp = new Date(Long.MAX_VALUE);

    if (Log.on && Log.isTraceEnabled()) {
      Log.trace("Leaving PingObject()");
    }
  }

  public PingObject(
      DsByteString network,
      DsByteString host,
      int port,
      Transport protocol,
      Boolean dnsServerGroup) {

    this(network, host, port, protocol, null, dnsServerGroup);
  }

  /** resets the timestamp value to for this particular PingObject */
  protected void resetTimeStamp() {

    timeStamp = new Date(Long.MAX_VALUE);
    // System.err.println(" time stamp RESET for  " + getKey() +" : " + timeStamp.toString());

  }

  /**
   * Decrements the number of tries.
   *
   * @return <code>true</code> if the number of tries was decremented, <code>false</code> otherwise.
   */
  public synchronized boolean decrementTries(String failureReason) {
    boolean b = checkServerState();
    if (b) {
      numTries--;
      if (Log.on && Log.isInfoEnabled()) {
        Log.info("Endpoint(" + this + "): number of tries left: " + numTries);
      }
    }
    return b;
  }

  /** Resets the number of tries to the maximum. */
  public synchronized void resetTries() {
    switch (protocol) {
      case UDP:
        numTries = LBFactory.getUDPTries();
        break;
      case TCP:
        numTries = LBFactory.getTCPTries();
        break;
      case TLS:
        numTries = LBFactory.getTLSTries();
        break;
    }
    Log.info("Endpoint( {} ): number of tries left: {}", this, numTries);
    EndPointUsed();
  }

  /**
   * Sets the number of tries to 0
   *
   * @return <code>true</code> if the number of tries was not already 0, <code>false</code>
   *     otherwise.
   */
  public synchronized boolean zeroTries() {

    boolean b = checkServerState();

    if (b) numTries = 0;

    return b;
  }

  public int getNumTries() {

    return numTries;
  }

  public synchronized boolean setRetryAfter(Date retryAfterDate) {
    boolean b = false;

    // Did we get a Retry-After?
    if (retryAfterDate != null) {
      Date date = new Date();

      // Is the Retry-After later than now?
      if (retryAfterDate.compareTo(date) > 0) {
        // Do we already have a retry after, and if so, is the new one greater than the old one?
        if ((retryAfter == null) || (retryAfterDate.compareTo(retryAfter) > 0)) {
          retryAfter = retryAfterDate;

          Log.debug("Endpoint( {} ): RetryAfter set to {}", this, retryAfter);

          b = true;
        }
      }
    } else {

      if (defaultRetryAfter > 0) {

        retryAfter = new Date(System.currentTimeMillis() + defaultRetryAfter);

        if (Log.on && Log.isDebugEnabled()) {
          Log.debug("Endpoint(" + this + "): RetryAfter set to default(" + retryAfter + ')');
        }

        b = true;
      }
    }

    return b;
  }

  public synchronized boolean setRetryAfter(DsSipRetryAfterHeader header) {
    Date date = null;

    if (header != null) {
      Log.trace("Endpoint( {} ): RetryAfter header found: {}", this, header);

      if (header.isSipDate()) {
        date = header.getDate().getDate();
      } else {
        date = new Date(System.currentTimeMillis() + (header.getDeltaSeconds() * 1000));
      }
    }

    return setRetryAfter(date);
  }

  /**
   * Check if there is a current RetryAfter date set.
   *
   * @return boolean indicating whether this PingObject is ready to be pinged
   */
  public synchronized boolean isReady() {

    return (checkServerState() && checkRetryAfter());
  }

  /**
   * @return <code>true</code> if no RetryAfter date is set or has expired, <code>false</code>
   *     otherwise.
   */
  protected boolean checkRetryAfter() {
    boolean success = true;

    // Is there a retry after date set?
    if (retryAfter != null) {
      Date current = new Date();
      if (Log.on && Log.isTraceEnabled()) {
        Log.trace("The current time is " + DateFormat.getDateTimeInstance().format(current));
        Log.trace("The retry-after is " + DateFormat.getDateTimeInstance().format(retryAfter));
      }

      // Is the current time before the end of the retry after period?
      if (current.before(retryAfter)) success = false;
      else {
        // The retry after has expired
        // Remove the retry after date
        if (Log.on && Log.isDebugEnabled()) {
          Log.debug("Endpoint(" + this + "): RetryAfter expired");
        }
        removeRetryAfter();
      }
    }
    return success;
  }

  protected void removeRetryAfter() {
    retryAfter = null;
  }

  /** helper method for isServerAvailable() to reduce debug when this method is used by ping */
  protected boolean checkServerState() {

    return (numTries > 0);
  }

  public void EndPointUsed() {}

  public boolean isPingAllowed() {
    return endPointLastUsedAt < System.currentTimeMillis();
  }

  public boolean isPingOn() {
    return (pingOn);
  }

  public void setPingOn(boolean pingOn) {
    this.pingOn = pingOn;
  }

  public boolean isCurrentlyBeingPinged() {
    return (currentlyBeingPinged);
  }

  public void setCurrentlyBeingPinged(boolean currentlyBeingPinged) {
    this.currentlyBeingPinged = currentlyBeingPinged;
  }

  public String toString() {

    StringBuffer str =
        new StringBuffer(super.toString()).append(" numTries=").append(getNumTries());

    if (retryAfter != null)
      str.append(" retryAfter=").append(DateFormat.getDateTimeInstance().format(retryAfter));

    return str.toString();
  }

  /**
   * This method will mark the Pingobject as reachable. This is done by calling following operations
   * on PingObject.
   *
   * <ul>
   *   <li>reset all retry counts and notify CLEAR_UNREACHABLE to listeners
   *   <li>remove retryAfter if present and notify CLEAR_OVERLOADED to listeners
   * </ul>
   *
   * @see ServerGlobalStateWrapper is a PingObject which does these notifications.
   * @param None.
   * @return None.
   */
  public void markAsUp() {
    resetTries();
    /*
     * null check is needed before calling removeRetryAfter.
     * directly calling removeRetryAfter will lead to
     * unwanted CLEAR_OVERLOADED notifications be sent
     * by ServerGlobalStateWrapper.
     */
    if (retryAfter != null) {
      removeRetryAfter();
    }
    /*
     * ServerGlobalStateWrapper, UP state also depends on
     * CallUsageLimit base CAC. Clear of active sessions
     * will be done seperately via SIPSessions.resetActiveSessions
     */
  }

  @Override
  public void process() {}

  private void readOP(SelectionKey key) throws IOException {

    String responseStr = null;
    PingObject po = null;
    SocketChannel sockChannel = (SocketChannel) key.channel();
    buffer.clear();
    if (sockChannel.isConnected() && sockChannel.read(buffer) != -1) {
      buffer.flip();
      charBuffer = decoder.decode(buffer);
      responseStr = charBuffer.toString();
      po = (PingObject) key.attachment();
      handleResponse(responseStr, po);
      Log.info(po.toString());
      if (buffer.hasRemaining()) {
        buffer.compact();
      } else {
        buffer.clear();
      }
    }
    if (responseStr == null) {
      Log.warn("closing channel since response stream is null");
      sockChannel.close();
    }
    Log.info("completed preocessing read operation");
  }

  private void handleResponse(String responseStr, PingObject po) throws IOException {}

  private InputStream getInputStream(String responseStr) throws IOException {
    return NetObjectsFactory.getInputStream(responseStr);
  }

  @Override
  public void abort() {
    // not used in this scope
  }

  @Override
  public void run() {
    // not used in this scope
  }

  @Override
  public int getID() {
    return 0;
  }

  @Override
  public SelectableChannel getChannel() {
    return socketChannel;
  }

  @Override
  public int getOperation() {
    return SelectionKey.OP_READ;
  }

  @Override
  public void setSelectionKey(SelectionKey sk) {
    selectionKey = sk;
  }

  @Override
  public SelectionKey getSelectionKey() {
    return selectionKey;
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }
}
