// Copyright (c) 2005-2012 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipAckMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipAllowHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipCancelMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipDialogID;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderList;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipKeyValidationException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMaxForwardsHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessageValidationException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipPRACKMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponseCode;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRouteFixInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransactionKey;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipVersionValidationException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipViaHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsURI;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBuckets;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface.SipMsgNormalizationState;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageStatistics;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsPerf;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsQueueInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsString;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsThrottle;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTimer;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTlsUtil;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsUnitOfWork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsWorkQueue;
import com.cisco.dhruva.util.log.Trace;
import com.cisco.dhruva.util.saevent.DiscardSAEventBuilder;
import com.cisco.dhruva.util.saevent.SAEventConstants;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * A single instance of this class manages all the transactions for a given lower layer. This class
 * is modelled after the Singleton design pattern to insure only one instance exists in the
 * application. The transaction manager takes a transport layer as input which allows it to receive
 * messages that arrive on the network. The transaction manager is responsible for mapping those
 * messages to the transaction they are associated with. If a SIP request arrives that does not
 * correspond to an existing transaction, the transaction manager creates a server transaction and
 * passes it to the SIP method specific interface if it has been set with the setRequestInterface
 * method function. If none has been set the request interface supplied in the constructor is
 * invoked. If none was present in the constructor, the transaction manager automatically responds
 * to the request with a 405 Method Not Allowed.
 *
 * @author dynamicsoft, Inc.
 */
