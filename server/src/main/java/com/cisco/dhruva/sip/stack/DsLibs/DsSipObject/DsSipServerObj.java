// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class is a structure that hold triplets from the SIP User-Agent and Server headers. They are
 * not necessarily related semantically, but they are grouped this way to maintain order between
 * elements.
 *
 * <p>Any of the elements in this object may be null.
 */
public class DsSipServerObj implements Cloneable {
  /** The product name. */
  public DsByteString productName;

  /** The product version. */
  public DsByteString productVersion;

  /** The comment. */
  public DsByteString comment;

  /** Constructs a DsSipServerObj object. */
  public DsSipServerObj() {}

  /**
   * Constructs a DsSipServerObj object with the specified parameters.
   *
   * @param productName The product name component of this server object.
   * @param productVersion The product version component of this server object.
   * @param comment The comment component of this server object.
   */
  public DsSipServerObj(
      DsByteString productName, DsByteString productVersion, DsByteString comment) {
    this.productName = productName;
    this.productVersion = productVersion;
    this.comment = comment;
  }

  /**
   * Check for the equality of this object as compared to the specified <code>obj</code> object.
   *
   * @param obj the other object which needs to be compared with this instance.
   * @return <code>true</code> if both the objects are equal, <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipServerObj so = null;
    try {
      so = (DsSipServerObj) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if ((productName == null || productName.length() == 0)
        && (so.productName == null || so.productName.length() == 0)) {
      // null == "" - this is ok
    } else if (productName == null || !productName.equals(so.productName)) {
      return false;
    }

    if ((productVersion == null || productVersion.length() == 0)
        && (so.productVersion == null || so.productVersion.length() == 0)) {
      // null == "" - this is ok
    } else if (productVersion == null || !productVersion.equals(so.productVersion)) {
      return false;
    }

    if ((comment == null || comment.length() == 0)
        && (so.comment == null || so.comment.length() == 0)) {
      // null == "" - this is ok
    } else if (comment == null || !comment.equals(so.comment)) {
      return false;
    }

    return true;
  }

  /**
   * Returns a clone object of this instance.
   *
   * @return a clone object of this instance.
   */
  public Object clone() {
    // shallow clone OK
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return new DsSipServerObj(productName, productVersion, comment);
    }
  }

  /**
   * Returns a string representation of the various components in this server object. It should be
   * used for debug purposes only.
   *
   * @return string representation of this server object.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(64);
    sb.append("(productName=");
    sb.append(productName);
    sb.append(", productVersion=");
    sb.append(productVersion);
    sb.append(", comment=");
    sb.append(comment);
    sb.append(')');
    return new String(sb);
  }

  /**
   * Clears all the member data and made this object reusable. The various components (sub-elements)
   * of this object can be set again.
   */
  public void reInit() {
    productName = null;
    productVersion = null;
    comment = null;
  }
}
