// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Provides for a non-blocking IO service where it uses the mutiplexed IO framework provided by the
 * nio package of J2SE 1.4. This class is a singleton and has exactly 1 Selector. When a key is
 * selected, it is passed off to another thread to be processed. Before it is passed, it is
 * registered for no interest ops, so that it will not be selected again while it is being
 * processed. The processing thread will set the interest ops appropriately after processing is
 * complete. Note that it was decided not to perform calls to connect asynchronously as it would do
 * nothing for speeding up the creation process.
 */

// TODO - we need a way to restart things

public final class DsSelector extends Thread {
  /** Used to set a selectable so that it will not be selected for any operations. */
  private static final int OP_NONE = 0;

  /** Maximum number of worker threads to allocate for processing the IO work queue. */
  private static int m_maxIOWorkThreads;

  /** Maximum length of the IO work queue. */
  private static int m_maxIOWorkQueue;

  /** Wire category logger. */
  static Logger cat = DsLog4j.wireCat;

  /** A work queue that grows without bound, for accepting connections and processing channels. */
  private static DsWorkQueue m_workQueue;

  /** The instance of the singleton. */
  private static DsSelector m_dsSelector;

  /** Set to <code>false</code> when you need the selector loop to exit. */
  private static boolean m_alive;

  /** The Java selector object. */
  private Selector m_selector;

  /** A list of things for the selector thread to do while no holding any locks. */
  private LinkedList m_invocations = new LinkedList(); // a list of invokations

  static {
    try {
      m_alive = true;
      m_dsSelector = new DsSelector();
      m_dsSelector.start();
      m_maxIOWorkThreads =
          DsConfigManager.getProperty(
              DsConfigManager.PROP_MAX_IO_WORK_THREADS,
              DsConfigManager.PROP_MAX_IO_WORK_THREADS_DEFAULT);
      m_maxIOWorkQueue =
          DsConfigManager.getProperty(
              DsConfigManager.PROP_MAX_IO_WORK_ENTRIES,
              DsConfigManager.PROP_MAX_IO_WORK_ENTRIES_DEFAULT);
      if (cat.isEnabled(Level.DEBUG)) {
        cat.debug(
            "Creating IO Work queue with max threads = "
                + m_maxIOWorkThreads
                + " and max queue entries = "
                + m_maxIOWorkQueue);
      }

      m_workQueue =
          new DsWorkQueue(DsWorkQueue.IO_WORK_QNAME, m_maxIOWorkQueue, m_maxIOWorkThreads);
      m_workQueue.setDiscardPolicy(DsWorkQueue.GROW_WITHOUT_BOUND);
      DsConfigManager.registerQueue((DsQueueInterface) m_workQueue);
    } catch (Exception e) {
      if (cat.isEnabled(Level.WARN)) {
        cat.warn("Exception starting the selector.", e);
      }
    }
  }

  /** Private constructor to avoid the instantiation of selector. */
  private DsSelector() throws IOException {
    super("SELECTOR");
    m_selector = SelectorProvider.provider().openSelector();
  }

  /**
   * Static method to get the singleton selector.
   *
   * @return the selector instance
   */
  public static DsSelector getInstance() {
    return m_dsSelector;
  }

  /**
   * Asynchronously registers the specified <code>selectable</code> to one of the available
   * selector. If the selector service is not started then it starts the selectors.
   *
   * @param selectable the selectable instance that is interested in being notified for any ready
   *     operation on its underlying channel.
   */
  public static void register(DsSelectable selectable) {
    m_dsSelector.invoke(new DsRegisterConn(selectable));
  }

  public void registerPingObject(DsSelectable selectable) {
    m_dsSelector.invoke(new DsRegisterConn(selectable));
  }

  /**
   * Asynchronously set the specified interest ops for the specified selection key.
   *
   * @param sk the selection key
   * @param interest the interest to register
   */
  public static void interestOps(SelectionKey sk, int interest) {
    m_dsSelector.invoke(new DsInterestOps(sk, interest));
  }

  /**
   * Asynchronously add the specified interest ops for the specified selection key.
   *
   * @param sk the selection key
   * @param interest the interest to add
   */
  public static void addInterestOps(SelectionKey sk, int interest) {
    m_dsSelector.invoke(new DsAddInterestOps(sk, interest));
  }

  /**
   * Asynchronously print debug information about this selector. Note that this could be a lot of
   * information, since it prints detail about each registered key.
   */
  public static void printDebug() {
    m_dsSelector.invoke(new DsSelectorDebug());
  }

  /**
   * Stops all the selectors in the selector pool and thus the selector service. It will just set
   * the flag and the selectors when come out the select call, will check for this flag and stops.
   */
  public static void stopAll() {
    m_alive = false;
  }

  /**
   * Get the underlying Java selector.
   *
   * @return the underlying Java selector
   */
  public Selector getSelector() {
    return m_selector;
  }

