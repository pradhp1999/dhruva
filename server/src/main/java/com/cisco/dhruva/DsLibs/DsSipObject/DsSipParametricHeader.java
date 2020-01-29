// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.util.*;

/** Base class for SIP headers that contain parameters. */
public abstract class DsSipParametricHeader extends DsSipHeader {
  /** The underlying parameters object that contains the parameters. */
  protected DsParameters m_paramTable;

  /** Default constructor. */
  protected DsSipParametricHeader() {
    super();
  }

  /*
   CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
      The origianl super() calling will eventually call down to the child and set child's private date member.

  protected DsSipParametricHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException

  protected DsSipParametricHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException

  protected DsSipParametricHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException
   */

  /**
   * Returns a deep copy of the header object and all of the other elements on the list that it is
   * associated with. NOTE: This behavior will change when the deprecated methods are removed and it
   * will just clone the single header.
   *
   * @return the cloned header object
   */
  public Object clone() {
    DsSipParametricHeader clone = (DsSipParametricHeader) super.clone();
    if (m_paramTable != null) {
      clone.m_paramTable = (DsParameters) m_paramTable.clone();
    }
    return clone;
  }

  /**
   * Copy another header's members to me.
   *
   * @param source the header to copy.
   */
  protected void copy(DsSipHeader source) {
    super.copy(source);
    try {
      DsSipParametricHeader hdr = (DsSipParametricHeader) source;
      m_paramTable = hdr.m_paramTable;
    } catch (ClassCastException cce) {
      // Nothing we would want to do here
    }
  }

  /**
   * Tells whether this header has any parameters.
   *
   * @return <code>true</code> if there are any parameters, <code>false</code> otherwise.
   */
  public boolean hasParameters() {
    return (m_paramTable != null && !m_paramTable.isEmpty());
  }

  /**
   * Returns the parameters that are present in this header.
   *
   * @return the parameters that are present in this header.
   */
  public DsParameters getParameters() {
    return m_paramTable;
  }

  /**
   * Sets the specified parameters for this header. It will override the existing parameters only if
   * the specified parameters object is not null. To remove the parameters from this header use
   * {@link #removeParameters()}.
   *
   * @param paramTable the new parameters object that need to be set for this header.
   */
  public void setParameters(DsParameters paramTable) {
    if (paramTable != m_paramTable) {
      m_paramTable = paramTable;
    }
  }

  /** Removes any existing parameters in this header. */
  public void removeParameters() {
    if (m_paramTable != null) {
      if (!m_paramTable.isEmpty()) {
        m_paramTable.clear();
      }
      m_paramTable = null;
    }
  }

  /**
   * Tells whether this header contains a parameter with the specified parameter <code>name</code>.
   *
   * @param key the name of the parameter that needs to be checked.
   * @return <code>true</code> if a parameter with the specified name is present, <code>false</code>
   *     otherwise.
   */
  public boolean hasParameter(DsByteString key) {
    return (m_paramTable != null && m_paramTable.isPresent(key));
  }

  /**
   * Returns the parameter value for the parameter with the specified <code>name</code>, if present,
   * otherwise returns <code>null</code>.
   *
   * @param name the name of the parameter that needs to be retrieved
   * @return the parameter value for the parameter with the specified <code>name</code>, if present,
   *     otherwise returns <code>null</code>.
   */
  public DsByteString getParameter(DsByteString name) {
    if (DsPerf.ON) DsPerf.start(DsPerf.HEADER_GET_PARAM);
    DsByteString value = null;
    if (m_paramTable != null && !m_paramTable.isEmpty()) {
      value = m_paramTable.get(name);
    }
    if (DsPerf.ON) DsPerf.stop(DsPerf.HEADER_GET_PARAM);
    return value;
  }

  /**
   * Returns the parameter value for the parameter with the specified <code>name</code>, if present,
   * otherwise returns <code>null</code>. Use this get method only if you do not already have a
   * DsByteString key and only have a String key.
   *
   * @param name the name of the parameter that needs to be retrieved
   * @return the parameter value for the parameter with the specified <code>name</code>, if present,
   *     otherwise returns <code>null</code>.
   */
  public DsByteString getParameter(String name) {
    if (DsPerf.ON) DsPerf.start(DsPerf.HEADER_GET_PARAM);
    DsByteString value = null;
    if (m_paramTable != null && !m_paramTable.isEmpty()) {
      value = m_paramTable.get(name);
    }
    if (DsPerf.ON) DsPerf.stop(DsPerf.HEADER_GET_PARAM);
    return value;
  }

  /**
   * Sets the specified name-value parameter in this header. It will override the existing value of
   * the parameter, if already present.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   */
  public void setParameter(DsByteString name, DsByteString value) {
    if (DsPerf.ON) DsPerf.start(DsPerf.HEADER_SET_PARAM);
    if (m_paramTable == null) {
      m_paramTable = new DsParameters(getStartsWithDelimeter());
    }
    m_paramTable.put(name, value);
    if (DsPerf.ON) DsPerf.stop(DsPerf.HEADER_SET_PARAM);
  }

  /**
   * Determines the initial value that the starts with delimeter variable in the parameter table
   * should be created with. The default is true, subclasses can override this value.
   */
  protected boolean getStartsWithDelimeter() {
    return true;
  }

  /**
   * Removes the parameter with the specified <code>name</code>, if present.
   *
   * @param name the name of the parameter that needs to be removed
   */
  public void removeParameter(DsByteString name) {
    if (DsPerf.ON) DsPerf.start(DsPerf.HEADER_REM_PARAM);
    if (m_paramTable != null) {
      m_paramTable.remove(name);
    }
    if (DsPerf.ON) DsPerf.stop(DsPerf.HEADER_REM_PARAM);
  }

  public int getParamCount() {
    return m_paramTable.size();
  }

  protected void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    writeEncodedParameters(out, md, false);
  }

  // todo need a way to indicate multiple params that do not get encoded here--- tag, tok , etc
  protected void writeEncodedParameters(
      OutputStream out, DsTokenSipMessageDictionary md, boolean excludeTokParam)
      throws IOException {

    if (m_paramTable == null) {
      return;
    }

    ListIterator paramIterator = m_paramTable.listIterator();
    while (paramIterator.hasNext()) {
      DsParameter param = (DsParameter) paramIterator.next();

      if ((excludeTokParam == true)
          && (param.getKey().equals(DsTokenSipConstants.s_TokParamName))) {
        continue;
      }

      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(param.getKey()).write(out);
      DsByteString val = param.getValue();
      if ((val != null) && (val.length() > 0)) {
        val.unquote();
        md.getEncoding(val).write(out);
      } else {
        out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
      }
    }
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    if (m_paramTable != null) {
      m_paramTable.reInit();
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("parameterFound - contextId = [" + contextId + "]");
      System.out.println(
          "parameterFound - name [offset, count] = [" + nameOffset + " ," + nameCount + "]");
      System.out.println(
          "parameterFound - name = ["
              + DsByteString.newString(buffer, nameOffset, nameCount)
              + "]");
      System.out.println(
          "parameterFound - value [offset, count] = [" + valueOffset + ", " + valueCount + "]");
      System.out.println(
          "parameterFound - value = ["
              + DsByteString.newString(buffer, valueOffset, valueCount)
              + "]");
      System.out.println();
    }
    setParameter(
        DsByteString.newLower(buffer, nameOffset, nameCount),
        new DsByteString(buffer, valueOffset, valueCount));
  }
} // Ends class DsSipParametricHeader
