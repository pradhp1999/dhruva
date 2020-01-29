/*

 FILENAME:    DsMimeMultipartEncrypted.java


 DESCRIPTION: Class DsMimeMultipartEncrypted is the parsed representation
              of a multipart/encrypted body. Currently, this is a placeholder
              and no concrete implementation is added into this class.


 MODULE:      DsMimeMultipartEncrypted

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.DsLibs.DsSipMime;

import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * A concrete subclass of DsMimeMultipart, class DsMimeMultipartEncrypted is the parsed
 * representation of a multipart/encrypted body. Currently, this is a placeholder and no concrete
 * implementation is added into this class.
 */
public class DsMimeMultipartEncrypted extends DsMimeMultipart {

  /** Not implemented. Throw UnsupportedOperationException. */
  public int getContainingEntityContentLength() {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  /** Not implemented. Throw UnsupportedOperationException. */
  public DsMimeEntityTraverser traverser() {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  /** Not implemented. Throw UnsupportedOperationException. */
  public DsMimeUnparsedBody encode() throws DsException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  /** Not implemented. Throw UnsupportedOperationException. */
  public void serialize(OutputStream outStream) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }
} // End of public class DsMimeMultipartEncrypted
