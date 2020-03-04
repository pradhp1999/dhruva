package com.cisco.dhruva.context;

import com.cisco.dhruva.filters.FilterError;
import com.cisco.dhruva.util.DeepCopy;
import java.io.NotSerializableException;
import java.util.*;

/**
 * Represents the context between client and origin server for the duration of the dedicated
 * connection/session between them. But we're currently still only modelling single request/response
 * pair per session.
 *
 * <p>NOTE: Not threadsafe, and not intended to be used concurrently.
 */
public class CallContext extends HashMap<String, Object> implements Cloneable {

  private static final int INITIAL_SIZE = 64;
  private static final String KEY_EVENT_PROPS = "eventProperties";
  private static final String KEY_FILTER_ERRORS = "_filter_errors";
  private static final String KEY_FILTER_EXECS = "_filter_executions";

  private static final String KEY_UUID = "_uuid";

  public CallContext() {
    // Use a higher than default initial capacity for the hashmap as we generally have more than the
    // default
    // 16 entries.
    super(INITIAL_SIZE);

    put(KEY_FILTER_EXECS, new StringBuilder());
    put(KEY_EVENT_PROPS, new HashMap<String, Object>());
    put(KEY_FILTER_ERRORS, new ArrayList<FilterError>());
  }

  /**
   * Makes a copy of the RequestContext. This is used for debugging.
   *
   * @return
   */
  @Override
  public CallContext clone() {
    return (CallContext) super.clone();
  }

  public String getString(String key) {
    return (String) get(key);
  }

  /**
   * Convenience method to return a boolean value for a given key
   *
   * @param key
   * @return true or false depending what was set. default is false
   */
  public boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  /**
   * Convenience method to return a boolean value for a given key
   *
   * @param key
   * @param defaultResponse
   * @return true or false depending what was set. default defaultResponse
   */
  public boolean getBoolean(String key, boolean defaultResponse) {
    Boolean b = (Boolean) get(key);
    if (b != null) {
      return b.booleanValue();
    }
    return defaultResponse;
  }

  /**
   * sets a key value to Boolean.TRUE
   *
   * @param key
   */
  public void set(String key) {
    put(key, Boolean.TRUE);
  }

  /**
   * puts the key, value into the map. a null value will remove the key from the map
   *
   * @param key
   * @param value
   */
  public void set(String key, Object value) {
    if (value != null) put(key, value);
    else remove(key);
  }

  /**
   * Makes a copy of the SessionContext. This is used for debugging.
   *
   * @return
   */
  public CallContext copy() {
    CallContext copy = new CallContext();
    // do field by filed assignment

    Iterator<String> it = keySet().iterator();
    String key = it.next();
    while (key != null) {
      Object orig = get(key);
      try {
        Object copyValue = DeepCopy.copy(orig);
        if (copyValue != null) {
          copy.set(key, copyValue);
        } else {
          copy.set(key, orig);
        }
      } catch (NotSerializableException e) {
        copy.set(key, orig);
      }
      if (it.hasNext()) {
        key = it.next();
      } else {
        key = null;
      }
    }
    return copy;
  }

  public String getUUID() {
    return getString(KEY_UUID);
  }

  public void setUUID(String uuid) {
    set(KEY_UUID, uuid);
  }

  /**
   * Gets the throwable that will be use in the Error endpoint.
   *
   * @return a set throwable
   */
  public Throwable getError() {
    return (Throwable) get("_error");
  }

  /**
   * Sets throwable to use for generating a response in the Error endpoint.
   *
   * @param th
   */
  public void setError(Throwable th) {
    put("_error", th);
  }

  /** appends filter name and status to the filter execution history for the current request */
  public void addFilterExecutionSummary(String name, String status, long time) {
    StringBuilder sb = getFilterExecutionSummary();
    if (sb.length() > 0) sb.append(", ");
    sb.append(name).append('[').append(status).append(']').append('[').append(time).append("ms]");
  }

  /** @return String that represents the filter execution history for the current request */
  public StringBuilder getFilterExecutionSummary() {
    return (StringBuilder) get(KEY_FILTER_EXECS);
  }
}
