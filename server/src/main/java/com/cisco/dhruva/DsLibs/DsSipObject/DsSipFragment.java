// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipMime.*;
import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import org.apache.logging.log4j.Level;

/**
 * This class represents the sipfrag message as per the RFC 3420. It provides methods to build,
 * access, modify, parse, and clone a sipfrag message.
 */
public class DsSipFragment extends DsSipMessageBase implements DsMimeBody {
  /** Integer to indicate the type of the sipfrag, REQUESTFRAG, RESPONSEFRAG, NOSTARTLINEFRAG */
  private int m_sipfragType = NOSTARTLINEFRAG;
  /** Integer ID for method type. */
  private int sID = DsSipConstants.UNKNOWN;
  /** Method name in DsByteString format */
  private DsByteString m_strMethod; // = null;
  /** The Request-URI */
  private DsURI m_URI; // = null;
  /** The status code for this response. */
  private int m_StatusCode; // = 0;
  /** The reason phrase for this response. */
  private DsByteString m_strPhrase; // = null;

  /** backward reference to containing entity, used to implement DsMimeBody */
  protected DsMimeEntity m_entity;

  private static final int RESPONSE_CLASS_STATUS_CONVERTER = 100;
  private static final int DEFAULT_OUT_STREAM_SIZE = 512;

  /** Three possible values to indicate the type of sipfrag */
  protected static final int REQUESTFRAG = 1;

  protected static final int RESPONSEFRAG = 2;
  protected static final int NOSTARTLINEFRAG = 0;

  /** message/sipfrag properties */
  private static final DsMimeContentProperties DEFAULT_SIPFRAG_PROPS =
      new DsMimeContentProperties(
          DsMimeContentManager.MIME_MT_MESSAGE_SIPFRAG,
          DsMimeContentManager.MIME_DISP_SESSION,
          false,
          DsSipFragmentParser.getInstance(),
          DsSipFragment.class);

  //////////////////////
  //    CONSTRUCTORS
  //////////////////////

  /** Default Constructor. Containing entity will be null. */
  public DsSipFragment() {
    super();
  }

