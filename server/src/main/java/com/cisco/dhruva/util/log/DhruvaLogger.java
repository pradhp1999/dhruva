package com.cisco.dhruva.util.log;

import org.slf4j.Logger;

public class DhruvaLogger {

  private Logger logger;

  public DhruvaLogger(Logger logger) {
    this.logger = logger;
  }

  public void error(String message, Throwable throwable) {

    logger.error(message, throwable);
  }

  public void info(String message, Object object) {

    logger.info(message, object);
  }
}
