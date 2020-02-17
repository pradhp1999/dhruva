/*

 FILENAME:    DsMimeMultipartAlternative.java


 DESCRIPTION: Class DsMimeMultipartAlternative is the parsed representation
              of a multipart/alternative body.


 MODULE:      DsMimeMultipartAlternative

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;

/**
 * A concrete subclass of DsMimeMultipartMixed, class DsMimeMultipartAlternative is the parsed
 * representation of a multipart/alternative body. It has the same semantics to manipulate body
 * parts as DsMimeMultipartMixed.
 */
public class DsMimeMultipartAlternative extends DsMimeMultipartMixed {

  ////////////////////////
  //      CONSTRUCTORS
  ////////////////////////

  /** Default Constructor. Equivalent to the constructor with initial capacity of 2. */
  public DsMimeMultipartAlternative() {
    super(2, null, null, null);
  }

  /**
   * Constructor
   *
   * @param initialCapacity the initial capacty for the part list (default is 2)
   */
  public DsMimeMultipartAlternative(int initialCapacity) {
    this(initialCapacity, null, null, null);
  }

  /**
   * Constructor
   *
   * @param initialCapacity the initial capacty for the part list (default is 2)
   * @param boundary the boundary deliminater for the multipart body
   * @param preamble the preamble for the multipart body
   * @param epilog the epilogue for the multipart body
   */
  public DsMimeMultipartAlternative(
      int initialCapacity, DsByteString boundary, DsByteString preamble, DsByteString epilog) {
    super(initialCapacity, boundary, preamble, epilog);
  }

  public DsByteString getContainingEntityContentType() {
    return DsMimeContentManager.MIME_MT_MULTIPART_ALTERNATIVE;
  }

  /**
   * Registers multpart/alternative content type with content manager. This method will call
   * registerContentType() method on DsMimeContentManager with appropriate arguments.
   */
  public static void registerType() {
    DsMimeContentManager.registerContentType(
        DsMimeContentManager.MIME_MT_MULTIPART_ALTERNATIVE, MULTIPART_ALTERNATIVE_PROPS);
  }

  /** multipar/alternative properties. */
  private static DsMimeContentProperties MULTIPART_ALTERNATIVE_PROPS =
      new DsMimeContentProperties(
          DsMimeContentManager.MIME_MT_MULTIPART_ALTERNATIVE,
          DsMimeContentManager.MIME_DISP_RENDER,
          true,
          DsMimeMultipartParser.getInstance(),
          DsMimeMultipartAlternative.class);
} // End of public class DsMimeMultipartAlternative
