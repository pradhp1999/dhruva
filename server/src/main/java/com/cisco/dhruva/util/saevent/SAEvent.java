package com.cisco.dhruva.util.saevent;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.util.JSONBuilder;
import com.cisco.dhruva.util.saevent.dataparam.DataParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class SAEvent implements EventBase {

  public static class Builder {
    private long ts;
    private EventType type;
    private String id;
    private EventLevel level;
    private DataParam dataParam;

    public SAEvent build() {
      return new SAEvent(this);
    }

    public Builder ts() {
      this.ts = Instant.now().toEpochMilli();
      return this;
    }

    public Builder eventType(EventType type) {
      this.type = type;
      return this;
    }

    public Builder id() {
      this.id = "CloudproxyEvent";
      return this;
    }

    public Builder level() {
      this.level = EventLevel.info;
      return this;
    }

    public Builder dataparam(DataParam dataParam) {
      this.dataParam = dataParam;
      return this;
    }
  }

  /** Timestamp in the form of EPOCH */
  private long ts = Instant.now().toEpochMilli();

  /** Type of the event */
  private EventType type;

  /** A unique application defined String identifier , setting default to CloudProxyEvent */
  private String id = "CloudproxyEvent";

  /** severity Level of the event , setting default to info */
  private EventLevel level = EventLevel.info;

  /** Information on source of Event */
  private HashMap eventSource;

  /** Application additional key value pairs , assigning null as this is optional */
  private HashMap keyParam = null;

  /**
   * Optional application defined string to describe a textual reason for generating the Event
   * Maximum length in 255 characters
   */
  private String eventReason = null;

  /**
   * The value is one of "raise" or "clear" for an alarm pertaining to raising or clearing the alarm
   */
  private String condition = null;

  /** A custom and optional set of application key value parameters */
  private DataParam dataParam;

  private Map[] correlatedEventsAlarms;

  private static String hostname = "";

  static {
    try {
      InetAddress ip = InetAddress.getLocalHost();
      hostname = ip.getHostName();
    } catch (Exception e) {
      // Caution:Do not change the level to error , doing so will cause circular logging in
      // ExceptionEventAppender
      DsLog4j.saEventTraceLog.info("Error getting the hostname in SAEvent", e);
    }
  }

  public SAEvent(Builder builder) {
    this();
    this.ts = builder.ts;
    this.type = builder.type;
    this.id = builder.id;
    this.level = builder.level;
    this.dataParam = builder.dataParam;
  }

  public SAEvent() {
    eventSource = new HashMap();
    eventSource.put("serviceID", "cloudproxy.dsnrs");
    eventSource.put("serviceInstanceID", "0");
    eventSource.put("customerID", "none");
    eventSource.put("hostname", hostname);
  }

  public long getTs() {
    return ts;
  }

  public void setTs(long ts) {
    this.ts = ts;
  }

  public EventType getType() {
    return type;
  }

  public void setType(EventType type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public EventLevel getLevel() {
    return level;
  }

  public void setLevel(EventLevel level) {
    this.level = level;
  }

  public HashMap getEventSource() {
    return eventSource;
  }

  public void setEventSource(HashMap eventSource) {
    this.eventSource = eventSource;
  }

  public HashMap getKeyParam() {
    return keyParam;
  }

  public void setKeyParam(HashMap keyParam) {
    this.keyParam = keyParam;
  }

  public String getEventReason() {
    return eventReason;
  }

  public void setEventReason(String eventReason) {
    this.eventReason = eventReason;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public DataParam getDataParam() {
    return dataParam;
  }

  public void setDataParam(DataParam dataParam) {
    this.dataParam = dataParam;
  }

  public Map[] getCorrelatedEventsAlarms() {
    return correlatedEventsAlarms;
  }

  public void setCorrelatedEventsAlarms(Map[] correlatedEventsAlarms) {
    this.correlatedEventsAlarms = correlatedEventsAlarms;
  }

  public String toJSONString() {
    String jsonString = null;
    try {
      jsonString = JSONBuilder.toJSON(this);
    } catch (JsonProcessingException e) {
      // Caution:Do not change the level to error , doing so will cause circular logging in
      // ExceptionEventAppender
      DsLog4j.saEventTraceLog.info("Error Parsing SAEvent object " + e.getMessage());
    }
    return jsonString;
  }

  /**
   * Returns JSON string in the SAEvent format
   *
   * @return
   */
  public String toEventJSON() {
    return "SAEVENT " + toJSONString();
  }
}
