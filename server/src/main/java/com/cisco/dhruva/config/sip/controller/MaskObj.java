/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.config.sip.controller;

import com.cisco.dhruva.config.sip.RE;

/**
 * Created by IntelliJ IDEA. User: rrachuma Date: Jul 25, 2005 Time: 4:19:03 PM To change this
 * template use File | Settings | File Templates.
 */
public class MaskObj {
  public static final short VIA = 0;
  public static final short MAX_HEADER_SIZE = 1;

  public String direction;
  public boolean[] headers = new boolean[MAX_HEADER_SIZE];

  public MaskObj(String direction) {
    this.direction = direction;
    for (short i = 0; i < headers.length; i++) headers[i] = false;
  }

  public boolean isHeaderSet(int hdr) {
    if (hdr >= 0 && hdr < MAX_HEADER_SIZE) return headers[hdr];
    else return false;
  }

  public void setHeaderMask(int hdr) {
    if (hdr >= 0 && hdr < MAX_HEADER_SIZE) headers[hdr] = true;
  }

  public boolean getHeaderMask(String header) {
    if (header.equals(RE.reMaskHeader_via)) return headers[VIA];
    return false;
  }

  public void unSetHeaderMask(int hdr) {
    if (hdr >= 0 && hdr < MAX_HEADER_SIZE) headers[hdr] = false;
  }

  public String getDirection() {
    return direction;
  }

  public boolean isHeaderMaskingEnabled() {
    for (short i = 0; i < headers.length; i++) {
      if (headers[i]) return true;
    }
    return false;
  }

  public String toString() {
    return new String("direction=" + direction + ';' + headers[VIA]);
  }
}
