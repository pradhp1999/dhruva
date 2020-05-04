package com.cisco.dhruva.sip.bean;

import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.transport.Transport;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonDeserialize(builder = SIPListenPoint.SIPListenPointBuilder.class)
public class SIPListenPoint {

  private String name;

  private String hostIPAddress;

  private Transport transport;

  private int port;

  private boolean recordRoute;

  private SIPListenPoint(SIPListenPointBuilder listenPointBuilder) {
    this.name = listenPointBuilder.name;
    this.hostIPAddress = listenPointBuilder.hostIPAddress;
    this.transport = listenPointBuilder.transport;
    this.port = listenPointBuilder.port;
    this.recordRoute = listenPointBuilder.recordRoute;
  }

  public String getHostIPAddress() {
    return hostIPAddress;
  }

  public Transport getTransport() {
    return transport;
  }

  public int getPort() {
    return port;
  }

  public String getName() {
    return name;
  }

  public boolean isRecordRoute() {
    return recordRoute;
  }

  public String toString() {
    return new StringBuilder("ListenPoint name = ")
        .append(name)
        .append(" hostIPAddress = ")
        .append(hostIPAddress)
        .append(" transport = ")
        .append(transport)
        .append(" port = ")
        .append(port)
        .append(" recordRouteEnabled = ")
        .append(recordRoute)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || other.getClass() != this.getClass()) {
      return false;
    }

    SIPListenPoint otherListenPoint = (SIPListenPoint) other;
    return new EqualsBuilder()
        .append(name, otherListenPoint.getName())
        .append(hostIPAddress, otherListenPoint.getHostIPAddress())
        .append(port, otherListenPoint.getPort())
        .append(transport, otherListenPoint.getTransport())
        .append(recordRoute, otherListenPoint.isRecordRoute())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(name)
        .append(hostIPAddress)
        .append(port)
        .append(transport)
        .append(recordRoute)
        .toHashCode();
  }

  public static class SIPListenPointBuilder {
    @JsonProperty private String name;

    @JsonProperty private String hostIPAddress;

    @JsonProperty private Transport transport;

    @JsonProperty private int port;

    @JsonProperty private boolean recordRoute;

    public SIPListenPointBuilder() {
      this.name = "UDPNetwork";
      this.hostIPAddress = "0.0.0.0";
      this.transport = DhruvaSIPConfigProperties.DEFAULT_TRANSPORT;
      this.port = DhruvaSIPConfigProperties.DEFAULT_PORT_UDP;
      this.recordRoute = DhruvaSIPConfigProperties.DEFAULT_RECORD_ROUTE_ENABLED;
    }

    public SIPListenPointBuilder setHostIPAddress(String hostIPAddress) {
      this.hostIPAddress = hostIPAddress;
      return this;
    }

    public SIPListenPointBuilder setTransport(Transport transport) {
      this.transport = transport;
      return this;
    }

    public SIPListenPointBuilder setPort(int port) {
      this.port = port;
      return this;
    }

    public SIPListenPointBuilder setName(String name) {
      this.name = name;
      return this;
    }

    public SIPListenPointBuilder setRecordRoute(boolean recordRoute) {
      this.recordRoute = recordRoute;
      return this;
    }

    public SIPListenPoint build() {
      return new SIPListenPoint(this);
    }
  }
}
