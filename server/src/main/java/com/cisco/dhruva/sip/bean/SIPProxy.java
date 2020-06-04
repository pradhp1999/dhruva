package com.cisco.dhruva.sip.bean;

import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SIPProxy {

  private boolean errorAggregrator;
  private boolean createDNSServerGroup;

  private SIPProxy(SIPProxyBuilder proxyBuilder) {
    this.errorAggregrator = proxyBuilder.errorAggregrator;
    this.createDNSServerGroup = proxyBuilder.createDNSServerGroup;
  }

  public boolean isErrorAggregratorEnabled() {
    return errorAggregrator;
  }

  public boolean isCreateDNSServergroupEnabled() {
    return createDNSServerGroup;
  }

  public String toString() {
    return new StringBuilder("SIPProxy isErrorAggregratorEnabled = ")
        .append(errorAggregrator)
        .append(" isCreateDNSServergroupEnabled = ")
        .append(createDNSServerGroup)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || other.getClass() != this.getClass()) {
      return false;
    }

    SIPProxy otherProxy = (SIPProxy) other;
    return new EqualsBuilder()
        .append(errorAggregrator, otherProxy.isErrorAggregratorEnabled())
        .append(createDNSServerGroup, otherProxy.isCreateDNSServergroupEnabled())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(errorAggregrator).append(createDNSServerGroup).toHashCode();
  }

  public static class SIPProxyBuilder {
    @JsonProperty private boolean errorAggregrator;

    @JsonProperty private boolean createDNSServerGroup;

    public SIPProxyBuilder() {
      this.errorAggregrator = DhruvaSIPConfigProperties.DEFAULT_PROXY_ERROR_AGGREGATOR_ENABLED;
      this.createDNSServerGroup =
          DhruvaSIPConfigProperties.DEFAULT_PROXY_CREATE_DNSSERVERGROUP_ENABLED;
    }

    public SIPProxyBuilder setErrorAggregrator(boolean errorAggregrator) {
      this.errorAggregrator = errorAggregrator;
      return this;
    }

    public SIPProxyBuilder setCreateDNSServergroup(boolean createDNSServerGroup) {
      this.createDNSServerGroup = createDNSServerGroup;
      return this;
    }

    public SIPProxy build() {
      return new SIPProxy(this);
    }
  }
}
