package com.cisco.dhruva.sip.controller;



import com.cisco.dhruva.sip.proxy.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipResolverUtils;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerLocator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.util.log.Trace;
import com.cisco.dhruva.config.sip.RE;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


/**
 * This is based on the original controller confing taken from the proxy code.
 * It has been scaled down to only include methods relevant to the Route Engine.
 */

public final class DsControllerConfig implements DsProxyParamsInterface, DsSipRouteFixInterface, Cloneable
{

    public static final byte UDP = (byte) DsSipTransportType.UDP;
    public static final byte SIGCOMP = 100;//(byte)DsSipTransportType.SIGCOMP;
    public static final byte TCP = (byte) DsSipTransportType.TCP;
    public static final byte NONE = (byte) DsSipTransportType.NONE;
    public static final byte MULTICAST = (byte) DsSipTransportType.MULTICAST;
    public static final byte TLS = (byte) DsSipTransportType.TLS;
    public static final int DEFAULT_TIMEOUT = -1;

    public static final byte STATEFUL = (byte) RE.index_dsReStateMode_stateful;
    public static final byte STATELESS = (byte) RE.index_dsReStateMode_stateless;
    public static final byte FAILOVER_STATEFUL = (byte) RE.index_dsReStateMode_failover_stateful;

    public static final String INBOUND = "inbound";//TODO both these values are bogus and are slated for removal
    public static final String OUTBOUND = "outbound";//TODO both these values are bogus and are slated for removal

    public static final String LISTEN_INTERNAL = "internal";//TODO both these values are bogus and are slated for removal
    public static final String LISTEN_EXTERNAL = "external";//TODO both these values are bogus and are slated for removal

    public static final short MASK_VIA = 1;

    public static final byte FAILOVER = 0;
    public static final byte DROP = 1;

    public static final String FAILOVER_TOKEN = "failover";
    public static final String DROP_TOKEN = "drop";

    protected static HashMap onNextHopFailureMap = null;
    protected byte stateMode = (byte) RE.getValidValueAsInt(RE.dsReStateMode, RE.dsReStateModeDefault);
    protected static Trace Log = Trace.getTrace(DsControllerConfig.class.getName());


    /**
     * Holds the domain names that indicate that a request URI/Route header was inserted by this RE
     */
    protected LinkedList ourRoutes = new LinkedList();
    protected HashMap popNames = new HashMap();

    //TODO optimize below, we don't need the objects created even before configured
    protected HashMap NetworkIf = new HashMap();
    protected HashMap NetworkIfMap = new HashMap();

    protected HashMap PathIf = new HashMap();
    protected HashMap PathIfMap = new HashMap();

    protected HashMap ViaIf = new HashMap();
    protected HashMap ViaIfMap = new HashMap();

    protected HashMap ViaListenHash = new HashMap();

    protected HashMap RecordRouteIf = new HashMap();
    protected HashMap RecordRouteIfMap = new HashMap();

    protected HashMap MaskIfMap = new HashMap();

    protected HashMap listenIf = new HashMap();

    protected HashMap AddServiceRouteIf = new HashMap();
    protected HashMap AddServiceRouteIfMap = new HashMap();

    protected HashMap ModifyServiceRouteIf = new HashMap();
    protected HashMap ModifyServiceRouteIfMap = new HashMap();


    protected int externalNetworkType = DsSipProxyManager.EXTERNAL_NETWORK;

    /**
     * The value set through the management console for the internal interface,
     * if this hashmap has not been configured, then the internalViaListenHash
     * will be populated from the current set of listen interfaces.
     */
    protected HashMap configuredInternalViaListenHash = new HashMap();

    /**
     * The value set through the management console for the external interface,
     * if this hashmap has not been configured, then the externalViaListenHash
     * will be populated from the current set of listen interfaces.
     */
    protected HashMap configuredExternalViaListenHash = new HashMap();

    protected HashMap internalViaListenHash = new HashMap();
    protected HashMap externalViaListenHash = new HashMap();
    protected HashMap viaMap = new HashMap();

    protected DsSipPathHeader path = null;

    //protected DsSipRecordRouteHeader recordRouteInternal = null;
    //protected DsSipRecordRouteHeader recordRouteExternal = null;
    protected HashMap recordRoutesMap = new HashMap();
    protected HashMap recordRoutesIf = new HashMap();


    // Default state is:
    //stateful with request-uri load balancing and no record route
    protected byte searchType = DsProxyController.SEARCH_PARALLEL;
    protected boolean isRecursing;
    protected boolean doRecordRoute;
    protected boolean doAddServiceRoute;
    protected boolean doModifyServiceRoute;
    protected int sequentialSearchTimeout = DsProxyController.SEQUENTIAL_SEARCH_TIMEOUT_DEFAULT;
    protected int defaultRetryAfterMilliSeconds = 0;

    protected boolean m_privacyEnabled = true;

    protected int defaultPort = 5060;

    // Controls if RE will send 100 responses to UDP non-INVITE requests
    //   protected boolean send100 = false;
//    protected int provisionalTimerValue = Integer.MAX_VALUE;
//  protected int internalProvisionalResponseTimer = 6000;
//  protected int externalProvisionalResponseTimer = Integer.MAX_VALUE;

    // the interval between two consecutive Timer cleanups of the
    // above hashmap. also, the time after which a particular entry
    // in the above callLegToNextHopMap should be removed. (milliseconds)
    private static int INTERVAL = 64000;

    // timer that removes entries from callLegToNextHopMap
    Timer timer = null;

    /**
     * default protocol for outgoing requests
     */
    protected byte defaultProtocol = UDP;
    protected boolean isListening = false;

    // Via Hiding parameters
    protected boolean viaHidingSet = false;
    protected byte viaHideValue = HIDE_NONE;

    protected int requestTimeout = DEFAULT_TIMEOUT;

    protected int m_iMaxRequestTimeout = DEFAULT_TIMEOUT;

    //TLS variables
    protected Hashtable tlsPeerList = new Hashtable();
    protected String tlsFileName = null;


    //next hop failure vaiues
    public static final byte NHF_ACTION_DROP = 8; //NHF stands for Next hop Failure :)
    public static final byte NHF_ACTION_FAILOVER = 9;

    protected byte nextHopFailureAction = NHF_ACTION_FAILOVER;


    protected boolean m_edgeProxyMode = false;
    // 	protected boolean				     m_wirelessEdgeProxyMode = false;
    // if set tto true, the RE will send headers in compact form
    protected boolean m_compactHeaderForm = false;
    // if set to true, the Edge Proxy will modify Contacts in incoming
    // REGISTER requests to point back to itself and will save the real
    // Contact info in hs-s and hs-p parameters
    protected boolean m_modifyContactHeaders = false;

    private byte m_NatMedia = NAT_MEDIA_OFF;
    //nat -media
    public static final byte NAT_MEDIA_OFF = 0;
    public static final byte NAT_MEDIA_ON = 1;
    public static final byte NAT_MEDIA_INTERNAL = 2;


    //on-next-hop-failure variables
    protected byte onNextHopFailure = FAILOVER;

    private static boolean m_FirewallInited = false;

    //for firewall

    private boolean m_firewallMode = false;


    /* The following two variables are used for dynamic
     * reconfiguration.
     * currentConfig refers to the most current copy
     * of the configuration
     * updated  true when some configuration settings
     * have been changed since the last getCurrent()
     */
    protected static DsControllerConfig currentConfig = null;
    protected static boolean updated = false;

    protected static final Integer[] protocols = new Integer[DsSipTransportType.ARRAY_SIZE];

    // added by ketul as a part of the Firewall merge,
    // can be removed later if not required.

    /**
     * default constructor is protected. It's only used from
     * clone() method.
     */
    static
    {
        currentConfig = new DsControllerConfig();
        onNextHopFailureMap = new HashMap(2);
        onNextHopFailureMap.put(DROP_TOKEN, new Integer(DROP));
        onNextHopFailureMap.put(FAILOVER_TOKEN, new Integer(FAILOVER));

        if (System.getProperty("failovercleanuptime") != null)
        {
            try
            {
                INTERVAL = Integer.parseInt(System.getProperty("failovercleanuptime"));
            }
            catch (Throwable t)
            {
                if (Log.on && Log.isEnabled(Level.ERROR))
                    Log.error("could not set the failovercleanuptime, using default value : " + INTERVAL);
            }
        }

        protocols[NONE] = new Integer(NONE);
        protocols[UDP] = new Integer(UDP);
        protocols[TCP] = new Integer(TCP);
        protocols[TLS] = new Integer(TLS);
    }


    private DsSipServerLocatorFactory dsSIPServerLocatorFactory = DsSipServerLocatorFactory.getInstance();

    /**
     * Our Constructor
     */
    private DsControllerConfig()
    {
    }

    /**
     * This is the only way to get access to current
     * configuration settings.
     *
     * @return the DsControllerConfig object containing the settings
     *         at the moment this method was called.
     */
    public synchronized static DsControllerConfig getCurrent()
    {
        updated = false;
        return currentConfig;
    }


    // added by ketul as a part of the firewall code merge
    // could be removed later if not required
    public static boolean isFirewallInited()
    {
        return m_FirewallInited;
    }

    /**
     * Method declaration
     *
     * @return
     * @see
     */
    public boolean isFirewallMode()
    {
        return m_firewallMode;
    }


    public boolean isNatMediaSet()
    {
        return m_NatMedia != NAT_MEDIA_OFF;
    }

    /**
     * Method declaration
     *
     * @return
     * @see
     */
    public byte getNatMediaType()
    {
        return m_NatMedia;
    }


    public String getRequestDirection()
    {
        // should never be called
        return null;
    }

    public DsByteString getRecordRouteUserParams()
    {
        // should never be called
        return null;
    }

    /**
     * adds a pop name added thru pop-names cli command
     *
     * @param name  pop element name to be removed
     * @param value pop element server group to be added
     */
    public static synchronized void addPopName(String name, String value)
    {
        update();

        DsByteString bsName = (new DsByteString(name)).toLowerCase();

        LinkedList list = (LinkedList) currentConfig.popNames.get(bsName);
        if (list == null)
            list = new LinkedList();

        list.add((new DsByteString(value)).toLowerCase());

        currentConfig.popNames.put(bsName, list);

        if (Log.on && Log.isDebugEnabled())
            Log.debug("addPopName() called with " + name + ", " + value +
                    "popNames=" + currentConfig.popNames.toString());


        if (name.equalsIgnoreCase("self"))
        {
            DsByteString byteDomain = (new DsByteString(value)).toLowerCase();
            currentConfig.ourRoutes.add(byteDomain);
        }
    }