  /** Closes this selector. */
  private void close() {
    if (m_selector != null) {
      try {
        m_selector.close();
      } catch (Exception exc) {
        if (cat.isEnabled(Level.INFO)) {
          cat.info("Exception while closing the selector-[" + getName() + "]", exc);
        }
      }
    }
  }

  /** Starts this selector thread and go in select call to wait for any ready operations. */
  public void run() {
    Set readyKeys = null;
    Iterator iter = null;
    SelectionKey sk = null;
    DsUnitOfWork selectable = null;

    while (DsSelector.m_alive) {
      try {
        // Moved this inside the try just to be safe
        doInvocations();

        // Do an indefinite select().
        // If anything is ready to be read, then do read.
        int numSelected = m_selector.select();
        if (numSelected > 0) {
          readyKeys = m_selector.selectedKeys();
          iter = readyKeys.iterator();
          if (cat.isEnabled(Level.DEBUG)) {
            cat.debug(
                "[Selected Keys = "
                    + readyKeys.size()
                    + ", "
                    + "Total Keys = "
                    + m_selector.keys().size()
                    + "]");
          }

          while (iter.hasNext()) {
            sk = (SelectionKey) iter.next();
            iter.remove();

            if (!sk.isValid()) {
              // this key was cancelled, enqueue so its process method can
              // deal with closing it...
              // so we skip it here
              m_workQueue.nqueue((DsUnitOfWork) sk.attachment());
              continue;
            }

            selectable = (DsUnitOfWork) sk.attachment();
            if (selectable != null) {
              // make sure that this channel is not selected, until the
              // worker thread is done and re-registers interest in it
              // if(selectable instanceof DsTlsNBListener ) then dont remove interest
              if (selectable instanceof DsTlsNBListener) {
                DsTlsNBListener listener = (DsTlsNBListener) selectable;
                listener.accept(sk);

              }
              // Since selector keeps on notifying till the connection is accepted, its better
              // to accept(only TCP connection) in selector thread itself
              else {
                sk.interestOps(OP_NONE);
              }
              // pass the actualy I/O work off to another thread
              // NOTE - this queue needs to grow w/o bound or a connection will
              // hang if it gets dropped or we need to re-reg interest here

              // Queue will grow without bound, so there is never overflow - jsm
              m_workQueue.nqueue(selectable);
            }
          } // _while
        } // _if
      } catch (IOException io) {
        if (cat.isEnabled(Level.WARN)) {
          cat.warn("Exception while polling for the network activity: ", io);
        }
      } catch (ClosedSelectorException cs) {
        if (cat.isEnabled(Level.WARN)) {
          cat.warn("Exception while polling for the network activity: ", cs);
        }
      } catch (Exception e) {
        if (cat.isEnabled(Level.WARN)) {
          cat.warn("Exception while polling for the network activity: ", e);
        }
      }
    } // _while

    if (cat.isEnabled(Level.WARN)) {
      cat.warn("Closing the Selector ... [" + getName() + "]");
    }
    // REFACTOR
    //    SIPMBeanImpl.sendNotification(
    //        SIPMBean.THREAD_EXIT_ALARM,
    //        new String[] {"Selector Thread exiting , closing the selector " + getName()});
    // Close this selector
    close();
  } // ends run

  /** Request a call back. */
  public void invoke(Runnable r) {
    synchronized (m_invocations) {
      m_invocations.add(r); // add it to our request queue
    }
    m_selector.wakeup(); // break out of the select
  }

  // run the m_invocations in our thread, these probably set the interestOps,
  // or register channels
  // but they could do almost anything
  /** Execute all of the tasks on the invocation list. */
  private void doInvocations() {
    Runnable r;

    synchronized (m_invocations) {
      while (m_invocations.size() > 0) {
        r = (Runnable) m_invocations.removeFirst();
        r.run();
      }
    }
  }
} // ends DsSelector class definition

/** A Runnable that registers a connection. */
class DsRegisterConn implements Runnable {
  /** The connection to register. */
  private DsSelectable m_selectable;

  /**
   * Constructor that sets all data.
   *
   * @param selectable the connection to register
   */
  public DsRegisterConn(DsSelectable selectable) {
    m_selectable = selectable;
  }

