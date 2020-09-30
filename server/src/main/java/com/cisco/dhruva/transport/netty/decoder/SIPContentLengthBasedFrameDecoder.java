package com.cisco.dhruva.transport.netty.decoder;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.netty.hanlder.AbstractChannelHandler;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.event.Event.ErrorType;
import com.cisco.dhruva.util.log.event.Event.EventSubType;
import com.cisco.dhruva.util.log.event.Event.EventType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.HashMap;
import java.util.List;

public class SIPContentLengthBasedFrameDecoder extends ByteToMessageDecoder {

  private Logger logger = DhruvaLoggerFactory.getLogger(SIPContentLengthBasedFrameDecoder.class);

  private ByteBuf contentLengthLowerCase =
      UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes("content-length".getBytes());
  private ByteBuf contentLengthUpperCase =
      UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes("CONTENT-LENGTH".getBytes());

  @Override
  protected void decode(
      ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list)
      throws Exception {

    if (byteBuf.readableBytes() > DsNetwork.getMaximumTcpFrameSize()
        || byteBuf.readerIndex() > DsNetwork.getMaximumTcpFrameSize()) {
      handleBufferSizeExceeded(channelHandlerContext, byteBuf);
    }

    trimLeadingWhiteSpace(byteBuf);

    if (!byteBuf.isReadable()) {
      return;
    }

    byteBuf.markReaderIndex();

    int contentLength = -1;
    int returnVal;

    try {
      while (true) {
        if (skipToNextHeader(byteBuf)) {
          byte ch = byteBuf.readByte();
          if (ch == 'l' || ch == 'L') {
            returnVal = skipToContentLengthValue(byteBuf);
            if (returnVal != -1) {
              contentLength = returnVal;
            }
          } else if (ch == 'C' || ch == 'c') {
            returnVal = checkIfContentLength(byteBuf);
            if (returnVal != -1) {
              contentLength = returnVal;
            }
          } // second EOL
          else if (ch == '\r') {
            ch = byteBuf.readByte();
            if (ch == '\n') {
              break; // found empty header
            }
          } else if (ch == '\n') {
            break; // found empty header
          }
        } else {
          byteBuf.resetReaderIndex();
          return;
        }
      }
    } catch (IndexOutOfBoundsException e) {
      byteBuf.resetReaderIndex();
      return;
    }

    int readerIndex = byteBuf.readerIndex();
    byteBuf.resetReaderIndex();

    // if execution reached here , then we have found the \r\n of sip message
    // if contentLength is not found, then the message is invalid
    if (contentLength == -1) {
      throw new Exception(
          "ContentLength header not found "
              + "dump of message ="
              + readFromBufferWithoutAltering(byteBuf));
    }

    if (readerIndex + contentLength <= byteBuf.writerIndex()) {
      byte[] copiedBytes = new byte[readerIndex + contentLength];
      byteBuf.readBytes(copiedBytes);
      list.add(copiedBytes);
    }
  }

  private int checkIfContentLength(ByteBuf byteBuf) {
    byte ch;
    int i = 1;
    boolean foundContentLength = true;

    // check if next 13 chars matches for content-length
    while (i <= 13) {
      ch = byteBuf.readByte();
      if (ch == contentLengthLowerCase.getByte(i) || ch == contentLengthUpperCase.getByte(i)) {
        i++;
      } else {
        foundContentLength = false;
        break;
      }
    }
    if (foundContentLength) {
      return skipToContentLengthValue(byteBuf);
    }

    return -1;
  }

  private int skipToContentLengthValue(ByteBuf byteBuf) {
    byte ch;
    ch = byteBuf.readByte();
    if (ch == ':') {
      return getContentLengthValue(byteBuf);
    } else if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') { // end of header name
      ch = byteBuf.readByte();
      while (ch != ':') {
        ch = byteBuf.readByte();
      }
      return getContentLengthValue(byteBuf);
    }
    return -1;
  }

  private int getContentLengthValue(ByteBuf byteBuf) {
    int contentLength = -1;
    StringBuilder sb = new StringBuilder();
    byte ch = byteBuf.readByte();

    while (ch == ' ' || ch == '\r' || ch == '\n') // remove leading ws
    {
      ch = byteBuf.readByte();
    }

    while (ch >= '0' && ch <= '9') // get digits
    {
      sb.append((char) ch);
      ch = byteBuf.readByte();
    }

    try {
      contentLength = Integer.parseInt(sb.toString());
    } catch (NumberFormatException e) {
      throw new NumberFormatException(
          " Cannot convert Contact-Length header in the Incoming sipMessage to number, "
              + "message will be dropped ,dump of message = "
              + readFromBufferWithoutAltering(byteBuf));
    }

    // Un read last read char (It could be end of line)
    byteBuf.readerIndex(byteBuf.readerIndex() - 1);

    return contentLength;
  }

  private String readFromBufferWithoutAltering(ByteBuf byteBuf) {
    int readerIndex = byteBuf.readerIndex();
    byteBuf.resetReaderIndex();
    byte[] messageBytes = new byte[byteBuf.readableBytes()];
    byteBuf.readBytes(messageBytes);
    String dumpOfMessage = new String(messageBytes);
    byteBuf.readerIndex(readerIndex);
    return dumpOfMessage;
  }

  private boolean skipToNextHeader(ByteBuf byteBuf) {
    byte ch = byteBuf.readByte();
    while (ch != '\n') {
      ch = byteBuf.readByte();
    }
    return true;
  }

  private void trimLeadingWhiteSpace(ByteBuf byteBuf) {

    int trimmedIndex = byteBuf.forEachByte(ch -> ch <= ' ');

    if (trimmedIndex != -1) {
      byteBuf.readerIndex(trimmedIndex);
    }
  }

  private void handleBufferSizeExceeded(
      ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
    emitBufferSizeExceededEvent(channelHandlerContext, byteBuf);

    throw new Exception(
        "TCP buffer size exceeded Maximum allowed size of "
            + DsNetwork.getMaximumTcpFrameSize()
            + " bytes");
  }

  private void emitBufferSizeExceededEvent(
      ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
    HashMap<String, String> sizeExceededInfoMap =
        AbstractChannelHandler.buildConnectionInfoMap(channelHandlerContext, true);
    sizeExceededInfoMap.put("unReadBytesInBuffer", String.valueOf(byteBuf.readableBytes()));
    sizeExceededInfoMap.put("bytesAlreadyReadByParser", String.valueOf(byteBuf.readerIndex()));
    sizeExceededInfoMap.put("maxSizeAllowed", String.valueOf(DsNetwork.getMaximumTcpFrameSize()));

    byte[] messageByteSnapShot = new byte[100];
    byteBuf.markReaderIndex();
    byteBuf.readBytes(messageByteSnapShot);
    byteBuf.resetReaderIndex();
    sizeExceededInfoMap.put("100BytesSnapShotOfMessage", new String(messageByteSnapShot));

    logger.emitEvent(
        EventType.CONNECTION,
        EventSubType.TLSCONNECTION,
        ErrorType.BufferSizeExceeded,
        "TCP buffer size exceeded Maximum allowed size , message will be dropped",
        sizeExceededInfoMap,
        null);
  }
}
