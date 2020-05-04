package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * Title: DsSipETagHeader
 *
 * <p>Description:
 *
 * <p>Copyright: Copyright (c) 2004, 2005 by Cisco Systems, Inc.
 *
 * <p>Company: Cisco Systems
 *
 * @author
 * @version
 */
import java.security.SecureRandom;

/**
 * This class represents the ETag header as specified in draft-ietf-simple-publish-03.txt. It
 * provides methods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * SIP-ETag   =  "SIP-ETag":" entity-tag
 * entity-tag =  token
 * </pre> </code>
 */
public final class DsSipETagHeader extends DsSipStringHeader {
  /** Header ID. */
  public static int sID = ETAG;

  /** Default constructor. */
  public DsSipETagHeader() {
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
  public DsSipETagHeader(byte[] value) {
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
  public DsSipETagHeader(byte[] value, int offset, int count) {
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
  public DsSipETagHeader(DsByteString value) {
    super(value);
  }

  /**
   * The method returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return BS_ETAG;
  }

  /**
   * The method Returns the token which is the compact name of the header.
   *
   * @return The compact token name
   */
  public DsByteString getCompactToken() {
    return BS_ETAG;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_ETAG_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return ETAG;
  }

  /**
   * Generating an ETag header. The entity-tag is composed by timestamp in a string form. Two ETags
   * that are generated from this function can be compared.
   *
   * @return an ETag Header.
   */
  public static DsSipETagHeader generateETag() {
    // considering 1. use fromTag logic. It is random + hex of currentTime
    //             2. sync. incr. an integer + currentTime
    // Since PUBLISH is still in draft, this will probably be revisited in later versions.
    // modified codes to used condiseration one
    StringBuffer sb = new StringBuffer();
    int aRandom = (int) (new SecureRandom().nextInt(Integer.MAX_VALUE) * 65535);
    int current_time = (int) (System.currentTimeMillis() % 65535);
    sb.append(Integer.toHexString(aRandom));
    sb.append(Integer.toHexString(current_time));
    return new DsSipETagHeader(new DsByteString(sb.toString()));
  }
}
