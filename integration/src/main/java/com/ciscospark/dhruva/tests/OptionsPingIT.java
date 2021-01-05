package com.ciscospark.dhruva.tests;

import com.ciscospark.dhruva.DhruvaIT;
import com.ciscospark.dhruva.DhruvaTestProperties;
import com.ciscospark.dhruva.TestGroups;
import com.ciscospark.dhruva.util.DhruvaSipPhone;
import com.ciscospark.dhruva.util.Token;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
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
  private int testTlsPort = testPro.getTestTlsPort();

  private String dhruvaHost = testPro.getDhruvaHost();
  private int dhruvaUdpPort = testPro.getDhruvaUdpPort();
  private int dhruvaTlsPort = testPro.getDhruvaTlsPort();
  private String optionsPingUrlUdp = dhruvaHost + Token.COLON + dhruvaUdpPort;
  private String optionsPingUrlTls = dhruvaHost + Token.COLON + dhruvaTlsPort;
  private int timeOutValue = 10000;

  int sendOptions(DhruvaSipPhone phone, String optionsReqUri)
      throws ParseException, InvalidArgumentException {

    Request option = phone.getParent().getMessageFactory().createRequest(optionsReqUri);

    AddressFactory addressFactory = phone.getParent().getAddressFactory();
    HeaderFactory headerFactory = phone.getParent().getHeaderFactory();
    CallIdHeader callIdHeader = phone.getParent().getSipProvider().getNewCallId();

    option.addHeader(callIdHeader);
    option.addHeader(headerFactory.createCSeqHeader((long) 1, Request.OPTIONS));
    option.addHeader(headerFactory.createFromHeader(phone.getAddress(), phone.generateNewTag()));

    Address toAddress =
        addressFactory.createAddress(
            addressFactory.createURI(Token.SIP_COLON + "service@" + optionsReqUri));
    option.addHeader(headerFactory.createToHeader(toAddress, null));

    option.addHeader(headerFactory.createContactHeader(phone.getAddress()));

    option.addHeader(headerFactory.createMaxForwardsHeader(1));

    SipTransaction transaction = phone.sendRequestWithTransaction(option, false, null);
    ResponseEvent responseEvent = (ResponseEvent) phone.waitResponse(transaction, timeOutValue);

    return responseEvent.getResponse().getStatusCode();
  }

  @Test(groups = TestGroups.DhruvaIT)
  void testOptionsPingTLS() throws Exception {
    DhruvaSipPhone phone;

    phone =
        new DhruvaSipPhone(
            super.sipStackService.getSipStackTls(),
            testHost,
            Token.TLS,
            testTlsPort,
            "sip:sipptest@" + testHost);

    String optionsReqUri =
        Request.OPTIONS + " " + Token.SIP_COLON + optionsPingUrlTls + " SIP/2.0\r\n\r\n";

    Assert.assertEquals(sendOptions(phone, optionsReqUri), 200);
  }

  @Test(groups = TestGroups.DhruvaIT)
  void testOptionsPingUdp() throws Exception {
    DhruvaSipPhone phone;

    phone =
        new DhruvaSipPhone(
            super.sipStackService.getSipStackUdp(),
            testHost,
            Token.UDP,
            testUdpPort,
            "sip:sipptest@" + testHost);

    String optionsReqUri =
        Request.OPTIONS + " " + Token.SIP_COLON + optionsPingUrlUdp + " SIP/2.0\r\n\r\n";

    Assert.assertEquals(sendOptions(phone, optionsReqUri), 200);
  }
}
