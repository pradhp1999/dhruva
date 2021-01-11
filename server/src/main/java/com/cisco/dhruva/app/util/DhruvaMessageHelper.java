package com.cisco.dhruva.app.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DhruvaMessageHelper {

  public static String getUserPortion(String reqUri) {

    Pattern pattern = Pattern.compile("((sip:)(.+)@(.+))");
    Matcher matcher = pattern.matcher(reqUri);
    String user = null;
    if (matcher.find()) {
      user = matcher.group(3);
    }
    return user;
  }

  public static String getHostPortion(String reqUri) {
    Pattern pattern = Pattern.compile("((sip:)(.+)@(.+))");
    Matcher matcher = pattern.matcher(reqUri);
    String host = null;
    if (matcher.find()) {
      host = matcher.group(4);
      String s[] = host.split(";");
      host = s[0];
    }
    return host;
  }

  public static String normalizeReqUri(String reqUri) {
    String normalizeReqUri = null;
    Pattern pattern = Pattern.compile("((.+)\\.(.+)@(?i)((dmz.)?webex.com))");
    Matcher matcher = pattern.matcher(reqUri);

    if (matcher.find()) {
      normalizeReqUri = matcher.group(2) + "@" + matcher.group(3) + "." + matcher.group(4);
    }
    return normalizeReqUri;
  }
}
