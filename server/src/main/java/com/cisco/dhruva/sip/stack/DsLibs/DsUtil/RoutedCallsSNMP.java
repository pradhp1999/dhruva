/**
 * ***************************************************************** Copyright (c) 2015 by Cisco
 * Systems, Inc., 170 West Tasman Drive, San Jose, California, 95134, U.S.A. All rights reserved.
 * *****************************************************************
 */
package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class RoutedCallsSNMP {

  private static AtomicInteger routedCallsPerSec;
  private static Timer timer = null;
  private static int[] oneMinCPS;
  private static int oneMinCPSIndex;

  public static void incrementCallsRouted() {
    routedCallsPerSec.incrementAndGet();
  }

  static {
    RoutedCallsSNMP.init();
  }

  public static void init() {
    oneMinCPS = new int[60];
    routedCallsPerSec = new AtomicInteger();
    routedCallsPerSec.set(0);
    timer = new Timer();
    timer.scheduleAtFixedRate(new RefreshTask(), 1000, 1000);
  }

  public static int getCallsRouted() {

    if (oneMinCPSIndex == 0) return oneMinCPS[59];
    else return oneMinCPS[oneMinCPSIndex - 1];
  }

  public static float getCallsRoutedAvg() {
    int sum = 0;
    float avg;

    for (int i = 0; i < 60; i++) {
      sum = sum + oneMinCPS[i];
    }

    avg = sum / 60;

    return avg;
  }

  public static int getCallsRoutedMax() {
    int max = 0;
    for (int i = 0; i < 60; i++) {
      if (oneMinCPS[i] > max) max = oneMinCPS[i];
    }
    return max;
  }

  static class RefreshTask extends TimerTask {
    public void run() {

      oneMinCPS[oneMinCPSIndex] = routedCallsPerSec.getAndSet(0);

      if (oneMinCPSIndex == 59) oneMinCPSIndex = 0;
      else oneMinCPSIndex++;
    }
  }
}
