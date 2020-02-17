// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import java.util.*;

/**
 * Concrete class DsMimeAndMatcher (extending DsMimeBinaryMatcher) provides logical AND operation of
 * two matchers. This can be used to construct complex matchers when looking for body parts. Methods
 * in this class are NOT thread safe.
 */
public class DsMimeAndMatcher extends DsMimeBinaryMatcher {
  /**
   * Constructor.
   *
   * @param op1 operand 1
   * @param op2 operand 2
   * @throws IllegalArgumentException if either op1 or op2 is null
   */
  public DsMimeAndMatcher(DsMimeEntityMatcher op1, DsMimeEntityMatcher op2) {
    super(op1, op2);
  }

  /**
   * Returns true if the entity matches both criteria specified by op1 and op2.
   *
   * @param entity mime entity
   * @return true if the entity matches both criteria specified by op1 and op2
   */
  public boolean matches(DsMimeEntity entity) {
    return (op1.matches(entity) && op2.matches(entity));
  }

  /**
   * Returns cached content type/disposition matcher. If there is no cache, a new DsMimeAndMatcher
   * will be created and stored in the cache. Key will be internally calculated as
   * <type>::<disposition> (without angel brackets, but with double colons).
   *
   * @param type content type
   * @param disposition content disposition
   * @return cached matcher
   */
  public static DsMimeAndMatcher getTypeAndDispositionMatcher(
      DsByteString type, DsByteString disposition) {
    DsByteString key = getTypeAndDispositionKey(type, disposition);
    DsMimeAndMatcher matcher = (DsMimeAndMatcher) cacheTypeDisposition.get(key);
    if (matcher == null) {
      matcher =
          new ReadOnlyTypeAndDispositionMatcher(
              DsMimeContentTypeMatcher.getMatcher(type),
              DsMimeDispositionMatcher.getMatcher(disposition));
      cacheTypeDisposition.put(type, matcher);
    }
    return matcher;
  }

  /**
   * Returns key used for caching content type/disposition matcher. Key is calculated as
   * <type>::<disposition>
   *
   * @param type content type
   * @param disposition content disposition
   * @return key
   */
  private static DsByteString getTypeAndDispositionKey(
      DsByteString type, DsByteString disposition) {
    int tLen = (type == null ? 0 : type.length());
    int dLen = (disposition == null ? 0 : disposition.length());
    byte[] keyBytes = new byte[tLen + dLen + 2];

    if (type != null) {
      System.arraycopy(type.data(), type.offset(), keyBytes, 0, tLen);
    }

    keyBytes[tLen] = keyBytes[tLen + 1] = DsSipConstants.B_COLON;

    if (disposition != null) {
      System.arraycopy(disposition.data(), disposition.offset(), keyBytes, tLen + 2, dLen);
    }

    return new DsByteString(keyBytes);
  }

  /** stores cached content type/disposition matchers. */
  private static Hashtable cacheTypeDisposition = new Hashtable(11);

  /** Read-only matcher. */
  static class ReadOnlyTypeAndDispositionMatcher extends DsMimeAndMatcher {
    public ReadOnlyTypeAndDispositionMatcher(DsMimeEntityMatcher op1, DsMimeEntityMatcher op2) {
      super(op1, op2);
    }

    public void reset(DsMimeEntityMatcher op1, DsMimeEntityMatcher op2) {
      throw new UnsupportedOperationException("This is a read-only matcher.");
    }
  }
} // End of public class DsMimeAndMatcher
