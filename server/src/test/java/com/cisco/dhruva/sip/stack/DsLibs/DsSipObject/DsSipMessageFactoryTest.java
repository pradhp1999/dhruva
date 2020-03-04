package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import org.junit.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/* Many of these tests are from [voiper](https://github.com/gremwell/voiper.git)
and contain malformed and valid SIP messages as defined in RFC 4475.

The same tests are re-run with deep parsing turned on.
Some tests have individual verification steps, so they are dealt with separately.

For troubleshooting, the X-Test-Info header in each message gives a description
of what the test is intending to catch.

The following property files contain the SIP messages:
- validMessages.properties: Different kinds of SIP requests and responses.
- malformedMessages.properties: Torture tests to confirm we throw the correct Exceptions.
- deepParsingBulk.properties: Slightly tweaked versions of validMessages.properties to test deep parsing.
- deepParsingIndividual.properties: Individual test cases with specific assertions.
*/

public class DsSipMessageFactoryTest {

  DsSipMessageFactory mf = DsSipDefaultMessageFactory.getInstance();
  Properties individualTests = loadPropFile("src/test/resources/deepParsingIndividual.properties");

  @Test(
      dataProvider = "validMessages",
      enabled = true,
      description = "Test parsing of valid sip messages")
  public void testSipMessage(byte[] buf) throws DsSipParserListenerException, DsSipParserException {
    DsSipMessage.setParseAllHeaders(false);
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
    // Input and toString() of output should be identical
    Assert.assertEquals(new String(buf), msg.toString());
  }

