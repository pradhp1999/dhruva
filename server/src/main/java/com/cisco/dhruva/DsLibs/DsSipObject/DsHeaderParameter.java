// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import gnu.trove.TLinkedList;
import java.io.*;

/**
 * This class represents an header parameter element that can be a member of DsHeaderParameters
 * list. This class behaves as a container for a parameter key and its value.
 */
public class DsHeaderParameter extends DsParameter {
  /** Represents the list of value part in key-value pair of this parameter. */
  TLinkedList list;

  /** Represents the Header type that this header parameter represents. */
  byte m_bType;

  /**
   * Constructs this parameter with the specified <code>key</code> and the specified <code>value
   * </code>.
   *
   * @param key the parameter key
   * @param value the parameter value
   */
  public DsHeaderParameter(DsByteString key, DsByteString value) {
    super(key, value);
  }

  /**
   * Returns the value of the first header parameter.
   *
   * @return the value of the first header parameter.
   * @see #setValue(DsByteString)
   */
  public final DsByteString getValue() {
    if (list != null && list.size() > 0) {
      return ((DsSipHeaderString) list.getFirst()).getValue();
    } else {
      return null;
    }
  }

  /**
   * Replaces all the existing header parameter values with this new header parameter <code>value
   * </code>.
   *
   * @param value the new value of the header parameter.
   * @see #getValue()
   */
  public final void setValue(DsByteString value) {
    if (value == null) {
      value = DsByteString.BS_EMPTY_STRING;
    }

    if (list == null) {
      list = new TLinkedList();
    }

    list.clear();
    list.addFirst(new DsSipHeaderString(m_bType, key, value));
  }

  /**
   * Returns the list of the header parameter values. These values would be in the DsSipHeaderString
   * form. The underlying reference to the list is returned. So any changes to this list object
   * would be reflected in this parameter list.
   *
   * @return the list of the header parameter values.
   */
  public final TLinkedList getValues() {
    return list;
  }

  /**
   * Add the specified header value at the end of list.
   *
   * @param value the value for the header parameter.
   */
  public final void add(DsByteString value) {
    add(value, false);
  }

  /**
   * Add the specified header value. If <code>start</code> is <code>true</code>, then it will be
   * added on the front, otherwise at the end of list.
   *
   * @param value the value for the header parameter.
   * @param start if true add at beginning, else add at end
   */
  public final void add(DsByteString value, boolean start) {
    if (value == null) {
      value = DsByteString.BS_EMPTY_STRING;
    }

    if (list == null) {
      list = new TLinkedList();
    }

    DsSipHeaderString str = new DsSipHeaderString(m_bType, key, value);
    if (start) list.addFirst(str);
    else list.addLast(str);
  }

  /**
   * Removes the header value from the end of list.
   *
   * @return the removed header value.
   */
  public final DsByteString remove() {
    return remove(false);
  }

  /**
   * Removes the header value. If <code>start</code> is <code>true</code>, then it will be removed
   * from the front, otherwise from the end of list.
   *
   * @param start If <code>true</code>, then the header will be removed from the front, otherwise
   *     from the end of list.
   * @return the removed header value.
   */
  public final DsByteString remove(boolean start) {
    if (list == null || list.size() == 0) {
      return DsByteString.BS_EMPTY_STRING;
    }

    DsSipHeaderString str = null;
    if (start) str = (DsSipHeaderString) list.removeFirst();
    else str = (DsSipHeaderString) list.removeLast();
    return (null != str) ? str.getValue() : null;
  }

  /**
   * Returns the key of this parameter.
   *
   * @return the key of this parameter.
   * @see #setKey(DsByteString)
   */
  public final DsByteString getKey() {
    return key;
  }

  /**
   * Sets the key of this parameter to the specified <code>key</code>.
   *
   * @param key the new key of this parameter.
   * @see #getKey()
   * @throws IllegarArgumentException if the specified key specifies a different header name than
   *     the header name this header parameter was constructed with.
   */
  public final void setKey(DsByteString key) {
    if (!equals(key)) {
      throw new IllegalArgumentException(
          "The specified header name [" + key + "] doesn't match with the original header name.");
    }
  }

