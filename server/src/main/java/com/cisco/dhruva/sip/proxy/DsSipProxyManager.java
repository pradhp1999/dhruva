/*
 * Copyright (c) 2001-2002, 2003-2014 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.proxy;


import com.cisco.dhruva.sip.proxy.Errors.DsProxyDnsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.Trace;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class is analogous to DsSipTransactionManager, that is, it manages
 * DsSipProxyTransaction objects for all active transactions.
 * <p/>
 * This is a singleton class, i.e., only a single instance of this class
 * should exist for the whole proxy.
 *
 * @author dynamicsoft, Inc.
 */
public class DsSipProxyManager implements DsSipRequestInterface,
        DsSipStrayMessageInterface,
        DsSipTransactionEventInterface
{

    // data members

    private DsSipTransactionManager transManager = null;
    private DsSipTransportLayer transport;
    private DsControllerFactoryInterface controllerFactory = null;

    // local reference to viaHandler. Saved here so that I don't
    // have to call DsViaHadler.getInstance() every time I need a ViaHandler
    // (as if it're any different...)
    private DsViaHandler viaHandler;

    private static DsSipProxyManager m_Singleton = null;
    private static boolean initCalled = false;

    private DsStrayResponseInterface strayResponseInterface = null;
    private DsSSLTrustManager trustManager = null;

    public final static int OVERLOAD_NONE = 0;
    public final static int OVERLOAD_REJECT = 1;
    public final static int OVERLOAD_REDIRECT = 2;

    public static final int GEO_DISABLED_SITE = 0;
    public static final int GEO_ENABLED_SITE = 1;
    public static final int DNS_LOOKUP_FAILURE = 2;

    public final static int DEFAULT_EVENT_QUEUE_LENGTH = 300;
    public final static int DEFAULT_REQUEST_QUEUE_LENGTH = 300;

    public final static int EXTERNAL_NETWORK = 0;
    public final static int INTERNAL_NETWORK = 0;
    public final static String EXTERNAL_NETWORK_STR = "EXTERNAL";
    public final static String INTERNAL_NETWORK_STR = "INTERNAL";

    public final static int EXTERNAL_WIRELESS_UDP_NETWORK = 1;
    public final static String EXTERNAL_WIRELESS_UDP_NETWORK_STR = "EXTERNAL_UDP_WIRELESS";


    public final static int EXTERNAL_WIRELESS_SIGCOMP_NETWORK = 2;
    public final static String EXTERNAL_WIRELESS_SIGCOMP_NETWORK_STR = "EXTERNAL_SIGCOMP_WIRELESS";

    public static final int EXTERNAL_WIRELESS_TOKENIZED_SIP_NETWORK = 3;
    public static final String EXTERNAL_WIRELESS_TOKENIZED_SIP_NETWORK_STR = "EXTERNAL_TOKENIZED_SIP_WIRELESS";

    private final static boolean USE_CONNECTED_UDP = Boolean.getBoolean("com.cisco.DsLibs.DsSipProxyManager.TOKENSIP_CONNECTED_UDP");

    private static boolean enableSpamRejection = DsConfigManager.getProperty(DsConfigManager.PROP_ENABLE_SPAM_REJECTION,
            DsConfigManager.PROP_ENABLE_SPAM_REJECTION_DEFAULT);
    
    private static final String WEBEX_INGRESS_REGION_CODE =
        DsConfigManager.getProperty(DsConfigManager.PROP_WEBEX_INGRESS_REGION_CODE,
                                    DsConfigManager.PROP_WEBEX_INGRESS_REGION_CODE_DEFAULT);

    private static final String PROTOCOL_FOR_SRV_LOOKUP =
        DsConfigManager.getProperty(DsConfigManager.PROP_PROTOCOL_FOR_SRV_LOOKUP,
                                    DsConfigManager.PROP_PROTOCOL_FOR_SRV_LOOKUP_DEFAULT);

    private DsSipRequestQueueHelper overloadHelper;
    private UAQueueConfig overloadQueueConfig =
            new UAQueueConfig(DsSipRequestQueueHelper.DEFAULT_WORKERS,
                              DEFAULT_REQUEST_QUEUE_LENGTH,
                              DsWorkQueue.THRESHOLD,
                              DsSipRequestQueueHelper.DISCARD_NEWEST);
    private int overloadRetryAfter = 0; //used when LL stack gets overloaded
    private int overloadRedirectPort = 0;
    private String overloadRedirectHost = null;
    private int overloadRedirectProtocol = DsSipTransportType.NONE;

    protected static Logger Log =
            DhruvaLoggerFactory.getLogger(DsSipProxyManager.class);

    //some private strings
    private static final DsByteString colon = new DsByteString(":");
    private static final DsByteString PING = new DsByteString("PING");
    // public methods

    /**
     * MUST be called exactly once before using the proxy library
     * This creates DsViaHandler
     *
     * @param transport TransportLayer to be used by the ViaHandler's
     *                  loop detection
     * @return true if completed succesfully, false if already initialized
     */
    public static boolean initProxyLib(DsSipTransportLayer transport)
            throws DsCryptoInitException
    {

        if (!initCalled)
            initCalled = true;
        else
            return false;

        try
        {
            DsViaHandler.init(transport);
        }
        catch (Throwable e)
        {
            Log.error("Exception creating ViaHandler", e);
            throw new DsCryptoInitException(e.getMessage());
        }

        return false;
    }

    /**
     * The constructor creates all underlying DsLibs objects
     *
     * @param transport transport layer to use in DsLibs
     * @param cf        ControllerFactory
     */
    public DsSipProxyManager(DsSipTransportLayer transport,
                             DsControllerFactoryInterface cf)
            throws DsSingletonException, DsException, DsCryptoInitException
    {

        if (m_Singleton != null)
        {
            Log.info("null ControolerFactory passed to the constructor!");
            throw new DsSingletonException("Only one instance of " +
                    "DsSipProxyManager can be created");
        }
        m_Singleton = this;

        setControllerFactory(cf);
        this.transport = transport;

        initProxyLib(transport);
        viaHandler = DsViaHandler.getInstance();

        // construct a low level transaction manager. The proxy manager
        // implements the DsSipRequestInterface, so pass this to the
        // constructor
        transManager = new DsSipTransactionManager(transport, this);
        transManager.setProxyServerMode(true);
        transManager.setStrayMessageInterface(this);
        transManager.setTransactionEventInterface(this);

        addSupportedExtension(DsSipConstants.BS_PATH.toString());

        // set default event queue size to 300.
        DsWorkQueue eventQueue =
                (DsWorkQueue) DsWorkQueue.getQueueTable().get(DsWorkQueue.DATA_IN_QNAME);
        eventQueue.setMaxSize(DEFAULT_EVENT_QUEUE_LENGTH);

        // setup overload queue
        onOverloadNone();

        Log.info("DsSipProxyManager created");
    }


    /**
     * Sets the ControllerFactory. This allows to change the
     * ControllerFactory after the ProxyManager was created
     *
     * @param cf the ControllerFactory to use whenever a request with this
     *           method is received
     */
    public void setControllerFactory(DsControllerFactoryInterface cf)
    {
        //MEETPASS refactor, Preconditions, Optional
        if (cf == null)
        {
            Log.info("null ControolerFactory passed to setControllerFactory!");

            throw new NullPointerException("ControllerFactory should not be null");
        }

        // remember the Controller Factory
        controllerFactory = cf;
    }

    /**
     * Sets the callback to handle stray responses
     *
     * @param ifc implements DsStrayResponseInterface
     */
    public void setStrayResponseInterface(DsStrayResponseInterface ifc)
    {
        strayResponseInterface = ifc;
    }

    public void setLoopDetector(DsLoopCheckInterface loopDetector)
    {
        getViaHandler().setLoopDetector(loopDetector);
    }

    /**
     * Sets a trust manager that can be used for authorizing SSL connections
     *
     * @param trustManager implementation of DsSSLTrustManager that will be
     *                     invoked every time a new SSL connection is established
     *                     <p/>
     *                     The parameters to the callbacks is the chain of credentials proving
     *                     the identity of the peer. The first element is the chain is the
     *                     identity of the peer; that's what Proxy needs to look at
     */
    public void setSSlTrustManagerInterface(DsSSLTrustManager trustManager)
    {
        this.trustManager = trustManager;
    }

    /**
     * Adds an extension supported by the application to the list of
     * extensions accepted by the Core. If a request with an unsupported
     * Proxy-Require header is present in the received request, the
     * request will be automatically rejected.
     *
     * @param extension exyension name as it will appear in Proxy-Require
     *                  (note that the string is case-sensitive)
     */
    public void addSupportedExtension(String extension)
    {
        DsSupportedExtensions.addExtension(new DsByteString(extension));
    }

    /**
     * Removes an extension supported by the application from the list of
     * extensions accepted by the Core. If a request with an unsupported
     * Proxy-Require header is present in the received request, the
     * request will be automatically rejected.
     *
     * @param extension exyension name as it will appear in Proxy-Require
     *                  (note that the string is case-sensitive)
     * @return true if the extension was previously accepted, false otherwise
     */
    public boolean removeSupportedExtension(String extension)
    {
        return DsSupportedExtensions.removeExtension(new DsByteString(extension));
    }

    /**
     * @return the transport layer passed as a parameter to constructor
     */
    protected DsSipTransportLayer getTransportLayer()
    {
        return m_Singleton.transport;
    }

    /**
     * @return an instance of ProxyManager
     */
    public static DsSipProxyManager getInstance()
    {
        return m_Singleton;
    }


    /*
    * Shuts down the proxy. All new requests received after
    * this command is issued will be responded with a
    * 503 Service Unavailable response.
    * @param shutdownSeconds the proxy will shutdown after this interval
    * even if there are active transaction remaining
    * @param retryAfter this value will go into Retry-After field
    * of the 503 responses. If -1, no Retry-After will be insterted.
    * @param observer will be notified when shutdown has completed
    */
    public void shutdownReject(int shutdownSeconds)
            throws DsSipTransactionManager.DsAlreadyShuttingDownException
    {
        DsSipTransactionManager.shutdownReject(shutdownSeconds);
    }

    /**
     * Shuts down the proxy. All new requests received after
     * this command is issued will be responded with a
     * 503 Service Unavailable response.
     *
     * @param redirectHost      hostname or IP address of the proxy to be
     *                          redirected to
     * @param redirectPort      port number of the proxy to be
     *                          redirected to
     * @param redirectTransport protocol to use for the host to be
     *                          redirected to
     * @param shutdownSeconds   will shutdown in that many seconds anyway
     */
    public void shutdownRedirect(String redirectHost, int redirectPort,
                                 int redirectTransport, int shutdownSeconds)
            throws DsSipTransactionManager.DsAlreadyShuttingDownException
    {
        DsSipTransactionManager.shutdownRedirect(shutdownSeconds, redirectHost,
                                      redirectPort, redirectTransport);
    }


    /**
     * Creates a queue that will be used for sending 503 responses
     * when it gets filled up
     *
     * @param retryAfter this value will go into Retry-After field
     *                   of the 503 responses. If -1, no Retry-After will be insterted.
     */
    public synchronized void onOverloadReject(int retryAfter)
            throws DsException
    {
        overloadHelper =
                new DsSipRequestRejectHelper(this, null, getOverloadQueueMaxThreads(),
                                             (int) getOverloadQueueDiscardPolicy(),
                                             getOverloadQueueMaxSize(),
                                             retryAfter);
        overloadHelper.setBypassQueue(false);

        overloadRetryAfter = retryAfter;
        overloadRedirectPort = 0;
        overloadRedirectHost = null;
        overloadRedirectProtocol = DsSipTransportType.NONE;

        DsWorkQueue queue = getOverloadQueue();
        queue.setThresholdSize(getOverloadQueueThreshold());

        transManager.setRequestInterface(overloadHelper, null);
    }

    /**
     * Creates a queue that will be used for sending 302 responses
     * when it gets filled up
     *
     * @param redirectHost      hostname or IP address of the proxy to be
     *                          redirected to
     * @param redirectPort      port number of the proxy to be
     *                          redirected to
     * @param redirectTransport protocol to use for the host to be
     *                          redirected to
     */
    //MEETPASS  We can delete overload
    public synchronized void onOverloadRedirect(String redirectHost,
                                                int redirectPort,
                                                int redirectTransport)
            throws DsException
    {

        overloadHelper =
                new DsSipRequestRedirectHelper(this, null, getOverloadQueueMaxThreads(),
                                               getOverloadQueueDiscardPolicy(),
                                               getOverloadQueueMaxSize(),
                                               redirectHost, redirectPort,
                                               redirectTransport);
        overloadHelper.setBypassQueue(false);

        overloadRedirectPort = redirectPort;
        overloadRedirectHost = redirectHost;
        overloadRedirectProtocol = redirectTransport;
        overloadRetryAfter = 0;

        DsWorkQueue queue = getOverloadQueue();
        queue.setThresholdSize(getOverloadQueueThreshold());

        transManager.setRequestInterface(overloadHelper, null);
    }


    /**
     * Disables handling of overload conditions
     */
    public void onOverloadNone() throws DsException
    {
        overloadHelper = new DsSipRequestRejectHelper(this, null, 0);
        overloadHelper.setBypassQueue(false);
        overloadRetryAfter = 0;
        overloadRedirectPort = 0;
        overloadRedirectHost = null;
        overloadRedirectProtocol = DsSipTransportType.NONE;

        transManager.setRequestInterface(overloadHelper, null);
    }

    /**
     * Truns  overload mode on, which causes the UA
     * to reject all new transactions with a 503
     *
     * @param retryAfter Retry-After to use in 503 responses;
     */
    public void setOverloadMode(int retryAfter)
    {
        if (overloadHelper instanceof DsSipRequestRejectHelper &&
                retryAfter >= 0)
            ((DsSipRequestRejectHelper) overloadHelper).constructRetryAfter(retryAfter);
        overloadHelper.setOverloaded(true);
    }


    public void clearOverloadMode()
    {
        overloadHelper.setOverloaded(false);
        if (overloadRetryAfter > 0 &&
                overloadHelper instanceof DsSipRequestRejectHelper)
            ((DsSipRequestRejectHelper) overloadHelper).constructRetryAfter(overloadRetryAfter);
    }


    /**
     * Sets the max request queue size
     *
     * @param max_size maximum size of the queue
     */
    public synchronized void setOverloadMaxSize(int max_size)
    {
        overloadQueueConfig.setMaxSize(max_size);

        DsWorkQueue queue = getOverloadQueue();

        if (queue != null)
            queue.setMaxSize(max_size);
    }

    /**
     * Sets the warning threshold for request queue
     *
     * @param threshold percentage of the maximum queue size where the overload
     *                  alarm is sent; when the queue size goes below this point, the overload
     *                  condition is considered to be gone
     */
    public synchronized void setOverloadQueueThreshold(int threshold)
    {
        overloadQueueConfig.setThreshold(threshold);

        DsWorkQueue queue = getOverloadQueue();

        if (queue != null)
            queue.setThresholdSize(threshold);
    }


    public synchronized void setRouteFixInterface(DsSipRouteFixInterface rfi)
    {
        transManager.setRouteFixInterface(rfi);
    }

    /**
     * Sets the max number of threads to be used for handling this queue
     *
     * @param n_threads maximum number of threads
     */
    public synchronized void setOverloadQueueMaxThreads(int n_threads)
    {
        overloadQueueConfig.setMaxThreads(n_threads);

        DsWorkQueue queue = getOverloadQueue();

        if (queue != null)
            queue.setMaxNoOfWorkers(n_threads);
    }

    /**
     * Sets the discard policy for the request (aka overload) queue
     *
     * @param discard_policy one of DsWorkQueue.DISCARD_OLDEST or
     *                       DsWorkQueue.DISCARD_NEWEST
     */
    public synchronized void setOverloadQueueDiscardPolicy(short discard_policy)
    {
        overloadQueueConfig.setDiscardPolicy(discard_policy);

        DsWorkQueue queue = getOverloadQueue();

        if (queue != null)
            queue.setDiscardPolicy(discard_policy);
    }

    /**
     * @return max overload queue size
     */
    public int getOverloadQueueMaxSize()
    {
        return overloadQueueConfig.getMaxSize();
    }

    /**
     * @return current number of entries in overload queue
     */
    public int getOverloadQueueSize()
    {
        DsWorkQueue queue = getOverloadQueue();

        if (queue != null)
            return queue.getSize();
        else
            return -1;
    }

    /**
     * @return overload queue threshold
     */
    public synchronized int getOverloadQueueThreshold()
    {
        return overloadQueueConfig.getThreshold();
    }

    /**
     * @return max number of threads for this queue
     */
    public synchronized int getOverloadQueueMaxThreads()
    {
        return overloadQueueConfig.getMaxThreads();
    }

    /**
     * @return current number of active threads servicing overload queue
     */
    public int getOverloadQueueActiveThreads()
    {
        DsWorkQueue queue = getOverloadQueue();

        if (queue != null)
            return queue.getActiveThreadCount();
        else
            return -1;
    }


    /**
     * @return discard policy for this queue
     */
    public synchronized short getOverloadQueueDiscardPolicy()
    {
        return overloadQueueConfig.getDiscardPolicy();
    }

    /**
     * @return one of OVERLOAD_NONE, OVERLOAD_REDIRECT or OVERLOAD_REJECT
     */
    public int getOnOverloadMode()
    {
        if (overloadHelper == null)
            return OVERLOAD_NONE;
        else if (overloadHelper instanceof DsSipRequestRedirectHelper)
            return OVERLOAD_REDIRECT;
        else
            return OVERLOAD_REJECT;
    }

    /**
     * @return the value of RetryAfter if onOverloadReject is set; 0 otherwise
     */
    public int getOverloadRetryAfter()
    {
        return overloadRetryAfter;
    }

    /**
     * @return the value of redirectPort if onOverloadReject is set; 0 otherwise
     */
    public int getOverloadRedirectPort()
    {
        return overloadRedirectPort;
    }

    /**
     * @return the value of redirectHost if onOverloadReject is set; 0 otherwise
     */
    public String getOverloadRedirectHost()
    {
        return overloadRedirectHost;
    }

    /**
     * @return the value of redirectTransport if onOverloadReject is set; 0 otherwise
     */
    public int getOverloadRedirectTransport()
    {
        return overloadRedirectProtocol;
    }

    protected DsWorkQueue getOverloadQueue()
    {
        return (DsWorkQueue) DsWorkQueue.getQueueTable().get(DsWorkQueue.REQUEST_IN_QNAME);
    }


    /**
     * Sets the timeout after wich a TCP connection will be closed
     *
     * @param seconds timeout in seconds
     */
    public void setTCPCleanupInterval(int seconds)
    {
        transport.setIncomingConnectionTimeout(seconds);
        transport.setOutgoingConnectionTimeout(seconds);
        Log.info("TCP cleanup interval is set to " + seconds);
    }

    /**
     * @return SSL Context
     */
    public DsSSLContext getSSLContext()
    {
        return transport.getSSLContext();

    }

    /**
     * @return TCP connection timeout
     */
    public int getTCPCleanupInterval()
    {
        return transport.getIncomingConnectionTimeout();
    }

    /**
     * @return the number of transaction in TransactionManager
     */
    public int getTransactionCount()
    {
        return transManager.getSizeClientTransactionMap() +
                transManager.getSizeServerTransactionMap();
    }


    /**
     * Adds another port to listen to
     *
     * @param aPort          port number
     * @param aTransportType transport type (UDP or TCP)
     * @param aHostAddress   network address of host
     */
    public void listenPort(int aPort, int aTransportType, InetAddress aHostAddress)
            throws DsException, IOException
    {
        transport.listenPort(aPort, aTransportType, aHostAddress);
        Log.debug("listen on default network, port=" + aPort +
                    ", transport=" + aTransportType + " ,address=" + aHostAddress);

    }


    /**
     * Adds another port to listen to
     *
     * @param network        configured network to listen on
     * @param aPort          port number
     * @param aTransportType transport type (UDP or TCP)
     * @param aHostAddress   network address of host
     */
    public void listenPort(DsNetwork network, int aPort, int aTransportType, InetAddress aHostAddress)
            throws DsException, IOException
    {
        transport.listenPort(network, aPort, aTransportType, aHostAddress);

        Log.debug("listen on network=" + network + ", port=" + aPort +
                    ", transport=" + aTransportType + " ,address=" + aHostAddress);
    }


    /**
     * Stops listening on a specified port/transport/interface
     *
     * @param aPort          port number
     * @param aTransportType transport type (UDP or TCP)
     * @param anAddress      network address of host
     * @param timeoutSeconds keep listening for timeoutSeconds
     * @return <b>true </b> if Proxy was listening on the specified socket,
     *         <b> false</b> otherwise
     */
    public boolean removeListenPort(int aPort, int aTransportType,
                                    InetAddress anAddress, int timeoutSeconds)
    {
        Log.debug("removing port " + aPort + ", transport " + aTransportType + ", address " + anAddress + ", timeout=" + timeoutSeconds);
        return transport.removeListenPort(aPort, aTransportType, anAddress, timeoutSeconds);
    }


    /**
     * This method is called by the transaction manager when a new transaction
     * has been created. It will find a proxy object for the transaction if one
     * exists, and invoke its DsSipRequestInterface, otherwise it obtains a
     * Controller from a ControllerFactory and asks it to create a new
     * proxy transaction
     */
    public void request(DsSipServerTransaction serverTransaction)
    {
        request(serverTransaction, serverTransaction.getRequest());
    }


    public void request(DsSipServerTransaction serverTransaction,
                        DsSipRequest request)
    {

        DsLog4j.logSessionId(request);
        Log.debug("Entering ProxyManager request()");


        Log.debug("BEGIN request()");

        DsSipResponse errorResponse =
                DsProxyUtils.validateRequest(request, true);


        if (errorResponse != null)
        {

            // serverTrans is null when dealing with stray ACKs and CANCELs
            if (serverTransaction != null)
            {
                try
                {
                    DsProxyUtils.sendErrorResponse(serverTransaction,
                                                   errorResponse);
                }
                catch (Exception e)
                {
                    if (Log.on && Log.isEnabled(Level.ERROR))
                        Log.error("Exception sending " +
                                errorResponse.getStatusCode() + "error response!", e);
                    try
                    {
                        serverTransaction.abort();
                    }
                    catch (Exception e1)
                    {

                        // This is a valid condition when operating on a stray ACK or
                        // CANCEL. Since there is currently no efficient way to check
                        // the request, I'm just leaving the log4j statement out for now.

//  		Log.warn(
//  				  "Exception aborting transaction. Must never happen", e1);
                    }
                }
            }
            return;
        }

        if(enableSpamRejection && isSpam(serverTransaction, request)) {
            return;
        }

        addCiscoGeoIPLookupInfoHeader(request);

        DsControllerFactoryInterface cf = getControllerFactory();

        if (perfControllerFactoryLog.on)
        {
            perfControllerFactoryLog.debug("Before ControllerFactory getCurrent");
        }
        DsControllerInterface controller = cf.getController(serverTransaction,
                                                            request);



        Log.debug("After ControllerFactory getCurrent");


        DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction,
                                                                    request);

        Log.debug("END request()");

        if (proxy != null)
        {
            if (!(proxy instanceof DsProxyTransaction))
            {
                // if stateless, abort the low level transaction
                try
                {
                    serverTransaction.abort();
                }
                catch (Exception e1)
                {
                    // This is a valid condition when operating on a stray ACK or
                    // CANCEL. Since there is currently no efficient way to check
                    // the request, I'm just leaving the log4j statement out for now.

                    // 		Log.warn(
                    // 				  "Exception aborting transaction. Must never happen", e1);
                }
            }
        }


        Log.debug("Leaving ProxyManager request()");
    }

    public boolean isSpam (DsSipServerTransaction serverTransaction, DsSipRequest request) {
        if(request.getMethodID() == DsSipConstants.INVITE && request.getToTag() == null 
                && SpamRejectorService.isTransportSupported(request.getConnectionTransport())) {
            String siteName = getSiteFromURI(request);
            if(siteName != null) {
                Log.trace("Performing spam check");
                DnsResolver dnsClient = DnsClientService.getInstance().getClient();
                MRSClientFactory mrsClientFactory = MRSClientFactory.getInstance();
                SpamRejector spamRejector = SpamRejectorService.getInstance(dnsClient, mrsClientFactory);
                if(spamRejector.isSpam(siteName)) {
                    try {
                        DsSipResponse response = new DsSipResponse(
                                DsSipResponseCode.DS_RESPONSE_SPAM, serverTransaction.getRequest(),
                                null, null);
                        response.setApplicationReason(DsMessageLoggingInterface.REASON_GENERATED);
                        response.setLoggingReasonPhrase(new DsByteString("Invalid site rejection"));
                        serverTransaction.sendResponse(response);
                    }
                    catch (Exception e) {
                        Log.error("Exception while sending 599 spam response: {}", e.getMessage(), e);
                    }
                    return true;
                }
            }
        }
        else {
            Log.debug("Incoming request is not a fresh invite/Incoming transport not supported. Skipping spam check");
        }
        return false;
    }
    
    protected DsControllerFactoryInterface getControllerFactory()
    {
        return controllerFactory;
    }

    private DsViaHandler getViaHandler()
    {
        return viaHandler;
    }


    /**
     * Ack message was received without an associated transaction
     *
     * @param ack ack message that was received
     */
    public void strayAck(DsSipAckMessage ack)
    {
        if (!proxyBasedOnRoute(ack))
        {
            // hack !!!!!!!
            request(null, ack);
        }
    }

    /**
     * Cancel message was received without an associated transaction
     *
     * @param cancel cancel  message that was received
     */
    public void strayCancel(DsSipCancelMessage cancel)
    {
        if (!proxyBasedOnRoute(cancel))
        {
            // hack !!!!!!!!
            request(null, cancel);
        }
    }

    public void strayPrack(DsSipPRACKMessage dsSipPRACKMessage)
    {
        //TODO REDDY
    }

    /**
     * Response that received without an associated transaction
     *
     * @param response response  that was received
     */
    public void strayResponse(DsSipResponse response)
    {
        if (strayResponseInterface != null)
            strayResponseInterface.strayResponse(response);
        else
            proxyStrayResponse(response);
    }

    /**
     * Statelessy proxies a response upstream.
     *
     * @param response the response to be proxied
     */
    public void proxyStrayResponse(DsSipResponse response)
    {
        Log.debug("Entering proxyStrayResponse(DsSipResponse response)");

        String direction = response.getNetwork().getName();

        proxyStrayResponse(response, direction);
        Log.debug("Leaving proxyStrayResponse(DsSipResponse response)");
    }

    public void proxyStrayResponse(DsSipResponse response, String direction)
    {

        Log.debug("Entering proxyStrayResponse(DsSipResponse response, int direction)");

        DsControllerConfig config = DsControllerConfig.getCurrent();
        //check the record route
        if (config.doRecordRoute())
        {
            //REDDY_RR_CHANGE
            // CSCui91407
            //Change the record route for stray responses as well
            try {
                setRecordRouteInterface(response);
            } catch (DsException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        // check the top Via;
        DsSipViaHeader myVia;
        try
        {
            myVia = response.getViaHeaderValidate();
        }
        catch (DsException e)
        {
            myVia = null;
            Log.error("Error in getting top Via header", e);
        }
        if (myVia == null)
            return;

        // check the the top Via matches our proxy
        if (myVia.getBranch() == null)
        { // we always insert branch
            Log.info("Dropped stray response with bad Via");
            return;
        }

        if (!getViaHandler().isLocalInterface(myVia.getHost(), myVia.getPort(), myVia.getTransport()))
        {
             Log.info("Dropped stray response with bad Via");
            return;
        }


        DsProxyUtils.removeTopVia(response);

        DsSipViaHeader via;
        try
        {
            via = response.getViaHeaderValidate();
        }
        catch (DsException e)
        {
            via = null;
            Log.error("Error in getting new top Via header", e);
        }
        if (via == null)
        {
            Log.error("Top via header is null. Return");
            return;
        }

        if (!getViaHandler().decryptVia(via))
        {
            Log.info("Dropped stray response with bad Via (canot decrypt)");
            return;
        }

        // added to support draft-ietf-sip-icm
        // basically this is only need to allow icm
        // traversal for stateless responses being
        // sent to a symmetric NAT
        DsBindingInfo newBinding = new DsBindingInfo();

        newBinding.setNetwork(config.getDefaultNetwork());

        /*
        // 06/07/05 - this was commented out as JUA now provides the
        //            ability for responses to follow the request
        //            when a network is NATed.
        response.setBindingInfo(newBinding);
        */

        if (via.hasParameter(DsProxyStatelessTransaction.RPORT))
        {

            // get the branch parameter from this server's via
            DsByteString branch = myVia.getBranch();

            // get the rport cookie from the branch
            // if found, it will be formatted as <ip>:<port>
            // where <ip> and <port> are the IP address and port
            // of the interface on which the
            // original request was received
            DsByteString rportCookie = DsProxyStatelessTransaction.getRPORTCookie(branch);
            if (rportCookie != null)
            {
                int indexOfColon = rportCookie.indexOf(colon);
                DsByteString rportAddr = rportCookie.substring(0, indexOfColon);
                DsByteString rportPort = rportCookie.substring(indexOfColon + 1);
                String rportAddrStr = rportAddr.toString();
                String rportPortStr = rportPort.toString();

                Log.debug("Setting local binding to " +
                        rportAddrStr + ' ' +
                        rportPortStr);
                try
                {
                    newBinding.setLocalAddress(InetAddress.getByName(rportAddrStr));
                    newBinding.setLocalPort(Integer.parseInt(rportPortStr));


                }
                catch (Throwable t)
                {
                    Log.error("Error setting the local binding", t);
                }
            }
        }


        try
        {
            if (Log.on && Log.isDebugEnabled()) Log.debug("About to set interface");

            newBinding.setNetwork(DsControllerConfig.getCurrent().getDefaultNetwork());

            DsSipConnection strayResponseConnection = DsSipTransactionManager.getConnection(response);

            if (strayResponseConnection != null)
            {
                strayResponseConnection.send(response);
            }
            if (Log.on && Log.isInfoEnabled())
                Log.info("sent stray response");

        }
        catch (Exception e)
        {
            if (Log.on && Log.isEnabled(Level.ERROR))
                Log.error("Couldn't forward stray response", e);
        }
    }

    /**
     * Checks if the message can ve proxied based on the Route
     * I can't really proxy based on Record-Route because
     * I don't know what to put in the Via!!!!
     * Route header processing is done on the Controller code.
     */
    private boolean proxyBasedOnRoute(DsSipRequest request)
    {
        return false;
    }


    /**
     * Notification that the transaction manager is shutting down.
     */
    public void transactionManagerShutdown()
    {
        try
        {
            REPStateMBeanImpl.getInstance().notify(REPStateMBean.SHUTTING_DOWN,"Shutting down...");
        }
        catch (Throwable t)
        {
            Log.error("Error sending shutdown notifcation", t);
        }

        Thread.yield();

        System.exit(0);
    }

    public void halt()
    {
        // all events in internal queues got written out to logging targets
        // send the proxy shutdown trap

        //MIGRATION
        /*
        MgmtNotification mn = new MgmtNotification(CC.serverShutdownTrap, this, 0);
        mn.bind(CC.serverShutdownUid, CoreComponentsBuffer.getInstance().getUID());
        mn.bind(CC.serverShutdownProductType, CoreComponentsCfg.getProductName() + ' ' +
                                              CoreComponentsCfg.getProductVersion().version + '.' +
                                              CoreComponentsCfg.getProductVersion().buildNumber);
        mn.bind(CC.serverShutdownMessage, CoreComponentsCfg.getProductName() + " has halted");
        // Send notification.
        MgmtNotificationServiceMBean mns = MgmtNotificationService.getInstance();
        try {
          mns.sendNotification(mn);
        }
        catch (Throwable t) {
          if (Log.on && Log.isEnabledFor(Level.ERROR)) Log.error("Error sending halt trap", t);
        }
        */
        try
        {
            REPStateMBeanImpl.getInstance().notify(REPStateMBean.SHUTTING_DOWN,"Halting...");
        }
        catch (Throwable t)
        {
            Log.error("Error sending halt notifcation", t);
        }

        Thread.yield();

        System.exit(0);
    }

    /**
     * Notification that the transaction has been removed from the
     * transaction manager transaction map.
     *
     * @param transaction the transaction that has been removed
     */
    public void transactionTerminated(DsSipTransaction transaction)
    {
    }

    /**
     * Notification that the transaction submitted to the
     * transaction manager has been started.
     *
     * @param transaction the transaction that has been started
     */
    public void transactionStarted(DsSipTransaction transaction)
    {
    }


    /**
     * Handle an individual error.
     *
     * @param transaction the transaction in which the error occured
     * @param message     the message being sent
     * @param error       the error encountered
     */
    public void transactionError(DsSipClientTransaction transaction, DsSipMessage message,
                                 Throwable error)
    {
    }

    /**
     * Handle an individual error.
     *
     * @param transaction the transaction in which the error occured
     * @param message the message being sent
     * @param error the error encountered
     *
     * */
    public void transactionError(DsSipServerTransaction transaction, DsSipMessage message,
                                 Throwable error) {
  }
    /**
     * Set the RR header properly in incoming message
     *
     * @param msg the incoming SIP message in which RR needs to be set
     * */
     public void setRecordRouteInterface(DsSipMessage msg) throws DsException
    {
        // Start collecting instrumentation data for FW Trans - Set RR Interface
        Log.debug("Entering setRecordRouteInterface()");

        
        if (msg.getHeaders(DsSipRecordRouteHeader.sID) != null) {
            // stateless, must flip them all
            /**
             * For stray responses we will not have any context on the position
             * of our record route so we have to check the complete list and
             * rewrite our Record-Route. This logic will not work if the request
             * gets spiraled through the proxy and response is a stray response
             * which is a very corner case. One way to fix this is to add a token
             * to the record route which will identify as added by the this
             * Particular proxy. (Token should be generated using top via and a
             * randon number identifying this proxy).
             */
            setRecordRouteInterfaceStateless(msg);
        }
        Log.debug("Leaving setRecordRouteInterface()");
    }

        //REDDY_RR_CHANGE
        private void setRecordRouteInterfaceStateful(DsSipMessage msg) throws DsException
     {
         //int interfacing = (m_RequestDirection == DsControllerConfig.INBOUND) ? DsControllerConfig.OUTBOUND : DsControllerConfig.INBOUND;
         DsSipHeaderList rrList = null;
         int m_MyRRIndexFromEnd;

         rrList = msg.getHeadersValidate(DsSipConstants.RECORD_ROUTE);

         boolean compress = msg.shouldCompress();
         DsTokenSipDictionary encode = msg.shouldEncode();
         DsSipHeaderList ll = msg.getHeaders(DsSipRecordRouteHeader.sID);

         if (ll == null)
         {
             m_MyRRIndexFromEnd = 0;
         }
         else
         {
             m_MyRRIndexFromEnd = ll.size();
         }

         int routeIndex = rrList.size() - 1;


         if ((routeIndex >= 0) && (routeIndex < rrList.size()))
         {
             DsSipRecordRouteHeader rrHeader = (DsSipRecordRouteHeader) rrList.get(routeIndex);
             DsSipURL currentRRURL = (DsSipURL) rrHeader.getNameAddress().getURI();
             setRRHelper(msg, currentRRURL, compress, encode);
         }
     }

     private void setRRHelper(DsSipMessage msg, DsSipURL currentRRURL, boolean compress, DsTokenSipDictionary tokDic)
     {
         if (currentRRURL != null)
         {
             DsControllerConfig config = DsControllerConfig.getCurrent();
             String network = null;
             String name = config.checkRecordRoutes(currentRRURL.getUser(), currentRRURL.getHost(), currentRRURL.getPort(), currentRRURL.getTransportParam());
             if (name != null)
             {
                 //todo optimize when get a chance
                 Log.debug("Record Route URL to be modified : "+ currentRRURL);
                 DsByteString u = currentRRURL.getUser();
                 String user = null;
                 if (u != null)
                 {
                     try
                     {
                         user = DsSipURL.getUnescapedString(u).toString();
                     }
                     catch (DsException e)
                     {
                         Log.error("Error in unescaping the RR URI user portion", e);
                         user = u.toString();
                     }
                 }
                 if (user != null)
                 {
                     StringTokenizer st = new StringTokenizer(user);
                     String t = st.nextToken(DsReConstants.DELIMITER_STR);
                     while (t != null)
                     {
                         if (t.startsWith(DsReConstants.NETWORK_TOKEN))
                         {
                             network = t.substring(DsReConstants.NETWORK_TOKEN.length());
                             user = user.replaceFirst(t, DsReConstants.NETWORK_TOKEN + name);
                             break;
                         }
                         t = st.nextToken(DsReConstants.DELIMITER_STR);
                     }
                     currentRRURL.setUser(DsSipURL.getEscapedString(new DsByteString(user), DsSipURL.USER_ESCAPE_BYTES));
                 }
                 else
                 {
                     network = msg.getNetwork().getName();
                 }

                 Log.debug("Outgoing network of the message for which record route has to be modified : " + network);
                 DsSipRecordRouteHeader recordRouteInterfaceHeader = config.getRecordRouteInterface(network, false);

                 if (recordRouteInterfaceHeader == null)
                 {

                     Log.debug("Did not find the Record Routing Interface!");
                     return;
                 }

                 DsSipURL recordRouteInterface = (DsSipURL) recordRouteInterfaceHeader.getURI();

                 currentRRURL.setHost(recordRouteInterface.getHost());

                 if (recordRouteInterface.hasPort())
                 {
                     currentRRURL.setPort(recordRouteInterface.getPort());
                 }
                 else
                 {
                     currentRRURL.removePort();
                 }

                 if (recordRouteInterface.hasTransport())
                 {
                     currentRRURL.setTransportParam(recordRouteInterface.getTransportParam());
                 }
                 else
                 {
                     currentRRURL.removeTransportParam();
                 }
                 if (compress)
                 {
                     currentRRURL.setCompParam(DsSipConstants.BS_SIGCOMP);
                 }
                 else
                 {
                     currentRRURL.removeCompParam();
                     if (null != tokDic)
                     {
                         currentRRURL.setParameter(DsTokenSipConstants.s_TokParamName, tokDic.getName());
                     }
                     else
                     {
                         currentRRURL.removeParameter(DsTokenSipConstants.s_TokParamName);
                     }
                 }
                 Log.debug("Modified Record route URL to : " + currentRRURL);
             }
         }

     }
     private void setRecordRouteInterfaceStateless(DsSipMessage msg) throws DsException
     {
         DsSipHeaderList rrHeaders = msg.getHeadersValidate(DsSipConstants.RECORD_ROUTE);
         boolean compress = msg.shouldCompress();
         DsTokenSipDictionary encode = msg.shouldEncode();
         if (rrHeaders != null && rrHeaders.size() > 0)
         {
             for (int headerCount = 0; headerCount < rrHeaders.size(); headerCount++)
             {
                 DsSipRecordRouteHeader recordRouteHeader = (DsSipRecordRouteHeader) rrHeaders.get(headerCount);
                 setRRHelper(msg, (DsSipURL) recordRouteHeader.getURI(), compress, encode);
             }
         }
     }

    /**
      * returns site from the incoming {@link DsSipRequest}'s {@link DsSipURL}
      * 
      * @param request
      * @return site name
      */
    private String getSiteFromURI (DsSipRequest request) {
        DsSipURL url = (DsSipURL) request.getURI();
        if(url == null) {
            Log.warn("reqUri in incoming request is empty");
            return null;
        }
        Log.trace("reqUri in incoming request: {}", url);
        String hostName = url.getHost().toString();
        Log.info("siteName in incoming request to be looked up: {}", hostName);

        return hostName;
    }

    private String getSrvQueryString(String siteName) {
        String srvQueryString = null;
        switch (PROTOCOL_FOR_SRV_LOOKUP) {
            case (DsSipTransportType.STR_TCP):
                srvQueryString = "_sip._tcp." + siteName;
                break;
            case (DsSipTransportType.STR_UDP):
                srvQueryString = "_sip._udp." + siteName;
                break;
            case (DsSipTransportType.STR_TLS):
                srvQueryString = "_sips._tcp." + siteName;
                break;
            default:
                srvQueryString = "_sip._tcp." + siteName;
                break;
        }

        return srvQueryString;
    }

    private int checkIfSiteIsGeoEnabled(DsSipRequest request) {
        DnsResolver client = DnsClientService.getInstance().getClient();
        List<DsSRVWrapper> records = null;

        try {
            String siteName = getSiteFromURI(request);
            String srvQueryString = getSrvQueryString(siteName);
            records = client.resolveSRV(srvQueryString);
            if(records == null) {
                Log.info("SRV records for {} not found!", siteName);
                return GEO_DISABLED_SITE;
            }

            DsSRVWrapper geoEnabled = records.stream().filter(DsSRVWrapper::isHostGeoEnabled)
                                      .findFirst().orElse(null);

            if (geoEnabled != null)
                return GEO_ENABLED_SITE;
        } catch (DsProxyDnsException e) {
            Log.error("Exception while doing DNS lookup" + e);
            return DNS_LOOKUP_FAILURE;
        }

       return GEO_DISABLED_SITE;
    }

    private HashMap getGeoLookupHeaderValue(DsSipRequest request) {
        HashMap value = null;
        GeoHintService geoHintService = new GeoHintService();

        if (request != null) {
            value = geoHintService.getGeoHintForDomainOrIp(request);
        }

        return value;
    }

    void addCiscoGeoIPLookupInfoHeader(DsSipRequest request) {
        DsBindingInfo bindingInfo = request.getBindingInfo();
        boolean isMidCall = request.getToTag() != null;
        DsNetwork network = bindingInfo.getNetwork();
        if (request.getMethodID() == DsSipConstants.INVITE && !isMidCall && bindingInfo != null && network != null &&
                                     (network.getGeoLookup() || network.getWebexIngress() || network.getGeoDNSLookUp())) {
            DsSipHeaderInterface geoIPLookupHeader = request.getHeader(DsSipConstants.X_CISCO_GEO_IP_LOOKUP_INFO_STRING);
            if (geoIPLookupHeader != null) {
                /*
                   If X-Cisco-Geo-Location-Info header is already available in incoming Invite and domain is trusted, do nothing
                   Else Remove the header
                */
                boolean isPeerDomainTrusted = DsTlsUtil.isPeerDomainTrusted((DsSSLBindingInfo) bindingInfo);
                if (!isPeerDomainTrusted) {
                    request.removeHeaders(DsSipConstants.X_CISCO_GEO_IP_LOOKUP_INFO_STRING);
                    Log.warn("addCiscoGeoIPLookupInfoHeader(): Remove existing X-Cisco-Geo-Location-Info");
                }
                else
                    return ;
            }
            /*
                Add header with details
            */
            HashMap geoLookupHeaderValue = null;
            if (network.getGeoLookup())
                geoLookupHeaderValue = getGeoLookupHeaderValue(request);

            if (network.getWebexIngress()) {
                if (geoLookupHeaderValue == null)
                    geoLookupHeaderValue = new HashMap();

                geoLookupHeaderValue.put(DsSipConstants.X_CISCO_SIP_CLOUD_INGRESS_DC_STRING.toString(), WEBEX_INGRESS_REGION_CODE);
            }

            if (network.getGeoDNSLookUp()) {
                if (geoLookupHeaderValue == null)
                    geoLookupHeaderValue = new HashMap();

                switch(checkIfSiteIsGeoEnabled(request)) {
                    case GEO_DISABLED_SITE:
                        geoLookupHeaderValue.put(DsSipConstants.X_CISCO_SIP_CLOUD_GEO_DNS_DIALLED_STRING.toString(), "false");
                        break;
                    case GEO_ENABLED_SITE:
                        geoLookupHeaderValue.put(DsSipConstants.X_CISCO_SIP_CLOUD_GEO_DNS_DIALLED_STRING.toString(), "true");
                        break;
                    case DNS_LOOKUP_FAILURE:
                        geoLookupHeaderValue.remove(DsSipConstants.X_CISCO_SIP_CLOUD_GEO_DNS_DIALLED_STRING.toString());
                        Log.info("Not populating "+ DsSipConstants.X_CISCO_SIP_CLOUD_GEO_DNS_DIALLED_STRING.toString() +
                                " as DNS lookup failed");
                        break;
                }
            }

            if (geoLookupHeaderValue != null)
                request.addGeoIPLookupInfoHeader(geoLookupHeaderValue);
        }
    }

    public static void setM_Singleton(DsSipProxyManager m_Singleton) {
        DsSipProxyManager.m_Singleton = m_Singleton;
    }
}
