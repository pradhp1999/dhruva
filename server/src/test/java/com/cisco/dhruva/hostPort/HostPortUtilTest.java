package com.cisco.dhruva.hostPort;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.sip.DsUtil.ListenIf;
import com.cisco.dhruva.sip.controller.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.proxy.DsListenInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class HostPortUtilTest {

  DsNetwork dsNetwork;
  DsNetwork externalIpEnabledNetwork;
  DhruvaSIPConfigProperties dhruvaSIPConfigProperties;

  DsSipURL privateNetworkInfo;
  DsSipURL publicNetworkInfo;
  DsSipURL publicNetworkWithExternalIpInfo;
  DsSipURL unrecognizedNetworkInfo;
  String localIp = "127.0.0.1";
  String hostIp = "1.1.1.1";
  String unknownIp = "1.2.3.4";
  boolean enableHostPort = true;
  boolean disableHostPort = false;

  @BeforeClass
  void init() throws Exception {
    dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    dsNetwork = DsNetwork.getNetwork("Default");
    externalIpEnabledNetwork = DsNetwork.getNetwork("External_IP_enabled");
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);

    // Add listen interfaces in DsControllerConfig, causes issues in getVia while sending out the
    // packet
    try {
      privateNetworkInfo = new DsSipURL("Default@127.0.0.1:5060;transport=udp;lr");
      publicNetworkInfo = new DsSipURL("External_IP_enabled@127.0.0.1:5061;transport=udp;lr");
      publicNetworkWithExternalIpInfo =
          new DsSipURL("External_IP_enabled@1.1.1.1:5061;transport=udp;lr");
      unrecognizedNetworkInfo = new DsSipURL("Unrecognized@1.2.3.4:5678;transport=udp;lr");

      DsControllerConfig.addListenInterface(
          dsNetwork,
          InetAddress.getByName(localIp),
          5060,
          Transport.UDP,
          InetAddress.getByName(localIp),
          false);

      DsControllerConfig.addListenInterface(
          externalIpEnabledNetwork,
          InetAddress.getByName(localIp),
          5061,
          Transport.UDP,
          InetAddress.getByName(localIp),
          true);

    } catch (DsInconsistentConfigurationException ignored) {
      // In this case it was already set, there is no means to remove the key from map
    }
    when(dhruvaSIPConfigProperties.getHostIp()).thenReturn(hostIp);
  }

  public class HostPortTestDataProvider {

    DsSipURL uri;
    DsListenInterface listenIf;
    String expectedIp;
    boolean isHostPortEnabled;

    public HostPortTestDataProvider(DsSipURL uri, String expectedIp, boolean isHostPortEnabled) {
      this.uri = uri;
      this.expectedIp = expectedIp;
      this.isHostPortEnabled = isHostPortEnabled;
    }

    public HostPortTestDataProvider(
        DsListenInterface listenIf, String expectedIp, boolean isHostPortEnabled) {
      this.listenIf = listenIf;
      this.expectedIp = expectedIp;
      this.isHostPortEnabled = isHostPortEnabled;
    }

    public String toString() {
      return "SipUri: {"
          + uri
          + "}; "
          + "Listen Interface: {"
          + listenIf
          + "}; "
          + "IP expected after conversion : {"
          + expectedIp
          + "}; "
          + "When HostPort feature: {"
          + isHostPortEnabled
          + "}";
    }
  }

  @DataProvider
  public Object[] getUriAndExpectedIpForLocalToExternal() {

    return new HostPortTestDataProvider[][] {
      {new HostPortTestDataProvider(privateNetworkInfo, localIp, enableHostPort)},
      {new HostPortTestDataProvider(publicNetworkInfo, hostIp, enableHostPort)},
      {new HostPortTestDataProvider(unrecognizedNetworkInfo, unknownIp, enableHostPort)},
      {new HostPortTestDataProvider(privateNetworkInfo, localIp, disableHostPort)},
      {new HostPortTestDataProvider(publicNetworkInfo, localIp, disableHostPort)},
      {new HostPortTestDataProvider(unrecognizedNetworkInfo, unknownIp, disableHostPort)}
    };
  }

  @Test(dataProvider = "getUriAndExpectedIpForLocalToExternal")
  public void testLocalIpToExternalIpConversion(HostPortTestDataProvider input) {

    when(dhruvaSIPConfigProperties.isHostPortEnabled()).thenReturn(input.isHostPortEnabled);
    Assert.assertEquals(
        HostPortUtil.convertLocalIpToExternalIp(input.uri).toString(), input.expectedIp);
  }

  @DataProvider
  public Object[] getUriAndExpectedIpForExternalToLocal() {

    return new HostPortTestDataProvider[][] {
      {new HostPortTestDataProvider(privateNetworkInfo, localIp, enableHostPort)},
      {new HostPortTestDataProvider(publicNetworkWithExternalIpInfo, localIp, enableHostPort)},
      {new HostPortTestDataProvider(unrecognizedNetworkInfo, unknownIp, enableHostPort)},
      {new HostPortTestDataProvider(privateNetworkInfo, localIp, disableHostPort)},
      {new HostPortTestDataProvider(publicNetworkInfo, localIp, disableHostPort)},
      {new HostPortTestDataProvider(unrecognizedNetworkInfo, unknownIp, disableHostPort)}
    };
  }

  @Test(dataProvider = "getUriAndExpectedIpForExternalToLocal")
  public void testExternalIpToLocalIpConversion(HostPortTestDataProvider input) {

    when(dhruvaSIPConfigProperties.isHostPortEnabled()).thenReturn(input.isHostPortEnabled);
    Assert.assertEquals(
        HostPortUtil.reverseExternalIpToLocalIp(input.uri).toString(), input.expectedIp);
  }

  @DataProvider
  public Object[] getListenInterfaceAndExpectedIpForExternalToLocal() throws UnknownHostException {
    ListenIf listenIf1 =
        (ListenIf)
            DsControllerConfig.getCurrent()
                .getInterface(
                    InetAddress.getByName(privateNetworkInfo.getHost().toString()),
                    privateNetworkInfo.getTransportParam(),
                    privateNetworkInfo.getPort());

    ListenIf listenIf2 =
        (ListenIf)
            DsControllerConfig.getCurrent()
                .getInterface(
                    InetAddress.getByName(publicNetworkInfo.getHost().toString()),
                    publicNetworkInfo.getTransportParam(),
                    publicNetworkInfo.getPort());

    return new HostPortTestDataProvider[][] {
      {new HostPortTestDataProvider(listenIf1, localIp, enableHostPort)},
      {new HostPortTestDataProvider(listenIf2, hostIp, enableHostPort)},
      {new HostPortTestDataProvider(listenIf1, localIp, disableHostPort)},
      {new HostPortTestDataProvider(listenIf2, localIp, disableHostPort)},
    };
  }

  @Test(dataProvider = "getListenInterfaceAndExpectedIpForExternalToLocal")
  public void testExternalIpToLocalIpUsingListenInterface(HostPortTestDataProvider input) {

    when(dhruvaSIPConfigProperties.isHostPortEnabled()).thenReturn(input.isHostPortEnabled);
    Assert.assertEquals(
        HostPortUtil.convertLocalIpToExternalIp(input.listenIf).toString(), input.expectedIp);
  }
}