    /**
     * Removes a pop name configured thru pop-names command
     *
     * @param name POP element name to be removed
     * @return true if the domain was in the HashMap previously, false otherwise
     */
    public static synchronized boolean removePopName(String name, String value)
    {
        update();

        DsByteString bsName = (new DsByteString(name)).toLowerCase();

        LinkedList list = (LinkedList) currentConfig.popNames.get(bsName);

        if (list == null)
        {
            if (Log.on && Log.isDebugEnabled())
                Log.debug("removePopName: No value associated with " + name);
            return false;
        }


        DsByteString bsValue = (new DsByteString(value)).toLowerCase();
        boolean obj = list.remove((new DsByteString(value)).toLowerCase());

        if (list.size() == 0)
            currentConfig.popNames.remove(bsName);

// 	Object obj = currentConfig.popNames.remove(new DsByteString(value));

        if (Log.on && Log.isDebugEnabled())
            Log.debug("removePopName() called with " + name +
                    ", removed object=" + obj + "popNames=" +
                    currentConfig.popNames.toString());

        if (name.equalsIgnoreCase("self") && obj)
        {
            currentConfig.ourRoutes.remove(bsValue.toLowerCase());
        }

        return(obj);
    }

    /**
     * returns the server group corresponding to configured POP element
     *
     * @param name POP element name to be removed
     * @return pop element server group name
     */
    public DsByteString getPopElement(DsByteString name)
    {
        LinkedList list = (LinkedList) popNames.get(name.toLowerCase());
        return(DsByteString) list.getFirst();
    }

    public void setDsSIPServerLocatorFactory(
            DsSipServerLocatorFactory dsSIPServerLocatorFactory) {
        this.dsSIPServerLocatorFactory = dsSIPServerLocatorFactory;
    }


