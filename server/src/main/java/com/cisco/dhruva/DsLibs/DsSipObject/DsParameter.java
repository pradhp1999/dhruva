// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsUtil.*;
import gnu.trove.TLinkable;

/**
 * This class represents a parameter element that can be a member of DsParameters list. This class
 * behaves as a container for a parameter key and its value.
 */
public class DsParameter implements TLinkable, Cloneable {
  /** Represents the key part in key-value pair of this parameter. */
  DsByteString key;
  /** Represents the value part in key-value pair of this parameter. */
  DsByteString value;

  /**
   * Holds a reference to the next element in the container list of which this parameter would be an
   * element.
   */
  TLinkable _next;
  /**
   * Holds a reference to the previous element in the container list of which this parameter would
   * be an element.
   */
  TLinkable _prev;

  /**
   * Constructs this parameter with the specified <code>key</code> and the specified <code>value
   * </code>.
   *
   * @param key the parameter key
   * @param value the parameter value
   */
  public DsParameter(DsByteString key, DsByteString value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException("The key or value in this parameter can not be null");
    }
    this.set(key, value);
  }

  /**
   * Returns the DsParameter next to this parameter in the containing TLinkedList. This method is
   * basically required by the TLinkedList, as only then the DsByteString that implements TLinkable,
   * can be stored in the TLinkedList.
   *
   * @return the DsParameter next to this parameter in the containing TLinkedList.
   * @see #setNext(TLinkable)
   */
  public final TLinkable getNext() {
    return _next;
  }

  /**
   * Returns the DsParameter previous to this parameter in the containing TLinkedList. This method
   * is basically required by the TLinkedList, as only then the DsByteString that implements
   * TLinkable, can be stored in the TLinkedList.
   *
   * @return the DsParameter previous to this parameter in the containing TLinkedList.
   * @see #setPrevious(TLinkable)
   */
  public final TLinkable getPrevious() {
    return _prev;
  }

  /**
   * Sets the DsParameter next to this parameter. This method is basically required by the
   * TLinkedList, as only then the DsByteString that implements TLinkable, can be stored in the
   * TLinkedList. Note: It is recommended not to use this method directly as its usage may corrupt
   * the underlying linkedlist that contains this DsParameter.
   *
   * @param next the DsParameter that need to be set as the next DsParameter object in the
   *     containing TLinkedList of this DsParameter.
   * @see #getNext
   */
  public final void setNext(TLinkable next) {
    _next = next;
  }

  /**
   * Sets the DsParameter previous to this parameter. This method is basically required by the
   * TLinkedList, as only then the DsByteString that implements TLinkable, can be stored in the
   * TLinkedList. Note: It is recommended not to use this method directly as its usage may corrupt
   * the underlying linkedlist that contains this DsParameter.
   *
   * @param prev the DsParameter that need to be set as the previous DsParameter object in the
   *     containing TLinkedList of this DsParameter.
   * @see #getPrevious
   */
  public final void setPrevious(TLinkable prev) {
    _prev = prev;
  }

  /**
   * Tells whether the specified <code>key</code> is equal to this parameter's key.
   *
   * @param key the key that needs to be compared with this parameter's key
   * @return <code>true</code> if the specified <code>key</code> is equal to this parameter's key,
   *     <code>false</code> otherwise.
   */
  public boolean equals_key(DsByteString key) {
    return (this.key.equalsIgnoreCase(key));
  }

  /**
   * Tells whether the specified <code>param</code> is equal to this parameter. The two parameters
   * are equal if their keys and corresponding values are equal.
   *
   * @param param the parameter object that needs to be compared with this parameter.
   * @return <code>true</code> if the specified <code>param</code> is equal to this parameter,
   *     <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object param) {
    if (this == param) {
      return true;
    }
    if (param == null) {
      return false;
    }
    if (!key.equalsIgnoreCase(((DsParameter) param).key)
        || !value.equalsIgnoreCase(((DsParameter) param).value)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the value of this parameter.
   *
   * @return the value of this parameter.
   * @see #setValue(DsByteString)
   */
  public DsByteString getValue() {
    return value;
  }

  /**
   * Sets the value of this parameter to the specified <code>value</code>.
   *
   * @param value the new value of this parameter.
   * @see #getValue()
   */
  public void setValue(DsByteString value) {
    if (value == null) {
      value = DsByteString.BS_EMPTY_STRING;
    }

    this.value = value;
  }

  /**
   * Returns the key of this parameter.
   *
   * @return the key of this parameter.
   * @see #setKey(DsByteString)
   */
  public DsByteString getKey() {
    return key;
  }

  /**
   * Sets the key of this parameter to the specified <code>key</code>.
   *
   * @param key the new key of this parameter, must not be <code>null</code>.
   * @see #getKey()
   * @throws IllegalArgumentException if <code>key</code> is <code>null</code>.
   */
  public void setKey(DsByteString key) {
    if (key == null) {
      throw new IllegalArgumentException("The key may not be null.");
    }

    this.key = key;
  }

  /**
   * Sets the specified <code>key</code> and the <code>value</code> to this parameter. It overrides
   * the previous key and value of this parameter.
   *
   * @param key the new key of this parameter, must not be <code>null</code>.
   * @param value the new value of this parameter.
   * @throws IllegalArgumentException if <code>key</code> is <code>null</code>.
   */
  public void set(DsByteString key, DsByteString value) {
    // use the set methods for the null checks
    setKey(key);
    setValue(value);
  }

  /**
   * Returns a clone of this parameter object. The reference to next and previous elements are set
   * to nullin the returned object.
   *
   * @return a clone of this parameter object.
   */
  public Object clone() {
    DsParameter clone = null;
    try {
      clone = (DsParameter) super.clone();
    } catch (CloneNotSupportedException cne) {
    }
    if (clone != null) {
      clone._next = null;
      clone._prev = null;
    }
    return clone;
  }
} // Ends class DsParameter
