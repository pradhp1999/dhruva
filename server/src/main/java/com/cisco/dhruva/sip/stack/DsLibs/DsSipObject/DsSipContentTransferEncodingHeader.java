// ////////////////////////////////////////////////////////////////
// FILENAME:    DsSipContentTransferEncodingHeader.java
//
// MODULE:      DsSipObject
//
// COPYRIGHT:
// ============== copyright 2004 Cisco Systems, Inc. =================
// ==================== all rights reserved =======================
// ////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the Content-Transfer-Encoding header as specified in RFC 2045. It provides
 * methods to build, access, modify, serialize the header. Content-Transfer-Encoding Syntax per
 * rfc2045 The Content-Transfer-Encoding field's value is a single token specifying the type of
 * encoding, as enumerated below. Formally:
 *
 * <p>encoding := "Content-Transfer-Encoding" ":" mechanism
 *
 * <p>mechanism := "7bit" / "8bit" / "binary" / "quoted-printable" / "base64" / ietf-token / x-token
 *
 * @author Jianren Yang (jryang@cisco.com)
 */
public final class DsSipContentTransferEncodingHeader extends DsSipStringHeader {
  /** Header ID. */
  public static final byte sID = CONTENT_TRANSFER_ENCODING;

  /** Default constructor. */
  public DsSipContentTransferEncodingHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipContentTransferEncodingHeader(byte[] value) {
    super(value);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public DsSipContentTransferEncodingHeader(byte[] value, int offset, int count) {
    super(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipContentTransferEncodingHeader(DsByteString value) {
    super(value);
  }

  /**
   * Sets the content transfer encoding for this header to the specified value. It in turn call
   * setValue().
   *
   * @param encoding the new encoding value that need to be set.
   */
  public void setContentTransferEncoding(DsByteString transferEncoding) {
    setValue(transferEncoding);
  }

  /**
   * Returns the content transfer encoding for this header.
   *
   * @return the content transfer encoding for this header.
   */
  public DsByteString getContentTransferEncoding() {
    return getValue();
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value.
   */
  public DsByteString getToken() {
    return BS_CONTENT_TRANSFER_ENCODING;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return (BS_CONTENT_TRANSFER_ENCODING_TOKEN);
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name.
   */
  public DsByteString getCompactToken() {
    return BS_CONTENT_TRANSFER_ENCODING;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public int getHeaderID() {
    return CONTENT_TRANSFER_ENCODING;
  }
}
