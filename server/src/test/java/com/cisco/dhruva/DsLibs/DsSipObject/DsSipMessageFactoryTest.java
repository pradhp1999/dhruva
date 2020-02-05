package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserListenerException;
import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/* Many of these tests are from [voiper](https://github.com/gremwell/voiper.git)
and contain malformed and valid SIP messages as defined in RFC 4475.
We intend to have a liberal parser in Dhruva, so feel free to move testcases
from the malformedMessages tests to the validMessages test if needed.
*/

public class DsSipMessageFactoryTest {

  DsSipMessageFactory mf = DsSipDefaultMessageFactory.getInstance();

  @Test(
      dataProvider = "validMessages",
      enabled = true,
      description = "Test parsing of raw, valid sip messages")
  public void testSipMessage(byte[] buf) {
    try {
      System.out.println("test SIp Message" + new String((byte[]) buf));
      DsSipMessage.setParseAllHeaders(true);
      DsSipMessage msg = mf.createMessage(buf, 0, buf.length, false, true);
    } catch (Exception e) {
      if (e instanceof DsSipParserException) {
        Assert.fail("Exception thrown in parser" + e.getCause().getMessage(), e.getCause());
      }
      Assert.fail("Exception thrown in parse" + e.getMessage(), e);
      e.printStackTrace();
    }
  }

  @Test(
      dataProvider = "malformedMessages",
      expectedExceptions = DsSipParserException.class,
      enabled = true,
      description = "Test parsing of malformed sip messages")
  public void testMalformedSipMessage(byte[] buf)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMessage msg = mf.createMessage(buf);
  }

  private Object[][] readPropFile(String propFileName) {
    /* Reads a property file that has valid or malformed sip messages
    and returns it as an Object[][] that the DataProvider can use */
    Properties props = new Properties();
    try {
      FileReader f = new FileReader(propFileName);
      System.out.println(f);
      props.load(new FileReader(propFileName));

    } catch (IOException e) {
      e.printStackTrace();
    }
    Object[][] result = new Object[props.size()][1];
    Enumeration e = props.propertyNames();
    int index = 0;
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      result[index][0] = props.getProperty(key).getBytes();
      index += 1;
    }

    return result;
  }

  @DataProvider(name = "validMessages")
  public Object[][] validMessages() {
    return readPropFile("src/test/resources/validMessages.properties");
  }

  @DataProvider(name = "malformedMessages")
  public Object[][] malformedMessages() {
    return readPropFile("src/test/resources/malformedMessages.properties");
  }

  @DataProvider(name = "sipMessage")
  public Object[][] sipMessage() {
    return new Object[][] {
      {
        ("INVITE sip:1234567@alpha.webex.com:5060 SIP/2.0\n"
                + "Via: SIP/2.0/TLS 10.78.98.22:5060;branch=z9hG4bK22OUz2jVIUJvqqCzTbFXXA~~13\n"
                + "Via: SIP/2.0/TLS 10.78.98.22:7777;branch=z9hG4bK-25058-1-0\n"
                + "Max-Forwards: 69\n"
                + "Route: <sip:10.78.98.22:5081;transport=tls;lr>\n"
                + "Record-Route: <sip:rr$n=sip1@10.78.98.22:5060;transport=tls;lr>\n"
                + "To: service <sip:service@10.78.98.22:5060>\n"
                + "From: sipp <sip:sipp@10.78.98.22:7777>;tag=25058SIPpTag001\n"
                + "Contact: sip:sipp@10.78.98.22:7777\n"
                + "Call-ID: 1-25058@10.78.98.22\n"
                + "CSeq: 1 INVITE\n"
                + "Subject: Performance Test\n"
                + "Content-Type: application/sdp\n"
                + "Content-Length: 50\n"
                + "Session-ID: 9a1e37dd952331e29a1a4c6c940528fd;remote=00000000000000000000000000000000\n"
                + "\n"
                + "v=0\n"
                + "o=user1 53655765 2353687637 IN IP4 10.78.98.22\n"
                + "s=-\n"
                + "c=IN IP4 10.78.98.22\n"
                + "t=0 0\n"
                + "m=audio 6001 RTP/AVP 0\n"
                + "a=rtpmap:0 PCMU/8000\n"
                + "")
            .getBytes()
      },
    };
  }
}
