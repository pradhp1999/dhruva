package com.cisco.dhruva.util.saevent.dataparam;

import java.io.Serializable;

// POJO class builder for Server Group status events

public class SGStatusDataParam extends DataParam implements Serializable {
  public static class Builder {
    private String eventType;
    private String eventInfo;
    private String serverGroupName;

    public SGStatusDataParam build() {
      return new SGStatusDataParam(this);
    }

    public Builder eventInfo(String eventInfo) {
      this.eventInfo = eventInfo;
      return this;
    }

    public Builder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder serverGroupName(String serverGroupName) {
      this.serverGroupName = serverGroupName;
      return this;
    }
  }

  private String serverGroupName;

  private SGStatusDataParam(Builder builder) {
    this.eventType = builder.eventType;
    this.eventInfo = builder.eventInfo;
    this.serverGroupName = builder.serverGroupName;
  }

  public String getServerGroupName() {
    return serverGroupName;
  }

  public void setServerGroupName(String serverGroupName) {
    this.serverGroupName = serverGroupName;
  }
}
