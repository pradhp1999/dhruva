// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * A class for measuring performance. This class allows users to instrument their code. There is a
 * boolean called ON in this class. It is the global control mechanism for determining if
 * instrumentation is turned on. However, the design is such that the caller must check this value
 * before calling start() or stop(). It is not checked by this class. This was done in order to
 * avoid method call overhead in an application that does not have performance measuring turned on.
 * So, code may look like this:
 *
 * <p>
 *
 * <blockquote>
 *
 * String myPerfName = "Thing I am Measuring "; // note, this needs to pad to 32 chars int
 * myPerfIndex = DsPerf.addType(myPerfName); <br>
 * if (DsPerf.ON) DsPerf.start(myPerfIndex); // do the work that I am measuring here if (DsPerf.ON)
 * DsPerf.stop(myPerfIndex);
 *
 * </blockquote>
 *
 * <p>Users of this class should call reset() after the JVM has had enough time to warm up and JIT
 * compilation has taken place. Then, many thousands of iterations should be used in order to get
 * accurate results.
 */
public final class DsPerf {
  /**
   * Determines if stats are collected or not - NOTE that it is the Caller's responsibility to check
   * this value before calling start or stop. This value is <code>off</code> by default but can be
   * set to true by adding "-Dcom.dynamicsoft.DsLibs.DsSipUtil.DsPerf=on" to the command line
   * arguments of the JVM.
   */
  public static final boolean ON;

  /** Counter that increments with each new constant added. */
  static int counter = 0;
  /** Predefined measurement constant. */
  public static final int PARSE = counter++;
  /** Predefined measurement constant. */
  public static final int GET_BYTES = counter++;
  /** Predefined measurement constant. */
  public static final int TM = counter++;
  /** Predefined measurement constant. */
  public static final int PROC_RESP = counter++;
  /** Predefined measurement constant. */
  public static final int ON_RESP = counter++;
  /** Predefined measurement constant. */
  public static final int PROC_REQ = counter++;
  /** Predefined measurement constant. */
  public static final int REQUEST_INTERFACE = counter++;
  /** Predefined measurement constant. */
  public static final int PROC_ACK = counter++;
  /** Predefined measurement constant. */
  public static final int CB_ACK = counter++;
  /** Predefined measurement constant. */
  public static final int TRANS_KEY = counter++;
  /** Predefined measurement constant. */
  public static final int CLIENT_EXEC_INITIAL = counter++;
  /** Predefined measurement constant. */
  public static final int CLIENT_EXEC_CALLING = counter++;
  /** Predefined measurement constant. */
  public static final int CLIENT_EXEC_PROCEEDING = counter++;
  /** Predefined measurement constant. */
  // CAFFEINE 2.0 (EDCS-295391) Add PRACK Support
  public static final int CLIENT_EXEC_RELPROCEEDING = counter++;
  /** Predefined measurement constant. */
  public static final int CLIENT_EXEC_COMPLETED = counter++;
  /** Predefined measurement constant. */
  public static final int CLIENT_EXEC_TEMINATED = counter++;
  /** Predefined measurement constant. */
  public static final int CLIENT_EXEC_XINITIAL = counter++;
  /** Predefined measurement constant. */
  public static final int CLIENT_EXEC_XCOMPLETED = counter++;
  /** Predefined measurement constant. */
  public static final int CLIENT_EXEC_XTEMINATED = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_INITIAL = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_CALLING = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_PROCEEDING = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_COMPLETED = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_CONFIRMED = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_TEMINATED = counter++;
  // CAFFEINE 2.0 (EDCS-295391) Add PRACK Support
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_WAIT_PRACK = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_RELPROCEEDING = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_XINITIAL = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_XCOMPLETED = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_XCONFIRMED = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_XTEMINATED = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_COMPLETED_FROM_INITIAL = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_COMPLETED_FROM_CALLING = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_COMPLETED_FROM_PROCEEDING = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_TERMINATED_FROM_PROCEEDING = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_GET_VIA_CONNECTION = counter++;
  /** Predefined measurement constant. */
  public static final int TM_GET_REQUEST_CONNECTION_R = counter++;
  /** Predefined measurement constant. */
  public static final int TM_GET_CONNECTION_UB = counter++;
  /** Predefined measurement constant. */
  public static final int TM_GET_CONNECTION_M = counter++;
  /** Predefined measurement constant. */
  public static final int TM_GET_REQUEST_CONNECTION_MS = counter++;
  /** Predefined measurement constant. */
  public static final int TM_GET_SRV_CONNECTION_IiUS = counter++;
  /** Predefined measurement constant. */
  public static final int TM_GET_SRV_CONNECTION_IiSiiS = counter++;
  /** Predefined measurement constant. */
  public static final int TM_GET_SRV_CONNECTION_SiiS = counter++;
  /** Predefined measurement constant. */
  public static final int TM_GET_SRV_CONNECTION_US = counter++;

