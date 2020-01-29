// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipMime;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import java.util.*;

/**
 * A concrete subclass of DsMimeMultipart, class DsMimeMultipartMixed is the parsed representation
 * of a multipart/mixed body.
 */
public class DsMimeMultipartMixed extends DsMimeMultipart {
  ////////////////////////
  //      CONSTRUCTORS
  ////////////////////////

  /** Default Constructor. Equivalent to the constructor with initial capacity of 2. */
  public DsMimeMultipartMixed() {
    this(2, null, null, null);
  }

  /**
   * Constructor
   *
   * @param initialCapacity the initial capacty for the part list (default is 2)
   */
  public DsMimeMultipartMixed(int initialCapacity) {
    this(initialCapacity, null, null, null);
  }

  /**
   * Constructor
   *
   * @param initialCapacity the initial capacty for the part list (default is 2)
   * @param boundary the boundary deliminater for the multipart body
   * @param preamble the preamble for the multipart body
   * @param epilog the epilogue for the multipart body
   */
  public DsMimeMultipartMixed(
      int initialCapacity, DsByteString boundary, DsByteString preamble, DsByteString epilog) {
    super(initialCapacity, boundary, preamble, epilog);
  }

  ////////////////////////
  //      METHODS
  ////////////////////////

  public DsByteString getContainingEntityContentType() {
    return DsMimeContentManager.MIME_MT_MULTIPART_MIXED;
  }

  /**
   * Adds the specified MIME entity to this multipart mixed body.
   *
   * @param part the specified MIME entity to be added.
   * @return position of the added body part. Returns -1 if part is not added.
   */
  public int addPart(DsMimeEntity part) {
    synchronized (m_partsList) {
      return (m_partsList.add(part) ? (m_partsList.size() - 1) : -1);
    }
  }

  /**
   * Adds the specified MIME body (wrapped in a new, empty, MIME entity) to this multipart mixed.
   * This is a convenience method for the common case where a MIME body is to be added and no
   * additional MIME headers are needed. If body was contained in another entity, this method call
   * will remove this body from that entity.
   *
   * @param body the specified MIME body to be added.
   * @return position of the added body part
   */
  public int addPart(DsMimeBody body) {
    DsMimeEntity entity = new DsMimeEntity(body);
    return (addPart(entity));
  }

  /**
   * Removes the specified (by the index) body part from the multipart. Any following body parts
   * that remain move up one position.
   *
   * @param index the specified (by the index) body part from the multipart.
   * @return the removed body part as DsMimeEntity
   * @throws IndexOutOfBoundsException for invalid index
   */
  public DsMimeEntity removePart(int index) throws IndexOutOfBoundsException {
    synchronized (m_partsList) {
      return (DsMimeEntity) (m_partsList.remove(index));
    }
  }

  /**
   * Removes the first entity that matches against the specified matcher from the multipart. Any
   * following body parts that remain move up one position. If the entity is not found in the
   * multipart, no action is taken and null will be returned.
   *
   * @param matcher the specified matcher.
   * @return the removed body part as DsMimeEntity. null is returned if entity is not found.
   */
  public DsMimeEntity removePart(DsMimeEntityMatcher matcher) {
    if (matcher == null) return null;
    DsMimeEntity entity = null;
    synchronized (m_partsList) {
      Iterator iter = m_partsList.iterator();
      while (iter.hasNext()) {
        if (matcher.matches((entity = (DsMimeEntity) iter.next()))) {
          iter.remove();
          return entity;
        }
      }
    }
    return null;
  }

  /**
   * Removes the first appearance of specified entity from the multipart. Any following body parts
   * that remain move up one position. It tests equality of entity by using ==. If entity is not
   * found in the multipart/mixed, no action is taken and null will be returned.
   *
   * @param entity the specified entity from the multipart.
   * @return the removed body part as DsMimeEntity. null is returned if entity is not found.
   */
  public DsMimeEntity removePart(DsMimeEntity entity) {
    if (entity == null) return null;
    synchronized (m_partsList) {
      Iterator iter = m_partsList.iterator();
      while (iter.hasNext()) {
        if (entity == iter.next()) {
          iter.remove();
          return entity;
        }
      }
    }
    return null;
  }