  /** Register the connection. */
  public void run() {
    try {
      SelectableChannel sc = m_selectable.getChannel();
      SelectionKey sk =
          sc.register(
              DsSelector.getInstance().getSelector(), m_selectable.getOperation(), m_selectable);
      m_selectable.setSelectionKey(sk);
    } catch (ClosedChannelException cce) {
      if (DsSelector.cat.isEnabled(Level.WARN)) {
        DsSelector.cat.warn("Exception: this channel is closed: ", cce);
      }
    } catch (IllegalBlockingModeException ibme) {
      if (DsSelector.cat.isEnabled(Level.WARN)) {
        DsSelector.cat.warn("Exception: this channel is in blocking mode: ", ibme);
      }
    } catch (IllegalSelectorException ise) {
      if (DsSelector.cat.isEnabled(Level.WARN)) {
        DsSelector.cat.warn(
            "Exception: this channel was not created by the "
                + "same provider as the given selector: ",
            ise);
      }
    } catch (CancelledKeyException cke) {
      if (DsSelector.cat.isEnabled(Level.WARN)) {
        DsSelector.cat.warn(
            "Exception: this channel is currently registered "
                + "with the given selector but the corresponding "
                + "key has already been cancelled: ",
            cke);
      }
    } catch (IllegalArgumentException iae) {
      if (DsSelector.cat.isEnabled(Level.WARN)) {
        DsSelector.cat.warn(
            "Exception: a bit in the set does not correspond "
                + "to an operation that is supported by this channel, "
                + "that is, if set & ~validOps() != 0: ",
            iae);
      }
    } catch (NullPointerException npe) {
      // This can happen from a race condition where the connection is registered
      // and then quickly closed, sc above will be null
      if (DsSelector.cat.isEnabled(Level.WARN)) {
        DsSelector.cat.warn("Exception: null pointer, probably from a closed connection: ", npe);
      }
    } catch (Exception e) {
      // it is catastrophic if this method throws an exception, since this will cause
      // the selector thread to exit
      if (DsSelector.cat.isEnabled(Level.WARN)) {
        DsSelector.cat.warn("Exception: ", e);
      }
    }
  }
}

/** A Runnable that prints extensive debug information. */
class DsSelectorDebug implements Runnable {
  /** Print the debug info. */
  public void run() {
    System.out.println("OP_ACCEPT = " + SelectionKey.OP_ACCEPT);
    System.out.println("OP_CONNECT = " + SelectionKey.OP_CONNECT);
    System.out.println("OP_READ = " + SelectionKey.OP_READ);
    System.out.println("OP_WRITE  = " + SelectionKey.OP_WRITE);

    System.out.println();
    System.out.println("DsSelectorDebug - state:");
    Set keys = DsSelector.getInstance().getSelector().keys();
    SelectionKey key;
    Iterator iter = keys.iterator();
    int count = 1;
    while (iter.hasNext()) {
      key = (SelectionKey) iter.next();

      System.out.println("Key " + count++);
      System.out.println("  Interest OPs = " + key.interestOps());
      System.out.println("  Ready OPs = " + key.readyOps());
      if (key.isAcceptable()) {
        System.out.println("    isAcceptable() = true");
      }
      if (key.isReadable()) {
        System.out.println("    isReadable() = true");
      }
      if (key.isWritable()) {
        System.out.println("    isWritable() = true");
      }
      if (key.isConnectable()) {
        System.out.println("    isConnectable() = true");
      }
      System.out.println();
    }
  }
}

/** A Runnable that registers for interest on a selection key. */
class DsInterestOps implements Runnable {
  /** The selection key to register interest for. */
  private SelectionKey m_sk;
  /** The interest to register for. */
  private int m_interest;

  /**
   * Constructor that sets all data.
   *
   * @param sk the selection key to register interest for
   * @param interest the interest to register for
   */
  public DsInterestOps(SelectionKey sk, int interest) {
    m_sk = sk;
    m_interest = interest;
  }

  /** Register interest. */
  public void run() {
    try {
      m_sk.interestOps(m_interest);
    } catch (CancelledKeyException e) {
      // ignore this exception
      // might be worth an INFO log here
    } catch (NullPointerException e) {
      // TODO
      // ignore this exception - race condition that I need to fix, but should not
      // affect behavior until we do multiple threads - jsm
    } catch (Exception e) {
      if (DsSelector.cat.isEnabled(Level.WARN)) {
        DsSelector.cat.warn("Exception setting interestOps: ", e);
      }
    }
  }
}

/** A Runnable that registers for interest on a selection key. */
class DsAddInterestOps implements Runnable {
  /** The selection key to register interest for. */
  private SelectionKey m_sk;
  /** The interest to register for. */
  private int m_interest;

  /**
   * Constructor that sets all data.
   *
   * @param sk the selection key to register interest for
   * @param interest the interest to register for
   */
  public DsAddInterestOps(SelectionKey sk, int interest) {
    m_sk = sk;
    m_interest = interest;
  }

  /** Register interest. */
  public void run() {
    try {
      m_sk.interestOps(m_sk.interestOps() | m_interest);
    } catch (CancelledKeyException e) {
      // ignore this exception
      // might be worth an INFO log here
    } catch (NullPointerException e) {
      // TODO
      // ignore this exception - race condition that I need to fix, but should not
      // affect behavior until we do multiple threads - jsm
    } catch (Exception e) {
      if (DsSelector.cat.isEnabled(Level.WARN)) {
        DsSelector.cat.warn("Exception setting interestOps: ", e);
      }
    }
  }
}
