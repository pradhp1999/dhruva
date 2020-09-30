package com.cisco.dhruva.sip.DsPings;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransportLayer;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.List;

/** this class contains the configuration parameters and constants. */
public class DsPingsConfigParams {
  public DsPingsConfigParams() {}

  /** constant value that represents UDP protocol */
  protected static final int PROTOCOL_UDP = DsSipTransportType.UDP;

  /** constant value that represents TCP protocol */
  protected static final int PROTOCOL_TCP = DsSipTransportType.TCP;

  /** constant value that represents TLS protocol */
  protected static final int PROTOCOL_TLS = DsSipTransportType.TLS;

  /** constant value that represents custom protocol */
  protected static final int PROTOCOL_CUS = 5;

  // protected List UDPQueue = Collections.synchronizedList(new LinkedList());

  /** Sip transport layer used to get the SSL context */
  protected DsSipTransportLayer transportLayer = null;

  /** status of the pinging/listening operations */
  protected boolean PingStatus = false;

  protected HashMap pingSocketMap = new HashMap();

  protected HashMap listenSocketMap = new HashMap();

  /** Ping mechanism map (Proactive or reactive per network) */
  protected HashMap pingMechanismMap = new HashMap();

  /**
   * queue per network which contains the responses for the pings that have been sent out via UDP
   */
  protected HashMap udpQueueMap = new HashMap();

  public DatagramSocket getPingSocket(String network) {
    return ((DatagramSocket) pingSocketMap.get(network));
  }

  public void setPingSocket(String network, DatagramSocket pingSocket) {
    pingSocketMap.put(network, pingSocket);
  }

  public void removePingSocket(String network) {
    pingSocketMap.remove(network);
  }

  public DatagramSocket getListenSocket(String network) {
    return ((DatagramSocket) listenSocketMap.get(network));
  }

  public void setListenSocket(String network, DatagramSocket listenSocket) {
    listenSocketMap.put(network, listenSocket);
  }

  public void setPingMechanism(String network, int pingMechanism) {
    pingMechanismMap.put(network, new Integer(pingMechanism));
  }

  public void removePingMechanism(String network) {
    pingMechanismMap.remove(network);
  }

  public List getUDPQueue(String network) {
    return ((List) udpQueueMap.get(network));
  }

  public void setUDPQueue(String network, List udpQueue) {
    udpQueueMap.put(network, udpQueue);
  }

  public void removeUDPQueue(String network) {
    udpQueueMap.remove(network);
  }
}
