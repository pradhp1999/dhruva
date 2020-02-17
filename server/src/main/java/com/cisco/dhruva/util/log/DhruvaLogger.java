package com.cisco.dhruva.util.log;

public class DhruvaLogger implements Logger {

  private org.slf4j.Logger logger;

  public DhruvaLogger(org.slf4j.Logger logger) {
    this.logger = logger;
  }

  @Override
  public void error(String message, Throwable throwable) {

    logger.error(message, throwable);
  }

  @Override
  public void error(String message) {
    logger.error(message);
  }

  @Override
  public void info(String message, Object object) {
    logger.info(message, object);
  }

  @Override
  public void info(String message) {
    logger.info(message);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  @Override
  public void info(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    logger.warn(message, throwable);
  }

  @Override
  public void warn(String message, Object... arguments) {
    logger.warn(message, arguments);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    logger.error(format, arg1, arg2);
  }

  @Override
  public void error(String format, Object... arguments) {
    logger.error(format, arguments);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    logger.warn(format, arg1, arg2);
  }
}
