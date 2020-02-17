/*

 FILENAME:    DsMimeContentManager.java

 DESCRIPTION: Class DsMimeContentManager is used to manage mappings of content
              type names and their corresponding properties.

 MODULE:      DsMimeContentManager

 AUTHOR:      JR Yang (jryang@cisco.com)
              Michael Zhou (xmzhou@cisco.com)

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Class DsMimeContentManager is used to manage various MIME content related properties defined by
 * the stack and user code. These properties include isContainer attribute, default disposition,
 * parser implementation, etc. NOTE: The class does not inhibit registering the same class for
 * different content types. However, the behavior might be unpredictable if application code chooses
 * to do that. One work-around is to create a subclass of intended class for different content
 * types.
 */
public class DsMimeContentManager implements Serializable, Cloneable, DsSipConstants {
  /** Logger */
  private static final Logger logger = DsLog4j.mimeCat;
  /** Logging message format for registering content types. */
  private static final String registerFmt = "Registered content type [{0}] with properties [{1}].";
  /** Logging message format for unregistering content types. */
  private static final String unregisterFmt = "Unregistered content type [{0}].";

  ////////////////////////
  //      CONSTANTS
  ////////////////////////

  /** Media type literal for "multipart/mixed" */
  public static final DsByteString MIME_MT_MULTIPART_MIXED = new DsByteString("multipart/mixed");

  /** Media type literal for "multipart/alternative" */
  public static final DsByteString MIME_MT_MULTIPART_ALTERNATIVE =
      new DsByteString("multipart/alternative");

  /** Media type literal for "application/sdp" */
  public static final DsByteString MIME_MT_APPLICATION_SDP = new DsByteString("application/sdp");

  /** Media type literal for "text/plain" */
  public static final DsByteString MIME_MT_TEXT_PLAIN = new DsByteString("text/plain");

  /** Media type literal for "message/sipfrag" */
  public static final DsByteString MIME_MT_MESSAGE_SIPFRAG = new DsByteString("message/sipfrag");

  /** Content type literal for "multipart" */
  public static final DsByteString MIME_CT_MULTIPART = new DsByteString("multipart");

  /** Content type literal for "application" */
  public static final DsByteString MIME_CT_APPLICATION = new DsByteString("application");

  /** Content type literal for "text" */
  public static final DsByteString MIME_CT_TEXT = new DsByteString("text");

  /** Content type literal for "message" */
  public static final DsByteString MIME_CT_MESSAGE = new DsByteString("message");

  /** Disposition type literal for "render" */
  public static final DsByteString MIME_DISP_RENDER = new DsByteString("render");

  /** Disposition type literal for "session" */
  public static final DsByteString MIME_DISP_SESSION = new DsByteString("session");

  ////////////////////////
  //      CONSTRUCTORS
  ////////////////////////

  /** Private constructor. */
  private DsMimeContentManager() {}

  ////////////////////////
  //      METHODS
  ////////////////////////

  /**
   * Registers content type and its associated properties. The method call has no effect if either
   * contentType or properties is null.
   *
   * @param contentType Content type.
   * @param properties content type properties
   */
  public static void registerContentType(
      DsByteString contentType, DsMimeContentProperties properties) {
    if (contentType != null && properties != null) {
      m_contentTable.put(getLowerCasedTrimmedBS(contentType), properties);
      if (logger.isEnabled(Level.INFO)) {
        logger.log(
            Level.INFO, MessageFormat.format(registerFmt, new Object[] {contentType, properties}));
      }
    }
  }

  /**
   * Unregisters content type. The method call has no effect if contentType is null.
   *
   * @param contentType Content type.
   */
  public static void unregisterContentType(DsByteString contentType) {
    if (contentType != null) {
      m_contentTable.remove(getLowerCasedTrimmedBS(contentType));
      if (logger.isEnabled(Level.INFO)) {
        logger.log(Level.INFO, MessageFormat.format(unregisterFmt, new Object[] {contentType}));
      }
    }
  }

  /**
   * Gets content type properties. Returns null if contentType is null or contentType is not
   * registered.
   *
   * @param contentType Content type.
   */
  public static DsMimeContentProperties getProperties(DsByteString contentType) {
    if (contentType == null) return null;
    return (DsMimeContentProperties) m_contentTable.get(getLowerCasedTrimmedBS(contentType));
  }

  /**
   * Gets all registered content types.
   *
   * @return set of registered content types (of type DsByteString).
   */
  public static Set getRegisteredContentTypes() {
    return m_contentTable.keySet();
  }

  /**
   * Search registered content type based on body class.
   *
   * @param clz class representing body of searched content-type.
   * @return registered content type.
   */
  public static DsByteString getRegisteredContentTypeByClass(Class clz) {
    synchronized (m_contentTable) {
      Set keys = m_contentTable.keySet();
      Iterator iter = keys.iterator();
      DsByteString type = null;
      while (iter.hasNext()) {
        type = (DsByteString) iter.next();
        if (((DsMimeContentProperties) m_contentTable.get(type)).getRepClass() == clz) {
          return type;
        }
      }
      return null;
    }
  }

  /**
   * Gets the all lower cased and trimmed byte string from s. If s is already that type of strings,
   * s itself is returned.
   *
   * @param s byte string
   * @return lower cased and trimmed byte string
   */
  static final DsByteString getLowerCasedTrimmedBS(DsByteString s) {
    // won't check null for s since it is called internally and null is already checked.
    if (s.length() <= 0) return s;
    byte[] data = s.data();
    try {
      if (Character.isWhitespace((char) data[s.offset()])
          || Character.isWhitespace((char) data[s.length() - 1])) {
        return toLowerCasedTrimmedBS(s);
      }
      for (int i = s.offset(); i < s.length(); i++) {
        if (Character.isUpperCase((char) data[i])) {
          return toLowerCasedTrimmedBS(s);
        }
      }
    } catch (Throwable ex) {
      return toLowerCasedTrimmedBS(s);
    }
    return s;
  }

  /**
   * Used by getLowerCasedTrimmedBS(). This method won't perform any checking for argument.
   *
   * @param s byte string
   * @return a new lower cased and trimmed byte string
   */
  private static final DsByteString toLowerCasedTrimmedBS(DsByteString s) {
    DsByteString t = s.toLowerCase();
    t.trim();
    return t;
  }

  ////////////////////////
  //      DATA
  ////////////////////////

  /** single instance */
  private static DsMimeContentManager instance = new DsMimeContentManager();
  /** content type properties table */
  private static Hashtable m_contentTable = new Hashtable();
} // End of public class DsMimeContentManager
