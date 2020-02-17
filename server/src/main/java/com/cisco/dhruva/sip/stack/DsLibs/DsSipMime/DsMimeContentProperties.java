/*

 FILENAME:    DsMimeContentProperties.java

 DESCRIPTION: Class DsMimeContentProperties is an aggregator for content
              type related properties, such as isContainer attribute,
              default disposition, parser implementation, and the
              Class representing body of a content type, etc.

 MODULE:      DsMimeContentProperties

 AUTHOR:      JR Yang (jryang@cisco.com)
              Michael Zhou (xmzhou@cisco.com)

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import java.io.*;
import java.text.*;

/**
 * Concrete class DsMimeContentProperties is an aggregator of various MIME content related
 * properties, including isContainer attribute, default disposition, parser, etc.
 */
public class DsMimeContentProperties implements Serializable, Cloneable, DsSipConstants {
  /** toString() format. */
  private static final String outputFmt =
      "Type={0}, Default Disposition={1}, isContainer={2}, Parser Class={3}, Representing Class={4}";

  ////////////////////////
  //       CONSTRUCTORS
  ////////////////////////

  /**
   * Constructor.
   *
   * @param contentType content type
   * @param defaultDisposition default content disposition
   * @param isContainer true is this is a container type
   * @param parser parser implementation
   * @param repClass class representing body of the content type
   */
  public DsMimeContentProperties(
      DsByteString contentType,
      DsByteString defaultDisposition,
      boolean isContainer,
      DsMimeBodyParser parser,
      Class repClass) {
    if (contentType == null || defaultDisposition == null || parser == null || repClass == null) {
      throw new IllegalArgumentException(
          "Null argument(s) passed into DsMimeContentProperties constructor.");
    }
    m_contentType = DsMimeContentManager.getLowerCasedTrimmedBS(contentType);
    m_defaultDisposition = DsMimeContentManager.getLowerCasedTrimmedBS(defaultDisposition);
    m_isContainer = isContainer;
    m_mimeBodyParser = parser;
    m_repClass = repClass;
  }

  ////////////////////////
  //       METHODS
  ////////////////////////

  /**
   * Returns the content type as DsByteString.
   *
   * @return the content type as DsByteString.
   */
  public DsByteString getContentType() {
    return (m_contentType);
  }

  /**
   * Returns true if the content type is a container.
   *
   * @return true if the content type is a container.
   */
  public boolean isContainer() {
    return (m_isContainer);
  }

  /**
   * Returns body parser implementation for this content type.
   *
   * @return body parser implementation for this content type.
   */
  public DsMimeBodyParser getParser() {
    return (m_mimeBodyParser);
  }

  /**
   * Returns the default content disposition as DsByteString.
   *
   * @return the default content disposition as DsByteString.
   */
  public DsByteString getDefaultDisposition() {
    return (m_defaultDisposition);
  }

  /**
   * Returns the class representing body of the content type.
   *
   * @return the class representing body of the content type.
   */
  public Class getRepClass() {
    return (m_repClass);
  }

  public String toString() {
    return MessageFormat.format(
        outputFmt,
        new Object[] {
          m_contentType.toString(),
          m_defaultDisposition.toString(),
          Boolean.valueOf(m_isContainer),
          m_mimeBodyParser.getClass().getName(),
          m_repClass.getName()
        });
  }

  ////////////////////////
  //       DATA
  ////////////////////////

  /** content type */
  private DsByteString m_contentType;
  /** default content disposition */
  private DsByteString m_defaultDisposition;
  /** is the content type representing a container */
  private boolean m_isContainer;
  /** parser implementation */
  private DsMimeBodyParser m_mimeBodyParser;
  /** class representing body of the content type */
  private Class m_repClass;
} // End of public class DsMimeContentProperties
