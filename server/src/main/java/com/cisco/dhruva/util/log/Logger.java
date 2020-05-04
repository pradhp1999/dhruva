/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.util.log;

import org.slf4j.event.Level;

public interface Logger {

  public void error(String message, Throwable throwable);

  public void error(String message);

  public void info(String message, Object object);

  public void info(String message);

  public void info(String format, Object arg1, Object arg2);

  public void info(String format, Object... arguments);

  public void warn(String message, Throwable throwable);

  public void warn(String message, Object... arguments);

  public void warn(String format, Object arg1, Object arg2);

  public void error(String format, Object arg1, Object arg2);

  public void error(String format, Object... arguments);

  public void debug(String message);

  String getName();

  // Should be removed , adding for compatible now
  boolean isEnabled(Level warn);

  // Should be removed , adding for compatible now
  void log(Level level, String message);

  // Should be removed , adding for compatible now
  void log(Level level, String message, Throwable throwable);

  // Should be removed , adding for compatible now
  boolean isDebugEnabled();
  // Should be removed , adding for compatible now
  boolean isWarnEnabled();
  // Should be removed , adding for compatible now
  boolean isInfoEnabled();
}
