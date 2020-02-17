package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.util.*;

/**
 * Helper class that implements DsUnitOfWork and holds one unparsed SIP message. When process() is
 * called the Transaction Manager is called, which in turn calls the message parser. Two major
 * things are accomplished by the addition of this class. The Transaction Manager can now catch
 * parsing errors directly and handle them accordingly, and the parsing of TCP (stream) based
 * messages is not truly two stage. The framing happens and then an instance of an object from this
 * class is put onto the work queue for parsing, later.
 */
public class SipMessageBytes extends DsMessageBytes {
  /** Reference to the Transaction Manager. */
  private static SipTransactionManager tm = SipTransactionManager.getInstance();

  private Calendar m_timestamp;
  /**
   * Constructor that takes the message and its binding information.
   *
   * @param bytes the message in its raw form
   * @param bi the binding information for this message
   */
  public SipMessageBytes(byte bytes[], DsBindingInfo bi) {
    super(bytes, bi);
    m_timestamp = new GregorianCalendar();
  }

  public void process() {

    tm.processMessageBytes(this);
  }

  public void run() {
    process();
  }

  public void abort() {}

  /**
   * Returns the timestamp recorded when this object is created.
   *
   * @return the Timestamp when this object is created.
   */
  public Calendar getTimestamp() {
    return m_timestamp;
  }
}
