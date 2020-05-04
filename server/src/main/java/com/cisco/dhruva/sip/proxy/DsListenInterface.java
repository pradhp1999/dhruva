package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;



/** the class is used to  describe an interface (port/protocol for now)
 * to listen on. The proxy uses it to populate Via and Record-Route
 */
public interface DsListenInterface {

  /**
   * @return port to insert into Via header
   */
  public int getPort();

  /**
   * @return protocol to insert into Via header
   */
  public int getProtocol();

  /**
   * @return the interface to insert into Via header
   */
  public DsByteString getAddress();

}

