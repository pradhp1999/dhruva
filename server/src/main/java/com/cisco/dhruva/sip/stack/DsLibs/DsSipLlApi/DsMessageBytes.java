// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsUnitOfWork;

/** Abstract base class for unit of work enqueued from the network. */
public abstract class DsMessageBytes implements DsUnitOfWork {
  /** The byte array that contains the messages. */
  protected byte m_msgBytes[];
  /** The binding information for this message. */
  protected DsBindingInfo m_bindingInfo;

  /** Default constructor. */
  public DsMessageBytes() {}

  /**
   * Constructor that sets the bytes. These bytes are not copied. The array that is passed in is now
   * owned by this object.
   *
   * @param bytes the message bytes that are to be stored by this class
   * @param bi the binding info that gets passed into the DsSipMessage after parsing
   */
  public DsMessageBytes(byte bytes[], DsBindingInfo bi) {
    m_msgBytes = bytes;
    m_bindingInfo = bi;
  }

  /**
   * Sets the message bytes for this class. The reference is maintained, bytes are not copied.
   *
   * @param bytes The message bytes that are to be stored by this class
   */
  public void setMessageBytes(byte bytes[]) {
    m_msgBytes = bytes;
  }

  /**
   * Gets the message bytes from this class.
   *
   * @return the byte array that contains the message, starting at index 0 and fills the entire
   *     array.
   */
  public byte[] getMessageBytes() {
    return m_msgBytes;
  }

  /**
   * Sets the binding info for this class. The reference is maintained, not copied.
   *
   * @param bi the binding info that is to be stored by this class
   */
  public void setBindingInfo(DsBindingInfo bi) {
    m_bindingInfo = bi;
  }

  /**
   * Gets the binding info from this class.
   *
   * @return the binding information associated with this messsage
   */
  public DsBindingInfo getBindingInfo() {
    return m_bindingInfo;
  }

  public abstract void process();

  public abstract void run();

  public abstract void abort();
}
