package com.cisco.dhruva.util.saevent.dataparam;

import java.io.Serializable;

// POJO class builder for Server Group Element status events

public class SGElementStatusDataParam extends DataParam implements Serializable {

  public static class Builder {
    private String endpointInfo;
    private String eventInfo;
    private String eventType;
    private String failureReason;
    private String serverGroupName;

    public SGElementStatusDataParam build() {
      return new SGElementStatusDataParam(this);
    }

    public Builder endpointInfo(String endpointInfo) {
      this.endpointInfo = endpointInfo;
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

    public Builder failureReason(String failureReason) {
      this.failureReason = failureReason;
      return this;
    }

    public Builder serverGroupName(String serverGroupName) {
      this.serverGroupName = serverGroupName;
      return this;
    }
  }

  private String endpointInfo;
  private String failureReason;
  private String serverGroupName;

  private SGElementStatusDataParam(Builder builder) {
    this.endpointInfo = builder.endpointInfo;
    this.failureReason = builder.failureReason;
    this.serverGroupName = builder.serverGroupName;
    this.eventType = builder.eventType;
    this.eventInfo = builder.eventInfo;
  }

  public String getEndpointInfo() {
    return endpointInfo;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public String getServerGroupName() {
    return serverGroupName;
  }

  public void setEndpointInfo(String endpointInfo) {
    this.endpointInfo = endpointInfo;
  }

  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }

  public void setServerGroupName(String serverGroupName) {
    this.serverGroupName = serverGroupName;
  }
}