    /**
     * This method is invoked by the DsSipRequest to perform the procedures
     * necessary to interoperate with strict routers.  For incoming requests,
     * the class which implements this interface is first asked to
     * recognize the request URI.  If the request URI is recognized, it is
     * saved internally by the invoking DsSipRequest as the LRFIX URI and
     * replaced by the URI of the bottom Route header.  If the request URI is
     * not recognized, the supplied interface is asked to recognize the URI of
     * the top Route header.  If the top Route header's URI is recognized, it
     * is removed and saved internally as the LRFIX URI.  If neither is
     * recognized, the DsSipRequest's  FIX URI is set to null.
     *
     * @param uri a URI from the SIP request as described above
     * @param isRequestURI true if the uri is the request-URI else false
     * @return true if the uri is recognized as a uri that was inserted
     *         into a Record-Route header, otherwise returns false
     */
    public boolean recognize(DsURI uri, boolean isRequestURI)
    {
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering recognize(" + uri + ", " + isRequestURI + ')');
        boolean b = false;
        if (uri.isSipURL())
        {
            DsSipURL url = (DsSipURL) uri;

            DsByteString host = null;
            int port = url.getPort();

            int transport = url.getTransportParam();
            if (transport == DsSipTransportType.NONE)
                transport = DsSipTransportType.UDP;

            DsByteString user = url.getUser();

            if(isRequestURI)
            {
                host = url.getHost();
                b = (null != checkRecordRoutes(user, host, port, transport));
                if (b && Log.on && Log.isDebugEnabled())
                    Log.debug("request-uri matches with one of Record-Route interfaces");
            }
            else
            {
                host = url.getMAddrParam();
                if (host == null)
                    host = url.getHost();
                b = recognize(user, host, port, transport);
            }
        }
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving recognize(), returning " + b);
        return b;
    }

    public boolean recognize(DsByteString user, DsByteString host, int port, int transport)
    {
        //Check Record-Route
        if (Log.on && Log.isDebugEnabled()) Log.debug("Checking Record-Route interfaces");
        if (null != checkRecordRoutes(user, host, port, transport))
            return true;

        //Check listeners
        if (Log.on && Log.isDebugEnabled()) Log.debug("Checking listen interfaces");
        ArrayList listenList = getListenPorts();
        if (listenList != null)
        {
            for (int i = 0; i < listenList.size(); i++)
            {
                if (isMyRoute(host, port, transport, (ListenIf) listenList.get(i)))
                    return true;
            }
        }

        //Check popid
        if (Log.on && Log.isDebugEnabled()) Log.debug("Checking pop-ids");
        if(ourRoutes.contains(host.toLowerCase()))
            return true;

        //Check Path
        if (Log.on && Log.isDebugEnabled()) Log.debug("Checking Path interface");
        if (null != checkPaths(user, host, port, transport))
            return true;

        return false;
    }

    public String checkPaths(DsByteString user, DsByteString host, int port, int transport)
    {
        if (user != null)
        {
            String usr = user.toString();
            if (usr.startsWith(DsReConstants.PR_TOKEN)
                    || usr.endsWith(DsReConstants.PR_TOKEN1)
                    || usr.contains(DsReConstants.PR_TOKEN2))
            {

                Set ps = PathIfMap.keySet();
                String key;
                for (Iterator i = ps.iterator(); i.hasNext();)
                {
                    key = (String) i.next();
                    HashMap map = (HashMap) PathIfMap.get(key);
                    if (map != null)
                    {
                        PathObj path = (PathObj) map.get(new Integer(transport));
                        if (path != null)
                        {
                            if (DsProxyUtils.recognize(host, port, transport, (DsSipURL) path.getPathHeader().getURI()))
                                return key;
                        }
                    }
                }
            }
        }
        return null;
    }

    public String checkRecordRoutes(DsByteString user, DsByteString host, int port, int transport)
    {
        if (user != null)
        {
            String usr = user.toString();
            if (usr.startsWith(DsReConstants.RR_TOKEN)
                    || usr.endsWith(DsReConstants.RR_TOKEN1)
                    || usr.contains(DsReConstants.RR_TOKEN2))
            {
                Set rrs = recordRoutesMap.keySet();
                String key;
                for (Iterator i = rrs.iterator(); i.hasNext();)
                {
                    key = (String) i.next();
                    DsSipRecordRouteHeader rr = (DsSipRecordRouteHeader) recordRoutesMap.get(key);
                    if (rr != null)
                    {
                        if (DsProxyUtils.recognize(host, port, transport, (DsSipURL) rr.getURI()))
                            return key;
                    }
                }
            }
        }
        return null;
    }


    public DsSipRecordRouteHeader getRecordRouteInterface(int index)
    {
        Object o = recordRoutesIf.get(new Integer(index));
        return getRecordRouteInterface((String) o);
    }

    /**
     * Fetches the record-route interface based on the specified direction
     *
     * @param direction The direction in which we need to get the record route interface.
     * @return record route interface based on the specified direction or null if does not exist.
     */
    public DsSipRecordRouteHeader getRecordRouteInterface(String direction)
    {
        return getRecordRouteInterface(direction, true);
    }

    public DsSipRecordRouteHeader getRecordRouteInterface(String direction, boolean clone)
    {
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering getRecordRouteInterface(" + direction + ')');
        if (Log.on && Log.isDebugEnabled())
            Log.debug("recordRoutesMap contains :\n" + recordRoutesMap.toString() + '\n');
        DsSipRecordRouteHeader rrHeader = (DsSipRecordRouteHeader) recordRoutesMap.get(direction);
        if (rrHeader != null && clone)
        {
            rrHeader = (DsSipRecordRouteHeader) rrHeader.clone();
        }
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving getRecordRouteInterface() returning: " + rrHeader);
        return rrHeader;
    }

    public static void setFirewallInited()
    {
        m_FirewallInited = true;
    }

    /**
     * @return returns the <CODE>ArrayList</CODE>
     *         list containing <CODE>ListenIf</CODE> Objects
     */
    public ArrayList getListenPorts()
    {
        return new ArrayList(listenIf.values());
    }


    public synchronized static void addListenInterface(String direction, String ip,
                                                       int port, int protocol, String translatedIP)
            throws UnknownHostException, DsInconsistentConfigurationException,
            DsException, IOException
    {

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering addListenInterface() ");
        update();
        if (ip == null || ip.length() == 0)
        {
            throw new DsInconsistentConfigurationException("ip cannot be null or empty");
        }

        if (translatedIP == null || translatedIP.length() == 0)
        {
            throw new DsInconsistentConfigurationException("translatedIP cannot be null or empty");
        }

        if (protocol != TCP && protocol != UDP && protocol != TLS && protocol != MULTICAST)
            throw new DsInconsistentConfigurationException("Unknown protocol specified");

        //Create the InetAddress we will use to create the ListenIf
        InetAddress address = InetAddress.getByName(ip);
        InetAddress translatedAddress = InetAddress.getByName(translatedIP);


        if (Log.on && Log.isDebugEnabled())
            Log.debug("Using InetAddress: " + address + " to create the ListenIf");
        ListenIf newInterface = new ListenIf(port, protocol, new DsByteString(ip), address, direction,
                new DsByteString(translatedIP),translatedAddress, 0);

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Created the ListenIf: " + newInterface);
        if (currentConfig.listenIf.containsKey(newInterface))
            throw new DsInconsistentConfigurationException("Entry already exists");

        if (currentConfig.defaultProtocol == NONE)
        {
            currentConfig.defaultProtocol = normalizedProtocol(protocol);
        }
        else if (normalizedProtocol(protocol) == UDP && currentConfig.defaultProtocol != UDP)
        {
            currentConfig.defaultProtocol = UDP;
        }

        //Start listening
        newInterface.startListening();
        //Store the interface in two different HashMaps.  One is indexed by index (SNMP),
        //while the other is by interface.  The second allows faster checks at add time
        //to ensure that we aren't adding the same interface twice.
        currentConfig.listenIf.put(newInterface, newInterface);

        //currentConfig.listenHash.put(newInterface, new Integer(index));

        if (Log.on && Log.isDebugEnabled())
            Log.debug("addListenInterface() - New list of interfaces we are listening on is: " + currentConfig.listenIf.keySet());

    }

    /**
     * Removes port/protocol from the list.
     * Note that this does NOT affect what TransportLayer is doing
     * in any way; the TransportLayer needs to be configured separetly.
     * I only need this info in ProxyConfig to deduce port values to be
     * placed in Via field.
     *
     * @param port     port to stop listening on
     * @param protocol DsProxyConfig.UDP or DsProxyConfig.TCP
     */

    public synchronized static void removeListenInterface(String direction, String ip, int port, int protocol) throws DsInconsistentConfigurationException, UnknownHostException
    {
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering removeListenInterface( port, protocol, addr ) ");

        if (ip == null || ip.length() == 0)
        {
            throw new DsInconsistentConfigurationException("ip cannot be null or empty");
        }

        if (protocol != TCP && protocol != UDP && protocol != TLS && protocol != MULTICAST)
            throw new DsInconsistentConfigurationException("Unknown protocol specified");

        ListenIf interfaceToRemove;
        //Create the InetAddress we will use to create the temporaryListenIf
        InetAddress address = InetAddress.getByName(ip);
        //Create the temporary ListenIf to do our hash lookup with
        interfaceToRemove = new ListenIf(port, protocol, new DsByteString(ip), address, direction,
                null, null, 0);

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Removing: port = " + port + "protocol = " + protocol + " ip = " + ip);

        ListenIf interfaceRemoved = (ListenIf) currentConfig.listenIf.remove(interfaceToRemove);

        if (interfaceRemoved != null)
        {

            interfaceRemoved.stopListening();

            removeViaListenInterface(interfaceRemoved);

            if (normalizedProtocol(protocol) == currentConfig.defaultProtocol)
            {
                currentConfig.defaultProtocol = getNewDefaultProtocol();
            }
        }
        else
        {
            if (Log.on && Log.isEnabled(Level.WARN))
                Log.warn("Leaving removeListenInterface() - Unable to remove port = " + port + "prot= " +
                        protocol + " ip = " + ip);
        }

    }


    public boolean isListenInterfaceSet()
    {
        isListening = !listenIf.isEmpty();

        return isListening;
    }

    /*
     * Implementation of the corresponding DsProxyParamsInterface method.  Returns
     * the first interface in our hashmap that is useing the specified protocol.
     */
    public DsListenInterface getInterface(int protocol, String direction)
    {

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering getInterface( protocol, direction) " + protocol + ' ' + direction);

        Iterator listenEntries = listenIf.values().iterator();

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Current listen entries are: " + listenIf.keySet());

        while (listenEntries.hasNext())
        {
            ListenIf li = (ListenIf) listenEntries.next();
            if (Log.on && Log.isDebugEnabled())
                Log.debug("Comparing " + li.getProtocol() + ' ' + li.getDirection() + " with " + protocol + ' ' + direction);
            if (li.getProtocol() == protocol && li.getDirection().equals(direction))
            {
                return li;
            }
        }
        return null;    // nothing is found
    }


    /*
     * Implementation of the corresponding DsProxyParamsInterface method.  Returns
     * the first interface in our hashmap that is useing the specified protocol.
     */
    public DsListenInterface getInterface(int protocol)
    {

        return getInterface(protocol, LISTEN_INTERNAL);
    }

    // THERE IS PROBABLY A BETTER WAY TO DO THIS - JPS - PERF
    // GENERATE A BINDING IDENTIFIER AT THE USER AGENT LEVEL
    // AND USE THAT AS THE INDEX

    /**
     * Returns the interface stored by the config that has the address, port and
     * protocol passed in.  If no interface is found, then null is returned.
     */
    public DsListenInterface getInterface(InetAddress address, int prot, int port)
    {

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering getInterface( address, prot, prot )");

        ListenIf lookupIf = new ListenIf(port, prot, address);

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Checking to see if the listenIf: " + listenIf.keySet() + " contains the interface: " + lookupIf);

        return(DsListenInterface)currentConfig.listenIf.get(lookupIf);
    }


    /**
     * Return the via interface which has been explicitly configured.  If the internal
     * interface has been configured, it will be returned, if it has not and an external
     * interface has been configured, then the external interface will be returned.
     */
    public DsViaListenInterface getConfiguredViaInterface(int protocol)
    {

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering getConfiguredViaInterface(protocol)");

        DsViaListenInterface viaIf = getConfiguredViaInterface(protocol, LISTEN_INTERNAL);

        if (viaIf == null)
        {

            if (Log.on && Log.isDebugEnabled())
                Log.debug("The internal interface was null, going to try and get the external interface");

            viaIf = getConfiguredViaInterface(protocol, LISTEN_EXTERNAL);
        }

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving getConfiguredViaInterface(protocol) with return value: "
                    + viaIf);

        return viaIf;
    }

    /**
     * Return the via interface which has been explicitly configured for the
     * specfied direction.
     */
    public DsViaListenInterface getConfiguredViaInterface(int protocol, String direction)
    {

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering getConfiguredViaInterface(), protocol=" + protocol + ", direction=" + direction);

        if (direction.equals(LISTEN_INTERNAL))
        {
            DsViaListenInterface viaInf = (DsViaListenInterface) currentConfig.configuredInternalViaListenHash.get(new Integer(protocol));
            if (Log.on && Log.isDebugEnabled())
                Log.debug("return internal via interface=" + viaInf);
            return viaInf;
        }
        else
        {
            DsViaListenInterface viaInf = (DsViaListenInterface) currentConfig.configuredExternalViaListenHash.get(new Integer(protocol));
            if (Log.on && Log.isDebugEnabled())
                Log.debug("return external via interface=" + viaInf);
            return viaInf;
        }
    }

    /**
     * Return the Via interface for this protocol and direction.  If we are listening
     * on this protocol/direction, then we will create a Via Interface based on that
     * listener, store it for the next lookup, and return it.
     */
    public DsViaListenInterface getViaInterface(int protocol, String direction)
    {

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering getViaInterface(protocol, direction)");

        //Grab the via interface if it has already been stored by protocol and direction
        DsViaListenInterface viaIf;

        viaIf = getVia(protocol, direction);
        if (viaIf == null)
        {
            viaIf = (DsViaListenInterface) ViaListenHash.get(new Integer(protocol));
        }
        /*
        //TODO remove this check and replace it with new via configuration map
        if (direction == LISTEN_INTERNAL) {
          //First try and get a via that has been configured, if one hasn't been configured
          //then try to get one that the controller config is storing which is based on a
          //listen interface.
          if ((viaIf = (DsViaListenInterface) configuredInternalViaListenHash.get(new Integer(protocol))) == null)
            viaIf = (DsViaListenInterface) internalViaListenHash.get(new Integer(protocol));
        }
        else {
          //First try and get a via that has been configured, if one hasn't been configured
          //then try to get one that the controller config is storing which is based on a
          //listen interface.
          if ((viaIf = (DsViaListenInterface) configuredExternalViaListenHash.get(new Integer(protocol))) == null)
            viaIf = (DsViaListenInterface) externalViaListenHash.get(new Integer(protocol));
        }
        */
        //Store a via interface if it hasn't be stored yet
        if (viaIf == null)
        {

            if (Log.on && Log.isDebugEnabled())
                Log.debug("No via interface stored for this protocol/direction pair, creating one");

            //Find a listen if with the same protocol and direction, if there is more
            //than one the first on will be selected.
            DsListenInterface tempInterface = getInterface(protocol, direction);
            if (tempInterface != null)
            {
                try
                {
                    viaIf = new ViaListenIf(tempInterface.getPort(),
                            tempInterface.getProtocol(),
                            tempInterface.getAddress(),
                            direction,
                            -1, null, null, null, -1);
                }
                catch (UnknownHostException unhe)
                {
                    if (Log.on && Log.isEnabled(Level.ERROR))
                        Log.error("Couldn't create a new via interface", unhe);
                    return null;
                }
                catch (DsException e)
                {
                    if (Log.on && Log.isEnabled(Level.ERROR))
                        Log.error("Couldn't create a new via interface", e);
                    return null;
                }
                HashMap viaListenHashDir = (HashMap) ViaListenHash.get(direction);
                if (viaListenHashDir == null)
                {
                    viaListenHashDir = new HashMap();
                    ViaListenHash.put(direction, viaListenHashDir);
                }
                viaListenHashDir.put(new Integer(protocol), viaIf);
            }
        }

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving getViaInterface(+ " + protocol + ", " + direction + " ) with return value: " + viaIf);

        return viaIf;
    }

    /*
     * Overwrites the current via being used for the specified protocol
     */
    public synchronized static void setViaInterface(int port, int protocol, String ipAddress,
                                                    int srcPort, InetAddress sourceAddress) throws UnknownHostException, DsException
    {
        setViaInterface(port, protocol, ipAddress, srcPort,
                sourceAddress, DsControllerConfig.LISTEN_INTERNAL);
    }

    /**
     * Sets the current via being used for the specified protocol.  If one was already
     * configured, it is overwritten.
     */
    public synchronized static void setViaInterface(int port, int protocol, String ipAddress,
                                                    int srcPort, InetAddress sourceAddress,
                                                    String direction) throws UnknownHostException, DsException
    {
        if (Log.isEnabled(Level.DEBUG))
            Log.debug("Entering setViaInterface()");

        update();
        ViaListenIf newViaIf = new ViaListenIf(port, protocol, new DsByteString(ipAddress),
                direction, srcPort, sourceAddress,
                null,null, 0);

        //Set the via configured via interface and remove any interfaces we were using for
        //this protocol, direction before (which were based on the listen intefaces).
        HashMap viaListenHash = (HashMap) currentConfig.viaMap.get(direction);
        viaListenHash.put(new Integer(protocol), newViaIf);
        if (Log.isEnabled(Level.DEBUG))
            Log.debug("Via Map for direction " + direction + " is " + viaListenHash);
        /*
        if (direction == LISTEN_INTERNAL) {
          currentConfig.configuredInternalViaListenHash.put(new Integer(protocol), newViaIf);
          currentConfig.internalViaListenHash.remove(new Integer(protocol));
        }
        else {
          currentConfig.configuredExternalViaListenHash.put(new Integer(protocol), newViaIf);
          currentConfig.externalViaListenHash.remove(new Integer(protocol));
        }

        if (Log.isEnabledFor(Level.DEBUG))
          Log.debug( "internalViaHash=" + currentConfig.configuredInternalViaListenHash);
        if (Log.isEnabledFor(Level.DEBUG))
          Log.debug( "externalViaHash=" + currentConfig.configuredExternalViaListenHash);
        */
        if (Log.isEnabled(Level.DEBUG))
            Log.debug("Leaving setViaInterface()");
    }

    /**
     * Remove the via interface that was explicitly configured.
     */
    public synchronized static void removeConfiguredViaInterface(int protocol, String direction)
    {
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering removeConfiguredViaInterface(), protocol="
                    + protocol + ", direction=" + direction);
        /*
        if (direction == LISTEN_INTERNAL) {
          if (Log.on && Log.isDebugEnabled())
            Log.debug("configuredInternalViaListenHash=" + currentConfig.configuredInternalViaListenHash);
          if (currentConfig.configuredInternalViaListenHash.get(new Integer(protocol)) != null) {
            update();

            if (Log.on && Log.isDebugEnabled())
              Log.debug("Removing internal via interface for protocol " + protocol);

            currentConfig.configuredInternalViaListenHash.remove(new Integer(protocol));
          }
        }
        else {
          if (Log.on && Log.isDebugEnabled())
            Log.debug("configuredInternalViaListenHash=" + currentConfig.configuredExternalViaListenHash);
          if (currentConfig.configuredExternalViaListenHash.get(new Integer(protocol)) != null) {
            update();

            if (Log.on && Log.isDebugEnabled())
              Log.debug("Removing external via interface for protocol " + protocol);

            currentConfig.configuredExternalViaListenHash.remove(new Integer(protocol));
          }
        }
        */
        HashMap viaListenHash = (HashMap) currentConfig.viaMap.get(direction);
        update();
        viaListenHash.remove(new Integer(protocol));
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Removing via interface for protocol " + protocol);
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving removeConfiguredViaInterface(protocol, direction )");

    }

