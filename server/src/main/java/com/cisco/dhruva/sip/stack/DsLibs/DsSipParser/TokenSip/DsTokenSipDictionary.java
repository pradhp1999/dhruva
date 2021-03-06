// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import java.io.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA. User: charris Date: Mar 30, 2004 Time: 11:49:27 AM To change this
 * template use Options | File Templates.
 */
public class DsTokenSipDictionary implements Serializable {
  DsByteString name;
  Integer signature;

  final int sip1_size = 256;

  // dictionary sections are read into these arrays
  DsByteString[] sip1Section = new DsByteString[512];
  DsByteString[] localSection = new DsByteString[256];

  int local_size;
  int sip_total_size;

  // used for fetching encodings based on a string
  final HashMap sip1SectionTable = new HashMap();
  final HashMap localSectionTable = new HashMap();

  final byte[][] sip1SectionBytes = new byte[512][];
  final byte[][] localSectionBytes = new byte[256][];

  DsTokenSipDictionary(DsByteString name) {
    this.name = name;
    Crc16Hasher h = new Crc16Hasher();
    signature = DsTokenSipInteger.read16Bit(h.genSignature(name.toByteArray()));
  }

  public DsByteString getName() {
    return name;
  }
}
