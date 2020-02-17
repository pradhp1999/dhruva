package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.util.log.Trace;
import com.cisco.dhruva.util.saevent.ConnectionSAEventBuilder;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * Defines a class for SSL Encryption and Decryption. The data to be encryted needs to be placed
 * into AppsendBuffer and DsSSLEngine places the encrypted data into netSendBuffer Encrypted data
 * present in netSendBuffer is then flushed to desired channel Similarly, Whatever comes into
 * netRecvBuffer is decrypted and placed into AppRecvBuffer
 *
 * @author akgowda
 */
public class DsSSLEngine {

  private static Trace logger = Trace.getTrace(DsSSLEngine.class.getName());
  private SocketChannel channel;
  private SSLEngine engine;
  private ByteBuffer appSendBuffer;
  private ByteBuffer netSendBuffer;
  private ByteBuffer appRecvBuffer;
  private ByteBuffer netRecvBuffer;
  private SSLEngineResult engineResult = null;
  private long handshakeTimeout = 5000;
  private boolean handshakeTimedOut = false;
  private Object outBoundLock = new Object();
  private Object inboundLock = new Object();
  private SSLException handshakeException;
  private boolean notified = false;

  final class NetByteBufferInitializer extends ThreadLocal<ByteBuffer> {
    @Override
    protected ByteBuffer initialValue() {
      if (DsTcpNBConnection.m_useDirectBuffers) {
        return ByteBuffer.allocateDirect(
            DsConfigManager.getProperty(
                DsConfigManager.PROP_NON_BLOCKING_BUFFER_SIZE,
                DsConfigManager.PROP_NON_BLOCKING_BUFFER_SIZE_DEFAULT));
      } else {
        return ByteBuffer.allocate(
            DsConfigManager.getProperty(
                DsConfigManager.PROP_NON_BLOCKING_BUFFER_SIZE,
                DsConfigManager.PROP_NON_BLOCKING_BUFFER_SIZE_DEFAULT));
      }
    }
  }

  public DsSSLEngine(SocketChannel channel, SSLEngine engine) {

    this.channel = channel;
    this.engine = engine;
    this.appSendBuffer = new NetByteBufferInitializer().get();
    this.netSendBuffer = new NetByteBufferInitializer().get();
    this.appRecvBuffer = new NetByteBufferInitializer().get();
    this.netRecvBuffer = new NetByteBufferInitializer().get();
  }

  public void setHandshakeTimeout(long milliseconds) {
    this.handshakeTimeout = milliseconds;
  }

  public ByteBuffer getAppRecvBuffer() {
    return appRecvBuffer;
  }

  public ByteBuffer getAppSendBuffer() {
    return appSendBuffer;
  }

  public ByteBuffer getNetSendBuffer() {
    return netSendBuffer;
  }

  public ByteBuffer getNetRecvBuffer() {
    return netRecvBuffer;
  }

  public HandshakeStatus getHandshakeStatus() {
    return engine.getHandshakeStatus();
  }

  private void setHandshakeException(SSLException e) {
    this.handshakeException = e;
  }

  public SSLException getHandshakeException() {
    return this.handshakeException;
  }
  /** Read from the channel via the SSLEngine into the application receive buffer.. */
  public int read() throws IOException {

    int cipherTextCount = 0;
    long startTime = System.currentTimeMillis(); // start handshake timer
    int plainTextCount = appRecvBuffer.position();
    do {

      if (engine.isInboundDone()) // check if engine is accepting inbound data
      return plainTextCount > 0 ? plainTextCount : -1;

      synchronized (inboundLock) {
        channel.read(netRecvBuffer); // reads only netRecvBuffer.remaining() bytes at max
        netRecvBuffer.flip();
        engineResult = engine.unwrap(netRecvBuffer, appRecvBuffer);
        netRecvBuffer.compact();
      }
      switch (engineResult.getStatus()) {
        case BUFFER_UNDERFLOW:
          // no data: time to read more ciphertext.

          assert (channel.isOpen());
          cipherTextCount = channel.read(netRecvBuffer);
          if (cipherTextCount == 0) return plainTextCount;
          if (cipherTextCount == -1) {

            logger.debug("DsSSLEngine():read EOF, closing inbound");

            engine
                .closeInbound(); // may throw if incoming close_notify was not received, detect ssl
            // truncation attack
            close();
            if (!notified) {
              this.notifyAll();
              notified = true;
            }
            setHandshakeException(new SSLException("Remote peer closed the connection"));
            throw getHandshakeException();
          }
          break;

        case BUFFER_OVERFLOW: // No space in Application Receive Buffer
          return 0;

        case CLOSED:
          channel.socket().shutdownInput();
          break;

        case OK:
          plainTextCount = appRecvBuffer.position();
          break;
      }
      while (processHandshakeStatus()) {
        if (logger.isDebugEnabled()) {
          logger.debug("DsSSLEngine() HS Status:" + engine.getHandshakeStatus());
        }
        if ((System.currentTimeMillis() - startTime) > handshakeTimeout) {
          handshakeTimedOut = true;
          break;
        }
      }

      if (handshakeTimedOut || (System.currentTimeMillis() - startTime) > handshakeTimeout) {
        logger.error(
            "DsSSLEngine(): handshakeTimeout for ["
                + channel.getLocalAddress()
                + " "
                + channel.getRemoteAddress()
                + "] quitting");
        close();
        throw new IOException("DsSSLEngine(): TLS NIO handshake timeout");
      }
    } while (plainTextCount == 0 || netRecvBuffer.position() > 0);

    return plainTextCount;
  }

