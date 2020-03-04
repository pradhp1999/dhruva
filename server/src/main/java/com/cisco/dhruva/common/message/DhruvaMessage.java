package com.cisco.dhruva.common.message;

import com.cisco.dhruva.context.CallContext;
import javax.annotation.Nullable;

/** Represents a message that propagates through the filter chain. */
public interface DhruvaMessage extends Cloneable {

  /** Returns the session context of this message. */
  CallContext getContext();

  /**
   * TODO Returns the headers for this message. They may be request or response headers, depending
   * on the underlying type of this object. For some messages, there may be no headers, such as with
   * chunked requests or responses. In this case, a non-{@code null} default headers value will be
   * returned.
   */
  MessageHeaders getHeaders();

  void setHeaders(MessageHeaders newHeaders);

  boolean hasBody();

  void setHasBody(boolean hasBody);

  /** Returns the message body. If there is no message body, this returns {@code null}. */
  @Nullable
  byte[] getBody();

  /** Returns the length of the message body, or {@code 0} if there isn't a message present. */
  int getBodyLength();

  /**
   * Sets the message body. Note: if the {@code body} is {@code null}, this may not reset the body
   * presence as returned by {@link #hasBody}. The body is considered complete after calling this
   * method.
   */
  void setBody(@Nullable byte[] body);

  /**
   * Sets the message body as UTF-8 encoded text. Note that this does NOT set any headers related to
   * the Content-Type; callers must set or reset the content type to UTF-8. The body is considered
   * complete after calling this method.
   */
  void setBodyAsText(@Nullable String bodyText);

  /** Gets the body of this message as UTF-8 text, or {@code null} if there is no body. */
  @Nullable
  String getBodyAsText();

  /**
   * Returns the maximum body size that this message is willing to hold. This value value should be
   * more than the sum of lengths of the body chunks. The max body size may not be strictly
   * enforced, and is informational.
   */
  int getMaxBodySize();

  /** Returns a copy of this message. */
  DhruvaMessage clone();
}
