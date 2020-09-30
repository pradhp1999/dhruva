package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.util.LMAUtil;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.event.Event.DIRECTION;
import java.io.IOException;
import org.apache.commons.lang3.exception.ExceptionUtils;

public abstract class DsSipAbstractSipConnection extends DsAbstractConnection
    implements DsSipConnection {

  private Logger logger = DhruvaLoggerFactory.getLogger(DsSipAbstractSipConnection.class);

  public DsSipAbstractSipConnection(Connection connection) {
    super(connection);
  }

  /**
   * Sends the specified SIP message across the network through the underlying datagram socket to
   * the desired destination. The message destination is specified in this connection's binding
   * info.
   *
   * @param message the SIP message to send across
   * @return the sent message as byte array
   * @throws IOException if there is an I/O error while sending the message
   */
  @Override
  public byte[] send(DsSipMessage message) throws IOException {
    DsLog4j.connectionCat.debug("Trying to send message to address {}", bindingInfo);

    message.setTimestamp();
    byte[] buffer = message.toByteArray();
    super.sendSync(buffer);
    message.updateBinding(bindingInfo);
    LMAUtil.emitEventAndMetrics(message, bindingInfo, DIRECTION.OUT);
    return buffer;
  }

  @Override
  public final void send(byte[] message) throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    logger.warn(
        "This method is deprecated , SIPMessage OUT event would not be generated"
            + " for this call , call will work as usual ",
        ExceptionUtils.getStackTrace(new Exception()));
    sendAsync(message);
  }
}
