// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser.TokenSip;

import Acme.Crypto.Crc16Hash;
import com.cisco.dhruva.DsLibs.DsSipObject.DsByteString;
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
    Crc16Hash hash = new Crc16Hash();
    hash.add(name.toByteArray());
    signature = new Integer(DsTokenSipInteger.read16Bit(hash.get()));
  }

  public DsByteString getName() {
    return name;
  }
}