    public synchronized static void removeViaInterface(int protocol)
    {
        removeViaInterface(protocol, LISTEN_INTERNAL);
    }

    /**
     * Removes the via interface that was build from a listen interface implicitly.
     * Each call to getViaInterface will add a via interface if one has not been set
     * explicitly.  The method will reset that interface.
     */

    public synchronized static void removeViaInterface(int protocol, String direction)
    {
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering removeViaInterface(protocol, direction )");
        /*
        if (direction == LISTEN_INTERNAL) {
          if (currentConfig.internalViaListenHash.get(new Integer(protocol)) != null) {
            update();

            if (Log.on && Log.isDebugEnabled())
              Log.debug("Removing internal via interface for protocol " + protocol);

            currentConfig.internalViaListenHash.remove(new Integer(protocol));
          }
        }
        else {
          if (currentConfig.externalViaListenHash.get(new Integer(protocol)) != null) {
            update();

            if (Log.on && Log.isDebugEnabled())
              Log.debug("Removing external via interface for protocol " + protocol);

            currentConfig.externalViaListenHash.remove(new Integer(protocol));
          }
        }
        */
        HashMap viaListenHash = (HashMap) currentConfig.viaMap.get(direction);
        update();
        viaListenHash.remove(new Integer(protocol));
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Removing via interface for protocol " + protocol);
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving removeViaInterface(protocol, direction )");
    }

    public boolean isViaInterfaceSet(int protocol)
    {
        return isViaInterfaceSet(protocol, LISTEN_INTERNAL);
    }

    public boolean isViaInterfaceSet(int protocol, String direction)
    {
        /*
        if (direction == LISTEN_INTERNAL) {
          //if( internalViaListenHash.get( new Integer(protocol) ) != null ) {
          if (configuredInternalViaListenHash.get(new Integer(protocol)) != null) {
            if (Log.on && Log.isDebugEnabled())
              Log.debug("Returning true from isViaInterfaceSet for protocol " + protocol +
                        " direction " + direction);
            return true;
          }
        }
        else {
          if (configuredExternalViaListenHash.get(new Integer(protocol)) != null) {
            if (Log.on && Log.isDebugEnabled())
              Log.debug("Returning true from isViaInterfaceSet for protocol " + protocol +
                        " direction " + direction);
            return true;
          }
        }
        */
        HashMap viaListenHash = (HashMap) currentConfig.viaMap.get(direction);
        if (null != viaListenHash)
        {
            if (null != viaListenHash.get(new Integer(protocol)))
            {
                if (Log.on && Log.isDebugEnabled())
                    Log.debug("Returning true from isViaInterfaceSet for protocol " + protocol +
                            " direction " + direction);
                return true;
            }
        }
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Returning false from isViaInterfaceSet for protocol " + protocol +
                    " direction " + direction);
        return false;
    }

    /**
     * Sets the current path being used for the specified protocol.  If one was already
     * configured, it is overwritten.
     */
/*
  public synchronized static void setPathInterface(String ipAddress,
                                                   int port,
                                                   int protocol) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering setPathInterface(" + ipAddress + ", " + port + ", " + protocol + ")");

    DsSipURL url = new DsSipURL(DsByteString.BS_EMPTY_STRING, new DsByteString(ipAddress));
    url.setLRParam();
    if (port > 0) url.setPort(port);
    if (protocol != DsSipTransportType.NONE) url.setTransportParam(protocol);
    if (protocol == TLS) url.setSecure(true);
    if (currentConfig.path == null || !currentConfig.path.getURI().equals(url)) {
      update();
      currentConfig.path = new DsSipPathHeader(url);
    }
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving setPathInterface()");
  }
*/

    /**
     * Removes the path interface that was build from a listen interface implicitly.
     * Each call to getPathInterface will add a path interface if one has not been set
     * explicitly.  The method will reset that interface.
     */