  /** Write from the application send buffer to the channel via the SSLEngine. */
  public int write() throws IOException {
    int count = appSendBuffer.position();
    int bytesConsumed = 0;

    while (count > 0) {
      synchronized (outBoundLock) {
        appSendBuffer.flip();
        engineResult = engine.wrap(appSendBuffer, netSendBuffer);
        appSendBuffer.compact();

        switch (engineResult.getStatus()) {
          case BUFFER_UNDERFLOW:
            throw new BufferUnderflowException();
          case BUFFER_OVERFLOW:
            int writeCount = flush();
            logger.debug("DsSSLEngine() :BUFFER_OVERFLOW while writing");
            if (writeCount == 0) {
              close();
              ConnectionSAEventBuilder.logTlsSendBufferFull(
                  "SSLException",
                  channel.socket().getLocalAddress(),
                  channel.socket().getLocalPort(),
                  channel.socket().getInetAddress(),
                  channel.socket().getPort(),
                  "TLS Send buffer full");
              throw new IOException("DsSSLEngine():TLS NIO Send buffer full");
            }

            continue;
          case CLOSED:
            throw new SSLException(
                "DsSSLEngine(): invalid state for write - " + engineResult.getStatus());

          case OK:
            bytesConsumed = engineResult.bytesConsumed();
            count -= bytesConsumed;
            flush();
            break;
        }
      }
      // is this needed, since handshake will be completed if write() is called.
      /*while (processHandshakeStatus()) {
      	if(logger.isDebugEnabled()) {
      		logger.debug("DsSSLEngine() HS Status:"+engine.getHandshakeStatus());
      	}
      	if((System.currentTimeMillis()-startTime)>handshakeTimeout) {
      		handshakeTimedOut =true;
      	}
      		break;
      }

      if (handshakeTimedOut || (System.currentTimeMillis()-startTime)>handshakeTimeout) {
      	logger.error("DsSSLEngine(): handshakeTimeout for ["+channel.getLocalAddress()+" "+channel.getRemoteAddress()+"] quitting");
      	close();
      	break;
      }
      */

    }
    return count;
  }

  public int flush() throws IOException {
    synchronized (outBoundLock) {
      netSendBuffer.flip();
      int count = channel.write(netSendBuffer);
      netSendBuffer.compact();
      return count;
    }
  }

  public void close() throws IOException {

    // check if there is any data in netRecvbuffer
    if (!engine.isInboundDone()) read();
    // check if there is any data in netSendBuffer
    while (netSendBuffer.position() > 0) {
      int count = flush();
      if (count == 0) {
        logger.error(
            "DsSSLEngine() :Can't flush remaining "
                + netSendBuffer.remaining()
                + " bytes while closing the channel");
        break;
      }
    }
    // Sending Close_Notify
    /**
     * engine.closeOutbound(); while (processHandshakeStatus()) { if((System.currentTimeMillis()-
     * startTime) > handshakeTimeout) break; }
     *
     * <p>if (netSendBuffer.position() > 0 && flush() == 0) logger.error("Can't flush remaining " +
     * netSendBuffer.position() + " bytes");
     */
    if (channel != null) {
      channel.close();
    }
  }

  boolean processHandshakeStatus() throws IOException {
    int count;
    switch (engine.getHandshakeStatus()) {
      case NOT_HANDSHAKING: // not presently handshaking => session is available

      case FINISHED: // just finished handshaking, SSLSession is available
        if (!notified) {
          this.notifyAll(); // notify handshake init thread if handshake is completed
          notified = true;
        }
        return false;

      case NEED_TASK:
        runDelegatedTasks();
        return true; // keep going

      case NEED_WRAP:
        synchronized (outBoundLock) {
          appSendBuffer.flip();
          try {
            engineResult =
                engine.wrap(
                    appSendBuffer,
                    netSendBuffer); // notify if exception occurs, to handshake init thread
          } catch (SSLException sslex) {
            setHandshakeException(sslex);
            if (!notified) {
              this.notifyAll();
            }
            throw sslex;
          }
          appSendBuffer.compact();
          if (engineResult.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW)
            return flush() > 0;
          if (engineResult.getStatus() == SSLEngineResult.Status.CLOSED) return false;
          if (engineResult.getStatus() == SSLEngineResult.Status.OK) flush();
        }
        return true;

      case NEED_UNWRAP:
        synchronized (inboundLock) {
          netRecvBuffer.flip();
          try {
            engineResult =
                engine.unwrap(
                    netRecvBuffer,
                    appRecvBuffer); // notify if exception occurs, to handshake init thread
          } catch (SSLException sslex) {
            setHandshakeException(sslex);
            if (!notified) {
              this.notifyAll();
            }
            throw sslex;
          }
          netRecvBuffer.compact();
        }

        if (engineResult.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
          if (engine.isInboundDone()) count = -1;
          else {
            assert (channel.isOpen());
            count = channel.read(netRecvBuffer);
          }
          return count > 0;
        }
        if ((engineResult.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW)
            || (engineResult.getStatus() == SSLEngineResult.Status.CLOSED)) return false;

        return true;
    }
    return false;
  }

  protected void runDelegatedTasks() {
    /*delegatedTaskWorkers.execute(()-> {
    		Runnable task;
    		if(logger.isDebugEnabled()) {
    			logger.debug("runDelegatedTask started");

    		}
    		while ((task = engine.getDelegatedTask()) != null) {
    			task.run();
    		}
    		if(logger.isDebugEnabled()) {
    			logger.debug("runDelegatedTask finished");

    		}
    });*/
    Runnable task;
    while ((task = engine.getDelegatedTask()) != null) {
      task.run();
    }
  }

  public SSLSession getSSLSession() {
    return engine.getSession();
  }

  public SSLEngine getEngineCore() {
    return engine;
  }
}
