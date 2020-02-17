package com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert;

public class SubjectAltName {

  private SANType sanType;
  private String sanName;

  public SubjectAltName(int sanType, String sanName) {
    this.sanName = sanName;
    this.sanType = SANType.values()[sanType];
  }

  public SANType getSanType() {
    return sanType;
  }

  public String getSanName() {
    return sanName;
  }
}
