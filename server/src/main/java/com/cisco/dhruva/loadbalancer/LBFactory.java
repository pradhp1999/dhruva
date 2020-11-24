/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME: $RCSfile: LBFactory.java,v $
//
// MODULE:  loadbalancer
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
//
// MODIFICATIONS:
//
//
//////////////////////////////////////////////////////////////////////////////
package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.servergroups.SG;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.util.log.Trace;
import java.util.HashMap;

/**
 * This factory creates a load balancer based on settings in the configuration or by loading a
 * specific named class.
 */
public abstract class LBFactory {

  private static Trace Log = Trace.getTrace(LBFactory.class.getName());

  public static final String DEFAULT_TOKEN = SG.sgSgLbType_global;
  public static final String REQUEST_URI_TOKEN = SG.sgSgLbType_request_uri;
  public static final String HIGHESTQ_TOKEN = SG.sgSgLbType_highest_q;
  public static final String CALLID_TOKEN = SG.sgSgLbType_call_id;
  public static final String TO_TOKEN = SG.sgSgLbType_to_uri;
  public static final String WEIGHT_TOKEN = SG.sgSgLbType_weight;

  public static final int GLOBAL = SG.index_sgSgLbType_global;
  public static final int REQUEST_URI = SG.index_sgSgLbType_request_uri;
  public static final int HIGHEST_Q = SG.index_sgSgLbType_highest_q;
  public static final int CALLID = SG.index_sgSgLbType_call_id;
  public static final int TO = SG.index_sgSgLbType_to_uri;
  public static final int WEIGHT = SG.index_sgSgLbType_weight;
  public static final int MS_ID = SG.index_sgSgLbType_ms_id;
  public static final int VARKEY = 999; /* internal lb type */

  public static final int CUSTOM = -1;
  public static int DEFAULT_TRIES = SG.sgSgElementRetriesDefault;
  public static int SGE_UDP_TRIES = DEFAULT_TRIES;
  public static int SGE_TCP_TRIES = 1;
  public static int SGE_TLS_TRIES = 1;
  private static int DEFAULT_LB_TYPE =
      SG.getValidValueAsInt(SG.sgSgLbType, SG.dsSgGlobalSelectionTypeDefault);
  private static String DEFAULT_LB_STR_TYPE = null;
  private static HashMap customClasses = null;

  /**
   * <p>Creates a <code>LBInterface</code> based on settings in the
   * <code>LoadBalancerConfigInterface</code>. Applications should maintain a
   * reference to the <code>LBInterface</code> for the duration
   * of the transaction. If the application needs to choose another next hop
   * destination from the server group due to a timeout or failure, successive
   * calls to {@link LBInterface#getServer() should be made.
   * @param serverGroupName the server group to load balance over.
   * @param server groups the entire server group repository
   * @param key the key used for hashing
   * @return a load balancer.
   * @throws LBException
   * @throws NonExistantServerGroupException
   */
  public static LBInterface createLoadBalancer(
      DsByteString serverGroupName, ServerGroupInterface serverGroup, DsSipRequest request)
      throws LBException {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering createLoadBalancer()");

    if (serverGroup == null)
      throw new LBException(
          "Cannot create load balancer.  Server group " + serverGroupName + " not found.");
    RepositoryReceiverInterface lb = null;
    boolean useDefaultCustom = false;
    int lbtype = serverGroup.getLBType();
    if (Log.on && Log.isInfoEnabled())
      Log.info("lbtype is " + lbtype + "(" + getLBTypeAsString(lbtype) + ")");
    if (lbtype == GLOBAL) {
      lbtype = getDefaultLBType();
      if (Log.on && Log.isInfoEnabled())
        Log.info("Default lbtype is " + lbtype + "(" + getLBTypeAsString(lbtype) + ")");
      useDefaultCustom = (lbtype == CUSTOM);
    }
    switch (lbtype) {
      case REQUEST_URI:
        lb = new LBHashBased();
        break;
      case HIGHEST_Q:
        lb = new LBHighestQ();
        break;
      case CALLID:
        lb = new LBCallID();
        break;
      case TO:
        lb = new LBTo();
        break;
      case WEIGHT:
        lb = new LBWeight();
        break;
      case MS_ID:
        lb = new LBMsid();
        break;
      case VARKEY:
        lb = new LBHashBasedVariableKey();
        break;
      case CUSTOM:
        String lbStrType = null;
        if (useDefaultCustom) lbStrType = getDefaultLBStrType();
        else lbStrType = serverGroup.getStrLBType();
        if (lbStrType == null)
          throw new LBException("Cannot create custom load balancer.  Class name is null.");
        if (customClasses == null) customClasses = new HashMap();
        Class c = (Class) customClasses.get(lbStrType);
        if (c == null) {
          try {
            // cache class so you don't have do to forName() every time
            c = Class.forName(lbStrType);
            customClasses.put(lbStrType, c);
          } catch (Throwable e) {
            throw new LBException(e.getMessage(), e);
          }
        }
        try {
          lb = (RepositoryReceiverInterface) c.newInstance();
        } catch (Throwable e) {
          throw new LBException(
              "Unable to create load balancer from class '" + lbStrType + "': " + e.getMessage(),
              e);
        }
        break;
      default:
        throw new LBException("Unknown lbtype: " + lbtype);
    }
    lb.setServerInfo(serverGroupName, serverGroup, request);
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving createLoadBalancer()");
    return lb;
  }

  /**
   * Sets the global load balancing type.
   *
   * @param lbtype the load balancing type.
   */
  public static synchronized void setDefaultLBType(int lbtype) {
    DEFAULT_LB_TYPE = lbtype;
  }

  /**
   * Sets the global load balancing type.
   *
   * @param lbtype fully qualified class name of the load balancing type.
   */
  public static synchronized void setDefaultLBType(String lbtype) {
    DEFAULT_LB_STR_TYPE = lbtype;
    DEFAULT_LB_TYPE = CUSTOM;
  }

  /**
   * Gets the global load balancing type.
   *
   * @return the global load balancing type.
   */
  public static synchronized int getDefaultLBType() {
    return DEFAULT_LB_TYPE;
  }

  /**
   * Gets the global load balancing type.
   *
   * @return the fully qualified class name of the load balancing type.
   */
  public static String getDefaultLBStrType() {
    return DEFAULT_LB_STR_TYPE;
  }

  public static int getLBTypeAsInt(String lbtype) {
    int i = SG.getValidValueAsInt(SG.sgSgLbType, lbtype);
    if (i == -1) i = CUSTOM;
    return i;
  }

  public static String getLBTypeAsString(int index) {
    return SG.getValidValueAsString(SG.sgSgLbType, index);
  }

  public static void setUDPTries(int tries) {
    SGE_UDP_TRIES = tries;
  }

  public static void setTCPTries(int tries) {
    SGE_TCP_TRIES = tries;
  }

  public static void setTLSTries(int tries) {
    SGE_TLS_TRIES = tries;
  }

  public static int getUDPTries() {
    return SGE_UDP_TRIES;
  }

  public static int getTCPTries() {
    return SGE_TCP_TRIES;
  }

  public static int getTLSTries() {
    return SGE_TLS_TRIES;
  }
}
