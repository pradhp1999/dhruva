package com.cisco.dhruva.common.message;

public interface Message<T> {

  T getPayload();

  MessageHeaders getHeaders();
}