  // obsolete variables. There are here so that current LlApi2 can compile
  /** Predefined measurement constant. */
  public static final int CLIENT_EXEC_FINAL = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_EXEC_FINAL = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_FINAL_FROM_INITIAL = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_FINAL_FROM_CALLING = counter++;
  /** Predefined measurement constant. */
  public static final int SERVER_FINAL_FROM_PROCEEDING = counter++;

  /** Predefined measurement constant. */
  public static final int SOCKET_SEND = counter++;
  /** Predefined measurement constant. */
  public static final int CLONE_MSG = counter++;
  /** Predefined measurement constant. */
  public static final int CLONE_HEADER = counter++;
  /** Predefined measurement constant. */
  public static final int CLONE_NAME_ADDR = counter++;
  /** Predefined measurement constant. */
  public static final int CLONE_SIP_URL = counter++;
  /** Predefined measurement constant. */
  public static final int WRITE_PARAMS = counter++;
  /** Predefined measurement constant. */
  public static final int CLONE_PARAMS = counter++;
  /** Predefined measurement constant. */
  public static final int GET_NEXT = counter++;
  /** Predefined measurement constant. */
  public static final int NEW_RESPONSE = counter++;
  /** Predefined measurement constant. */
  public static final int NEW_RESPONSE_BYTES = counter++;

  // DsPerf DsSipObject categories
  /** Predefined measurement constant. */
  public static final int HEADER_WRITE = counter++;
  /** Predefined measurement constant. */
  public static final int HEADER_CLONE = counter++;
  /** Predefined measurement constant. */
  public static final int HEADER_CREATE = counter++;
  /** Predefined measurement constant. */
  public static final int HEADER_SET_PARAM = counter++;
  /** Predefined measurement constant. */
  public static final int HEADER_REM_PARAM = counter++;
  /** Predefined measurement constant. */
  public static final int HEADER_GET_PARAM = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_WRITE = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_CLONE = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_ADD_HEADER = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_ADD_HEADERS = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_REM_HEADER = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_REM_HEADERS = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_UPD_HEADER = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_UPD_HEADERS = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_GET_HEADER = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_GET_HEADERS = counter++;
  /** Predefined measurement constant. */
  public static final int MSG_GET_VHEADER = counter++;
  /** Predefined measurement constant. */
  public static final int HEADER_LIST_VALIDATE = counter++;
  /** Predefined measurement constant. */
  public static final int PARAMS_WRITE = counter++;
  /** Predefined measurement constant. */
  public static final int PARAMS_CLONE = counter++;
  /** Predefined measurement constant. */
  public static final int URI_WRITE = counter++;
  /** Predefined measurement constant. */
  public static final int URI_CLONE = counter++;

  // hack in max size so we do not have to dynamically grow when users add types
  /** Maximum array size. */
  static final int MAX = 768;

  /** The mapping of ints to string names. */
  static String NAMES[] = new String[MAX];

  /** The global results for all threads. */
  private static Results results = new Results();

  /** The local results for this thread. */
  private static ThreadLocal tl = new ResultsInitializer();

