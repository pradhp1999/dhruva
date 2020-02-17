// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;
import org.apache.logging.log4j.Logger;

/**
 * As a concrete implementation of DsMimeAbstractBody, class DsMimeUnparsedBody is the unparsed
 * representation of body parts. It is used for any part whose content type is not registered with
 * the stack. It is also used in support of lazy parsing. When messages are initially parsed, bodies
 * are represented this way, and only parsed when there is need. Unlike most other body types, this
 * one isn't bound to particular values of the Content-* headers. So it permits values for those
 * headers to be changed at will. Content-length is set implicitly by use of setBytes.
 */
public class DsMimeUnparsedBody extends DsMimeAbstractBody {
  /** Logger */
  private static final Logger logger = DsLog4j.mimeCat;

  //////////////////////
  //    CONSTRUCTORS
  //////////////////////

  /**
   * Constructor
   *
   * @param unparsedBody the unparsed body. Its content length will be calculated
   */
  public DsMimeUnparsedBody(DsByteString unparsedBody) {
    this(null, unparsedBody);
  }

  /**
   * Constructor
   *
   * @param entity the containing entity
   * @param unparsedBody the unparsed body. Its content length will be calculated
   */
  public DsMimeUnparsedBody(DsMimeEntity entity, DsByteString unparsedBody) {
    super(entity);
    setBytes(unparsedBody);
  }

  //////////////////////
  //    METHODS
  //////////////////////

  public int getContainingEntityContentLength() {
    return (m_unparsedBody == null ? 0 : m_unparsedBody.length());
  }

  /**
   * Sets Content-Type value for the containing entity in the form of type/subtype.
   *
   * @param contentType the content-type value (in form of type/subtype) to be associated with the
   *     containing entity. If null is passed in, no action will be taken.
   */
  public void setContainingEntityContentType(DsByteString contentType) {
    if (m_entity == null && contentType != null) {
      m_entity = new DsMimeEntity(this);
    }

    if (contentType != null) {
      DsSipContentTypeHeader hdr = new DsSipContentTypeHeader();
      hdr.setMediaType(contentType);
      m_entity.updateHeader(hdr);
    }
  }

  /**
   * Sets the Content-Type parameter value for the containing entity. The Content-Type header should
   * be set first.
   *
   * @param parameters the content-type parameters associated with this body
   */
  public void setContainingEntityContentTypeParameters(DsParameters parameters) {
    if (m_entity == null) return;
    if (m_entity.hasHeaders(CONTENT_TYPE)) {
      try {
        DsSipContentTypeHeader ct =
            (DsSipContentTypeHeader) m_entity.getHeaderValidate(CONTENT_TYPE);
        ct.setParameters(parameters);
      } catch (Throwable ex) {
        logger.warn(ex);
      }
    } else {
      logger.warn("Set Content-Type first, then set Content-Type parameters");
    }
  }

  /**
   * Sets the Content-Type parameter value for the containing entity. The Content-Type header should
   * be set first.
   *
   * @param name the content-type parameter name
   * @param value the content-type parameter value
   */
  public void setContainingEntityContentTypeParameter(DsByteString name, DsByteString value) {
    if (m_entity == null) return;
    if (m_entity.hasHeaders(CONTENT_TYPE)) {
      try {
        DsSipContentTypeHeader ct =
            (DsSipContentTypeHeader) m_entity.getHeaderValidate(CONTENT_TYPE);
        ct.setParameter(name, value);
      } catch (Throwable ex) {
        logger.warn(ex);
      }
    } else {
      logger.warn("Set Content-Type first, then set Content-Type parameter");
    }
  }

  /**
   * Sets the Content-Disposition type for the containing entity.
   *
   * @param disposition the content-disposition value (e.g. session, alert, inline) for the
   *     containing entity. If null is passed in, no action will be taken.
   */
  public void setContainingEntityDispositionType(DsByteString disposition) {
    if (m_entity == null && disposition != null) {
      m_entity = new DsMimeEntity(this);
    }

    if (disposition != null) {
      DsSipContentDispositionHeader hdr = new DsSipContentDispositionHeader();
      hdr.setType(disposition);
      m_entity.updateHeader(hdr);
    }
  }

  /**
   * Sets Content-Disposition handling parameter for the containing entity. The Content-Disposition
   * should be set first.
   *
   * @param handling the content-disposition handling parameter
   */
  public void setContainingEntityDispositionHandling(DsByteString handling) {
    if (m_entity == null) return;
    if (m_entity.hasHeaders(CONTENT_DISPOSITION)) {
      try {
        DsSipContentDispositionHeader cd =
            (DsSipContentDispositionHeader) m_entity.getHeaderValidate(CONTENT_DISPOSITION);
        cd.setHandling(handling);
      } catch (Throwable ex) {
        logger.warn(ex);
      }
    } else {
      logger.warn("Set Content-Disposition first, then set Content-Disposition handling parameter");
    }
  }

  /**
   * Sets the Content-Id value for the containing entity.
   *
   * @param cid the content-id value for the containing entity without angel brackets.
   */
  public void setContainingEntityContentId(DsByteString cid) {
    if (m_entity == null && cid != null) {
      m_entity = new DsMimeEntity(this);
    }
    DsSipContentIdHeader hdr = null;
    if (m_entity != null && m_entity.hasHeaders(CONTENT_ID)) {
      try {
        hdr = (DsSipContentIdHeader) m_entity.getHeaderValidate(CONTENT_ID);
      } catch (Throwable ex) {
        logger.warn(ex);
      }
    }
    if (hdr == null) hdr = new DsSipContentIdHeader();
    hdr.setContentId(cid);
    m_entity.updateHeader(hdr);
  }

  public boolean isParsed() {
    return false;
  }

  /**
   * Returns the content of the body as a DsByteString.
   *
   * @return the content of the body as a DsByteString.
   */
  public DsByteString getBytes() {
    return m_unparsedBody;
  }

  /**
   * Replaces the content of the body with a new DsByteString.
   *
   * @param bytes the content of the body in DsByteString
   */
  public void setBytes(DsByteString bytes) {
    m_unparsedBody = bytes;
  }

  public DsMimeEntityTraverser traverser() {
    return DsMimeNullTraverser.getInstance();
  }

  public DsMimeUnparsedBody encode() throws DsException {
    return this;
  }

  public String toString() {
    return (m_unparsedBody == null ? null : m_unparsedBody.toString());
  }

  /**
   * Writes the unparsed body to a stream.
   *
   * @param outStream the stream to write the unparsed body to.
   * @throws IOException if there is an error while writing to the specified output stream
   */
  public void serialize(OutputStream outStream) throws IOException {
    if (m_unparsedBody != null) {
      m_unparsedBody.write(outStream);
    }
  }

  /**
   * Writes the unparsed body to a stream. Same as serialize(OutputStream).
   *
   * @param outStream the stream to write the unparsed body to.
   * @throws IOException if there is an error while writing to the specified output stream
   */
  public void write(OutputStream outStream) throws IOException {
    serialize(outStream);
  }

  //////////////////////
  //    DATA
  //////////////////////

  /* unparsed body */
  private DsByteString m_unparsedBody;
} // End of public class DsMimeUnparsedBody
