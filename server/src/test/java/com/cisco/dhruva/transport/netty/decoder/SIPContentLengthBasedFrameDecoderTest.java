package com.cisco.dhruva.transport.netty.decoder;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.cisco.dhruva.util.SIPRequestBuilder;
import com.cisco.dhruva.util.SIPRequestBuilder.LineSeparator;
import com.cisco.dhruva.util.SIPRequestBuilder.RequestHeader;
import com.cisco.dhruva.util.SIPRequestBuilder.RequestMethod;
import com.cisco.dhruva.util.SIPRequestBuilder.SDPType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SIPContentLengthBasedFrameDecoderTest {

  SIPContentLengthBasedFrameDecoder sipContentLengthBasedFrameDecoder;
  ChannelHandlerContext channelHandlerContext = spy(ChannelHandlerContext.class);
  EmbeddedChannel embeddedChannel = spy(EmbeddedChannel.class);
  List<Object> outList = new ArrayList();

  @BeforeMethod
  public void setUp() {
    sipContentLengthBasedFrameDecoder = new SIPContentLengthBasedFrameDecoder();
    InetSocketAddress remoteAddress = new InetSocketAddress("10.78.98.21", 5060);
    InetSocketAddress localAddress = new InetSocketAddress("10.78.98.20", 5070);
    doReturn(localAddress).when(embeddedChannel).localAddress();
    doReturn(remoteAddress).when(embeddedChannel).remoteAddress();
    doReturn(embeddedChannel).when(channelHandlerContext).channel();
  }

  @AfterMethod
  public void cleanUp() {
    outList.clear();
  }

  @Test(description = "Test decode with a proper SIP Invite Request message with a content-length")
  public void testDecode() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withSdpType(SDPType.small)
            .build()
            .getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    // Decode should read complete message
    assertEquals(byteBuf.readerIndex(), writerIndexBeforeTest);
    // Decode should not write any data to buffer
    assertEquals(byteBuf.writerIndex(), writerIndexBeforeTest);
    // outList should be of 1 size, indicating only one message is parsed
    assertEquals(outList.size(), 1);
    // outList should be filled with whole message
    assertEquals(((byte[]) outList.get(0)).length, inviteRequest.length);

    byteBuf.release();
  }


  @Test(description = "Test decode with a proper SIP Invite Request message with a content-length,"
      + " with \n as line separator")
  public void testDecodeWithNewLineAsLineSeparator() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withSdpType(SDPType.small)
            .withLineSeparator(LineSeparator.NEWLINE)
            .build()
            .getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    // Decode should read complete message
    assertEquals(byteBuf.readerIndex(), writerIndexBeforeTest);
    // Decode should not write any data to buffer
    assertEquals(byteBuf.writerIndex(), writerIndexBeforeTest);
    // outList should be of 1 size, indicating only one message is parsed
    assertEquals(outList.size(), 1);
    // outList should be filled with whole message
    assertEquals(((byte[]) outList.get(0)).length, inviteRequest.length);

    byteBuf.release();
  }


  @Test(description = "Test decode with a proper SIP Invite Request message with a content-length,"
      + " but without any line separator , decode should return without altering the buffer")
  public void testDecodeWithoutLineSeparators() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withLineSeparator(LineSeparator.CRLF)
            .build().replace(LineSeparator.CRLF.getLineSeparator(), "").getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    // Decode should read complete message
    assertEquals(byteBuf.readerIndex(), 0);
    // Decode should not write any data to buffer
    assertEquals(byteBuf.writerIndex(), writerIndexBeforeTest);
    // outList should be of 1 size, indicating only one message is parsed
    assertEquals(outList.size(), 0);

    byteBuf.release();
  }


  @Test(description = "Test decode with a proper SIP Invite Request message with a content-length,"
      + " but without last line separator , decode should return without altering the buffer")
  public void testDecodeWithoutLastLineSeparators() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withLineSeparator(LineSeparator.CRLF)
            .build().getBytes();

    System.out.println(new String(inviteRequest));
    //remove the last line separator
    inviteRequest[inviteRequest.length - 1] = 0;
    inviteRequest[inviteRequest.length - 2] = 0;

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    // Decode should read complete message
    assertEquals(byteBuf.readerIndex(), 0);
    // Decode should not write any data to buffer
    assertEquals(byteBuf.writerIndex(), writerIndexBeforeTest);
    // outList should be of 1 size, indicating only one message is parsed
    assertEquals(outList.size(), 0);

    byteBuf.release();
  }

  @Test(
      description =
          "Test decode with a proper SIP Invite Request message with a short form of "
              + "Content-Length header i.e. L: ")
  public void testDecodeWithShortFormOfContentLengthHeader() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withSdpType(SDPType.small)
            .withShortForm(RequestHeader.ContentLength)
            .build()
            .getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    // Decode should read complete message
    assertEquals(byteBuf.readerIndex(), writerIndexBeforeTest);
    // Decode should not write any data to buffer
    assertEquals(byteBuf.writerIndex(), writerIndexBeforeTest);
    // outList should be of 1 size, indicating only one message is parsed
    assertEquals(outList.size(), 1);
    // outList should be filled with whole message
    assertEquals(((byte[]) outList.get(0)).length, inviteRequest.length);

    byteBuf.release();
  }

  @Test(
      description =
          "Test decode with a  SIP Invite Request message with a content-length"
              + " higher than the SDP size , decode method should return null outlist "
              + "and should have reader index set to zero , indicating it is expecting more data")
  public void testDecodeWithContentLengthHavingAHigherValueThanSDPSize() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withSdpType(SDPType.small)
            .withContentLength(300)
            .build()
            .getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int readerIndexBeforeTest = byteBuf.readerIndex();
    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    // Decode should reset readerIndex, indicating it is expecting more value
    assertEquals(byteBuf.readerIndex(), readerIndexBeforeTest);
    // Decode should not write any data to buffer
    assertEquals(byteBuf.writerIndex(), writerIndexBeforeTest);
    // outList should be of Zero size, indicating it is expecting more value
    assertEquals(outList.size(), 0);

    byteBuf.release();
  }

  @Test(
      description =
          "Testing the maximum allowed buffer size , decode will be looking for"
              + " content-length in the buffer , and it keeps the bytes in buffer until it finds"
              + " content-length and SIPMessage+contentlength bytes , we have a maximum buffer size"
              + " to prevent overflows , testing the feature here")
  public void testDecodeMaximumBufferSize() {

    byte[] inviteRequest = new byte[1048577];
    int byteChunkSize = 100000;

    // fill in byte buffer with some junk data
    Arrays.fill(inviteRequest, (byte) 55);

    int totalLength = inviteRequest.length;
    int sourceIndex = 0;
    int lengthToCopy;

    Exception caughtException = null;
    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer();
    try {
      // send data in chunks of byteChunkSize bytes
      while (totalLength > 0) {

        lengthToCopy = Math.min(totalLength, byteChunkSize);

        byteBuf.writeBytes(inviteRequest, sourceIndex, lengthToCopy);
        sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);
        totalLength -= lengthToCopy;
        sourceIndex += lengthToCopy;
      }
    } catch (Exception e) {
      caughtException = e;
    }

    byteBuf.release();

    assertNotNull(caughtException);
    assertTrue(
        caughtException.getMessage().contains("TCP buffer size exceeded Maximum allowed size of"));
    // reader index should not be altered
    assertEquals(byteBuf.readerIndex(), 0);
  }

  @Test(
      description =
          "Test decode with a proper SIP Invite Request message with a short form of "
              + "Content-Length header i.e. L:  and with white spaces before value and colon")
  public void testDecodeWithShortFormOfContentLengthHeaderWithWhiteSpacesBeforeValueAndColon()
      throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withSdpType(SDPType.small)
            .withHeader(RequestHeader.ContentLength, "content-length     :       209")
            .build()
            .getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    // Decode should read complete message
    assertEquals(byteBuf.readerIndex(), writerIndexBeforeTest);
    // Decode should not write any data to buffer
    assertEquals(byteBuf.writerIndex(), writerIndexBeforeTest);
    // outList should be of 1 size, indicating only one message is parsed
    assertEquals(outList.size(), 1);
    // outList should be filled with whole message
    assertEquals(((byte[]) outList.get(0)).length, inviteRequest.length);

    byteBuf.release();
  }

  @Test(
      description =
          "Test decode with a proper SIP Invite Request message with a short form of "
              + "Content-Length header i.e. L:  and with white spaces before value and colon")
  public void testDecodeWithShortFormOfContentLengthHeaderWithoutValue() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withSdpType(SDPType.small)
            .withHeader(RequestHeader.ContentLength, "content-length:")
            .build()
            .getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    NumberFormatException caughtException = null;
    try {
      sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    } catch (NumberFormatException e) {
      caughtException = e;
    }

    byteBuf.release();

    assertNotNull(caughtException);
    assertEquals(caughtException.getClass(), NumberFormatException.class);
    assertTrue(
        caughtException.getMessage().contains("For input string: \"\""));
  }


  @Test(
      description =
          "Test decode with a proper SIP Invite Request message with invalid contentLength header "
              + "Content-Length header doesn't have colon here")
  public void testDecodeWithShortFormOfContentLengthHeaderWithInvalidValue() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withHeader(RequestHeader.ContentLength, "content-length")
            .build()
            .getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    NumberFormatException caughtException = null;
    try {
      sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    } catch (NumberFormatException e) {
      caughtException = e;
    }

    byteBuf.release();

    assertNotNull(caughtException);
    assertEquals(caughtException.getClass(), NumberFormatException.class);
    assertTrue(
        caughtException.getMessage().contains("For input string: \"\""));
  }

  @Test(
      description =
          "Test decode with a proper SIP Invite Request message with invalid contentLength header "
              + "Content-Length header doesn't have colon here, has some invalid value  ")
  public void testDecodeWithShortFormOfContentLengthHeaderWithoutColonAndInvalidValue()
      throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withHeader(RequestHeader.ContentLength, "content-lengthsomeinvalid")
            .build()
            .getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    Exception caughtException = null;
    try {
      sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    } catch (Exception e) {
      caughtException = e;
    }

    byteBuf.release();

    assertNotNull(caughtException);
    assertEquals(caughtException.getClass(), Exception.class);
    assertTrue(
        caughtException.getMessage().contains("ContentLength header not found"));
  }


  @Test(
      description =
          "Test decode with a proper SIP Invite Request message with invalid contentLength header "
              + "Content-Length header has some invalid string")
  public void testDecodeWithShortFormOfContentLengthHeaderWithInvalidValue2() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();

    byte[] inviteRequest =
        sipRequestBuilder
            .withMethod(RequestMethod.INVITE)
            .withHeader(RequestHeader.ContentLength, "content-length: someinvalidcontent")
            .build()
            .getBytes();

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(inviteRequest);

    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    NumberFormatException caughtException = null;
    try {
      sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    } catch (NumberFormatException e) {
      caughtException = e;
    }

    byteBuf.release();

    assertNotNull(caughtException);
    assertEquals(caughtException.getClass(), NumberFormatException.class);
    assertTrue(
        caughtException.getMessage().contains("For input string: \"\""));
  }


  @Test(
      description =
          "Test decode with a proper SIP Invite Request message with Content-Length header"
              + " in two chunks , decode should form out message in second decode call")
  public void testDecodeWithShortFormOfContentLengthHeaderWithContentLengthHeaderGoingInTwoChunks()
      throws Exception {

    String firstblock = "INVITE sip:UserB@there.com SIP/2.0\n"
        + "Via: SIP/2.0/UDP here.com:5060\n"
        + "content-length:";

    ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes(firstblock.getBytes());

    sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    assertEquals(byteBuf.readerIndex(), 0);
    assertEquals(byteBuf.writerIndex(), firstblock.length());
    //outList should be of 0
    assertEquals(outList.size(), 0);

    String secondBlock = "0\n\n";
    byteBuf.writeBytes(secondBlock.getBytes());
    sipContentLengthBasedFrameDecoder.decode(channelHandlerContext, byteBuf, outList);

    assertEquals(byteBuf.readerIndex(), byteBuf.writerIndex());
    // outList should be of 1 size, indicating only one message is parsed
    assertEquals(outList.size(), 1);
    // outList should be filled with whole message
    assertEquals(((byte[]) outList.get(0)).length, firstblock.length() + secondBlock.length());

    byteBuf.release();

  }
}
