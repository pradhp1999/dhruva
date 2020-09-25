package com.ciscospark.dhruva.tests;

import com.ciscospark.dhruva.DhruvaIT;
import com.ciscospark.dhruva.DhruvaTestProperties;
import com.ciscospark.dhruva.TestGroups;
import com.ciscospark.dhruva.util.DhruvaSipPhone;
import com.ciscospark.dhruva.util.Token;
import java.util.Properties;
import javax.sip.ResponseEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.CallIdHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTransaction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OptionsPingIT extends DhruvaIT {

  DhruvaTestProperties testPro = new DhruvaTestProperties();
  public String testHost = testPro.getTestAddress();
  public int testUdpPort = testPro.getTestUdpPort();

  public String dhruvaHost = testPro.getDhruvaHost();
  public int dhruvaUdpPort = testPro.getDhruvaUdpPort();

  public String optionPingApi = dhruvaHost + Token.Colon + dhruvaUdpPort;

  @Test(groups = TestGroups.DhruvaIT)
  void testOptions() throws Exception {

   /* Properties properties = new Properties();

    properties.setProperty("javax.sip.STACK_NAME", "TestDhruva");

    properties.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");

    SipStack sipStack = new SipStack(SipStack.PROTOCOL_UDP, 5070, properties);

    DhruvaSipPhone phone;

    phone = new DhruvaSipPhone(sipStack, "localhost", "udp", 5070, "sip:sipptest@127.0.0.1");

    String optionsReqUri = "OPTIONS " + "sip:" + optionPingApi + ("") + " SIP/2.0\r\n\r\n";

    Request option = phone.getParent().getMessageFactory().createRequest(optionsReqUri);

    AddressFactory addressFactory = phone.getParent().getAddressFactory();
    HeaderFactory headerFactory = phone.getParent().getHeaderFactory();
    CallIdHeader callIdHeader = phone.getParent().getSipProvider().getNewCallId();

    option.addHeader(callIdHeader);
    option.addHeader(headerFactory.createCSeqHeader((long) 1, Request.OPTIONS));
    option.addHeader(headerFactory.createFromHeader(phone.getAddress(), phone.generateNewTag()));

    Address toAddress =
        addressFactory.createAddress(addressFactory.createURI("sip:service@" + optionPingApi));
    option.addHeader(headerFactory.createToHeader(toAddress, null));

    option.addHeader(headerFactory.createContactHeader(phone.getAddress()));

    option.addHeader(headerFactory.createMaxForwardsHeader(1));

    SipTransaction transaction = phone.sendRequestWithTransaction(option, false, null);

    ResponseEvent responseEvent = (ResponseEvent) phone.waitResponse(transaction, 10000);

    Assert.assertEquals(responseEvent.getResponse().getStatusCode(), 200);*/
    System.out.println("Helloooo");
    DhruvaSipPhone phone;

    phone =
        new DhruvaSipPhone(
            super.sipStackService.getSipStack(),
            testHost,
            "udp",
            testUdpPort,
            "sip:sipptest@" + testHost);

    String optionsReqUri = "OPTIONS " + Token.SipColon + optionPingApi + ("") + " SIP/2.0\r\n\r\n";

    Request option = phone.getParent().getMessageFactory().createRequest(optionsReqUri);

    AddressFactory addressFactory = phone.getParent().getAddressFactory();
    HeaderFactory headerFactory = phone.getParent().getHeaderFactory();
    CallIdHeader callIdHeader = phone.getParent().getSipProvider().getNewCallId();

    option.addHeader(callIdHeader);
    option.addHeader(headerFactory.createCSeqHeader((long) 1, Request.OPTIONS));
    option.addHeader(headerFactory.createFromHeader(phone.getAddress(), phone.generateNewTag()));

    Address toAddress =
        addressFactory.createAddress(
            addressFactory.createURI(Token.SipColon + "service@" + optionPingApi));
    option.addHeader(headerFactory.createToHeader(toAddress, null));

    option.addHeader(headerFactory.createContactHeader(phone.getAddress()));

    option.addHeader(headerFactory.createMaxForwardsHeader(1));

    SipTransaction transaction = phone.sendRequestWithTransaction(option, false, null);

    System.out.println("Public Address" + phone.getPublicAddress());
    System.out.println(
        " get transaction" + transaction.getClientTransaction().getRequest().getMethod());

    ResponseEvent responseEvent = (ResponseEvent) phone.waitResponse(transaction, 10000);

    System.out.println("Executing option ping test" + responseEvent.getResponse().getStatusCode());
    Assert.assertEquals(responseEvent.getResponse().getStatusCode(), 200);
  }
}