  static {
    ON = DsConfigManager.getProperty(DsConfigManager.PROP_PERF, DsConfigManager.PROP_PERF_DEFAULT);

    NAMES[PARSE] = "Message Parser                 ";
    NAMES[GET_BYTES] = "Message Serialization          ";
    NAMES[TM] = "\nTransaction Mgr                ";
    NAMES[PROC_RESP] = " TM Process Resp               ";
    NAMES[ON_RESP] = "  TM Proc Resp onResponse      ";
    NAMES[PROC_REQ] = " TM Process Request            ";
    NAMES[REQUEST_INTERFACE] = "  TM Request Interface         ";
    NAMES[PROC_ACK] = " TM Process ACK                ";
    NAMES[CB_ACK] = "  TM ACK CB                    ";
    NAMES[TRANS_KEY] = " TM Build Transaction Key      ";
    NAMES[CLIENT_EXEC_INITIAL] = "\n TM CT Exec Initial            ";
    NAMES[CLIENT_EXEC_CALLING] = " TM CT Exec Calling            ";
    NAMES[CLIENT_EXEC_PROCEEDING] = " TM CT Exec Proceeding         ";
    // CAFFEINE 2.0 (EDCS-295391) Add PRACK Support
    NAMES[CLIENT_EXEC_RELPROCEEDING] = " TM CT Exec Reliable Proceeding         ";
    NAMES[CLIENT_EXEC_COMPLETED] = " TM CT Exec Completed          ";
    NAMES[CLIENT_EXEC_TEMINATED] = " TM CT Exec Terminated         ";
    NAMES[CLIENT_EXEC_XINITIAL] = "\n TM CT Exec XInitial           ";
    NAMES[CLIENT_EXEC_XCOMPLETED] = " TM CT Exec XCompleted         ";
    NAMES[CLIENT_EXEC_XTEMINATED] = " TM CT Exec XTerminated        ";
    NAMES[SERVER_EXEC_INITIAL] = "\n TM ST Exec Initial            ";
    NAMES[SERVER_EXEC_CALLING] = " TM ST Exec Calling            ";
    NAMES[SERVER_EXEC_PROCEEDING] = " TM ST Exec Proceeding         ";
    NAMES[SERVER_EXEC_COMPLETED] = " TM ST Exec Completed          ";
    NAMES[SERVER_EXEC_CONFIRMED] = " TM ST Exec Confirmed          ";
    NAMES[SERVER_EXEC_TEMINATED] = " TM ST Exec Terminated         ";
    // CAFFEINE 2.0 (EDCS-295391) Add PRACK Support
    NAMES[SERVER_EXEC_WAIT_PRACK] = " TM ST Exec Wait PRACK         ";
    NAMES[SERVER_EXEC_RELPROCEEDING] = " TM ST Exec Reliable Proceeding         ";
    NAMES[SERVER_EXEC_XINITIAL] = "\n TM ST Exec XInitial           ";
    NAMES[SERVER_EXEC_XCOMPLETED] = " TM ST Exec XCompleted         ";
    NAMES[SERVER_EXEC_XCONFIRMED] = " TM ST Exec XConfirmed         ";
    NAMES[SERVER_EXEC_XTEMINATED] = " TM ST Exec XTerminated        ";
    NAMES[SERVER_COMPLETED_FROM_INITIAL] = " TM ST Completed From Initial  ";
    NAMES[SERVER_COMPLETED_FROM_CALLING] = " TM ST Completed From Calling  ";
    NAMES[SERVER_COMPLETED_FROM_PROCEEDING] = " TM ST Completed From Proc     ";
    NAMES[SERVER_TERMINATED_FROM_PROCEEDING] = " TM ST Terminated From Proc    ";
    NAMES[SERVER_GET_VIA_CONNECTION] = " TM ST GetViaConnection        ";
    NAMES[TM_GET_REQUEST_CONNECTION_R] = " TM GetRequestConnection_R     ";
    NAMES[TM_GET_REQUEST_CONNECTION_MS] = " TM GetRequestConnection_MS    ";
    NAMES[TM_GET_SRV_CONNECTION_IiUS] = "    TM GetSRVConnection_IiUS   ";
    NAMES[TM_GET_SRV_CONNECTION_IiSiiS] = "    TM GetSRVConnection_IiSiiS ";
    NAMES[TM_GET_SRV_CONNECTION_SiiS] = "    TM GetSRVConnection_SiiS   ";
    NAMES[TM_GET_SRV_CONNECTION_US] = "    TM GetSRVConnection_US     ";
    NAMES[TM_GET_CONNECTION_UB] = "    TM GetConnection_UB        ";
    NAMES[TM_GET_CONNECTION_M] = "    TM GetConnection_M         ";
    NAMES[SOCKET_SEND] = "\nSocket Send                    ";
    NAMES[CLONE_MSG] = "Clone Msg                      ";
    NAMES[CLONE_HEADER] = "Clone Header                   ";
    NAMES[CLONE_NAME_ADDR] = "Clone Name Address             ";
    NAMES[CLONE_SIP_URL] = "Clone SIP URL                  ";
    NAMES[WRITE_PARAMS] = "Clone DsParameters             ";
    NAMES[CLONE_PARAMS] = "Write DsParameters             ";
    NAMES[GET_NEXT] = "Header - getNext()             ";
    NAMES[NEW_RESPONSE] = "New Response                   ";
    NAMES[NEW_RESPONSE_BYTES] = "New Response From Bytes        ";

    NAMES[HEADER_WRITE] = "Header Write                   ";
    NAMES[HEADER_CLONE] = "Header Clone                   ";
    NAMES[HEADER_CREATE] = "Header Create                  ";
    NAMES[HEADER_SET_PARAM] = "Header Set Parameter           ";
    NAMES[HEADER_GET_PARAM] = "Header Get Parameter           ";
    NAMES[HEADER_REM_PARAM] = "Header Remove Parameter        ";
    NAMES[MSG_WRITE] = "Message Write                  ";
    NAMES[MSG_CLONE] = "Message Clone                  ";
    NAMES[MSG_ADD_HEADER] = "Message Add Header             ";
    NAMES[MSG_ADD_HEADERS] = "Message Add Headers            ";
    NAMES[MSG_GET_HEADER] = "Message Get Header             ";
    NAMES[MSG_GET_HEADERS] = "Message Get Headers            ";
    NAMES[MSG_GET_VHEADER] = "Message Get Validated Header   ";
    NAMES[MSG_REM_HEADER] = "Message Remove Header          ";
    NAMES[MSG_REM_HEADERS] = "Message Remove Headers         ";
    NAMES[MSG_UPD_HEADER] = "Message Update Header          ";
    NAMES[MSG_UPD_HEADERS] = "Message Update Headers         ";
    NAMES[HEADER_LIST_VALIDATE] = "Header List Validate           ";

    NAMES[URI_WRITE] = "URI Write                      ";
    NAMES[URI_CLONE] = "URI Clone                      ";
    NAMES[PARAMS_WRITE] = "Parameters Write               ";
    NAMES[PARAMS_CLONE] = "Parameters Clone               ";

    // obsolete variables. There are here so that current LlApi2 can compile
    NAMES[CLIENT_EXEC_FINAL] = " TM CT Exec Final              ";
    NAMES[SERVER_EXEC_FINAL] = " TM ST Exec Final              ";
    NAMES[SERVER_FINAL_FROM_INITIAL] = " TM ST Final From Initial      ";
    NAMES[SERVER_FINAL_FROM_CALLING] = " TM ST Final From Calling      ";
    NAMES[SERVER_FINAL_FROM_PROCEEDING] = " TM ST Final From Proceeding   ";
  }

