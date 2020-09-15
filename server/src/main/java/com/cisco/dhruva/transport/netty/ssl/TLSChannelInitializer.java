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

public class TLSChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final AbstractChannelHandler channelHandler;
  private final SslContext sslcontext;

  public TLSChannelInitializer(
      DsNetwork networkConfig, AbstractChannelHandler channelHandler, SSLContextType sslContextType)
      throws Exception {
    // super(networkConfig);
    this.channelHandler = channelHandler;
    sslcontext =
        NettySSLContextFactory.getInstance().createSslContext(sslContextType, networkConfig);
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    SslHandler sslHandler = sslcontext.newHandler(ch.alloc());
    pipeline.addLast(sslHandler);
    pipeline.addLast(new SIPContentLengthBasedFrameDecoder());
    pipeline.addLast(channelHandler);
  }
}
