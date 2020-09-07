package com.cisco.dhruva.transport.netty.decoder;

import static org.testng.Assert.assertEquals;

import com.cisco.dhruva.util.SIPRequestBuilder;
import com.cisco.dhruva.util.SIPRequestBuilder.RequestMethod;
import com.cisco.dhruva.util.SIPRequestBuilder.SDPType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SIPContentLengthBasedFrameDecoderTest {

  SIPContentLengthBasedFrameDecoder sipContentLengthBasedFrameDecoder;
  List outList = new ArrayList();

  @BeforeMethod
  public void setUp() {
    sipContentLengthBasedFrameDecoder = new SIPContentLengthBasedFrameDecoder();
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

    int readerIndexBeforeTest = byteBuf.readerIndex();
    int writerIndexBeforeTest = byteBuf.writerIndex();

    System.out.println(new String(inviteRequest));

    sipContentLengthBasedFrameDecoder.decode(null, byteBuf, outList);

    // Decode should read complete message
    assertEquals(byteBuf.readerIndex(), writerIndexBeforeTest);
    // Decode should not write any data to buffer
    assertEquals(byteBuf.writerIndex(), writerIndexBeforeTest);
    //outList should be of 1 size, indicating only one message is parsed
    assertEquals(outList.size(), 1);
    // outList should be filled with whole message
    assertEquals(((byte[]) outList.get(0)).length, inviteRequest.length);

    byteBuf.release();
  }

  @Test(description = "Test decode with a  SIP Invite Request message with a content-length"
      + " higher than the SDP size , decode method should return null outlist "
      + "and should have reader index set to zero , indicating it is expecting more data")

  public void testDecodeWithContentLengthHavingAHgherValueThanSDPSize() throws Exception {

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

    sipContentLengthBasedFrameDecoder.decode(null, byteBuf, outList);

    //Decode should reset readerIndex, indicating it is expecting more value
    assertEquals(byteBuf.readerIndex(), readerIndexBeforeTest);
    // Decode should not write any data to buffer
    assertEquals(byteBuf.writerIndex(), writerIndexBeforeTest);
    //outList should be of Zero size, indicating it is expecting more value
    assertEquals(outList.size(), 0);

    byteBuf.release();
  }
}
