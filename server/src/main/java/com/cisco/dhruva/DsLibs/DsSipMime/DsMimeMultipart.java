/*

 FILENAME:    DsMimeMultipart.java


 DESCRIPTION: The class holds all the common behaviors for all the multipart
              types.


 MODULE:      DsMimeMultipart

 AUTHOR:      JR Yang (jryang@cisco.com)
              Michael Zhou (xmzhou@cisco.com)

 COPYRIGHT:

 Copyright (c) 2004-2008 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.DsLibs.DsSipMime;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Derived from DsMimeAbstractBody, this is the base class for all the multipart types and holds all
 * the common behavior. It provides no methods for setting the parts, because the rules for doing so
 * are subtype specific. It is effectively a read only implementation of subtype multipart/mixed.
 */
public abstract class DsMimeMultipart extends DsMimeAbstractBody {
  ////////////////////////
  //       DATA
  ////////////////////////

  /** The boundary. */
  private DsByteString m_boundary;
  /** The preamble. */
  private DsByteString m_preamble;
  /** The epilog. */
  private DsByteString m_epilog;
  /** The list of parts contained in this multipart body. */
  protected List m_partsList;

  /** The seed for generating random boundary. */
  private static int seed = 0;

  /** Net Addr part of the boundry String. */
  private static final String m_netAddrStr;

  static {
    String netAddrStr;
    try {
      netAddrStr = InetAddress.getLocalHost().getHostAddress();
    } catch (Exception ex) {
      netAddrStr = "0.0.0.0";
    }

    char[] chars = netAddrStr.toCharArray();

    for (int i = 0; i < chars.length; i++) {
      if (!isLegalBoundaryChar(chars[i])) {
        chars[i] = '_';
      }
    }

    // There are about 22 chars used in addition to this string
    // in the bondary.  Ensure that this one does not put us over
    // the 70 char boundary limit.
    if (chars.length > 40) {
      netAddrStr = new String(chars, 0, 40);
    } else {
      netAddrStr = new String(chars);
    }

    // pad the final string with '_' so that we do not have to append it each time
    m_netAddrStr = '_' + netAddrStr + '_';
  }

  ////////////////////////
  //       CONSTRUCTORS
  ////////////////////////

  /** Default Constructor. Equivalent to the constructor with initial capacity of 2. */
  public DsMimeMultipart() {
    this(2, null, null, null);
  }

  /**
   * Constructor.
   *
   * @param initialCapacity the initial capacty for the part list
   */
  public DsMimeMultipart(int initialCapacity) {
    this(initialCapacity, null, null, null);
  }

  /**
   * Constructor.
   *
   * @param initialCapacity the initial capacty for the part list (default is 2)
   * @param boundary the boundary deliminater for the multipart body
   * @param preamble the preamble for the multipart body
   * @param epilog the epilogue for the multipart body
   */
  public DsMimeMultipart(
      int initialCapacity, DsByteString boundary, DsByteString preamble, DsByteString epilog) {
    m_boundary = boundary; // user is responsible for ensuring uniqeness of boundary
    m_preamble = preamble;
    m_epilog = epilog;

    if (initialCapacity <= 0) initialCapacity = 2;
    m_partsList = new ArrayList(initialCapacity);
  }

  ////////////////////////
  //       METHODS
  ////////////////////////

  /**
   * Sets the containing entity.
   *
   * @param entity the containing entity
   */
  public void setContainingEntity(DsMimeEntity entity) {
    super.setContainingEntity(entity);
    if (entity != null) {
      DsSipContentTypeHeader hdr = new DsSipContentTypeHeader();
      hdr.setMediaType(getContainingEntityContentType());
      hdr.setParameter(BS_BOUNDARY, getBoundary());
      entity.updateHeader(hdr, false, true);
    }
  }

  /**
   * Returns number of parts currently contained.
   *
   * @return number of parts currently contained
   */
  public int getPartCount() {
    synchronized (m_partsList) {
      return m_partsList.size();
    }
  }

  /**
   * Returns a body part by index. First body part has an index of 0.
   *
   * @param index position of the body inside the multipart
   * @return the MIME entity specified by the index
   * @throws IndexOutOfBoundsException if <code>index</code> is invalid
   */
  public DsMimeEntity getPart(int index) throws IndexOutOfBoundsException {
    synchronized (m_partsList) {
      return (DsMimeEntity) m_partsList.get(index);
    }
  }

