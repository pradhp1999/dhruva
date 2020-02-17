// ////////////////////////////////////////////////////////////////
// FILENAME:    DsSipRequestDispositionHeader.java
//
// MODULE:      SipCallerPrefs
//
// COPYRIGHT:
// ////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the Request-Disposition header as specified in caller prefs.
 *
 * <p>It providesmethods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * RequestDisposition = "Request-Disposition" ":" 1#directive
 * directive            = proxy-directive | cancel-directive |
 *                        fork-directive | recurse-directive |
 *                        parallel-directive | queue-directive)
 * proxy-directive      = "proxy" | "redirect"
 * cancel-directive     = "cancel" | "no-cancel"
 * fork-directive       = "fork" | "no-fork"
 * recurse-directive    = "recurse" | "no-recurse"
 * parallel-directive   = "parallel" | "sequential"
 * queue-directive      = "queue" | "no-queue"
 * </pre> </code>
 */
public final class DsSipRequestDispositionHeader extends DsSipStringHeader {
  /** Header token. */
  public static DsByteString sToken = BS_REQUEST_DISPOSITION;
  /** Header token plus colon. */
  public static DsByteString sTokenC = BS_REQUEST_DISPOSITION_TOKEN;
  /** Header ID. */
  public static int sID = REQUEST_DISPOSITION;

  /** Default constructor. */
  public DsSipRequestDispositionHeader() {
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
  public DsSipRequestDispositionHeader(byte[] value) {
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
  public DsSipRequestDispositionHeader(byte[] value, int offset, int count) {
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
  public DsSipRequestDispositionHeader(DsByteString value) {
    super(value);
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name.
   */
  public DsByteString getCompactToken() {
    return BS_REQUEST_DISPOSITION_C;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public DsByteString getTokenC() {
    return (isCompact()) ? BS_REQUEST_DISPOSITION_C_TOKEN : BS_REQUEST_DISPOSITION_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public int getHeaderID() {
    return sID;
  }
}
