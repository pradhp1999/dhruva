package com.cisco.dhruva.sip.controller;



/* This is a wrapper class used for storing a DsProxyCookieInterface,
 * proxyparamsInterface and the next hop server to be used in case 
 * of a failoverstateful mode.
 * 
 */

import com.cisco.dhruva.sip.proxy.DsProxyParamsInterface;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;


public class DsFailOverStatefulWrapper
{
	 /** our log object **/

	protected static Logger Log = DhruvaLoggerFactory.getLogger(DsFailOverStatefulWrapper.class);

	private DsProxyCookieThing cookie = null;
	private DsProxyParamsInterface ppInterface = null;
	private long expTime;
	// the default should be false since, the first time
	// the wrapper is used it is never going to be down.
	// it's down only once we turn it true on icmperror  or
	// onproxyfailure.
	private boolean m_markedDown = false;

	public DsFailOverStatefulWrapper(DsProxyCookieThing cookie,
                                   DsProxyParamsInterface proxyParamsInterface)
	{
		this.cookie = cookie;
		ppInterface = proxyParamsInterface;
		setTime();
	}

	public DsProxyCookieThing getCookieThing() 
	{
	  return cookie;
	}

	public DsProxyParamsInterface getPpInterface()
	{
		return ppInterface;
	}
	
	public boolean isMarkedDown()
	{

		Log.debug( " returning marked down set to "
			  + m_markedDown );
		return this.m_markedDown;
	}
	
	public void setMarkedDown(boolean markedDown)
	{
		this.m_markedDown = markedDown;
		Log.debug( "marked down set to " + m_markedDown
			  		  + " for this wrapper ");
	}
	
	public void setTime()
	{
		expTime = System.currentTimeMillis() 
				+ DsControllerConfig.getTimerInterval();
	}
	
	public long getExpTime()
	{
		return this.expTime;
	}

}