  @Test(
      dataProvider = "deepParsingBulk",
      enabled = true,
      description = "Test DEEP parsing of valid sip messages")
  public void testDeepParsingBulk(byte[] buf)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMessage.setParseAllHeaders(true);
    System.out.println(new String(buf));
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
    // Input and toString() of output should be identical
    Assert.assertEquals(new String(buf), msg.toString());
  }

  @Test(
      dataProvider = "malformedMessages",
      expectedExceptions = {
        DsSipParserException.class,
        DsSipMessageValidationException.class,
        DsSipVersionValidationException.class,
        DsSipKeyValidationException.class
      },
      enabled = true,
      description = "Test parsing of malformed sip messages")
  public void testMalformedSipMessage(byte[] buf)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMessage.setParseAllHeaders(false);
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
  }

  @Test
  public void testEscapedUri() throws DsSipParserListenerException, DsSipParserException {
    /* We send a Request URI like this:
    sip:user@example.com?Route=%3Csip:example.com%3E
    so we assert that a Route header like this is constructed:
    Route: <sip:example.com>
     */
    DsSipMessage.setParseAllHeaders(true);
    String testcase = individualTests.getProperty("ESC_URI");
    byte[] buf = testcase.getBytes();
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);

    Assert.assertEquals(
        "Route: <sip:example.com>\r\n", msg.getHeader(DsByteString.valueOf("Route")).toString());
  }

  @Test
  public void testWhitespaceInTo() throws DsSipParserListenerException, DsSipParserException {
    /* We send a To header like this (note the whitespace):
    To: "Watson, Thomas"  sip:t.watson@example.org
    so we assert that Dhruva converts it to this (note <> and whitespace):
    To: "Watson, Thomas" <sip:a.g.bell@example.com>
     */
    DsSipMessage.setParseAllHeaders(true);
    String testcase = individualTests.getProperty("WHITESPACE_TO");
    byte[] buf = testcase.getBytes();
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);

    Assert.assertEquals(
        "To: \"Watson, Thomas\" <sip:a.g.bell@example.com>\r\n",
        msg.getHeader(DsByteString.valueOf("To")).toString());
  }

  @Test
  public void testLeadingWhitespace() throws DsSipParserListenerException, DsSipParserException {
    /* We send a malformed From like this (no whitespace after display name):
    From: caller<sip:caller@example.com>;tag=323
    so we assert that Dhruva converts it to this:
    From: caller <sip:caller@example.com>;tag=323
     */
    DsSipMessage.setParseAllHeaders(true);
    String testcase = individualTests.getProperty("LWS_DISP");
    byte[] buf = testcase.getBytes();
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
    Assert.assertEquals(
        "From: caller <sip:caller@example.com>;tag=323\r\n",
        msg.getHeader(DsByteString.valueOf("From")).toString());
  }

  @Test
  public void testQuotedContact() throws DsSipParserListenerException, DsSipParserException {
    /* We send a quoted Contact like this
    Contact: sip:user@example.com?Route=%3Csip:sip.example.com%3E
    so we assert that Dhruva adds a Route header like this:
    Route: <sip:sip.example.com>
     */
    DsSipMessage.setParseAllHeaders(true);
    String testcase = individualTests.getProperty("REG_BAD_CT");
    byte[] buf = testcase.getBytes();
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
    Assert.assertEquals(
        "Route: <sip:sip.example.com>\r\n",
        msg.getHeader(DsByteString.valueOf("Route")).toString());
  }

  @Test
  public void testIllegalExpires() throws DsSipParserListenerException, DsSipParserException {
    /* We send illegal Expires values like these
    Expires: 10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</repeat>\r\n\
    Contact: <sip:user@host129.example.com>;expires=280297596632815\r\n\
    so we assert that Dhruva strips out the Expires header, and has a Contact like this:
    Contact: <sip:user@host129.example.com>;expires=4294967295
     */
    DsSipMessage.setParseAllHeaders(true);
    String testcase = individualTests.getProperty("SCALAR_02");
    byte[] buf = testcase.getBytes();
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
    Assert.assertNull(msg.getHeader(DsByteString.valueOf("Expires")));
    Assert.assertEquals(
        "Contact: <sip:user@host129.example.com>;expires=4294967295\r\n",
        msg.getHeader(DsByteString.valueOf("Contact")).toString());
  }

  @Test
  public void testViaCombinations() throws DsSipParserListenerException, DsSipParserException {
    /* We send comma separated Via headers like this:
    Via: SIP/2.0/UDP 192.168.1.6:5060,SIP/2.0/UDP 192.168.1.7:5060\r\n\
    so we assert that Dhruva converts them to individual Via headers
     */
    String expected =
        "Via: SIP/2.0/UDP 192.168.1.2:5060\r\n"
            + "Via: SIP/2.0/TCP 192.168.1.3:5060\r\n"
            + "Via: SIP/2.0/TLS 192.168.1.4:5061\r\n"
            + "Via: SIP/2.0/UDP [fe80::42:e6ff:fe3f:3bec]:5060\r\n"
            + "Via: SIP/2.0/UDP 192.168.1.6:5060\r\n"
            + "Via: SIP/2.0/UDP 192.168.1.7:5060\r\n"
            + "Via: SIP/2.0/UDP 192.168.1.8:5060;transport=udp;name2=\"val2\"\r\n";
    DsSipMessage.setParseAllHeaders(true);
    String testcase = individualTests.getProperty("INV_VIA_COMBINATIONS");
    byte[] buf = testcase.getBytes();
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
    String output = msg.getViaHeaders().toString();
    Assert.assertEquals(expected, output);
  }

  @Test
  public void testCoverage() throws DsSipParserListenerException, DsSipParserException {
    /* Miscellaneous assertions for Privacy and other headers
     */
    String expected1 =
        "Proxy-Authorization: Digest username=\"Alice\",realm=\"atlanta.com\",nonce=\"c60f3082ee1212b402a21831ae\",response=\"245f23415f11432b3434341c022\"\r\n";
    String expected2 = "Privacy: session,history,user,header\r\n";
    String expected3 = "Accept-Language: da\r\n";
    DsSipMessage.setParseAllHeaders(true);
    String testcase = individualTests.getProperty("INV_COVERAGE");
    byte[] buf = testcase.getBytes();
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
    String output = msg.toString();
    Assert.assertEquals(
        expected1, msg.getHeader(DsByteString.valueOf("Proxy-Authorization")).toString());
    Assert.assertEquals(expected2, msg.getHeader(DsByteString.valueOf("Privacy")).toString());
    Assert.assertEquals(
        expected3, msg.getHeader(DsByteString.valueOf("Accept-Language")).toString());
  }

  private Properties loadPropFile(String propFileName) {
    /* Reads a property file and returns its contents
     */
    Properties props = new Properties();
    try {
      FileReader f = new FileReader(propFileName);
      props.load(new FileReader(propFileName));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return props;
  }

  private Object[][] propFileToObjectArray(String propFileName) {
    /* Reads a property file that has valid or malformed sip messages
    and returns it as an Object[][] that the DataProvider can use */
    Properties props = new Properties();
    try {
      FileReader f = new FileReader(propFileName);
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
    return propFileToObjectArray("src/test/resources/validMessages.properties");
  }

  @DataProvider(name = "deepParsingBulk")
  public Object[][] deepParsingBulk() {
    return propFileToObjectArray("src/test/resources/deepParsingBulk.properties");
  }

  @DataProvider(name = "malformedMessages")
  public Object[][] malformedMessages() {
    return propFileToObjectArray("src/test/resources/malformedMessages.properties");
  }
}