public class DsSipTransactionManager {
  private static final boolean IS_NON_BLOCKING_TCP =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NON_BLOCKING_TCP, DsConfigManager.PROP_NON_BLOCKING_TCP_DEFAULT);

  private static final boolean IS_NON_BLOCKING_TLS =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NON_BLOCKING_TLS, DsConfigManager.PROP_NON_BLOCKING_TLS_DEFAULT);

  private static final String FINAL = "FINAL";

  /** How to create transactions and server transaction interfaces. */
  private static DsSipTransactionFactory m_transactionFactory =
      new DsSipDefaultTransactionFactory();

  /** Request logging category. */
  private static Logger reqCat = DsLog4j.transMReqCat;
  /** CANCEL logging category. */
  private static Logger cancelCat = DsLog4j.transMCancelCat;
  // CAFFEINE 2.0 DEVELOPMENT - PRACK required
  /** PRACK logging category. */
  private static Logger prackCat = DsLog4j.transMPrackCat;
  /** ACK logging category. */
  private static Logger ackCat = DsLog4j.transMAckCat;
  /** Response logging category. */
  private static Logger respCat = DsLog4j.transMRespCat;
  /** General logging category. */
  private static Logger generalCat = DsLog4j.transMCat;

  /** Local port unspecified. */
  private static final int LPU = DsBindingInfo.LOCAL_PORT_UNSPECIFIED;
  /** Remote port unspecified. */
  private static final int RPU = DsBindingInfo.REMOTE_PORT_UNSPECIFIED;
  /** Binding transport unspecified. */
  private static final int BTU = DsBindingInfo.BINDING_TRANSPORT_UNSPECIFIED;

  private static DsBuckets buckets;
  private static boolean flowOK = true;
  private static final String INCOMING_MESSAGE_THROTTLE = "Incoming_Message_Throttle";
  private static final Object aLock = new Object(); // use as a lock
  private static HashMap m_dialogMap = new HashMap(16, .60f);

  private static final boolean shouldDecrementMaxForwards;
  private static final Trace SaEventLog = Trace.getTrace("com.cisco.CloudProxy.SAEvent");

  private static final String propFile = getPropertiesFile();

  private static long eventSequenceNumber = 0;

  private static volatile OperationalState operationalState;

  /** <code>true</code> if emulating RFC 2543 responses. */
  static final boolean m_emulate2543Responses =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_EMULATE_RFC2543_RESPONSES,
          DsConfigManager.PROP_EMULATE_RFC2543_RESPONSES_DEFAULT);

  /*
   * CSCtz70393 Thread pool exhausted when all elements in srv group is down
   */
  private static boolean m_useDsUnreachableTable =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_USE_DSUNREACHABLE_DESTINATION_TABLE,
          DsConfigManager.PROP_USE_DSUNREACHABLE_DESTINATION_TABLE_DEFAULT);

  private static boolean isCreateCAEvents =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_CREATE_CA_EVENTS, DsConfigManager.PROP_CREATE_CA_EVENTS_DEFAULT);

  private static String getPropertiesFile() {
    String filename = null;
    FileInputStream istream = null;
    try {
      ClassLoader loader = DsSipTransactionManager.class.getClassLoader();
      if (loader != null) {
        URL url = loader.getResource("dsua.properties");
        if (url != null) {
          filename = url.getFile();
          istream = new FileInputStream(filename);
          System.getProperties().load(istream);
          // System.getProperties().list(System.out);
        }
      }
    } catch (Exception exc) {
      filename = null;
    } finally {
      try {
        if (istream != null) {
          istream.close();
        }
      } catch (IOException e) {
        // ignore
      }
    }

    return filename;
  }

  static {
    // Log the stack version as a WARNING since we almost always want this to print.
    // Moved from the static block in DsLog4j, since we want the ability to set logging level
    // before the logging of this statement takes place. -stemayer
    // REFACTOR
    //        if (generalCat.isEnabled(Level.WARN))
    //        {
    //            generalCat.warn(Version.getLongVersionAsString());
    //        }

    m_transactionFactory = new DsSipDefaultTransactionFactory();

    // Loads the DsTimer class which in turn will instantiate and register the
    // Timer queue. Also loads the transaction factory.
    try {
      Class.forName(DsTimer.class.getName());
    } catch (ClassNotFoundException cnfe) {
      // nothing to do
    }

    // if JAIN is set, then do not decrement Max-Forwards, regardless of the previous setting

    boolean tempDec =
        !DsConfigManager.getProperty(DsConfigManager.PROP_MAXF, DsConfigManager.PROP_MAXF_DEFAULT);
    boolean jain =
        DsConfigManager.getProperty(DsConfigManager.PROP_JAIN, DsConfigManager.PROP_JAIN_DEFAULT);

    if (jain) {
      tempDec = false;

      // The testSendCancel() method in the TCK sleeps 5 seconds
      // and the test fails intermittantly because this timer is
      // 5 seconds be default.
      DsConfigManager.setTimerValue(DsSipConstants.T4, 6000);
    }

    shouldDecrementMaxForwards = tempDec;

    // ackCat.setLevel(Level.DEBUG); DsLog4j.thread(ackCat);
    // reqCat.setLevel(Level.DEBUG); DsLog4j.thread(reqCat);
    // cancelCat.setLevel(Level.DEBUG); DsLog4j.thread(cancelCat);
    // respCat.setLevel(Level.DEBUG); DsLog4j.thread(respCat);
    // generalCat.setLevel(Level.DEBUG); DsLog4j.thread(generalCat);
  }

  /** Different Cloudproxy Operational states */
  public enum OperationalState {
    RUNNING("Running"),
    SUSPENDED("Suspended"),
    SHUTTINGDOWN("ShuttingDown"),
    SHUTDOWN("Shutdown");
    String value;

    private OperationalState(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  };

  /** Redirect all incoming requests for shutdown. */
  public static final int REDIRECT_ON_SHUTDOWN = 1;

  /** Reject all incoming requests for shutdown. */
  public static final int REJECT_ON_SHUTDOWN = 2;

  public static final int REJECT_ALL = 3;

  public static final int REJECT_OPTIONS = 4;

  // /////////////////////////////////////////////////
  // Static Data

  /** The singleton representation of the transaction manager. */
  protected static DsSipTransactionManager smp_theSingleton = null;

  private static DsWorkQueue m_outgoingRequestQueue = null;

  private static DsSipTransportLayer m_transportLayer;

  private static DsSipTransactionEventInterface m_eventInterface;

  private static DsSipTransactionTable m_transactionTable = new DsSipTransactionTable();

  private static DsSipRouteFixInterface m_routeFixInterface;

  /** The default interface and the map of specific interfaces. */
  private static DsSipRequestInterface m_defaultInterface;
  // holds DsByteStrings now, rather than Strings - jsm 06/17/2002
  private static Map m_interfaceMap = new HashMap();

  /** <code>true</code> if running as a proxy server. */
  protected static boolean m_proxyServerMode = false;

  private static boolean m_autoResponseToStrayCancel = true;

  // CAFFEINE 2.0 DEVELOPMENT - 100rel required
  /**
   * Used to indicate if 100rel is Required/Supported/Unsupported. It gets default value (SUPPORTED)
   * from the Configure Manager and this value is the defaul value for transactions. Users can set
   * it to other value by calling set100relSupport(). This can be done at transaction manager level
   * and at transaction (client and server) level.
   */
  protected byte m_100relSupport =
      (byte)
          DsConfigManager.getProperty(
              DsConfigManager.PROP_100REL_SUPPORT, DsConfigManager.PROP_100REL_SUPPORT_DEFAULT);

  private static DsSipHeaderList m_serverHeader = null;
  private static DsSipHeaderList m_acceptHeader = null;
  private static DsSipHeaderList m_supportedHeader = null;

  private DsSipMalformedMessageInterface malformedMessageInterface = null;

  private static DsSipTransactionInterfaceFactory m_transactionInterfaceFactory = null;

  // if the user chooses to uses a queue for outbound requests, this is the
  //    number of workers that service the queue
  private static final int DEFAULT_REQ_WORKERS = 2;
  private static final String REQUEST_OUT_QNAME = "REQUESTO";

  private static DsTransactionRemovalInterface m_transactionRemovalCb;

  // /////////////////////////////////////////////////
  // Constructors

  /**
   * Constructs the DsSipTransactionManager and associates it with the transport layer.
   *
   * @param transportLayer transport layer to be associated with the transaction manager
   * @param defaultInterface the default interface to be used
   * @throws DsException if the transaction manager has already been constructed
   */
  public DsSipTransactionManager(
      DsSipTransportLayer transportLayer, DsSipRequestInterface defaultInterface)
      throws DsException {
    Logger cat = generalCat;

    if (smp_theSingleton != null) {
      throw new DsException("There can only be one transaction manager");
    }

    smp_theSingleton = this;

    // Set up tracing and environment.  May better be set up in the Transport constructor.
    operationalState = OperationalState.RUNNING;
    m_transportLayer = transportLayer;
    m_defaultInterface = defaultInterface;

    // provides the transport layer hook to DsConfigManager
    DsConfigManager.setTransportLayer(m_transportLayer);

    m_statelessTransactionInterface = new StatelessTransactionInterface();

    if ((propFile != null) && cat.isEnabled(Level.INFO)) {
      cat.log(Level.INFO, "Successfully loaded Java UA properties from " + propFile);
    }
  }

  /**
   * Constructs the DsSipTransactionManager and associates it with the transport layer and an
   * outbound request queue. All outgoing requests will be enqueued.
   *
   * @param transportLayer transport layer to be associated with the transaction manager
   * @param defaultInterface the default interface to be used
   * @param max_len the maximum length of the outgoing request queue
   * @throws DsException if the the max_len &LT 1 or the transaction manager has already been
   *     constructed
   */
  public DsSipTransactionManager(
      DsSipTransportLayer transportLayer, DsSipRequestInterface defaultInterface, int max_len)
      throws DsException {
    this(transportLayer, defaultInterface);
    if (max_len < 1) {
      throw new DsException(
          "Invalid maximum outgoing request queue length [" + max_len + "], should be > 0");
    }

    // the user code controls neither the queue name nor the number
    //   of workers. These can be manipulated via the DsWorkQueue static
    //   map of (name ==> DsWorkQueue).  This static map is intentionally
    //   undocumented.

    m_outgoingRequestQueue = new DsWorkQueue(REQUEST_OUT_QNAME, max_len, DEFAULT_REQ_WORKERS);
    DsConfigManager.registerQueue((DsQueueInterface) m_outgoingRequestQueue);
  }

  /**
   * Dump state to the log file. Useful for debugging.
   *
   * @return a debug string
   * @deprecated This method is for debugging purpose only and is unsupported.
   */
  public static String dump() {
    StringBuffer buf = new StringBuffer(128);
    buf.append(m_transactionTable.dump());
    buf.append(m_transportLayer.dump());
    buf.append("---------------------------------------------------\n");
    buf.append("-- number of Timers            --------------------\n");
    buf.append("---------------------------------------------------\n");
    // qfang - 07.05.06 - CSCse63190 switch to JDK's Timer implementation
    // buf.append("" + DsTimer.size() + "\n");

    String ret = buf.toString();
    DsLog4j.dumpCat.info(ret);
    return ret;
  }

  /** This method is called to resume the processing of calls by cloudproxy. */
  public static synchronized void resume() {
    if (operationalState == OperationalState.SUSPENDED) {
      String currentState = operationalState.toString();
      operationalState = OperationalState.RUNNING;
      //        	LicenseChecker.setSuspendState(false);
      DsSipTransactionManager.operationsEvent(
          SAEventConstants.CLOUDPROXY_RESUME_ALARM, currentState, operationalState.toString());
    }
  }

  /** This method is called to retrieve CP operationalState */
  public static String getOperationalState() {
    return operationalState.toString();
  }

  /**
   * Determine whether or not the transaction manager is asynchronous mode. In asynchronous mode,
   * outgoing requests will be enqueued before they are sent.
   *
   * @return <code>true</code> if the transaction manager is in asynchronous mode, otherwise returns
   *     <code>false</code>
   */
  public boolean isAsyncMode() {
    return (m_outgoingRequestQueue != null);
  }

  /**
   * Obtain a connection to a server for a given request and process route.
   *
   * @param request the request for which to obtain a connection
   * @return the request connection
   * @throws IOException if thrown by the underlying connection
   * @throws DsException if there is an exception in the User Agent
   */
  public static DsSipConnection getRequestConnection(DsSipRequest request)
      throws IOException, DsException {
    if (DsPerf.ON) DsPerf.start(DsPerf.TM_GET_REQUEST_CONNECTION_R);
    Logger cat = generalCat;
    DsSipConnection ret_connection = null;
    if (cat.isEnabled(Level.DEBUG)) cat.log(Level.DEBUG, "getRequestConnection(DsSipMessage)");

    //  The transport layer adds a route for the local outbound proxy,
    //    if one is set.
    m_transportLayer.setLocalProxyRoute(request);

    DsURI routeTo;
    if (!m_emulate2543Responses) {
      routeTo = request.lrEscape();
    } else {
      routeTo = request.getURI();
    }

    ret_connection = getConnection(routeTo, request.getBindingInfo());

    if (DsPerf.ON) DsPerf.stop(DsPerf.TM_GET_REQUEST_CONNECTION_R);
    return ret_connection;
  }

  /**
   * Obtain a connection to a server for a given request. If server should be searched for,
   * initialize the provided DsSipResolver and return null.
   *
   * @param message the message for which to obtain a connection
   * @param resolver a DsSipResolver to fill in in case we should search for a server
   * @return null if there is more than a single possible <port, IP address, protocol> tuple to try
   *     to connect to per the SIP RFC paragraph on server location. In this case, the resolver
   *     parameter will be initialized with a list of services to try
   * @see DsSipResolver
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException if either remote address not set on non-SIP URI or transport protocol not
   *     supported for this element
   */
  public static DsSipConnection getRequestConnection(DsSipMessage message, DsSipResolver resolver)
      throws SocketException, DsException, UnknownHostException, IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.TM_GET_REQUEST_CONNECTION_MS);

    Logger cat = generalCat;
    if (cat.isEnabled(Level.DEBUG)) {
      cat.log(Level.DEBUG, "getRequestConnection(DsSipMessage, DsSipResolver)");
    }

    DsSipConnection ret_connection = null;

    DsSipRequest request = (DsSipRequest) message;

    //  The transport layer adds a route for the local outbound proxy,
    //    if one is set.
    m_transportLayer.setLocalProxyRoute(request);

    DsURI routeToURI;
    if (!m_emulate2543Responses) {
      routeToURI = request.lrEscape();
    } else {
      routeToURI = request.getURI();
    }

    /* settings made through setConnectionXXXX, override those of the Request-URI */
    String host_str = "";
    int port = RPU, transport = BTU;

    DsBindingInfo binfo = request.getBindingInfo();

    if (routeToURI.isSipURL()) {
      DsSipURL url = (DsSipURL) routeToURI;
      if (!binfo.isRemoteAddressSet()) {
        DsByteString host_byte_str = url.getMAddrParam();
        host_str = (host_byte_str == null ? null : host_byte_str.toString());
        if (host_str == null) {
          host_str = DsByteString.toString(url.getHost());
        }
      } else {
        host_str = binfo.getRemoteAddressStr();
      }

      port =
          binfo.isRemotePortSet() ? binfo.getRemotePort() : (url.hasPort() ? url.getPort() : RPU);
      // GOGONG - 07.13.05 - Return BTU if outgoing transport type is not specified
      int transportParam = url.getTransportParam();
      /*
       * Changing Default transport to TLS if transport is not specified.
       * This changes the default behavior , commented is previous code
       * transport = binfo.isTransportSet() ? binfo.getTransport() :
       * (url.isSecure() ? DsSipTransportType.TLS : ((transportParam ==
       * DsSipTransportType.NONE) ? BTU : transportParam));
       */
      transport =
          binfo.isTransportSet()
              ? binfo.getTransport()
              : (url.isSecure()
                  ? DsSipTransportType.TLS
                  : ((transportParam == DsSipTransportType.NONE)
                      ? DsSipTransportType.TLS
                      : transportParam));

      if (cat.isEnabled(Level.DEBUG)) {
        cat.log(Level.DEBUG, "Remote Host = " + host_str);
        cat.log(Level.DEBUG, "Remote Port = " + port);
        cat.log(Level.DEBUG, "Remote Transport = " + transport);
      }
    } else // not a SIP URL
    {
      if (!binfo.isRemoteAddressSet()) {
        throw new DsException("remote address not set on non-SIP URI");
      }
      host_str = binfo.getRemoteAddressStr();
      port = binfo.isRemotePortSet() ? binfo.getRemotePort() : RPU;
      transport = binfo.isTransportSet() ? binfo.getTransport() : BTU;
    }

    InetAddress localAddr = message.getLocalBindingAddress();
    int localPort = message.getLocalBindingPort();
    DsNetwork network = message.getNetworkReliably();
    // only check if the address is null; we don't want to reset the
    // localAddr simply because the local port is 0
    if (transport == DsSipTransportType.UDP) {
      if (localAddr == null) {
        DsUdpListener listener = network.getUdpListener();
        if (listener != null) {
          localAddr = listener.m_address;
          if (network.isBehindNAT()) {
            localPort = listener.m_port;
          }
        }
      } else if (localPort == LPU && network.isBehindNAT()) {
        Iterator iter = network.getListeners();
        DsTransportListener listener;

        while (iter.hasNext()) {
          listener = (DsTransportListener) iter.next();
          if (listener.getTransport() == DsSipTransportType.UDP) {
            DsUdpListener udpListener = (DsUdpListener) listener;
            if (localAddr == udpListener.m_address) {
              localPort = udpListener.m_port;
              break;
            }
          }
        }
      }
    } else if ((transport == DsSipTransportType.TCP || transport == DsSipTransportType.TLS)
        && (localAddr == null)) {
      DsStreamListener listener = (DsStreamListener) network.getListener(transport);
      if (listener != null) {
        localAddr = listener.localAddress;
      }
    }

    if (cat.isEnabled(Level.INFO)) {
      cat.log(
          Level.INFO,
          "network = "
              + network
              + "\nLocal Address = "
              + localAddr
              + "\nLocal Port = "
              + localPort
              + "\nHost String = "
              + host_str
              + "\nRemote Port = "
              + port
              + "\nTransport = "
              + transport
              + "\nResolver = "
              + resolver);
    }

    ret_connection =
        getSRVConnection(network, localAddr, localPort, host_str, port, transport, resolver);

    if (DsPerf.ON) DsPerf.stop(DsPerf.TM_GET_REQUEST_CONNECTION_MS);
    return ret_connection;
  }

  /**
   * Obtain a connection to a server for a given url. If server should be searched for, initialize
   * the provided DsSipResolver and return null.
   *
   * @param network the network associated with this connection
   * @param lAddr local address
   * @param lPort local port
   * @param url the URL to connect to
   * @param resolver a DsSipResolver to fill in in case we should search for a server
   * @return <code>null</code> if there is more than a single possible &LTport, IP address,
   *     protocol&GT tuple to try to connect to per the SIP RFC paragraph on server location. In
   *     this case, the resolver parameter will be initialized with a list of services to try
   * @see DsSipResolver
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException if transport protocol not supported for this element
   */
  protected static DsSipConnection getSRVConnection(
      DsNetwork network, InetAddress lAddr, int lPort, DsSipURL url, DsSipResolver resolver)
      throws SocketException, DsException, UnknownHostException, IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.TM_GET_SRV_CONNECTION_IiUS);

    DsSipConnection ret_connection = null;

    if (!resolver.shouldSearch(url)) {
      // GOGONG - 07.13.05 - Return default outgoing transport if the transport is not specified
      int transportParam = url.getTransportParam();
      int transport =
          url.isSecure()
              ? DsSipTransportType.TLS
              : ((transportParam == DsSipTransportType.NONE)
                  ? DsConfigManager.getDefaultOutgoingTransport()
                  : transportParam);
      if (!resolver.isSupported(transport)) {
        throw new DsException(
            DsSipTransportType.getTypeAsString(transport)
                + " transport protocol not supported for this element. "
                + " An element can only send messages via a transport protocol it listens on.");
      }
      DsBindingInfo info = url.getBindingInfo();
      info.setLocalAddress(lAddr);
      info.setLocalPort(lPort);
      info.setNetwork(network);
      ret_connection = (DsSipConnection) m_transportLayer.getConnection(info);
    } else {
      resolver.initialize(network, lAddr, lPort, url);
    }

    if (DsPerf.ON) DsPerf.stop(DsPerf.TM_GET_SRV_CONNECTION_IiUS);

    return ret_connection;
  }

  /**
   * Obtain a connection based on the rules for locating a server specified in the SIP RFC.
   *
   * @param network the network associated with this connection
   * @param lAddr local address
   * @param lPort local port
   * @param host the host to connect to
   * @param port the port to connection to
   * @param transport the transport to use for the connection or
   *     DsSipBindingInfo.BINDING_TRANSPORT_UNSPECIFIED
   * @param resolver a DsSipResolver to initialize if SRV searching should be performed according to
   *     the SIP spec
   * @return a DsSipConnection for this host, port and transport or null if the server should be
   *     searched for according to the SIP specification
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException if transport protocol not supported for this element
   */
  protected static DsSipConnection getSRVConnection(
      DsNetwork network,
      InetAddress lAddr,
      int lPort,
      String host,
      int port,
      int transport,
      DsSipResolver resolver)
      throws SocketException, DsException, UnknownHostException, IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.TM_GET_SRV_CONNECTION_IiSiiS);
    DsSipConnection ret_connection = null;

    if (!resolver.shouldSearch(host, port, transport)) {
      if (!resolver.isSupported(transport)) {
        throw new DsException(
            DsSipTransportType.getTypeAsString(transport)
                + " transport protocol not supported for this element. "
                + " An element can only send messages via a transport protocol it listens on");
      }

      DsBindingInfo info = new DsBindingInfo(lAddr, lPort, host, port, transport);
      info.setNetwork(network);

      /*
       * edited by radmohan
       *
       * CSCtz70393 Thread pool exhausted when all elements in srv group is down
       */
      if (DsSipTransactionManager.m_useDsUnreachableTable) {

        DsLog4j.resolvCat.log(
            Level.DEBUG,
            "DsSipTransactionManager.m_useDsUnreachableTable: is set to :"
                + DsSipTransactionManager.m_useDsUnreachableTable);
        if (!DsUnreachableDestinationTable.getInstance()
            .contains(info.getRemoteAddress(), info.getRemotePort(), info.getTransport())) {

          DsLog4j.resolvCat.log(
              Level.DEBUG,
              "DsSipTransactionManager.getSRVConnection: not present DsUnreachableDestinationTable:"
                  + info.getRemoteAddress());

          ret_connection = (DsSipConnection) m_transportLayer.getConnection(info);
          if (ret_connection == null) {

            DsUnreachableDestinationTable.getInstance()
                .add(info.getRemoteAddress(), info.getRemotePort(), info.getTransport());
          }
        } else {
          ret_connection = null;
        }

        /*
         * edited by kiran
         */
      } else {
        DsLog4j.resolvCat.log(
            Level.DEBUG,
            "DsSipTransactionManager.m_useDsUnreachableTable: is set to :"
                + DsSipTransactionManager.m_useDsUnreachableTable);
        ret_connection = (DsSipConnection) m_transportLayer.getConnection(info);
      }
    } else {
      resolver.initialize(network, lAddr, lPort, host, port, transport);
    }

    if (DsPerf.ON) DsPerf.stop(DsPerf.TM_GET_SRV_CONNECTION_IiSiiS);

    return ret_connection;
  }

  /**
   * Obtain a connection based on the rules for locating a server specified in the SIP RFC.
   *
   * @param network the network associated with this connection
   * @param host the host to connect to
   * @param port the port to connection to
   * @param transport the transport to use for the connection or
   *     DsSipBindingInfo.BINDING_TRANSPORT_UNSPECIFIED
   * @param resolver a DsSipResolver to initialize if SRV searching should be performed according to
   *     the SIP spec
   * @return a DsSipConnection for this host, port and transport or null if the server should be
   *     searched for according to the SIP specification
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException if transport protocol not supported for this element
   */
  protected static DsSipConnection getSRVConnection(
      DsNetwork network, String host, int port, int transport, DsSipResolver resolver)
      throws SocketException, DsException, UnknownHostException, IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.TM_GET_SRV_CONNECTION_SiiS);

    DsSipConnection ret_connection = null;

    if (!resolver.shouldSearch(host, port, transport)) {
      if (!resolver.isSupported(transport)) {
        throw new DsException(
            DsSipTransportType.getTypeAsString(transport)
                + " transport protocol not supported for this element."
                + " An element can only send messages via a transport protocol it listens on");
      }
      DsBindingInfo info = new DsBindingInfo(host, port, transport);
      info.setNetwork(network);
      ret_connection = (DsSipConnection) m_transportLayer.getConnection(info);
    } else {
      resolver.initialize(network, host, port, transport);
    }

    if (DsPerf.ON) DsPerf.stop(DsPerf.TM_GET_SRV_CONNECTION_SiiS);
    return ret_connection;
  }

  /**
   * Sets the malformed message interface that will be called back if any malformed SIP message is
   * received.
   *
   * @param malformedMessageInterface the malformed message interface that will be called back if
   *     any malformed SIP message is received.
   */
  public void setMalformedMessageInterface(
      DsSipMalformedMessageInterface malformedMessageInterface) {
    this.malformedMessageInterface = malformedMessageInterface;
  }

  /**
   * Returns the malformed message interface that is called back if any malformed SIP message is
   * received.
   *
   * @return the malformed message interface that is called back if any malformed SIP message is
   *     received.
   */
  public DsSipMalformedMessageInterface getMalformedMessageInterface() {
    return malformedMessageInterface;
  }

  /**
   * Sets the callback interface for notification when a transaction is removed.
   *
   * @param cb the callback interface for transaction removal notification.
   */
  public void setTransactionRemovalCb(DsTransactionRemovalInterface cb) {
    m_transactionRemovalCb = cb;
  }

  /**
   * Gets the callback interface for notification when a transaction is removed.
   *
   * @return the callback interface for transaction removal notification.
   */
  public DsTransactionRemovalInterface getTransactionRemovalCb() {
    return m_transactionRemovalCb;
  }

  /**
   * Obtain a connection to a server for a given url. If server should be searched for, initialize
   * the provided DsSipResolver and return null.
   *
   * @param network the network associated with this connection
   * @param url the URL to connect to
   * @param resolver a DsSipResolver to fill in in case we should search for a server
   * @return null if there is more than a single possible <port, IP address, protocol> tuple to try
   *     to connect to per the SIP RFC paragraph on server location. In this case, the resolver
   *     parameter will be initialized with a list of services to try
   * @see DsSipResolver
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException if transport protocol not supported for this element
   */
  protected static DsSipConnection getSRVConnection(
      DsNetwork network, DsSipURL url, DsSipResolver resolver)
      throws SocketException, DsException, UnknownHostException, IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.TM_GET_SRV_CONNECTION_US);

    DsSipConnection ret_connection = null;

    if (!resolver.shouldSearch(url)) {
      // GOGONG - 07.13.05 - Return default outgoing transport if the transport type is not
      // specified
      int transportParam = url.getTransportParam();
      int transport =
          url.isSecure()
              ? DsSipTransportType.TLS
              : ((transportParam == DsSipTransportType.NONE)
                  ? DsConfigManager.getDefaultOutgoingTransport()
                  : transportParam);

      if (!resolver.isSupported(transport)) {
        throw new DsException(
            DsSipTransportType.getTypeAsString(transport)
                + " transport protocol not supported for this element. "
                + " An element can only send messages via a transport protocol it listens on");
      }
      DsBindingInfo info = url.getBindingInfo();
      ret_connection = (DsSipConnection) m_transportLayer.getConnection(info);
    } else {
      resolver.initialize(network, url);
    }

    if (DsPerf.ON) DsPerf.stop(DsPerf.TM_GET_SRV_CONNECTION_US);
    return ret_connection;
  }

  /**
   * Connect to URI, with DsBindingInfo overriding DsURI. If the URI parameter isn't a SIP URL, the
   * binding info's address must be present or an IOException will be thrown.
   *
   * @param uri the URI to get the connection for
   * @param info the binding info to get the connection for
   * @return the connection that matches the supplied URI and binding info
   * @throws IOException if thrown by the underlying connection
   */
  public static DsSipConnection getConnection(DsURI uri, DsBindingInfo info) throws IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.TM_GET_CONNECTION_UB);

    // Check for the connection ID
    if (info != null) {
      DsByteString conId = info.getConnectionId();
      if (conId != null) {
        return DsSipConnectionAssociations.getConnection(conId);
      }
    }

    DsSipConnection ret_connection = null;
    boolean sip_url = uri.isSipURL();
    DsSipURL url = null;

    // two conditions where we complain about not having a host
    // to connect to.

    //  1) no binding info and no sip url
    if (info == null && !sip_url) {
      throw new IOException("can't connect to non-SIP URI: " + uri);
    }

    //  2) no binding info remote host part and no sip url
    info = info == null ? DsBindingInfo.EMPTY_INFO : info;
    if (!info.isRemoteAddressSet() && !sip_url) {
      throw new IOException("can't connect to non-SIP URI: " + uri);
    }

    // if it's a SIP URL cast it
    if (sip_url) url = (DsSipURL) uri;

    // first get the host string

    String host_str = null;
    if (!info.isRemoteAddressSet()) {
      // this is safe, since we did validation already
      DsByteString host_byte_str = url.getMAddrParam();

      host_str = (host_byte_str == null) ? null : host_byte_str.toString();
      if (host_str == null) {
        host_str = DsByteString.toString(url.getHost());
      }
    } else {
      host_str = info.getRemoteAddressStr();
    }

    //  then get the transport and port
    // GOGONG - 07.13.05 - get default transport type from DsConfigManager instead of hard-coding to
    // UDP
    int transport = DsConfigManager.getDefaultOutgoingTransport();
    int port = 5060;

    if (sip_url) {
      // GOGONG - 07.13.05 - Return default outgoing transport if the transport type is not
      // specified
      int transportParam = url.getTransportParam();
      transport =
          info.isTransportSet()
              ? info.getTransport()
              : (url.isSecure()
                  ? DsSipTransportType.TLS
                  : ((transportParam == DsSipTransportType.NONE) ? transport : transportParam));
      DsSipTransportType type = DsSipTransportType.intern(transport);
      if (type != null) {
        port = type.getDefaultPort();
      }
      port = info.isRemotePortSet() ? info.getRemotePort() : (url.hasPort() ? url.getPort() : port);
    } else {
      transport = info.isTransportSet() ? info.getTransport() : transport;
      DsSipTransportType type = DsSipTransportType.intern(transport);
      if (type != null) {
        port = type.getDefaultPort();
      }
      port = info.isRemotePortSet() ? info.getRemotePort() : port;
    }

    try {
      ret_connection =
          (DsSipConnection)
              DsSipTransactionManager.getTransportLayer()
                  .getConnection(
                      info.getNetwork(),
                      info.getLocalAddress(),
                      info.getLocalPort(),
                      InetAddress.getByName(host_str),
                      port,
                      transport);
    } catch (Exception exc) {
      throw new IOException(exc.getMessage());
    }
    if (DsPerf.ON) DsPerf.stop(DsPerf.TM_GET_CONNECTION_UB);
    return ret_connection;
  }

  /**
   * Obtain a connection to a server for a given message.
   *
   * <blockquote>
   *
   * SRV records will be ordered as described by the SIP RFC. The (protocol, port, addr) tuples will
   * be searched until the first UDP protocol is hit or first successful TCP connection is
   * established.
   *
   * </blockquote>
   *
   * @param message the message for which to obtain a connection
   * @return connection for the given message
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException if either remote address not set on non-SIP URI or transport protocol not
   *     supported for this proxy or invalid VIA header in the message
   */
  public static DsSipConnection getConnection(DsSipMessage message)
      throws SocketException, DsException, UnknownHostException, IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.TM_GET_CONNECTION_M);
    Logger cat = generalCat;

    DsSipConnection ret_connection = null;
    if (cat.isEnabled(Level.INFO)) cat.log(Level.INFO, "getConnection(DsSipMessage)");

    if (message.isRequest()) {
      if (DsPerf.ON) DsPerf.stop(DsPerf.TM_GET_CONNECTION_M);
      ret_connection = getRequestConnection((DsSipRequest) message);
    } else // Response
    {

      DsSipViaHeader viaHeader = null;
      try {
        Object header = message.getViaHeader();
        if (header == null) {
          throw new DsException("No Via Header in response");
        }
        viaHeader = (DsSipViaHeader) message.getHeaderValidate(DsSipConstants.VIA);
      } catch (Exception exc) {
        throw new DsException("Invalid Via Header in response");
      }

      ret_connection = getConnection(message.getBindingInfo(), viaHeader);
    }

    if (DsPerf.ON) DsPerf.stop(DsPerf.TM_GET_CONNECTION_M);

    return ret_connection;
  }

  /**
   * Obtain a connection based on the Via header following the rules for maddr, rport etc.
   *
   * @param viaHeader the Via header for which to obtain the connection
   * @return the found connection
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException if either remote address not set on non-SIP URI or transport protocol not
   *     supported for this proxy or invalid VIA header in the message
   */
  public static DsSipConnection getConnection(DsSipViaHeader viaHeader)
      throws SocketException, DsException, UnknownHostException, IOException {
    return getConnection(null, viaHeader);
  }

  /** Obtain a connection based on the Via header following the rules for maddr, rport etc. */
  private static DsSipConnection getConnection(DsBindingInfo info, DsSipViaHeader viaHeader)
      throws SocketException, DsException, UnknownHostException, IOException {
    // Check for the connection ID
    if (info != null) {
      DsByteString conId = info.getConnectionId();
      if (conId != null) {
        return DsSipConnectionAssociations.getConnection(conId);
      }
    }

    DsSipConnection ret_connection = null;
    InetAddress addr = null;
    DsNetwork network = null;

    if (viaHeader == null) {
      throw new IllegalArgumentException("null Via header");
    }

    int transport = viaHeader.getTransport();

    String param = DsByteString.toString(viaHeader.getMaddr());

    if (param != null) {
      addr = InetAddress.getByName(param);
    } else {
      param = DsByteString.toString(viaHeader.getReceived());

      if (param != null) {
        addr = InetAddress.getByName(param);
      } else {
        param = DsByteString.toString(viaHeader.getHost());
        if (param == null) {
          throw new DsException("maddr, received, and host parameters are null in VIA header");
        }
        addr = InetAddress.getByName(param);
      }
    }

    boolean rport = false;
    int port = 0;

    if (viaHeader.isRPortSet()) {
      port = viaHeader.getRPort();
      rport = true;
    } else {
      port = viaHeader.getPort();
    }

    if (info != null) {
      network = info.getNetwork();
    }

    // another ugly hack to select the listen point as the source of
    //    the response...  this one is here because originally this
    //    behavior was tied to the DGRAM_NAT_TRAVERSAL setting
    if (transport == DsSipTransportType.UDP && rport) {
      InetAddress laddr = null;
      int lport = LPU;

      if (info != null) {
        laddr = info.getLocalAddress();
        lport = info.getLocalPort();
      }

      ret_connection =
          (DsSipConnection)
              m_transportLayer.getConnection(network, laddr, lport, addr, port, transport, true);
    }
    // if we're using UDP and NAT traversal, use the local binding info to
    //    bind locally to the listen port.
    else if (transport == DsSipTransportType.UDP && (network != null && network.isBehindNAT())) {
      InetAddress laddr = null;
      int lport = LPU;

      if (info != null) {
        laddr = info.getLocalAddress();
        lport = info.getLocalPort();
      }

      ret_connection =
          (DsSipConnection)
              m_transportLayer.getConnection(network, laddr, lport, addr, port, transport);
    } else {
      ret_connection =
          (DsSipConnection) m_transportLayer.getConnection(network, addr, port, transport);
    }

    return ret_connection;
  }

  // /////////////////////////////////////////////////
  // Public Methods

  /**
   * If you are running the lower layer as a Proxy Server, call setProxyServerMode() after
   * constructing the TransactionManager. This method will allow the slight differences required to
   * for proxies to be handled correctly. Two major issues Proxy mode addresses are having Proxy
   * servers only send 200 Responses only once and having client 200 Response retransmissions all
   * call the ClientTransaction.finalResponse() method.
   *
   * @param proxyServerMode Pass in true to place Transaction Manager in Proxy Server mode and false
   *     to turn off Proxy Server mode.
   */
  public static final void setProxyServerMode(boolean proxyServerMode) {
    m_proxyServerMode = proxyServerMode;
    DsSipTransactionTable.setUseRequestURI(proxyServerMode);
  }

  /**
   * Use isProxyServerMode() to see if the TransactionManager is in Proxy mode.
   *
   * @return If the TransactionManager is in proxy mode, true is returned. Otherwise false is
   *     returned.
   */
  public static final boolean isProxyServerMode() {
    return m_proxyServerMode;
  }

  // CAFFEINE 2.0 DEVELOPMENT - PRACK required
  /**
   * Set the 100rel support level to one of "Require" (REQUIRE), "Supported" (SUPPORTED), or
   * "Unsupported" (UNSUPPORTED)
   *
   * @param attribute pass in one of the above attributes; if null is passed, the default is in
   *     effect.
   */
  public void set100relSupport(byte attribute) {
    Logger cat = generalCat;

    if (attribute == DsSipConstants.REQUIRE
        || attribute == DsSipConstants.SUPPORTED
        || attribute == DsSipConstants.UNSUPPORTED) {
      m_100relSupport = attribute;
    } else {
      if (cat.isEnabled(Level.WARN))
        cat.log(Level.WARN, "set100relSupport() Failed with invalid attribute: " + attribute);
    }
  }

  /**
   * Use get100relSupport() to get the value of 100rel support level
   *
   * @return The value of the current 100rel support level is returned.
   */
  public byte get100relSupport() {
    return m_100relSupport;
  }

  /**
   * Normally a 481 response is sent when a CANCEL is received for a transaction that does not
   * exist. This method prevents that response from automatically being sent.
   *
   * @deprecated use autoResponseToStrayCancel(boolean) instead.
   */
  public static final void noAutoResponseToStrayCancel() {
    m_autoResponseToStrayCancel = false;
  }

  /**
   * Normally a 481 response is sent when a CANCEL is received for a transaction that does not
   * exist. This method allows turning on and off this automatic sending of responses.
   *
   * @param autoRespond boolean to indicate whether 481 responses should be automatically sent or
   *     not.
   */
  public static final void autoResponseToStrayCancel(boolean autoRespond) {
    m_autoResponseToStrayCancel = autoRespond;
  }

  /**
   * Tells if we should automatically send response for stray cancel.
   *
   * @return <code>true</code> if we should automatically send response for stray cancel
   */
  static final boolean shouldAutoRespondToStrayCancel() {
    return m_autoResponseToStrayCancel;
  }

  /**
   * This method is used to shutdown the transactionManager basically closing the SIP stack Any new
   * requests will be redirected. Upon actual shutdown, the DsSipTransactionEventInterface
   * implementation (if provided) will be notified.
   *
   * @param shutdownSeconds the seconds in which to no longer wait for transactions(Hard Kill). -1 =
   *     infinite
   * @param redirectHost the host to send in the contact message.
   * @param redirectPort the port to send in the contact message.
   * @param redirectTransport the transport type to send in the contact message.
   * @throws DsAlreadyShuttingDownException if it is already shutting down
   */
  public static synchronized void shutdownRedirect(
      long shutdownSeconds, String redirectHost, int redirectPort, int redirectTransport)
      throws DsAlreadyShuttingDownException {
    if (operationalState == OperationalState.SHUTTINGDOWN) {
      throw new DsAlreadyShuttingDownException();
    }
    operationalState = OperationalState.SHUTTINGDOWN;

    if (shutdownSeconds > -1) {
      DsTimer.schedule(shutdownSeconds * 1000L, new ShutdownMonitor(), FINAL);
    } else {
      DsTimer.schedule(5000L, new ShutdownMonitor(), null);
    }
  }

  /**
   * This method is used to shutdown the transactionManager basically closing the SIP stack Any new
   * requests will be rejected. Upon actual shutdown, the DsSipTransactionEventInteraface
   * implementation (if provided) will be notified.
   *
   * @param shutdownSeconds the seconds in which to no longer wait for transactions(Hard Kill). -1 =
   *     infinite
   * @param retryAfter The retry value for the retryAfter header.
   * @throws DsAlreadyShuttingDownException if it is already shutting down
   */
  public static synchronized void shutdownReject(int shutdownSeconds)
      throws DsAlreadyShuttingDownException {
    if (operationalState == OperationalState.SHUTTINGDOWN) {
      throw new DsAlreadyShuttingDownException();
    }

    operationalState = OperationalState.SHUTTINGDOWN;

    if (shutdownSeconds > -1) {
      DsTimer.schedule(shutdownSeconds * 1000L, new ShutdownMonitor(), FINAL);
    } else {
      DsTimer.schedule(5000L, new ShutdownMonitor(), null);
    }
  }

  /**
   * This method is used to suspend the transactionManager basically closing the SIP stack. Any new
   * requests will be rejected.
   *
   * @param suspendSeconds time in seconds - Reject only OPTIONS till this timeout - Reject all
   *     Requests after this timeout.
   */
  public static synchronized void maintenanceSuspend() {
    String currentState = operationalState.toString();
    operationalState = OperationalState.SUSPENDED;
    //    	LicenseChecker.setSuspendState(true);
    DsSipTransactionManager.operationsEvent(
        SAEventConstants.CLOUDPROXY_SUSPEND_ALARM, currentState, operationalState.toString());
  }

  /**
   * Returns a pointer to the Transaction Manager.
   *
   * @return the singleton instance of transaction manager
   * @throws NullPointerException if transaction manager is not constructed yet
   */
  public static DsSipTransactionManager getTransactionManager() throws NullPointerException {
    if (smp_theSingleton == null) {
      throw new NullPointerException(
          "TransactionManager.getTransactionManager(): "
              + " Trying to get the Transaction Manager before constructing it.");
    }

    return smp_theSingleton;
  }

  /**
   * Returns the number of client Transactions currently being processed.
   *
   * @return the number of client transactions.
   */
  public int getSizeClientTransactionMap() {
    return m_transactionTable.getCurrentClientTransactionCount();
  }

  /**
   * Returns the total number of client Transactions created.
   *
   * @return the number of client transactions created.
   */
  public int getTotalClientTransactions() {
    return m_transactionTable.getTotalClientTransactionCount();
  }

  /**
   * Returns the number of server Transactions currently being processed.
   *
   * @return the number of server transactions.
   */
  public int getSizeServerTransactionMap() {
    return m_transactionTable.getCurrentServerTransactionCount();
  }

  /**
   * Returns the total number of server Transactions created.
   *
   * @return the number of server transactions created.
   */
  public int getTotalServerTransactions() {
    return m_transactionTable.getTotalServerTransactionCount();
  }

  // qfang - 02.02.06 - These methods were added as part of statistics
  // enhancement in caffeine 1.0 (EDCS-306362). While merging with DS's
  // latest code, "shadow" table is replaced by "previous" one.
  /**
   * Returns the total number of transactions (client & server) that are in progress plus the
   * transactions that have reached the terminated state.
   *
   * @param isCurrent <code>false</code> if you want to get count since reboot <code>false</code> if
   *     you want to get last snapshot counts.
   */
  public int getTotalTransactionCount(boolean isCurrent) {
    if (isCurrent) {
      return m_transactionTable.getTotalClientTransactionCount()
          + m_transactionTable.getTotalServerTransactionCount();
    } else {
      return m_transactionTable.getTotalClientTransactionCountReset()
          + m_transactionTable.getTotalServerTransactionCountReset();
    }
  }

  /**
   * This method takes a snapshot of the "absolute" or SNMP view of the transaction count into
   * "previous" transaction count.
   */
  public void takeSnapshotForTransactionCount() {
    m_transactionTable.takeSnapshotForTransactionCount();
  }

  // /////////////////////////////////////
  //  factory methods
  //

  /**
   * Factory interface for DsSipClientTransaction.
   *
   * @param request Handle of message to be sent to server.
   * @param clientInterface Optional callback interface to user-level callbacks.
   * @param clientTransportInfo If the client wishes to use transport information other than that
   *     held by transport layer, DsSipClientTransportInfo is implemented and passed to this
   *     constructor
   * @return the new client transaction or null if the transaction manager is asynchronous mode and
   *     the outgoing request queue is full
   * @throws DsException if stack is shut down
   */
  public DsSipClientTransaction createClientTransaction(
      DsSipRequest request,
      DsSipClientTransportInfo clientTransportInfo,
      DsSipClientTransactionInterface clientInterface)
      throws DsException {
    if (operationalState == OperationalState.SHUTDOWN) {
      throw new DsShutdownException("Stack is shut down");
    }

    return m_transactionFactory.createClientTransaction(
        request, clientTransportInfo, clientInterface);
  }

  /**
   * Factory interface for DsSipClientTransaction. Also starts the transaction.
   *
   * @param request Handle of message to be sent to server.
   * @param clientInterface Optional callback interface to user-level callbacks.
   * @param clientTransportInfo If the client wishes to use transport information other than that
   *     held by transport layer, DsSipClientTransportInfo is implemented and passed to this
   *     constructor
   * @return the new client transaction or null if the transaction manager is asynchronous mode and
   *     the outgoing request queue is full
   * @throws IOException if the execution of the state machine results in an IOException
   * @throws DsException if stack is shut down
   */
  public DsSipClientTransaction startClientTransaction(
      DsSipRequest request,
      DsSipClientTransportInfo clientTransportInfo,
      DsSipClientTransactionInterface clientInterface)
      throws DsException, IOException {
    if (operationalState == OperationalState.SHUTDOWN) {
      throw new DsShutdownException("Stack is shut down");
    }
    DsSipClientTransaction txn =
        m_transactionFactory.createClientTransaction(request, clientTransportInfo, clientInterface);
    return startClientTransaction(txn);
  }

  /**
   * Factory interface for DsSipClientTransaction.
   *
   * @param request Handle of message to be sent to server.
   * @param clientInterface Optional callback interface to user-level callbacks.
   * @param transactionParams Optional. Reserved for future use.
   * @return the new client transaction or null if the transaction manager is asynchronous mode and
   *     the outgoing request queue is full
   * @throws DsException if stack is shut down
   */
  public DsSipClientTransaction createClientTransaction(
      DsSipRequest request,
      DsSipClientTransactionInterface clientInterface,
      DsSipTransactionParams transactionParams)
      throws DsException {
    if (operationalState == OperationalState.SHUTDOWN) {
      throw new DsShutdownException("Stack is shut down");
    }
    return m_transactionFactory.createClientTransaction(
        request, clientInterface, transactionParams);
  }

  /**
   * Factory interface for DsSipClientTransaction. Also starts the transaction.
   *
   * @param request Handle of message to be sent to server.
   * @param clientInterface optional callback interface to user-level callbacks.
   * @param transactionParams optional, reserved for future use.
   * @return the new client transaction or null if the transaction manager is asynchronous mode and
   *     the outgoing request queue is full
   * @throws IOException if the execution of the state machine results in an IOException
   * @throws DsException if stack is shut down
   */
  public DsSipClientTransaction startClientTransaction(
      DsSipRequest request,
      DsSipClientTransactionInterface clientInterface,
      DsSipTransactionParams transactionParams)
      throws DsException, IOException {
    if (operationalState == OperationalState.SHUTDOWN) {
      throw new DsShutdownException("Stack is shut down");
    }
    DsSipClientTransaction txn =
        m_transactionFactory.createClientTransaction(request, clientInterface, transactionParams);
    return startClientTransaction(txn);
  }

  /**
   * Start the client transaction, enqueuing it if there is an outbound queue defined.
   *
   * @param txn the transaction to start
   * @return the new client transaction or null if the transaction manager is asynchronous mode and
   *     the outgoing request queue is full
   * @throws IOException if the execution of the state machine results in an IOException
   * @throws DsException if stack is shut down
   */
  public DsSipClientTransaction startClientTransaction(DsSipClientTransaction txn)
      throws DsException, IOException {
    if (m_outgoingRequestQueue != null) {
      // if the queue is full, nqueue will return non-null (the displaced
      //   element or the new element depending on discard policy)

      DsUnitOfWork uow = null;
      if ((uow = m_outgoingRequestQueue.nqueue(new OutgoingRequestUOW(txn))) != null) {
        uow.abort();
        txn = null;
      }
    } else {
      txn.start();
    }
    return txn;
  }

  //
  //  End factory methods
  // /////////////////////////////////////

  /**
   * If the user sets a DsSipTransactionFactory, the transaction manager will use it to create
   * transactions. The default behavior is to use DsSipServerTransaction and DsSipClientTransaction.
   * If the user wishes to extend these classes, this factory interface should be used to force the
   * transaction manager to use these derived classes.
   *
   * @param factory the transaction factory to use
   */
  public synchronized void setTransactionFactory(DsSipTransactionFactory factory) {
    if (factory == null) {
      m_transactionFactory = new DsSipDefaultTransactionFactory(m_transactionInterfaceFactory);
    } else {
      m_transactionFactory = factory;
    }
  }

  /**
   * Get the current transaction factory.
   *
   * @return the current transaction factory
   */
  public synchronized DsSipTransactionFactory getTransactionFactory() {
    return m_transactionFactory;
  }

  /**
   * If the user sets a DsSipTransactionInterfaceFactory, the transaction manager will use it to
   * create transaction interfaces. Transaction interfaces are passed to transaction constructors to
   * allow the user code to handle callbacks. If the user has set a DsSipTransactionFactory, this
   * interface will be ignored.
   *
   * @param factory the factory to use for creating transaction interfaces
   */
  public synchronized void setTransactionInterfaceFactory(
      DsSipTransactionInterfaceFactory factory) {
    m_transactionInterfaceFactory = factory;
    if (m_transactionFactory != null
        && (m_transactionFactory instanceof DsSipDefaultTransactionFactory)) {
      ((DsSipDefaultTransactionFactory) m_transactionFactory)
          .setTransactionInterfaceFactory(factory);
    }
  }

  /**
   * Get a request interface. NOTE: Method names are <b>case-sensitive</b>.
   *
   * @param method the <b>case-sensitive</b> name of the SIP method corresponding to the interface
   * @return the request interface
   * @throws DsException if method specified is CANCEL or PRACK (unless in proxy server mode)
   */
  public synchronized DsSipRequestInterface getRequestInterface(String method) throws DsException {

    if (method == null) {
      return m_defaultInterface;
    } else if (method.equals("CANCEL")) {
      throw new DsException(
          "TransactionManager.setRequestInterface(): Cannot get CANCEL interface");
    }
    // CAFFEINE 2.0 DEVELOPMENT - PRACK required
    else if (method.equals("PRACK") && !isProxyServerMode()) {
      throw new DsException("TransactionManager.setRequestInterface(): Cannot get PRACK interface");
    } else {
      DsByteString bsMethod = DsByteString.newInstance(method);
      return (DsSipRequestInterface) m_interfaceMap.get(bsMethod);
    }
  }

  /**
   * Sets the interface to be used as a callback mechanism for the given SIP method string supplied.
   * When a request arrives that is not associated with an existing transaction, the supplied
   * interface is invoked if the SIP request method matches the string. Method names are
   * case-sensitive.
   *
   * @param defaultInterface the interface called when a SIP request arrives
   * @param method the name of the SIP method corresponding to the interface
   * @throws DsException if method specified is CANCEL
   */
  public synchronized void setRequestInterface(
      DsSipRequestInterface defaultInterface, String method) throws DsException {
    Logger cat = generalCat;
    if (cat.isEnabled(Level.DEBUG)) {
      cat.log(Level.DEBUG, "setRequestInterface() - method = " + method);
    }

    if (method == null) {
      m_defaultInterface = defaultInterface;
    } else if (method.equals("CANCEL")) {
      // we might have an implementation that lets users to change the CANCEL
      // interface for fancy things but right now do not allow it
      throw new DsException(
          "TransactionManager.setRequestInterface(): Cannot change CANCEL interface");
    } else {
      // Make sure that we use THE BS_METHOD so that == works for HashMap lookup,
      // rather than needing to do a full .equals() comparison
      DsByteString bsMethod = DsByteString.newInstance(method);
      m_interfaceMap.put(bsMethod, defaultInterface);
    }
  }

  /**
   * Removes the interface being used as a callback mechanism for the given SIP method string
   * supplied. This method has no effect if <code>method</code> is <code>null</code>.
   *
   * @param method the name of the SIP method's interface to remove
   * @return the removed request interface, possibly <code>null</code>.
   * @throws DsException if method specified is CANCEL
   */
  public synchronized DsSipRequestInterface removeRequestInterface(String method)
      throws DsException {
    Logger cat = generalCat;
    if (cat.isEnabled(Level.DEBUG)) {
      cat.log(Level.DEBUG, "removeRequestInterface() - method = " + method);
    }

    if (method == null) {
      return null;
    } else if (method.equals("CANCEL")) {
      // we might have an implementation that lets users to change the CANCEL
      // interface for fancy things but right now do not allow it
      throw new DsException(
          "TransactionManager.removeRequestInterface(): Cannot remove the CANCEL interface");
    }

    // Make sure that we use THE BS_METHOD so that == works for HashMap lookup,
    // rather than needing to do a full .equals() comparison
    DsByteString bsMethod = DsByteString.newInstance(method);
    DsSipRequestInterface ri = (DsSipRequestInterface) m_interfaceMap.remove(bsMethod);

    return ri;
  }

  /**
   * Set the interface for letting the user code decide if it recognizes a URI. This is used to do
   * the fixing (unescaping) trick for backward compatibility with strict routers.
   *
   * @param rfi the route fix interface to use
   */
  public synchronized void setRouteFixInterface(DsSipRouteFixInterface rfi) {
    m_routeFixInterface = rfi;
  }

  /**
   * Get the route fix interface (used by DsSipServerTransactionIImpl to fix the ACK).
   *
   * @return the route fix interface to use
   */
  synchronized DsSipRouteFixInterface getRouteFixInterface() {
    return m_routeFixInterface;
  }

  /**
   * Adds the received parameter to the Via header.
   *
   * @param aMessage the message to add the received parameter to
   * @param originAddress the address to add
   */
  private void addReceiveParameter(DsSipMessage aMessage, InetAddress originAddress) {
    Logger cat = generalCat;

    try {
      DsSipViaHeader via = (DsSipViaHeader) aMessage.getHeaderValidate(DsSipConstants.VIA);
      if (via != null) {
        // try to resolve the hosts and compare the fully qualified host names
        DsByteString addressStr = via.getHost();
        byte[] oriAddressStr = DsString.getHostBytes(originAddress);
        if (addressStr == null || !addressStr.equals(oriAddressStr)) {
          via.setReceived(new DsByteString(oriAddressStr));
        }
      }
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error("Problem setting received parameter!", e);
      }
    }
  }

  /**
   * Checks if the rport flag is set in this message. If yes then adds the rport parameter.
   *
   * @param aMessage the message for which the rport needs to be set
   * @param port the rport to be set
   */
  private void addRPortParameter(DsSipMessage aMessage, int port) {
    // here we must have a valid via header
    DsSipViaHeader viaHeader = null;
    try {
      viaHeader = aMessage.getViaHeaderValidate();
    } catch (Exception exc) {
      // ??
    }
    if (viaHeader != null && viaHeader.isRPortPresent()) {
      viaHeader.setRPort(port);
    }
  }

  /**
   * This method is invoked when one of the listeners in the system is in the process of shutting
   * down. During listener shutdown, non-ACK requests are either rejected(SERVICE_UNAVAILABLE
   * response) or redirected(MOVED_TEMPORARILY response). according to user configuration.
   *
   * @param request the request to process
   * @param transaction the transaction to use for processing the request
   * @return true if the message is a request other than ACK, otherwise returns false
   */
  private boolean processRequestForListenerShutdown(
      DsSipRequest request, DsSipServerTransaction transaction) {
    Logger cat = generalCat;
    if (request.getMethodID() == DsSipConstants.ACK) {
      return false;
    }

    try {
      DsSipResponse response;
      String sbody =
          "This transaction could not complete because this specific listen port is shutting down momentarily.";
      byte[] body = sbody.getBytes();
      DsByteString contentType = new DsByteString("text/plain");

      response =
          new DsSipResponse(
              DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE,
              request,
              body,
              new DsByteString("text/plain"));

      // the via header gets set in the creation of the response
      transaction.sendResponse(response);
      DsMessageStatistics.logRequest(
          DsMessageLoggingInterface.REASON_SHUTDOWN,
          DsMessageLoggingInterface.DIRECTION_IN,
          request);
      DsMessageStatistics.logResponse(
          DsMessageLoggingInterface.REASON_AUTO,
          DsMessageLoggingInterface.DIRECTION_OUT,
          response,
          request);
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error("processForiListenerShutdown(): Exception when sending Response", e);
      }
    }

    return true;
  }

  /*

  xxxxD-via    Cancel Server Transactions
  ^
  xxxx-via     Server Transactions
  ^
  xxxx-via     Merged Server Transactions
  ^
  xxxx-via     Client Transactions
  ^

  */

  /**
   * This method should now replace processMessage as the primary entry into the Transaction
   * Manager. It used to be processMessage(), but that required another class to handle the message
   * parsing phase. It is preferred that the Transaction Manager handle the call to the parser. This
   * way, parser exceptions can be handled directly. After the message is parsed, processMessage()
   * is called.
   *
   * @param msgBytes the unparsed message to be processed
   */
  public void processMessageBytes(DsSipMessageBytes msgBytes) {
    Logger cat = generalCat;
    DsSipMessage message = null;
    String badMessageReason = null;
    int code = 0;

    try {
      if (msgBytes.m_msgBytes.length > DsConfigManager.getsipMessagePolicyMaxSize()) {
        DiscardSAEventBuilder.tooLargeSipMessageSAEventAlarm(
            DsSipTransportType.getTypeAsString(msgBytes.m_bindingInfo.getTransport()),
            msgBytes.m_bindingInfo.getRemoteAddress().toString(),
            msgBytes.m_bindingInfo.getRemotePort(),
            msgBytes.m_bindingInfo.getLocalAddress().toString(),
            msgBytes.m_bindingInfo.getLocalPort(),
            msgBytes.m_msgBytes.length);
        if (cat.isEnabled(Level.INFO)) {
          cat.log(
              Level.INFO,
              "processMessageBytes: Message is too Large. size : "
                  + msgBytes.m_msgBytes.length
                  + ", "
                  + " Dropping this SIP packet : "
                  + msgBytes.m_bindingInfo);
        }
        return;
      }

      if (DsPerf.ON) DsPerf.start(DsPerf.PARSE);
      message = DsSipMessage.createMessage(msgBytes.getMessageBytes(), true, true);
      message.setTimestamp(msgBytes.getTimestamp());
      if (DsPerf.ON) DsPerf.stop(DsPerf.PARSE);
      DsLog4j.logSessionId(message);

    } catch (DsSipParserException pe) {
      // Level 1: here, the parser itself choked on the message
      //          -- nothing we can do -- call malformed, then toss

      byte[] bytes = pe.getValue().data();
      // call malformed interface..
      if (null != malformedMessageInterface) {
        malformedMessageInterface.malformedMessage(bytes);
      }

      if (cat.isEnabled(Level.ERROR)) {
        cat.log(
            Level.ERROR,
            "processMessageBytes: parser exception, message reported, dropping.\n",
            pe);
      }
      message = null;
    } catch (DsSipKeyValidationException kve) {
      message = kve.getSipMessage();
      boolean sentResponse = false;

      if (message.isRequest()) // ignore malformed responses
      {
        DsSipRequest request = (DsSipRequest) message;
        DsSipTransactionKey key = request.forceCreateKey();

        if (key != null) {
          try {
            DsSipServerTransaction txn =
                m_transactionFactory.createServerTransaction(request, key, key, false);

            DsBindingInfo binfo = msgBytes.getBindingInfo();
            message.setBindingInfo(binfo);

            badMessageReason = kve.getMessage();
            code = DsSipResponseCode.DS_RESPONSE_BAD_REQUEST;

            sendErrorResponse(txn, request, badMessageReason, code);
            sentResponse = true;
          } catch (DsException e) {
            // ignore exception, nothing we can do, just drop the message below
          }
        }
      }

      // Level 2: here we can't build a key for a message so
      //         call malformed interface then toss it

      // call malformed interface..
      if (null != malformedMessageInterface) {
        malformedMessageInterface.malformedMessage(message);
      }
      if (cat.isEnabled(Level.WARN)) {
        if (sentResponse) {
          cat.log(
              Level.WARN,
              "processMessageBytes: Can't construct key. Sent 400 response:\n"
                  + message.maskAndWrapSIPMessageToSingleLineOutput());
        } else {
          cat.log(
              Level.WARN,
              "processMessageBytes: Can't construct key. dropping this message:\n"
                  + message.maskAndWrapSIPMessageToSingleLineOutput());
        }
        cat.log(Level.WARN, "processMessageBytes: specific exception is:\n", kve);
      }
      message = null;
    }
    //
    // Level 3: here, we can probably still do something (like: send
    //     a response)
    catch (DsSipVersionValidationException vve) {
      message = vve.getSipMessage();
      badMessageReason = vve.getMessage();
      code = DsSipResponseCode.DS_RESPONSE_SIP_VERSION_NOT_SUPPORTED;
      // call malformed interface..
      if (null != malformedMessageInterface) {
        // it's not malformed, but this is the interface we have so
        //   might as well pass it through here for the moment -dg
        malformedMessageInterface.malformedMessage(message);
      }

      if (cat.isEnabled(Level.ERROR)) {
        cat.log(Level.ERROR, "processMessageBytes: invalid version, continuing to process\n", vve);
      }
    } catch (DsSipMessageValidationException mve) {

      message = mve.getSipMessage();
      badMessageReason = mve.getMessage();
      code = DsSipResponseCode.DS_RESPONSE_BAD_REQUEST;
      // call malformed interface..
      if (null != malformedMessageInterface) {
        malformedMessageInterface.malformedMessage(message);
      }

      if (cat.isEnabled(Level.ERROR)) {
        cat.log(Level.ERROR, "processMessageBytes: invalid message, continuing to process\n", mve);
      }
    } catch (DsSipParserListenerException ple) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.log(
            Level.ERROR,
            "processMessageBytes: parser listener exception, message not available:\n",
            ple);
      }
      message = null;
    }

    // If there was an exception, it is possible that the message is null
    // Not much we can do at this point, except logging.
    if (message == null) {
      return;
    }

    DsBindingInfo bi = msgBytes.getBindingInfo();
    /*--
            // The key construction itself will take care of setting the sent-by
            // address and port number. The following code segment is not required
            // any more.

            // Update the message key with the remote source address and port
            DsSipTransactionKey key = message.getKey();
            if (null != key)
            {
                key.setSourceAddress(DsString.getHostBytes(bi.getRemoteAddress()));
                key.setSourcePort(bi.getRemotePort());
            }
    --*/
    // add the binding info to the message
    message.setBindingInfo(bi);

    if (DsPerf.ON) DsPerf.start(DsPerf.TM);
    // send the message through the transaction manager
    try {
      processMessage(message, badMessageReason, code);
    } catch (Exception exc) {
      if (cat.isEnabled(Level.ERROR)) {
        if (badMessageReason != null) {
          cat.log(Level.ERROR, badMessageReason);
        }
        cat.log(Level.ERROR, "processMessageBytes: exception in processMessage:\n", exc);
      }
    }
    if (DsPerf.ON) DsPerf.stop(DsPerf.TM);
  }

  /**
   * Deprecated - use one of the other processMessage methods.
   *
   * @param message the message to process
   * @param badMessageReason the message to use with a 400 response
   * @deprecated use processMessage(DsSipMessage, String, int)
   */
  public void processMessage(DsSipMessage message, String badMessageReason) {
    processMessage(message, badMessageReason, DsSipResponseCode.DS_RESPONSE_BAD_REQUEST);
  }

  /**
   * Process a message. This method looks up the transaction in the map using the full key (with the
   * Via). For requests it then delegates to either processCancel, processAck, processPrack, or
   * processRequest (for requests other than ACK, PRACK, and CANCEL). For responses it delegates to
   * processResponse.
   *
   * @param message the message to be processed
   * @param badMessageReason the message to use with a 400 response
   * @param code the response code to use
   */
  public void processMessage(DsSipMessage message, String badMessageReason, int code) {
    Logger cat = generalCat;
    DsSipServerTransaction transaction = null;
    // -- not used any more
    // --        boolean                 bServerTrans = false;
    DsBindingInfo binding_info = null;
    boolean Level_DEBUG = cat.isEnabled(Level.DEBUG);
    boolean Level_INFO = cat.isEnabled(Level.INFO);

    if (Level_DEBUG) {
      cat.log(
          Level.DEBUG,
          "processMessage(): -----  BEGINING PROCESSING NEW MESSAGE ------\n"
              + message.maskAndWrapSIPMessageToSingleLineOutput());
    }

    if (badMessageReason != null) {
      // CAFFEINE 2.0 DEVELOPMENT - check if warning enabled before logging messages.
      if (cat.isEnabled(Level.WARN)) {
        cat.log(
            Level.WARN,
            "processMessage(): -----  MALFORMED MESSAGE ----\n"
                + message.maskAndWrapSIPMessageToSingleLineOutput());
      }
      if (null != malformedMessageInterface) {
        malformedMessageInterface.malformedMessage(message);
      }
    }

    binding_info = message.getBindingInfo();

    if (Level_DEBUG) {
      cat.log(Level.DEBUG, "processMessage(): Incoming message binding info: " + binding_info);
      cat.log(
          Level.DEBUG,
          "processMessage(): Incoming message:\n"
              + message.maskAndWrapSIPMessageToSingleLineOutput());
    }

    if (Level_DEBUG) {
      cat.log(Level.DEBUG, "processMessage(DsSipMessage)");
    }

    int methodID = message.getCSeqType();
    // Adding SessionID header
    message.addAndUpdateSessionIdHeader();
    DsLog4j.logSessionId(message);
    if (message.isRequest()) // SIP Request
    {
      if (Level_INFO) {
        cat.log(Level.INFO, "processMessage(): Got a request from transport layer.");
      }
      addReceiveParameter(message, binding_info.getRemoteAddress());
      addRPortParameter(message, binding_info.getRemotePort());

      // For an ACK call processAck
      if (methodID == DsSipConstants.ACK) {
        DsSipAckMessage ack = (DsSipAckMessage) message;
        transaction = processAck(ack);

        // ACK retransmission
        if (transaction != null) {
          if (Level_INFO) {
            cat.log(
                Level.INFO,
                "processMessage(): calling DsSipServerTransaction.onAck for retransmitted ACK.");
          }

          if (processMaxForwards(ack, transaction.isProxyServerMode())) {
            // since the max forwards header is 0 and it is proxy server mode - we can not forward -
            // must drop this msg
            if (cat.isEnabled(Level.INFO)) {
              cat.log(Level.INFO, "processAck(): dropping ack, Max-Forwards exceeded.");
            }
          } else {
            transaction.onAck(ack);
          }
        }
      }
      // For a CANCEL call processCancel
      else if (methodID == DsSipConstants.CANCEL) {
        DsSipCancelMessage cancel = (DsSipCancelMessage) message;
        transaction = processCancel(cancel, badMessageReason, code);

        // CANCEL retransmission
        if (transaction != null) {
          if (Level_INFO) {
            cat.log(
                Level.INFO,
                "processMessage(): calling DsSipServerTransaction.onRequestRetransmission for retransmitted CANCEL.");
          }
          transaction.onRequestRetransmission(cancel);
        }
      }
      // CAFFEINE 2.0 DEVELOPMENT - PRACK required
      // For a PRACK call processPrack
      else if (methodID == DsSipConstants.PRACK && !isProxyServerMode()) {
        // PRACK is a normal message for a proxy
        DsSipPRACKMessage prack = (DsSipPRACKMessage) message;
        transaction = processPrack(prack, badMessageReason, code);

        // PRACK retransmission
        if (transaction != null) {
          if (Level_INFO) {
            cat.info(
                "processMessage(): calling DsSipServerTransaction.Prack for retransmitted PRACK.");
          }
          // "..., a UAC SHOULD NOT retransmit the PRACK request when it
          // receives a retransmission of a provisional response being
          // acknowledged, although doing so does not create a protocol error."
          // The following code calls execute(DS_ST_IN_REQUEST); In order to do
          // this, we need add input REQ for Wait_PRACK and Relproceeding states
          // on the STI_TABLE, otherwise, an exception will be thrown...
          // For now, let's ignore it.
          // transaction.onRequestRetransmission(prack);
        }
      }
      // For other requests call processRequest
      else {
        DsSipRequest request = (DsSipRequest) message;

        transaction = processRequest(request, request.getMethod(), badMessageReason, code);
        // Request retransmission
        if (transaction != null) {
          if (Level_INFO) {
            cat.log(
                Level.INFO,
                "processMessage(): calling DsSipServerTransaction.onRequestRetransmission for retransmitted request.");
          }
          transaction.onRequestRetransmission(request);
        }
      }
    } else // SIP Response
    {
      if (Level_INFO) {
        cat.log(Level.INFO, "processMessage(): Got a response from transport layer.");
      }
      processResponse((DsSipResponse) message, badMessageReason);
    }
  } // end processMessage

  /**
   * Process a response. See comments in DsSipTransactionTable for algorithm details.
   *
   * @param response the response to be processed
   */
  private void processResponse(DsSipResponse response, String badResponseReason) {
    if (DsPerf.ON) DsPerf.start(DsPerf.PROC_RESP);
    Logger cat = respCat;
    DsSipTransactionKey key = response.getKey();
    DsSipClientTransaction retrievedTransaction = null;
    boolean Level_INFO = cat.isEnabled(Level.INFO);
    boolean Level_DEBUG = cat.isEnabled(Level.DEBUG);

    if (Level_DEBUG) {
      cat.log(Level.DEBUG, "processResponse(): begin");
    }

    if (badResponseReason != null) {
      if (cat.isEnabled(Level.WARN)) {
        cat.warn("processResponse(): Received a poorly formed response: " + badResponseReason);
        cat.warn("response:\n" + response.maskAndWrapSIPMessageToSingleLineOutput());
      }
      return;
    }

    // Updating the message statistics to record the message
    // metrics, that in turn can be queried either through SNMP or
    // CLI. new response
    // 09/07/01 - ldu: shouldn't do updateStats here. trans.onResponse() does it.
    // DsMessageStatistics.updateStats(response,false,true);

    // System.out.println(response);

    DsByteString responseToTag = key.getToTag();

    if (responseToTag != null) // If response has To tag
    {
      if (DsSipConstants.BS_INVITE.equals(response.getCSeqMethod())) {
        response.setKeyContext(DsSipTransactionKey.USE_VIA | DsSipTransactionKey.USE_TO_TAG);
        try {
          // Look for client transaction with key
          retrievedTransaction = m_transactionTable.findClientTransaction(key);
        } catch (Exception e) {
          if (cat.isEnabled(Level.ERROR)) {
            cat.error(
                "processResponse(): Exception looking up transaction. Message not usable.", e);
          }
        }

        if (retrievedTransaction != null) // Client transaction found
        {
          if (Level_INFO) {
            // CAFFEINE 2.0 DEVELOPMENT - log more information
            cat.log(
                Level.INFO,
                "processResponse(1): client transaction found; calling onResponse and returning, retrievedTransaction = "
                    + retrievedTransaction);
          }
          // Passing response to client transaction
          if (DsPerf.ON) DsPerf.start(DsPerf.ON_RESP);
          retrievedTransaction.onResponse(response);
          if (DsPerf.ON) DsPerf.stop(DsPerf.ON_RESP);
        } else // Client transaction not found
        {
          // Create key without To tag
          // key = m_transactionTable.createClientTransactionKey(response,
          // false);
          // --                    response.setKeyContext(false);   // don't use tag
          response.setKeyContext(DsSipTransactionKey.USE_VIA);
          try {
            // Look for client transaction with key
            retrievedTransaction = m_transactionTable.findClientTransaction(key);
          } catch (Exception e) {
            if (cat.isEnabled(Level.ERROR)) {
              cat.error(
                  "processResponse(): Exception looking up transaction. Message not usable.", e);
            }
          }

          if (retrievedTransaction != null) // Client transaction found
          {
            if (response.getStatusCode() / 100 == 1) // If provisional response
            {
              if (Level_INFO) {
                // CAFFEINE 2.0 DEVELOPMENT - log more information
                cat.log(
                    Level.INFO,
                    "processResponse(2): client transaction found; calling onResponse and returning, retrievedTransaction = "
                        + retrievedTransaction);
              }
              // Passing response to retrieved client transaction
              if (DsPerf.ON) DsPerf.start(DsPerf.ON_RESP);
              retrievedTransaction.onResponse(response);
              if (DsPerf.ON) DsPerf.stop(DsPerf.ON_RESP);
            } else // Final response
            {
              DsByteString retrievedTransactionToTag = retrievedTransaction.getToTag();
              if (retrievedTransactionToTag == null) // If Client transaction has no To tag
              {
                // Set client transaction To tag to response To tag
                retrievedTransaction.setToTag(responseToTag);
                if (Level_INFO) {
                  // CAFFEINE 2.0 DEVELOPMENT - log more information
                  cat.log(
                      Level.INFO,
                      "processResponse(3): client transaction found; calling onResponse and returning, retrievedTransaction = "
                          + retrievedTransaction);
                }
                // Passing response to client transaction
                if (DsPerf.ON) DsPerf.start(DsPerf.ON_RESP);
                retrievedTransaction.onResponse(response);
                if (DsPerf.ON) DsPerf.stop(DsPerf.ON_RESP);
              } else // retrieved client transaction has To tag
              {
                // If retrieved client transaction To tag matches response To tag
                if (retrievedTransactionToTag.equals(responseToTag)) {
                  if (Level_INFO) {
                    // CAFFEINE 2.0 DEVELOPMENT - log more information
                    cat.log(
                        Level.INFO,
                        "processResponse(4): client transaction found; calling onResponse and returning, retrievedTransaction = "
                            + retrievedTransaction);
                  }
                  // Passing response to client transaction
                  if (DsPerf.ON) DsPerf.start(DsPerf.ON_RESP);
                  retrievedTransaction.onResponse(response);
                  if (DsPerf.ON) DsPerf.stop(DsPerf.ON_RESP);
                } else // retrieved client transaction To tag doesn't match response To tag
                {
                  // If multiple final responses are enabled for this transaction
                  if (retrievedTransaction.multipleFinalResponsesEnabled()) {
                    DsSipClientTransaction copiedTransaction;
                    try {
                      copiedTransaction = retrievedTransaction.createCopy(response);
                      copiedTransaction.setToTag(responseToTag);
                      addTransaction(copiedTransaction, true, true);
                      if (Level_INFO) {
                        cat.log(
                            Level.INFO,
                            "processResponse(): new client transaction created; calling multipleFinalResponse and returning");
                      }
                      // Passing response to client transaction
                      copiedTransaction.onMultipleFinalResponse(retrievedTransaction, response);
                    } catch (
                        DsException
                            e) // Transaction already in table - shouldn't be because we just looked
                    // for it
                    {
                      if (Level_INFO) {
                        cat.log(
                            Level.ERROR,
                            "processResponse(): client transaction already existed or couldn't create copy - returning",
                            e);
                      }
                    }
                  } else // retrieved client transaction has no multiple final response interface
                  {
                    if (Level_INFO) {
                      cat.log(
                          Level.INFO,
                          "processResponse(): Multiple final response received but not enabled. Original client transaction found; calling onResponse and returning");
                    }
                    // Passing response to client transaction
                    if (DsPerf.ON) DsPerf.start(DsPerf.ON_RESP);
                    retrievedTransaction.onResponse(response);
                    if (DsPerf.ON) DsPerf.stop(DsPerf.ON_RESP);
                  }
                }
              }
            }
          } else // Client transaction not found
          {
            passResponseToStrayMessageInterface(response);
          }
        }
      } else // non-INVITE response
      {
        // Create key with To tag
        // key = m_transactionTable.createClientTransactionKey(response,
        // true);
        // --                response.setKeyContext(true);   // use tag
        // CAFFEINE 2.0 DEVELOPMENT - required by PRACK.
        if (DsSipConstants.BS_PRACK.equals(
            response.getCSeqMethod())) { // Set the key context the sme way when the PRACK txn was
          // created.
          response.setKeyContext(DsSipTransactionKey.USE_VIA | DsSipTransactionKey.USE_METHOD);
        } else {
          response.setKeyContext(DsSipTransactionKey.USE_VIA | DsSipTransactionKey.USE_TO_TAG);
        }
        try {
          // Look for client transaction with key
          retrievedTransaction = m_transactionTable.findClientTransaction(key);
        } catch (Exception e) {
          if (cat.isEnabled(Level.ERROR)) {
            cat.warn("processResponse(): Exception looking up transaction. Message not usable.", e);
          }
        }

        if (retrievedTransaction != null) // Client transaction found
        {
          if (Level_INFO) {
            // CAFFEINE 2.0 DEVELOPMENT - log more information.
            cat.log(
                Level.INFO,
                "processResponse(5): client transaction found; calling onResponse and returning, retrievedTransaction = "
                    + retrievedTransaction);
          }
          // Passing response to client transaction
          if (DsPerf.ON) DsPerf.start(DsPerf.ON_RESP);
          retrievedTransaction.onResponse(response);
          if (DsPerf.ON) DsPerf.stop(DsPerf.ON_RESP);
        } else // Client transaction not found
        {
          // Create key without To tag
          // key = m_transactionTable.createClientTransactionKey(response,
          // false);
          // --                    response.setKeyContext(false);    // don't use tag
          // CAFFEINE 2.0 DEVELOPMENT - required by PRACK.
          if (DsSipConstants.BS_PRACK.equals(
              response.getCSeqMethod())) { // Set the key context the sme way when the PRACK txn was
            // created.
            response.setKeyContext(DsSipTransactionKey.USE_VIA | DsSipTransactionKey.USE_METHOD);
          } else {
            response.setKeyContext(DsSipTransactionKey.USE_VIA);
          }
          try {
            // Look for client transaction with key
            retrievedTransaction = m_transactionTable.findClientTransaction(key);
          } catch (Exception e) {
            if (cat.isEnabled(Level.ERROR)) {
              cat.error(
                  "processResponse(): Exception looking up transaction. Message not usable.", e);
            }
          }

          if (retrievedTransaction != null) // Client transaction found
          {
            if (Level_INFO) {
              // CAFFEINE 2.0 DEVELOPMENT - log more information.
              cat.log(
                  Level.INFO,
                  "processResponse(6): client transaction found; calling onResponse and returning, retrievedTransaction = "
                      + retrievedTransaction);
            }
            // Set To tag of transaction to response To tag
            retrievedTransaction.setToTag(responseToTag);
            // Passing response to retrieved client transaction
            if (DsPerf.ON) DsPerf.start(DsPerf.ON_RESP);
            retrievedTransaction.onResponse(response);
            if (DsPerf.ON) DsPerf.stop(DsPerf.ON_RESP);
          } else // client transaction not found
          {
            passResponseToStrayMessageInterface(response);
          }
        }
      }
    } else // response has no To tag
    {
      // Create key without To tag
      // key = m_transactionTable.createClientTransactionKey(response,
      // false);
      // --            response.setKeyContext(false);    // don't use tag
      // CAFFEINE 2.0 DEVELOPMENT - required by PRACK.
      if (DsSipConstants.BS_PRACK.equals(
          response.getCSeqMethod())) { // Set the key context the sme way when the PRACK txn was
        // created.
        response.setKeyContext(DsSipTransactionKey.USE_VIA | DsSipTransactionKey.USE_METHOD);
      } else {
        response.setKeyContext(DsSipTransactionKey.USE_VIA);
      }
      try {
        // Look for client transaction with key
        retrievedTransaction = m_transactionTable.findClientTransaction(key);
      } catch (Exception e) {
        if (cat.isEnabled(Level.ERROR)) {
          cat.error("processResponse(): Exception looking up transaction. Message not usable.", e);
        }
      }

      if (retrievedTransaction != null) // Client transaction found
      {
        if (Level_INFO) {
          // CAFFEINE 2.0 DEVELOPMENT - log more information.
          cat.log(
              Level.INFO,
              "processResponse(7): client transaction found; calling onResponse and returning, retrievedTransaction = "
                  + retrievedTransaction);
        }
        // Passing response to retrieved client transaction
        if (DsPerf.ON) DsPerf.start(DsPerf.ON_RESP);
        retrievedTransaction.onResponse(response);
        if (DsPerf.ON) DsPerf.stop(DsPerf.ON_RESP);
      } else // client transaction not found
      {
        passResponseToStrayMessageInterface(response);
      }
    }
    if (DsPerf.ON) DsPerf.stop(DsPerf.PROC_RESP);
  } // End processResponse

  private void passResponseToStrayMessageInterface(DsSipResponse response) {
    if (respCat.isEnabled(Level.INFO)) {
      respCat.log(
          Level.INFO,
          "processResponse(): Received a stray response; calling DsSipStrayMessageInterface.strayResponse (if set)");
    }

    if (m_StrayMessageInterface != null) {
      m_StrayMessageInterface.strayResponse(response);
    }
  }

  /**
   * Process a request other than ACK, PRACK, or CANCEL. See comments in DsSipTransactionTable for
   * algorithm details.
   *
   * @param request the request to process
   * @param method the method extracted by the processMessage
   * @return the transaction found with the full transaction key or null if the transaction is
   *     created and/or processed here
   */
  private DsSipServerTransaction processRequest(
      DsSipRequest request, DsByteString method, String badRequestReason, int code) {
    if (DsPerf.ON) DsPerf.start(DsPerf.PROC_REQ);
    Logger cat = reqCat;
    DsSipServerTransaction transaction = null;

    boolean Level_INFO = cat.isEnabled(Level.INFO);
    boolean Level_DEBUG = cat.isEnabled(Level.DEBUG);

    if (Level_DEBUG) {
      cat.log(Level.DEBUG, "processRequest begin");
    }

    boolean merge_detected = false, foundTransaction = true;

    try {
      transaction = m_transactionTable.findOrCreateServerTransaction(request, m_transactionFactory);
      if (transaction.isNew()) {
        foundTransaction = false;
        if (transaction.isMerged()) {
          merge_detected = true;
        } else {
          DsBindingInfo bi = request.getBindingInfo();
          if ((DsSipTransportType.UDP == bi.getTransport()) && bi.isPendingClosure()) {
            if (processRequestForListenerShutdown(request, transaction)) {
              return null;
            }
          }
        }
      }
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error(
            "processRequest(): Exception finding or creating server transaction. Message not usable.",
            e);
      }
    }

    if (foundTransaction) {
      if (Level_INFO) {
        cat.log(
            Level.INFO,
            "processRequest(): Received a request retransmission for request with method "
                + method);
      }

      // Updating the message statistics to record the message metrics,
      // that in turn can be queried either through SNMP or CLI.
      // duplicate request received.
      DsMessageStatistics.updateStats(request, true, true);
      DsMessageStatistics.logRequest(
          DsMessageLoggingInterface.REASON_RETRANSMISSION,
          DsMessageLoggingInterface.DIRECTION_IN,
          request);

      if (DsPerf.ON) DsPerf.stop(DsPerf.PROC_REQ);
      return transaction;
    }

    // Updating the message statistics to record the message metrics,
    // that in turn can be queried either through SNMP or CLI.
    // regular or merged request received.
    DsMessageStatistics.updateStats(request, merge_detected, true);
    // since running status can only be changed by configure, no synchronization here
    if (buckets != null && buckets.isRunning() && request.getMethodID() != DsSipConstants.BYE) {
      if (!buckets.isOK()) {
        // Log incoming request that is going to be dropped because of throttling
        DsMessageStatistics.logRequest(
            DsMessageLoggingInterface.REASON_REGULAR,
            DsMessageLoggingInterface.DIRECTION_IN,
            request);
        if (Level_INFO) {
          cat.log(Level.INFO, "processRequest(): sending 503 for new request b/c of throttling");
        }

        try {
          DsSipViaHeader via = request.getViaHeaderValidate();
          // don't create response bytes directly unless compression isn't being
          //    used
          if (via != null && via.getComp() == null) {
            byte[] bytes =
                DsSipResponse.createResponseBytes(
                    DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE, request, null, null);
            transaction.sendResponse(bytes, DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE);
            // Log the sent response
            DsMessageStatistics.logResponse(
                DsMessageLoggingInterface.REASON_AUTO,
                DsMessageLoggingInterface.DIRECTION_OUT,
                bytes,
                DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE,
                request.getMethodID(),
                null,
                request);
          } else {
            DsSipResponse res =
                new DsSipResponse(
                    DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE, request, null, null);
            transaction.sendResponse(res);
            // Log the sent response
            DsMessageStatistics.logResponse(
                DsMessageLoggingInterface.REASON_AUTO,
                DsMessageLoggingInterface.DIRECTION_OUT,
                res,
                request);
          }
        } catch (Exception e) {
          if (cat.isEnabled(Level.ERROR)) {
            cat.error("processRequest(): Exception sending a 503 response", e);
          }
        }

        synchronized (aLock) {
          if (flowOK == true) {
            DsConfigManager.raiseFlowExceedsThreshold(buckets.getName());
            flowOK = false;
          }
        }

        if (DsPerf.ON) DsPerf.stop(DsPerf.PROC_REQ);

        return null;
      } else {
        synchronized (aLock) {
          if (flowOK == false) {
            DsConfigManager.raiseFlowOKAgain(buckets.getName());
            flowOK = true;
          }
        }
      }
    }

    // Determine if the message received is valid.  If it isn't, handle it.
    if (badRequestReason != null) {
      // Log the received malformed request and
      // send a 400 response.
      sendErrorResponse(transaction, request, badRequestReason, code);
      if (DsPerf.ON) DsPerf.stop(DsPerf.PROC_REQ);
      return null;
    }

    if (merge_detected) {
      if (!isProxyServerMode()) {
        if (Level_INFO) {
          cat.log(
              Level.INFO,
              "processRequest(): merge detected. Sending "
                  + DsSipResponseCode.DS_RESPONSE_LOOP_DETECTED
                  + " response and returning");
        }
        try {
          DsSipResponse response =
              new DsSipResponse(DsSipResponseCode.DS_RESPONSE_LOOP_DETECTED, request, null, null);
          // always a merged request at UA level, never a loop
          response.setReasonPhrase(new DsByteString("Merged Request"));
          transaction.sendResponse(response);
          DsMessageStatistics.logRequest(
              DsMessageLoggingInterface.REASON_MERGED,
              DsMessageLoggingInterface.DIRECTION_IN,
              request);
          DsMessageStatistics.logResponse(
              DsMessageLoggingInterface.REASON_AUTO,
              DsMessageLoggingInterface.DIRECTION_OUT,
              response,
              request);
        } catch (Exception e) {
          if (cat.isEnabled(Level.ERROR)) {
            cat.error(
                "processRequest(): Exception sending a 482 (LOOP DETECTED) response to a merged request",
                e);
          }
        }
        if (DsPerf.ON) DsPerf.stop(DsPerf.PROC_REQ);
        return null;
      }
    }

    DsSipRequestInterface requestInterface;

    // Find the request interface associated with the request method and use it.
    if ((requestInterface = (DsSipRequestInterface) m_interfaceMap.get(method)) == null) {
      // The default interface will be null unless explicitly set using setRequestInterface()
      requestInterface = m_defaultInterface;
    }

    if (requestInterface == null) {
      if (Level_INFO) {
        cat.log(
            Level.INFO,
            "processRequest(): no registered request interface. Sending "
                + DsSipResponseCode.DS_RESPONSE_METHOD_NOT_ALLOWED
                + " response");
      }

      DsSipResponse response =
          new DsSipResponse(DsSipResponseCode.DS_RESPONSE_METHOD_NOT_ALLOWED, request, null, null);
      // CAFFEINE 2.0 DEVELOPMENT - required by PRACK.
      boolean addPrack = (m_100relSupport != DsSipConstants.UNSUPPORTED) ? true : false;
      addAllowHeaders(response, addPrack);
      try {
        transaction.sendResponse(response);
        DsMessageStatistics.logRequest(
            DsMessageLoggingInterface.REASON_NO_HANDLER,
            DsMessageLoggingInterface.DIRECTION_IN,
            request);
        DsMessageStatistics.logResponse(
            DsMessageLoggingInterface.REASON_AUTO,
            DsMessageLoggingInterface.DIRECTION_OUT,
            response,
            request);
      } catch (DsException dse) {
        if (cat.isEnabled(Level.ERROR))
          cat.error("processRequest(): error sending 405 (METHOD_NOT_ALLOWED) response", dse);
      } catch (IOException ioe) {
        if (cat.isEnabled(Level.ERROR))
          cat.error("processRequest(): error sending 405 (METHOD_NOT_ALLOWED) response", ioe);
      }

      if (DsPerf.ON) {
        DsPerf.stop(DsPerf.PROC_REQ);
      }
      return null;
    }

    // Log the received regular request.
    DsMessageStatistics.logRequest(
        DsMessageLoggingInterface.REASON_REGULAR, DsMessageLoggingInterface.DIRECTION_IN, request);

    DsLog4j.logSessionId(request);

    addCiscoPeerCertInfoHeader(request);

    try {
      // apply the Route header fixing trick for backward compatibility with strict routers
      if (m_routeFixInterface != null) {
        transaction.getRequest().lrFix(m_routeFixInterface);
      }

      if (isCreateCAEvents) {
        // REFACTOR
        // Need to execute pre-normalization after lrFix is done. because appParam uses that value
        // to apply normalization
        //            	AppParamsInterface appParamsInterface =
        // ParseProxyParamUtil.getAppParamsInterface(request);
        //
        //	Normalization.getInstance().doNormalization(Normalization.PRENORMALIZATION_MODULE, null,
        // new DsByteString(request.getNetwork().toString()), null, request, null,
        // appParamsInterface);
        //
        // this must be set after the pre-normalization execution
        request.setNormalizationState(SipMsgNormalizationState.PRE_NORMALIZED);

        /**
         * Log the normalized request. see SIPMessageLoggerMBeanImpl#logRequest(int, byte,
         * DsSipRequest)
         */
        DsMessageStatistics.logRequest(
            DsMessageLoggingInterface.REASON_REGULAR,
            DsMessageLoggingInterface.DIRECTION_IN,
            request);
      }

      if (processForMaintenance(transaction)) {

        if (DsPerf.ON) {
          DsPerf.stop(DsPerf.PROC_REQ);
        }
        return null;
      }

      if (request.getMethodID() == DsSipConstants.OPTIONS && processOptionsRequest(transaction)) {

        if (DsPerf.ON) {
          DsPerf.stop(DsPerf.PROC_REQ);
        }
        return null;
      }

      if (processMaxForwards(transaction, null)) {

        if (DsPerf.ON) {
          DsPerf.stop(DsPerf.PROC_REQ);
        }
        return null;
      }

      if (!isProxyServerMode()
          && request.getMethodID() == DsSipConstants.INVITE
          && DsSipServerTransactionIImpl.send100UponInvite()) {
        transaction.sendResponse(null); // send 100
      }

      cat.debug(
          "processRequest(): No auto response; calling user code's registered request interface");

      if (DsPerf.ON) {
        DsPerf.start(DsPerf.REQUEST_INTERFACE);
      }

      requestInterface.request(transaction);
      if (DsPerf.ON) {
        DsPerf.stop(DsPerf.REQUEST_INTERFACE);
      }
    } catch (DsException dse) {
      if (cat.isEnabled(Level.ERROR))
        cat.error("processRequest(): error on call to requestInterface.request(request)", dse);
    } catch (IOException ioe) {
      if (cat.isEnabled(Level.ERROR))
        cat.error("processRequest(): error on call to requestInterface.request(request)", ioe);
    }

    if (DsPerf.ON) DsPerf.stop(DsPerf.PROC_REQ);
    return null;
  } // End processRequest()

  /**
   * {@link DsSipRouteFixInterface} try to identify requestUri, <br>
   * If it matches CP ip, port & protocol, it sends 200 OK response and return true
   *
   * @param serverTransaction
   * @return
   */
  public boolean processOptionsRequest(DsSipServerTransaction serverTransaction) {
    DsSipRequest request = serverTransaction.getRequest();
    boolean responseSent = false;
    if (!serverTransaction.isProxyServerMode() || m_routeFixInterface == null) {
      return responseSent;
    }
    DsURI updatedUri = getUpdateUriTransport(request);

    if (updatedUri == null) {
      return responseSent;
    }

    boolean recognizedUri = m_routeFixInterface.recognize(updatedUri, false);
    if (!recognizedUri) {
      return responseSent;
    }
    try {
      // If possible, form a 200 Ok response and send it.
      DsSipResponse response =
          new DsSipResponse(DsSipResponseCode.DS_RESPONSE_OK, request, null, null);
      response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);

      // CAFFEINE 2.0 DEVELOPMENT - addAllowHeaders was modified with addPrack boolean.
      DsSipTransactionManager.addAllowHeaders(response, false);

      if (m_supportedHeader != null) {
        response.addHeaders(m_supportedHeader, false, false);
      }

      if (m_serverHeader != null) {
        response.addHeaders(m_serverHeader, false, false);
      }

      if (m_acceptHeader != null) {
        response.addHeaders(m_acceptHeader, false, false);
      }

      serverTransaction.sendResponse(response);
    } catch (Exception e) {
      reqCat.error(
          "Got Exception while trying to send 200 response for OPTIONS request Call-ID:"
              + request.getCallId(),
          e);
    }
    return true;
  }

  /**
   * Clone the requestUri and add transport param from BindingInfo
   *
   * @param request
   * @return
   */
  private DsSipURL getUpdateUriTransport(DsSipRequest request) {
    DsURI cloneUri = (DsURI) request.getURI().clone();

    if (!(cloneUri instanceof DsSipURL)) {
      return null;
    }
    DsSipURL sipURL = (DsSipURL) cloneUri;
    sipURL.setTransportParam(request.getBindingInfo().getTransport());
    return sipURL;
  }

  /**
   * CP checks the state (Running | Suspend).<br>
   * if its in Suspend state, sends 503 response to Client and return true;
   *
   * @param serverTransaction
   * @return
   * @throws IOException
   */
  public boolean processForMaintenance(DsSipServerTransaction serverTransaction)
      throws IOException {
    //        if(!LicenseChecker.checkSuspendState())
    //        {
    //            return false;
    //        }
    //        DsSipRequest request = serverTransaction.getRequest();
    // REFACTOR
    //        try
    //        {
    //            Map<String, String> proxyParams =
    // ParseProxyParamUtil.getParsedProxyParams(request, DsReConstants.MY_URI, false,
    // DsReConstants.DELIMITER_STR);
    //            if(proxyParams == null || !proxyParams.containsKey(DsReConstants.RR)) {
    //
    //                reqCat.error("Dropping request, CP in suspended state.");
    //
    //                DsSipResponse errorResponse =
    // DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE,
    // request);
    //                DsProxyResponseGenerator.addMaintenanceReasonHeader(errorResponse);
    //
    //                serverTransaction.sendResponse(errorResponse);
    //            }else {
    //                return false;
    //            }
    //        }
    //        catch (DsException e)
    //        {
    //            reqCat.error("Error in parsing Proxy Param, request Call-ID:"+request.getCallId(),
    // e);
    //            return false;
    //        }
    //        catch (IOException e)
    //        {
    //            reqCat.error("Got Exception while trying sendResponse() with 503 Maintenance
    // Response. request Call-ID:"+request.getCallId(), e);
    //        }
    return true;
  }

  void addCiscoPeerCertInfoHeader(DsSipRequest request) {
    Logger cat = generalCat;
    DsBindingInfo bindingInfo = request.getBindingInfo();
    boolean isMidCall = request.getToTag() != null;
    if (request.getMethodID() == DsSipConstants.INVITE
        && !isMidCall
        && bindingInfo != null
        && bindingInfo instanceof DsSSLBindingInfo) {
      boolean isPeerTrusted =
          DsTlsUtil.isPeerTrusted(((DsSSLBindingInfo) bindingInfo).getSession());
      DsNetwork network = bindingInfo.getNetwork();
      DsSipHeaderInterface certHeaderLists =
          request.getHeader(DsSipConstants.X_CISCO_PEER_CERT_INFO_STRING);
      if (certHeaderLists != null) {
        /*
          If X-Cisco-Peer-Cert-Info header is already available in incoming connection,check if domain is trusted.
          This usually happens for inter-region calls.
          Flow:
          (Enterprise)<--mTLS-->(Cluster 1 CP,add X-Cisco-Peer-Cert-Info)<----mTLS---> (Cluster 2 CP)
          If domain is not trusted, remove the header
        */
        boolean isPeerDomainTrusted = DsTlsUtil.isPeerDomainTrusted((DsSSLBindingInfo) bindingInfo);
        if (!isPeerDomainTrusted) {
          request.removeHeader(certHeaderLists);
          if (cat.isEnabled(Level.WARN)) {
            cat.log(
                Level.WARN,
                "addCiscoPeerCertInfoHeader(): Remove existing X-Cisco-Peer-Cert-Info since domain is not trusted");
          }
        }
      } else {
        /*
         Incoming connection does not have X-Cisco-Peer-Cert-Info header
         Add header with details
        */
        if (isPeerTrusted && network != null && network.getPeerCertInfoHeader()) {
          request.addPeerCertInfoHeader((DsSSLBindingInfo) bindingInfo);
        }
      }
    }
  }

  /**
   * Process a CANCEL. See comments in DsSipTransactionTable for algorithm details.
   *
   * @param request the request to process
   * @return the transaction found using the full key
   */
  private DsSipServerTransaction processCancel(
      DsSipCancelMessage request, String badRequestReason, int code) {
    Logger cat = cancelCat;
    DsSipTransactionKey keyCancel = null;
    DsSipServerTransaction transactionCancel = null; // this is the transaction to be cancelled
    DsSipServerTransaction cancelTransaction = null;

    boolean marked_cancelled = false;

    if (cat.isEnabled(Level.TRACE)) {
      cat.log(Level.TRACE, "processCancel() begin");
    }

    try {
      cancelTransaction =
          (DsSipServerTransaction)
              m_transactionTable.findOrCreateCancelTransaction(request, m_transactionFactory);
      // Handle CANCEL retransmission
      if (!cancelTransaction.isNew()) {
        // Updating the message statistics to record the message metrics,
        // that in turn can be queried either through SNMP or CLI.
        // duplicate CANCEL
        DsMessageStatistics.updateStats(request, true, true);
        DsMessageStatistics.logRequest(
            DsMessageLoggingInterface.REASON_RETRANSMISSION,
            DsMessageLoggingInterface.DIRECTION_IN,
            request);

        if (cat.isEnabled(Level.INFO)) {
          cat.log(
              Level.INFO, "processCancel(): Received a CANCEL retransmission; returning to caller");
        }
        return cancelTransaction;
      }
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error(
            "processCancel(): Exception looking up or creating CANCEL transaction. Message not usable.",
            e);
      }
    }

    // Updating the message statistics to record the message metrics,
    // that in turn can be queried either through SNMP or CLI.
    // CANCEL
    DsMessageStatistics.updateStats(request, false, true);
    DsMessageStatistics.logRequest(
        DsMessageLoggingInterface.REASON_REGULAR, DsMessageLoggingInterface.DIRECTION_IN, request);

    //
    //      -construct V-B-'N'
    //      -look in merge table
    //            -not found:
    //                -construct B-'N'
    //                -look in server table
    //                    -via parts match: found txn being cancelled
    //                    -via parts not match: stray cancel
    //            -found: found merged txn being cancelled
    //

    // Try to find the transaction being cancelled

    keyCancel = (DsSipTransactionKey) request.getKey().clone();

    try {
      // cancel lookup (first without the via: will match non-merged)
      //      -construct V-B-'N'
      // keyCancel = m_transactionTable.createCancelLookupTransactionKey(request);
      /*--
                  request.setKeyContext(true,              // use via
                                          m_proxyServerMode, // use ruri
                                          true);             // is lookup
      --*/
      keyCancel.setKeyContext(
          DsSipTransactionKey.INCOMING
              | DsSipTransactionKey.USE_VIA
              | DsSipTransactionKey.LOOKUP
              | (DsSipTransactionTable.getUseRequestURI()
                  ? DsSipTransactionKey.USE_URI
                  : DsSipTransactionKey.NONE));

      transactionCancel = m_transactionTable.findMergedTransaction(keyCancel);

      //            -not found:
      if (transactionCancel == null) {
        //                -construct B-'N'
        // keyCancel.setUseVia(false);
        //  request.setKeyContext(false,             // use via
        //                   m_proxyServerMode, // use ruri
        //                   true);             // is lookup
        keyCancel.setKeyContext(
            DsSipTransactionKey.INCOMING
                | DsSipTransactionKey.LOOKUP
                | (DsSipTransactionTable.getUseRequestURI()
                    ? DsSipTransactionKey.USE_URI
                    : DsSipTransactionKey.NONE));

        //                -look in server table
        transactionCancel =
            (DsSipServerTransaction) m_transactionTable.findServerTransaction(keyCancel);
        //                    -via parts match: found txn being cancelled
        //                    -via parts not match: stray cancel
        if ((transactionCancel != null) && !keyCancel.viaEquals(transactionCancel.getKey())) {
          transactionCancel = null;
        }
      }
      //            -found: found merged txn being cancelled
      // else {}

      if (transactionCancel != null) // if the transaction being cancelled is found
      {
        if (cat.isEnabled(Level.INFO)) {
          cat.log(Level.INFO, "processCancel(): Found transaction to cancel");
        }

        // This call will mark the transaction as cancelled if it
        //                has not yet been started. It is marked cancelled by
        //                setting the a private member equal to the server
        //                transaction of the CANCEL request.  If the transaction to
        //                be cancelled is not in its initial state, tryMarkCancelled
        //                returns false.

        marked_cancelled = transactionCancel.tryMarkCancelled(cancelTransaction);

        if (cat.isEnabled(Level.INFO)) {
          cat.log(
              Level.INFO,
              "processCancel(): transaction to be cancelled "
                  + (marked_cancelled ? "marked for cancellation" : "already started"));
        }
      }
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error("processCancel(): Error in CANCEL processing", e);
      }

      return null;
    }

    // apply the Route header fixing trick for backward
    //    compatibility with strict routers
    if (m_routeFixInterface != null) {
      try {
        request.lrFix(m_routeFixInterface);
      } catch (Exception exc) {
        if (cat.isEnabled(Level.ERROR)) {
          cat.error("Exception calling route fix interface for CANCEL", exc);
        }
      }
    }

    // Determine if the message received is invalid.  If it isn't, handle it.
    if (badRequestReason != null) {
      // send a 400 response
      sendErrorResponse(cancelTransaction, request, badRequestReason, code);
      return null;
    }

    if (transactionCancel == null) // if the transaction being cancelled is NOT found
    {
      // this method will call the stray message interface or
      // send a 481
      processStrayCancel(cancelTransaction);
      return null;
    }

    // if the transaction to be cancelled isn't null, but couldn't be marked as cancelled,
    // it means that it was started, so send an OK to the cancel and call the cancel interface
    if (!marked_cancelled) {
      if (cat.isEnabled(Level.INFO)) {
        cat.log(
            Level.INFO,
            "processCancel(): cancelling server transaction which is in progress; Sending OK to CANCEL and calling DsSipServerTransaction.onCancel");
      }
      try {
        DsSipViaHeader via = request.getViaHeaderValidate();

        if (!processMaxForwards(cancelTransaction, request)) {
          // send an OK to the CANCEL

          // don't create response bytes directly unless compression isn't being
          //    used
          if (via != null && via.getComp() == null) {
            byte[] responseBytes =
                DsSipResponse.createResponseBytes(
                    DsSipResponseCode.DS_RESPONSE_OK, (DsSipRequest) request, null, null);
            cancelTransaction.sendResponse(responseBytes, DsSipResponseCode.DS_RESPONSE_OK);
          } else {
            DsSipResponse response =
                new DsSipResponse(
                    DsSipResponseCode.DS_RESPONSE_OK, (DsSipRequest) request, null, null);
            response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
            cancelTransaction.sendResponse(response);
          }

          // CAFFEINE 2.0 DEVELOPMENT - added more notes
          // this will result in a 487 (Request Terminated) to the INVITE
          transactionCancel.onCancel(request); // which calls execute(DS_ST_IN_CANCEL);
        }
      } catch (DsException dse) {
        if (cat.isEnabled(Level.ERROR))
          cat.error("processCancel(): DsException from call to onCancel", dse);
      } catch (IOException ioe) {
        if (cat.isEnabled(Level.ERROR))
          cat.error(
              "processCancel(): IOException from call to requestInterface.request(request)", ioe);
      }
    }

    return null;
  } // End processCancel()

  /**
   * If the stray message interface is set, remove the server transaction for the CANCEL and call
   * DsSipStrayMessageInterface.strayCancel. Otherwise if configured to auto-respond to stray
   * cancels, send a 481 response else, remove the CANCEL server transaction to prevent a memory
   * leak.
   *
   * @param the server transaction for the CANCEL request
   */
  void processStrayCancel(DsSipServerTransaction transaction) {
    Logger cat = cancelCat;
    DsSipCancelMessage cancel_message = (DsSipCancelMessage) transaction.getRequest();
    if (cat.isEnabled(Level.INFO)) {
      cat.log(Level.INFO, "processStrayCancel(): stray cancel detected;");
    }
    try {
      if (m_StrayMessageInterface != null) {
        if (cat.isEnabled(Level.INFO)) {
          cat.log(
              Level.INFO,
              "processStrayCancel(): removing CANCEL server transaction and calling stray message interface");
        }
        // removeTransaction(transaction);
        // use abort() so that the Tn timer gets cleared
        transaction.abort();
        m_StrayMessageInterface.strayCancel(cancel_message);
      } else {
        if (m_autoResponseToStrayCancel) {
          // send a 481 response.
          if (cat.isEnabled(Level.INFO)) {
            cat.log(
                Level.INFO,
                "processStrayCancel(): sending "
                    + DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST
                    + "(CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST) response");
          }

          DsSipViaHeader via = cancel_message.getViaHeaderValidate();
          if (via.getComp() == null) {
            byte[] responseBytes =
                DsSipResponse.createResponseBytes(
                    DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST,
                    cancel_message,
                    null,
                    null);
            transaction.sendResponse(
                responseBytes,
                DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST);
            DsMessageStatistics.logResponse(
                DsMessageLoggingInterface.REASON_AUTO,
                DsMessageLoggingInterface.DIRECTION_OUT,
                responseBytes,
                DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST,
                DsSipConstants.CANCEL,
                null);
          } else {
            DsSipResponse response =
                new DsSipResponse(
                    DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST,
                    cancel_message,
                    null,
                    null);
            response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
            transaction.sendResponse(response);
            DsMessageStatistics.logResponse(
                DsMessageLoggingInterface.REASON_AUTO,
                DsMessageLoggingInterface.DIRECTION_OUT,
                response);
          }
        } else {
          // here, if we are configured not to respond
          // to stray CANCELs we remove the transaction
          // for the CANCEL request to prevent a leak
          // -dg
          if (cat.isEnabled(Level.INFO)) {
            cat.log(
                Level.INFO,
                "processStrayCancel(): removing CANCEL server transaction; stray cancel auto response is turned off");
          }
          //    removeTransaction(transaction);
          // use abort() so that the Tn timer gets cleared
          transaction.abort();
        }
      }
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error(
            "processStrayCancel(): Exception sending 481 (CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST) response to stray cancel",
            e);
      }
    }
  } // end processStrayCancel

  // CAFFEINE 2.0 DEVELOPMENT - required by PRACK.
  /**
   * Process a PRACK. See comments in DsSipTransactionTable for algorithm details. Cloned from
   * processCancel().
   *
   * @param request the request to process
   * @return the transaction found using the full key
   */
  private DsSipServerTransaction processPrack(
      DsSipPRACKMessage request, String badRequestReason, int code) {
    Logger cat = prackCat;
    DsSipTransactionKey keyPrack = null;
    DsSipServerTransaction transactionToBePracked = null; // this is the transaction to be Pracked
    DsSipServerTransaction prackTransaction = null;

    // boolean marked_pracked = false;

    if (cat.isEnabled(Level.TRACE)) {
      cat.log(Level.TRACE, "processPrack() begin");
    }

    try {
      prackTransaction =
          (DsSipServerTransaction)
              m_transactionTable.findOrCreatePrackTransaction(request, m_transactionFactory);
      // Handle PRACK retransmission
      if (!prackTransaction.isNew()) {
        // Updating the message statistics to record the message metrics,
        // that in turn can be queried either through SNMP or CLI.
        // duplicate PRACK
        // For now, the re-transmit will be ignored.
        DsMessageStatistics.updateStats(request, true, true);
        DsMessageStatistics.logRequest(
            DsMessageLoggingInterface.REASON_RETRANSMISSION,
            DsMessageLoggingInterface.DIRECTION_IN,
            request);

        if (cat.isEnabled(Level.INFO)) {
          cat.info("processPrack(): Received a PRACK retransmission; returning to caller");
        }
        return prackTransaction;
      }
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error(
            "processPrack(): Exception looking up or creating PRACK transaction. Message not usable.",
            e);
      }
    }

    // Updating the message statistics to record the message metrics,
    // that in turn can be queried either through SNMP or CLI.
    // PRACK
    DsMessageStatistics.updateStats(request, false, true);
    DsMessageStatistics.logRequest(
        DsMessageLoggingInterface.REASON_REGULAR, DsMessageLoggingInterface.DIRECTION_IN, request);

    //
    //      -construct V-B-'N'
    //      -look in merge table
    //            -not found:
    //                -construct B-'N'
    //                -look in server table
    //                    -via parts match: found txn being pracked
    //                    -via parts not match: stray prack
    //            -found: found merged txn being pracked
    //

    // Try to find the transaction being pracked, that is, the associated INVITE txn

    keyPrack = (DsSipTransactionKey) request.getKey().clone();
    try {
      // lookup (first without the via: will match non-merged)
      //      -construct V-B-'N'
      keyPrack.setKeyContext(
          DsSipTransactionKey.INCOMING
              | DsSipTransactionKey.USE_VIA
              | DsSipTransactionKey.LOOKUP
              | (DsSipTransactionTable.getUseRequestURI()
                  ? DsSipTransactionKey.USE_URI
                  : DsSipTransactionKey.NONE));

      transactionToBePracked = m_transactionTable.findMergedTransaction(keyPrack);

      //            -not found:
      if (transactionToBePracked == null) {
        //                -construct B-'N'
        keyPrack.setKeyContext(
            DsSipTransactionKey.INCOMING
                | DsSipTransactionKey.LOOKUP
                | (DsSipTransactionTable.getUseRequestURI()
                    ? DsSipTransactionKey.USE_URI
                    : DsSipTransactionKey.NONE));

        //                -look in server table
        transactionToBePracked =
            (DsSipServerTransaction) m_transactionTable.findServerTransaction(keyPrack);

        //                    -via parts match: found txn being pracked
        //                    -via parts not match: stray prack
        if ((transactionToBePracked != null)
            && !keyPrack.viaEquals(transactionToBePracked.getKey())) {
          transactionToBePracked = null;
        }
        if (transactionToBePracked == null) {
          if (!m_dialogMap.isEmpty()) {
            try {
              DsSipDialogID aDialogID = constructDialogID(request);
              transactionToBePracked = (DsSipServerTransaction) m_dialogMap.get(aDialogID);
            } catch (DsException e) {
              if (cat.isEnabled(Level.ERROR)) {
                cat.error("processPrack(): ", e);
              }
              transactionToBePracked = null;
            }

            if (transactionToBePracked != null) {
              if (cat.isEnabled(Level.INFO)) {
                cat.info("processPrack(): found transaction being PRACK'ed by dialogID ");
              }
            }
          }
        }
      }
      //            -found: found merged txn being pracked
      // else {}

      if (transactionToBePracked != null) // if the transaction being pracked is found
      {
        if (cat.isEnabled(Level.INFO)) {
          cat.log(
              Level.INFO, "processPrack(): Found transaction to prack" + transactionToBePracked);
        }
      }
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error("processPrack(): Error in PRACK processing", e);
      }

      return null;
    }

    // Determine if the message received is invalid.  If it isn't, handle it.
    if (badRequestReason != null) {
      // send a 400 response
      sendErrorResponse(prackTransaction, request, badRequestReason, code);
      return null;
    }

    if (transactionToBePracked == null) // if the transaction being pracked is NOT found
    {
      // this method will call the stray message interface or
      // send a 481
      processStrayPrack(prackTransaction);
      return null;
    }

    if (transactionToBePracked.get100relSupport() == DsSipConstants.UNSUPPORTED) {
      if (cat.isEnabled(Level.WARN)) {
        cat.warn("processPrack(): 100rel is NOT supported, No PRACK is accepted.");
      }

      DsSipResponse response =
          new DsSipResponse(DsSipResponseCode.DS_RESPONSE_METHOD_NOT_ALLOWED, request, null, null);
      response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
      addAllowHeaders(response, false);
      try {
        transactionToBePracked.sendResponse(response);
        DsMessageStatistics.logRequest(
            DsMessageLoggingInterface.REASON_NO_HANDLER,
            DsMessageLoggingInterface.DIRECTION_IN,
            request);
        DsMessageStatistics.logResponse(
            DsMessageLoggingInterface.REASON_AUTO,
            DsMessageLoggingInterface.DIRECTION_OUT,
            response,
            request);
      } catch (DsException dse) {
        if (cat.isEnabled(Level.ERROR)) {
          cat.error("processPrack(): error sending 405 (METHOD_NOT_ALLOWED) response", dse);
        }
      } catch (IOException ioe) {
        if (cat.isEnabled(Level.ERROR)) {
          cat.error("processPrack(): error sending 405 (METHOD_NOT_ALLOWED) response", ioe);
        }
      }
      return null;
    }

    try {
      transactionToBePracked.onPrack(prackTransaction, request);
      // which calls execute(DS_ST_IN_PRACK), changing the state of
      // the INVITE Server txn from Wait_PRACK to Relproceeding
      // and reliable_proceeding() logic will be executed as a result, which
      // will push the PRACK to user code (callback()).
    } catch (DsException dse) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error("processPrack(): DsException from call to onPrack", dse);
      }
    }

    return null;
  } // End processPrack()

  /**
   * If the stray message interface is set, remove the server transaction for the PRACK and call
   * DsSipStrayMessageInterface.strayPrack. Otherwise if configured to auto-respond to stray
   * cancels, send a 481 response else, remove the PRACK server transaction to prevent a memory
   * leak.
   *
   * @param the server transaction for the PRACK request
   */
  void processStrayPrack(DsSipServerTransaction transaction) {
    Logger cat = prackCat;
    DsSipPRACKMessage prack_message = (DsSipPRACKMessage) transaction.getRequest();
    if (cat.isEnabled(Level.INFO)) {
      cat.info("processStrayPrack(): stray prack detected;");
    }
    try {
      /* "If a PRACK request is received by the UA core that does not match
       * any unacknowledged reliable provisional response, the UAS MUST respond
       * to the PRACK with a 481 response."
       */
      if (cat.isEnabled(Level.INFO)) {
        cat.log(
            Level.INFO,
            "processStrayPrack(): sending "
                + DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST
                + "(CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST) response");
      }

      DsSipViaHeader via = prack_message.getViaHeaderValidate();
      if (via.getComp() == null) {
        byte[] responseBytes =
            DsSipResponse.createResponseBytes(
                DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST,
                prack_message,
                null,
                null);
        transaction.sendResponse(
            responseBytes, DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST);
        DsMessageStatistics.logResponse(
            DsMessageLoggingInterface.REASON_AUTO,
            DsMessageLoggingInterface.DIRECTION_OUT,
            responseBytes,
            DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST,
            DsSipConstants.PRACK,
            null);
      } else {
        DsSipResponse response =
            new DsSipResponse(
                DsSipResponseCode.DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST,
                prack_message,
                null,
                null);
        response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
        transaction.sendResponse(response);
        DsMessageStatistics.logResponse(
            DsMessageLoggingInterface.REASON_AUTO,
            DsMessageLoggingInterface.DIRECTION_OUT,
            response);
      }

      if (m_StrayMessageInterface != null) {
        if (cat.isEnabled(Level.INFO)) {
          cat.info(
              "processStrayPrack(): removing PRACK server transaction and calling stray message interface");
        }
        // removeTransaction(transaction);
        // use abort() so that the Tn timer gets cleared
        transaction.abort();
        m_StrayMessageInterface.strayPrack(prack_message);
      } else {
        // here, if we are configured not to respond
        // to stray PRACK we remove the transaction
        // for the PRACK request to prevent a leak
        if (cat.isEnabled(Level.INFO)) {
          cat.info(
              "processStrayPrack(): removing PRACK server transaction; stray prack auto response is turned off");
        }
        // removeTransaction(transaction);
        // use abort() so that the Tn timer gets cleared
        transaction.abort();
      }
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error(
            "processStrayPrack(): Exception sending 481 (CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST) response to stray prack",
            e);
      }
    }
  } // end processStrayPrack

  /**
   * Process a ACK. See comments in DsSipTransactionTable for algorithm details.
   *
   * @param request the request to process
   */
  private DsSipServerTransaction processAck(DsSipAckMessage request) {
    if (DsPerf.ON) DsPerf.start(DsPerf.PROC_ACK);
    Logger cat = ackCat;
    DsSipServerTransaction transaction = null;
    DsSipTransactionKey transactionKey = null;

    if (cat.isEnabled(Level.TRACE)) {
      cat.log(Level.TRACE, "processAck() begin");
    }

    //      -construct V-B-'N'
    //      -look in merge table
    //            -not found:
    //                -construct B-'N'
    //                -look in server table
    //                    -not found: stray ACK
    //                    -found: ACK for non merged request
    //            -found: ACK for merged request

    // --        try
    // --        {
    //      -construct V-B-'N'
    // transactionKey = m_transactionTable.createAckLookupTransactionKey(request);
    transactionKey = request.getKey();
    /*--
                request.setKeyContext(true,              // use via
                                             m_proxyServerMode, // use ruri
                                             true);             // is lookup
    --*/
    transactionKey.setKeyContext(
        DsSipTransactionKey.INCOMING
            | DsSipTransactionKey.USE_VIA
            | DsSipTransactionKey.LOOKUP
            | (DsSipTransactionTable.getUseRequestURI()
                ? DsSipTransactionKey.USE_URI
                : DsSipTransactionKey.NONE));

    // --        } catch (Exception e){}

    try {
      //      -look in merge table
      transaction = m_transactionTable.findMergedTransaction(transactionKey);
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error(
            "processAck(): Exception looking up transaction with via. Message not usable.", e);
      }
    }

    if (transaction != null) {
      if (cat.isEnabled(Level.INFO)) {
        cat.log(
            Level.INFO,
            "processAck(): found transaction being ACK'ed: matched a merged transaction");
      }
      // Log incoming ACK request.
      DsMessageStatistics.logRequest(
          DsMessageLoggingInterface.REASON_REGULAR,
          DsMessageLoggingInterface.DIRECTION_IN,
          request);
      if (DsPerf.ON) DsPerf.stop(DsPerf.PROC_ACK);
      return transaction;
    }

    //            -not found:
    //                -construct B-'N'
    // transactionKey.setUseVia(false);
    /*--
            request.setKeyContext(false,              // use via
                                         m_proxyServerMode,  // use uri
                                         true);              // is lookup
    --*/
    transactionKey.setKeyContext(
        DsSipTransactionKey.INCOMING
            | DsSipTransactionKey.LOOKUP
            | (DsSipTransactionTable.getUseRequestURI()
                ? DsSipTransactionKey.USE_URI
                : DsSipTransactionKey.NONE));

    try {
      //                -look in server table
      transaction =
          (DsSipServerTransaction) m_transactionTable.findServerTransaction(transactionKey);
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error(
            "processAck(): Exception looking up transaction with via. Message not usable.", e);
      }
    }

    // IS: Eventually we need to check that this is an ACK for a 200 OK
    //    response here. If it's not, we have to make sure the via matches.

    //                    -found: ACK for non merged request
    if (transaction != null) {
      if (cat.isEnabled(Level.INFO)) {
        cat.log(Level.INFO, "processAck(): found transaction being ACK'ed by transKey");
      }
      // Log incoming ACK request.
      DsMessageStatistics.logRequest(
          DsMessageLoggingInterface.REASON_REGULAR,
          DsMessageLoggingInterface.DIRECTION_IN,
          request);
      if (DsPerf.ON) DsPerf.stop(DsPerf.PROC_ACK);
      return transaction;
    }

    if (!m_dialogMap.isEmpty() && !m_proxyServerMode) // endpoint or edge proxy
    {
      DsSipServerTransaction aServerTxn = null;
      try {
        DsSipDialogID aDialogID = constructDialogID(request);
        aServerTxn = (DsSipServerTransaction) m_dialogMap.get(aDialogID);
      } catch (DsException e) {
        if (cat.isEnabled(Level.ERROR)) {
          cat.log(Level.ERROR, "processAck(): ", e);
        }
        aServerTxn = null;
      }

      if (aServerTxn != null) {
        if (cat.isEnabled(Level.INFO)) {
          cat.log(Level.INFO, "processAck(): found transaction being ACK'ed by dialogID ");
        }
        // Log incoming ACK request.
        DsMessageStatistics.logRequest(
            DsMessageLoggingInterface.REASON_REGULAR,
            DsMessageLoggingInterface.DIRECTION_IN,
            request);
        return aServerTxn;
      }
    }

    if (cat.isEnabled(Level.INFO)) {
      cat.log(Level.INFO, "processAck(): Received a Stray ACK.  Calling strayAck() interface.");
    }

    // Updating the message statistics to record the message metrics,
    // that in turn can be queried either through SNMP or CLI.
    // stray ACK as no corresponding transaction exists.
    DsMessageStatistics.updateStats(request, false, true);
    DsMessageStatistics.logRequest(
        DsMessageLoggingInterface.REASON_STRAY, DsMessageLoggingInterface.DIRECTION_IN, request);

    if (operationalState == OperationalState.SHUTDOWN) {
      // since we are in shutdown mode, there is no need to pass stray ACK
      // to the application. Just dump it here. CR #7853.
      if (cat.isEnabled(Level.INFO)) {
        cat.log(Level.INFO, "processAck(): dropping stray ACK, while in shutdown mode.");
      }
      return null;
    }

    if (m_StrayMessageInterface != null) {
      if (m_routeFixInterface != null) {
        try {
          request.lrFix(m_routeFixInterface);
        } catch (Exception exc) {
          if (cat.isEnabled(Level.ERROR)) {
            cat.error("Exception calling route fix interface for ACK", exc);
          }
        }
      }

      if (processMaxForwards(request, isProxyServerMode())) {
        // since the max forwards header is 0 and it is proxy server mode - we can not forward -
        // must drop this msg
        if (cat.isEnabled(Level.INFO)) {
          cat.log(Level.INFO, "processAck(): dropping stray ack, Max-Forwards exceeded.");
        }
      } else {
        m_StrayMessageInterface.strayAck(request);
      }
    }

    if (DsPerf.ON) DsPerf.stop(DsPerf.PROC_ACK);
    return null;
  } // End processAck()

  /**
   * Send a 400 response to a request.
   *
   * @param transaction the server transaction
   * @param request the request
   * @param reason the reason phrase for the response
   */
  private void sendErrorResponse(
      DsSipServerTransaction transaction, DsSipRequest request, String reason, int code) {
    Logger cat = reqCat;
    if (cat.isEnabled(Level.INFO)) {
      cat.log(Level.INFO, "sendErrorResponse(): invalid request. Sending " + code + " response");
    }
    try {
      // If possible, form 400 Bad REQUEST response and send it.
      DsSipResponse response = new DsSipResponse(code, request, null, null);
      response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
      // response.setReasonPhrase(new DsByteString(reason));

      // is this content-type ok? -dg
      response.setBody(new DsByteString(reason), new DsByteString("text/plain"));
      transaction.sendResponse(response);

      DsMessageStatistics.logRequest(
          DsMessageLoggingInterface.REASON_MALFORMED,
          DsMessageLoggingInterface.DIRECTION_IN,
          request);
      DsMessageStatistics.logResponse(
          DsMessageLoggingInterface.REASON_AUTO,
          DsMessageLoggingInterface.DIRECTION_OUT,
          response,
          request);
    } catch (Exception e) {
      if (cat.isEnabled(Level.ERROR)) {
        cat.error("sendErrorResponse(): Exception sending " + code + " response", e);
      }
    }
  }

  /**
   * Validates and decrements Max-Forwards for an ACK request. This ACK may be associated with a
   * transaction or a stray.
   *
   * @param ack the ACK message to be validated
   * @param isProxyServerMode true if we are acting as a proxy
   * @return boolean true if max forwards is == 0 and is proxyServerMode is true
   */
  private boolean processMaxForwards(DsSipAckMessage ack, boolean isProxyServerMode) {
    Logger cat = reqCat;

    if (ack == null) {
      return false;
    }

    DsSipMaxForwardsHeader maxForwardsHeader = null;
    try {
      maxForwardsHeader =
          (DsSipMaxForwardsHeader) ack.getHeaderValidate(DsSipConstants.MAX_FORWARDS);
    } catch (Exception exc) {
      return false;
    }

    if (maxForwardsHeader == null) {
      return false;
    }

    int maxForwards = maxForwardsHeader.getMaxForwards();

    if (isProxyServerMode && maxForwards == 0) {
      return true;
    }

    // It is now the UA's responsibility to decrement Max-Forwards - we do this regardless of the
    // proxy
    // server mode setting.
    if (shouldDecrementMaxForwards) {
      --maxForwards;
      maxForwardsHeader.setMaxForwards(maxForwards);
    }

    if (cat.isEnabled(Level.DEBUG)) {
      cat.log(Level.DEBUG, "processMaxForwards(): decremented Max-Forwards");
    }

    return false;
  }

  /**
   * Tries to send an automatic response. Returns true if an automatic response has been sent.<br>
   * if Max-Forward==0 it sends 483 response
   *
   * @param serverTransaction The server transaction that contains the request that just came in.
   * @return boolean true if an automatic response has been sent.
   */
  protected boolean processMaxForwards(
      DsSipServerTransaction transactionWithVia, DsSipCancelMessage cxlMsg) {
    Logger cat = reqCat;
    // Check to see if Max-Forwards equals 0 and we are in proxy mode
    // If so, then the only choice is to respond with too many hops

    if (transactionWithVia == null) {
      return false;
    }

    DsSipRequest request = transactionWithVia.getRequest();
    if (cxlMsg != null) {
      request = cxlMsg;
    }

    DsSipMaxForwardsHeader maxForwardsHeader = null;
    try {
      maxForwardsHeader =
          (DsSipMaxForwardsHeader) request.getHeaderValidate(DsSipConstants.MAX_FORWARDS);
    } catch (Exception exc) {
      return false;
    }

    if (maxForwardsHeader == null) {
      return false;
    }

    int maxForwards = maxForwardsHeader.getMaxForwards();

    if (transactionWithVia.isProxyServerMode()) {
      // user recognizes the rURI?
      if (maxForwards == 0 && request.getMethodID() != DsSipConstants.REGISTER) {
        try {
          // If possible, form a 483 Too Many Hops response and send it.
          DsSipResponse response =
              new DsSipResponse(DsSipResponseCode.DS_RESPONSE_TOO_MANY_HOPS, request, null, null);

          // CAFFEINE 2.0 DEVELOPMENT - addAllowHeaders was modified with addPrack boolean.
          // CAFFEINE 2.0 DEVELOPMENT - required by handling 100 rel.
          boolean addPrack = (m_100relSupport != DsSipConstants.UNSUPPORTED) ? true : false;
          DsSipTransactionManager.addAllowHeaders(response, addPrack);

          if (m_supportedHeader != null) {
            response.addHeaders(m_supportedHeader, false, false);
          }

          if (m_serverHeader != null) {
            response.addHeaders(m_serverHeader, false, false);
          }

          if (m_acceptHeader != null) {
            response.addHeaders(m_acceptHeader, false, false);
          }

          transactionWithVia.sendResponse(response);
          DsMessageStatistics.logRequest(
              DsMessageLoggingInterface.REASON_MAXHOPS,
              DsMessageLoggingInterface.DIRECTION_IN,
              request);
          DsMessageStatistics.logResponse(
              DsMessageLoggingInterface.REASON_AUTO,
              DsMessageLoggingInterface.DIRECTION_OUT,
              response,
              request);
        } catch (Exception e) {
          if (cat.isEnabled(Level.ERROR)) {
            cat.error(
                "processMaxForwards(): Got Exception while trying sendResponse() with 483 Response. ",
                e);
          }
        }

        return true;
      }
    }

    // It is now the UA's responsibility to decrement Max-Forwards - we do this regardless of the
    // proxy
    // server mode setting.
    if (shouldDecrementMaxForwards) {
      if (maxForwards != 0) {
        --maxForwards;
      }
      maxForwardsHeader.setMaxForwards(maxForwards);
    }

    if (cat.isEnabled(Level.DEBUG)) {
      cat.log(Level.DEBUG, "processMaxForwards(): decremented Max-Forwards to: " + maxForwards);
    }

    return false;
  }

  // /////////////////////////////////////////////////
  // Protected methods

  /**
   * Set a logging interface.
   *
   * @param loggingInterface implementation of DsMessageLoggingInterface
   * @see DsMessageLoggingInterface
   * @deprecated Use {@link
   *     DsMessageStatistics#setMessageLoggingInterface(DsMessageLoggingInterface)}
   */
  public void setMessageLoggingInterface(DsMessageLoggingInterface loggingInterface) {}

  /**
   * Get the message logger.
   *
   * @return the DsSipMessageLogger
   * @see DsSipMessageLogger
   * @deprecated No more supported. Refer {@link
   *     DsMessageStatistics#setMessageLoggingInterface(DsMessageLoggingInterface)}
   */
  public DsSipMessageLogger getMessageLogger() {
    return null;
  }

  /**
   * Set the Stray Message interface.
   *
   * @param strayInterface Implementation of stray message interface in which to notify of stray
   *     messages.
   */
  public void setStrayMessageInterface(DsSipStrayMessageInterface strayInterface) {
    m_StrayMessageInterface = strayInterface;
  }

  /**
   * Set the DsSipTransactionEventInterface.
   *
   * @param event_interface the transaction event interface
   */
  public void setTransactionEventInterface(DsSipTransactionEventInterface event_interface) {
    m_eventInterface = event_interface;
  }

  /**
   * Get the Stray Message interface that was set.
   *
   * @return strayInterface DsSipStrayMessageInterface that was set.
   */
  public DsSipStrayMessageInterface getStrayMessageInterface() {
    return m_StrayMessageInterface;
  }

  /**
   * Find the server transaction identified by 'key'. This method had package scope before and is
   * changed to public to support serialization. It could be changed back to more restricted scope.
   * So try to avoid using it in your code.
   *
   * @param key The transaction key of the server transaction
   * @return The server transaction found
   */
  public DsSipServerTransaction findServerTransaction(DsSipMessage key) {
    return m_transactionTable.findServerTransaction(key.getKey());
  }

  /**
   * Find the client transaction identified by 'key'. This method had package scope before and is
   * changed to public to support serialization. It could be changed back to more restricted scope.
   * So try to avoid using it in your code.
   *
   * @param key The transaction key of the client transaction
   * @return The client transaction found
   */
  public DsSipClientTransaction findClientTransaction(DsSipMessage key) {
    return m_transactionTable.findClientTransaction(key.getKey());
  }

  /**
   * Replace a client transaction.
   *
   * @param transaction the new transaction
   */
  protected static synchronized void replaceClientTransaction(DsSipClientTransaction transaction) {
    m_transactionTable.replaceClientTransaction(transaction);
  }

  /**
   * Replace a key in the client transaction map. The old key is replaced with the new key gotten
   * from the transaction.
   *
   * @param transaction the transaction whose key is being replaced
   * @param old_key the old key
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected static synchronized void replaceClientKey(
      DsSipTransaction transaction, DsSipTransactionKey old_key) throws DsException {
    m_transactionTable.remapClientTransaction(old_key);
  }

  /**
   * Try to find a server transaction for the given request.
   *
   * @param request the SIP request for which a server transaction is being sought
   * @return the server transaction found by constructing a full key (with Via) or null if no server
   *     transaction exists for the given request or the key could not be constructed
   */
  public DsSipServerTransaction findServerTransaction(DsSipRequest request) {
    if (request == null) return null;

    Logger cat = generalCat;

    DsSipServerTransaction transaction = null;
    try {
      transaction = m_transactionTable.findServerTransaction(request);
    } catch (DsException dse) {
      if (cat.isEnabled(Level.WARN)) {
        cat.warn("findServerTransaction(): exception finding server transaction", dse);
      }
    }
    return transaction;
  }

  /**
   * Adds the specified transaction into the appropriate map. This method had protected scope before
   * and is changed to public to support serialization. It could be changed back to more restricted
   * scope. So try to avoid using it in your code.
   *
   * @param transaction the transaction to be added
   * @param isClient if its client transaction
   * @param useVia whether use VIA header
   * @throws DsException if the transaction is already in the table
   */
  public static void addTransaction(DsSipTransaction transaction, boolean isClient, boolean useVia)
      throws DsException {
    if (isClient) {
      m_transactionTable.addClientTransaction((DsSipClientTransaction) transaction);
    } else {
      m_transactionTable.addServerTransaction((DsSipServerTransaction) transaction);
    }
  }

  /**
   * Adds the specified transaction into the appropriate map. This method had protected scope before
   * and is changed to public to support serialization. It could be changed back to more restricted
   * scope. So try to avoid using it in your code.
   *
   * @param transaction the transaction to be added
   * @param isClient if its client transaction
   * @param useVia whether use VIA header
   * @param toTag whether to use To tag
   * @throws DsException if the transaction is already in the table
   */
  public static void addTransaction(
      DsSipTransaction transaction, boolean isClient, boolean useVia, boolean toTag)
      throws DsException {
    if (isClient) {
      m_transactionTable.addClientTransaction((DsSipClientTransaction) transaction);
    } else {
      m_transactionTable.addServerTransaction((DsSipServerTransaction) transaction);
    }
  }

  /**
   * Remove the client transaction specified by 'key'.
   *
   * @param key the key of the client transaction to remove
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected void removeClientTransaction(DsSipMessage key) throws DsException {
    m_transactionTable.removeClientTransaction(key.getKey());
  }

  /**
   * The removeTransaction() method removes a transaction from the appropriate map. This method had
   * protected scope before and is changed to public to support serialization. It could be changed
   * back to more restricted scope. So try to avoid using it in your code.
   *
   * @param transaction to remove
   * @throws DsException if there is no transaction to remove
   */
  public static void removeTransaction(DsSipTransaction transaction) throws DsException {
    Logger cat = generalCat;
    boolean transactionRemoved = false;

    if (cat.isEnabled(Level.DEBUG)) cat.log(Level.DEBUG, "removeTransaction");

    if (transaction.isServerTransaction()) {
      try {
        ((DsSipServerTransactionImpl) transaction).removeSession();

      } catch (Exception e) {
        cat.error("Exception in Removing Session" + e);
      }
      if (cat.isEnabled(Level.INFO))
        // CAFFEINE 2.0 DEVELOPMENT - log more information.
        cat.log(
            Level.INFO, "removeTransaction: trying to remove server transaction: " + transaction);

      if (m_transactionTable.removeServerTransaction((DsSipServerTransaction) transaction)) {
        transactionRemoved = true;
      }

      if ((m_eventInterface != null) && (transactionRemoved)) {
        try {
          m_eventInterface.transactionTerminated(transaction);
        } catch (Exception e) {
          if (cat.isEnabled(Level.WARN)) {
            cat.warn(
                "removeTransaction(): error notifying user code of transaction termination", e);
          }
        }
      }

    } else // removing client transaction
    {
      if (cat.isEnabled(Level.INFO))
        // CAFFEINE 2.0 DEVELOPMENT - log more info.
        cat.log(
            Level.INFO,
            "removeTransaction: trying to remove client transaction: "
                + transaction
                + "KEY = "
                + transaction.getKey()
                + " From m_clientTransactionMap");
      if (m_transactionTable.removeClientTransaction((DsSipClientTransaction) transaction)) {
        transactionRemoved = true;
      }
      if ((m_eventInterface != null) && transactionRemoved) {
        try {
          m_eventInterface.transactionTerminated(transaction);
        } catch (Exception e) {
          if (cat.isEnabled(Level.WARN)) {
            cat.warn(
                "removeTransaction(): error notifying user code of transaction termination", e);
          }
        }
      }
    }

    if (!transactionRemoved) {
      throw new DsException(
          "Transaction does not exist to remove.  KEY = <"
              + transaction.getKey()
              + "> transaction = "
              + transaction);
    } else {
      if (m_transactionRemovalCb != null) {
        m_transactionRemovalCb.transactionRemoved(transaction);
      }
    }
  }

  // currently this method is only used by INVITE_SERVER txn. 3/26/02
  static void addToDialogMap(DsSipTransaction txn) throws DsException {
    DsSipDialogID dialogID = ((DsSipServerTransactionIImpl) txn).getDialogID();
    // dialogID is guaranteed not to be null
    synchronized (m_dialogMap) {
      m_dialogMap.put(dialogID, txn);
    }
    if (generalCat.isEnabled(Level.INFO)) generalCat.log(Level.INFO, "added dialogID " + dialogID);
  }

  // currently this method is only used by INVITE_SERVER txn. 3/26/02
  static boolean removeFromDialogMap(DsSipDialogID dialogID) throws DsException {
    Object obj = null;
    // dialogID is guaranteed not to be null
    synchronized (m_dialogMap) {
      obj = m_dialogMap.remove(dialogID);
    }
    if (generalCat.isEnabled(Level.INFO))
      generalCat.log(Level.INFO, "removed dialogID " + dialogID);
    return (obj != null);
  }

  static DsSipDialogID constructDialogID(DsSipMessage msg) throws DsException {
    if (msg == null)
      throw new DsException("Error in constructDialogID(). The DsSipResponse obj is null.");

    return new DsSipDialogID(
        msg.getCallId(), msg.getToHeaderValidate().getTag(), msg.getFromHeaderValidate().getTag());
  }

  // qfang - 03.29.2006 - CSCsd80019 make the accessor public in case two
  // UAs share the same stack instance both can access the tranport layer
  // to for example add listening port without one having to depend on
  // the other who originally created the transport layer thus has the
  // reference.
  /**
   * Returns a reference to the singleton transport layer.
   *
   * @return a reference to the singleton transport layer.
   */
  public static DsSipTransportLayer getTransportLayer() {
    return m_transportLayer;
  }

  /**
   * Returns an existing connection associated with the message parameter. The application calls
   * this method to get the connection and to set the connection persistence type.
   *
   * @param message the SIP message to use to find the connection
   * @return the connection for the binding information in <code>message</code>.
   * @throws IllegalArgumentException if the connection table does not have an entry corresponding
   *     to <code>message</code>
   */
  public final DsConnection findConnection(DsSipMessage message) {
    DsBindingInfo bindingInfo = message.getBindingInfo();
    return findConnection(bindingInfo);
  }

  /**
   * Returns an connection associated with the request parameter. The application calls this method
   * to get the connection and to set the connection persistence type.
   *
   * @param request the SIP message to use to find the connection
   * @return the connection for the binding information in <code>request</code>.
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException if transport protocol not supported for this element
   */
  public final DsConnection persistRequestConnection(DsSipRequest request)
      throws SocketException, DsException, UnknownHostException, IOException {
    DsSipResponse resp = new DsSipResponse(DsSipResponseCode.DS_RESPONSE_OK, request, null, null);
    return getConnection(resp);
  }

  /**
   * Returns an existing connection associated with the binding info parameter. The application
   * calls this method to get the connection.
   *
   * @param bindingInfo Binding Information
   * @return the connection for the binding information in <code>bindinginfo</code>.
   */
  public final DsConnection findConnection(DsBindingInfo bindingInfo) {
    Logger cat = generalCat;
    if (null == bindingInfo) {
      if (cat.isEnabled(Level.DEBUG)) {
        cat.log(Level.DEBUG, "Connection Persistence findConnection() bindingInfo is NULL");
      }
      return null;
    }

    if (cat.isEnabled(Level.DEBUG)) {
      cat.log(Level.DEBUG, "Connection Persistence findConnection() bindingInfo = " + bindingInfo);
    }

    DsConnection conn = (DsConnection) m_transportLayer.findConnection(bindingInfo);

    if (null == conn && cat.isEnabled(Level.DEBUG)) {
      cat.log(
          Level.DEBUG,
          "Connection Persistence findConnection(): Could not derive connection from message argument");
    }

    return conn;
  }

  /**
   * This static method allows the user to set default Server header for automated responses.
   *
   * @param header The Server header to become the default Server header for automated responses
   */
  public static synchronized void setServerHeader(DsSipHeaderList header) {
    m_serverHeader = header;
  }

  /**
   * This static method allows the user to set default Accept header for automated responses.
   *
   * @param header The Accept header to become the default Accept header for automated responses
   */
  public static synchronized void setAcceptHeader(DsSipHeaderList header) {
    m_acceptHeader = header;
  }

  /**
   * This static method allows the user to set default Supported header for automated responses.
   *
   * @param header The Supported header to become the default Supported header for automated
   *     responses
   */
  public static synchronized void setSupportedHeader(DsSipHeaderList header) {
    m_supportedHeader = header;
  }

  /**
   * Helper method that will add the allow headers to the response passed in.
   *
   * @param response
   */
  // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
  private static synchronized void addAllowHeaders(DsSipResponse response, boolean addPrack) {
    // As per the spec bis-04, All methods, including ACK and CANCEL, (add PRACK)
    // understood by the UA MUST be included in the list of methods in the
    // Allow header, when present.
    Set methodKeySet = m_interfaceMap.keySet();
    Iterator methodKeyIterator = methodKeySet.iterator();
    DsByteString method;
    while (methodKeyIterator.hasNext()) {
      method = (DsByteString) methodKeyIterator.next();
      response.addHeader(new DsSipAllowHeader(method));
    }
    response.addHeader(new DsSipAllowHeader(DsSipConstants.BS_ACK));
    response.addHeader(new DsSipAllowHeader(DsSipConstants.BS_CANCEL));
    if (addPrack) {
      response.addHeader(new DsSipAllowHeader(DsSipConstants.BS_PRACK));
    }
  } // END addAllowHeaders()

  /*
   * javadoc inherited
   */
  protected void finalize() throws Throwable {
    super.finalize();
  }

  /** */
  DsSipClientTransactionInterface getStatelessClientTransactionInterface() {
    return m_statelessTransactionInterface;
  }

  /** */
  DsSipServerTransactionInterface getStatelessServerTransactionInterface() {
    return m_statelessTransactionInterface;
  }

  /**
   * This method create a throttle for throttling of incoming new request other than BYE, ACK and
   * CANCEL. It uses the parameter to create a DsBuckets object and thus limits the incoming token
   * flow. User code can use the throttle it returns to control its action.
   *
   * @param setting a two-dimensional int array containing the configure value pairs for each of
   *     buckets intended to create. The first value in the pair is the time interval(in seconds)
   *     you want to limit number of tokens for and the second value is the number of tokens you
   *     want to allow for that time interval.
   * @throws DsException When the setting array is not a two-dimensional array or the values are not
   *     all positive.
   * @return the DsThrottle that was created
   */
  public static DsThrottle createIncomingMessageThrottle(int[][] setting) throws DsException {
    buckets = new DsBuckets(setting);
    buckets.setName(INCOMING_MESSAGE_THROTTLE);
    return new DsThrottle(buckets);
  }

  //     public static void setTransKeyGenerator(DsSipTransKeyGenerator generator)
  //     {
  //         m_transactionTable.setTransKeyGenerator(generator);
  //     }

  /** */
  class StatelessTransactionInterface
      implements DsSipClientTransactionInterface, DsSipServerTransactionInterface {
    public void provisionalResponse(
        DsSipClientTransaction clientTransaction, DsSipResponse response) {
      if (m_StrayMessageInterface != null) m_StrayMessageInterface.strayResponse(response);
    }

    public void finalResponse(DsSipClientTransaction clientTransaction, DsSipResponse response) {
      if (m_StrayMessageInterface != null) m_StrayMessageInterface.strayResponse(response);
    }

    public void timeOut(DsSipClientTransaction clientTransaction) {}

    public void icmpError(DsSipClientTransaction clientTransaction) {}

    public void close(DsSipClientTransaction clientTransaction) {}

    public void ack(DsSipServerTransaction serverTransaction, DsSipAckMessage ackMessage) {
      // don't need to call route fix interface here since it has been
      //    called in DsSipServerTransactionImpl already
      if (m_StrayMessageInterface != null) {
        if (processMaxForwards(ackMessage, isProxyServerMode())) {
          // since the max forwards header is 0 and it is proxy server mode - we can not forward -
          // must drop this msg
          Logger cat = reqCat;
          if (cat.isEnabled(Level.INFO)) {
            cat.log(
                Level.INFO,
                "StatelessTransactionInterface.ack(): dropping stray ack, Max-Forwards exceeded.");
          }
        } else {
          m_StrayMessageInterface.strayAck(ackMessage);
        }
      }
    }

    public void cancel(DsSipServerTransaction serverTransaction, DsSipCancelMessage cancelMessage) {
      if (m_StrayMessageInterface != null) m_StrayMessageInterface.strayCancel(cancelMessage);
    }

    // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
    public void prack(
        DsSipServerTransaction inviteServerTransaction,
        DsSipServerTransaction prackServerTransaction) {
      if (m_StrayMessageInterface != null)
        m_StrayMessageInterface.strayPrack(
            (DsSipPRACKMessage) (prackServerTransaction.getRequest()));
    }

    public void close(DsSipServerTransaction serverTransaction) {}

    public void timeOut(DsSipServerTransaction serverTransaction) {}

    public void icmpError(DsSipServerTransaction serverTransaction) {}
  }

  /**
   * This is the unit of work which is enqueued for outgoing requests when the transaction manager
   * is constructed with a max queue length and number of worker threads.
   */
  private class OutgoingRequestUOW implements DsUnitOfWork {
    Logger cat = generalCat;

    OutgoingRequestUOW(DsSipClientTransaction transaction) {
      m_clientTransaction = transaction;
    }

    public void run() {
      process();
    }

    /** Process a new outgoing request by starting its client transaction. */
    public void process() {
      try {
        m_clientTransaction.start();
        if (m_eventInterface != null) {
          m_eventInterface.transactionStarted(m_clientTransaction);
        }
      } catch (Exception e) {
        if (m_eventInterface == null) {
          if (cat.isEnabled(Level.WARN)) {
            cat.warn(
                "process: Exception starting client transaction and no error interface set", e);
          }
        } else {
          m_eventInterface.transactionError(
              m_clientTransaction, m_clientTransaction.getRequest(), e);
        }
      }
    }

    /**
     * Abort an outgoing unit of work. Since the user code is made aware of this event, there is no
     * need to have any abort behavior.
     */
    public void abort() {}

    private DsSipClientTransaction m_clientTransaction;
  }

  private StatelessTransactionInterface m_statelessTransactionInterface;
  private DsSipStrayMessageInterface m_StrayMessageInterface;

  /** The shutdown monitor. */
  private static class ShutdownMonitor implements DsEvent {

    /** Stop all threads and stop accepting messages. */
    private static void stop() {
      DsTimer.stop();
      DsSipClientTransactionImpl.stop();
      DsSipServerTransactionImpl.stop();
      m_transportLayer.stop();
      m_transactionTable = new DsSipTransactionTable();
      // check that we are using non blocking io before calling stopAll()
      // so that we don't load the selector by making the call
      // only to stop it.
      if (IS_NON_BLOCKING_TCP || IS_NON_BLOCKING_TLS) {
        DsSelector.stopAll();
      }
      operationalState = OperationalState.SHUTDOWN;
    }

    public void run(Object arg) {
      if (arg == null) {
        if (m_transactionTable.size() == 0) {

          // transaction maps are empty shutdown and notify
        } else {
          DsTimer.schedule(5000L, this, null);

          return;
        }
      }

      stop();
      // notify the callee of TransactionManager shutdown that we have closed the user agent.
      if (m_eventInterface != null) {
        m_eventInterface.transactionManagerShutdown();
      }
    }
  }

  /** Exception specifying the already shutting down state. */
  public static class DsAlreadyShuttingDownException extends DsException {
    /** Constructs the Already shutting down exception. */
    public DsAlreadyShuttingDownException() {
      super("Already in shutdown state!");
    }
  }

  /** Exception specifying the already shutting down state. */
  public static class DsShutdownException extends DsException {
    /**
     * Constructs the shutdown exception.
     *
     * @param reason the String to use for this exception
     */
    public DsShutdownException(String reason) {
      super(reason);
    }
  }

  /** Exception specifying the already suspended state. */
  public static class DsAlreadySuspendInProgressException extends DsException {
    /** Constructs the Already suspend in progress exception. */
    public DsAlreadySuspendInProgressException() {
      super("Already in suspend in progress state!");
    }
  }

  /** Exception specifying the already suspended state state. */
  public static class DsSuspendedException extends DsException {
    /** Constructs the suspended exception. */
    public DsSuspendedException() {
      super("Already in Suspended state!");
    }

    /**
     * Constructs the suspended exception.
     *
     * @param reason the String to use for this exception
     */
    public DsSuspendedException(String reason) {
      super(reason);
    }
  }

  /**
   * Populates SAEvent for Operations event Suspend and Resume. Sample format for the Event sent to
   * EMS - Event Received - SuspendTimerExpiry, Current State - SuspendInProgress, New State -
   * Suspended, Max Timeout in Seconds - 0
   */
  // REFACTOR
  public static void operationsEvent(String eventReceived, String currentState, String newState) {
    //        Logger cat = generalCat;
    //        try {
    //            Notification n = new Notification(SAEventConstants.OPERATIONS_EVENT_NOTIFICATION,
    //                SIPSessionsMBeanImpl.getInstance().getObjectName(), eventSequenceNumber++);
    //            StringBuilder message = new StringBuilder();
    //            message.append("Event Received - ").append(eventReceived)
    //                .append(SAEventConstants.COMMA).append(SAEventConstants.SPACE)
    //                .append("Current State - ").append(currentState)
    //                .append(SAEventConstants.COMMA).append(SAEventConstants.SPACE)
    //                .append("New State - ").append(newState);
    //            String[] data = new String[2];
    //            data[0] = eventReceived;
    //            data[1] = message.toString();
    //            n.setUserData(data);
    //            SIPSessionsMBeanImpl.getInstance().sendNotification(n);
    //        } catch (Throwable t) {
    //            if (cat.isEnabled(Level.ERROR))
    //                cat.error("Error while creating and sending operations event notification",
    // t);
    //        }
    //
    //        //Send SAEvent
    //        SAEvent operationsSaEvent = new SAEvent();
    //        operationsSaEvent.setType(EventType.ApplicationEvents);
    //        operationsSaEvent.setId(eventReceived);
    //
    //        OperationEventDataParam eventDataParam = new OperationEventDataParam.Builder()
    //        		.eventReceived(eventReceived)
    //        		.currentState(currentState)
    //        		.newState(newState)
    //        		.build();
    //
    //        operationsSaEvent.setDataParam(eventDataParam);
    //        SaEventLog.info(operationsSaEvent.toEventJSON());
  }

  public static void setSmp_theSingleton(DsSipTransactionManager smp_theSingleton) {
    DsSipTransactionManager.smp_theSingleton = smp_theSingleton;
  }
} // End class DsSipTransactionManager