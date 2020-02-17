/*

 FILENAME:    DsMimeAbstractBody.java


 DESCRIPTION: Abstract class DsMimeAbstractBody implements interface
              DsMimeBody, acting as the base of all content types
              except message/sipfrag

 MODULE:      DsMimeAbstractBody

 AUTHOR:      JR Yang (jryang@cisco.com)
              Michael Zhou (xmzhou@cisco.com)

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import java.io.*;

/**
 * DsMimeAbstractBody is an abstract class implementing interface DsMimeBody, used as the base class
 * of all body types, except SIP fragments. It does not define additional APIs.
 */
public abstract class DsMimeAbstractBody implements DsMimeBody, Serializable, DsSipConstants {
  //////////////////////
  //    CONSTRUCTORS
  //////////////////////

  /** Default Constructor. Containing entity will be null. */
  protected DsMimeAbstractBody() {
    this(null);
  }

  /**
   * Construct body and set containing entity.
   *
   * @param entity the containing entity
   */
  protected DsMimeAbstractBody(DsMimeEntity entity) {
    setContainingEntity(entity);
  }

  //////////////////////
  //    METHODS
  //////////////////////

  /**
   * Returns the containing entity.
   *
   * @return the containing entity. Null is returned if the body is not contained in any entity.
   */
  public synchronized DsMimeEntity getContainingEntity() {
    return m_entity;
  }

  public synchronized void setContainingEntity(DsMimeEntity entity) {
    if (entity == m_entity) return;
    if (entity == null) // deref
    {
      // m_entity is not null for sure here
      m_entity.m_body = null;
      m_entity = null;
    } else {
      // entity is not null for sure here
      if (m_entity != null) {
        m_entity.m_body = null;
      }
      if (entity.m_body != null) entity.m_body.setContainingEntity(null);
      entity.m_body = null;
      m_entity = entity;
      m_entity.m_body = this;
    }
  }

  public DsByteString getContainingEntityContentType() {
    return (m_entity == null ? null : m_entity.getContentType());
  }

  public DsByteString getContainingEntityContentTypeParameter(DsByteString name) {
    return (m_entity == null ? null : m_entity.getContentTypeParameter(name));
  }

  public DsParameters getContainingEntityContentTypeParameters() {
    return (m_entity == null ? null : m_entity.getContentTypeParameters());
  }

  public DsByteString getContainingEntityDispositionType() {
    return (m_entity == null ? null : m_entity.getDispositionType());
  }

  public DsByteString getContainingEntityDispositionHandling() {
    return (m_entity == null ? null : m_entity.getDispositionHandling());
  }

  public DsByteString getContainingEntityContentId() {
    return (m_entity == null ? null : m_entity.getContentId());
  }

  public DsMimeEntityTraverser traverser() {
    return DsMimeNullTraverser.getInstance();
  }

  //////////////////////
  //    DATA
  //////////////////////

  /** backward reference to containing entity */
  protected DsMimeEntity m_entity;
} // End of public abstract class DsMimeAbstractBody
