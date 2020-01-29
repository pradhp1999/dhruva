// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import java.io.*;
import java.util.*;

/**
 * A helper class that tries to locate name servers and the search path to be appended to
 * unqualified names. Currently, this works if either the appropriate properties are set, the OS has
 * a unix-like /etc/resolv.conf, or the system is Windows based with ipconfig or winipcfg.
 */
public class DsDnsLocator {
  /**
   * Returns all located servers.
   *
   * @return all located servers.
   */
  public static String[] servers() {
    probe();
    return server;
  }

  /**
   * Returns the first located server.
   *
   * @return the first located server.
   */
  static String server() {
    String[] array = servers();
    if (array == null) return null;
    else return array[0];
  }

  /**
   * Returns all entries in the located search path.
   *
   * @return all entries in the located search path.
   */
  public static String[] searchPath() {
    probe();
    return search;
  }

  /**
   * Looks in /etc/resolv.conf to find servers and a search path. "nameserver" lines specify
   * servers. "domain" and "search" lines define the search path.
   */
  private static void findUnix() {
    InputStream in = null;
    try {
      in = new FileInputStream("/etc/resolv.conf");
    } catch (FileNotFoundException e) {
      return;
    }
    InputStreamReader isr = new InputStreamReader(in);
    BufferedReader br = new BufferedReader(isr);
    Vector vserver = null;
    Vector vsearch = null;
    try {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("nameserver")) {
          if (vserver == null) vserver = new Vector();
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken(); /* skip nameserver */
          vserver.addElement(st.nextToken());
        } else if (line.startsWith("domain")) {
          if (vsearch == null) vsearch = new Vector();
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken(); /* skip domain */
          String s = st.nextToken();
          if (!vsearch.contains(s)) vsearch.addElement(s);
        } else if (line.startsWith("search")) {
          if (vsearch == null) vsearch = new Vector();
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken(); /* skip domain */
          String s;
          while (st.hasMoreTokens()) {
            s = st.nextToken();
            if (!vsearch.contains(s)) vsearch.addElement(s);
          }
        }
      }
    } catch (IOException e) {
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }

    if (server == null && vserver != null) {
      server = new String[vserver.size()];
      for (int i = 0; i < vserver.size(); i++) server[i] = (String) vserver.elementAt(i);
    }
    if (search == null && vsearch != null) {
      search = new String[vsearch.size()];
      for (int i = 0; i < vsearch.size(); i++) search[i] = (String) vsearch.elementAt(i);
    }
  }

  /**
   * Parses the output of winipcfg or ipconfig.
   *
   * @param in the input stream to parse from
   */
  private static void findWin(InputStream in) {
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    try {
      Vector vserver = null;
      String line = null;
      boolean readingServers = false;
      while ((line = br.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(line);
        if (!st.hasMoreTokens()) {
          readingServers = false;
          continue;
        }
        String s = st.nextToken();
        if (line.indexOf(":") != -1) readingServers = false;

        // --                if (line.indexOf("Host Name") != -1)
        if (line.indexOf("DNS Suffix") != -1) {
          while (st.hasMoreTokens()) s = st.nextToken();
          search = new String[1];
          search[0] = s;
        } else if (readingServers || line.indexOf("DNS Servers") != -1) {
          while (st.hasMoreTokens()) s = st.nextToken();
          if (s.equals(":")) continue;
          if (vserver == null) vserver = new Vector();
          vserver.addElement(s);
          readingServers = true;
        }
      }

      if (server == null && vserver != null) {
        server = new String[vserver.size()];
        for (int i = 0; i < vserver.size(); i++) server[i] = (String) vserver.elementAt(i);
      }
    } catch (IOException e) {
    } finally {
      try {
        br.close();
      } catch (IOException e) {
      }
    }
    return;
  }

  /** Calls winipcfg and parses the result to find servers and a search path. */
  private static void find95() {
    String s = "winipcfg.out";
    try {
      Process p;
      p = Runtime.getRuntime().exec("winipcfg /all /batch " + s);
      p.waitFor();
      File f = new File(s);
      findWin(new FileInputStream(f));
      new File(s).delete();
    } catch (Exception e) {
      return;
    }
  }

  /** Calls ipconfig and parses the result to find servers and a search path. */
  private static void findNT() {
    try {
      Process p;
      p = Runtime.getRuntime().exec("ipconfig /all");
      findWin(p.getInputStream());
      p.destroy();
    } catch (Exception e) {
      return;
    }
  }

  private static synchronized void probe() {
    if (probed) return;
    probed = true;
    if (server == null || search == null) {
      String OS = System.getProperty("os.name");
      if (OS.indexOf("Windows") != -1) {
        if ((OS.indexOf("NT") != -1) || (OS.indexOf("2000") != -1)) findNT();
        else find95();
      } else findUnix();
    }
    if (search == null) search = new String[1];
    else {
      String[] oldsearch = search;
      search = new String[oldsearch.length + 1];
      System.arraycopy(oldsearch, 0, search, 0, oldsearch.length);
    }
    search[search.length - 1] = ROOT;
  }

  private DsDnsLocator() {}

  private static final String ROOT = ".";
  private static String[] server;
  private static String[] search;
  private static boolean probed;
}
