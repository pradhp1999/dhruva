package com.cisco.dhruva.sip.hostPort;

import static org.mockito.Mockito.*;

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
import org.testng.annotations.*;

public class HostPortUtilTest {

  DsNetwork dsNetwork, externalIpEnabledNetwork;
  DsSipURL privateNetworkInfo,
      publicNetworkInfo,
      publicNetworkWithHostIPInfo,
      publicNetworkWithHostFqdnInfo,
      unrecognizedNetworkInfo;
  String localIp = "127.0.0.1";
  String hostIp = "1.1.1.1";
  String unknownIp = "1.2.3.4";
  String hostFqdn = "dhruva.sjc.webex.com";
  boolean enableHostPort = true;
  boolean disableHostPort = false;

  @BeforeClass
  void init() throws Exception {
    dsNetwork = DsNetwork.getNetwork("Default");
    externalIpEnabledNetwork = DsNetwork.getNetwork("External_IP_enabled");

    try {
      privateNetworkInfo = new DsSipURL("Default@127.0.0.1:5060;transport=udp;lr");
      publicNetworkInfo = new DsSipURL("External_IP_enabled@127.0.0.1:5061;transport=udp;lr");
      publicNetworkWithHostIPInfo =
          new DsSipURL("External_IP_enabled@1.1.1.1:5061;transport=udp;lr");
      publicNetworkWithHostFqdnInfo =
          new DsSipURL("External_IP_enabled@dhruva.sjc.webex.com:5061;transport=udp;lr");
      unrecognizedNetworkInfo = new DsSipURL("Unrecognized@1.2.3.4:5678;transport=udp;lr");
    } catch (Exception ignored) {

    }
  }

  @BeforeMethod
  private void addListenInterfaces() throws Exception {

    // Add listen interfaces in DsControllerConfig
    try {
      DsControllerConfig.addListenInterface(
          dsNetwork,
          InetAddress.getByName(localIp),
          5060,
          Transport.UDP,
          InetAddress.getByName(localIp),
          false);
    } catch (DsInconsistentConfigurationException ignored) {
    }

    try {
      DsControllerConfig.addListenInterface(
          externalIpEnabledNetwork,
          InetAddress.getByName(localIp),
          5061,
          Transport.UDP,
          InetAddress.getByName(localIp),
          true);

    } catch (DsInconsistentConfigurationException ignored) {
    }
  }

  @AfterMethod
  private void resetDhruvaProp() {
    DsNetwork.setDhruvaConfigProperties(null);
  }

  public class HostPortTestDataProvider {

    DsSipURL uri;
    DsListenInterface listenIf;
    String expectedIp, hostInfoFromProps;
    boolean isHostPortEnabled;

    public HostPortTestDataProvider(
        DsSipURL uri, String expectedIp, String hostInfoFromProps, boolean isHostPortEnabled) {
      this.uri = uri;
      this.expectedIp = expectedIp;
      this.hostInfoFromProps = hostInfoFromProps;
      this.isHostPortEnabled = isHostPortEnabled;
    }

    public HostPortTestDataProvider(
        DsListenInterface listenIf,
        String expectedIp,
        String hostInfoFromProps,
        boolean isHostPortEnabled) {
      this.listenIf = listenIf;
      this.expectedIp = expectedIp;
      this.hostInfoFromProps = hostInfoFromProps;
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
          + "Host IP/FQDN: {"
          + hostInfoFromProps
          + "}; "
          + "When HostPort feature: {"
          + isHostPortEnabled
          + "}";
    }
  }

  @DataProvider
  public Object[] getUriAndExpectedIpForLocalToHost() {

    return new HostPortTestDataProvider[][] {
      {new HostPortTestDataProvider(privateNetworkInfo, localIp, hostIp, enableHostPort)},
      {new HostPortTestDataProvider(publicNetworkInfo, hostIp, hostIp, enableHostPort)},
      {new HostPortTestDataProvider(publicNetworkInfo, hostFqdn, hostFqdn, enableHostPort)},
      {new HostPortTestDataProvider(unrecognizedNetworkInfo, unknownIp, hostIp, enableHostPort)},
      {new HostPortTestDataProvider(privateNetworkInfo, localIp, null, disableHostPort)},
      {new HostPortTestDataProvider(publicNetworkInfo, localIp, null, disableHostPort)},
      {new HostPortTestDataProvider(unrecognizedNetworkInfo, unknownIp, null, disableHostPort)}
    };
  }

  @Test(dataProvider = "getUriAndExpectedIpForLocalToHost")
  public void testLocalIpToHostInfoConversion(HostPortTestDataProvider input) {

    DhruvaSIPConfigProperties dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);

    doReturn(input.isHostPortEnabled).when(dhruvaSIPConfigProperties).isHostPortEnabled();
    doReturn(input.hostInfoFromProps).when(dhruvaSIPConfigProperties).getHostInfo();

    Assert.assertEquals(
        HostPortUtil.convertLocalIpToHostInfo(input.uri).toString(), input.expectedIp);
  }

  @DataProvider
  public Object[] getUriAndExpectedIpForHostToLocal() {

    return new HostPortTestDataProvider[][] {
      {new HostPortTestDataProvider(privateNetworkInfo, localIp, null, enableHostPort)},
      {new HostPortTestDataProvider(publicNetworkWithHostIPInfo, localIp, null, enableHostPort)},
      {new HostPortTestDataProvider(publicNetworkWithHostFqdnInfo, localIp, null, enableHostPort)},
      {new HostPortTestDataProvider(unrecognizedNetworkInfo, unknownIp, null, enableHostPort)},
      {new HostPortTestDataProvider(privateNetworkInfo, localIp, null, disableHostPort)},
      {new HostPortTestDataProvider(publicNetworkInfo, localIp, null, disableHostPort)},
      {new HostPortTestDataProvider(unrecognizedNetworkInfo, unknownIp, null, disableHostPort)}
    };
  }

  @Test(dataProvider = "getUriAndExpectedIpForHostToLocal")
  public void testHostInfoToLocalIpConversion(HostPortTestDataProvider input) {

    DhruvaSIPConfigProperties dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);

    doReturn(input.isHostPortEnabled).when(dhruvaSIPConfigProperties).isHostPortEnabled();

    Assert.assertEquals(
        HostPortUtil.reverseHostInfoToLocalIp(input.uri).toString(), input.expectedIp);
  }

  @DataProvider
  public Object[] getListenInterfaceAndExpectedIpForLocalToHost() throws UnknownHostException {
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
      {new HostPortTestDataProvider(listenIf1, localIp, hostIp, enableHostPort)},
      {new HostPortTestDataProvider(listenIf2, hostIp, hostIp, enableHostPort)},
      {new HostPortTestDataProvider(listenIf2, hostFqdn, hostFqdn, enableHostPort)},
      {new HostPortTestDataProvider(listenIf1, localIp, null, disableHostPort)},
      {new HostPortTestDataProvider(listenIf2, localIp, null, disableHostPort)},
    };
  }

  @Test(dataProvider = "getListenInterfaceAndExpectedIpForLocalToHost")
  public void testLocalIpToHostInfoUsingListenInterface(HostPortTestDataProvider input) {

    DhruvaSIPConfigProperties dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);

    doReturn(input.isHostPortEnabled).when(dhruvaSIPConfigProperties).isHostPortEnabled();
    doReturn(input.hostInfoFromProps).when(dhruvaSIPConfigProperties).getHostInfo();

    Assert.assertEquals(
        HostPortUtil.convertLocalIpToHostInfo(input.listenIf).toString(), input.expectedIp);
  }
}
