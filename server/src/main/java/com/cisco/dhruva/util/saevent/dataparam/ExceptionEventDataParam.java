package com.cisco.dhruva.util.saevent.dataparam;

// POJO class builder for exception events

public class ExceptionEventDataParam extends DataParam {
  public static class Builder {
    private String exception;
    private String exceptionInfo;
    private String stackTrace;

    public ExceptionEventDataParam build() {
      return new ExceptionEventDataParam(this);
    }

    public Builder exception(String exception) {
      this.exception = exception;
      return this;
    }

    public Builder exceptionInfo(String exceptionInfo) {
      this.exceptionInfo = exceptionInfo;
      return this;
    }

    public Builder stackTrace(String stackTrace) {
      this.stackTrace = stackTrace;
      return this;
    }
  }

  private String exception;
  private String exceptionInfo;
  private String stackTrace;

  private ExceptionEventDataParam(Builder builder) {
    this.exception = builder.exception;
    this.exceptionInfo = builder.exceptionInfo;
    this.stackTrace = builder.stackTrace;
  }

  public String getException() {
    return exception;
  }

  public String getExceptionInfo() {
    return exceptionInfo;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public void setException(String exception) {
    this.exception = exception;
  }

  public void setExceptionInfo(String exceptionInfo) {
    this.exceptionInfo = exceptionInfo;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }
}
