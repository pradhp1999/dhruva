// Copyright (c) 2003-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import java.util.*;
import java.util.Iterator;

/** @author kimle */
public class DsSipCallerPrefs {
  public static final DsByteString ATTENDANT = new DsByteString("attendant");
  public static final DsByteString AUDIO = new DsByteString("audio");
  public static final DsByteString AUTOMATA = new DsByteString("automata");
  public static final DsByteString CLASS = new DsByteString("class");
  public static final DsByteString DUPLEX = new DsByteString("duplex");
  public static final DsByteString DATA = new DsByteString("data");
  public static final DsByteString CONTROL = new DsByteString("control");
  public static final DsByteString MOBILITY = new DsByteString("mobility");
  public static final DsByteString DESCRIPTION = new DsByteString("description");
  public static final DsByteString EVENTS = new DsByteString("events");
  public static final DsByteString PRIORITY = new DsByteString("priority");
  public static final DsByteString METHODS = new DsByteString("methods");
  public static final DsByteString SCHEMES = new DsByteString("schemes");
  public static final DsByteString APPLICATION = new DsByteString("application");
  public static final DsByteString VIDEO = new DsByteString("video");
  public static final DsByteString MSGSERVER = new DsByteString("msgserver");
  public static final DsByteString LANGUAGES = new DsByteString("language");
  public static final DsByteString TYPE = new DsByteString("type");
  public static final DsByteString ISFOCUS = new DsByteString("isFocus");
  public static final DsByteString URIUSER = new DsByteString("uri-user");
  public static final DsByteString URIDOMAIN = new DsByteString("uri-domain");

  /** list of registered callepref features */
  public static final HashSet featureList = new HashSet();

  static {
    featureList.add(ATTENDANT);
    featureList.add(AUDIO);
    featureList.add(AUTOMATA);
    featureList.add(CLASS);
    featureList.add(DUPLEX);
    featureList.add(DATA);
    featureList.add(CONTROL);
    featureList.add(MOBILITY);
    featureList.add(DESCRIPTION);
    featureList.add(EVENTS);
    featureList.add(PRIORITY);
    featureList.add(METHODS);
    featureList.add(SCHEMES);
    featureList.add(APPLICATION);
    featureList.add(VIDEO);
    featureList.add(MSGSERVER);
    featureList.add(LANGUAGES);
    featureList.add(TYPE);
    featureList.add(ISFOCUS);
    featureList.add(URIUSER);
    featureList.add(URIDOMAIN);
  }

  /**
   * returns true if feature is a valid callerpref feature
   *
   * @param feature feature to test
   * @return true if feature is a valid callerpref feature
   */
  public static boolean isFeature(DsByteString feature) {

    if (featureList.contains(feature)) {
      return true;
    }

    DsByteString feature2 = feature.toLowerCase();
    if (featureList.contains(feature2)) {
      return true;
    }

    if (feature.length() != 0 && feature.charAt(0) == '+') {
      return true;
    }

    return false;
  }

  /**
   * Matches feature values in two DsByteString
   *
   * @param inA value of feature
   * @param inB value of feature
   * @return true if inA intersect intB
   */
  public static boolean matchFeatureValue(DsByteString inA, DsByteString inB) {
    if (inA == inB || inA.equals(inB)) {
      return true;
    }

    DsByteString a = inA.copy();
    DsByteString b = inB.copy();

    // Case-insensitive string matching
    if ((a.charAt(0) == '"' && a.charAt(1) == '<') || (b.charAt(0) == '"' && b.charAt(1) == '<')) {
      a = a.unquoted();
      b = b.unquoted();
      return a.equals(b);
    }

    if ((a.indexOf(',') != -1) || (b.indexOf(',') != -1)) {

      // List of tokens
      try {
        a = a.unquoted().toLowerCase();
        b = b.unquoted().toLowerCase();
        DsParameters aParams = new DsParameters(a.toString());
        DsParameters bParams = new DsParameters(b.toString());

        Iterator aItr = aParams.iterator();
        while (aItr.hasNext()) {
          DsByteString key = ((DsParameter) aItr.next()).getKey();
          if (key.charAt(0) != '!') {
            if (bParams.isPresent(key)) {
              return true;
            }
          } else {
            if (!bParams.isPresent(key.substring(1))) {
              return true;
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      a = a.unquoted();
      b = b.unquoted();
      if (a.charAt(0) != '!') {
        if (b.charAt(0) != '!') {
          return b.equalsIgnoreCase(a);
        } else {
          return !a.equalsIgnoreCase(b.substring(1));
        }
      } else {
        return !b.equalsIgnoreCase(a.substring(1));
      }
    }

    return false;
  }
}
