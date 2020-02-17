package com.cisco.dhruva.util.saevent.dataparam;

import java.io.Serializable;

// POJO class builder for TLS Queue size events

public class TLSQueueEventDataParam extends DataParam implements Serializable {
  public static class Builder {
    private String bindingInfo;
    private String eventInfo;
    private String eventType;
    private int queueMaxSize;
    private String queueName;
    private int queueSize;
    private String threadDumpInfo;
    private boolean threadDumpStatus;
    private int threshold;
    private String transport;

    public Builder bindingInfo(String bindingInfo) {
      this.bindingInfo = bindingInfo;
      return this;
    }

    public TLSQueueEventDataParam build() {
      return new TLSQueueEventDataParam(this);
    }

    public Builder eventInfo(String eventInfo) {
      this.eventInfo = eventInfo;
      return this;
    }

    public Builder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder queueMaxSize(int queueMaxSize) {
      this.queueMaxSize = queueMaxSize;
      return this;
    }

    public Builder queueName(String queueName) {
      this.queueName = queueName;
      return this;
    }

    public Builder queueSize(int queueSize) {
      this.queueSize = queueSize;
      return this;
    }

    public Builder threadDumpInfo(String threadDumpInfo) {
      this.threadDumpInfo = threadDumpInfo;
      return this;
    }

    public Builder threadDumpStatus(boolean threadDumpStatus) {
      this.threadDumpStatus = threadDumpStatus;
      return this;
    }

    public Builder threshold(int threshold) {
      this.threshold = threshold;
      return this;
    }

    public Builder transport(String transport) {
      this.transport = transport;
      return this;
    }
  }

  private String bindingInfo;
  private int queueMaxSize;
  private String queueName;
  private int queueSize;
  private String threadDumpInfo;
  private boolean threadDumpStatus;
  private int threshold;

  private String transport;

  private TLSQueueEventDataParam(Builder builder) {
    this.eventInfo = builder.eventInfo;
    this.eventType = builder.eventType;
    this.queueName = builder.queueName;
    this.queueSize = builder.queueSize;
    this.threshold = builder.threshold;
    this.queueMaxSize = builder.queueMaxSize;
    this.transport = builder.transport;
    this.bindingInfo = builder.bindingInfo;
    this.threadDumpStatus = builder.threadDumpStatus;
    this.threadDumpInfo = builder.threadDumpInfo;
  }

  public String getBindingInfo() {
    return bindingInfo;
  }

  public int getQueueMaxSize() {
    return queueMaxSize;
  }

  public String getQueueName() {
    return queueName;
  }

  public int getQueueSize() {
    return queueSize;
  }

  public String getThreadDumpInfo() {
    return threadDumpInfo;
  }

  public int getThreshold() {
    return threshold;
  }

  public String getTransport() {
    return transport;
  }

  public boolean isThreadDumpStatus() {
    return threadDumpStatus;
  }

  public void setBindingInfo(String bindingInfo) {
    this.bindingInfo = bindingInfo;
  }

  public void setQueueMaxSize(int queueMaxSize) {
    this.queueMaxSize = queueMaxSize;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  public void setThreadDumpInfo(String threadDumpInfo) {
    this.threadDumpInfo = threadDumpInfo;
  }

  public void setThreadDumpStatus(boolean threadDumpStatus) {
    this.threadDumpStatus = threadDumpStatus;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public void setTransport(String transport) {
    this.transport = transport;
  }
}
