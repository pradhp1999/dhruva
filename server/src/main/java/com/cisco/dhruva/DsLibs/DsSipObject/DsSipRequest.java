// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import org.apache.logging.log4j.Level;

/**
 * This class is the parent class of all SIP Request classes like DsSipInviteMessage,
 * DsSipAckMessage, etc. It defines some common methods and variables.
 */
public class DsSipRequest extends DsSipMessage {
  /** <code>true</code> if running as a JAIN stack. */
  private static final boolean m_jainCompatability =
      DsConfigManager.getProperty(DsConfigManager.PROP_JAIN, DsConfigManager.PROP_JAIN_DEFAULT);

  /**
   * <code>true</code> If required to throw exception when request URI is recognized by the
   * interface and the route header is empty
   */
  private static final boolean m_failOnLRFix =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_FAIL_ON_LRFIX, DsConfigManager.PROP_FAIL_ON_LRFIX_DEFAULT);

  /** Integer ID for method type. */
  private int sID = DsSipConstants.UNKNOWN;

  private DsByteString m_strMethod; // = null;
  private DsURI m_URI; // = null;
  private boolean m_escaped;
  private DsURI m_routeTo;
  private DsURI m_fixURI = LRUNSET; // URI for lsr fixing
  private static final DsURI LRUNSET = new DsURI();
  private DsParameters m_fixRouteParameters;

  // SIP tokenization changes
  // protected DsTokenSipNameAddressFixedFormatEncoder requestURIEncodingFlags;
  protected boolean encodingFlagsSet; // = false;

  /**
   * The branch ID interface that gets initialized to the default implementation. Users can provide
   * their own implementation by implementing DsSipBranchIdInterface and setting it through the
   * DsSipRequest.setBranchIdInterface(DsSipBranchIdInterface).
   */
  private static DsSipBranchIdInterface branchIdGenerator = new DsSipDefaultBranchIdImpl();

  /**
   * Sets the Branch ID interface implementation that will be used for the generation of the branch
   * ID for the VIA header in the SIP Request. This implementation will be used to generate and set
   * the branch parameter of the VIA header in the SIP request only if there is no branch parameter
   * is already set.
   *
   * @param impl the implementation of the DsSipBranchIdInterface that will be used in the
   *     generation of the branch ID value for the VIA header in all the SIP requests.
   * @throws IllegalArgumentException if the specified <code>impl</code> is null.
   */
  public static void setBranchIdInterface(DsSipBranchIdInterface impl) {
    if (null == impl) {
      throw new IllegalArgumentException(
          "The DsSipBranchIdInterface implementation" + " can't be null");
    }
    branchIdGenerator = impl;
  }

  /**
   * Returns the Branch ID interface implementation that is used for the generation of the branch ID
   * for the VIA header in the SIP Request. This implementation is used to generate and set the
   * branch parameter of the VIA header in the SIP request only if there is no branch parameter is
   * already set.
   *
   * @return the implementation of the DsSipBranchIdInterface that is used in the generation of the
   *     branch ID value for the VIA header in all the SIP requests.
   */
  public static DsSipBranchIdInterface getBranchIdInterface() {
    return branchIdGenerator;
  }

  /**
   * This method will create a new request with the specified method name, and a matching CSeq
   * method. This method uses the DsSipMessageListenerFactory that is set in the
   * DsSipDefaultMessageFactory. So, a properly registered user defined extension method will
   * generate an object of the registered type.
   *
   * @param method the SIP method of the request
   * @return the newly create request
   */
  public static DsSipRequest createRequest(DsByteString method) {
    DsSipMessageListenerFactory mlf =
        ((DsSipDefaultMessageFactory) DsSipDefaultMessageFactory.getInstance())
            .getMessageListenerFactory();
    DsSipRequest request = null;

    try {
      request =
          (DsSipRequest) mlf.requestBegin(method.data(), method.offset(), method.length(), false);
    } catch (Exception exc) {
      throw new IllegalArgumentException("Error with the method supplied.");
    }

    request.setCSeqMethod(method);

    return request;
  }

  /** Default constructor. */
  protected DsSipRequest() {}

  protected DsSipRequest(boolean encoded) {
    super(encoded);
  }

  /**
   * Constructs this request object with the method name specified in the passed in <code>buffer
   * </code>.
   *
   * @param buffer the byte array containing the name of the request method.
   * @param offset the offset in the <code>buffer</code> byte array where from the request method
   *     starts.
   * @param count the number of bytes in the <code>buffer</code> byte array that comprise the
   *     request method.
   */
  public DsSipRequest(byte[] buffer, int offset, int count) {
    this(new DsByteString(buffer, offset, count));
  }

  protected DsSipRequest(byte[] buffer, int offset, int count, boolean encoded) {
    this(new DsByteString(buffer, offset, count), encoded);
  }

  /**
   * Constructs this request object with the specified <code>method</code>.
   *
   * @param method the integer constant for the method name.
   */
  public DsSipRequest(int method) {
    this(method, DsSipMsgParser.getMethod(method));
  }

  protected DsSipRequest(int method, boolean encoded) {
    this(method, DsSipMsgParser.getMethod(method), encoded);
  }

  protected DsSipRequest(int id, DsByteString method, boolean encoded) {
    super(encoded);
    if (method == null || method.length() == 0) {
      throw new IllegalArgumentException("The SIP Method cannot be NULL or EMPTY");
    }

    m_strMethod = method;
    sID = id;
  }

  /**
   * Constructs this request object with the specified <code>method</code>.
   *
   * @param method the method name.
   */
  public DsSipRequest(DsByteString method) {
    this(DsSipMsgParser.getMethod(method), method);
  }

  protected DsSipRequest(DsByteString method, boolean encoded) {
    this(DsSipMsgParser.getMethod(method), method, encoded);
  }

  /**
   * Constructs this request object with the specified <code>method</code> name and the specified
   * method <code>id</code>.
   *
   * @param id the integer constant for the method.
   * @param method the method name.
   * @throws IllegalArgumentException if the specified method name is null or empty string.
   */
  private DsSipRequest(int id, DsByteString method) {
    super();
    if (method == null || method.length() == 0) {
      throw new IllegalArgumentException("The SIP Method cannot be NULL or EMPTY");
    }

    m_strMethod = method;
    sID = id;
  }

  /**
   * Constructs this request with the specified method <code>id</code>, <code>fromHeader</code>,
   * <code>toHeader</code>, <code>contactHeader</code>, <code>callId</code>, <code>aCSeqNo</code>,
   * <code>bodyType</code> and the specified <code>body</code>. The specified headers, <code>
   * fromHeader</code>, <code>toHeader</code> and <code>contactHeader</code> will be cloned.
   *
   * @param method the integer constant for the method.
   * @param fromHeader the from header object
   * @param toHeader the to header object
   * @param contactHeader the contact header object
   * @param callId the call id
   * @param aCSeqNo the sequence number
   * @param bodyType the body type
   * @param body the body data
   */
  public DsSipRequest(
      int method,
      DsSipFromHeader fromHeader,
      DsSipToHeader toHeader,
      DsSipContactHeader contactHeader,
      DsByteString callId,
      long aCSeqNo,
      DsByteString bodyType,
      byte[] body) {
    this(
        method,
        DsSipMsgParser.getMethod(method),
        fromHeader,
        toHeader,
        contactHeader,
        callId,
        aCSeqNo,
        bodyType,
        body,
        true);
  }

  /**
   * Constructs this request with the specified <code>method</code> name, <code>fromHeader</code>,
   * <code>toHeader</code>, <code>contactHeader</code>, <code>callId</code>, <code>aCSeqNo</code>,
   * <code>bodyType</code> and the specified <code>body</code>. The specified headers, <code>
   * fromHeader</code>, <code>toHeader</code> and <code>contactHeader</code> will be cloned.
   *
   * @param method the method name.
   * @param fromHeader the from header object
   * @param toHeader the to header object
   * @param contactHeader the contact header object
   * @param callId the call id
   * @param aCSeqNo the sequence number
   * @param bodyType the body type
   * @param body the body data
   */
  public DsSipRequest(
      DsByteString method,
      DsSipFromHeader fromHeader,
      DsSipToHeader toHeader,
      DsSipContactHeader contactHeader,
      DsByteString callId,
      long aCSeqNo,
      DsByteString bodyType,
      byte[] body) {
    this(
        DsSipMsgParser.getMethod(method),
        method,
        fromHeader,
        toHeader,
        contactHeader,
        callId,
        aCSeqNo,
        bodyType,
        body,
        true);
  }

  /**
   * Constructs this request with the specified method <code>id</code>, <code>fromHeader</code>,
   * <code>toHeader</code>, <code>contactHeader</code>, <code>callId</code>, <code>aCSeqNo</code>,
   * <code>bodyType</code> and the specified <code>body</code>. The specified headers, <code>
   * fromHeader</code>, <code>toHeader</code> and <code>contactHeader</code> will be cloned if the
   * specified option <code>doClone</code> is <code>true</code>.
   *
   * @param method the integer constant for the method.
   * @param fromHeader the from header object
   * @param toHeader the to header object
   * @param contactHeader the contact header object
   * @param callId the call id
   * @param aCSeqNo the sequence number
   * @param bodyType the body type
   * @param body the body data
   * @param doClone if true, it will clone the headers, else just use the references
   */
  public DsSipRequest(
      int method,
      DsSipFromHeader fromHeader,
      DsSipToHeader toHeader,
      DsSipContactHeader contactHeader,
      DsByteString callId,
      long aCSeqNo,
      DsByteString bodyType,
      byte[] body,
      boolean doClone) {
    this(
        method,
        DsSipMsgParser.getMethod(method),
        fromHeader,
        toHeader,
        contactHeader,
        callId,
        aCSeqNo,
        bodyType,
        body,
        doClone);
  }

  /**
   * Constructs this request with the specified <code>method</code> name, <code>fromHeader</code>,
   * <code>toHeader</code>, <code>contactHeader</code>, <code>callId</code>, <code>aCSeqNo</code>,
   * <code>bodyType</code> and the specified <code>body</code>. The specified headers, <code>
   * fromHeader</code>, <code>toHeader</code> and <code>contactHeader</code> will be cloned if the
   * specified option <code>doClone</code> is <code>true</code>.
   *
   * @param method the method name.
   * @param fromHeader the from header object
   * @param toHeader the to header object
   * @param contactHeader the contact header object
   * @param callId the call id
   * @param aCSeqNo the sequence number
   * @param bodyType the body type
   * @param body the body data
   * @param doClone if true, it will clone the header, else just use the references
   */
  public DsSipRequest(
      DsByteString method,
      DsSipFromHeader fromHeader,
      DsSipToHeader toHeader,
      DsSipContactHeader contactHeader,
      DsByteString callId,
      long aCSeqNo,
      DsByteString bodyType,
      byte[] body,
      boolean doClone) {
    this(
        DsSipMsgParser.getMethod(method),
        method,
        fromHeader,
        toHeader,
        contactHeader,
        callId,
        aCSeqNo,
        bodyType,
        body,
        doClone);
  }

  /**
   * Constructs this request with the specified method <code>id</code>, <code>method</code> name,
   * <code>fromHeader</code>, <code>toHeader</code>, <code>contactHeader</code>, <code>callId</code>
   * , <code>aCSeqNo</code>, <code>bodyType</code> and the specified <code>body</code>. The
   * specified headers, <code>fromHeader</code>, <code>toHeader</code> and <code>contactHeader
   * </code> will be cloned if the specified option <code>doClone</code> is <code>true</code>.
   *
   * @param id the integer constant for the method.
   * @param method the method name.
   * @param fromHeader the from header object
   * @param toHeader the to header object
   * @param contactHeader the contact header object
   * @param callId the call id
   * @param aCSeqNo the sequence number
   * @param bodyType the body type
   * @param body the body data
   * @param doClone if true, it will clone the header, else just use the references
   */
  private DsSipRequest(
      int id,
      DsByteString method,
      DsSipFromHeader fromHeader,
      DsSipToHeader toHeader,
      DsSipContactHeader contactHeader,
      DsByteString callId,
      long aCSeqNo,
      DsByteString bodyType,
      byte[] body,
      boolean doClone) {
    this(id, method);
    if (doClone) {
      fromHeader = (DsSipFromHeader) fromHeader.clone();
      toHeader = (DsSipToHeader) toHeader.clone();
    }
    updateHeader(fromHeader, false);
    updateHeader(toHeader, false);
    DsByteString tag = fromHeader.getTag();
    if (tag == null || tag.length() < 1) {
      fromHeader.setTag(DsSipTag.generateTag());
    }
    // Contact header
    if (contactHeader != null) {
      updateHeader(contactHeader, doClone);
    }
    // Call-Id header
    m_strCallId =
        (callId != null && callId.length() > 0)
            ? callId
            : new DsByteString(DsSipCallIdHeader.reGenerate());
    // CSeq header
    m_strCSeq = m_strMethod;
    m_lCSeq = (aCSeqNo < 1) ? DsSipCSeqHeader.reGenerate() : aCSeqNo;

    // Body
    setBody(body, bodyType);
    try {
      // Updates the headers from the SIP URL, if any.
      setURI(createURI(toHeader, true));
    } catch (Exception exc) {
      // What to do?
      // May be log
    }
  }

  /**
   * Serializes this request's start line to the specified <code>out</code> output stream.
   *
   * @param out the output stream where this request's start line needs to be serialized.
   * @throws IOException thrown when there is an I/O error
   */
  public void writeStartLine(OutputStream out) throws IOException {
    m_strMethod.write(out);
    if (m_URI != null) {
      BS_SPACE.write(out);
      m_URI.write(out);
    }
    BS_SPACE.write(out);
    if (versionHigh == 2 && versionLow == 0) {
      BS_SIP_VERSION.write(out);
    } else {
      BS_SIP.write(out);
      BS_SLASH.write(out);
      out.write(DsIntStrCache.intToBytes(getVersionHigh()));
      BS_PERIOD.write(out);
      out.write(DsIntStrCache.intToBytes(getVersionLow()));
    }
    BS_EOH.write(out);
  }

  /**
   * Serializes this request's start line to the specified <code>out</code> output stream.
   *
   * @param out the output stream where this request's start line needs to be serialized.
   * @throws IOException thrown when there is an I/O error
   */
  public void writeEncodedStartLine(OutputStream out) throws IOException {
    writeEncodedMethod(out);
    writeEncodedURI(out);
  }

  public void writeEncodedMethod(OutputStream out) throws IOException {
    // default start line handling
    out.write(DsTokenSipMsgParser.TOKEN_SIP_FIXED_FORMAT_REQUEST_START_LINE);
    // write the method ID
    if (getMethodID() == UNKNOWN) {
      int methodId = DsTokenSipMethodDictionary.getEncoding(getMethod());
      out.write(methodId);

      if (methodId == DsTokenSipMethodDictionary.UNKNOWN) {
        getEncodedMessageDictionary().getEncoding(getMethod()).write(out);
      }

    } else {
      out.write(DsTokenSipMethodDictionary.getEncoding(sID));
    }
  }

  public void writeEncodedURI(OutputStream out) throws IOException {
    DsTokenSipNameAddressFixedFormatEncoder requestURIEncodingFlags =
        new DsTokenSipNameAddressFixedFormatEncoder(getURI());

    requestURIEncodingFlags.writeEncoded(out, getEncodedMessageDictionary());
  }

  /**
   * Returns the method name of this request.
   *
   * @return the method name of this request.
   */
  public DsByteString getMethod() {
    return (m_strMethod);
  }

  /**
   * Returns the Request-URI present in the start line of this request.
   *
   * @return the Request-URI present in the start line of this request.
   */
  public DsURI getURI() {
    if (m_URI == null) {
      m_URI = new DsURI();
    }

    return m_URI;
  }

  /**
   * Apply the loose routing protect operation if necessary:
   *
   * <p>If the next hop is a strict router (top Route header's URI does not contain the 'lr'
   * parameter), move the request URI to the bottom Route header and the top Route header to the the
   * request URI.
   *
   * @return the request URI if no Route header exists or the top (pre-protection) Route header's
   *     URI
   * @throws DsSipParserException if the parser encounters an error.
   * @throws DsSipParserListenerException if there is an error with the data the parser reports.
   */
  public DsURI lrEscape() throws DsSipParserException, DsSipParserListenerException {
    // do the escape checking ONE time
    if (m_routeTo != null) return m_routeTo;

    m_routeTo = getURI();
    DsSipRouteHeader topRoute = (DsSipRouteHeader) getHeaderValidate(ROUTE);
    if (topRoute != null) {
      m_routeTo = topRoute.getURI();
      if (!topRoute.lr()) {
        addHeader(new DsSipRouteHeader(getURI())); // URI to bottom

        setURI(m_routeTo); // top to URI
        removeHeader(ROUTE, true);
        m_escaped = true;
      }
    }
    return m_routeTo;
  }

  /**
   * If the protected operation was performed, undo it. Allow lrEscape to be performed again.
   *
   * @return <code>true</code> if the unescaping was successful, <code>false</code> otherwise.
   * @throws DsSipParserException if the parser encounters an error.
   * @throws DsSipParserListenerException if there is an error with the data the parser reports.
   */
  public boolean lrUnescape() throws DsSipParserException, DsSipParserListenerException {
    boolean unescaped = false;
    if (!m_escaped) {
      // m_routeTo is the rURI
      unescaped = false;
      m_routeTo = null;
    } else {
      // bottom route is the old rURI
      // bottom to URI
      DsSipRouteHeader bottomRoute = (DsSipRouteHeader) getHeaderValidate(ROUTE, false);
      removeHeader(ROUTE, false); //  RURI   <-- bottom
      setURI(bottomRoute.getURI()); // there had better be a bottom route

      // m_routeTo is the URI of the old top Route, put it back
      addHeader(new DsSipRouteHeader(m_routeTo), true);
      m_routeTo = null;

      unescaped = true;
      m_escaped = false;
    }

    return unescaped;
  }

  /**
   * Reinitializes this request to be able to resend through a new Client Transaction. The following
   * actions are taken: <br>
   *
   * <pre>
   * 1) Invokes {@link #lrUnescape()} on this request.
   * 2) Increments the CSeq number by one if the <code>incrementCSeq</code>
   *    is <code>true</code>.
   * 3) Sets the Branch ID parameter value to the next Branch ID by invoking
   *    {@link #nextBranchId()}.
   * 4) Set the binding info to new binding info by invoking
   *    <code>setBindingInfo(new DsBindingInfo())</code>.
   * 5) Set the finalised flag to flase so that it can be reserialized with
   *    the changes done to this request.
   * </pre>
   *
   * @param incrementCSeq if <code>true</code> then the CSeq number would be incremented by one.
   * @throws DsSipParserException if the parser encounters an error while parsing the Route headers
   *     during lrUnescape().
   * @throws DsSipParserListenerException if there is an error with the data the parser reports
   *     while parsing the Route headers during lrUnescape().
   */
  public void reinitialize(boolean incrementCSeq)
      throws DsSipParserException, DsSipParserListenerException {
    try {
      // Unset the Route information if it was set in the initial Invite.
      lrUnescape();
    } catch (Exception exc) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn("Exception while unescaping the Route information", exc);
      }
    }
    if (incrementCSeq) {
      setCSeqNumber(getCSeqNumber() + 1);
    }

    nextBranchId();
    setBindingInfo(new DsBindingInfo());
    setFinalised(false);
  }

  /*

  public final static void main (String args[]) throws Exception
  {
      byte[] MSG_I =    ("INVITE sip:UserB@there.com SIP/2.0\r\n" +
              "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
              "Via: SIP/2.0/UDP here.com:5060\r\n" +
              "Route: <sip:UserE@xxx.yyy.com;maddr=ss1.wcom.com>\r\n" +
              "Route: <sip:TinkyWinky@tellytubbyland.com;maddr=ss1.wcom.com>\r\n" +
              "From: BigGuy <sip:UserA@here.com>\r\n" +
              "To: LittleGuy <sip:UserB@there.com>\r\n" +
              "Call-ID: 12345601@here.com\r\n" +
              "CSeq: 1 INVITE\r\n" +
              "Contact: <sip:UserA@100.101.102.103>\r\n" +
              "Content-Type: application/sdp\r\n" +
              "Content-Length: 147\r\n" +
              "\r\n" +
              "v=0\r\n" +
              "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
              "s=Session SDP\r\n" +
              "c=IN IP4 100.101.102.103\r\n" +
              "t=0 0\r\n" +
              "m=audio 49172 RTP/AVP 0\r\n" +
              "a=rtpmap:0 PCMU/8000\r\n").getBytes();

      System.out.println("orig");
      DsSipRequest foo = (DsSipRequest) DsSipMessage.createMessage(MSG_I);
      System.out.println("-----------");
      System.out.println(foo);


      System.out.println("ESCAPE 1");
      foo.lrEscape();
      System.out.println("-----------");
      System.out.println(foo);

      System.out.println("ESCAPE 2");
      foo.lrEscape();
      System.out.println("-----------");
      System.out.println(foo);



      System.out.println("unescape 1");
      foo.lrUnescape();
      System.out.println("-----------");
      System.out.println(foo);



      System.out.println("unescape 2");
      foo.lrUnescape();
      System.out.println("-----------");
      System.out.println(foo);


      System.out.println("ESCAPE 3");
      foo.lrEscape();
      System.out.println("-----------");
      System.out.println(foo);

  }
  */

  // /**
  //  * This class is for testing only.  Used to test the lrFix method below.
  //  */
  //
  //    public class  LRFixTester implements DsSipRouteFixInterface
  //    {
  //        /**
  //         *
  //         */
  //        public boolean recognize(DsURI uri)
  //        {
  //            return (((DsSipURL)uri).getParameter(new DsByteString("me")) != null);
  //        }
  //
  //    }
  //

  /**
   * Apply the loose routing fix operation. The supplied interface is first asked to recognize the
   * request URI.
   *
   * <p>CASSE 1: If the request URI is recognized, it is saved internally as the LRFIX URI and
   * replaced by the URI of the bottom Route header. If the this is the only header, and the route
   * headers's URI is recoginized, it is saved internally as LRFIX URI. If there are more headers,
   * the supplied interface is asked to recognize the URI of the top Route header. If the top Route
   * header's URI is recognized, it is removed and saved internally as the LRFIX URI.
   *
   * <p>CASE 2: If the request URI is not recognized, the supplied interface is asked to recognize
   * the URI of the top Route header. If the top Route header's URI is recognized, it is removed and
   * saved internally as the LRFIX URI.
   *
   * <p>CASE 3: If neither is recognized, the FIX URI is set to null. Once this method is called, it
   * will always return the value returned on its first invocation.
   *
   * <p>Saves the Route Header Parameters, if any, locally. The application needs to call
   * getRouteParameters() to get the value. As per RFC 2543 (strict routing), the route header does
   * not support header parameters in route header. So, if the previous element is a strict router,
   * the header parameters object remains null.
   *
   * @param fix user supplied DsSIpRouteFixInterface implementation
   * @return the FIX URI as described above
   * @throws DsException if fix is null.
   */
  public DsURI lrFix(DsSipRouteFixInterface fix) throws DsException {
    // user gets one chance to apply the fixing
    if (m_fixURI != LRUNSET) {
      return m_fixURI;
    }

    if (fix == null) {
      throw new DsException("lrFix: user provided a null DsSipRouteFixInterface");
    }

    m_fixURI = null;

    DsURI uri = getURI();

    // user recognizes the rURI
    if (fix.recognize(uri, true)) {
      DsSipRouteHeader bottomRoute = (DsSipRouteHeader) getHeaderValidate(ROUTE, false);
      // if the user recognizes the URI there MUST be a Route header
      if (bottomRoute == null) {
        if (m_failOnLRFix) {
          m_fixURI = LRUNSET;
          throw new DsException("lrFix: no Route header: can't complete Route fix operation");
        } else {
          m_fixURI = uri;
          return m_fixURI;
        }
      }

      m_fixURI = uri; //  FIXURI <-- RURI

      removeHeader(ROUTE, false); //  RURI   <-- bottom

      DsURI bottomURI = bottomRoute.getURI();
      setURI(bottomURI);

      if (getHeaders(ROUTE) == null) {
        // no more headers in the list
        if (m_fixURI.equals(bottomURI)) {
          m_fixURI = bottomURI; // FIXURI  <-- bottom Route URI
          // save parameters associated with this route header
          m_fixRouteParameters = bottomRoute.getParameters();
        }
      } else {
        DsSipRouteHeader topRoute = (DsSipRouteHeader) getHeaderValidate(ROUTE);
        if (topRoute != null) {
          uri = topRoute.getURI();
          // user recognizes the top Route
          if (m_fixURI.equals(uri)) {
            m_fixURI = uri; // FIXURI  <-- top Route URI
            // save parameters associated with this route header
            m_fixRouteParameters = topRoute.getParameters();
            removeHeader(ROUTE, true); // remove the top Route header
          }
        }
      }
    } else {
      DsSipRouteHeader topRoute = (DsSipRouteHeader) getHeaderValidate(ROUTE);
      if (topRoute != null) {
        uri = topRoute.getURI();
        // user recognizes the top Route
        if (fix.recognize(uri, false)) {
          m_fixURI = uri; // FIXURI  <-- top Route URI
          // save parameters associated with this route header
          m_fixRouteParameters = topRoute.getParameters();
          removeHeader(ROUTE, true); // remove the top Route header
        }
      }
    }

    return m_fixURI;
  }

  /**
   * Returns the DsParameters Object associated with the SIP header processed by the lrFix() method.
   *
   * @return the Route Parameters saved by the lrFix() method.
   */
  public DsParameters getRouteParameters() {
    return m_fixRouteParameters;
  }

  /**
   * Returns the host name present in the Request-URI of this request.
   *
   * @return the host in the Request-URI. If the URI is not a SIP URL, null is returned
   * @throws DsSipParserException thrown when the parser encounters an error
   */
  public DsByteString getRequestURIHost() throws DsSipParserException {
    if (m_URI == null) {
      m_URI = new DsURI();
    }

    if (m_URI instanceof DsSipURL) {
      return ((DsSipURL) m_URI).getHost();
    } else {
      return null;
    }
  }

  /**
   * Returns the port number present in the Request-URI of this request.
   *
   * @return the port number in the Request-URI. If the URI is not a SIP URL, -1 is returned.
   * @throws DsSipParserException if the parser encounters an error.
   */
  public int getRequestURIPort() throws DsSipParserException {
    if (m_URI == null) {
      m_URI = new DsURI();
    }

    if (m_URI instanceof DsSipURL) {
      return ((DsSipURL) m_URI).getPort();
    } else {
      return -1;
    }
  }

  /**
   * Sets the method of this request.
   *
   * @param aMethod the method to set
   */
  protected void setMethod(int aMethod) {
    sID = aMethod;
    m_strMethod = DsSipMsgParser.getMethod(aMethod);
  }

  /**
   * Sets the method of this request.
   *
   * @param aMethod the method to set
   */
  protected void setMethod(DsByteString aMethod) {
    m_strMethod = aMethod;
    sID = DsSipMsgParser.getMethod(m_strMethod);
  }

  /**
   * Sets the Request-URI.
   *
   * @param aURI a URI object.
   */
  public void setURI(DsURI aURI) {
    m_URI = aURI;
  }

  /**
   * Checks if it is a request or response.
   *
   * @return <code>true</code>.
   */
  public boolean isRequest() {
    return true;
  }

  /**
   * Returns a copy of the Request object.
   *
   * @return the cloned object.
   */
  public Object clone() {
    DsSipRequest clone = (DsSipRequest) super.clone();
    if (m_URI != null) {
      clone.m_URI = (DsURI) m_URI.clone();
    }
    return clone;
  }

  /**
   * Checks for the equality of the start line semantics of this request object against the start
   * line of the specified request <code>message</code>.
   *
   * @param message the message whose start line semantics needs to be compared for equality against
   *     the start line semantics of this request object.
   * @return <code>true</code> if the start line of this request is semantically equal to the start
   *     line of the specified request <code>message</code>, <code>false</code> otherwise.
   */
  // CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy
  public boolean equalsStartLine(DsSipMessageBase message) {
    DsSipRequest request = null;
    try {
      request = (DsSipRequest) message;
    } catch (ClassCastException e) {
      return false;
    }
    // Check for method
    if (!DsByteString.equals(m_strMethod, request.m_strMethod)) return false;

    if (m_URI != null) {
      if (request.m_URI == null) {
        return false;
      }
      if (!m_URI.equals(request.m_URI)) {
        return false;
      }
    } else {
      if (request.m_URI != null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the unique method ID for this request.
   *
   * @return the method ID.
   */
  public final int getMethodID() {
    return sID;
  }

  /**
   * Retrieves the Authorization header or the ProxyAuthorization header.
   *
   * @return the Authorization header or the ProxyAuthorization header.
   */
  public DsSipHeader getAuthenticationHeader() {
    DsSipHeader aHeader = null;
    try {
      aHeader = getHeaderValidate(DsSipConstants.AUTHORIZATION);
      if (null == aHeader) {
        aHeader = getHeaderValidate(DsSipConstants.PROXY_AUTHORIZATION);
      }
    } catch (DsSipParserException pe) {
      // pe.printStackTrace();
      // log?
    } catch (DsSipParserListenerException ple) {
      // ple.printStackTrace();
      // log?
    }
    return aHeader;
  }

  /**
   * Insert a via header into the request with the supplied parameters.
   *
   * @param pHost the host
   * @param aPort the port
   * @param pTransport the transport
   */
  public void insertViaHeader(DsByteString pHost, int aPort, int pTransport) {
    DsSipViaHeader pVia = new DsSipViaHeader(pHost, aPort, pTransport);
    if (NEW_KEY) {
      pVia.setBranch(branchIdGenerator.nextBranchId(null));
    }
    addHeader(pVia, true, false);
  }

  /**
   * Used by the transport layer to determine whether or not a message should be compressed.
   *
   * @return false if the message has already been compressed (based on it's serialized form). After
   *     the serialized form has been checked, this method will check the compress flag on the
   *     message. If that flag is set, this method will return true; If that flag is not set,
   *     comp=sigcomp is checked on the Via header for responses and the rURI for requests. This
   *     method returns true if that parameter is set.
   */
  public final boolean shouldCompress() {
    if (isCompressed()) return false;
    boolean shouldCompress = compress();

    if (!shouldCompress) {
      DsByteString compval = null;
      DsURI uri = getURI();
      if (uri.isSipURL()) {
        compval = ((DsSipURL) uri).getCompParam();
      }
      if (compval != null) {
        if (DsSipConstants.BS_SIGCOMP.equals(compval)) {
          shouldCompress = true;
        }
      }
    }

    return shouldCompress;
  }

  public final DsTokenSipDictionary shouldEncode() {
    if (Log.isDebugEnabled()) Log.debug("Entering shouldEncode of DsSipRequest");
    if (Log.isDebugEnabled()) Log.debug("DsSipRequest Binding Info " + getBindingInfo());

    if ((getBindingInfo() == null)
        || (getBindingInfo().getNetwork() == null)
        || (getBindingInfo().getNetwork().isTSIPEnabled() == false))
    //       if (canEncode() == false)
    {
      if (Log.isDebugEnabled()) Log.debug("Can not encode this request");
      return null;
    }

    if (Log.isDebugEnabled()) Log.debug("Can encode this request");

    // todo what happens if the URI is not a SIP URL

    DsSipRouteHeader topRoute = null;
    try {
      if (Log.isDebugEnabled()) Log.debug("Checking route or r-uri");
      if (Log.isDebugEnabled()) Log.debug(this.maskAndWrapSIPMessageToSingleLineOutput());

      topRoute = (DsSipRouteHeader) getHeaderValidate(DsSipRouteHeader.sID);
    } catch (DsSipParserException e) {
      if (Log.isDebugEnabled()) Log.debug("Cannot parse route header.  No route.");
    } catch (DsSipParserListenerException e) {
      if (Log.isDebugEnabled()) Log.debug("Cannot parse route header.  No route.");
    }

    if (topRoute != null) {
      DsURI uri = topRoute.getURI();
      if (uri.isSipURL()) {
        if (Log.isDebugEnabled()) Log.debug("shouldEncode - in Record-Route URL");

        DsByteString tokval = ((DsSipURL) uri).getParameter(DsTokenSipConstants.s_TokParamName);

        if (tokval != null) {
          if (Log.isDebugEnabled())
            Log.debug("shouldEncode Record-Route - param value is <" + tokval + ">");
          return DsTokenSipMasterDictionary.getDictionary(tokval.unquoted());
        }
      }
    }
    // -- If Route header is present and doesn't have tokenized parameter, we should
    // -- check in the RURI for the tokenized parameter.
    // --        else
    {
      DsURI uri = getURI();
      if (uri.isSipURL()) {
        if (Log.isDebugEnabled()) Log.debug("shouldEncode - in sip URL");

        DsByteString tokval = ((DsSipURL) uri).getParameter(DsTokenSipConstants.s_TokParamName);
        if (tokval != null) {
          if (Log.isDebugEnabled()) Log.debug("shouldEncode - param value is <" + tokval + ">");
          return DsTokenSipMasterDictionary.getDictionary(tokval.unquoted());
        }
      }
    }

    return null;
  }

  /**
   * Stateful key - Changes the branch ID value of the VIA header in this request to the next unique
   * branch ID. The next branch ID is generated by the branch ID provider set by the {@link
   * #setBranchIdInterface(DsSipBranchIdInterface)}. If no branch ID provider implementation is set
   * then the default {@link DsSipDefaultBranchIdImpl} is used.
   *
   * @return <code>true</code> if the branch ID is set to the new value, <code>false</code>
   *     otherwise.
   */
  public boolean nextBranchId() {
    if (m_jainCompatability) {
      return false;
    }

    boolean res = false;
    DsSipViaHeader via = null;
    try {
      via = (DsSipViaHeader) getHeaderValidate(DsSipConstants.VIA);
    } catch (DsSipParserException pe) {
    } catch (DsSipParserListenerException ple) {
    }
    if (null != via) {
      via.setBranch(branchIdGenerator.nextBranchId(null));
      res = true;
    }
    return res;
  }

  /**
   * Stateless key - Changes the branch ID value of the VIA header in this request to the next
   * unique branch ID. The next branch ID is generated by the branch ID provider set by the {@link
   * #setBranchIdInterface(DsSipBranchIdInterface)}. If no branch ID provider implementation is set
   * then the default {@link DsSipDefaultBranchIdImpl} is used.
   *
   * @return <code>true</code> if the branch ID is set to the new value, <code>false</code>
   *     otherwise.
   */
  public boolean nextStatelessBranchId() {
    if (m_jainCompatability) {
      return false;
    }

    boolean res = false;
    DsSipViaHeader via = null;
    try {
      via = (DsSipViaHeader) getHeaderValidate(DsSipConstants.VIA);
    } catch (DsSipParserException pe) {
    } catch (DsSipParserListenerException ple) {
    }
    if (null != via) {
      via.setBranch(branchIdGenerator.nextBranchId(this));
      res = true;
    }
    return res;
  }

  /*
   * javadoc inherited.
   */
  public DsSipElementListener requestURIBegin(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG)
      System.out.println(
          "requestURIBegin - scheme = [" + DsByteString.newString(buffer, offset, count) + "]");
    /* CAFFEINE 2.0 DEVELOPMENT
      - moved the method initURI to DsSipMessageBase.java.
      - changed the method signature to return a DsURI object.
    */
    m_URI = initURI(buffer, offset, count);
    return m_URI;
  }

  /*
   * javadoc inherited.
   */
  public void requestURIFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    /* CAFFEINE 2.0 DEVELOPMENT
      This method implementation was moved to a new method finishURI() in DsSipMessageBase.java
    */
    finishURI(m_URI, buffer, offset, count, valid);
  }

  /* CAFFEINE 2.0 DEVELOPMENT
     This method was moved to DsSipMessageBase.java
      private void initURI(byte[] buffer, int off, int count)
  */

  public final DsSipTransactionKey forceCreateKey() {
    try {
      DsSipViaHeader viaHdr = getViaHeaderValidate();

      if (viaHdr != null) {
        // we are going to treat this via branch as the key, even if it is not a magic cookie
        DsSipDefaultTransactionKey key = new DsSipDefaultTransactionKey();
        key.setCSeqMethod(m_strCSeq);

        DsByteString viaBranch = viaHdr.getBranch();
        if (viaBranch != null) {
          key.setViaBranch(viaBranch.toByteArray());
          m_key = key;
        } else {
          // there is no via branch, so just make up a key
          key.setViaBranch(branchIdGenerator.nextBranchId(null).toByteArray());
        }

        m_key = key;

        return key;
      }
    } catch (Exception e) {
      // log
    }

    return null;
  }
}
