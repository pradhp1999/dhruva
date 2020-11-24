package com.cisco.dhruva.app;

public class Destination {

  public final DestinationType destinationType;
  public final String address;
  public final String network;

  public Destination(DestinationType destinationType, String address, String network) {
    this.destinationType = destinationType;
    this.address = address;
    this.network = network;
  }

  public enum DestinationType {
    SRV,
    DEFAULT_SIP
  }

  @Override
  public String toString() {
    return "Destination{"
        + "destinationType="
        + destinationType
        + ", address='"
        + address
        + '\''
        + ", network='"
        + network
        + '\''
        + '}';
  }
};