/*
  public synchronized static void removePathInterface() {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering removePathInterface()");

    if (currentConfig.path != null) {
      update();
      currentConfig.path = null;
    }

    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving removePathInterface()");
  }
*/
    public DsSipPathHeader getPathInterface(int transport, String direction)
    {
        PathObj path = getPath(transport, direction);
        if (path != null)
        {
            return path.getPathHeader();
        }
        return null;
    }

    /**
     * Sets the Next hop failure action
     */
    public synchronized static void setNextHopFailureAction(String action)
    {
        byte actionInt;

        //no need to check the validity of the action string
        // as it is already done for us by the CLI.

        if (action.equalsIgnoreCase("drop"))
            actionInt = NHF_ACTION_DROP;
        else
            actionInt = NHF_ACTION_FAILOVER;

        if (actionInt == currentConfig.nextHopFailureAction)
            return;

        update();
        currentConfig.nextHopFailureAction = actionInt;

    }

    /**
     * getter for the Next hop failure action
     */
    public synchronized byte getNextHopFailureAction()
    {
        return currentConfig.nextHopFailureAction;

    }

    /**
     * Checks to see whether we are currently listening on the specified
     * address and port
     */
    public boolean isListeningOn(InetAddress addr, int port, int protocol)
    {
        if (listenIf == null)
            return false;

        ListenIf tempListenIf = new ListenIf(port, protocol, addr);

        return listenIf.containsKey(tempListenIf);
    }


    /**
     * Removes the specified via listen interface from the appropriate via
     * listen HashMap.  If the via listen HashMap does not contain the
     * specified interface, no action is taken.
     *
     * @param listenIf The interface which should be removed from the via
     *                 listen HashMap.
     */
    protected synchronized static void removeViaListenInterface(ListenIf listenIf)
    {
        Integer protocol = new Integer(listenIf.getProtocol());
        //TODO remove below check and update to direction and protocol lookup
        if (listenIf.getDirection().equals(LISTEN_INTERNAL))
        {
            ListenIf viaListenIf = (ListenIf) currentConfig.internalViaListenHash.get(protocol);

            if ((viaListenIf != null) && listenIf.equals(viaListenIf))
                currentConfig.internalViaListenHash.remove(protocol);
        }
        // LISTEN_EXTERNAL
        else
        {
            ListenIf viaListenIf = (ListenIf) currentConfig.externalViaListenHash.get(protocol);

            if ((viaListenIf != null) && listenIf.equals(viaListenIf))
                currentConfig.externalViaListenHash.remove(protocol);
        }
    }


    /**
     * normalizes the protocol value to either UDP, TCP
     */
    public static byte normalizedProtocol(int protocol)
    {
        if ((protocol != DsControllerConfig.TCP) && (protocol != DsControllerConfig.TLS))
        {
            return DsControllerConfig.UDP;
        }

        return(byte) protocol;
    }


    /**
     * Removes all ports from the Listener list.
     */

    public synchronized static void removeAllListenInterfaces()
    {
        update();

        Iterator iter = currentConfig.listenIf.values().iterator();

        while (iter.hasNext())
        {
            ListenIf listen = (ListenIf) iter.next();
            listen.stopListening();
            iter.remove();
        }

        currentConfig.listenIf = new HashMap();
        //currentConfig.listenHash = new HashMap();
        currentConfig.defaultProtocol = NONE;
    }

    /* Stops all listen interfaces. Required by ConfigValidator to
     * insert mocks before the listeners are configured. This will
     * avoid having to reconfigure cp for each test case.
     */
    public synchronized static void stopAllListenInterfaces()
    {
        update();

        Iterator iter = currentConfig.listenIf.values().iterator();

        while (iter.hasNext())
        {
            ListenIf listen = (ListenIf) iter.next();
            listen.stopListening();
        }
    }

    /* Starts all listen interfaces. Required by ConfigValidator to
     * insert mocks before the listeners are configured. This will
     * avoid having to reconfigure cp for each test case.
     */
    public synchronized static void startAllListenInterfaces() throws DsException, IOException
    {
        update();

        Iterator iter = currentConfig.listenIf.values().iterator();

        while (iter.hasNext())
        {
            ListenIf listen = (ListenIf) iter.next();
            listen.startListening();
        }
    }

    /**
     * Allows to overwrite SIP default port 5060
     *
     * @param port port number to use instead of 5060
     */
    public synchronized static void setDefaultPort(int port)
    {
        if (port > 0 && port != currentConfig.defaultPort)
        {
            update();

            currentConfig.defaultPort = port;
        }
    }

    /**
     * Sets the default protocol to use for outgoing requests
     *
     * @param protocol one of DsProxyConfig.UDP or DsProxyConfig.TCP;
     *                 any other value will be converted to UDP
     */
    public synchronized static void setDefaultProtocol(int protocol)
    {
        if (currentConfig.defaultProtocol != protocol)
        {
            update();

            currentConfig.defaultProtocol = normalizedProtocol(protocol);
        }
    }


    /**
     * Method declaration
     *
     * @return
     * @see
     */
    public synchronized static byte getNewDefaultProtocol()
    {
        boolean tcpFlag = false;

        Iterator iter = currentConfig.listenIf.values().iterator();

        while (iter.hasNext())
        {
            ListenIf li = (ListenIf) iter.next();
            int prot = li.getProtocol();

            if (normalizedProtocol(prot) == UDP)
            {
                return(byte) prot;
            }
            else
            {
                tcpFlag = true;
            }
        }

        if (tcpFlag)
        {
            return TCP;

        }

        return NONE;
    }


    public synchronized static void addRecordRouteInterface(int index, String interfaceIP,
                                                            int port, int protocol,
                                                            String direction) throws Exception
    {

        //REDDY_RR_CHANGE
        // add to another map to store the RR in a index based list to facilitate addition and removal

        addRecordRouteInterface(interfaceIP, port, protocol, direction);
        currentConfig.recordRoutesIf.put(new Integer(index), direction);
    }

    public synchronized static void addRecordRouteInterface(String interfaceIP, int port,
                                                            int protocol, String direction) throws Exception
    {

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering setRecordRouteInterface() ");

        try
        {
            if (DsNetwork.findNetwork(direction) == null)
                throw new Exception("Network does not exist");

        }
        catch (DsException e)
        {
            throw new Exception(e.getMessage());
        }

        update();
        currentConfig.doRecordRoute = true;

        //SimpleListenIf listenIf = new SimpleListenIf(port, protocol, interfaceIP, direction );
        //ListenIf newListenIf = new ListenIf( port, protocol, interfaceIP, direction, null, 0);
        // we will not verify the ip address for hostname of Record Route. Bug #4349.
        DsSipURL sipURL = null;
        try
        {
            InetAddress addr = InetAddress.getByName(interfaceIP);

		/* check if an IPAddress was passed if yes check translated ip address
		 if domain is passed do not pass convert translated IPAddress for RR
		 since the FQDN would resolve to external IP

		 TODO:
		 We are not validating the hostname with our interface IP's ,we are blindly trusting
		 the configuration. This could be changed to resolve and compare the hostname against our
		 interface so that we don't allow invalid hostname
		 */

            if (addr.getHostAddress().equals(interfaceIP)) {
                ListenIf listenIf = (ListenIf) DsControllerConfig.getCurrent()
                        .getInterface(InetAddress.getByName(interfaceIP), protocol, port);
                DsByteString translatedIp = null;
                if (listenIf != null)
                    translatedIp = listenIf.getTranslatedAddress();

                if (translatedIp != null) {
                    sipURL = new DsSipURL(DsByteString.BS_EMPTY_STRING, translatedIp);
                }
            }
        }catch(UnknownHostException e)
        {
            Log.error("Failed to resolve hostname ", e);
        }

        if (sipURL == null) {
            sipURL = new DsSipURL(DsByteString.BS_EMPTY_STRING, new DsByteString(interfaceIP));
        }

        if (port > 0) sipURL.setPort(port);
        if (protocol != DsSipTransportType.NONE) sipURL.setTransportParam(protocol);
        sipURL.setLRParam();
        DsSipRecordRouteHeader rr = new DsSipRecordRouteHeader(sipURL);

        currentConfig.recordRoutesMap.put(direction, rr);

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Setting record route(" + rr + ") on network: " + direction);

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving setRecordRouteInterface() ");

    }

    public synchronized static void removeRecordRouteInterface(int index)
    {
        Integer i = new Integer(index);
        Object o = currentConfig.recordRoutesIf.get(i);
        currentConfig.removeRecordRouteInterface((String) o);
        currentConfig.recordRoutesIf.remove(i);
    }

    public synchronized static void removeRecordRouteInterface(String direction)
    {
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering removeRecordRouteInterface(direction) with direction = " + direction);
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Removing record route on :" + direction);
        currentConfig.recordRoutesMap.remove(direction);

        if (currentConfig.recordRoutesMap.size() == 0)
        {
            currentConfig.doRecordRoute = false;
        }
        /*
        switch (direction) {
          case LISTEN_EXTERNAL:
            //case INBOUND:
            if (currentConfig.recordRouteExternal != null) {
              update();

              if (Log.on && Log.isDebugEnabled())
                Log.debug( "Removing external record route interface ");
              recordRoutesMap.remove("external");
              currentConfig.recordRouteExternal = null;
            }
            break;
          default:
            if (currentConfig.recordRouteInternal != null) {
              update();
              recordRoutesMap.remove("internal");
              if (Log.on && Log.isDebugEnabled())
                Log.debug( "Removing internal record route interface ");

              currentConfig.recordRouteInternal = null;
            }
            break;
        }

        if (currentConfig.recordRouteInternal == null &&
          currentConfig.recordRouteExternal == null)
          currentConfig.doRecordRoute = false;
        */
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving removeRecordRouteInterface(direction)");
    }

    /**
     *
     */

    public synchronized static void setStateful()
    {

        if (STATEFUL != currentConfig.stateMode)
        {
            update();
            currentConfig.stateMode = STATEFUL;
            if (currentConfig.timer != null)
            {
                DsNextHopTable nextHopTable = DsNextHopTable.getInstance();
                nextHopTable.removeAllCallLegNextHop();
                currentConfig.cancelTimer();
            }
        }
    }

    /**
     * sets the proxy to operate in stateless mode.
     */

    public synchronized static void setStateless()
    {
        if (STATELESS != currentConfig.stateMode)
        {
            update();
            currentConfig.stateMode = STATELESS;
            if (currentConfig.timer != null)
            {
                DsNextHopTable nextHopTable = DsNextHopTable.getInstance();
                nextHopTable.removeAllCallLegNextHop();
                currentConfig.cancelTimer();
            }
        }
    }

    /**
     * sets the proxy to operate in failover_stateful mode.
     */

    public synchronized static void setFailoverStateful()
    {

        if (FAILOVER_STATEFUL != currentConfig.stateMode)
        {
            update();
            currentConfig.stateMode = FAILOVER_STATEFUL;
            currentConfig.scheduleTimer();
        }
    }

    /**
     * Returns SIP default port number (5060 unless
     * overwritten with setDeafultPort)
     */
    public synchronized int getDefaultPort()
    {
        return defaultPort;
    }

    /**
     * @return the default protocol to be used for outgoing requests
     */
    public synchronized int getDefaultProtocol()
    {
        return defaultProtocol;
    }


    /**
     * @return the proxyToProtocol to be used for outgoing requests
     */
    public int getProxyToProtocol()
    {
        return NONE;
    }

    /**
     * True means that the proxy will insert itself
     * in the Record-Route by default.
     * Part of DsProxyBranchParamsInterface
     *
     * @return if true, proxy will insert itself in
     *         the Record-Route
     */
    public boolean doRecordRoute()
    {
        return doRecordRoute;
    }

    /**
     * if set to true, RE will generate 100 response for UDP non-INVITE requests,
     * otherwise, it won't
     *
     * @param network network on this to set the send 100 timer
     * @param timer timer value for sending 100
     */
    public static void setSend100(String network, String timer)
    {
        try
        {
            if (Log.on && Log.isDebugEnabled())
                Log.debug("entering setSend100");

            int timerValue;

            if (timer != null) // If time is equal to null set it to INT_MAX
            {
                Integer i = new Integer(timer);
                timerValue = i.intValue();
            }
            else
            {
                timerValue = Integer.MAX_VALUE;
            }

            if (timerValue != getSend100(network))
            {
                update();
                DsConfigManager.setTimerValue(DsNetwork.getNetwork(network),
                        new Byte(DsSipConstants.TU3).byteValue(),
                        timerValue);
            }

            if (Log.on && Log.isDebugEnabled())
                Log.debug("sending 100 provisional is turned on for non invites with a TU3 timer value of : " + timerValue+" for network : "+network);
        }
        catch (DsException e)
        {
            Log.warn("Couldn't get network "+network, e);
        }
    }


    private static int getSend100(String network)
    {
        int i = Integer.MAX_VALUE;
        try
        {
            i = DsConfigManager.getTimerValue(DsNetwork.getNetwork(network), new Byte(DsSipConstants.TU3).byteValue());
        }
        catch (DsException e)
        {
            Log.warn("Couldn't get network "+network+".", e);
        }
        return i;
    }

    public int getDefaultRetryAfterMilliSeconds()
    {
        return defaultRetryAfterMilliSeconds;
    }

    public static void setDefaultRetryAfterMilliSeconds(int milliSeconds)
    {
        if (milliSeconds != currentConfig.defaultRetryAfterMilliSeconds)
        {
            update();
            currentConfig.defaultRetryAfterMilliSeconds = milliSeconds;
            DsPings ping = DsPings.getInstance();
            if (ping != null)
            {
                ping.setDefaultRetryAfterMillis(milliSeconds);
            }
        }
    }

    /**
     * @return if true, the proxy will operate in stateful mode
     *         be default.
     */
    public boolean isStateful()
    {
        return(currentConfig.stateMode == STATEFUL);
    }


    /**
     * @return if true, the proxy will operate in stateless mode
     */
    public boolean isStateless()
    {
        return(currentConfig.stateMode == STATELESS);
    }


    /**
     * @return if true, the proxy will operate in FailoverStateful mode
     */
    public boolean isFailoverStateful()
    {
        return(currentConfig.stateMode == FAILOVER_STATEFUL);
    }

    /**
     * @return state-mode in int value
     */

    public byte getStateMode()
    {
        return stateMode;
    }

    /*
     * Returns true if we are recursing on 3xx responses
     * @return True if we are recursing on 3xx response, false otherwise
     */
    public boolean isRecursing()
    {
        return isRecursing;
    }

    /**
     * Returns the current search mechanism being used.
     *
     * @return The current search mechanism being used;
     *         DsProxyController.SEARCH_HIGHEST, DsProxyController.SEARCH_PARALLEL or
     *         DsProxyController.SEARCH_SEQUENTIAL.
     */
    public byte getSearchType()
    {
        return searchType;
    }

    public synchronized static void setRequestTimeout(int newTimeout)
    {

        if (newTimeout != currentConfig.requestTimeout)
        {
            update();
            currentConfig.requestTimeout = newTimeout;
        }
    }

    /**
     * Returns the address of the destination IP address
     *
     * @return the address to proxy to, null if the default logic
     *         is to be used . Application sets the proxyToAddress value
     *         at run time using ProxyParams object.
     *         For use with ProxyParams interfaces
     */

    public DsByteString getProxyToAddress()
    {
        return null;
    }

    /**
     * Returns the port number of  destination port
     *
     * @return the port to proxy to, default port if set to -1
     *         For use with ProxyParams interfaces
     */
    public int getProxyToPort()
    {
        return -1;
    }

    /**
     * @return the timeout value in milliseconds for outgoing requests.
     *         -1 means default timeout
     *         This allows to set timeout values that are _lower_
     *         than SIP defaults. Values higher than SIP deafults will have
     *         no effect.
     */
    public synchronized long getRequestTimeout()
    {
        return requestTimeout;
    }


    /**
     * sets the time out value for a sequential search
     *
     * @param time in seconds
     */
    public synchronized static void setSequentialSearchTimeout(int time)
    {
        update();
        currentConfig.sequentialSearchTimeout = time;
    }

    /**
     * returns the time out value for a sequential search
     */
    public synchronized int getSequentialSearchTimeout()
    {
        return sequentialSearchTimeout;
    }

    /**
     * If the current instance was obtained by anybody,
     * this method will create a new instance
     */
    private static void update()
    {
        if (!updated)
        {
            currentConfig = (DsControllerConfig) currentConfig.clone();
            updated = true;
        }
    }

    /**
     * creates a clone of this object
     */
    public Object clone()
    {
        DsControllerConfig clone;

        try
        {
            clone = (DsControllerConfig) super.clone();
        }
        catch (CloneNotSupportedException e)
        {

            // should never happen.
            if (Log.on && Log.isEnabled(Level.WARN))
            {
                Log.warn("couldn't clone controllerConfig ", e);
            }
            return null;
        }

        clone.listenIf = (HashMap) listenIf.clone();
        clone.tlsPeerList = (Hashtable) tlsPeerList.clone();
        //clone.viaListenIf = (HashMap) viaListenIf.clone();
        clone.configuredInternalViaListenHash = (HashMap) configuredInternalViaListenHash.clone();
        clone.configuredExternalViaListenHash = (HashMap) configuredExternalViaListenHash.clone();

        //todo recordrouteif can be rmeoved if not used
        clone.recordRoutesIf = (HashMap) recordRoutesIf.clone();
        clone.recordRoutesMap = (HashMap) recordRoutesMap.clone();

        return clone;
    }

    /**
     * Add a trusted peer
     *
     * @param name trusted peer name
     */
    public synchronized static boolean addTlsPeer(int index, String name)
    {

        if (!currentConfig.tlsPeerList.containsKey(name))
        {
            update();
            currentConfig.tlsPeerList.put(new Integer(index), name);
            return true;
        }

        return false;
    }

    /**
     * Removes a truster peer based on the index of the peer.
     *
     * @param index The index in the truster peer table of the peer to be removed.
     * @return True if the peer could be removed from the table, false if the
     *         peer could not be removed from the table.
     */
    public synchronized static boolean removeTlsPeer(int index)
    {
        return currentConfig.tlsPeerList.remove(new Integer(index)) != null;
    }

    /* Remove a trusted peer
     * @param String name trusted peer name
     */
    public synchronized static boolean removeTlsPeer(String name)
    {
        Collection tlsPeerCollection = currentConfig.tlsPeerList.values();
        return tlsPeerCollection.remove(name);
    }

    /* Remove a trusted peer
     * @param String name trusted peer name
     */
    public synchronized static boolean ClearTlsPeers()
    {
        if (currentConfig.tlsPeerList.size() > 0)
        {
            update();
            currentConfig.tlsPeerList.clear();
            return true;
        }
        return false;
    }

    /**
     * Check if the peer is trusted
     *
     * @param name trusted peer name
     */
    public boolean isPeerTrusted(String name)
    {
        if (tlsPeerList.size() != 0)
            return tlsPeerList.containsValue(name);

        return true;
    }

    /**
     * Get the list of trusted peer
     */
    public Enumeration getTlsPeers()
    {
        return tlsPeerList.elements();
    }

    /**
     * Get the size of trusted peer list
     */
    public int getTlsListSize()
    {
        return tlsPeerList.size();
    }

    public synchronized static void setTlsFileName(String name)
    {
        update();
        currentConfig.tlsFileName = name;
    }

    public synchronized String getTlsFileName()
    {
        return tlsFileName;
    }

    public synchronized int getMaxRequestTimeout()
    {
        return m_iMaxRequestTimeout;
    }

    public static synchronized void setMaxRequestTimeout(int timeoutInSeconds)
    {
        if (currentConfig.m_iMaxRequestTimeout != timeoutInSeconds)
        {
            update();
            currentConfig.m_iMaxRequestTimeout = timeoutInSeconds;
        }
    }

    /**
     * unset the edge proxy mode.
     */

    public static synchronized void unsetEdgeProxyMode()
    {
        if (currentConfig.m_edgeProxyMode)
        {
            update();
            currentConfig.m_edgeProxyMode = false;
        }
    }

    /**
     *
     */
    public static synchronized void setEdgeProxyMode()
    {
        if (!currentConfig.isEdgeProxyMode())
        {
            update();
            currentConfig.m_edgeProxyMode = true;
        }
    }

    /**
     * Method declaration
     *
     * @return
     * @see
     */
    public boolean isEdgeProxyMode()
    {
        return m_edgeProxyMode;
    }


    /**
     * Method declaration
     *
     * @return true is compact header form mode is turned on, false otherwise
     * @see
     */
    public boolean isCompactHeaderForm()
    {
        return m_compactHeaderForm;
    }

    /**
     * set the compact header form setting
     */
    public static synchronized void setCompactHeaderForm()
    {
        if (!currentConfig.isCompactHeaderForm())
        {
            update();
            currentConfig.m_compactHeaderForm = true;
            DsSipHeader.setCompact(true);
        }
    }


    /**
     * unset the compact header form setting.
     */

    public static synchronized void unsetCompactHeaderForm()
    {
        if (currentConfig.isCompactHeaderForm())
        {
            update();
            currentConfig.m_compactHeaderForm = false;
            DsSipHeader.setCompact(false);
        }
    }

    public synchronized boolean isPrivacyEnabled()
    {
        return m_privacyEnabled;
    }

    public static synchronized void setPrivacyEnabled(boolean flag)
    {
        if (currentConfig.m_privacyEnabled != flag)
        {
            update();
            currentConfig.m_privacyEnabled = flag;
        }
    }

    /**
     * Sets the external network type to one of those defined in "network" command
     *
     * @see
     */
