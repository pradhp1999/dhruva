package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

/**
 * DsInvalidParameterException is thrown when an argument or a set of argument passed to a method
 * does not make sense in the context of the method
 */
public class DsInvalidParameterException extends DsException {

  public DsInvalidParameterException(String msg) {
    super(msg);
  }
}
