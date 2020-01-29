package com.cisco.dhruva.util.saevent.dataparam;

public class TlsSendBufferDataParam extends DataParam {
  public static class Builder {
    private String eventType;
    private String endpoint;
    private String eventInfo;

    public TlsSendBufferDataParam build() {
      return new TlsSendBufferDataParam(this);
    }

    public Builder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder endpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public Builder eventInfo(String eventInfo) {
      this.eventInfo = eventInfo;
      return this;
    }
  }

  private String endpoint;

  public TlsSendBufferDataParam(Builder builder) {
    this.eventType = builder.eventType;
    this.endpoint = builder.endpoint;
    this.eventInfo = builder.eventInfo;
  }

  public String getEndpoint() {
    return endpoint;
  }
}
