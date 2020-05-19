/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
/**
 * Created by IntelliJ IDEA.
 *
 * @author : Yoga Ramalingam (yramalin@cisco.com) Created : Jun 22, 2005 2:18:19 PM
 */
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.sip.DsPings.DsErrorResponseCodeSet;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class FailoverResponseCode {
  private static FailoverResponseCode ourInstance;

  /** Response code-set considered not a success response FailoverCodes are per server group */
  protected HashMap failoverCodesMap = new HashMap();

  protected DsErrorResponseCodeSet globalDnsServerFailoverCodeSet;

  public DsErrorResponseCodeSet getGlobalDnsServerFailoverCodes() {
    return globalDnsServerFailoverCodeSet;
  }

  public String setGlobalDnsServerFailoverCodes(String dnsServerFailoverCodes) throws Exception {
    if (dnsServerFailoverCodes == null) {
      throw new Exception("Dns Failover Codes cannot be null");
    }
    globalDnsServerFailoverCodeSet = new DsErrorResponseCodeSet();
    dnsServerFailoverCodes = dnsServerFailoverCodes.trim();
    dnsServerFailoverCodes = dnsServerFailoverCodes.replaceAll("\\s+", "");
    String[] codes = dnsServerFailoverCodes.split(",");
    for (int i = 0; i < codes.length; i++) {
      globalDnsServerFailoverCodeSet.addErrorCode(Integer.parseInt(codes[i]));
    }
    return dnsServerFailoverCodes;
  }

  public static synchronized FailoverResponseCode getInstance() {
    if (ourInstance == null) {
      ourInstance = new FailoverResponseCode();
    }
    return ourInstance;
  }

  private FailoverResponseCode() {
    try {
      setGlobalDnsServerFailoverCodes(DsConfigManager.getGlobalDnsServerFailoverCodes());
    } catch (Exception e) {

    }
  }

  public DsErrorResponseCodeSet getFailoverCodes(String serverGroup) {
    return ((DsErrorResponseCodeSet) failoverCodesMap.get(serverGroup));
  }

  public void setFailoverCodes(String serverGroup, DsErrorResponseCodeSet failoverCodes) {
    failoverCodesMap.put(serverGroup, failoverCodes);
  }

  public void removeFailoverCodes(String serverGroup) {
    failoverCodesMap.remove(serverGroup);
  }

  public void addFailoverCode(String serverGroup, Integer failoverCode) {
    DsErrorResponseCodeSet failoverCodeSet = getFailoverCodes(serverGroup);
    if (failoverCodeSet == null) {
      failoverCodeSet = new DsErrorResponseCodeSet();
    }
    failoverCodeSet.addErrorCode(failoverCode);
    setFailoverCodes(serverGroup, failoverCodeSet);
  }

  public void removeFailoverCode(String serverGroup, Integer code) {
    DsErrorResponseCodeSet failoverCodeSet = getFailoverCodes(serverGroup);
    if (failoverCodeSet != null) {
      failoverCodeSet.removeErrorCode(code);
    }
  }

  public boolean isCodeInFailoverCodeSet(String serverGroup, int code) {
    DsErrorResponseCodeSet failoverCodeSet = getFailoverCodes(serverGroup);
    if (failoverCodeSet != null && failoverCodeSet.isValueInResponseCodeSet(code)) {
      return (true);
    }
    return (false);
  }

  public boolean isCodeInFailoverCodeSet(String[] serverGroups, int code) {
    for (int i = 0; i < serverGroups.length; i++) {
      if (isCodeInFailoverCodeSet(serverGroups[i], code)) {
        return (true);
      }
    }
    return (false);
  }

  public boolean isCodeInFailoverCodeSet(List serverGroups, int code) {
    for (Iterator iterator = serverGroups.iterator(); iterator.hasNext(); ) {
      String serverGroup = (String) iterator.next();
      if (isCodeInFailoverCodeSet(serverGroup, code)) {
        return (true);
      }
    }
    return (false);
  }
}
