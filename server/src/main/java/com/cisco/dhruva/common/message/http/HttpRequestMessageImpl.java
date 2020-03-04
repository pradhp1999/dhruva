package com.cisco.dhruva.common.message.http;

import com.cisco.dhruva.common.message.DhruvaMessage;
import com.cisco.dhruva.common.message.MessageHeaders;
import com.cisco.dhruva.context.CallContext;
import javax.annotation.Nullable;

public class HttpRequestMessageImpl implements DhruvaMessage {
  @Override
  public CallContext getContext() {
    return null;
  }

  @Override
  public MessageHeaders getHeaders() {
    return null;
  }

  @Override
  public void setHeaders(MessageHeaders newHeaders) {}

  @Override
  public boolean hasBody() {
    return false;
  }

  @Override
  public void setHasBody(boolean hasBody) {}

  @Nullable
  @Override
  public byte[] getBody() {
    return new byte[0];
  }

  @Override
  public int getBodyLength() {
    return 0;
  }

  @Override
  public void setBody(@Nullable byte[] body) {}

  @Override
  public void setBodyAsText(@Nullable String bodyText) {}

  @Nullable
  @Override
  public String getBodyAsText() {
    return null;
  }

  @Override
  public int getMaxBodySize() {
    return 0;
  }

  @Override
  public DhruvaMessage clone() {
    return null;
  }
}
