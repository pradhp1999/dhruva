// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.Level;

public class DsTokenSipURIGenericEncoder {
  int flags;
  int scheme;
  boolean portSpecified = false;

  DsURI m_uri;

  // Set the logging category
  protected static Trace Log = Trace.getTrace(DsTokenSipURIGenericEncoder.class.getName());

  public DsTokenSipURIGenericEncoder(int uriFlags) {
    flags = uriFlags;
    m_uri = null;

    if ((flags & DsTokenSipConstants.TOKEN_SIP_INVITE_URI_KNOWN_SCHEME)
        != DsTokenSipConstants.TOKEN_SIP_INVITE_URI_KNOWN_SCHEME) {
      if ((flags & DsTokenSipConstants.TOKEN_SIP_INVITE_TEL_URI_SCHEME)
          == DsTokenSipConstants.TOKEN_SIP_INVITE_TEL_URI_SCHEME) {
        scheme = DsSipConstants.TEL_URL_ID;
      } else {
        scheme = DsSipConstants.SIP_URL_ID;
      }
    } else {
      // unknown
      scheme = DsSipConstants.UNKNOWN_URL_ID;
    }

    if ((flags & DsTokenSipConstants.TOKEN_SIP_INVITE_URI_PORT)
        == DsTokenSipConstants.TOKEN_SIP_INVITE_URI_PORT) {
      portSpecified = true;
    }
  }

  private static final DsByteString TEL = new DsByteString("tel");

  public DsTokenSipURIGenericEncoder(DsURI uri) {
    this(uri, true);
  }

  public DsTokenSipURIGenericEncoder(DsURI uri, boolean isFixed) {
    if (uri == null) {
      IllegalArgumentException e = new IllegalArgumentException("URI cannot be null.");
      if (Log.isEnabled(Level.WARN)) {
        Log.warn("Null URI", e);

        throw e;
      }
    }

    m_uri = uri;

    if (isFixed) {
      flags = 0;
    } else {
      flags = DsTokenSipConstants.TOKEN_SIP_URI_LOW;
    }

    // scheme
    if (uri.isSipURL() == false) {
      // todo get a faster way to check this

      // todo change this to NEW tel static
      DsByteString schemeTmp = uri.getScheme();
      if (null != schemeTmp && schemeTmp.equalsIgnoreCase(TEL) == true) {
        // it's a tel URI
        flags |= DsTokenSipConstants.TOKEN_SIP_INVITE_TEL_URI_SCHEME;
        scheme = DsSipConstants.TEL_URL_ID;
      } else {
        if (Log.isEnabled(Level.ERROR)) {
          Log.error("Unknown scheme in the URI" + uri);
        }

        // unknown URI
        flags |= DsTokenSipConstants.TOKEN_SIP_INVITE_URI_KNOWN_SCHEME;
        scheme = DsSipConstants.UNKNOWN_URL_ID;
      }
    } else {
      scheme = DsSipConstants.SIP_URL_ID;

      // port- If scheme is SIP, and port is not 5060, then set flag.  Otherwise, leave it blank.
      if (((DsSipURL) uri).getPort() != DsSipURL.DEFAULT_PORT) {
        flags |= DsTokenSipConstants.TOKEN_SIP_INVITE_URI_PORT;
        portSpecified = true;
      }
    }
  }

  public void writeEncoded(OutputStream out, DsTokenSipMessageDictionary md) throws IOException {
    out.write(flags);

    switch (getScheme()) {
      case DsSipConstants.SIP_URL_ID:
        DsByteString user = ((DsSipURL) this.getUri()).getUser();
        if (user != null) {
          md.getEncoding(user).write(out);
        }
        md.getEncoding(((DsSipURL) this.getUri()).getHost()).write(out);

        if (isPortSpecified()) {
          DsTokenSipInteger.write16Bit(out, ((DsSipURL) this.getUri()).getPort());
        }
        break;
      case DsSipConstants.TEL_URL_ID:
        md.getEncoding(((DsTelURL) this.getUri()).getTelephoneSubscriber().getPhoneNumber())
            .write(out);
        break;
      default:
        md.getEncoding(this.getUri().getValue()).write(out);
        break;
    }

    m_uri.writeEncodedParameters(out, md);
  }

  public int getScheme() {
    return scheme;
  }

  public boolean isPortSpecified() {
    return portSpecified;
  }

  public int getFlags() {
    return flags;
  }

  DsURI getUri() {
    return m_uri;
  }
}
