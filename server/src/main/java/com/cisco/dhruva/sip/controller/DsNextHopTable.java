package com.cisco.dhruva.sip.controller;


import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;

import java.util.Hashtable;



public class DsNextHopTable
{

    private static DsNextHopTable m_this_Singleton= null;

    private static long INIT_CAP = -1;

    // hashtable for failover stateful mapping .
    /* with key as a string callLegKey and value as a
     * DsFailOverStatefulWrapper which basically wraps a lbURI and pp.
     */
    private Hashtable callLegToNextHopTable = null;

    protected static Logger Log = DhruvaLoggerFactory.getLogger(
            DsNextHopTable.class);
    /**
     * Our Constructor
     */
    private DsNextHopTable()
    {
        // initial capacity of the hashtable used for failOverStateful
        // assuming 400 cps and 50% capacity utilisation with
        // the INTERVAL being the cleanup time.
        if ( DsControllerConfig.getTimerInterval() > 2000 )
        {
            INIT_CAP = ((DsControllerConfig.getTimerInterval() / 2000) * 400 );
        }
        // creating hashtable with intial cap only if
        // the intial cap reqd is high. picked 1000 as
        // high cap.
        if ( INIT_CAP > 1000 )
        {
            callLegToNextHopTable = new Hashtable((int)INIT_CAP);
        }
        else
        {
            callLegToNextHopTable = new Hashtable();
        }

    }

    public static DsNextHopTable getInstance()
    {
        if ( m_this_Singleton == null )
        {
            m_this_Singleton = new DsNextHopTable();
        }

        return m_this_Singleton;
    }



    public synchronized void addCallLegNextHop(String callLegKey, DsFailOverStatefulWrapper failOverWrapper)
    {
        this.callLegToNextHopTable.put(callLegKey, failOverWrapper );
        Log.debug( "added callLegKey "
                    + callLegKey + " to the table " );

    }


    public synchronized DsFailOverStatefulWrapper getNextHop(String callLegKey)
    {
        return (DsFailOverStatefulWrapper)this.callLegToNextHopTable.get(callLegKey);
    }


    public Hashtable getCallLegToNextHopTable()
    {
        return this.callLegToNextHopTable;
    }

    public synchronized void removeCallLegNextHop(String callLegKey)
    {
        DsFailOverStatefulWrapper tempObj = (DsFailOverStatefulWrapper)callLegToNextHopTable.remove(callLegKey);
        if ( tempObj == null )
        {
            Log.error( "error occured while removeing the nextHop from callLegToNextHopTable" );
        }
    }

    public synchronized void removeAllCallLegNextHop()
    {
        this.callLegToNextHopTable.clear();
    }


}

