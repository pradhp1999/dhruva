package com.cisco.dhruva.util.saevent.dataparam;

import com.cisco.dhruva.util.saevent.EventBase;
import java.io.Serializable;

// POJO class builder for invalid connection events

public class InvalidConnectionDataParam extends DataParam implements Serializable {
  public static class Builder {
    private String connection;
    private String eventInfo;
    private EventBase.EventLevel eventLevel;
    private String eventType;

    public InvalidConnectionDataParam build() {
      return new InvalidConnectionDataParam(this);
    }

    public Builder connection(String connection) {
      this.connection = connection;
      return this;
    }

    public Builder eventInfo(String eventInfo) {
      this.eventInfo = eventInfo;
      return this;
    }

    public Builder eventLevel(EventBase.EventLevel eventLevel) {
      this.eventLevel = eventLevel;
      return this;
    }

    public Builder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }
  }

  private String connection;
  private EventBase.EventLevel eventLevel;

  private InvalidConnectionDataParam(Builder builder) {
    this.eventType = builder.eventType;
    this.eventInfo = builder.eventInfo;
    this.connection = builder.connection;
    this.eventLevel = builder.eventLevel;
  }

  public String getConnection() {
    return connection;
  }

  public EventBase.EventLevel getEventLevel() {
    return eventLevel;
  }

  public void setConnection(String connection) {
    this.connection = connection;
  }

  public void setEventLevel(EventBase.EventLevel eventLevel) {
    this.eventLevel = eventLevel;
  }
}
