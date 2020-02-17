// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import gnu.trove.TLinkable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Defines the contract for the elements that can be members of the header list in an SIP message.
 * There are three types of header elements defined so far, that can be member of header list in an
 * SIP message. These three types are: <br>
 *
 * <pre>
 *  HEADER  - the member is an {@link DsSipHeader} object
 *  STRING  - the member is an {@link DsSipHeaderString} object
 *  LIST    - the member is an {@link DsSipHeaderList} object
 * </pre>
 *
 * To know the form or type of any concrete implementation of this interface, one can invoke {@link
 * #getForm()} method, that should return any of the above three forms. <br>
 * As the Linked List in the SIP message requires that all its members should implement TLinkable
 * interface, this interface extends from TLinkable. With this, any member of the header list in an
 * SIP message knows its adjacent members.
 */
public interface DsSipHeaderInterface extends TLinkable {
  /** Ennumerated type representing a header. */
  public static final int HEADER = 0;
  /** Ennumerated type representing a string. */
  public static final int STRING = 1;
  /** Ennumerated type representing a list. */
  public static final int LIST = 2;

  /**
   * Tells the header ID of this header.
   *
   * @return the header ID.
   */
  public int getHeaderID();

  /**
   * Returns the complete header name for this header.
   *
   * @return the complete header name for this header.
   */
  public DsByteString getToken();

  /**
   * Tells whether this header is of the same type as the specified <code>header</code>. The two
   * headers are supposed to be of same type if they have the same header name and same integer
   * header ID.
   *
   * @param header the header whose type needs to be compared with this header
   * @return <code>true</code> if this header is of same type as the specified <code>header</code>,
   *     <code>false</code> otherwise.
   */
  public boolean isType(DsSipHeaderInterface header);

  /**
   * Clones this header and returns the cloned object.
   *
   * @return the clone of this header.
   */
  public Object clone();

  /**
   * Writes this header to the specified <code>out</code> output byte stream in its SIP format. <br>
   * If the global option to serialize the headers in the compact form is set, then this header will
   * be serialized with the compact header name, otherwise full header name will be serialized along
   * with the value. <br>
   * It also serializes the EOH character at the end of the header value. Invoke {@link
   * DsSipHeader#setCompact(boolean)} to set or reset this flag.
   *
   * @param out the output stream where this header data (bytes) needs to be serialized.
   * @throws IOException if there is an error while writing to the specified output stream.
   */
  public void write(OutputStream out) throws IOException;

  /**
   * Appends this header to the specified header list. This header is first cloned before appending
   * to the <code>list</code> if the specified flag <code>clone</code> is <code>true</code>. <br>
   * Note: A DsSipHeaderInterface object can not be a member of two lists simultaneously. One should
   * take care in adding headers to the header list.
   *
   * @param list the header list in the SIP message to which this header needs to be appended.
   * @param clone tells whether this header should be cloned before adding to the specified <code>
   *     list</code>. If <code>true</code>, then the clone of this header is appended to the list,
   *     otherwise this header object itself is appended to the list.
   */
  public void appendToList(DsSipHeaderList list, boolean clone);

  /**
   * This is an convenient method that is relevant only in case this header is of <code>LIST</code>
   * form. Tells whether this header list is empty or not.
   *
   * @return <code>true</code> if this header list is empty, <code>false</code> otherwise.
   */
  public boolean isEmpty();

  /**
   * Returns the value of this header. There will be no end of line character or carriage return
   * character at the end of this returned value.
   *
   * @return the DsByteString representation of the value of this header.
   */
  public DsByteString getValue();

  /**
   * Returns the value (No Copy) of this header. There will be no end of line character or carriage
   * return character at the end of this returned value. This is an equivalent to invoking {@link
   * #getValue() getValue()} except that it provides the concrete implementations for not to copy or
   * create a new DsByteString before returning the header value. One possible scenario is in {@link
   * DsSipHeaderString} where this method actually returns reference to its object itself, which is
   * derived from DsByteString. It provides for performance but its toString() and toByteString()
   * methods may not work as expected as DsSipHeaderString class overrides the toString() and
   * toByteString() methods. Invoking any of these overridden methods on the returned value will
   * actually invoke the methods on DsSipHeaderString object itself and these methods will return
   * the header name as well as the header value (name: value).
   *
   * @return the DsByteString representation of the value of this header.
   */
  public DsByteString getValueNC();

  /**
   * Tells the form of this header type.<br>
   * There are three forms of header elements defined so far, that can be member of header list in
   * an SIP message. These three forms are: <br>
   *
   * <pre>
   *  HEADER  - the member is an {@link DsSipHeader} object
   *  STRING  - the member is an {@link DsSipHeaderString} object
   *  LIST    - the member is an {@link DsSipHeaderList} object
   * </pre>
   *
   * @return the form of this header.
   */
  public int getForm();

  /**
   * Tells whether the specified token matches the header name of this header. It should consider
   * the compact header name also.
   *
   * @param token the name of the header to recognize.
   * @return <code>true</code> if the specified token matches this header name, <code>false</code>
   *     otherwise.
   */
  public boolean recognize(DsByteString token);

  /**
   * Writes the value of this header to the specified <code>out</code> output stream.
   *
   * @param out the byte output stream where this header's value will be serialized.
   * @throws IOException if there is an error while writing on to the specified output stream.
   */
  public void writeValue(OutputStream out) throws IOException;

  public void writeEncoded(OutputStream out, DsTokenSipMessageDictionary md) throws IOException;

  // public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md) throws
  // IOException;
}
