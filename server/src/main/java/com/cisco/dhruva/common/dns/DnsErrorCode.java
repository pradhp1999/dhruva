package com.cisco.dhruva.common.dns;

public enum DnsErrorCode {
  ERROR_DNS_QUERY_TIMEDOUT(900, "DNS-A-Query Failed, Timed Out.") {
    @Override
    public String toString() {
      return getDescription();
    }
  },
  ERROR_DNS_HOST_NOT_FOUND(901, "DNS-A-Query Failed, invalid host name.") {
    @Override
    public String toString() {
      return getDescription();
    }
  },
  ERROR_DNS_OTHER(902, "DNS-A-Querry Failed, Due To Any Other Reason") {
    @Override
    public String toString() {
      return getDescription();
    }
  },
  ERROR_DNS_INVALID_QUERY(903, "DNS , Invalid qury string") {
    @Override
    public String toString() {
      return getDescription();
    }
  },
  ERROR_DNS_INVALID_TYPE(904, "invalid dns type") {
    @Override
    public String toString() {
      return getDescription();
    }
  },
  ERROR_DNS_INTERNAL_ERROR(905, "internal execution error") {
    @Override
    public String toString() {
      return getDescription();
    }
  },
  ERROR_UNKNOWN(906, "unknown error") {
    @Override
    public String toString() {
      return getDescription();
    }
  };

  private final int value;
  private final String description;

  DnsErrorCode(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }
}
