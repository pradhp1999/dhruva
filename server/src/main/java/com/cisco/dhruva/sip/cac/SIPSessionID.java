package com.cisco.dhruva.sip.cac;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.util.log.Trace;
import java.util.UUID;

public class SIPSessionID {
  String localUuid;
  String remoteUuid;
  private static final String SEMICOLON = ";";
  boolean uacStandardSessionIDImplementation = true;
  boolean uasStandardSessionIDImplementation = true;
  static final String nillSessionId = "00000000000000000000000000000000";
  private static final Trace Log = Trace.getTrace(SIPSessionID.class.getName());
  DsNetwork uacNetwork;

  public static String getNillsessionid() {
    return nillSessionId;
  }

  public boolean isUacStandardSessionIDImplementation() {
    return uacStandardSessionIDImplementation;
  }

  public void setUacStandardSessionIDImplementation(boolean uacStandardSessionIDImplementation) {
    this.uacStandardSessionIDImplementation = uacStandardSessionIDImplementation;
  }

  public boolean isUasStandardSessionIDImplementation() {
    return uasStandardSessionIDImplementation;
  }

  public void setUasStandardSessionIDImplementation(boolean uasStandardSessionIDImplementation) {
    this.uasStandardSessionIDImplementation = uasStandardSessionIDImplementation;
  }

  public String getLocalUuid() {
    return localUuid;
  }

  public void setLocalUuid(String localUuid) {
    this.localUuid = localUuid;
  }

  public String getRemoteUuid() {
    return remoteUuid;
  }

  public void setRemoteUuid(String remoteUuid) {
    this.remoteUuid = remoteUuid;
  }

  public DsNetwork getUacNetwork() {
    return uacNetwork;
  }

  public void setUacNetwork(DsNetwork uacNetwork) {
    this.uacNetwork = uacNetwork;
  }

  /**
   * Creates the local and remote uuid for the request This gets called only for the Invite and
   * Options messages (which needs to be routed) Using the standard java uuid package to generate
   * the uuid. Remote uuid is set to nilll uuid which is a 32 zeroes
   *
   * @param request
   */
  protected void createSessionUuid(DsSipRequest request) {
    try {
      DsSipHeaderInterface sessionIDHeader = request.getHeader(new DsByteString("Session-ID"));
      if (sessionIDHeader != null) {

        String sessionIdValue = sessionIDHeader.getValue().toString();
        if (sessionIdValue.contains("remote")) // New standard
        // Session-Id
        // implementation
        {
          localUuid = sessionIdValue.split(SEMICOLON)[0];
          remoteUuid = sessionIdValue.split(SEMICOLON)[1];
          remoteUuid = remoteUuid.split("remote=")[1];
          uacStandardSessionIDImplementation = true;
        } else // Pre-standard session id
        {
          localUuid = sessionIdValue.trim();
          remoteUuid = nillSessionId;
          uacStandardSessionIDImplementation = false;
        }

      } else {
        localUuid = generateUuid(request);
        remoteUuid = nillSessionId;
      }
    } catch (Exception e) {
      Log.error("Exception in SIPSession " + e);
    }
  }

  public static String generateUuid(DsSipMessage message) {

    String callId = message.getCallId().toString();
    String tag = "";
    DsByteString temp;
    if (message.isRequest()) {

      temp = message.getFromTag();
      if (temp != null) tag = temp.toString();

    } else if (message.isResponse()) {
      temp = message.getToTag();
      if (temp != null) tag = temp.toString();
    }

    String name = callId + tag;
    String uuid = UUID.nameUUIDFromBytes(name.getBytes()).toString();
    uuid = uuid.replace("-", "");
    return uuid;
  }
}
