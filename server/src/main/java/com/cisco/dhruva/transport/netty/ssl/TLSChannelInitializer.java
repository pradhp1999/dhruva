package com.cisco.dhruva.transport.netty.ssl;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.netty.decoder.SIPContentLengthBasedFrameDecoder;
import com.cisco.dhruva.transport.netty.hanlder.AbstractChannelHandler;
import com.cisco.dhruva.transport.netty.ssl.NettySSLContextFactory.SSLContextType;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import java.util.concurrent.TimeUnit;

public class TLSChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final AbstractChannelHandler channelHandler;
  private final SslContext sslcontext;
  private final DsNetwork networkConfig;

  public enum ChannelType {
    CLIENT,
    SERVER
  }

  public TLSChannelInitializer(
      DsNetwork networkConfig, AbstractChannelHandler channelHandler, ChannelType channelType)
      throws Exception {
    this.channelHandler = channelHandler;
    this.networkConfig = networkConfig;
    sslcontext =
        NettySSLContextFactory.getInstance()
            .createSslContext(
                channelType == ChannelType.SERVER ? SSLContextType.SERVER : SSLContextType.CLIENT,
                networkConfig);
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    SslHandler sslHandler = sslcontext.newHandler(ch.alloc());
    sslHandler.setHandshakeTimeout(
        networkConfig.getTlsHandshakeTimeoutMilliSeconds(), TimeUnit.MILLISECONDS);
    pipeline.addLast(sslHandler);
    pipeline.addLast(new SIPContentLengthBasedFrameDecoder());
    pipeline.addLast(channelHandler);
  }
}
