package com.cisco.dhruva.util.saevent.dataparam;

import java.io.Serializable;

// POJO class builder for SIP Message too large events

public class SIPMessageTooLargeDataParam extends DataParam implements Serializable {
  public static class Builder {
    private String localIPAddress;
    private int localPort;
    private int packetSize;
    private String remoteIPAddress;
    private int remotePort;
    private String transportType;

    public SIPMessageTooLargeDataParam build() {
      return new SIPMessageTooLargeDataParam(this);
    }

    public Builder localIPAddress(String localIPAddress) {
      this.localIPAddress = localIPAddress;
      return this;
    }

    public Builder localPort(int localPort) {
      this.localPort = localPort;
      return this;
    }

    public Builder packetSize(int packetSize) {
      this.packetSize = packetSize;
      return this;
    }

    public Builder remoteIPAddress(String remoteIPAddress) {
      this.remoteIPAddress = remoteIPAddress;
      return this;
    }

    public Builder remotePort(int remotePort) {
      this.remotePort = remotePort;
      return this;
    }

    public Builder transportType(String transportType) {
      this.transportType = transportType;
      return this;
    }
  }

  private String localIPAddress;
  private int localPort;
  private int packetSize;
  private String remoteIPAddress;
  private int remotePort;

  private String transportType;

  private SIPMessageTooLargeDataParam(Builder builder) {
    this.transportType = builder.transportType;
    this.remoteIPAddress = builder.remoteIPAddress;
    this.remotePort = builder.remotePort;
    this.localIPAddress = builder.localIPAddress;
    this.localPort = builder.localPort;
    this.packetSize = builder.packetSize;
  }

  public String getLocalIPAddress() {
    return localIPAddress;
  }

  public int getLocalPort() {
    return localPort;
  }

  public int getPacketSize() {
    return packetSize;
  }

  public String getRemoteIPAddress() {
    return remoteIPAddress;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public String getTransportType() {
    return transportType;
  }

  public void setLocalIPAddress(String localIPAddress) {
    this.localIPAddress = localIPAddress;
  }

  public void setLocalPort(int localPort) {
    this.localPort = localPort;
  }

  public void setPacketSize(int packetSize) {
    this.packetSize = packetSize;
  }

  public void setRemoteIPAddress(String remoteIPAddress) {
    this.remoteIPAddress = remoteIPAddress;
  }

  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  public void setTransportType(String transportType) {
    this.transportType = transportType;
  }
}
