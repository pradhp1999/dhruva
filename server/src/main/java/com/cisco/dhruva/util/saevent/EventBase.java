package com.cisco.dhruva.util.saevent;

public interface EventBase {

  /** Different event types */
  public enum EventType {
    ApplicationEvents,
    ProtocolEvents,
    ProtocolSubEvents,
    SystemEvents,
    SecurityEvents,
    DeviceEvents,
    HardwareEvents,
    GenericEvents,
    MediaStatsEvent
  };

  /** Event levels */
  public enum EventLevel {
    emerg,
    alert,
    crit,
    err,
    warn,
    notice,
    info,
    debug
  }

  /**
   * Returns JSON string in the SAEvent format
   *
   * @return
   */
  public String toEventJSON();
}
