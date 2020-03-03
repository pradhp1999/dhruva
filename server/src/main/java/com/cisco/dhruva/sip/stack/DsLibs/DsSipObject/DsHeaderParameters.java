// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import gnu.trove.TLinkable;
import gnu.trove.TLinkedList;
import java.io.IOException;
import java.io.OutputStream;

/** This class provides the functionality to parse and generate SIP parameters. */
public class DsHeaderParameters extends DsParameters {
  /** Default constructor. */
  public DsHeaderParameters() {
    super();
    setDelimiter(B_AMPERSAND);
  }

  /**
   * Constructs and parses the name-value pairs from the specified String into this object.
   *
   * @param value the input value from where name-value pairs need to be parsed
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsHeaderParameters(String value)
      throws DsSipParserException, DsSipParserListenerException {
    this(DsByteString.getBytes(value));
  }

  /**
   * Constructs and parses the name-value pairs from the specified byte array into this object.
   *
   * @param value the input byte buffer from where name-value pairs need to be parsed
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsHeaderParameters(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Constructs and parses the name-value pairs from the specified byte array into this object.
   *
   * @param value the input byte buffer from where name-value pairs need to be parsed
   * @param offset the offset of the parameters in the specified buffer
   * @param count the number of bytes to be looked starting from the offset in the specified buffer
   *     for parsing.
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsHeaderParameters(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    super(value, offset, count);
    setDelimiter(B_AMPERSAND);
  }

  /**
   * Adds the specified header name value pair as the parameter in this header parameter table. If a
   * parameter with the specified key already exists, then it adds that parameter value with this
   * new specified value. The null values are not allowed. If either the key or the value is null,
   * this method will do nothing.
   *
   * @param key the parameter key
   * @param value the parameter value
   */
  public void put(DsByteString key, DsByteString value) {
    if (key != null && value != null) {
      DsHeaderParameter param = (DsHeaderParameter) find(key);
      if (param != null) {
        param.add(value);
      } else {
        addLast(new DsHeaderParameter(key, value));
      }
    }
  }

  /**
   * Removes the header value for the specified key, if present, from the end of the list.
   *
   * @param key the header name whose value is to be removed.
   * @return the removed header value.
   */
  public DsByteString remove(DsByteString key) {
    return remove(key, false);
  }

  /**
   * Removes the header value for the specified key, if present. If <code>start</code> is <code>true
   * </code>, then it will be removed from the front, otherwise from the end of list.
   *
   * @param key the header name whose value is to be removed.
   * @param start If <code>true</code>, then the header will be removed from the front, otherwise
   *     from the end of list.
   * @return the removed header value.
   */
  public DsByteString remove(DsByteString key, boolean start) {
    DsHeaderParameter param = (DsHeaderParameter) getFirst();
    DsByteString value = null;
    while (null != param) {
      if (param.equals(key)) {
        value = param.remove(start);

        // If there are more values for the same header, then exit.
        if (!param.isEmpty()) break;

        // else remove this header parameter from the list.
        if (_size == 1) {
          _head = _tail = null;
        } else {
          TLinkable n = param.getNext();
          TLinkable p = param.getPrevious();
          if (null != p) {
            p.setNext(n);
          }
          if (null != n) {
            n.setPrevious(p);
          }
          if (_head == param) {
            _head = n;
          } else if (_tail == param) {
            _tail = p;
          }
        }
        param.setNext(null);
        param.setPrevious(null);
        _size--;
      }
      param = (DsHeaderParameter) param.getNext();
    }
    return value;
  }

  /**
   * Removes all the header values for the specified key, if present.
   *
   * @param key the key whose value is to be removed
   * @return the TLinkedList of DsSipHeaderString representing all the header values that are
   *     removed.
   */
  public TLinkedList removeAll(DsByteString key) {
    DsHeaderParameter param = (DsHeaderParameter) getFirst();
    TLinkedList value = null;
    while (null != param) {
      if (param.equals(key)) {
        value = param.getValues();

        if (_size == 1) {
          _head = _tail = null;
        } else {
          TLinkable n = param.getNext();
          TLinkable p = param.getPrevious();
          if (null != p) {
            p.setNext(n);
          }
          if (null != n) {
            n.setPrevious(p);
          }
          if (_head == param) {
            _head = n;
          } else if (_tail == param) {
            _tail = p;
          }
        }
        param.setNext(null);
        param.setPrevious(null);
        _size--;
      }
      param = (DsHeaderParameter) param.getNext();
    }
    return value;
  }

