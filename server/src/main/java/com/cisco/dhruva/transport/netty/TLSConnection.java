package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.netty.hanlder.AbstractChannelHandler;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.concurrent.CompletableFuture;

public class TLSConnection extends AbstractConnection {

  Logger logger = DhruvaLoggerFactory.getLogger(TLSConnection.class);

  public TLSConnection(
      Channel channel, DsNetwork networkConfig, AbstractChannelHandler channelHandler) {
    super(channel, networkConfig, Transport.TLS, channelHandler);
  }

  /** @return transport type of this connection */
  @Override
  public Transport getConnectionType() {
    return this.transport;
  }

  @Override
  public CompletableFuture<Boolean> send(byte[] buffer) {

    ByteBuf byteBuf = channel.alloc().buffer(buffer.length);
    byteBuf.writeBytes(buffer);
    ChannelFuture channelFuture = channel.writeAndFlush(byteBuf);
    logMessage(buffer);
    return getCompletableFutureFromNettyFuture(channelFuture);
  }
}
