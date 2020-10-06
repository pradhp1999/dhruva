// Copyright (c) 2004-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.ByteBuffer;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsParameters;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipContentDispositionHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipContentIdHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipContentLengthHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipContentTypeHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderList;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipStringHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipUnknownHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsMsgParserBase;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipHeaderListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A DsMimeEntity instance represents a MIME entity. It is a new class and it will be a new parent
 * for DsSipMessage.
 */
public class DsMimeEntity
    implements Serializable,
        Cloneable,
        DsSipConstants,
        DsMimeMessageListener,
        DsSipElementListener,
        DsSipHeaderListener {

  ////////////////////////////////////////////////////////////////////////////////
  // static functions
  ////////////////////////////////////////////////////////////////////////////////
  /**
   * Allows for setting what headers should be deeply parsed while parsing a SIP message. User needs
   * to specify an array of integers, where integer values specify the header ids of the headers
   * that need to be deeply parsed during message parsing.<br>
   * For Example, to set for these headers (To, From, Via, CSeq, Call-Id) to be deeply parsed.
   *
   * <blockquote>
   *
   * <pre>
   * int[] headers = {TO,
   *                  From,
   *                  VIA,
   *                  CSEQ,
   *                  CALL_ID};
   * initDeepHeaders(headers);
   * </pre>
   *
   * </blockquote>
   *
   * Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   *
   * @param ids the array of the ids of the headers that need to be deeply parsed.
   */
  public static void initDeepHeaders(int[] ids) {
    for (int j = 0; j < deepHeaders.length; j++) {
      deepHeaders[j] = false;
    }
    int id = 0;
    for (int i = 0; i < ids.length; i++) {
      id = ids[i];
      if (id > -1 && id < deepHeaders.length) {
        deepHeaders[id] = true;
      }
    }
  }

  /**
   * Sets the flag telling whether the headers of same type should be serialized as a comma
   * separated list. This option is false by default.<br>
   * Note: Not supported completely. Need a little more work.
   *
   * @param option if <code>true</code> then the headers of the same type will be serialized as a
   *     comma separated list. Otherwise, will be serialized in their existing state.
   */
  public static void setCommaSeparated(boolean option) {
    s_bCommaSeparated = option;
  }

  /**
   * Tells whether the headers of same type will be serialized as a comma separated list. This
   * option is false by default.<br>
   * Note: Not supported completely. Need a little more work.
   *
   * @return <code>true</code> if the headers of the same type will be serialized as a comma
   *     separated list, false otherwise.
   */
  public static boolean isCommaSeparated() {
    return s_bCommaSeparated;
  }

  /**
   * Sets the flag that tells whether all the headers in the SIP message should be deeply parsed. If
   * set to <code>true</code>, then while parsing a SIP message, all the headers would be deeply
   * parsed and will be of {@link DsSipHeader } type. This flag, if set, takes priority over {@link
   * #initDeepHeaders(int[]) initDeepHeaders()}.
   *
   * @param enable If <code>true</code>, then while parsing a SIP message, all the headers would be
   *     deeply parsed and will be of {@link DsSipHeader } type. Otherwise, only the headers
   *     specified in {@link #initDeepHeaders(int[]) initDeepHeaders()} will be deeply parsed and
   *     again only the first header in a header list would be deeply parsed, not all.
   */
  public static void setParseAllHeaders(boolean enable) {
    s_bAllHeaders = enable;
  }

  /**
   * Tells whether all the headers in the SIP message would be deeply parsed.
   * Returns <code>true</code>, if while parsing a SIP message, all the headers
   * would be deeply parsed and will be of {@link DsSipHeader } type. This flag,
   * if set, takes priority over {@link #initDeepHeaders(int[]) initDeepHeaders()}.
   *
   * @return <code>true</code> if while parsing a SIP message, all the headers
   *         would be deeply parsed and will be of {@link DsSipHeader } type,
   *         <code>false</code> otherwise. If this flag is <code>false<code> then
   *         only the headers specified in {@link #initDeepHeaders(int[]) initDeepHeaders()}
   *         will be deeply parsed and again only the first header in a header list would be
   *         deeply parsed, not all.
   */
  public static boolean isParseAllHeaders() {
    return s_bAllHeaders;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // member functions
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Adds the specified header at the start of the header list (list of same type of headers). This
   * header is first cloned before adding into this SIP message. This operation has no effect if
   * header is DsSipContentLengthHeader as content-length will be calculated by the stack based on
   * message body.<br>
   * Note: To add a list of headers of same type at the beginning of the corresponding header list,
   * use {@link DsSipMessage#addHeaders(DsSipHeaderList)}.
   *
   * @param header the header to be added to the corresponding header list.
   */
  public void addHeader(DsSipHeaderInterface header) {
    addHeader(header, false, true);
  }

  /**
   * Adds the specified header either at the start of the header list (list of same type of headers)
   * or at the end based on the specified flag <code>start</code>. If this flag is true, then the
   * header will be added at the beginning of the list, otherwise at the end of the list. <br>
   * This header is first cloned before adding into this SIP message.<br>
   * This operation has no effect if header is DsSipContentLengthHeader as conent-length will be
   * calculated by the stack based on message body.<br>
   * Note: To add a list of headers of same type, use {@link
   * DsSipMessage#addHeaders(DsSipHeaderList, boolean)}.
   *
   * @param header the header to be added to the corresponding header list.
   * @param start the boolean value indicating whether the header is to be added to the start of the
   *     corresponding header list or at the end.
   */
  public void addHeader(DsSipHeaderInterface header, boolean start) {
    addHeader(header, start, true);
  }

  /**
   * Adds the specified header either at the start of the header list (list of same type of headers)
   * or at the end based on the specified flag <code>start</code>. If this flag is true, then the
   * header will be added at the beginning of the list, otherwise at the end of the list. <br>
   * The header to be added can be of singular type. In that case, there won't be any corresponding
   * header list. We will just add this header or replace the existing header with this header in
   * this message. This operation has no effect if header is DsSipContentLengthHeader as
   * conent-length will be calculated by the stack based on message body.<br>
   * Note: To add a list of headers of same type, use {@link
   * DsSipMessage#addHeaders(DsSipHeaderList, boolean)}.
   *
   * @param header the header to be added to the corresponding header list.
   * @param start the boolean value indicating whether the header is to be added to the start of the
   *     corresponding header list or at the end.
   * @param clone tells whether we should clone this header before adding into this SIP message. If
   *     <code>true</code> then clones it before adding, otherwise adds this header itself to this
   *     message.
   */
  public final void addHeader(DsSipHeaderInterface header, boolean start, boolean clone) {
    if (header != null) {
      int kind = header.getHeaderID();
      if (cannotUpdateHeader(kind)) return;
      if (header.getForm() == DsSipHeaderInterface.LIST) {
        addHeaders((DsSipHeaderList) header, start, clone);
        return;
      }

      DsByteString token = header.getToken();
      if (isSingular(kind)) {
        update(header, clone);
      } else {
        // get the list of headers of this type
        DsSipHeaderList tmp = retrieveList(kind, token, null);
        // at this point we are guarenteed to have a reference to the
        // correct header list.
        // Also whether we need to clone this header before adding,
        // Lets check clone flag.
        if (clone) {
          header = (DsSipHeaderInterface) header.clone();
        } else {
          // If we assume that the previous pointer will be null, then its
          // fine, otherwise nullify here
          header.setPrevious(null);
          header.setNext(null);
        }
        if (start) tmp.addFirst(header); // now add the header to the front
        else tmp.addLast(header); // now add the header to the end
      } // _else _if
    } // _if
  }

  /**
   * Adds all the headers contained in the specified list of headers at the end of the header list
   * (list of same type of headers). The headers will be cloned before adding to the list.<br>
   * Note: To add a single header, use {@link DsSipMessage#addHeader(DsSipHeaderInterface,
   * boolean)}.
   *
   * @param headers The list of headers to be added to the corresponding header list.
   */
  public void addHeaders(DsSipHeaderList headers) {
    addHeaders(headers, false, true);
  }

  /**
   * Adds all the headers contained in the specified list of headers either at the start of the
   * header list (list of same type of headers) or at the end based on the specified flag <code>
   * start</code>. If this flag is true, then the headers will be added at the beginning of the
   * list, otherwise at the end of the list. The headers will be cloned before adding to the list.
   * <br>
   * Note: To add a single header, use {@link DsSipMessage#addHeader(DsSipHeaderInterface,
   * boolean)}.
   *
   * @param headers The list of headers to be added to the corresponding header list.
   * @param start The boolean value indicating whether the headers to be added to the start of the
   *     corresponding header list or at the end.
   */
  public void addHeaders(DsSipHeaderList headers, boolean start) {
    addHeaders(headers, start, true);
  }
  /**
   * Adds all the headers contained in the specified list of headers either at the start of the
   * header list (list of same type of headers) or at the end based on the specified flag <code>
   * start</code>. If this flag is true, then the headers will be added at the beginning of the
   * list, otherwise at the end of the list. If the specified <code>clone</code> flag is set to true
   * then the headers will be cloned before addition, otherwise headers will not be cloned before
   * addition and that would result in removing these headers from the original list, if they are
   * already a member of some other list.<br>
   * Note: To add a single header, use {@link DsSipMessage#addHeader(DsSipHeaderInterface,
   * boolean)}.
   *
   * @param headers The list of headers to be added to the corresponding header list.
   * @param start The boolean value indicating whether the headers to be added to the start of the
   *     corresponding header list or at the end.
   * @param clone if <code>true</code> then the headers to be added are cloned before addition.
   */
  public final void addHeaders(DsSipHeaderList headers, boolean start, boolean clone) {
    if (headers == null || headers.isEmpty()) {
      return;
    }
    DsSipHeaderInterface header = ((DsSipHeaderInterface) headers.getFirst());
    int id = header.getHeaderID();
    if (cannotUpdateHeader(id)) {

      return;
    }
    DsByteString token = header.getToken();
    if (isSingular(id)) {
      update(header, clone);
    } else {
      // where we keep that kind
      DsSipHeaderList l = retrieveList(id, token, null);
      // add them all onto the list
      l.addAll(headers, start, clone);
    }
  }

  /**
   * Removes the first header with the specified <code>header</code> name from the corresponding
   * list of headers and thus from this message. This operation has no effect on CONTENT-LENGTH
   * header, which is calculated based on message body.
   *
   * @param header the name of header that needs to be removed.
   * @return the removed header or null if no header could be removed.
   */
  public DsSipHeaderInterface removeHeader(DsByteString header) {
    return removeHeader(DsSipMsgParser.getHeader(header), header, true);
  }

  /**
   * Removes the header with the specified <code>header</code> name from the corresponding list of
   * headers and thus from this message. This operation has no effect on CONTENT-LENGTH header,
   * which is calculated based on message body. The header can be removed from the beginning or from
   * the end of the corresponding header list, depending on the specified flag <code>start</code>.
   * If this passed flag is true, then header is removed from the beginning of the corresponding
   * header list, otherwise from the end of the list.
   *
   * @param header the name of header that needs to be removed.
   * @param start The boolean value indicating whether the header to be removed from the start of
   *     the corresponding header list or from the end.
   * @return the removed header or null if no header could be removed.
   */
  public DsSipHeaderInterface removeHeader(DsByteString header, boolean start) {
    return removeHeader(DsSipMsgParser.getHeader(header), header, start);
  }

  /**
   * Removes the first header of the specified <code>id</code> type from the corresponding list of
   * headers and thus from this message. This operation has no effect on CONTENT-LENGTH header,
   * which is calculated based on message body.<br>
   * Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * Note: To remove an header of unknown type or the headers that don't have any id assigned, use
   * {@link DsSipMessage#removeHeader(DsByteString)}.
   *
   * @param id the type of header that needs to be removed.
   * @return the removed header or null if no header could be removed.
   */
  public DsSipHeaderInterface removeHeader(int id) {
    // not for unknown headers, must remove individually by name
    if (id == UNKNOWN_HEADER) {
      return null;
    }
    return removeHeader(id, null, true);
  }

  /**
   * Removes the first header of the specified <code>id</code> type from the corresponding list of
   * headers and thus from this message. This operation has no effect on CONTENT-LENGTH header,
   * which is calculated based on message body.<br>
   * The header can be removed from the beginning or from the end of the corresponding header list,
   * depending on the specified flag <code>start</code>. If this passed flag is true, then header is
   * removed from the beginning of the corresponding header list, otherwise from the end of the
   * list. Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * Note: To remove an header of unknown type or the headers that don't have any id assigned, use
   * {@link DsSipMessage#removeHeader(DsByteString, boolean)}.
   *
   * @param id the type of header that needs to be removed.
   * @param start The boolean value indicating whether the header to be removed from the start of
   *     the corresponding header list or from the end.
   * @return the removed header or null if no header could be removed.
   */
  public DsSipHeaderInterface removeHeader(int id, boolean start) {
    // not for unknown headers, must remove individually by name
    if (id == UNKNOWN_HEADER) {
      return null;
    }
    return removeHeader(id, null, start);
  }

  /**
   * Removes the specified header from the corresponding list of headers and thus from this message.
   *
   * @param header the header that needs to be removed.
   * @return the removed header or null if no header could be removed.
   */
  public DsSipHeaderInterface removeHeader(DsSipHeaderInterface header) {
    return remove(header);
  }

  /**
   * Removes all the headers of the specified <code>id</code> type from this message. This operation
   * has no effect on CONTENT-LENGTH header, which is calculated based on message body.<br>
   * Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * Note: To remove an header of unknown type or the headers that don't have any id assigned, use
   * {@link DsSipMessage#removeHeaders(DsByteString)}.
   *
   * @param id the type of which all the header need to be removed.
   * @return the removed header(s) or null if no header could be removed.
   */
  public DsSipHeaderInterface removeHeaders(int id) {
    // not for unknown headers, must remove individually by name
    if (id == UNKNOWN_HEADER) {
      return null;
    }
    return removeHeaders(id, null);
  }

  /**
   * Removes all the headers of the specified <code>name</code> from this message. This operation
   * has no effect on CONTENT-LENGTH header, which is calculated based on message body.<br>
   * Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * Note: To remove an header of known type or the headers that have been assigned an id, use
   * {@link DsSipMessage#removeHeaders(int)}.
   *
   * @param name all the headers with this name needs to be removed.
   * @return the removed header(s) or null if no header could be removed.
   */
  public DsSipHeaderInterface removeHeaders(DsByteString name) {
    DsSipHeaderInterface headers = null;
    if (null != name) {
      headers = removeHeaders(DsSipMsgParser.getHeader(name), name);
    }
    return headers;
  }

  /**
   * Updates the specified header in this message. <br>
   * It removes all the headers, if any, of the same type and adds this specified header to this
   * message. The specified header must not be null. This method has no effect on CONTENT-LENGTH
   * header, which is calculated automatically based on message body. To remove all the headers of
   * the same type use either of these two methods, {@link DsSipMessage#removeHeaders(DsByteString)}
   * for unknown header types and {@link DsSipMessage#removeHeaders(int)} for known header types.
   * The known headers can also be removed by removeHeader(DsByteString), but its recommended to use
   * removeHeaders(int). Each header of known type has been assigned a unique integer value. Refer
   * {@link DsSipConstants} class for the various header ids.<br>
   *
   * @param header the header that needs to be set in this message.
   * @return the old header that is replaced, if any, otherwise return null.
   */
  public DsSipHeaderInterface updateHeader(DsSipHeaderInterface header) {
    return updateHeader(header, true, false);
  }

  /**
   * Updates the specified header in this message. <br>
   * It removes all the headers, if any, of the same type and adds this specified header to this
   * message. The specified header must not be null. This method has no effect on CONTENT-LENGTH
   * header, which is calculated automatically based on message body. To remove all the headers of
   * the same type use either of these two methods, {@link DsSipMessage#removeHeaders(DsByteString)}
   * for unknown header types and {@link DsSipMessage#removeHeaders(int)} for known header types.
   * The known headers can also be removed by removeHeader(DsByteString), but its recommended to use
   * removeHeaders(int). Each header of known type has been assigned a unique integer value. Refer
   * {@link DsSipConstants} class for the various header ids.<br>
   *
   * @param header the header that need to be set in this message.
   * @param clone tells whether we should clone this header before updating into this SIP message.
   *     If <code>true</code> then clones it before updating, otherwise adds this header itself to
   *     this message.
   * @return the old header that is replaced, if any, otherwise return null.
   */
  public final DsSipHeaderInterface updateHeader(DsSipHeaderInterface header, boolean clone) {
    return updateHeader(header, clone, false);
  }

  /**
   * Updates the specified header in this message. <br>
   * It removes all the headers, if any, of the same type and adds this specified header to this
   * message. The specified header must not be null. This method has no effect on CONTENT-LENGTH
   * header, which is calculated automatically based on message body. To remove all the headers of
   * the same type use either of these two methods, {@link DsSipMessage#removeHeaders(DsByteString)}
   * for unknown header types and {@link DsSipMessage#removeHeaders(int)} for known header types.
   * The known headers can also be removed by removeHeader(DsByteString), but its recommended to use
   * removeHeaders(int). Each header of known type has been assigned a unique integer value. Refer
   * {@link DsSipConstants} class for the various header ids.<br>
   *
   * @param header the header that need to be set in this message.
   * @param clone tells whether we should clone this header before updating into this SIP message.
   *     If <code>true</code> then clones it before updating, otherwise adds this header itself to
   *     this message.
   * @return the old header that is replaced, if any, otherwise return null.
   */
  DsSipHeaderInterface updateHeader(DsSipHeaderInterface header, boolean clone, boolean force) {
    if (header == null) {
      // One should use removeHeaders()
      return null;
    }
    int kind = header.getHeaderID();
    if (!force && cannotUpdateHeader(kind)) return null;

    // In case of singular, check for list.
    if (isSingular(kind)) {
      if (header.getForm() == DsSipHeaderInterface.LIST) {
        header = (DsSipHeaderInterface) ((DsSipHeaderList) header).getFirst();
      }
    }
    // In case of list, check for singular.
    else if (header.getForm() != DsSipHeaderInterface.LIST) {
      DsSipHeaderList list = new DsSipHeaderList(kind, header.getToken());
      if (clone) {
        header = (DsSipHeaderInterface) header.clone();
        clone = false;
      }
      list.addLast(header);
      header = list;
    }
    DsSipHeaderInterface hdr = update(header, clone);
    return hdr;
  }

  /**
   * Sets the specified list of headers in this message. <br>
   * It removes all the headers, if any, of the same type as of these specified headers type and
   * adds this specified header list to this message. The specified headers must not be null. This
   * method has no effect on CONTENT-LENGTH header, which is calculated automatically based on
   * message body. To remove all the headers of the same type use either of these two methods,
   * {@link DsSipMessage#removeHeaders(DsByteString)} for unknown header types and {@link
   * DsSipMessage#removeHeaders(int)} for known header types. The known headers can also be removed
   * by removeHeader(DsByteString), but its recommended to use removeHeaders(int). Each header of
   * known type has been assigned a unique integer value. Refer {@link DsSipConstants} class for the
   * various header ids.<br>
   *
   * @param headers the list of headers that need to be set in this message.
   * @return the old header list that is replaced, if any, otherwise return null.
   */
  public DsSipHeaderInterface updateHeaders(DsSipHeaderList headers) {
    return updateHeaders(headers, true);
  }

  /**
   * Sets the specified list of headers in this message. <br>
   * It removes all the headers, if any, of the same type as of these specified headers type and
   * adds this specified header list to this message. The specified headers must not be null. This
   * method has no effect on CONTENT-LENGTH header, which is calculated automatically based on
   * message body. To remove all the headers of the same type use either of these two methods,
   * {@link DsSipMessage#removeHeaders(DsByteString)} for unknown header types and {@link
   * DsSipMessage#removeHeaders(int)} for known header types. The known headers can also be removed
   * by removeHeader(DsByteString), but its recommended to use removeHeaders(int). Each header of
   * known type has been assigned a unique integer value. Refer {@link DsSipConstants} class for the
   * various header ids.<br>
   *
   * @param headers the list of headers that need to be set in this message.
   * @param clone if <code>true</code> then the headers to be added are cloned before updation.
   * @return the old header list that is replaced, if any, otherwise return null.
   */
  public final DsSipHeaderInterface updateHeaders(DsSipHeaderList headers, boolean clone) {
    if (null == headers) {
      return null;
    }

    DsSipHeaderInterface header = headers;
    int kind = headers.getHeaderID();
    if (cannotUpdateHeader(kind)) {

      return null;
    }
    if (isSingular(kind)) {
      header = (DsSipHeaderInterface) headers.getFirst();
    }
    DsSipHeaderInterface hdr = update(header, clone);
    return hdr;
  }

  /**
   * Tells whether there is any header with the specified name present in this message. Return
   * <code>true</code> if present, <code>false</code> otherwise. <br>
   * <b>Note:</b> This is an expensive operation except for the three in-built headers, CSEQ,
   * CALL-ID, CONTENT-LENGTH. If user wants to use any of the corresponding header or headers (if
   * its an header list), then he/she should use {@link #getHeader(DsByteString)} or {@link
   * #getHeaders(DsByteString)} and check for the returned value. If the returned value is null then
   * it means there is/are no such header(s). If its not null, then user can use the returned
   * header(s). But if user is not intended to use the header(s) and just interested in knowing the
   * mere presence of the header(s), then he/she can use this method.
   *
   * @param name the name of header.
   * @return <code>true</code> if one or more header(s) with the specified name is/are present in
   *     this message, <code>false</code> otherwise.
   */
  public boolean hasHeaders(DsByteString name) {
    return hasHeaders(DsMsgParserBase.getHeader(name));
  }

  /**
   * Tells whether there is any header with the specified id present in this message. Return <code>
   * true</code> if present, <code>false</code> otherwise. Each header of known type has been
   * assigned a unique integer value. Refer {@link DsSipConstants} class for the various header ids.
   * <br>
   * <b>Note:</b> This is an expensive operation except for the three in-built headers, CSEQ,
   * CALL-ID, CONTENT-LENGTH. If user wants to use any of the corresponding header or headers (if
   * its an header list), then he/she should use {@link #getHeader(int)} or {@link #getHeaders(int)}
   * and check for the returned value. If the returned value is null then it means there is/are no
   * such header(s). If its not null, then user can use the returned header(s). But if user is not
   * intended to use the header(s) and just interested in knowing the mere presence of the
   * header(s), then he/she can use this method.
   *
   * @param id the id of header.
   * @return <code>true</code> if one or more header(s) with the specified name is/are present in
   *     this message, <code>false</code> otherwise.
   */
  public boolean hasHeaders(int id) {
    if (isInBuilt(id)) {
      return hasInBuiltHeader(id);
    }
    return (getHeader(id) != null);
  }

  /**
   * Retrieves the header at the start of the list of headers of the specified type <code>id</code>.
   * Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * This operation is very expensive for three headers, CALL_ID, CSEQ, and CONTENT-LENGTH, for
   * which a new DsSipHeader instance will be created each time this method is called. To get/set
   * values for these three headers, users should call the corresponding specific methods as
   * mentioned in the class description.<br>
   * Note: To retrieve an header of unknown type or the headers that don't have any id assigned, use
   * {@link DsSipMessage#getHeader(DsByteString)}. If you pass in UNKNOWN_HEADER as the header type,
   * you will get a null back. Unknown headers can only be accessed by name.
   *
   * @param id the id of the header to retrieve.
   * @return the header at the start of the list of headers of the specified type <code>id</code>. A
   *     null is returned if there are no headers of the specified type in this message, or you
   *     passed in UNKNOWN_HEADER.
   */
  public DsSipHeaderInterface getHeader(int id) {
    // UNKNOWN_HEADERs are lists of lists, not lists of headers,
    // does not make sense to return this list here.
    if (id == UNKNOWN_HEADER) {
      return null;
    }
    return getHeader(id, null, true);
  }

  /**
   * Retrieves the header either from the start of the list of headers of the specified type <code>
   * id</code>, or from the end based on the specified flag <code>start</code>. If this flag is
   * true, then the header will be retrieved from the beginning of the list, otherwise from the end.
   * <br>
   * Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * This operation is very expensive for three headers, CALL_ID, CSEQ, and CONTENT-LENGTH, for
   * which a new DsSipHeader instance will be created each time this method is called. To get/set
   * values for these three headers, users should call the corresponding specific methods as
   * mentioned in the class description.<br>
   * Note: To retrieve an header of unknown type or the headers that don't have any id assigned, use
   * {@link DsSipMessage#getHeader(DsByteString, boolean)}. If you pass in UNKNOWN_HEADER as the
   * header type, you will get a null back. Unknown headers can only be accessed by name.
   *
   * @param id the id of the header to retrieve.
   * @param start The boolean value indicating whether the header to be retrieved from the start of
   *     the corresponding header list or from the end.
   * @return the header either from the start of the list of headers of the specified type <code>id
   *     </code>, or from the end based on the specified flag <code>start</code>. A null is returned
   *     if there are no headers of the specified type in this message, or you passed in
   *     UNKNOWN_HEADER.
   */
  public DsSipHeaderInterface getHeader(int id, boolean start) {
    // UNKNOWN_HEADERs are lists of lists, not lists of headers,
    // does not make sense to return this list here.
    if (id == UNKNOWN_HEADER) {
      return null;
    }
    return getHeader(id, null, start);
  }

  /**
   * Retrieves the header from the start of the list of headers of the specified type <code>name
   * </code>. Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * Note: To retrieve an header of known type or the headers that have an id assigned, use {@link
   * DsSipMessage#getHeader(int)}.
   *
   * @param name the name of the header to retrieve.
   * @return the header at the start of the list of headers of the specified type <code>name</code>.
   *     A null is returned if there are no headers of the specified type in this message.
   */
  public DsSipHeaderInterface getHeader(DsByteString name) {
    return (null == name) ? null : getHeader(DsMsgParserBase.getHeader(name), name, true);
  }

  /**
   * Retrieves the header either from the start of the list of headers of the specified type <code>
   * name</code>, or from the end based on the specified flag <code>start</code>. If this flag is
   * true, then the header will be retrieved from the beginning of the list, otherwise from the end.
   * <br>
   * Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * Note: To retrieve an header of known type or the headers that have an id assigned, use {@link
   * DsSipMessage#getHeader(int)}.
   *
   * @param name the name of the header to retrieve.
   * @param start The boolean value indicating whether the header to be retrieved from the start of
   *     the corresponding header list or from the end.
   * @return the header either from the start of the list of headers of the specified type <code>
   *     name</code>, or from the end based on the specified flag <code>start</code>. A null is
   *     returned if there are no headers of the specified type in this message.
   */
  public DsSipHeaderInterface getHeader(DsByteString name, boolean start) {
    return (null == name) ? null : getHeader(DsSipMsgParser.getHeader(name), name, start);
  }

  /**
   * Retrieves the header at the start of the list of headers of the specified type <code>id</code>.
   * <br>
   * If the header to be returned is not parsed deeply during the parsing and construction of this
   * message, then before returning, this header is parsed deeply.<br>
   * The header is retrieved from the header list in this message and if its {@link
   * DsSipHeaderString} type then its {@link DsSipHeaderString#validate()} method is invoked to
   * parse it deeply and replace this parsed header in the message. User can also invoke {@link
   * #getHeader(int)} method first and then, if returned header is required to be deeply parsed, can
   * invoke {@link DsSipHeaderString#validate()} on the returned header. Each header of known type
   * has been assigned a unique integer value. Refer {@link DsSipConstants} class for the various
   * header ids.<br>
   * This operation is very expensive for three headers, CALL_ID, CSEQ, and CONTENT-LENGTH, for
   * which a new DsSipHeader instance will be created each time this method is called. To get/set
   * values for these three headers, users should call the corresponding specific methods as
   * mentioned in the class description.<br>
   * Note: To retrieve an header of unknown type or the headers that don't have any id assigned, use
   * {@link DsSipMessage#getHeaderValidate(DsByteString)}. If you pass in UNKNOWN_HEADER as the
   * header type, you will get a null back. Unknown headers can only be accessed by name.
   *
   * @param id the id of the header to retrieve.
   * @return the header at the start of the list of headers of the specified type <code>id</code>. A
   *     null is returned if there are no headers of the specified type in this message, or you
   *     passed in UNKNOWN_HEADER.
   * @throws DsSipParserException if there is an error while parsing the requested header.
   * @throws DsSipParserListenerException if there is an error condition detected by the requested
   *     header as a Parser Listener, while parsing.
   */
  public DsSipHeader getHeaderValidate(int id)
      throws DsSipParserException, DsSipParserListenerException {
    // UNKNOWN_HEADERs are lists of lists, not lists of headers,
    // does not make sense to return this list here.
    if (id == UNKNOWN_HEADER) {
      return null;
    }
    return getHeaderValidate(id, null, true);
  }

  /**
   * Retrieves the header either from the start of the list of headers of the specified type <code>
   * id</code>, or from the end based on the specified flag <code>start</code>. If this flag is
   * true, then the header will be retrieved from the beginning of the list, otherwise from the end.
   * <br>
   * If the header to be returned is not parsed deeply during the parsing and construction of this
   * message, then before returning, this header is parsed deeply.<br>
   * The header is retrieved from the header list in this message and if its {@link
   * DsSipHeaderString} type then its {@link DsSipHeaderString#validate()} method is invoked to
   * parse it deeply and replace this parsed header in the message. User can also invoke {@link
   * #getHeader(int)} method first and then, if returned header is required to be deeply parsed, can
   * invoke {@link DsSipHeaderString#validate()} on the returned header. Each header of known type
   * has been assigned a unique integer value. Refer {@link DsSipConstants} class for the various
   * header ids.<br>
   * This operation is very expensive for three headers, CALL_ID, CSEQ, and CONTENT-LENGTH, for
   * which a new DsSipHeader instance will be created each time this method is called. To get/set
   * values for these three headers, users should call the corresponding specific methods as
   * mentioned in the class description.<br>
   * Note: To retrieve an header of unknown type or the headers that don't have any id assigned, use
   * {@link DsSipMessage#getHeader(DsByteString)}. If you pass in UNKNOWN_HEADER as the header type,
   * you will get a null back. Unknown headers can only be accessed by name.
   *
   * @param id the id of the header to retrieve.
   * @param start The boolean value indicating whether the header to be retrieved from the start of
   *     the corresponding header list or from the end.
   * @return the header at the start of the list of headers of the specified type <code>id</code>. A
   *     null is returned if there are no headers of the specified type in this message, or you
   *     passed in UNKNOWN_HEADER.
   * @throws DsSipParserException if there is an error while parsing the requested header.
   * @throws DsSipParserListenerException if there is an error condition detected by the requested
   *     header as a Parser Listener, while parsing.
   */
  public DsSipHeader getHeaderValidate(int id, boolean start)
      throws DsSipParserException, DsSipParserListenerException {
    // UNKNOWN_HEADERs are lists of lists, not lists of headers,
    // does not make sense to return this list here.
    if (id == UNKNOWN_HEADER) {
      return null;
    }
    return getHeaderValidate(id, null, start);
  }

  /**
   * Retrieves the header from the start of the list of headers of the specified type <code>name
   * </code>. If the header to be returned is not parsed deeply during the parsing and construction
   * of this message, then before returning, this header is parsed deeply.<br>
   * The header is retrieved from the header list in this message and if its {@link
   * DsSipHeaderString} type then its {@link DsSipHeaderString#validate()} method is invoked to
   * parse it deeply and replace this parsed header in the message. User can also invoke {@link
   * #getHeader(DsByteString)} method first and then, if returned header is required to be deeply
   * parsed, can invoke {@link DsSipHeaderString#validate()} on the returned header. Each header of
   * known type has been assigned a unique integer value. Refer {@link DsSipConstants} class for the
   * various header ids.<br>
   * Note: To retrieve an header of known type or the headers that have an id assigned, use {@link
   * DsSipMessage#getHeaderValidate(int)}.
   *
   * @param name the name of the header to retrieve.
   * @return the header at the start of the list of headers of the specified type <code>name</code>.
   *     A null is returned if there are no headers of the specified type in this message.
   * @throws DsSipParserException if there is an error while parsing the requested header.
   * @throws DsSipParserListenerException if there is an error condition detected by the requested
   *     header as a Parser Listener, while parsing.
   */
  public DsSipHeader getHeaderValidate(DsByteString name)
      throws DsSipParserException, DsSipParserListenerException {
    return (null == name) ? null : getHeaderValidate(DsMsgParserBase.getHeader(name), name, true);
  }

  /**
   * Retrieves the header either from the start of the list of headers of the specified type <code>
   * name</code>, or from the end based on the specified flag <code>start</code>. If this flag is
   * true, then the header will be retrieved from the beginning of the list, otherwise from the end.
   * <br>
   * If the header to be returned is not parsed deeply during the parsing and construction of this
   * message, then before returning, this header is parsed deeply.<br>
   * The header is retrieved from the header list in this message and if its {@link
   * DsSipHeaderString} type then its {@link DsSipHeaderString#validate()} method is invoked to
   * parse it deeply and replace this parsed header in the message. User can also invoke {@link
   * #getHeaderValidate(int, boolean)} method first and then, if returned header is required to be
   * deeply parsed, can invoke {@link DsSipHeaderString#validate()} on the returned header. Each
   * header of known type has been assigned a unique integer value. Refer {@link DsSipConstants}
   * class for the various header ids.<br>
   * Note: To retrieve an header of known type or the headers that have an id assigned, use {@link
   * DsSipMessage#getHeader(int)}.
   *
   * @param name the name of the header to retrieve.
   * @param start The boolean value indicating whether the header to be retrieved from the start of
   *     the corresponding header list or from the end.
   * @return the header either from the start of the list of headers of the specified type <code>
   *     name</code>, or from the end based on the specified flag <code>start</code>. A null is
   *     returned if there are no headers of the specified type in this message.
   * @throws DsSipParserException if there is an error while parsing the requested header.
   * @throws DsSipParserListenerException if there is an error condition detected by the requested
   *     header as a Parser Listener, while parsing.
   */
  public DsSipHeader getHeaderValidate(DsByteString name, boolean start)
      throws DsSipParserException, DsSipParserListenerException {
    return (null == name) ? null : getHeaderValidate(DsMsgParserBase.getHeader(name), name, start);
  }

  /**
   * Retrieves the header either from the start of the list of headers of the specified type <code>
   * name</code>, or from the end based on the specified flag <code>start</code>. If this flag is
   * true, then the header will be retrieved from the beginning of the list, otherwise from the end.
   * <br>
   * If the header to be returned is not parsed deeply during the parsing and construction of this
   * message, then before returning, this header is parsed deeply.<br>
   * The header is retrieved from the header list in this message and if its {@link
   * DsSipHeaderString} type then its {@link DsSipHeaderString#validate()} method is invoked to
   * parse it deeply and replace this parsed header in the message. User can also invoke {@link
   * #getHeaderValidate(int, boolean)} method first and then, if returned header is required to be
   * deeply parsed, can invoke {@link DsSipHeaderString#validate()} on the returned header. Each
   * header of known type has been assigned a unique integer value. Refer {@link DsSipConstants}
   * class for the various header ids.<br>
   * Note: To retrieve an header of known type or the headers that have an id assigned, use {@link
   * DsSipMessage#getHeader(int)}.
   *
   * @param id the id of the header to retrieve.
   * @param name the name of the header to retrieve.
   * @param start The boolean value indicating whether the header to be retrieved from the start of
   *     the corresponding header list or from the end.
   * @return the header either from the start of the list of headers of the specified type <code>
   *     name</code>, or from the end based on the specified flag <code>start</code>. A null is
   *     returned if there are no headers of the specified type in this message.
   * @throws DsSipParserException if there is an error while parsing the requested header.
   * @throws DsSipParserListenerException if there is an error condition detected by the requested
   *     header as a Parser Listener, while parsing.
   */
  public DsSipHeader getHeaderValidate(int id, DsByteString name, boolean start)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipHeader header = null;

    if (isSingular(id)) {
      header = getSingularValidate(id, name);
    } else {
      header = getValidate(id, name, start);
    }
    return header;
  }

  /**
   * Retrieves the list of headers of the specified type <code>id</code>. Each header of known type
   * has been assigned a unique integer value. Refer {@link DsSipConstants} class for the various
   * header ids.<br>
   * Note: To retrieve a list of headers of any unknown type or the headers that don't have any id
   * assigned, use {@link DsSipMessage#getHeaders(DsByteString)}. If you pass in UNKNOWN_HEADER as
   * the header type, you will get a null back. Unknown headers can only be accessed by name.
   *
   * @param id the id of the header to retrieve.
   * @return the list of headers of the specified type <code>id</code>. A null is returned if there
   *     are no headers of the specified type in this message.
   */
  public DsSipHeaderList getHeaders(int id) {
    if (isSingular(id)) {
      throw new IllegalArgumentException(
          "The Singular header can not be "
              + "retrieved through this method. "
              + "Use getHeader(int) instead.");
    }
    DsSipHeaderList l = (DsSipHeaderList) find(id, null);
    return ((l == null) || l.isEmpty()) ? null : l;
  }

  /**
   * Retrieves the list of headers with the specified <code>name</code>. Each header of known type
   * has been assigned a unique integer value. Refer {@link DsSipConstants} class for the various
   * header ids.<br>
   * Note: To retrieve a list of headers of any known type or the headers that have an id assigned,
   * it is recommended to use {@link DsSipMessage#getHeaders(int)}.
   *
   * @param name the name of the header to retrieve.
   * @return the list of headers with the specified <code>name</code>. A null is returned if there
   *     are no headers with the specified name in this message.
   */
  public DsSipHeaderList getHeaders(DsByteString name) {
    return getHeaders(DsMsgParserBase.getHeader(name));

    // TODO: why was this implemenation changed?
    // CAFFEINE 2.0 DEVELOPMENT - original dynamicsoft implementation in commented block
    /*
            if (DsPerf.ON) DsPerf.start(DsPerf.MSG_GET_HEADERS);
            int id = DsSipMsgParser.getHeader(name);
            if (isSingular(id))
            {
                throw new IllegalArgumentException("The Singular header can not be "
                                                + "retrieved through this method. "
                                                + "Use getHeader(int) instead.");
            }
            DsSipHeaderList l = (DsSipHeaderList)find(id, name);
            if (DsPerf.ON) DsPerf.stop(DsPerf.MSG_GET_HEADERS);
            return ((l == null) || l.isEmpty()) ? null :  l;
    */
  }

  /**
   * Retrieves the list of headers of the specified type <code>id</code>. If the headers to be
   * returned are not parsed deeply during the parsing and construction of this message, then before
   * returning, these headers are parsed deeply.<br>
   * It may be possible that user don't want to parse deeply all the headers in the returned list.
   * In that case, user can specify <code>num</code> the number of headers that should be parsed
   * deeply. Also he can specify, whether the required headers to be parsed deeply from the start of
   * the list or from the end, by specifying the flag <code>start</code>. The header is retrieved
   * from the header list in this message and if its {@link DsSipHeaderString} type then its {@link
   * DsSipHeaderString#validate()} method is invoked to parse it deeply and replace this parsed
   * header in the message. User can also invoke {@link #getHeaders(int)} method first and then, if
   * returned header is required to be deeply parsed, can invoke {@link
   * DsSipHeaderString#validate()} on the returned header. Each header of known type has been
   * assigned a unique integer value. Refer {@link DsSipConstants} class for the various header ids.
   * <br>
   * Note: To retrieve a list of headers of any unknown type or the headers that don't have any id
   * assigned, use {@link DsSipMessage#getHeadersValidate(DsByteString, int, boolean)}.
   *
   * @param id the id of the header to retrieve.
   * @param num the number of headers in the returned list that should be parsed deeply.
   * @param start tells whether the headers from the start of the list or from the end of the list,
   *     should be parsed deeply. If <code>true</code> then in the returned list, the <code>num
   *     </code> number of headers from the start will be parsed deeply, otherwise from the end.
   * @return the list of headers of the specified type <code>id</code>. A null is returned if there
   *     are no headers of the specified type in this message.
   * @throws DsSipParserException if there is an error while parsing the requested headers.
   * @throws DsSipParserListenerException if there is an error condition detected by the requested
   *     headers as Parser Listeners, while parsing.
   */
  public DsSipHeaderList getHeadersValidate(int id, int num, boolean start)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipHeaderList list = getHeaders(id);
    if (null != list) list.validate(num, start);
    return list;
  }

  /**
   * Retrieves the list of headers with the specified <code>name</code>. If the headers to be
   * returned are not parsed deeply during the parsing and construction of this message, then before
   * returning, these headers are parsed deeply.<br>
   * It may be possible that user don't want to parse deeply all the headers in the returned list.
   * In that case, user can specify <code>num</code> the number of headers that should be parsed
   * deeply. Also he can specify, whether the required headers to be parsed deeply from the start of
   * the list of from the end, by specifying the flag <code>start</code>. The header is retrieved
   * from the header list in this message and if its {@link DsSipHeaderString} type then its {@link
   * DsSipHeaderString#validate()} method is invoked to parse it deeply and replace this parsed
   * header in the message. User can also invoke {@link #getHeaders(DsByteString)} method first and
   * then, if returned header is required to be deeply parsed, can invoke {@link
   * DsSipHeaderString#validate()} on the returned header. Each header of known type has been
   * assigned a unique integer value. Refer {@link DsSipConstants} class for the various header ids.
   * <br>
   * Note: To retrieve a list of headers of any known type or the headers that have an id assigned,
   * it is recommended to use {@link DsSipMessage#getHeadersValidate(int, int, boolean)}.
   *
   * @param name the name of the header to retrieve.
   * @param num the number of headers in the returned list that should be parsed deeply.
   * @param start tells whether the headers from the start of the list or from the end of the list,
   *     should be parsed deeply. If <code>true</code> then in the returned list, the <code>num
   *     </code> number of headers from the start will be parsed deeply, otherwise from the end.
   * @return the list of headers with the specified <code>name</code>. A null is returned if there
   *     are no headers with the specified name in this message.
   * @throws DsSipParserException if there is an error while parsing the requested headers.
   * @throws DsSipParserListenerException if there is an error condition detected by the requested
   *     headers as Parser Listeners, while parsing.
   */
  public DsSipHeaderList getHeadersValidate(DsByteString name, int num, boolean start)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipHeaderList list = getHeaders(name);
    if (null != list) list.validate(num, start);
    return list;
  }

  /**
   * Retrieves the list of headers of the specified type <code>id</code>. If the headers to be
   * returned are not parsed deeply during the parsing and construction of this message, then before
   * returning, these headers are parsed deeply.<br>
   * All the headers in this list will be parsed.<br>
   * Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * Note: To retrieve a list of headers of any unknown type or the headers that don't have any id
   * assigned, use {@link DsSipMessage#getHeadersValidate(DsByteString)}.
   *
   * @param id the id of the header to retrieve.
   * @return the list of headers of the specified type <code>id</code>. A null is returned if there
   *     are no headers of the specified type in this message.
   * @throws DsSipParserException if there is an error while parsing the requested headers.
   * @throws DsSipParserListenerException if there is an error condition detected by the requested
   *     headers as Parser Listeners, while parsing.
   */
  public DsSipHeaderList getHeadersValidate(int id)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipHeaderList list = getHeaders(id);
    if (list != null) list.validate();
    return list;
  }

  /**
   * Retrieves the list of headers with the specified <code>name</code>. If the headers to be
   * returned are not parsed deeply during the parsing and construction of this message, then before
   * returning, these headers are parsed deeply.<br>
   * All the headers in this list will be parsed.<br>
   * Each header of known type has been assigned a unique integer value. Refer {@link
   * DsSipConstants} class for the various header ids.<br>
   * Note: To retrieve a list of headers of any known type or the headers that have an id assigned,
   * it is recommended to use {@link DsSipMessage#getHeadersValidate(int)}.
   *
   * @param name the name of the header to retrieve.
   * @return the list of headers with the specified <code>name</code>. A null is returned if there
   *     are no headers with the specified name in this message.
   * @throws DsSipParserException if there is an error while parsing the requested headers.
   * @throws DsSipParserListenerException if there is an error condition detected by the requested
   *     headers as Parser Listeners, while parsing.
   */
  public DsSipHeaderList getHeadersValidate(DsByteString name)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipHeaderList list = getHeaders(name);
    if (list != null) list.validate();
    return list;
  }

  /**
   * Retrieves the list of all the headers that are present in this message. It includes the known
   * headers as well as the unknown headers. Each header of known type has been assigned a unique
   * integer value. Refer {@link DsSipConstants} class for the various header ids.<br>
   * Note: To retrieve a list of headers of any known type or the headers that have an id assigned,
   * it is recommended to use {@link DsSipMessage#getHeaders(int)}. To retrieve a list of headers of
   * any unknown type or the headers that don't have any id assigned, use {@link
   * DsSipMessage#getHeaders(DsByteString)}.
   *
   * @return the list of all the headers that are present in this message. An empty list is returned
   *     if there are no headers in this message.
   */
  public DsSipHeaderList getHeaders() {
    DsSipHeaderList headerList = new DsSipHeaderList();
    DsSipHeaderInterface headers = null;
    int len = headerType.length - 1;
    for (int type = 0; type < len; type++) {
      headers = headerType[type];
      if (null != headers) {
        headers.appendToList(headerList, true);
      } // _if
    } // _for
    DsSipHeaderList list = (DsSipHeaderList) headerType[len];
    if (null != list) {
      headers = (DsSipHeaderInterface) list.getFirst();
      while (null != headers) {
        headers.appendToList(headerList, true);
        headers = (DsSipHeaderInterface) headers.getNext();
      } // _while
    } // _if
    return headerList;
  }

  /**
   * Checks for the presence of headers.
   *
   * @return <code>true</code> if any headers are present, <code>false</code> otherwise
   */
  public boolean hasHeaders() {
    int length = headerType.length - 1;
    if (length == 0) {
      return false;
    }
    boolean headerFound = false;
    DsSipHeaderInterface headers = null;
    for (int i = 0; i < length; i++) {
      headers = headerType[i];
      if (null != headers) {
        if (headers instanceof DsSipHeaderList && ((DsSipHeaderList) headers).isEmpty()) {
          continue;
        }
        headerFound = true;
        break;
      }
    }

    while (null != headers) {
      if (headers instanceof DsSipHeaderList && ((DsSipHeaderList) headers).isEmpty()) {
        headers = (DsSipHeaderInterface) headers.getNext();
        continue;
      }
      headerFound = true;
      break;
    }
    return headerFound;
  }

  /**
   * Returns a HashMap of all headers.
   *
   * @return a HashMap of all headers.
   */
  public Map getHeadersMap() {
    HashMap headerMap = new HashMap(32);
    DsByteString name, value;
    DsSipHeaderInterface headers = null;
    int len = headerType.length - 1;
    for (int type = 0; type < len; type++) {
      headers = headerType[type];
      if (null != headers) {
        value = headers.getValue();
        if (null != value) {
          name = DsMsgParserBase.getHeader(type);
          headerMap.put(name, value);
        } // _if
      } // _if
    } // _for
    DsSipHeaderList list = (DsSipHeaderList) headerType[len];
    if (null != list) {
      headers = (DsSipHeaderInterface) list.getFirst();
      while (null != headers) {
        value = headers.getValue();
        if (null != value) {
          name = headers.getToken();
          headerMap.put(name, value);
        } // _if
        headers = (DsSipHeaderInterface) headers.getNext();
      } // _while
    } // _if
    return headerMap;
  }

  /**
   * Returns a HashMap of all headers with header name and value type being String.
   *
   * @return a HashMap of all headers.
   */
  public Map getHeadersStringMap() {
    HashMap headerMap = new HashMap(32);
    String name, value;
    DsSipHeaderInterface headers = null;
    int len = headerType.length - 1;
    for (int type = 0; type < len; type++) {
      headers = headerType[type];
      if (null != headers) {
        value = headers.getValue().toString();
        if (null != value) {
          name = DsMsgParserBase.getHeader(type).toString();
          headerMap.put(name, value);
        } // _if
      } // _if
    } // _for
    DsSipHeaderList list = (DsSipHeaderList) headerType[len];
    if (null != list) {
      headers = (DsSipHeaderInterface) list.getFirst();
      while (null != headers) {
        value = headers.getValue().toString();
        if (null != value) {
          name = headers.getToken().toString();
          headerMap.put(name, value);
        } // _if
        headers = (DsSipHeaderInterface) headers.getNext();
      } // _while
    } // _if
    return headerMap;
  }

  /** @return a String in the form of comma separated key,value pairs for all headers */
  public String getHeadersKeyValueString() {
    int len = headerType.length - 1;
    final int APPROX_SIP_MESSAGE_CHAR_COUNT =
        7000; /* Approximate size of INVITE/100/200/ACK messages in CMR callflows */
    DsByteString name, value;
    DsSipHeaderInterface headers = null;
    StringBuilder result = new StringBuilder(APPROX_SIP_MESSAGE_CHAR_COUNT);
    result.append("");
    String comma = "";
    for (int type = 0; type < len; type++) {
      headers = headerType[type];
      if (null != headers) {
        value = headers.getValue();
        if (null != value) {
          name = DsMsgParserBase.getHeader(type);
          result.append(
              comma
                  + "\""
                  + name.toString()
                  + "\":\""
                  + value.toString().replace("\"", "\\\"")
                  + "\"");
          comma = ",";
        } // _if
      } // _if
    } // _for
    DsSipHeaderList list = (DsSipHeaderList) headerType[len];
    if (null != list) {
      headers = (DsSipHeaderInterface) list.getFirst();
      while (null != headers) {
        value = headers.getValue();
        if (null != value) {
          name = headers.getToken();
          result.append(
              comma
                  + "\""
                  + name.toString()
                  + "\":\""
                  + value.toString().replace("\"", "\\\"")
                  + "\"");
          comma = ",";
        } // _if
        headers = (DsSipHeaderInterface) headers.getNext();
      } // _while
    } // _if
    return result.toString();
  }

  //////////////////////////////////////////////////////////////////////////////////
  //  The following set of methods are convenience functions that can be used
  //  in place of the more general getHeader() and getHeaders() method.
  //////////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the content length of the contents in this message.
   *
   * @return the content length of the contents in this message.
   */
  public int getContentLength() {
    if (m_body == null) return 0;
    return (m_body.getContainingEntityContentLength());
  }

  /**
   * Returns the body as DsByteString, if present in this message, otherwise return null. Note this
   * method call results in serialization which is expensive if the contained body is already in
   * parsed form.
   *
   * @return the body part, if present in this message, otherwise return null.
   * @throws IllegalStateException if for some reason, the body cannot be serialized.
   */
  public DsByteString getBody() {
    try {
      if (m_body == null) return null;
      return m_body.encode().getBytes();
    } catch (DsException ex) {
      messageLogger.warn("Could not serialize the body.", ex);
      throw new IllegalStateException("Could not serialize the body.");
    }
  }

  /**
   * Sets the specified <code>body</code> and the specified content type <code>type</code> of the
   * body for this message. If the specified content type <code>type</code> is null, then the
   * Content-Type header will not be changed in this message.<br>
   * Note: To remove Content-Type header, use {@link #removeHeader(int)}. <br>
   * It is the developers responsibility to make sure that there is a proper Content-Type header
   * that matches the body of the message.
   *
   * @param body the body for the message
   * @param type the content type of the specified body
   */
  public void setBody(DsByteString body, DsByteString type) {
    if (type != null) {
      // update content-type header since we know this is a standalone body
      DsSipContentTypeHeader hdr = null;
      hdr = new DsSipContentTypeHeader();
      DsByteString t = type.toLowerCase();
      t.trim();
      hdr.setMediaType(t);
      updateHeader(hdr);
    }
    if (body == null) {
      m_body = null;
    } else {
      setMimeBody(new DsMimeUnparsedBody(this, body), false);
    }
  }

  /**
   * Sets the specified <code>body</code> and the specified content type <code>type</code> of the
   * body for this message. It will also update the Content-Length header, if present, in this
   * message to reflect the length of the specified body. If the Content-Length header is not
   * already present in this message then a new Content-Length header is constructed and added in
   * this message, depicting the length of the specified body. If the specified content type <code>
   * type</code> is null, then the Content-Type header will not be changed in this message.<br>
   * Note: To remove Content-Type header, use {@link #removeHeader(int)}. <br>
   * It is the developers responsibility to make sure that there is a proper Content-Type header
   * that matches the body of the message.
   *
   * @param body the body for the message
   * @param type the content type of the specified body
   */
  public void setBody(byte[] body, DsByteString type) {
    setBody((body == null ? null : new DsByteString(body)), type);
  }

  /**
   * Returns the type of body present in this message. The Content-Type header is looked for the
   * type of body. If the Content-Type header is present, then the content type specified in this
   * Content-Type header is returned. Otherwise returns null.
   *
   * @return the type of body present in this message.
   */
  public DsByteString getBodyType() {
    DsSipContentTypeHeader ct = null;
    try {
      ct = (DsSipContentTypeHeader) getHeaderValidate(CONTENT_TYPE);
    } catch (Exception exc) {
      messageLogger.warn("Could not get body type - ", exc);
      // What to do? Help less, return null.
      return null;
    }
    return (null != ct) ? ct.getMediaType() : null;
  }

  /**
   * Method used to get the body length. Note this method results in serialization which is
   * expensive.
   *
   * @return the length of the body.
   */
  public int getBodyLength() {
    return getContentLength();
  }

  /**
   * Checks for the presence of body.
   *
   * @return boolean <b>True</b> if the body is present, <b>False</b> otherwise.
   */
  public boolean hasBody() {
    return (m_body != null);
  }

  /**
   * Writes the message to the output stream, and then calls flush().
   *
   * @param out the output stream
   * @throws IOException if there is an exception in writing to the stream
   */
  public void write(OutputStream out) throws IOException {
    writeHeadersAndBody(out);
    out.flush();
  }

  /**
   * Checks for the semantic equality of the headers in this entity against the headers in the
   * specified <code>entity</code> object.
   *
   * @param entity the entity whose headers' semantics needs to be compared for equality against the
   *     semantics of this entity object's headers.
   * @return <code>true</code> if the headers in this entity are semantically equal to the headers
   *     in the specified <code>entity</code>, <code>false</code> otherwise.
   */
  // I made this package protected so that DsHeaderParameters can use it
  // kevmo: 02.24.2006
  // need the method be public access to pass the compilation
  // error because DsHeaderParameters can use it
  public boolean equalsHeaders(DsMimeEntity entity) {
    // Make sure all the headers in both the entities are validated
    validateHeaders();
    entity.validateHeaders();

    int len = headerType.length - 1;
    DsSipHeaderInterface h1 = null;
    DsSipHeaderInterface h2 = null;
    for (int type = 0; type < len; type++) {
      h1 = headerType[type];
      h2 = entity.headerType[type];

      if ((h1 == null && h2 != null) || (h2 == null && h1 != null)) {
        return false;
      }

      if (h1 != null && !h1.equals(h2)) {
        return false;
      } // _if
    } // _for
    // CAFFEINE 2.0 DEVELOPMENT  Check for in-built headers
    if (!equalsInBuiltHeaders(entity)) return false;

    DsSipHeaderList list = (DsSipHeaderList) headerType[len];
    if (list != null) {
      h1 = (DsSipHeaderInterface) list.getFirst();
      while (h1 != null) {
        h2 =
            isSingular(h1.getHeaderID())
                ? entity.getHeader(h1.getToken())
                : entity.getHeaders(h1.getToken());
        if (!h1.equals(h2)) {
          return false;
        }
        h1 = (DsSipHeaderInterface) h1.getNext();
      } // _while
    } // _if
    return true;
  }

  /**
   * Tries to validate all the headers in this entity to be an instance of {@link DsSipHeader}.
   * There could be some headers in this entity that are of {@link DsSipHeaderInterface#STRING}
   * form. This method tries to convert all such headers in the {@link DsSipHeaderInterface#HEADER}
   * form. This is a costly operation and should only be used when required absolutely. One instant
   * of its usage is in {@link #equals(Object)} method.
   */
  private void validateHeaders() {
    int len = headerType.length - 1;
    DsSipHeaderInterface h = null;
    DsSipHeader header = null;
    try {
      for (int type = 0; type < len; type++) {
        h = headerType[type];
        if (null != h) {
          try {
            if (h.getForm() == DsSipHeaderInterface.STRING) {
              DsSipHeaderString str = (DsSipHeaderString) h;
              headerType[type] = str.validate();
            } else if (h.getForm() == DsSipHeaderInterface.LIST) {
              ((DsSipHeaderList) h).validate();
            }
          } catch (Exception exc) {
            // While parsing any String headers. log?
          }
        } // _if
      } // _for
      DsSipHeaderList list = (DsSipHeaderList) headerType[len];
      if (null != list) {
        h = (DsSipHeaderInterface) list.getFirst();
        while (null != h) {
          if (h.getForm() == DsSipHeaderInterface.STRING) {
            DsSipHeaderString str = (DsSipHeaderString) h;
            header = str.validate();
            list.replace(h, header);
            h = header;
          } else if (h.getForm() == DsSipHeaderInterface.LIST) {
            ((DsSipHeaderList) h).validate();
          }
          h = (DsSipHeaderInterface) h.getNext();
        } // _while
      } // _if
    } catch (Exception exc) {
      // While parsing any String headers. log?
    }
  }

  /**
   * Checks for the semantic equality of this entity against the specified <code>comparator</code>
   * entity object.
   *
   * @param comparator the entity whose semantics needs to be compared for equality against the
   *     semantics of this entity object.
   * @return <code>true</code> if this entity is semantically equal to the the specified <code>
   *     comparator</code> entity object, <code>false</code> otherwise.
   */
  public boolean equals(Object comparator) {
    if (this == comparator) return true;
    if (!(comparator instanceof DsMimeEntity)) return false;
    return equals((DsMimeEntity) comparator);
  }

  /**
   * Checks for the semantic equality of this entity against the specified <code>entity</code>
   * object.
   *
   * @param entity the entity whose semantics needs to be compared for equality against the
   *     semantics of this entity object.
   * @return <code>true</code> if this entity is semantically equal to the the specified <code>
   *     entity</code>, <code>false</code> otherwise.
   */
  public boolean equals(DsMimeEntity entity) {
    if (entity == this) return true;
    if (null == entity) return false;

    // check for headers.
    if (!equalsHeaders(entity)) return false;
    // check for body.
    // null = length 0 OK
    if (!equalsBody(entity)) return false;

    return true;
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
    // If we have finalized all the individual items (headers and body)
    // Then we should have the whole message in m_strValue object
    if (m_bFinalized) {
      // Because we know the offset is = 0.
      return m_strValue.data();
    }
    ByteBuffer buffer = ByteBuffer.newInstance(1024);
    try {
      write(buffer);
    } catch (IOException e) {
      // We may never get this exception as we are writing to the
      // byte buffer. Even if we enter here, it may be possible that
      // some of the bytes are already written to this buffer, so
      // just return those bytes only.
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
    // If we have finalized all the individual items (headers and body)
    // Then we should have the whole message in m_strValue object
    if (m_bFinalized) {
      return m_strValue;
    }
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
   * Tells whether this message is already finalised. If yes, then this message can not be
   * manipulated, but can be serialized.
   *
   * @return <code>true</code> if this message is already finalised, <code>false</code> otherwise.
   */
  public boolean isFinalised() {
    return m_bFinalized;
  }

  /**
   * Sets/resets the finalised flag. One should be careful in using this method as by setting this
   * flag to true, this message becomes immutable. And resetting it to false will allow this request
   * to be manipulated, but resetting it to false when its not required to manipulate this message
   * will result in this message serialization overhead.
   *
   * @param finalised <code>true</code> if this message is not required to be manipulated anymore,
   *     <code>false</code> otherwise.
   */
  public void setFinalised(boolean finalised) {
    if (finalised) {
      if (m_strValue == null) {
        m_strValue = toByteString();
      }
    } else {
      m_strValue = null;
    }
    m_bFinalized = finalised;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipMessageListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipHeaderListener getHeaderListener() {
    return this;
  }

  /*
   * javadoc inherited.
   */
  public void messageFound(byte[] buffer, int offset, int count, boolean messageValid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG)
      System.out.println(
          "bodyFound - body = [" + DsByteString.newString(buffer, offset, count) + "]");

    if (m_clHdrVal != -1) // found a content length for this msg
    {
      if ((count > 0) && (m_clHdrVal > 0)) {
        setMimeBody(
            new DsMimeUnparsedBody(
                this, new DsByteString(buffer, offset, Math.min(count, m_clHdrVal))),
            false);
      }
    } else if (count > 0) {
      setMimeBody(new DsMimeUnparsedBody(this, new DsByteString(buffer, offset, count)), false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipHeaderListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("headerBegin = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
    }

    // As we decided to parse these header in this message itself
    switch (headerId) {
      case CONTENT_LENGTH:
        return this;
    }
    return createElementListener(headerId);
  }

  /*
   * javadoc inherited.
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("headerFound - type = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
      System.out.println(
          "headerFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
    }

    // As we decided to parse these header in this message itself
    switch (headerId) {
      case CONTENT_LENGTH:
        return;
    }

    if (header != null) {
      addHeader(header, false, false);
      header = null;
    } else {
      addHeader(
          new DsSipHeaderString(
              headerId, DsSipMsgParser.getHeader(headerId), buffer, offset, count),
          false,
          false);
    }
  }

  /*
   * javadoc inherited.
   */
  public void unknownFound(
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount,
      boolean isValid)
      throws DsSipParserListenerException {
    if (deepHeaders[UNKNOWN_HEADER]) {
      addHeader(
          new DsSipUnknownHeader(
              new DsByteString(buffer, nameOffset, nameCount),
              new DsByteString(buffer, valueOffset, valueCount)),
          false,
          false);
    } else {
      addHeader(
          new DsSipHeaderString(
              UNKNOWN_HEADER, buffer, nameOffset, nameCount, buffer, valueOffset, valueCount),
          false,
          false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println();
    }
    return null;
  }

  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementFound - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println("elementFound - value [offset, count] = [" + offset + ", " + count + "]");
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }
    // As we decided to parse these header in this message itself
    switch (contextId) {
      case CONTENT_LENGTH:
        // won't get here for (m_jainCompatability == true)
        switch (elementId) {
          case SINGLE_VALUE:
            m_clHdrVal = DsSipMsgParser.parseInt(buffer, offset, count);
            break;
        } // _switch
        break;
    } // _switch
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("parameterFound - contextId = [" + contextId + "]");
      System.out.println(
          "parameterFound - name [offset, count] = [" + nameOffset + " ," + nameCount + "]");
      System.out.println(
          "parameterFound - name = ["
              + DsByteString.newString(buffer, nameOffset, nameCount)
              + "]");
      System.out.println(
          "parameterFound - value [offset, count] = [" + valueOffset + ", " + valueCount + "]");
      System.out.println(
          "parameterFound - value = ["
              + DsByteString.newString(buffer, valueOffset, valueCount)
              + "]");
      System.out.println();
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // private member functions
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns true if the header needs to be parsed deeply, and inits the <code>header</code> member
   * variable.
   *
   * @param headerId the ID of the header to check.
   */
  private boolean initHeader(int headerId) {
    // only parse the first header in a list
    if (s_bAllHeaders
        || (headerId < getHeaderPriLevel()
            && deepHeaders[headerId]
            && (headerType[headerId] == null))) {
      header = DsSipHeader.newInstance(headerId);
      return true;
    }
    return false;
  }

  /**
   * Serializes the headers in this message to the specified <code>out</code> output stream.
   *
   * @param out the output stream where the headers need to be serialized.
   * @param clen content length
   * @throws IOException if there is an error while writing to the output stream.
   */
  protected void writeHeaders(OutputStream out, int clen) throws IOException {
    int len = headerType.length - 1;
    DsSipHeaderInterface l = null;
    for (int type = 0; type < len; type++) {
      l = headerType[type];
      if (null != l) {
        l.write(out);
      } // _if
    } // _for

    writeInBuiltHeaders(out, clen);

    DsSipHeaderList list = (DsSipHeaderList) headerType[len];
    if (null != list) {
      l = (DsSipHeaderInterface) list.getFirst();
      while (null != l) {
        l.write(out);
        l = (DsSipHeaderInterface) l.getNext();
      } // _while
    } // _if
    // headers are finished - add the empty header to separate body
    BS_EOH.write(out);
  }

  /**
   * Serializes the Content-Length header to the specified <code>out</code> output stream.
   *
   * @param out the output stream where the Content-Length header needs to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public void writeContentLength(OutputStream out) throws IOException {
    writeContentLength(out, getContentLength());
  }

  /**
   * Serializes the Content-Length header to the specified <code>out</code> output stream.
   *
   * @param out the output stream where the Content-Length header needs to be serialized.
   * @param len the value of the content length.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public static void writeContentLength(OutputStream out, int len) throws IOException {
    BS_CONTENT_LENGTH_TOKEN.write(out);
    if (len == 0) {
      out.write('0');
    } else {
      out.write(DsIntStrCache.intToBytes(len));
    }
    BS_EOH.write(out);
  }

  /**
   * Tells whether the specified header type is a singular header type. There are certain headers
   * that are assumed to be singular, means only one header of that type can be present in a SIP
   * message.
   *
   * @param id the header id.
   * @return <code>true</code> if the specified header type is singular, <code>false</code>
   *     otherwise.
   */
  public static boolean isSingular(int id) {
    switch (id) {
      case TO:
      case FROM:
      case CSEQ:
      case CALL_ID:
      case CONTENT_LENGTH:
      case CONTENT_TYPE:
      case SUBJECT:
        return true;
    }
    return false;
  }

  private DsSipHeaderInterface getHeader(int id, DsByteString name, boolean start) {
    // Check if the header is in-built
    if (isInBuilt(id)) {
      return getInBuiltHeader(id);
    }
    DsSipHeaderInterface header = find(id, name);
    if (null != header) {
      if (!isSingular(id)) {
        DsSipHeaderList l = (DsSipHeaderList) header;
        if (!l.isEmpty()) {
          if (start) {
            header = (DsSipHeaderInterface) l.getFirst();
          } else {
            header = (DsSipHeaderInterface) l.getLast();
          }
        } else // if the list is empty return null
        {
          header = null;
        }
      }
    }
    return header;
  }

  private DsSipHeaderList retrieveList(int kind, DsByteString name, DsSipHeaderList newList) {
    DsSipHeaderList l = null;
    int len = headerType.length - 1;
    if (kind < len) {
      l = (DsSipHeaderList) headerType[kind];
      if (null == l) {
        if (null != newList) {
          headerType[kind] = newList;
          return null;
        } else {
          headerType[kind] = l = new DsSipHeaderList(kind, name);
        }
        return l;
      }
    } else {
      DsSipHeaderList ol = (DsSipHeaderList) headerType[len];
      // If null, then initialize the lists (outer and inner), and
      // return the inner(header list) list.
      if (null == ol) {
        headerType[len] = ol = new DsSipHeaderList();
        if (null != newList) {
          ol.addLast(newList);
          return null;
        } else {
          l = new DsSipHeaderList(kind, name);
          ol.addLast(l);
        }
        return l;
      } else {
        DsSipHeaderInterface link = (DsSipHeaderInterface) ol.getFirst();
        while (null != link) {
          if ((kind == link.getHeaderID())
              && ((kind != UNKNOWN_HEADER) || (link.recognize(name)))) {
            l = (DsSipHeaderList) link;
            return l; // Found the list
          }
          link = (DsSipHeaderInterface) link.getNext();
        } // _while
        // check if we found the list or we need to create one
        if (null == link) {
          if (null != newList) {
            ol.addLast(newList);
          } else {
            l = new DsSipHeaderList(kind, name);
            ol.addLast(l);
          }
          return l;
        } // _if
      } // _else _if
    } // _else _if
    return l;
  } // Ends retrieveList()

  // get, remove, set,
  private final DsSipHeaderInterface find(int kind, DsByteString name) {
    DsSipHeaderInterface l = null;
    int len = headerType.length - 1;
    if (kind < len) {
      l = headerType[kind];
    } else {
      DsSipHeaderList ol = (DsSipHeaderList) headerType[len];
      // If not null, then only we need to search further
      if (null != ol) {
        l = (DsSipHeaderInterface) ol.getFirst();
        while (null != l) {
          if ((kind == l.getHeaderID()) && ((kind != UNKNOWN_HEADER) || (l.recognize(name)))) {
            break; // Found the list
          }
          l = (DsSipHeaderInterface) l.getNext();
        } // _while
      } // _if
    } // _else _if
    return l;
  } // ends find()

  public DsSipHeaderInterface getUnknownHeaders(DsByteString name) {
    return find(DsSipMsgParser.getHeader(name), name);
  }

  private DsSipHeaderInterface removeHeader(int id, DsByteString name, boolean start) {
    if (cannotUpdateHeader(id)) return null;
    DsSipHeaderInterface header = null;
    if (isSingular(id)) {
      header = removeHeaders(id, name);
    } else {
      DsSipHeaderList l = (DsSipHeaderList) find(id, name);
      if (l != null && !l.isEmpty()) {
        if (start) header = (DsSipHeaderInterface) l.removeFirstHeader();
        else header = (DsSipHeaderInterface) l.removeLastHeader();
      }
    }
    return header;
  } // Ends removeHeader()

  /** Remove headers. Used internally. */
  private final DsSipHeaderInterface removeHeaders(int kind, DsByteString name) {
    if (cannotUpdateHeader(kind)) return null;
    if (isInBuilt(kind)) {
      return removeInBuiltHeader(kind);
    }
    DsSipHeaderInterface removed = null;
    int len = headerType.length - 1;
    if (kind < len) {
      removed = headerType[kind];
      headerType[kind] = null;
    } else {
      DsSipHeaderList ol = (DsSipHeaderList) headerType[len];
      // If not null, then only we need to search further
      if (null != ol) {
        DsSipHeaderInterface l = (DsSipHeaderInterface) ol.getFirst();
        while (null != l) {
          if ((kind == l.getHeaderID()) && ((kind != UNKNOWN_HEADER) || (l.recognize(name)))) {
            ol.delete(l);
            removed = l;
            break;
          } // _if
          l = (DsSipHeaderInterface) l.getNext();
        } // _while
      } // _if
    } // _else _if
    return removed;
  } // ends removeHeaders()

  private DsSipHeaderInterface update(DsSipHeaderInterface header, boolean clone) {
    if (null == header) {
      return null;
    }
    int kind = header.getHeaderID();
    // Check if the header is in-built
    if (isInBuilt(kind)) {
      return updateInBuiltHeader(kind, header);
    }
    if (clone) header = (DsSipHeaderInterface) header.clone();
    int len = headerType.length - 1;
    DsSipHeaderInterface l = null;
    if (kind < len) {
      l = headerType[kind];
      headerType[kind] = header;
    } else {
      DsSipHeaderList ol = (DsSipHeaderList) headerType[len];
      // If null, then initialize the lists (outer and inner), and
      // return the inner(header list) list.
      if (null == ol) {
        headerType[len] = ol = new DsSipHeaderList();
        ol.addLast(header);
      } else {
        l = (DsSipHeaderInterface) ol.getFirst();
        while (null != l) {
          if (l.isType(header)) {
            ol.replace(l, header);
            return l; // Found the list
          }
          l = (DsSipHeaderInterface) l.getNext();
        } // _while
        // check if we found and replaced the header or we need to add it.
        if (null == l) {
          ol.addLast(header);
        } // _if
      } // _else _if
    } // _else _if
    return l;
  } // Ends update()

  private DsSipHeader getSingularValidate(int kind, DsByteString name)
      throws DsSipParserException, DsSipParserListenerException {
    // Check if the header is in-built
    if (isInBuilt(kind)) {
      return (DsSipHeader) getInBuiltHeader(kind);
    }
    DsSipHeaderInterface l = null;
    DsSipHeader header = null;
    int len = headerType.length - 1;
    if (kind < len) {
      l = headerType[kind];
      if (null != l) {
        if (l.getForm() == DsSipHeaderInterface.HEADER) {
          header = (DsSipHeader) l;
        } else {
          DsSipHeaderString str = (DsSipHeaderString) l;
          header = DsSipHeader.newInstance(kind, name);
          header.parse(str.data(), str.offset(), str.length());
          headerType[kind] = header;
        }
      }
    } else {
      DsSipHeaderList ol = (DsSipHeaderList) headerType[len];
      // If not null, then only we need to search further
      if (null != ol) {
        l = (DsSipHeaderInterface) ol.getFirst();
        while (null != l) {
          if ((kind == l.getHeaderID()) && ((kind != UNKNOWN_HEADER) || (l.recognize(name)))) {
            if (l.getForm() == DsSipHeaderInterface.HEADER) {
              header = (DsSipHeader) l;
            } else {
              DsSipHeaderString str = (DsSipHeaderString) l;
              header = DsSipHeader.newInstance(kind, name);
              header.parse(str.data(), str.offset(), str.length());
              ol.replace(l, header);
            }
            break; // Found the list
          }
          l = (DsSipHeaderInterface) l.getNext();
        } // _while
      } // _if
    } // _else _if
    return header;
  } // Ends getSingularValidate()

  private DsSipHeader getValidate(int kind, DsByteString name, boolean start)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipHeaderInterface l = null;
    DsSipHeader header = null;
    DsSipHeaderList headers = null;
    int len = headerType.length - 1;
    if (kind < len) {
      l = headerType[kind];
      if (null != l) {
        headers = (DsSipHeaderList) l;
        if (start) header = headers.getFirstHeader();
        else header = headers.getLastHeader();
      }
    } else {
      DsSipHeaderList ol = (DsSipHeaderList) headerType[len];
      // If not null, then only we need to search further
      if (null != ol) {
        l = (DsSipHeaderInterface) ol.getFirst();
        while (null != l) {
          if ((kind == l.getHeaderID()) && ((kind != UNKNOWN_HEADER) || (l.recognize(name)))) {
            headers = (DsSipHeaderList) l;
            if (start) header = headers.getFirstHeader();
            else header = headers.getLastHeader();
            break; // Found the list
          }
          l = (DsSipHeaderInterface) l.getNext();
        } // _while
      } // _if
    } // _else _if
    return header;
  } // Ends getValidate()

  private DsSipHeaderInterface removeSingular(
      DsSipHeaderInterface header, int kind, DsByteString name) {
    // Check if the header is in-built
    if (isInBuilt(kind)) {
      return removeInBuiltHeader(kind);
    }

    DsSipHeaderInterface l = null;
    int len = headerType.length - 1;
    if (kind < len) {
      l = headerType[kind];
      if (l.equals(header)) {
        headerType[kind] = null;
        return l;
      }
      return null;
    } else {
      DsSipHeaderList ol = (DsSipHeaderList) headerType[len];
      // If not null, then only we need to search further
      if (null != ol) {
        l = (DsSipHeaderInterface) ol.getFirst();
        while (null != l) {
          if (l.isType(header)) {
            if (l.equals(header)) {
              ol.delete(l);
              return l;
            }
            return null;
          }
          l = (DsSipHeaderInterface) l.getNext();
        } // _while
      } // _if
    } // _else _if
    return null;
  } // Ends removeSingular()

  /**
   * Removes the specified header from the corresponding list of headers and thus from this message.
   *
   * @param header the header that needs to be removed.
   * @return the removed header or null if no header could be removed.
   */
  private DsSipHeaderInterface remove(DsSipHeaderInterface header) {
    if (header != null) {
      int kind = header.getHeaderID();
      if (cannotUpdateHeader(kind)) return null;
      DsByteString token = header.getToken();
      if (isSingular(kind)) {
        return removeSingular(header, kind, token);
      } else {
        DsSipHeaderInterface l = null;
        DsSipHeaderList ol = null;
        int len = headerType.length - 1;
        if (kind < len) {
          ol = (DsSipHeaderList) headerType[kind];
          if (null != ol) {
            l = (DsSipHeaderInterface) ol.getFirst();
            while (null != l) {
              if (l.equals(header)) {
                ol.delete(l);
                return l;
              }
              l = (DsSipHeaderInterface) l.getNext();
            } // _while
          } // _if
        } // _if
        else {
          ol = (DsSipHeaderList) headerType[len];
          // If not null, then only we need to search further
          if (null != ol) {
            DsSipHeaderInterface ll = (DsSipHeaderInterface) ol.getFirst();
            while (null != ll) {
              if (ll.getForm() == DsSipHeaderInterface.LIST) {
                DsSipHeaderList tmp = (DsSipHeaderList) ll;
                l = (DsSipHeaderInterface) tmp.getFirst();
                while (null != l) {
                  if (l.equals(header)) {
                    tmp.delete(l);
                    return l;
                  }
                  l = (DsSipHeaderInterface) l.getNext();
                } // _while
              } else if (ll.equals(header)) {
                ol.delete(ll);
                return ll;
              }
              ll = (DsSipHeaderInterface) ll.getNext();
            } // _while
          } // _if
        } // _else _if
      } // _else _if
    } // _if
    return null;
  } // Ends removeHeader()

  ////////////////////////////////////////////////////////////////////////////////
  // Data
  ////////////////////////////////////////////////////////////////////////////////
  /** A reference to the DsMimeBody */
  protected DsMimeBody m_body;

  /** The constant flag to turn on/off the debug traces for the parser event notifications. */
  public static final boolean DEBUG = false;
  /** Tells what headers need to be deep parsed. */
  protected static boolean[] deepHeaders;

  protected DsByteString m_strValue;
  protected boolean m_bFinalized;
  protected DsSipHeaderInterface headerType[];

  /** Internal representation of the Content-Length header, used to set the body length. */
  private int m_clHdrVal = -1;

  /**
   * Flag that tells whether the headers of same type could be serialized as a comma separated list.
   * <br>
   * Note: Not supported completely. Need a little more work.
   */
  private static boolean s_bCommaSeparated = false;

  /** Flag that tells whether all the headers should be deeply parsed while parsing a message. */
  private static boolean s_bAllHeaders;

  // DsSipHeader holder used by this message while serving as an Header Listener
  protected DsSipHeader header;

  // Initializes the deep headers bit set, that tells which headers should be
  // deeply parsed.
  static {
    // Initialize default deep headers
    deepHeaders = new boolean[UNKNOWN_HEADER + 1];
  }

  // /**
  //  * Just for testing. Would be commented out later.
  //  * @param args input arguments.
  //  */

  /*
      public static void main(String[] args)
      {
          // register types
          DsMimeMultipartAlternative.registerType();
          DsMimeMultipartMixed.registerType();
          DsSdpMsg.registerType();

          byte[] msgBytes = (
              "Content-Type: multipart/alternative;boundary=godzilla\r\n" +
              "Content-ID: this-is-a-unique-id\r\n" +
                      "\r\n" +
                  "--godzilla\r\n" +
                      "Content-Type: text/plain\r\n" +
                      "Content-Disposition: session;handling=required\r\n" +
                      "\r\n" +
                      "How are you doing\r\n" +
                  "--godzilla\r\n" +
                      "Content-Type: text/html\r\n" +
                      "Content-Disposition: session;handling=optional\r\n" +
                      "\r\n" +
                      "hello world\r\n" +
                  "--godzilla--\r\n").getBytes();

          byte[] sdpBytes = (
                  "v=0\r\n" +
                  "o=bell 628504043 608504006 IN IP4 128.25.25.21\r\n" +
                  "s=Example program......\r\n" +
                  "c=IN IP4 me.com\r\n" +
                  "e=example@example.com\r\n" +
                  "m=video 3456 RTP/AVP 17\r\n").getBytes();

          DsMimeEntity e = new DsMimeEntity(msgBytes);
          System.out.println("Unparsed body is -\r\n" + e.getMimeBody());
          System.out.println("");

          e.parseBody(PARSE_ALL);

          DsSdpMsg sdp = new DsSdpMsg(sdpBytes);

          e.addBodyPart(sdp);
          System.out.println("After adding SDP -\r\n" + e);
  //        e.parseBody(PARSE_CONTAINERS_ONLY);
  //        e.parseBody(PARSE_DIRECT_CHILDREN_ONLY);

  //        DsMimeMultipartMixed mixed = (DsMimeMultipartMixed) e.getMimeBody();
  //
  //        System.out.println("Mixed body boundary: " + mixed.getBoundary());
  //        System.out.println("Mixed body preamble: " + mixed.getPreamble());
  //        System.out.println("Mixed body epilog: " + mixed.getEpilog());
  //        System.out.println("Mixed body part count: " + mixed.getPartCount());
  //
  //        System.out.println("\n\n====== Mixed body part 1 body type: " + mixed.getPart(0).getMimeBody().getClass().getName());
  //        System.out.println("****** Mixed body part 1: \n" + mixed.getPart(0).getMimeBody());
  //
  //        mixed.getPart(0).parseBody(PARSE_ALL);
  //        DsMimeMultipartAlternative ma = (DsMimeMultipartAlternative)mixed.getPart(0).getMimeBody();
  //
  //        System.out.println("\n\nAlternative body boundary: " + ma.getBoundary());
  //        System.out.println("Alternative body preamble: " + ma.getPreamble());
  //        System.out.println("Alternative body epilog: " + ma.getEpilog());
  //        System.out.println("Alternative body part count: " + ma.getPartCount());
  //
  //        System.out.println("\n\n====== Alternative body part 1 body type: \n" + ma.getPart(0).getMimeBody().getClass().getName());
  //        System.out.println("****** Alternative body part 1: \n" + ma.getPart(0));
  //        System.out.println("\n\n====== Alternative body part 2 body type: \n" + ma.getPart(1).getMimeBody().getClass().getName());
  //        System.out.println("****** Alternative body part 2: \n" + ma.getPart(1));
  //
  //        System.out.println("\n\n====== Mixed body part 2 body type: \n" + mixed.getPart(1).getMimeBody().getClass().getName());
  //        System.out.println("****** Mixed body part 2: \n" + mixed.getPart(1));
  //
      }
  */

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // All the code below this line is unique to the Caffeine Stack.
  //
  // This code can be either merged in logically with the code above, or grouped below as a whole.
  // In either case, the purpose of the code should be documented and noted as appropriate
  //

  ////////////////////////
  //      CONSTRUCTORS
  ////////////////////////

  /**
   * Constructor for subclass. The MIME body contained in this entity will be set to null.
   *
   * @param priLevel header priority level
   */
  protected DsMimeEntity(int priLevel) {
    headerType = new DsSipHeaderInterface[priLevel + 1];
  }

  /** Default constructor. The MIME body contained in this entity will be set to null. */
  public DsMimeEntity() {
    this(1);
  }

  /**
   * Constructor that takes byte array argument.
   *
   * @param bytes body as byte array
   */
  public DsMimeEntity(byte[] bytes) throws DsSipParserException, DsSipParserListenerException {
    this(bytes, 0, bytes.length);
  }

  /**
   * Constructor that takes byte array argument.
   *
   * @param bytes body as byte array
   * @param offset starting point of the byte array
   * @param count number of bytes to be included
   */
  public DsMimeEntity(byte[] bytes, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    this();
    DsMimeMsgParser.parse(this, bytes, offset, count);
  }

  /**
   * Constructor that takes DsByteString argument.
   *
   * @param bytes body as DsByteString
   */
  public DsMimeEntity(DsByteString bytes)
      throws DsSipParserException, DsSipParserListenerException {
    this(bytes.toByteArray(), 0, bytes.length());
  }

  /**
   * Constructor that takes MIME body.
   *
   * @param body MIME body
   */
  public DsMimeEntity(DsMimeBody body) {
    this();
    setMimeBody(body);
  }

  ////////////////////////
  //      METHODS
  ////////////////////////

  /**
   * Start visitation by accepting a visitor and matcher (or in the other sense, a filter).
   *
   * @param visitor entity visitor
   * @param matcher entity matcher (in the other sense, filter)
   * @throws for parsing errors during the visitation
   */
  public void acceptVisitor(DsMimeEntityVisitor visitor, DsMimeEntityMatcher matcher)
      throws DsSipParserException {
    if (visitor == null) return;
    if (matcher == null) matcher = DsMimeMatchAllMatcher.getInstance();
    int retCode = DsMimeEntityVisitor.CONTINUE_VISIT;

    // visit this entity first
    if ((matcher.matches(this))
        && ((retCode = visitor.visit(this)) == DsMimeEntityVisitor.STOP_VISIT)) {
      return;
    }

    // traverse the body (and its body parts)
    if (m_body == null) return;
    DsMimeEntity entity = null;
    DsMimeEntityTraverser traverser = m_body.traverser();
    while (traverser.hasNext()) {
      if (matcher.matches(entity = (DsMimeEntity) traverser.next())
          && (retCode = visitor.visit(entity)) == DsMimeEntityVisitor.STOP_VISIT) {
        return;
      }
    }
  }

  /**
   * Returns the entity traverser. Note this method creates is a wrapper that traverses this entity
   * and its body. Therefore, it introduces an additional object creation.
   *
   * @return the entity traverser.
   * @throws for parsing errors during the visitation
   */
  public DsMimeEntityTraverser traverser() throws DsSipParserException {
    return new Traverser();
  }

  /**
   * Returns the MIME body as it is, parsed or not.
   *
   * @return the MIME body as it is, parsed or not.
   */
  public DsMimeBody getMimeBody() {
    return m_body;
  }

  /**
   * Returns the MIME body in parsed form if parser exists for the content type. If it is of type
   * DsMimeUnparsedBody, then it is first parsed, and the parsed result replaces the unparsed body.
   * Then it returns the body just as getMimeBody does.
   *
   * @throws DsSipParserException thrown if there is anything wrong with parsing.
   */
  public DsMimeBody getMimeBodyValidate() throws DsSipParserException {
    parseBody(PARSE_DIRECT_CHILDREN_ONLY);
    return m_body;
  }

  /**
   * Replaces the MIME body of this entity with specified MIME body.
   *
   * @param mimeBody the new MIME body
   */
  public void setMimeBody(DsMimeBody mimeBody) {
    setMimeBody(mimeBody, true);
  }

  /**
   * Replaces the MIME body of this entity with specified MIME body. The cross references between
   * the entity and the bodies (old and new) will be updated as well. This method is currently used
   * internally by the stack and MAY be made public to application code in the future. However, that
   * should be used with great caution since it may mess up content-type header and the entity's
   * actual type.
   *
   * @param mimeBody the new MIME body
   * @param updateHeader whether to update Content-Type header
   */
  private synchronized void setMimeBody(DsMimeBody mimeBody, boolean updateHeader) {
    if (m_body == mimeBody) return;
    if (m_body != null) m_body.setContainingEntity(null);
    if (mimeBody != null) {
      if (!updateHeader) {
        mimeBody.setContainingEntity(this);
        m_body = mimeBody;
        return;
      }
      DsByteString type = mimeBody.getContainingEntityContentType();
      DsParameters params = mimeBody.getContainingEntityContentTypeParameters();
      if (type == null) {
        // check if it is registered
        type = DsMimeContentManager.getRegisteredContentTypeByClass(mimeBody.getClass());
      }
      mimeBody.setContainingEntity(this);
      // update content-type header
      DsSipContentTypeHeader hdr = new DsSipContentTypeHeader();
      hdr.setMediaType(type);
      hdr.setParameters(params);
      updateHeader(hdr, false, true);
    }
    m_body = mimeBody;
  }

  /**
   * Adds the specified body as a part of this MIME entity. If the entity has no body, assigns this
   * body to it. If a body is already present, and is of type DsMimeMultipartMixed, then this part
   * is wrapped with an empty MIME entity and inserted in existing body. If a body of some other
   * type is present, a new DsMimeMultipartMixed body is created, the existing body is added to it,
   * and the new body is added to it. This then replaces the existing body. The cross references
   * between bodies and entities will be updated.
   *
   * @param body MIME body to be added
   * @throws DsSipParserException thrown if there is anything wrong with parsing.
   */
  public void addBodyPart(DsMimeBody body) throws DsSipParserException {
    if (m_body == null) {
      setMimeBody(body);
      return;
    }
    DsByteString type = m_body.getContainingEntityContentType();
    if (DsMimeContentManager.MIME_MT_MULTIPART_MIXED.equalsIgnoreCase(type)) {
      // Make sure that the body is in parsed form, otherwise,
      // it will cause java.lang.ClassCastException
      parseBody(PARSE_DIRECT_CHILDREN_ONLY);
      DsMimeMultipartMixed mixed = (DsMimeMultipartMixed) m_body;
      mixed.addPart(body);
    } else {
      // create a new entity wrapper for the old body and move content-* headers
      // from this class to the new entity
      DsMimeEntity e = new DsMimeEntity(m_body);
      e.updateHeader(removeHeader(CONTENT_TYPE));
      e.updateHeader(removeHeader(CONTENT_DISPOSITION));
      e.updateHeader(removeHeader(CONTENT_ID));

      // create a new multipart/mixed and add the two parts in
      DsMimeMultipartMixed newBody = new DsMimeMultipartMixed();
      newBody.addPart(e);
      newBody.addPart(body);
      setMimeBody(newBody);
    }
  }

  /**
   * Returns the first MIME body that satisfies the provided matcher. Returns null if no such body
   * part is present. Note that except for containers, this method does not parse except for
   * containers.
   *
   * @param matcher entity matcher
   * @throws DsSipParserException thrown if there is anything wrong with parsing.
   */
  public DsMimeBody findBodyPart(DsMimeEntityMatcher matcher) throws DsSipParserException {
    parseBody(PARSE_CONTAINERS_ONLY);
    acceptVisitor(threadLocalVisitor, matcher);
    DsMimeEntity entity = threadLocalVisitor.getEntity();
    return (entity == null ? null : entity.getMimeBody());
  }

  /**
   * Returns the first MIME body with specified type and disposition. Returns null if no such body
   * part is present. Note that except for containers, this method does not parse. It is a
   * convenience method that will be implemented using the visitor pattern.
   * findBodyPart(DsMimeEntityMatcher) is preferred over this method if a combination of type and
   * disposition is expected to be searched multiple times, in which case, a DsMimeAndMatcher should
   * be created, taking DsMimeContentTypeMatcher and DsMimeDispositionMatcher as arguments.
   *
   * @param type content type
   * @param disposition content disposition type
   * @throws DsSipParserException thrown if there is anything wrong with parsing.
   */
  public DsMimeBody findBodyPart(DsByteString type, DsByteString disposition)
      throws DsSipParserException {
    DsMimeEntityMatcher matcher = DsMimeAndMatcher.getTypeAndDispositionMatcher(type, disposition);
    return findBodyPart(matcher);
  }

  /**
   * Returns the MIME body that satisfies the matcher. Unlike findBodyPart, this attempts to parse
   * the found body part, if a parser is available.
   *
   * @param matcher entity matcher
   * @throws DsSipParserException thrown if there is any wrong with parsing.
   */
  public DsMimeBody findBodyPartValidate(DsMimeEntityMatcher matcher) throws DsSipParserException {
    parseBody(PARSE_CONTAINERS_ONLY);
    acceptVisitor(threadLocalVisitor, matcher);
    DsMimeEntity entity = threadLocalVisitor.getEntity();
    if (entity == null) return null;
    entity.parseBody(PARSE_DIRECT_CHILDREN_ONLY);
    return entity.getMimeBody();
  }

  /**
   * Returns the MIME body with specified type and disposition. Unlike findBodyPart, this attempts
   * to parse the found body part, if a parser is available.
   * findBodyPartValidate(DsMimeEntityMatcher) is preferred over this method if a combination of
   * type and disposition is expected to be searched multiple times, in which case, a
   * DsMimeAndMatcher should be created, taking DsMimeContentTypeMatcher and
   * DsMimeDispositionMatcher as arguments.
   *
   * @param type content type
   * @param disposition content disposition type
   * @throws DsSipParserException thrown if there is any wrong with parsing.
   */
  public DsMimeBody findBodyPartValidate(DsByteString type, DsByteString disposition)
      throws DsSipParserException {
    DsMimeEntityMatcher matcher = DsMimeAndMatcher.getTypeAndDispositionMatcher(type, disposition);
    return findBodyPartValidate(matcher);
  }

  /**
   * Parse MIME body with different levels.
   *
   * @param instruction a constant that tells the stack how to parse the body. One of the predefined
   *     constants, PARSE_ALL, PARSE_CONTAINERS_ONLY, PARSE_DIRECT_CHILDREN_ONLY.
   */
  public void parseBody(int instruction) throws DsSipParserException {
    if (instruction == PARSE_NONE) return;
    DsByteString type = getContentType();
    DsMimeContentProperties props = DsMimeContentManager.getProperties(type);
    if (props == null) {
      messageLogger.info(
          "In DsMimeEntity.parseBody() - Content type "
              + type
              + " is unregistered, skipping parsebody");

      return;
    }
    switch (instruction) {
      case PARSE_ALL:
        parseBody(PARSE_DIRECT_CHILDREN_ONLY);
        if (props.isContainer()) {
          DsMimeEntityTraverser traverser = m_body.traverser();
          while (traverser.hasNext()) {
            traverser.next().parseBody(PARSE_ALL);
          }
        }
        break;
      case PARSE_CONTAINERS_ONLY:
        if (!props.isContainer()) return;
        parseBody(PARSE_DIRECT_CHILDREN_ONLY);
        DsMimeEntityTraverser traverser = m_body.traverser();
        while (traverser.hasNext()) {
          traverser.next().parseBody(PARSE_CONTAINERS_ONLY);
        }
        break;
      case PARSE_DIRECT_CHILDREN_ONLY:
        if (m_body != null && !m_body.isParsed()) {
          props.getParser().parse(this);
        }
        break;
    }
  }

  /**
   * Returns the type of body present in this message. Same as getBodyType()
   *
   * @return the type of body present in this message.
   */
  public DsByteString getContentType() {
    return getBodyType();
  }

  /**
   * Returns the Content-Type header's named parameter.
   *
   * @param name parameter name
   * @return the Content-Type header's named parameter.
   */
  public DsByteString getContentTypeParameter(DsByteString name) {
    try {
      DsSipContentTypeHeader hdr = (DsSipContentTypeHeader) getHeaderValidate(CONTENT_TYPE);
      return (hdr == null ? null : hdr.getParameter(name));
    } catch (Throwable ex) {
      messageLogger.warn("Could not get content type parameter: " + name, ex);
      return null;
    }
  }

  /**
   * Returns the Content-Type header's parameters.
   *
   * @return the Content-Type header's parameters.
   */
  public DsParameters getContentTypeParameters() {
    try {
      DsSipContentTypeHeader hdr = (DsSipContentTypeHeader) getHeaderValidate(CONTENT_TYPE);
      return (hdr == null ? null : hdr.getParameters());
    } catch (Throwable ex) {
      messageLogger.warn("Could not get content type parameters.", ex);
      return null;
    }
  }

  /**
   * Returns the Content-Disposition header value.
   *
   * @return the Content-Disposition header value.
   */
  public DsByteString getDispositionType() {
    try {
      DsSipContentDispositionHeader hdr =
          (DsSipContentDispositionHeader) getHeaderValidate(CONTENT_DISPOSITION);
      if (hdr != null) return hdr.getType();

      // return default disposition if there is no content-disposition header
      DsMimeContentProperties p = DsMimeContentManager.getProperties(getContentType());
      return (p == null ? null : p.getDefaultDisposition());
    } catch (Throwable ex) {
      messageLogger.warn("Could not get disposition type", ex);
      return null;
    }
  }

  /**
   * Returns the Content-Disposition header's handling parameter value.
   *
   * @return the Content-Disposition header's handling parameter value.
   */
  public DsByteString getDispositionHandling() {
    try {
      DsSipContentDispositionHeader hdr =
          (DsSipContentDispositionHeader) getHeaderValidate(CONTENT_DISPOSITION);
      return (hdr == null ? null : hdr.getParameter(BS_HANDLING));
    } catch (Throwable ex) {
      messageLogger.warn("Could not get disposition handling", ex);
      return null;
    }
  }

  /**
   * Returns the Content-Id header value.
   *
   * @return the Content-Id header value.
   */
  public DsByteString getContentId() {
    try {
      DsSipContentIdHeader hdr = (DsSipContentIdHeader) getHeaderValidate(CONTENT_ID);
      return (hdr == null ? null : hdr.getContentId());
    } catch (Throwable ex) {
      messageLogger.warn("Could not get content ID.", ex);
      return null;
    }
  }

  /**
   * Find out if the message has the header specified by <code>headerName</code>, with the option
   * tag specified by <code>tagName</code>
   *
   * @param headerName The header to be checked
   * @param tagName The option tag to be checked
   * @return true, if the message has the option tag (specified by <code>tagName</code> in the
   *     header (specified by <code>headerName</code>)
   */
  public boolean headerContainsTag(byte headerName, DsByteString tagName) {
    DsSipHeaderList sipHeaders = null;
    DsSipStringHeader headerStr = null;
    Iterator iterator = null;
    Object obj = null;

    try {
      sipHeaders = getHeadersValidate(headerName);
    } catch (Exception exc) {
      messageLogger.warn("headerContainsTag(): Can't parse the header - ", exc);
      return false;
    }
    if (sipHeaders != null) {
      iterator = sipHeaders.listIterator();
      try {
        while (iterator.hasNext()) {
          obj = iterator.next();
          if (obj instanceof DsSipStringHeader) {
            headerStr = (DsSipStringHeader) obj;
            if (headerStr.containsTag(tagName) == true) {
              return true;
            }
          }
        }
      } catch (Exception exc) {
        messageLogger.warn("headerContainsTag(): Can't parse the header - ", exc);
        return false;
      }
    }
    return false;
  }

  /**
   * Checks the equality of in-built headers.
   *
   * @param entity MIME entity
   * @return true if in-built headers are equal.
   */
  protected boolean equalsInBuiltHeaders(DsMimeEntity entity) {
    // Content-Lenght is the only in-built header for DsMimeEntity,
    // but will not be compared for entity equlity, since we will
    // compare the body.
    return true;
  }

  /**
   * Compares the equality of entity body. This cause serialization of both entity bodies so this
   * method call is expensive.
   *
   * @param entity MIME entity whose body is to be compared with the body in this entity
   * @return true if the serialized bodies are the same.
   */
  private boolean equalsBody(DsMimeEntity entity) {
    DsByteString thisBody = null;
    if (m_body != null) {
      try {
        DsMimeUnparsedBody unparsed = m_body.encode();
        thisBody = unparsed.getBytes();
      } catch (Throwable ex) {
      }
    }
    DsByteString otherBody = null;
    if (entity != null && entity.m_body != null) {
      try {
        DsMimeUnparsedBody unparsed = entity.m_body.encode();
        otherBody = unparsed.getBytes();
      } catch (Throwable ex) {
      }
    }
    if (DsByteString.compareIgnoreNull(thisBody, otherBody) != 0) return false;
    return true;
  }

  /** For code reuse. Used by write(OutputStream). */
  protected void writeHeadersAndBody(OutputStream out) throws IOException {
    DsMimeUnparsedBody unparsed = null;
    if (m_body != null) {
      try {
        unparsed = m_body.encode();
      } catch (DsException ex) {
        messageLogger.warn("Could not serialize entity body.", ex);
        throw new IOException("Could not serialize entity body.");
      }
    }
    writeHeaders(out, (unparsed == null ? 0 : unparsed.getContainingEntityContentLength()));
    if (unparsed != null) unparsed.write(out);
  }

  protected boolean isInBuilt(int id) {
    return (id == CONTENT_LENGTH);
  }

  protected boolean hasInBuiltHeader(int id) {
    return (id == CONTENT_LENGTH);
  }

  protected DsSipHeaderInterface getInBuiltHeader(int id) {
    if (id == CONTENT_LENGTH) {
      return new DsSipContentLengthHeader(getContentLength());
    }
    return null;
  }

  protected DsSipHeaderInterface updateInBuiltHeader(int id, DsSipHeaderInterface header) {
    return null;
  }

  protected DsSipHeaderInterface removeInBuiltHeader(int id) {
    return null;
  }

  /**
   * Writes in built headers to output stream.
   *
   * @param out output stream
   * @param len content length
   */
  protected void writeInBuiltHeaders(OutputStream out, int len) throws IOException {
    // CSCsm39865: content length added to multipart mime sections for SDP
    // I moved this call to DsSipMessageBase so that Content-Length does not appear in MIME parts
    // writeContentLength (out, len);
  }

  /**
   * Helper instance method to take advantage of polymorphism. Must be reimplemented in subclasses.
   */
  protected int getHeaderPriLevel() {
    return 0;
  }

  /**
   * Returns true if updating the specified header is disallowed.
   *
   * @param id header id
   * @return true if updating the specified header is disallowed.
   */
  private boolean cannotUpdateHeader(int id) {
    if (id == CONTENT_TYPE && m_body != null && m_body.isParsed()) {
      return true;
    }
    return false;
  }

  ////////////////////////
  //      LISTENERS
  ////////////////////////

  /**
   * Create element listener based on header id.
   *
   * @param headerId header id
   * @return element listener
   */
  protected DsSipElementListener createElementListener(int headerId) {
    if (initHeader(headerId)) {
      return header;
    }
    return null;
  }

  ////////////////////////
  //      INNER CLASSES
  ////////////////////////

  /**
   * Inner class for finding the first element matching search criteria. This class is currently
   * defined as inner class of DsMimeEntity and it might be generalized and made public as a
   * standalone helper class to application code if so desired. But be aware that the previously
   * found entity has to be cleared in some way before a subsequent visitation. In existing code,
   * the call to getEntity() clears it.
   */
  static class SingleEntityVisitor extends ThreadLocal implements DsMimeEntityVisitor {
    /**
     * Returns the found entity. Subsequent call will return null. This is to make the visitor
     * reusable immediately.
     */
    DsMimeEntity getEntity() {
      DsMimeEntity e = foundEntity;
      foundEntity = null;
      return e;
    }

    public int visit(DsMimeEntity entity) {
      foundEntity = entity;
      return STOP_VISIT;
    }

    DsMimeEntity foundEntity = null;
  }

  /** Entity traverser */
  class Traverser implements DsMimeEntityTraverser {
    Traverser() {}

    public boolean hasNext() {
      if (!selfTraversed) {
        return true;
      }
      if (bodyTraverser == null) {
        bodyTraverser = m_body.traverser();
      }
      return bodyTraverser.hasNext();
    }

    public DsMimeEntity next() {
      if (selfTraversed) {
        if (bodyTraverser == null) {
          bodyTraverser = m_body.traverser();
        }
        return bodyTraverser.next();
      } else {
        selfTraversed = true;
        return DsMimeEntity.this;
      }
    }

    public void remove() {
      if (!selfTraversed) {
        throw new IllegalStateException("Cannot remove self.");
      }
      if (bodyTraverser == null) {
        bodyTraverser = m_body.traverser();
      }
      bodyTraverser.remove();
    }

    /** flag: is the root entity traversed? */
    boolean selfTraversed = false;

    /** body traverser. */
    DsMimeEntityTraverser bodyTraverser = null;
  }

  ////////////////////////
  //      DATA
  ////////////////////////

  /** Message Logger */
  private static final Logger messageLogger = DsLog4j.messageCat;

  /** single entity visitor used in findBodyPart() */
  private static SingleEntityVisitor threadLocalVisitor = new SingleEntityVisitor();

  // Parsing instructions
  /** Parsing instruction: do not parse */
  public static final int PARSE_NONE = -1;
  /** Parsing instruction: parse all containers and leaves if possible */
  public static final int PARSE_ALL = 0;
  /** Parsing instruction: parse containers only. Leaves will not be parsed */
  public static final int PARSE_CONTAINERS_ONLY = 1;
  /** Parsing instruction: parse direct children only */
  public static final int PARSE_DIRECT_CHILDREN_ONLY = 2;
} // End of public class DsMimeEntity
