package com.cisco.dhruva.common.message;

import java.util.Map;
import javax.annotation.Nullable;

public class ErrorMessage extends GenericMessage<Throwable> {

  private static final long serialVersionUID = -5470210965279837728L;

  @Nullable private final Message<?> originalMessage;

  /**
   * Create a new message with the given payload.
   *
   * @param payload the message payload (never {@code null})
   */
  public ErrorMessage(Throwable payload) {
    super(payload);
    this.originalMessage = null;
  }

  /**
   * Create a new message with the given payload and headers. The content of the given header map is
   * copied.
   *
   * @param payload the message payload (never {@code null})
   * @param headers message headers to use for initialization
   */
  public ErrorMessage(Throwable payload, Map<String, Object> headers) {
    super(payload, headers);
    this.originalMessage = null;
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
  public ErrorMessage(Throwable payload, MessageHeaders headers) {
    super(payload, headers);
    this.originalMessage = null;
  }

  /**
   * Create a new message with the given payload and original message.
   *
   * @param payload the message payload (never {@code null})
   * @param originalMessage the original message (if present) at the point in the stack where the
   *     ErrorMessage was created
   * @since 5.0
   */
  public ErrorMessage(Throwable payload, Message<?> originalMessage) {
    super(payload);
    this.originalMessage = originalMessage;
  }

  /**
   * Create a new message with the given payload, headers and original message. The content of the
   * given header map is copied.
   *
   * @param payload the message payload (never {@code null})
   * @param headers message headers to use for initialization
   * @param originalMessage the original message (if present) at the point in the stack where the
   *     ErrorMessage was created
   * @since 5.0
   */
  public ErrorMessage(Throwable payload, Map<String, Object> headers, Message<?> originalMessage) {
    super(payload, headers);
    this.originalMessage = originalMessage;
  }

  /**
   * Create a new message with the payload, {@link MessageHeaders} and original message.
   *
   * <p><strong>Note:</strong> the given {@code MessageHeaders} instance is used directly in the new
   * message, i.e. it is not copied.
   *
   * @param payload the message payload (never {@code null})
   * @param headers message headers
   * @param originalMessage the original message (if present) at the point in the stack where the
   *     ErrorMessage was created
   * @since 5.0
   */
  public ErrorMessage(Throwable payload, MessageHeaders headers, Message<?> originalMessage) {
    super(payload, headers);
    this.originalMessage = originalMessage;
  }

  /**
   * Return the original message (if available) at the point in the stack where the ErrorMessage was
   * created.
   *
   * @since 5.0
   */
  @Nullable
  public Message<?> getOriginalMessage() {
    return this.originalMessage;
  }

  @Override
  public String toString() {
    if (this.originalMessage == null) {
      return super.toString();
    }
    return super.toString() + " for original " + this.originalMessage;
  }
}
