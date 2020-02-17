// ////////////////////////////////////////////////////////////////
// FILENAME:    DsSipContentDescriptionHeader.java
//
// MODULE:      DsSipObject
//
// COPYRIGHT:
// ============== copyright 2004 Cisco Systems, Inc. =================
// ==================== all rights reserved =======================
// ////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * Representation of Content-Description header, RFC 2045.
 *
 * <p>The ability to associate some descriptive information with a given body is often desirable.
 * For example, it may be useful to mark an "image" body as "a picture of the Space Shuttle
 * Endeavor." Such text may be placed in the Content-Description header field. This header field is
 * always optional.
 *
 * <p>description := "Content-Description" ":" *text
 *
 * @author Michael Zhou (xmzhou@cisco.com)
 * @author Jianren Yang (jryang@cisco.com)
 */
public final class DsSipContentDescriptionHeader extends DsSipStringHeader {
  /** Header ID. */
  public static final byte sID = CONTENT_DESCRIPTION;

  /** Creates a new instance of DsSipContentDescriptionHeader */
  public DsSipContentDescriptionHeader() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipContentDescriptionHeader(byte[] value) {
    super(value);
  }
  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
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
  public DsSipContentDescriptionHeader(byte[] value, int offset, int count) {
    super(value, offset, count);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipContentDescriptionHeader(DsByteString value) {
    super(value);
  }

  public DsByteString getCompactToken() {
    return BS_CONTENT_DESCRIPTION;
  }

  public int getHeaderID() {
    return CONTENT_DESCRIPTION;
  }

  public DsByteString getToken() {
    return BS_CONTENT_DESCRIPTION;
  }

  public DsByteString getTokenC() {
    return BS_CONTENT_DESCRIPTION_TOKEN;
  }

  /**
   * Returns the content description value
   *
   * @return the content description value
   */
  public DsByteString getContentDescription() {
    return getValue();
  }

  /**
   * Sets content description.
   *
   * @param cd Content description value
   */
  public void setContentDescription(DsByteString cd) {
    setValue(cd);
  }
}