  /**
   * Construct sipfrag and set containing entity. The body of the <code>entity</code> will be used
   * to construct the sipfrag, while the headers in the entity will be set as containing entity of
   * the sipfrag.
   *
   * @param entity the containing entity
   */
  public DsSipFragment(DsMimeEntity entity) {
    this();
    if (entity != null) {
      // Make sure we deal with "message/sipfrag" only.
      DsByteString bodyType = entity.getBodyType();
      if (!DsMimeContentManager.MIME_MT_MESSAGE_SIPFRAG.equalsIgnoreCase(bodyType)) {
        if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
          DsLog4j.messageCat.warn("Wrong Content-Type");
          DsLog4j.messageCat.warn(entity);
        }
      } else {
        // unparsed string
        DsByteString unparsed = ((DsMimeUnparsedBody) entity.getMimeBody()).getBytes();
        // body byte array
        byte[] body = unparsed.data();
        // body offset
        int offset = unparsed.offset();
        int count = unparsed.length();

        try {
          DsSipFragmentParser.parse(this, body, offset, count);
          setContainingEntity(entity);
          entity.setMimeBody(this);
        } catch (DsSipParserListenerException ple) {
          if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
            DsLog4j.messageCat.warn("DsSipFragment: DsSipParserListenerException", ple);
          }
        } catch (DsSipParserException pe) {
          if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
            DsLog4j.messageCat.warn("DsSipFragment: DsSipParserException", pe);
          }
        }
      }
    }
  }

  /**
   * Constructor that take request or response as argument. The resulting sipfrag will have request
   * / response startLine and the whole request or response will be part of the sipfrag.
   *
   * @param message the request/response that the sipfrag is based on
   * @param cloneHeaders to indicate if headers cloning is needed
   */
  public DsSipFragment(DsSipMessage message) {
    this();
    try {
      DsSipFragmentParser.parse(this, message.toByteString().data());
    } catch (DsSipParserListenerException ple) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn("DsSipFragment: Exception parsing the message", ple);
      }
    } catch (DsSipParserException pe) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn("DsSipFragment: Exception parsing the message", pe);
      }
    }
  }

  /**
   * Constructor that takes reason code of the response as argument (default reason phrase will be
   * used)
   *
   * @param code the reason code of the response
   */
  public DsSipFragment(int reasonCode) {
    this();
    m_StatusCode = reasonCode;
    m_strPhrase = DsSipResponseCode.getBSReasonPhrase(reasonCode); // Use the default phrase
    m_sipfragType = RESPONSEFRAG;
  }

  /**
   * Constructor that takes reason code and reason phrase of the response as arguments
   *
   * @param code the reason code of the response
   * @param reasonPhrase the reason phrase the user wants to use for the response
   */
  public DsSipFragment(int reasonCode, DsByteString reasonPhrase) {
    this();
    m_StatusCode = reasonCode;
    m_strPhrase = reasonPhrase;
    m_sipfragType = RESPONSEFRAG;
  }

  //////////////////////
  //    METHODS
  //////////////////////

  /**
   * Creates sipfrag message from the byte array, by parsing the byte array.
   *
   * @param bytes the byte array to be used to create the sipfrag message
   * @return returns the newly created sipfrag message, DsSipFragment
   */
  public static DsSipFragment createFragment(byte[] bytes)
      throws DsSipParserListenerException, DsSipParserException {
    return createFragment(bytes, 0, bytes.length);
  }

  /**
   * Creates sipfrag message from the byte array (subset indicated by the offset and count).
   *
   * @param bytes the byte array to be used to create the sipfrag message
   * @param offset the offset to the byte array
   * @param count number of bytes to be included
   * @return returns the newly created sipfrag message, DsSipFragment
   */
  public static DsSipFragment createFragment(byte[] bytes, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipFragment sipfrag = new DsSipFragment();
    try {
      DsSipFragmentParser.parse(sipfrag, bytes, offset, count);
      return sipfrag;
    } catch (DsSipParserListenerException ple) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn(
            "DsSipFragment.createFragment(): Exception parsing the message", ple);
      }
    } catch (DsSipParserException pe) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn(
            "DsSipFragment.createFragment(): Exception parsing the message", pe);
      }
    }
    return null;
  }

  /**
   * Creates sipfrag message from the byte string (by parsing).
   *
   * @param bytes the byte string to be used to create the sipfrag message
   * @return returns the newly created sipfrag message, DsSipFragment
   */
  public static DsSipFragment createFragment(DsByteString bytes)
      throws DsSipParserListenerException, DsSipParserException {
    // the effective data begin at offset, not 0. See fix by CSCsc71466
    return createFragment(bytes.data, bytes.offset(), bytes.length());
  }

  /**
   * Checks if it is a request sipfrag
   *
   * @return <code>true</code> if it's a request sipfrag.
   */
  public boolean isRequestFrag() {
    return (m_sipfragType == REQUESTFRAG) ? true : false;
  }

  /**
   * Checks if it is a response sipfrag
   *
   * @return <code>true</code> if it's a response sipfrag.
   */
  public boolean isResponseFrag() {
    return (m_sipfragType == RESPONSEFRAG) ? true : false;
  }

  /**
   * Retrieves the response class code.
   *
   * @return the response class code. For a 180 response, getResponseClass() returns 1. For a 200
   *     response, it returns 2, etc.
   */
  public int getResponseClass() {
    if (m_sipfragType != RESPONSEFRAG) return 0;
    return (m_StatusCode / RESPONSE_CLASS_STATUS_CONVERTER);
  }

  /**
   * Retrieves the status code.
   *
   * @return the status code.
   */
  public int getStatusCode() {
    if (m_sipfragType != RESPONSEFRAG) return -1;
    return m_StatusCode;
  }

  /**
   * Retrieves the reason phrase.
   *
   * @return the reason phrase.
   */
  public DsByteString getReasonPhrase() {
    if (m_sipfragType != RESPONSEFRAG) return null;
    return m_strPhrase;
  }

  /**
   * Sets the status code.
   *
   * @param aStatusCode the status code.
   */
  public void setStatusCode(int aStatusCode) {
    if (m_sipfragType == RESPONSEFRAG) // Only do it if the sipfrag is response
    {
      m_StatusCode = aStatusCode;
    } else {
      throw new IllegalArgumentException("Can not call setStatusCode() with non-response sipfrag");
    }
  }

  /**
   * Sets the reason phrase.
   *
   * @param pReasonPhrase the reason phrase.
   */
  public void setReasonPhrase(DsByteString pReasonPhrase) {
    if (m_sipfragType == RESPONSEFRAG) // Only do it if the sipfrag is response
    {
      m_strPhrase = pReasonPhrase;
    } else {
      throw new IllegalArgumentException(
          "Can not call setReasonPhrase() with non-response sipfrag");
    }
  }

  /**
   * Method to get the unique method ID for a message, this is the same value as sID and matches the
   * values in DsSipConstants.
   *
   * @return the method ID. If there's no CSeq header, -1 is returned.
   */
  public int getMethodID() {
    if (m_sipfragType != REQUESTFRAG) return DsSipConstants.UNKNOWN;
    return sID;
  }

  /**
   * Returns the method name of this request.
   *
   * @return the method name of this request.
   */
  public DsByteString getMethod() {
    if (m_sipfragType != REQUESTFRAG) return null;
    return (m_strMethod);
  }

  /**
   * Sets the method of this request.
   *
   * @param aMethod the method to set
   */
  public void setMethod(int aMethod) {
    if (m_sipfragType == REQUESTFRAG) // Only do it if the sipfrag is Request
    {
      sID = aMethod;
      m_strMethod = DsSipMsgParser.getMethod(aMethod);
    } else {
      throw new IllegalArgumentException("Can not call setMethod() with non-request sipfrag");
    }
  }

  /**
   * Sets the method of this request.
   *
   * @param aMethod the method to set
   */
  public void setMethod(DsByteString aMethod) {
    if (m_sipfragType == REQUESTFRAG) // Only do it if the sipfrag is Request
    {
      m_strMethod = aMethod;
      sID = DsSipMsgParser.getMethod(m_strMethod);
    } else {
      throw new IllegalArgumentException("Can not call setMethod() with non-request sipfrag");
    }
  }

  /**
   * Returns the Request-URI present in the start line of this request sipfrag.
   *
   * @return the Request-URI present in the start line of this request sipfrag.
   */
  public DsURI getURI() {
    if (m_sipfragType != REQUESTFRAG) return null;
    if (m_URI == null) {
      m_URI = new DsURI();
    }

    return m_URI;
  }

  /**
   * Sets the Request-URI.
   *
   * @param aURI a URI object.
   */
  public void setURI(DsURI aURI) {
    if (m_sipfragType == REQUESTFRAG) // Only do it if the sipfrag is Request
    {
      m_URI = aURI;
    } else {
      throw new IllegalArgumentException("Can not call setMethod() with non-request sipfrag");
    }
  }

  /** Sets the sipfrag type */
  protected void setSipfragType(int type) {
    if (type >= 0 && type <= 2) {
      m_sipfragType = type;
    }
  }

  /**
   * Serialize this sipfrag message and return the results as a String.
   *
   * @return a serialized version of this sipfrag message, empty string if there is an exception
   *     encountered
   */
  public String toString() {
    return new String(toByteArray());
  }

  /**
   * Writes the sipfrag message to the output stream, and then calls flush().
   *
   * @param out the output stream
   * @throws IOException if there is an exception in writing to the stream
   */
  public void write(OutputStream out) throws IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.MSG_WRITE);
    if (m_bFinalized) {
      m_strValue.write(out);
    } else {
      writeStartLine(out);
      writeHeadersAndBody(out);
    }
    out.flush();
    if (DsPerf.ON) DsPerf.stop(DsPerf.MSG_WRITE);
  }

  /** Registers content type "message/sipfrag" with DsMimeContentManager. */
  public static void registerType() {
    DsMimeContentManager.registerContentType(
        DsMimeContentManager.MIME_MT_MESSAGE_SIPFRAG, DEFAULT_SIPFRAG_PROPS);
  }

  /**
   * Set the StartLine of the sipfrag to request frag, based on the <code>methodID</code> and <code>
   * uri</code>
   *
   * @param methodID the methodID will be used to set the sipfrag's startLine.
   * @param uri the URI will be used to set the sipfrag's startLine.
   */
  public void setStartLine(int methodID, DsURI uri) {
    if (methodID == -1 || uri == null) return;
    m_sipfragType = REQUESTFRAG;
    sID = methodID;
    m_strMethod = DsSipMsgParser.getMethod(sID);
    m_URI = uri;
    // reset and clear out data for the other type of sipfrag
    m_StatusCode = 0;
    m_strPhrase = null;
  }

  /**
   * Set the StartLine of the sipfrag to request frag, based on the <code>strMethod</code> and
   * <code>uri</code>
   *
   * @param strMethod the strMethod will be used to set the sipfrag's startLine.
   * @param uri the URI will be used to set the sipfrag's startLine.
   */
  public void setStartLine(DsByteString strMethod, DsURI uri) {
    if (strMethod == null || uri == null) return;
    m_sipfragType = REQUESTFRAG;
    sID = DsSipMsgParser.getMethod(strMethod);
    m_strMethod = strMethod;
    m_URI = uri;
    // reset and clear out data for the other type of sipfrag
    m_StatusCode = 0;
    m_strPhrase = null;
  }

  /**
   * Set the StartLine of the sipfrag to the response frag
   *
   * @param statusCode the status code of the response frag's startLine
   */
  public void setStartLine(int statusCode) {
    if (statusCode == -1) return;
    m_sipfragType = RESPONSEFRAG;
    m_StatusCode = statusCode;
    m_strPhrase = DsSipResponseCode.getBSReasonPhrase(statusCode); // Use the default phrase
    sID = DsSipConstants.RESPONSE; // Constant representing the integer
    // ID for the Response, defined in DsSipConstants..
    // reset and clear out data for the other type of sipfrag
    m_strMethod = null;
    m_URI = null;
  }

  /**
   * Set the StartLine of the sipfrag to the response frag
   *
   * @param statusCode the status code of the response frag's startLine
   */
  public void setStartLine(int statusCode, DsByteString reasonPhrase) {
    if (statusCode == -1 || reasonPhrase == null) return;
    m_sipfragType = RESPONSEFRAG;
    m_StatusCode = statusCode;
    m_strPhrase = reasonPhrase; // use the new reason phrase provided
    sID = DsSipConstants.RESPONSE; // Constant representing the integer
    // ID for the Response, defined in DsSipConstants..
    // reset and clear out data for the other type of sipfrag
    m_strMethod = null;
    m_URI = null;
  }

  /** Set the StartLine of the sipfrag to neither request, nor response frag */
  public void setStartLine() {
    m_sipfragType = NOSTARTLINEFRAG;
    sID = DsSipConstants.UNKNOWN;
    m_strMethod = null;
    m_URI = null;
    // setVersion(2, 0);
    m_StatusCode = -1;
    m_strPhrase = null;
  }

  /**
   * Delete the StartLine of the sipfrag, resulting in a new sipfrag that is neither request nor
   * response sipfrag.
   */
  public void deleteStartLine() {
    m_sipfragType = NOSTARTLINEFRAG;
    sID = DsSipConstants.UNKNOWN;
    m_strMethod = null;
    m_URI = null;
    setVersion(2, 0);
    m_StatusCode = 0;
    m_strPhrase = null;
  }

  /**
   * Serializes the start line (if any) of this sipfrag message to the specified <code>out</code>
   * output stream.
   *
   * @param out the output stream where the start line needs to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public void writeStartLine(OutputStream out) throws IOException {
    if (isRequestFrag()) {
      if (m_strMethod != null) {
        m_strMethod.write(out);
      }
      if (m_URI != null) {
        BS_SPACE.write(out);
        m_URI.write(out);
      }
      BS_SPACE.write(out);
      writeVersion(versionHigh, versionLow, out);
    } else if (isResponseFrag()) {
      writeVersion(versionHigh, versionLow, out);
      out.write(B_SPACE);
      out.write(DsIntStrCache.intToBytes(m_StatusCode));
      out.write(B_SPACE);
      if (m_strPhrase != null) {
        m_strPhrase.write(out);
      }
    } else // which is OK for sipfrag, do nothing, not even a blank line.
    {
      return;
    }
    BS_EOH.write(out);
  }

  private void writeVersion(int versionHigh, int versionLow, OutputStream out) throws IOException {
    // optimized for most used case
    if (versionHigh == 2 && versionLow == 0) {
      BS_SIP_VERSION.write(out);
    } else {
      BS_SIP.write(out);
      out.write(B_SLASH);
      out.write(DsIntStrCache.intToBytes(getVersionHigh()));
      out.write(B_PERIOD);
      out.write(DsIntStrCache.intToBytes(getVersionLow()));
    }
  }

  /**
   * Checks for the semantic equality of this sipfrag against the specified <code>comparator</code>
   * sipfrag object.
   *
   * @param comparator the sipfrag whose semantics needs to be compared for equality against the
   *     semantics of this sipfrag object.
   * @return <code>true</code> if this sipfrag is semantically equal to the the specified <code>
   *     comparator</code> sipfrag object, <code>false</code> otherwise.
   */
  public boolean equals(Object o) {
    if (o.equals(this)) return true;
    if (!(o instanceof DsSipFragment)) return false;

    return equals((DsSipFragment) o);
  }

  /**
   * Checks for the semantic equality of this sipfrag against the specified <code>sipfrag</code>
   * object.
   *
   * @param sipfrag the sipfrag whose semantics needs to be compared for equality against the
   *     semantics of this sipfrag object.
   * @return <code>true</code> if this sipfrag is semantically equal to the the specified <code>
   *     sipfrag</code>, <code>false</code> otherwise.
   */
  public boolean equals(DsSipFragment frag) {
    return super.equals(frag);
  }

  /**
   * Checks for the equality of the start line semantics of this message object against the start
   * line of the specified <code>message</code>.
   *
   * @parammessage the message whose start line semantics needs to be compared for equality against
   *     the start line semantics of this message object.
   * @return <code>true</code> if the start line of this message is semantically equal to the start
   *     line of the specified <code>message</code>, <code>false</code> otherwise.
   */
  public boolean equalsStartLine(DsSipMessageBase message) {
    DsSipFragment sipfrag = null;
    if (message == null) return false;
    if (!(message instanceof DsSipFragment)) return false;
    sipfrag = (DsSipFragment) message;

    if (m_sipfragType == REQUESTFRAG) { // Compare the request startLine
      if (sipfrag.isRequestFrag() == false) return false;
      // Check for method
      if (!DsByteString.equals(m_strMethod, sipfrag.m_strMethod)) return false;
      // Check for request URI
      if (m_URI != null) {
        if (sipfrag.m_URI == null) {
          return false;
        }
        if (!m_URI.equals(sipfrag.m_URI)) {
          return false;
        }
      } else {
        if (sipfrag.m_URI != null) {
          return false;
        }
      }
    } else if (m_sipfragType == RESPONSEFRAG) { // Compare the response startLine
      if (sipfrag.isResponseFrag() == false) return false;
      // check for status code.
      if (m_StatusCode != sipfrag.m_StatusCode) return false;
      // check for reason phrase.
      if (!DsByteString.equals(m_strPhrase, sipfrag.m_strPhrase)) return false;
    } else // No startLine
    {
      if ((sipfrag.isResponseFrag() == true) || (sipfrag.isRequestFrag() == true)) {
        return false;
      }
    }

    return true;
  }

  /*
   * javadoc inherited.
   */
  public DsSipElementListener requestURIBegin(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {
    m_URI = initURI(buffer, offset, count);
    return m_URI;
  }

  /*
   * javadoc inherited.
   */
  public void requestURIFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    finishURI(m_URI, buffer, offset, count, valid);
  }

  // Implementing DsMimeBody

  /**
   * Returns the containing entity.
   *
   * @return the containing entity. Null is returned if the body is not contained in any enti ty.
   */
  public DsMimeEntity getContainingEntity() {
    return m_entity;
  }

  public void setContainingEntity(DsMimeEntity entity) {
    if (entity == m_entity) return;
    if (entity == null) // deref
    {
      // m_entity is not null for sure here
      m_entity.setMimeBody(null);
      m_entity = null;
    } else {
      // entity is not null for sure here
      if (m_entity != null) {
        m_entity.setMimeBody(null);
      }
      if (entity.getMimeBody() != null) entity.getMimeBody().setContainingEntity(null);
      entity.setMimeBody(null);
      m_entity = entity;
      m_entity.setMimeBody(this);
    }
  }

  public DsByteString getContainingEntityContentType() {
    return (m_entity == null ? null : m_entity.getContentType());
  }

  public DsByteString getContainingEntityContentTypeParameter(DsByteString name) {
    return (m_entity == null ? null : m_entity.getContentTypeParameter(name));
  }

  public DsParameters getContainingEntityContentTypeParameters() {
    return (m_entity == null ? null : m_entity.getContentTypeParameters());
  }

  public DsByteString getContainingEntityDispositionType() {
    return (m_entity == null ? null : m_entity.getDispositionType());
  }

  public DsByteString getContainingEntityDispositionHandling() {
    return (m_entity == null ? null : m_entity.getDispositionHandling());
  }

  public DsByteString getContainingEntityContentId() {
    return (m_entity == null ? null : m_entity.getContentId());
  }

  public DsMimeEntityTraverser traverser() {
    return DsMimeNullTraverser.getInstance();
  }

  public boolean isParsed() {
    return true;
  }

  public DsMimeUnparsedBody encode() throws DsException {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream(DEFAULT_OUT_STREAM_SIZE);
      write(os);
      return new DsMimeUnparsedBody(new DsByteString(os.toByteArray()));
    } catch (IOException ioe) {
      throw new DsException(ioe);
    }
  }

  public int getContainingEntityContentLength() {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream(DEFAULT_OUT_STREAM_SIZE);
      write(os);
      return os.size();
    } catch (IOException ioe) {
      // IOException returns -1;
    }
    return -1;
  }
} // Ends class DsSipFragment
