package com.cisco.dhruva.common.message;

import com.cisco.dhruva.context.CallContext;
import com.google.common.base.Charsets;
import java.util.HashMap;

public class DhruvaMessageImpl implements DhruvaMessage {
  protected final CallContext context;
  protected MessageHeaders headers;
  private byte[] payload;

  private boolean hasBody;

  public DhruvaMessageImpl(CallContext context) {
    this(context, new MessageHeaders(new HashMap<>()));
  }

  public DhruvaMessageImpl(CallContext context, MessageHeaders headers) {
    this.context = context == null ? new CallContext() : context;
    this.headers = headers == null ? new MessageHeaders(new HashMap<String, Object>()) : headers;
    this.payload = new byte[1000];
  }

  @Override
  public CallContext getContext() {
    return context;
  }

  @Override
  public MessageHeaders getHeaders() {
    return headers;
  }

  @Override
  public void setHeaders(MessageHeaders newHeaders) {
    this.headers = newHeaders;
  }

  @Override
  public void setHasBody(boolean hasBody) {
    this.hasBody = hasBody;
  }

  @Override
  public boolean hasBody() {
    return hasBody;
  }

  private void setContentLength(int length) {}

  @Override
  public void setBodyAsText(String bodyText) {}

  @Override
  public void setBody(byte[] body) {}

  @Override
  public String getBodyAsText() {
    final byte[] body = getBody();
    return (body != null && body.length > 0) ? new String(getBody(), Charsets.UTF_8) : null;
  }

  @Override
  public int getMaxBodySize() {
    return 0;
  }

  @Override
  public byte[] getBody() {
    return payload;
  }

  @Override
  public int getBodyLength() {
    return 0;
  }

  @Override
  public DhruvaMessage clone() {
    final DhruvaMessageImpl copy = new DhruvaMessageImpl(context.clone(), headers);
    return copy;
  }
}
