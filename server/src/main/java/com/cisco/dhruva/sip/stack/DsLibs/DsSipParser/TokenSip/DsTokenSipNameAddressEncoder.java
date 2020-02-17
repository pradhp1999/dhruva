// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.io.OutputStream;

public class DsTokenSipNameAddressEncoder extends DsTokenSipURIGenericEncoder {

  protected static Trace Log = Trace.getTrace(DsTokenSipNameAddressEncoder.class.getName());

  boolean displayNameSet = false;
  DsByteString m_displayName = null;

  boolean tagParam = false;
  boolean nonTagParams = false;
  boolean twoUserParts = false;
  int userSeperatorIndex = 0;

  public DsTokenSipNameAddressEncoder(int uriFlags) {
    super(uriFlags);

    if ((flags & DsTokenSipConstants.TOKEN_SIP_INVITE_URI_DISPLAY_NAME)
        == DsTokenSipConstants.TOKEN_SIP_INVITE_URI_DISPLAY_NAME) {
      displayNameSet = true;
    }
    twoUserParts = true;
    nonTagParams = true;

    debugFlags();
  }

  public DsTokenSipNameAddressEncoder(int uriFlags, boolean isFixedFormat) {
    super(uriFlags);
  }

  // specialty constructor for fixed format request start lines only
  protected DsTokenSipNameAddressEncoder(DsURI uri) {
    super(uri);
  }

  public DsTokenSipNameAddressEncoder(DsSipNameAddress nameAddr) {
    super(nameAddr.getURI(), false);

    if ((nameAddr.getDisplayName() != null) && (nameAddr.getDisplayName().length() > 0)) {
      int tmpFlag = flags | DsTokenSipConstants.TOKEN_SIP_INVITE_URI_DISPLAY_NAME;
      flags = tmpFlag;
      displayNameSet = true;
      m_displayName = nameAddr.getDisplayName();
    }

    twoUserParts = true;
    nonTagParams = true;

    debugFlags();
  }

  public void writeEncoded(OutputStream out, DsTokenSipMessageDictionary md) throws IOException {
    out.write(flags);
    if (isDisplayNameSet()) {
      md.getEncoding(m_displayName).write(out);
    }

    switch (getScheme()) {
      case DsSipConstants.SIP_URL_ID:
        DsByteString user = ((DsSipURL) getUri()).getUser();
        DsByteString password = ((DsSipURL) getUri()).getUserPassword();
        if ((password != null) && (password.length() > 0)) {
          user = user.copy().append(':').append(password);
        }

        if (isTwoUserParts()) {
          if (userSeperatorIndex > 0) {
            md.getEncoding(user.substring(0, getUserPartSeperator())).write(out);
            md.getEncoding(
                    user.substring(
                        getUserPartSeperator()
                            + DsTokenSipConstants.TOKEN_SIP_TWO_PART_USER_SEPERATOR.length()))
                .write(out);
          } else {
            out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
            if (user == null || user.length() == 0) {
              out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
            } else {
              md.getEncoding(user).write(out);
            }
          }
        } else {
          if (Log.isDebugEnabled())
            Log.debug(
                "In name addr encoder, the 1 part user is <"
                    + ((DsSipURL) this.getUri()).getUser()
                    + ">");

          if (user != null) {
            md.getEncoding(user).write(out);
          } else {
            out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
          }
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
        md.getEncoding(this.getUri().getScheme()).write(out);
        md.getEncoding(this.getUri().getSchemeData()).write(out);
        break;
    }

    if (isNonTagParams()) {
      getUri().writeEncodedParameters(out, md);
    }
  }

  public final boolean isDisplayNameSet() {
    return displayNameSet;
  }

  public final boolean isTagParam() {
    return tagParam;
  }

  public final boolean isNonTagParams() {
    return nonTagParams;
  }

  public final boolean isTwoUserParts() {
    return twoUserParts;
  }

  public final int getUserPartSeperator() {
    return userSeperatorIndex;
  }

  public void debugFlags() {
    if (Log.isDebugEnabled()) {
      Log.debug("URI FLAGS");
      Log.debug("*******************");
      Log.debug("Display Name set? - " + displayNameSet);
      Log.debug("Tag param set? - " + tagParam);
      Log.debug("Non-Tag param set? - " + nonTagParams);
      Log.debug("Two user parts? - " + twoUserParts);
      Log.debug("User seperator index? - " + userSeperatorIndex);
      Log.debug("Port specified? - " + this.isPortSpecified());
      Log.debug("*******************");
      Log.debug("");
    }
  }
}