  /**
   * Returns all body parts in an array.
   *
   * @return all body parts contained in the multipart
   */
  public DsMimeEntity[] getParts() {
    synchronized (m_partsList) {
      DsMimeEntity[] parts = new DsMimeEntity[m_partsList.size()];
      return (DsMimeEntity[]) m_partsList.toArray(parts);
    }
  }

  /**
   * Sets boundary used to delimit body parts when the multipart is encoded. Normally this value
   * does not need to be set. When the multipart is obtained by parsing it is extracted from the
   * encoded form. This method should only be used if the application requires a particular value be
   * used. In that case the application is responsible for ensuring uniqueness.
   *
   * @param boundary boundary bytes
   */
  public void setBoundary(DsByteString boundary) {
    m_boundary = boundary;
    if (m_entity != null) {
      // update entity's boundary parameter
      try {
        DsSipContentTypeHeader hdr =
            (DsSipContentTypeHeader) m_entity.getHeaderValidate(CONTENT_TYPE);
        if (hdr == null) {
          hdr = new DsSipContentTypeHeader();
          hdr.setMediaType(getContainingEntityContentType());
          m_entity.addHeader(hdr);
        }
        hdr.setParameter(BS_BOUNDARY, boundary);
      } catch (Throwable ex) {
        DsLog4j.mimeCat.warn("In DsMimeMultipart.setBoundary - ", ex);
      }
    }
  }

  /**
   * Gets boundary.
   *
   * @return boundary bytes
   */
  public DsByteString getBoundary() {
    return m_boundary;
  }

  /**
   * Sets preamble.
   *
   * @param preamble preamble bytes
   */
  public void setPreamble(DsByteString preamble) {
    m_preamble = preamble;
  }

  /**
   * Gets preamble.
   *
   * @return preamble bytes
   */
  public DsByteString getPreamble() {
    return m_preamble;
  }

  /**
   * Sets epilog.
   *
   * @param epilog epilog bytes
   */
  public void setEpilog(DsByteString epilog) {
    m_epilog = epilog;
  }

  /**
   * Gets epilog.
   *
   * @return epilog bytes
   */
  public DsByteString getEpilog() {
    return m_epilog;
  }

  /**
   * Determine if this body has been parsed. This parent implementation always returns true.
   *
   * @return </code>true</code> if this body has been parsed, otherwise <code>false</code>
   */
  public boolean isParsed() {
    return true;
  }

