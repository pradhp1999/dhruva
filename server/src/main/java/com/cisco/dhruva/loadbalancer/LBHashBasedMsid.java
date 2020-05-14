package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.servergroups.ServerGroupElement;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.util.log.Trace;
import java.util.ArrayList;
import java.util.TreeSet;
import org.apache.logging.log4j.Level;

public class LBHashBasedMsid extends LBBase {

  public static final String MPARAM_START_STRING = "m=";
  public static final String MPARAM_END_STRING = "a=";
  public static final String MPARAM_DS_STRING = "applicationsharing";
  public static final String MPARAM_AV_STRING = "audio";
  protected boolean isDSCall = false;
  protected ArrayList lbList = new ArrayList();
  protected static Trace Log = Trace.getTrace(LBHashBasedMsid.class.getName());

  @Override
  public final ServerInterface getServer() {
    if (Trace.on && Log.isDebugEnabled()) Log.debug("Entering getServer()");

    lastTried = pickServer(null);
    if (Trace.on && Log.isTraceEnabled()) Log.trace("Leaving getServer()");
    return lastTried;
  }

  /**
   * This method performs the appropriate load balancing algorithm to determine the next hop, using
   * the passed in key (varKey), to perform the hashing.
   */
  @Override
  public final ServerInterface getServer(DsByteString varKey) {
    if (Trace.on && Log.isDebugEnabled()) Log.debug("Entering getServer(varKey)");

    lastTried = pickServer(varKey);
    if (Trace.on && Log.isTraceEnabled()) Log.trace("Leaving getServer(varKey)");
    return lastTried;
  }

  @Override
  public final ServerInterface pickServer(DsByteString varKey) {

    lastTried = null;

    if (domainsToTry == null) initializeDomains();
    if (domainsToTry.isEmpty()) {
      if (Trace.on && Log.isEnabled(Level.WARN)) Log.warn("No more routes remain");
      return null;
    }

    ServerGroupElementInterface selectedElement = selectElement(varKey);
    boolean isMyNextHop = true;
    if (selectedElement == null) {
      domainsToTry.clear();
    } else if (isMyNextHop) {
      domainsToTry.remove(selectedElement);

      lastTried = (ServerInterface) selectedElement;
      if (Trace.on && Log.isDebugEnabled())
        Log.debug("Server group " + serverGroupName + " selected " + selectedElement.toString());
    }

    return lastTried;
  }

  @Override
  protected final ServerGroupElementInterface selectElement(DsByteString varKey) {
    ServerGroupElementInterface selectedElement = null;

    if (domainsToTry.isEmpty()) {
      Log.info("domainsToTry is empty hence returning null...");
      return null;
    }
    if (lbList != null && !lbList.isEmpty()) {
      selectedElement = (ServerGroupElementInterface) lbList.get(0);
      Log.info("list is not empty hence selected element is " + selectedElement);
      lbList.remove(0);
      domainsToTry.remove(selectedElement);
    } else {
      selectedElement = getElementFromLB(varKey);
    }
    ServerInterface nextHop = (ServerInterface) selectedElement;
    if (isDsCall()) {
      Log.info("its a DS Call, hence returning element is " + selectedElement);
      return selectedElement;
    } else if (nextHop != null && nextHop.isAvailable()) {
      Log.info("its a AV Call and available hence returning element is " + selectedElement);
      return selectedElement;
    } else {
      while (nextHop != null && !nextHop.isAvailable()) {
        Log.info("its a AV Call but not available hence checking next available element");
        if (lbList.isEmpty()) {
          Log.info(
              "list is empty, hence creating a one more list with next heigher q-value elements for Load Balance");
          selectedElement = selectElement(null);
          return selectedElement;
        }
        selectedElement = (ServerGroupElementInterface) lbList.get(0);
        lbList.remove(0);
        domainsToTry.remove(selectedElement);
        nextHop = (ServerInterface) selectedElement;
      }
    }
    return selectedElement;
  }

  private ServerGroupElementInterface getElementFromLB(DsByteString varKey) {
    ServerGroupElementInterface selectedElement = null;
    float highestQ = -1;
    for (Object o : domainsToTry) {
      ServerGroupElementInterface sge = (ServerGroupElementInterface) o;
      if (Float.compare(highestQ, -1) == 0) {
        highestQ = sge.getQValue();
        lbList.add(sge);
      } else if (Float.compare(sge.getQValue(), highestQ) == 0) {
        lbList.add(sge);
      } else break;
    }

    Log.info("list of elements in order on which load balancing is done : " + lbList.toString());

    if (lbList.size() == 1) {
      selectedElement = (ServerGroupElementInterface) lbList.get(0);
      Log.info("list has only 1 element, hence selected element is " + selectedElement);
      lbList.remove(0);
      domainsToTry.remove(selectedElement);
    } else {
      selectedElement = getElementByHashing(varKey);
    }
    return selectedElement;
  }

  private ServerGroupElementInterface getElementByHashing(DsByteString varKey) {
    ServerGroupElementInterface selectedElement = null;
    DsByteString hashKey = (varKey != null) ? varKey : key;

    if (Trace.on && Log.isInfoEnabled()) Log.info("Hashing on " + hashKey);
    int index = DsHashAlgorithm.selectIndex(hashKey, lbList.size());
    if (index != -1) {
      if (Trace.on && Log.isInfoEnabled()) Log.info("Index selected " + index);
      selectedElement = (ServerGroupElementInterface) lbList.get(index);
      Log.info("after LBHashing selected element is " + selectedElement);
      lbList.remove(index);
      domainsToTry.remove(selectedElement);
    }
    return selectedElement;
  }

  @Override
  protected ServerGroupElementInterface selectElement() {
    return selectElement(null);
  }

  @Override
  protected void setKey() {
    // Defined in its implementation class
  }

  public boolean isDsCall() {
    String mParam = null;
    if (request.getBody() != null) {
      mParam = request.getBody().toString();
      if (mParam != null) {
        String[] mParamArr = mParam.split(MPARAM_START_STRING);
        isDSCall = setDsFlag(mParamArr);
      }
    }
    return isDSCall;
  }

  private boolean setDsFlag(String[] mParamArr) {
    for (String s : mParamArr) {
      if (s.contains(MPARAM_DS_STRING)) {
        isDSCall = true;
        break;
      }
    }
    return isDSCall;
  }

  public final void initializeDomains() {
    if (Trace.on && Log.isDebugEnabled()) Log.debug("Entering initializeDomains()");
    ServerGroupInterface serverGroup = this.serverGroup;
    domainsToTry = new TreeSet();
    if (serverGroup == null) {
      if (Trace.on && Log.isEnabled(Level.WARN))
        Log.warn("Could not find server group " + serverGroupName);
      return;
    }
    for (Object o : serverGroup.getElements()) {
      ServerGroupElementInterface sge = (ServerGroupElementInterface) o;
      if (sge.isNextHop()) {
        domainsToTry.add(sge);
      } else {
        if (((ServerGroupElement) sge).isAvailable()) {
          domainsToTry.add(sge);
        }
      }
    }

    if (Trace.on && Log.isTraceEnabled()) Log.trace("Leaving initializeDomains()");
  }
}