  /**
   * Removes the first appearance of the entity that contains the specified body part from the
   * multipart. Any following body parts that remain move up one position. If body was originally
   * contained in another entity, then the cross referencing between the body and old entity will be
   * broken. The method does == for equality comparison on bodies. If body is not found in the
   * multipart/mixed, no action is taken and null will be returned.
   *
   * @param body the specified body from the multipart.
   * @return the removed body part as DsMimeEntity
   */
  public DsMimeEntity removePart(DsMimeBody body) {
    if (body == null) return null;
    synchronized (m_partsList) {
      Iterator iter = m_partsList.iterator();
      DsMimeEntity e = null;
      while (iter.hasNext()) {
        if (body == (e = (DsMimeEntity) iter.next()).getMimeBody()) {
          iter.remove();
          return e;
        }
      }
    }
    return null;
  }

  /**
   * Replaces the specified existing body part of this multipart mixed with the specified MIME
   * entity.
   *
   * @param index the specified (by the index) body part from the multipart. Valid index ranges from
   *     0 to n, where n is the current number of body parts.
   * @param entity the specified MIME entity
   * @throws IndexOutOfBoundsException for invalid index
   */
  public void setPart(int index, DsMimeEntity entity) throws IndexOutOfBoundsException {
    synchronized (m_partsList) {
      if (index == m_partsList.size()) {
        m_partsList.add(entity);
      } else {
        m_partsList.set(index, entity);
      }
    }
  }

  /**
   * Replaces the specified existing body part of this multipart mixed with the specified MIME body.
   * The new body is wrapped in a new, empty, MIME entity before being inserted. This is a
   * convenience method for the common case where a MIME body is to be added and no additional MIME
   * headers are needed. If body was contained in another entity, this method call will remove this
   * body from that entity.
   *
   * @param index the specified (by the index) body part from the multipart.
   * @param body the specified MIME body
   * @throws IndexOutOfBoundsException for invalid index
   */
  public void setPart(int index, DsMimeBody body) throws IndexOutOfBoundsException {
    DsMimeEntity entity = new DsMimeEntity(body);
    setPart(index, entity);
  }

  /**
   * Returns a traverser that traverses the body parts in the order of their positions.
   *
   * @return the traverser that traverses the body parts in the order of their positions.
   */
  public DsMimeEntityTraverser traverser() {
    return new MultipartMixedTraverser();
  }

  /**
   * Registers multpart/mixed content type with content manager. This method will call
   * registerContentType() method on DsMimeContentManager with appropriate arguments.
   */
  public static void registerType() {
    DsMimeContentManager.registerContentType(
        DsMimeContentManager.MIME_MT_MULTIPART_MIXED, MULTIPART_MIXED_PROPS);
  }

  /** Inner class for traversing multipart/mixed. */
  class MultipartMixedTraverser implements DsMimeEntityTraverser {
    Iterator partsIterator = m_partsList.iterator();

    public boolean hasNext() {
      return partsIterator.hasNext();
    }

    public DsMimeEntity next() {
      return (DsMimeEntity) partsIterator.next();
    }

    public void remove() {
      partsIterator.remove();
    }
  }

  ////////////////////////
  //      DATA
  ////////////////////////

  /**
   * RFC 3261: For backward-compatibility, if the Content-Disposition header field is missing, the
   * server SHOULD assume bodies of Content-Type application/sdp are the disposition "session",
   * while other content types are "render".
   */
  private static DsMimeContentProperties MULTIPART_MIXED_PROPS =
      new DsMimeContentProperties(
          DsMimeContentManager.MIME_MT_MULTIPART_MIXED,
          DsMimeContentManager.MIME_DISP_RENDER,
          true,
          DsMimeMultipartParser.getInstance(),
          DsMimeMultipartMixed.class);
} // End of public class DsMimeMultipartMixed
