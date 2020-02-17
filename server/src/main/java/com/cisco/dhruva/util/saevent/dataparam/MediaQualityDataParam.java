package com.cisco.dhruva.util.saevent.dataparam;

import com.fasterxml.jackson.annotation.JsonProperty;

// POJO class builder for media quality parameters events

public class MediaQualityDataParam extends DataParam {
  public static class Builder {
    private String callId;
    private String localIPAddress;
    private String localSessionID;
    private String remoteIPAddress;
    private String remoteSessionID;
    private String pRtpStatHeader;
    private int mediaPacketsSentCount;
    private int mediaOctetSentCount;
    private int mediaPacketsReceivedCount;
    private int mediaOctetReceivedCount;
    private int mediaPacketsLostCount;
    private int mediaPacketsJitter;
    private int mediaRoundTripDelay;
    private int callDuration;

    public MediaQualityDataParam build() {
      return new MediaQualityDataParam(this);
    }

    public Builder callId(String callId) {
      this.callId = callId;
      return this;
    }

    public Builder localIPAddress(String localIPAddress) {
      this.localIPAddress = localIPAddress;
      return this;
    }

    public Builder localSessionID(String localSessionID) {
      this.localSessionID = localSessionID;
      return this;
    }

    public Builder remoteIPAddress(String remoteIPAddress) {
      this.remoteIPAddress = remoteIPAddress;
      return this;
    }

    public Builder remoteSessionID(String remoteSessionID) {
      this.remoteSessionID = remoteSessionID;
      return this;
    }

    public Builder pRtpStatHeader(String pRtpStatHeader) {
      this.pRtpStatHeader = pRtpStatHeader;
      return this;
    }

    public Builder mediaPacketsSentCount(int mediaPacketsSentCount) {
      this.mediaPacketsSentCount = mediaPacketsSentCount;
      return this;
    }

    public Builder mediaOctetSentCount(int mediaOctetSentCount) {
      this.mediaOctetSentCount = mediaOctetSentCount;
      return this;
    }

    public Builder mediaPacketsReceivedCount(int mediaPacketsReceivedCount) {
      this.mediaPacketsReceivedCount = mediaPacketsReceivedCount;
      return this;
    }

    public Builder mediaOctetReceivedCount(int mediaOctetReceivedCount) {
      this.mediaOctetReceivedCount = mediaOctetReceivedCount;
      return this;
    }

    public Builder mediaPacketsLostCount(int mediaPacketsLostCount) {
      this.mediaPacketsLostCount = mediaPacketsLostCount;
      return this;
    }

    public Builder mediaPacketsJitter(int mediaPacketsJitter) {
      this.mediaPacketsJitter = mediaPacketsJitter;
      return this;
    }

    public Builder mediaRoundTripDelay(int mediaRoundTripDelay) {
      this.mediaRoundTripDelay = mediaRoundTripDelay;
      return this;
    }

    public Builder callDuration(int callDuration) {
      this.callDuration = callDuration;
      return this;
    }
  }

  private String callId;
  private String localIPAddress;
  private String localSessionID;
  private String remoteIPAddress;
  private String remoteSessionID;
  private String pRtpStatHeader;
  private int mediaPacketsSentCount;
  private int mediaOctetSentCount;
  private int mediaPacketsReceivedCount;
  private int mediaOctetReceivedCount;
  private int mediaPacketsLostCount;
  private int mediaPacketsJitter;
  private int mediaRoundTripDelay;
  private int callDuration;

  private MediaQualityDataParam(Builder builder) {
    this.callId = builder.callId;
    this.localSessionID = builder.localSessionID;
    this.remoteSessionID = builder.remoteSessionID;
    this.localIPAddress = builder.localIPAddress;
    this.remoteIPAddress = builder.remoteIPAddress;
    this.pRtpStatHeader = builder.pRtpStatHeader;
    this.mediaPacketsSentCount = builder.mediaPacketsSentCount;
    this.mediaOctetSentCount = builder.mediaOctetSentCount;
    this.mediaPacketsReceivedCount = builder.mediaPacketsReceivedCount;
    this.mediaOctetReceivedCount = builder.mediaOctetReceivedCount;
    this.mediaPacketsLostCount = builder.mediaPacketsLostCount;
    this.mediaPacketsJitter = builder.mediaPacketsJitter;
    this.mediaRoundTripDelay = builder.mediaRoundTripDelay;
    this.callDuration = builder.callDuration;
  }

  @JsonProperty("Call-ID")
  public String getCallId() {
    return callId;
  }

  public String getLocalIPAddress() {
    return localIPAddress;
  }

  public String getLocalSessionID() {
    return localSessionID;
  }

  public String getRemoteIPAddress() {
    return remoteIPAddress;
  }

  public String getRemoteSessionID() {
    return remoteSessionID;
  }

  @JsonProperty("sipHeader")
  public String getpRtpStatHeader() {
    return pRtpStatHeader;
  }

  public int getMediaPacketsSentCount() {
    return mediaPacketsSentCount;
  }

  public int getMediaOctetSentCount() {
    return mediaOctetSentCount;
  }

  public int getMediaPacketsReceivedCount() {
    return mediaPacketsReceivedCount;
  }

  public int getMediaOctetReceivedCount() {
    return mediaOctetReceivedCount;
  }

  public int getMediaPacketsLostCount() {
    return mediaPacketsLostCount;
  }

  public int getMediaPacketsJitter() {
    return mediaPacketsJitter;
  }

  public int getMediaRoundTripDelay() {
    return mediaRoundTripDelay;
  }

  public int getCallDuration() {
    return callDuration;
  }

  public void setMediaPacketsSentCount(int mediaPacketsSentCount) {
    this.mediaPacketsSentCount = mediaPacketsSentCount;
  }

  public void setMediaOctetSentCount(int mediaOctetSentCount) {
    this.mediaOctetSentCount = mediaOctetSentCount;
  }

  public void setMediaPacketsReceivedCount(int mediaPacketsReceivedCount) {
    this.mediaPacketsReceivedCount = mediaPacketsReceivedCount;
  }

  public void setMediaOctetReceivedCount(int mediaOctetReceivedCount) {
    this.mediaOctetReceivedCount = mediaOctetReceivedCount;
  }

  public void setMediaPacketsLostCount(int mediaPacketsLostCount) {
    this.mediaPacketsLostCount = mediaPacketsLostCount;
  }

  public void setMediaPacketsJitter(int mediaPacketsJitter) {
    this.mediaPacketsJitter = mediaPacketsJitter;
  }

  public void setMediaRoundTripDelay(int mediaRoundTripDelay) {
    this.mediaRoundTripDelay = mediaRoundTripDelay;
  }

  public void setCallDuration(int callDuration) {
    this.callDuration = callDuration;
  }
}
