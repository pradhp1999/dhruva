// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsString;
import java.io.IOException;
import java.net.InetAddress;
import org.slf4j.event.Level;

/** This class defines a default implementation for DsSipNonceInterface. */
public class DsSipNonceGenerator implements DsSipNonceInterface, DsSipConstants {
  private static final String ETag = "dynamicsoft";
  private static final String Key = "(c) dynamicsoft 2003";
  private static final byte[] NonceBytes = (":" + ETag + ":" + Key).getBytes();

  private int Time_Window =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NONCE_TIME_WINDOW, DsConfigManager.PROP_NONCE_TIME_WINDOW_DEFAULT);
  private int m_cnTimeWindow =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_CNONCE_TIME_WINDOW, DsConfigManager.PROP_CNONCE_TIME_WINDOW_DEFAULT);

  private boolean protectMethod = true;
  private int[] hdrsToProtect = {TO, FROM, CALL_ID, CONTACT, EXPIRES};

  /** Constructs this nonce generator with the default values. */
  public DsSipNonceGenerator() {}

  /**
   * Sets the current time window. This time window is used in case of normal nonce generation. This
   * time window defines that after how long the generated nonce will be valid. It means with in
   * this time period, all parameters being same, generated nonce value will be same.
   *
   * @param timeInMinutes the time window in minutes
   */
  public void setTimeWindow(int timeInMinutes) {
    Time_Window = timeInMinutes;
  }

  /**
   * Returns the current time window. This time window is used in case of normal nonce generation.
   * This time window defines that after how long the generated nonce will be valid. It means with
   * in this time period, all parameters being same, generated nonce value will be same.
   *
   * @return the time window in minutes
   */
  public int getTimeWindow() {
    return Time_Window;
  }

  /**
   * Sets the Client Nonce "cnonce" time window value in seconds. This time window defines that how
   * long the generated cnonce will be valid.
   *
   * @param timeInSeconds the cnonce time window in seconds.
   */
  public void setCNTimeWindow(int timeInSeconds) {
    m_cnTimeWindow = timeInSeconds;
  }

  /**
   * Returns the Client Nonce "cnonce" time window value in seconds. This time window defines that
   * how long the generated cnonce will be valid.
   *
   * @return the cnonce time window in seconds
   */
  public int getCNTimeWindow() {
    return m_cnTimeWindow;
  }

  /**
   * Sets the option whether the CSeq method should be protected while calculating the predictive
   * nonce. The CSeq method will be included if this option is set to <code>true</code>, otherwise
   * the CSeq method will not be protected.
   *
   * @param protect if <code>true</code> then the CSeq method will be protected in the predictive
   *     nonce computation.
   */
  public void setMethodProtection(boolean protect) {
    protectMethod = protect;
  }

  /**
   * Tells whether the CSeq method will be protected while calculating the predictive nonce. The
   * CSeq method will be included if this option is set to <code>true</code>, otherwise the CSeq
   * method will not be protected.
   *
   * @return <code>true</code> if the CSeq method will be protected in the predictive nonce
   *     computation, <code>false</code> otherwise.
   */
  public boolean isMethodProtection() {
    return protectMethod;
  }

  /**
   * Sets the list of headers to be protected by predictive nonce computation. The specified array
   * should contain the constant header Ids, as defined in the {@link DsSipConstants} class, of the
   * headers that needs to be protected by predictive nonce computation. <br>
   * This header ids list is used in case of predictive nonce generation.
   *
   * @param hIds the array containing the integer constant header Ids as defined in the
   *     DsSipParserConstants class, of the headers that needs to be protected by predictive nonce
   *     computation
   */
  public void setHeadersToProtect(int[] hIds) {
    hdrsToProtect = hIds;
  }

  /**
   * Tells the list of headers that will be protected by predictive nonce computation. The returned
   * array contains the constant header Ids, as defined in the {@link DsSipConstants} class, of the
   * headers that will be protected by predictive nonce computation. <br>
   * This header ids list is used in case of predictive nonce generation.
   *
   * @return the array containing the integer constant header Ids as defined in the
   *     DsSipParserConstants class, of the headers that will be protected by predictive nonce
   *     computation
   */
  public int[] getHeadersToProtect() {
    return hdrsToProtect;
  }

  /**
   * Generates the nonce value as per the algorithm specified by rfc2617. <br>
   * <br>
   * nonce = time-stamp H(time-stamp ":" ETag ":" private-key) <br>
   * where time-stamp is a server-generated time or other non-repeating value, ETag is the value of
   * the HTTP ETag header associated with the requested entity, and private-key is data known only
   * to the server. Note: If user wants to provide application specific nonce computation algorithm,
   * then he/she can override this method.
   *
   * @param timestamp the time-stamp tick that needs to be used in place of time-stamp in the above
   *     algorithm.
   * @return the nonce value as an un-hashed byte array of (time-stamp ":" ETag ":" private-key)
   */
  public byte[] nonce(int timestamp) {
    String ts = Integer.toString(timestamp, 10);
    int len = ts.length();

    byte[] nonce = new byte[len + NonceBytes.length];

    for (int i = 0; i < len; i++) {
      nonce[i] = (byte) ts.charAt(i);
    }

    System.arraycopy(NonceBytes, 0, nonce, len, NonceBytes.length);

    return nonce;
  }

  /**
   * Generates the nonce value for the specified SIP request as per the algorithm defined by the
   * "predictive nonce" draft for the HTTP Digest authentication scheme. The predictive nonce is
   * computed as:<br>
   * <br>
   * nonce = H(source-IP:&LTcanonicalization of headers to be protected&GT) <br>
   * where H is a suitable cryptographic hash function. <br>
   * By default, To, From, Call-Id and Contact(in case of Register request only) headers are
   * protected. User can specify the headers to be protected by invoking {@link
   * #setHeadersToProtect(int[]) setHeadersToProtect(int[])} method. The tag parameter in case of To
   * and From headers is ignored. Note: If user wants to provide application specific predictive
   * nonce computation algorithm, then he/she can override this method.
   *
   * @param request the SIP request whose headers needs to be protected for message integrity
   * @param timestamp the time-stamp tick that needs to be used in place of time-stamp in the above
   *     algorithm.
   * @return the predictive nonce value as an un-hashed byte array of the value
   *     (source-IP:&LTcanonicalization of headers to be protected&GT)
   */
  public byte[] predictiveNonce(DsSipRequest request, int timestamp) {
    ByteBuffer buffer = new ByteBuffer(200);
    boolean protectContact = false;
    boolean protectExpires = false;

    try {
      // Append SourceIP
      InetAddress address = request.getBindingInfo().getRemoteAddress();
      if (null != address) {
        buffer.write(DsString.getHostBytes(address));
        buffer.write(B_COLON);
      }
      if (null != hdrsToProtect) {
        DsSipHeaderList headers = null;
        DsSipHeaderInterface header = null;
        int id = -1;
        for (int i = 0; i < hdrsToProtect.length; i++) {
          id = hdrsToProtect[i];
          switch (id) {
            case TO:
            case FROM:
              // Assumes only one To, From header will be present
              try {
                header = request.getHeaderValidate(id);
                if (null == header) break;
                DsSipToFromHeader hdr = (DsSipToFromHeader) header.clone();
                hdr.removeTag();
                hdr.writeValue(buffer);
                buffer.write(B_COMMA);
              } catch (DsSipParserException pe) {
              } catch (DsSipParserListenerException ple) {
              }
              break;
            case CSEQ:
              long l = request.getCSeqNumber();
              buffer.write(DsIntStrCache.intToBytes(l));
              DsByteString cseq = request.getCSeqMethod();
              if (null != cseq) buffer.write(cseq);
              buffer.write(B_COMMA);
              break;
            case CALL_ID:
              DsByteString callid = request.getCallId();
              if (null != callid) buffer.write(callid);
              buffer.write(B_COMMA);
              break;
            case CONTENT_LENGTH:
              int cl = request.getContentLength();
              buffer.write(DsIntStrCache.intToBytes(cl));
              buffer.write(B_COMMA);
              break;
            case CONTACT:
              // Contact header will be protected only in case of Register request
              protectContact = true;
              break;
            case EXPIRES:
              // Expires header will be protected only in case of Register request
              protectExpires = true;
              break;
            case CONTENT_TYPE:
              DsSipHeaderInterface h = request.getHeader(id);
              if (h != null) {
                h.writeValue(buffer);
                buffer.write(B_COMMA);
              }
              break;
            default:
              headers = request.getHeaders(id);
              if (null == headers) break;
              header = (DsSipHeaderInterface) headers.getFirst();
              while (null != header) {
                header.writeValue(buffer);
                buffer.write(B_COMMA);
                header = (DsSipHeader) header.getNext();
              } // _while
          } // _switch
        } // _for
      }
      int mID = request.getMethodID();
      if (mID == REGISTER) {
        if (protectExpires) {
          // Append expires header value, if present
          try {
            DsSipExpiresHeader expires = (DsSipExpiresHeader) request.getHeaderValidate(EXPIRES);
            if (expires != null) {
              long ds = expires.getDeltaSeconds() / 60 + 1; // round off to minutes
              buffer.write(DsIntStrCache.intToBytes(ds));
              buffer.write(B_COMMA);
            }
          } catch (DsSipParserException pe) {
            if (DsLog4j.authCat.isEnabled(Level.WARN)) {
              DsLog4j.authCat.warn("Exception while retrieving Expires header.", pe);
            }
          } catch (DsSipParserListenerException ple) {
            if (DsLog4j.authCat.isEnabled(Level.WARN)) {
              DsLog4j.authCat.warn("Exception while retrieving Expires header.", ple);
            }
          }
        } // _if protectExpires

        if (protectContact) {
          // Append sorted list of contact header URIs with
          // corresponding expires parameter, if present
          try {
            DsSipHeaderList contactHeaders = request.getHeadersValidate(CONTACT);
            byte[][] contacts = sortContacts(contactHeaders);
            if (null != contacts) {
              int len = contacts.length - 1;
              for (int i = 0; i < len; i++) {
                buffer.write(contacts[i]);
                buffer.write(B_COMMA);
              }
              if (len > -1) {
                buffer.write(contacts[len]);
              }
            }
          } catch (DsSipParserException pe) {
            if (DsLog4j.authCat.isEnabled(Level.WARN)) {
              DsLog4j.authCat.warn("Exception while retrieving Contact header(s).", pe);
            }
          } catch (DsSipParserListenerException ple) {
            if (DsLog4j.authCat.isEnabled(Level.WARN)) {
              DsLog4j.authCat.warn("Exception while retrieving Contact header(s).", ple);
            }
          }
        } // _if protectContact
      } else // its a non-register request, append CSeq method
      {
        if (protectMethod) {
          try {
            DsByteString cSeq = request.getCSeqMethod();
            if (cSeq != null) {
              cSeq.write(buffer);
            }
          } catch (Exception exc) {
            if (DsLog4j.authCat.isEnabled(Level.WARN)) {
              DsLog4j.authCat.warn("Exception writing the CSeq method.", exc);
            }
          }
        } // _if
      } // _else

      // appending ':round-time'
      buffer.write(B_COLON);
      buffer.write(DsByteString.getBytes(Integer.toString(timestamp)));
    } catch (IOException ioe) {
      // We shouldn't get this exception
    } finally {
      try {
        buffer.close();
      } catch (Exception e) {
        // ignore
      }
    }

    if (DsLog4j.authCat.isEnabled(Level.DEBUG)) {
      DsLog4j.authCat.debug("The pnonce string = [" + buffer.toString() + "]");
    }
    return buffer.toByteArray();
  }

  /**
   * Sorts the contact headers present in the specified LinkedList based on the contact URIs. The
   * contact URIs are sorted in an increasing order alphabetically and a string array of the sorted
   * contact URI strings is returned
   *
   * @param contacts the Linked List containing Contact headers that need to be sorted
   *     alphabetically
   * @return sorted string array of Contact URI strings of the corresponding contact headers in the
   *     specified LinkedList
   */
  private static byte[][] sortContacts(DsSipHeaderList contacts) {
    if (contacts == null || contacts.isEmpty()) {
      return null;
    }
    byte[][] ca = new byte[contacts.size()][];
    int i = 0;
    DsSipContactHeader contact = null;
    long expires = 0;
    ByteBuffer buffer = new ByteBuffer(100);

    try {
      contact = (DsSipContactHeader) contacts.getFirst();
      while (null != contact) {
        buffer.reset();
        if (contact.isWildCard()) {
          byte[] wild = new byte[1];
          wild[0] = B_WILDCARD;
          ca[i++] = wild;
          contact = (DsSipContactHeader) contact.getNext();
          continue;
        }
        DsSipNameAddress na = contact.getNameAddress();
        if (na == null) continue;
        buffer.write(na.getURIString());
        expires = contact.getExpires(); // expires in seconds
        if (expires != -1) {
          expires = expires / 60 + 1; // round off to minutes.
          buffer.write(B_COMMA);
          buffer.write(DsIntStrCache.intToBytes(expires));
        }
        ca[i++] = buffer.toByteArray();
        contact = (DsSipContactHeader) contact.getNext();
      }
      sort(ca);
    } finally {
      try {
        buffer.close();
      } catch (Exception e) {
        // ignore
      }
    }

    return ca;
  }

  /**
   * Sorts the specified array of strings alphabetically and the algorithm used for sorting is
   * "bubble-sort".
   *
   * @param elements the elements to be sorted
   */
  private static void sort(byte[][] elements) {
    int n = elements.length - 1;
    if (n < 1) return; // only one element, so nothing to sort
    byte[] t = null;
    for (int i = n - 1; i >= 0; i--) {
      for (int j = 0; j <= i; j++) {
        if (DsByteString.compare(elements[j + 1], elements[j]) < 0) {
          t = elements[j];
          elements[j] = elements[j + 1];
          elements[j + 1] = t;
        }
      }
    }
  }
}