  /** The private constructor. */
  private DsPerf() {}

  /**
   * Register a new type to instrument with.
   *
   * @param name a 32 character String, use spaces to indent from the left and spacest to pad the
   *     right for 32 characters. If this is not exactly 32 characters, then the String returned
   *     from getResults() will not be properly formatted. You may use a 33 character String if the
   *     leading character is LF. This will start a new line for the results that follow.
   * @return the integer to pass to start and stop for this name
   */
  public static synchronized int addType(String name) {
    int retval = counter++;

    NAMES[retval] = name;

    return retval;
  }

  /**
   * Start a timer for the given name.
   *
   * @param name the integer name to start a timer for
   */
  public static void start(int name) {
    getTl().start(name, System.currentTimeMillis());
  }

  /**
   * Stop a timer for the given name, and get the delta time since start() was called.
   *
   * @param name the integer name to stop the timer for
   * @return the delta time since start was called for this name
   */
  public static int stop(int name) {
    long start = getTl().stop(name);
    long end = System.currentTimeMillis();
    int time = (int) (end - start);

    results.incTime(name, time);

    return time;
  }

  /**
   * Get the results so far in as a table formatted String.
   *
   * @return a formatted String of the results so far
   */
  public static String getResults() {
    return results.toString();
  }

  /** Set all values to 0. This is useful for getting around JVM warmup times. */
  public static void reset() {
    results.reset();
  }

  /**
   * Gets the thread local results for this thread.
   *
   * @return the thread local results for this thread.
   */
  private static Results getTl() {
    return (Results) tl.get();
  }

  /**
   * Returns the name of a type from the int.
   *
   * @param name the index of the name.
   * @return the name of the type from the int.
   */
  public static String getName(int name) {
    return NAMES[name];
  }

  // methods added to act as a StatCollector

  /**
   * Returns the number of registered elements.
   *
   * @return the number of registered elements.
   */
  public static int getNumberOfElements() {
    return counter;
  }

  /**
   * Responds to a query for statistics, by event type.
   *
   * @param name the type of event
   * @return a DsPerfData element, for the name requested
   */
  public static DsPerfData getStats(int name) {
    return results.getStats(name);
  }

