package com.cisco.dhruva.common.message;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class GenericMessage<T> implements Message<T>, Serializable {

  private static final long serialVersionUID = 4268801052358035098L;

  private final T payload;

  private final MessageHeaders headers;

  /**
   * Create a new message with the given payload.
   *
   * @param payload the message payload (never {@code null})
   */
  public GenericMessage(T payload) {
    this(payload, new MessageHeaders(null));
  }

  /**
   * Create a new message with the given payload and headers. The content of the given header map is
   * copied.
   *
   * @param payload the message payload (never {@code null})
   * @param headers message headers to use for initialization
   */
  public GenericMessage(T payload, Map<String, Object> headers) {
    this(payload, new MessageHeaders(headers));
  }

  /**
   * A constructor with the {@link MessageHeaders} instance to use.
   *
   * <p><strong>Note:</strong> the given {@code MessageHeaders} instance is used directly in the new
   * message, i.e. it is not copied.
   *
   * @param payload the message payload (never {@code null})
   * @param headers message headers
   */
  public GenericMessage(T payload, MessageHeaders headers) {
    Assert.notNull(payload, "Payload must not be null");
    Assert.notNull(headers, "MessageHeaders must not be null");
    this.payload = payload;
    this.headers = headers;
  }

  @Override
  public T getPayload() {
    return this.payload;
  }

  @Override
  public MessageHeaders getHeaders() {
    return this.headers;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GenericMessage)) {
      return false;
    }
    GenericMessage<?> otherMsg = (GenericMessage<?>) other;
    // Using nullSafeEquals for proper array equals comparisons
    return (ObjectUtils.nullSafeEquals(this.payload, otherMsg.payload)
        && this.headers.equals(otherMsg.headers));
  }

  @Override
  public int hashCode() {
    // Using nullSafeHashCode for proper array hashCode handling
    return (ObjectUtils.nullSafeHashCode(this.payload) * 23 + this.headers.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    sb.append(" [payload=");
    if (this.payload instanceof byte[]) {
      sb.append("byte[").append(((byte[]) this.payload).length).append("]");
    } else {
      sb.append(this.payload);
    }
    sb.append(", headers=").append(this.headers).append("]");
    return sb.toString();
  }
}
