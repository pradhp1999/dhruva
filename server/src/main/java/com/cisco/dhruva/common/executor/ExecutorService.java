package com.cisco.dhruva.common.executor;


import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * All the servers and services requiring executors should fetch it from this class
 * This maintains mapping to the executor service to consumer
 * Provides a layer of abstraction for tracking , debugging
 */

public class ExecutorService {

  private Logger LOG = DhruvaLoggerFactory.getLogger(ExecutorService.class);

  private final ConcurrentMap<String, Executor> executorMap = new ConcurrentHashMap<>();

  private final String servername;

  /**
   * Default constructor
   * @param servername
   */
  public ExecutorService(final String servername) {
    this.servername = servername;
  }

  /**
   * provide the thread name that you want to assign
   * @param name Name of thread
   * @param maxThreads Accepts the max threads to be configured as input
   */

  public void startExecutorService(String name, int maxThreads) {
    Executor e =
        this.executorMap.compute(
            name,
            (key, value) -> {
              if (value != null) {
                throw new RuntimeException(
                    "An executor service with the name " + key + " is already running!");
              }
              return new Executor(key, maxThreads);
            });

    LOG.info(
        "Starting executor service name={}, corePoolSize={}, maxPoolSize={}",
        name,
        e.threadPoolExecutor.getCorePoolSize(),
        e.threadPoolExecutor.getMaximumPoolSize());
  }

  boolean isExecutorServiceRunning(String name) {
    return this.executorMap.containsKey(name);
  }

  public void shutdown() {
    for (Entry<String, Executor> entry : this.executorMap.entrySet()) {
      List<Runnable> wasRunning = entry.getValue().threadPoolExecutor.shutdownNow();
      if (!wasRunning.isEmpty()) {
        LOG.info(entry.getValue() + " had " + wasRunning + " on shutdown");
      }
    }
    this.executorMap.clear();
  }

  /**
   * Use this API to fetch the executor based on the type
   * @param type Executor type for which the executor thread pool was created
   * @return Executor object that was already created
   */
  Executor getExecutor(final ExecutorType type) {
    return getExecutor(type.getExecutorName(this.servername));
  }


  Executor getExecutor(String name) {
    Executor executor = this.executorMap.get(name);
    return executor;
  }

  public ThreadPoolExecutor getExecutorThreadPool(final ExecutorType type) {
    return getExecutor(type).getThreadPoolExecutor();
  }

  public void startExecutorService(final ExecutorType type, final int maxThreads) {
    String name = type.getExecutorName(this.servername);
    if (isExecutorServiceRunning(name)) {
      LOG.info("Executor service {} already running on {}", this, this.servername);
      return;
    }
    startExecutorService(name, maxThreads);
  }

  /**
   * Dhruva Executor
   * Wraps the native executor
   */
  static class Executor {
    static final long keepAliveTimeInMillis = 1000;
    final CustomThreadPoolExecutor threadPoolExecutor;
    final BlockingQueue<Runnable> q = new LinkedBlockingQueue<>();
    private final String name;
    private static final AtomicLong seqids = new AtomicLong(0);
    private final long id;

    protected Executor(String name, int maxThreads) {
      this.id = seqids.incrementAndGet();
      this.name = name;

      this.threadPoolExecutor =
          new CustomThreadPoolExecutor(
              maxThreads, maxThreads, keepAliveTimeInMillis, TimeUnit.MILLISECONDS, q);

      ThreadFactoryBuilder tfb = new ThreadFactoryBuilder();
      tfb.setNameFormat(this.name + "-%d");
      tfb.setDaemon(true);
      this.threadPoolExecutor.setThreadFactory(tfb.build());
    }

    CustomThreadPoolExecutor getThreadPoolExecutor() {
      return threadPoolExecutor;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "-" + id + "-" + name;
    }

    static class CustomThreadPoolExecutor extends ThreadPoolExecutor {
      private ConcurrentMap<Thread, Runnable> running = Maps.newConcurrentMap();

      public CustomThreadPoolExecutor(
          int corePoolSize,
          int maximumPoolSize,
          long keepAliveTime,
          TimeUnit unit,
          BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
      }

      /**
       * Middleware for keeping track of tasks
       * Can customize in future based on needs
       * @param r
       * @param t
       */
      @Override
      protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        running.remove(Thread.currentThread());
      }

      /**
       * Pre execution call
       * @param t
       * @param r
       */
      @Override
      protected void beforeExecute(Thread t, Runnable r) {
        Runnable oldPut = running.put(t, r);
        assert oldPut == null : "inconsistency for thread " + t;
        super.beforeExecute(t, r);
      }

      /**
       * Fetch the map holding the runnable job and associated thread
       * @return
       */
      public ConcurrentMap<Thread, Runnable> getRunningTasks() {
        return running;
      }
    }
  }
}
