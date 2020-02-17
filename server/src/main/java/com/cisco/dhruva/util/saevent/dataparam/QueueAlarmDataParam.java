package com.cisco.dhruva.util.saevent.dataparam;

import java.io.Serializable;

// POJO class builder for invalid queue events

public class QueueAlarmDataParam extends DataParam implements Serializable {
  public static class Builder {
    private int activeThreadCount;
    private int droppedSinceOverflow;
    private String eventInfo;
    private String eventType;
    private int maximumThreads;
    private int queueMaxSize;
    private String queueName;
    private int queueSize;
    private String threadDumpInfo;
    private boolean threadDumpStatus;
    private int threshold;

    public Builder activeThreadCount(int activeThreadCount) {
      this.activeThreadCount = activeThreadCount;
      return this;
    }

    public QueueAlarmDataParam build() {
      return new QueueAlarmDataParam(this);
    }

    public Builder droppedSinceOverflow(int droppedSinceOverflow) {
      this.droppedSinceOverflow = droppedSinceOverflow;
      return this;
    }

    public Builder eventInfo(String eventInfo) {
      this.eventInfo = eventInfo;
      return this;
    }

    public Builder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder maximumThreads(int maximumThreads) {
      this.maximumThreads = maximumThreads;
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
  }

  private int activeThreadCount;
  private int droppedSinceOverflow;
  private int maximumThreads;
  private int queueMaxSize;
  private String queueName;
  private int queueSize;
  private String threadDumpInfo;
  private boolean threadDumpStatus;

  private int threshold;

  private QueueAlarmDataParam(Builder builder) {
    this.eventInfo = builder.eventInfo;
    this.eventType = builder.eventType;
    this.queueName = builder.queueName;
    this.queueSize = builder.queueSize;
    this.threshold = builder.threshold;
    this.queueMaxSize = builder.queueMaxSize;
    this.activeThreadCount = builder.activeThreadCount;
    this.maximumThreads = builder.maximumThreads;
    this.threadDumpStatus = builder.threadDumpStatus;
    this.threadDumpInfo = builder.threadDumpInfo;
    this.droppedSinceOverflow = builder.droppedSinceOverflow;
  }

  public int getActiveThreadCount() {
    return activeThreadCount;
  }

  public int getDroppedSinceOverflow() {
    return droppedSinceOverflow;
  }

  public int getMaximumThreads() {
    return maximumThreads;
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

  public boolean isThreadDumpStatus() {
    return threadDumpStatus;
  }

  public void setActiveThreadCount(int activeThreadCount) {
    this.activeThreadCount = activeThreadCount;
  }

  public void setDroppedSinceOverflow(int droppedSinceOverflow) {
    this.droppedSinceOverflow = droppedSinceOverflow;
  }

  public void setMaximumThreads(int maximumThreads) {
    this.maximumThreads = maximumThreads;
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
}
