// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.util.log.Trace;

public class DsTokenSipNameAddressFixedFormatEncoder extends DsTokenSipNameAddressEncoder {

  protected static Trace Log =
      Trace.getTrace(DsTokenSipNameAddressFixedFormatEncoder.class.getName());

  public DsTokenSipNameAddressFixedFormatEncoder(int uriFlags) {
    super(uriFlags, true);

    if ((flags & DsTokenSipConstants.TOKEN_SIP_INVITE_URI_TAG_PARAM)
        == DsTokenSipConstants.TOKEN_SIP_INVITE_URI_TAG_PARAM) {
      tagParam = true;
    }

    if ((flags & DsTokenSipConstants.TOKEN_SIP_INVITE_URI_TWO_PART_USER)
        == DsTokenSipConstants.TOKEN_SIP_INVITE_URI_TWO_PART_USER) {
      twoUserParts = true;
    }

    if ((flags & DsTokenSipConstants.TOKEN_SIP_INVITE_URI_PARAMS)
        == DsTokenSipConstants.TOKEN_SIP_INVITE_URI_PARAMS) {
      nonTagParams = true;
    }

    if ((flags & DsTokenSipConstants.TOKEN_SIP_INVITE_URI_DISPLAY_NAME)
        == DsTokenSipConstants.TOKEN_SIP_INVITE_URI_DISPLAY_NAME) {
      displayNameSet = true;
    }

    userSeperatorIndex = -1;

    debugFlags();
  }

  public DsTokenSipNameAddressFixedFormatEncoder(DsSipNameAddress nameAddr) {
    super(nameAddr.getURI());

    int tmpFlag = 0;

    if ((nameAddr.getDisplayName() != null) && (nameAddr.getDisplayName().length() > 0)) {
      tmpFlag = flags | DsTokenSipConstants.TOKEN_SIP_INVITE_URI_DISPLAY_NAME;
      flags = tmpFlag;
      displayNameSet = true;
      m_displayName = nameAddr.getDisplayName();
    }

    switch (getScheme()) {
      case DsSipConstants.SIP_URL_ID:
        DsByteString user = ((DsSipURL) nameAddr.getURI()).getUser();
        DsByteString password = ((DsSipURL) getUri()).getUserPassword();
        if ((password != null) && (password.length() > 0)) {
          user = user.copy().append(':').append(password);
        }

        userSeperatorIndex =
            (user == null)
                ? -1
                : user.indexOf(DsTokenSipConstants.TOKEN_SIP_TWO_PART_USER_SEPERATOR);
        if (userSeperatorIndex > 0) {
          tmpFlag = flags | DsTokenSipConstants.TOKEN_SIP_INVITE_URI_TWO_PART_USER;
          flags = tmpFlag;
          twoUserParts = true;
        }

        // other params check
        if (((DsSipURL) nameAddr.getURI()).getParameters() != null) {
          nonTagParams = true;
          // flags = flags | DsTokenSipConstants.TOKEN_SIP_INVITE_URI_PARAMS;
          tmpFlag = flags | DsTokenSipConstants.TOKEN_SIP_INVITE_URI_PARAMS;
          flags = tmpFlag;
        }
        break;

      case DsSipConstants.TEL_URL_ID:
        // other params check
        // todo no way to get ALL parameters except to check shortcut methods and then go to the
        // "futureExtensions".

        break;
    }
    debugFlags();
  }

  // specialty constructor for fixed format request URI start lines ONLY
  public DsTokenSipNameAddressFixedFormatEncoder(DsURI uri) {
    super(uri);

    int tmpFlag = 0;
    switch (getScheme()) {
      case DsSipConstants.SIP_URL_ID:
        DsByteString user = ((DsSipURL) uri).getUser();
        userSeperatorIndex =
            (user == null)
                ? -1
                : user.indexOf(DsTokenSipConstants.TOKEN_SIP_TWO_PART_USER_SEPERATOR);
        if (userSeperatorIndex > 0) {
          tmpFlag = flags | DsTokenSipConstants.TOKEN_SIP_INVITE_URI_TWO_PART_USER;
          flags = tmpFlag;
          twoUserParts = true;
        }

        // other params check
        if (((DsSipURL) uri).getParameters() != null) {
          nonTagParams = true;
          tmpFlag = flags | DsTokenSipConstants.TOKEN_SIP_INVITE_URI_PARAMS;
          flags = tmpFlag;
        }
        break;

      case DsSipConstants.TEL_URL_ID:

        // other params check
        // todo no way to get ALL parameters except to check shortcut methods and then go to the
        // "futureExtensions".

        break;
    }

    debugFlags();
  }

  public void setTagPresent() {
    int tmpFlag = flags | DsTokenSipConstants.TOKEN_SIP_INVITE_URI_TAG_PARAM;
    flags = tmpFlag;
    tagParam = true;
  }
}