  /**
   * Serializes these parameters table to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream
   * @throws IOException if there is an error while writing to the output stream
   */
  public void write(OutputStream out) throws IOException {
    boolean first = !startsWithDelimiter();
    DsHeaderParameter param = (DsHeaderParameter) getFirst();
    while (param != null) {
      if (!first) {
        out.write(getDelimiter());
      } else {
        first = false;
      }
      param.write(out);
      param = (DsHeaderParameter) param.getNext();
    }
  }

  public DsHeaderParameter getHeaderParameter(DsByteString key) {
    DsHeaderParameter param = (DsHeaderParameter) find(key);

    return param;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    DsHeaderParameters hp = null;
    try {
      hp = (DsHeaderParameters) obj;
    } catch (ClassCastException e) {
      return false;
    }

    // We are going to cheat a little here so we can get some code reuse
    // Create messages and compare the headers within these messages

    DsSipOptionsMessage op1 = new DsSipOptionsMessage();
    DsSipOptionsMessage op2 = new DsSipOptionsMessage();

    copyHeadersToRequest(op1, false);
    hp.copyHeadersToRequest(op2, false);

    if (!op1.equalsHeaders(op2)) {
      return false;
    }

    return true;
  }

  /**
   * Copy these header parameters to the request. The following headers are ignored if <code>
   * ignoreRedirectHeaders</code> is set to <code>true</code>:
   *
   * <blockquote>
   *
   * From Call-ID CSeq Via Record-Route Route Accept Accept-Encoding Accept-Language Allow Contact
   * Organization Supported User-Agent
   *
   * </blockquote>
   *
   * @param request the request to which these header parameters should be copied. If request is
   *     <code>null</code> this method does nothing.
   * @param ignoreRedirectHeaders ignore headers that are not relevant to a redirect
   */
  public void copyHeadersToRequest(DsSipRequest request, boolean ignoreRedirectHeaders) {
    if (request == null) return;

    // Iterate through the headers in the SIP URL and add to the
    // request where required.
    int id = -1;
    DsHeaderParameter header = (DsHeaderParameter) getFirst();
    while (header != null) {
      DsByteString name = header.getKey();
      id = DsSipMsgParser.getHeader(name);
      switch (id) {
        case FROM:
        case CALL_ID:
        case CSEQ:
        case VIA:
        case RECORD_ROUTE:
        case ROUTE:
        case ACCEPT:
        case ACCEPT_ENCODING:
        case ACCEPT_LANGUAGE:
        case ALLOW:
        case CONTACT:
        case ORGANIZATION:
        case SUPPORTED:
        case USER_AGENT:
          if (ignoreRedirectHeaders) break;
        default:
          TLinkedList list = header.getValues();
          if (list != null) {
            DsSipHeaderString str = (DsSipHeaderString) list.getFirst();
            if (name.equalsIgnoreCase(BS_BODY)) {
              request.setBody(str.getValue(), null);
            } else {
              while (str != null) {
                request.addHeader(
                    new DsSipHeaderString(id, name, str.getValue()),
                    false, // append
                    false); // no clone
                str = (DsSipHeaderString) str.getNext();
              }
            }
          }
          break;
      }
      header = (DsHeaderParameter) header.getNext();
    }
  }

  /*
      public static void main(String [] args)
      {
          try
          {
              byte[] bytes = DsSipHeader.read();
              DsHeaderParameters parameters = new DsHeaderParameters(bytes);
              parameters.startWithDelimiter(false);
              System.out.println();
              System.out.println("<<<<<<<<<<<<<<<<< PARAMETERS >>>>>>>>>>>>>>>>>>>>");
              System.out.println();
              parameters.write(System.out);
              System.out.println();
              System.out.println();
              System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
              System.out.println();
              DsHeaderParameters clone = (DsHeaderParameters) parameters.clone();
              clone.write(System.out);
              System.out.println();
              System.out.println();
              System.out.println("<<<<<<<<<<<<<<<<< (PARAMETERS == CLONE) = "
                                                      + parameters.equals(clone)
                                                      +" >>>>>>>>>>>>>>>>>>>>");
              System.out.println();
              System.out.println();
              System.out.println("<<<<<<<<<<<<<<<<< (CLONE == PARAMETERS) = "
                                                      + clone.equals(parameters)
                                                      +" >>>>>>>>>>>>>>>>>>>>");
              System.out.println();
          }
          catch(Exception e)
          {
              e.printStackTrace();
          }
      }// Ends main()
  */

} // Ends class DsHeaderParameters
