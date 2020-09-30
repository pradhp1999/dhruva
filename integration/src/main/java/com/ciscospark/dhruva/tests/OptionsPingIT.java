package com.ciscospark.dhruva.tests;

import com.ciscospark.dhruva.DhruvaIT;
import com.ciscospark.dhruva.DhruvaTestProperties;
import com.ciscospark.dhruva.TestGroups;
import com.ciscospark.dhruva.util.DhruvaSipPhone;
import com.ciscospark.dhruva.util.Token;
import javax.sip.ResponseEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.CallIdHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import org.cafesip.sipunit.SipTransaction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OptionsPingIT extends DhruvaIT {
  DhruvaTestProperties testPro = new DhruvaTestProperties();
  private String testHost = testPro.getTestAddress();
  private int testUdpPort = testPro.getTestUdpPort();

  private String dhruvaHost = testPro.getDhruvaHost();
  private int dhruvaUdpPort = testPro.getDhruvaUdpPort();

  private String optionsPingUrl = dhruvaHost + Token.COLON + dhruvaUdpPort;

  private int timeOutValue = 10000;

  @Test(groups = TestGroups.DhruvaIT)
  void testOptions() throws Exception {
    DhruvaSipPhone phone;

    phone =
        new DhruvaSipPhone(
            super.sipStackService.getSipStack(),
            testHost,
            "udp",
            testUdpPort,
            "sip:sipptest@" + testHost);

    String optionsReqUri =
        Request.OPTIONS + " " + Token.SIP_COLON + optionsPingUrl + " SIP/2.0\r\n\r\n";

    Request option = phone.getParent().getMessageFactory().createRequest(optionsReqUri);

    AddressFactory addressFactory = phone.getParent().getAddressFactory();
    HeaderFactory headerFactory = phone.getParent().getHeaderFactory();
    CallIdHeader callIdHeader = phone.getParent().getSipProvider().getNewCallId();

    option.addHeader(callIdHeader);
    option.addHeader(headerFactory.createCSeqHeader((long) 1, Request.OPTIONS));
    option.addHeader(headerFactory.createFromHeader(phone.getAddress(), phone.generateNewTag()));

    Address toAddress =
        addressFactory.createAddress(
            addressFactory.createURI(Token.SIP_COLON + "service@" + optionsPingUrl));
    option.addHeader(headerFactory.createToHeader(toAddress, null));

    option.addHeader(headerFactory.createContactHeader(phone.getAddress()));

    option.addHeader(headerFactory.createMaxForwardsHeader(1));

    SipTransaction transaction = phone.sendRequestWithTransaction(option, false, null);
    ResponseEvent responseEvent = (ResponseEvent) phone.waitResponse(transaction, timeOutValue);

    Assert.assertEquals(responseEvent.getResponse().getStatusCode(), 200);
  }
}
