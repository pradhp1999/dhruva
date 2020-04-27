package com.cisco.dhruva.common.messaging.models;

import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;

public class MessageHeaders extends HashMap<String, Object> implements Serializable {

  public static final UUID ID_VALUE_NONE = new UUID(0, 0);

  public static final String ID = "id";

  public static final String TIMESTAMP = "timestamp";

  public static final String CONTENT_TYPE = "contentType";

  private static final long serialVersionUID = 7035068984263400920L;

  private Logger logger = DhruvaLoggerFactory.getLogger(MessageHeaders.class);

  private static final IdGenerator defaultIdGenerator = new AlternativeJdkIdGenerator();

  @Nullable private static volatile IdGenerator idGenerator;

  private final Map<String, Object> headers;

  public MessageHeaders(@Nullable Map<String, Object> headers) {
    this(headers, null, null);
  }

  protected MessageHeaders(
      @Nullable Map<String, Object> headers, @Nullable UUID id, @Nullable Long timestamp) {
    this.headers = (headers != null ? new HashMap<>(headers) : new HashMap<>());

    if (id == null) {
      this.headers.put(ID, getIdGenerator().generateId());
    } else if (id == ID_VALUE_NONE) {
      this.headers.remove(ID);
    } else {
      this.headers.put(ID, id);
    }

    if (timestamp == null) {
      this.headers.put(TIMESTAMP, System.currentTimeMillis());
    } else if (timestamp < 0) {
      this.headers.remove(TIMESTAMP);
    } else {
      this.headers.put(TIMESTAMP, timestamp);
    }
  }

  protected Map<String, Object> getRawHeaders() {
    return this.headers;
  }

  protected static IdGenerator getIdGenerator() {
    IdGenerator generator = idGenerator;
    return (generator != null ? generator : defaultIdGenerator);
  }

  @Nullable
  public UUID getId() {
    return get(ID, UUID.class);
  }

  @Nullable
  public Long getTimestamp() {
    return get(TIMESTAMP, Long.class);
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public <T> T get(Object key, Class<T> type) {
    Object value = this.headers.get(key);
    if (value == null) {
      return null;
    }
    if (!type.isAssignableFrom(value.getClass())) {
      logger.warn("exception trying to get the header obj", key);
      throw new IllegalArgumentException(
          "Incorrect type specified for header '"
              + key
              + "'. Expected ["
              + type
              + "] but actual type is ["
              + value.getClass()
              + "]");
    }
    return (T) value;
  }

  public void set(String key, Object value) {
    if (value != null) headers.put(key, value);
    else headers.remove(key);
  }

  @Override
  public boolean equals(@Nullable Object other) {
    return (this == other
        || (other instanceof MessageHeaders
            && this.headers.equals(((MessageHeaders) other).headers)));
  }

  @Override
  public int hashCode() {
    return this.headers.hashCode();
  }

  @Override
  public String toString() {
    return this.headers.toString();
  }
}
