package com.cisco.dhruva.util.saevent.dataparam;

// POJO class builder for operation events

public class OperationEventDataParam extends DataParam {
  public static class Builder {
    private String currentState;
    private String eventReceived;
    private String newState;

    public OperationEventDataParam build() {
      return new OperationEventDataParam(this);
    }

    public Builder currentState(String currentState) {
      this.currentState = currentState;
      return this;
    }

    public Builder eventReceived(String eventReceived) {
      this.eventReceived = eventReceived;
      return this;
    }

    public Builder newState(String newState) {
      this.newState = newState;
      return this;
    }
  }

  private String currentState;
  private String eventReceived;

  private String newState;

  private OperationEventDataParam(Builder builder) {
    this.eventReceived = builder.eventReceived;
    this.currentState = builder.currentState;
    this.newState = builder.newState;
  }

  public String getCurrentState() {
    return currentState;
  }

  public String getEventReceived() {
    return eventReceived;
  }

  public String getNewState() {
    return newState;
  }

  public void setCurrentState(String currentState) {
    this.currentState = currentState;
  }

  public void setEventReceived(String eventReceived) {
    this.eventReceived = eventReceived;
  }

  public void setNewState(String newState) {
    this.newState = newState;
  }
}
