package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.router.AbstractAppSession;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;

public abstract class AbstractProxyAdaptor<T extends AbstractAppSession> {
  /** logger */
  private Logger logger = DhruvaLoggerFactory.getLogger(AbstractProxyAdaptor.class);

  public AbstractProxyAdaptor() {}
}