  /**
   * Encode the body.
   *
   * @return the unparsed, encuded body
   * @throws DsException if there is an IOException encountered
   */
  public DsMimeUnparsedBody encode() throws DsException {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream(512);
      serialize(os);
      return new DsMimeUnparsedBody(new DsByteString(os.toByteArray()));
    } catch (IOException ioe) {
      throw new DsException(ioe);
    }
  }

  /**
   * Serialize the multipart and write to a stream.
   *
   * @param outStream the stream to write the serialized object to.
   * @throws IOException if there is an error while writing to the
   * @throws DsException if required data is malformed or missing.
   */
  public void serialize(OutputStream outStream) throws IOException, DsException {
    if (DsByteString.nullOrEmpty(m_boundary)) {
      setBoundary(generateBoundaryString());
    }

    if (m_partsList.size() <= 0) {
      throw new DsException("No body parts in the multipart body.");
    }

    // preamble
    if (m_preamble != null) {
      outStream.write(m_preamble.data(), m_preamble.offset(), m_preamble.length());
      outStream.write(B_RETURN);
      outStream.write(B_NEWLINE);
    }

    Iterator iter = m_partsList.iterator();
    while (iter.hasNext()) {
      // boundary
      outStream.write(B_HIPHEN);
      outStream.write(B_HIPHEN);
      outStream.write(m_boundary.data(), m_boundary.offset(), m_boundary.length());
      outStream.write(B_RETURN);
      outStream.write(B_NEWLINE);

      // body part
      ((DsMimeEntity) iter.next()).write(outStream);
      outStream.write(B_RETURN);
      outStream.write(B_NEWLINE);
    }

    // closing boundary
    outStream.write(B_HIPHEN);
    outStream.write(B_HIPHEN);
    outStream.write(m_boundary.data(), m_boundary.offset(), m_boundary.length());
    outStream.write(B_HIPHEN);
    outStream.write(B_HIPHEN);
    outStream.write(B_RETURN);
    outStream.write(B_NEWLINE);

    // epilog
    if (m_epilog != null) {
      outStream.write(m_epilog.data(), m_epilog.offset(), m_epilog.length());
    }
  }

  /**
   * Write the multipart and write to a stream. Same as serialize(OutputStream).
   *
   * @param outStream the stream to write the serialized object to
   * @throws IOException if there is an error while writing to the specified output stream
   * @throws DsException if required data is malformed or missing
   */
  public void write(OutputStream outStream) throws IOException, DsException {
    serialize(outStream);
  }

  /**
   * Returns the whole message as a byte array. A new byte array object is created every time unless
   * this message is finalised. If finalised, then we should have the whole message as DsByteString
   * already. In that case, that DsByteString object's bytes will be returned. To finalise the
   * message, call {@link #setFinalised(boolean)} method.
   *
   * @return this message as a byte array representation.
   */
  public byte[] toByteArray() {
    ByteBuffer buffer = ByteBuffer.newInstance(1024);
    try {
      write(buffer);
    } catch (IOException e) {
      // We may never get this exception as we are writing to the
      // byte buffer. Even if we enter here, it may be possible that
      // some of the bytes are already written to this buffer, so
      // just return those bytes only.
    } catch (DsException e) {
    }
    return buffer.toByteArray();
  }

  /**
   * Returns the whole message as a DsByteString. A new DsByteString object is created every time
   * unless this message is finalized. If finalised, then we should have the whole message as
   * DsByteString already. In that case, that DsByteString object will be returned. To finalise the
   * message, call {@link #setFinalised(boolean)} method.
   *
   * @return this message as a byte string representation.
   */
  public DsByteString toByteString() {
    return new DsByteString(toByteArray());
  }

  /**
   * Returns a String representation of this message.
   *
   * @return a String representation of this message.
   */
  public String toString() {
    return toByteString().toString();
  }

  /**
   * Returns the containing entity content length.
   *
   * @return the containing entity content length
   */
  public int getContainingEntityContentLength() {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream(512);
      serialize(os);
      return os.size();
    } catch (IOException ioe) {
      // IOException returns -1;
    } catch (DsException e) {
      // DsException returns -1;
    }
    return -1;
  }

  /**
   * Returns a random string for boundary when it is not set by application.
   *
   * @return a random string for boundary when it is not set by application
   */
  private static synchronized DsByteString generateBoundaryString() {
    // system time + "local host address" + seed number
    StringBuffer buff =
        new StringBuffer(64)
            .append('_')
            .append(System.currentTimeMillis())
            .append(m_netAddrStr)
            .append(nextSeed())
            .append('_');

    return new DsByteString(buff);
  }

  /**
   * Returns next seed value to be used in a boundary string.
   *
   * @return the next seed value to be used in a boundary string
   */
  private static synchronized int nextSeed() {
    if (++seed == 1000) {
      seed = 0;
    }

    return seed;
  }

  // MIME Bondary BNF
  //
  // boundary      := 0*69<bchars> bcharsnospace
  // bchars        := bcharsnospace / " "
  // bcharsnospace := DIGIT / ALPHA / "'" / "(" / ")" /
  //                  "+" / "_" / "," / "-" / "." /
  //                  "/" / ":" / "=" / "?"
  /**
   * Checks if a char is legal in a boundary String.
   *
   * @returns <code>true</code> if this char is legal, else <code>false</code>
   */
  private static boolean isLegalBoundaryChar(char c) {
    if (c >= '0' && c <= '9') {
      return true;
    }

    if (c >= 'a' && c <= 'z') {
      return true;
    }

    if (c >= 'A' && c <= 'Z') {
      return true;
    }

    if (c == '(' || c == ')' || c == '+' || c == '-' || c == '_' || c == '.' || c == '/' || c == ':'
        || c == '=' || c == '?' || c == ',' || c == '\'') {
      return true;
    }

    return false;
  }

  // public static void main(String[] args)
  // {
  //    System.out.println(generateBoundaryString());
  // }

} // End of public abstract class DsMimeMultipart
