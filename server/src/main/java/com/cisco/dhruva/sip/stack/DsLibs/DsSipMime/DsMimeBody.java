// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsParameters;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import java.io.Serializable;

/**
 * Interface DsMimeBody represents a read-only implementation of MIME body. It defines getter
 * methods for common properties of a MIME body, such as Content-Type, Content-Disposition and
 * Content-Length. WARNING: Note that user code can still call getHeader() on the containing entity
 * and then change Content-* headers, which might make the entity state inconsistent with body
 * state. The behavior of the following operations is therefore unpredictable.
 */
public interface DsMimeBody extends Serializable, Cloneable {
  /**
   * Returns the containing entity.
   *
   * @return the containing entity. Null is returned if the body is not contained in any entity.
   */
  public DsMimeEntity getContainingEntity();

  /**
   * Sets the containing entity. This will also update the reference to the body in the containing
   * entity. If the entity contains a body before the method is called, the cross-reference between
   * the entity and the old body will also be updated.
   *
   * @param the containing entity.
   */
  public void setContainingEntity(DsMimeEntity entity);

  /**
   * Returns the content type of the containing entity. Returns null if the body is not contained in
   * any entity or the entity does not have content-type header.
   *
   * @return the content type of the containing entity.
   */
  public DsByteString getContainingEntityContentType();

  /**
   * Returns the content type parameter of the containing entity. Returns null if the body is not
   * contained in any entity or the entity does not have the specified parameter name in its
   * content-type header.
   *
   * @param name parameter name
   * @return the content type parameter of the containing entity.
   */
  public DsByteString getContainingEntityContentTypeParameter(DsByteString name);

  /**
   * Returns the content type parameters of the containing entity. Returns null if the body is not
   * contained in any entity or the entity does not have parameters. Note that the return value is
   * considered read-only. If user code changes the parameters to be inconsistent with actual body
   * state, then the behavior of the stack on following entity/body operations may be unpredictable.
   *
   * @return the content type parameters of the containing entity.
   */
  public DsParameters getContainingEntityContentTypeParameters();

  /**
   * Returns the content disposition of the containing entity. Returns null if the body is not
   * contained in any entity or the entity does not have content-disposition header.
   *
   * @return the content disposition of the containing entity.
   */
  public DsByteString getContainingEntityDispositionType();

  /**
   * Returns the content disposition handling of the containing entity. Returns null if the body is
   * not contained in any entity or the entity does not have handling parameter in its
   * content-disposition header.
   *
   * @return the content disposition handling of the containing entity.
   */
  public DsByteString getContainingEntityDispositionHandling();

  /**
   * Returns the content length of the containing entity. NOTE that this operation is very expensive
   * if called upon a parsed MIME body, such as DsSdpMsg or DsMimeMultipartMixed, since
   * serialization has to be done. Calling this method on an instance of DsMimeUnparsedBody is cheap
   * though.
   *
   * @return the content length of the containing entity. Returns -1 if there is anything wrong.
   *     Returns 0 if the body is empty.
   */
  public int getContainingEntityContentLength();

  /**
   * Returns the Content-Id value of the containing entity. Returns null if the body is not
   * contained in any entity or the entity does not have content-id header or the content-id value
   * is null.
   *
   * @return the value of Content-ID header
   */
  public DsByteString getContainingEntityContentId();

  /**
   * Returns the traverser for traversing body parts of the containing entity. It is recommended to
   * return a singleton object, DsMimeNullTraverser, for non-container types of MIME bodies. If the
   * concrete subclasses implement this interface by returning null, a NullPointerException will be
   * thrown during the traversal of this type of bodies.
   *
   * @return the traverser. Must not return null.
   */
  public DsMimeEntityTraverser traverser();

  /**
   * Returns the unparsed form of this body. If the body is in parsed form, this method will cause
   * serialization of the body. If the body is in unparsed form, the body itself will be returned.
   *
   * @return the unparsed form of the body.
   * @throws DsException if anything goes wrong during the serialization, for example, non-unique
   *     boundaries.
   */
  public DsMimeUnparsedBody encode() throws DsException;

  /**
   * Returns true if the body is in parsed form.
   *
   * @return true if the body is in parsed form.
   */
  public boolean isParsed();
} // End of public interface DsMimeBody