  /**
   * Sets the specified <code>key</code> and the <code>value</code> to this parameter. It overrides
   * the previous key and value of this parameter.
   *
   * @param key the new key of this parameter, must not be <code>null</code>.
   * @param value the new value of this parameter.
   * @throws IllegalArgumentException if <code>key</code> is <code>null</code>.
   */
  public final void set(DsByteString key, DsByteString value) {
    if (key == null) {
      throw new IllegalArgumentException("The key may not be null.");
    }
    if (value == null) {
      value = DsByteString.BS_EMPTY_STRING;
    }

    m_bType = (byte) DsSipMsgParser.getHeader(key);
    this.key = key;

    if (list == null) {
      list = new TLinkedList();
    } else {
      list.clear();
    }

    this.add(value);
  }

  /**
   * Tells whether the specified <code>key</code> is equal to this parameter's key.
   *
   * @param key the key that needs to be compared with this parameter's key
   * @return <code>true</code> if the specified <code>key</code> is equal to this parameter's key,
   *     <code>false</code> otherwise.
   */
  public final boolean equals(DsByteString key) {
    int id = DsSipMsgParser.getHeader(key);

    return (id == DsSipConstants.UNKNOWN_HEADER) ? this.key.equalsIgnoreCase(key) : (id == m_bType);
  }

  /**
   * Tells whether the specified <code>param</code> is equal to this parameter. The two parameters
   * are equal if their keys and corresponding values are equal.
   *
   * @param object the parameter object that needs to be compared with this parameter.
   * @return <code>true</code> if the specified <code>param</code> is equal to this parameter,
   *     <code>false</code> otherwise.
   */
  public final boolean equals(DsParameter object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }

    DsHeaderParameter param = null;
    try {
      param = (DsHeaderParameter) object;
    } catch (ClassCastException exc) {
      return false;
    }

    if (list == null && param.list == null) return true;
    if (list == null && param.list != null) return false;
    if (list != null && param.list == null) return false;

    if (list.size() != param.list.size()) return false;

    boolean result = false;

    if (equals(param.key)) {
      DsSipHeaderString str1 = (DsSipHeaderString) list.getFirst();
      while (null != str1) {
        result = false;
        DsSipHeaderString str2 = (DsSipHeaderString) param.list.getFirst();
        while (null != str2) {
          if (str1.getValue().equals(str2.getValue())) {
            result = true;
            break;
          }
          str2 = (DsSipHeaderString) str2.getNext();
        }
        if (!result) break;
        str1 = (DsSipHeaderString) str1.getNext();
      }
    }
    return result;
  }

  /**
   * Returns a clone of this parameter object. The reference to next and previous elements are set
   * to nullin the returned object.
   *
   * @return a clone of this parameter object.
   */
  public Object clone() {
    DsHeaderParameter clone = (DsHeaderParameter) super.clone();
    clone.list = new TLinkedList();

    if (list != null && list.size() > 0) {
      DsSipHeaderString str1 = (DsSipHeaderString) list.getFirst();
      while (null != str1) {
        clone.list.addLast(str1.clone());
        str1 = (DsSipHeaderString) str1.getNext();
      }
    }

    return clone;
  }

  /**
   * Serializes the header values to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public void write(OutputStream out) throws IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.PARAMS_WRITE);
    boolean first = true;
    if (list != null) {
      DsSipHeaderString str1 = (DsSipHeaderString) list.getFirst();
      while (null != str1) {
        if (!first) {
          out.write(DsSipConstants.B_AMPERSAND);
        } else {
          first = false;
        }
        key.write(out);
        out.write(DsSipConstants.B_EQUAL);
        str1.getValue().write(out);

        str1 = (DsSipHeaderString) str1.getNext();
      }
    }
    if (DsPerf.ON) DsPerf.stop(DsPerf.PARAMS_WRITE);
  }

  /**
   * Tells whether there are no values in this header parameter.
   *
   * @return true if empty, else return false
   */
  public boolean isEmpty() {
    if (list == null) {
      return true;
    }

    return list.size() < 1;
  }
} // Ends class DsHeaderParameter
