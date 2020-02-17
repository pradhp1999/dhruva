package com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DsCRL {
  private HashSet<String> revokedSerialNumbers = new HashSet<String>();

  public void setRevokedSerialNumbers(HashSet<String> serialNo) {
    this.revokedSerialNumbers = serialNo;
  }

  public HashSet<String> getRevokedSerialNumbers() {
    return revokedSerialNumbers;
  }
}