  /**
   * Responds to a query for all statistics.
   *
   * @return an array of DsPerfData elements, one for each name
   */
  public static DsPerfData[] getStats() {
    return results.getStats();
  }
}

/** Internal class that holds a set of results. */
final class Results {
  /** The array of times. */
  private long times[] = new long[DsPerf.MAX];
  /** The array of counts. */
  private int counts[] = new int[DsPerf.MAX];

  /** Default constructor. */
  public Results() {}

  /** Resets the arrays. */
  public synchronized void reset() {
    for (int i = 0; i < DsPerf.counter; i++) {
      times[i] = 0;
      counts[i] = 0;
    }
  }

  // for final results
  /**
   * Increments the time by time for name.
   *
   * @param name the index to increment.
   * @param time the amount to increment.
   */
  public synchronized void incTime(int name, int time) {
    times[name] += time;
    counts[name]++;
  }

  // for keeping count
  /**
   * Sets the time when an event began.
   *
   * @param name the index to set.
   * @param start the time that the event began.
   */
  public void start(int name, long start) {
    times[name] = start;
  }

  /**
   * Sets the time when an event ended.
   *
   * @param name the index to set.
   * @return the elapsed time since start() was called.
   */
  public long stop(int name) {
    return times[name];
  }

  /**
   * Forms a string that represents this object.
   *
   * @return a string that represents this object.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(1024);
    sb.append("Name                               Time (ms)      Count Average (ms)\n");
    sb.append("=============================== ============ ========== ============\n");

    synchronized (this) {
      for (int i = 0; i < DsPerf.counter; i++) {
        sb.append(DsPerf.NAMES[i]);
        sb.append(' ');
        sb.append(pad(times[i], 12));
        sb.append(' ');
        sb.append(pad(counts[i], 10));
        sb.append(' ');
        sb.append(pad((double) times[i] / (double) counts[i], 12));
        sb.append('\n');
      }
    }

    return sb.toString();
  }

  /**
   * Pads a double value to width and truncates to 3 decimal places.
   *
   * @param time the value to pad.
   * @param width the size to pad to.
   * @return the padded string.
   */
  private String pad(double time, int width) {
    // 3 decimal places
    time = ((double) ((long) (time * 1000))) / 1000.0;

    String tStr = String.valueOf(time);
    int dotIndex = tStr.indexOf('.');
    if (dotIndex == -1) {
      tStr = tStr + ".000";
    } else if (tStr.length() - dotIndex < 4) {
      int cnt = 4 - (tStr.length() - dotIndex);

      for (int i = 0; i < cnt; i++) {
        tStr = tStr + '0';
      }
    } else if (tStr.length() - dotIndex > 4) {
      int cnt = (tStr.length() - dotIndex) - 4;

      tStr = tStr.substring(0, tStr.length() - cnt);
    }

    return pad(tStr, width);
  }

  /**
   * Pads a long value to width.
   *
   * @param time the value to pad.
   * @param width the size to pad to.
   * @return the padded string.
   */
  private String pad(long time, int width) {
    String tStr = String.valueOf(time);

    return pad(tStr, width);
  }

  /**
   * Pads a string value to width.
   *
   * @param tStr the value to pad.
   * @param width the size to pad to.
   * @return the padded string.
   */
  public String pad(String tStr, int width) {
    int padCnt = width - tStr.length();

    StringBuffer sb = new StringBuffer(width);

    for (int i = 0; i < padCnt; i++) {
      sb.append(' ');
    }

    sb.append(tStr);

    return sb.toString();
  }

  /**
   * Returns a single data item for the index requested.
   *
   * @param name the index requested.
   * @return a single data item for the index requested.
   */
  public synchronized DsPerfData getStats(int name) {
    return new DsPerfData(counts[name], times[name], DsPerf.NAMES[name]);
  }

  /**
   * Returns all data items from this object.
   *
   * @return all data items from this object.
   */
  public synchronized DsPerfData[] getStats() {
    DsPerfData[] array = new DsPerfData[DsPerf.counter];

    for (int i = 0; i < DsPerf.counter; i++) {
      array[i] = new DsPerfData(counts[i], times[i], DsPerf.NAMES[i]);
    }
    return array;
  }
}

/** A ThreadLocal that initializes the results object. */
final class ResultsInitializer extends ThreadLocal {
  protected Object initialValue() {
    return new Results();
  }
}
