package com.cisco.dhruva.app.util.predicates;

public class RegexCallFlows {

  public static final String UserPortionDialInCMRStandard = "^(\\d{9})$|^[a-zA-Z0-9_\\-.]{1,64}$";
  public static final String HostPortionDialInCMRStandard =
      "^([a-zA-Z0-9-]{1,64})\\.(?i)((dmz.)?webex.com)$";
  public static final String UserPortionDialInCMRVanity =
      "^([a-zA-Z0-9_\\-]{1,64}+[.]{1})+([A-Za-z0-9_-]+){1}$";
  public static final String UserPortionDialInCMRShortUri = "^[0-9]{8,11}$|^(?i)meet$";
  public static final String HostPortionDialInCMRVanityShortUri = "^(?i)(dmz.)?webex.com$";

  public static final String HostPortionDialInCalling =
      "^(?i).*cisco.calls.webex.com$|^(?i).*cisco.rooms.webex.com$|^(?i).*cisco.call.ciscospark.com$|^(?i).*calls.cisco.webex.com$|(?i).*ciscospark.com$|(?i).*wbx2.com$|(?i).*calls.webex.com$|(?i).*rooms.webex.com$|(?i).*usgov.webex.com$";
}
