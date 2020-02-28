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
We intend to have a liberal parser in Dhruva, so feel free to move testcases
from the malformedMessages tests to the validMessages test if needed.

For troubleshooting, the X-Test-Info header in each message gives a description
of what the test is intending to catch.
*/

public class DsSipMessageFactoryTest {

  DsSipMessageFactory mf = DsSipDefaultMessageFactory.getInstance();

  @Test(
      dataProvider = "validMessages",
      enabled = true,
      description = "Test parsing of raw, valid sip messages")
  public void testSipMessage(byte[] buf) throws DsSipParserListenerException, DsSipParserException {
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
    System.out.println("Test message:\n" + new String(buf));
    DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
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
}