/*
  public static synchronized void setExternalNetwork(int network) {
    if (Log.on && Log.isDebugEnabled())
      Log.debug("setExternalNetwork called with network=" + network);
    if (currentConfig.getExternalNetworkType() != network) {
      update();
      currentConfig.externalNetworkType = network;
      if (!DsSipProxyManager.getInstance().setCurrentExternalNetwork(network))
        throw new RuntimeException("Unable to change external network settings! Unknown network type: " +
                                   network);
    }
  }
*/

    /**
     * returns the type of external network configured with "network" CLI command
     */
/*
  public int getExternalNetworkType() {
    return externalNetworkType;
  }
*/

    /**
     * Method declaration
     *
     * @return true is modify contact headers form mode is turned on,
     *         false otherwise
     * @see
     */
    public boolean isModifyContactHeaders()
    {
        return m_modifyContactHeaders;
    }

    /**
     * set the compact header form setting
     */
    public static synchronized void setModifyContactHeaders()
    {
        if (!currentConfig.isModifyContactHeaders())
        {
            update();
            currentConfig.m_modifyContactHeaders = true;
        }
    }


    /**
     * unset the compact header form setting.
     */

    public static synchronized void unsetModifyContactHeaders()
    {
        if (currentConfig.isModifyContactHeaders())
        {
            update();
            currentConfig.m_modifyContactHeaders = false;
        }
    }

/*
    public boolean chkInternalListenInterface()
    {
// TODO remove this method
        Iterator listenEntries = currentConfig.listenIf.values().iterator();

        while (listenEntries.hasNext())
        {
            ListenIf li = (ListenIf) listenEntries.next();

            if (li.getDirection().equals(LISTEN_INTERNAL))
            {
                return true;
            }
        }
        return false;
    }


    public boolean chkExternalListenInterface()
    {
//TODO remove this method
        Iterator listenEntries = currentConfig.listenIf.values().iterator();

        while (listenEntries.hasNext())
        {
            ListenIf li = (ListenIf) listenEntries.next();

            if (li.getDirection().equals(LISTEN_EXTERNAL))
            {
                return true;
            }
        }

        return true;
    }
*/

    public static synchronized void setNatMedia(int state) throws Exception
    {

        if (currentConfig.getNatMediaType() != state)
        {
            update();
            currentConfig.m_NatMedia = (byte) state;
            //setRecordRoute(true);
            //setStateful(true);
        }
        if (!currentConfig.isFirewallInited())
        {

            DsFirewallInitializer.init();
            setFirewallInited();

        }
    }


    public void cancelTimer()
    {
        update();
        currentConfig.timer.cancel();
        if (Log.on && Log.isDebugEnabled())
        {
            Log.debug("canceling callLegToNextHopMap cleanup ");

        }
    }

    public void scheduleTimer()
    {
        update();
        currentConfig.timer = new Timer();
        currentConfig.timer.scheduleAtFixedRate(new DsFailOverStatefulTT(),
                INTERVAL, INTERVAL);

        if (Log.on && Log.isDebugEnabled())
        {
            Log.debug("scheduling callLegToNextHopMap cleanup "
                    + "with an interval " + INTERVAL);
        }
    }


    public static int getTimerInterval()
    {
        return INTERVAL;
    }


    public synchronized byte getOnNextHopFailure()
    {
        return onNextHopFailure;
    }

    public synchronized static void setOnNextHopFailure(byte value)
    {
        update();
        currentConfig.onNextHopFailure = value;
    }

    public static int getOnNextHopFailureAsInt(String value)
    {
        int i = -1;
        Integer integer = (Integer) onNextHopFailureMap.get(value);
        if (integer != null) i = integer.intValue();
        return i;
    }

    public static String getOnNextHopFailureAsString(int i)
    {
        String value = null;
        for (Iterator iter = onNextHopFailureMap.keySet().iterator(); iter.hasNext();)
        {
            String key = (String) iter.next();
            Integer val = (Integer) onNextHopFailureMap.get(key);
            if (val.intValue() == i)
            {
                value = key;
                break;
            }
        }

        return value;
    }


    public static synchronized void unsetFirewallMode()
    {
        if (currentConfig.isFirewallMode())
        {
            update();

            currentConfig.m_firewallMode = false;
        }
    }

    /**
     * Method declaration
     *
     * @see
     */
    public static synchronized void setFirewallMode()
    {
        if (!currentConfig.isFirewallMode())
        {
            update();

            Log.debug("In setFirewallMode, Mode is now true");
            currentConfig.m_firewallMode = true;

            //   setRecordRoute(true);
            //   setStateful(true);      // RS+ check the method name

            if (!currentConfig.isFirewallInited())
            {
                try
                {
                    DsFirewallInitializer.init();
                    setFirewallInited();
                }
                catch (Exception e)
                {
                    //Log.dserror( "Error in setNatMedia", e);
                }
            }
        }
    }

    /*   public static void setProvisionalTimerValue(int timerValue)
       {
           update();
           currentConfig.provisionalTimerValue = timerValue;
           if (currentConfig.getSend100())
           {
               //TODO hack so we can reuse the setSend100 method
               setSend100(true);
               if (Log.on && Log.isDebugEnabled())
                   Log.debug("sending 100 provisional is turned on for non invites with a TU3 timer value of : " + timerValue);
           }
       }

       public int getProvisionalTimerValue()
       {
           return provisionalTimerValue;
       }*/
