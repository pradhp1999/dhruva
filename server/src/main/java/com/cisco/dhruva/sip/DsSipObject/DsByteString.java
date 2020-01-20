package com.cisco.dhruva.sip.DsSipObject;

import java.io.Serializable;

/**
 * These objects should be treated as immutable. That is, the {@link #data()} method returns the
 * actual underlying byte[] that represents this string. That array must not be modified, as you do
 * not know who else is using it. Use {@link #toByteArray()} to get a copy of the data is a byte
 * array if you need to modify anything or {@link #copy()} before mutating.
 *
 * <p>We could have enfored this, as the java.lang.String class does, but that would have prevented
 * us from getting some of the performance that we needed out of this class.
 */
public class DsByteString implements Cloneable, Serializable {
  /** The private constructor */
  private DsByteString() {}

  public DsByteString(char[] cdata) {}

  public DsByteString(String data) {}
}
