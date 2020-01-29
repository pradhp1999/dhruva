package com.cisco.dhruva.util.saevent.dataparam;

import java.util.Map;

// POJO class builder for connection events

public class ConnectionEventDataParam extends DataParam {
  public static class Builder {
    private Map certInfos;
    private String direction;
    private String eventInfo;
    private String eventType;
    private String localIPAddress;
    private int localPort;
    private String negotiatedCipherSuite;
    private String peerCertificateHash;
    private String peerCertificateSerialNumber;
    private String peerPrincipalName;
    private String peerPrincipalNameAuthenticated;
    private String remoteIPAddress;
    private int remotePort;
    private String responseCode;
    private String status;
    private String tlsVersion;
    private String type;
    private String url;

    public ConnectionEventDataParam build() {
      return new ConnectionEventDataParam(this);
    }

    public Builder certInfos(Map certInfos) {
      this.certInfos = certInfos;
      return this;
    }

    public Builder direction(String direction) {
      this.direction = direction;
      return this;
    }

    public Builder eventInfo(String eventInfo) {
      this.eventInfo = eventInfo;
      return this;
    }

    public Builder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder localIPAddress(String localIPAddress) {
      this.localIPAddress = localIPAddress;
      return this;
    }

    public Builder localPort(int localPort) {
      this.localPort = localPort;
      return this;
    }

    public Builder negotiatedCipherSuite(String negotiatedCipherSuite) {
      this.negotiatedCipherSuite = negotiatedCipherSuite;
      return this;
    }

    public Builder peerCertificateHash(String peerCertificateHash) {
      this.peerCertificateHash = peerCertificateHash;
      return this;
    }

    public Builder peerCertificateSerialNumber(String peerCertificateSerialNumber) {
      this.peerCertificateSerialNumber = peerCertificateSerialNumber;
      return this;
    }

    public Builder peerPrincipalName(String peerPrincipalName) {
      this.peerPrincipalName = peerPrincipalName;
      return this;
    }

    public Builder peerPrincipalNameAuthenticated(String peerPrincipalNameAuthenticated) {
      this.peerPrincipalNameAuthenticated = peerPrincipalNameAuthenticated;
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

    public Builder responseCode(String responseCode) {
      this.responseCode = responseCode;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder tlsVersion(String tlsVersion) {
      this.tlsVersion = tlsVersion;
      return this;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder url(String url) {
      this.url = url;
      return this;
    }
  }

  private Map certInfos;
  private String direction;
  private String localIPAddress;
  private int localPort;
  private String negotiatedCipherSuite;
  private String peerCertificateHash;
  private String peerCertificateSerialNumber;
  private String peerPrincipalName;
  private String peerPrincipalNameAuthenticated;
  private String remoteIPAddress;
  private int remotePort;
  private String responseCode;
  private String status;
  private String tlsVersion;
  private String type;

  private String url;

  private ConnectionEventDataParam(Builder builder) {
    this.status = builder.status;
    this.eventInfo = builder.eventInfo;
    this.type = builder.type;
    this.eventType = builder.eventType;
    this.peerPrincipalName = builder.peerPrincipalName;
    this.peerPrincipalNameAuthenticated = builder.peerPrincipalNameAuthenticated;
    this.peerCertificateSerialNumber = builder.peerCertificateSerialNumber;
    this.peerCertificateHash = builder.peerCertificateHash;
    this.tlsVersion = builder.tlsVersion;
    this.negotiatedCipherSuite = builder.negotiatedCipherSuite;
    this.certInfos = builder.certInfos;
    this.localIPAddress = builder.localIPAddress;
    this.localPort = builder.localPort;
    this.remoteIPAddress = builder.remoteIPAddress;
    this.remotePort = builder.remotePort;
    this.direction = builder.direction;
    this.responseCode = builder.responseCode;
    this.url = builder.url;
  }

  public Map getCertInfos() {
    return certInfos;
  }

  public String getDirection() {
    return direction;
  }

  public String getLocalIPAddress() {
    return localIPAddress;
  }

  public int getLocalPort() {
    return localPort;
  }

  public String getNegotiatedCipherSuite() {
    return negotiatedCipherSuite;
  }

  public String getPeerCertificateHash() {
    return peerCertificateHash;
  }

  public String getPeerCertificateSerialNumber() {
    return peerCertificateSerialNumber;
  }

  public String getPeerPrincipalName() {
    return peerPrincipalName;
  }

  public String getPeerPrincipalNameAuthenticated() {
    return peerPrincipalNameAuthenticated;
  }

  public String getRemoteIPAddress() {
    return remoteIPAddress;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public String getResponseCode() {
    return responseCode;
  }

  public String getStatus() {
    return status;
  }

  public String getTlsVersion() {
    return tlsVersion;
  }

  public String getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public void setCertInfos(Map certInfos) {
    this.certInfos = certInfos;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public void setLocalIPAddress(String localIPAddress) {
    this.localIPAddress = localIPAddress;
  }

  public void setLocalPort(int localPort) {
    this.localPort = localPort;
  }

  public void setNegotiatedCipherSuite(String negotiatedCipherSuite) {
    this.negotiatedCipherSuite = negotiatedCipherSuite;
  }

  public void setPeerCertificateHash(String peerCertificateHash) {
    this.peerCertificateHash = peerCertificateHash;
  }

  public void setPeerCertificateSerialNumber(String peerCertificateSerialNumber) {
    this.peerCertificateSerialNumber = peerCertificateSerialNumber;
  }

  public void setPeerPrincipalName(String peerPrincipalName) {
    this.peerPrincipalName = peerPrincipalName;
  }

  public void setPeerPrincipalNameAuthenticated(String peerPrincipalNameAuthenticated) {
    this.peerPrincipalNameAuthenticated = peerPrincipalNameAuthenticated;
  }

  public void setRemoteIPAddress(String remoteIPAddress) {
    this.remoteIPAddress = remoteIPAddress;
  }

  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  public void setResponseCode(String responseCode) {
    this.responseCode = responseCode;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setTlsVersion(String tlsVersion) {
    this.tlsVersion = tlsVersion;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