/*
  public static void setInternalProvisionalTimerValue(int timer) {
    if (Log.on && Log.isDebugEnabled())
      Log.debug( "entering setInternalProvisionalTimerValue");

    update();
    currentConfig.internalProvisionalResponseTimer = timer;
    if (currentConfig.getSend100()) {
      if (Log.on && Log.isDebugEnabled())
        Log.debug( "sending 100 provisional is turned on for non invites with a TU3 timer value of : " + currentConfig.externalProvisionalResponseTimer);
      DsConfigManager.setTimerValue(null,
                                    new Byte(DsSipConstants.TU3).byteValue(),
                                    currentConfig.internalProvisionalResponseTimer);
    }
  }

  public static void setExternalProvisionalTimerValue(int timer) {
    if (Log.on && Log.isDebugEnabled())
      Log.debug( "entering setExternalProvisionalTimerValue");
    update();
    currentConfig.externalProvisionalResponseTimer = timer;
    if (currentConfig.getSend100())
      DsConfigManager.setTimerValue(DsSipProxyManager.getCurrentExternalNetwork(),
                                    new Byte(DsSipConstants.TU3).byteValue(),
                                    currentConfig.externalProvisionalResponseTimer);
  }

  public int getInternalProvisonalTimerValue() {
    return internalProvisionalResponseTimer;
  }

  public int getExternalProvisonalTimerValue() {
    return externalProvisionalResponseTimer;
  }
*/

    public static boolean isMyRoute(DsByteString routeHost, int routePort, int routeTransport, ListenIf myIF)
    {
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering isMyRoute(" + routeHost + ", " + routePort + ", " + routeTransport + ", " + myIF + ')');
        boolean match = false;
        if (myIF != null)
        {
            if (routeHost.equals(myIF.getAddress()))
            {
                if (routePort == myIF.getPort())
                {
                    if (routeTransport == myIF.getProtocol())
                    {
                        match = true;
                    }
                }
            }
        }
        if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving isMyRoute(), returning " + match);
        return match;
    }

    /*
     * Checks if the passed host,transport,port,user info is recognized.
     * If host is Domain name , this methods first resolves the domain to IP and then
     * checks if its recognized.This method only works for SRV
     *
     */

    public  boolean recognize(DsByteString user,DsByteString host, int port, int transport, DsNetwork network, boolean fallBackToAQuery) throws UnknownHostException, DsSipHostNotValidException {

        //check for IP and cases where host matches aliases.
        if(recognize(user, host, port, transport))
            return true;

        if (!IPValidator.hostIsIPAddr(host.toString())) {
            DsSipServerLocator dnsResolver = dsSIPServerLocatorFactory.createNewSIPServerLocator();
            dnsResolver.setAQuery(fallBackToAQuery);
            try {
                dnsResolver.initialize(network, null, DsSipResolverUtils.LPU, host.toString(), port,
                        transport, false);
                boolean isDNSResolverEmpty = true;
                while (dnsResolver.hasNextBindingInfo()) {
                    isDNSResolverEmpty = false;
                    DsBindingInfo bindingInfo = dnsResolver.getNextBindingInfo();
                    if (recognize(user, new DsByteString(bindingInfo.getRemoteAddressStr()),
                            bindingInfo.getRemotePort(), bindingInfo.getTransport())) {
                        return true;
                    }
                }
                if (isDNSResolverEmpty) {
                    throw new DsSipHostNotValidException(dnsResolver.getFailureException());
                }
            } catch (UnknownHostException | DsSipHostNotValidException e ) {
                Log.error("Exception in resolving " + host, e);
                throw e;
            }
        }
        return false;
    }

    public synchronized static void addNetwork(String direction, String type, boolean connectionsAllowed, String UDPDatagramSize, int maxBuffer, int threshold) throws DsException
    {
        NetworkObj network = (NetworkObj) currentConfig.NetworkIfMap.get(direction);
        update();
        if (network == null)
        {
            network = new NetworkObj(direction, type, connectionsAllowed, Integer.parseInt(UDPDatagramSize), maxBuffer, threshold);
            currentConfig.NetworkIfMap.put(direction, network);
        }
    }

    public synchronized static void setNetwork(String direction, String type, Boolean connectionsAllowed, String UDPDatagramSize, int maxBuffer, int threshold) throws DsException
    {
        NetworkObj network = (NetworkObj) currentConfig.NetworkIfMap.get(direction);

        if (network != null)
        {
            if (type != null)
                network.setType(type);

            if (connectionsAllowed != null)
                network.setConnectionsAllowed(connectionsAllowed.booleanValue());

            if (UDPDatagramSize != null)
                network.setUDPDatagramSize(Integer.parseInt(UDPDatagramSize));

            if (maxBuffer != 0)
                network.setMaxBuffer(maxBuffer);

            if (threshold != 0)
                network.setThreshold(threshold);

        }


    }

    public synchronized static void removeNetwork(String direction)
    {
        NetworkObj network = (NetworkObj) currentConfig.NetworkIfMap.get(direction);
        if (network != null)
        {
            //todo if the network can be removed, no dependencies with other configurations
            currentConfig.NetworkIfMap.remove(network);
        }
    }

    public DsNetwork getNetwork(String direction)
    {
        NetworkObj network = (NetworkObj) currentConfig.NetworkIfMap.get(direction);
        if (network != null)
        {
            return network.getNetwork();
        }
        return null;
    }

    public DsNetwork getDefaultNetwork()
    {
        return null;
    }

    public synchronized static void addPath(int index, String direction, String PathAddress, int PathPort, int PathTransport) throws DsException
    {
        DsNetwork network = currentConfig.getNetwork(direction);
        if (network == null)
        {
            throw new DsException("Create a Network for this direction before configuring path");
        }
        update();
        HashMap pathDirMap = (HashMap) currentConfig.PathIfMap.get(direction);
        if (pathDirMap == null)
        {
            pathDirMap = new HashMap();
            currentConfig.PathIfMap.put(direction, pathDirMap);
        }
        PathObj path = new PathObj(direction, PathAddress, PathPort, PathTransport);
        pathDirMap.put(new Integer(PathTransport), path);
        currentConfig.PathIf.put(new Integer(index), path);
    }

    public synchronized static void removePath(int index)
    {
        Integer i = new Integer(index);
        PathObj path = (PathObj) currentConfig.PathIf.get(i);
        if (path != null)
        {
            HashMap pathDirMap = (HashMap) currentConfig.PathIfMap.get(path.getDirection());
            if (pathDirMap != null)
            {
                pathDirMap.remove(new Integer(path.getPathTransport()));
                if (pathDirMap.isEmpty())
                {
                    currentConfig.PathIfMap.remove(path.getDirection());
                }

            }
            currentConfig.PathIf.remove(i);
        }
    }

    public PathObj getPath(int index)
    {
        return(PathObj) PathIf.get(new Integer(index));
    }

    public PathObj getPath(int transport, String direction)
    {
        HashMap pathDirMap = (HashMap) PathIfMap.get(direction);
        if (pathDirMap != null)
        {
            return(PathObj) pathDirMap.get(new Integer(transport));
        }
        return null;
    }

    public synchronized static void addVia(int index, String direction,
                                           String ViaAddress, int ViaPort, int ViaTransport,
                                           String ViaSrcAddress, int ViaSrcPort) throws DsException
    {
        InetAddress ViaSrcInetAddress = null;
        DsNetwork network = currentConfig.getNetwork(direction);
        if (network == null)
        {
            throw new DsException("Create a Network for this direction before configuring via");
        }
        //MIGRATION
        //replaced with null check
        //if (!ViaSrcAddress.equalsIgnoreCase(DSRE_MIB.DEFAULTVALUE))
        if (ViaSrcAddress != null && ViaSrcAddress.length() > 0)
        {
            try
            {
                // Check to make sure we are not listening on the src address and port
                // that we want to insert in outgoing packets, otherwise we will get
                // a bind exception from the low-level when we go to send
                ViaSrcInetAddress = InetAddress.getByName(ViaSrcAddress);
                if (currentConfig.isListeningOn(ViaSrcInetAddress, ViaSrcPort, ViaTransport))
                {
                    throw new DsException("Via header fields CANNOT use the same source-ip, source-port, " +
                            "and transport combination used by listeners (configured " +
                            "via the listen command).");
                }
            }
            catch (UnknownHostException e)
            {
                throw new DsException(ViaSrcAddress + " is not a valid IP address.");
            }
        }
        update();
        HashMap viaDirMap = (HashMap) currentConfig.ViaIfMap.get(direction);
        if (viaDirMap == null)
        {
            viaDirMap = new HashMap();
            currentConfig.ViaIfMap.put(direction, viaDirMap);
        }
        ViaObj via = new ViaObj(direction, ViaAddress, ViaPort, ViaTransport, ViaSrcAddress, ViaSrcInetAddress, ViaSrcPort);
        viaDirMap.put(new Integer(ViaTransport), via);
        currentConfig.ViaIf.put(new Integer(index), via);
    }

    public synchronized static void removeVia(int index)
    {
        Integer i = new Integer(index);
        ViaObj via = (ViaObj) currentConfig.ViaIf.get(i);
        if (via != null)
        {
            HashMap viaDirMap = (HashMap) currentConfig.ViaIfMap.get(via.getDirection());
            if (viaDirMap != null)
            {
                viaDirMap.remove(new Integer(via.getViaTransport()));
            }
            currentConfig.ViaIf.remove(i);
        }
    }

    public ViaObj getVia(int index)
    {
        return(ViaObj) ViaIf.get(new Integer(index));
    }

    public ViaObj getVia(int transport, String direction)
    {
        HashMap viaDirMap = (HashMap) ViaIfMap.get(direction);
        if (viaDirMap != null)
        {
            return(ViaObj) viaDirMap.get(new Integer(transport));
        }
        return null;
    }

    public synchronized static void addRecordRoute(int index, String direction,
                                                   String RecordRouteAddress, int RecordRoutePort,
                                                   int RecordRouteTransport) throws DsException
    {
        DsNetwork network = currentConfig.getNetwork(direction);
        if (network == null)
        {
            throw new DsException("Create a Network for this direction before configuring record route");
        }
        update();
        HashMap rrDirMap = (HashMap) currentConfig.RecordRouteIfMap.get(direction);
        if (rrDirMap == null)
        {
            rrDirMap = new HashMap();
            currentConfig.RecordRouteIfMap.put(direction, rrDirMap);
        }
        RecordRouteObj rr = new RecordRouteObj(direction, RecordRouteAddress, RecordRoutePort, RecordRouteTransport);
        rrDirMap.put(new Integer(RecordRouteTransport), rr);
        currentConfig.RecordRouteIf.put(new Integer(index), rr);
    }

    public synchronized static void removeRecordRoute(int index)
    {
        Integer i = new Integer(index);
        RecordRouteObj rr = (RecordRouteObj) currentConfig.RecordRouteIf.get(i);
        if (rr != null)
        {
            HashMap rrDirMap = (HashMap) currentConfig.RecordRouteIfMap.get(rr.getDirection());
            if (rrDirMap != null)
            {
                rrDirMap.remove(new Integer(rr.getRecordRouteTransport()));
            }
            currentConfig.RecordRouteIf.remove(i);
        }
    }

    public RecordRouteObj getRecordRoute(int index)
    {
        return(RecordRouteObj) RecordRouteIf.get(new Integer(index));
    }

    public RecordRouteObj getRecordRoute(int transport, String direction)
    {
        HashMap rrDirMap = (HashMap) RecordRouteIfMap.get(direction);
        if (rrDirMap != null)
        {
            return(RecordRouteObj) rrDirMap.get(new Integer(transport));
        }
        return null;
    }

    public boolean isPathSet()
    {
        return !PathIfMap.isEmpty();
    }

    /**
     * Determines if masking for the specified header is enabled.
     *
     * @param hdr The header type.  Possible values are {@link #MASK_VIA}.
     * @return <code>true</code> if masking is explicitly enabled
     *         for the specified header type or if masking is set for all headers.
     */
    public boolean isMasked(String direction, int hdr)
    {
        if (Log.on && Log.isDebugEnabled()) Log.debug("Entering isMasked(" + direction + ')');
        boolean val = false;
        MaskObj mask = (MaskObj) MaskIfMap.get(direction);
        if (mask != null)
            val = mask.isHeaderSet(hdr);
        if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving isMasked(), returning " + val);
        return val;
    }

    public boolean isMaskingEnabled(String direction)
    {
        if (Log.on && Log.isDebugEnabled()) Log.debug("Entering isMaskingEnabled()");
        boolean val = false;
        MaskObj mask = (MaskObj) MaskIfMap.get(direction);
        if (mask != null)
            val = mask.isHeaderMaskingEnabled();
        if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving isMaskingEnabled(), returning " + val);
        return val;
    }

    public MaskObj getMaskType(String direction)
    {
        if (Log.on && Log.isDebugEnabled()) Log.debug("Entering getMaskType(" + direction + ')');
        MaskObj mask = (MaskObj) MaskIfMap.get(direction);
        if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving getMaskType(), returning: " + mask);
        return mask;
    }

    public static synchronized void unSetMaskType(String direction, String maskHeader)
    {
        if (Log.on && Log.isDebugEnabled()) Log.debug("Entering unSetMaskType(" + maskHeader + ')');
        short hdr = -1;
        if (maskHeader.equals(RE.reMaskHeader_via))
            hdr = MaskObj.VIA;
        MaskObj mask = (MaskObj) currentConfig.MaskIfMap.get(direction);
        if (mask != null)
        {
            mask.unSetHeaderMask(hdr);

            if (!mask.isHeaderMaskingEnabled())
            {
                currentConfig.MaskIfMap.remove(direction);
            }
        }
        if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving unSetMaskType()");
    }

    public static synchronized void setMaskType(String direction, String maskHeader)
    {
        if (Log.on && Log.isDebugEnabled()) Log.debug("Entering setMaskType(" + maskHeader + ')');

        short hdr = -1;

        if (maskHeader.equals(RE.reMaskHeader_via))
            hdr = MaskObj.VIA;

        MaskObj mask = (MaskObj) currentConfig.MaskIfMap.get(direction);

        if (mask == null)
        {
            currentConfig.update();
            mask = new MaskObj(direction);
            currentConfig.MaskIfMap.put(direction, mask);
        }

        mask.setHeaderMask(hdr);

        if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving setMaskType()");
    }

    public synchronized static void addServiceRouteAdd(int index, String direction,
                                                       String ServiceRouteAddress, int ServiceRoutePort,
                                                       int ServiceRouteTransport, int ServiceRouteSequence,
                                                       String ServiceRouteParams) throws DsException
    {
        DsNetwork network = currentConfig.getNetwork(direction);
        if (network == null)
        {
            throw new DsException("Create a Network for this direction before configuring Service Route");
        }
        update();
        ArrayList svcRouteDirList = (ArrayList) currentConfig.AddServiceRouteIfMap.get(direction);
        if (svcRouteDirList == null)
        {
            svcRouteDirList = new ArrayList();
            currentConfig.AddServiceRouteIfMap.put(direction, svcRouteDirList);
        }
        currentConfig.doAddServiceRoute = true;
        ServiceRouteObj svcRoute = new ServiceRouteObj(direction, ServiceRouteAddress,
                ServiceRoutePort, ServiceRouteTransport,
                ServiceRouteSequence, ServiceRouteParams);
        int seq = svcRoute.getServiceRouteSequence();
        if (seq >= svcRouteDirList.size())
            svcRouteDirList.add(svcRoute);
        else
            svcRouteDirList.add(seq, svcRoute);
        currentConfig.AddServiceRouteIf.put(new Integer(index), svcRoute);
    }

    public synchronized static void removeServiceRouteAdd(int index)
    {
        Integer i = new Integer(index);
        ServiceRouteObj svcRoute = (ServiceRouteObj) currentConfig.AddServiceRouteIf.get(i);
        if (svcRoute != null)
        {
            ArrayList svcRouteDirList = (ArrayList) currentConfig.AddServiceRouteIfMap.get(svcRoute.getDirection());
            if (svcRouteDirList != null)
            {
                svcRouteDirList.remove(svcRoute);
                if (svcRouteDirList.isEmpty())
                {
                    currentConfig.AddServiceRouteIfMap.remove(svcRoute.getDirection());
                }

            }
            currentConfig.AddServiceRouteIf.remove(i);
        }

        if (currentConfig.AddServiceRouteIfMap.size() == 0)
            currentConfig.doAddServiceRoute = false;
    }

    public ServiceRouteObj getServiceRouteAdd(int index)
    {
        return(ServiceRouteObj) AddServiceRouteIf.get(new Integer(index));
    }

    public ArrayList getServiceRouteAdd(String direction)
    {
        return(ArrayList) AddServiceRouteIfMap.get(direction);
    }

    public boolean doAddServiceRoute()
    {
        return doAddServiceRoute;
    }

    public synchronized static void addServiceRouteModify(int index, String direction,
                                                          String ServiceRouteAddress, int ServiceRoutePort,
                                                          int ServiceRouteTransport) throws DsException
    {
        DsNetwork network = currentConfig.getNetwork(direction);
        if (network == null)
        {
            throw new DsException("Create a Network for this direction before configuring Service Route");
        }
        update();
        currentConfig.doModifyServiceRoute = true;
        ServiceRouteObj svcRoute = new ServiceRouteObj(direction, ServiceRouteAddress, ServiceRoutePort, ServiceRouteTransport);
        currentConfig.ModifyServiceRouteIfMap.put(direction, svcRoute);
        currentConfig.ModifyServiceRouteIf.put(new Integer(index), svcRoute);
    }

    public synchronized static void removeServiceRouteModify(int index)
    {
        Integer i = new Integer(index);
        ServiceRouteObj svcRoute = (ServiceRouteObj) currentConfig.ModifyServiceRouteIf.get(i);
        if (svcRoute != null)
        {
            currentConfig.ModifyServiceRouteIfMap.remove(svcRoute.getDirection());
            currentConfig.ModifyServiceRouteIf.remove(i);
        }

        if (currentConfig.ModifyServiceRouteIfMap.size() == 0)
            currentConfig.doModifyServiceRoute = false;
    }

    public ServiceRouteObj getServiceRouteModify(int index)
    {
        return(ServiceRouteObj) ModifyServiceRouteIf.get(new Integer(index));
    }

    public ServiceRouteObj getServiceRouteModify(int transport, String direction)
    {
        return(ServiceRouteObj) ModifyServiceRouteIfMap.get(direction);
    }

    public boolean doModifyServiceRoute()
    {
        return doModifyServiceRoute;
    }

    public DsSipServiceRouteHeader getServiceRouteInterface(String direction)
    {
        ServiceRouteObj svcRoute = (ServiceRouteObj) ModifyServiceRouteIfMap.get(direction);
        if (svcRoute != null)
            return svcRoute.getServiceRouteHeader();
        return null;
    }

}




