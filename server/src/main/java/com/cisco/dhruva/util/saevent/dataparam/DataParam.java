package com.cisco.dhruva.util.saevent.dataparam;

import java.io.Serializable;

// abstract POJO class for common parameters of SaEvent

public abstract class DataParam implements Serializable {
  protected String eventInfo;
  protected String eventType;

  public String getEventInfo() {
    return eventInfo;
  }

  public void setEventInfo(String eventInfo) {
    this.eventInfo = eventInfo;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
}
