package com.cisco.dhruva.util.saevent.dataparam;

import java.io.Serializable;

// POJO class builder for notification messages

public class MessageDataParam extends DataParam implements Serializable {
  public static class Builder {
    private String message;

    public MessageDataParam build() {
      return new MessageDataParam(this);
    }

    public Builder message(String message) {
      this.message = message;
      return this;
    }
  }

  private String message;

  private MessageDataParam(Builder builder) {
    this.message = builder.message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
